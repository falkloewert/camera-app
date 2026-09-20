# Kickoff: Foto-Einstellungen-Blatt (Allgemein) + linker Dock-Button + Swipe-Down

Schritt **A** von drei (B: Pro-Reiter + Video-Blatt + Dock rechts; C: Kopfzeile, dann
Icon-Leiste + Popup weg). Bis C bleiben Icon-Leiste und Popup; Blatt und Popup müssen
denselben Zustand zeigen (gleiche Preferences, gleiche Update-Funktionen).

Session-Titel: `camera-app – Einstellungen-Blatt (Foto)`. Regeln: `CLAUDE.md`, SPEC 2.1.

## Lesen (nur das)
- `ui-spezifikation.md` Abschnitt 4 (Zeilen, Zeilenschema), 4b (Entscheidungen, nicht neu
  aufmachen), 7.1 Punkte 2–3, Abschnitt 8 (Messung schlägt Detail-Spec, 2,25 px/dp;
  keine Animationen außer Auslöser; nichts verschiebt sich).
- `pixel-camera-screenshots.md` Abschnitt 4; `pixel-camera-detailspec.md` Abschnitt 8
  (Dock-Maße) – Abschnitt 9 sagt „Pull-Down von oben", Screenshot-Spec (von unten) gilt.
- Code: `ui/PopupView.java` Z. ~144 (`preview.updateFlash`), ~181 (`updateFocus`),
  ~874–889 (Timer), Auflösung (`getResolutionPreferenceKey`), danach
  `updateForSettings(...)` – diese Pfade wiederverwenden. `MainActivity.openSettings()`
  Z. 2678, `MyGestureDetector.onFling()` Z. 4979 (heute nur Entsperren).
  `ui/PhotoVideoSwitch` + `MainUI.layoutUI()` (Fußzeile, Umschalter-Zentrum 85 dp).
  Material 1.14.0 eingebunden, `BottomSheetDialogFragment` noch ungenutzt.

## Vorab-Fragen an Falk (kurz, dann bauen)
1. ~~Screenshots~~ liegen vor: `docs/design-reference/pixel-sheet-allgemein-hochformat.png`,
   `-querformat.png` (+ `pixel-sheet-pro-*.png` für B, `pixel-original-hochformat-2026-09-18.png`).
   Daraus messen (2,25 px/dp).
2. „…"-Button im Blatt → `openSettings()` (volle Liste)? Empfehlung ja.
3. Quer gehalten: Blatt weiter von der physischen Unterkante, Glyphen drehen? Ein
   `BottomSheetDialogFragment` ist ein eigenes Fenster – `rotateGlyphs()` greift nicht.
4. Dock-Button im Videomodus (Video-Blatt erst in B): `INVISIBLE`, Platz bleibt?

## Bauen
1. Bottom-Sheet „Fotoeinstellungen": von unten, Kamera dahinter abgedunkelt; Ziehgriff,
   Titel, „…" im Kreis. Reiter-Umschalter erst in B (Abweichung listen).
2. Zeilen, exakt: **Mehr Licht** = Blitz Aus·Auto·An · **Timer** Aus·3·5·10 s ·
   **Makrofokus** Aus·An (`focus_mode_macro` / zurück auf Kontinuierlich, nicht „auto") ·
   **Verhältnis** 16:9·4:3 (größte Auflösung mit dem Verhältnis, pro Kamera).
   Schema: Label klein grau, Wert groß weiß, rechts runde Icon-Buttons, aktiv = weiß voll.
3. Dock links: Kreis 40 dp `rgba(255,255,255,0.10)`, Icon 22 dp weiß (messen), Zeile des
   Umschalters, fest am Rand, Glyph dreht, Kreis nicht. Öffnet das Blatt.
4. Swipe-Down auf dem Sucher öffnet das Blatt; Entsperren bleibt; nicht während Aufnahme.
5. Nicht anfassen: Icon-Leiste, `PopupView`, `settings`-Taste, Farben (7.3).

## Test (gebündelt)
Jede Zeile beidseitig (Blatt↔Popup), Kamerawechsel, Timer-Foto, Makro, 16:9↔4:3 sichtbar,
Öffnen per Button/Wisch, Schließen per Wisch/Tipp/Zurück, alle vier Lagen, Immersive.

## Abschluss
SPEC 4.5 (Stand; PM-Punkt 10: A erledigt), `docs/changelog.md` ≤ 10 Zeilen,
`ui-spezifikation.md` 4/4b/6/7.1(2–3)/8 (Abweichungen), `graphify update .`, Commit mit
Pfaden, Push, drei Zeilen für den PM-Chat.
