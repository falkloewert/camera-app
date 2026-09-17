package net.sourceforge.opencamera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/** A TextView whose text can be laid on its side, the way the mode bar labels appear when the
 *  device is held in landscape (docs/design-reference/pixel-original-querformat.png).
 *
 *  Why not just View.setRotation(): that only rotates how the view is drawn, not the space it
 *  takes up in the layout - a wide label would still occupy a wide, short box, so a column of
 *  them would be spaced as if they were lying flat, and anything anchored to the bar would sit
 *  in the wrong place. Instead this view swaps its measured width and height while the text is
 *  rotated, so the layout sees the box the reader sees, and the background (e.g. the pill behind
 *  the active mode) automatically becomes a vertical pill. Touch handling needs no special care,
 *  since the view itself isn't rotated - only its text is drawn rotated.
 *
 *  With a rotation of 0 this behaves exactly like a plain TextView.
 */
public class RotatedTextView extends AppCompatTextView {
    /** Clockwise rotation of the text in degrees: 0, 90 (reads top to bottom) or 270 (reads
     *  bottom to top). */
    private int text_rotation = 0;

    public RotatedTextView(Context context) {
        super(context);
    }

    public RotatedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotatedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTextRotation(int degrees) {
        if( degrees != 0 && degrees != 90 && degrees != 270 )
            throw new IllegalArgumentException("unsupported text rotation: " + degrees);
        if( this.text_rotation != degrees ) {
            this.text_rotation = degrees;
            requestLayout();
            invalidate();
        }
    }

    private boolean isSideways() {
        return text_rotation == 90 || text_rotation == 270;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if( isSideways() ) {
            // measure the text as if it were upright, then report the swapped size
            super.onMeasure(heightMeasureSpec, widthMeasureSpec);
            setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if( !isSideways() ) {
            super.onDraw(canvas);
            return;
        }
        // TextView.onDraw() clips and positions the text using the view's (now swapped) width and
        // height, so instead draw the text layout directly, on a canvas rotated so that the
        // upright text box maps onto our sideways bounds.
        Layout layout = getLayout();
        if( layout == null )
            return;
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        canvas.save();
        if( text_rotation == 90 ) {
            canvas.translate(getWidth(), 0);
            canvas.rotate(90);
        }
        else {
            canvas.translate(0, getHeight());
            canvas.rotate(-90);
        }
        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        layout.draw(canvas);
        canvas.restore();
    }
}
