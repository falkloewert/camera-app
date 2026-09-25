# Mehrpass-Review: Bugs, Instabilitäten, Überengineering (2026-09-24)

6 Durchläufe außerhalb des normalen Chat-Workflows (Gemini 3.1 Pro High via `agy`, nicht Claude):
5 fokussierte Erstpässe gegen den Code-Stand, ein skeptischer Verifikationspass gegen alle
Erstfunde, danach ein Diff-Pass direkt auf `git diff f1667919^..HEAD` (= alle 116 eigenen
Commits seit 2026-08-27, sauber ohne Upstream-Merges dazwischen). Reiner Bericht, nichts
geändert.

**Wichtig für Priorisierung:** `Preshots.java`, `PanoramaProcessor.java` und
`JavaImageFunctionsPanorama.java` liegen außerhalb des eigenen Diffs — die dort gefundenen
Bugs sind Open-Camera-Upstream-Altlasten, keine eigenen Fehler. Der Rest liegt im eigenen Diff
(Material-3-UI: `ModeBar`, `ProPanel`, `SettingsSheet`, `PhotoVideoSwitch`, `RulerSlider`,
`ZoomStepper`, `ZoomWheel`, `SelfieLightView`, plus Umbauten `MainUI`/`DrawPreview`/
`OnScreenIcons`/`MainActivity`).

## Funde — Crash/ANR/OOM

### 1. `onDestroy()` blockiert UI-Thread ohne Timeout
- **Stelle:** `MainActivity.java:1047` → `ImageSaver.java:1037` (`waitUntilDone()`);
  `preview/Preview.java:8312` (`refreshPreviewBitmapTask.get()`, Nachbar-Task Z. 8328 hat
  3000ms-Timeout, dieser nicht).
- **Auslöser:** HDR/Burst-Foto, App sofort schließen/rotieren → ANR, im zweiten Fall
  permanent, wenn der Task hängt.
- **Status:** behoben in `0b3d4b88` (Preview-Task 3000 ms; `ImageSaver.waitUntilDone()` bewusst unbegrenzt – Datenverlust geht vor ANR).

### 2. Panorama-Blending: Clamp auskommentiert (Upstream)
- **Stelle:** `JavaImageFunctionsPanorama.java:824,929,1576,1711`.
- **Auslöser:** Kontrastreiche Szene → `byte`-Wraparound → Schwarz-/Farbartefakte.
- **Status:** behoben in `c7e280ab` (Clamps wieder aktiv).

### 3. Panorama `expandBitmap()`: 3 volle Bildkopien im Speicher (Upstream)
- **Stelle:** `PanoramaProcessor.java:~183-192`.
- **Auslöser:** 12-48MP-Sensor → OOM, Rendering-Thread crasht.
- **Status:** zurückgestellt – `ponytail:`-Marker an `PanoramaProcessor.expandBitmap` (`c7e280ab`).

### 4. Pre-Shot-Encoder: Exception im `finally` verhindert Cleanup (Upstream)
- **Stelle:** `Preshots.java:461,470`.
- **Auslöser:** Speicherplatzmangel/Kamera-Disconnect während Pre-Shot-Aufnahme →
  MediaMuxer- und Filedescriptor-Leak.
- **Status:** behoben in `c7e280ab`.

## Funde — Race Conditions / Lifecycle (Kamera-Kern, eigener Diff wo markiert)

### 5. Kamera-Release ohne Synchronisation
- **Stelle:** `CameraController1.java:114,140` (kein `synchronized`);
  `CameraController2.java:1087,1459` (`onError()` ohne `background_camera_lock`, `release()`
  nutzt ihn).
- **Auslöser:** Error-Callback feuert während UI die Kamera schließt → Double-Release/NPE/
  `IllegalStateException`.
- **Status:** behoben in `0b3d4b88` (`CameraController2`; `CameraController1` unverändert, auf dem Pixel nicht benutzt).

### 6. `ModeBar` stapelt Runnables bei schnellem Touch
- **Stelle:** `ui/ModeBar.java:284` (`settle_check` ohne `removeCallbacks()` davor).
- **Auslöser:** Schnelles Antippen/Wischen → konkurrierende Runnables, Jitter.
- **Status:** behoben in `583de612`.

### 7. Animator ohne Detach-Cleanup
- **Stelle:** `ui/PhotoVideoSwitch.java:122-126` (`glyph_rotation_animator`),
  `ui/ZoomWheel.java:191` (`morph_animator`). Zum Vergleich: `RulerSlider.java` macht das
  Cleanup korrekt.
- **Auslöser:** View-Detach/Activity-Zerstörung während Animation läuft → Leak.
- **Status:** behoben in `583de612`.

### 8. `SettingsSheet`/`ProPanel` schließen nicht bei `onPause()` (eigener Diff)
- **Stelle:** `MainActivity.java:1499`. Die ersetzte `PopupView` hat in `onPause`
  aggressiv `closePopup()` gerufen, die neuen Komponenten nicht.
- **Auslöser:** Settings-Sheet offen, App backgrounden (Kamera-Session stirbt), zurückkommen
  → Sheet refresht gegen neu initialisierende Kamera → stale UI/Crash auf Null-Profildaten.
- **Status:** behoben in `583de612`.

### 9. `SettingsSheet` verzögertes Runnable ohne Cancel (eigener Diff)
- **Stelle:** `ui/SettingsSheet.java:1591` (`update_settings_runnable`, 400-800ms Delay,
  nie in `close()`/`onPause()`/`onDestroy()` gecancelt).
- **Auslöser:** Einstellung antippen, sofort Screen sperren/Zurück → Runnable feuert gegen
  pausierte/zerstörte Activity → Crash-Risiko.
- **Status:** behoben in `583de612`.

## Funde — stille Bildfehler (Upstream, kein Crash, falsches Ergebnis)

### 10. Auto-Stabilisierung ignoriert `ignore_exif_orientation`
- **Stelle:** `PostProcessing.java:122,246,544`.
- **Auslöser:** Pipeline will bewusst unrotierte Rohdaten (z.B. Panorama-Alignment) →
  wird trotzdem EXIF-rotiert → doppelte Rotation bei späterem manuellem Nachdrehen.
- **Status:** verworfen: der zweite Parameter von `loadBitmapWithRotation()` ist `mutable`, nicht `ignore_exif_orientation`; das Flag behandelt `postProcessBitmap()` korrekt (`c7e280ab`).

### 11. Frontkamera-Kippkorrektur verdoppelt bei deaktiviertem Spiegeln
- **Stelle:** `PostProcessing.java:183` (`autoStabilise`, `postRotate(-level_angle)` geht
  von immer folgender Spiegelung aus).
- **Auslöser:** "Frontkamera spiegeln" aus, Selfie mit 10° Neigung → Ergebnis 20° in
  falsche Richtung statt begradigt.
- **Status:** behoben in `c7e280ab` (richtet sich nach `request.mirror`).

## Funde — Remote-Control / Storage

### 12. SAF-History-Löschen ohne Null-Check
- **Stelle:** `SaveLocationHandler.java:335`.
- **Auslöser:** History löschen bei deaktiviertem SAF → NullPointerException.
- **Status:** behoben in `08b92c70`.

### 13. Bluetooth-Reconnect: fehlendes `return`
- **Stelle:** `remotecontrol/BluetoothLeService.java:171-174` (`attemptReconnect()`).
- **Auslöser:** `!is_bound` wird geloggt, aber trotzdem ein Reconnect-`TimerTask`
  geplant → läuft endlos weiter, auch nach Unbind.
- **Status:** behoben in `08b92c70`.

### 14. Bluetooth `close()` überspringt GATT-Cleanup
- **Stelle:** `remotecontrol/BluetoothLeService.java:485-489`.
- **Auslöser:** Fehlende `BLUETOOTH_CONNECT`-Permission (Android 12+) → `close()` kehrt
  früh zurück, `bluetoothGatt.close()` bleibt aus → Leak.
- **Status:** behoben in `08b92c70`.

### 15. Remote-Control-Teardown übersprungen
- **Stelle:** `remotecontrol/BluetoothRemoteControl.java:299-311` (`stopRemoteControl()`
  unter `if(remoteEnabled())`).
- **Auslöser:** Remote-Control in Einstellungen deaktiviert, während Service läuft →
  `unregisterReceiver`/`unbindService` übersprungen → BroadcastReceiver-Leak.
- **Status:** behoben in `08b92c70`.

### 16. Location-Listener-Leak bei fehlenden Permissions
- **Stelle:** `LocationSupplier.java:~275` (`freeLocationListeners()`).
- **Auslöser:** Beide Standort-Permissions entzogen → früher Return, `removeUpdates()`
  übersprungen → Listener bleibt an Context/Activity hängen.
- **Status:** behoben in `08b92c70`.

### 17. Fragiler Pfadaufbau
- **Stelle:** `StorageUtils.java:786` (String-Konkatenation statt `new File(dir, name)`).
- **Status:** behoben in `08b92c70` (kosmetisch).

## Ponytail-Lens — Überengineering (nicht als "Bug" gegengeprüft)

1. `MyApplicationInterface.java` (~150-1000+): hunderte fast identische
   `getXPref()`/`setXPref()` → generisches `getSharedPref(key, default)` ersetzt tausende
   Zeilen.
2. `preview/Preview.java:2618-3483` (`setupCameraParameters`, 865 Zeilen): repetitives
   "check support → get pref → set camera → save pref" pro Parameter → generischer Helper.
3. `ui/MainUI.java:605-979` (`layoutUI()`, 375 Zeilen manuelles `ConstraintSet`) — deckt
   sich mit Fund 2 aus dem Ponytail-Review vom 2026-09-18, weiterhin offen.
4. `PanoramaProcessor.java:158-250`: manuelles Byte-Array-Scaling statt
   `Bitmap.createScaledBitmap()`.
5. `preview/Preview.java`: `if(MyDebug.LOG) Log.d(...)` >700x statt `MyDebug.d()`-Helper.
6. `MyApplicationInterface.java:2395-2665`: 270-Zeilen-`SubtitleVideoTimerTask` inline
   statt eigener Klasse.
7. `preview/Preview.java:2645-2814`: 170-Zeilen-`MyFaceDetectionListener` inline.
8. `ui/DrawPreview.java:709-759`: ~30 Preferences einzeln in Felder kopiert statt
   Settings-Objekt.
9. `MyApplicationInterface.java:3618`: manuelles `System.gc()` — Anti-Pattern, entfernen.

## Geprüft und verworfen
- **`ui/ProPanel.java` Badge-Poll-Leak (Erstfund):** `close()` ruft
  `handler.removeCallbacks(badge_poll)` bereits korrekt auf. Kein Bug.
- **`ExifHandler.java:242-243` Apertur/FNumber-Verwechslung (Erstfund):** Code behandelt
  `TAG_MAX_APERTURE_VALUE` und `TAG_F_NUMBER` getrennt und korrekt (Kommentar an Z. 170
  bestätigt das explizit). Kein Bug.

## Stand 2026-09-25
15 behoben (Gruppen A–D, `583de612`, `0b3d4b88`, `08b92c70`, `c7e280ab`), 1 zurückgestellt (3), 1 verworfen (10). Ponytail-Punkte und Security-/IPC-Analyse weiter offen.

## Fazit
17 echte Bugs (4 kritisch, davon 3 Upstream-Altlasten außerhalb des eigenen Diffs), 9
Ponytail-Punkte, 2 falsche Erstfunde ausgeschlossen. Zwei Bugs (8, 9) liegen direkt im
neuen Material-3-Code und sind vermutlich am leichtesten zu reproduzieren (Settings-Sheet
öffnen, backgrounden). Noch nicht gemacht: Cross-Feature-Interaktionscheck (HDR/Panorama/
Timelapse/Dual-Cam gegenseitig) und eine echte Security/IPC-Analyse (Gemini hat das 3x
verweigert, müsste manuell/mit Claude gemacht werden).
