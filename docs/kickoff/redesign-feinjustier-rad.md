# Kickoff-Kontext: Feinjustier-Rad für den Zoom (Redesign, Audit-Schritt „Zoom")

Teil des Material-3-Redesigns (`docs/SPEC.md` 4.5). Baut `ui-spezifikation.md` Abschnitt 3.2
und 7.1 Punkt 1, bringt die Zoom-Stufen-Pille auf Referenzmaße (7.2) und räumt den alten
Zoom-Schieberegler ab. Nimmt zwei Punkte der PM-Liste in SPEC 4.5 mit: **3** (Selfie-
Weitwinkel-Filterung) und **8** (Tele-Verzögerung beim „5"-Knopf, optional am Ende).
Vom Projektmanager-Chat am 2026-09-18 vor „Kopfzeile + unterste Zeile" gezogen, weil das
Rad in sich geschlossen ist und keine offenen Spec-Entscheidungen hat – die Kopfzeile hängt
am noch nicht gebauten Einstellungen-Blatt und hat offene Fragen.

## Zuerst lesen

1. `docs/SPEC.md` Abschnitt 2.1 (deine Rolle & Abschlussprotokoll) und 4.5 (Stand, PM-Liste
   Punkte 3 und 8)
2. `docs/design-reference/ui-spezifikation.md`: Kopf (Grundregel, „Abweichungen auflisten"),
   Abschnitt 3 (Zoom-Bedienung, v. a. 3.2 und 3.3), 7.1 Punkt 1, 7.2 „Zoom-Stufen-Pille",
   Abschnitt 8 (Falks Entscheidungen: gemessene Werte schlagen die Detail-Spec; keine
   Animationen außer am Auslöser), Abschnitt 9 – K7 (Hauptmarken **mit** „,5") und K11 („×")
3. `docs/design-reference/pixel-camera-detailspec.md` Abschnitt 5 (Zoom-System) und 10 (Haptik)
4. `docs/design-reference/pixel-camera-screenshots.md` Abschnitt 5, Punkte 1–3
5. `CLAUDE.md` – insbesondere die Git-Regel „Commit nur mit Dateipfaden"
6. Code (erst `graphify query`, dann gezielt lesen):
   - `ui/ZoomStepper.java` komplett – baut die vier Stufen aus dem echten Zoombereich
     (`chooseStepRatios()`, Filter `min_step_separation` = 1,25 → PM-Punkt 3), tippt per
     `Preview.zoomTo(factor, false, true)`, folgt Pinch über `updateSelection()`
   - `preview/Preview.java`: `ScaleListener` (Pinch, ca. Z. 803–871, inkl. Einrasten auf 1×),
     `scaleZoom()`, `zoomTo()` (Z. 4487), `getZoomRatio()/getMinZoomRatio()/getMaxZoomRatio()`
     (Z. 9336 ff.) – der Zoom ist eine Liste ganzzahliger Prozentwerte (`zoom_ratios`), das Rad
     muss also auf den nächstliegenden Index abbilden
   - `ui/MainUI.java`: `setSeekbarZoom()` (Z. 1930, hier landet jeder Zoom von außen – Pinch,
     Lautstärketasten), `rotateGlyphs()`/`ZoomStepper.getStepButtons()` (Glyph-Drehung),
     `performHapticFeedback()` (Z. 2902, `CLOCK_TICK`), `performHeavyClick()`
   - Reste des alten Reglers `zoom_seekbar`: `activity_main.xml` Z. 220, `MainActivity` Z. 430,
     1217 (Lautstärketasten → `changeSeekbar`), 5141–5164, 5414 ff.; `MainUI` Z. 152,
     569–585, 1085–1109

## Kurzfassung

- **Ich (Falk) habe keine Programmiererfahrung.** Erkläre bei jedem Schritt, was du tust
  und warum.
- **Git:** Du übernimmst add/commit/push komplett selbst – Commits **nur mit expliziter
  Dateiangabe** (`git commit -m "…" -- <Dateien>`), siehe `CLAUDE.md`.
- **Nach Codeänderungen:** `graphify update .`
- **Nur eine Sitzung gleichzeitig im Repo** – `git status`/`git fetch` vor Codeänderungen.
- **Als Allererstes:** Session per `set_session_title` (`session_id: "self"`) umbenennen in
  `camera-app – Redesign: Feinjustier-Rad`.
- **Grundregel der UI-Spec:** eins zu eins wie in der Referenz, nichts erfinden, bei
  Unklarheit nachfragen. **Gemessene Werte schlagen die Detail-Spec** (Entscheidung Falk,
  Audit-Schritt 1): Gerät 960 × 2142 px bei Dichte 360 = **2,25 px/dp**.

## Vorab am echten Pixel klären (Falk, bevor gebaut wird)

Die Spec ist beim Rad an zwei Stellen nicht eindeutig, und zwei Dinge fehlen. Frag Falk zu
Beginn genau das – er schaut in seine Pixel-Kamera-App:

1. **Was zeigt die Pille nach dem Loslassen bei z. B. 6,7×?** `ui-spezifikation.md` 3.2 sagt
   „zurück zu den vier Stufen", `pixel-camera-screenshots.md` 5.2 sagt „oder ist man auf
   einer hohen Stufe" (Rad bleibt sichtbar?). Widerspruch → Falk klärt am Gerät, Ergebnis
   in 3.2 festschreiben (ggf. als neuen K-Punkt in Abschnitt 9 mit Entscheidung).
2. **Selfie-Modus:** Welche Stufen zeigt die Pixel-App in der Zoom-Pille bei der Frontkamera?
   Unsere Frontkamera meldet 0,896×–10×; `ZoomStepper` filtert die Weitwinkel-Stufe, wenn sie
   weniger als 1,25-fach weiter ist als 1× (sonst Fast-Duplikat) – aktuell also nur
   „1× · 2 · 5". Danach `min_step_separation` bestätigen oder anpassen (PM-Punkt 3).
3. **Screenshot des Rads** (Pixel-App, Hochformat **und** Gerät quer gehalten) – falls es in
   `pixel-original-hochformat.png`/`-querformat.png` nicht zu sehen ist (prüfen, z. B. per
   `gemini-ask` oder Bild-Read). Daraus messen: Rad-Höhe/-Breite, Strich-Abstände (die
   Hauptmarken ,5 · 1 · 2 · 5 · 10 · 30 sind vermutlich **logarithmisch** verteilt – ablesen,
   nicht raten), Badge-Größe und -Position (auch quer gehalten: wo sitzt das Badge dann?).
   Screenshots unter `docs/design-reference/` ablegen.
4. **Übergang Pille → Rad:** Falks Regel „keine Animationen außer am Auslöser". Ohne
   weichen Übergang bauen (schlichter Wechsel); Falk entscheidet beim Gerätetest, ob das Rad
   die zweite Ausnahme (wie die Glyph-Drehung) bekommt. Entscheidung in Abschnitt 8 notieren.

## Aufgabe

1. **Rad (7.1 Punkt 1, 3.2):** Gedrückthalten + waagerechtes Ziehen auf der Zoom-Pille
   verwandelt sie in das Lineal: Scroll-Container (Detail-Spec 280–320 × 48 dp – Messung
   gilt), Alpha-Maske links/rechts, Hauptmarken **,5 · 1 · 2 · 5 · 10 · 30** (K7) soweit im
   echten Zoombereich der Kamera, Striche 1,5 dp / Zwischenstriche 0,75 dp, Zahlen 60 % Weiß
   mittig unter den Hauptmarken, statischer weiß leuchtender Mittelbalken, darüber das
   Floating-Badge (28 dp hoch, Radius 9999, 12 dp Padding waagerecht) mit dem Wert wie
   `2,5×` (deutsches Komma, „×" nach K11). Zoom über `Preview.zoomTo()` auf den
   nächstliegenden Index; alles über 5× ist **nur** hierüber erreichbar. Nach Loslassen:
   Verhalten laut Vorab-Frage 1.
2. **Pinch (3.3, 7.1 Punkt 1 „oder Pinch im Sucher"):** Während des Zwei-Finger-Zooms
   ebenfalls das Rad zeigen und mitführen, Loslassen wie oben. Das bestehende Einrasten auf
   1× im `ScaleListener` bleibt.
3. **Haptik (7.1 Punkt 10, Detail-Spec 10):** `CLOCK_TICK` pro überstrichenem Strich und –
   bislang fehlend (7.2) – beim Antippen einer Stufe. Über die vorhandenen Helfer in
   `MainUI`, keine neue Vibrations-Berechtigung.
4. **Zoom-Pille auf Referenzmaße (7.2):** heute 52 dp hoch / aktiver Kreis 44 dp
   (`dimens.xml` `zoom_step_size`/`zoom_step_margin`), Detail-Spec 40 / 36 dp – **im
   Referenz-Screenshot messen**, Messung gilt. Abstand zum Auslöser-Ring: 24 dp (gemessen in
   Audit-Schritt 1). **Farben nicht anfassen** (Salbei/Deckkraft/Blur kommen im Farb-Schritt
   7.3).
5. **Alten Regler entfernen (7.2, letzter Punkt):** `zoom_seekbar` aus Layout und Code, so
   klein wie möglich: Lautstärketasten-Zoom (`MainActivity` Z. 1217, heute über
   `changeSeekbar`) direkt auf `zoomTo()` umleiten; `MainUI.setSeekbarZoom()` auf das
   Nötige (Stepper/Rad nachführen) reduzieren. Was daran Open-Camera-Code ist, nur
   entfernen, nicht umbauen (Nähe zum Upstream, vgl. PM-Punkt 9).
6. **Drehmodell einhalten:** Rad-Zahlen und Badge-Text sind Glyphen → wie die Stufen-Ziffern
   über `MainUI.rotateGlyphs()` an Ort und Stelle drehen; das Lineal selbst bleibt „wie
   gedruckt" (liegt das Gerät quer, steht es von selbst senkrecht – so ergibt sich 3.2
   „im Querformat senkrecht" ohne Sonderfall). Badge-Lage quer: aus Vorab-Frage 3.
7. **Optional, nur nach Abnahme von 1–6 und als eigener Commit – Tele-Verzögerung
   (PM-Punkt 8):** Der „5"-Knopf setzt heute einen Zoomfaktor auf der *logischen* Kamera;
   der Treiber entscheidet, wann er auf das Tele umschaltet (~1 s). Alternative zum
   Ausprobieren: beim Tipp die **physische** Tele-Linse direkt anfordern, wie das
   Panorama-Linsenpaar mit dem Weitwinkel (`MainActivity.userSwitchToCamera(id,
   physicalId)`). Falk vergleicht am Gerät: sofortiger Wechsel gegen die Automatik der
   logischen Kamera (bei wenig Licht bleibt sie absichtlich auf dem Hauptsensor). Vorsicht:
   derselbe Mechanismus hatte gerade den Fehler `a47d7df5`. Überzeugt es nicht →
   zurücknehmen; Entscheidung so oder so in SPEC 4.5 (Punkt 8 abschließen).

## Nicht Teil dieses Schritts

Kopfzeile, unterste Zeile (Einstellungen-/Regler-Button), Moduswahl-Karussell,
Einstellungen-Blätter, Fokus-Ring/Nivellierung, Sucher-Position (K4), Farben/Blur/Schrift
(auch nicht die Pillen-Farben), Smart Chips. Ponytail-Fund 3 (`docs/reviews/2026-09-18-
ponytail-review.md`: doppelter „ausgewählt"-Stil in `applyStepStyle()`/`applyModeBarStyle()`)
kommt mit dem Farb-Schritt – **darf** mit erledigt werden, wenn `applyStepStyle()` ohnehin
angefasst wird, ist aber keine Pflicht.

## Vorgehen

Wie gewohnt: Code-Stelle identifizieren und erklären, bevor geändert wird. Reihenfolge-
Vorschlag: Vorab-Fragen → Messen → Pillen-Maße (Schritt 4, sichtbar, klein) → Rad statisch
zeichnen → Zieh-Interaktion → Pinch → Haptik → Regler entfernen → Drehung prüfen. Nach jedem
sichtbaren Zwischenschritt am Pixel 9 Pro testen lassen. Endabnahme: Foto- **und**
Videomodus, Rück- **und** Frontkamera, alle vier Lagen, Zoom über 5× nur per Rad, Pinch,
Lautstärketasten, Panorama (dort ist Zoom aus – Pille darf nichts Falsches zeigen),
Screenshot-Vergleich mit der Referenz.

## Aufwand / Risiko

Eine Sitzung, vergleichbar mit Audit-Schritt 1. Risiko: Touch-Handling (Ziehen auf der
Pille darf das Tippen der Stufen nicht kaputt machen; Pinch im Sucher läuft über den
`ScaleListener` des Preview, das Rad über die Pille – zwei Eingänge, ein Zustand) und die
Abbildung stufenloser Werte auf Open Cameras ganzzahlige Zoom-Liste.

## Abschluss

- `docs/SPEC.md` 4.5 (Stand; PM-Liste Punkte 3 und 8 abschließen), Änderungsprotokoll.
- `ui-spezifikation.md`: 3.2 („NOCH NICHT UMGESETZT" weg, Verhalten nach Loslassen
  festschreiben), 6 (gebaut), 7.1 Punkt 1 und 7.2 „Zoom-Stufen-Pille" (erledigt/gemessen),
  8 (Abweichungen + Entscheidung zur Morph-Animation), 9 (falls Vorab-Frage 1 einen neuen
  Konflikt ergibt).
- Abweichungen zur Spec ausdrücklich auflisten, `graphify update .`, Commit mit Dateipfaden,
  Push, kurze Zusammenfassung für den Projektmanager-Chat.
