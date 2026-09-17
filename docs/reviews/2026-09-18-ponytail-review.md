# Ponytail-Review über den eigenen Code (2026-09-18)

Durchlauf nach `docs/kickoff/ponytail-review.md` im Chat „camera-app – Ponytail-Review":
Diff `0dd4cbe7..HEAD` (Stand `5fd2bd12`, ~4500 Zeilen eigener Code ohne Doku/Bilder),
per Gemini analysiert und vom Chat gegen Code und Git-Historie verifiziert. Reiner
Bericht, nichts geändert. Hier festgehalten, weil der Chat nur Fund 1 in die SPEC
eingetragen hat (PM-Liste 4.5, Punkt 9) und die anderen beiden sonst nur im
Chat-Transkript stünden.

## Funde

### 1. Toter Code durch `lock_to_portrait = true` — bewusste Abwägung, zu bestätigen
- **Stelle:** `MainActivity.java` Z. 1589, 1649, 1732 (`if(!lock_to_portrait)`-/Cache-Zweige).
- **Was raus könnte:** Zweige, die nie mehr laufen, seit `lock_to_portrait` beim
  Drehmodell-Umbau (`8dcd8fc3`) fest auf `true` steht.
- **Aber:** Der Commit begründet das ausdrücklich („kept only to stay close to upstream")
  – Nähe zum Open-Camera-Original für leichtere Upstream-Merges. Kein vergessener Fund,
  sondern eine Ja/Nein-Entscheidung. **Status:** in SPEC 4.5 als Punkt 9 zur Entscheidung.

### 2. Layout wird komplett per Java-Code berechnet statt in XML
- **Stelle:** `MainUI.layoutUI()` (~350 Zeilen, `ConstraintSet`-Aufrufe wie `connect()`,
  `centerHorizontally()`, `pinToBottom()`).
- **Was raus könnte:** Der starre Teil der Positionierung. Seit der Hochformat-Sperre gibt
  es nur noch **eine** feste Anordnung – genau der Fall, für den XML-Constraints
  (`app:layout_constraintBottom_toTopOf` usw.) gemacht sind.
- **Bleibt:** Der Teil, der von Laufzeitwerten abhängt (Gesten-/System-Ränder).
- **Warum:** Gleiche Anordnung im Code jedes Mal neu zusammenzurechnen ist schwerer zu
  lesen als ein XML-Layout, das sie einfach beschreibt.
- **Schätzung:** 50–150 Zeilen, aber aufwendiger (Gerätetest jeder Position nötig).
- **Status:** offen, noch nicht in der SPEC. Empfehlung: **nicht jetzt** – Kopfzeile,
  unterste Zeile und Sucher-Position (K4) ändern das Layout ohnehin noch; XML-Umzug erst,
  wenn die Anordnung steht (nach Audit-Schritt Kopfzeile/Dock), sonst zweimal Arbeit.

### 3. Doppelter Code für die „ausgewählt"-Optik (Moduswahl & Zoom-Stufen)
- **Stelle:** `MainUI.applyModeBarStyle()` (Z. 936–947) und
  `ZoomStepper.applyStepStyle()` (Z. 202–214) – fast identisch.
- **Was raus könnte:** Handgeschriebenes Umschalten von Hintergrund/Textfarbe/Fettschrift.
- **Ersatz:** Selektor-Drawable + `ColorStateList` in XML, dann `view.setSelected(…)` –
  Android wechselt die Optik selbst, einmal definiert statt zweimal im Code.
- **Warum:** Wird die „aktiv"-Farbe geändert (steht an: K2, 7.2 – Salbei/helles Grau statt
  `#F2F2F2`), müssen beide Stellen synchron gehalten werden.
- **Schätzung:** 10–20 Zeilen sicher.
- **Status:** offen, noch nicht in der SPEC. Empfehlung: **beim Farb-Schritt (7.3)**
  miterledigen – dort wird die „aktiv"-Optik ohnehin angefasst; ein eigener Chat lohnt
  dafür nicht.

## Geprüft und verworfen
- Touch-Listener in `MainActivity` Z. 498–505 („nur für Klick-Animation"): existiert
  ohnehin zwingend für die Lang-Klick-Erkennung, die zwei Optik-Zeilen kosten nichts.
- Handgezeichnete Animationen in `ShutterButton`/`PhotoVideoSwitch`: in den Klassen
  begründet (Form und Größe werden laufend animiert), keine unnötigen Umwege.

## Fazit
Drei echte Funde, kein Handlungsdruck. Fund 1 = Entscheidung, Fund 2 = später (nach
Layout-Stabilisierung), Fund 3 = Beifang im Farb-Schritt. Kein eigener Fix-Chat nötig.
