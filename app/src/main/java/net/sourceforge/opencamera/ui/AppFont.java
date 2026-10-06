package net.sourceforge.opencamera.ui;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import net.sourceforge.opencamera.R;

/** Die eine Stelle, an der die Schrift der Bedienoberflaeche herkommt: Roboto Flex (SPEC 4.5,
 *  OFL) mit einem Gewicht. Das Theme setzt die Familie bereits app-weit
 *  (res/values/themes.xml); diese Klasse ist fuer die Stellen noetig, die ihr Typeface selbst
 *  bauen - Canvas-Text und die Pillen-Stile.
 *
 *  Gewicht 600 statt des frueheren {@link Typeface#BOLD} = 700, siehe ui-spezifikation.md 7.3.
 *  Die Gewichtsachse einer variablen Schrift wertet Android erst ab API 28 aus; auf aelteren
 *  Geraeten faellt die Auswahl auf die naechste vorhandene Instanz zurueck.
 */
final class AppFont {

    /** Standardgewicht der Bedienoberflaeche - ersetzt BOLD (700). */
    static final int WEIGHT_SEMIBOLD = 600;

    /** Fuer nicht ausgewaehlte Pillen-Eintraege. Nicht {@link Typeface#NORMAL} (0): die
     *  Gewichtsachse hat den Wertebereich 1 bis 1000. */
    static final int WEIGHT_REGULAR = 400;

    /** Fuer die Reiter des Einstellungen-Blattes, die vorher "sans-serif-medium" (500) waren. */
    static final int WEIGHT_MEDIUM = 500;

    private AppFont() {
    }

    static Typeface at(Context context, int weight) {
        return Typeface.create(ResourcesCompat.getFont(context, R.font.roboto_flex), weight, false);
    }
}
