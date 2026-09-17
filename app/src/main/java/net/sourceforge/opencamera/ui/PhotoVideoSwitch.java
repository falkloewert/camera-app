package net.sourceforge.opencamera.ui;

import net.sourceforge.opencamera.R;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;

/** The photo/video toggle from the bottom row of the reference (docs/design-reference/
 *  ui-spezifikation.md section 1, point 4): a dark pill with a camera glyph at one end and a
 *  film-camera glyph at the other, and a white knob that springs to whichever is active - the
 *  glyph under the knob turns dark, the other one is white.
 *
 *  Drawn by hand rather than built from child views: the pill and knob are one shape that
 *  never moves (the layout is locked to portrait, ui-spezifikation.md section 2), and only the
 *  two glyphs need to turn on the spot when the device is held on its side.
 *
 *  Geometry measured off the reference screenshot (ui-spezifikation.md section 6): pill
 *  92 x 40 dp in #333333, knob 48 dp - it overhangs the pill by 4 dp on both sides, which is
 *  why the view is 48 dp thick - glyphs about 20 dp, centred 26 dp in from either end. (The
 *  detail spec's 96 x 40 dp pill with a 34 dp knob is what the screenshot overrules.)
 */
public class PhotoVideoSwitch extends View {
    private static final float PILL_LENGTH_DP = 92.0f;
    private static final float PILL_THICKNESS_DP = 40.0f;
    private static final float KNOB_DP = 48.0f;
    private static final float GLYPH_DP = 20.0f;
    /** Centre of each glyph (and of the knob when on that end) from the pill's end. */
    private static final float GLYPH_INSET_DP = 26.0f;

    private final float scale; // px per dp
    private final Paint pill_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knob_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF pill_rect = new RectF();
    private final Drawable photo_glyph;
    private final Drawable video_glyph;
    private final int glyph_on_knob_color;
    private final int glyph_color;

    private boolean video;
    /** Where the knob is along the pill: 0 at the photo end, 1 at the video end. */
    private float knob_position;
    /** Clockwise rotation of the two glyphs about their own centres, see setGlyphRotation(). */
    private float glyph_rotation;
    private ValueAnimator glyph_rotation_animator;

    private static final FloatPropertyCompat<PhotoVideoSwitch> KNOB_POSITION = new FloatPropertyCompat<PhotoVideoSwitch>("knobPosition") {
        @Override
        public float getValue(PhotoVideoSwitch view) {
            return view.knob_position;
        }

        @Override
        public void setValue(PhotoVideoSwitch view, float value) {
            view.knob_position = value;
            view.invalidate();
        }
    };
    private final SpringAnimation knob_spring;

    public PhotoVideoSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        scale = getResources().getDisplayMetrics().density;
        pill_paint.setColor(ContextCompat.getColor(context, R.color.photo_video_switch_pill));
        knob_paint.setColor(ContextCompat.getColor(context, R.color.photo_video_switch_knob));
        glyph_on_knob_color = ContextCompat.getColor(context, R.color.photo_video_switch_glyph_on_knob);
        glyph_color = ContextCompat.getColor(context, R.color.photo_video_switch_glyph);
        photo_glyph = ContextCompat.getDrawable(context, R.drawable.ic_photo_camera_white_48dp).mutate();
        video_glyph = ContextCompat.getDrawable(context, R.drawable.ic_videocam_white_48dp).mutate();
        knob_spring = new SpringAnimation(this, KNOB_POSITION).setSpring(ShutterButton.newSpringForce());
        // the knob's travel is a fraction, so the default 1 px threshold would end the spring at once
        knob_spring.setMinimumVisibleChange(0.002f);
    }

    private float dp(float dp) {
        return dp * scale;
    }

    public boolean isVideo() {
        return video;
    }

    /** Moves the knob to the video or photo end - with a spring when the state actually changes
     *  on screen, otherwise (first layout, or nothing changed) just puts it there.
     */
    public void setVideo(boolean video, boolean animate) {
        this.video = video;
        float target = video ? 1.0f : 0.0f;
        if( animate && isLaidOut() ) {
            knob_spring.animateToFinalPosition(target);
        }
        else {
            knob_spring.cancel();
            KNOB_POSITION.setValue(this, target);
        }
    }

    /** Turns the two glyphs on the spot so they read upright however the device is held - the
     *  pill and knob stay put. Same contract as MainUI.setViewRotation(): the shorter way round,
     *  at once when laying out or with the shared ease when the device has just been turned.
     */
    public void setGlyphRotation(int degrees, boolean animate) {
        if( glyph_rotation_animator != null ) {
            glyph_rotation_animator.cancel();
            glyph_rotation_animator = null;
        }
        float rotate_by = MainUI.shortestTurn(degrees - glyph_rotation);
        if( !animate || rotate_by == 0.0f ) {
            glyph_rotation = degrees;
            invalidate();
            return;
        }
        glyph_rotation_animator = ValueAnimator.ofFloat(glyph_rotation, glyph_rotation + rotate_by);
        glyph_rotation_animator.setDuration(MainUI.glyph_rotation_duration_ms);
        glyph_rotation_animator.setInterpolator(MainUI.glyph_rotation_interpolator);
        glyph_rotation_animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                glyph_rotation = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        glyph_rotation_animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Math.round(dp(PILL_LENGTH_DP)), Math.round(dp(KNOB_DP)));
    }

    /** X of the glyph (and knob) centre at position t along the pill, 0 = photo end on the
     *  left, 1 = video end on the right. */
    private float alongAt(float t) {
        float inset = dp(GLYPH_INSET_DP);
        float length = dp(PILL_LENGTH_DP);
        return inset + t * (length - 2 * inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float thickness = dp(KNOB_DP);
        float pill_thickness = dp(PILL_THICKNESS_DP);
        float length = dp(PILL_LENGTH_DP);
        float pill_inset = (thickness - pill_thickness) / 2;
        pill_rect.set(0, pill_inset, length, pill_inset + pill_thickness);
        canvas.drawRoundRect(pill_rect, pill_thickness / 2, pill_thickness / 2, pill_paint);

        float y = thickness / 2;
        canvas.drawCircle(alongAt(knob_position), y, thickness / 2, knob_paint);

        drawGlyph(canvas, photo_glyph, alongAt(0.0f), y, knob_position < 0.5f);
        drawGlyph(canvas, video_glyph, alongAt(1.0f), y, knob_position >= 0.5f);
    }

    private void drawGlyph(Canvas canvas, Drawable glyph, float x, float y, boolean on_knob) {
        int half = Math.round(dp(GLYPH_DP) / 2);
        glyph.setBounds(Math.round(x) - half, Math.round(y) - half, Math.round(x) + half, Math.round(y) + half);
        glyph.setTint(on_knob ? glyph_on_knob_color : glyph_color);
        canvas.save();
        canvas.rotate(glyph_rotation, x, y);
        glyph.draw(canvas);
        canvas.restore();
    }
}
