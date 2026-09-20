package net.sourceforge.opencamera.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
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
    /** In landscape the tabs and the "…" end here (44 dp from the outer edge, like the last
     *  option button); the button cells end at the strip's outer edge (40 dp). */
    private static final float LANDSCAPE_CONTENT_OUT = 4.0f;
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

    private static final int TITLE_SP = 24, TAB_SP = 16, LABEL_SP = 14, VALUE_SP = 20;
    private static final int OPEN_DURATION = 250, CLOSE_DURATION = 200;

    /** Option labels of the timer row and the values they write (as the old popup does). */
    private static final String [] TIMER_VALUES = {"0", "3", "5", "10"};
    private static final float RATIO_TOLERANCE = 0.02f;
    /** Largest picture size that still counts as the "12 MP" class of the resolution row. */
    private static final float SMALL_MP_CLASS = 13.0f;

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
        buildContent();
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
        int strip_x, strip_width;
        int handle_left, handle_top, handle_w, handle_h;
    }

    private Metrics metrics() {
        Metrics m = new Metrics();
        int screen_w = container.getWidth();
        if( screen_w <= 0 )
            screen_w = main_activity.getResources().getDisplayMetrics().widthPixels;
        boolean landscape = ui_rotation == 90 || ui_rotation == 270;
        if( landscape ) {
            m.panel_w = dp(PANEL_DEPTH_LANDSCAPE);
            m.panel_h = screen_w;
            m.strip_x = dp(LANDSCAPE_STRIP_IN);
            m.strip_width = dp(LANDSCAPE_STRIP_WIDTH);
            m.label_x = m.strip_x + dp(TITLE_INSET);
            m.cells_right = m.strip_x + m.strip_width;
            m.tabs_left = m.label_x;
            m.tabs_right = m.cells_right - dp(LANDSCAPE_CONTENT_OUT);
            m.title_centre = dp(TITLE_CENTRE - LANDSCAPE_SHIFT);
            m.more_centre = dp(MORE_CENTRE - LANDSCAPE_SHIFT);
            m.tabs_top = dp(TABS_TOP - LANDSCAPE_SHIFT);
            m.rows_first_centre = dp(ROW_FIRST_CENTRE - LANDSCAPE_SHIFT);
            m.handle_left = dp(HANDLE_INSET);
            m.handle_top = m.panel_h / 2 - dp(HANDLE_LONG) / 2;
            m.handle_w = dp(HANDLE_THICK);
            m.handle_h = dp(HANDLE_LONG);
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
            m.title_centre = dp(TITLE_CENTRE);
            m.more_centre = dp(MORE_CENTRE);
            m.tabs_top = dp(TABS_TOP);
            m.rows_first_centre = dp(ROW_FIRST_CENTRE);
            m.handle_left = screen_w / 2 - dp(HANDLE_LONG) / 2;
            m.handle_top = dp(HANDLE_INSET);
            m.handle_w = dp(HANDLE_LONG);
            m.handle_h = dp(HANDLE_THICK);
        }
        return m;
    }

    /** Sizes and turns the panel, and puts its centre onto the physical bottom edge. The
     *  translations are in the window's axes, hence the plain centre arithmetic. */
    private void place() {
        Metrics m = metrics();
        placed_landscape = ui_rotation == 90 || ui_rotation == 270;
        ViewGroup.LayoutParams params = panel.getLayoutParams();
        params.width = m.panel_w;
        params.height = m.panel_h;
        panel.setLayoutParams(params);
        panel.setRotation(ui_rotation);
        int screen_w = container.getWidth() > 0 ? container.getWidth()
                : main_activity.getResources().getDisplayMetrics().widthPixels;
        int screen_h = container.getHeight() > 0 ? container.getHeight()
                : main_activity.getResources().getDisplayMetrics().heightPixels;
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

    /** Rounded corners on the inboard edge: local y = 0 in portrait (the top), local x = 0 in
     *  landscape (the left) - both are the edge towards the viewfinder (Kickoff 2.2/2.3). */
    private GradientDrawable panelBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_panel));
        float r = dp(CORNER_RADIUS);
        if( placed_landscape )
            background.setCornerRadii(new float [] {r, r, 0, 0, 0, 0, r, r});
        else
            background.setCornerRadii(new float [] {r, r, r, r, 0, 0, 0, 0});
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
        int more_right = placed_landscape ? m.cells_right - dp(LANDSCAPE_CONTENT_OUT) : m.cells_right;
        addChild(more, more_right - dp(MORE_WIDTH), m.more_centre - dp(MORE_HEIGHT) / 2, dp(MORE_WIDTH), dp(MORE_HEIGHT));

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

        // rows
        List<Row> rows = buildRows();
        for(int i=0;i<rows.size();i++)
            createRow(rows.get(i), m.rows_first_centre + i * dp(ROW_RASTER), m);
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
        label_params.leftMargin = dp(TITLE_INSET);
        // the label's centre sits 10 dp above, the value's 13 dp below the row's centre
        label_params.topMargin = dp(ROW_RASTER) / 2 - dp(LABEL_UP) - dp(LABEL_BOX) / 2;
        strip.addView(label, label_params);

        TextView value = new TextView(main_activity);
        value.setText(row.value);
        value.setTextSize(VALUE_SP);
        value.setTextColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_value));
        value.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams value_params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(VALUE_BOX));
        value_params.leftMargin = dp(TITLE_INSET);
        value_params.topMargin = dp(ROW_RASTER) / 2 + dp(VALUE_DOWN) - dp(VALUE_BOX) / 2;
        strip.addView(value, value_params);

        // the buttons sit in a 48 dp raster that ends at the right edge of the content
        int cells_right = m.cells_right - m.strip_x;
        int count = row.options.size();
        for(int i=0;i<count;i++) {
            Option option = row.options.get(i);
            int cell_left = cells_right - (count - i) * dp(OPTION_RASTER);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(OPTION_SIZE), dp(OPTION_SIZE));
            params.leftMargin = cell_left + dp(OPTION_PAD);
            params.topMargin = dp(ROW_RASTER) / 2 - dp(OPTION_SIZE) / 2;
            strip.addView(createOptionButton(option), params);
        }

        addChild(strip, m.strip_x, centre_y - dp(ROW_RASTER) / 2, m.strip_width, dp(ROW_RASTER));
    }

    private View createOptionButton(final Option option) {
        int glyph_color = ContextCompat.getColor(main_activity, option.selected ?
                R.color.settings_sheet_button_glyph_selected : R.color.settings_sheet_button_glyph);
        FrameLayout button = new FrameLayout(main_activity);
        button.setBackgroundResource(option.selected ? R.drawable.settings_sheet_button_selected : R.drawable.settings_sheet_button);
        button.setContentDescription(option.label);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( MyDebug.LOG )
                    Log.d(TAG, "option: " + option.label);
                option.action.run();
            }
        });
        View glyph;
        if( option.glyph == null ) {
            ImageView symbol = new ImageView(main_activity);
            symbol.setImageResource(option.symbol);
            symbol.setImageTintList(ColorStateList.valueOf(glyph_color));
            glyph = symbol;
        }
        else {
            GlyphView drawn = new GlyphView(option.glyph, option.text, option.ratio, glyph_color);
            glyph = drawn;
        }
        button.addView(glyph, new FrameLayout.LayoutParams(dp(SYMBOL_SIZE), dp(SYMBOL_SIZE), Gravity.CENTER));
        return button;
    }

    /* ---- rows of the photo sheet (Kickoff 3.3/3.4) ---------------------------------------- */

    private List<Row> buildRows() {
        if( is_video )
            return selected_tab == 0 ? videoGeneralRows() : videoMoreRows();
        return selected_tab == 0 ? photoGeneralRows() : photoProRows();
    }

    private List<Row> photoGeneralRows() {
        List<Row> rows = new ArrayList<>();
        Preview preview = main_activity.getPreview();

        // more light: one button per light position this camera offers (the front camera has its
        // own frontscreen values)
        {
            List<String> supported = preview.getSupportedFlashValues();
            String [][] values = {
                    {"flash_off"},
                    {"flash_auto", "flash_frontscreen_auto"},
                    {"flash_on", "flash_frontscreen_on"},
            };
            int [] labels = {R.string.settings_sheet_none, R.string.settings_sheet_auto, R.string.settings_sheet_flash};
            int [] icons = {R.drawable.ms_block, R.drawable.ms_flash_auto, R.drawable.ms_flash_on};
            String current = preview.getCurrentFlashValue();
            List<Option> options = new ArrayList<>();
            for(int i=0;i<values.length;i++) {
                final String value = firstSupported(supported, values[i]);
                if( value == null )
                    continue;
                final boolean selected = value.equals(current);
                options.add(new Option(getString(labels[i]), icons[i], null, null, 0.0f, selected,
                        new Runnable() {
                            @Override
                            public void run() {
                                preview.updateFlash(value);
                                MainUI mainUI = main_activity.getMainUI();
                                mainUI.setPopupIcon();
                                mainUI.destroyPopup();
                                refresh();
                            }
                        }));
            }
            if( options.size() >= 2 )
                rows.add(new Row(getString(R.string.settings_sheet_more_light), selectedLabelOr(options, current), options));
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
                                refresh();
                            }
                        }));
            }
            rows.add(new Row(getString(R.string.settings_sheet_timer),
                    selectedLabelOr(options, current == null ? null : getString(R.string.settings_sheet_seconds, current)), options));
        }

        // macro focus
        {
            List<String> focus_values = preview.getSupportedFocusValues();
            if( focus_values != null && focus_values.contains("focus_mode_macro") ) {
                boolean on = "focus_mode_macro".equals(preview.getCurrentFocusValue());
                String label_off = getString(R.string.settings_sheet_off);
                String label_on = getString(R.string.settings_sheet_on);
                List<Option> options = new ArrayList<>();
                options.add(new Option(label_off, R.drawable.ms_macro_off, null, null, 0.0f, !on,
                        new Runnable() {
                            @Override
                            public void run() {
                                main_activity.getPreview().updateFocus("focus_mode_continuous_picture", false, true);
                                main_activity.getMainUI().destroyPopup();
                                refresh();
                            }
                        }));
                options.add(new Option(label_on, R.drawable.ms_local_florist, null, null, 0.0f, on,
                        new Runnable() {
                            @Override
                            public void run() {
                                main_activity.getPreview().updateFocus("focus_mode_macro", false, true);
                                main_activity.getMainUI().destroyPopup();
                                refresh();
                            }
                        }));
                rows.add(new Row(getString(R.string.settings_sheet_macro), on ? label_on : label_off, options));
            }
        }

        // aspect ratio - not in panorama mode
        if( main_activity.getApplicationInterface().getPhotoMode() != MyApplicationInterface.PhotoMode.Panorama ) {
            CameraController.Size current = preview.getCurrentPictureSize();
            List<CameraController.Size> sizes = preview.getSupportedPictureSizes(true);
            if( current != null && sizes != null && !sizes.isEmpty() ) {
                float current_ratio = ratio(current);
                List<Option> options = new ArrayList<>();
                final float [] ratios = {16.0f / 9.0f, 4.0f / 3.0f};
                int [] labels = {R.string.settings_sheet_aspect_wide, R.string.settings_sheet_aspect_full};
                String [] texts = {"16:9", "4:3"};
                for(int i=0;i<ratios.length;i++) {
                    final float ratio = ratios[i];
                    options.add(new Option(getString(labels[i]), 0, Glyph.ASPECT, null, ratio,
                            Math.abs(current_ratio - ratio) <= RATIO_TOLERANCE,
                            new Runnable() {
                                @Override
                                public void run() {
                                    CameraController.Size size = largestSize(main_activity.getPreview().getSupportedPictureSizes(true), ratio, aspectLimit());
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

    private List<Row> photoProRows() {
        List<Row> rows = new ArrayList<>();
        Preview preview = main_activity.getPreview();
        final MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();

        // resolution: two classes - the largest size up to 13 MP, and the largest size at all
        {
            CameraController.Size current = preview.getCurrentPictureSize();
            List<CameraController.Size> sizes = preview.getSupportedPictureSizes(true);
            // Kickoff 3.4: the resolution list the camera reports, for the first device test
            // (Falk suspects the 50 MP size is reserved for the Google app)
            if( MyDebug.LOG )
                Log.d(TAG, "supported picture sizes: " + sizes);
            if( current != null && sizes != null && !sizes.isEmpty() ) {
                float ratio = ratio(current);
                CameraController.Size max_size = largestSize(sizes, ratio, Float.MAX_VALUE);
                CameraController.Size small_size = largestSize(sizes, ratio, SMALL_MP_CLASS);
                if( max_size != null && small_size != null && mp(max_size) > SMALL_MP_CLASS ) {
                    boolean small_selected = mp(current) <= SMALL_MP_CLASS;
                    List<Option> options = new ArrayList<>();
                    options.add(resolutionOption(small_size, small_selected));
                    options.add(resolutionOption(max_size, !small_selected));
                    rows.add(new Row(getString(R.string.settings_sheet_resolution),
                            getString(R.string.settings_sheet_mp, String.valueOf(Math.round(mp(current)))), options));
                }
            }
        }

        // RAW/JPEG
        if( preview.supportsRaw() ) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            boolean raw = "preference_raw_yes".equals(sharedPreferences.getString(PreferenceKeys.RawPreferenceKey, "preference_raw_no"));
            List<Option> options = new ArrayList<>();
            options.add(new Option(getString(R.string.settings_sheet_raw_off), 0, Glyph.RAW, "RAW", 0.0f, !raw,
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

        // lens selection - only for the rear camera, which is the one with physical lenses
        {
            List<MainActivity.Lens> lenses = main_activity.getManualLenses();
            if( preview.hasPhysicalCameras() && lenses != null && lenses.size() >= 2 ) {
                final boolean manual = applicationInterface.getCameraIdSPhysicalPref() != null;
                List<Option> options = new ArrayList<>();
                options.add(new Option(getString(R.string.settings_sheet_auto), R.drawable.ms_camera, null, null, 0.0f, !manual,
                        new Runnable() {
                            @Override
                            public void run() {
                                if( main_activity.getApplicationInterface().getCameraIdSPhysicalPref() != null )
                                    main_activity.userSwitchToCamera(main_activity.getActualCameraId(), null);
                                refresh();
                            }
                        }));
                options.add(new Option(getString(R.string.settings_sheet_manual), 0, Glyph.LENS_AUTO, "A", 0.0f, manual,
                        new Runnable() {
                            @Override
                            public void run() {
                                // start on the main lens, see MainActivity.getMainLensId()
                                main_activity.switchToPhysicalLens(main_activity.getMainLensId());
                                refresh();
                            }
                        }));
                rows.add(new Row(getString(R.string.settings_sheet_lens),
                        getString(manual ? R.string.settings_sheet_manual : R.string.settings_sheet_auto), options));
            }
        }

        return rows;
    }

    /* ---- rows of the video sheet (Kickoff 3.5) -------------------------------------------- */

    private List<Row> videoGeneralRows() {
        List<Row> rows = new ArrayList<>();
        Preview preview = main_activity.getPreview();
        final MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        final String fps_value = applicationInterface.getVideoFPSPref();

        // flash: only the values that make sense while recording
        {
            List<String> supported = new ArrayList<>();
            List<String> all = preview.getSupportedFlashValues();
            if( all != null ) {
                for(String value : all) {
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
                                main_activity.getPreview().updateFlash(value);
                                MainUI mainUI = main_activity.getMainUI();
                                mainUI.setPopupIcon();
                                mainUI.destroyPopup();
                                refresh();
                            }
                        }));
            }
            if( options.size() >= 2 )
                rows.add(new Row(getString(R.string.settings_sheet_flash), selectedLabelOr(options, current), options));
        }

        // video resolution: FHD / 4K / 8K where the current frame rate offers them
        {
            List<String> qualities = preview.getSupportedVideoQuality(fps_value);
            if( qualities == null || qualities.isEmpty() )
                qualities = preview.getVideoQualityHander().getSupportedVideoQuality();
            // Kickoff 3.4/3.5: the video quality and frame rate lists the camera reports
            if( MyDebug.LOG )
                Log.d(TAG, "supported video qualities: " + qualities);
            List<Option> options = new ArrayList<>();
            int [] widths = {1920, 3840, 7680};
            // the labels are the same on screen and in the content description, so they stay
            // untranslated (Kickoff 3.5)
            String [] labels = {"FHD", "4K", "8K"};
            String current = preview.getVideoQualityHander().getCurrentVideoQuality();
            for(int i=0;i<widths.length;i++) {
                final String quality = findQuality(qualities, widths[i]);
                if( quality == null )
                    continue;
                options.add(new Option(labels[i], 0, Glyph.TEXT, labels[i], 0.0f, quality.equals(current),
                        new Runnable() {
                            @Override
                            public void run() {
                                setVideoQuality(quality);
                            }
                        }));
            }
            if( !options.isEmpty() )
                rows.add(new Row(getString(R.string.settings_sheet_resolution),
                        selectedLabelOr(options, current == null ? null : preview.getCamcorderProfileDescriptionShort(current)), options));
        }

        // frame rate - the same candidates the settings list offers
        {
            int [] candidates = {24, 30, 60};
            List<Option> options = new ArrayList<>();
            for(int i=0;i<candidates.length;i++) {
                final String value = String.valueOf(candidates[i]);
                if( !preview.fpsIsHighSpeed(value) && !preview.getVideoQualityHander().videoSupportsFrameRate(candidates[i]) )
                    continue;
                options.add(new Option(value, 0, Glyph.TEXT, value, 0.0f, value.equals(fps_value),
                        new Runnable() {
                            @Override
                            public void run() {
                                setVideoFPS(value);
                            }
                        }));
            }
            CharSequence value_text = selectedLabelOr(options, fps_value);
            if( value_text == null && fps_value != null )
                value_text = fps_value.equals("default") ? getString(R.string.settings_sheet_auto) : fps_value;
            if( !options.isEmpty() )
                rows.add(new Row(getString(R.string.settings_sheet_fps), value_text, options));
        }

        // macro focus, as in photo mode but with the video focus mode
        {
            List<String> focus_values = preview.getSupportedFocusValues();
            if( focus_values != null && focus_values.contains("focus_mode_macro") ) {
                boolean on = "focus_mode_macro".equals(preview.getCurrentFocusValue());
                String label_off = getString(R.string.settings_sheet_off);
                String label_on = getString(R.string.settings_sheet_on);
                List<Option> options = new ArrayList<>();
                options.add(new Option(label_off, R.drawable.ms_macro_off, null, null, 0.0f, !on,
                        new Runnable() {
                            @Override
                            public void run() {
                                main_activity.getPreview().updateFocus("focus_mode_continuous_video", false, true);
                                main_activity.getMainUI().destroyPopup();
                                refresh();
                            }
                        }));
                options.add(new Option(label_on, R.drawable.ms_local_florist, null, null, 0.0f, on,
                        new Runnable() {
                            @Override
                            public void run() {
                                main_activity.getPreview().updateFocus("focus_mode_macro", false, true);
                                main_activity.getMainUI().destroyPopup();
                                refresh();
                            }
                        }));
                rows.add(new Row(getString(R.string.settings_sheet_macro), on ? label_on : label_off, options));
            }
        }

        return rows;
    }

    private List<Row> videoMoreRows() {
        List<Row> rows = new ArrayList<>();
        Preview preview = main_activity.getPreview();
        if( preview.supportsVideoStabilization() ) {
            boolean on = PreferenceManager.getDefaultSharedPreferences(main_activity)
                    .getBoolean(PreferenceKeys.VideoStabilizationPreferenceKey, false);
            String label_off = getString(R.string.settings_sheet_off);
            String label_on = getString(R.string.settings_sheet_on);
            List<Option> options = new ArrayList<>();
            options.add(new Option(label_off, R.drawable.ms_video_stable, null, null, 0.0f, !on,
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

    private static String firstSupported(List<String> supported, String [] values) {
        if( supported == null )
            return null;
        for(String value : values) {
            if( supported.contains(value) )
                return value;
        }
        return null;
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

    private static float mp(CameraController.Size size) {
        return size.width * (float)size.height / 1000000.0f;
    }

    /** The largest size of the given ratio that stays within the megapixel limit. */
    private static CameraController.Size largestSize(List<CameraController.Size> sizes, float ratio, float mp_limit) {
        CameraController.Size best = null;
        for(CameraController.Size size : sizes) {
            if( Math.abs(ratio(size) - ratio) > RATIO_TOLERANCE )
                continue;
            if( mp(size) > mp_limit )
                continue;
            if( best == null || mp(size) > mp(best) )
                best = size;
        }
        return best;
    }

    /** The megapixel class the aspect row has to stay in: the class of the current resolution
     *  as the Pro tab defines it (Kickoff 3.3/3.4). */
    private float aspectLimit() {
        List<CameraController.Size> sizes = main_activity.getPreview().getSupportedPictureSizes(true);
        CameraController.Size current = main_activity.getPreview().getCurrentPictureSize();
        if( current == null || sizes == null || sizes.isEmpty() )
            return Float.MAX_VALUE;
        CameraController.Size max_size = largestSize(sizes, ratio(current), Float.MAX_VALUE);
        if( max_size == null || mp(max_size) <= SMALL_MP_CLASS )
            return Float.MAX_VALUE;  // no Pro choice for this camera
        return mp(current) <= SMALL_MP_CLASS ? SMALL_MP_CLASS : Float.MAX_VALUE;
    }

    /** The quality string with the given video width, or null if this camera doesn't offer it. */
    private String findQuality(List<String> qualities, int width) {
        if( qualities == null )
            return null;
        for(String quality : qualities) {
            CamcorderProfile profile = main_activity.getPreview().getCamcorderProfile(quality);
            if( profile != null && profile.videoFrameWidth == width )
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
    private enum Glyph { ASPECT, MP, RAW, LENS_AUTO, TEXT }

    private class GlyphView extends View {
        private final Glyph glyph;
        private final String text;
        private final float ratio;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        GlyphView(Glyph glyph, String text, float ratio, int color) {
            super(main_activity);
            this.glyph = glyph;
            this.text = text;
            this.ratio = ratio;
            paint.setColor(color);
            paint.setTextAlign(Paint.Align.CENTER);
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
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(7.0f));
                    canvas.drawText(text, w / 2, h * 0.46f, paint);
                    paint.setTextSize(sp(5.0f));
                    canvas.drawText("MP", w / 2, h * 0.92f, paint);
                    break;
                }
                case RAW: {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(6.0f));
                    canvas.drawText(text, w / 2, h * 0.68f, paint);
                    break;
                }
                case LENS_AUTO: {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(1.5f));
                    canvas.drawCircle(w / 2, h / 2, dp(10.0f), paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(6.5f));
                    canvas.drawText(text, w / 2, h * 0.68f, paint);
                    break;
                }
                case TEXT: {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    paint.setTextSize(sp(8.0f));
                    canvas.drawText(text, w / 2, h * 0.68f, paint);
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
