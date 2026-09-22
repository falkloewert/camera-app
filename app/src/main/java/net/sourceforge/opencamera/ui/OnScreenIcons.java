package net.sourceforge.opencamera.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyApplicationInterface;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.R;
import net.sourceforge.opencamera.ToastBoxer;
import net.sourceforge.opencamera.preview.ApplicationInterface;

import java.util.Arrays;

/** Zustandsanzeigen der Kopfzeile.
 *
 *  Seit dem Redesign (SPEC 4.5, Schritt C) gibt es die alte Icon-Leiste nicht mehr. Von den
 *  frueheren On-Screen-Icons sind nur die fuenf reinen Zustandsanzeigen geblieben - sie sitzen
 *  jetzt rechts im Kopfband, direkt neben dem Aufloesungs-Chip:
 *  Blitz, RAW, GPS, Ausrichtungssperre, Textstempel.
 *
 *  Alle uebrigen Icons sind entfallen: entweder doppelt (das Einstellungen-Blatt bzw. das
 *  Pro-Panel bietet sie an) oder in der vollen Einstellungen-Aktivitaet zu Hause.
 *
 *  To add a new header indicator:
 *  - Add the button to activity_main.xml inside header_bar.
 *  - Add its id to HEADER_INDICATORS below.
 *  - If the icon image depends on something persistent, add a new update*Icon() method and call
 *    it from updateOnScreenIcons().
 *  - Add a clicked*() method, and call this from a corresponding onClick method in MainActivity.
 */
public class OnScreenIcons {
    private static final String TAG = "OnScreenIcons";

    private final MainActivity main_activity;

    private final ToastBoxer store_location_toast = new ToastBoxer();
    private final ToastBoxer cycle_lock_orientation_toast = new ToastBoxer();
    private final ToastBoxer cycle_timer_toast = new ToastBoxer();

    /** Die Zustandsanzeigen der Kopfzeile, in der Reihenfolge, in der sie dort stehen. Der Blitz
     *  steht zuletzt: er sitzt am rechten Rand des Bandes, und bei Nacht traegt er den Mond
     *  (Falk, Schritt G Punkt 4: "er soll auch nach oben rechts wo sonnst blitz ist"). */
    private static final int [] HEADER_INDICATORS = new int [] {
            R.id.cycle_raw,
            R.id.store_location,
            R.id.cycle_lock_orientation,
            R.id.text_stamp,
            R.id.face_detection,
            R.id.auto_stabilise,
            R.id.audio_disabled,
            R.id.cycle_timer,
            R.id.cycle_flash,
    };

    /** Die Timer-Modi des Chips, in der Reihenfolge, in der er sie durchtippt. Dieselben vier wie
     *  die Timer-Zeile des Blattes (SettingsSheet.TIMER_VALUES). */
    private static final String [] TIMER_VALUES = {"0", "3", "5", "10"};
    private static final int [] TIMER_ICONS = {R.drawable.ms_timer_off, R.drawable.ms_timer_3,
            R.drawable.ms_timer_5, R.drawable.ms_timer_10};

    public OnScreenIcons(MainActivity main_activity) {
        if( MyDebug.LOG )
            Log.d(TAG, "OnScreenIcons");
        this.main_activity = main_activity;
    }

    public void updateOnScreenIcons() {
        if( MyDebug.LOG )
            Log.d(TAG, "updateOnScreenIcons");
        this.updateCycleRawIcon();
        this.updateCycleFlashIcon();
        this.updateStoreLocationIcon();
        this.updateCycleLockOrientationIcon();
        this.updateTextStampIcon();
        this.updateCycleTimerIcon();
    }

    /** Der Nacht-Zustand der Lichtzeile (Schritt E/F), wie ihn der Mond-Chip zeigt: 0 = keine
     *  Nacht, sonst das Symbol - "Nacht an" den vollen Mond, "Nacht auto" den Mond mit dem A
     *  (Falk, Schritt G Punkt 4: der Mond von "auto nacht" stand ohne A da). */
    private int nightIcon() {
        // Die Nacht gibt es nur in Foto und Panorama (Schritt F): im Video laeuft die
        // Nacht-Erweiterung nicht (MyApplicationInterface.getPhotoMode() antwortet dort nie
        // X_Night), der gemerkte Zustand ist dort also nur noch ein Rest - der Mond stand sonst
        // ueber dem Blitz-Chip, der im Video die Taschenlampe schaltet (Schritt G).
        if( main_activity.getPreview().isVideo() )
            return 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        // Die Automatik zuerst: sie ist der staerkere Zustand der beiden und der einzige, der
        // beide Schalter true stehen laesst - die Automatik schaltet die Nacht selbst ein, sobald
        // es dunkel wird (MainUI.checkNightAuto() schreibt night_on neben dem eigenen Schalter).
        // Genau daran fehlte das "A" (Falk, Schritt G Punkt 4).
        if( sharedPreferences.getBoolean(PreferenceKeys.NightAutoPreferenceKey, false) )
            return R.drawable.ms_night_sight_auto;
        if( sharedPreferences.getBoolean(PreferenceKeys.NightOnPreferenceKey, false) )
            return R.drawable.ms_dark_mode;
        return 0;
    }

    private void updateCycleRawIcon() {
        ApplicationInterface.RawPref raw_pref = main_activity.getApplicationInterface().getRawPref();
        ImageButton view = main_activity.findViewById(R.id.cycle_raw);
        if( raw_pref == ApplicationInterface.RawPref.RAWPREF_JPEG_DNG ) {
            if( main_activity.getApplicationInterface().isRawOnly() ) {
                // actually RAW only
                view.setImageResource(R.drawable.raw_only_icon);
            }
            else {
                view.setImageResource(R.drawable.raw_icon);
            }
        }
        else {
            view.setImageResource(R.drawable.raw_off_icon);
        }
    }

    private void updateCycleFlashIcon() {
        ImageButton view = main_activity.findViewById(R.id.cycle_flash);
        // Bei Nacht traegt der Platz des Blitzes dessen Mond (Schritt G Punkt 4): der Zustand
        // steht sonst nirgends mehr, seit die Icon-Reihe des Suchers weg ist (Punkt 3). Der
        // Blitzwert selbst ist dann ohne Bedeutung - das Blatt schaltet beides gegeneinander.
        int night_icon = nightIcon();
        if( night_icon != 0 ) {
            view.setImageResource(night_icon);
            view.setContentDescription(main_activity.getResources().getString(
                    night_icon == R.drawable.ms_night_sight_auto ? R.string.settings_sheet_night_auto : R.string.settings_sheet_night_on));
            return;
        }
        view.setContentDescription(main_activity.getResources().getString(R.string.cycle_flash));
        // n.b., read from preview rather than saved application preference - so the icon updates correctly when in flash
        // auto mode, but user switches to manual ISO where flash auto isn't supported
        String flash_value = main_activity.getPreview().getCurrentFlashValue();
        if( flash_value == null ) {
            view.setImageResource(R.drawable.flash_off);
            return;
        }
        switch( flash_value ) {
            case "flash_off":
                view.setImageResource(R.drawable.flash_off);
                break;
            case "flash_auto":
            case "flash_frontscreen_auto":
                view.setImageResource(R.drawable.flash_auto);
                break;
            case "flash_on":
            case "flash_frontscreen_on":
                view.setImageResource(R.drawable.flash_on);
                break;
            case "flash_torch":
            case "flash_frontscreen_torch":
                view.setImageResource(R.drawable.baseline_highlight_white_48);
                break;
            case "flash_red_eye":
                view.setImageResource(R.drawable.baseline_remove_red_eye_white_48);
                break;
            default:
                // just in case??
                Log.e(TAG, "unknown flash value " + flash_value);
                view.setImageResource(R.drawable.flash_off);
                break;
        }
    }

    private void updateStoreLocationIcon() {
        ImageButton view = main_activity.findViewById(R.id.store_location);
        boolean enabled = main_activity.getApplicationInterface().getGeotaggingPref();
        view.setImageResource(enabled ? R.drawable.ic_gps_fixed_red_48dp : R.drawable.ic_gps_fixed_white_48dp);
        view.setContentDescription( main_activity.getResources().getString(enabled ? R.string.preference_location_disable : R.string.preference_location_enable) );
    }

    private void updateCycleLockOrientationIcon() {
        ImageButton view = main_activity.findViewById(R.id.cycle_lock_orientation);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        String pref = sharedPreferences.getString(PreferenceKeys.LockOrientationPreferenceKey, "none");

        switch( pref ) {
            case "portrait":
                view.setImageResource(R.drawable.mobile_lock_portrait_48px_red);
                break;
            case "landscape":
                view.setImageResource(R.drawable.mobile_lock_landscape_48px_red);
                break;
            case "none":
                view.setImageResource(R.drawable.mobile_unlock_48px);
                break;
            default:
                // just in case??
                Log.e(TAG, "unknown lock orientation " + pref);
                view.setImageResource(R.drawable.mobile_unlock_48px);
                break;
        }
    }

    private void updateTextStampIcon() {
        ImageButton view = main_activity.findViewById(R.id.text_stamp);
        boolean enabled = !main_activity.getApplicationInterface().getTextStampPref().isEmpty();
        view.setImageResource(enabled ? R.drawable.baseline_text_fields_red_48 : R.drawable.baseline_text_fields_white_48);
    }

    /** Setzt die Sichtbarkeit aller Zustandsanzeigen der Kopfzeile.
     *  Anzeigen, die das Geraet oder die Einstellungen nicht hergeben, bleiben aus.
     * @param visibility Visibility flag to use for the header band.
     */
    void setHeaderVisibility(int visibility) {
        // Beim Einblenden die Symbole auffrischen: der Zustand des Mond-Chips (Schritt G Punkt 4)
        // und der uebrigen Anzeigen aendert sich auch ohne Zutun des Nutzers - die Nacht-Automatik
        // etwa schaltet selbst um, und der Sucher zeichnet diese Reihe nicht mehr, die sie frueher
        // von allein aktuell hielt. showGUI() ist die Stelle, an der jede Aenderung
        // vorbeikommt (Kamera-Aufbau, Blatt, Moduswechsel).
        if( visibility == View.VISIBLE )
            updateOnScreenIcons();
        main_activity.findViewById(R.id.header_indicators).setVisibility(visibility);
        for(int id : HEADER_INDICATORS) {
            View view = main_activity.findViewById(id);
            if( visibility == View.VISIBLE && showIndicator(id) )
                view.setVisibility(View.VISIBLE);
            else
                view.setVisibility(View.GONE);
        }
    }

    private boolean showIndicator(int id) {
        if( id == R.id.cycle_raw )
            return showCycleRawIcon();
        if( id == R.id.cycle_flash )
            return showCycleFlashIcon();
        if( id == R.id.store_location )
            return showStoreLocationIcon();
        if( id == R.id.cycle_lock_orientation )
            return showCycleLockOrientationIcon();
        if( id == R.id.text_stamp )
            return showTextStampIcon();
        if( id == R.id.face_detection )
            return showFaceDetectionIcon();
        if( id == R.id.auto_stabilise )
            return showAutoStabiliseIcon();
        if( id == R.id.audio_disabled )
            return showAudioDisabledIcon();
        if( id == R.id.cycle_timer )
            return showCycleTimerIcon();
        return false;
    }

    /** Disables the optional on-screen icons if either user doesn't want to enable them, or not
     *  supported). Note that displaying icons is done via MainUI.showGUI.
     * @return Whether an icon's visibility was changed.
     */
    public boolean checkDisableGUIIcons() {
        if( MyDebug.LOG )
            Log.d(TAG, "checkDisableGUIIcons");
        boolean changed = false;
        for(int id : HEADER_INDICATORS) {
            if( !showIndicator(id) ) {
                View button = main_activity.findViewById(id);
                changed = changed || (button.getVisibility() != View.GONE);
                button.setVisibility(View.GONE);
            }
        }
        if( !main_activity.showSwitchMultiCamIcon() ) {
            // also handle the multi-cam icon here, as this can change when switching between front/back cameras
            // (e.g., if say a device only has multiple back cameras)
            View button = main_activity.findViewById(R.id.switch_multi_camera);
            changed = changed || (button.getVisibility() != View.GONE);
            button.setVisibility(View.GONE);
        }
        if( MyDebug.LOG )
            Log.d(TAG, "checkDisableGUIIcons: " + changed);
        return changed;
    }

    private boolean showCycleRawIcon() {
        if( !main_activity.getPreview().supportsRaw() )
            return false;
        if( !main_activity.getApplicationInterface().isRawAllowed(main_activity.getApplicationInterface().getPhotoMode()) )
            return false;
        // Der Chip traegt den RAW-Zustand, keine eigene Anzeige-Einstellung (Falk, Schritt H
        // Punkt 4: "den nur zeigen wenn raw modus an ist, bei aus kein chip"). Die Preference
        // ShowCycleRawPreferenceKey stammt aus der alten Sucher-Reihe und steuerte dort das
        // Symbol unabhaengig vom Zustand - im Band ist sie damit ohne Wirkung.
        return main_activity.getApplicationInterface().getRawPref() == ApplicationInterface.RawPref.RAWPREF_JPEG_DNG;
    }

    boolean showCycleFlashIcon() {
        // Der Blitz-Zustand steht immer da, sobald das Geraet einen Blitz hat - unabhaengig vom
        // Modus und von der Blitz-Einstellung (Falk 2026-09-22: "soll immer da stehen"). Die
        // beiden frueheren Sperren sind damit weg: im Video war die Anzeige aus, weil das Symbol
        // ein Knopf war und Torch sich darueber nicht schalten liess; und die Preference
        // "Blitz-Symbol anzeigen" ist entfallen, weil das Symbol seit Schritt C rein visuell ist
        // (der Zustand selbst wird weiter im Blitz-Blatt gesetzt).
        // Bei Nacht traegt der Platz den Mond (Schritt G Punkt 4) - der steht auch auf einem
        // Geraet ohne Blitz, die Nacht braucht keinen.
        if( nightIcon() != 0 )
            return true;
        return main_activity.getPreview().supportsFlash();
    }

    /** Das Symbol des Timer-Chips: aus, 3, 5 oder 10 Sekunden. */
    private void updateCycleTimerIcon() {
        ImageButton view = main_activity.findViewById(R.id.cycle_timer);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        String current = sharedPreferences.getString(PreferenceKeys.TimerPreferenceKey, "0");
        int index = Arrays.asList(TIMER_VALUES).indexOf(current);
        if( index < 0 )
            index = 0; // eine Zahl, die der Chip nicht kennt (z.B. die 15 des Popups): zeigen wir als "aus"
        view.setImageResource(TIMER_ICONS[index]);
        view.setContentDescription(main_activity.getResources().getString(R.string.settings_sheet_timer) + ": " +
                (index == 0 ? main_activity.getResources().getString(R.string.settings_sheet_off)
                        : main_activity.getResources().getString(R.string.settings_sheet_seconds, TIMER_VALUES[index])));
    }

    private boolean showStoreLocationIcon() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        return sharedPreferences.getBoolean(PreferenceKeys.ShowStoreLocationPreferenceKey, false);
    }

    private boolean showCycleLockOrientationIcon() {
        if( main_activity.getApplicationInterface().getPhotoMode() == MyApplicationInterface.PhotoMode.Panorama )
            return false; // see MyApplicationInterface.getLockOrientationPref(): for now panorama only supports portrait
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        return sharedPreferences.getBoolean(PreferenceKeys.ShowCycleLockOrientationPreferenceKey, false);
    }

    private boolean showTextStampIcon() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        return sharedPreferences.getBoolean(PreferenceKeys.ShowTextStampPreferenceKey, false);
    }

    /** Gesichtserkennung an - dieselbe Bedingung, unter der die Sucher-Reihe das Symbol zeigte:
     *  der Zustand aus den Einstellungen, und nur wenn die Kamera es kann. */
    private boolean showFaceDetectionIcon() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        return sharedPreferences.getBoolean(PreferenceKeys.FaceDetectionPreferenceKey, false) &&
                main_activity.getPreview().supportsFaceDetection();
    }

    /** Auto-Ausrichten an (Foto) - das Symbol stand frueher in der Sucher-Reihe, sobald die
     *  Kamera das Ausrichten per Wasserwaage kann. */
    private boolean showAutoStabiliseIcon() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        return sharedPreferences.getBoolean(PreferenceKeys.AutoStabilisePreferenceKey, false) &&
                main_activity.getPreview().hasLevelAngleStable();
    }

    /** Ton aus im Video (Aufnahme-Ton abgeschaltet) - wie das stumme Mikrofon der Sucher-Reihe. */
    private boolean showAudioDisabledIcon() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        return !sharedPreferences.getBoolean(PreferenceKeys.RecordAudioPreferenceKey, true) &&
                main_activity.getApplicationInterface().isVideoPref();
    }

    /** Der Timer-Chip steht immer da, wo es den Timer gibt - er ist der einzige Chip, den man
     *  antippt, und ohne ihn waere der Timer nur ueber das Blatt erreichbar. Im Panorama fehlt er
     *  wie die Timer-Zeile des Blattes (SettingsSheet). */
    private boolean showCycleTimerIcon() {
        return main_activity.getApplicationInterface().getPhotoMode() != MyApplicationInterface.PhotoMode.Panorama;
    }

    public void clickedCycleRaw() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedCycleRaw");

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        String new_value = null;
        switch( sharedPreferences.getString(PreferenceKeys.RawPreferenceKey, "preference_raw_no") ) {
            case "preference_raw_no":
                new_value = "preference_raw_yes";
                break;
            case "preference_raw_yes":
                new_value = "preference_raw_only";
                break;
            case "preference_raw_only":
                new_value = "preference_raw_no";
                break;
            default:
                Log.e(TAG, "unrecognised raw preference");
                break;
        }
        if( new_value != null ) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PreferenceKeys.RawPreferenceKey, new_value);
            editor.apply();

            updateCycleRawIcon();
            main_activity.getApplicationInterface().getDrawPreview().updateSettings();
            main_activity.getPreview().reopenCamera(); // needed for RAW options to take effect
        }
    }

    public void clickedStoreLocation() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedStoreLocation");
        boolean value = main_activity.getApplicationInterface().getGeotaggingPref();
        value = !value;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferenceKeys.LocationPreferenceKey, value);
        editor.apply();

        updateStoreLocationIcon();
        main_activity.getApplicationInterface().getDrawPreview().updateSettings(); // because we cache the geotagging setting
        main_activity.initLocation(); // required to enable or disable GPS, also requests permission if necessary

        String message = main_activity.getResources().getString(R.string.preference_location) + ": " + main_activity.getResources().getString(value ? R.string.on : R.string.off);
        main_activity.getPreview().showToast(store_location_toast, message, true);
    }

    public void clickedCycleLockOrientation() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedCycleLockOrientation");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        String new_value = null;

        switch( sharedPreferences.getString(PreferenceKeys.LockOrientationPreferenceKey, "none") ) {
            case "none":
                new_value = "portrait";
                break;
            case "portrait":
                new_value = "landscape";
                break;
            case "landscape":
                new_value = "none";
                break;
            default:
                Log.e(TAG, "unrecognised lock orientation preference");
                break;
        }

        if( new_value != null ) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PreferenceKeys.LockOrientationPreferenceKey, new_value);
            editor.apply();

            updateCycleLockOrientationIcon();
            {
                String [] entries_array = main_activity.getResources().getStringArray(R.array.preference_lock_orientation_entries);
                String [] values_array = main_activity.getResources().getStringArray(R.array.preference_lock_orientation_values);
                int index = Arrays.asList(values_array).indexOf(new_value);
                if( index != -1 ) { // just in case!
                    main_activity.getPreview().showToast(cycle_lock_orientation_toast, entries_array[index], true);
                }
            }
        }
    }

    /** Tippt den Timer durch: aus -> 3 -> 5 -> 10 -> aus (Schritt G Punkt 6). Geschrieben wird
     *  dieselbe Preference wie in der Timer-Zeile des Blattes, und die Kamera liest sie erst beim
     *  Auslösen (MyApplicationInterface.getTimerPref()) - der Knopf braucht also nichts zu
     *  oeffnen. Die Rueckmeldung ist der Chip selbst plus ein Hinweis wie beim Ausrichtungssperre-Chip. */
    public void clickedCycleTimer() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedCycleTimer");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        String current = sharedPreferences.getString(PreferenceKeys.TimerPreferenceKey, "0");
        int index = Arrays.asList(TIMER_VALUES).indexOf(current);
        String new_value = TIMER_VALUES[(index + 1) % TIMER_VALUES.length];

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.TimerPreferenceKey, new_value);
        editor.apply();

        updateCycleTimerIcon();
        String message = main_activity.getResources().getString(R.string.settings_sheet_timer) + ": " +
                ("0".equals(new_value) ? main_activity.getResources().getString(R.string.settings_sheet_off)
                        : main_activity.getResources().getString(R.string.settings_sheet_seconds, new_value));
        main_activity.getPreview().showToast(cycle_timer_toast, message, true);
    }

    /** Setzt den Textstempel. Das Icon zeigt danach an, ob einer gesetzt ist; der Text selbst
     *  wird in der vollen Einstellungen-Aktivitaet bearbeitet und in diesem Dialog. */
    public void clickedTextStamp() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedTextStamp");

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(main_activity);
        alertDialog.setTitle(R.string.preference_textstamp);

        final View dialog_view = LayoutInflater.from(main_activity).inflate(R.layout.alertdialog_edittext, null);
        final EditText editText = dialog_view.findViewById(R.id.edit_text);
        // set hint instead of content description for EditText, see https://support.google.com/accessibility/android/answer/6378120
        editText.setHint(main_activity.getResources().getString(R.string.preference_textstamp));
        editText.setText(main_activity.getApplicationInterface().getTextStampPref());
        alertDialog.setView(dialog_view);
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if( MyDebug.LOG )
                    Log.d(TAG, "custom text stamp clicked okay");

                String custom_text = editText.getText().toString();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.TextStampPreferenceKey, custom_text);
                editor.apply();

                updateTextStampIcon();
            }
        });
        alertDialog.setNegativeButton(android.R.string.cancel, null);

        final AlertDialog alert = alertDialog.create();
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                if( MyDebug.LOG )
                    Log.d(TAG, "custom stamp text dialog dismissed");
                main_activity.setWindowFlagsForCamera();
                main_activity.showPreview(true);
            }
        });

        main_activity.showPreview(false);
        main_activity.setWindowFlagsForSettings(true);
        main_activity.showAlert(alert);
    }

    /** Fokus-Peaking ein-/ausschalten. Seit Schritt C gibt es dafuer keinen Knopf in der
     *  Kopfzeile mehr - aufgerufen wird das nur noch aus dem Fokus-Reiter des Pro-Panels. */
    public void clickedFocusPeaking() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedFocusPeaking");
        boolean value = main_activity.getApplicationInterface().getFocusPeakingPref();
        value = !value;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.FocusPeakingPreferenceKey, value ? "preference_focus_peaking_on" : "preference_focus_peaking_off");
        editor.apply();

        main_activity.getApplicationInterface().getDrawPreview().updateSettings(); // needed to update focus peaking
    }
}
