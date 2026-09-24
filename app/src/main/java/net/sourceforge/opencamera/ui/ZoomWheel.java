package net.sourceforge.opencamera.ui;

import net.sourceforge.opencamera.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** The zoom pill of the reference (docs/design-reference/ui-spezifikation.md section 3). At
 *  rest it is the LinearLayout holding the step buttons that ZoomStepper builds. Dragged
 *  sideways - or while the user pinch-zooms in the viewfinder - it grows into the fine
 *  adjustment ruler ("Rad", section 3.2; pixel-camera-detailspec.md section 5): a 300 x 48 dp
 *  container in which a logarithmic scale with tick marks slides under a fixed white pointer
 *  in the middle, fading out towards both ends. The value under the pointer is shown by
 *  ZoomStepper in the badge floating above (zoom_badge).
 *
 *  One view for both states, so the touch that turned the pill into the ruler keeps driving
 *  the ruler, and everything that positions or hides the pill positions and hides the ruler
 *  with it. The step buttons are merely hidden while the ruler shows. Only the numbers turn
 *  when the device is held on its side (setGlyphRotation()); the ruler itself stays as printed
 *  on the fixed layout, which is what makes it stand upright in landscape as in the reference.
 *
 *  The reference screenshots don't show the ruler, so its geometry is the detail spec's
 *  where it gives a number and a plain choice where it doesn't (the choices are listed in
 *  ui-spezifikation.md section 8): 80 dp of scale per doubling of the zoom, minor ticks
 *  about 10 dp apart, main ticks 14 dp and minor ticks 9 dp tall, labels 12 sp, a 40 dp fade
 *  at either end.
 */
public class ZoomWheel extends LinearLayout {
    private static final float WHEEL_WIDTH_DP = 300.0f;
    private static final float WHEEL_HEIGHT_DP = 48.0f;
    /** How long the pill takes to grow into the ruler and back - Falk wanted it snappy
     *  (2026-09-18; one of the two animations besides the shutter, with the glyph turn). */
    private static final int MORPH_DURATION_MS = 120;
    /** Once the ruler has shrunk back to the pill, the step buttons fade in rather than pop
     *  (Falk, 2026-09-18: the selected circle appearing at once looked like the shrink being
     *  cut short). */
    private static final int BUTTONS_FADE_MS = 100;
    /** How much scale a doubling of the zoom takes up - the drag sensitivity. */
    private static final float DP_PER_DOUBLING = 80.0f;
    private static final float MINOR_TICK_SPACING_DP = 10.0f;
    private static final float FADE_DP = 40.0f;
    private static final float MAIN_TICK_WIDTH_DP = 1.5f;
    private static final float MINOR_TICK_WIDTH_DP = 0.75f;
    private static final float TICK_TOP_DP = 8.0f;
    private static final float MAIN_TICK_HEIGHT_DP = 14.0f;
    private static final float MINOR_TICK_HEIGHT_DP = 9.0f;
    private static final float LABEL_SIZE_SP = 12.0f;
    private static final float LABEL_BASELINE_DP = 39.0f;
    private static final float POINTER_WIDTH_DP = 2.0f;
    private static final float POINTER_HALO_WIDTH_DP = 6.0f;
    private static final float POINTER_TOP_DP = 6.0f;
    private static final float POINTER_HEIGHT_DP = 18.0f;
    /** The labelled marks (detail spec section 5, conflict K7), used as far as the camera's
     *  zoom range reaches. */
    private static final float [] MAIN_MARKS = {0.5f, 1.0f, 2.0f, 5.0f, 10.0f, 30.0f};

    /** What ZoomStepper listens to. */
    public interface Listener {
        /** The ruler has just appeared (true) or folded back into the pill (false). */
        void onWheelShown(boolean shown);
        /** The user is dragging the ruler: the value now under the pointer, and whether a tick
         *  mark went past the pointer since the last call. */
        void onWheelDragged(float ratio, boolean crossed_tick);
        /** The finger that dragged the ruler has lifted. */
        void onWheelReleased();
    }

    private final float scale; // px per dp
    private final int touch_slop;
    private final Paint tick_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointer_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint halo_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fade_paint = new Paint();

    private Listener listener;
    private float min_ratio = 1.0f;
    private float max_ratio = 1.0f;
    /** The zoom ratio under the pointer. */
    private float ratio = 1.0f;
    /** The tick marks, as log2 of their zoom ratio, ascending; a label where the tick is one
     *  of the MAIN_MARKS. */
    private float [] tick_log2 = new float[0];
    private String [] tick_label = new String[0];

    private boolean wheel_shown;
    /** How far the pill has grown into the ruler: 0 pill-sized, 1 ruler-sized. The ruler is
     *  drawn whenever this is above 0, the buttons come back once it is 0 again. */
    private float morph;
    private ValueAnimator morph_animator;
    private ValueAnimator buttons_fade;
    private boolean dragging;
    /** The touch moved mostly up or down before it moved sideways - leave it alone. */
    private boolean drag_rejected;
    /** Whether dragging the pill sideways turns it into the ruler at all. Off while the pill
     *  shows the manual lens selection, where the buttons are what the user wants (Kickoff
     *  3.4) - a pinch in the viewfinder still brings the ruler up. */
    private boolean drag_enabled = true;
    private float down_x;
    private float down_y;
    private float last_x;

    /** Clockwise rotation of the labels about their own centres, see setGlyphRotation(). */
    private float glyph_rotation;
    private ValueAnimator glyph_rotation_animator;

    public ZoomWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        scale = getResources().getDisplayMetrics().density;
        touch_slop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false); // a LinearLayout doesn't expect to draw anything itself

        int tick_color = ContextCompat.getColor(context, R.color.zoom_wheel_tick);
        tick_paint.setColor(tick_color);
        tick_paint.setStrokeCap(Paint.Cap.BUTT);
        label_paint.setColor(tick_color);
        label_paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, LABEL_SIZE_SP, getResources().getDisplayMetrics()));
        label_paint.setTextAlign(Paint.Align.CENTER);
        int pointer_color = ContextCompat.getColor(context, R.color.zoom_wheel_pointer);
        pointer_paint.setColor(pointer_color);
        halo_paint.setColor(pointer_color);
        halo_paint.setAlpha(64); // the "glow" around the pointer: the same white, faint and wider
        fade_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    private float dp(float dp) {
        return dp * scale;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isWheelShown() {
        return wheel_shown;
    }

    /** The zoom range the ruler covers; rebuilds the tick marks. */
    public void setRange(float min_ratio, float max_ratio) {
        this.min_ratio = min_ratio;
        this.max_ratio = Math.max(max_ratio, min_ratio);
        buildTicks();
        setRatio(ratio);
    }

    public float getRatio() {
        return ratio;
    }

    /** Moves the scale so that the supplied zoom ratio sits under the pointer.
     *  @return Whether a tick mark went past the pointer on the way there.
     */
    public boolean setRatio(float new_ratio) {
        new_ratio = Math.max(min_ratio, Math.min(max_ratio, new_ratio));
        boolean crossed = crossesTick(log2(ratio), log2(new_ratio));
        ratio = new_ratio;
        invalidate();
        return crossed;
    }

    /** Swaps between the step buttons (false) and the ruler (true): the buttons go at once
     *  and the pill grows into the ruler, or the ruler shrinks back and the buttons return
     *  once it is pill-sized again. */
    public void setWheelShown(boolean shown) {
        if( shown == wheel_shown )
            return;
        wheel_shown = shown;
        if( shown )
            setButtonsVisible(false);
        if( morph_animator != null ) {
            morph_animator.cancel();
            morph_animator = null;
        }
        morph_animator = ValueAnimator.ofFloat(morph, shown ? 1.0f : 0.0f);
        morph_animator.setDuration(MORPH_DURATION_MS);
        morph_animator.setInterpolator(MainUI.glyph_rotation_interpolator);
        morph_animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                morph = (float) animation.getAnimatedValue();
                requestLayout();
                invalidate();
            }
        });
        morph_animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // also reached when cancelled by the opposite morph, hence the checks
                if( !wheel_shown && morph < 0.001f )
                    setButtonsVisible(true);
            }
        });
        morph_animator.start();
        if( listener != null )
            listener.onWheelShown(shown);
    }

    /** Hides the step buttons at once, or brings them back with a short fade. Own animator
     *  rather than View.animate(), which MainUI uses to turn the buttons - the two must not
     *  cancel each other. */
    private void setButtonsVisible(boolean visible) {
        if( buttons_fade != null ) {
            buttons_fade.cancel();
            buttons_fade = null;
        }
        for(int i=0;i<getChildCount();i++) {
            View button = getChildAt(i);
            button.setAlpha(visible ? 0.0f : 1.0f);
            button.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
        if( visible ) {
            buttons_fade = ValueAnimator.ofFloat(0.0f, 1.0f);
            buttons_fade.setDuration(BUTTONS_FADE_MS);
            buttons_fade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float alpha = (float) animation.getAnimatedValue();
                    for(int i=0;i<getChildCount();i++)
                        getChildAt(i).setAlpha(alpha);
                }
            });
            buttons_fade.start();
        }
    }

    /** Turns the labels on the spot so they read upright however the device is held - the
     *  ruler stays put. Same contract as MainUI.setViewRotation(): the shorter way round, at
     *  once when laying out or with the shared ease when the device has just been turned.
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
    protected void onDetachedFromWindow() {
        // running animators keep the view alive after the dock was rebuilt - the same cleanup
        // RulerSlider does (review 2026-09-24, fund 7). The fade goes first: cancelling the morph
        // can bring the buttons back, and that fade is cancelled right after.
        if( buttons_fade != null ) {
            buttons_fade.cancel();
            buttons_fade = null;
        }
        if( glyph_rotation_animator != null ) {
            glyph_rotation_animator.cancel();
            glyph_rotation_animator = null;
        }
        if( morph_animator != null ) {
            morph_animator.cancel();
            morph_animator = null;
        }
        super.onDetachedFromWindow();
    }

    private static float log2(float value) {
        return (float) (Math.log(value) / Math.log(2.0));
    }

    /** Lays the tick marks out along the scale: a main tick with a label at every MAIN_MARK
     *  inside the range, and between neighbouring main marks as many evenly spaced minor
     *  ticks as fit at about MINOR_TICK_SPACING_DP; the scale's ends beyond the first and
     *  last main mark continue at the spacing of the neighbouring stretch.
     */
    private void buildTicks() {
        float lo = log2(min_ratio);
        float hi = log2(max_ratio);
        List<Float> marks = new ArrayList<>();
        for(float mark : MAIN_MARKS) {
            // a little tolerance at the ends: the camera's minimum comes as 0.51 for a 0.5x
            // lens (CameraController2.computeZoomRatios() rounds it up), and ",5" belongs
            // on the scale
            if( mark >= min_ratio * 0.97f && mark <= max_ratio * 1.03f )
                marks.add(log2(mark));
        }

        List<Float> ticks = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        final float default_step = MINOR_TICK_SPACING_DP / DP_PER_DOUBLING;
        if( marks.isEmpty() ) {
            for(float t=lo;t<=hi+1.0e-4f;t+=default_step) {
                ticks.add(t);
                labels.add(null);
            }
        }
        else {
            float first_step = default_step;
            float last_step = default_step;
            for(int i=0;i<marks.size();i++) {
                float mark = marks.get(i);
                ticks.add(mark);
                labels.add(ZoomStepper.formatRatio((float) Math.pow(2.0, mark), false));
                if( i+1 < marks.size() ) {
                    float next = marks.get(i+1);
                    int n_minor = Math.max(1, Math.round((next - mark) * DP_PER_DOUBLING / MINOR_TICK_SPACING_DP));
                    float step = (next - mark) / n_minor;
                    if( i == 0 )
                        first_step = step;
                    last_step = step;
                    for(int k=1;k<n_minor;k++) {
                        ticks.add(mark + k * step);
                        labels.add(null);
                    }
                }
            }
            // the stretch below the first main mark, and above the last one
            for(float t=marks.get(0)-first_step;t>=lo-1.0e-4f;t-=first_step) {
                ticks.add(t);
                labels.add(null);
            }
            for(float t=marks.get(marks.size()-1)+last_step;t<=hi+1.0e-4f;t+=last_step) {
                ticks.add(t);
                labels.add(null);
            }
        }

        // sort ascending, keeping labels with their ticks
        List<Integer> order = new ArrayList<>();
        for(int i=0;i<ticks.size();i++)
            order.add(i);
        Collections.sort(order, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Float.compare(ticks.get(a), ticks.get(b));
            }
        });
        tick_log2 = new float[ticks.size()];
        tick_label = new String[ticks.size()];
        for(int i=0;i<order.size();i++) {
            tick_log2[i] = ticks.get(order.get(i));
            tick_label[i] = labels.get(order.get(i));
        }
    }

    /** Whether a tick lies strictly between two positions on the scale (or exactly at the
     *  new one), i.e. the pointer passed over it going from one to the other. */
    private boolean crossesTick(float from_log2, float to_log2) {
        float a = Math.min(from_log2, to_log2);
        float b = Math.max(from_log2, to_log2);
        for(float tick : tick_log2) {
            if( tick > a && tick <= b )
                return true;
            if( tick > b )
                break;
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if( morph > 0.0f ) {
            // between the pill's size (what the buttons want, hidden or not) and the ruler's
            int pill_width = getMeasuredWidth();
            int pill_height = getMeasuredHeight();
            int width = Math.round(pill_width + (dp(WHEEL_WIDTH_DP) - pill_width) * morph);
            int height = Math.round(pill_height + (dp(WHEEL_HEIGHT_DP) - pill_height) * morph);
            setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // alpha mask that lets the scale fade out over the last FADE_DP at either end
        float fade = Math.min(dp(FADE_DP) / w, 0.5f);
        fade_paint.setShader(new LinearGradient(0, 0, w, 0,
                new int[] {0x00FFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0x00FFFFFF},
                new float[] {0.0f, fade, 1.0f - fade, 1.0f},
                Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if( morph <= 0.0f )
            return;
        int w = getWidth();
        int h = getHeight();
        float centre_x = w / 2.0f;
        float px_per_doubling = dp(DP_PER_DOUBLING);
        float current = log2(ratio);
        float tick_top = dp(TICK_TOP_DP);
        float main_bottom = tick_top + dp(MAIN_TICK_HEIGHT_DP);
        float minor_bottom = tick_top + dp(MINOR_TICK_HEIGHT_DP);
        float baseline = dp(LABEL_BASELINE_DP);
        Paint.FontMetrics metrics = label_paint.getFontMetrics();
        float label_centre_y = baseline + (metrics.ascent + metrics.descent) / 2.0f;

        // the scale, drawn into a layer so the fade can be multiplied into its alpha
        int saved = canvas.saveLayer(0, 0, w, h, null);
        for(int i=0;i<tick_log2.length;i++) {
            float x = centre_x + (tick_log2[i] - current) * px_per_doubling;
            if( x < -dp(FADE_DP) || x > w + dp(FADE_DP) )
                continue;
            boolean main = tick_label[i] != null;
            tick_paint.setStrokeWidth(dp(main ? MAIN_TICK_WIDTH_DP : MINOR_TICK_WIDTH_DP));
            canvas.drawLine(x, tick_top, x, main ? main_bottom : minor_bottom, tick_paint);
            if( main ) {
                canvas.save();
                canvas.rotate(glyph_rotation, x, label_centre_y);
                canvas.drawText(tick_label[i], x, baseline, label_paint);
                canvas.restore();
            }
        }
        canvas.drawRect(0, 0, w, h, fade_paint);
        canvas.restoreToCount(saved);

        // the static pointer in the middle
        float pointer_top = dp(POINTER_TOP_DP);
        float pointer_bottom = pointer_top + dp(POINTER_HEIGHT_DP);
        float halo_half = dp(POINTER_HALO_WIDTH_DP) / 2.0f;
        float pointer_half = dp(POINTER_WIDTH_DP) / 2.0f;
        canvas.drawRoundRect(centre_x - halo_half, pointer_top - halo_half, centre_x + halo_half, pointer_bottom + halo_half, halo_half, halo_half, halo_paint);
        canvas.drawRoundRect(centre_x - pointer_half, pointer_top, centre_x + pointer_half, pointer_bottom, pointer_half, pointer_half, pointer_paint);
    }

    // Touch: a tap still goes to the step button under it; a sideways move of more than the
    // touch slop is taken away from the button and drives the ruler instead. Once the ruler
    // shows (also when a pinch in the viewfinder brought it up), a touch on it drags at once.
    // Positions are taken in screen coordinates (getRawX()): the view grows and shifts when
    // the pill turns into the ruler, so its own coordinate frame moves under the finger.

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch( ev.getActionMasked() ) {
            case MotionEvent.ACTION_DOWN:
                down_x = ev.getRawX();
                down_y = ev.getRawY();
                drag_rejected = false;
                dragging = false;
                return wheel_shown; // no buttons to tap on the ruler: take the touch straight away
            case MotionEvent.ACTION_MOVE:
                if( !dragging && !drag_rejected )
                    decideDrag(ev);
                return dragging;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch( ev.getActionMasked() ) {
            case MotionEvent.ACTION_DOWN:
                down_x = ev.getRawX();
                down_y = ev.getRawY();
                drag_rejected = false;
                if( wheel_shown )
                    startDrag(ev.getRawX());
                return true;
            case MotionEvent.ACTION_MOVE:
                if( dragging )
                    drag(ev.getRawX());
                else if( !drag_rejected )
                    decideDrag(ev);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if( dragging )
                    endDrag();
                return true;
            default:
                return super.onTouchEvent(ev);
        }
    }

    public void setDragEnabled(boolean enabled) {
        this.drag_enabled = enabled;
    }

    /** Once the touch has moved further than the slop: sideways starts a drag, up or down
     *  gives the touch up for good. */
    private void decideDrag(MotionEvent ev) {
        if( !drag_enabled ) {
            drag_rejected = true;
            return;
        }
        float dx = ev.getRawX() - down_x;
        float dy = ev.getRawY() - down_y;
        if( Math.abs(dx) < touch_slop && Math.abs(dy) < touch_slop )
            return;
        if( Math.abs(dx) >= Math.abs(dy) )
            startDrag(ev.getRawX());
        else
            drag_rejected = true;
    }

    private void startDrag(float x) {
        dragging = true;
        last_x = x;
        setWheelShown(true);
    }

    /** Dragging the scale to the left brings the higher values under the pointer. */
    private void drag(float x) {
        float dx = x - last_x;
        last_x = x;
        float new_ratio = ratio * (float) Math.pow(2.0, -dx / dp(DP_PER_DOUBLING));
        boolean crossed = setRatio(new_ratio);
        if( listener != null )
            listener.onWheelDragged(ratio, crossed);
    }

    private void endDrag() {
        dragging = false;
        if( listener != null )
            listener.onWheelReleased();
    }
}
