# Kickoff: Schritt L – Sucher-Overlays (Fokus-Ring, Raster, Wasserwaage, Sucher-Lage)

Redesign-Schritte A–K sind abgenommen (2026-09-23). Übrig: dieser Schritt, danach Farben/Blur/
Schrift (7.3). Arbeitsstand `master`. Maße 2,25 px/dp. Session-Titel:
`camera-app – Schritt L Sucher-Overlays`.

## 1. Soll (`ui-spezifikation.md` 7.1 Punkte 6–9, 7.2 „Drittel-Raster", Detail-Spec Abschnitt 3)

1. **Fokus-Ring** statt gelbem Rechteck (`DrawPreview` ~L1233): Ring ⌀ 64 dp, Kontur 1,5 dp,
   **gold/gelb** (Konflikt K9 entschieden: Screenshot-Spec), am Berührungspunkt, Puls
   1,15 → 1,0, weg nach 4 s ohne Eingriff.
2. **Drittel-Raster** (`DrawPreview.drawGrids()` ~L935): 1 dp, Weiß 25 %. Heute volle Deckkraft
   und Standard „aus".
3. **Wasserwaage** (Detail-Spec 3): Modus A (Horizont) – zweigeteilte 1-px-Linie, feste Mittel-
   marke, kippende Achse, Grad-Anzeige, bei 0° verschmelzen die Linien, werden Signalgelb
   `#FEE75C` und lösen `CONFIRM` aus. Modus B (Kamera zeigt nach unten) – weißes Fadenkreuz
   12 × 12 dp / 1,5 dp, Toleranzkreis r 34 dp / 1 dp / 60 % Weiß, gelbes Kreuz wandert mit der
   Neigung, beim Treffen Einrasten + `CONFIRM`. Ersetzt die alte grüne Winkellinie
   (`preference_show_angle_line`, `#14e715`).
4. **Sucher-Lage 4:3** (PM-Punkt 7, K4/1a): bei uns endet der 4:3-Sucher über der Moduswahl,
   in der Referenz reicht er bis unter die Auslöser-Zeile. **Aus den Referenz-PNGs messen**
   (`pixel-original-hochformat*.png` gegen `unsere-app-hochformat.png`), dann angleichen. K4 ist
   entschieden: die Bedienelemente liegen bei 4:3 **über** dem Bild, **kein** 28-dp-Eckradius.

## 2. Vorschläge PM (gelten, wenn Falk nicht widerspricht)

- **Keine Regler am Fokus-Ring** (Sonne/Thermometer) und **keine AE/AF-Sperre per langem
  Drücken** – Belichtung und Weißabgleich sitzen im Pro-Panel, die Sperre hat Falk in Schritt C
  gestrichen. Nur der Ring.
- **Smart Chips (7.1 Punkt 9) entfallen** – das sind Googles Hinweis-Pillen („Nachtsicht
  empfohlen" o. ä.), die App hat nichts, was sie zeigen könnte. Wie beim „?".
- **Raster und Wasserwaage:** Standard **an** (wie die Referenz, dort immer sichtbar); der
  Schalter bleibt in der vollen Einstellungen-Aktivität. Ins Blatt nur, wenn die Referenz-PNGs
  eine Zeile dafür zeigen – vorher nachsehen.

## 3. Inventar (Faulheits-Leiter)

- `ui/DrawPreview`: `drawGrids()` ~L935, Winkellinie ~L683, Fokus-Rechteck ~L1233,
  `onDrawPreview()` ~L2622. Keys: `PreferenceKeys.ShowGridPreferenceKey` (318),
  `ShowAngleLinePreferenceKey` (300). Neigungswerte liefert schon der Orientierungs-Sensor, den
  die Winkellinie nutzt – nichts Neues anmelden.
- Haptik über `MainUI.performHapticFeedback()` / `performHeavyClick()` (gibt es schon).
- Sucher-Lage: `MainUI.layoutUI()` und die `preview`-Einträge in `activity_main.xml`.

## 4. Bauen und Fallstricke

- `DrawPreview`-Overlays bleiben **ortsfest** (Pixel-Drehmodell K3); nur Grad-Zahl und Symbole
  drehen wie Glyphen. Wasserwaage im Querformat am Gerät ansehen.
- Animationen: nur der Ring-Puls und das Einrasten sind neu – beide stehen in der Detail-Spec,
  sonst die bestehende Regel (SPEC 4.5, feste Entscheidungen).
- Nichts in Farben/Blur/Schrift anfassen (Schritt 7.3).
- Sucher-Lage verschiebt Tipp-zum-Fokussieren-Koordinaten: am Gerät prüfen, dass der Ring dort
  sitzt, wo getippt wurde, auch bei 16:9.

## 5. Nicht Teil dieses Schritts

Haptik-Profil (7.1 Punkt 10), Doppeltippen (Feature 8), Hardware-Tasten (Punkt 13),
Panorama-Bildzahl (SPEC 4.5, offen).

## 6. Abschluss (nach Falks Gerätetest)

`docs/SPEC.md` 4.5 + Übersicht 3, `ui-spezifikation.md` 7.1 Punkte 6–9 und 7.2 „Drittel-Raster",
PM-Punkt 7 schließen, `docs/changelog.md` ≤ 10 Zeilen, `graphify update .`, Commit mit
Dateipfaden + Push, drei Zeilen für den PM-Chat. Kein nächstes Thema anfangen.
