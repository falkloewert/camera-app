package net.sourceforge.opencamera.ui;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.R;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.preview.Preview;

import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The zoom control of the Pixel Camera reference (docs/design-reference/ui-spezifikation.md
 *  section 3): the row of fixed zoom steps in the pill (",5 / 1x / 2 / 5"), and the fine
 *  adjustment wheel the pill turns into when dragged or while pinch-zooming (ZoomWheel), with
 *  the badge above it showing the value being dialled in.
 *
 *  Steps are the reference's, as far as the camera's zoom range reaches them: the back camera
 *  gets ",5 / 1x / 2 / 5" and everything past 5 is only reachable on the wheel. Behaviour
 *  after the wheel is let go follows the Pixel Camera as Falk checked it (2026-09-18): the
 *  pill comes back with the step nearest to the zoom selected, and that step's label shows
 *  the actual zoom ("2,8×" in the 2 slot, "6,7×" in the 5 slot); tapping it goes back to the
 *  step's own value.
 */
public class ZoomStepper implements ZoomWheel.Listener {
    private static final String TAG = "ZoomStepper";

    /** The steps offered above the camera's minimum on the back camera, where the zoom range
     *  reaches them - the reference's "1x / 2 / 5". */
    private static final float [] back_steps = {1.0f, 2.0f, 5.0f};
    /** The front camera's minimum is 0.9x, so a 1x step would sit right next to it; Falk chose
     *  these instead of the reference's ",7 / 1x" (decision 2026-09-18, after trying
     *  ",9 / 1x / 1,5" on the device). */
    private static final float [] front_steps = {1.25f, 2.0f, 5.0f};
    /** How far the wheel reaches on the front camera. The camera itself reports 10x, but the
     *  selfie sensor has nothing left to give past this - the Pixel Camera stops at 3x, Falk
     *  chose 5x (2026-09-18). */
    private static final float front_wheel_max = 5.0f;

    private final MainActivity main_activity;
    /** The zoom ratio each step stands for, widest first. Ratios rather than indices into the
     *  Preview's zoom list, since that list changes shape while recording video
     *  (CameraController2.setZoomSticky()). */
    private final List<Float> step_ratios = new ArrayList<>();
    private final List<Button> step_buttons = new ArrayList<>();
    private int selected_step = -1;
    /** What the selected step's label shows: its own ratio, or the actual zoom when the wheel
     *  or a pinch settled somewhere between steps. */
    private float selected_ratio = 1.0f;
    private long last_haptic_time;

    public ZoomStepper(MainActivity main_activity) {
        this.main_activity = main_activity;
    }

    public boolean hasSteps() {
        return !step_ratios.isEmpty();
    }

    /** The step buttons, widest lens first - the glyphs MainUI turns when the device is held
     *  on its side (the pill itself stays put). */
    List<Button> getStepButtons() {
        return step_buttons;
    }

    private ZoomWheel getWheel() {
        return main_activity.findViewById(R.id.zoom_stepper);
    }

    private TextView getBadge() {
        return main_activity.findViewById(R.id.zoom_badge);
    }

    /** Rebuilds the step buttons and the wheel's range for the camera that's currently open.
     *  Safe to call whenever the camera is (re)opened or switched.
     */
    public void update() {
        ZoomWheel wheel = getWheel();
        wheel.setListener(this);
        wheel.setWheelShown(false);
        wheel.removeAllViews();
        step_buttons.clear();
        step_ratios.clear();
        selected_step = -1;

        Preview preview = main_activity.getPreview();
        if( preview == null || !preview.supportsZoom() || preview.getCameraController() == null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "no zoom support, no steps");
            return;
        }

        for(float ratio : chooseStepRatios(preview)) {
            int step_index = step_ratios.size();
            step_ratios.add(ratio);
            Button button = createStepButton(ratio, step_index);
            // a new digit starts out turned the way the device is currently held, like the others
            button.setRotation(main_activity.getMainUI().getUIRotation());
            step_buttons.add(button);
            wheel.addView(button);
        }

        boolean front = preview.getCameraController().getFacing() == CameraController.Facing.FACING_FRONT;
        wheel.setRange(preview.getMinZoomRatio(), front ? Math.min(preview.getMaxZoomRatio(), front_wheel_max) : preview.getMaxZoomRatio());
        float current = preview.getZoomRatio();
        wheel.setRatio(current);
        updateSelection(current);
    }

    /** Picks which zoom ratios get a button: the camera's minimum if it's wider than 1x (an
     *  ultra-wide lens, or the selfie sensor's full width), then the steps for this camera as
     *  far as the zoom range reaches.
     */
    private List<Float> chooseStepRatios(Preview preview) {
        float min_ratio = preview.getMinZoomRatio();
        float max_ratio = preview.getMaxZoomRatio();
        boolean front = preview.getCameraController().getFacing() == CameraController.Facing.FACING_FRONT;
        List<Float> ratios = new ArrayList<>();

        if( min_ratio < 0.95f ) // anything closer to 1x would be labelled "1" twice
            ratios.add(min_ratio);
        for(float step : (front ? front_steps : back_steps)) {
            if( step >= min_ratio && step <= max_ratio )
                ratios.add(step);
        }
        if( MyDebug.LOG )
            Log.d(TAG, "zoom steps: " + ratios + " (range " + min_ratio + " to " + max_ratio + ", front " + front + ")");
        return ratios;
    }

    /** Maps a zoom ratio onto the nearest index in the Preview's zoom list - the only way to
     *  set the zoom. The list has 40 entries per doubling (about 1.75 % apart), so the
     *  difference to the wanted ratio is invisible. */
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

    private Button createStepButton(final float ratio, final int step_index) {
        Button button = new Button(main_activity);
        int size = main_activity.getResources().getDimensionPixelSize(R.dimen.zoom_step_size);
        int margin = main_activity.getResources().getDimensionPixelSize(R.dimen.zoom_step_margin);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, margin, margin, margin);
        button.setLayoutParams(params);
        button.setText(formatRatio(ratio, false, 2));
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setPadding(0, 0, 0, 0);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setMinimumWidth(0);
        button.setMinimumHeight(0);
        button.setContentDescription(main_activity.getResources().getString(R.string.zoom) + " " + formatRatio(ratio, true, 2));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preview preview = main_activity.getPreview();
                if( preview == null || preview.getCameraController() == null )
                    return;
                preview.zoomTo(findZoomFactor(preview, ratio), false, true);
                getWheel().setRatio(ratio);
                tick(view);
                // select the step that was tapped rather than reading the zoom back: zoomTo()
                // may still be transitioning, so the camera would report the old factor here
                setSelection(step_index, ratio);
            }
        });
        return button;
    }

    /** Labels exactly as the reference does it: the leading zero is dropped (",5" rather than
     *  "0,5"), one decimal below 10x ("6,7") and none from 10x up ("12"), and the "×" only
     *  where asked for (the selected step, the wheel's badge). */
    static String formatRatio(float ratio, boolean with_sign) {
        return formatRatio(ratio, with_sign, 1);
    }

    /** As above, with up to max_decimals decimals (trailing zeros dropped): 2 for the fixed
     *  steps, so the front camera's "1,25" keeps its value, 1 for anything dialled in. */
    private static String formatRatio(float ratio, boolean with_sign, int max_decimals) {
        String number;
        if( ratio >= 9.95f ) {
            number = String.format(Locale.getDefault(), "%.0f", ratio);
        }
        else {
            number = String.format(Locale.getDefault(), "%." + max_decimals + "f", ratio);
            number = number.replaceAll("0+$", "").replaceAll("[.,]$", "");
            if( number.startsWith("0") )
                number = number.substring(1);
        }
        return with_sign ? number + "×" : number;
    }

    /** Keeps the pill - and the wheel, while the user is pinch-zooming in the viewfinder - in
     *  step with a zoom set from anywhere else (pinch, volume keys). The wheel shows for as
     *  long as the pinch lasts (Preview.hasSmoothZoom()) and folds back into the pill when the
     *  fingers lift.
     */
    public void followZoom(int zoom_factor) {
        Preview preview = main_activity.getPreview();
        if( preview == null || !hasSteps() )
            return;
        float ratio = preview.getZoomRatio(zoom_factor);
        ZoomWheel wheel = getWheel();
        boolean crossed = wheel.setRatio(ratio);
        if( preview.hasSmoothZoom() ) {
            wheel.setWheelShown(true);
            getBadge().setText(formatRatio(wheel.getRatio(), true));
            if( crossed )
                tick(wheel);
        }
        else {
            wheel.setWheelShown(false);
        }
        updateSelection(ratio);
    }

    /** Highlights the step nearest to the supplied zoom (nearest in ratio, i.e. on the wheel's
     *  logarithmic scale), with the actual zoom as its label. */
    private void updateSelection(float ratio) {
        int nearest = -1;
        double best_distance = Double.MAX_VALUE;
        for(int i=0;i<step_ratios.size();i++) {
            double distance = Math.abs(Math.log(ratio / step_ratios.get(i)));
            if( distance < best_distance ) {
                best_distance = distance;
                nearest = i;
            }
        }
        setSelection(nearest, ratio);
    }

    private void setSelection(int new_selected, float shown_ratio) {
        if( new_selected == selected_step && shown_ratio == selected_ratio )
            return;
        selected_step = new_selected;
        selected_ratio = shown_ratio;
        for(int i=0;i<step_buttons.size();i++) {
            applyStepStyle(i, i == selected_step);
        }
    }

    /** Selected steps get the full light pill fill with dark text, unselected ones are plain
     *  light text on the stepper's own dark background (see the design reference). */
    private void applyStepStyle(int index, boolean selected) {
        Button button = step_buttons.get(index);
        float step_ratio = step_ratios.get(index);
        float shown_ratio = selected ? selected_ratio : step_ratio;
        // the step's own value keeps its second decimal ("1,25"), a value dialled in on the
        // wheel or by pinching shows one ("2,8")
        button.setText(formatRatio(shown_ratio, selected, shown_ratio == step_ratio ? 2 : 1));
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

    /** The reference's CLOCK_TICK, for a tapped step and for every tick mark the wheel's
     *  pointer passes - not while recording video, where the microphone would pick it up. */
    private void tick(View view) {
        Preview preview = main_activity.getPreview();
        if( preview != null && !preview.isVideoRecording() )
            last_haptic_time = MainUI.performHapticFeedback(view, last_haptic_time);
    }

    // ZoomWheel.Listener

    @Override
    public void onWheelShown(boolean shown) {
        TextView badge = getBadge();
        badge.setText(formatRatio(getWheel().getRatio(), true));
        badge.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onWheelDragged(float ratio, boolean crossed_tick) {
        Preview preview = main_activity.getPreview();
        if( preview == null || preview.getCameraController() == null )
            return;
        // straight to the value, no 200 ms transition: the finger is still on the wheel
        preview.zoomTo(findZoomFactor(preview, ratio), false, false);
        getBadge().setText(formatRatio(ratio, true));
        if( crossed_tick )
            tick(getWheel());
    }

    @Override
    public void onWheelReleased() {
        ZoomWheel wheel = getWheel();
        wheel.setWheelShown(false);
        updateSelection(wheel.getRatio());
    }
}
