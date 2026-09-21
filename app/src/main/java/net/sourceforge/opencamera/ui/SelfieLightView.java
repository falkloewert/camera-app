package net.sourceforge.opencamera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import net.sourceforge.opencamera.R;

/** Fills the areas beside the preview with white while the selfie lighting is on: the front
 *  camera has no flash, so the picture is lit this way instead (Kickoff Video-Blatt Runde 2,
 *  item 3). Only the bars around {@code @id/preview} are painted - the preview itself and the
 *  controls stay as they are. The view is deliberately not clickable, so touches fall through to
 *  the preview underneath. */
public class SelfieLightView extends View {
    private boolean on;

    public SelfieLightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOn(boolean on) {
        if( this.on != on ) {
            this.on = on;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if( !on )
            return;
        View preview = getRootView().findViewById(R.id.preview);
        if( preview == null )
            return;
        // the preview is a child of the same layout, so its bounds are already in our coordinates
        float left = preview.getLeft();
        float top = preview.getTop();
        float right = preview.getRight();
        float bottom = preview.getBottom();
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        canvas.drawRect(0.0f, 0.0f, canvas.getWidth(), top, p);           // above the preview
        canvas.drawRect(0.0f, bottom, canvas.getWidth(), canvas.getHeight(), p); // below it
        canvas.drawRect(0.0f, top, left, bottom, p);                     // left of it
        canvas.drawRect(right, top, canvas.getWidth(), bottom, p);        // right of it
    }
}
