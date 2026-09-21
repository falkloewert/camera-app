package net.sourceforge.opencamera.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

import net.sourceforge.opencamera.R;

/**
 * Das Massband der Pro-Panel-Regler (Referenz: Pixel-Kamera, Kickoff redesign-pro-panel.md 3).
 *
 * <p>Die Referenz zeigt keine runde Kugel, die ueber eine feste Skala wandert, sondern ein
 * Massband: die Skala mit Teilstrichen und Zahlen laeuft unter einem <b>feststehenden</b>
 * Mittelbalken durch (am Einzelbild nachgemessen: der Balken sitzt in jeder Sekunde auf x 480
 * = Bildmitte, die Zahlen wandern). Der Abstand zweier Teilstriche ist 18 px = 8 dp, die kurzen
 * Striche sind 7 dp, die hohen 13 dp hoch, die Zahlen stehen unter den hohen Strichen.</p>
 *
 * <p>Das Band gehoert dem Finger: ein Zug daran zieht es genau so weit mit wie der Finger
 * (wie das Zoomrad, {@code ZoomWheel.drag()}: nach links kommen die hoeheren Werte unter den
 * Balken). Waere der SeekBar der App der Bediente, haenge die Empfindlichkeit an dessen
 * Werteliste - ein Zug quer durch die Pille machte bei ISO Hunderte Schritte (Falk
 * 2026-09-21). Der SeekBar wird stattdessen von hier aus gestellt ({@link ProPanel}), seine
 * Zuhoerer in MainActivity wirkten bisher auch ohne Nutzer-Ereignis ({@code fromUser}).</p>
 */
public class RulerSlider extends View {
    private static final float PX_PER_STEP_DP = 8.0f;   // Teilstrich-Abstand (Referenz: 18 px / 2,25)
    private static final float TICK_BOTTOM_DP = 26.0f;  // Unterkante der Striche
    private static final float MINOR_TICK_DP = 7.0f;    // Laenge kurzer Strich
    private static final float MAIN_TICK_DP = 13.0f;    // Laenge hoher Strich
    private static final float TICK_WIDTH_DP = 1.0f;
    private static final float POINTER_TOP_DP = 10.0f;
    private static final float POINTER_HEIGHT_DP = 16.0f;
    private static final float POINTER_WIDTH_DP = 4.4f; // Referenz: 10 px / 2,25
    private static final float LABEL_BASELINE_DP = 40.0f;
    private static final float LABEL_SP_DP = 12.0f;
    private static final float FADE_DP = 24.0f;         // Ausblenden zu den Raendern hin
    /** Das Rad laeuft nicht in Spruengen, sondern faehrt den neuen Wert an: je weiter, desto
     *  laenger, aber immer kurz - so fuehlt sich das Schieben wie ein Drehrad an (Falk
     *  2026-09-21). */
    private static final long ROLL_MIN_MS = 70L, ROLL_MAX_MS = 220L, ROLL_PER_STEP_MS = 35L;

    private final Paint tick_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointer_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fade_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Meldet dem Panel, welcher Schritt unter dem Balken steht (das Panel stellt den SeekBar
     *  der App darauf). */
    public interface OnScrollListener {
        void onScroll(int step);

        /** Der Finger hat das Band losgelassen. */
        void onScrollEnd();
    }

    private float px_per_step_dp = PX_PER_STEP_DP;  // Abstand zweier Striche, je Reiter
    private int max = 0;            // hoechster Wert, den der SeekBar kennt
    private int progress = 0;       // aktueller Wert (derselbe wie im SeekBar)
    private float shown = 0.0f;     // der gezeichnete Wert - zwischen zwei Schritten unterwegs
    private ValueAnimator roll;     // die Fahrt zum neuen Wert
    private String [] labels = new String [0]; // labels[schritt] = Text, sonst null
    private OnScrollListener scroll_listener;
    private boolean dragging;       // der Finger fuehrt gerade
    private float last_x;

    public RulerSlider(Context context) {
        this(context, null);
    }

    public RulerSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        int tick_color = ContextCompat.getColor(context, R.color.settings_sheet_button_glyph);
        tick_paint.setColor(tick_color);
        tick_paint.setStrokeWidth(dp(TICK_WIDTH_DP));
        label_paint.setColor(tick_color);
        label_paint.setTextSize(sp(LABEL_SP_DP));
        label_paint.setTextAlign(Paint.Align.CENTER);
        pointer_paint.setColor(ContextCompat.getColor(context, R.color.settings_sheet_value));
        // Alpha-Maske wie beim Zoom-Massband: die Skala blendet zu beiden Raendern hin aus
        fade_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    /** Der Abstand zweier Striche. ISO und Fokus bekommen mehr davon: ihre Leiter hat vierstellige
     *  Zahlen, die bei 8 dp aneinanderkleben (am Geraet gesehen, Falk 2026-09-21). */
    public void setStepSpacingDp(float dp) {
        this.px_per_step_dp = dp;
        invalidate();
    }

    /** Der hoechste Wert des zugehoerigen SeekBars. */
    public void setMax(int max) {
        this.max = Math.max(max, 0);
        invalidate();
    }

    public int getMax() {
        return max;
    }

    /** Beschriftung: {@code labels[schritt]} ist der Text an diesem Schritt (null = keiner).
     *  Die Abstaende sind bewusst ungleich - die Referenz beschriftet die runden Werte
     *  (100, 200, 400, 800 ISO), nicht jeden n-ten Strich. */
    public void setLabels(String [] labels) {
        this.labels = labels != null ? labels : new String [0];
        invalidate();
    }

    /** Der Wert des zugehoerigen SeekBars: das Massband faehrt ihn an (wie ein Drehrad). */
    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    /** {@code animate=false} springt ohne Fahrt - beim Reiterwechsel, wo die Skala ohnehin
     *  eine andere ist und ein Nachrollen nur verwirren wuerde. */
    public void setProgress(int progress, boolean animate) {
        if( dragging ) {
            // waehrend des Ziehens fuehrt der Finger: was der Regler meldet, ist genau der Wert,
            // den dieses View gerade selbst gestellt hat - nicht nachfahren
            this.progress = progress;
            return;
        }
        if( this.progress == progress ) {
            // derselbe Schritt kann in der neuen Skala ein anderer Wert sein: dann trotzdem
            // neu zeichnen
            if( !animate )
                snapTo(progress);
            return;
        }
        this.progress = progress;
        if( animate )
            rollTo(progress);
        else
            snapTo(progress);
    }

    /** Einen Zwischenwert anfahren: die messende Kamera liefert keine Striche (Fokus), das Band
     *  soll ihr aber stetig folgen - in ganzen Schritten saehe das wie Ruckeln aus (Falk
     *  2026-09-21). {@link #getProgress()} bleibt der gerundete Schritt. */
    public void setValue(float value, boolean animate) {
        if( dragging )
            return;
        progress = Math.round(value);
        if( animate )
            rollTo(value);
        else
            snapTo(value);
    }

    public int getProgress() {
        return progress;
    }

    private void snapTo(float progress) {
        cancelRoll();
        shown = progress;
        invalidate();
    }

    private void rollTo(float target) {
        cancelRoll();
        if( getWindowToken() == null ) {
            // noch nicht im Fenster: ohne Fahrt stellen
            shown = target;
            invalidate();
            return;
        }
        final float from = shown;
        long duration = ROLL_MIN_MS + (long) (Math.abs(target - from) * ROLL_PER_STEP_MS);
        if( duration > ROLL_MAX_MS )
            duration = ROLL_MAX_MS;
        roll = ValueAnimator.ofFloat(from, target);
        roll.setDuration(duration);
        // schnell los, sanft anhalten - das Anhalten ist das, was das Rad ausmacht
        roll.setInterpolator(new DecelerateInterpolator(1.6f));
        roll.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                shown = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        roll.start();
    }

    private void cancelRoll() {
        if( roll != null ) {
            roll.cancel();
            roll = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelRoll();
        super.onDetachedFromWindow();
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.scroll_listener = listener;
    }

    /** Das Band ziehen - dasselbe Gefuehl wie am Zoomrad ({@code ZoomWheel}): es folgt dem Finger
     *  Strich fuer Strich, ohne Fahrt dazwischen. Nach links zieht die hoeheren Werte unter den
     *  Balken, wie beim Rad. */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if( max <= 0 )
            return false;
        switch( event.getActionMasked() ) {
            case MotionEvent.ACTION_DOWN:
                dragging = true;
                last_x = event.getX();
                cancelRoll();
                if( getParent() != null )
                    getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_MOVE: {
                if( !dragging )
                    return false;
                float dx = event.getX() - last_x;
                last_x = event.getX();
                float value = shown - dx / dp(px_per_step_dp);
                if( value < 0.0f )
                    value = 0.0f;
                else if( value > max )
                    value = max;
                shown = value;
                invalidate();
                int step = Math.round(shown);
                if( step != progress ) {
                    progress = step;
                    if( scroll_listener != null )
                        scroll_listener.onScroll(step);
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if( dragging ) {
                    dragging = false;
                    if( scroll_listener != null )
                        scroll_listener.onScrollEnd();
                    // den halben Schritt, auf dem der Finger stehen geblieben ist, einschnappen
                    rollTo(Math.round(shown));
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    /** Der Text, der zu einem Schritt gehoert (null, wenn der Schritt keinen hat). */
    private String labelAt(int step) {
        if( step < 0 || step >= labels.length )
            return null;
        return labels[step];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if( w > 0 ) {
            float fade = Math.min(dp(FADE_DP) / w, 0.5f);
            fade_paint.setShader(new LinearGradient(0, 0, w, 0,
                    new int [] {0x00FFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0x00FFFFFF},
                    new float [] {0.0f, fade, 1.0f - fade, 1.0f},
                    Shader.TileMode.CLAMP));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if( w <= 0 || h <= 0 )
            return;
        float centre_x = w / 2.0f;
        float px_per_step = dp(px_per_step_dp);
        float tick_bottom = dp(TICK_BOTTOM_DP);
        float baseline = dp(LABEL_BASELINE_DP);

        // der sichtbare Bereich in Schritten: der Mittelbalken steht auf dem aktuellen Wert.
        // Gezeichnet wird die unterwegs befindliche Stelle (shown), nicht der Zielwert - die
        // Skala laeuft dadurch stetig und nicht in Achter-dp-Spruengen.
        int half = (int) Math.ceil(centre_x / px_per_step) + 1;
        int from = Math.max(0, (int) Math.floor(shown) - half);
        int to = Math.min(max, (int) Math.ceil(shown) + half);

        int saved = canvas.saveLayer(0, 0, w, h, null);
        for(int step = from; step <= to; step++) {
            float x = centre_x + (step - shown) * px_per_step;
            String label = labelAt(step);
            boolean main = label != null;
            tick_paint.setStrokeWidth(dp(main ? TICK_WIDTH_DP * 1.5f : TICK_WIDTH_DP));
            tick_paint.setAlpha(main ? 255 : 200);
            canvas.drawLine(x, tick_bottom - dp(main ? MAIN_TICK_DP : MINOR_TICK_DP), x, tick_bottom, tick_paint);
            if( main )
                canvas.drawText(label, x, baseline, label_paint);
        }
        // Zu den Raendern hin ausblenden, damit die Skala nicht abrupt abbricht
        canvas.drawRect(0, 0, w, h, fade_paint);
        canvas.restoreToCount(saved);

        // der feststehende Mittelbalken
        float pointer_top = dp(POINTER_TOP_DP);
        float half_width = dp(POINTER_WIDTH_DP) / 2.0f;
        canvas.drawRoundRect(centre_x - half_width, pointer_top, centre_x + half_width,
                pointer_top + dp(POINTER_HEIGHT_DP), half_width, half_width, pointer_paint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
