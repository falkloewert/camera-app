# Kickoff: Video-Blatt, Runde 2 (Makro, Blitz „Autom.", Selfiebeleuchtung)

Branch: `bench/blatt-deepseek-flash-2`. Nur Video-Modus. Regeln: `CLAUDE.md`, SPEC 2.1
(Gerätetest durch Falk, Abschlussprotokoll). Die drei restlichen Wünsche aus Falks Liste
(2026-09-21) laufen in einem Lauf, ein Gerätetest.

## Lesen (nur das)
- `preview/Preview.java`: `handleSingleTouch()` (ab 723), `tryAutoFocus()` (6968),
  `updateFocus()` (5390/5448), `updateFocusForVideo()` (5166), `isFlashSupportedForVideo()`
  (5209), `updateFlash()` (5256), `updateFlashForVideo()` (5192).
- `ui/DrawPreview.java` 2958–2968 (Vollbild-Weiß der beiden Frontschein-Modi).
- `cameracontroller/Camera2Settings.java` `setFlashValue()` (520–570),
  `CameraController2.java` 2483 (Flash-Werte der Frontkamera).
- `ui/SettingsSheet.java` `videoGeneralRows()` (897–1035, Blitz-Zeile 905–937).
- `res/layout/activity_main.xml` (Reihenfolge der Kinder = Zeichnungsreihenfolge).

## Muss (Runde 2)

1. **Makro bleibt fixiert.** Steht der Video-Fokus auf `focus_mode_macro`, darf ein Tippen
   auf den Bildschirm den Modus nicht ändern: kein Umschalten auf Autofokus, die
   Makro-Linse bleibt aktiv. Bei „Aus" bleibt es beim kontinuierlichen Video-Fokus.
   *Fertig:* Makro an → Tippen ändert weder Blatt-Zeile noch Aufnahmefokus; Makro aus →
   Tippen fokussiert wie bisher.
   *Vor dem Fixen:* Welcher Pfad den Modus beim Tippen zurücksetzt, ist am Gerät zu
   belegen (TEMP-Log in `handleSingleTouch`/`tryAutoFocus`/`autoFocusCompleted`), sonst
   wird am Symptom gefixt. Bei Gerätebelegung durch Falk: warten.

2. **Video-Blitz „Autom." (Rückkamera).** Die Blitz-Zeile bekommt drei Knöpfe: Aus /
   Autom. / An. „Autom." = Torch bei Dunkelheit: Lichtsensor (`Sensor.TYPE_LIGHT`), unter
   der Schwelle Torch an, darüber aus — nur im Video-Modus. Der gespeicherte Wert bleibt
   „Autom."; das Blatt schaltet nicht selbst auf An/Aus um.
   *Fertig:* Blitz „Autom." → im Dunkeln leuchtet die Torch-LED (Vorschau und Aufnahme),
   im Hellen nicht, die Zeile steht weiter auf „Autom.".
   **Korrektur 2026-09-21:** Dieser „Fertig"-Vermerk war nie am Gerät belegt. Der
   Lichtsensor sitzt auf der Displayseite und maß 152/230 lx im dunklen Zimmer gegen die
   Schwelle 10 lx, die Lampe ging nie an. Umgesetzt ist jetzt das gemessene ISO je Frame
   (`Preview.torchAutoOn()`), siehe `docs/changelog.md`, Eintrag „Blatt-Feinschliff".

3. **Selfiebeleuchtung statt Blitz (Frontkamera, Video).** Die Blitz-Zeile der Frontkamera
   wird zur Zeile „Selfiebeleuchtung" mit Glühbirnen-Symbol und den Knöpfen An/Aus. An =
   nur die schwarzen Ränder um das Vorschaubild werden hell weiß gefüllt (Rechteck
   `@id/preview`, auf dem Pixel [0,295][960,2001], also oben/unten je ~142 px); Bild und
   Bedienelemente bleiben unberührt. Aus = wie bisher.
   *Fertig:* Selfie-Blatt zeigt „Selfiebeleuchtung" An/Aus, An füllt nur die Ränder, kein
   Vollbild-Weiß.

## Nicht in dieser Runde
- Foto-Modus (dort bleibt „Mehr Licht" unverändert), Fokusring, Leveler.

## Abschluss
- Gerätetest durch Falk; danach `graphify update .`, Commit **nur mit Dateipfaden**, Push
  auf `bench/blatt-deepseek-flash-2`, Bericht ≤ 10 Zeilen (was geprüft, was nicht).
