package net.sourceforge.opencamera.ui;

import net.sourceforge.opencamera.MyApplicationInterface;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.preview.Preview;
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
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
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

    private volatile boolean popup_view_is_open; // must be volatile for test project reading the state
    private PopupView popup_view;
    private final static boolean cache_popup = true; // if false, we recreate the popup each time
    private boolean force_destroy_popup = false; // if true, then the popup isn't cached for only the next time the popup is closed

    private int current_orientation; // how far the device has been turned clockwise from portrait, in steps of 90 degrees (onOrientationChanged())
    private int ui_rotation; // the rotation that makes a glyph read upright for the user, see updateUIRotation()
    /** Where the old icon row sits. There is only one fixed layout now (see layoutUI()), with
     *  the icons along the top edge; the enum survives for DrawPreview, which positions its
     *  on-screen text relative to the icons. */
    enum UIPlacement {
        UIPLACEMENT_RIGHT,
        UIPLACEMENT_LEFT,
        UIPLACEMENT_TOP
    }
    private View top_icon = null;
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

    // For remote control: keep track of the currently highlighted
    // line and icon within the line
    private boolean remote_control_mode; // whether remote control mode is enabled
    private int mPopupLine = 0;
    private int mPopupIcon = 0;
    private LinearLayout mHighlightedLine;
    private View mHighlightedIcon;
    private boolean mSelectingIcons = false;
    private boolean mSelectingLines = false;
    private int mExposureLine = 0;
    private boolean mSelectingExposureUIElement = false;
    private final int highlightColor = Color.rgb(183, 28, 28); // Red 900
    private final int highlightColorExposureUIElement = Color.rgb(244, 67, 54); // Red 500

    // for testing:
    private final Map<String, View> test_ui_buttons = new Hashtable<>();
    public int test_saved_popup_width;
    public int test_saved_popup_height;
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
     *  ui-spezifikation.md section 2): the icons of the top row, the shutter row and its
     *  stand-ins during video/panorama, the panorama lens pair and the zoom step digits. Each
     *  is a square view holding a single glyph - or a circle - so turning the whole view is the
     *  same as turning just the glyph. What is NOT here stays as printed on the fixed layout:
     *  the pills behind the zoom steps, mode bar and photo/video switch (the switch turns its
     *  two glyphs itself, see rotateGlyphs()), the mode bar labels, the popup menu and the
     *  exposure sliders (both old Open Camera elements the redesign replaces).
     */
    private List<View> glyphViews() {
        List<View> views = new ArrayList<>();
        views.add(main_activity.findViewById(R.id.settings));
        views.add(main_activity.findViewById(R.id.popup));
        views.add(main_activity.findViewById(R.id.exposure));
        onScreenIcons.addOnScreenIcons(views);
        views.add(main_activity.findViewById(R.id.kraken_icon));
        views.add(main_activity.findViewById(R.id.trash));
        views.add(main_activity.findViewById(R.id.share));
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

    public void layoutUI() {
        layoutUI(false);
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

    /** Die Zoom-Stufen samt ihrer Blase. Solange das Pro-Panel offen ist, bleiben sie in jedem
     *  Fall weg - die Pille des Panels sitzt an ihrer Stelle, und die Stufen haben dort nichts
     *  zu suchen (Falk 2026-09-21). Alle Stellen, die die Stufen ein- oder ausblenden, gehen
     *  hier durch, damit keine sie zurueckholt. */
    public void setZoomStepperVisibility(int visibility) {
        View view = main_activity.findViewById(R.id.zoom_stepper);
        if( view == null )
            return;
        if( main_activity.proPanelIsOpen() )
            visibility = View.INVISIBLE;
        view.setVisibility(visibility);
    }

    private void layoutUI(boolean popup_container_only) {
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

        if( !popup_container_only )
        {
            // reset:
            top_icon = null;

            // The old Open Camera icon row along the top edge (settings, popup menu, exposure,
            // plus whatever OnScreenIcons adds), running right to left from a dummy anchor in
            // the top right corner, so the row keeps its positioning even if the Settings
            // button is hidden (visibility set to View.GONE). Not part of the redesign yet -
            // the reference's header row replaces it in a later step.
            View view = main_activity.findViewById(R.id.gui_anchor);
            int view_id = view.getId();
            resetView(cs, view);
            cs.connect(view_id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
            cs.connect(view_id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
            View previous_view = view;

            // the gallery thumbnail is no longer one of these icons - it sits on the shutter row,
            // see below
            List<View> buttons_permanent = new ArrayList<>();
            buttons_permanent.add(main_activity.findViewById(R.id.settings));
            buttons_permanent.add(main_activity.findViewById(R.id.popup));
            buttons_permanent.add(main_activity.findViewById(R.id.exposure));

            onScreenIcons.addOnScreenIcons(buttons_permanent);

            buttons_permanent.add(main_activity.findViewById(R.id.kraken_icon));

            List<View> buttons_all = new ArrayList<>(buttons_permanent);
            // icons which only sometimes show on the icon panel:
            buttons_all.add(main_activity.findViewById(R.id.trash));
            buttons_all.add(main_activity.findViewById(R.id.share));

            for(View this_view : buttons_all) {
                int this_id = this_view.getId();
                resetView(cs, this_view);
                cs.connect(this_id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
                cs.connect(this_id, ConstraintSet.RIGHT, previous_view.getId(), ConstraintSet.LEFT, 0);
                previous_view = this_view;
            }

            // spread the permanent icons evenly across the width
            int button_size = main_activity.getResources().getDimensionPixelSize(R.dimen.onscreen_button_size);
            {
                int count = 0;
                View first_visible_view = null;
                View last_visible_view = null;
                for(View this_view : buttons_permanent) {
                    if( this_view.getVisibility() == View.VISIBLE ) {
                        if( first_visible_view == null )
                            first_visible_view = this_view;
                        last_visible_view = this_view;
                        count++;
                    }
                }
                if( MyDebug.LOG ) {
                    Log.d(TAG, "count: " + count);
                    Log.d(TAG, "display_width: " + display_width);
                }
                if( count > 0 ) {
                    int total_button_size = count*button_size;
                    int margin = 0;
                    if( total_button_size > display_width ) {
                        if( MyDebug.LOG )
                            Log.d(TAG, "need to reduce button size");
                        button_size = display_width / count;
                    }
                    else {
                        if( MyDebug.LOG )
                            Log.d(TAG, "need to increase margin");
                        if( count > 1 )
                            margin = (display_width - total_button_size) / (count-1);
                    }
                    if( MyDebug.LOG ) {
                        Log.d(TAG, "button_size: " + button_size);
                        Log.d(TAG, "total_button_size: " + total_button_size);
                        Log.d(TAG, "margin: " + margin);
                    }
                    // keep clear of the privacy indicator (camera/microphone dot) at the top: a
                    // hardcoded dp rather than WindowInsets.getPrivacyIndicatorBounds(), so the
                    // icons stay in the same location
                    final int privacy_gap_top = (int) (12 * scale + 0.5f); // convert dps to pixels
                    for(View this_view : buttons_permanent) {
                        if( this_view.getVisibility() == View.VISIBLE ) {
                            if( MyDebug.LOG ) {
                                Log.d(TAG, "set view layout for: " + this_view.getContentDescription());
                                if( this_view==first_visible_view ) {
                                    Log.d(TAG,"    first visible view");
                                }
                            }
                            int this_id = this_view.getId();
                            // be careful if we change how the margins are laid out: it looks nicer when only the settings icon
                            // is displayed (when taking a photo) if it is still shown right-most, rather than centred; also
                            // needed for "pause preview" trash/icons to be shown properly
                            int margin_right = this_view==first_visible_view ? navigation_gap_right : margin/2;
                            int margin_left = this_view==last_visible_view ? navigation_gap_left : margin/2;
                            cs.setMargin(this_id, ConstraintSet.LEFT, margin_left);
                            cs.setMargin(this_id, ConstraintSet.TOP, privacy_gap_top);
                            cs.setMargin(this_id, ConstraintSet.RIGHT, margin_right);
                            cs.setMargin(this_id, ConstraintSet.BOTTOM, 0);
                            cs.constrainWidth(this_id, button_size);
                            cs.constrainHeight(this_id, button_size);
                        }
                    }
                    top_icon = first_visible_view;
                }
            }

            // end icon panel

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
        if( !popup_container_only && !main_activity.proPanelIsOpen() )
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

        if( popupIsOpen() )
        {
            // the popup menu opens below the icon row, at the left of the screen, and is
            // limited to the screen's width in height so it doesn't take up the full screen
            final View view = main_activity.findViewById(R.id.popup_container);
            int view_id = view.getId();
            resetView(cs, view);
            cs.connect(view_id, ConstraintSet.TOP, R.id.popup, ConstraintSet.BOTTOM, 0);
            cs.connect(view_id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
            cs.constrainHeight(view_id, display_width);

            view.getViewTreeObserver().addOnGlobalLayoutListener(
                    new OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if( MyDebug.LOG )
                                Log.d(TAG, "onGlobalLayout()");
                            // We need to call onPopupLaidOut after the above layout param changes
                            // have taken effect, otherwise popup_height would be incorrect.
                            onPopupLaidOut();

                            // stop listening - only want to call this once!
                            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
            );
        }

        // apply every ConstraintSet change queued up above in a single pass, rather than
        // relaying out after each view (also matches how the old RelativeLayout code only ever
        // took visible effect once all the addRule() calls for that layoutUI() invocation had
        // been made)
        cs.applyTo(constraint_root);

        if( !popup_container_only ) {
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

    /** Remembers the popup's laid-out size (for the tests) and checks it against the size the
     *  popup asked for. The popup itself doesn't turn with the device: it is one of the old
     *  Open Camera elements the redesign replaces with a bottom sheet (ui-spezifikation.md 7.1).
     */
    private void onPopupLaidOut() {
        if( MyDebug.LOG )
            Log.d(TAG, "onPopupLaidOut");
        View view = main_activity.findViewById(R.id.popup_container);

        int popup_width = view.getWidth();
        int popup_height = view.getHeight();
        test_saved_popup_width = popup_width;
        test_saved_popup_height = popup_height;
        if( MyDebug.LOG ) {
            Log.d(TAG, "popup_width: " + popup_width);
            Log.d(TAG, "popup_height: " + popup_height);
            if( popup_view != null )
                Log.d(TAG, "popup total width: " + popup_view.getTotalWidth());
        }
        if( popup_view != null && popup_width > popup_view.getTotalWidth()*1.2  ) {
            // This is a workaround for the rare but annoying bug where the popup window is too large
            // (and appears partially off-screen). Unfortunately have been unable to fix - and trying
            // to force the popup container to have a particular width just means some of the contents
            // (e.g., Timer) are missing. But at least stop caching it, so that reopening the popup
            // should fix it, rather than having to restart or pause/resume Open Camera.
            // Also note, normally we should expect popup_width == popup_view.getTotalWidth(), but
            // have put a fudge factor of 1.2 just in case it's normally slightly larger on some
            // devices.
            Log.e(TAG, "### popup view is too big?!");
            force_destroy_popup = true;
        }
        else {
            force_destroy_popup = false;
        }
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
                state = main_activity.getPreview().isVideoRecording() ? ShutterButton.State.VIDEO_RECORDING : ShutterButton.State.VIDEO;
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

    /** Wires up the mode bar (Photo / Video / Panorama), the horizontal replacement for picking
     *  the mode out of the popup menu - see docs/design-reference/pixel-camera-screenshots.md.
     *  Only modes the app actually implements are offered here.
     */
    private void setupModeBar() {
        // The bar only lists the sub-modes of whatever the PhotoVideoSwitch is set to, and
        // photo <-> video isn't switched here at all. While taking photos that is "Foto" (the
        // ordinary photo) and "Panorama", as in the reference - Falk's decision (2026-09-17,
        // revising audit step 1): a "Foto" entry is needed to get back out of Panorama, and out
        // of whatever modes come later. Video has no sub-modes yet, so the bar is empty while
        // filming (updateModeBar()).
        setModeBarEntry(R.id.mode_bar_photo, "preference_photo_mode_std");
        setModeBarEntry(R.id.mode_bar_panorama, "preference_photo_mode_panorama");
    }

    private void setModeBarEntry(int view_id, final String photo_mode_preference_value) {
        main_activity.findViewById(view_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchPhotoMode(photo_mode_preference_value);
                updateModeBar();
            }
        });
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
     *  PopupView.changePhotoMode() takes when the mode is picked from the popup menu.
     */
    private void switchPhotoMode(String photo_mode_preference_value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.PhotoModePreferenceKey, photo_mode_preference_value);
        editor.apply();
        main_activity.getApplicationInterface().getDrawPreview().updateSettings(); // because we cache the photomode
        main_activity.updateForSettings(true, null, false, true);
        destroyPopup(); // popup contents depend on the photo mode
    }

    /** Shows the sub-modes of the current top-level mode and highlights the active one: Foto
     *  and Panorama while taking photos (Panorama only where the camera supports it - not with
     *  the selfie camera, see MainActivity.supportsPanorama()), nothing while filming. The bar's
     *  own place is fixed (layoutUI()), so an empty bar moves nothing.
     */
    public void updateModeBar() {
        boolean is_video = main_activity.getPreview().isVideo();
        boolean offer_panorama = !is_video && main_activity.supportsPanorama();
        boolean is_panorama = !is_video &&
                main_activity.getApplicationInterface().getPhotoMode() == MyApplicationInterface.PhotoMode.Panorama;

        TextView photo_entry = main_activity.findViewById(R.id.mode_bar_photo);
        TextView panorama_entry = main_activity.findViewById(R.id.mode_bar_panorama);
        photo_entry.setVisibility(is_video ? View.GONE : View.VISIBLE);
        panorama_entry.setVisibility(offer_panorama ? View.VISIBLE : View.GONE);
        applyModeBarStyle(photo_entry, !is_video && !is_panorama);
        applyModeBarStyle(panorama_entry, is_panorama);
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

    /** Set content description for switch camera button.
     */
    public void setSwitchCameraContentDescription() {
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

    public void updateRemoteConnectionIcon() {
        View remoteConnectedIcon = main_activity.findViewById(R.id.kraken_icon);
        if (main_activity.getBluetoothRemoteControl().remoteConnected() ) {
            if( MyDebug.LOG )
                Log.d(TAG, "Remote control connected");
            remoteConnectedIcon.setVisibility(View.VISIBLE);
        } else {
            if( MyDebug.LOG )
                Log.d(TAG, "Remote control DISconnected");
            remoteConnectedIcon.setVisibility(View.GONE);
        }

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
                View exposureButton = main_activity.findViewById(R.id.exposure);
                View popupButton = main_activity.findViewById(R.id.popup);
                View galleryButton = main_activity.findViewById(R.id.gallery);
                View settingsButton = main_activity.findViewById(R.id.settings);
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
                if( main_activity.supportsExposureButton() )
                    exposureButton.setVisibility(visibility);
                onScreenIcons.setVisibility(visibility, visibility);
                popupButton.setVisibility(visibility);
                galleryButton.setVisibility(visibility);
                settingsButton.setVisibility(visibility);
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
                View settingsButton = main_activity.findViewById(R.id.settings);
                View switchCameraButton = main_activity.findViewById(R.id.switch_camera);
                View switchMultiCameraButton = main_activity.findViewById(R.id.switch_multi_camera);
                View galleryButton = main_activity.findViewById(R.id.gallery);
                View exposureButton = main_activity.findViewById(R.id.exposure);
                View popupButton = main_activity.findViewById(R.id.popup);
                settingsButton.setVisibility(visibility_video); // still allow settings when recording video - arguably we shouldn't, but looks wierd given that the other default icons aren't hidden when recording video
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
                    panoramaLens1xButton.setAlpha(is_wide ? 0.5f : 1.0f);
                    panoramaLensWideButton.setAlpha(is_wide ? 1.0f : 0.5f);
                    panoramaLensRow.setVisibility(visibility);
                }
                else {
                    panoramaLensRow.setVisibility(View.GONE);
                }
                // the gallery thumbnail gives way to the photo snapshot button while recording
                // video (both sit left of the shutter); switch_video is never shown any more
                galleryButton.setVisibility(show_gui_video ? View.VISIBLE : View.GONE);
                if( main_activity.supportsExposureButton() )
                    exposureButton.setVisibility(visibility_video); // still allow exposure when recording video
                onScreenIcons.setVisibility(visibility, visibility_video);
                // zoom steps only make sense where zoom is allowed at all (e.g. not in panorama
                // mode, see MyApplicationInterface.allowZoom()); they stay while recording
                // video, so a lens can still be picked mid-recording (Falk, 2026-09-18)
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
                if( !(show_gui_photo && show_gui_video) ) {
                    closePopup(); // we still allow the popup when recording video, but need to update the UI (so it only shows flash options), so easiest to just close
                }

                View remoteConnectedIcon = main_activity.findViewById(R.id.kraken_icon);
                if (main_activity.getBluetoothRemoteControl().remoteConnected() ) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "Remote control connected");
                    remoteConnectedIcon.setVisibility(View.VISIBLE);
                } else {
                    if( MyDebug.LOG )
                        Log.d(TAG, "Remote control DISconnected");
                    remoteConnectedIcon.setVisibility(View.GONE);
                }
                popupButton.setVisibility(main_activity.getPreview().supportsFlash() ? visibility_video : visibility); // still allow popup in order to change flash mode when recording video

                if( show_gui_photo && show_gui_video ) {
                    layoutUI(); // needed for "top" UIPlacement, to auto-arrange the buttons
                }
            }
        });
    }

    public void audioControlStarted() {
        ImageButton view = main_activity.findViewById(R.id.audio_control);
        view.setImageResource(R.drawable.ic_mic_red_48dp);
        view.setContentDescription( main_activity.getResources().getString(R.string.audio_control_stop) );
    }

    public void audioControlStopped() {
        ImageButton view = main_activity.findViewById(R.id.audio_control);
        view.setImageResource(R.drawable.ic_mic_white_48dp);
        view.setContentDescription( main_activity.getResources().getString(R.string.audio_control_start) );
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
        closePopup();
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
                nextIsoItem(false);
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
                nextIsoItem(true);
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

    private void nextIsoItem(boolean previous) {
        if( MyDebug.LOG )
            Log.d(TAG, "nextIsoItem: " + previous);
        // Find current ISO
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        String current_iso = sharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
        int count = iso_buttons.size();
        int step = previous ? -1 : 1;
        boolean found = false;
        for(int i = 0; i < count; i++) {
            Button button = (Button)iso_buttons.get(i);
            String button_text = String.valueOf(button.getText());
            if( ISOTextEquals(button_text, current_iso) ) {
                found = true;
                // Select next one, unless it's "Manual", which we skip since
                // it's not practical in remote mode.
                Button nextButton = (Button) iso_buttons.get((i + count + step)%count);
                String nextButton_text = String.valueOf(nextButton.getText());
                if( nextButton_text.contains("m") ) {
                    nextButton = (Button) iso_buttons.get((i+count+ 2*step)%count);
                }
                nextButton.callOnClick();
                break;
            }
        }
        if( !found ) {
            // For instance, we are in ISO manual mode and "M" is selected. default
            // back to "Auto" to avoid being stuck since we're with a remote control
            iso_buttons.get(0).callOnClick();
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
            ViewGroup iso_buttons_container = main_activity.findViewById(R.id.iso_buttons);
            iso_buttons_container.setBackgroundColor(highlightColorExposureUIElement);
            //iso_buttons_container.setAlpha(1f);
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            String current_iso = sharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
            // if the manual ISO value isn't one of the "preset" values, then instead highlight the manual ISO icon
            boolean found = false;
            Button manualButton = null;
            for(View view : iso_buttons) {
                Button button = (Button)view;
                String button_text = String.valueOf(button.getText());
                if( ISOTextEquals(button_text, current_iso) ) {
                    PopupView.setButtonSelected(button, true);
                    //button.setBackgroundColor(highlightColorExposureUIElement);
                    //button.setAlpha(0.3f);
                    found = true;
                }
                else {
                    if( button_text.contains("m") ) {
                        manualButton = button;
                    }
                    PopupView.setButtonSelected(button, false);
                    button.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            if( !found && manualButton != null ) {
                // We are in manual ISO, highlight the "M" button
                PopupView.setButtonSelected(manualButton, true);
                manualButton.setBackgroundColor(highlightColorExposureUIElement);
                //manualButton.setAlpha(0.3f);
            }
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
        if (popupIsOpen()) {
            didProcess = true;
            if (selectingIcons()) {
                previousPopupIcon();
            } else if (selectingLines()) {
                previousPopupLine();
            }
        } else if (isExposureUIOpen()) {
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
        if (popupIsOpen()) {
            if (selectingIcons()) {
                nextPopupIcon();
            } else if (selectingLines()) {
                nextPopupLine();
            }
            didProcess = true;
        } else if (isExposureUIOpen()) {
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
        test_ui_buttons.clear();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        final Preview preview = main_activity.getPreview();
        ImageButton view = main_activity.findViewById(R.id.exposure);
        view.setImageResource(R.drawable.ic_exposure_red_48dp);
        View sliders_container = main_activity.findViewById(R.id.sliders_container);
        sliders_container.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(main_activity, R.anim.fade_in);
        sliders_container.startAnimation(animation);
        ViewGroup iso_buttons_container = main_activity.findViewById(R.id.iso_buttons);
        iso_buttons_container.removeAllViews();
        List<String> supported_isos;
        if( preview.isVideoRecording() ) {
            supported_isos = null;
        }
        else if( preview.supportsISORange() ) {
            if( MyDebug.LOG )
                Log.d(TAG, "supports ISO range");
            int min_iso = preview.getMinimumISO();
            int max_iso = preview.getMaximumISO();
            List<String> values = new ArrayList<>();
            values.add(CameraController.ISO_DEFAULT);
            values.add(manual_iso_value);
            iso_button_manual_index = 1; // must match where we place the manual button!
            int [] iso_values = {50, 100, 200, 400, 800, 1600, 3200, 6400};
            values.add(ISOToButtonText(min_iso));
            for(int iso_value : iso_values) {
                if( iso_value > min_iso && iso_value < max_iso ) {
                    values.add(ISOToButtonText(iso_value));
                }
            }
            values.add(ISOToButtonText(max_iso));
            supported_isos = values;
        }
        else {
            supported_isos = preview.getSupportedISOs();
            iso_button_manual_index = -1;
        }
        String current_iso = sharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
        // if the manual ISO value isn't one of the "preset" values, then instead highlight the manual ISO icon
        if( !current_iso.equals(CameraController.ISO_DEFAULT) && supported_isos != null && supported_isos.contains(manual_iso_value) && !supported_isos.contains(current_iso) )
            current_iso = manual_iso_value;


        int total_width_dp = 280;
        int max_width_dp = getMaxHeightDp(true);
        if( total_width_dp > max_width_dp )
            total_width_dp = max_width_dp;
        if( MyDebug.LOG )
            Log.d(TAG, "total_width_dp: " + total_width_dp);

        // n.b., we hardcode the string "ISO" as this isn't a user displayed string, rather it's used to filter out "ISO" included in old Camera API parameters
        iso_buttons = PopupView.createButtonOptions(iso_buttons_container, main_activity, total_width_dp, test_ui_buttons, supported_isos, -1, -1, "ISO", false, current_iso, 0, "TEST_ISO", new PopupView.ButtonOptionsPopupListener() {
            @Override
            public void onClick(String option) {
                if( MyDebug.LOG )
                    Log.d(TAG, "clicked iso: " + option);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String old_iso = sharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
                if( MyDebug.LOG )
                    Log.d(TAG, "old_iso: " + old_iso);
                editor.putString(PreferenceKeys.ISOPreferenceKey, option);
                String toast_option = option;

                if( preview.supportsISORange() ) {
                    if( option.equals(CameraController.ISO_DEFAULT) ) {
                        if( MyDebug.LOG )
                            Log.d(TAG, "switched from manual to auto iso");
                        // also reset exposure time when changing from manual to auto from the popup menu:
                        editor.putLong(PreferenceKeys.ExposureTimePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT);
                        editor.apply();
                        preview.showToast(null, "ISO: " + toast_option, 0, true); // supply offset_y_dp to be consistent with preview.setExposure(), preview.setISO()
                        main_activity.updateForSettings(true, ""); // already showed the toast, so block from showing again
                    }
                    else if( old_iso.equals(CameraController.ISO_DEFAULT) ) {
                        if( MyDebug.LOG )
                            Log.d(TAG, "switched from auto to manual iso");
                        if( option.equals("m") ) {
                            // if we used the generic "manual", then instead try to preserve the current iso if it exists
                            if( preview.getCameraController() != null && preview.getCameraController().captureResultHasIso() ) {
                                int iso = preview.getCameraController().captureResultIso();
                                if( MyDebug.LOG )
                                    Log.d(TAG, "apply existing iso of " + iso);
                                editor.putString(PreferenceKeys.ISOPreferenceKey, String.valueOf(iso));
                                toast_option = String.valueOf(iso);
                            }
                            else {
                                if( MyDebug.LOG )
                                    Log.d(TAG, "no existing iso available");
                                // use a default
                                final int iso = 800;
                                editor.putString(PreferenceKeys.ISOPreferenceKey, "" + iso);
                                toast_option = "" + iso;
                            }
                        }

                        // if changing from auto to manual, preserve the current exposure time if it exists
                        if( preview.getCameraController() != null && preview.getCameraController().captureResultHasExposureTime() ) {
                            long exposure_time = preview.getCameraController().captureResultExposureTime();
                            if( MyDebug.LOG )
                                Log.d(TAG, "apply existing exposure time of " + exposure_time);
                            editor.putLong(PreferenceKeys.ExposureTimePreferenceKey, exposure_time);
                        }
                        else {
                            if( MyDebug.LOG )
                                Log.d(TAG, "no existing exposure time available");
                        }

                        editor.apply();
                        preview.showToast(null, "ISO: " + toast_option, 0, true); // supply offset_y_dp to be consistent with preview.setExposure(), preview.setISO()
                        main_activity.updateForSettings(true, ""); // already showed the toast, so block from showing again
                    }
                    else {
                        if( MyDebug.LOG )
                            Log.d(TAG, "changed manual iso");
                        if( option.equals("m") ) {
                            // if user selected the generic "manual", then just keep the previous non-ISO option
                            if( MyDebug.LOG )
                                Log.d(TAG, "keep existing iso of " + old_iso);
                            editor.putString(PreferenceKeys.ISOPreferenceKey, old_iso);
                        }

                        editor.apply();
                        int iso = preview.parseManualISOValue(option);
                        if( iso >= 0 ) {
                            // if changing between manual ISOs, no need to call updateForSettings, just change the ISO directly (as with changing the ISO via manual slider)
                            //preview.setISO(iso);
                            //updateSelectedISOButton();
                            // rather than set ISO directly, we move the seekbar, and the ISO will be changed via the seekbar listener
                            SeekBar iso_seek_bar = main_activity.findViewById(R.id.iso_seekbar);
                            main_activity.getManualSeekbars().setISOProgressBarToClosest(iso_seek_bar, iso);
                        }
                    }
                }
                else {
                    editor.apply();
                    if( preview.getCameraController() != null ) {
                        preview.getCameraController().setISO(option);
                    }
                }

                setupExposureUI();
            }
        });
        if( supported_isos != null ) {
            View iso_container_view = main_activity.findViewById(R.id.iso_container);
            iso_container_view.setVisibility(View.VISIBLE);
        }

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
    public static boolean ISOTextEquals(String button_text, String iso) {
        // Can't use equals(), due to the \n that Popupview.getButtonOptionString() inserts, and
        // also good to make this general in case in future we support other text formats.
        // We really want to check that iso is the last word in button_text.
        if( button_text.endsWith(iso) ) {
            return button_text.length()==iso.length() || Character.isWhitespace( button_text.charAt(button_text.length()-iso.length()-1) );
        }
        return false;
    }

    /** Returns the ISO button text for the supplied iso.
     *  Should only be used for Preview.supportsISORange()==true (i.e., full manual ISO).
     */
    public static String ISOToButtonText(int iso) {
        // n.b., if we change how the ISO is converted to a string for the button, will also need
        // to update updateSelectedISOButton()
        return String.valueOf(iso);
    }

    /** If the exposure panel is open, updates the selected ISO button to match the current ISO value,
     *  if a continuous range of ISO values are supported by the camera.
     */
    public void updateSelectedISOButton() {
        if( MyDebug.LOG )
            Log.d(TAG, "updateSelectedISOButton");
        Preview preview = main_activity.getPreview();
        if( preview.supportsISORange() && isExposureUIOpen() ) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            String current_iso = sharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
            // if the manual ISO value isn't one of the "preset" values, then instead highlight the manual ISO icon
            if( MyDebug.LOG )
                Log.d(TAG, "current_iso: " + current_iso);
            boolean found = false;
            for(View view : iso_buttons) {
                Button button = (Button)view;
                if( MyDebug.LOG )
                    Log.d(TAG, "button: " + button.getText());
                String button_text = String.valueOf(button.getText());
                if( ISOTextEquals(button_text, current_iso) ) {
                    PopupView.setButtonSelected(button, true);
                    found = true;
                }
                else {
                    PopupView.setButtonSelected(button, false);
                }
            }
            if( !found && !current_iso.equals(CameraController.ISO_DEFAULT) ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "must be manual");
                if( iso_button_manual_index >= 0 && iso_button_manual_index < iso_buttons.size() ) {
                    Button button = (Button)iso_buttons.get(iso_button_manual_index);
                    PopupView.setButtonSelected(button, true);
                }
            }
        }
    }

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
        ImageButton image_button = main_activity.findViewById(R.id.exposure);
        image_button.setImageResource(R.drawable.ic_exposure_white_48dp);

        clearRemoteControlForExposureUI(); // must be called before we actually close the exposure panel
        View view = main_activity.findViewById(R.id.sliders_container);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.iso_container);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.exposure_container);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.manual_exposure_container);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.manual_white_balance_container);
        view.setVisibility(View.GONE);
    }

    public void setPopupIcon() {
        if( MyDebug.LOG )
            Log.d(TAG, "setPopupIcon");
        ImageButton popup = main_activity.findViewById(R.id.popup);
        String flash_value = main_activity.getPreview().getCurrentFlashValue();
        if( MyDebug.LOG )
            Log.d(TAG, "flash_value: " + flash_value);
        if( main_activity.getMainUI().getOnScreenIcons().showCycleFlashIcon() ) {
            popup.setImageResource(R.drawable.popup);
        }
        else if( flash_value != null && flash_value.equals("flash_off") ) {
            popup.setImageResource(R.drawable.popup_flash_off);
        }
        else if( flash_value != null && ( flash_value.equals("flash_torch") || flash_value.equals("flash_frontscreen_torch") ) ) {
            popup.setImageResource(R.drawable.popup_flash_torch);
        }
        else if( flash_value != null && ( flash_value.equals("flash_auto") || flash_value.equals("flash_frontscreen_auto") ) ) {
            popup.setImageResource(R.drawable.popup_flash_auto);
        }
        else if( flash_value != null && ( flash_value.equals("flash_on") || flash_value.equals("flash_frontscreen_on") ) ) {
            popup.setImageResource(R.drawable.popup_flash_on);
        }
        else if( flash_value != null && flash_value.equals("flash_red_eye") ) {
            popup.setImageResource(R.drawable.popup_flash_red_eye);
        }
        else {
            popup.setImageResource(R.drawable.popup);
        }
    }

    public void closePopup() {
        if( MyDebug.LOG )
            Log.d(TAG, "close popup");

        main_activity.enablePopupOnBackPressedCallback(false);

        if( popupIsOpen() ) {
            clearRemoteControlForPopup(); // must be called before we set popup_view_is_open to false; and before clearSelectionState() so we know which highlighting to disable
            clearSelectionState();

            popup_view_is_open = false;
            /* Not destroying the popup doesn't really gain any performance.
             * Also there are still outstanding bugs to fix if we wanted to do this:
             *   - Not resetting the popup menu when switching between photo and video mode. See test testVideoPopup().
             *   - When changing options like flash/focus, the new option isn't selected when reopening the popup menu. See test
             *     testPopup().
             *   - Changing settings potentially means we have to recreate the popup, so the natural place to do this is in
             *     MainActivity.updateForSettings(), but doing so makes the popup close when checking photo or video resolutions!
             *     See test testSwitchResolution().
             */
            if( cache_popup && !force_destroy_popup ) {
                popup_view.setVisibility(View.GONE);
            }
            else {
                destroyPopup();
            }
            main_activity.initImmersiveMode(); // to reset the timer when closing the popup
        }
    }

    public boolean popupIsOpen() {
        return popup_view_is_open;
    }

    public boolean selectingIcons() {
        return mSelectingIcons;
    }

    public boolean selectingLines() {
        return mSelectingLines;
    }

    public void destroyPopup() {
        if( MyDebug.LOG )
            Log.d(TAG, "destroyPopup");
        force_destroy_popup = false;
        if( popupIsOpen() ) {
            closePopup();
        }
        ViewGroup popup_container = main_activity.findViewById(R.id.popup_container);
        popup_container.removeAllViews();
        popup_view = null;
    }

    /**
     * Higlights the next LinearLayout view
     */
    private void highlightPopupLine(boolean highlight, boolean goUp) {
        if( MyDebug.LOG ) {
            Log.d(TAG, "highlightPopupLine");
            Log.d(TAG, "highlight: " + highlight);
            Log.d(TAG, "goUp: " + goUp);
        }
        if (!popupIsOpen()) { // Safety check
            clearSelectionState();
            return;
        }
        final ViewGroup popup_container = main_activity.findViewById(R.id.popup_container);
        Rect scrollBounds = new Rect();
        popup_container.getDrawingRect(scrollBounds);
        final LinearLayout inside = (LinearLayout) popup_container.getChildAt(0);
        if (inside == null)
            return; // Safety check
        int count = inside.getChildCount();
        boolean foundLine = false;
        while (!foundLine) {
            // Ensure we stay within our bounds:
            mPopupLine = (mPopupLine + count ) % count;
            View v = inside.getChildAt(mPopupLine);
            if( MyDebug.LOG )
                Log.d(TAG, "line: " + mPopupLine + " view: " + v);
            // to test example with HorizontalScrollView, see popup menu on Nokia 8 with Camera2 API, the flash icons row uses a HorizontalScrollView
            if( v instanceof HorizontalScrollView && ((HorizontalScrollView) v).getChildCount() > 0 )
                v = ((HorizontalScrollView) v).getChildAt(0);
            if (v.isShown() && v instanceof LinearLayout ) {
                if (highlight) {
                    v.setBackgroundColor(highlightColor);
                    //v.setAlpha(0.3f);
                    if (v.getBottom() > scrollBounds.bottom || v.getTop() < scrollBounds.top)
                        popup_container.scrollTo(0, v.getTop());
                    mHighlightedLine = (LinearLayout) v;
                } else {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    v.setAlpha(1f);
                }
                foundLine = true;
                if( MyDebug.LOG )
                    Log.d(TAG, "found at line: " + foundLine);
            } else {
                mPopupLine += goUp ? -1 : 1;
            }
        }
        if( MyDebug.LOG )
            Log.d(TAG,"Current line: " + mPopupLine);
    }

    /**
     * Highlights an icon on a horizontal line, such as flash mode,
     * focus mode, etc. Checks that the popup is open in case it is
     * wrongly called, so that it doesn't crash the app.
     */
    private void highlightPopupIcon(boolean highlight, boolean goLeft) {
        if( MyDebug.LOG ) {
            Log.d(TAG, "highlightPopupIcon");
            Log.d(TAG, "highlight: " + highlight);
            Log.d(TAG, "goLeft: " + goLeft);
        }
        if (!popupIsOpen()) { // Safety check
            clearSelectionState();
            return;
        }
        highlightPopupLine(false, false);
        int count = mHighlightedLine.getChildCount();
        boolean foundIcon = false;
        while (!foundIcon) {
            // Ensure we stay within our bounds:
            // (careful, modulo in Java will allow negative numbers, hence the line below:
            mPopupIcon= (mPopupIcon + count ) % count;
            View v = mHighlightedLine.getChildAt(mPopupIcon);
            if( MyDebug.LOG )
                Log.d(TAG, "row: " + mPopupIcon + " view: " + v);
            if (v instanceof ImageButton || v instanceof Button ) {
                if (highlight) {
                    v.setBackgroundColor(highlightColor);
                    //v.setAlpha(0.5f);
                    mHighlightedIcon = v;
                    mSelectingIcons = true;
                } else {
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
                if( MyDebug.LOG )
                    Log.d(TAG, "found icon at row: " + mPopupIcon);
                foundIcon = true;
            } else {
                mPopupIcon+= goLeft ? -1 : 1;
            }
        }
    }

    /**
     * Select the next line on the settings popup. Called by MainActivity
     * when receiving a remote control command.
     */
    private void nextPopupLine() {
        highlightPopupLine(false, false);
        mPopupLine++;
        highlightPopupLine(true, false);
    }

    private void previousPopupLine() {
        highlightPopupLine(false, true);
        mPopupLine--;
        highlightPopupLine(true, true);
    }

    private void nextPopupIcon() {
        highlightPopupIcon(false, false);
        mPopupIcon++;
        highlightPopupIcon(true, false);
    }

    private void previousPopupIcon() {
        highlightPopupIcon(false, true);
        mPopupIcon--;
        highlightPopupIcon(true, true);
    }

    /**
     * Simulates a press on the currently selected icon
     */
    private void clickSelectedIcon() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickSelectedIcon: " + mHighlightedIcon);
        if (mHighlightedIcon != null) {
            mHighlightedIcon.callOnClick();
        }
    }

    /**
     * Ensure all our selection tracking variables are cleared when we
     * exit menu selection (used in remote control mode)
     */
    private void clearSelectionState() {
        if( MyDebug.LOG )
            Log.d(TAG, "clearSelectionState");
        mPopupLine = 0;
        mPopupIcon = 0;
        mSelectingIcons = false;
        mSelectingLines = false;
        mHighlightedIcon= null;
        mHighlightedLine = null;
    }

    /**
     * Opens or closes the settings popup on the camera preview. The popup that
     * differs depending whether we're in photo or video mode
     */
    public void togglePopupSettings() {
        final ViewGroup popup_container = main_activity.findViewById(R.id.popup_container);
        if( popupIsOpen() ) {
            closePopup();
            return;
        }
        // the sheet and the popup are never open at the same time (Kickoff 3.2) - the other
        // direction is in SettingsSheet.open()
        SettingsSheet settingsSheet = main_activity.getSettingsSheet();
        if( settingsSheet != null && settingsSheet.isOpen() )
            settingsSheet.close();
        if( main_activity.getPreview().getCameraController() == null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "camera not opened!");
            return;
        }
        else if( main_activity.getPreview().isPreviewStarting() ) {
            if( MyDebug.LOG )
                Log.d(TAG, "preview is starting!");
            // risk if preview is currently opening on background thread - don't want to open popup menu that would allow being
            // able to change settings that would then require restarting the preview
            // still allow opening popup if preview is not started (but not starting in background) - in practice this means the
            // preview failed to start, so can be useful to allow opening the popup to change modes (in case failing to start is
            // due to a specific photo mode or other setting)
            return;
        }

        if( MyDebug.LOG )
            Log.d(TAG, "open popup");

        main_activity.enablePopupOnBackPressedCallback(true); // so that back button will close the popup instead of exiting the application

        closeExposureUI();
        main_activity.getPreview().cancelTimer(); // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings
        main_activity.stopAudioListeners();

        final long time_s = System.currentTimeMillis();

        {
            // prevent popup being transparent
            popup_container.setBackgroundColor(Color.argb(230, 0, 0, 0));
        }

        if( popup_view == null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "create new popup_view");
            test_ui_buttons.clear();
            popup_view = new PopupView(main_activity);
            popup_container.addView(popup_view);
        }
        else {
            if( MyDebug.LOG )
                Log.d(TAG, "use cached popup_view");
            popup_view.setVisibility(View.VISIBLE);
        }
        popup_view_is_open = true;

        if (main_activity.getBluetoothRemoteControl().remoteEnabled()) {
            initRemoteControlForPopup();
        }

        // need to call layoutUI to make sure the new popup is oriented correctly
        // but need to do after the layout has been done, so we have a valid width/height to use
        // n.b., even though we only need the portion of layoutUI for the popup container, there
        // doesn't seem to be any performance benefit in only calling that part
        popup_container.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if( MyDebug.LOG )
                            Log.d(TAG, "onGlobalLayout()");
                        if( MyDebug.LOG )
                            Log.d(TAG, "time after global layout: " + (System.currentTimeMillis() - time_s));
                        layoutUI(true);
                        if( MyDebug.LOG )
                            Log.d(TAG, "time after layoutUI: " + (System.currentTimeMillis() - time_s));
                        // stop listening - only want to call this once!
                        popup_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        // the popup hangs below the icon row at the left, so it grows from its top left corner
                        ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                        animation.setDuration(200);
                        //popup_container.setAnimation(animation);
                        AlphaAnimation fade_animation = new AlphaAnimation(0.0f, 1.0f);
                        fade_animation.setDuration(200);
                        AnimationSet animation_set = new AnimationSet(false);
                        animation_set.addAnimation(animation);
                        animation_set.addAnimation(fade_animation);
                        popup_container.setAnimation(animation_set);
                    }
                }
        );

        if( MyDebug.LOG )
            Log.d(TAG, "time to create popup: " + (System.currentTimeMillis() - time_s));
    }

    private void initRemoteControlForPopup() {
        if( MyDebug.LOG )
            Log.d(TAG, "initRemoteControlForPopup");
        if( popupIsOpen() ) { // just in case
            // For remote control, we want to highlight lines and icons on the popup view
            // so that we can control those just with the up/down buttons and "OK"
            clearSelectionState();
            remote_control_mode = true;
            mSelectingLines = true;
            highlightPopupLine(true, false);
        }
    }

    private void clearRemoteControlForPopup() {
        if( MyDebug.LOG )
            Log.d(TAG, "clearRemoteControlForPopup");
        if( popupIsOpen() && remote_control_mode ) {
            remote_control_mode = false;

            // reset highlighting
            final ViewGroup popup_container = main_activity.findViewById(R.id.popup_container);
            Rect scrollBounds = new Rect();
            popup_container.getDrawingRect(scrollBounds);
            final LinearLayout inside = (LinearLayout) popup_container.getChildAt(0);
            if( inside == null )
                return; // Safety check
            View v = inside.getChildAt(mPopupLine);
            if( v.isShown() && v instanceof LinearLayout ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "reset " + mPopupLine + "th view: " + v);
                v.setBackgroundColor(Color.TRANSPARENT);
                v.setAlpha(1f);
            }
            if( mHighlightedLine != null ) {
                v = mHighlightedLine.getChildAt(mPopupIcon);
                if( v instanceof ImageButton || v instanceof Button ) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
            }
			/*for(int i=0;i<inside.getChildCount();i++) {
				View v = inside.getChildAt(i);
				if( v.isShown() && v instanceof LinearLayout ) {
					if( MyDebug.LOG )
						Log.d(TAG, "reset " + i + "th view: " + v);
					v.setBackgroundColor(Color.TRANSPARENT);
					v.setAlpha(1f);
				}
			}*/
        }
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
                            this.destroyPopup(); // need to recreate popup in order to update the auto-level checkbox
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
                else if( popupIsOpen() && remote_control_mode ) {
                    commandMenuPopup();
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
                    if( popupIsOpen() ) {
                        initRemoteControlForPopup();
                        return true;
                    }
                    else if( isExposureUIOpen() ) {
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
                    if( popupIsOpen() ) {
                        initRemoteControlForPopup();
                        return true;
                    }
                    else if( isExposureUIOpen() ) {
                        initRemoteControlForExposureUI();
                        return true;
                    }
                }
                else if( processRemoteDownButton() )
                    return true;
                break;
            case KeyEvent.KEYCODE_FUNCTION:
            case KeyEvent.KEYCODE_NUMPAD_MULTIPLY:
                togglePopupSettings();
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

    /** If the popup menu is open, selects a current line or option. Else does nothing.
     */
    public void commandMenuPopup() {
        if( MyDebug.LOG )
            Log.d(TAG, "commandMenuPopup");
        if( popupIsOpen() ) {
            if( selectingIcons() ) {
                clickSelectedIcon();
            }
            else {
                highlightPopupIcon(true, false);
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

    View getTopIcon() {
        return this.top_icon;
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
    public View getUIButton(String key) {
        if( MyDebug.LOG ) {
            Log.d(TAG, "getPopupButton(" + key + "): " + test_ui_buttons.get(key));
            Log.d(TAG, "this: " + this);
            Log.d(TAG, "popup_buttons: " + test_ui_buttons);
        }
        return test_ui_buttons.get(key);
    }

    Map<String, View> getTestUIButtonsMap() {
        return test_ui_buttons;
    }

    public PopupView getPopupView() {
        return popup_view;
    }

    public boolean testGetRemoteControlMode() {
        return remote_control_mode;
    }

    public int testGetPopupLine() {
        return mPopupLine;
    }

    public int testGetPopupIcon() {
        return mPopupIcon;
    }

    public int testGetExposureLine() {
        return mExposureLine;
    }
}
