# Kickoff-Kontext: Material-3-Redesign, Audit-Schritt 1 – Auslöser-Zeile

Teil des laufenden Material-3-Redesigns (siehe `docs/SPEC.md` Abschnitt 4.5). Dies ist
Schritt 1 der im Audit (`docs/design-reference/ui-spezifikation.md` Abschnitt 7)
empfohlenen Reihenfolge: kleinster Eingriff, größter sichtbarer Effekt.

## Zuerst lesen

1. `docs/SPEC.md` Abschnitt 2.1 (deine Rolle & Abschlussprotokoll) und 4.5
2. `docs/design-reference/ui-spezifikation.md` – **verbindliche Bau-Vorgabe**,
   insbesondere Abschnitt 1b (Feinheiten Auslöser-Zeile), 7.1 Punkte 5 und 10, 7.2
   (Auslöser + Kamera-Wechsel, mit genauen Ist/Soll-Werten und Zeilenangaben), und
   Abschnitt 9 (alle elf Konflikte sind entschieden, Entscheidungen stehen in der
   Tabelle dort)
3. `docs/design-reference/pixel-camera-detailspec.md` – ergänzende Maße
4. `CLAUDE.md`

## Kurzfassung

- **Ich (Falk) habe keine Programmiererfahrung.** Erkläre bei jedem Schritt, was du
  tust und warum.
- **Git:** Du übernimmst add/commit/push komplett selbst.
- **Nach Codeänderungen:** `graphify update .` ausführen.
- **Nur eine Sitzung gleichzeitig im Repo** – vor Codeänderungen `git status`/
  `git fetch` prüfen.
- **Als Allererstes:** Session per `set_session_title` (`session_id: "self"`)
  umbenennen in `camera-app – Redesign: Auslöser-Zeile`.
- **Grundregel der UI-Spec:** eins zu eins wie in der Referenz, nichts erfinden. Bei
  Unklarheit nachfragen statt annehmen.

## Aufgabe (aus `ui-spezifikation.md` 7.1/7.2, Ist→Soll mit Datei-/Zeilenangaben dort)

1. **Auslöser** (`take_photo`, `take_photo_shutter.xml`, `animator/button_animation.xml`):
   Außenring von hartem dunklem Rand auf transluzent-grauen Außenring umstellen (⌀ 88 dp,
   Kern ⌀ 72 dp Reinweiß – genaue Werte in 7.2, Konflikt K10 ist entschieden:
   Screenshot-Spec). Tap-Feedback von Scale-1,1-linear auf Scale-0,92 mit
   Feder-Rückstellung (tension 300, friction 20) ändern. Video-Variante: Kernfarbe
   `#F44336` → `#E53935`; Aufnahme-Zustand von hartem Drawable-Tausch auf
   Feder-Morph zu 32×32 dp / Radius 8 dp umstellen.
2. **Kamera-Wechsel-Button** (`switch_camera`, `circle_background.xml`): Form von
   Kreis auf **Squircle** (K1: entschieden), 60 dp → 56 dp, Hintergrund
   `rgba(255,255,255,0.12)` statt `#20000000`, Icon 24 dp Weiß, Zentrumsabstand zum
   Auslöser von 80 dp auf 64 dp.
3. **Galerie-Miniatur** an die richtige Position: aktuell Teil der alten Icon-Leiste
   oben, **soll** auf der Auslöser-Zeile links vom Auslöser sitzen (56×56 dp Squircle,
   Radius 22 dp, `centerCrop`, Zentrum 64 dp links vom Auslöser-Zentrum), mit
   Scale-Bounce 1,1→1,0 + Cross-Fade bei neuem Medium. Das verdrängt aktuell den
   Video-Umschalter von dieser Position – wohin der wandert, ist Teil von Audit-Schritt 2
   (unterste Zeile/Dock), hier erstmal nur Platz schaffen bzw. mit Falk kurz klären, wie
   der Übergang aussieht, ohne Funktionalität zu verlieren.
4. **Kamera-Wechsel-Animation:** 180°-Drehung, 350 ms, `cubic-bezier(0.4, 0.0, 0.2, 1)`.
5. **Haptik:** Heavy Click beim Auslösen und bei Video-Start/-Stopp (aktuell nur
   SeekBar-Ticks vorhanden, siehe `MainUI.performHapticFeedback()`).

**Nicht Teil dieses Schritts** (kommen in späteren Audit-Schritten): Kopfzeile,
unterste Zeile/Dock, Karussell-Mechanik der Moduswahl, Feinjustier-Rad,
Einstellungen-Blätter, Fokus-Ring/Nivellierung, globale Farb-/Blur-/Schrift-Anpassungen.

## Vorgehen

Wie gewohnt: Code-Stelle identifizieren und erklären, bevor geändert wird. In
überschaubaren Schritten umsetzen, nach jedem sichtbaren Zwischenschritt von mir am
Pixel 9 Pro testen lassen (Foto- UND Video-Modus, da der Auslöser für beide Zustände
anders aussieht).

## Abschluss

- `docs/SPEC.md` Abschnitt 4.5 aktualisieren (was aus Audit-Schritt 1 erledigt ist).
- `docs/design-reference/ui-spezifikation.md` Abschnitt 6 ("Was bereits umgesetzt")
  entsprechend ergänzen, Abschnitt 7.2 um die jetzt erledigten Punkte kürzen/markieren.
- Abweichungen zur Spec ausdrücklich auflisten, falls es welche gab (Grundregel).
- Änderungsprotokoll-Eintrag, committen, pushen.
- Kurze Zusammenfassung für den Projektmanager-Chat.
