# Kickoff: Panorama senkrecht + Selfie-Panorama

Arbeitsstand `master` nach `465b023b` (Schritt N). Maße 2,25 px/dp.
Session-Titel: `camera-app – Panorama senkrecht/Selfie`.

## 1. Soll

1. **Selfie-Panorama.** `MainActivity.supportsPanorama()` lässt die Frontkamera zu (die
   Sperre `FACING_FRONT` von Falk 2026-09-17 fällt weg). Das Karussell bietet „Panorama"
   dann auch vorne an; `getPhotoMode()` braucht keine Ausnahme mehr. Zu prüfen: die
   Vorschau der Frontkamera ist gespiegelt – die Stitch-Reihenfolge
   (`panorama_dir_left_to_right`) und die Zeichenrichtung des Stitch-Ergebnisses müssen
   dazu passen, sonst steht das Ergebnis seitenverkehrt oder die Scheiben liegen falsch.
2. **Umschalter Waagerecht | Senkrecht** (Entscheidung Falk 2026-10-06, siehe Skizze im
   Chat): im Panorama-Modus ein Bedienelement unter der Zoom-Pille, Stil wie die
   Zoom-Pille (`ZoomStepper.applyPillStyle`), zwei Einträge, der aktive im Salbei-Akzent.
   Das Modus-Karussell bleibt bei `Foto · Panorama · Bokeh` – **kein** vierter Eintrag.
   Die Wahl persistiert als Preference (Muster: die übrigen Panorama-relevanten
   Preferences in `PreferenceKeys`/`PreferenceSubPhoto`).
3. **Achse.** `MyApplicationInterface.panoramaTargetPoint()` dreht heute um die
   Erdvertikale (Gier). Bei „Senkrecht" muss um die Querachse gedreht werden – die Achse,
   die auf der Blickrichtung und der Erdvertikalen senkrecht steht. Die Schwenk**richtung**
   bleibt automatisch: bei Bild 1 werden zwei Ziele gesetzt, das zuerst erreichte gewinnt
   (`setNextPanoramaPoint`, `panorama_dir_left_to_right`). Das gilt für hoch/runter genauso.
4. **Bildwinkel.** `setNextPanoramaPoint()` rechnet mit `camera_angle_y`
   (`Preview.getViewAngleY`). Senkrecht braucht `getViewAngleX` (existiert) – beim Pixel
   9 Pro ist der Winkel in Hochformat der kleinere, die Scheiben liegen also enger.
   `panorama_pics_per_screen` und `max_panorama_pics_c` (17) sind darauf zu prüfen.
5. **Stitcher.** `PanoramaProcessor` projiziert auf einen stehenden Zylinder und
   `createProjectedBitmap()` zieht die Quell**spalten** auf die Winkelspalten. Für
   „Senkrecht" muss das um 90° gedreht gelten: Projektion um die liegende Achse, Zeilen
   statt Spalten, und die Reihenfolge der Scheiben entsprechend. Die
   Photosphere-Angabe in `ImageSaver.savePanoramaBitmap()` (Bildwinkel = Bilder × Schritt,
   auf 360° gedeckelt) muss die Achse mitbringen, sonst dehnt der Betrachter.
   `panorama_dir_left_to_right` in `ImageSaver.PanoramaRequest` um die Achse erweitern.
6. **Nicht kaputtmachen:** die waagerechte Aufnahme ist am 2026-10-06 abgenommen. Für
   „Waagerecht" darf sich am Ergebnis nichts ändern – am besten vorher/nachher mit
   denselben Testbildern stitchen.

## 2. Fallstricke

- `getPhotoMode() == Panorama` erzwingt in `MyApplicationInterface` die Ausrichtung
  „portrait" (Z. 1319) und schaltet Zoom, Timer und Serienbild ab. Senkrecht braucht
  dieselben Sperren.
- `setNextPanoramaPoint()` hat `upright_angle_tol` (3°) und `too_far_angle` (45°) fest
  eingebaut; „upright" ist beim senkrechten Schwenk nicht mehr sinnvoll – prüfen, ob die
  Führung das Hoch-/Runterziehen dann blockiert.
- Die Wasserwaage (`DrawPreview`, `getPhotoMode() == Panorama`) und die Richtungsmarke
  gehen beide von der waagerechten Achse aus.
- `calculateImageRotation`/EXIF je Bild, und die Frontkamera-Spiegelung, können die
  Scheiben einzeln verdrehen.
- Speicher: 17 Scheiben à 4:3 sind das heutige Budget. Senkrecht hat einen anderen
  Bildwinkel; `choosePanoramaResolution()` und `computePanoramaSampleSize()` mitprüfen.

## 3. Bauen und Testen

- `JAVA_HOME=/home/falk/.jdks/jbr-21.0.11 ./gradlew :app:assembleDebug`, dann
  `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- App nur über `de.falkloewert.cameraapp` starten, nie die Upstream-App.
- Was ohne Handbewegung prüfbar ist, selbst prüfen: Start/Ende ohne Absturz, Logcat auf
  `E/`-Zeilen, Umschalter sichtbar und persistent, Selfie-Panorama startet, die
  gespeicherte Datei öffnet sich und hat plausible Maße (`adb pull` + Photosphere-Angabe).
- Was eine echte Schwenkbewegung braucht (Naht, Verzerrung, Kippung), kann nur Falk am
  Gerät beurteilen – gebündelt vorlegen, nicht nach jedem Handgriff.

## 4. Abschluss (nach Falks Gerätetest)

SPEC 4.4 (Stand + was senkrecht/Selfie anders machen), `docs/changelog.md` ≤ 10 Zeilen,
`graphify update .`, Commit mit Dateipfaden + Push, drei Zeilen für den PM-Chat.
