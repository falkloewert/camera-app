package net.sourceforge.opencamera.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraExtensionCharacteristics;
import android.media.CamcorderProfile;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyApplicationInterface;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.R;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.preview.Preview;

import java.util.ArrayList;
import java.util.List;

/**
 * The settings sheet: a panel that slides up from the physical bottom edge of the device and
 * carries the most used settings of the current mode (photo or video) - see
 * docs/kickoff/redesign-einstellungen-blatt-benchmark.md (sections 2 and 3).
 * <p>
 * Rotation model: the window is locked to portrait and is never re-laid-out, so the panel is
 * laid out in <em>its own</em> frame and turned as a whole with {@link View#setRotation(int)}.
 * With ui_rotation applied that local frame is exactly the frame the user looks at (local +x =
 * the user's right, local +y = the user's down); the content therefore only needs different
 * numbers per orientation, never a mirrored layout. {@link View#setTranslationX(int)} and
 * {@link View#setTranslationY(int)} are applied in the parent's (i.e. the window's) axes, which
 * is what {@link #place()} uses to put the centre of the turned panel on the bottom edge.
 * <p>
 * The sheet owns no state of its own: every row reads the current value when it is built, and
 * every option writes it through the same calls the old popup uses and then asks for a rebuild.
 */
public class SettingsSheet {
    private static final String TAG = "SettingsSheet";

    /* ---- measured geometry, Kickoff 2.2 (portrait) / 2.3 (landscape), in dp ---------------- */
    /** Depth of the panel in portrait (its height). */
    private static final float PANEL_DEPTH_PORTRAIT = 538.0f;
    /** Depth of the panel in landscape (its width), = 36 + 394.7 + 40. */
    private static final float PANEL_DEPTH_LANDSCAPE = 470.7f;
    /** The content block: 394.7 dp in both directions, inset 36/40 (depth) and 16/16 (along). */
    private static final float LANDSCAPE_STRIP_IN = 36.0f;
    private static final float LANDSCAPE_STRIP_WIDTH = 394.7f;
    private static final float CORNER_RADIUS = 24.0f;

    private static final float HANDLE_LONG = 36.0f, HANDLE_THICK = 4.0f, HANDLE_INSET = 16.0f;
    private static final float TITLE_INSET = 16.0f, TITLE_CENTRE = 65.0f;
    private static final float MORE_WIDTH = 52.0f, MORE_HEIGHT = 40.0f, MORE_INSET = 16.0f;
    private static final float MORE_CENTRE = 64.0f;
    /** Every vertical position in landscape sits 20 dp higher than in portrait (Kickoff 2.3). */
    private static final float LANDSCAPE_SHIFT = 20.0f;

    private static final float TABS_TOP = 96.0f, TABS_HEIGHT = 44.0f, TAB_GAP = 2.0f;
    private static final float ROW_RASTER = 56.0f, ROW_FIRST_CENTRE = 182.0f;
    private static final float LABEL_UP = 10.0f, VALUE_DOWN = 13.0f;
    private static final float OPTION_SIZE = 40.0f, OPTION_RASTER = 48.0f, OPTION_PAD = 4.0f;
    private static final float SYMBOL_SIZE = 24.0f;
    /** Height of the fixed boxes the label/value lines are centred in. */
    private static final float LABEL_BOX = 20.0f, VALUE_BOX = 26.0f, TITLE_BOX = 32.0f;

    // the value line is smaller than the reference's 20sp since Falk's device runs a larger system
    // font (1.15x, checked 2026-09-21): at 20sp the text outgrew its 26dp box and sat visibly off
    // the row's line
    private static final int TITLE_SP = 24, TAB_SP = 16, LABEL_SP = 14, VALUE_SP = 18;
    private static final int OPEN_DURATION = 250, CLOSE_DURATION = 200;

    /** Option labels of the timer row and the values they write (as the old popup does). */
    private static final String [] TIMER_VALUES = {"0", "3", "5", "10"};
    private static final float RATIO_TOLERANCE = 0.02f;
    /** The border between the two classes of the resolution row: everything this narrow is the
     *  space-saving class, everything above it the standard class (whose own limit is
     *  CameraController.STANDARD_PICTURE_WIDTH). A width, not a megapixel number - with the old
     *  13 MP border a 12 MP camera fell out of its own class (Runde 3). */
    private static final int SAVING_PICTURE_WIDTH = 1920;

    private final MainActivity main_activity;
    private final FrameLayout container;
    private final FrameLayout panel;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean open;
    private boolean is_video;
    private int selected_tab;
    private int ui_rotation;

    /** The rotation that was used for the current layout, to skip rebuilding when unchanged. */
    private boolean placed_landscape;
    private boolean placed_mirrored;    // see mirrored()
    private int depth_px;               // the panel's extent away from the physical bottom edge
    private int resting_translation_y;  // window translation of the closed/open resting position

    public SettingsSheet(MainActivity main_activity) {
        this.main_activity = main_activity;
        this.container = main_activity.findViewById(R.id.settings_sheet);
        this.panel = main_activity.findViewById(R.id.settings_sheet_panel);
        this.ui_rotation = main_activity.getMainUI().getUIRotation();
        // a tap outside the panel closes the sheet - and keeps the viewfinder from seeing the
        // touch at all (Kickoff 3.2)
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( MyDebug.LOG )
                    Log.d(TAG, "tap outside the panel");
                close();
            }
        });
        panel.setOnTouchListener(new PanelTouchListener());
    }

    /* ---- open state ----------------------------------------------------------------------- */

    public boolean isOpen() {
        return open;
    }

    public void open() {
        if( open )
            return;
        Preview preview = main_activity.getPreview();
        if( preview == null )
            return;
        open = true;
        // the sheet always opens on the left tab (Kickoff 2.2)
        selected_tab = 0;
        is_video = preview.isVideo();
        place();
        buildContent();
        container.setVisibility(View.VISIBLE);
        panel.clearAnimation();
        panel.setTranslationY(resting_translation_y + depth_px);
        panel.animate().translationY(resting_translation_y).setDuration(OPEN_DURATION)
                .setInterpolator(MainUI.glyph_rotation_interpolator).start();
        main_activity.enableSettingsSheetOnBackPressedCallback(true);
    }

    public void close() {
        if( !open )
            return;
        open = false;
        main_activity.enableSettingsSheetOnBackPressedCallback(false);
        panel.animate().translationY(resting_translation_y + depth_px).setDuration(CLOSE_DURATION)
                .setInterpolator(MainUI.glyph_rotation_interpolator)
                .setListener(new AnimatorListenerAdapter() {
                    private boolean cancelled;

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        cancelled = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        panel.animate().setListener(null);
                        if( !cancelled && !open )
                            container.setVisibility(View.GONE);
                    }
                }).start();
    }

    /** The device was turned: the panel hangs off the physical bottom edge, so it has to be
     *  re-placed (Kickoff 3.2) - immediately, without an animation. */
    public void onUIRotationChanged(int ui_rotation) {
        if( ui_rotation == this.ui_rotation ) {
            // MainUI.layoutUI() calls this on every layout pass, and a camera restart causes one.
            // Rebuilding there dropped the camera dependent rows for the moment the camera was
            // closed, the rows below them moved up, and everything came back a moment later - the
            // double jump Falk saw on the ratio button, plus two full rebuilds. Nothing was turned,
            // so just re-read the values, which waits for the camera (found on the device
            // 2026-09-21).
            refresh();
            return;
        }
        this.ui_rotation = ui_rotation;
        if( open ) {
            place();
            // a sheet that was already open on the other edge: put it straight into its new
            // resting position instead of animating from the old one
            panel.animate().cancel();
            buildContent();
        }
    }

    /** Re-reads every row. Cheap no-op when the sheet is closed. */
    public void refresh() {
        if( !open )
            return;
        if( Looper.myLooper() != Looper.getMainLooper() ) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
            return;
        }
        if( restart_pending ) {
            // One of our own options started a camera restart: re-read the values once it is
            // through, not in between (see onCameraSetUp). The timeout is the net for a restart
            // that never reports back.
            return;
        }
        if( !cameraReady() ) {
            // A change just reopened the camera: right now it reports nothing, and a build would
            // drop every camera dependent row the user is looking at for a moment. cameraSetup()
            // asks again when it has finished; the retry is the net for the case where that call
            // came before the camera was ready (found on the device 2026-09-21).
            if( refresh_retries < MAX_REFRESH_RETRIES ) {
                refresh_retries++;
                handler.removeCallbacks(refresh_retry);
                handler.postDelayed(refresh_retry, REFRESH_RETRY_DELAY);
            }
            return;
        }
        refresh_retries = 0;
        buildContent();
    }

    /** Whether the camera can answer questions about itself - the rows ask it for its picture
     *  sizes, video qualities, flash and focus values while they are being built. */
    private boolean cameraReady() {
        Preview preview = main_activity.getPreview();
        if( preview == null || preview.getCameraController() == null || preview.isOpeningCamera() )
            return false;
        // the unfiltered list is only null while the camera has not been set up at all
        return preview.getSupportedPictureSizes(false) != null;
    }

    /** Whether the camera that is open right now looks at the user. Rows that only make sense
     *  for the rear camera (resolution, macro, lens choice) are dropped for the front one
     *  (Kickoff Video-Blatt Runde 1, items 4-6). */
    private boolean frontCamera() {
        Preview preview = main_activity.getPreview();
        CameraController controller = preview == null ? null : preview.getCameraController();
        return controller != null && controller.getFacing() == CameraController.Facing.FACING_FRONT;
    }

    private int refresh_retries;
    private static final int MAX_REFRESH_RETRIES = 20;
    private static final long REFRESH_RETRY_DELAY = 100;

    private final Runnable refresh_retry = new Runnable() {
        @Override
        public void run() {
            refresh();
        }
    };

    /** Set while a camera restart started by one of our own options is on its way. Rebuilding the
     *  sheet in that window shows a half torn down camera: the same double jump as in
     *  {@link #onUIRotationChanged} (found on the device 2026-09-21). */
    private boolean restart_pending;
    private static final long RESTART_PENDING_TIMEOUT = 3000;

    private final Runnable restart_pending_timeout = new Runnable() {
        @Override
        public void run() {
            // the restart never reported back - re-read the values anyway
            restart_pending = false;
            refresh();
        }
    };

    /** The camera has been set up again after a restart: the rows can be re-read now. Called from
     *  MainActivity.cameraSetup(), the same place that used to call refresh() directly. */
    public void onCameraSetUp() {
        restart_pending = false;
        handler.removeCallbacks(restart_pending_timeout);
        refresh();
    }

    /* ---- geometry ------------------------------------------------------------------------- */

    /** The numbers of the current orientation. Everything is in the panel's own frame, which
     *  after {@link View#setRotation(int)} is the frame the user looks at: x runs from the
     *  viewfinder side to the physical bottom edge, y is the user's down. */
    private static class Metrics {
        int panel_w, panel_h;           // the panel's local size in px
        int label_x;                    // left edge of the title and of the row labels
        int cells_right;                // outer edge of the rightmost option cell
        int tabs_left, tabs_right, tabs_top;
        int title_centre, more_centre, rows_first_centre;
        int row_step;                   // from one row's centre to the next, away from the viewfinder
        int strip_x, strip_width;
        int handle_left, handle_top, handle_w, handle_h;
    }

    /** Whether the panel lies on its side, i.e. its depth runs along local x instead of local y. */
    private boolean landscape() {
        return ui_rotation == 90 || ui_rotation == 270;
    }

    /** Whether the panel's local frame is turned away from the physical bottom edge.
     *  {@link View#setRotation(float)} turns clockwise, so the panel's local origin ends up on the
     *  window's top edge at 90 (a rectangle turned clockwise puts its left edge on top) and on the
     *  window's bottom edge at 270; in portrait the same holds for local y at 0 and 180. The
     *  numbers of the kickoff are all measured from the edge towards the viewfinder, which is the
     *  local origin at 0/90 and the far end of the panel at 180/270 - there they have to be
     *  mirrored, otherwise the rounded corners, the drag handle and the insets sit on the edge the
     *  panel hangs off (Kickoff 2.2/2.3). */
    private boolean mirrored() {
        return ui_rotation == 180 || ui_rotation == 270;
    }

    /** The space the sheet hangs in, in the window's axes. The catcher container is the right
     *  answer, but it starts out GONE and only gets its bounds in the first layout pass - on the
     *  very first open it still measures 0, and the activity root is then the same rectangle.
     *  The display metrics are no substitute: they also cover the window's top inset, and the
     *  sheet would hang exactly that much too low, its bottom row off the screen (found on the
     *  device 2026-09-21: first open after a start, panel 68 dp too deep). */
    private int windowSize(boolean horizontal) {
        int size = horizontal ? container.getWidth() : container.getHeight();
        if( size <= 0 ) {
            View root = main_activity.findViewById(R.id.activity_main_root);
            if( root != null )
                size = horizontal ? root.getWidth() : root.getHeight();
        }
        if( size <= 0 ) {
            DisplayMetrics display_metrics = main_activity.getResources().getDisplayMetrics();
            size = horizontal ? display_metrics.widthPixels : display_metrics.heightPixels;
        }
        return size;
    }

    private Metrics metrics() {
        Metrics m = new Metrics();
        int screen_w = windowSize(true);
        boolean mirrored = mirrored();
        if( landscape() ) {
            m.panel_w = dp(PANEL_DEPTH_LANDSCAPE);
            m.panel_h = screen_w;
            m.strip_width = dp(LANDSCAPE_STRIP_WIDTH);
            // the content strip keeps its 36 dp inset towards the viewfinder, which is the far end
            // of the panel at 270 (see mirrored()); the rows inside it are not mirrored - their
            // axis is the same local x, and a mirrored row would put its buttons left of its label
            m.strip_x = mirrored ? m.panel_w - dp(LANDSCAPE_STRIP_IN) - m.strip_width : dp(LANDSCAPE_STRIP_IN);
            // the strip already is the content block, so the title, the row labels and the left
            // tab edge start at its edge - that is the 36 dp inset from the viewfinder-side edge
            // Kickoff 2.3 asks for (a further TITLE_INSET made it 52 dp, Runde 3)
            m.label_x = m.strip_x;
            m.cells_right = m.strip_x + m.strip_width;
            // tabs, "…" and the button cells all span the content block, so the tab strip is the
            // same 394,7 dp wide as in portrait (Kickoff 2.3); the last option button ends 44 dp
            // from the outer edge by its own 4 dp cell padding, not by a narrower strip
            m.tabs_left = m.label_x;
            m.tabs_right = m.cells_right;
            m.title_centre = dp(TITLE_CENTRE - LANDSCAPE_SHIFT);
            m.more_centre = dp(MORE_CENTRE - LANDSCAPE_SHIFT);
            m.tabs_top = dp(TABS_TOP - LANDSCAPE_SHIFT);
            m.rows_first_centre = dp(ROW_FIRST_CENTRE - LANDSCAPE_SHIFT);
            m.row_step = dp(ROW_RASTER);    // the rows stack along local y, which is not mirrored
            m.handle_w = dp(HANDLE_THICK);
            m.handle_h = dp(HANDLE_LONG);
            m.handle_left = mirrored ? m.panel_w - dp(HANDLE_INSET) - m.handle_w : dp(HANDLE_INSET);
            m.handle_top = m.panel_h / 2 - dp(HANDLE_LONG) / 2;
        }
        else {
            m.panel_w = screen_w;
            m.panel_h = dp(PANEL_DEPTH_PORTRAIT);
            m.strip_x = 0;
            m.strip_width = screen_w;
            m.label_x = dp(TITLE_INSET);
            m.cells_right = screen_w - dp(TITLE_INSET);
            m.tabs_left = dp(TITLE_INSET);
            m.tabs_right = screen_w - dp(TITLE_INSET);
            m.handle_w = dp(HANDLE_LONG);
            m.handle_h = dp(HANDLE_THICK);
            m.handle_left = screen_w / 2 - dp(HANDLE_LONG) / 2;
            // in portrait the depth runs along local y, so every one of these distances is
            // measured from the other edge at 180 (see mirrored()) - the panel is then the mirror
            // image of the portrait one, handle and rounded corners towards the viewfinder
            if( mirrored ) {
                m.title_centre = m.panel_h - dp(TITLE_CENTRE);
                m.more_centre = m.panel_h - dp(MORE_CENTRE);
                m.tabs_top = m.panel_h - dp(TABS_TOP) - dp(TABS_HEIGHT);
                m.rows_first_centre = m.panel_h - dp(ROW_FIRST_CENTRE);
                m.row_step = -dp(ROW_RASTER);
                m.handle_top = m.panel_h - dp(HANDLE_INSET) - m.handle_h;
            }
            else {
                m.title_centre = dp(TITLE_CENTRE);
                m.more_centre = dp(MORE_CENTRE);
                m.tabs_top = dp(TABS_TOP);
                m.rows_first_centre = dp(ROW_FIRST_CENTRE);
                m.row_step = dp(ROW_RASTER);
                m.handle_top = dp(HANDLE_INSET);
            }
        }
        return m;
    }

    /** Sizes and turns the panel, and puts its centre onto the physical bottom edge. The
     *  translations are in the window's axes, hence the plain centre arithmetic. */
    private void place() {
        Metrics m = metrics();
        placed_landscape = landscape();
        placed_mirrored = mirrored();
        ViewGroup.LayoutParams params = panel.getLayoutParams();
        params.width = m.panel_w;
        params.height = m.panel_h;
        panel.setLayoutParams(params);
        panel.setRotation(ui_rotation);
        int screen_w = windowSize(true);
        int screen_h = windowSize(false);
        // how much room the turned panel takes up in the window's axes: a quarter turn swaps the
        // two, so the thickness ends up on the window's vertical
        int window_w = placed_landscape ? m.panel_h : m.panel_w;
        int window_h = placed_landscape ? m.panel_w : m.panel_h;
        depth_px = window_h;
        // the translations are applied in the window's axes, after the rotation: put the centre
        // of the panel on the physical bottom edge, which is the window's bottom in every device
        // orientation (the gesture bar and the dock always sit there, see ui-spezifikation 2)
        resting_translation_y = (screen_h - depth_px / 2) - m.panel_h / 2;
        panel.setTranslationX(screen_w / 2 - m.panel_w / 2);
        panel.setTranslationY(resting_translation_y);
        panel.setBackground(panelBackground());
        if( MyDebug.LOG )
            Log.d(TAG, "place: panel " + window_w + "x" + window_h + ", translation " + panel.getTranslationX() + " / " + resting_translation_y);
    }

    /** Rounded corners on the inboard edge, i.e. the edge towards the viewfinder (Kickoff
     *  2.2/2.3): local y = 0 in portrait (the top) and local x = 0 in landscape (the left), and
     *  the opposite edge of the panel when its frame is mirrored (see {@link #mirrored()}). */
    private GradientDrawable panelBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_panel));
        float r = dp(CORNER_RADIUS);
        // the radii run top-left, top-right, bottom-right, bottom-left, x and y of each
        if( placed_landscape ) {
            if( placed_mirrored )
                background.setCornerRadii(new float [] {0, 0, r, r, r, r, 0, 0});
            else
                background.setCornerRadii(new float [] {r, r, 0, 0, 0, 0, r, r});
        }
        else {
            if( placed_mirrored )
                background.setCornerRadii(new float [] {0, 0, 0, 0, r, r, r, r});
            else
                background.setCornerRadii(new float [] {r, r, r, r, 0, 0, 0, 0});
        }
        return background;
    }

    private int dp(float value) {
        return (int)(value * main_activity.getResources().getDisplayMetrics().density + 0.5f);
    }

    private float sp(float value) {
        return value * main_activity.getResources().getDisplayMetrics().scaledDensity;
    }

    private void addChild(View view, int left, int top, int width, int height) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = left;
        params.topMargin = top;
        panel.addView(view, params);
    }

    /* ---- content -------------------------------------------------------------------------- */

    private void buildContent() {
        panel.removeAllViews();
        Metrics m = metrics();

        // drag handle
        View handle = new View(main_activity);
        GradientDrawable handle_background = new GradientDrawable();
        handle_background.setColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_handle));
        handle_background.setCornerRadius(dp(HANDLE_THICK) / 2.0f);
        handle.setBackground(handle_background);
        addChild(handle, m.handle_left, m.handle_top, m.handle_w, m.handle_h);

        // title
        TextView title = new TextView(main_activity);
        title.setText(is_video ? R.string.settings_sheet_video : R.string.settings_sheet_photo);
        title.setTextSize(TITLE_SP);
        title.setTextColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_value));
        title.setGravity(Gravity.CENTER_VERTICAL);
        addChild(title, m.label_x, m.title_centre - dp(TITLE_BOX) / 2, ViewGroup.LayoutParams.WRAP_CONTENT, dp(TITLE_BOX));

        // "…" opens the full settings list (Kickoff 2.2)
        FrameLayout more = new FrameLayout(main_activity);
        more.setBackgroundResource(R.drawable.settings_sheet_affordance);
        more.setContentDescription(getString(R.string.settings_sheet_more_options));
        ImageView more_glyph = new ImageView(main_activity);
        more_glyph.setImageResource(R.drawable.ms_more_horiz);
        more_glyph.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(main_activity, R.color.settings_sheet_value)));
        more.addView(more_glyph, new FrameLayout.LayoutParams(dp(SYMBOL_SIZE), dp(SYMBOL_SIZE), Gravity.CENTER));
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( MyDebug.LOG )
                    Log.d(TAG, "clicked more options");
                close();
                main_activity.openSettings();
            }
        });
        addChild(more, m.cells_right - dp(MORE_WIDTH), m.more_centre - dp(MORE_HEIGHT) / 2, dp(MORE_WIDTH), dp(MORE_HEIGHT));

        // tabs
        LinearLayout tabs = new LinearLayout(main_activity);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        int tabs_width = m.tabs_right - m.tabs_left;
        int tab_width = (tabs_width - dp(TAB_GAP)) / 2;
        String [] tab_labels = is_video ?
                new String [] {getString(R.string.settings_sheet_tab_general), getString(R.string.settings_sheet_tab_more)} :
                new String [] {getString(R.string.settings_sheet_tab_general), getString(R.string.settings_sheet_tab_pro)};
        for(int i=0;i<tab_labels.length;i++) {
            final int index = i;
            TextView tab = new TextView(main_activity);
            tab.setText(tab_labels[i]);
            tab.setTextSize(TAB_SP);
            tab.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            tab.setGravity(Gravity.CENTER);
            tab.setBackgroundResource(selected_tab == i ? R.drawable.settings_sheet_tab_active : R.drawable.settings_sheet_tab_inactive);
            tab.setTextColor(ContextCompat.getColor(main_activity, selected_tab == i ?
                    R.color.settings_sheet_tab_active_text : R.color.settings_sheet_tab_inactive_text));
            LinearLayout.LayoutParams tab_params = new LinearLayout.LayoutParams(tab_width, dp(TABS_HEIGHT));
            if( i > 0 )
                tab_params.leftMargin = dp(TAB_GAP);
            tab.setLayoutParams(tab_params);
            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( selected_tab == index )
                        return;
                    // no animation, the reference switches instantly (Kickoff 2.2)
                    selected_tab = index;
                    buildContent();
                }
            });
            tabs.addView(tab);
        }
        addChild(tabs, m.tabs_left, m.tabs_top, tabs_width, dp(TABS_HEIGHT));

        // rows - the camera's picture sizes are read once here and handed down, instead of every
        // row asking for them again (Kickoff Runde 2, 1.5)
        List<Row> rows = buildRows(pictureSizes());
        for(int i=0;i<rows.size();i++)
            createRow(rows.get(i), m.rows_first_centre + i * m.row_step, m);
    }

    /** One row of the sheet: a label, the current value, and the options right-aligned in it. */
    private static class Row {
        final CharSequence label;
        final CharSequence value;
        final List<Option> options;

        Row(CharSequence label, CharSequence value, List<Option> options) {
            this.label = label;
            this.value = value;
            this.options = options;
        }
    }

    /** One option button. Either a Material Symbol or a small shape drawn by {@link GlyphView}. */
    private static class Option {
        final CharSequence label;   // the content description (and the value line when selected)
        final int symbol;           // drawable resource, or 0 when the glyph is drawn
        final Glyph glyph;
        final String text;          // text carried by the drawn glyphs
        final float ratio;          // only for the aspect glyph
        final boolean selected;
        final Runnable action;

        Option(CharSequence label, int symbol, Glyph glyph, String text, float ratio, boolean selected, Runnable action) {
            this.label = label;
            this.symbol = symbol;
            this.glyph = glyph;
            this.text = text;
            this.ratio = ratio;
            this.selected = selected;
            this.action = action;
        }
    }

    private void createRow(Row row, int centre_y, Metrics m) {
        FrameLayout strip = new FrameLayout(main_activity);

        TextView label = new TextView(main_activity);
        label.setText(row.label);
        label.setTextSize(LABEL_SP);
        label.setTextColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_label));
        label.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams label_params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(LABEL_BOX));
        // the same inset the title has, measured inside the strip: 16 dp in portrait (from the
        // screen edge), 0 in landscape where the strip itself already sits 36 dp in
        label_params.leftMargin = m.label_x - m.strip_x;
        // the label's centre sits 10 dp above, the value's 13 dp below the row's centre
        label_params.topMargin = dp(ROW_RASTER) / 2 - dp(LABEL_UP) - dp(LABEL_BOX) / 2;
        strip.addView(label, label_params);

        TextView value = new TextView(main_activity);
        value.setText(row.value);
        value.setTextSize(VALUE_SP);
        value.setTextColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_value));
        value.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams value_params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(VALUE_BOX));
        value_params.leftMargin = m.label_x - m.strip_x;
        value_params.topMargin = dp(ROW_RASTER) / 2 + dp(VALUE_DOWN) - dp(VALUE_BOX) / 2;
        strip.addView(value, value_params);

        // the buttons sit in a 48 dp raster that ends at the right edge of the content
        int cells_right = m.cells_right - m.strip_x;
        int count = row.options.size();
        List<View> buttons = new ArrayList<>();
        for(int i=0;i<count;i++) {
            Option option = row.options.get(i);
            int cell_left = cells_right - (count - i) * dp(OPTION_RASTER);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(OPTION_SIZE), dp(OPTION_SIZE));
            params.leftMargin = cell_left + dp(OPTION_PAD);
            params.topMargin = dp(ROW_RASTER) / 2 - dp(OPTION_SIZE) / 2;
            View button = createOptionButton(option);
            buttons.add(button);
            strip.addView(button, params);
        }
        // a tap marks its option right away instead of waiting for the camera to be reconfigured
        // (Kickoff Runde 2, 1.5); the next refresh() corrects it if the camera disagrees
        for(int i=0;i<count;i++) {
            final int index = i;
            final Option option = row.options.get(i);
            buttons.get(index).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( MyDebug.LOG )
                        Log.d(TAG, "option: " + option.label);
                    for(int j=0;j<buttons.size();j++)
                        markOptionButton(buttons.get(j), j == index);
                    option.action.run();
                }
            });
        }

        addChild(strip, m.strip_x, centre_y - dp(ROW_RASTER) / 2, m.strip_width, dp(ROW_RASTER));
    }

    private View createOptionButton(final Option option) {
        FrameLayout button = new FrameLayout(main_activity);
        button.setContentDescription(option.label);
        View glyph;
        if( option.glyph == null ) {
            ImageView symbol = new ImageView(main_activity);
            symbol.setImageResource(option.symbol);
            glyph = symbol;
        }
        else {
            glyph = new GlyphView(option.glyph, option.text, option.ratio, option.selected);
        }
        button.addView(glyph, new FrameLayout.LayoutParams(dp(SYMBOL_SIZE), dp(SYMBOL_SIZE), Gravity.CENTER));
        markOptionButton(button, option.selected);
        return button;
    }

    /** Draws one option button as selected or not - also used when a tap marks its option
     *  immediately (Kickoff Runde 2, 1.5). Every button is the same square: selected the white
     *  circle, unselected the dark rounded square (reference, measured 2026-09-21). */
    private void markOptionButton(View button, boolean selected) {
        button.setBackgroundResource(selected ? R.drawable.settings_sheet_button_selected : R.drawable.settings_sheet_button);
        int glyph_color = ContextCompat.getColor(main_activity, selected ?
                R.color.settings_sheet_button_glyph_selected : R.color.settings_sheet_button_glyph);
        int fill_color = ContextCompat.getColor(main_activity, selected ?
                R.color.settings_sheet_button_selected : R.color.settings_sheet_button);
        View glyph = ((ViewGroup)button).getChildAt(0);
        if( glyph instanceof ImageView )
            ((ImageView)glyph).setImageTintList(ColorStateList.valueOf(glyph_color));
        else if( glyph instanceof GlyphView )
            ((GlyphView)glyph).setColor(glyph_color, fill_color);
    }

    /* ---- rows of the photo sheet (Kickoff 3.3/3.4) ---------------------------------------- */

    private List<Row> buildRows(List<CameraController.Size> sizes) {
        if( is_video )
            return selected_tab == 0 ? videoGeneralRows() : videoMoreRows();
        return selected_tab == 0 ? photoGeneralRows(sizes) : photoProRows(sizes);
    }

    /** The picture sizes the rows work on, read once per build (Kickoff Runde 2, 1.5). A camera
     *  that is being reopened cannot answer for a moment; the list it gave last is then what the
     *  sheet has to keep showing, otherwise the resolution and aspect rows drop out under the
     *  user's finger (found on the device 2026-09-21, same as {@link #manualLenses()}). Kept per
     *  camera, so switching to the other camera never puts the old camera's sizes into the new
     *  one's rows. */
    private List<CameraController.Size> pictureSizes() {
        Preview preview = main_activity.getPreview();
        List<CameraController.Size> sizes = preview == null ? null : preview.getSupportedPictureSizes(true);
        int camera_id = main_activity.getActualCameraId();
        if( sizes != null && !sizes.isEmpty() ) {
            last_sizes = sizes;
            last_sizes_camera_id = camera_id;
        }
        else if( last_sizes != null && camera_id == last_sizes_camera_id ) {
            sizes = last_sizes;
        }
        return sizes;
    }

    private List<CameraController.Size> last_sizes;
    private int last_sizes_camera_id = -1;

    /** The physical lenses the lens row offers, or null when the current camera has none. During
     *  a lens switch the camera reports none for a moment, which used to make the whole row
     *  disappear and come back (device 2026-09-21): the lenses of the camera being switched are
     *  the ones the row has to keep showing, and they are the same before and after the switch. */
    private List<MainActivity.Lens> manualLenses() {
        List<MainActivity.Lens> lenses = main_activity.getManualLenses();
        int camera_id = main_activity.getActualCameraId();
        if( lenses != null && lenses.size() >= 2 ) {
            last_lenses = lenses;
            last_lenses_camera_id = camera_id;
        }
        else if( last_lenses != null && camera_id == last_lenses_camera_id ) {
            lenses = last_lenses;
        }
        return lenses != null && lenses.size() >= 2 ? lenses : null;
    }

    private List<MainActivity.Lens> last_lenses;
    private int last_lenses_camera_id = -1;

    /** The selfie lighting: the front camera has no flash, so these two buttons switch the white
     *  areas beside the preview instead. Photo and video both show it (Kickoff Video-Blatt
     *  Runde 2, item 3; Falk asked for it in the photo sheet too, 2026-09-21). */
    private Row selfieLightRow() {
        Preview preview = main_activity.getPreview();
        final boolean on = preview.isSelfieLightOn();
        String label_off = getString(R.string.settings_sheet_off);
        String label_on = getString(R.string.settings_sheet_on);
        List<Option> options = new ArrayList<>();
        options.add(new Option(label_off, R.drawable.ms_lightbulb_off, null, null, 0.0f, !on,
                new Runnable() {
                    @Override
                    public void run() {
                        main_activity.getPreview().setSelfieLightOn(false);
                        refresh();
                    }
                }));
        options.add(new Option(label_on, R.drawable.ms_lightbulb, null, null, 0.0f, on,
                new Runnable() {
                    @Override
                    public void run() {
                        main_activity.getPreview().setSelfieLightOn(true);
                        refresh();
                    }
                }));
        return new Row(getString(R.string.settings_sheet_selfie_light), on ? label_on : label_off, options);
    }

    private List<Row> photoGeneralRows(final List<CameraController.Size> sizes) {
        List<Row> rows = new ArrayList<>();
        Preview preview = main_activity.getPreview();
        final MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();

        // light: the front camera gets the selfie lighting instead of the flash, which does not
        // exist on it (Falk 2026-09-21)
        if( frontCamera() ) {
            rows.add(selfieLightRow());
        }
        else {
            // The light row (Falk's choice, Schritt E/F): Nachtsicht is no mode of its own, it is a
            // state of the light, and the buttons are mutually exclusive - which the camera
            // enforces anyway (a night extension session only allows flash_off,
            // Preview.setupCamera()). "Nacht auto" hands the switch to the darkness of the scene:
            // with it the night extension comes on by itself and goes off again in the light
            // (MainUI.checkNightAuto()).
            // Which buttons appear follows the mode (Schritt F): Foto and Panorama get all five,
            // DRO only the flash ones (a real flash works there, the night extension would not),
            // and HDR and Porträt get none - HDR because its exposure bracket holds AE off, so
            // even the flash could not fire, and Porträt because it is an extension session of its
            // own (only one at a time). A row with fewer than two options is dropped below.
            // Their order is Falk's (Schritt G, Punkt 7): nichts · auto nacht · auto blitz ·
            // nacht an · blitz an.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            MyApplicationInterface.PhotoMode photo_mode = applicationInterface.getTopLevelPhotoMode();
            boolean night_offered = photo_mode == MyApplicationInterface.PhotoMode.Standard ||
                    photo_mode == MyApplicationInterface.PhotoMode.Panorama;
            boolean flash_offered = night_offered || photo_mode == MyApplicationInterface.PhotoMode.DRO;
            // Die Nacht-Knöpfe fragen hier das Modell, nicht die laufende Sitzung: mit festgelegtem
            // physischem Objektiv meldet die Kamera gar keine Erweiterung (CameraController2 holt
            // die Charakteristiken nur ohne physisches Objektiv), und die Wahl der Nacht nimmt die
            // Festlegung zurück (Kickoff Schritt G, Punkt 8)
            // Und sie bleiben stehen, solange der Zustand sie trägt (Schritt H, Punkt 1): wer die
            // Nacht gerade gewählt hat, hat sie in den Preferences stehen, während der Neuaufbau die
            // Erweiterung noch nicht kennt - genau in diesem Fenster verschwanden die beiden Knöpfe
            // sonst (Falk: "außerdem verschwinden auto nacht und nacht"), und die Markierung fiel
            // auf den Blitz zurück.
            boolean night_stored = sharedPreferences.getBoolean(PreferenceKeys.NightAutoPreferenceKey, false) ||
                    sharedPreferences.getBoolean(PreferenceKeys.NightOnPreferenceKey, false);
            boolean night_possible = main_activity.supportsCameraExtension(CameraExtensionCharacteristics.EXTENSION_NIGHT) ||
                    applicationInterface.getCameraIdSPhysicalPref() != null || night_stored;
            boolean night_auto = night_offered && night_possible &&
                    sharedPreferences.getBoolean(PreferenceKeys.NightAutoPreferenceKey, false);
            boolean night_on = night_possible && applicationInterface.isNightModeOn();
            // Der gemerkte Blitzwert markiert, nicht der der laufenden Sitzung: waehrend des
            // Neuaufbaus ist der Index der Sitzung -1, Preview.getCurrentFlashValue() antwortet
            // also null und die Markierung sprang beim Tippen erst auf "nichts" und dann auf den
            // gewaehlten Knopf (Schritt H, Punkt 2).
            String current = applicationInterface.getFlashPref();
            if( current == null || current.length() == 0 )
                current = preview.getCurrentFlashValue();
            // Die Nacht hat Vorrang vor dem Blitz: eine Nacht-Sitzung lässt der Plattform wegen nur
            // flash_off zu, der gemerkte Blitzwert kommt zurück, sobald die Nacht aus ist. Damit ist
            // immer genau ein Knopf markiert (Kickoff Schritt G, Punkt 13).
            boolean flash_auto = !night_auto && !night_on && "flash_auto".equals(current);
            boolean flash_on = !night_auto && !night_on && "flash_on".equals(current);
            List<Option> options = new ArrayList<>();
            options.add(lightOption(getString(R.string.settings_sheet_none), R.drawable.ms_block, null, null,
                    "flash_off", false, Boolean.FALSE, !night_auto && !night_on && !flash_auto && !flash_on));
            if( night_offered && night_possible ) {
                options.add(new Option(getString(R.string.settings_sheet_night_auto), R.drawable.ms_night_sight_auto, null, null, 0.0f, night_auto,
                        new Runnable() {
                            @Override
                            public void run() {
                                // only the automation is switched here; whether the night itself
                                // runs stays as it is, and so does the flash - the darkness decides
                                main_activity.getMainUI().lightSettings(null, true, null);
                            }
                        }));
            }
            // the flash buttons ask the hardware, not the session (Preview.hasFlashUnit()): with
            // the night extension running the session allows nothing but flash_off, and asking it
            // would take the buttons away with no way back to the flash (Falk 2026-09-22)
            if( flash_offered && preview.hasFlashUnit() ) {
                options.add(lightOption(getString(R.string.settings_sheet_flash_auto), R.drawable.ms_flash_auto, null, null,
                        "flash_auto", false, Boolean.FALSE, flash_auto));
            }
            if( night_offered && night_possible ) {
                // der Blitzwert bleibt gemerkt: diese Wahl rührt ihn nicht an (Schritt F)
                // Der Mond als Kontur, nicht als gefuellte Scheibe (Falk, Schritt H Punkt 5)
                options.add(lightOption(getString(R.string.settings_sheet_night_on), R.drawable.ms_moon_outline, null, null,
                        null, false, Boolean.TRUE, night_on && !night_auto));
            }
            if( flash_offered && preview.hasFlashUnit() ) {
                options.add(lightOption(getString(R.string.settings_sheet_flash_on), R.drawable.ms_flash_on, null, null,
                        "flash_on", false, Boolean.FALSE, flash_on));
            }
            // Die Blitz-Knöpfe schalten die Nacht mit ab (night_on = FALSE oben): bliebe sie
            // stehen, spränge die Markierung beim nächsten Aufbau zurück - genau das war Falks
            // "Auto Blitz lässt sich nicht auswählen" (Schritt G, Punkt 5).
            if( options.size() >= 2 )
                // without a value line: five buttons are 240 dp of the row, and the marked button
                // already says what is on (Schritt E, to be looked at on the device)
                rows.add(new Row(getString(R.string.settings_sheet_more_light), "", options));
        }

        // timer - not in panorama mode (as in the popup)
        if( main_activity.getApplicationInterface().getPhotoMode() != MyApplicationInterface.PhotoMode.Panorama ) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            String current = sharedPreferences.getString(PreferenceKeys.TimerPreferenceKey, "0");
            int [] icons = {R.drawable.ms_timer_off, R.drawable.ms_timer_3, R.drawable.ms_timer_5, R.drawable.ms_timer_10};
            List<Option> options = new ArrayList<>();
            for(int i=0;i<TIMER_VALUES.length;i++) {
                final String value = TIMER_VALUES[i];
                final String label = value.equals("0") ? getString(R.string.settings_sheet_off) : getString(R.string.settings_sheet_seconds, value);
                options.add(new Option(label, icons[i], null, null, 0.0f, value.equals(current),
                        new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
                                editor.putString(PreferenceKeys.TimerPreferenceKey, value);
                                editor.apply();
                                // Der Timer-Chip im Band traegt denselben Zustand (Schritt G Punkt 14)
                                // und muss mit: er frischt sich sonst erst beim naechsten showGUI auf
                                // und stand so auf dem alten Zeichen (Schritt H, Punkt 1).
                                main_activity.getMainUI().getOnScreenIcons().updateOnScreenIcons();
                                refresh();
                            }
                        }));
            }
            rows.add(new Row(getString(R.string.settings_sheet_timer),
                    selectedLabelOr(options, current == null ? null : getString(R.string.settings_sheet_seconds, current)), options));
        }

        // no macro row (2026-09-21): macro is a lens, not a mode - it lives in the WW and NO
        // buttons of the manual lens selection now, and without that selection in video there is
        // none at all

        // aspect ratio - in panorama mode too since Schritt E: the panorama used to be nailed to
        // 4:3 (MyApplicationInterface.choosePanoramaResolution()), and with the ratio reachable
        // the row can offer 16:9 there as well. Together with the resolution row's two classes
        // that is the 4:3/16:9 x FHD/large choice Falk asked for; the panorama is a fixed 10
        // frames since Schritt F, and its stitch resolution follows the heap
        // (ImageSaver.computePanoramaSampleSize()).
        {
            CameraController.Size current = preview.getCurrentPictureSize();
            if( current != null && sizes != null && !sizes.isEmpty() ) {
                final float [] ratios = ASPECT_CLASSES;
                int [] labels = {R.string.settings_sheet_aspect_wide, R.string.settings_sheet_aspect_full};
                String [] texts = {"16:9", "4:3"};
                // exactly one button is marked, and it is the nearer class when the camera holds
                // a size of neither (the selfie's full 10 MP size is 1,40:1) - an unmarked row
                // looked like a bug on the device (2026-09-21)
                int selected_ratio = aspectClassIndex(ratio(current));
                List<Option> options = new ArrayList<>();
                for(int i=0;i<ratios.length;i++) {
                    final float ratio = ratios[i];
                    options.add(new Option(getString(labels[i]), 0, Glyph.ASPECT, null, ratio,
                            i == selected_ratio,
                            new Runnable() {
                                @Override
                                public void run() {
                                    // the snapshot of this build, so a tap doesn't query the
                                    // camera characteristics again (Kickoff Runde 2, 1.5)
                                    CameraController.Size size = aspectSize(sizes, ratio);
                                    if( size != null )
                                        setPictureSize(size);
                                }
                            }));
                }
                rows.add(new Row(getString(R.string.settings_sheet_aspect),
                        selectedLabelOr(options, ratioText(current)), options));
            }
        }

        return rows;
    }

    /** One button of the light row. The light buttons do not choose the photo mode: they only set
     *  the light, and the mode standing stays as it is (MainUI.lightSettings()). That is what lets
     *  the night run inside the panorama (Schritt F).
     *  @param flash_value the flash to store, null to leave it as it is (the night buttons).
     *  @param night_on the night state to store, null to leave it as it is.
     */
    private Option lightOption(CharSequence label, int symbol, Glyph glyph, String text,
                               String flash_value, boolean night_auto, Boolean night_on, boolean selected) {
        return new Option(label, symbol, glyph, text, 0.0f, selected, new Runnable() {
            @Override
            public void run() {
                main_activity.getMainUI().lightSettings(flash_value, night_auto, night_on);
            }
        });
    }

    /** One button of the HDR/DRO row: the photo mode it picks, written through the carousel's own
     *  switch (MainUI.switchPhotoMode()), because that is what clears the cached mode and rebuilds
     *  the camera. Picking by hand also ends the night automation - see MainUI. */
    private Option modeOption(int label_id, String text, String photo_mode_value, boolean selected) {
        return new Option(getString(label_id), 0, Glyph.MODE, text, 0.0f, selected,
                new Runnable() {
                    @Override
                    public void run() {
                        main_activity.getMainUI().switchPhotoMode(photo_mode_value);
                    }
                });
    }

    /** The value line of the HDR/DRO row: the mode it holds, and nothing when the camera is in a
     *  mode this row doesn't offer (Panorama, Porträt) - the carousel shows that one. */
    private CharSequence modeLabel(MyApplicationInterface.PhotoMode photo_mode) {
        switch( photo_mode ) {
            case HDR:
                return getString(R.string.settings_sheet_hdr);
            case DRO:
                return getString(R.string.settings_sheet_dro);
            case Standard:
                return getString(R.string.settings_sheet_normal);
            default:
                return "";
        }
    }

    private List<Row> photoProRows(final List<CameraController.Size> sizes) {
        List<Row> rows = new ArrayList<>();
        Preview preview = main_activity.getPreview();
        final MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();

        // HDR/DRO: no modes of the carousel any more (Falk 2026-09-22, Schritt E) - they are the
        // three buttons of this row now, next to the resolution they change. Both are photo modes
        // in the app's core (MyApplicationInterface.getPhotoMode()), so the choice still goes
        // through the mode preference and the camera is rebuilt for it.
        {
            boolean hdr = main_activity.supportsHDR();
            boolean dro = main_activity.supportsDRO();
            if( hdr || dro ) {
                // the top-level mode: with the night on, getPhotoMode() answers X_Night, which this
                // row does not offer - the night is the light row's business, not the mode's
                // (Schritt F)
                MyApplicationInterface.PhotoMode photo_mode = applicationInterface.getTopLevelPhotoMode();
                List<Option> options = new ArrayList<>();
                options.add(modeOption(R.string.settings_sheet_normal, "NORM", "preference_photo_mode_std",
                        photo_mode == MyApplicationInterface.PhotoMode.Standard));
                if( hdr )
                    options.add(modeOption(R.string.settings_sheet_hdr, "HDR", "preference_photo_mode_hdr",
                            photo_mode == MyApplicationInterface.PhotoMode.HDR));
                if( dro )
                    options.add(modeOption(R.string.settings_sheet_dro, "DRO", "preference_photo_mode_dro",
                            photo_mode == MyApplicationInterface.PhotoMode.DRO));
                rows.add(new Row(getString(R.string.settings_sheet_photo_mode), modeLabel(photo_mode), options));
            }
        }

        // resolution: two sizes per aspect class - the camera's largest, and the space-saving one
        // Falk asked for (2026-09-21): its largest size that is still 1920 wide, the "FHD" he
        // means (1920x1080 in 16:9, 1920x1440 in 4:3). The selfie has no choice, as in the native
        // app: its row shows the one size it has, and the camera is put onto its largest (a stored
        // 3 MP would otherwise be stuck there with no button to leave it)
        {
            CameraController.Size current = preview.getCurrentPictureSize();
            // Kickoff 3.4: the resolution list the camera reports, for the first device test
            // (Falk suspects the 50 MP size is reserved for the Google app)
            if( MyDebug.LOG )
                Log.d(TAG, "supported picture sizes: " + sizes);
            if( sizes != null && !sizes.isEmpty() ) {
                // the camera's own size is the truth, but it is null while the camera is being
                // reopened; then the stored preference decides, which is the value the reopen is
                // going to apply anyway (Kickoff Runde 2, 1.4)
                String current_text = current != null ? sizeText(current) : storedPictureSize(preview);
                // the selfie's own sizes are not exactly 16:9 or 4:3 either (its full size is
                // 1,40:1, which the aspect row calls 4:3), so its row works on the aspect class
                float ratio = current != null ? ratio(current) : currentRatio(current_text);
                if( frontCamera() )
                    ratio = aspectClass(ratio);
                // the front camera's largest size (3840x2736) misses 4:3 by more than the generic
                // tolerance, so its row takes the largest size of the class instead - otherwise
                // the one size it is supposed to use is not reachable (Runde 3)
                CameraController.Size max_size = frontCamera() ?
                        largestInAspectClass(sizes, ratio) : standardSize(sizes, ratio);
                CameraController.Size save_size = savingSize(sizes, ratio);
                if( frontCamera() ) {
                    CameraController.Size only = max_size != null ? max_size : current;
                    if( only != null ) {
                        rows.add(new Row(getString(R.string.settings_sheet_resolution),
                                withResolution(getString(R.string.settings_sheet_mp, String.valueOf(Math.round(mp(only)))), only),
                                new ArrayList<Option>()));
                        if( current_text != null && !sizeText(only).equals(current_text) )
                            setPictureSize(only);
                    }
                }
                else if( max_size != null && save_size != null && !sizeText(save_size).equals(sizeText(max_size)) ) {
                    // exactly one button is marked at all times: the one holding the camera's
                    // size, otherwise the larger one (Kickoff Runde 2, 1.4)
                    boolean matches_small = sizeText(save_size).equals(current_text);
                    boolean matches_max = sizeText(max_size).equals(current_text);
                    boolean small_selected = matches_small || (!matches_max && (current == null || mp(current) <= mp(save_size)));
                    List<Option> options = new ArrayList<>();
                    options.add(resolutionOption(save_size, small_selected));
                    options.add(resolutionOption(max_size, !small_selected));
                    // the value line shows the marked class, or the real size when neither
                    // button holds it
                    CameraController.Size shown = current != null && !matches_small && !matches_max ?
                            current : (small_selected ? save_size : max_size);
                    rows.add(new Row(getString(R.string.settings_sheet_resolution),
                            withResolution(getString(R.string.settings_sheet_mp, String.valueOf(Math.round(mp(shown)))), shown),
                            options));
                }
            }
        }

        // RAW/JPEG
        if( preview.supportsRaw() ) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            boolean raw = "preference_raw_yes".equals(sharedPreferences.getString(PreferenceKeys.RawPreferenceKey, "preference_raw_no"));
            List<Option> options = new ArrayList<>();
            // "off" is the same RAW text with a diagonal stroke through it (Kickoff 2.4)
            options.add(new Option(getString(R.string.settings_sheet_raw_off), 0, Glyph.RAW_OFF, "RAW", 0.0f, !raw,
                    new Runnable() {
                        @Override
                        public void run() {
                            setRaw("preference_raw_no");
                        }
                    }));
            options.add(new Option(getString(R.string.settings_sheet_raw_on), 0, Glyph.RAW, "RAW", 0.0f, raw,
                    new Runnable() {
                        @Override
                        public void run() {
                            setRaw("preference_raw_yes");
                        }
                    }));
            rows.add(new Row(getString(R.string.settings_sheet_raw),
                    getString(raw ? R.string.settings_sheet_raw_on : R.string.settings_sheet_raw_off), options));
        }

        // lens selection - for every camera that reports physical lenses. That is the rear camera
        // on this device; the front camera reports a pair too (checked 2026-09-21) but choosing
        // between them is pointless, so the row is dropped there (Kickoff Video-Blatt Runde 1,
        // item 6).
        if( !frontCamera() ) {
            List<MainActivity.Lens> lenses = manualLenses();
            if( lenses != null ) {
                // Im Panorama ist diese Zeile die Wahl der Panorama-Reihe selbst ("1×" oder
                // "Weitwinkel"): dieselbe Festlegung des physischen Objektivs hieße hier sonst
                // "Manuell", obwohl Falk nur die Panorama-Linse gewechselt hat (Schritt G,
                // Punkt 9).
                final boolean panorama = applicationInterface.getPhotoMode() == MyApplicationInterface.PhotoMode.Panorama;
                final boolean manual = applicationInterface.getCameraIdSPhysicalPref() != null;
                List<Option> options = new ArrayList<>();
                // "A" in a circle is the automatic lens choice, the aperture symbol the manual
                // one (Kickoff 2.4)
                options.add(new Option(getString(R.string.settings_sheet_auto), 0, Glyph.LENS_AUTO, "A", 0.0f, !manual,
                        new Runnable() {
                            @Override
                            public void run() {
                                if( panorama )
                                    main_activity.switchPanoramaLensFromSheet(false);
                                else
                                    main_activity.switchToAutoLens();
                                refresh();
                            }
                        }));
                options.add(new Option(panorama ? main_activity.getPanoramaWideLensLabel() : getString(R.string.settings_sheet_manual),
                        R.drawable.ms_camera, null, null, 0.0f, manual,
                        new Runnable() {
                            @Override
                            public void run() {
                                if( panorama )
                                    main_activity.switchPanoramaLensFromSheet(true);
                                else
                                    // start on the main lens, see MainActivity.getMainLens()
                                    main_activity.switchToManualLens(main_activity.getMainLens());
                                refresh();
                            }
                        }));
                String value = panorama ?
                        (manual ? main_activity.getPanoramaWideLensLabel() : getString(R.string.settings_sheet_auto)) :
                        getString(manual ? R.string.settings_sheet_manual : R.string.settings_sheet_auto);
                rows.add(new Row(getString(R.string.settings_sheet_lens), value, options));
            }
        }
        else if( applicationInterface.getCameraIdSPhysicalPref() != null ) {
            // without the row a manual lens picked earlier could not be given up any more, so the
            // front camera falls back to the automatic choice. No camera restart: the pref is
            // only cleared here, the next open picks it up.
            applicationInterface.setCameraIdPref(main_activity.getActualCameraId(), null);
        }

        return rows;
    }

    private static String sizeText(CameraController.Size size) {
        return size == null ? null : size.width + " " + size.height;
    }

    /** The short form of a row's value ("12MP", "FHD") plus the exact frame size in brackets:
     *  the badges have room for the short form only, the value line carries both (Falk,
     *  2026-09-21). */
    private String withResolution(String value, CameraController.Size size) {
        return withResolution(value, size.width, size.height);
    }

    private String withResolution(String value, int width, int height) {
        return getString(R.string.settings_sheet_with_resolution, value, width + "x" + height);
    }

    /** The resolution the stored preference holds for this camera, in the camera's own
     *  "width height" format, or null. */
    private String storedPictureSize(Preview preview) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        String stored = sharedPreferences.getString(PreferenceKeys.getResolutionPreferenceKey(
                preview.getCameraId(), main_activity.getApplicationInterface().getCameraIdSPhysicalPref()), null);
        if( MyDebug.LOG )
            Log.d(TAG, "stored picture size: " + stored);
        return stored;
    }

    /** The ratio of the "width height" string the preference holds, for the rows that need one
     *  while the camera is closed. Anything unparsable falls back to 16:9, the reference's own
     *  ratio. */
    private static float currentRatio(String size_text) {
        if( size_text != null ) {
            String [] parts = size_text.trim().split(" ");
            if( parts.length == 2 ) {
                try {
                    int width = Integer.parseInt(parts[0]);
                    int height = Integer.parseInt(parts[1]);
                    if( width > 0 && height > 0 )
                        return width / (float)height;
                }
                catch(NumberFormatException e ) {
                    // fall through to the default
                }
            }
        }
        return 16.0f / 9.0f;
    }

    /* ---- rows of the video sheet (Kickoff 3.5) -------------------------------------------- */

    private List<Row> videoGeneralRows() {
        List<Row> rows = new ArrayList<>();
        Preview preview = main_activity.getPreview();
        final MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        final String fps_value = applicationInterface.getVideoFPSPref();
        final boolean front = frontCamera();

        if( front ) {
            rows.add(selfieLightRow());
        }
        // flash: only the values that make sense while recording. The middle one is not a flash
        // mode of the camera but the torch by ambient light (Kickoff Video-Blatt Runde 2, item 2)
        else {
            // Zwei Knöpfe, aus und an (Falk 2026-09-22: "bei video lassen wir den automatischen
            // blitz einfach komplett und machen nur an oder aus da es nur probleme verursacht und
            // bloat ist"). Der mittlere war zuletzt zweierlei: der Blitz der Kamera und zugleich
            // das Taschenlicht bei Dunkelheit - letzteres ist entfernt (Preview.updateTorchAuto
            // gibt es nicht mehr), ein gemerktes "flash_auto" fällt auf "aus".
            // Die beiden Knöpfe fragen weiter die Hardware, nicht die Werte der laufenden Sitzung:
            // mit manuellem ISO streicht Preview.setupCameraParameters() das "flash_auto" aus der
            // Liste (Falk, Schritt G Punkt 1).
            List<String> supported = new ArrayList<>();
            if( preview.hasFlashUnit() ) {
                for(String value : new String[] {"flash_off", "flash_torch"}) {
                    if( Preview.isFlashSupportedForVideo(value) )
                        supported.add(value);
                }
            }
            String current = preview.getCurrentFlashValue();
            String [] values = {"flash_off", "flash_torch"};
            int [] labels = {R.string.settings_sheet_off, R.string.settings_sheet_on};
            int [] icons = {R.drawable.ms_flash_off, R.drawable.ms_flash_on};
            List<Option> options = new ArrayList<>();
            for(int i=0;i<values.length;i++) {
                final String value = values[i];
                if( !supported.contains(value) )
                    continue;
                options.add(new Option(getString(labels[i]), icons[i], null, null, 0.0f, value.equals(current),
                        new Runnable() {
                            @Override
                            public void run() {
                                setVideoFlash(value);
                            }
                        }));
            }
            if( options.size() >= 2 )
                rows.add(new Row(getString(R.string.settings_sheet_flash), selectedLabelOr(options, current), options));
        }

        // video resolution: FHD / 4K / 8K where the current frame rate offers them. Only 16:9 -
        // the short badge is enough here, the resolution itself is what the knobs are for. The
        // selfie gets the same row (Falk 2026-09-21): it records up to 4K, so it shows FHD and 4K
        // its own scope: the flash row above has an "options" of its own
        // In Zeitraffer and Zeitlupe the resolution and the frame rate are not chosen but set by
        // the mode (SPEC 4.6, Falk 2026-09-21), so neither row appears there.
        final boolean video_speed_mode = !MyApplicationInterface.VIDEO_SPEED_OFF.equals(applicationInterface.getVideoSpeedMode());
        if( !video_speed_mode ) {
            // the front camera records 16:9 only, so a stored quality of another ratio (the pixel
            // offers 1:1) is put right before the row reads the qualities (Video-Blatt Runde 1, 4)
            if( front )
                fixFrontVideoQuality();
            List<String> qualities = preview.getSupportedVideoQuality(fps_value);
            if( qualities == null || qualities.isEmpty() )
                qualities = preview.getVideoQualityHander().getSupportedVideoQuality();
            // Kickoff 3.4/3.5: the video quality and frame rate lists the camera reports
            if( MyDebug.LOG ) {
                Log.d(TAG, "supported video qualities: " + qualities);
                Log.d(TAG, "video sizes: " + preview.getVideoQualityHander().getSupportedVideoSizes());
            }
            List<Option> options = new ArrayList<>();
            int [] widths = {1920, 3840, 7680};
            int [] heights = {1080, 2160, 4320};
            // the labels are the same on screen and in the content description, so they stay
            // untranslated (Kickoff 3.5)
            String [] labels = {"FHD", "4K", "8K"};
            String current = preview.getVideoQualityHander().getCurrentVideoQuality();
            // the badge text of every quality the row offers, parallel to the qualities: the value
            // line names the current one with it
            List<String> used = new ArrayList<>();
            List<String> used_names = new ArrayList<>();
            for(int i=0;i<widths.length;i++) {
                final String quality = findQuality(qualities, widths[i], heights[i]);
                if( quality == null )
                    continue;
                used.add(quality);
                used_names.add(labels[i]);
                options.add(new Option(labels[i], 0, Glyph.TEXT, labels[i], 0.0f, quality.equals(current),
                        new Runnable() {
                            @Override
                            public void run() {
                                setVideoQuality(quality);
                            }
                        }));
            }
            // Falk's rule (2026-09-21) is a high and a space-saving size in every camera. The
            // selfie has no 4K: this device's front camera reports FullHD as its highest profile
            // and stops at 3264x1836 for recording, so its largest quality joins FullHD as the
            // second button. It is named by its resolution class like the buttons next to it
            // ("3K" - the class is rounded, 3264 is 3.2K), not by its megapixels (Falk, 2026-09-21)
            if( options.size() == 1 && qualities != null ) {
                String largest = null;
                int largest_width = 0;
                float largest_mp = 0.0f;
                for(String quality : qualities) {
                    CamcorderProfile profile = preview.getCamcorderProfile(quality);
                    if( profile == null || profile.videoFrameWidth == 0 )
                        continue;
                    float megapixels = profile.videoFrameWidth * (float)profile.videoFrameHeight / 1000000.0f;
                    if( megapixels > largest_mp && !used.contains(quality) ) {
                        largest_mp = megapixels;
                        largest_width = profile.videoFrameWidth;
                        largest = quality;
                    }
                }
                if( largest != null && largest_mp > 0.0f ) {
                    final String quality = largest;
                    String label = Math.round(largest_width / 1000.0f) + "K";
                    used.add(quality);
                    used_names.add(label);
                    options.add(new Option(label, 0, Glyph.TEXT, label, 0.0f, quality.equals(current),
                            new Runnable() {
                                @Override
                                public void run() {
                                    setVideoQuality(quality);
                                }
                            }));
                }
            }
            if( !options.isEmpty() ) {
                // the value line names the current quality and adds its exact frame size, the same
                // shape as the photo row (Falk, 2026-09-21). The camera's own wording
                // ("1920x1080 FullHD · 2MP") made the longest line of the sheet and sat noticeably
                // off the row's line (device 2026-09-21)
                CharSequence value_text = null;
                if( current != null ) {
                    CamcorderProfile profile = preview.getCamcorderProfile(current);
                    if( profile != null && profile.videoFrameWidth > 0 ) {
                        int index = used.indexOf(current);
                        String short_form = index >= 0 ? used_names.get(index) :
                                getString(R.string.settings_sheet_mp,
                                        String.valueOf(Math.round(profile.videoFrameWidth * (float)profile.videoFrameHeight / 1000000.0f)));
                        value_text = withResolution(short_form, profile.videoFrameWidth, profile.videoFrameHeight);
                    }
                }
                rows.add(new Row(getString(R.string.settings_sheet_resolution),
                        value_text != null ? value_text : selectedLabelOr(options, null), options));
            }
        }

        // frame rate - the same candidates the settings list offers
        if( !video_speed_mode ) {
            int [] candidates = {24, 30, 60};
            // "default" means whatever the camcorder profile of the current quality uses, so the
            // knobs are matched against that number instead of the word
            // (Kickoff Video-Blatt Runde 1, item 2)
            final int real_fps = defaultFPS(preview);
            List<Option> options = new ArrayList<>();
            List<String> supported_fps = new ArrayList<>();
            for(int i=0;i<candidates.length;i++) {
                final String value = String.valueOf(candidates[i]);
                if( !preview.fpsIsHighSpeed(value) && !preview.getVideoQualityHander().videoSupportsFrameRate(candidates[i]) )
                    continue;
                supported_fps.add(value);
                boolean selected = value.equals(fps_value) ||
                        ("default".equals(fps_value) && candidates[i] == real_fps);
                options.add(new Option(value, 0, Glyph.FPS, value, 0.0f, selected,
                        new Runnable() {
                            @Override
                            public void run() {
                                setVideoFPS(value);
                            }
                        }));
            }
            // the selected knob carries the number already; only an fps the knobs cannot show
            // falls back to the stored value
            CharSequence value_text = selectedLabelOr(options, fps_value);
            if( MyDebug.LOG )
                Log.d(TAG, "supported frame rates: " + supported_fps + ", current " + fps_value + ", real " + real_fps);
            if( value_text == null && fps_value != null )
                value_text = fps_value.equals("default") ? getString(R.string.settings_sheet_auto) : fps_value;
            if( !options.isEmpty() )
                rows.add(new Row(getString(R.string.settings_sheet_fps), value_text, options));
        }

        // no macro row for video (2026-09-21): the continuous autofocus gets as close as needed,
        // and the camera picks the near lens on its own when the subject is closer than ~10 cm

        return rows;
    }

    private List<Row> videoMoreRows() {
        List<Row> rows = new ArrayList<>();
        Preview preview = main_activity.getPreview();
        if( preview.supportsVideoStabilization() ) {
            boolean on = PreferenceManager.getDefaultSharedPreferences(main_activity)
                    .getBoolean(PreferenceKeys.VideoStabilizationPreferenceKey, true);
            String label_off = getString(R.string.settings_sheet_off);
            String label_on = getString(R.string.settings_sheet_on);
            List<Option> options = new ArrayList<>();
            // "off" is the crossed out circle the other rows use for it, not the same symbol as
            // "on" (Runde 1)
            options.add(new Option(label_off, R.drawable.ms_block, null, null, 0.0f, !on,
                    new Runnable() {
                        @Override
                        public void run() {
                            setVideoStabilization(false);
                        }
                    }));
            options.add(new Option(label_on, R.drawable.ms_video_stable, null, null, 0.0f, on,
                    new Runnable() {
                        @Override
                        public void run() {
                            setVideoStabilization(true);
                        }
                    }));
            rows.add(new Row(getString(R.string.settings_sheet_stabilization), on ? label_on : label_off, options));
        }
        return rows;
    }

    /* ---- writing the settings (the calls the old popup uses) ------------------------------ */

    private Option resolutionOption(final CameraController.Size size, boolean selected) {
        return new Option(getString(R.string.settings_sheet_mp, String.valueOf(Math.round(mp(size)))),
                0, Glyph.MP, String.valueOf(Math.round(mp(size))), 0.0f, selected,
                new Runnable() {
                    @Override
                    public void run() {
                        setPictureSize(size);
                    }
                });
    }

    private void setPictureSize(CameraController.Size size) {
        Preview preview = main_activity.getPreview();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
        editor.putString(PreferenceKeys.getResolutionPreferenceKey(preview.getCameraId(),
                main_activity.getApplicationInterface().getCameraIdSPhysicalPref()), size.width + " " + size.height);
        editor.apply();
        // the camera has to be reconfigured, so the sheet re-reads its rows from cameraSetup()
        postUpdateForSettings();
    }

    private void setRaw(String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
        editor.putString(PreferenceKeys.RawPreferenceKey, value);
        editor.apply();
        postUpdateForSettings();
    }

    /** Ein Blitz-Knopf des Video-Blatts: aus oder an, leise wie bisher (kein Toast). Das feste ISO
     *  muss nicht mehr mit zurückgenommen werden - das war nur für die weggefallene Automatik des
     *  Taschenlichts nötig, die an einem gemessenen ISO hing (Schritt G, Punkt 1; Falk hat die
     *  Automatik am 2026-09-22 ganz gestrichen). */
    private void setVideoFlash(String value) {
        main_activity.getPreview().updateFlashQuiet(value);
        // wie die Auflösungs- und die Qualitäts-Zeile: der Neuaufbau ist der Weg, auf dem der neue
        // Wert bei der Kamera ankommt
        postUpdateForSettings();
    }

    private void setVideoQuality(String quality) {
        Preview preview = main_activity.getPreview();
        MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
        editor.putString(PreferenceKeys.getVideoQualityPreferenceKey(preview.getCameraId(),
                applicationInterface.getCameraIdSPhysicalPref(), applicationInterface.fpsIsHighSpeed()), quality);
        editor.apply();
        postUpdateForSettings();
    }

    private void setVideoFPS(String value) {
        Preview preview = main_activity.getPreview();
        MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
        editor.putString(PreferenceKeys.getVideoFPSPreferenceKey(preview.getCameraId(),
                applicationInterface.getCameraIdSPhysicalPref()), value);
        editor.apply();
        postUpdateForSettings();
    }

    private void setVideoStabilization(boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
        editor.putBoolean(PreferenceKeys.VideoStabilizationPreferenceKey, value);
        editor.apply();
        postUpdateForSettings();
    }

    /** the same 400 ms the popup waits for before reconfiguring the camera (800 ms when a
     *  camera extension is in use) */
    private void postUpdateForSettings() {
        handler.removeCallbacks(update_settings_runnable);
        long delay = main_activity.getApplicationInterface().isCameraExtensionPref() ? 800 : 400;
        // the sheet keeps what it shows until the camera is back (onCameraSetUp), instead of
        // following it through the restart
        restart_pending = true;
        handler.removeCallbacks(restart_pending_timeout);
        handler.postDelayed(restart_pending_timeout, RESTART_PENDING_TIMEOUT);
        handler.postDelayed(update_settings_runnable, delay);
    }

    private final Runnable update_settings_runnable = new Runnable() {
        @Override
        public void run() {
            if( MyDebug.LOG )
                Log.d(TAG, "update settings due to a sheet option");
            main_activity.updateForSettings(true, "", true, false);
        }
    };

    /* ---- small helpers -------------------------------------------------------------------- */

    private String getString(int resId) {
        return main_activity.getResources().getString(resId);
    }

    private String getString(int resId, String arg) {
        return main_activity.getResources().getString(resId, arg);
    }

    private String getString(int resId, String arg1, String arg2) {
        return main_activity.getResources().getString(resId, arg1, arg2);
    }

    /** The label of the selected option, or the fallback when none of them is selected - which
     *  happens when the stored value is one the row doesn't offer (e.g. a timer of "15"). */
    private static CharSequence selectedLabelOr(List<Option> options, CharSequence fallback) {
        for(Option option : options) {
            if( option.selected )
                return option.label;
        }
        return fallback;
    }

    private static float ratio(CameraController.Size size) {
        return size.width / (float)size.height;
    }

    /** The two aspect classes the sheet offers, as the ratio of the sizes behind them. */
    private static final float [] ASPECT_CLASSES = {16.0f / 9.0f, 4.0f / 3.0f};

    /** The aspect class a ratio belongs to: the exact class when one matches, otherwise the
     *  nearer one. So the selfie's full size (1,40:1) counts as 4:3 and a 1:1 size picked in the
     *  full settings list lands on the nearer class - an unmarked aspect row looked like a bug on
     *  the device (2026-09-21). */
    private static float aspectClass(float size_ratio) {
        return ASPECT_CLASSES[aspectClassIndex(size_ratio)];
    }

    /** The index of the class {@link #aspectClass(float)} puts the ratio into. */
    private static int aspectClassIndex(float size_ratio) {
        int best = 0;
        float best_distance = Float.MAX_VALUE;
        for(int i=0;i<ASPECT_CLASSES.length;i++) {
            float distance = Math.abs(size_ratio - ASPECT_CLASSES[i]);
            if( distance <= RATIO_TOLERANCE )
                return i;
            if( distance < best_distance ) {
                best_distance = distance;
                best = i;
            }
        }
        return best;
    }

    /** The space-saving size of an aspect class: its largest size that is still 1920 wide, the
     *  "FHD" Falk means (2026-09-21) - 1920x1080 in 16:9, 1920x1440 in 4:3. Falls back to the
     *  smallest size of the class when the camera has nothing that narrow. */
    private static CameraController.Size savingSize(List<CameraController.Size> sizes, float ratio) {
        CameraController.Size best = null;
        for(CameraController.Size size : sizes) {
            if( Math.abs(ratio(size) - ratio) > RATIO_TOLERANCE || size.width > SAVING_PICTURE_WIDTH )
                continue;
            if( best == null || mp(size) > mp(best) )
                best = size;
        }
        return best != null ? best : smallestSize(sizes, ratio);
    }

    /** The smallest size of the given ratio - the fallback of savingSize() above. */
    private static CameraController.Size smallestSize(List<CameraController.Size> sizes, float ratio) {
        CameraController.Size best = null;
        for(CameraController.Size size : sizes) {
            if( Math.abs(ratio(size) - ratio) > RATIO_TOLERANCE )
                continue;
            if( best == null || mp(size) < mp(best) )
                best = size;
        }
        return best;
    }

    private static float mp(CameraController.Size size) {
        return size.width * (float)size.height / 1000000.0f;
    }

    /** The standard size of the given ratio: its largest size within the camera's own 12 MP grid
     *  (CameraController.STANDARD_PICTURE_WIDTH) - 4032x2268 in 16:9, 4032x3024 in 4:3 on the test
     *  device. The sensor's full readout (4080x3072) stays reachable through the full settings, it
     *  is just no longer what the app starts with (Falk, 2026-09-21).
     *  Falls back to largestSize() when the camera has nothing that narrow. */
    private static CameraController.Size standardSize(List<CameraController.Size> sizes, float ratio) {
        CameraController.Size best = null;
        for(CameraController.Size size : sizes) {
            if( Math.abs(ratio(size) - ratio) > RATIO_TOLERANCE || size.width > CameraController.STANDARD_PICTURE_WIDTH )
                continue;
            if( best == null || mp(size) > mp(best) )
                best = size;
        }
        return best != null ? best : largestSize(sizes, ratio);
    }

    /** The largest size of the given ratio. */
    private static CameraController.Size largestSize(List<CameraController.Size> sizes, float ratio) {
        CameraController.Size best = null;
        for(CameraController.Size size : sizes) {
            if( Math.abs(ratio(size) - ratio) > RATIO_TOLERANCE )
                continue;
            if( best == null || mp(size) > mp(best) )
                best = size;
        }
        return best;
    }

    /** The size the aspect row switches to: it has to stay in the class the Pro tab has (Kickoff
     *  3.3/3.4), and the class border is the same one standardSize()/savingSize() use - a width of
     *  SAVING_PICTURE_WIDTH, not a megapixel number. With the old 13 MP border a camera whose
     *  largest size is 12 MP (4032x3024) counted as "above the small class", so a tap on 16:9/4:3
     *  put it back onto the standard class even when the space-saving one was chosen (Runde 3).
     *  Works on the build's size snapshot. */
    private CameraController.Size aspectSize(List<CameraController.Size> sizes, float ratio) {
        CameraController.Size current = main_activity.getPreview().getCurrentPictureSize();
        if( current != null && current.width <= SAVING_PICTURE_WIDTH )
            return savingSize(sizes, ratio);
        // the front camera's own sizes are not exactly 4:3 (its largest is 3840x2736, 1,40:1), so
        // it needs the class match instead of the generic tolerance - see largestInAspectClass()
        if( frontCamera() )
            return largestInAspectClass(sizes, ratio);
        return standardSize(sizes, ratio);
    }

    /** The largest size belonging to the given aspect class, by {@link #aspectClass(float)}
     *  rather than the generic RATIO_TOLERANCE of standardSize(). The front camera needs this:
     *  its largest size (3840x2736, 1,40:1) is 0,07 away from 4:3 and so fell out of that
     *  tolerance, which left exactly the size the front camera is supposed to use unreachable
     *  through the sheet (Runde 3, foto-blatt-fixes-runde1.md). */
    private static CameraController.Size largestInAspectClass(List<CameraController.Size> sizes, float class_ratio) {
        CameraController.Size best = null;
        for(CameraController.Size size : sizes) {
            if( aspectClass(ratio(size)) != class_ratio )
                continue;
            if( best == null || mp(size) > mp(best) )
                best = size;
        }
        return best;
    }

    /** The frame rate the current video quality really records with, or -1 if the camera cannot
     *  say. This is what "default" resolves to (Kickoff Video-Blatt Runde 1, item 2). */
    private int defaultFPS(Preview preview) {
        String quality = preview.getVideoQualityHander().getCurrentVideoQuality();
        CamcorderProfile profile = quality == null ? null : preview.getCamcorderProfile(quality);
        return profile == null ? -1 : profile.videoFrameRate;
    }

    /** The front camera records 16:9; a stored quality with another ratio (the pixel offers
     *  1:1) is replaced by the 16:9 one of the same class. Does nothing when the stored value
     *  is fine, so the camera is not restarted over and over. */
    private void fixFrontVideoQuality() {
        Preview preview = main_activity.getPreview();
        String current = preview.getVideoQualityHander().getCurrentVideoQuality();
        CamcorderProfile profile = current == null ? null : preview.getCamcorderProfile(current);
        if( profile == null || profile.videoFrameHeight == 0 )
            return;
        if( profile.videoFrameWidth * 9 == profile.videoFrameHeight * 16 )
            return;
        List<String> qualities = preview.getVideoQualityHander().getSupportedVideoQuality();
        String fix = findQuality(qualities, 1920, 1080);
        if( fix == null && qualities != null )
            fix = findQuality(qualities, 1280, 720);
        if( fix == null )
            return;  // nothing 16:9 to switch to - leave the camera alone
        if( MyDebug.LOG )
            Log.d(TAG, "front camera video quality " + current + " is not 16:9, switching to " + fix);
        setVideoQuality(fix);
    }

    /** The quality string whose profile is exactly this frame size, or null if the camera
     *  doesn't offer it. Both numbers have to match: the pixel offers 1920x1920, and matching
     *  the width alone used to pick that for "FHD" (Kickoff Video-Blatt Runde 1, item 1). */
    private String findQuality(List<String> qualities, int width, int height) {
        if( qualities == null )
            return null;
        for(String quality : qualities) {
            CamcorderProfile profile = main_activity.getPreview().getCamcorderProfile(quality);
            if( profile != null && profile.videoFrameWidth == width && profile.videoFrameHeight == height )
                return quality;
        }
        return null;
    }

    /** A ratio like "16:9" or "1:1", for a picture size that fits none of the two options. */
    private static String ratioText(CameraController.Size size) {
        int a = size.width, b = size.height;
        while( b != 0 ) {
            int t = b;
            b = a % b;
            a = t;
        }
        if( a == 0 )
            return size.width + ":" + size.height;
        return size.width / a + ":" + size.height / a;
    }

    /* ---- the self-drawn glyphs of the option buttons -------------------------------------- */

    /** The shapes of the option buttons that are not Material Symbols (Kickoff 2.4). */
    private enum Glyph { ASPECT, MP, FPS, RAW, RAW_OFF, LENS_AUTO, TEXT, MODE }

    private class GlyphView extends View {
        private final Glyph glyph;
        private final String text;
        private final float ratio;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        /** The button's own fill, the label colour of the badge chips. */
        private int fill_color;

        GlyphView(Glyph glyph, String text, float ratio, boolean selected) {
            super(main_activity);
            this.glyph = glyph;
            this.text = text;
            this.ratio = ratio;
            paint.setTextAlign(Paint.Align.CENTER);
            setColor(ContextCompat.getColor(main_activity, selected ?
                            R.color.settings_sheet_button_glyph_selected : R.color.settings_sheet_button_glyph),
                    ContextCompat.getColor(main_activity, selected ?
                            R.color.settings_sheet_button_selected : R.color.settings_sheet_button));
        }

        void setColor(int color, int fill) {
            paint.setColor(color);
            this.fill_color = fill;
            invalidate();
        }

        /** Draws a badge label on the small inverted chip of the reference: the chip in the
         *  button's glyph colour, the label in the button's own fill colour, the lines led by
         *  their cap height. Measured on the Pixel screenshots (2026-09-21): the chip is
         *  #E2E2E2 with a #1F1F1F label on the dark button, the other way round on the white
         *  one - the same four colours the sheet already uses. */
        private void drawChip(Canvas canvas, float w, float h, String [] lines, float pad_x, float pad_y) {
            // Roboto's cap height is 0.71 of the text size; the reference leads the lines of a
            // two-line badge by barely more
            float cap = paint.getTextSize() * 0.72f;
            float line_gap = dp(1.0f);
            float block = lines.length * cap + (lines.length - 1) * line_gap;
            float text_width = 0.0f;
            for(String line : lines)
                text_width = Math.max(text_width, paint.measureText(line));
            int glyph_color = paint.getColor();
            rect.set(w / 2 - text_width / 2 - pad_x, h / 2 - block / 2 - pad_y,
                    w / 2 + text_width / 2 + pad_x, h / 2 + block / 2 + pad_y);
            canvas.drawRoundRect(rect, dp(2.5f), dp(2.5f), paint);
            paint.setColor(fill_color);
            float y = h / 2 - block / 2 + cap;
            for(String line : lines) {
                canvas.drawText(line, w / 2, y, paint);
                y += cap + line_gap;
            }
            paint.setColor(glyph_color);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            switch( glyph ) {
                case ASPECT: {
                    // a portrait rectangle of the option's ratio, 22 dp high
                    float height = dp(22.0f);
                    float width = height / ratio;
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(2.0f));
                    rect.set(w / 2 - width / 2, h / 2 - height / 2, w / 2 + width / 2, h / 2 + height / 2);
                    canvas.drawRoundRect(rect, dp(2.0f), dp(2.0f), paint);
                    break;
                }
                case MP: {
                    // the number over "MP", both lines the same size, on the chip
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(8.5f));
                    drawChip(canvas, w, h, new String [] { text, "MP" }, dp(3.5f), dp(2.5f));
                    break;
                }
                case FPS: {
                    // the frame rate over the reference's little gauge: three thin strokes and one
                    // wide one, all the same height
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(15.0f));
                    float cap = paint.getTextSize() * 0.72f;
                    float gauge_height = dp(5.5f), gauge_gap = dp(3.0f);
                    float tick_width = dp(1.8f), bar_width = dp(7.5f), stroke_gap = dp(2.5f);
                    float total = 3 * (tick_width + stroke_gap) + bar_width;
                    float block = cap + gauge_gap + gauge_height;
                    canvas.drawText(text, w / 2, h / 2 - block / 2 + cap, paint);
                    float left = w / 2 - total / 2;
                    float top = h / 2 + block / 2 - gauge_height;
                    for(int i=0;i<4;i++) {
                        float width = i == 3 ? bar_width : tick_width;
                        rect.set(left, top, left + width, top + gauge_height);
                        canvas.drawRect(rect, paint);
                        left += width + stroke_gap;
                    }
                    break;
                }
                case RAW:
                case RAW_OFF: {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(6.0f));
                    canvas.drawText(text, w / 2, h * 0.68f, paint);
                    if( glyph == Glyph.RAW_OFF ) {
                        // RAW switched off: the same text with a diagonal stroke (Kickoff 2.4)
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(dp(1.0f));
                        float half = dp(9.0f);
                        canvas.drawLine(w / 2 - half, h / 2 + half, w / 2 + half, h / 2 - half, paint);
                    }
                    break;
                }
                case LENS_AUTO: {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(1.5f));
                    canvas.drawCircle(w / 2, h / 2, dp(10.0f), paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    // the A fills the circle: cap height centred on the circle's centre (Falk,
                    // 2026-09-21: the letter was too small)
                    paint.setTextSize(sp(12.0f));
                    canvas.drawText(text, w / 2, h / 2 + paint.getTextSize() * 0.72f / 2, paint);
                    break;
                }
                case TEXT: {
                    // the video resolution's short name ("FHD") on the chip
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(8.5f));
                    drawChip(canvas, w, h, new String [] { text }, dp(3.5f), dp(5.0f));
                    break;
                }
                case MODE: {
                    // the short name of a mode ("AUTO", "NORM", "HDR") on the chip - a size
                    // smaller than TEXT's, four letters still fit the 24 dp the glyph gets
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(7.0f));
                    drawChip(canvas, w, h, new String [] { text }, dp(3.5f), dp(4.0f));
                    break;
                }
                default:
                    break;
            }
        }
    }

    /* ---- dragging the sheet down to close it (Kickoff 3.2) -------------------------------- */

    /** Drags and flings on the panel itself: everything that is not caught by an option button.
     *  The panel always hangs off the physical bottom edge, which is the bottom edge of our
     *  portrait-locked window in every device orientation, so the direction to close in is
     *  always window +y - no ui_rotation needed. */
    private class PanelTouchListener implements View.OnTouchListener {
        private float down_raw_y;
        private VelocityTracker velocity_tracker;
        private boolean dragging;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch( event.getActionMasked() ) {
                case MotionEvent.ACTION_DOWN:
                    down_raw_y = event.getRawY();
                    dragging = false;
                    if( velocity_tracker == null )
                        velocity_tracker = VelocityTracker.obtain();
                    else
                        velocity_tracker.clear();
                    velocity_tracker.addMovement(event);
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if( velocity_tracker != null )
                        velocity_tracker.addMovement(event);
                    float dy = event.getRawY() - down_raw_y;
                    if( dy > 0 ) {
                        dragging = true;
                        panel.animate().cancel();
                        panel.setTranslationY(resting_translation_y + dy);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    float dy = event.getRawY() - down_raw_y;
                    float velocity = 0.0f;
                    if( velocity_tracker != null ) {
                        velocity_tracker.addMovement(event);
                        velocity_tracker.computeCurrentVelocity(1000);
                        velocity = velocity_tracker.getYVelocity();
                    }
                    release();
                    float minimum_fling = ViewConfiguration.get(main_activity).getScaledMinimumFlingVelocity();
                    if( open && (dy > depth_px / 2.0f || (dragging && velocity > minimum_fling)) )
                        close();
                    else if( dragging )
                        panel.animate().translationY(resting_translation_y).setDuration(CLOSE_DURATION)
                                .setInterpolator(MainUI.glyph_rotation_interpolator).start();
                    return true;
                }
                case MotionEvent.ACTION_CANCEL:
                    release();
                    panel.animate().translationY(resting_translation_y).setDuration(CLOSE_DURATION)
                            .setInterpolator(MainUI.glyph_rotation_interpolator).start();
                    return true;
                default:
                    return true;
            }
        }

        private void release() {
            dragging = false;
            if( velocity_tracker != null ) {
                velocity_tracker.recycle();
                velocity_tracker = null;
            }
        }
    }
}
