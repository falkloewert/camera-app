package net.sourceforge.opencamera.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.HorizontalScrollView;

/**
 * The mode carousel under the shutter row (Kickoff Schritt D, ui-spezifikation.md 7.2): the active
 * entry sits in the middle, its neighbours run off to both sides and fade out at the edges.
 * Tapping a neighbour word slides it into the middle - that is the Rast - and the mode is only
 * switched once it has come to rest: {@link Listener#onEntryPicked} fires at the end of the
 * movement, so an entry that is merely passing through never changes the mode. That is what keeps
 * the camera from being rebuilt once per scrolled-past entry (switchPhotoMode() goes all the way
 * through MainActivity.updateForSettings()).
 *
 * The scrolling itself is the platform's: half a viewport of padding at either end is what lets
 * even the first and the last entry reach the middle, and the fade towards the edges is each
 * entry's own alpha, recomputed as it moves. requiresFadingEdge cannot do that: it paints the
 * edge over with the view's background colour, and this bar has none - it stands on the live
 * preview. Only the rest is animated here, so that its end can be caught.
 *
 * That padding is scroll room, not a visual inset, so clipping to it has to go: a scroll view
 * clips its children to itself minus padding, and half a viewport of padding leaves a clip of
 * exactly zero width - the whole bar laid out perfectly and drew nothing at all.
 */
public class ModeBar extends HorizontalScrollView {
    /** How long the entry takes to slide into the middle. The same 300 ms and easing as the glyph
     *  rotation - the app has exactly one motion language (SPEC 4.5, feste Entscheidungen). */
    private static final int SNAP_MS = 300;

    /** How long the scrolling has to stand still before it counts as come to rest, polled - long
     *  enough not to cut a fling short, short enough not to feel like a wait. */
    private static final int SETTLE_MS = 50;

    /** How far the entries fade out towards either edge - the same width the zoom ruler fades
     *  over (ui-spezifikation.md 7.2 "Alpha-Verlaeufe an beiden Raendern"). */
    private static final float FADE_DP = 40.0f;

    public interface Listener {
        /** The carousel has come to rest on this entry (a tap or a swipe): the mode should follow. */
        void onEntryPicked(View entry);
    }

    private Listener listener;

    /** The entry that belongs in the middle - the active one. Null until the first updateModeBar(). */
    private View wanted_entry;

    /** The running Rast movement, null when the carousel is at rest. */
    private ValueAnimator snap_animator;

    /** The scroll position the settle check last saw, to tell "still moving" from "standing still". */
    private int settle_scroll_x = Integer.MIN_VALUE;

    /** The CLOCK_TICK the last haptic was performed at (MainUI.performHapticFeedback() rate-limits). */
    private long last_haptic_time;

    private final Runnable settle_check = new Runnable() {
        @Override
        public void run() {
            if( snap_animator != null )
                return; // the Rast movement is still running - it catches its own end
            int scroll_x = getScrollX();
            if( scroll_x != settle_scroll_x ) {
                settle_scroll_x = scroll_x;
                postDelayed(this, SETTLE_MS);
                return;
            }
            snapToNearest();
        }
    };

    public ModeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipToPadding(false); // see the class comment - together with the padding in onSizeChanged()
    }

    /** The listeners are set once in the constructor of MainUI, before anything is laid out. */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Hands every entry of the bar to the same handling: a tap slides it into the middle, and the
     *  switch follows when it has arrived (updateModeBar() then finds it already there). */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if( getChildCount() == 0 )
            return;
        ViewGroup entries = (ViewGroup) getChildAt(0);
        for(int i = 0; i < entries.getChildCount(); i++) {
            entries.getChildAt(i).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View entry) {
                    tapOn(entry);
                }
            });
        }
    }

    /** Puts an entry in the middle. animate=false jumps: start-up, and every updateModeBar() after
     *  a mode change, where the Rast has already brought the entry there. */
    public void centerOn(View entry, boolean animate) {
        wanted_entry = entry;
        applyWanted(animate);
    }

    /** Half a viewport of padding at either end - without it the first and the last entry could
     *  never reach the middle (targetX() would be outside the scroll range). */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setPadding(w/2, 0, w/2, 0);
        applyEdgeFade();
    }

    /** A word that no longer fits in the bar fades as it leaves: whatever has run out at an edge
     *  takes it down over FADE_DP, so that it dissolves into the edge instead of being cut off
     *  (Detail-Spec 7: "horizontale Alpha-Verlaeufe an den Aussenraendern gegen hartes
     *  Abschneiden"). A word the bar still holds in full keeps its full colour, however close to
     *  the edge it stands. */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        applyEdgeFade();
    }

    /** Where an entry stands in the bar, in the bar's own coordinates: the entries are laid out
     *  in the child layout, which the scroll view puts behind its own padding. */
    private int localLeft(View entry) {
        return entry.getLeft() + getPaddingLeft() - getScrollX();
    }

    private void applyEdgeFade() {
        if( getChildCount() == 0 )
            return;
        ViewGroup entries = (ViewGroup) getChildAt(0);
        float fade = dp(FADE_DP);
        int w = getWidth();
        for(int i = 0; i < entries.getChildCount(); i++) {
            View entry = entries.getChildAt(i);
            if( entry.getVisibility() != View.VISIBLE ) {
                entry.setAlpha(1.0f); // back to full for the next time it is shown
                continue;
            }
            // the word itself, without the pill's own padding: a word the bar still holds in full
            // keeps its full colour, however close to the edge it stands
            float left = localLeft(entry) + entry.getPaddingLeft();
            float right = localLeft(entry) + entry.getWidth() - entry.getPaddingRight();
            float cut = Math.max(0.0f, -left) + Math.max(0.0f, right - w);
            entry.setAlpha(1.0f - Math.min(1.0f, cut / fade));
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // only now do the entries have their widths and lefts, and every updateModeBar() before the
        // first layout ran into an empty bar
        applyWanted(false);
        applyEdgeFade();
    }

    /** Where the bar has to scroll to so that entry sits in the middle. The entries sit inside the
     *  child layout, which the scroll view lays out behind its own padding, so the entry's centre
     *  only becomes a scroll position once that padding is added - and half a viewport of it is
     *  exactly what takes half a viewport off again. */
    private int targetX(View entry) {
        return getPaddingLeft() + entry.getLeft() + entry.getWidth()/2 - getWidth()/2;
    }

    private void applyWanted(boolean animate) {
        if( wanted_entry == null || snap_animator != null || getWidth() == 0 )
            return;
        if( wanted_entry.getVisibility() != View.VISIBLE || wanted_entry.getWidth() == 0 )
            return; // not laid out yet, or hidden right now (the other top-level mode's entries)
        int target = targetX(wanted_entry);
        if( animate )
            animateTo(target, null);
        else
            scrollTo(target, 0);
    }

    private void tapOn(View entry) {
        if( entry == wanted_entry )
            return; // already in the middle - nothing to rest on, and the mode is the same
        wanted_entry = entry;
        final View picked = entry;
        animateTo(targetX(entry), new Runnable() {
            @Override
            public void run() {
                snapHaptic();
                if( listener != null )
                    listener.onEntryPicked(picked);
            }
        });
    }

    /** The Rast movement - the only animation the carousel has (Kickoff 4). */
    private void animateTo(final int target, final Runnable on_rest) {
        cancelSnap();
        if( getScrollX() == target ) {
            if( on_rest != null )
                on_rest.run();
            return;
        }
        ValueAnimator animator = ValueAnimator.ofInt(getScrollX(), target);
        animator.setDuration(SNAP_MS);
        animator.setInterpolator(new PathInterpolator(0.2f, 0.0f, 0.0f, 1.0f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if( snap_animator != animation )
                    return; // cancelled or superseded - whoever did that owns the next step
                snap_animator = null;
                if( on_rest != null )
                    on_rest.run();
            }
        });
        snap_animator = animator;
        animator.start();
    }

    private void cancelSnap() {
        if( snap_animator != null ) {
            ValueAnimator animator = snap_animator;
            snap_animator = null;
            animator.cancel();
        }
    }

    /** A finger on the bar takes the movement over from the Rast, and a release hands it back: the
     *  settle check waits for the scrolling (drag or fling) to stand still, then rests on whatever
     *  entry ended up nearest the middle. */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if( action == MotionEvent.ACTION_DOWN )
            cancelSnap();
        else if( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL ) {
            settle_scroll_x = Integer.MIN_VALUE; // always poll once before deciding it has stopped
            // a second release must not start a second chain - both would poll and snap on their own
            // (review 2026-09-24, fund 6)
            removeCallbacks(settle_check);
            post(settle_check);
        }
        return super.onTouchEvent(ev);
    }

    private void snapToNearest() {
        View nearest = nearestToCenter();
        if( nearest == null )
            return;
        boolean changed = nearest != wanted_entry;
        wanted_entry = nearest;
        if( changed ) {
            animateTo(targetX(nearest), new Runnable() {
                @Override
                public void run() {
                    snapHaptic();
                    if( listener != null )
                        listener.onEntryPicked(nearest);
                }
            });
        }
        else {
            // the same entry as before, just left slightly off the middle by the finger
            scrollTo(targetX(nearest), 0);
        }
    }

    /** The visible entry whose centre is closest to the middle of the viewport. */
    private View nearestToCenter() {
        if( getChildCount() == 0 )
            return null;
        ViewGroup entries = (ViewGroup) getChildAt(0);
        int center = getScrollX() + getWidth()/2 - getPaddingLeft(); // the middle of the viewport, in the entries' coordinates
        View nearest = null;
        int nearest_distance = Integer.MAX_VALUE;
        for(int i = 0; i < entries.getChildCount(); i++) {
            View entry = entries.getChildAt(i);
            if( entry.getVisibility() != View.VISIBLE )
                continue;
            int distance = Math.abs(entry.getLeft() + entry.getWidth()/2 - center);
            if( distance < nearest_distance ) {
                nearest_distance = distance;
                nearest = entry;
            }
        }
        return nearest;
    }

    /** The CLOCK_TICK the reference gives when the carousel rests (detailspec 10) - the same
     *  preferred-feedback-aware helper the zoom wheel ticks with. */
    private void snapHaptic() {
        last_haptic_time = MainUI.performHapticFeedback(this, last_haptic_time);
    }
}
