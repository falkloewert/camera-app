package net.sourceforge.opencamera.preview;

import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.sourceforge.opencamera.MyDebug;

/** MediaCodec + MediaMuxer based video recorder, replacing MediaRecorder for the Camera2,
 *  non-high-speed MP4 path.
 *
 *  The video encoder is fed through a persistent input surface (API 23+), so the same surface can
 *  later be re-targeted by a new camera capture session (Feature 5: switching camera while
 *  recording) without stopping the encoder or muxer. Audio is captured independently through
 *  AudioRecord and keeps running while the camera is being switched.
 *
 *  ponytail: only the Camera2, non-high-speed, MP4 path is supported; Camera1 and high-speed keep
 *  MediaRecorder (see Preview.startVideoRecording). Pause/resume timing and PTS drift need device
 *  verification.
 */
public class MediaCodecVideoRecorder {

    private static final String TAG = "MediaCodecVideoRecorder";

    /** Source of a video surface for the capture session (replaces MediaRecorder.getSurface()). */
    public interface VideoSurfaceSource {
        Surface getVideoSurface();
    }

    /** Mirrors Preview.onVideoInfo/onVideoError; "what" uses MediaRecorder.MEDIA_RECORDER_INFO_*. */
    public interface Callback {
        void onInfo(int what, int extra);
        void onError(int what, int extra);
    }

    private MediaCodec video_encoder;
    private MediaCodec audio_encoder;
    private MediaMuxer muxer;
    private Surface input_surface; // persistent input surface, survives camera switches
    private AudioRecord audio_record;

    private Thread audio_thread;
    private Thread drain_thread;

    private volatile boolean running;
    private volatile boolean stopped;
    private volatile boolean paused;
    private volatile boolean released;

    private boolean record_audio;

    private int video_track = -1;
    private int audio_track = -1;
    private boolean video_format_ready;
    private volatile boolean audio_format_ready;
    private volatile boolean audio_failed;
    private boolean muxer_started;

    private int audio_buffer_size;

    private volatile long bytes_written;

    // audio PTS is derived from System.nanoTime() relative to recording_base_nanos, then aligned
    // to the video clock by audio_pts_offset_us (set when the first video frame arrives).
    private volatile long recording_base_nanos;
    private volatile long audio_pts_offset_us;
    // correction for the camera-switch PTS discontinuity (Feature 5, step 4) and for pause gaps.
    private volatile long video_pts_offset_us;
    private volatile boolean first_video_frame_seen;

    private long paused_at_nanos;

    private long max_duration_ms;    // 0 = unlimited
    private long max_filesize_bytes; // 0 = unlimited
    private volatile boolean max_duration_reported;
    private volatile boolean max_filesize_reported;
    private volatile boolean failure_reported;

    private Callback callback;

    public Surface getVideoSurface() {
        return input_surface;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public long getBytesWritten() {
        return bytes_written;
    }

    /** Starts the recorder. Encoders and muxer are started before the caller creates the capture
     *  session, so {@link #getVideoSurface()} is already valid on return. The first sample is only
     *  written once both tracks have been added and the muxer has started.
     */
    public void start(VideoProfile profile, String file_path, FileDescriptor file_descriptor,
                      int orientation_hint, Location location,
                      long max_duration_ms, long max_filesize_bytes) throws IOException {
        if( MyDebug.LOG )
            Log.d(TAG, "start");
        this.record_audio = profile.record_audio;
        this.max_duration_ms = max_duration_ms;
        this.max_filesize_bytes = max_filesize_bytes;
        this.recording_base_nanos = System.nanoTime();
        this.released = false;
        this.stopped = false;
        this.running = false;
        this.paused = false;
        this.audio_failed = false;

        // video encoder fed through a persistent input surface
        input_surface = MediaCodec.createPersistentInputSurface();

        MediaFormat video_format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                profile.videoFrameWidth, profile.videoFrameHeight);
        video_format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        video_format.setInteger(MediaFormat.KEY_BIT_RATE, profile.videoBitRate);
        video_format.setInteger(MediaFormat.KEY_FRAME_RATE, profile.videoFrameRate);
        video_format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        video_format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);

        video_encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        try {
            video_encoder.configure(video_format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        }
        catch(RuntimeException e) {
            // e.g. MediaCodec.CodecException if the surface color format/size is rejected
            throw new IOException("failed to configure video encoder", e);
        }
        video_encoder.setInputSurface(input_surface);
        video_encoder.start();

        if( record_audio ) {
            if( !setupAudio(profile) ) {
                if( MyDebug.LOG )
                    Log.e(TAG, "audio unavailable, recording video only");
                record_audio = false;
            }
        }
        audio_format_ready = !record_audio; // no audio track to wait for when audio is disabled

        if( file_path != null )
            muxer = new MediaMuxer(file_path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        else
            muxer = new MediaMuxer(file_descriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && orientation_hint != 0 ) {
            muxer.setOrientationHint(orientation_hint);
        }
        if( location != null ) {
            muxer.setLocation((float)location.getLatitude(), (float)location.getLongitude());
        }

        running = true;
        if( record_audio )
            startAudioThread();

        drain_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                drainLoop();
            }
        }, "MediaCodecVideoRecorder-drain");
        drain_thread.setDaemon(true);
        drain_thread.start();
    }

    private boolean setupAudio(VideoProfile profile) {
        if( MyDebug.LOG )
            Log.d(TAG, "setupAudio");
        MediaFormat audio_format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                profile.audioSampleRate, profile.audioChannels);
        audio_format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audio_format.setInteger(MediaFormat.KEY_BIT_RATE, profile.audioBitRate);
        try {
            audio_encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            audio_encoder.configure(audio_format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            audio_encoder.start();
        }
        catch(IOException | RuntimeException e) {
            MyDebug.logStackTrace(TAG, "failed to setup audio encoder", e);
            releaseAudioEncoder();
            return false;
        }

        int sample_rate = profile.audioSampleRate;
        int channel_config = profile.audioChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        int min_buffer = AudioRecord.getMinBufferSize(sample_rate, channel_config, AudioFormat.ENCODING_PCM_16BIT);
        int buffer_size = Math.max(min_buffer, 8192);

        AudioRecord record = null;
        try {
            record = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, sample_rate, channel_config,
                    AudioFormat.ENCODING_PCM_16BIT, buffer_size);
            if( record.getState() != AudioRecord.STATE_INITIALIZED ) {
                if( MyDebug.LOG )
                    Log.e(TAG, "CAMCORDER AudioRecord failed to initialise, falling back to MIC");
                record.release();
                record = new AudioRecord(MediaRecorder.AudioSource.MIC, sample_rate, channel_config,
                        AudioFormat.ENCODING_PCM_16BIT, buffer_size);
            }
            if( record.getState() != AudioRecord.STATE_INITIALIZED ) {
                if( MyDebug.LOG )
                    Log.e(TAG, "MIC AudioRecord failed to initialise");
                record.release();
                record = null;
            }
        }
        catch(RuntimeException e) {
            MyDebug.logStackTrace(TAG, "failed to create AudioRecord", e);
            if( record != null ) {
                try {
                    record.release();
                }
                catch(RuntimeException e2) {
                }
            }
            record = null;
        }

        if( record == null ) {
            releaseAudioEncoder();
            return false;
        }
        this.audio_record = record;
        this.audio_buffer_size = buffer_size;
        return true;
    }

    private void startAudioThread() {
        if( MyDebug.LOG )
            Log.d(TAG, "startAudioThread");
        final int buffer_size = audio_buffer_size;
        audio_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if( MyDebug.LOG )
                    Log.d(TAG, "audio thread start");
                final byte[] buffer = new byte[buffer_size];
                try {
                    audio_record.startRecording();
                }
                catch(RuntimeException e) {
                    MyDebug.logStackTrace(TAG, "failed to start AudioRecord", e);
                    // carry on without audio, so the muxer can still start video-only
                    audio_failed = true;
                    audio_format_ready = true;
                    return;
                }
                while( running ) {
                    int n_read;
                    try {
                        n_read = audio_record.read(buffer, 0, buffer.length);
                    }
                    catch(RuntimeException e) {
                        if( running )
                            MyDebug.logStackTrace(TAG, "failed to read AudioRecord", e);
                        break;
                    }
                    if( n_read > 0 ) {
                        feedAudioEncoder(buffer, n_read);
                    }
                    else if( n_read < 0 ) {
                        if( MyDebug.LOG )
                            Log.e(TAG, "AudioRecord read error: " + n_read);
                        if( running )
                            break;
                    }
                }
                // signal end of audio stream so the drain loop can finish
                try {
                    int input_index = audio_encoder.dequeueInputBuffer(10000);
                    if( input_index >= 0 ) {
                        audio_encoder.queueInputBuffer(input_index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                }
                catch(RuntimeException e) {
                    MyDebug.logStackTrace(TAG, "failed to signal audio EOS", e);
                }
                if( MyDebug.LOG )
                    Log.d(TAG, "audio thread stop");
            }
        }, "MediaCodecVideoRecorder-audio");
        audio_thread.setDaemon(true);
        audio_thread.start();
    }

    private void feedAudioEncoder(byte[] data, int length) {
        try {
            int input_index = audio_encoder.dequeueInputBuffer(10000);
            if( input_index >= 0 ) {
                ByteBuffer input_buffer = audio_encoder.getInputBuffer(input_index);
                if( input_buffer != null ) {
                    input_buffer.clear();
                    int copy = Math.min(length, input_buffer.capacity());
                    input_buffer.put(data, 0, copy);
                    // raw PTS; the audio->video clock offset is applied when writing to the muxer
                    long pts_us = (System.nanoTime() - recording_base_nanos) / 1000;
                    audio_encoder.queueInputBuffer(input_index, 0, copy, pts_us, 0);
                }
                else {
                    audio_encoder.queueInputBuffer(input_index, 0, 0, 0, 0);
                }
            }
        }
        catch(RuntimeException e) {
            MyDebug.logStackTrace(TAG, "failed to feed audio encoder", e);
        }
    }

    private void drainLoop() {
        if( MyDebug.LOG )
            Log.d(TAG, "drain loop start");
        boolean video_eos = false;
        boolean audio_eos = !record_audio;
        long stop_deadline = Long.MAX_VALUE;
        while( !(video_eos && audio_eos) ) {
            if( stopped ) {
                if( stop_deadline == Long.MAX_VALUE )
                    stop_deadline = System.nanoTime() + 2000_000_000L;
                if( System.nanoTime() > stop_deadline )
                    break;
            }
            if( !video_eos )
                video_eos = drainVideo();
            if( !audio_eos )
                audio_eos = drainAudio();
            if( !muxer_started && video_format_ready && audio_format_ready ) {
                try {
                    muxer.start();
                    muxer_started = true;
                    if( MyDebug.LOG )
                        Log.d(TAG, "muxer started");
                }
                catch(RuntimeException e) {
                    reportFailure(e);
                    break;
                }
            }
            checkLimits();
        }
        if( muxer != null && muxer_started ) {
            try {
                muxer.stop();
            }
            catch(RuntimeException e) {
                MyDebug.logStackTrace(TAG, "failed to stop muxer", e);
            }
            muxer_started = false;
        }
        if( MyDebug.LOG )
            Log.d(TAG, "drain loop stop");
    }

    /** @return true if the video encoder reported end-of-stream (or a fatal error). */
    private boolean drainVideo() {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int index;
        try {
            index = video_encoder.dequeueOutputBuffer(info, 10000);
        }
        catch(RuntimeException e) {
            reportFailure(e);
            return true;
        }
        if( index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED ) {
            if( !muxer_started && video_track == -1 ) {
                try {
                    video_track = muxer.addTrack(video_encoder.getOutputFormat());
                    video_format_ready = true;
                }
                catch(RuntimeException e) {
                    reportFailure(e);
                    return true;
                }
            }
            return false;
        }
        else if( index == MediaCodec.INFO_TRY_AGAIN_LATER ) {
            return false;
        }
        else if( index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED ) {
            return false;
        }
        else if( index < 0 ) {
            if( MyDebug.LOG )
                Log.e(TAG, "unexpected video dequeue result: " + index);
            return false;
        }

        boolean is_eos = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
        ByteBuffer buffer = video_encoder.getOutputBuffer(index);
        if( (info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && muxer_started && !paused && info.size > 0 && buffer != null ) {
            long pts_us = info.presentationTimeUs + video_pts_offset_us;
            info.presentationTimeUs = pts_us;
            try {
                muxer.writeSampleData(video_track, buffer, info);
                bytes_written += info.size;
            }
            catch(Exception e) {
                reportFailure(e);
                video_encoder.releaseOutputBuffer(index, false);
                return true;
            }
            if( !first_video_frame_seen ) {
                first_video_frame_seen = true;
                audio_pts_offset_us = pts_us - ((System.nanoTime() - recording_base_nanos) / 1000);
                if( MyDebug.LOG )
                    Log.d(TAG, "first video frame pts: " + pts_us + ", audio offset: " + audio_pts_offset_us);
            }
        }
        video_encoder.releaseOutputBuffer(index, false);
        return is_eos;
    }

    /** @return true if the audio encoder reported end-of-stream (or a fatal error). */
    private boolean drainAudio() {
        if( audio_encoder == null || audio_failed )
            return true;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int index;
        try {
            index = audio_encoder.dequeueOutputBuffer(info, 10000);
        }
        catch(RuntimeException e) {
            reportFailure(e);
            return true;
        }
        if( index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED ) {
            if( !muxer_started && audio_track == -1 ) {
                try {
                    audio_track = muxer.addTrack(audio_encoder.getOutputFormat());
                    audio_format_ready = true;
                }
                catch(RuntimeException e) {
                    reportFailure(e);
                    return true;
                }
            }
            return false;
        }
        else if( index == MediaCodec.INFO_TRY_AGAIN_LATER ) {
            return false;
        }
        else if( index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED ) {
            return false;
        }
        else if( index < 0 ) {
            if( MyDebug.LOG )
                Log.e(TAG, "unexpected audio dequeue result: " + index);
            return false;
        }

        boolean is_eos = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
        ByteBuffer buffer = audio_encoder.getOutputBuffer(index);
        // drop audio until the first video frame has set the audio->video clock offset
        if( (info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && muxer_started && !paused && first_video_frame_seen && info.size > 0 && buffer != null ) {
            long pts_us = info.presentationTimeUs + audio_pts_offset_us;
            info.presentationTimeUs = pts_us;
            try {
                muxer.writeSampleData(audio_track, buffer, info);
                bytes_written += info.size;
            }
            catch(Exception e) {
                reportFailure(e);
                audio_encoder.releaseOutputBuffer(index, false);
                return true;
            }
        }
        audio_encoder.releaseOutputBuffer(index, false);
        return is_eos;
    }

    private void checkLimits() {
        if( max_duration_ms > 0 && !max_duration_reported ) {
            long elapsed_ms = (System.nanoTime() - recording_base_nanos) / 1000000;
            if( elapsed_ms >= max_duration_ms ) {
                max_duration_reported = true;
                if( callback != null )
                    callback.onInfo(MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED, 0);
            }
        }
        if( max_filesize_bytes > 0 && !max_filesize_reported && bytes_written >= max_filesize_bytes ) {
            max_filesize_reported = true;
            if( callback != null )
                callback.onInfo(MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED, 0);
        }
    }

    private void reportFailure(Exception e) {
        MyDebug.logStackTrace(TAG, "MediaCodecVideoRecorder failure", e);
        if( callback != null && !failure_reported ) {
            failure_reported = true;
            callback.onError(MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN, 0);
        }
    }

    public void pause() {
        if( MyDebug.LOG )
            Log.d(TAG, "pause");
        if( paused )
            return;
        paused = true;
        paused_at_nanos = System.nanoTime();
    }

    public void resume() {
        if( MyDebug.LOG )
            Log.d(TAG, "resume");
        if( !paused )
            return;
        long now = System.nanoTime();
        long pause_us = (now - paused_at_nanos) / 1000;
        // keep the recording clock (max duration) and both PTS clocks continuous across the pause
        recording_base_nanos += now - paused_at_nanos;
        video_pts_offset_us -= pause_us;
        audio_pts_offset_us -= pause_us;
        paused = false;
    }

    public void stop() {
        if( MyDebug.LOG )
            Log.d(TAG, "stop");
        if( released )
            return;
        stopped = true;
        running = false;
        if( audio_record != null ) {
            try {
                audio_record.stop();
            }
            catch(RuntimeException e) {
            }
        }
        joinThread(audio_thread, 1000);
        if( video_encoder != null ) {
            try {
                video_encoder.signalEndOfInputStream();
            }
            catch(RuntimeException e) {
            }
        }
        joinThread(drain_thread, 5000);
        releaseAll();
    }

    private void joinThread(Thread thread, long timeout_ms) {
        if( thread == null )
            return;
        try {
            thread.join(timeout_ms);
        }
        catch(InterruptedException e) {
            MyDebug.logStackTrace(TAG, "interrupted joining thread", e);
        }
    }

    private void releaseAll() {
        if( MyDebug.LOG )
            Log.d(TAG, "releaseAll");
        released = true;
        if( muxer != null ) {
            try {
                muxer.release();
            }
            catch(RuntimeException e) {
            }
            muxer = null;
        }
        releaseAudioEncoder();
        if( video_encoder != null ) {
            try {
                video_encoder.stop();
            }
            catch(RuntimeException e) {
            }
            try {
                video_encoder.release();
            }
            catch(RuntimeException e) {
            }
            video_encoder = null;
        }
        if( input_surface != null ) {
            input_surface.release();
            input_surface = null;
        }
        if( audio_record != null ) {
            try {
                audio_record.release();
            }
            catch(RuntimeException e) {
            }
            audio_record = null;
        }
        callback = null;
    }

    private void releaseAudioEncoder() {
        if( audio_encoder != null ) {
            try {
                audio_encoder.stop();
            }
            catch(RuntimeException e) {
            }
            try {
                audio_encoder.release();
            }
            catch(RuntimeException e) {
            }
            audio_encoder = null;
        }
    }
}
