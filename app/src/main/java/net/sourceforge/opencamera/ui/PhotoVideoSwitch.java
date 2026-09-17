package net.sourceforge.opencamera.ui;

import net.sourceforge.opencamera.R;

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
 *  Drawn by hand rather than built from child views so it can lay itself out along either
 *  axis: in landscape the pill stands on end like everything else in the rigidly turning
 *  layout (ui-spezifikation.md section 2), while the glyphs stay upright like the zoom digits.
 *  Which end holds "photo" follows the turn too - the portrait left end becomes the bottom in
 *  landscape, the top in reverse landscape (see setLayoutRotation()).
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

    /** 0, 90 or 270: how far the portrait arrangement has been turned on screen, see
     *  MainUI.getPortraitLayoutRotation(). */
    private int layout_rotation;
    private boolean video;
    /** Where the knob is along the pill: 0 at the photo end, 1 at the video end. */
    private float knob_position;

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

    private boolean isVertical() {
        return layout_rotation != 0;
    }

    /** See MainUI.getPortraitLayoutRotation(); called from MainUI.layoutUI(). */
    public void setLayoutRotation(int layout_rotation) {
        if( this.layout_rotation != layout_rotation ) {
            this.layout_rotation = layout_rotation;
            requestLayout();
            invalidate();
        }
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int length = Math.round(dp(PILL_LENGTH_DP));
        int thickness = Math.round(dp(KNOB_DP));
        if( isVertical() )
            setMeasuredDimension(thickness, length);
        else
            setMeasuredDimension(length, thickness);
    }

    /** Centre of the glyph (and knob) at position t along the pill, 0 = photo end, 1 = video
     *  end, in this view's coordinates. In portrait the photo end is on the left; turned 90
     *  degrees anticlockwise (landscape, rotation 270) that end is at the bottom, turned
     *  clockwise (reverse landscape, 90) at the top.
     */
    private float alongAt(float t) {
        float inset = dp(GLYPH_INSET_DP);
        float length = dp(PILL_LENGTH_DP);
        float from_photo_end = inset + t * (length - 2 * inset);
        if( layout_rotation == 270 )
            return length - from_photo_end;
        return from_photo_end;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float thickness = dp(KNOB_DP);
        float pill_thickness = dp(PILL_THICKNESS_DP);
        float length = dp(PILL_LENGTH_DP);
        float pill_inset = (thickness - pill_thickness) / 2;
        if( isVertical() )
            pill_rect.set(pill_inset, 0, pill_inset + pill_thickness, length);
        else
            pill_rect.set(0, pill_inset, length, pill_inset + pill_thickness);
        canvas.drawRoundRect(pill_rect, pill_thickness / 2, pill_thickness / 2, pill_paint);

        float across = thickness / 2;
        float knob_along = alongAt(knob_position);
        if( isVertical() )
            canvas.drawCircle(across, knob_along, thickness / 2, knob_paint);
        else
            canvas.drawCircle(knob_along, across, thickness / 2, knob_paint);

        drawGlyph(canvas, photo_glyph, alongAt(0.0f), across, knob_position < 0.5f);
        drawGlyph(canvas, video_glyph, alongAt(1.0f), across, knob_position >= 0.5f);
    }

    private void drawGlyph(Canvas canvas, Drawable glyph, float along, float across, boolean on_knob) {
        float x = isVertical() ? across : along;
        float y = isVertical() ? along : across;
        int half = Math.round(dp(GLYPH_DP) / 2);
        glyph.setBounds(Math.round(x) - half, Math.round(y) - half, Math.round(x) + half, Math.round(y) + half);
        glyph.setTint(on_knob ? glyph_on_knob_color : glyph_color);
        glyph.draw(canvas);
    }
}
