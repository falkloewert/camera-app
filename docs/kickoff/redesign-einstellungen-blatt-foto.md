# Kickoff-Kontext: Foto-Einstellungen-Blatt (Allgemein) + linker Dock-Button + Wischgeste

Teil des Material-3-Redesigns (`docs/SPEC.md` 4.5). **Schritt A von drei** für den Block
„Einstellungen-Blatt / unterste Zeile / Kopfzeile" (Reihenfolge-Entscheidung PM-Chat
2026-09-18, SPEC 4.5 PM-Punkt 10):

- **A (dieser Chat):** Bottom-Sheet „Fotoeinstellungen", Reiter **Allgemein** (vier Zeilen),
  linker Kreis-Button der untersten Zeile, Swipe-Down auf dem Sucher, „…"-Button → volle
  Einstellungsliste.
- **B (später):** Reiter „Pro", Video-Blatt, rechter Kreis-Button (Regler).
- **C (später):** Kopfzeile; erst dann alte Icon-Leiste + Drei-Punkte-Popup abschaffen.

**Warum in dieser Reihenfolge:** Die Kopfzeile ersetzt die alte Icon-Leiste. Deren
Funktionen (Blitz, Timer, Belichtung, Popup-Optionen …) brauchen vorher ein Ziel – das
Blatt. Solange C nicht gebaut ist, bleiben Icon-Leiste und Popup **unverändert bestehen**;
Blatt und Popup lesen/schreiben dieselben Einstellungen und müssen sich gegenseitig
aktualisieren.

## Zuerst lesen

1. `docs/SPEC.md` Abschnitt 2.1 (deine Rolle & Abschlussprotokoll) und 4.5 (Stand, PM-Liste –
   v. a. Punkt 2 zu Fokus/Blitz und Punkt 10)
2. `docs/design-reference/ui-spezifikation.md`: Kopf (Grundregel), Abschnitt 1 Punkt 4
   (Fußfläche), **4** (Foto-Einstellungen, Zeilenschema, Reiter Allgemein – exakt diese vier
   Zeilen), **4b** (Falks Entscheidungen, was die App nicht kann – nicht neu diskutieren),
   7.1 Punkte 2 und 3, Abschnitt 8 (Entscheidungen: gemessene Werte schlagen die
   Detail-Spec; keine Animationen außer am Auslöser; Bedienelemente verschieben einander
   nie), Abschnitt 9 (K6 – Badge zeigt aktive Auflösung; K8 – Doppel-Tap bleibt)
3. `docs/design-reference/pixel-camera-screenshots.md` Abschnitt 4 (Sheet-Beschreibung)
4. `docs/design-reference/pixel-camera-detailspec.md` Abschnitt 8 (Dock-Maße: Kreis 40 dp,
   `rgba(255,255,255,0.10)`, Icon 22 dp; Dock 52 dp hoch) und 9 („Quick Settings" –
   **Achtung:** dort „Pull-Down-Card über dem oberen Sucherbereich"; die Screenshot-Spec
   sagt **Blatt von unten** – Screenshot-Spec gilt, Grundregel)
5. `CLAUDE.md` – insbesondere die Git-Regel „Commit nur mit Dateipfaden"
6. Code (erst `graphify query`, dann gezielt):
   - `ui/PopupView.java` – **Referenz dafür, wie Optionen heute angewendet werden**: Blitz
     Z. ~144 (`preview.updateFlash(option)`), Fokus Z. ~181 (`preview.updateFocus(…)`),
     Timer Z. ~874–889 (`PreferenceKeys.TimerPreferenceKey`), Auflösung (suchen:
     `getResolutionPreferenceKey`), danach `main_activity.updateForSettings(…)`. Dieselben
     Pfade benutzen, nicht nachbauen.
   - `PreferenceKeys.java`: `getFlashPreferenceKey(cameraId)`, `getFocusPreferenceKey(…)`,
     `getResolutionPreferenceKey(…)`, `TimerPreferenceKey` (Z. 52–62, 372)
   - `MainActivity.openSettings()` (Z. 2678) – die volle Einstellungsliste, Ziel des „…"
   - `MainActivity.MyGestureDetector.onFling()` (Z. 4979) – heute nur Bildschirm-Entsperren
   - `ui/PhotoVideoSwitch` + `MainUI.layoutUI()` (Fußzeilen-Verankerung: Umschalter-Zentrum
     85 dp vom Rand, gemessen) – der linke Kreis-Button kommt in dieselbe Zeile
   - `ui/ShutterButton`, `squircle_background.xml`, `circle_background.xml` – Vorbilder für
     Zeichnen/Maße
   - `app/build.gradle`: `com.google.android.material:material:1.14.0` ist eingebunden,
     `BottomSheetDialogFragment`/`BottomSheetBehavior` bisher **nirgends** benutzt

## Kurzfassung

- **Ich (Falk) habe keine Programmiererfahrung.** Erkläre bei jedem Schritt, was du tust
  und warum.
- **Git:** Du übernimmst add/commit/push komplett selbst – Commits **nur mit expliziter
  Dateiangabe** (`git commit -m "…" -- <Dateien>`), siehe `CLAUDE.md`.
- **Nach Codeänderungen:** `graphify update .`
- **Nur eine Sitzung gleichzeitig im Repo** – `git status`/`git fetch` vor Codeänderungen.
- **Als Allererstes:** Session per `set_session_title` (`session_id: "self"`) umbenennen in
  `camera-app – Redesign: Einstellungen-Blatt (Foto)`.
- **Grundregel der UI-Spec:** eins zu eins wie in der Referenz, nichts erfinden, bei
  Unklarheit nachfragen. Gemessene Werte schlagen die Detail-Spec (2,25 px/dp).

## Vorab mit Falk klären (bevor gebaut wird)

1. **Screenshots des Blatts als Dateien.** Die Referenz-Screenshots vom 2026-09-14 (Sheet
   „Fotoeinstellungen", Reiter Allgemein und Pro) liegen nur in einem alten Chat, nicht im
   Repo. Falk bitte um neue Screenshots seiner Pixel-App (Blatt offen, Reiter Allgemein;
   dazu einmal das Blatt mit **quer gehaltenem** Gerät) → unter `docs/design-reference/`
   ablegen und daraus messen (Blatthöhe, Eckradius, Ziehgriff, Zeilenhöhe, Icon-Kreise,
   Abstände, Deckkraft der Abdunklung). Ohne Screenshots: nach Beschreibung bauen und das
   in Abschnitt 8 als „gewählt, nicht gemessen" vermerken (Präzedenz: PM-Punkt 12).
2. **„…"-Button oben rechts im Blatt** (in 4b offen): PM-Empfehlung → öffnet Open Cameras
   **volle Einstellungsliste** (`MainActivity.openSettings()`). Das ist das Gegenstück zur
   „vollständigen Einstellungsliste" der Pixel-App (SPEC 4.5, Screenshot-Analyse Punkt 2)
   und der Grund, warum in Schritt C die Icon-Leiste weg kann, ohne dass etwas unerreichbar
   wird. Falk bestätigt oder entscheidet anders.
3. **Blatt bei quer gehaltenem Gerät:** Was macht die Pixel-App – kommt das Blatt weiter
   von der physischen Unterkante (Hochformat-Fenster), drehen nur die Glyphen? Unser
   Fenster ist auf Hochformat gesperrt (Drehmodell); ein `BottomSheetDialogFragment` ist
   ein eigenes Fenster, das `MainUI.rotateGlyphs()` nicht erreicht. Je nach Antwort:
   Glyphen im Blatt über `MainUI.getUIRotation()` mitdrehen oder bewusst nicht (dann in
   Abschnitt 8 notieren).
4. **Linker Dock-Button im Videomodus:** Das Video-Blatt kommt erst in B. Vorschlag: im
   Videomodus öffnet der Button vorerst **nichts** und ist ausgeblendet (`INVISIBLE`, Platz
   bleibt – Regel „nichts verschiebt sich"). Alternative: dasselbe Blatt mit nur den Zeilen,
   die für Video gelten – das wäre Erfinden. Falk entscheidet.

## Aufgabe

1. **Bottom-Sheet „Fotoeinstellungen"** (`ui-spezifikation.md` 4): schiebt sich von unten
   über den Sucher, Kamera-Oberfläche bleibt abgedunkelt dahinter sichtbar. Kopf:
   Ziehgriff, Titel „Fotoeinstellungen", rechts „…" im Kreis (→ Vorab-Frage 2).
   Material-`BottomSheetDialogFragment` liegt nahe (Ziehgriff, Eckradius, Wisch-Schließen
   kommen mit) – prüfen, ob es sich mit dem gesperrten Hochformat-Fenster und dem
   Immersive-Modus verträgt; sonst eigene View im ConstraintLayout.
   **Reiter-Umschalter „Allgemein / Pro" erst in Schritt B** (ein Reiter ohne Inhalt wäre
   ein toter Knopf) – als Abweichung auflisten.
2. **Vier Zeilen, exakt diese, in dieser Reihenfolge** (Zeilenschema: links Bezeichnung
   klein/grau, darunter aktueller Wert groß/weiß; rechts eine Reihe runder Icon-Buttons,
   ausgewählt = voll weiß gefüllt):
   - **Mehr Licht** → Blitz **Aus · Auto · An** (4b; Icons ⊘ / Blitz-A / Blitz). Hinweis
     PM-Punkt 2: „Auto" kostet den ZSL-Puffer (Auslöse-Verzögerung) – das ist Falks Wahl,
     nicht deine; keine Warnung einbauen.
   - **Timer** → **Aus · 3 s · 5 s · 10 s** (Screenshot-Spec; die Detail-Spec sagt 3/10 –
     Screenshot gilt). `TimerPreferenceKey`.
   - **Makrofokus** → **Aus · An** (4b: kein „Automatisch"; An = `focus_mode_macro`, Aus =
     zurück auf den Standard „Kontinuierlich", siehe PM-Punkt 2 – **nicht** „auto").
   - **Verhältnis** → **Breitbild (16:9) · Vollbild (4:3)**: schreibt die **größte
     verfügbare Auflösung mit diesem Verhältnis** in die Auflösungs-Einstellung der
     aktiven Kamera (4b). Kein „Quadratisch", keine weiteren Verhältnisse.
   Änderungen laufen über **dieselben** Funktionen wie im Popup (`Preview.updateFlash()`,
   `updateFocus()`, `updateForSettings()`), damit Popup/Icon-Leiste und Blatt nie
   auseinanderlaufen; das Blatt liest beim Öffnen den aktuellen Zustand.
3. **Linker Kreis-Button der untersten Zeile** (7.1 Punkt 3): Kreis 40 dp,
   `rgba(255,255,255,0.10)`, weißes Zahnrad-/Regler-Icon 22 dp (Detail-Spec 8 – **messen**,
   wenn Screenshot vorhanden), in der Zeile des `PhotoVideoSwitch` (Zentrum 85 dp vom Rand),
   Position links wie in `pixel-original-hochformat.png`. Öffnet das Blatt. Verankerung wie
   die übrigen Fußzeilen: fest am Rand, nichts verkettet. Dreht als Glyph mit
   (`rotateGlyphs()`), der Kreis bleibt. Videomodus → Vorab-Frage 4.
4. **Swipe-Down auf dem Sucher** öffnet das Blatt (Detail-Spec 10, 7.1 Punkt 2):
   `MyGestureDetector.onFling()` erweitern, das bestehende Entsperren bei gesperrtem
   Bildschirm bleibt. Nicht während Video-/Panorama-Aufnahme.
5. **Haptik:** kein neues Profil nötig; Auswahl einer Option ohne Vibration, wie in der
   Pixel-App nicht spezifiziert – nichts erfinden.
6. **Nicht anfassen:** alte Icon-Leiste, `PopupView`, die Zahnrad-Taste `settings` oben –
   alles bleibt bis Schritt C.

## Nicht Teil dieses Schritts

Reiter „Pro", Video-Blatt, rechter Kreis-Button (Regler) → Schritt B. Kopfzeile,
Abschaffung von Icon-Leiste/Popup → Schritt C. Moduswahl-Karussell, Fokus-Ring/
Nivellierung, Sucher-Position (K4), Farben/Blur/Schrift (7.3 – auch nicht die Farben des
Blatts über das Nötige hinaus; Deckkraft/Abdunklung nach Messung), Smart Chips,
PM-Punkte 5–9, 11, 12. Fällt beim Bau etwas auf, das die Pixel-App im Blatt anders macht
als hier beschrieben: nachfragen, nicht abweichen.

## Vorgehen

Wie gewohnt: Code-Stelle identifizieren und erklären, bevor geändert wird. Reihenfolge-
Vorschlag: Vorab-Fragen → Messen → Dock-Button (klein, sichtbar) → Blatt leer mit Kopf →
Zeile für Zeile (jede am Gerät: setzt sie wirklich um, zeigt das Popup danach denselben
Zustand?) → Swipe-Down → „…" → Drehung/Immersive prüfen. Endabnahme: jede Zeile in beide
Richtungen (Blatt → Popup, Popup → Blatt), Kamerawechsel (Verhältnis/Blitz sind pro
Kamera), Foto mit Timer, Makro-Nahaufnahme, 16:9 ↔ 4:3 sichtbar im Sucher, Öffnen per
Button und per Wisch, Schließen per Wisch/Tipp daneben/Zurück, alle vier Lagen,
Screenshot-Vergleich mit der Referenz.

## Aufwand / Risiko

Eine Sitzung, eher länger als das Rad. Risiko: (a) Zustands-Synchronisation zwischen
Blatt und altem Popup (beide bleiben bis C), (b) Auflösungs-Umschaltung reißt die Kamera
neu auf (`updateForSettings`) – Blatt muss das überleben, (c) Dialog-Fenster vs.
Immersive-Modus/Hochformat-Sperre.

## Abschluss

- `docs/SPEC.md` 4.5 (Stand; PM-Punkt 10: A erledigt, B nächster), Änderungsprotokoll.
- `ui-spezifikation.md`: 4 (gebaut, gemessene Werte), 4b (Entscheidung „…"-Button
  nachtragen), 6 (gebaut), 7.1 Punkte 2 und 3 (Teilstand), 8 (Abweichungen: kein Reiter-
  Umschalter bis B, Videomodus-Verhalten, Drehung im Blatt), 9 (falls neuer Konflikt).
- Abweichungen zur Spec ausdrücklich auflisten, `graphify update .`, Commit mit Dateipfaden,
  Push, kurze Zusammenfassung für den Projektmanager-Chat.
