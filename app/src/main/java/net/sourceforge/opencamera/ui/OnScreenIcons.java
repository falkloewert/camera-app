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

    /** Die Zustandsanzeigen der Kopfzeile, in der Reihenfolge, in der sie dort stehen. */
    private static final int [] HEADER_INDICATORS = new int [] {
            R.id.cycle_raw,
            R.id.cycle_flash,
            R.id.store_location,
            R.id.cycle_lock_orientation,
            R.id.text_stamp,
    };

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
        // n.b., read from preview rather than saved application preference - so the icon updates correctly when in flash
        // auto mode, but user switches to manual ISO where flash auto isn't supported
        String flash_value = main_activity.getPreview().getCurrentFlashValue();
        ImageButton view = main_activity.findViewById(R.id.cycle_flash);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        return sharedPreferences.getBoolean(PreferenceKeys.ShowCycleRawPreferenceKey, false);
    }

    boolean showCycleFlashIcon() {
        // Der Blitz-Zustand steht immer da, sobald das Geraet einen Blitz hat - unabhaengig vom
        // Modus und von der Blitz-Einstellung (Falk 2026-09-22: "soll immer da stehen"). Die
        // beiden frueheren Sperren sind damit weg: im Video war die Anzeige aus, weil das Symbol
        // ein Knopf war und Torch sich darueber nicht schalten liess; und die Preference
        // "Blitz-Symbol anzeigen" ist entfallen, weil das Symbol seit Schritt C rein visuell ist
        // (der Zustand selbst wird weiter im Blitz-Blatt gesetzt).
        return main_activity.getPreview().supportsFlash();
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

    public void clickedCycleFlash() {
        if( MyDebug.LOG )
            Log.d(TAG, "clickedCycleFlash");

        main_activity.getPreview().cycleFlash(true, true);
        updateCycleFlashIcon();
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
