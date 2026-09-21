package net.sourceforge.opencamera.ui;

import net.sourceforge.opencamera.R;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/** Looks and feel of the shutter button (take_photo), built to the Pixel Camera reference
 *  (docs/design-reference/ui-spezifikation.md 7.2, pixel-camera-detailspec.md 6): a fixed
 *  translucent ring (the view's background, shutter_ring.xml) around a core that this class
 *  draws and animates - a white disc in photo mode, a red one in video mode, and while
 *  recording a small red rounded square (the "stop" glyph), the morph between disc and square
 *  driven by a spring. Also the press feedback: the whole button springs to 92 % while held
 *  and back to full size on release.
 *
 *  The core is a plain GradientDrawable rather than a set of XML shapes, because size, corner
 *  radius and colour all have to change at run time for the morph; the ImageButton draws it
 *  centred at the drawable's own size (scaleType=center), so growing or shrinking the drawable
 *  is all it takes to resize the core on screen.
 */
public class ShutterButton {

    public enum State {
        PHOTO, // white disc
        VIDEO, // red disc
        VIDEO_LAPSE, // red disc with the time lapse glyph, as in the reference
        VIDEO_SLOW, // red disc with the slow motion glyph, as in the reference
        VIDEO_RECORDING, // red rounded square, tap to stop
        PANORAMA_RECORDING // check mark, tap to finish the panorama
    }

    // Geometry measured off the reference screenshot (2.25 px/dp on Falk's device), see
    // ui-spezifikation.md section 6; the ring around it is 6 dp (shutter_ring.xml)
    private static final float CORE_DIAMETER_DP = 74.0f;
    // The video disc is a little smaller than the photo one and carries the mode's glyph
    // (Falk 2026-09-21); the reference measures the disc of the two video sub-modes at 53 dp
    // (pixel-zeitraffer-zeitlupe-beschreibung.md 4), which with our ring would leave a very
    // thick border - this is the middle ground Falk asked for.
    private static final float VIDEO_DIAMETER_DP = 62.0f;
    // the glyph on the video disc, in the middle of it
    private static final float GLYPH_DIAMETER_DP = 30.0f;
    // the stop square while recording (detail spec section 6)
    private static final float RECORDING_SIZE_DP = 32.0f;
    private static final float RECORDING_CORNER_DP = 8.0f;
    private static final float PRESSED_SCALE = 0.92f;
    /** The spec's "tension: 300, friction: 20", read as spring stiffness and damping
     *  coefficient with unit mass (the react-spring convention that matches the CSS-style
     *  notation used throughout the detail spec): damping ratio = 20 / (2 * sqrt(300)).
     *  Shared with the gallery thumbnail's bounce (MainActivity.updateGalleryIcon()). */
    static final float SPRING_STIFFNESS = 300.0f;
    static final float SPRING_DAMPING_RATIO = 0.577f;

    private final ImageButton view;
    private final GradientDrawable core = new GradientDrawable();
    private final float scale; // px per dp

    // Backing values of the animated core properties - GradientDrawable can't be asked for
    // its corner radius before API 24, so we remember what we set
    private float core_size_px;
    private float core_corner_px;

    private static final FloatPropertyCompat<ShutterButton> CORE_SIZE = new FloatPropertyCompat<ShutterButton>("coreSize") {
        @Override
        public float getValue(ShutterButton button) {
            return button.core_size_px;
        }

        @Override
        public void setValue(ShutterButton button, float value) {
            button.core_size_px = value;
            // the ImageView picks up the changed intrinsic size from the drawable's invalidate
            button.core.setSize(Math.round(value), Math.round(value));
        }
    };

    private static final FloatPropertyCompat<ShutterButton> CORE_CORNER = new FloatPropertyCompat<ShutterButton>("coreCorner") {
        @Override
        public float getValue(ShutterButton button) {
            return button.core_corner_px;
        }

        @Override
        public void setValue(ShutterButton button, float value) {
            button.core_corner_px = value;
            button.core.setCornerRadius(value);
        }
    };

    private final SpringAnimation core_size_spring;
    private final SpringAnimation core_corner_spring;
    private final SpringAnimation press_scale_x_spring;
    private final SpringAnimation press_scale_y_spring;

    public ShutterButton(ImageButton view) {
        this.view = view;
        this.scale = view.getResources().getDisplayMetrics().density;
        // a rectangle whose corner radius is half its size is a circle, and unlike an OVAL it
        // can morph into the rounded stop square just by changing size and radius
        core.setShape(GradientDrawable.RECTANGLE);
        core_size_spring = new SpringAnimation(this, CORE_SIZE).setSpring(newSpringForce());
        core_corner_spring = new SpringAnimation(this, CORE_CORNER).setSpring(newSpringForce());
        press_scale_x_spring = new SpringAnimation(view, DynamicAnimation.SCALE_X).setSpring(newSpringForce());
        press_scale_y_spring = new SpringAnimation(view, DynamicAnimation.SCALE_Y).setSpring(newSpringForce());
        setState(State.PHOTO, false);
    }

    public static SpringForce newSpringForce() {
        return new SpringForce().setStiffness(SPRING_STIFFNESS).setDampingRatio(SPRING_DAMPING_RATIO);
    }

    private float dp(float dp) {
        return dp * scale;
    }

    /** Shows the given state. With animate, the core springs between disc and stop square
     *  (video start/stop); without, it just switches (mode changes, layout passes).
     */
    public void setState(State state, boolean animate) {
        if( state == State.PANORAMA_RECORDING ) {
            view.setImageResource(R.drawable.baseline_check_white_48);
            return;
        }
        int glyph_resource;
        switch( state ) {
            case VIDEO_LAPSE:
                glyph_resource = R.drawable.ic_fast_forward_white_48dp;
                break;
            case VIDEO_SLOW:
                glyph_resource = R.drawable.ic_slow_motion_video_white_48dp;
                break;
            default:
                glyph_resource = 0;
                break;
        }
        showGlyph(glyph_resource != 0 ? ContextCompat.getDrawable(view.getContext(), glyph_resource) : null);
        boolean video = state == State.VIDEO || state == State.VIDEO_LAPSE || state == State.VIDEO_SLOW;
        boolean recording = state == State.VIDEO_RECORDING;
        core.setColor(ContextCompat.getColor(view.getContext(), state == State.PHOTO ? R.color.shutter_icon : R.color.shutter_icon_video));
        float size = dp(recording ? RECORDING_SIZE_DP : (video ? VIDEO_DIAMETER_DP : CORE_DIAMETER_DP));
        float corner = dp(recording ? RECORDING_CORNER_DP : (video ? VIDEO_DIAMETER_DP : CORE_DIAMETER_DP) / 2);
        if( animate ) {
            core_size_spring.animateToFinalPosition(size);
            core_corner_spring.animateToFinalPosition(corner);
        }
        else {
            core_size_spring.cancel();
            core_corner_spring.cancel();
            CORE_SIZE.setValue(this, size);
            CORE_CORNER.setValue(this, corner);
        }
    }

    /** Puts the mode's glyph into the middle of the disc (Zeitraffer, Zeitlupe) or takes it out
     *  again - the photo and the ordinary video disc stay plain, as in the reference. The glyph
     *  rides in the same drawable as the disc (a LayerDrawable), so the button keeps drawing one
     *  image and the press spring scales the two together. */
    private void showGlyph(Drawable glyph) {
        if( glyph == null ) {
            if( view.getDrawable() != core )
                view.setImageDrawable(core);
            return;
        }
        int glyph_size = Math.round(dp(GLYPH_DIAMETER_DP));
        LayerDrawable layers = new LayerDrawable(new Drawable[]{core, glyph});
        layers.setLayerGravity(1, Gravity.CENTER);
        layers.setLayerSize(1, glyph_size, glyph_size);
        view.setImageDrawable(layers);
    }

    /** Press feedback: springs the button to 92 % while held, and back on release. Fed from
     *  the take_photo touch listener in MainActivity (a view has only one of those, and that
     *  one already exists for the long-press handling).
     */
    public void setPressed(boolean pressed) {
        float target = pressed ? PRESSED_SCALE : 1.0f;
        press_scale_x_spring.animateToFinalPosition(target);
        press_scale_y_spring.animateToFinalPosition(target);
    }
}
