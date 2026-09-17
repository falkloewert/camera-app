package net.sourceforge.opencamera.ui;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.R;
import net.sourceforge.opencamera.preview.Preview;

import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The row of fixed zoom steps (",5 / 1x / 2x / 5x") that replaces the old continuous zoom
 *  slider, modelled on the Pixel Camera app - see
 *  docs/design-reference/pixel-camera-screenshots.md.
 *
 *  The steps aren't hardcoded: they're derived from the zoom range the currently open camera
 *  actually reports, so a device with an ultra-wide lens gets a "0.5x" step and one without
 *  simply doesn't.
 */
public class ZoomStepper {
    private static final String TAG = "ZoomStepper";

    /** Round zoom factors offered in addition to the camera's minimum and 1x, where the zoom
     *  range reaches them. Deliberately stops at 5: the reference only offers ",5 / 1x / 2 / 5"
     *  and everything beyond that is meant to be dialled in on the fine adjustment wheel, not
     *  jumped to with a button (docs/design-reference/pixel-camera-screenshots.md). */
    private static final float [] preferred_steps = {2.0f, 5.0f};
    /** How much wider than 1x the camera's minimum has to be before it earns its own step, so a
     *  camera that only reaches 0.9x doesn't get a near-duplicate of "1x". */
    private static final float min_step_separation = 1.25f;

    private final MainActivity main_activity;
    /** Zoom factors (indices into the Preview's zoom ratio list), one per step button. */
    private final List<Integer> step_zoom_factors = new ArrayList<>();
    /** The actual zoom ratio each step lands on, used for its label. */
    private final List<Float> step_ratios = new ArrayList<>();
    private final List<Button> step_buttons = new ArrayList<>();
    private int selected_step = -1;

    public ZoomStepper(MainActivity main_activity) {
        this.main_activity = main_activity;
    }

    public boolean hasSteps() {
        return !step_zoom_factors.isEmpty();
    }

    /** The step buttons in their portrait order (widest lens first), for MainUI.orientBar(). */
    List<Button> getStepButtons() {
        return step_buttons;
    }

    /** Rebuilds the step buttons for the camera that's currently open. Safe to call whenever the
     *  camera is (re)opened or switched.
     */
    public void update() {
        LinearLayout container = main_activity.findViewById(R.id.zoom_stepper);
        container.removeAllViews();
        step_buttons.clear();
        step_zoom_factors.clear();
        step_ratios.clear();
        selected_step = -1;

        Preview preview = main_activity.getPreview();
        if( preview == null || !preview.supportsZoom() || preview.getCameraController() == null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "no zoom support, no steps");
            return;
        }

        for(float ratio : chooseStepRatios(preview)) {
            int zoom_factor = findZoomFactor(preview, ratio);
            int step_index = step_zoom_factors.size();
            step_zoom_factors.add(zoom_factor);
            step_ratios.add(preview.getZoomRatio(zoom_factor));
            Button button = createStepButton(preview.getZoomRatio(zoom_factor), zoom_factor, step_index);
            step_buttons.add(button);
        }
        // the buttons go into the bar via MainUI, which knows which way round they belong for
        // the current orientation (this runs after layoutUI() when a camera is opened)
        main_activity.getMainUI().orientBar(container, step_buttons);

        updateSelection(preview.getCameraController().getZoom());
    }

    /** Picks which zoom ratios deserve their own button: the camera's minimum (only if it's
     *  meaningfully wider than 1x, i.e. there's an ultra-wide lens), 1x itself, and whichever
     *  round steps the zoom range reaches.
     */
    private List<Float> chooseStepRatios(Preview preview) {
        float min_ratio = preview.getMinZoomRatio();
        float max_ratio = preview.getMaxZoomRatio();
        List<Float> ratios = new ArrayList<>();

        if( min_ratio * min_step_separation < 1.0f )
            ratios.add(min_ratio);
        if( min_ratio <= 1.0f && max_ratio >= 1.0f )
            ratios.add(1.0f);
        for(float step : preferred_steps) {
            if( step <= max_ratio )
                ratios.add(step);
        }
        if( MyDebug.LOG )
            Log.d(TAG, "zoom steps: " + ratios + " (range " + min_ratio + " to " + max_ratio + ")");
        return ratios;
    }

    /** Maps a desired zoom ratio onto the nearest zoom factor the camera actually offers. */
    private int findZoomFactor(Preview preview, float target_ratio) {
        int best_factor = 0;
        float best_distance = Float.MAX_VALUE;
        for(int factor=0;factor<=preview.getMaxZoom();factor++) {
            float distance = Math.abs(preview.getZoomRatio(factor) - target_ratio);
            if( distance < best_distance ) {
                best_distance = distance;
                best_factor = factor;
            }
        }
        return best_factor;
    }

    private Button createStepButton(float ratio, final int zoom_factor, final int step_index) {
        Button button = new Button(main_activity);
        int size = main_activity.getResources().getDimensionPixelSize(R.dimen.zoom_step_size);
        int margin = main_activity.getResources().getDimensionPixelSize(R.dimen.zoom_step_margin);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, margin, margin, margin);
        button.setLayoutParams(params);
        button.setText(formatRatio(ratio, false));
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setPadding(0, 0, 0, 0);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setMinimumWidth(0);
        button.setMinimumHeight(0);
        button.setContentDescription(main_activity.getResources().getString(R.string.zoom) + " " + formatRatio(ratio, true));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preview preview = main_activity.getPreview();
                if( preview == null || preview.getCameraController() == null )
                    return;
                preview.zoomTo(zoom_factor, false, true);
                // select the step that was tapped rather than reading the zoom back: zoomTo()
                // may still be transitioning, so the camera would report the old factor here
                setSelectedStep(step_index);
            }
        });
        return button;
    }

    /** Labels exactly as the reference does it: the leading zero is dropped (",5" rather than
     *  "0,5") and only the currently selected step carries the "x" ("1x" selected, plain "2"
     *  and "5" beside it). */
    private String formatRatio(float ratio, boolean selected) {
        String number;
        if( Math.abs(ratio - Math.round(ratio)) < 0.05f ) {
            number = String.format(Locale.getDefault(), "%.0f", ratio);
        }
        else {
            number = String.format(Locale.getDefault(), "%.1f", ratio);
            if( number.startsWith("0") )
                number = number.substring(1);
        }
        return selected ? number + "×" : number;
    }

    /** Highlights the step matching the supplied zoom factor (the step at or below the current
     *  zoom, so pinch-zooming between steps still shows which lens range you're in).
     */
    public void updateSelection(int zoom_factor) {
        int new_selected = -1;
        for(int i=0;i<step_zoom_factors.size();i++) {
            if( step_zoom_factors.get(i) <= zoom_factor )
                new_selected = i;
        }
        setSelectedStep(new_selected);
    }

    private void setSelectedStep(int new_selected) {
        if( new_selected == selected_step )
            return;
        selected_step = new_selected;
        for(int i=0;i<step_buttons.size();i++) {
            applyStepStyle(i, i == selected_step);
        }
    }

    /** Selected steps get the full light pill fill with dark text, unselected ones are plain
     *  light text on the stepper's own dark background (see the design reference). */
    private void applyStepStyle(int index, boolean selected) {
        Button button = step_buttons.get(index);
        button.setText(formatRatio(step_ratios.get(index), selected));
        if( selected ) {
            button.setBackgroundResource(R.drawable.pill_selected_background);
            button.setTextColor(main_activity.getResources().getColor(R.color.md_dark_surface, null));
            button.setTypeface(null, Typeface.BOLD);
        }
        else {
            button.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            button.setTextColor(main_activity.getResources().getColor(R.color.md_dark_on_surface, null));
            button.setTypeface(null, Typeface.NORMAL);
        }
    }
}
