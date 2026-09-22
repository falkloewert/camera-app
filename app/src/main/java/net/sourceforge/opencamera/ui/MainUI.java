package net.sourceforge.opencamera.ui;

import net.sourceforge.opencamera.MyApplicationInterface;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.preview.Preview;
import net.sourceforge.opencamera.preview.VideoProfile;
import net.sourceforge.opencamera.R;

import android.animation.TimeInterpolator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraExtensionCharacteristics;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/** This contains functionality related to the main UI.
 */
public class MainUI {
    private static final String TAG = "MainUI";

    private final MainActivity main_activity;

    private final OnScreenIcons onScreenIcons;

    private final ZoomStepper zoomStepper;
    private final ShutterButton shutterButton;

    private int current_orientation; // how far the device has been turned clockwise from portrait, in steps of 90 degrees (onOrientationChanged())
    private int ui_rotation; // the rotation that makes a glyph read upright for the user, see updateUIRotation()
    /** Where the on-screen text of DrawPreview sits. There is only one fixed layout now (see
     *  layoutUI()), with the header along the top edge; the enum survives for DrawPreview, which
     *  positions its on-screen text relative to the header band. */
    enum UIPlacement {
        UIPLACEMENT_RIGHT,
        UIPLACEMENT_LEFT,
        UIPLACEMENT_TOP
    }
    private int navigation_gap_left; // inset along the left edge, remembered for setFocusSeekbarsRotation()
    private int focus_seekbars_bottom_margin; // likewise the focus seekbars' distance from the bottom edge
    /** How the glyphs turn when the device is turned: the reference's 300 ms with
     *  cubic-bezier(0.2, 0.0, 0.0, 1.0) (docs/design-reference/pixel-camera-detailspec.md
     *  section 10). Shared with PhotoVideoSwitch, which turns its glyphs itself. */
    final static int glyph_rotation_duration_ms = 300;
    final static TimeInterpolator glyph_rotation_interpolator = new PathInterpolator(0.2f, 0.0f, 0.0f, 1.0f);
    public final static int privacy_indicator_gap_dp = 24;

    private boolean immersive_mode;
    private boolean show_gui_photo = true; // result of call to showGUI() - false means a "reduced" GUI is displayed, whilst taking photo or video
    private boolean show_gui_video = true;

    private boolean keydown_volume_up;
    private boolean keydown_volume_down;

    /** Ob eine Videoaufnahme läuft, soweit die Modusleiste das angeht (SPEC 4.6). startingVideo()
     *  meldet den Start selbst, weil Preview.isVideoRecording() in dem Moment noch falsch ist -
     *  aus demselben Grund ruft es setShutterState() direkt; stoppedVideo() meldet das Ende. */
    private boolean video_recording_ui;

    /** Welcher Eintrag der Modusleiste welchen Modus setzt - Ansichts-ID auf Wert. Die beiden
     *  Karten sind die einzige Zuordnung: die Karussell-Leiste meldet nur den angetippten Eintrag
     *  zurück (ModeBar.Listener), und der unterscheidet nicht zwischen Foto- und Video-Einträgen. */
    private final Map<Integer, String> mode_bar_photo_modes = new HashMap<>();
    private final Map<Integer, String> mode_bar_video_speeds = new HashMap<>();

    // For remote control: keep track of the currently highlighted
    // line and icon within the exposure UI
    private boolean remote_control_mode; // whether remote control mode is enabled
    private int mExposureLine = 0;
    private boolean mSelectingExposureUIElement = false;
    private final int highlightColor = Color.rgb(183, 28, 28); // Red 900
    private final int highlightColorExposureUIElement = Color.rgb(244, 67, 54); // Red 500

    // for testing:
    public volatile int test_navigation_gap;
    public volatile int test_navigation_gap_landscape;
    public volatile int test_navigation_gap_reversed_landscape;

    public MainUI(MainActivity main_activity) {
        if( MyDebug.LOG )
            Log.d(TAG, "MainUI");
        this.main_activity = main_activity;

        this.onScreenIcons = new OnScreenIcons(main_activity);
        this.zoomStepper = new ZoomStepper(main_activity);
        this.shutterButton = new ShutterButton((ImageButton)main_activity.findViewById(R.id.take_photo));

        this.setSeekbarColors();
        this.setupModeBar();
        // the night automation (see checkNightAuto) is armed here as well, so that a "Nacht auto"
        // that is still picked when the app starts takes over again; it starts itself only when it
        // is wanted, and the first look happens after the camera is up (nothing may be asked of
        // the preview right now: it is created after this UI, in MainActivity.onCreate)
        this.night_auto_handler.postDelayed(this.night_auto_runnable, NIGHT_AUTO_CHECK_MS);
    }

    public OnScreenIcons getOnScreenIcons() {
        return this.onScreenIcons;
    }

    public ZoomStepper getZoomStepper() {
        return this.zoomStepper;
    }

    public ShutterButton getShutterButton() {
        return this.shutterButton;
    }

    private void setSeekbarColors() {
        if( MyDebug.LOG )
            Log.d(TAG, "setSeekbarColors");
        {
            ColorStateList progress_color = ColorStateList.valueOf( Color.argb(255, 240, 240, 240) );
            ColorStateList thumb_color = ColorStateList.valueOf( Color.argb(255, 255, 255, 255) );

            SeekBar seekBar = main_activity.findViewById(R.id.focus_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.focus_bracketing_target_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.exposure_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.iso_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.exposure_time_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.white_balance_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);
        }
    }

    /** Turns a glyph on the spot to ui_rotation, the shortest way round: at once when laying
     *  out, or with the reference's ease when the device has just been turned (animate). Only
     *  the view's drawing turns - View.setRotation() leaves its layout bounds alone - which is
     *  what keeps every control at its physical spot on the device.
     */
    private void setViewRotation(View view, int ui_rotation, boolean animate) {
        float rotate_by = shortestTurn(ui_rotation - view.getRotation());
        if( !animate ) {
            view.animate().cancel(); // in case a turn is still under way
            view.setRotation(ui_rotation);
        }
        else if( rotate_by != 0.0f ) {
            // view.animate() modifies the view's rotation attribute, so it ends up equivalent to view.setRotation()
            // we use rotationBy() instead of rotation(), so we get the minimal rotation for clockwise vs anti-clockwise
            view.animate().rotationBy(rotate_by).setDuration(glyph_rotation_duration_ms).setInterpolator(glyph_rotation_interpolator).start();
        }
    }

    /** Brings a turn in degrees into (-180, 180], i.e. the shorter of the two ways round. */
    static float shortestTurn(float degrees) {
        degrees = degrees % 360.0f;
        if( degrees > 180.0f )
            degrees -= 360.0f;
        else if( degrees <= -180.0f )
            degrees += 360.0f;
        return degrees;
    }

    /** The rotation that makes a glyph read upright for the user, in degrees clockwise: 0 with
     *  the device upright, 90 with its top edge on the left, and so on - see updateUIRotation(). */
    int getUIRotation() {
        return ui_rotation;
    }

    /** Recomputes ui_rotation from current_orientation and hands it to the Preview, whose
     *  overlays (DrawPreview) turn with it too.
     *  current_orientation is how far the device has been turned clockwise from its natural
     *  (portrait) position (onOrientationChanged()), so the glyph has to turn the same amount
     *  the other way. The window's own rotation is added in case the natural orientation isn't
     *  portrait (a tablet held on end): for a phone it's 0.
     */
    private void updateUIRotation() {
        int rotation = main_activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
            default:
                break;
        }
        // getRotation is anti-clockwise, but current_orientation is clockwise, so we add rather than subtract
        int relative_orientation = (current_orientation + degrees) % 360;
        this.ui_rotation = (360 - relative_orientation) % 360;
        if( MyDebug.LOG ) {
            Log.d(TAG, "    current_orientation = " + current_orientation);
            Log.d(TAG, "    degrees = " + degrees);
            Log.d(TAG, "    ui_rotation = " + ui_rotation);
        }
        main_activity.getPreview().setUIRotation(ui_rotation);
    }

    /** Everything that turns in place when the device is turned (rotation model,
     *  ui-spezifikation.md section 2): the icons of the shutter row and its stand-ins during
     *  video/panorama, the panorama lens pair and the zoom step digits. Each is a square view
     *  holding a single glyph - or a circle - so turning the whole view is the same as turning
     *  just the glyph. What is NOT here stays as printed on the fixed layout: the pills behind
     *  the zoom steps, mode bar and photo/video switch (the switch turns its two glyphs itself,
     *  see rotateGlyphs()), the mode bar labels, the header row and the exposure sliders (both
     *  old Open Camera elements the redesign replaces; the icon row is gone since Schritt C).
     */
    private List<View> glyphViews() {
        List<View> views = new ArrayList<>();
        views.add(main_activity.findViewById(R.id.take_photo));
        views.add(main_activity.findViewById(R.id.switch_camera));
        views.add(main_activity.findViewById(R.id.switch_multi_camera));
        views.add(main_activity.findViewById(R.id.gallery));
        views.add(main_activity.findViewById(R.id.pause_video));
        views.add(main_activity.findViewById(R.id.cancel_panorama));
        views.add(main_activity.findViewById(R.id.take_photo_when_video_recording));
        views.add(main_activity.findViewById(R.id.panorama_lens_wide));
        views.add(main_activity.findViewById(R.id.panorama_lens_1x));
        views.addAll(zoomStepper.getStepButtons());
        views.add(main_activity.findViewById(R.id.zoom_badge));
        // the left dock button: only its glyph turns, the circle it sits in is rotationally
        // symmetric (Kickoff 3.1)
        views.add(main_activity.findViewById(R.id.settings_sheet_button));
        // the right dock button (the pro panel) - same treatment
        views.add(main_activity.findViewById(R.id.pro_panel_button));
        return views;
    }

    /** Turns all the glyphs to the current ui_rotation - see glyphViews() and setViewRotation(). */
    private void rotateGlyphs(boolean animate) {
        for(View view : glyphViews())
            setViewRotation(view, ui_rotation, animate);
        PhotoVideoSwitch photoVideoSwitch = main_activity.findViewById(R.id.photo_video_switch);
        photoVideoSwitch.setGlyphRotation(ui_rotation, animate);
        ZoomWheel zoomWheel = main_activity.findViewById(R.id.zoom_stepper);
        zoomWheel.setGlyphRotation(ui_rotation, animate);
        // an open settings sheet hangs off the physical bottom edge, so it has to move to the
        // new edge as a whole (Kickoff 3.2) - immediately, without an animation
        SettingsSheet settingsSheet = main_activity.getSettingsSheet();
        if( settingsSheet != null )
            settingsSheet.onUIRotationChanged(ui_rotation);
    }

    // stores with width and height of the last time we laid out the UI
    public int layoutUI_display_w = -1;
    public int layoutUI_display_h = -1;

    private ConstraintLayout getConstraintRoot() {
        return (ConstraintLayout) main_activity.findViewById(R.id.activity_main_root);
    }

    /** Clears all of a view's constraints in the pending ConstraintSet (equivalent to what the
     *  old RelativeLayout code achieved by explicitly zeroing every rule before setting the ones
     *  that should be active) - then immediately restores its width, height and visibility,
     *  since ConstraintSet.clear(id) removes the view's entry entirely, and those three
     *  properties are otherwise controlled elsewhere (static XML size, or visibility toggled by
     *  showGUI()/setImmersiveMode()/onScreenIcons - none of which layoutUI() should override). */
    private void resetView(ConstraintSet cs, View view) {
        int view_id = view.getId();
        ViewGroup.LayoutParams params = view.getLayoutParams();
        int width = params.width;
        int height = params.height;
        int visibility = view.getVisibility();
        cs.clear(view_id);
        cs.constrainWidth(view_id, width);
        cs.constrainHeight(view_id, height);
        cs.setVisibility(view_id, visibility);
    }

    /** Centres a view on the screen's width. */
    private void centerHorizontally(ConstraintSet cs, int view_id) {
        cs.connect(view_id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
        cs.connect(view_id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
    }

    /** Places view above anchor_id, its bottom edge margin above the anchor's top edge. */
    private void placeAbove(ConstraintSet cs, int view_id, int anchor_id, int margin) {
        cs.connect(view_id, ConstraintSet.BOTTOM, anchor_id, ConstraintSet.TOP, margin);
    }

    /** Pins a view's bottom edge to the bottom of the screen, margin above the gesture inset. */
    private void pinToBottom(ConstraintSet cs, int view_id, int navigation_gap, int margin) {
        cs.connect(view_id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, navigation_gap + margin);
    }

    /* ---- Kopfzeile (Schritt C, SPEC 4.5) --------------------------------------------------- */

    /** Maße der Kopfzeile, alle aus dem Screenrecord der Pixel-App gemessen (2,25 px/dp,
     *  docs/design-reference/pixel-kopfzeile-beschreibung.md) - nicht aus der Textreferenz, die
     *  an diesen Stellen falsch ist (sie nannte 6 dp Eckenradius und ein gestapeltes Makro-Symbol). */
    private static final float header_height_dp = 70.7f;          // das schwarze Band über dem Sucher
    /** Feste Achse, auf der alle drei gemessenen Chip-Varianten sitzen ("FHD", "4K", "50 MP") -
     *  die Beschriftung wächst symmetrisch um sie herum, der Chip sitzt nicht bündig am Rand. */
    private static final float header_chip_center_x_dp = 38.0f;
    private static final float header_chip_center_y_dp = 42.9f;
    private static final float header_chip_wide_w_dp = 21.3f;      // "FHD": 21,3 × 16,0 dp
    private static final float header_chip_wide_h_dp = 16.0f;
    private static final float header_chip_square_dp = 17.8f;     // "4K" und "50 MP": 17,8 × 17,8 dp
    private static final float header_chip_text_size_dp = 9.0f;
    /** Abstand zwischen Auflösungs- und Bildraten-Chip. Die Referenz hatte dort ihr
     *  Makrofokus-Symbol (80,9 dp), das es hier nicht gibt; Falk will die beiden Chips dicht
     *  beieinander haben, damit die Kopfzeile Platz spart (2026-09-22). */
    private static final float header_chip_gap_dp = 4.0f;
    /** Lage der Zustandsanzeigen rechts im Band. Gewählt, nicht gemessen: der Record, aus dem die
     *  übrigen Maße stammen, läuft im einfachen Foto-Modus, und dort ist keine der Anzeigen an.
     *  Der rechte Rand ist der des Bandes, die Höhe die Trefferfläche einer Anzeige; ihre Mitte
     *  sitzt auf derselben Achse wie der Chip. */
    private static final float header_indicators_right_dp = 16.0f;
    private static final float header_indicators_height_dp = 32.0f;

    /** Der Text des Auflösungs-Chips. Foto: die Megapixel unserer Auflösung, zweizeilig wie das
     *  "50 MP" der Referenz (bei uns "12 MP", weil der Standard das native 12-MP-Raster ist).
     *  Video: die Auflösungsklasse wie im Record ("4K", "FHD", "8K") - dieselbe Schreibweise, die
     *  das Video-Blatt für seine Knöpfe benutzt. */
    private String headerChipLabel() {
        Preview preview = main_activity.getPreview();
        if( preview.isVideo() ) {
            String quality = preview.getVideoQualityHander().getCurrentVideoQuality();
            CamcorderProfile profile = quality == null ? null : preview.getCamcorderProfile(quality);
            int width = profile == null ? 0 : profile.videoFrameWidth;
            if( width <= 0 )
                return "FHD"; // die Kamera sagt noch nichts - 16:9 startet bei uns mit FHD
            String resolution;
            if( width >= 7680 )
                resolution = "8K";
            else if( width >= 3840 )
                resolution = "4K";
            else if( width >= 1920 )
                resolution = "FHD";
            else
                resolution = Math.round(width / 1000.0f) + "K"; // z.B. das 3264er der Frontkamera
            return resolution;
        }
        CameraController.Size size = preview.getCurrentPictureSize();
        if( size == null )
            return "";
        return Math.round(size.width * (float)size.height / 1000000.0f) + "\nMP";
    }

    /** Die Bildrate fuer den eigenen Chip rechts neben der Aufloesung (Falk 2026-09-22: "fps muss
     *  separat sein weil es ja viele Kombinationen gibt") - leer im Foto und solange die Kamera
     *  noch nichts gemeldet hat. Die Zahl kommt aus getVideoProfile(), nicht aus dem
     *  CamcorderProfile der Aufloesung: nur dort ist die fps-Vorgabe des Video-Blatts
     *  eingerechnet (Falk 2026-09-22: "fps zeigt immer 60 auch wenn 30 eingestellt ist").
     *
     *  Gezeigt wird die Aufnahmerate, nicht die des fertigen Films: bei Zeitlupe steckt die
     *  Qualitaet in der Aufnahmerate (2-fach = 60/s), waehrend der Film derselben Regel folgend
     *  immer mit 30/s laeuft - "30" waere dort also irrefuehrend (Falk 2026-09-22: "sollte das
     *  fuer bessere Zeitlupe nicht immer 60 haben?"). Bei Zeitraffer ist die Aufnahmerate
     *  umgekehrt kleiner als eins, darum gilt sie nur, wenn sie mindestens 1 ist. */
    private String headerFpsLabel() {
        Preview preview = main_activity.getPreview();
        if( !preview.isVideo() )
            return "";
        VideoProfile profile = preview.getVideoProfile();
        int fps = profile.videoFrameRate;
        int capture_fps = Math.round((float)profile.videoCaptureRate);
        if( capture_fps >= 1 && capture_fps > fps )
            fps = capture_fps;
        return fps > 0 ? String.valueOf(fps) : "";
    }

    /** Das Symbol des Foto-Modus fuer den Chip links im Band (Schritt G Punkt 8: "HDR und Dro Chip
     *  sollen oben links neben aufloesungs chip") - 0, wenn es fuer den Modus keines gibt. Das sind
     *  genau die Modi, deren Symbol die Sucher-Reihe bis Schritt G zeigte; Panorama fehlt, weil die
     *  Modusleiste es ohnehin ausweist und seine eigene Objektivreihe hat. */
    private int headerModeIcon() {
        Preview preview = main_activity.getPreview();
        if( preview == null || preview.isVideo() )
            return 0;
        switch( main_activity.getApplicationInterface().getPhotoMode() ) {
            case DRO:
                return R.drawable.dro_icon;
            case HDR:
                return R.drawable.ic_hdr_on_white_48dp;
            case ExpoBracketing:
                return R.drawable.expo_icon;
            case FastBurst:
                return R.drawable.ic_burst_mode_white_48dp;
            case NoiseReduction:
                return R.drawable.nr_icon;
            default:
                return 0;
        }
    }

    /** Der Name des Modus fuer die Vorlesehilfe des Modus-Chips - dieselbe Beschriftung, die das
     *  Modus-Popup benutzt. */
    private int headerModeIconLabel() {
        switch( main_activity.getApplicationInterface().getPhotoMode() ) {
            case DRO:
                return R.string.photo_mode_dro;
            case HDR:
                return R.string.photo_mode_hdr;
            case ExpoBracketing:
                return R.string.photo_mode_expo_bracketing_full;
            case FastBurst:
                return R.string.photo_mode_fast_burst_full;
            case NoiseReduction:
                return R.string.photo_mode_noise_reduction_full;
            default:
                return 0;
        }
    }

    /** Legt die Kopfzeile: das schwarze Band, den Auflösungs-Chip auf seiner festen Achse und
     *  rechts daneben den Bildraten-Chip (Video) auf derselben Mittelachse. Band und Chips sitzen fest im
     *  Layout und drehen nicht mit (wie die Beschriftungen der Modusleiste): sie sind auf die
     *  feste Hochformat-Anordnung gedruckt, nur die Symbole der Bedienelemente drehen sich
     *  (Drehmodell K3, ui-spezifikation.md Abschnitt 2). */
    private void layoutHeader(ConstraintSet cs, float scale) {
        View bar = main_activity.findViewById(R.id.header_bar);
        int bar_id = bar.getId();
        resetView(cs, bar);
        cs.connect(bar_id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
        cs.connect(bar_id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
        cs.connect(bar_id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        cs.constrainHeight(bar_id, (int) (header_height_dp * scale + 0.5f));

        TextView chip = main_activity.findViewById(R.id.header_chip);
        chip.setText(headerChipLabel());
        // Die vier Zeichen von "FHD" brauchen den breiten, dafür flacheren Chip; "4K" und das
        // zweizeilige "12 MP" sitzen im Quadrat (gemessen, pixel-kopfzeile-beschreibung.md 2).
        String label = chip.getText().toString();
        boolean wide = label.length() >= 4 && label.indexOf('\n') < 0;
        int chip_w = (int) ((wide ? header_chip_wide_w_dp : header_chip_square_dp) * scale + 0.5f);
        int chip_h = (int) ((wide ? header_chip_wide_h_dp : header_chip_square_dp) * scale + 0.5f);
        int chip_id = chip.getId();
        resetView(cs, chip);
        // Solange die Kamera noch keine Auflösung meldet (erster Aufbau, bevor sie offen ist),
        // bleibt der Chip weg statt als leerer weißer Block dazustehen. Nach resetView setzen,
        // das stellt sonst die Sichtbarkeit der View wieder her.
        cs.setVisibility(chip_id, label.isEmpty() ? View.GONE : chip.getVisibility());
        cs.constrainWidth(chip_id, chip_w);
        cs.constrainHeight(chip_id, chip_h);
        cs.connect(chip_id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT,
                (int) (header_chip_center_x_dp * scale + 0.5f) - chip_w / 2);
        cs.connect(chip_id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP,
                (int) (header_chip_center_y_dp * scale + 0.5f) - chip_h / 2);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_PX, header_chip_text_size_dp * scale);
        chip.setLineSpacing(0.0f, 0.7f); // die zwei Zeilen sitzen dicht untereinander (gemessen)

        // Der zweite Chip, die Bildrate - nur im Video. Er hängt am rechten Rand des
        // Auflösungs-Chips (nicht an einer festen Position, damit er bei beiden Chip-Breiten dicht
        // anschließt) und sitzt auf derselben Achse.
        TextView fps = main_activity.findViewById(R.id.header_fps);
        fps.setText(headerFpsLabel());
        String fps_label = fps.getText().toString();
        int fps_size = (int) (header_chip_square_dp * scale + 0.5f);
        int fps_id = fps.getId();
        resetView(cs, fps);
        cs.setVisibility(fps_id, fps_label.isEmpty() ? View.GONE : fps.getVisibility());
        cs.constrainWidth(fps_id, fps_size);
        cs.constrainHeight(fps_id, fps_size);
        cs.connect(fps_id, ConstraintSet.LEFT, chip_id, ConstraintSet.RIGHT,
                (int) (header_chip_gap_dp * scale + 0.5f));
        cs.connect(fps_id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP,
                (int) (header_chip_center_y_dp * scale + 0.5f) - fps_size / 2);
        fps.setTextSize(TypedValue.COMPLEX_UNIT_PX, header_chip_text_size_dp * scale);

        // Der Modus-Chip (Foto) nimmt dieselbe Lücke ein wie der Bildraten-Chip: er hängt wie
        // dieser am rechten Rand des Auflösungs-Chips und sitzt auf derselben Achse. Beide zusammen
        // gibt es nicht - der Chip erscheint nur, wenn der fps-Chip leer ist (und umgekehrt, s.
        // headerFpsLabel(): eine Rate gibt es nur im Video, diese Foto-Modi nur im Foto).
        final int indicators_icon_size = (int) (header_indicators_height_dp * scale + 0.5f);
        ImageButton mode = main_activity.findViewById(R.id.header_mode);
        int mode_icon = headerModeIcon();
        int mode_id = mode.getId();
        resetView(cs, mode);
        cs.setVisibility(mode_id, (fps_label.isEmpty() && mode_icon != 0) ? View.VISIBLE : View.GONE);
        if( mode_icon != 0 ) {
            mode.setImageResource(mode_icon);
            int mode_label = headerModeIconLabel();
            if( mode_label != 0 )
                mode.setContentDescription(main_activity.getResources().getString(mode_label));
        }
        cs.constrainWidth(mode_id, indicators_icon_size);
        cs.constrainHeight(mode_id, indicators_icon_size);
        cs.connect(mode_id, ConstraintSet.LEFT, chip_id, ConstraintSet.RIGHT,
                (int) (header_chip_gap_dp * scale + 0.5f));
        cs.connect(mode_id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP,
                (int) (header_chip_center_y_dp * scale + 0.5f) - indicators_icon_size / 2);

        // Die Zustandsanzeigen (RAW, Blitz, GPS, Ausrichtungssperre, Textstempel) sitzen rechts
        // im Band; welche davon an sind, entscheidet OnScreenIcons. Die Reihe selbst hängt rechts
        // am Rand und bleibt sonst, wie das Band sie mitbringt (Breite aus dem Layout).
        View indicators = main_activity.findViewById(R.id.header_indicators);
        int indicators_id = indicators.getId();
        resetView(cs, indicators);
        cs.constrainHeight(indicators_id, indicators_icon_size);
        cs.connect(indicators_id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                (int) (header_indicators_right_dp * scale + 0.5f));
        cs.connect(indicators_id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP,
                (int) ((header_chip_center_y_dp - header_indicators_height_dp / 2.0f) * scale + 0.5f));
    }

    /** Blendet die Kopfzeile ein oder aus - Band, Auflösungs-Chip und die Zustandsanzeigen
     *  zusammen, damit keine der drei je allein zurückbleibt. Alle Stellen, die die Kopfzeile
     *  ein- oder ausblenden, gehen hier durch. */
    private void setHeaderVisibility(int visibility) {
        main_activity.findViewById(R.id.header_bar).setVisibility(visibility);
        main_activity.findViewById(R.id.header_chip).setVisibility(visibility);
        // den Bildraten-Chip nur einblenden, wenn es im aktuellen Modus eine Rate gibt
        main_activity.findViewById(R.id.header_fps).setVisibility(
                visibility == View.VISIBLE && headerFpsLabel().isEmpty() ? View.GONE : visibility);
        // den Modus-Chip nur einblenden, wenn der Modus ein Symbol hat - und nicht neben dem
        // Bildraten-Chip, mit dem er sich die Luecke teilt
        main_activity.findViewById(R.id.header_mode).setVisibility(
                visibility == View.VISIBLE && headerFpsLabel().isEmpty() && headerModeIcon() != 0 ? View.VISIBLE : View.GONE);
        onScreenIcons.setHeaderVisibility(visibility);
    }

    /** Die Zoom-Stufen samt ihrer Blase. Solange das Pro-Panel offen ist, bleiben sie in jedem
     *  Fall weg - die Pille des Panels sitzt an ihrer Stelle, und die Stufen haben dort nichts
     *  zu suchen (Falk 2026-09-21). Dasselbe gilt für die Stärke-Leiste: sie sitzt auf dem Platz
     *  der Zoom-Pille und hat Vorrang (Falk 2026-09-21). Alle Stellen, die die Stufen ein- oder
     *  ausblenden, gehen hier durch, damit keine sie zurueckholt. */
    public void setZoomStepperVisibility(int visibility) {
        View view = main_activity.findViewById(R.id.zoom_stepper);
        if( view == null )
            return;
        if( main_activity.proPanelIsOpen() )
            visibility = View.INVISIBLE;
        else if( visibility == View.VISIBLE && isVideoSpeedBarOpen() )
            visibility = View.GONE;
        // Im Panorama bleibt die Pille in jedem Fall weg: dort führt die Panorama-Reihe die
        // Objektivwahl. Sonst ginge der Stepper in seinen Objektiv-Modus, sobald ein physisches
        // Objektiv festgelegt ist, und stünde hinter dem offenen Blatt (Falk, Schritt G Punkt 11).
        else if( visibility == View.VISIBLE &&
                main_activity.getApplicationInterface().getPhotoMode() == MyApplicationInterface.PhotoMode.Panorama )
            visibility = View.GONE;
        view.setVisibility(visibility);
    }

    public void layoutUI() {
        long debug_time = 0;
        if( MyDebug.LOG ) {
            Log.d(TAG, "layoutUI");
            debug_time = System.currentTimeMillis();
        }

        // Rotation model (docs/design-reference/ui-spezifikation.md section 2, docs/kickoff/
        // rotation-pixel-modell.md): the window is locked to portrait
        // (MainActivity.lock_to_portrait), so there is exactly ONE arrangement of the controls,
        // laid out below in real screen directions - nothing here depends on how the device is
        // held. Turning the device only changes ui_rotation, which turns the glyphs in place
        // (rotateGlyphs()) and never touches a layout parameter; that is what keeps every
        // control pixel-exact at its physical spot on the device, as in the Pixel Camera.
        updateUIRotation();

        final ConstraintLayout constraint_root = getConstraintRoot();
        final ConstraintSet cs = new ConstraintSet();
        cs.clone(constraint_root);

        Point display_size = new Point();
        main_activity.getApplicationInterface().getDisplaySize(display_size, true);
        this.layoutUI_display_w = display_size.x;
        this.layoutUI_display_h = display_size.y;
        if( MyDebug.LOG ) {
            Log.d(TAG, "layoutUI_display_w: " + layoutUI_display_w);
            Log.d(TAG, "layoutUI_display_h: " + layoutUI_display_h);
        }
        // the screen's width (the shorter side), which the icon row along the top is spread across
        final int display_width = Math.min(display_size.x, display_size.y);

        final float scale = main_activity.getResources().getDisplayMetrics().density;
        if( MyDebug.LOG )
            Log.d(TAG, "scale: " + scale);

        // System insets to keep clear of: the gesture bar along the bottom edge, and anything
        // along the two long edges (normally nothing in portrait; MainActivity names them after
        // the landscape edges they'd be on if the window could turn).
        int navigation_gap = main_activity.getNavigationGap();
        this.navigation_gap_left = main_activity.getNavigationGapLandscape();
        int navigation_gap_right = main_activity.getNavigationGapReverseLandscape();
        test_navigation_gap = navigation_gap;
        test_navigation_gap_landscape = navigation_gap_left;
        test_navigation_gap_reversed_landscape = navigation_gap_right;
        if( MyDebug.LOG ) {
            Log.d(TAG, "navigation_gap: " + navigation_gap);
        }

        {
            // Die Kopfzeile des Redesigns ersetzt die alte Icon-Zeile (SPEC 4.5, Schritt C).
            layoutHeader(cs, scale);

            // Die alte Icon-Zeile (settings/popup/exposure, die Zustands-Icons, trash/share und
            // die Fernbedienungs-Anzeige) ist abgerissen. Was sie an Funktionen trug, steht im
            // Einstellungen-Blatt, in den vollen Einstellungen oder im Pro-Panel; die reinen
            // Zustandsanzeigen sitzen in der Kopfzeile (layoutHeader()).
            View view;
            int view_id;

            // The three bottom rows - shutter row, mode bar, photo/video switch - each hang off
            // the bottom edge on their own, at fixed distances measured off the reference
            // screenshot (ui-spezifikation.md section 6: centres 241 / 149 / 85 dp from the
            // bottom edge, of which 24 dp is the gesture inset). Deliberately NOT chained to one
            // another: nothing that appears or disappears may ever push another row about
            // (Falk, 2026-09-17). The mode bar takes the outermost slot below the shutter, as in
            // the reference.
            view = main_activity.findViewById(R.id.take_photo);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            pinToBottom(cs, view_id, navigation_gap, (int) (167 * scale + 0.5f)); // 241 dp centre - 50 dp half view - 24 dp inset

            // The shutter's neighbours: camera switch on its right, gallery thumbnail on its
            // left, each the reference distance from the shutter's centre and lined up with it
            // rather than with the screen edge. The buttons that take over those two spots while
            // recording video (pause / photo snapshot) or a panorama (cancel) are placed the
            // same way, so nothing shifts when they appear.
            view = main_activity.findViewById(R.id.switch_camera);
            flankShutter(cs, view, true, scale);

            view = main_activity.findViewById(R.id.gallery);
            flankShutter(cs, view, false, scale);

            // the multi-camera switch sits just above the camera switch, lined up with it
            view = main_activity.findViewById(R.id.switch_multi_camera);
            view_id = view.getId();
            resetView(cs, view);
            placeAbove(cs, view_id, R.id.switch_camera, (int) (5 * scale + 0.5f));
            cs.connect(view_id, ConstraintSet.LEFT, R.id.switch_camera, ConstraintSet.LEFT, 0);
            cs.connect(view_id, ConstraintSet.RIGHT, R.id.switch_camera, ConstraintSet.RIGHT, 0);

            // panorama_lens_row (containing the "wide" and "1x" buttons) sits centred above the
            // shutter, with a gap so the two don't visually merge (Falk confirmed he wants it
            // tied to the shutter rather than floating at a fixed screen spot). The gap between
            // the two buttons is a divider set in the XML.
            view = main_activity.findViewById(R.id.panorama_lens_row);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            placeAbove(cs, view_id, R.id.take_photo, (int) (16 * scale + 0.5f));

            // The mode bar, below the shutter row. Its labels are plain TextViews that never
            // turn: when the device is held on its side they read sideways, printed on the
            // fixed layout exactly as in the reference (ui-spezifikation.md section 2).
            view = main_activity.findViewById(R.id.mode_bar);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            pinToBottom(cs, view_id, navigation_gap, (int) (109 * scale + 0.5f)); // reference pill bottom edge 133 dp from the screen edge - 24 dp inset

            // The left dock button, at the same height as the switch's own row (Kickoff 3.1):
            // centre 40 dp from the left screen edge and 85 dp from the bottom, i.e. a 16 dp
            // left margin for the 48 dp circle, and 85 - 24 - 24 = 37 dp above the gesture
            // inset, exactly like the switch. Not chained to any other view.
            view = main_activity.findViewById(R.id.settings_sheet_button);
            view_id = view.getId();
            resetView(cs, view);
            cs.connect(view_id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, (int) (16 * scale + 0.5f));
            pinToBottom(cs, view_id, navigation_gap, (int) (37 * scale + 0.5f));

            // The photo/video switch is the outermost element (the reference's bottom row; its
            // other two buttons come with audit step 2).
            view = main_activity.findViewById(R.id.photo_video_switch);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            pinToBottom(cs, view_id, navigation_gap, (int) (37 * scale + 0.5f)); // 85 dp centre - 24 dp half view - 24 dp inset

            // The right dock button (the pro panel, Kickoff 3): the mirror image of the left one,
            // same row and same size.
            view = main_activity.findViewById(R.id.pro_panel_button);
            view_id = view.getId();
            resetView(cs, view);
            cs.connect(view_id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, (int) (16 * scale + 0.5f));
            pinToBottom(cs, view_id, navigation_gap, (int) (37 * scale + 0.5f));

            // The speed button of Zeitraffer/Zeitlupe stands in the same slot (the pro panel button
            // is out while filming, so only one of the two is ever up), and the row of rates it
            // opens takes the zoom pill's slot - both as in the reference.
            view = main_activity.findViewById(R.id.video_speed_button);
            view_id = view.getId();
            resetView(cs, view);
            cs.connect(view_id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, (int) (16 * scale + 0.5f));
            pinToBottom(cs, view_id, navigation_gap, (int) (37 * scale + 0.5f));

            view = main_activity.findViewById(R.id.video_speed_bar);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            placeAbove(cs, view_id, R.id.take_photo, (int) (29 * scale + 0.5f)); // the zoom pill's slot

            // The pro panel's pill sits exactly where the zoom pill sits (the two are never shown
            // at the same time), its value badge above it, and its tab row in the dock row's slot
            // at the very bottom - in the reference the Pro row is the lowest line of the screen
            // and the dock row (with the zoom pill and the mode bar) is gone while it is up
            // (measured: tab row bottom edge 59 dp above the screen edge, Falk 2026-09-21).
            view = main_activity.findViewById(R.id.pro_panel_pill);
            view_id = view.getId();
            resetView(cs, view);
            // the pill is centred on the screen (reference, measured: 69 dp from each edge);
            // the round auto button no longer sits beside it but next to the badge above it
            // (Falk 2026-09-21)
            centerHorizontally(cs, view_id);
            placeAbove(cs, view_id, R.id.take_photo, (int) (29 * scale + 0.5f));

            view = main_activity.findViewById(R.id.pro_panel_badge);
            view_id = view.getId();
            resetView(cs, view);
            // the badge is centred on the screen, not over the pill (Falk 2026-09-21)
            centerHorizontally(cs, view_id);
            placeAbove(cs, view_id, R.id.pro_panel_pill, (int) (12 * scale + 0.5f));

            view = main_activity.findViewById(R.id.pro_panel_auto);
            view_id = view.getId();
            resetView(cs, view);
            // right beside the pill, centred on it (reference, measured: 8 dp right of the pill's
            // right edge, 48 dp circle - not above it next to the badge, Falk 2026-09-21)
            cs.connect(view_id, ConstraintSet.LEFT, R.id.pro_panel_pill, ConstraintSet.RIGHT,
                    (int) (8 * scale + 0.5f));
            cs.connect(view_id, ConstraintSet.TOP, R.id.pro_panel_pill, ConstraintSet.TOP, 0);
            cs.connect(view_id, ConstraintSet.BOTTOM, R.id.pro_panel_pill, ConstraintSet.BOTTOM, 0);

            view = main_activity.findViewById(R.id.pro_panel_tabs_scroll);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            pinToBottom(cs, view_id, navigation_gap, (int) (35 * scale + 0.5f));

            // die Fokus-Modi schweben ueber dem Band, direkt ueber dem Griffstueck (Falk
            // 2026-09-21) - sichtbar sind sie nur im Reiter "Fokus" (ProPanel)
            view = main_activity.findViewById(R.id.pro_panel_focus_modes_scroll);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            placeAbove(cs, view_id, R.id.pro_panel_handle, (int) (12 * scale + 0.5f));

            // the dark band behind the Pro row (reference: the lowest zone of the Pro view lies
            // on its own surface, its top edge ~16 dp above the tab row's top - measured)
            view = main_activity.findViewById(R.id.pro_panel_sheet);
            view_id = view.getId();
            resetView(cs, view);
            cs.connect(view_id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
            cs.connect(view_id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
            cs.connect(view_id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
            cs.connect(view_id, ConstraintSet.TOP, R.id.pro_panel_tabs_scroll, ConstraintSet.TOP,
                    -(int) (36 * scale + 0.5f));

            // the drag handle above the tab row - the same piece as on the settings sheet, a
            // swipe down on it closes the panel (measured: 20 dp above the row)
            view = main_activity.findViewById(R.id.pro_panel_handle);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            placeAbove(cs, view_id, R.id.pro_panel_tabs_scroll, (int) (20 * scale + 0.5f));

            // the zoom steps pill, centred above the shutter: measured in the reference, its
            // bottom edge is 36 dp above the shutter ring, and the 86 dp ring sits 7 dp inside
            // the 100 dp take_photo view
            view = main_activity.findViewById(R.id.zoom_stepper);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            placeAbove(cs, view_id, R.id.take_photo, (int) (29 * scale + 0.5f));

            // the wheel's value badge floats above the pill (its position when the device is
            // held on its side isn't in the reference - it simply stays there and turns)
            view = main_activity.findViewById(R.id.zoom_badge);
            view_id = view.getId();
            resetView(cs, view);
            centerHorizontally(cs, view_id);
            placeAbove(cs, view_id, R.id.zoom_stepper, (int) (12 * scale + 0.5f));

            view = main_activity.findViewById(R.id.pause_video);
            flankShutter(cs, view, true, scale);

            view = main_activity.findViewById(R.id.cancel_panorama);
            flankShutter(cs, view, true, scale);

            // switch_video stays hidden (see activity_main.xml), so it isn't positioned any more

            view = main_activity.findViewById(R.id.take_photo_when_video_recording);
            flankShutter(cs, view, false, scale);

            // The manual focus seekbars stand on end along the left edge (see standOnEnd()),
            // the focus bracketing target seekbar to the right of the focus seekbar. They keep
            // the spot they had above the old zoom seekbar (removed with the fine adjustment
            // wheel): its 50 dp box stood 20 dp above the gesture inset, turned by 90 degrees.
            focus_seekbars_bottom_margin = (int) (120 * scale + 0.5f) + navigation_gap;

            view = main_activity.findViewById(R.id.focus_seekbar);
            view_id = view.getId();
            resetView(cs, view);
            cs.connect(view_id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
            cs.connect(view_id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);

            view = main_activity.findViewById(R.id.focus_bracketing_target_seekbar);
            view_id = view.getId();
            resetView(cs, view);
            cs.connect(view_id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
            cs.connect(view_id, ConstraintSet.LEFT, R.id.focus_seekbar, ConstraintSet.RIGHT, 0);

            applyFocusSeekbarsRotation(cs);
        }

        // While the pro panel is open, four of these seekbars live in the panel's pill, where the
        // panel owns their geometry: they are no longer children of sliders_container, so the
        // RelativeLayout.LayoutParams cast below would throw (2026-09-21).
        if( !main_activity.proPanelIsOpen() )
        {
            // the exposure/ISO/... sliders: limit the width so they fit on screen (the limit only
            // ever bites on small devices, e.g. Galaxy Nexus or smaller)
            int width_dp = 250;
            int max_width_dp = getMaxHeightDp(true);
            if( width_dp > max_width_dp )
                width_dp = max_width_dp;
            if( MyDebug.LOG )
                Log.d(TAG, "width_dp: " + width_dp);
            int height_dp = 50;
            int width_pixels = (int) (width_dp * scale + 0.5f); // convert dps to pixels
            int height_pixels = (int) (height_dp * scale + 0.5f); // convert dps to pixels

            // the container is centred on screen by the XML; shift it down so it doesn't sit
            // right over the middle of the preview. It doesn't turn with the device (an old
            // Open Camera element, see glyphViews()).
            View view = main_activity.findViewById(R.id.sliders_container);
            view.setTranslationX(0.0f);
            view.setTranslationY(2*height_pixels);

            view = main_activity.findViewById(R.id.exposure_seekbar);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);

            view = main_activity.findViewById(R.id.iso_seekbar);
            lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);

            view = main_activity.findViewById(R.id.exposure_time_seekbar);
            lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);

            view = main_activity.findViewById(R.id.white_balance_seekbar);
            lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);
        }

        // apply every ConstraintSet change queued up above in a single pass, rather than
        // relaying out after each view (also matches how the old RelativeLayout code only ever
        // took visible effect once all the addRule() calls for that layoutUI() invocation had
        // been made)
        cs.applyTo(constraint_root);

        {
            // the glyphs (including any the zoom stepper has just rebuilt) take the current
            // rotation straight away - the animated turn is only for when the device is turned
            rotateGlyphs(false);
            setTakePhotoIcon();
            // no need to call setSwitchCameraContentDescription()
            // An open pro panel keeps the zoom pill, the mode bar, the photo/video switch and the
            // app's own focus seekbar hidden (Kickoff 3) - after the visibility logic above, which
            // sets them again on every pass. The panel only exists once it has been opened, hence
            // the null-safe accessor (getProPanel() would build views during a layout pass).
            ProPanel proPanel = main_activity.getProPanelOrNull();
            if( proPanel != null )
                proPanel.applyHiddenWhileOpen();
        }

        if( MyDebug.LOG ) {
            Log.d(TAG, "layoutUI: total time: " + (System.currentTimeMillis() - debug_time));
        }
    }

    /** The seekbars are ordinary horizontal SeekBars stood on end (rotated 90 degrees) along
     *  the left edge - Android has poor support for a vertical seekbar. View.setRotation()
     *  doesn't affect the layout bounds, so as a hack the margins are shifted by half the
     *  difference of width and height, which puts the rotated view where its bounds appear to
     *  be. For this to work, the view must have a specified width (which can be computed
     *  programmatically), rather than having both left and right sides aligned to another
     *  view. Margins only take effect on sides that already have a connect() on this view.
     *  @param left_margin   Wanted visual gap to whatever the view's left side is connected to.
     *  @param bottom_margin Wanted visual gap to whatever the view's bottom side is connected to.
     */
    private void standOnEnd(ConstraintSet cs, View view, int left_margin, int bottom_margin) {
        view.setRotation(90);
        int view_id = view.getId();
        // width/height are only ever set from static XML (or programmatically, for the focus
        // seekbars) for these views, so reading them here rather than from the pending
        // ConstraintSet, which doesn't track plain dimensions the same way, is safe
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int diff = (layoutParams.width-layoutParams.height)/2;
        if( MyDebug.LOG )
            Log.d(TAG, "diff: " + diff);
        cs.setMargin(view_id, ConstraintSet.LEFT, left_margin - diff);
        cs.setMargin(view_id, ConstraintSet.BOTTOM, bottom_margin + diff);
    }

    /** Adjusts just the two focus seekbars' rotation/margins on an existing (not yet applied)
     *  ConstraintSet - used both from within layoutUI() (sharing its pending ConstraintSet) and
     *  standalone from setFocusSeekbarsRotation() (e.g. called from DrawPreview after a width
     *  change), which builds and applies its own ConstraintSet around this.
     */
    private void applyFocusSeekbarsRotation(ConstraintSet cs) {
        standOnEnd(cs, main_activity.findViewById(R.id.focus_seekbar), navigation_gap_left, focus_seekbars_bottom_margin);
        // the focus bracketing target seekbar sits to the right of the focus seekbar, so needs no inset
        standOnEnd(cs, main_activity.findViewById(R.id.focus_bracketing_target_seekbar), 0, focus_seekbars_bottom_margin);
    }

    void setFocusSeekbarsRotation() {
        ConstraintLayout constraint_root = getConstraintRoot();
        ConstraintSet cs = new ConstraintSet();
        cs.clone(constraint_root);
        applyFocusSeekbarsRotation(cs);
        cs.applyTo(constraint_root);
    }

    /** Set icons for taking photos vs videos.
     *  Also handles content descriptions for the take photo button and switch video button.
     */
    public void setTakePhotoIcon() {
        if( MyDebug.LOG )
            Log.d(TAG, "setTakePhotoIcon()");
        if( main_activity.getPreview() != null ) {
            ShutterButton.State state;
            int switch_video_content_description;
            if( main_activity.getPreview().isVideo() ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "set icon to video");
                state = main_activity.getPreview().isVideoRecording() ? ShutterButton.State.VIDEO_RECORDING : videoShutterState();
                switch_video_content_description = R.string.switch_to_photo;
            }
            else if( main_activity.getApplicationInterface().getPhotoMode() == MyApplicationInterface.PhotoMode.Panorama &&
                    main_activity.getApplicationInterface().getGyroSensor().isRecording() ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "set icon to recording panorama");
                state = ShutterButton.State.PANORAMA_RECORDING;
                switch_video_content_description = R.string.switch_to_video;
            }
            else {
                if( MyDebug.LOG )
                    Log.d(TAG, "set icon to photo");
                state = ShutterButton.State.PHOTO;
                switch_video_content_description = R.string.switch_to_video;
            }
            setShutterState(state, false);

            PhotoVideoSwitch photoVideoSwitch = main_activity.findViewById(R.id.photo_video_switch);
            photoVideoSwitch.setVideo(main_activity.getPreview().isVideo(), true);
            photoVideoSwitch.setContentDescription( main_activity.getResources().getString(switch_video_content_description) );

            ImageButton view = main_activity.findViewById(R.id.switch_video);
            view.setContentDescription( main_activity.getResources().getString(switch_video_content_description) );
            // monochrome glyphs rather than the old blue take_photo/take_video icons, to match
            // the Pixel Camera reference (docs/design-reference/pixel-camera-screenshots.md)
            int resource = main_activity.getPreview().isVideo() ? R.drawable.ic_photo_camera_white_48dp : R.drawable.ic_videocam_white_48dp;
            view.setImageResource(resource);
            view.setTag(resource); // for testing
        }
    }

    /** Der Zustand des Auslösers, solange nicht aufgenommen wird: rot in allen drei Video-Arten,
     *  mit dem Symbol des Modus in Zeitraffer und Zeitlupe (Referenz 4, Falk 2026-09-21). Auch
     *  MyApplicationInterface.stoppedVideo() holt sich hierüber den Zustand nach dem Stopp. */
    public ShutterButton.State videoShutterState() {
        String speed_mode = getVideoSpeedMode();
        if( MyApplicationInterface.VIDEO_SPEED_SLOW.equals(speed_mode) )
            return ShutterButton.State.VIDEO_SLOW;
        if( MyApplicationInterface.VIDEO_SPEED_LAPSE.equals(speed_mode) )
            return ShutterButton.State.VIDEO_LAPSE;
        return ShutterButton.State.VIDEO;
    }

    /** Puts the shutter button into the given state, including its content description. Video
     *  start/stop pass animate=true so the core morphs between disc and stop square; they call
     *  this directly rather than setTakePhotoIcon(), because at that moment
     *  Preview.isVideoRecording() doesn't yet reflect the new state.
     */
    public void setShutterState(ShutterButton.State state, boolean animate) {
        shutterButton.setState(state, animate);
        int content_description;
        switch( state ) {
            case VIDEO:
            case VIDEO_LAPSE:
            case VIDEO_SLOW:
                content_description = R.string.start_video;
                break;
            case VIDEO_RECORDING:
                content_description = R.string.stop_video;
                break;
            case PANORAMA_RECORDING:
                content_description = R.string.finish_panorama;
                break;
            default:
                content_description = R.string.take_photo;
                break;
        }
        View view = main_activity.findViewById(R.id.take_photo);
        view.setContentDescription( main_activity.getResources().getString(content_description) );
    }

    /** The "heavy click" the reference gives on the shutter (photo taken, video started or
     *  stopped), if haptic feedback is allowed in the settings. HapticFeedbackConstants.CONFIRM
     *  is what Android maps to VibrationEffect.EFFECT_HEAVY_CLICK, and unlike driving the
     *  Vibrator directly it needs no VIBRATE permission.
     */
    public void performHeavyClick() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        if( sharedPreferences.getBoolean(PreferenceKeys.AllowHapticFeedbackPreferenceKey, true) ) {
            View view = main_activity.findViewById(R.id.take_photo);
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.R )
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            else
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    /** Wires up the mode carousel (the photo modes while taking photos, Video / Zeitlupe /
     *  Zeitraffer while filming), the horizontal replacement for picking the mode out of the popup
     *  menu - see docs/design-reference/pixel-camera-screenshots.md. Only modes the app actually
     *  implements are offered here; a tap is handled by the bar itself (ModeBar.Listener), which
     *  first slides the entry into the middle.
     */
    private void setupModeBar() {
        ModeBar mode_bar = main_activity.findViewById(R.id.mode_bar);
        mode_bar.setListener(new ModeBar.Listener() {
            @Override
            public void onEntryPicked(View entry) {
                onModeBarEntryPicked(entry);
            }
        });
        // The bar only lists the sub-modes of whatever the PhotoVideoSwitch is set to, and
        // photo <-> video isn't switched here at all. While taking photos that is "Foto" (the
        // ordinary photo) and the photo modes that Schritt C left unreachable when it removed the
        // popup - Falk's decision (2026-09-17, revising audit step 1, and 2026-09-22 for the
        // order): "Foto" first, then the special modes. A "Foto" entry is needed to get back out
        // of any of them. Serienbild stays out (Falk: holding the shutter is enough), the two
        // bracketing modes live in the settings sheet, and since Schritt E so do HDR, DRO and
        // Nachtsicht - Nachtsicht sits in the flash row of the photo sheet now, HDR/DRO in its
        // Pro tab.
        addPhotoModeEntry(R.id.mode_bar_photo, "preference_photo_mode_std");
        addPhotoModeEntry(R.id.mode_bar_panorama, "preference_photo_mode_panorama");
        addPhotoModeEntry(R.id.mode_bar_bokeh, "preference_photo_mode_x_bokeh");
        // While filming it is "Video" (the ordinary one, the way back out of the two special
        // modes), "Zeitlupe" and "Zeitraffer" (SPEC 4.6), in the order the reference lists them.
        addVideoSpeedModeEntry(R.id.mode_bar_video, MyApplicationInterface.VIDEO_SPEED_OFF);
        addVideoSpeedModeEntry(R.id.mode_bar_slow_motion, MyApplicationInterface.VIDEO_SPEED_SLOW);
        addVideoSpeedModeEntry(R.id.mode_bar_timelapse, MyApplicationInterface.VIDEO_SPEED_LAPSE);
    }

    /** What a tap on a mode bar entry does once the carousel has brought it into the middle: the
     *  entry on its own says nothing, its id is what names the mode. */
    private void onModeBarEntryPicked(View entry) {
        String photo_mode = mode_bar_photo_modes.get(entry.getId());
        if( photo_mode != null ) {
            switchPhotoMode(photo_mode); // refreshes the bar itself
            return;
        }
        String speed_mode = mode_bar_video_speeds.get(entry.getId());
        if( speed_mode != null )
            switchVideoSpeedMode(speed_mode);
    }

    private void addPhotoModeEntry(int view_id, String photo_mode_preference_value) {
        mode_bar_photo_modes.put(view_id, photo_mode_preference_value);
    }

    private void addVideoSpeedModeEntry(int view_id, String speed_mode) {
        mode_bar_video_speeds.put(view_id, speed_mode);
    }

    /** Centre-to-centre distance between the shutter and the buttons flanking it (gallery,
     *  camera switch), measured off the reference screenshot - see
     *  docs/design-reference/ui-spezifikation.md 7.2. (The detail spec's 64 dp can't be right:
     *  the neighbour would sit inside the 86 dp ring.) */
    private static final int shutter_neighbour_distance_dp = 124;

    /** Places a view beside the shutter on its row, its centre shutter_neighbour_distance_dp from
     *  the shutter's centre: the margin is whatever is left of that distance after half the
     *  shutter view and half this view, so views of different sizes still line up on the same
     *  centre. Tying both top and bottom to the shutter centres the (fixed-size) view on the
     *  shutter's band, so the neighbours follow the shutter wherever it sits rather than the
     *  screen edge.
     *  @param right_of_shutter true for the right-hand neighbour, false for the left-hand one.
     */
    private void flankShutter(ConstraintSet cs, View view, boolean right_of_shutter, float scale) {
        int view_id = view.getId();
        resetView(cs, view);
        cs.connect(view_id, ConstraintSet.TOP, R.id.take_photo, ConstraintSet.TOP, 0);
        cs.connect(view_id, ConstraintSet.BOTTOM, R.id.take_photo, ConstraintSet.BOTTOM, 0);
        // all these views are square, so the width is the size
        int shutter_size = main_activity.findViewById(R.id.take_photo).getLayoutParams().width;
        int view_size = view.getLayoutParams().width;
        int distance = (int) (shutter_neighbour_distance_dp * scale + 0.5f); // convert dps to pixels
        int margin = distance - shutter_size/2 - view_size/2;
        if( right_of_shutter )
            cs.connect(view_id, ConstraintSet.LEFT, R.id.take_photo, ConstraintSet.RIGHT, margin);
        else
            cs.connect(view_id, ConstraintSet.RIGHT, R.id.take_photo, ConstraintSet.LEFT, margin);
    }

    /** Applies a new photo mode preference and refreshes everything that caches it - same steps
     *  PopupView.changePhotoMode() takes when the mode is picked from the popup menu. The photo
     *  sheet calls this too (Schritt E): HDR and DRO are picked there now, and that needs the
     *  camera to be rebuilt. A pick by hand also ends the night automation - whoever chooses a
     *  mode wants that mode, not the one darkness would have picked.
     *  HDR and DRO get the light cleared with it (Schritt F, Falk 2026-09-22): HDR cannot use a
     *  real flash at all (the exposure bracket holds AE off), and neither of the two is offered a
     *  night button - so they are entered with the light off, and DRO's own buttons can turn the
     *  flash back on. */
    public void switchPhotoMode(String photo_mode_preference_value) {
        boolean clear_light = "preference_photo_mode_hdr".equals(photo_mode_preference_value)
                || "preference_photo_mode_dro".equals(photo_mode_preference_value);
        // The night belongs to Foto and Panorama alone (Schritt F): every other mode brings a
        // session of its own (Porträt, the other extensions) or an exposure of its own (HDR, DRO),
        // and a night left on from before would silently take it over - getPhotoMode() rates the
        // mode higher than the light. So entering one puts the night off; Foto and Panorama keep
        // it, and the light row turns it on there.
        boolean keep_night = "preference_photo_mode_std".equals(photo_mode_preference_value)
                || "preference_photo_mode_panorama".equals(photo_mode_preference_value);
        applyLightSettings(photo_mode_preference_value, clear_light ? "flash_off" : null, false,
                (clear_light || !keep_night) ? Boolean.FALSE : null);
    }

    /** The one place that writes the light of the photo sheet (Schritt E): which photo mode, which
     *  flash value, and whether the night automation stays on. All of it ends in a camera rebuild,
     *  so it is written together and rebuilt once.
     *  @param flash_value the flash value to store, null to leave the flash as it is.
     *  @param night_auto whether the night extension should keep switching itself by darkness.
     *  @param night_on whether the night extension is on, null to leave it as it is (Schritt F:
     *                  it is a light state, no longer a mode preference).
     */
    public void applyLightSettings(String photo_mode_preference_value, String flash_value, boolean night_auto, Boolean night_on) {
        MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        // Blitz-Automatik und festes ISO schließen sich aus: mit abgeschalteter Belichtungsautomatik
        // nimmt die Kamera das "flash_auto" aus ihren Werten (Preview.setupCameraParameters), der
        // gespeicherte Wert liefe ins Leere und der Knopf im Blatt spränge zurück (Falk: "Auto Blitz
        // lässt sich nicht auswählen", Schritt G Punkt 5). Wer die Automatik wählt, meint sie.
        if( "flash_auto".equals(flash_value) ) {
            applicationInterface.clearISOPref();
            applicationInterface.clearExposureTimePref();
        }
        // Nacht und physisches Objektiv schließen sich aus: die Nacht-Erweiterung läuft auf der
        // Hauptkamera, ein Weitwinkel-Nachtbild wäre still ein 1×-Bild (CameraController2 holt die
        // Erweiterungs-Charakteristiken nur ohne festgelegtes physisches Objektiv, Zeile 1215).
        // Wer die Nacht wählt, bekommt 1× - der Neuaufbau unten nimmt die zurückgenommene
        // Festlegung mit (Schritt G, Punkt 8).
        if( (night_on != null && night_on) || night_auto ) {
            if( applicationInterface.getCameraIdSPhysicalPref() != null )
                applicationInterface.setCameraIdPref(main_activity.getActualCameraId(), null);
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
        editor.putString(PreferenceKeys.PhotoModePreferenceKey, photo_mode_preference_value);
        editor.putBoolean(PreferenceKeys.NightAutoPreferenceKey, night_auto);
        if( night_on != null )
            editor.putBoolean(PreferenceKeys.NightOnPreferenceKey, night_on);
        editor.apply();
        if( flash_value != null )
            main_activity.getApplicationInterface().setFlashPref(flash_value);
        main_activity.getApplicationInterface().getDrawPreview().updateSettings(); // because we cache the photomode
        main_activity.updateForSettings(true, null, false, true);
        updateModeBar();
        updateNightAutoCheck();
    }

    /** Nimmt die Nacht zurück, ohne die Kamera neu aufzubauen: der Aufrufer baut sie danach selbst
     *  auf und nimmt den neuen Zustand dabei mit (Objektivwechsel im Panorama, Schritt G Punkt 8).
     *  Ohne das bliebe die Nacht-Automatik weiter angemeldet und schaltete die Nacht gleich wieder
     *  ein. */
    public void clearNightState() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
        editor.putBoolean(PreferenceKeys.NightAutoPreferenceKey, false);
        editor.putBoolean(PreferenceKeys.NightOnPreferenceKey, false);
        editor.apply();
        main_activity.getApplicationInterface().getDrawPreview().updateSettings(); // der Photomodus ist gecacht
        updateModeBar();
        updateNightAutoCheck();
    }

    /** The same write from the light row of the sheet (Schritt F), which has no photo mode of its
     *  own: the light buttons only set the light, the mode standing stays - that is what lets the
     *  night run inside the panorama. */
    public void lightSettings(String flash_value, boolean night_auto, Boolean night_on) {
        applyLightSettings(
                PreferenceManager.getDefaultSharedPreferences(main_activity)
                        .getString(PreferenceKeys.PhotoModePreferenceKey, "preference_photo_mode_std"),
                flash_value, night_auto, night_on);
    }

    // "Nacht auto" (Schritt E): while the button is picked, the night extension switches itself by
    // how dark the scene is. The video torch reads the same ISO through a callback, but that
    // callback is a single slot and belongs to the video flash - so this asks the camera directly,
    // every couple of seconds, and only while the button is on.
    /** How often the measured ISO is looked at. Reading it costs nothing: it is the value the
     *  camera already reported for the last preview frame (CameraController.captureResultIso()). */
    private static final long NIGHT_AUTO_CHECK_MS = 2000;
    /** Every switch rebuilds the whole camera, so two of them never follow each other faster than
     *  this - the measurement settles within a second, a rebuild takes longer. */
    private static final long NIGHT_AUTO_MIN_DWELL_MS = 10000;
    /** How many checks in a row have to say "bright" before the night extension goes off again:
     *  the way back is the risky one, because the extension itself shoots with long exposures, so a
     *  single low ISO right after the switch would not mean anything. Three checks are ~6 s. */
    private static final int NIGHT_AUTO_BRIGHT_CHECKS = 3;

    private final Handler night_auto_handler = new Handler();
    private int night_auto_bright_checks;
    private long night_auto_last_switch;
    private final Runnable night_auto_runnable = new Runnable() {
        @Override
        public void run() {
            checkNightAuto();
            if( nightAutoWanted() )
                night_auto_handler.postDelayed(this, NIGHT_AUTO_CHECK_MS);
            else
                night_auto_bright_checks = 0;
        }
    };

    /** Whether the night extension should keep switching itself: only with the button picked, only
     *  in photo mode (the night extension is a photo mode), and only where the camera has the
     *  extension at all. */
    private boolean nightAutoWanted() {
        Preview preview = main_activity.getPreview();
        if( preview == null || preview.isVideo() )
            return false;
        if( !preview.supportsCameraExtension(CameraExtensionCharacteristics.EXTENSION_NIGHT) )
            return false;
        return PreferenceManager.getDefaultSharedPreferences(main_activity)
                .getBoolean(PreferenceKeys.NightAutoPreferenceKey, false);
    }

    /** Starts or stops the check above and treats the rebuild that just happened as the last
     *  switch. Called when the light row writes its preferences; afterwards the check keeps itself
     *  alive while the button is on, so nothing has to be hooked into the activity's lifecycle. */
    private void updateNightAutoCheck() {
        night_auto_handler.removeCallbacks(night_auto_runnable);
        night_auto_bright_checks = 0;
        night_auto_last_switch = SystemClock.uptimeMillis();
        if( nightAutoWanted() )
            night_auto_handler.postDelayed(night_auto_runnable, NIGHT_AUTO_CHECK_MS);
    }

    /** Asks the measured ISO whether the night extension should switch. Into the night as soon as
     *  the scene is clearly dark - but only from a mode that offers the light (Foto and Panorama,
     *  Schritt F): after HDR, DRO or Porträt the mode the user picked wins. Out of the night only
     *  once the scene has been clearly bright for a stretch (see NIGHT_AUTO_BRIGHT_CHECKS).
     *  It switches the night *state*, not the photo mode, so a panorama stays a panorama - but
     *  never while one is recording: every switch rebuilds the camera (MainActivity), and that
     *  would tear the running panorama apart.
     *  ponytail: eine Szene, in der die Nacht-Erweiterung selbst mit langer Belichtung arbeitet,
     *  kann dauerhaft unter AUTO_ISO_OFF messen - dann bleibt Nacht an, bis Falk sie abwaehlt.
     */
    private void checkNightAuto() {
        if( !nightAutoWanted() )
            return;
        Preview preview = main_activity.getPreview();
        // Die Nacht gibt es nur in Foto und Panorama (Schritt F): im Video antwortet
        // getTopLevelPhotoMode() zwar Standard, die Erweiterung laeuft dort aber nicht
        // (MyApplicationInterface.getPhotoMode()) - die Automatik baute die Kamera dort sonst neu
        // auf, ohne dass sich etwas aendert (Schritt G).
        if( preview.isVideo() )
            return;
        CameraController controller = preview.getCameraController();
        if( controller == null || !controller.captureResultHasIso() )
            return; // no camera open, or nothing measured yet
        if( preview.isTakingPhoto() )
            return; // never rebuild the camera under a running capture
        MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        if( applicationInterface.getGyroSensor().isRecording() )
            return; // ... and never under a running panorama either
        int iso = controller.captureResultIso();
        MyApplicationInterface.PhotoMode photo_mode = applicationInterface.getTopLevelPhotoMode();
        boolean light_offered = photo_mode == MyApplicationInterface.PhotoMode.Standard ||
                photo_mode == MyApplicationInterface.PhotoMode.Panorama;
        boolean in_night = applicationInterface.isNightModeOn();
        if( in_night ) {
            if( !light_offered )
                return;
            if( iso < Preview.AUTO_ISO_OFF )
                night_auto_bright_checks++;
            else
                night_auto_bright_checks = 0;
            if( night_auto_bright_checks < NIGHT_AUTO_BRIGHT_CHECKS )
                return;
        }
        else {
            night_auto_bright_checks = 0;
            if( !light_offered || !Preview.autoLightOn(false, iso) )
                return;
        }
        if( SystemClock.uptimeMillis() < night_auto_last_switch + NIGHT_AUTO_MIN_DWELL_MS )
            return;
        if( MyDebug.LOG )
            Log.d(TAG, "measured ISO " + iso + " -> night " + (in_night ? "off" : "on"));
        // the mode stays what it is - only the light changes
        applyLightSettings(
                PreferenceManager.getDefaultSharedPreferences(main_activity)
                        .getString(PreferenceKeys.PhotoModePreferenceKey, "preference_photo_mode_std"),
                null, true, !in_night);
    }

    /** Shows the sub-modes of the current top-level mode, highlights the active one and puts it in
     *  the middle of the carousel: Foto and the other photo modes while taking photos, Video,
     *  Zeitlupe and Zeitraffer while filming. A mode the camera cannot do is left out rather than
     *  offered dead (a camera without high speed video gets no Zeitlupe): Panorama not with the
     *  selfie camera (MainActivity.supportsPanorama()), Porträt only where the camera reports the
     *  extension. The bar's own place is fixed (layoutUI()), so an empty bar moves nothing.
     */
    public void updateModeBar() {
        boolean is_video = main_activity.getPreview().isVideo();
        // the top-level mode: night counts as the plain photo mode there, it is a light state
        // (Schritt F) - without this a night photo would leave the carousel unmarked
        MyApplicationInterface.PhotoMode photo_mode = main_activity.getApplicationInterface().getTopLevelPhotoMode();

        TextView[] photo_entries = {
                main_activity.findViewById(R.id.mode_bar_photo),
                main_activity.findViewById(R.id.mode_bar_panorama),
                main_activity.findViewById(R.id.mode_bar_bokeh) };
        boolean[] photo_offered = {
                !is_video,
                !is_video && main_activity.supportsPanorama(),
                !is_video && main_activity.supportsCameraExtension(CameraExtensionCharacteristics.EXTENSION_BOKEH) };
        MyApplicationInterface.PhotoMode[] photo_modes = {
                MyApplicationInterface.PhotoMode.Standard,
                MyApplicationInterface.PhotoMode.Panorama,
                MyApplicationInterface.PhotoMode.X_Bokeh };
        View selected_entry = null;
        for(int i = 0; i < photo_entries.length; i++) {
            // getPhotoMode() already answers Standard where the preference names a mode the camera
            // cannot do, so an entry is only ever highlighted where it is offered
            boolean selected = photo_offered[i] && photo_mode == photo_modes[i];
            photo_entries[i].setVisibility(photo_offered[i] ? View.VISIBLE : View.GONE);
            applyModeBarStyle(photo_entries[i], selected);
            if( selected )
                selected_entry = photo_entries[i];
        }

        // The video sub-modes are for choosing a mode, not for use while filming: they hide for
        // the length of a recording, as in the reference (pixel-zeitraffer-zeitlupe-beschreibung
        // .md 5), which also keeps the rate - fixed when the file was opened - out of reach.
        boolean show_video_modes = is_video && !video_recording_ui
                && !main_activity.getPreview().isVideoRecording();
        String speed_mode = getVideoSpeedMode();
        boolean offer_slow = supportsVideoSpeedMode(MyApplicationInterface.VIDEO_SPEED_SLOW);
        boolean offer_lapse = supportsVideoSpeedMode(MyApplicationInterface.VIDEO_SPEED_LAPSE);
        TextView[] video_entries = {
                main_activity.findViewById(R.id.mode_bar_video),
                main_activity.findViewById(R.id.mode_bar_slow_motion),
                main_activity.findViewById(R.id.mode_bar_timelapse) };
        boolean[] video_offered = {
                show_video_modes,
                show_video_modes && offer_slow,
                show_video_modes && offer_lapse };
        String[] speed_modes = {
                MyApplicationInterface.VIDEO_SPEED_OFF,
                MyApplicationInterface.VIDEO_SPEED_SLOW,
                MyApplicationInterface.VIDEO_SPEED_LAPSE };
        for(int i = 0; i < video_entries.length; i++) {
            boolean selected = video_offered[i] && speed_modes[i].equals(speed_mode);
            video_entries[i].setVisibility(video_offered[i] ? View.VISIBLE : View.GONE);
            applyModeBarStyle(video_entries[i], selected);
            if( selected )
                selected_entry = video_entries[i];
        }

        if( selected_entry != null ) {
            // ohne Animation: hier kommt die Leiste entweder gerade erst hoch (Start) oder der
            // Eintrag steht schon in der Mitte (die Rast der Leiste hat ihn dorthin gebracht)
            ModeBar mode_bar = main_activity.findViewById(R.id.mode_bar);
            mode_bar.centerOn(selected_entry, false);
        }

        updateVideoSpeedButton();
        if( !is_video || video_recording_ui )
            hideVideoSpeedBar(); // the bar belongs to these two modes only, and not to a running recording
    }

    private void applyModeBarStyle(TextView view, boolean selected) {
        if( selected ) {
            view.setBackgroundResource(R.drawable.pill_selected_background);
            view.setTextColor(main_activity.getResources().getColor(R.color.md_dark_surface, null));
            view.setTypeface(null, Typeface.BOLD);
        }
        else {
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setTextColor(main_activity.getResources().getColor(R.color.md_dark_on_surface, null));
            view.setTypeface(null, Typeface.NORMAL);
        }
    }

    /* ---- Zeitraffer & Zeitlupe (SPEC 4.6) ---------------------------------------------------- */

    /** The video sub-mode the camera is set to: "off" (ordinary video), "slow" (Zeitlupe) or
     *  "lapse" (Zeitraffer). The rate itself lives in the capture rate preference, so a mode the
     *  user leaves and comes back to hands them the rate they last used there. */
    private String getVideoSpeedMode() {
        return main_activity.getApplicationInterface().getVideoSpeedMode();
    }

    /** The rates a sub-mode offers on the current camera, in the reference's order. */
    private List<Float> ratesForSpeedMode(String speed_mode) {
        MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        if( MyApplicationInterface.VIDEO_SPEED_SLOW.equals(speed_mode) )
            return applicationInterface.getSupportedSlowMotionRates();
        if( MyApplicationInterface.VIDEO_SPEED_LAPSE.equals(speed_mode) )
            return applicationInterface.getSupportedTimeLapseRates();
        return new ArrayList<>();
    }

    private boolean supportsVideoSpeedMode(String speed_mode) {
        return !ratesForSpeedMode(speed_mode).isEmpty();
    }

    /** The rate a sub-mode starts at: the one it used last if that one still fits, otherwise the
     *  reference's default (1/4x for Zeitlupe, 5x for Zeitraffer) or the camera's nearest rate. */
    private float rateForSpeedMode(String speed_mode, float stored) {
        List<Float> rates = ratesForSpeedMode(speed_mode);
        if( rates.isEmpty() )
            return 1.0f;
        boolean is_slow = MyApplicationInterface.VIDEO_SPEED_SLOW.equals(speed_mode);
        if( (is_slow && stored < 1.0f) || (!is_slow && stored > 1.0f) ) {
            for(float rate : rates) {
                if( Math.abs(rate - stored) < 1.0e-5f )
                    return rate;
            }
        }
        float preferred = is_slow ? 0.25f : 5.0f;
        float best = rates.get(0);
        for(float rate : rates) {
            if( Math.abs(rate - preferred) < Math.abs(best - preferred) )
                best = rate;
        }
        return best;
    }

    /** A rate as the reference writes it: "5×" for the time lapse, "1/4×" for the slow motion. */
    private String speedLabel(float rate) {
        if( rate < 1.0f - 1.0e-5f )
            return "1/" + Math.round(1.0f / rate) + "×";
        return Math.round(rate) + "×";
    }

    private String speedModePreferenceKey() {
        return PreferenceKeys.getVideoSpeedModePreferenceKey(main_activity.getPreview().getCameraId(),
                main_activity.getApplicationInterface().getCameraIdSPhysicalPref());
    }

    private String captureRatePreferenceKey() {
        return PreferenceKeys.getVideoCaptureRatePreferenceKey(main_activity.getPreview().getCameraId(),
                main_activity.getApplicationInterface().getCameraIdSPhysicalPref());
    }

    /** Switches the video sub-mode: writes the mode and the rate it starts at, then refreshes the
     *  camera the way a photo mode change does - the video profile, the recording fps and the high
     *  speed video size all hang off the capture rate (Preview.setupCameraParameters()).
     */
    private void switchVideoSpeedMode(String speed_mode) {
        if( main_activity.getPreview().isVideoRecording() )
            return; // the rate went into the file when it was opened, mid-recording it is too late
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(speedModePreferenceKey(), speed_mode);
        if( !MyApplicationInterface.VIDEO_SPEED_OFF.equals(speed_mode) ) {
            float stored = sharedPreferences.getFloat(captureRatePreferenceKey(), 1.0f);
            editor.putFloat(captureRatePreferenceKey(), rateForSpeedMode(speed_mode, stored));
        }
        editor.apply();
        hideVideoSpeedBar();
        main_activity.getApplicationInterface().getDrawPreview().updateSettings();
        main_activity.updateForSettings(true, null, false, true);
        updateModeBar();
        // der Auslöser trägt das Symbol des Modus (Referenz 4) - hier und nicht in
        // updateModeBar(), weil das auch beim Start einer Aufnahme läuft und den Stopp-Zustand
        // wieder überschreiben würde
        setTakePhotoIcon();
    }

    /** Picks one of the rates the row offers, without leaving the mode. */
    private void switchVideoSpeedRate(float rate) {
        if( main_activity.getPreview().isVideoRecording() )
            return;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        sharedPreferences.edit().putFloat(captureRatePreferenceKey(), rate).apply();
        main_activity.getApplicationInterface().getDrawPreview().updateSettings();
        main_activity.updateForSettings(true, null, false, true);
        buildVideoSpeedBar(); // repaint the selection
        updateVideoSpeedButton();
    }

    /** The dock button toggles the row of rates. */
    public void toggleVideoSpeedBar() {
        if( isVideoSpeedBarOpen() )
            hideVideoSpeedBar();
        else
            showVideoSpeedBar();
    }

    public boolean isVideoSpeedBarOpen() {
        return main_activity.findViewById(R.id.video_speed_bar).getVisibility() == View.VISIBLE;
    }

    /** Shows the row of rates in the zoom pill's place - the reference puts it exactly there, and
     *  the two are never up at the same time (pixel-zeitraffer-zeitlupe-beschreibung.md 2). */
    public void showVideoSpeedBar() {
        String speed_mode = getVideoSpeedMode();
        if( !main_activity.getPreview().isVideo() || main_activity.getPreview().isVideoRecording()
                || MyApplicationInterface.VIDEO_SPEED_OFF.equals(speed_mode) )
            return;
        buildVideoSpeedBar();
        main_activity.findViewById(R.id.video_speed_bar).setVisibility(View.VISIBLE);
        setZoomStepperVisibility(View.GONE);
    }

    public void hideVideoSpeedBar() {
        View bar = main_activity.findViewById(R.id.video_speed_bar);
        if( bar.getVisibility() != View.VISIBLE )
            return;
        bar.setVisibility(View.GONE);
        // the zoom pill comes back - unless it has no steps or it is not welcome right now
        setZoomStepperVisibility(getZoomStepper().hasSteps() && !inImmersiveMode() ? View.VISIBLE : View.GONE);
    }

    /** One chip per rate the mode offers, styled like the mode bar's entries - the selected one is
     *  the light pill with the dark bold label, exactly as in the reference. */
    private void buildVideoSpeedBar() {
        LinearLayout bar = main_activity.findViewById(R.id.video_speed_bar);
        bar.removeAllViews();
        String speed_mode = getVideoSpeedMode();
        float current = main_activity.getApplicationInterface().getVideoCaptureRateFactor();
        final float scale = main_activity.getResources().getDisplayMetrics().density;
        int pad_h = (int) (16 * scale + 0.5f);
        int pad_v = (int) (8 * scale + 0.5f);
        int mode_label = MyApplicationInterface.VIDEO_SPEED_SLOW.equals(speed_mode) ?
                R.string.video_speed_slow : R.string.video_speed_lapse;
        for(final float rate : ratesForSpeedMode(speed_mode)) {
            TextView chip = new TextView(main_activity);
            String label = speedLabel(rate);
            chip.setText(label);
            chip.setTextSize(14);
            chip.setPadding(pad_h, pad_v, pad_h, pad_v);
            chip.setContentDescription(main_activity.getString(mode_label) + " " + label);
            chip.setTag(rate); // for testing
            applyModeBarStyle(chip, Math.abs(rate - current) < 1.0e-5f);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchVideoSpeedRate(rate);
                }
            });
            bar.addView(chip);
        }
    }

    /** Shows the current rate and its symbol on the right dock button, and keeps the button away
     *  everywhere else - the reference has it in the two special modes only. */
    private void updateVideoSpeedButton() {
        LinearLayout button = main_activity.findViewById(R.id.video_speed_button);
        String speed_mode = getVideoSpeedMode();
        boolean show = main_activity.getPreview().isVideo()
                && !video_recording_ui
                && !main_activity.getPreview().isVideoRecording()
                && !MyApplicationInterface.VIDEO_SPEED_OFF.equals(speed_mode)
                && supportsVideoSpeedMode(speed_mode);
        button.setVisibility(show ? View.VISIBLE : View.GONE);
        if( !show )
            return;
        ImageView icon = main_activity.findViewById(R.id.video_speed_icon);
        icon.setImageResource(MyApplicationInterface.VIDEO_SPEED_SLOW.equals(speed_mode) ?
                R.drawable.ic_slow_motion_video_white_48dp : R.drawable.ic_fast_forward_white_48dp);
        TextView label = main_activity.findViewById(R.id.video_speed_label);
        label.setText(speedLabel(main_activity.getApplicationInterface().getVideoCaptureRateFactor()));
    }

    /** Meldet Anfang und Ende einer Aufnahme aus MyApplicationInterface.startingVideo() und
     *  stoppedVideo(): die Video-Einträge der Modusleiste und der Stärke-Knopf verschwinden für
     *  die Dauer der Aufnahme (siehe {@link #video_recording_ui}). */
    public void setVideoRecordingForUI(boolean recording) {
        video_recording_ui = recording;
        updateModeBar();
    }

    /** Set content description for switch camera button.
     */    public void setSwitchCameraContentDescription() {
        if( MyDebug.LOG )
            Log.d(TAG, "setSwitchCameraContentDescription()");
        if( main_activity.getPreview() != null && main_activity.getPreview().canSwitchCamera() ) {
            ImageButton view = main_activity.findViewById(R.id.switch_camera);
            int content_description;
            int cameraId = main_activity.getNextCameraId();
            switch( main_activity.getPreview().getCameraControllerManager().getFacing( cameraId  ) ) {
                case FACING_FRONT:
                    content_description = R.string.switch_to_front_camera;
                    break;
                case FACING_BACK:
                    content_description = R.string.switch_to_back_camera;
                    break;
                case FACING_EXTERNAL:
                    content_description = R.string.switch_to_external_camera;
                    break;
                default:
                    content_description = R.string.switch_to_unknown_camera;
                    break;
            }
            if( MyDebug.LOG )
                Log.d(TAG, "content_description: " + main_activity.getResources().getString(content_description));
            view.setContentDescription( main_activity.getResources().getString(content_description) );
        }
    }

    /** Set content description for pause video button.
     */
    public void setPauseVideoContentDescription() {
        if (MyDebug.LOG)
            Log.d(TAG, "setPauseVideoContentDescription()");
        ImageButton pauseVideoButton = main_activity.findViewById(R.id.pause_video);
        int content_description;
        if( main_activity.getPreview().isVideoRecordingPaused() ) {
            content_description = R.string.resume_video;
            pauseVideoButton.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
        }
        else {
            content_description = R.string.pause_video;
            pauseVideoButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        }
        if( MyDebug.LOG )
            Log.d(TAG, "content_description: " + main_activity.getResources().getString(content_description));
        pauseVideoButton.setContentDescription(main_activity.getResources().getString(content_description));
    }

    UIPlacement getUIPlacement() {
        return UIPlacement.UIPLACEMENT_TOP;
    }

    /** Die Fernbedienungs-Anzeige (kraken_icon) ist mit der Icon-Zeile entfallen (Schritt C);
     *  die Verbindung selbst läuft unverändert weiter, sie zeigt sich nur nicht mehr an. */
    public void updateRemoteConnectionIcon() {
    }

    /** Fed by MainActivity's OrientationEventListener with the device's tilt in degrees
     *  (0 upright, growing clockwise). The window never turns (MainActivity.lock_to_portrait), so
     *  this is where the app learns how the device is being held: the tilt is snapped to the
     *  nearest quarter turn, with a 60 degree hysteresis so a phone held near 45 degrees doesn't
     *  flicker between two states, and the glyphs are turned to match (rotateGlyphs()). Photos
     *  and videos don't depend on this: Preview has its own listener for their orientation.
     */
    public void onOrientationChanged(int orientation) {
		/*if( MyDebug.LOG ) {
			Log.d(TAG, "onOrientationChanged()");
			Log.d(TAG, "orientation: " + orientation);
			Log.d(TAG, "current_orientation: " + current_orientation);
		}*/
        if( orientation == OrientationEventListener.ORIENTATION_UNKNOWN )
            return;
        int diff = Math.abs(orientation - current_orientation);
        if( diff > 180 )
            diff = 360 - diff;
        // only change orientation when sufficiently changed
        if( diff > 60 ) {
            orientation = (orientation + 45) / 90 * 90;
            orientation = orientation % 360;
            if( orientation != current_orientation ) {
                this.current_orientation = orientation;
                if( MyDebug.LOG ) {
                    Log.d(TAG, "current_orientation is now: " + current_orientation);
                }
                updateUIRotation();
                rotateGlyphs(true);

                // Call DrawPreview.updateSettings() so that we reset calculations that depend on
                // getLocationOnScreen() - since the result is affected by a View's rotation, we need
                // to recompute - this also means we need to delay slightly until after the rotation
                // animation is complete.
                // Potentially we could use Animation.setAnimationListener(), but we set a separate
                // animation for every icon.
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if( MyDebug.LOG )
                            Log.d(TAG, "onOrientationChanged->postDelayed()");

                        main_activity.getApplicationInterface().getDrawPreview().updateSettings();
                    }
                }, glyph_rotation_duration_ms+20);
            }
        }
    }

    public void setImmersiveMode(final boolean immersive_mode) {
        if( MyDebug.LOG )
            Log.d(TAG, "setImmersiveMode: " + immersive_mode);
        this.immersive_mode = immersive_mode;
        if( immersive_mode ) {
            // hiding the GUI closes an open settings sheet (Kickoff 3.2)
            SettingsSheet settingsSheet = main_activity.getSettingsSheet();
            if( settingsSheet != null && settingsSheet.isOpen() )
                settingsSheet.close();
        }
        main_activity.runOnUiThread(new Runnable() {
            public void run() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                // if going into immersive mode, the we should set GONE the ones that are set GONE in showGUI(false)
                //final int visibility_gone = immersive_mode ? View.GONE : View.VISIBLE;
                final int visibility = immersive_mode ? View.GONE : View.VISIBLE;
                if( MyDebug.LOG )
                    Log.d(TAG, "setImmersiveMode: set visibility: " + visibility);
                // n.b., don't hide share and trash buttons, as they require immediate user input for us to continue
                View switchCameraButton = main_activity.findViewById(R.id.switch_camera);
                View switchMultiCameraButton = main_activity.findViewById(R.id.switch_multi_camera);
                View galleryButton = main_activity.findViewById(R.id.gallery);
                View focusSeekBar = main_activity.findViewById(R.id.focus_seekbar);
                View focusBracketingTargetSeekBar = main_activity.findViewById(R.id.focus_bracketing_target_seekbar);
                if( main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1 )
                    switchCameraButton.setVisibility(visibility);
                final boolean panorama_lens_buttons_shown = main_activity.getApplicationInterface().getPhotoMode() == MyApplicationInterface.PhotoMode.Panorama
                        && main_activity.getPreview().hasPhysicalCameras();
                if( !panorama_lens_buttons_shown && main_activity.showSwitchMultiCamIcon() )
                    switchMultiCameraButton.setVisibility(visibility);
                View panoramaLensRow = main_activity.findViewById(R.id.panorama_lens_row);
                if( panorama_lens_buttons_shown ) {
                    panoramaLensRow.setVisibility(visibility);
                }
                // die Kopfzeile gehört zum GUI wie die übrigen Bedienelemente (Schritt C) - sie
                // trägt die beiden Chips (Auflösung, Bildrate) und die Zustandsanzeigen, die die
                // alte Icon-Zeile hinterlassen hat
                setHeaderVisibility(visibility);
                galleryButton.setVisibility(visibility);
                setZoomStepperVisibility(zoomStepper.hasSteps() ? visibility : View.GONE);
                int mode_visibility = immersive_mode ? View.INVISIBLE : View.VISIBLE;
                main_activity.findViewById(R.id.mode_bar).setVisibility(mode_visibility);
                main_activity.findViewById(R.id.photo_video_switch).setVisibility(mode_visibility);
                main_activity.findViewById(R.id.settings_sheet_button).setVisibility(mode_visibility);
                // the pro panel is photo-only (Kickoff 3/4), so this path must not resurrect its
                // button in video mode
                main_activity.findViewById(R.id.pro_panel_button).setVisibility(
                        main_activity.getPreview().isVideo() ? View.GONE : mode_visibility);
                if( main_activity.showManualFocusSeekbar(false) )
                    focusSeekBar.setVisibility(visibility);
                if( main_activity.showManualFocusSeekbar(true) )
                    focusBracketingTargetSeekBar.setVisibility(visibility);
                String pref_immersive_mode = sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_off");
                if( pref_immersive_mode.equals("immersive_mode_everything") ) {
                    if( sharedPreferences.getBoolean(PreferenceKeys.ShowTakePhotoPreferenceKey, true) ) {
                        View takePhotoButton = main_activity.findViewById(R.id.take_photo);
                        takePhotoButton.setVisibility(visibility);
                    }
                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && main_activity.getPreview().isVideoRecording() ) {
                        View pauseVideoButton = main_activity.findViewById(R.id.pause_video);
                        pauseVideoButton.setVisibility(visibility);
                    }
                    if( main_activity.getPreview().supportsPhotoVideoRecording() && main_activity.getApplicationInterface().usePhotoVideoRecording() && main_activity.getPreview().isVideoRecording() ) {
                        View takePhotoVideoButton = main_activity.findViewById(R.id.take_photo_when_video_recording);
                        takePhotoVideoButton.setVisibility(visibility);
                    }
                    if( main_activity.getApplicationInterface().getGyroSensor().isRecording() ) {
                        View cancelPanoramaButton = main_activity.findViewById(R.id.cancel_panorama);
                        cancelPanoramaButton.setVisibility(visibility);
                    }
                }
                if( !immersive_mode ) {
                    // make sure the GUI is set up as expected
                    showGUI();
                }
            }
        });
    }

    public boolean inImmersiveMode() {
        return immersive_mode;
    }

    public void showGUI(final boolean show, final boolean is_video) {
        if( MyDebug.LOG ) {
            Log.d(TAG, "showGUI: " + show);
            Log.d(TAG, "is_video: " + is_video);
        }
        if( is_video )
            this.show_gui_video = show;
        else
            this.show_gui_photo = show;
        showGUI();
    }

    public void showGUI() {
        if( MyDebug.LOG ) {
            Log.d(TAG, "showGUI");
            Log.d(TAG, "show_gui_photo: " + show_gui_photo);
            Log.d(TAG, "show_gui_video: " + show_gui_video);
        }
        if( inImmersiveMode() )
            return;
        if( (show_gui_photo || show_gui_video) && main_activity.usingKitKatImmersiveMode() ) {
            // call to reset the timer
            main_activity.initImmersiveMode();
        }
        main_activity.runOnUiThread(new Runnable() {
            public void run() {
                final boolean is_panorama_recording = main_activity.getApplicationInterface().getGyroSensor().isRecording();
                final int visibility = is_panorama_recording ? View.GONE : (show_gui_photo && show_gui_video) ? View.VISIBLE : View.GONE; // for UI that is hidden while taking photo or video
                final int visibility_video = is_panorama_recording ? View.GONE : show_gui_photo ? View.VISIBLE : View.GONE; // for UI that is only hidden while taking photo
                View switchCameraButton = main_activity.findViewById(R.id.switch_camera);
                View switchMultiCameraButton = main_activity.findViewById(R.id.switch_multi_camera);
                View galleryButton = main_activity.findViewById(R.id.gallery);
                if( main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1 )
                    switchCameraButton.setVisibility(visibility);
                // In Panorama mode, panorama_lens_1x/panorama_lens_wide (direct-tap lens choice)
                // replace switch_multi_camera (icon that opens a dialog / cycles lenses) - see
                // MainActivity.clickedPanoramaLens1x/clickedPanoramaLensWide for why.
                final boolean panorama_lens_buttons_shown = main_activity.getApplicationInterface().getPhotoMode() == MyApplicationInterface.PhotoMode.Panorama
                        && main_activity.getPreview().hasPhysicalCameras();
                if( panorama_lens_buttons_shown ) {
                    switchMultiCameraButton.setVisibility(View.GONE);
                }
                else if( main_activity.showSwitchMultiCamIcon() ) {
                    switchMultiCameraButton.setVisibility(visibility);
                }
                View panoramaLensRow = main_activity.findViewById(R.id.panorama_lens_row);
                if( panorama_lens_buttons_shown ) {
                    Button panoramaLens1xButton = main_activity.findViewById(R.id.panorama_lens_1x);
                    Button panoramaLensWideButton = main_activity.findViewById(R.id.panorama_lens_wide);
                    boolean is_wide = main_activity.getApplicationInterface().getCameraIdSPhysicalPref() != null;
                    panoramaLensWideButton.setText(main_activity.getPanoramaWideLensLabel());
                    panoramaLens1xButton.setText(main_activity.getResources().getString(R.string.panorama_lens_main));
                    // dieselbe Auswahl wie in der Zoom-Pille statt der halbtransparenten Blase
                    // (Schritt G, Punkt 10)
                    ZoomStepper.applyPillStyle(main_activity, panoramaLensWideButton, is_wide);
                    ZoomStepper.applyPillStyle(main_activity, panoramaLens1xButton, !is_wide);
                    panoramaLensRow.setVisibility(visibility);
                }
                else {
                    panoramaLensRow.setVisibility(View.GONE);
                }
                // the gallery thumbnail gives way to the photo snapshot button while recording
                // video (both sit left of the shutter); switch_video is never shown any more
                galleryButton.setVisibility(show_gui_video ? View.VISIBLE : View.GONE);
                // Die Kopfzeile ist während einer Aufnahme aus und kommt mit ihrem Ende zurück -
                // im Record der Referenz ist das Band in beiden Aufnahme-Fenstern leer
                // (pixel-kopfzeile-beschreibung.md 6).
                setHeaderVisibility(show_gui_video ? View.VISIBLE : View.GONE);
                // zoom steps only make sense where zoom is allowed at all (e.g. not in panorama
                // mode, see MyApplicationInterface.allowZoom()); they stay while recording
                // video, so a lens can still be picked mid-recording (Falk, 2026-09-18).
                setZoomStepperVisibility(zoomStepper.hasSteps() ? visibility_video : View.GONE);
                // Taking a photo leaves the mode bar and the photo/video switch alone (as the
                // reference does); while recording a video or a panorama they're INVISIBLE -
                // out of sight and untouchable, without anything moving.
                int mode_visibility = (is_panorama_recording || !show_gui_video) ? View.INVISIBLE : View.VISIBLE;
                main_activity.findViewById(R.id.mode_bar).setVisibility(mode_visibility);
                main_activity.findViewById(R.id.photo_video_switch).setVisibility(mode_visibility);
                // the left dock button goes with them (Kickoff 3.1) - and with it the sheet: a
                // recording that starts while the sheet is open closes it (Kickoff 3.2)
                main_activity.findViewById(R.id.settings_sheet_button).setVisibility(mode_visibility);
                if( mode_visibility != View.VISIBLE ) {
                    SettingsSheet settingsSheet = main_activity.getSettingsSheet();
                    if( settingsSheet != null && settingsSheet.isOpen() )
                        settingsSheet.close();
                }
                // The pro panel is a photo-mode panel: manual ISO and exposure time are what the
                // video auto-torch hangs off (Kickoff 4), so the right dock button disappears in
                // video mode and an open panel closes with it. This runnable serves both modes, so
                // the mode has to be tested here - otherwise the button would never come back.
                if( main_activity.getPreview().isVideo() ) {
                    main_activity.findViewById(R.id.pro_panel_button).setVisibility(View.GONE);
                    ProPanel proPanelVideo = main_activity.getProPanelOrNull();
                    if( proPanelVideo != null && proPanelVideo.isOpen() )
                        proPanelVideo.close();
                }
                else {
                    main_activity.findViewById(R.id.pro_panel_button).setVisibility(mode_visibility);
                }
                updateModeBar();

                if( show_gui_photo && show_gui_video ) {
                    layoutUI(); // needed for "top" UIPlacement, to auto-arrange the buttons
                }
            }
        });
    }

    public boolean isExposureUIOpen() {
        View exposure_seek_bar = main_activity.findViewById(R.id.exposure_container);
        int exposure_visibility = exposure_seek_bar.getVisibility();
        View manual_exposure_seek_bar = main_activity.findViewById(R.id.manual_exposure_container);
        int manual_exposure_visibility = manual_exposure_seek_bar.getVisibility();
        return exposure_visibility == View.VISIBLE || manual_exposure_visibility == View.VISIBLE;
    }

    /**
     * Opens or close the exposure settings (ISO, white balance, etc)
     */
    public void toggleExposureUI() {
        if( MyDebug.LOG )
            Log.d(TAG, "toggleExposureUI");
        mSelectingExposureUIElement = false;
        if( isExposureUIOpen() ) {
            closeExposureUI();
        }
        else if( main_activity.getPreview().getCameraController() != null && !main_activity.getPreview().isPreviewStarting() && main_activity.supportsExposureButton() ) {
            // make sure preview is not starting - risk here is if preview is currently opening on
            // background thread - don't want to open exposure UI that would allow being
            // able to change settings that would then require restarting the preview
            setupExposureUI();
            if (main_activity.getBluetoothRemoteControl().remoteEnabled()) {
                initRemoteControlForExposureUI();
            }
        }
    }

    private void initRemoteControlForExposureUI() {
        if( MyDebug.LOG )
            Log.d(TAG, "initRemoteControlForExposureUI");
        if( isExposureUIOpen() ) { // just in case
            remote_control_mode = true;
            mExposureLine = 0;
            highlightExposureUILine(true);
        }
    }

    private void clearRemoteControlForExposureUI() {
        if( MyDebug.LOG )
            Log.d(TAG, "clearRemoteControlForExposureUI");
        if( isExposureUIOpen() && remote_control_mode ) {
            remote_control_mode = false;
            resetExposureUIHighlights();
        }
    }

    private void resetExposureUIHighlights() {
        if( MyDebug.LOG )
            Log.d(TAG, "resetExposureUIHighlights");
        ViewGroup iso_buttons_container = main_activity.findViewById(R.id.iso_buttons); // Shown when Camera API2 enabled
        View exposure_seek_bar = main_activity.findViewById(R.id.exposure_container);
        View shutter_seekbar = main_activity.findViewById(R.id.exposure_time_seekbar);
        View iso_seekbar = main_activity.findViewById(R.id.iso_seekbar);
        View wb_seekbar = main_activity.findViewById(R.id.white_balance_seekbar);
        // Set all lines to black
        iso_buttons_container.setBackgroundColor(Color.TRANSPARENT);
        exposure_seek_bar.setBackgroundColor(Color.TRANSPARENT);
        shutter_seekbar.setBackgroundColor(Color.TRANSPARENT);
        iso_seekbar.setBackgroundColor(Color.TRANSPARENT);
        wb_seekbar.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Highlights the relevant line on the Exposure UI based on
     * the value of mExposureLine
     *
     */
    private void highlightExposureUILine(boolean selectNext) {
        if( MyDebug.LOG )
            Log.d(TAG, "highlightExposureUILine: " + selectNext);
        if (!isExposureUIOpen()) { // Safety check
            return;
        }
        ViewGroup iso_buttons_container = main_activity.findViewById(R.id.iso_buttons); // Shown when Camera API2 enabled
        View exposure_seek_bar = main_activity.findViewById(R.id.exposure_container);
        View shutter_seekbar = main_activity.findViewById(R.id.exposure_time_seekbar);
        View iso_seekbar = main_activity.findViewById(R.id.iso_seekbar);
        View wb_seekbar = main_activity.findViewById(R.id.white_balance_seekbar);
        // Our order for lines is:
        // - ISO buttons
        // - ISO slider
        // - Shutter speed
        // - exposure seek bar
        if( MyDebug.LOG )
            Log.d(TAG, "mExposureLine: " + mExposureLine);
        mExposureLine = ( mExposureLine  + 5 ) % 5;
        if( MyDebug.LOG )
            Log.d(TAG, "mExposureLine modulo: " + mExposureLine);
        if (selectNext) {
            if (mExposureLine == 0 && !iso_buttons_container.isShown())
                mExposureLine++;
            if (mExposureLine == 1 && !iso_seekbar.isShown())
                mExposureLine++;
            if (mExposureLine == 2 && !shutter_seekbar.isShown())
                mExposureLine++;
            if ((mExposureLine == 3) && !exposure_seek_bar.isShown())
                mExposureLine++;
            if ((mExposureLine == 4) && !wb_seekbar.isShown())
                mExposureLine++;
        } else {
            // Select previous
            if (mExposureLine == 4 && !wb_seekbar.isShown())
                mExposureLine--;
            if (mExposureLine == 3 && !exposure_seek_bar.isShown())
                mExposureLine--;
            if (mExposureLine == 2 && !shutter_seekbar.isShown())
                mExposureLine--;
            if (mExposureLine == 1 && !iso_seekbar.isShown())
                mExposureLine--;
            if (mExposureLine == 0 && !iso_buttons_container.isShown())
                mExposureLine--;
        }
        if( MyDebug.LOG )
            Log.d(TAG, "after skipping: mExposureLine: " + mExposureLine);
        mExposureLine = ( mExposureLine  + 5 ) % 5;
        if( MyDebug.LOG )
            Log.d(TAG, "after skipping: mExposureLine modulo: " + mExposureLine);
        resetExposureUIHighlights();

        if (mExposureLine == 0) {
            iso_buttons_container.setBackgroundColor(highlightColor);
            //iso_buttons_container.setAlpha(0.5f);
        } else if (mExposureLine == 1) {
            iso_seekbar.setBackgroundColor(highlightColor);
            //iso_seekbar.setAlpha(0.5f);
        } else if (mExposureLine == 2) {
            shutter_seekbar.setBackgroundColor(highlightColor);
            //shutter_seekbar.setAlpha(0.5f);
        } else if (mExposureLine == 3) { //
            exposure_seek_bar.setBackgroundColor(highlightColor);
            //exposure_seek_bar.setAlpha(0.5f);
        } else if (mExposureLine == 4) {
            wb_seekbar.setBackgroundColor(highlightColor);
            //wb_seekbar.setAlpha(0.5f);
        }
    }

    private void nextExposureUILine() {
        mExposureLine++;
        highlightExposureUILine(true);
    }

    private void previousExposureUILine() {
        mExposureLine--;
        highlightExposureUILine(false);
    }

    /**
     * Our order for lines is:
     *  -0: ISO buttons
     *  -1: ISO slider
     *  -2: Shutter speed
     *  -3: exposure seek bar
     */
    private void nextExposureUIItem() {
        if( MyDebug.LOG )
            Log.d(TAG, "nextExposureUIItem");
        switch (mExposureLine) {
            case 0:
                // Die ISO-Vorlagenknopfreihe ist mit dem Popup entfallen (Schritt C) - die Zeile
                // wird nie erreicht, siehe selectExposureUILine()
                break;
            case 1:
                changeSeekbar(R.id.iso_seekbar, 10);
                break;
            case 2:
                changeSeekbar(R.id.exposure_time_seekbar, 5);
                break;
            case 3:
                //changeSeekbar(R.id.exposure_seekbar, 1);
                // call via MainActivity.changeExposure(), to handle repeated zeroes
                main_activity.changeExposure(1);
                break;
            case 4:
                changeSeekbar(R.id.white_balance_seekbar, 3);
                break;
        }
    }

    private void previousExposureUIItem() {
        if( MyDebug.LOG )
            Log.d(TAG, "previousExposureUIItem");
        switch (mExposureLine) {
            case 0:
                // wie in nextExposureUIItem(): die Zeile gibt es nicht mehr
                break;
            case 1:
                changeSeekbar(R.id.iso_seekbar, -10);
                break;
            case 2:
                changeSeekbar(R.id.exposure_time_seekbar, -5);
                break;
            case 3:
                //changeSeekbar(R.id.exposure_seekbar, -1);
                // call via MainActivity.changeExposure(), to handle repeated zeroes
                main_activity.changeExposure(-1);
                break;
            case 4:
                changeSeekbar(R.id.white_balance_seekbar, -3);
                break;
        }
    }


    /**
     * Select element on exposure UI. Based on the value of mExposureLine
     *         // Our order for lines is:
     *         // - ISO buttons
     *         // - ISO slider
     *         // - Shutter speed
     *         // - exposure seek bar
     */
    private void selectExposureUILine() {
        if( MyDebug.LOG )
            Log.d(TAG, "selectExposureUILine");
        if( !isExposureUIOpen() ) { // Safety check
            return;
        }

        if( mExposureLine == 0 ) { // ISO presets
            // ponytail: Die ISO-Vorlagenknopfreihe ist mit dem Popup entfallen (Schritt C), die
            // Liste ist dauerhaft leer. Der Zweig wird nie erreicht, weil sein Container auf
            // "gone" steht; er bleibt nur stehen, damit die Zeilennummerierung der
            // Fernbedienungs-Navigation (0..4) unverändert bleibt.
            mSelectingExposureUIElement = true;
        }
        else if( mExposureLine == 1 ) {
            // ISO seek bar - change color
            View seek_bar = main_activity.findViewById(R.id.iso_seekbar);
            //seek_bar.setAlpha(0.1f);
            seek_bar.setBackgroundColor(highlightColorExposureUIElement);
            mSelectingExposureUIElement = true;
        }
        else if( mExposureLine == 2 ) {
            // ISO seek bar - change color
            View seek_bar = main_activity.findViewById(R.id.exposure_time_seekbar);
            //seek_bar.setAlpha(0.1f);
            seek_bar.setBackgroundColor(highlightColorExposureUIElement);
            mSelectingExposureUIElement = true;
        }
        else if ( mExposureLine == 3 ) {
            // Exposure compensation
            View container = main_activity.findViewById(R.id.exposure_container);
            //container.setAlpha(0.1f);
            container.setBackgroundColor(highlightColorExposureUIElement);
            mSelectingExposureUIElement = true;
        }
        else if( mExposureLine == 4 ) {
            // Manual white balance
            View container = main_activity.findViewById(R.id.white_balance_seekbar);
            //container.setAlpha(0.1f);
            container.setBackgroundColor(highlightColorExposureUIElement);
            mSelectingExposureUIElement = true;
        }
    }

    /** Returns the height of the device in dp (or width in portrait mode), allowing for space for the
     *  on-screen UI icons.
     * @param centred If true, then find the max height for a view that will be centred.
     */
    int getMaxHeightDp(boolean centred) {
        // ensure we have display for landscape orientation (even if we ever allow Open Camera
        Point display_size = new Point();
        main_activity.getApplicationInterface().getDisplaySize(display_size, true);

        // normally we should always have heightPixels < widthPixels, but good not to assume we're running in landscape orientation
        int smaller_dim = Math.min(display_size.x, display_size.y);
        // the smaller dimension should limit the width, due to when held in portrait
        final float scale = main_activity.getResources().getDisplayMetrics().density;
        int dpHeight = (int)(smaller_dim / scale);
        if( MyDebug.LOG ) {
            Log.d(TAG, "display size: " + display_size.x + " x " + display_size.y);
            Log.d(TAG, "dpHeight: " + dpHeight);
        }
        // allow space for the icons at top/right of screen
        int margin = centred ? 120 : 50;
        dpHeight -= margin;
        return dpHeight;
    }

    public boolean isSelectingExposureUIElement() {
        if( MyDebug.LOG )
            Log.d(TAG, "isSelectingExposureUIElement returns:" + mSelectingExposureUIElement);
        return mSelectingExposureUIElement;
    }


    /**
     * Process a press to the "Up" button on a remote. Called from MainActivity.
     * @return true if an action was taken
     */
    public boolean processRemoteUpButton() {
        if( MyDebug.LOG )
            Log.d(TAG, "processRemoteUpButton");
        boolean didProcess = false;
        if (isExposureUIOpen()) {
            didProcess = true;
            if (isSelectingExposureUIElement()) {
                nextExposureUIItem();
            } else {
                previousExposureUILine();
            }
        }
        return didProcess;
    }

    /**
     * Process a press to the "Down" button on a remote. Called from MainActivity.
     * @return true if an action was taken
     */
    public boolean processRemoteDownButton() {
        if( MyDebug.LOG )
            Log.d(TAG, "processRemoteDownButton");
        boolean didProcess = false;
        if (isExposureUIOpen()) {
            if (isSelectingExposureUIElement()) {
                previousExposureUIItem();
            } else {
                nextExposureUILine();
            }
            didProcess = true;
        }
        return didProcess;
    }

    private List<View> iso_buttons;
    private int iso_button_manual_index = -1;
    private final static String manual_iso_value = "m";

    /** Opens the exposure UI if not already open, and sets up or updates the UI.
     */
    public void setupExposureUI() {
        if( MyDebug.LOG )
            Log.d(TAG, "setupExposureUI");
        // Die ISO-Vorlagenknopfreihe ist mit dem Popup entfallen (Redesign Schritt C). Die
        // ISO-Regelung läuft über den ISO-Schieber und das Pro-Panel. Die Liste bleibt als
        // leere Liste bestehen, weil die Fernbedienungs-Navigation sie mitzählt.
        iso_buttons = new ArrayList<>();
        final Preview preview = main_activity.getPreview();
        View sliders_container = main_activity.findViewById(R.id.sliders_container);
        sliders_container.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(main_activity, R.anim.fade_in);
        sliders_container.startAnimation(animation);

        View exposure_seek_bar = main_activity.findViewById(R.id.exposure_container);
        View manual_exposure_seek_bar = main_activity.findViewById(R.id.manual_exposure_container);
        String iso_value = main_activity.getApplicationInterface().getISOPref();
        if( main_activity.getPreview().usingCamera2API() && !iso_value.equals(CameraController.ISO_DEFAULT) ) {
            exposure_seek_bar.setVisibility(View.GONE);

            // with Camera2 API, when using manual ISO we instead show sliders for ISO range and exposure time
            if( main_activity.getPreview().supportsISORange() ) {
                manual_exposure_seek_bar.setVisibility(View.VISIBLE);
                SeekBar exposure_time_seek_bar = main_activity.findViewById(R.id.exposure_time_seekbar);
                if( main_activity.getPreview().supportsExposureTime() ) {
                    exposure_time_seek_bar.setVisibility(View.VISIBLE);
                }
                else {
                    exposure_time_seek_bar.setVisibility(View.GONE);
                }
            }
            else {
                manual_exposure_seek_bar.setVisibility(View.GONE);
            }
        }
        else {
            manual_exposure_seek_bar.setVisibility(View.GONE);

            if( main_activity.getPreview().supportsExposures() ) {
                exposure_seek_bar.setVisibility(View.VISIBLE);
            }
            else {
                exposure_seek_bar.setVisibility(View.GONE);
            }
        }

        View manual_white_balance_seek_bar = main_activity.findViewById(R.id.manual_white_balance_container);
        if( main_activity.getPreview().supportsWhiteBalanceTemperature()) {
            // we also show slider for manual white balance, if in that mode
            String white_balance_value = main_activity.getApplicationInterface().getWhiteBalancePref();
            if( main_activity.getPreview().usingCamera2API() && white_balance_value.equals("manual") ) {
                manual_white_balance_seek_bar.setVisibility(View.VISIBLE);
            }
            else {
                manual_white_balance_seek_bar.setVisibility(View.GONE);
            }
        }
        else {
            manual_white_balance_seek_bar.setVisibility(View.GONE);
        }

        //layoutUI(); // needed to update alignment of exposure UI
    }

    /** Returns whether the ISO button with the supplied text is a match for the supplied iso.
     *  Should only be used for Preview.supportsISORange()==true (i.e., full manual ISO).
     */

    /** Returns the ISO button text for the supplied iso.
     *  Should only be used for Preview.supportsISORange()==true (i.e., full manual ISO).
     */

    /** Keeps the zoom pill (and the fine adjustment wheel, while pinching) in step with a zoom
     *  set from anywhere else: pinch in the viewfinder, volume keys.
     */
    public void onZoomChanged(int zoom_factor) {
        if( MyDebug.LOG )
            Log.d(TAG, "onZoomChanged: " + zoom_factor);
        zoomStepper.followZoom(zoom_factor);
    }

    public void changeSeekbar(int seekBarId, int change) {
        if( MyDebug.LOG )
            Log.d(TAG, "changeSeekbar: " + change);
        SeekBar seekBar = main_activity.findViewById(seekBarId);
        int value = seekBar.getProgress();
        int new_value = value + change;
        if( new_value < 0 )
            new_value = 0;
        else if( new_value > seekBar.getMax() )
            new_value = seekBar.getMax();
        if( MyDebug.LOG ) {
            Log.d(TAG, "value: " + value);
            Log.d(TAG, "new_value: " + new_value);
            Log.d(TAG, "max: " + seekBar.getMax());
        }
        if( new_value != value ) {
            seekBar.setProgress(new_value);
        }
    }

    /** Closes the exposure UI.
     */
    public void closeExposureUI() {
        clearRemoteControlForExposureUI(); // must be called before we actually close the exposure panel
        View view = main_activity.findViewById(R.id.sliders_container);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.exposure_container);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.manual_exposure_container);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.manual_white_balance_container);
        view.setVisibility(View.GONE);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( MyDebug.LOG )
            Log.d(TAG, "onKeyDown: " + keyCode);
        switch( keyCode ) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS: // media codes are for "selfie sticks" buttons
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_STOP:
            {
                if( keyCode == KeyEvent.KEYCODE_VOLUME_UP )
                    keydown_volume_up = true;
                else if( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN )
                    keydown_volume_down = true;

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                String volume_keys = sharedPreferences.getString(PreferenceKeys.VolumeKeysPreferenceKey, "volume_take_photo");

                if((keyCode==KeyEvent.KEYCODE_MEDIA_PREVIOUS
                        ||keyCode==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                        ||keyCode==KeyEvent.KEYCODE_MEDIA_STOP)
                        &&!(volume_keys.equals("volume_take_photo"))) {
                    AudioManager audioManager = (AudioManager) main_activity.getSystemService(Context.AUDIO_SERVICE);
                    if(audioManager==null) break;
                    if(!audioManager.isWiredHeadsetOn()) break; // isWiredHeadsetOn() is deprecated, but comment says "Use only to check is a headset is connected or not."
                }

                switch(volume_keys) {
                    case "volume_take_photo":
                        boolean done = false;
                        if( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && main_activity.getPreview().isVideoRecording() ) {
                            done = true;
                            main_activity.pauseVideo();
                        }
                        if( !done ) {
                            main_activity.takePicture(false);
                        }
                        return true;
                    case "volume_focus":
                        if(keydown_volume_up && keydown_volume_down) {
                            if (MyDebug.LOG)
                                Log.d(TAG, "take photo rather than focus, as both volume keys are down");
                            main_activity.takePicture(false);
                        }
                        else if (main_activity.getPreview().getCurrentFocusValue() != null && main_activity.getPreview().getCurrentFocusValue().equals("focus_mode_manual2")) {
                            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                                main_activity.changeFocusDistance(-1, false);
                            else
                                main_activity.changeFocusDistance(1, false);
                        }
                        else {
                            // important not to repeatedly request focus, even though main_activity.getPreview().requestAutoFocus() will cancel, as causes problem if key is held down (e.g., flash gets stuck on)
                            // also check DownTime vs EventTime to prevent repeated focusing whilst the key is held down
                            if(event.getDownTime() == event.getEventTime() && !main_activity.getPreview().isFocusWaiting()) {
                                if(MyDebug.LOG)
                                    Log.d(TAG, "request focus due to volume key");
                                main_activity.getPreview().requestAutoFocus();
                            }
                        }
                        return true;
                    case "volume_zoom":
                        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                            main_activity.zoomIn();
                        else
                            main_activity.zoomOut();
                        return true;
                    case "volume_exposure":
                        if(main_activity.getPreview().getCameraController() != null) {
                            String value = sharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
                            boolean manual_iso = !value.equals(CameraController.ISO_DEFAULT);
                            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                if(manual_iso) {
                                    main_activity.changeISO(1);
                                }
                                else
                                    main_activity.changeExposure(1);
                            }
                            else {
                                if(manual_iso) {
                                    main_activity.changeISO(-1);
                                }
                                else
                                    main_activity.changeExposure(-1);
                            }
                        }
                        return true;
                    case "volume_auto_stabilise":
                        if( main_activity.supportsAutoStabilise() ) {
                            boolean auto_stabilise = sharedPreferences.getBoolean(PreferenceKeys.AutoStabilisePreferenceKey, false);
                            auto_stabilise = !auto_stabilise;
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(PreferenceKeys.AutoStabilisePreferenceKey, auto_stabilise);
                            editor.apply();
                            String message = main_activity.getResources().getString(R.string.preference_auto_stabilise) + ": " + main_activity.getResources().getString(auto_stabilise ? R.string.on : R.string.off);
                            main_activity.getPreview().showToast(main_activity.getChangedAutoStabiliseToastBoxer(), message, true);
                            main_activity.getApplicationInterface().getDrawPreview().updateSettings(); // because we cache the auto-stabilise setting
                        }
                        else if( !main_activity.deviceSupportsAutoStabilise() ) {
                            // n.b., need to check deviceSupportsAutoStabilise() - if we're in e.g. Panorama mode, we shouldn't display a toast (as then supportsAutoStabilise() returns false even if auto-level is supported on the device)
                            main_activity.getPreview().showToast(main_activity.getChangedAutoStabiliseToastBoxer(), R.string.auto_stabilise_not_supported);
                        }
                        return true;
                    case "volume_really_nothing":
                        // do nothing, but still return true so we don't change volume either
                        return true;
                }
                // else do nothing here, but still allow changing of volume (i.e., the default behaviour)
                break;
            }
            case KeyEvent.KEYCODE_MENU:
            {
                // needed to support hardware menu button
                // tested successfully on Samsung S3 (via RTL)
                // see http://stackoverflow.com/questions/8264611/how-to-detect-when-user-presses-menu-key-on-their-android-device
                main_activity.openSettings();
                return true;
            }
            case KeyEvent.KEYCODE_CAMERA:
            {
                if( event.getRepeatCount() == 0 ) {
                    main_activity.takePicture(false);
                    return true;
                }
            }
            case KeyEvent.KEYCODE_FOCUS:
            {
                // important not to repeatedly request focus, even though main_activity.getPreview().requestAutoFocus() will cancel - causes problem with hardware camera key where a half-press means to focus
                // also check DownTime vs EventTime to prevent repeated focusing whilst the key is held down - see https://sourceforge.net/p/opencamera/tickets/174/ ,
                // or same issue above for volume key focus
                if( event.getDownTime() == event.getEventTime() && !main_activity.getPreview().isFocusWaiting() ) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "request focus due to focus key");
                    main_activity.getPreview().requestAutoFocus();
                }
                return true;
            }
            case KeyEvent.KEYCODE_ZOOM_IN:
            case KeyEvent.KEYCODE_PLUS:
            case KeyEvent.KEYCODE_NUMPAD_ADD:
            {
                main_activity.zoomIn();
                return true;
            }
            case KeyEvent.KEYCODE_ZOOM_OUT:
            case KeyEvent.KEYCODE_MINUS:
            case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
            {
                main_activity.zoomOut();
                return true;
            }
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_NUMPAD_5:
            {
                if( isExposureUIOpen() && remote_control_mode ) {
                    commandMenuExposure();
                    return true;
                }
                else if( event.getRepeatCount() == 0 ) {
                    main_activity.takePicture(false);
                    return true;
                }
                break;
            }
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_NUMPAD_8:
                //case KeyEvent.KEYCODE_VOLUME_UP: // test
                if( !remote_control_mode ) {
                    if( isExposureUIOpen() ) {
                        initRemoteControlForExposureUI();
                        return true;
                    }
                }
                else if( processRemoteUpButton() )
                    return true;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_NUMPAD_2:
                //case KeyEvent.KEYCODE_VOLUME_DOWN: // test
                if( !remote_control_mode ) {
                    if( isExposureUIOpen() ) {
                        initRemoteControlForExposureUI();
                        return true;
                    }
                }
                else if( processRemoteDownButton() )
                    return true;
                break;
            case KeyEvent.KEYCODE_FUNCTION:
            case KeyEvent.KEYCODE_NUMPAD_MULTIPLY:
                // Die Drei-Punkte-Taste der Fernbedienung öffnet jetzt das Einstellungen-Blatt,
                // das das Popup ersetzt hat (SPEC 4.5, Schritt C).
                main_activity.clickedSettingsSheet(null);
                break;
            case KeyEvent.KEYCODE_SLASH:
            case KeyEvent.KEYCODE_NUMPAD_DIVIDE:
                toggleExposureUI();
                break;
        }
        return false;
    }

    public void onKeyUp(int keyCode, KeyEvent event) {
        if( MyDebug.LOG )
            Log.d(TAG, "onKeyUp: " + keyCode);
        if( keyCode == KeyEvent.KEYCODE_VOLUME_UP )
            keydown_volume_up = false;
        else if( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN )
            keydown_volume_down = false;
    }

    /** If the exposure menu is open, selects a current line or option. Else does nothing.
     */
    public void commandMenuExposure() {
        if( MyDebug.LOG )
            Log.d(TAG, "commandMenuExposure");
        if( isExposureUIOpen() ) {
            if( isSelectingExposureUIElement() ) {
                // Close Exposure UI if new press on MENU
                // while already selecting
                toggleExposureUI();
            }
            else {
                // Select current element in Exposure UI
                selectExposureUILine();
            }
        }
    }

    /** Shows an information dialog, with a button to request not to show again.
     *  Note it's up to the caller to check whether the info_preference_key (to not show again) was
     *  already set.
     * @param title_id Resource id for title string.
     * @param info_id Resource id for dialog text string.
     * @param info_preference_key Preference key to set in SharedPreferences if the user selects to
     *                            not show the dialog again.
     * @return The AlertDialog that was created.
     */
    public AlertDialog showInfoDialog(int title_id, int info_id, final String info_preference_key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(main_activity);
        alertDialog.setTitle(title_id);
        if( info_id != 0 )
            alertDialog.setMessage(info_id);
        else {
            // Setting an empty string here is still needed to allow setting a message later after it's been created.
            // Needed for MagneticSensor's dialog
            alertDialog.setMessage("");
        }
        alertDialog.setPositiveButton(android.R.string.ok, null);
        alertDialog.setNegativeButton(R.string.dont_show_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if( MyDebug.LOG )
                    Log.d(TAG, "user clicked dont_show_again for info dialog");
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(info_preference_key, true);
                editor.apply();
            }
        });

        //main_activity.showPreview(false);
        //main_activity.setWindowFlagsForSettings(false); // set set_lock_protect to false, otherwise if screen is locked, user will need to unlock to see the info dialog!

        AlertDialog alert = alertDialog.create();
        // AlertDialog.Builder.setOnDismissListener() requires API level 17, so do it this way instead
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                if( MyDebug.LOG )
                    Log.d(TAG, "info dialog dismissed");
                //main_activity.setWindowFlagsForCamera();
                //main_activity.showPreview(true);
            }
        });
        //main_activity.showAlert(alert);
        alert.show();
        return alert;
    }

    /** Returns a (possibly translated) user readable string for a white balance preference value.
     *  If the value is not recognised (this can happen for the old Camera API, some devices can
     *  have device-specific options), then the received value is returned.
     */
    public String getEntryForWhiteBalance(String value) {
        int id = -1;
        switch( value ) {
            case CameraController.WHITE_BALANCE_DEFAULT:
                id = R.string.white_balance_auto;
                break;
            case "cloudy-daylight":
                id = R.string.white_balance_cloudy;
                break;
            case "daylight":
                id = R.string.white_balance_daylight;
                break;
            case "fluorescent":
                id = R.string.white_balance_fluorescent;
                break;
            case "incandescent":
                id = R.string.white_balance_incandescent;
                break;
            case "shade":
                id = R.string.white_balance_shade;
                break;
            case "twilight":
                id = R.string.white_balance_twilight;
                break;
            case "warm-fluorescent":
                id = R.string.white_balance_warm;
                break;
            case "manual":
                id = R.string.white_balance_manual;
                break;
            default:
                break;
        }
        String entry;
        if( id != -1 ) {
            entry = main_activity.getResources().getString(id);
        }
        else {
            entry = value;
        }
        return entry;
    }

    /** Returns a (possibly translated) user readable string for a scene mode preference value.
     *  If the value is not recognised (this can happen for the old Camera API, some devices can
     *  have device-specific options), then the received value is returned.
     */
    public String getEntryForSceneMode(String value) {
        int id = -1;
        switch( value ) {
            case "action":
                id = R.string.scene_mode_action;
                break;
            case "barcode":
                id = R.string.scene_mode_barcode;
                break;
            case "beach":
                id = R.string.scene_mode_beach;
                break;
            case "candlelight":
                id = R.string.scene_mode_candlelight;
                break;
            case CameraController.SCENE_MODE_DEFAULT:
                id = R.string.scene_mode_auto;
                break;
            case "fireworks":
                id = R.string.scene_mode_fireworks;
                break;
            case "landscape":
                id = R.string.scene_mode_landscape;
                break;
            case "night":
                id = R.string.scene_mode_night;
                break;
            case "night-portrait":
                id = R.string.scene_mode_night_portrait;
                break;
            case "party":
                id = R.string.scene_mode_party;
                break;
            case "portrait":
                id = R.string.scene_mode_portrait;
                break;
            case "snow":
                id = R.string.scene_mode_snow;
                break;
            case "sports":
                id = R.string.scene_mode_sports;
                break;
            case "steadyphoto":
                id = R.string.scene_mode_steady_photo;
                break;
            case "sunset":
                id = R.string.scene_mode_sunset;
                break;
            case "theatre":
                id = R.string.scene_mode_theatre;
                break;
            default:
                break;
        }
        String entry;
        if( id != -1 ) {
            entry = main_activity.getResources().getString(id);
        }
        else {
            entry = value;
        }
        return entry;
    }

    /** Returns a (possibly translated) user readable string for a color effect preference value.
     *  If the value is not recognised (this can happen for the old Camera API, some devices can
     *  have device-specific options), then the received value is returned.
     */
    public String getEntryForColorEffect(String value) {
        int id = -1;
        switch( value ) {
            case "aqua":
                id = R.string.color_effect_aqua;
                break;
            case "blackboard":
                id = R.string.color_effect_blackboard;
                break;
            case "mono":
                id = R.string.color_effect_mono;
                break;
            case "negative":
                id = R.string.color_effect_negative;
                break;
            case CameraController.COLOR_EFFECT_DEFAULT:
                id = R.string.color_effect_none;
                break;
            case "posterize":
                id = R.string.color_effect_posterize;
                break;
            case "sepia":
                id = R.string.color_effect_sepia;
                break;
            case "solarize":
                id = R.string.color_effect_solarize;
                break;
            case "whiteboard":
                id = R.string.color_effect_whiteboard;
                break;
            default:
                break;
        }
        String entry;
        if( id != -1 ) {
            entry = main_activity.getResources().getString(id);
        }
        else {
            entry = value;
        }
        return entry;
    }

    /** Returns a (possibly translated) user readable string for an antibanding preference value.
     *  If the value is not recognised, then the received value is returned.
     */
    public String getEntryForAntiBanding(String value) {
        int id = -1;
        switch( value ) {
            case CameraController.ANTIBANDING_DEFAULT:
                id = R.string.anti_banding_auto;
                break;
            case "50hz":
                id = R.string.anti_banding_50hz;
                break;
            case "60hz":
                id = R.string.anti_banding_60hz;
                break;
            case "off":
                id = R.string.anti_banding_off;
                break;
            default:
                break;
        }
        String entry;
        if( id != -1 ) {
            entry = main_activity.getResources().getString(id);
        }
        else {
            entry = value;
        }
        return entry;
    }

    /** Returns a (possibly translated) user readable string for an noise reduction mode preference value.
     *  If the value is not recognised, then the received value is returned.
     *  Also used for edge mode.
     */
    public String getEntryForNoiseReductionMode(String value) {
        int id = -1;
        switch( value ) {
            case CameraController.NOISE_REDUCTION_MODE_DEFAULT:
                id = R.string.noise_reduction_mode_default;
                break;
            case "off":
                id = R.string.noise_reduction_mode_off;
                break;
            case "minimal":
                id = R.string.noise_reduction_mode_minimal;
                break;
            case "fast":
                id = R.string.noise_reduction_mode_fast;
                break;
            case "high_quality":
                id = R.string.noise_reduction_mode_high_quality;
                break;
            default:
                break;
        }
        String entry;
        if( id != -1 ) {
            entry = main_activity.getResources().getString(id);
        }
        else {
            entry = value;
        }
        return entry;
    }

    /** Die View, unterhalb derer DrawPreview seinen Text halten muss: seit Schritt C ist das
     *  Kopfzeilenband, das die alte Icon-Zeile ersetzt hat. DrawPreview rechnet daraus den
     *  Abstand von der Sucher-Oberkante zur Unterkante der View aus. */
    View getTopIcon() {
        View header = main_activity.findViewById(R.id.header_bar);
        return header != null && header.getVisibility() == View.VISIBLE ? header : null;
    }

    /** Performs haptic feedback (a CLOCK_TICK) on behalf of the supplied view, if allowed by
     *  settings and at most once every 16 ms.
     */
    public static long performHapticFeedback(View view, long last_haptic_time) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        if( sharedPreferences.getBoolean(PreferenceKeys.AllowHapticFeedbackPreferenceKey, true) ) {
            long time_ms = System.currentTimeMillis();
            if( time_ms > last_haptic_time + 16 ) {
                last_haptic_time = time_ms;
                // SEGMENT_TICK or SEGMENT_TICK doesn't work on Galaxy S24+ at least, even though on Android 14!
                /*if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ) {
                    view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK);
                }
                else*/ {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                }
            }
        }
        return last_haptic_time;
    }

    // for testing
    public boolean testGetRemoteControlMode() {
        return remote_control_mode;
    }

    public int testGetExposureLine() {
        return mExposureLine;
    }
}
