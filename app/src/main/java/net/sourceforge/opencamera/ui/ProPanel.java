package net.sourceforge.opencamera.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.R;
import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.preview.ApplicationInterface;
import net.sourceforge.opencamera.preview.Preview;

import java.util.ArrayList;
import java.util.List;

/** Das Pro-Panel am rechten Dock-Button (K13, Kickoff redesign-pro-panel.md 3).
 *
 *  Kein zweites Blatt: die Pille schwebt ueber dem Sucher (an der Stelle der Zoom-Pille), die
 *  Reiter-Reihe sitzt unten an der Stelle der Modus-Leiste. Beides blendet die App waehrend des
 *  offenen Panels aus; Auslöser und Dock-Zeile bleiben.
 *
 *  Zwei Dinge sind bewusst so gebaut:
 *  - Der Regler des aktiven Reiters ist einer der **vorhandenen** SeekBars aus
 *    activity_main.xml. Er wird beim Reiterwechsel aus seinem eigenen Container in die Pille
 *    umgehaengt und beim Schliessen zurueckgesetzt - so bleiben Skalierung, Zuordnung und
 *    Zuhoerer aus MainActivity.cameraSetup() die alten (Kickoff 3: wiederverwenden, nicht
 *    nachbauen). Ausnahme: der Fokus-Regler. R.id.focus_seekbar wird von DrawPreview in jedem
 *    Bild vermessen und verschoben, ein Umhaengen wuerde dagegen ankämpfen; deshalb bringt das
 *    Panel fuer den Fokus einen eigenen SeekBar mit demselben Verhalten mit.
 *  - Ein Reiter ist ein Modus: beim Wechsel in einen Reiter wird der zugehoerige Parameter
 *    manuell gestellt (bzw. beim Reiter Belichtung wieder auf AE), weil die Kamera die
 *    Einstellungen sonst gar nicht anwendet. Verlassen des Reiters laesst den Wert stehen
 *    (Falk, 2026-09-21); zurueck auf Automatik geht es nur ueber den runden Knopf in der Pille
 *    und ueber "Alle zuruecksetzen".
 *
 *  Maße aus docs/design-reference/pixel-pro-panel-beschreibung.md (2,25 px/dp).
 */
public class ProPanel {
    private static final String TAG = "ProPanel";

    /* ---- Reiter, in der Reihenfolge der Referenz (Beschreibung 4) ------------------------- */

    private static final int TAB_RESET = 0;
    private static final int TAB_EXPOSURE = 1;
    private static final int TAB_WB = 2;
    private static final int TAB_FOCUS = 3;
    private static final int TAB_SHUTTER = 4;
    private static final int TAB_ISO = 5;
    private static final int TAB_PEAKING = 6;
    private static final int N_TABS = 7;

    /** Mit diesem Reiter oeffnet das Panel ("Helligkeit" der Referenz). */
    private static final int OPEN_TAB = TAB_EXPOSURE;

    private static final float TAB_SIZE = 56.0f;        // Kachel, gemessen
    private static final float TAB_GAP = 20.0f;
    /** Die Beschriftung darf breiter sein als die Kachel: in der Referenz steht "Weißabgleich"
     *  in einer Zeile unter einer schmaleren Kachel. */
    private static final float TAB_LABEL_WIDTH = 88.0f;
    private static final float TAB_ROW_PADDING = 16.0f;
    private static final float TAB_RADIUS = 16.0f;
    private static final float TAB_ICON_SIZE = 24.0f;
    private static final float DIVIDER_WIDTH = 2.0f, DIVIDER_HEIGHT = 28.0f;
    private static final int TAB_SP = 11;  // kleiner als die Referenzschrift: die deutschen
                                           // Beschriftungen brauchen sonst drei Zeilen
    private static final int BADGE_POLL_MS = 150;
    /** Zwischen zwei Zahlen des Massbands liegen mindestens so viele Striche (8 dp je Strich). */
    private static final int MIN_LABEL_STEPS = 5;
    /** Und hoechstens so viele - sonst stuende auf einer Bildschirmbreite keine Zahl. */
    private static final int MAX_LABEL_STEPS = 12;
    private static final float HANDLE_HEIGHT = 3.0f;  // wie das Griffstueck des Einstellungen-Blatts
    private static final float CLOSE_SWIPE = 16.0f;   // so weit nach unten wischen schliesst

    private final MainActivity main_activity;

    private final FrameLayout pill;
    private final ImageView end_minus_icon, end_plus_icon;
    private final View end_minus, end_plus;
    private final FrameLayout slider_holder;
    private final ImageButton auto_button;
    private final TextView badge;
    private final View sheet, handle;
    private final HorizontalScrollView tabs_scroll;
    private final LinearLayout tabs_row;

    /** Das Massband unter dem Regler (zeichnen tut es, bedient wird der SeekBar darueber). */
    private final RulerSlider ruler;

    /** Der eigene Fokus-Regler (siehe Klassenkommentar). */
    private final SeekBar focus_seekbar;

    private boolean open;
    private int selected_tab = OPEN_TAB;

    /* ---- der umgehaengte Regler ----------------------------------------------------------- */

    private View hosted_view;
    private ViewGroup hosted_parent;
    private int hosted_index;
    private ViewGroup.LayoutParams hosted_params;

    /* ---- was das Panel waehrend des Offenseins ausblendet --------------------------------- */

    private final List<View> hidden_views = new ArrayList<>();
    private final List<Integer> hidden_visibilities = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private String badge_text;
    /** Der Reiter, fuer den das Massband zuletzt beschriftet wurde. */
    private int ruler_labels_tab = -1;

    /** Der Regler des eigenen Fokus-SeekBars - dasselbe Verhalten wie der Fokus-Regler der App
     *  (MainActivity.setManualFocusSeekbar()), nur auf einer eigenen View. */
    private final SeekBar.OnSeekBarChangeListener focus_listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Preview preview = main_activity.getPreview();
            if( preview == null || preview.getCameraController() == null )
                return;
            double frac = progress / (double) seekBar.getMax();
            float focus_distance = (float) (ManualSeekbars.seekbarScaling(frac) * preview.getMinimumFocusDistance());
            preview.setFocusDistance(focus_distance, false, true);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Preview preview = main_activity.getPreview();
            if( preview != null )
                preview.stoppedSettingFocusDistance(false);
        }
    };

    private final Runnable badge_poll = new Runnable() {
        @Override
        public void run() {
            if( !open )
                return;
            updateBadge();
            updateRuler();
            handler.postDelayed(this, BADGE_POLL_MS);
        }
    };

    public ProPanel(MainActivity main_activity) {
        this.main_activity = main_activity;
        this.pill = main_activity.findViewById(R.id.pro_panel_pill);
        this.end_minus = main_activity.findViewById(R.id.pro_panel_minus);
        this.end_plus = main_activity.findViewById(R.id.pro_panel_plus);
        this.end_minus_icon = main_activity.findViewById(R.id.pro_panel_minus_icon);
        this.end_plus_icon = main_activity.findViewById(R.id.pro_panel_plus_icon);
        this.slider_holder = main_activity.findViewById(R.id.pro_panel_slider_holder);
        this.auto_button = main_activity.findViewById(R.id.pro_panel_auto);
        this.badge = main_activity.findViewById(R.id.pro_panel_badge);
        this.sheet = main_activity.findViewById(R.id.pro_panel_sheet);
        this.handle = main_activity.findViewById(R.id.pro_panel_handle);
        this.tabs_scroll = main_activity.findViewById(R.id.pro_panel_tabs_scroll);
        this.tabs_row = main_activity.findViewById(R.id.pro_panel_tabs);

        // das Massband liegt unter dem Regler; der Regler selbst ist der SeekBar der App, der
        // beim Reiterwechsel hier hineinwandert (showSlider)
        this.ruler = new RulerSlider(main_activity);
        ruler.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        ruler.setVisibility(View.GONE);
        slider_holder.addView(ruler);

        // Griffstueck wie am Einstellungen-Blatt: abgerundeter Balken, Wischen nach unten
        // schliesst das Panel (Falk 2026-09-21)
        GradientDrawable handle_background = new GradientDrawable();
        handle_background.setColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_handle));
        handle_background.setCornerRadius(dp(HANDLE_HEIGHT) / 2.0f);
        handle.setBackground(handle_background);
        // das Griffstueck ist nur 3 dp hoch - die Geste haengt deshalb auch am Band darunter,
        // sonst muesste man den Balken genau treffen
        HandleTouchListener close_swipe = new HandleTouchListener();
        handle.setOnTouchListener(close_swipe);
        sheet.setOnTouchListener(close_swipe);

        this.focus_seekbar = new SeekBar(main_activity);
        focus_seekbar.setContentDescription(main_activity.getResources().getString(R.string.pro_panel_focus));
        focus_seekbar.setOnSeekBarChangeListener(focus_listener);

        auto_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedAuto();
            }
        });
        end_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepActiveSlider(-1);
            }
        });
        end_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepActiveSlider(1);
            }
        });

        buildTabs();
    }

    /* ---- Reiter-Reihe --------------------------------------------------------------------- */

    private void buildTabs() {
        int size = dp(TAB_SIZE);
        int label_width = dp(TAB_LABEL_WIDTH);
        int padding = dp(TAB_ROW_PADDING);
        tabs_row.setPadding(padding, 0, padding, 0);
        tabs_row.removeAllViews();
        for(int tab = 0; tab < N_TABS; tab++) {
            if( tab == TAB_EXPOSURE ) {
                // Trennstrich zwischen "Alle zuruecksetzen" und dem ersten Regler-Reiter
                View divider = new View(main_activity);
                divider.setBackgroundColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_outline));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(DIVIDER_WIDTH), dp(DIVIDER_HEIGHT));
                lp.gravity = Gravity.CENTER_VERTICAL;
                lp.setMarginEnd(dp(TAB_GAP));
                divider.setLayoutParams(lp);
                tabs_row.addView(divider);
            }
            // der letzte Eintrag (TAB_PEAKING) ist kein Reiter, sondern ein Schalter - siehe
            // clickedTab()
            LinearLayout entry = new LinearLayout(main_activity);
            entry.setOrientation(LinearLayout.VERTICAL);
            entry.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams entry_lp = new LinearLayout.LayoutParams(label_width, ViewGroup.LayoutParams.WRAP_CONTENT);
            entry_lp.setMarginEnd(dp(TAB_GAP));
            entry.setLayoutParams(entry_lp);

            ImageView glyph = new ImageView(main_activity);
            LinearLayout.LayoutParams glyph_lp = new LinearLayout.LayoutParams(size, size);
            glyph.setLayoutParams(glyph_lp);
            glyph.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int pad = (size - dp(TAB_ICON_SIZE)) / 2;
            glyph.setPadding(pad, pad, pad, pad);
            glyph.setImageResource(tabIcon(tab));
            glyph.setContentDescription(tabLabel(tab));
            entry.addView(glyph);

            TextView label = new TextView(main_activity);
            LinearLayout.LayoutParams label_lp = new LinearLayout.LayoutParams(label_width, ViewGroup.LayoutParams.WRAP_CONTENT);
            label.setLayoutParams(label_lp);
            label.setGravity(Gravity.CENTER_HORIZONTAL);
            label.setTextSize(TAB_SP);
            label.setTextColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_tab_inactive_text));
            label.setText(tabLabel(tab));
            entry.addView(label);

            final int tab_index = tab;
            entry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickedTab(tab_index);
                }
            });

            tabs_row.addView(entry);
        }
    }

    private int tabIcon(int tab) {
        switch( tab ) {
            case TAB_RESET:
                return R.drawable.ms_restart_alt;
            case TAB_EXPOSURE:
                return R.drawable.ic_exposure_white_48dp;
            case TAB_WB:
                return R.drawable.ic_colorize_white_48dp;
            case TAB_FOCUS:
                return R.drawable.ms_center_focus_strong;
            case TAB_SHUTTER:
                return R.drawable.baseline_shutter_speed_white_48;
            case TAB_ISO:
                return R.drawable.iso_icon;
            default:
                return R.drawable.ms_lightbulb;
        }
    }

    private String tabLabel(int tab) {
        switch( tab ) {
            case TAB_RESET:
                return main_activity.getResources().getString(R.string.pro_panel_reset);
            case TAB_EXPOSURE:
                return main_activity.getResources().getString(R.string.exposure);
            case TAB_WB:
                return main_activity.getResources().getString(R.string.white_balance);
            case TAB_FOCUS:
                return main_activity.getResources().getString(R.string.pro_panel_focus);
            case TAB_SHUTTER:
                return main_activity.getResources().getString(R.string.exposure_shutter_speed);
            case TAB_ISO:
                return main_activity.getResources().getString(R.string.iso);
            default:
                return main_activity.getResources().getString(R.string.preference_focus_peaking);
        }
    }

    private void clickedTab(int tab) {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedTab: " + tab);
        if( tab == TAB_RESET ) {
            resetAll();
            return;
        }
        if( tab == TAB_PEAKING ) {
            // derselbe Weg wie der Fokus-Peaking-Knopf der Icon-Leiste
            main_activity.clickedFocusPeaking(null);
            markTabs();
            return;
        }
        if( tab != selected_tab ) {
            selected_tab = tab;
            ensureModeForTab(tab);
        }
        showSlider();
        markTabs();
        updateAutoButton();
        updateBadge();
    }

    /** Setzt die Reiter-Kacheln: weisser Kreis = aktiver Reiter; das Fokus-Peaking zeigt seinen
     *  Zustand ebenfalls an (es ist kein Reiter, sondern ein Schalter). */
    private void markTabs() {
        boolean peaking = main_activity.getApplicationInterface().getFocusPeakingPref();
        for(int tab = 0; tab < N_TABS; tab++) {
            // die Eintraege liegen in Reihenfolge; der Trennstrich schiebt alles ab TAB_EXPOSURE um eins
            int index = tab + (tab >= TAB_EXPOSURE ? 1 : 0);
            if( index >= tabs_row.getChildCount() )
                continue;
            View entry = tabs_row.getChildAt(index);
            if( !(entry instanceof LinearLayout) )
                continue;
            ImageView glyph = (ImageView) ((LinearLayout) entry).getChildAt(0);
            TextView label = (TextView) ((LinearLayout) entry).getChildAt(1);
            boolean active = (tab == TAB_PEAKING) ? peaking : (tab == selected_tab);
            GradientDrawable background = new GradientDrawable();
            if( active ) {
                background.setShape(GradientDrawable.OVAL);
                background.setColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_button_selected));
            }
            else {
                background.setCornerRadius(dp(TAB_RADIUS));
                background.setColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_button));
            }
            glyph.setBackground(background);
            glyph.setColorFilter(ContextCompat.getColor(main_activity, active ?
                    R.color.settings_sheet_button_glyph_selected : R.color.settings_sheet_button_glyph));
            label.setTextColor(ContextCompat.getColor(main_activity, R.color.settings_sheet_tab_inactive_text));
        }
    }

    /* ---- Regler --------------------------------------------------------------------------- */

    /** Der SeekBar des aktiven Reiters (null bei "Alle zuruecksetzen" und Fokus-Peaking - die
     *  beiden haben keinen Regler). */
    private SeekBar activeSeekbar() {
        switch( selected_tab ) {
            case TAB_EXPOSURE:
                return main_activity.findViewById(R.id.exposure_seekbar);
            case TAB_WB:
                return main_activity.findViewById(R.id.white_balance_seekbar);
            case TAB_FOCUS:
                return focus_seekbar;
            case TAB_SHUTTER:
                return main_activity.findViewById(R.id.exposure_time_seekbar);
            case TAB_ISO:
                return main_activity.findViewById(R.id.iso_seekbar);
            default:
                return null;
        }
    }

    /** Haengt den Regler des aktiven Reiters in die Pille (und den vorigen zurueck). */
    private void showSlider() {
        Preview preview = main_activity.getPreview();
        if( preview == null )
            return;
        if( selected_tab == TAB_FOCUS )
            setFocusSeekbarProgress();
        SeekBar slider = activeSeekbar();
        hostSlider(slider);
        // links und rechts der Pille steht das Symbol des Reiters mit Minus bzw. Plus (Referenz)
        end_minus_icon.setImageResource(tabIcon(selected_tab));
        end_plus_icon.setImageResource(tabIcon(selected_tab));
        updateRuler();
    }

    private void hostSlider(View slider) {
        if( slider == hosted_view )
            return;
        restoreSlider();
        if( slider == null )
            return;
        ViewGroup parent = (slider.getParent() instanceof ViewGroup) ? (ViewGroup) slider.getParent() : null;
        if( parent != null ) {
            hosted_parent = parent;
            hosted_index = parent.indexOfChild(slider);
            hosted_params = slider.getLayoutParams();
            parent.removeView(slider);
        }
        hosted_view = slider;
        // der SeekBar ist nur noch die Bedienung: sichtbar ist das Massband darunter. Alpha 0
        // statt INVISIBLE/GONE - sonst nimmt er keine Beruehrungen mehr an.
        slider.setAlpha(0.0f);
        slider_holder.addView(slider, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void restoreSlider() {
        if( hosted_view == null )
            return;
        hosted_view.setAlpha(1.0f);
        slider_holder.removeView(hosted_view);
        if( hosted_parent != null && hosted_params != null )
            hosted_parent.addView(hosted_view, Math.min(hosted_index, hosted_parent.getChildCount()), hosted_params);
        hosted_view = null;
        hosted_parent = null;
        hosted_params = null;
    }

    /** Den Fokus-Regler auf die aktuelle Entfernung stellen (der eigene SeekBar wird von
     *  cameraSetup() nicht mitgestellt, anders als die vier anderen). */
    private void setFocusSeekbarProgress() {
        Preview preview = main_activity.getPreview();
        if( preview == null || preview.getCameraController() == null )
            return;
        focus_seekbar.setOnSeekBarChangeListener(null);
        ManualSeekbars.setProgressSeekbarScaled(focus_seekbar, 0.0, preview.getMinimumFocusDistance(),
                preview.getCameraController().getFocusDistance());
        focus_seekbar.setOnSeekBarChangeListener(focus_listener);
    }

    /* ---- Feinjustierung und Massband ------------------------------------------------------- */

    /** Ein Schritt des aktiven Reglers weiter oder zurueck: die Knoepfe links und rechts der
     *  Pille (Referenz: das Symbol des Reiters mit Minus bzw. Plus). Der Weg ist derselbe wie
     *  bei den Lautstaerketasten - die Zuhoerer der App in MainActivity.cameraSetup() wirken
     *  auch ohne Nutzer-Ereignis (fromUser), das Umhaengen in die Pille aendert daran nichts. */
    private void stepActiveSlider(int direction) {
        if( !open || direction == 0 )
            return;
        switch( selected_tab ) {
            case TAB_EXPOSURE:
                // ueberspringt die wiederholten Nullen in der Mitte des Reglers
                main_activity.changeExposure(direction);
                break;
            case TAB_WB:
                main_activity.getMainUI().changeSeekbar(R.id.white_balance_seekbar, direction);
                break;
            case TAB_FOCUS: {
                // der eigene Regler haengt nicht im Baum der Activity, also direkt stellen -
                // sein Zuhoerer wendet den Wert auch ohne fromUser an
                int progress = focus_seekbar.getProgress() + direction;
                if( progress < 0 )
                    progress = 0;
                else if( progress > focus_seekbar.getMax() )
                    progress = focus_seekbar.getMax();
                focus_seekbar.setProgress(progress);
                break;
            }
            case TAB_SHUTTER:
                main_activity.getMainUI().changeSeekbar(R.id.exposure_time_seekbar, direction);
                break;
            case TAB_ISO:
                main_activity.changeISO(direction);
                break;
        }
        // sofort nachziehen, damit Wert und Massband nicht bis zum naechsten Poll stehen bleiben
        updateRuler();
        updateBadge();
    }

    /** Das Massband auf den aktiven Regler stellen (Wert, Skala, Beschriftung). */
    private void updateRuler() {
        SeekBar seekbar = activeSeekbar();
        if( !open || seekbar == null ) {
            ruler.setVisibility(View.GONE);
            return;
        }
        int max = seekbar.getMax();
        if( ruler.getMax() != max || ruler_labels_tab != selected_tab ) {
            ruler.setMax(max);
            ruler.setLabels(buildLabels(selected_tab, max));
            ruler_labels_tab = selected_tab;
        }
        ruler.setProgress(seekbar.getProgress());
        ruler.setVisibility(View.VISIBLE);
    }

    /** Die Beschriftung des Massbands. Runde Werte bekommen ihre Zahl (die Referenz beschriftet
     *  100/200/400/800 ISO, nicht jeden fuenften Strich), und wo die runden Werte weit
     *  auseinanderliegen, fuellt ein Zwischenwert die Luecke - sonst stuende auf einem Bildschirm
     *  voller Skala keine einzige Zahl. Zwischen zwei Zahlen liegen immer mindestens
     *  {@link #MIN_LABEL_STEPS} Striche, hoechstens {@link #MAX_LABEL_STEPS}. */
    private String [] buildLabels(int tab, int max) {
        String [] labels = new String [max + 1];
        if( max <= 0 )
            return labels;
        int last = -MIN_LABEL_STEPS;
        for(int step = 0; step <= max; step++) {
            if( step - last < MIN_LABEL_STEPS )
                continue;
            String text = niceLabel(tab, step);
            if( text == null && (step - last >= MAX_LABEL_STEPS || step == max) )
                text = plainLabel(tab, step);
            if( text == null )
                continue;
            labels[step] = text;
            last = step;
        }
        return labels;
    }

    /** Die Zahl an einem runden Wert - oder null, wenn dieser Schritt keinen runden Wert hat. */
    private String niceLabel(int tab, int step) {
        switch( tab ) {
            case TAB_FOCUS: {
                double metres = focusMetres(step, focus_seekbar.getMax());
                if( metres <= 0.0 )
                    return main_activity.getResources().getString(R.string.infinite);
                if( metres >= 10.0 )
                    return null; // jenseits von 10 m steht nur noch "unendlich"
                double [] nice = {0.1, 0.15, 0.2, 0.3, 0.5, 0.7, 1.0, 1.5, 2.0, 3.0, 5.0, 7.0};
                for(double value : nice) {
                    if( Math.abs(metres - value) < value * 0.02 )
                        return formatMetres(metres);
                }
                return null;
            }
            case TAB_ISO: {
                int iso = main_activity.getManualSeekbars().getISO(step);
                return isNiceISO(iso) ? String.valueOf(iso) : null;
            }
            case TAB_SHUTTER:
                return niceExposure(main_activity.getManualSeekbars().getExposureTime(step));
            default:
                return null;
        }
    }

    /** Der Wert an einem beliebigen Schritt (die Zwischenzahl, wenn kein runder Wert in der Naehe
     *  liegt). */
    private String plainLabel(int tab, int step) {
        ManualSeekbars manualSeekbars = main_activity.getManualSeekbars();
        switch( tab ) {
            case TAB_EXPOSURE:
                // die Werte des Belichtungsreglers (1/3-EV-Schritte, in der Mitte dreimal die
                // Null); vor cameraSetup gibt es sie noch nicht
                if( !main_activity.hasExposureSeekbarValues() )
                    return null;
                return exposureText(main_activity.getExposureSeekbarValue(step));
            case TAB_WB:
                return String.valueOf(manualSeekbars.getWhiteBalanceTemperature(step));
            case TAB_FOCUS:
                return formatMetres(focusMetres(step, focus_seekbar.getMax()));
            case TAB_ISO:
                return String.valueOf(manualSeekbars.getISO(step));
            case TAB_SHUTTER:
                return exposureTimeText(manualSeekbars.getExposureTime(step));
            default:
                return null;
        }
    }

    /** Die Entfernung in Metern zu einem Schritt des Fokus-Reglers (0 = unendlich). */
    private double focusMetres(int step, int max) {
        Preview preview = main_activity.getPreview();
        if( preview == null || max <= 0 )
            return 0.0;
        // der Regler ist in Dioptrien geteilt: Wert = Brennweite * Skalierung, Meter = 1 / Wert
        double dioptres = ManualSeekbars.seekbarScaling(step / (double) max) * preview.getMinimumFocusDistance();
        return dioptres > 0.0 ? 1.0 / dioptres : 0.0;
    }

    private static String formatMetres(double metres) {
        return String.format(java.util.Locale.US, metres < 1.0 ? "%.2f" : "%.1f", metres);
    }

    /** „Belichtungskorrektur -1,00 EV" der App auf die Zahl kuerzen - die Blase und das Massband
     *  haben keinen Platz fuer den langen Text (er sprengte die Blase auf 228 dp). */
    private String exposureText(int value) {
        String text = main_activity.getPreview().getExposureCompensationString(value);
        String prefix = main_activity.getResources().getString(R.string.exposure_compensation) + " ";
        if( text.startsWith(prefix) )
            text = text.substring(prefix.length());
        if( text.endsWith(" EV") )
            text = text.substring(0, text.length() - 3);
        return text;
    }

    /** Die ISO-Werte, die eine Zahl bekommen (die Stufen der Kamera, nicht jeder Schritt). */
    private static boolean isNiceISO(int iso) {
        if( iso < 100 )
            return iso % 10 == 0;
        int value = 100;
        while( value < iso )
            value *= 2;
        return value == iso || iso % 1000 == 0;
    }

    /** Die Verschlusszeiten, die eine Zahl bekommen: die Stufen der Kamera. */
    private static String niceExposure(long exposure_ns) {
        if( exposure_ns <= 0 )
            return null;
        if( exposure_ns < 1000000000L ) {
            long denominator = Math.round(1000000000.0 / exposure_ns);
            return isNiceDenominator(denominator) ? String.valueOf(denominator) : null;
        }
        long seconds = Math.round(exposure_ns / 1000000000.0);
        return (seconds * 1000000000L == exposure_ns) ? String.valueOf(seconds) : null;
    }

    private static boolean isNiceDenominator(long denominator) {
        long value = 1;
        while( value < denominator )
            value *= 2;
        if( value == denominator )
            return true;
        return denominator == 15 || denominator == 30 || denominator == 60 || denominator == 125
                || denominator == 250 || denominator == 500;
    }

    /** Eine Verschlusszeit als kurze Zahl: der Nenner bei kurzen Zeiten (250 = 1/250 s),
     *  sonst die Sekunden - so schreibt es die Referenz. */
    private static String exposureTimeText(long exposure_ns) {
        if( exposure_ns <= 0 )
            return null;
        if( exposure_ns < 1000000000L )
            return String.valueOf(Math.round(1000000000.0 / exposure_ns));
        return String.format(java.util.Locale.US, "%.1f", exposure_ns / 1000000000.0);
    }

    /** Wischen nach unten am Griffstueck schliesst das Panel - dieselbe Geste wie am
     *  Einstellungen-Blatt (SettingsSheet.PanelTouchListener), hier ohne Mitwandern. */
    private class HandleTouchListener implements View.OnTouchListener {
        private float down_raw_y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch( event.getActionMasked() ) {
                case MotionEvent.ACTION_DOWN:
                    down_raw_y = event.getRawY();
                    return true;
                case MotionEvent.ACTION_UP:
                    if( open && event.getRawY() - down_raw_y > dp(CLOSE_SWIPE) )
                        close();
                    return true;
                default:
                    return true;
            }
        }
    }

    /* ---- Modus des Reiters ---------------------------------------------------------------- */

    /** Stellt den Parameter des Reiters manuell (bzw. beim Reiter Belichtung wieder auf AE).
     *  Ohne das wendet die Kamera den Regler nicht an; die Umschaltung ist genau die, die auch
     *  das ISO-Fenster und das Weissabgleich-Fenster der App machen (updateForSettings). */
    private void ensureModeForTab(int tab) {
        Preview preview = main_activity.getPreview();
        ApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        if( preview == null || preview.getCameraController() == null )
            return;
        boolean changed = false;
        switch( tab ) {
            case TAB_EXPOSURE:
                // Belichtungskorrektur wirkt nur mit automatischer Belichtung
                if( !preview.getCameraController().captureResultHasIso() || !applicationInterface.getISOPref().equals(CameraController.ISO_DEFAULT) ) {
                    applicationInterface.setISOPref(CameraController.ISO_DEFAULT);
                    applicationInterface.setExposureTimePref(CameraController.EXPOSURE_TIME_DEFAULT);
                    changed = true;
                }
                break;
            case TAB_WB:
                if( !applicationInterface.getWhiteBalancePref().equals("manual") ) {
                    applicationInterface.setWhiteBalancePref("manual");
                    changed = true;
                }
                break;
            case TAB_FOCUS:
                if( preview.getSupportedFocusValues() != null && preview.getSupportedFocusValues().contains("focus_mode_manual2")
                        && !"focus_mode_manual2".equals(preview.getCurrentFocusValue()) ) {
                    applicationInterface.setFocusPref("focus_mode_manual2", preview.isVideo());
                    changed = true;
                }
                break;
            case TAB_SHUTTER:
            case TAB_ISO:
                if( applicationInterface.getISOPref().equals(CameraController.ISO_DEFAULT) ) {
                    // wie das ISO-Fenster: den gemessenen Wert uebernehmen, sonst 800
                    String iso = CameraController.ISO_DEFAULT;
                    if( preview.getCameraController().captureResultHasIso() )
                        iso = String.valueOf(preview.getCameraController().captureResultIso());
                    else
                        iso = "800";
                    applicationInterface.setISOPref(iso);
                    changed = true;
                }
                break;
        }
        if( changed )
            main_activity.updateForSettings(true, "");
    }

    /* ---- Automatik-Knopf ------------------------------------------------------------------ */

    /** Der runde Knopf in der Pille: den aktiven Reiter auf Automatik zurueck. */
    public void clickedAuto() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedAuto: " + selected_tab);
        if( !open )
            return;
        Preview preview = main_activity.getPreview();
        ApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        if( preview == null )
            return;
        switch( selected_tab ) {
            case TAB_EXPOSURE:
                applicationInterface.setExposureCompensationPref(0);
                break;
            case TAB_WB:
                applicationInterface.clearWhiteBalancePref();
                break;
            case TAB_FOCUS:
                applicationInterface.setFocusPref("focus_mode_continuous_picture", preview.isVideo());
                break;
            case TAB_SHUTTER:
            case TAB_ISO:
                // ISO und Verschlusszeit sind eine gemeinsame Einstellung: Auto heisst beide auf Auto
                applicationInterface.setISOPref(CameraController.ISO_DEFAULT);
                applicationInterface.setExposureTimePref(CameraController.EXPOSURE_TIME_DEFAULT);
                break;
        }
        main_activity.updateForSettings(true, "");
        updateAutoButton();
    }

    /** „Alle zuruecksetzen": alles, was das Panel verstellen kann, auf den Standard. */
    private void resetAll() {
        if( MyDebug.LOG )
            Log.d(TAG, "resetAll");
        Preview preview = main_activity.getPreview();
        ApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        if( preview == null )
            return;
        applicationInterface.setExposureCompensationPref(0);
        applicationInterface.clearWhiteBalancePref();
        applicationInterface.setFocusPref("focus_mode_continuous_picture", preview.isVideo());
        applicationInterface.setISOPref(CameraController.ISO_DEFAULT);
        applicationInterface.setExposureTimePref(CameraController.EXPOSURE_TIME_DEFAULT);
        main_activity.updateForSettings(true, "");
        preview.showToast(null, main_activity.getResources().getString(R.string.pro_panel_reset_done), 0, true);
        updateAutoButton();
    }

    /* ---- Wert und Automatik-Knopf ---------------------------------------------------------- */

    private void updateAutoButton() {
        // Der Knopf bleibt immer stehen, auch wenn der Reiter schon auf Automatik steht (Falk
        // 2026-09-21): vorher verschwand er nach dem Druck, jetzt holt er immer den Standardwert
        // des aktiven Reiters zurueck. Die Unterscheidung "steht auf Automatik" braucht er nicht
        // mehr, deshalb faellt sie hier weg.
        auto_button.setVisibility(View.VISIBLE);
    }

    /** Der Wert des aktiven Reglers, in der Blase ueber der Pille. */
    private void updateBadge() {
        Preview preview = main_activity.getPreview();
        if( preview == null ) {
            badge.setVisibility(View.GONE);
            return;
        }
        ManualSeekbars manualSeekbars = main_activity.getManualSeekbars();
        String text = null;
        switch( selected_tab ) {
            case TAB_EXPOSURE: {
                // der Wert des Reglers im Panel, nicht der der Kamera: der Regler ist die
                // Anzeige, die Falk im Blick hat
                SeekBar seekbar = main_activity.findViewById(R.id.exposure_seekbar);
                if( main_activity.hasExposureSeekbarValues() )
                    text = exposureText(main_activity.getExposureSeekbarValue(seekbar.getProgress()));
                else
                    text = preview.getExposureCompensationString(preview.getCurrentExposure());
                break;
            }
            case TAB_WB: {
                int temperature = manualSeekbars.getWhiteBalanceTemperature(((SeekBar) main_activity.findViewById(R.id.white_balance_seekbar)).getProgress());
                text = temperature + " K";
                break;
            }
            case TAB_FOCUS: {
                float distance = (float) (ManualSeekbars.seekbarScaling(focus_seekbar.getProgress() / (double) focus_seekbar.getMax()) * preview.getMinimumFocusDistance());
                // Dioptrien -> Meter, wie Preview.setFocusDistance() es fuer den Toast macht
                if( distance > 0.0f ) {
                    text = String.format(java.util.Locale.US, "%.2f %s", 1.0f / distance, main_activity.getResources().getString(R.string.metres_abbreviation));
                }
                else {
                    text = main_activity.getResources().getString(R.string.infinite);
                }
                break;
            }
            case TAB_SHUTTER:
                text = preview.getExposureTimeString(manualSeekbars.getExposureTime(((SeekBar) main_activity.findViewById(R.id.exposure_time_seekbar)).getProgress()));
                break;
            case TAB_ISO:
                text = preview.getISOString(manualSeekbars.getISO(((SeekBar) main_activity.findViewById(R.id.iso_seekbar)).getProgress()));
                break;
        }
        if( text == null ) {
            badge.setVisibility(View.GONE);
        }
        else {
            if( !text.equals(badge_text) ) {
                badge_text = text;
                badge.setText(text);
            }
            badge.setVisibility(View.VISIBLE);
        }
    }

    /* ---- auf und zu ----------------------------------------------------------------------- */

    public boolean isOpen() {
        return open;
    }

    public void open() {
        if( open )
            return;
        Preview preview = main_activity.getPreview();
        if( preview == null )
            return;
        if( preview.isVideo() )
            return; // das Panel gibt es nur im Foto-Modus (MainUI.layoutUI() blendet den Knopf aus)
        main_activity.closePopup();
        if( main_activity.settingsSheetIsOpen() )
            main_activity.getSettingsSheet().close();
        // die alte Belichtungs-UI der Icon-Leiste hat hier nichts mehr zu suchen - ihre Regler
        // wandern gleich in die Pille
        if( main_activity.getMainUI().isExposureUIOpen() )
            main_activity.getMainUI().closeExposureUI();

        open = true;
        selected_tab = OPEN_TAB;
        ruler_labels_tab = -1;
        hideWhileOpen();
        ensureModeForTab(selected_tab);
        showSlider();
        markTabs();
        updateAutoButton();
        badge_text = null;
        updateBadge();

        pill.setVisibility(View.VISIBLE);
        tabs_scroll.setVisibility(View.VISIBLE);
        badge.setVisibility(View.VISIBLE);
        // Hintergrundband und Griffstueck wie am Einstellungen-Blatt (Falk 2026-09-21)
        sheet.setVisibility(View.VISIBLE);
        handle.setVisibility(View.VISIBLE);
        main_activity.enableProPanelOnBackPressedCallback(true);
        handler.removeCallbacks(badge_poll);
        handler.postDelayed(badge_poll, BADGE_POLL_MS);
    }

    public void close() {
        if( !open )
            return;
        open = false;
        handler.removeCallbacks(badge_poll);
        main_activity.enableProPanelOnBackPressedCallback(false);
        restoreSlider();
        restoreHidden();
        pill.setVisibility(View.GONE);
        badge.setVisibility(View.GONE);
        tabs_scroll.setVisibility(View.GONE);
        sheet.setVisibility(View.GONE);
        handle.setVisibility(View.GONE);
        ruler.setVisibility(View.GONE);
    }

    /** Zoom-Pille, Modus-Leiste und Foto/Video-Umschalter gehen weg, solange das Panel offen ist
     *  (Falk, Kickoff 3): die Pille des Panels und seine Reiter-Reihe nehmen ihre Plaetze ein.
     *  Die gemeinten Ansichten werden nicht auf GONE gesetzt, sondern auf INVISIBLE - sonst
     *  verschiebt das ConstraintLayout die Ansichten, die an ihnen haengen. */
    private void hideWhileOpen() {
        hidden_views.clear();
        hidden_visibilities.clear();
        // R.id.focus_seekbar gehoert dazu: im Panel bringt der Reiter Fokus einen eigenen Regler
        // mit, der Fokus-Regler der App (den DrawPreview ueber den Sucher legt) bleibt weg.
        // Die Dock-Zeile (beide Kreise, Foto/Video-Umschalter) geht mit: laut Referenz sitzt die
        // Tab-Reihe in deren Slot ganz unten (Falk 2026-09-21) - beide koennen nicht dort sein.
        int [] ids = { R.id.zoom_stepper, R.id.zoom_badge, R.id.mode_bar, R.id.photo_video_switch,
                R.id.focus_seekbar, R.id.settings_sheet_button, R.id.pro_panel_button };
        for(int id : ids) {
            View view = main_activity.findViewById(id);
            if( view == null )
                continue;
            hidden_views.add(view);
            hidden_visibilities.add(view.getVisibility());
            view.setVisibility(View.INVISIBLE);
        }
    }

    private void restoreHidden() {
        for(int i = 0; i < hidden_views.size(); i++)
            hidden_views.get(i).setVisibility(hidden_visibilities.get(i));
        hidden_views.clear();
        hidden_visibilities.clear();
    }

    /** Von MainUI.layoutUI() gerufen, nachdem es die Sichtbarkeiten der Modi gesetzt hat: sonst
     *  holt jeder Layout-Durchlauf die Zoom-Pille und die Modus-Leiste zurueck. */
    public void applyHiddenWhileOpen() {
        if( !open )
            return;
        for(View view : hidden_views)
            view.setVisibility(View.INVISIBLE);
    }

    private int dp(float value) {
        float scale = main_activity.getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }
}
