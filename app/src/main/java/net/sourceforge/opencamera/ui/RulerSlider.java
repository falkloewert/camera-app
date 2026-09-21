package net.sourceforge.opencamera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

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
 * <p>Dieses View zeichnet nur. Bedient wird der Regler von dem SeekBar der App, der unsichtbar
 * darueber liegt ({@link ProPanel}) - nur so kommen echte Nutzer-Ereignisse
 * ({@code fromUser}) bei den Zuhoerern in MainActivity an, und nur die loesen die
 * Kameraeinstellung aus.</p>
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

    private final Paint tick_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointer_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fade_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int max = 0;            // hoechster Wert, den der SeekBar kennt
    private int progress = 0;       // aktueller Wert (derselbe wie im SeekBar)
    private String [] labels = new String [0]; // labels[schritt] = Text, sonst null

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

    /** Der Wert des zugehoerigen SeekBars. */
    public void setProgress(int progress) {
        if( this.progress != progress ) {
            this.progress = progress;
            invalidate();
        }
    }

    public int getProgress() {
        return progress;
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
        float px_per_step = dp(PX_PER_STEP_DP);
        float tick_bottom = dp(TICK_BOTTOM_DP);
        float baseline = dp(LABEL_BASELINE_DP);

        // der sichtbare Bereich in Schritten: der Mittelbalken steht auf dem aktuellen Wert
        int half = (int) Math.ceil(centre_x / px_per_step) + 1;
        int from = Math.max(0, progress - half);
        int to = Math.min(max, progress + half);

        int saved = canvas.saveLayer(0, 0, w, h, null);
        for(int step = from; step <= to; step++) {
            float x = centre_x + (step - progress) * px_per_step;
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
