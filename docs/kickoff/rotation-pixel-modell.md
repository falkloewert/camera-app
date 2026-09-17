# Kickoff-Kontext: Drehmodell wie die Pixel-Kamera (Hochformat-Sperre, nur Glyphen drehen)

> **Stand 2026-09-18: umgesetzt.** Der Feature-Chat „Redesign: Drehmodell" (2026-09-17) hat
> die Aufgabe in den Commits `8dcd8fc3` (Sperre), `1a247924` (festes Layout), `ae011cb6`
> (Glyph-Drehung) gebaut und am Pixel 9 Pro geprüft; dazu `86f3f3ec`, `00cd3e4c`, `a47d7df5`,
> `9327410c`. Offen ist nur noch die Datei-Prüfung (EXIF/Video/Panorama) plus Doku –
> dafür gilt `docs/kickoff/rotation-pixel-modell-abschluss.md`. Dieses Dokument ist
> Hintergrund (Problem, Ziel, Belege), keine To-do-Liste mehr.

Teil des Material-3-Redesigns (`docs/SPEC.md` 4.5). Ersetzt den Orientierungs-Umbau vom
2026-09-17 (Commit `2045620a`), dessen **Endlagen** stimmen, dessen **Übergang** aber nicht.
Vorgeschlagen vom Feature-Chat „Audit-Schritt 1 – Auslöser-Zeile" (2026-09-17), von Falk am
selben Tag als eigener Feature-Chat gewünscht. Konflikt K3 in `ui-spezifikation.md`
Abschnitt 9 ist damit wieder offen.

## Zuerst lesen

1. `docs/SPEC.md` Abschnitt 2.1 (deine Rolle & Abschlussprotokoll) und 4.5
2. `docs/design-reference/ui-spezifikation.md` – Abschnitt 2 (Querformat-Anordnung, die
   bleibt), Abschnitt 6 (was gebaut ist, v. a. „Auslöser-Zeile" und „Drehmodell"),
   Abschnitt 8 (Falks Entscheidungen: feste Plätze, keine Animationen außer am Auslöser),
   Abschnitt 9 K3
3. `docs/design-reference/pixel-camera-detailspec.md` Abschnitt 10 („Bildschirmdrehung")
4. `docs/design-reference/rotation-uebergang-2026-09-17.jpg` – sieben Einzelbilder aus
   Falks Video (30 fps) beim Drehen unserer App, Beleg für das Problem (unten)
5. `CLAUDE.md` – insbesondere die Git-Regel „Commit nur mit Dateipfaden"

## Kurzfassung

- **Ich (Falk) habe keine Programmiererfahrung.** Erkläre bei jedem Schritt, was du tust
  und warum.
- **Git:** Du übernimmst add/commit/push komplett selbst – Commits **nur mit expliziter
  Dateiangabe** (`git commit -- <Dateien>`), siehe `CLAUDE.md`.
- **Nach Codeänderungen:** `graphify update .`
- **Nur eine Sitzung gleichzeitig im Repo** – `git status`/`git fetch` vor Codeänderungen.
- **Als Allererstes:** Session per `set_session_title` (`session_id: "self"`) umbenennen in
  `camera-app – Redesign: Drehmodell`.
- **Grundregel der UI-Spec:** eins zu eins wie in der Referenz, nichts erfinden, bei
  Unklarheit nachfragen.

## Das Problem, belegt

Falks Anspruch (2026-09-17): *„Pixelperfekt immer an der gleichen Stelle – nicht ein
verschiebbares Element, sondern es dreht sich einfach nur, bleibt aber auf dem Handy an der
gleichen Stelle."* Im Video zeigen sich zwei Effekte:

1. **Glitschen beim Drehen** (Bild 3 im JPG): für 1–2 Frames erscheint ein falscher
   Zwischenzustand – die Zoom-Pille liegt plötzlich waagerecht („5 2 1×"), der Auslöser
   fehlt, die Umschalter-Pille ist halb gezeichnet, darüber liegt das alte Bild. Ursache:
   Androids **Fenster-Drehanimation**. Unsere Activity ist nicht gesperrt (kein
   `screenOrientation` im Manifest), das System dreht das ganze Fenster inklusive Sucher,
   rendert die neue Lage *bevor* `MainUI.layoutUI()` (aus `onConfigurationChanged()`) die
   Leisten per `orientBar()` umgestapelt hat, und blendet einen Screenshot der alten Lage
   darüber. In einer nicht gesperrten Activity nicht vermeidbar.
2. **Versatz von einigen Millimetern zwischen den Lagen:** im Hochformat wird z. B. der
   Auslöser in der *Fensterbreite* zentriert, im Querformat in der *Fensterhöhe* – die
   Fenstermaße unterscheiden sich um Statusleiste, Gestenbalken und Kamera-Loch je Lage
   (`getNavigationGap()`, `getNavigationGapLandscape()`, …). Jede Lage rechnet in einem
   anderen Koordinatensystem; pixelgleiche Ergebnisse sind da Glück, nicht Konstruktion.

Eine fremde Analyse, die Falk eingebracht hatte, nannte zusätzlich „ungedämpfte
Sensordaten" und „fehlende Hysterese" – **das trifft nicht zu**: mit
`MainActivity.lock_to_landscape = false` erreicht kein Sensorwert die UI
(`MainUI.onOrientationChanged()` tut nichts), die UI reagiert nur auf die diskreten
System-Orientierungen; Open Camera hat für den Sensor-Pfad ohnehin eine 60°-Hysterese.
Drehpunkte stimmen (Mitte). Richtig an der Analyse ist allein der Kern: **die Pixel-App ist
auf Hochformat gesperrt und dreht nur die Glyphen an Ort und Stelle.**

## Aufgabe

Die Pixel-App nachbauen, nicht nachrechnen:

1. **Activity fest auf Hochformat sperren** (`android:screenOrientation="portrait"` bzw.
   Open Cameras `lock_to_landscape`-Mechanik mit Hochformat als Basis). Der Sucher darf bei
   physischer Drehung weder neu konfiguriert noch neu gerendert werden
   (`Preview.setCameraDisplayOrientation()` / `getDisplayRotationDegrees()` laufen dann nur
   noch einmal).
2. **Ein einziges festes Layout**: die heutige Hochformat-Anordnung (Auslöser-Zeile,
   Moduswahl, Umschalter, Zoom-Pille, alte Icon-Leiste) bleibt physisch am Gerät kleben.
   Die Orientierungs-Tabelle in `MainUI.layoutUI()` (`Side`-Zuordnung, `align_*`,
   `left_of`/`above`-Varianten für Quer- und umgekehrtes Querformat), `orientBar()` und
   `PhotoVideoSwitch.setLayoutRotation()` entfallen; `RotatedTextView` wird zur normalen
   Textansicht, die gedreht wird wie alles andere.
3. **Nur Blatt-Elemente drehen**: Orientierung aus dem vorhandenen
   `OrientationEventListener` (Hysterese ist da) in vier diskreten Stufen → `ui_rotation`;
   pro Stufe `setViewRotation()` (existiert) auf Icons, Zoom-Ziffern, Modus-Text,
   Galerie-Bild, Auslöser-Kern (Panorama-Haken), Umschalter-Glyphen; 300 ms,
   `cubic-bezier(0.2, 0.0, 0.0, 1.0)` (Detail-Spec 10, als `PathInterpolator`). Keine
   Layout-Parameter anfassen. Was in der Referenz *nicht* mitdreht (Pillen-Hintergründe,
   Ring), bleibt stehen. Falks Regel gilt weiter: keine Animationen außer am Auslöser
   selbst – die Glyph-Drehung ist die einzige Ausnahme, weil sie die Lesbarkeit sichert.
4. **Bildausrichtung** kommt dann aus dem Sensor, nicht mehr aus dem Fenster: Foto-EXIF
   (`Preview.getImageVideoRotation()`, `current_orientation`), Video-Rotationshinweis,
   Panorama-Linsenpaar, `DrawPreview`-Texte/Overlays (`ui_rotation`), Popup, Seekbars,
   Immersive-Modus. Open Camera kann das alles bereits für `lock_to_landscape = true` –
   die Arbeit ist, es auf Hochformat als Basis umzustellen, nicht es zu erfinden.

## Nicht Teil dieses Schritts

Kopfzeile, restliche unterste Zeile (Einstellungen-/Regler-Button), Karussell-Mechanik der
Moduswahl, Feinjustier-Rad, Einstellungen-Blätter, Fokus-Ring/Nivellierung, Sucher-Position
(K4), Farben/Blur/Schrift, Auslöse-Verzögerung (Fokus-/Blitz-Standardwerte, siehe SPEC 4.5).

## Vorgehen

Wie gewohnt: Code-Stelle identifizieren und erklären, bevor geändert wird. Reihenfolge-
Vorschlag: erst Sperre + festes Layout (sichtbarer Effekt: nichts verspringt mehr), dann
Glyph-Drehung, dann die Bildausrichtung. Nach jedem sichtbaren Zwischenschritt am Pixel 9 Pro
testen lassen; am Ende **alles neu verifizieren, was der Umbau `2045620a` verifiziert hatte**:
EXIF in allen vier Lagen, Video-Rotation, Panorama, Immersive-Modus. Screenshot-Vergleich mit
`docs/design-reference/pixel-original-querformat.png` (Galerie unter dem Auslöser, Wechsel
darüber, Zoom-Pille links, Moduswahl rechts, Umschalter außen).

## Aufwand / Risiko

Eine eigene Sitzung, vergleichbar mit `2045620a`. Risiko liegt in der Kamera-Pipeline
(Sucher-Transform, Bildausrichtung), nicht in der Optik. Die Auslöser-Zeile aus
Audit-Schritt 1 positioniert alles relativ zum Auslöser bzw. fest am Rand und übersteht den
Umbau unverändert – nur die Querformat-Sonderfälle (`flankShutter()` misst dort Höhen statt
Breiten, `PhotoVideoSwitch` zeichnet senkrecht) werden überflüssig.

## Abschluss

- `docs/SPEC.md` 4.5 + Änderungsprotokoll, `ui-spezifikation.md` Abschnitt 2/6/8/9 (K3
  endgültig entscheiden und den alten Punkt „Bewusst kein `View.setRotation()` auf ganze
  Leisten" in Abschnitt 8 anpassen – er gilt dann nicht mehr, weil keine Leiste mehr gedreht
  werden muss).
- Abweichungen zur Spec ausdrücklich auflisten, committen (mit Dateipfaden), pushen,
  `graphify update .`, kurze Zusammenfassung für den Projektmanager-Chat.
