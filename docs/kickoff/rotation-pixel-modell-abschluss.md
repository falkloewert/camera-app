# Kickoff-Kontext: Drehmodell abschließen (Wiederaufnahme, 2026-09-18)

Der Feature-Chat „camera-app – Redesign: Drehmodell" (2026-09-17) hat das Pixel-Drehmodell
aus `docs/kickoff/rotation-pixel-modell.md` in drei Schritten gebaut und am Pixel 9 Pro
geprüft, dazu drei Folgethemen erledigt – ist dann aber **mitten in der Endabnahme**
abgebrochen (Chat archiviert). Dieser Kickoff bringt die Arbeit zu Ende. **Nicht neu bauen**,
nur die Datei-Prüfung nachholen, dokumentieren, committen.

## Zuerst lesen

1. `docs/SPEC.md` Abschnitt 2.1 (deine Rolle & Abschlussprotokoll) und 4.5 (Stand)
2. `CLAUDE.md` – insbesondere die Git-Regel „Commit nur mit Dateipfaden"
3. Die Commit-Beschreibungen des Umbaus (sie sind die genaueste Doku dessen, was gebaut
   wurde und warum):
   `git log --format='%n=== %h %s%n%b' 40dc9612..a47d7df5`
4. `docs/kickoff/rotation-pixel-modell.md` nur als **Hintergrund** (Problem, Ziel, was
   zu verifizieren ist) – **nicht** als To-do-Liste, die Aufgabe daraus ist erledigt.
5. `docs/design-reference/ui-spezifikation.md` Abschnitte 2, 6, 7.2 („Rotation"), 8, 9 (K3)
   – die musst du am Ende anpassen (siehe unten).

## Kurzfassung

- **Ich (Falk) habe keine Programmiererfahrung.** Erkläre bei jedem Schritt, was du tust
  und warum.
- **Git:** Du übernimmst add/commit/push komplett selbst – Commits **nur mit expliziter
  Dateiangabe** (`git commit -- <Dateien>`), siehe `CLAUDE.md`.
- **Nach Codeänderungen:** `graphify update .`
- **Nur eine Sitzung gleichzeitig im Repo** – `git status`/`git fetch` vor Codeänderungen.
- **Als Allererstes:** Session per `set_session_title` (`session_id: "self"`) umbenennen in
  `camera-app – Redesign: Drehmodell (Abschluss)`.
- Bauen/Installieren wie bisher: `./gradlew :app:installDebug`, dann
  `adb shell am start -n de.falkloewert.cameraapp/net.sourceforge.opencamera.MainActivity`.

## Was bereits gebaut und committet ist (Stand `a47d7df5`)

| Commit | Inhalt |
|---|---|
| `8dcd8fc3` | Schritt 1: Fenster auf Hochformat gesperrt (`lock_to_landscape` → `lock_to_portrait`, Manifest `portrait`) |
| `1a247924` | Schritt 2: `MainUI.layoutUI()` auf eine einzige feste Anordnung reduziert; `orientBar()`, `RotatedTextView`, senkrechter `PhotoVideoSwitch`-Modus entfernt (~900 Zeilen weniger) |
| `ae011cb6` | Schritt 3: `OrientationEventListener` → `MainUI.onOrientationChanged()` → `rotateGlyphs()`; alle Glyphen drehen an Ort und Stelle, 300 ms, `cubic-bezier(0.2, 0, 0, 1)`. Pillen, Ring, Modus-Text bleiben stehen. `DrawPreview`-Overlays bleiben (nur Videozeit + Timer-Countdown drehen). Popup und Belichtungs-Slider drehen **nicht** (Entscheidung Falk – alte Elemente, die das Redesign ersetzt). |
| `86f3f3ec` | Folgethema: „Foto"-Eintrag wieder in der Moduswahl neben „Panorama" (Entscheidung Falk – sonst kein klarer Ausweg aus Panorama/künftigen Modi). Kehrt die Entscheidung aus Audit-Schritt 1 um. |
| `00cd3e4c` | Folgethema: kein Panorama bei der Frontkamera (`supportsPanorama()` false, solange die Frontkamera offen ist) |
| `a47d7df5` | Bugfix: Panorama-Weitwinkel nahm nach Selfie→Rück-Wechsel die physische Kamera der *vorherigen* Kamera (Cache jetzt am `CameraController`, `Preview.closeCamera()` vergisst die physischen Kameras) |

## Zuletzt committet (2026-09-18, vom PM-Chat nach Falks Abnahme)

`9327410c` – **Entfernung der Open-Camera-Info-Toasts** („Fotomodus: …", Kamera-ID/Linsenname
beim Wechsel, Startup-Toast `showPhotoVideoToast()` samt `block_startup_toast`/
`push_info_toast_text`). Grund: Falks Regel „keine Modus-/Linsen-Ankündigungen in der App"
(`ui-spezifikation.md` 8). Fehler-Toasts und die Erstinfo-Dialoge (HDR/Panorama) bleiben
absichtlich. Falk hat es am Gerät geprüft (Foto ↔ Video, Kamerawechsel, Panorama-Linse,
Einstellungen: keine Einblendung mehr). Das Arbeitsverzeichnis ist damit **sauber**.

## Aufgabe

1. **`git status`/`git fetch`** – das Arbeitsverzeichnis muss sauber sein und `HEAD` auf
   `origin/master` stehen. Einmal bauen und installieren, damit der Gerätestand sicher
   `HEAD` entspricht.
2. ~~Toast-Abnahme~~ – erledigt und committet (`9327410c`), nichts mehr zu tun.
3. **Datei-Prüfung – das ist der eigentliche offene Punkt des Kickoffs** („alles neu
   verifizieren, was der Umbau `2045620a` verifiziert hatte"): Mit dem gesperrten Fenster
   kommt die Bildausrichtung jetzt aus dem Lagesensor, nicht mehr aus dem Fenster. Falk
   macht **vier Fotos** (Hochformat, Querformat Oberkante links, Querformat Oberkante
   rechts, Hochformat auf dem Kopf), **ein Video** im Hochformat und **ein Panorama**;
   du holst die Dateien per `adb pull` und prüfst mit `exiftool -Orientation -n` bzw.
   `ffprobe` (Rotations-Tag). **Kriterium:** In jedem Betrachter (Galerie, Immich,
   `exiftool`-Vorschau) richtig herum. Die konkreten Zahlen dürfen sich vom alten Modell
   unterscheiden (damals: Hochformat 6, Quer links 1, Quer rechts 1 mit 180° in Pixeln,
   Kopf 8; Video 90°) – entscheidend ist das Ergebnis, nicht die Zahl. Zusätzlich kurz am
   Gerät: Immersive-Modus, Panorama-Linsenpaar in beiden Querlagen, schnelle 180°-Drehung.
4. **Abschlussprotokoll** (SPEC 2.1), konkret:
   - `docs/SPEC.md`: Feature-Tabelle Zeile 6 und Abschnitt 4.5 („Stand Orientierungs-
     Modell" ersetzen; Liste „Für den Projektmanager-Chat": Punkt 1 erledigt, Punkt 4
     erledigt durch `00cd3e4c`), Änderungsprotokoll-Eintrag 2026-09-17/18 mit dem, was
     gebaut wurde, den Entscheidungen (Popup/Slider drehen nicht; „Foto"-Eintrag zurück;
     Toasts weg, `9327410c`) und den Testergebnissen der Datei-Prüfung.
   - `docs/design-reference/ui-spezifikation.md`:
     - Abschnitt 2 neu fassen: Fenster ist **fest Hochformat**, nur Glyphen drehen; Modus-
       Beschriftungen stehen im Querformat quer „wie gedruckt" (das war schon so, jetzt
       aber ohne `RotatedTextView`); Endlagen unverändert.
     - Abschnitt 6 „Drehmodell": Technik-Absatz ersetzen (`orientBar()`/`RotatedTextView`
       gibt es nicht mehr; jetzt `rotateGlyphs()`); Moduswahl-Punkt: „Foto · Panorama" statt
       „nur Panorama".
     - Abschnitt 7.2 „Rotation": erledigt (300 ms, PathInterpolator).
     - Abschnitt 8: Punkt „Kein ‚Foto'-Eintrag" als umgekehrt markieren (`86f3f3ec`);
       alten Punkt „Bewusst kein `View.setRotation()` auf ganze Leisten" streichen/als
       überholt markieren – es wird keine Leiste mehr gedreht; neuer Punkt: Popup und
       Belichtungs-Slider drehen nicht (Entscheidung Falk); neuer Punkt: keine Info-Toasts.
     - Abschnitt 9 K3: **endgültig entschieden** – Hochformat-Sperre + Glyph-Drehung,
       umgesetzt.
   - `docs/kickoff/rotation-pixel-modell.md`: Statusvermerk oben ist schon drin, ggf.
     Commit-Hash des Abschlusses ergänzen.
   - Abweichungen zur Spec ausdrücklich auflisten (Regel aus `ui-spezifikation.md`, Kopf).
   - `graphify update .`, Commit mit Pfaden, Push, kurze Zusammenfassung für den
     Projektmanager-Chat.

## Nicht Teil dieses Schritts

Alles Neue. Insbesondere **nicht** anfassen: die vom Drehmodell-Chat aufgeworfene
Tele-Verzögerung beim „5"-Knopf (steht bereits in SPEC 4.5 auf der PM-Liste), Kopfzeile,
unterste Zeile, Karussell, Feinjustier-Rad, Einstellungen-Blätter, Fokus-Ring, Farben.
Fällt beim Testen etwas Neues auf: in die PM-Liste in SPEC 4.5 eintragen, nicht bauen.

## Aufwand

Kurze Sitzung: Datei-Prüfung, Doku, ein Commit. Kein Neubau.
