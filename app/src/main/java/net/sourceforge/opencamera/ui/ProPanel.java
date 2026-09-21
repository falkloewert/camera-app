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
    /** Der Strich-Abstand des Massbands ({@code RulerSlider.PX_PER_STEP_DP} ist der Vorgabewert).
     *  ISO und Fokus bekommen mehr Platz je Strich: ihre Zahlen kleben sonst aneinander - bei ISO
     *  liegen zwischen zwei Zahlen drei Striche, und eine vierstellige Zahl ist breiter als zwei
     *  Striche (am Geraet gesehen, Falk 2026-09-21). */
    private static final float RULER_STEP_DP = 8.0f;
    private static final float RULER_STEP_FOCUS_DP = 11.0f;
    private static final float RULER_STEP_ISO_DP = 14.0f;
    /** Zwischen zwei Zahlen des Massbands liegen mindestens so viele Striche (8 dp je Strich). */
    private static final int MIN_LABEL_STEPS = 5;
    /** Und hoechstens so viele, wo eine Zwischenzahl erlaubt ist. Die gibt es nur bei der
     *  Verschlusszeit (Nenner) - dort ist auch der Zwischenwert eine runde Zahl. Bei Belichtung,
     *  Weissabgleich, ISO und Fokus stehen ausschliesslich die runden Werte: eine Zahl wie "+2,83"
     *  oder "0,74" will Falk dort nicht sehen (2026-09-21). */
    private static final int MAX_LABEL_STEPS = 12;
    /** Der Belichtungsregler teilt in Sechstel-EV; eine volle Zahl sind sechs Schritte. */
    private static final int EV_SIXTHS = 6;
    /** Das Massband der Belichtung teilt groeber: einen halben EV je Strich (drei Sechstel des
     *  App-Reglers). Damit liegt die Null genau in der Mitte, die Striche stehen gleich weit
     *  links und rechts davon, und ein Zug am Band landet auf Zahlen wie "+2" oder "+2,5" statt
     *  auf "+2,83" (Falk 2026-09-21). */
    private static final int EXPOSURE_STEP_SIXTHS = EV_SIXTHS / 2;
    /** Belichtung: eine Zahl alle zwei EV (vier Schritte des Massbands). Von der Null aus nach
     *  beiden Seiten gezaehlt, damit unter dem Balken die "0" steht und links und rechts davon
     *  dieselben Zahlen - vorher hing die Beschriftung am linken Rand des Bereichs, dadurch
     *  standen dort "-4, -1, +2" und unter dem Balken gar nichts (Falk 2026-09-21). */
    private static final int EXPOSURE_LABEL_STEPS = 2 * EV_SIXTHS / EXPOSURE_STEP_SIXTHS;
    /** Der Weissabgleich geht in Schritten von 500 K - eine runde Zahl, die man ansagen kann. */
    private static final int WB_STEP = 500;
    /** Auf dem Massband stehen nur die vollen Tausend: fuenfstellige Zahlen liegen bei 500 K
     *  Abstand so dicht, dass sie aneinanderkleben (am Geraet gesehen, 2026-09-21). */
    private static final int WB_LABEL_STEP = 1000;
    /** Was die App als Farbtemperatur mitbringt ({@code MyApplicationInterface}, 5000 K) - der
     *  Wert, auf den der Automatik-Knopf den Weissabgleich zurueckstellt, wenn er nicht auf
     *  Auto geht (siehe {@link #clickedAuto()}). */
    private static final int WB_DEFAULT_TEMPERATURE = 5000;
    /** Der Fokuswert der App fuer "von Hand gestellt" - nur dann wirkt der Fokus-Regler. */
    private static final String FOCUS_MANUAL = "focus_mode_manual2";

    /** Die ISO-Stufen einer Kamera: die Drittelstufen-Reihe, die ein Fotograf erwartet (und die
     *  einzigen, die eine Zahl auf dem Massband bekommen). Bewusst <b>nur</b> diese Reihe - eine
     *  dichtere Folge am unteren Ende (50, 60, 70, 80, 90, 100) rueckte die Zahlen dort zusammen
     *  und liess die Striche ungleichmaessig aussehen (Falk 2026-09-21). */
    private static final int [] NICE_ISO = {50, 64, 80, 100, 125, 160, 200, 250, 320, 400, 500,
            640, 800, 1000, 1250, 1600, 2000, 2500, 3200, 4000, 5000, 6400, 8000, 10000, 12800};
    /** ISO: so viele Striche liegen mindestens zwischen zwei Zahlen. Drei Striche sind eine volle
     *  Blende (100, 200, 400 ...) - die Reihe hat genau diese Teilung. */
    private static final int ISO_LABEL_STEPS = 3;

    /** Die Nenner der Verschlusszeit, die eine Zahl bekommen (1/60 s usw.). */
    private static final long [] NICE_DENOMINATORS = {1, 2, 3, 4, 5, 6, 8, 10, 15, 20, 25, 30, 40,
            50, 60, 80, 100, 125, 160, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 2000,
            2500, 3200, 4000, 5000, 6400, 8000, 10000};

    /** Dasselbe in Sekunden. */
    private static final long [] NICE_SECONDS = {1, 2, 3, 4, 5, 6, 8, 10, 15, 20, 30, 60, 90, 120,
            180, 240, 300, 420, 600, 900, 1200};

    /** Die Entfernungen des Fokus-Massbands: <b>eine Stufe je Strich</b>, von unendlich (0.0) bis
     *  zur Naheinstellgrenze - die Meter-Skala eines Objektivs, nach rechts werden die
     *  Entfernungen kleiner. Nur runde Werte, die man ansagen kann.
     *
     *  <p>Das Band folgt nicht der Diaptrien-Teilung des App-Reglers: die hat tausend Striche, ein
     *  Zug quer durch die Pille bewegte den Wert dann um ein Prozent, und die Zahlen darauf waeren
     *  krumme Zwischenwerte wie "0,74" (Falk 2026-09-21).</p> */
    private static final double [] FOCUS_LADDER = {0.0 /* unendlich */, 15.0, 10.0, 7.0, 5.0, 4.0,
            3.0, 2.0, 1.5, 1.0, 0.7, 0.5, 0.4, 0.3, 0.25, 0.2, 0.15, 0.12, 0.1};
    /** Beim Fokus steht nur jede dritte Stufe angeschrieben (8 dp je Strich), gezaehlt von der
     *  Naheinstellgrenze her: die nahen Entfernungen sind die, die man einstellt. */
    private static final int FOCUS_LABEL_STEPS = 3;
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

    /** Die Leitern von ISO und Fokus, gebaut fuer die Grenzen, die dabei galten ({@link #isoLadder()},
     *  {@link #focusLadder()}). */
    private int [] iso_ladder;
    private int iso_ladder_min, iso_ladder_max;
    private double [] focus_ladder;
    private double focus_ladder_min;
    /** Ein Zug am Band hat den Fokus auf manuell gestellt, die Kamera hat es noch nicht
     *  uebernommen (siehe {@link #pendingManualFocus()}). */
    private boolean focus_manual_pending;
    /** Der Schritt, den ein Zug am Band vorgemerkt hat, solange die Kamera noch auf Automatik
     *  steht ({@link #onRulerScrollEnd()}). */
    private int focus_pending_step = -1;

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
            if( selected_tab == TAB_FOCUS ) {
                if( focusIsManual() )
                    // die Kamera hat den manuellen Fokus uebernommen - die Vormerkung ist erfuellt
                    focus_manual_pending = false;
                // sucht die Kamera selbst, wandert die Entfernung mit dem Motiv - der Regler
                // laeuft dann mit (von Hand gestellt bleibt er stehen)
                setFocusSeekbarProgress();
            }
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
        ruler.setOnScrollListener(new RulerSlider.OnScrollListener() {
            @Override
            public void onScroll(int step) {
                onRulerScroll(step);
            }

            @Override
            public void onScrollEnd() {
                onRulerScrollEnd();
            }
        });
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
        // So weit laesst sich die Reihe scrollen, dass der erste bzw. letzte Eintrag etwa in der
        // Mitte steht. Vorher lag die Reihe breiter als der Schirm mittig im Fenster, dadurch war
        // "Alle zuruecksetzen" am linken Rand abgeschnitten und nicht erreichbar (Falk 2026-09-21).
        int screen_width = main_activity.getResources().getDisplayMetrics().widthPixels;
        int entry_width = label_width + dp(TAB_GAP);
        int padding = Math.max(dp(TAB_ROW_PADDING), (screen_width - entry_width) / 2);
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
        // Der Reiterwechsel allein stellt die Kamera noch nicht um: sonst legte schon ein Blick
        // auf den Weissabgleich die Automatik still (11200 K statt Auto, Falk 2026-09-21). Erst
        // ein Zug am Band oder ein Druck auf + / - schaltet den Reiter auf manuell
        // (onRulerScroll, stepActiveSlider).
        selected_tab = tab;
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
            // beim Reiterwechsel kommt der Wert von der Kamera: der Regler dieses Panels wurde
            // dann noch nie gestellt und stuende sonst auf unendlich (Falk 2026-09-21)
            setFocusSeekbarProgress(false);
        SeekBar slider = activeSeekbar();
        hostSlider(slider);
        // ein stehengebliebener Sechstel-Wert (etwa aus einer frueheren Sitzung) wird beim
        // Oeffnen auf das Raster gezogen - sonst stuende in der Blase gleich "+3,17"
        snapExposureToGrid();
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
        // der SeekBar ist nur noch der Wert: sichtbar und bedient wird das Massband. Alpha 0
        // statt INVISIBLE/GONE - die App stellt ihre Regler selbst auf sichtbar, und ein
        // unsichtbarer Regler liesse sich nicht mehr stellen.
        slider.setAlpha(0.0f);
        slider_holder.addView(slider, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        // das Massband nimmt die Beruehrungen an, nicht der Regler darunter (Falk 2026-09-21:
        // das Band soll dem Finger folgen, nicht der Werteliste des Reglers)
        ruler.bringToFront();
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
        setFocusSeekbarProgress(focusHoldsValue());
    }

    /** @param hold wenn true, bleibt der Wert des Reglers stehen - er hat ihn selbst gestellt;
     *  sonst kommt er von der Kamera. */
    private void setFocusSeekbarProgress(boolean hold) {
        Preview preview = main_activity.getPreview();
        if( preview == null || preview.getCameraController() == null )
            return;
        focus_seekbar.setOnSeekBarChangeListener(null);
        ManualSeekbars.setProgressSeekbarScaled(focus_seekbar, 0.0, preview.getMinimumFocusDistance(),
                (float) shownFocusDioptres(hold));
        focus_seekbar.setOnSeekBarChangeListener(focus_listener);
    }

    /** Ob das Band den Wert haelt, statt der Kamera zu folgen: von Hand gestellt - oder gerade
     *  dabei, es zu werden (siehe {@link #focus_manual_pending}). */
    private boolean focusHoldsValue() {
        return focusIsManual() || focus_manual_pending;
    }

    /** Die Schaerfestellung, die das Band zeigen soll, in Dioptrien (0 = unendlich): von Hand
     *  gestellt der Wert des Reglers, sonst das, was die Kamera gerade selbst misst. */
    private double shownFocusDioptres(boolean hold) {
        Preview preview = main_activity.getPreview();
        if( preview == null || preview.getCameraController() == null )
            return 0.0;
        CameraController camera_controller = preview.getCameraController();
        if( hold || !camera_controller.captureResultHasFocusDistance() ) {
            // von Hand gestellt bleibt der Wert stehen; sucht die Kamera selbst, ist ihre Messung
            // der Wert, den das Band zeigen muss (Falk 2026-09-21)
            return focusDioptres(focus_seekbar.getProgress());
        }
        return camera_controller.captureResultFocusDistance();
    }

    /** Einen Zug am Band oder einen Druck auf +/- vormerken: der Fokus wird dabei von Hand
     *  gestellt, die Kamera uebernimmt das aber erst mit dem naechsten updateForSettings -
     *  solange darf der Poll den Regler nicht auf den gemessenen Wert zurueckstellen, sonst ist
     *  der erste Druck verloren (am Geraet gesehen, 2026-09-21). */
    private void pendingManualFocus() {
        if( !focusManualSupported() )
            return;
        focus_manual_pending = true;
    }

    /** Ob die Kamera einen von Hand gestellten Fokus ueberhaupt kann. */
    private boolean focusManualSupported() {
        Preview preview = main_activity.getPreview();
        List<String> values = preview != null ? preview.getSupportedFocusValues() : null;
        return values != null && values.contains(FOCUS_MANUAL);
    }

    /** Ob der Fokus von Hand gestellt ist - dann gehoert das Band dem Finger und die Kamera misst
     *  nicht selbst. */
    private boolean focusIsManual() {
        Preview preview = main_activity.getPreview();
        return preview != null && FOCUS_MANUAL.equals(preview.getCurrentFocusValue());
    }

    /* ---- Feinjustierung und Massband ------------------------------------------------------- */

    /** Ein Schritt des aktiven Reglers weiter oder zurueck: die Knoepfe links und rechts der
     *  Pille (Referenz: das Symbol des Reiters mit Minus bzw. Plus).
     *
     *  <p>Die Schrittweite ist je Einstellung die, die man auch ansagen wuerde - kein Sprung auf
     *  einen krummen Wert: Belichtung in halben EV (der erste Druck rundet auf die naechste volle
     *  Zahl), Weissabgleich in 500 K, ISO und Fokus auf der naechsten Stufe der Kamera- bzw.
     *  Meter-Skala, Verschlusszeit eine Stufe ihrer eigenen Reihe (Falk 2026-09-21).</p>
     *
     *  <p>Der Weg ist derselbe wie bei den Lautstaerketasten - die Zuhoerer der App in
     *  MainActivity.cameraSetup() wirken auch ohne Nutzer-Ereignis (fromUser), das Umhaengen in
     *  die Pille aendert daran nichts.</p> */
    private void stepActiveSlider(int direction) {
        if( !open || direction == 0 )
            return;
        // steht der Reiter auf Auto (Automatik-Knopf, "Alle zuruecksetzen"), wirkt ein Schritt nur
        // im manuellen Modus - erst den einschalten, wie es das ISO-Fenster der App auch macht
        if( selected_tab == TAB_FOCUS && !focusIsManual() )
            pendingManualFocus();
        ensureModeForTab(selected_tab);
        switch( selected_tab ) {
            case TAB_EXPOSURE:
                stepExposure(direction);
                break;
            case TAB_WB:
                stepWhiteBalance(direction);
                break;
            case TAB_FOCUS:
                stepFocus(direction);
                break;
            case TAB_SHUTTER:
                // die Reihe des Reglers ist schon die der Kamera (1/50, 1/45, ... 1/15) - eine
                // Stufe weiter ist genau der naechste sinnvolle Wert
                main_activity.getMainUI().changeSeekbar(R.id.exposure_time_seekbar, direction);
                break;
            case TAB_ISO:
                stepISO(direction);
                break;
        }
        // sofort nachziehen, damit Wert und Massband nicht bis zum naechsten Poll stehen bleiben
        updateRuler();
        updateBadge();
    }

    /** Den Belichtungswert auf das Raster des Panels (halbe EV) runden. Der Regler der App teilt
     *  feiner, und eine Zahl wie "+3,17" ist keine, die man ansagen kann. */
    private void snapExposureToGrid() {
        if( !open || selected_tab != TAB_EXPOSURE || !main_activity.hasExposureSeekbarValues() )
            return;
        SeekBar seekbar = main_activity.findViewById(R.id.exposure_seekbar);
        int value = main_activity.getExposureSeekbarValue(seekbar.getProgress());
        int target = Math.round(value / (float) (EV_SIXTHS / 2)) * (EV_SIXTHS / 2);
        if( target != value )
            setSliderValue(TAB_EXPOSURE, target);
    }

    /** Belichtung: der Regler teilt in Sechstel-EV. Der erste Druck rundet auf die naechste volle
     *  Zahl (auf bzw. ab), danach geht es in halben EV weiter. */
    private void stepExposure(int direction) {
        if( !main_activity.hasExposureSeekbarValues() )
            return;
        SeekBar seekbar = main_activity.findViewById(R.id.exposure_seekbar);
        int value = main_activity.getExposureSeekbarValue(seekbar.getProgress());
        int target;
        if( value % EV_SIXTHS == 0 )        // steht schon glatt: halber Schritt weiter
            target = value + direction * (EV_SIXTHS / 2);
        else if( direction > 0 )            // sonst auf die naechste volle Zahl aufrunden
            target = (Math.floorDiv(value, EV_SIXTHS) + 1) * EV_SIXTHS;
        else                                // bzw. abrunden
            target = Math.floorDiv(value, EV_SIXTHS) * EV_SIXTHS;
        setSliderValue(TAB_EXPOSURE, target);
    }

    /** Weissabgleich: 500 K je Druck (die Zahlen des Massbands liegen im selben Raster). */
    private void stepWhiteBalance(int direction) {
        SeekBar seekbar = main_activity.findViewById(R.id.white_balance_seekbar);
        int temperature = main_activity.getManualSeekbars().getWhiteBalanceTemperature(seekbar.getProgress());
        long target;
        if( temperature % WB_STEP == 0 )
            target = temperature + direction * WB_STEP;
        else if( direction > 0 )
            target = (temperature / WB_STEP + 1) * WB_STEP;
        else
            target = (temperature / WB_STEP) * WB_STEP;
        setSliderValue(TAB_WB, target);
    }

    /** ISO: eine Stufe der Kamera-Reihe weiter (100, 125, 160, 200, ...) - genau ein Strich des
     *  Massbands, nicht der naechste Schritt des Reglers (der waere nur 5 oder 10 ISO weit). */
    private void stepISO(int direction) {
        stepLadder(TAB_ISO, direction, main_activity.findViewById(R.id.iso_seekbar));
    }

    /** Fokus: eine Stufe der Meter-Skala weiter - ein Strich des Massbands. Plus geht naeher
     *  heran (die Skala laeuft nach rechts zu den kleinen Entfernungen), Minus weiter weg.
     *
     *  <p>Vorher suchte der Schritt den naechsten runden Wert in einer Liste; weil die Liste
     *  abwaerts durchlaufen wurde, sprang Minus von 0,3 m gleich auf 500 m (Falk 2026-09-21).</p> */
    private void stepFocus(int direction) {
        stepLadder(TAB_FOCUS, direction, focus_seekbar);
    }

    /** Einen Strich der Leiter weiter: der Wert unter dem Balken, ein Schritt davor oder danach.
     *  Beide Enden halten (unendlich bzw. die Naheinstellgrenze). */
    private void stepLadder(int tab, int direction, SeekBar seekbar) {
        if( seekbar == null )
            return;
        int step = rulerStep(tab, seekbar.getProgress()) + direction;
        int max = rulerMax(tab);
        if( step < 0 )
            step = 0;
        else if( step > max )
            step = max;
        seekbar.setProgress(seekbarProgress(tab, step));
    }

    /** Den Regler auf einen Wert stellen: den Schritt suchen, dessen Wert am naechsten liegt. Die
     *  Wertelisten sind aufsteigend und hoechstens ein paar hundert Eintraege lang, deshalb genuegt
     *  das Suchen. */
    private void setSliderValue(int tab, long target) {
        SeekBar seekbar = activeSeekbar();
        if( seekbar == null )
            return;
        int max = rulerMax(tab);
        int best = 0;
        long best_distance = Long.MAX_VALUE;
        for(int step = 0; step <= max; step++) {
            long distance = Math.abs(sliderValueAt(tab, step) - target);
            if( distance < best_distance ) {
                best_distance = distance;
                best = step;
                if( distance == 0 )
                    break;
            }
        }
        seekbar.setProgress(seekbarProgress(tab, best));
    }

    /* ---- Schritt des Massbands und Schritt des App-Reglers --------------------------------- */

    /** Der hoechste Schritt des Massbands. Beim Belichtungsregler ist das Band groeber geteilt
     *  als der Regler (halbe EV statt Sechstel) und die drei Nullen in der Mitte fallen auf einen
     *  Schritt zusammen (MainActivity baut sie fuer das Einrasten ein). ISO und Fokus haben ihre
     *  eigene Leiter ({@link #isoLadder()}, {@link #focusLadder()}). */
    private int rulerMax(int tab) {
        SeekBar seekbar = activeSeekbar();
        if( seekbar == null )
            return 0;
        if( tab == TAB_EXPOSURE && main_activity.hasExposureSeekbarValues() )
            return exposureHalfSteps(seekbar.getMax()) - exposureHalfSteps(0);
        if( tab == TAB_ISO )
            return isoLadder().length - 1;
        if( tab == TAB_FOCUS )
            return focusLadder().length - 1;
        return seekbar.getMax();
    }

    /** Der Schritt des Massbands zu einem Schritt des App-Reglers. */
    private int rulerStep(int tab, int progress) {
        if( tab == TAB_EXPOSURE && main_activity.hasExposureSeekbarValues() )
            return exposureHalfSteps(progress) - exposureHalfSteps(0);
        // der Regler kennt feinere Werte, das Band nur seine eigenen Stufen: die naechste nehmen
        if( tab == TAB_ISO )
            return nearestISOStep(main_activity.getManualSeekbars().getISO(progress));
        if( tab == TAB_FOCUS )
            return nearestFocusStep(focusDioptres(progress));
        return progress;
    }

    /** Die Umkehrung: der Schritt des App-Reglers zu einem Schritt des Massbands. */
    private int seekbarProgress(int tab, int step) {
        if( tab == TAB_ISO )
            return isoSeekbarProgress(isoLadder()[step]);
        if( tab == TAB_FOCUS )
            return focusProgress(focusLadder()[step]);
        if( tab == TAB_EXPOSURE && main_activity.hasExposureSeekbarValues() ) {
            SeekBar seekbar = main_activity.findViewById(R.id.exposure_seekbar);
            int value = (step + exposureHalfSteps(0)) * EXPOSURE_STEP_SIXTHS;
            if( value == 0 ) {
                // von den drei Nullen des Reglers die mittlere nehmen (wie changeExposure())
                return main_activity.getExposureSeekbarProgressZero();
            }
            for(int p = 0; p <= seekbar.getMax(); p++) {
                if( main_activity.getExposureSeekbarValue(p) == value )
                    return p;
            }
        }
        return step;
    }

    /** Ein Schritt des Belichtungsreglers in halben EV (die Teilung des Massbands). */
    private int exposureHalfSteps(int progress) {
        return Math.round(main_activity.getExposureSeekbarValue(progress) / (float) EXPOSURE_STEP_SIXTHS);
    }

    /* ---- die Leitern von ISO und Fokus ----------------------------------------------------- */

    /** Die ISO-Stufen des Massbands, aufsteigend: die Reihe der Kamera ({@link #NICE_ISO}) plus
     *  die Grenzen, die der Regler der App kennt. Die Werteliste des Reglers selbst hat Hunderte
     *  Eintraege (jeder fuenfte, zehnte, fuenfzigste Wert) - ein Strich je Eintrag ergaebe ein
     *  Band von tausend Strichen, an dem ein Zug kaum etwas bewegt (Falk 2026-09-21). */
    private int [] isoLadder() {
        ManualSeekbars manualSeekbars = main_activity.getManualSeekbars();
        SeekBar seekbar = main_activity.findViewById(R.id.iso_seekbar);
        int min = manualSeekbars.getISO(0);
        int max = manualSeekbars.getISO(seekbar.getMax());
        if( iso_ladder != null && iso_ladder_min == min && iso_ladder_max == max )
            return iso_ladder;
        List<Integer> values = new ArrayList<>();
        values.add(min);
        for(int value : NICE_ISO) {
            if( value > min && value <= max )
                values.add(value);
        }
        if( values.get(values.size() - 1) != max )
            values.add(max);
        iso_ladder = new int [values.size()];
        for(int i = 0; i < values.size(); i++)
            iso_ladder[i] = values.get(i);
        iso_ladder_min = min;
        iso_ladder_max = max;
        return iso_ladder;
    }

    /** Die Entfernungen des Fokus-Massbands, von unendlich bis zur Naheinstellgrenze der Kamera
     *  ({@link #FOCUS_LADDER}, gekuerzt um das, was naeher liegt als das Objektiv kann - und mit
     *  der Naheinstellgrenze selbst als letzter Stufe: die Kamera misst dort einen Wert, den die
     *  runden Zahlen der Reihe nicht haben, das Band zeigte dann etwas anderes als die Blase). */
    private double [] focusLadder() {
        Preview preview = main_activity.getPreview();
        double min_focus_distance = preview != null ? preview.getMinimumFocusDistance() : 0.0f;
        if( focus_ladder != null && focus_ladder_min == min_focus_distance )
            return focus_ladder;
        double near_limit = min_focus_distance > 0.0f ? 1.0 / min_focus_distance : 0.0;
        List<Double> values = new ArrayList<>();
        values.add(FOCUS_LADDER[0]);            // unendlich
        for(int i = 1; i < FOCUS_LADDER.length; i++) {
            if( near_limit > 0.0 && FOCUS_LADDER[i] < near_limit )
                break;
            values.add(FOCUS_LADDER[i]);
        }
        double last_value = values.get(values.size() - 1);
        if( near_limit > 0.0 && near_limit < last_value )
            values.add(near_limit);
        focus_ladder = new double [values.size()];
        for(int i = 0; i < values.size(); i++)
            focus_ladder[i] = values.get(i);
        focus_ladder_min = min_focus_distance;
        return focus_ladder;
    }

    /** Die Stufe der ISO-Leiter, die einem Wert am naechsten liegt. */
    private int nearestISOStep(long iso) {
        int [] ladder = isoLadder();
        int best = 0;
        long best_distance = Long.MAX_VALUE;
        for(int step = 0; step < ladder.length; step++) {
            long distance = Math.abs(ladder[step] - iso);
            if( distance < best_distance ) {
                best_distance = distance;
                best = step;
            }
        }
        return best;
    }

    /** Die Stufe der Fokus-Leiter zu einer Schaerfestellung - verglichen in Dioptrien, so wie der
     *  Regler der App geteilt ist (in Metern laege "unendlich" naeher an 15 m als an 10 m). */
    private int nearestFocusStep(double dioptres) {
        double [] ladder = focusLadder();
        int best = 0;
        double best_distance = Double.MAX_VALUE;
        for(int step = 0; step < ladder.length; step++) {
            double step_dioptres = ladder[step] > 0.0 ? 1.0 / ladder[step] : 0.0;
            double distance = Math.abs(step_dioptres - dioptres);
            if( distance < best_distance ) {
                best_distance = distance;
                best = step;
            }
        }
        return best;
    }

    /** Und umgekehrt: die Stelle des Massbands zu einer Entfernung, auch zwischen zwei Strichen.
     *  Die messende Kamera liefert keine Striche, das Band soll ihr aber stetig folgen. */
    private float focusPosition(double metres) {
        double [] ladder = focusLadder();
        int last = ladder.length - 1;
        if( metres <= 0.0 )
            return 0.0f;                            // unendlich
        if( metres <= ladder[last] )
            return last;
        double dioptres = 1.0 / metres;
        for(int step = 0; step < last; step++) {
            double far = ladder[step] > 0.0 ? 1.0 / ladder[step] : 0.0;
            double near = 1.0 / ladder[step + 1];
            if( dioptres >= far && dioptres <= near )
                return (float) (step + (dioptres - far) / (near - far));
        }
        return last;
    }

    /** Der Schritt des ISO-Reglers zu einer Stufe der Leiter (die Werteliste der App ist
     *  aufsteigend, deshalb genuegt das Suchen). */
    private int isoSeekbarProgress(long iso) {
        SeekBar seekbar = main_activity.findViewById(R.id.iso_seekbar);
        int max = seekbar.getMax();
        int best = 0;
        long best_distance = Long.MAX_VALUE;
        for(int progress = 0; progress <= max; progress++) {
            long distance = Math.abs(main_activity.getManualSeekbars().getISO(progress) - iso);
            if( distance < best_distance ) {
                best_distance = distance;
                best = progress;
                if( distance == 0 )
                    break;
            }
        }
        return best;
    }

    /** Der Wert hinter einem Schritt des Massbands. */
    private long sliderValueAt(int tab, int step) {
        ManualSeekbars manualSeekbars = main_activity.getManualSeekbars();
        switch( tab ) {
            case TAB_EXPOSURE:
                if( main_activity.hasExposureSeekbarValues() )
                    return (long) (step + exposureHalfSteps(0)) * EXPOSURE_STEP_SIXTHS;
                return 0;
            case TAB_WB:
                return manualSeekbars.getWhiteBalanceTemperature(step);
            case TAB_ISO:
                return isoLadder()[step];
            case TAB_FOCUS: {
                double metres = focusLadder()[step];
                return metres > 0.0 ? (long) (1000.0 / metres) : 0;   // Dioptrien, wie der Regler
            }
            case TAB_SHUTTER:
                return manualSeekbars.getExposureTime(step);
            default:
                return 0;
        }
    }

    /** Der Schritt des Fokus-Reglers zu einer Entfernung (die Umkehrung von
     *  {@link #focusDioptres(int)}, dieselbe Rechnung wie ManualSeekbars). */
    private int focusProgress(double metres) {
        Preview preview = main_activity.getPreview();
        if( preview == null || metres <= 0.0 )
            return 0;
        double scaling = (1.0 / metres) / preview.getMinimumFocusDistance();
        double frac = Math.log(99.0 * scaling + 1.0) / Math.log(100.0);
        int progress = (int) (frac * focus_seekbar.getMax() + 0.5);
        if( progress < 0 )
            progress = 0;
        else if( progress > focus_seekbar.getMax() )
            progress = focus_seekbar.getMax();
        return progress;
    }

    /** Das Massband auf den aktiven Regler stellen (Wert, Skala, Beschriftung). */
    private void updateRuler() {
        SeekBar seekbar = activeSeekbar();
        if( !open || seekbar == null ) {
            ruler.setVisibility(View.GONE);
            return;
        }
        int max = rulerMax(selected_tab);
        boolean rescaled = ruler.getMax() != max || ruler_labels_tab != selected_tab;
        float step_dp = RULER_STEP_DP;
        if( selected_tab == TAB_ISO )
            step_dp = RULER_STEP_ISO_DP;
        else if( selected_tab == TAB_FOCUS )
            step_dp = RULER_STEP_FOCUS_DP;
        ruler.setStepSpacingDp(step_dp);
        if( rescaled ) {
            ruler.setMax(max);
            ruler.setLabels(buildLabels(selected_tab, max));
            ruler_labels_tab = selected_tab;
        }
        // beim Reiterwechsel springt das Massband (die Skala ist eine andere), sonst faehrt es
        // den neuen Wert an wie ein Drehrad
        if( selected_tab == TAB_FOCUS && !focusHoldsValue() ) {
            // sucht die Kamera selbst, steht das Band auf ihrem Wert - und zwar stetig, auch
            // zwischen zwei Strichen: die Kamera misst keine Striche, und in Achter-dp-Spruengen
            // saehe das Band aus, als haenge es (Falk 2026-09-21)
            double dioptres = shownFocusDioptres(false);
            ruler.setValue(focusPosition(dioptres > 0.0 ? 1.0 / dioptres : 0.0), true);
        }
        else {
            ruler.setProgress(rulerStep(selected_tab, seekbar.getProgress()), !rescaled);
        }
        ruler.setVisibility(View.VISIBLE);
    }

    /** Das Massband zieht der Finger: der Schritt unter dem Balken kommt sofort in den Regler der
     *  App. Das Band selbst laeuft dabei stufenlos weiter (RulerSlider), der Wert springt von
     *  Stufe zu Stufe - genau wie beim Zoomrad (Falk 2026-09-21). */
    private void onRulerScroll(int step) {
        if( !open )
            return;
        SeekBar seekbar = activeSeekbar();
        if( seekbar == null )
            return;
        // von Hand gestellte Werte wirken nur im manuellen Modus des Reiters - nach "Auto"
        // (Automatik-Knopf oder "Alle zuruecksetzen") schaltet der erste Zug ihn wieder ein.
        // Das aber erst, wenn der Finger das Band loslaesst: ein Fokuswert waehrend der
        // Automatik laesst die Kamera neu starten, und das reisst die laufende Beruehrung ab -
        // der Zug kam dann nur bis zu dieser Stelle an (am Geraet gesehen, Falk 2026-09-21).
        if( selected_tab == TAB_FOCUS && !focusHoldsValue() ) {
            pendingManualFocus();
            focus_pending_step = step;
            return;
        }
        int progress = seekbarProgress(selected_tab, step);
        if( progress == seekbar.getProgress() )
            return;
        ensureModeForTab(selected_tab);
        seekbar.setProgress(progress);
        updateBadge();
    }

    /** Der Finger hat das Band losgelassen: jetzt den Modus stellen, den der Zug einschalten
     *  musste (siehe {@link #onRulerScroll(int)}), und den Wert noch einmal an die Kamera geben -
     *  waehrend des Zuges stand sie noch auf Auto und hat ihn nicht angenommen. */
    private void onRulerScrollEnd() {
        if( !open || selected_tab != TAB_FOCUS )
            return;
        if( !focus_manual_pending || focusIsManual() )
            return;
        ensureModeForTab(TAB_FOCUS);
        focus_manual_pending = false;
        if( focus_pending_step >= 0 ) {
            // der Zug hat den Wert nur vorgemerkt: jetzt, im manuellen Modus, kommt er in den
            // Regler der App (setProgress loest den Zuhoerer aus und stellt die Kamera darauf)
            focus_seekbar.setProgress(seekbarProgress(TAB_FOCUS, focus_pending_step));
            focus_pending_step = -1;
        }
        else {
            focus_listener.onProgressChanged(focus_seekbar, focus_seekbar.getProgress(), false);
        }
    }

    /** Die Beschriftung des Massbands. Beschriftet werden nur runde Werte - die Referenz schreibt
     *  auch nicht jeden Strichen an, sondern die Stufen (100/200/400/800 ISO). Zwischen zwei
     *  Zahlen liegen mindestens {@link #MIN_LABEL_STEPS} Striche; wo eine Zwischenzahl erlaubt ist
     *  (Fokus, Verschlusszeit), hoechstens {@link #MAX_LABEL_STEPS}. */
    private String [] buildLabels(int tab, int max) {
        String [] labels = new String [max + 1];
        if( max <= 0 )
            return labels;
        if( tab == TAB_EXPOSURE && main_activity.hasExposureSeekbarValues() ) {
            // die Beschriftung haengt an der Null, nicht am Rand: so steht die "0" genau unter
            // dem Balken und beiderseits dieselben Zahlen
            int zero = rulerStep(TAB_EXPOSURE, main_activity.getExposureSeekbarProgressZero());
            for(int step = zero; step <= max; step += EXPOSURE_LABEL_STEPS)
                labels[step] = roundLabel(tab, step);
            for(int step = zero - EXPOSURE_LABEL_STEPS; step >= 0; step -= EXPOSURE_LABEL_STEPS)
                labels[step] = roundLabel(tab, step);
            return labels;
        }
        if( tab == TAB_ISO ) {
            // die vollen Blenden (100, 200, 400, 800 ...), und die im gleichen Abstand: die
            // Drittelstufen-Reihe hat zwischen zwei Blenden genau drei Striche. Eine Stufe, die
            // die Kamera zusaetzlich kennt (ihr kleinstes ISO), bekommt keine Zahl, wenn sie
            // dadurch zu dicht an der naechsten staende (Falk 2026-09-21)
            int [] ladder = isoLadder();
            int last = -ISO_LABEL_STEPS;
            for(int step = 0; step <= max; step++) {
                if( step - last < ISO_LABEL_STEPS || !isStopISO(ladder[step]) )
                    continue;
                labels[step] = String.valueOf(ladder[step]);
                last = step;
            }
            return labels;
        }
        if( tab == TAB_FOCUS ) {
            // von unendlich her gezaehlt, damit die Zahlen immer dieselben sind: die
            // Naheinstellgrenze am Ende der Reihe haengt an der Kamera und verschob die Zahlen
            // sonst bei jedem Geraet (Falk 2026-09-21)
            for(int step = 0; step <= max; step += FOCUS_LABEL_STEPS)
                labels[step] = roundLabel(tab, step);
            return labels;
        }
        // erst die runden Werte: sie stehen fest, egal was die Zwischenzahlen machen
        List<Integer> rounds = new ArrayList<>();
        int last = -MIN_LABEL_STEPS;
        for(int step = 0; step <= max; step++) {
            if( step - last < MIN_LABEL_STEPS )
                continue;
            String text = roundLabel(tab, step);
            if( text == null )
                continue;
            labels[step] = text;
            rounds.add(step);
            last = step;
        }
        // dann die Luecken, wo eine Zwischenzahl erlaubt ist (Verschlusszeit): gleichmaessig
        // verteilt und mit Abstand zu beiden Nachbarn - sonst verdraengt eine Zwischenzahl die
        // naechste runde Zahl
        if( tab == TAB_SHUTTER ) {
            for(int i = 0; i < rounds.size(); i++) {
                int from = rounds.get(i);
                int to = (i + 1 < rounds.size()) ? rounds.get(i + 1) : max;
                for(int step = from + MAX_LABEL_STEPS; to - step >= MIN_LABEL_STEPS; step += MAX_LABEL_STEPS) {
                    String text = valueLabel(tab, step);
                    if( text != null )
                        labels[step] = text;
                }
            }
        }
        return labels;
    }

    /** Die Zahl an einem runden Wert - oder null, wenn dieser Schritt keinen runden Wert hat. */
    private String roundLabel(int tab, int step) {
        switch( tab ) {
            case TAB_EXPOSURE: {
                if( !main_activity.hasExposureSeekbarValues() )
                    return null;
                long value = sliderValueAt(TAB_EXPOSURE, step);   // Sechstel-EV
                // nur ganze EV anschreiben: "+2,83" ist keine Zahl, die man ansagen kann
                return value % EV_SIXTHS == 0 ? exposureLabel((int) value) : null;
            }
            case TAB_WB: {
                int temperature = main_activity.getManualSeekbars().getWhiteBalanceTemperature(step);
                return temperature % WB_LABEL_STEP == 0 ? String.valueOf(temperature) : null;
            }
            case TAB_ISO:
                return String.valueOf(isoLadder()[step]);
            case TAB_SHUTTER:
                return niceExposureLabel(main_activity.getManualSeekbars().getExposureTime(step));
            case TAB_FOCUS: {
                double metres = focusLadder()[step];
                return metres > 0.0 ? formatMetres(metres)
                        : main_activity.getResources().getString(R.string.pro_panel_infinite_short);
            }
            default:
                return null;
        }
    }

    /** Der Wert an einem beliebigen Schritt (die Zwischenzahl zwischen zwei runden Werten) - nur
     *  bei der Verschlusszeit: ISO und Fokus haben eine Leiter aus lauter runden Werten, dort gibt
     *  es keine Luecke zu fuellen. */
    private String valueLabel(int tab, int step) {
        if( tab == TAB_SHUTTER )
            return exposureTimeText(main_activity.getManualSeekbars().getExposureTime(step));
        return null;
    }

    /** Die Schaerfestellung eines Schritts des Fokus-Reglers in Dioptrien (0 = unendlich, wie
     *  {@code Preview.getMinimumFocusDistance()}). */
    private double focusDioptres(int progress) {
        Preview preview = main_activity.getPreview();
        if( preview == null || focus_seekbar.getMax() <= 0 )
            return 0.0;
        // der Regler ist in Dioptrien geteilt: Wert = Brennweite * Skalierung
        return ManualSeekbars.seekbarScaling(progress / (double) focus_seekbar.getMax())
                * preview.getMinimumFocusDistance();
    }

    /** Eine Entfernung als kurze Zahl: bis 10 m mit Nachkommastelle, darueber in ganzen Metern
     *  (niemand sagt "302,2 Meter"). Die ueberfluessige Null faellt weg - "0,50" ist keine Zahl,
     *  die man ansagt (Falk 2026-09-21). */
    private static String formatMetres(double metres) {
        String text = String.format(java.util.Locale.getDefault(), metres < 10.0 ? "%.2f" : "%.0f", metres);
        int sep = text.indexOf(',') >= 0 ? text.indexOf(',') : text.indexOf('.');
        if( sep < 0 )
            return text;
        int end = text.length();
        while( end > sep + 1 && text.charAt(end - 1) == '0' )
            end--;
        if( end == sep + 1 )
            end = sep;                              // "2,00" -> "2"
        return text.substring(0, end);
    }

    /** Die Belichtungskorrektur als glatte Zahl: ganze EV ohne Nachkommastelle ("+1"), halbe mit
     *  einer ("+0,5") - statt "+2,83" aus der Sechstel-Teilung des Reglers (Falk 2026-09-21). */
    private static String exposureLabel(int value) {
        double ev = value / (double) EV_SIXTHS;
        String sign = ev > 0.0 ? "+" : (ev < 0.0 ? "-" : "");
        double abs = Math.abs(ev);
        String number = (abs == Math.floor(abs)) ? String.valueOf((int) abs)
                : String.format(java.util.Locale.getDefault(), "%.1f", abs);
        return sign + number;
    }

    /** Der Text fuer einen Wert, den die Kamera selbst stellt. */
    private String autoText() {
        return main_activity.getResources().getString(R.string.pro_panel_auto_value);
    }

    /** Die ISO-Werte, die eine Zahl bekommen: die Stufen, die ein Fotograf ansagt (25, 50, 100,
     *  200, 400, 800 ...) - jede zweite Stufe der Leiter bleibt damit ohne Zahl, sonst klebten die
     *  Zahlen aneinander (Falk 2026-09-21). */
    private static boolean isStopISO(long iso) {
        if( iso <= 0 )
            return false;
        while( iso > 25 && iso % 2 == 0 )
            iso /= 2;
        return iso == 25;
    }

    /** Die Verschlusszeiten, die eine Zahl bekommen: die Nenner bzw. Sekunden der Kamera-Skala. */
    private static String niceExposureLabel(long exposure_ns) {
        if( exposure_ns <= 0 )
            return null;
        if( exposure_ns < 1000000000L ) {
            if( 1000000000L % exposure_ns != 0 )
                return null; // krumme Nenner wie 1/45 s bleiben ohne Zahl
            long denominator = 1000000000L / exposure_ns;
            return contains(NICE_DENOMINATORS, denominator) ? String.valueOf(denominator) : null;
        }
        if( exposure_ns % 1000000000L != 0 )
            return null;
        long seconds = exposure_ns / 1000000000L;
        return contains(NICE_SECONDS, seconds) ? String.valueOf(seconds) : null;
    }

    /** Eine Verschlusszeit als kurze Zahl: der Nenner bei kurzen Zeiten (250 = 1/250 s),
     *  sonst die Sekunden - so schreibt es die Referenz. */
    private static String exposureTimeText(long exposure_ns) {
        if( exposure_ns <= 0 )
            return null;
        if( exposure_ns < 1000000000L )
            return String.valueOf(Math.round(1000000000.0 / exposure_ns));
        if( exposure_ns % 1000000000L == 0 )
            return String.valueOf(exposure_ns / 1000000000L);
        return String.format(java.util.Locale.getDefault(), "%.1f", exposure_ns / 1000000000.0);
    }

    private static boolean contains(long [] values, long value) {
        for(long entry : values) {
            if( entry == value )
                return true;
        }
        return false;
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

    /** Der runde Knopf neben der Pille: den aktiven Reiter auf den Standard zurueck.
     *
     *  <p>Weissabgleich und Fokus gehen dabei <b>nicht</b> auf Automatik: die App hat fuer die
     *  Farbtemperatur einen eigenen Standardwert (5000 K), und ein automatischer Weissabgleich
     *  liesse sich am Massband nicht anzeigen - dort stuende dann eine Temperatur, die die Kamera
     *  gar nicht benutzt (Falk 2026-09-21). ISO, Verschlusszeit und Belichtung haben dagegen
     *  einen echten Auto-Zustand, den die Blase auch anzeigt.</p> */
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
                applicationInterface.setWhiteBalancePref("manual");
                applicationInterface.setWhiteBalanceTemperaturePref(WB_DEFAULT_TEMPERATURE);
                break;
            case TAB_FOCUS:
                applicationInterface.setFocusPref("focus_mode_continuous_picture", preview.isVideo());
                // die Kamera sucht wieder selbst: die Vormerkung eines Zuges ist damit hinfaellig
                focus_manual_pending = false;
                break;
            case TAB_SHUTTER:
            case TAB_ISO:
                // ISO und Verschlusszeit sind eine gemeinsame Einstellung: Auto heisst beide auf Auto
                applicationInterface.setISOPref(CameraController.ISO_DEFAULT);
                applicationInterface.setExposureTimePref(CameraController.EXPOSURE_TIME_DEFAULT);
                break;
        }
        main_activity.updateForSettings(true, "");
        // den Regler des Panels selbst auf den Standardwert stellen: die App stellt ihre SeekBars
        // beim Aktualisieren nicht in jedem Fall mit, und dann saehe der Knopf wirkungslos aus
        // (Falk 2026-09-21: "bei Weissabgleich funktioniert Reset nicht")
        switch( selected_tab ) {
            case TAB_EXPOSURE:
                setSliderValue(TAB_EXPOSURE, 0);
                break;
            case TAB_WB:
                setSliderValue(TAB_WB, WB_DEFAULT_TEMPERATURE);
                break;
            case TAB_FOCUS:
                // zurueck auf Automatik: der Wert kommt wieder von der Kamera
                setFocusSeekbarProgress(false);
                break;
            default:
                // ISO und Verschlusszeit: die App stellt ihre Regler auf die gemessenen Auto-Werte
                break;
        }
        updateAutoButton();
        updateRuler();
        updateBadge();
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
        focus_manual_pending = false;
        applicationInterface.setISOPref(CameraController.ISO_DEFAULT);
        applicationInterface.setExposureTimePref(CameraController.EXPOSURE_TIME_DEFAULT);
        if( main_activity.getApplicationInterface().getFocusPeakingPref() )
            // der Fokus-Peaking-Knopf der Icon-Leiste schaltet um, nicht ein - nur druecken, wenn
            // es an ist (Falk 2026-09-21: "bei reset fuer alle soll fokus peaking aus")
            main_activity.getMainUI().getOnScreenIcons().clickedFocusPeaking();
        main_activity.updateForSettings(true, "");
        // den Regler des offenen Reiters mitstellen - die App tut es nicht in jedem Fall (wie
        // clickedAuto). setSliderValue() arbeitet immer auf dem offenen Reiter.
        if( selected_tab == TAB_EXPOSURE )
            setSliderValue(TAB_EXPOSURE, 0);
        else if( selected_tab == TAB_FOCUS )
            setFocusSeekbarProgress(false);
        markTabs();
        preview.showToast(null, main_activity.getResources().getString(R.string.pro_panel_reset_done), 0, true);
        updateAutoButton();
        updateRuler();
        updateBadge();
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
        ApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        String text = null;
        switch( selected_tab ) {
            case TAB_EXPOSURE: {
                // der Wert des Reglers im Panel, nicht der der Kamera: der Regler ist die
                // Anzeige, die Falk im Blick hat
                SeekBar seekbar = main_activity.findViewById(R.id.exposure_seekbar);
                int value = main_activity.hasExposureSeekbarValues()
                        ? main_activity.getExposureSeekbarValue(seekbar.getProgress())
                        : preview.getCurrentExposure();
                text = exposureLabel(value);
                break;
            }
            case TAB_WB: {
                if( !applicationInterface.getWhiteBalancePref().equals("manual") ) {
                    // die Kamera sucht die Temperatur selbst: am Massband stuende sonst eine Zahl,
                    // die nichts bedeutet (Falk 2026-09-21)
                    text = autoText();
                    break;
                }
                int temperature = manualSeekbars.getWhiteBalanceTemperature(((SeekBar) main_activity.findViewById(R.id.white_balance_seekbar)).getProgress());
                text = temperature + " K";
                break;
            }
            case TAB_FOCUS: {
                // Dioptrien -> Meter, wie Preview.setFocusDistance() es fuer den Toast macht
                double dioptres = focusDioptres(focus_seekbar.getProgress());
                text = dioptres > 0.0
                        ? formatMetres(1.0 / dioptres) + " " + main_activity.getResources().getString(R.string.metres_abbreviation)
                        : main_activity.getResources().getString(R.string.infinite);
                break;
            }
            case TAB_SHUTTER:
                // ISO und Verschlusszeit sind eine gemeinsame Einstellung: "Auto" ist der
                // ISO-Standard der App (getExposureTimePref() kennt keinen Auto-Zustand)
                if( applicationInterface.getISOPref().equals(CameraController.ISO_DEFAULT) ) {
                    text = autoText();
                    break;
                }
                text = preview.getExposureTimeString(manualSeekbars.getExposureTime(((SeekBar) main_activity.findViewById(R.id.exposure_time_seekbar)).getProgress()));
                break;
            case TAB_ISO:
                if( applicationInterface.getISOPref().equals(CameraController.ISO_DEFAULT) ) {
                    text = autoText();
                    break;
                }
                text = preview.getISOString(manualSeekbars.getISO(((SeekBar) main_activity.findViewById(R.id.iso_seekbar)).getProgress()));
                break;
        }
        if( text == null ) {
            badge.setVisibility(View.GONE);
        }
        else {
            text = text.trim();  // die App-Strings der Belichtungszeit beginnen mit einem Leerzeichen
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
        // kein ensureModeForTab() hier: das Oeffnen des Panels darf die Kamera nicht umstellen
        // (der Belichtungs-Reiter haette sonst bei jedem Oeffnen ISO und Verschlusszeit auf
        // Automatik gezogen - Falk 2026-09-21)
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
