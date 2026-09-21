# Kickoff: Schritt C – Kopfzeile, dann Icon-Leiste + Popup weg (SPEC 4.5, PM-Punkt 10)

Letzter großer Redesign-Schritt. Arbeitsstand `master` (Pro-Panel, Einstellungen-Blätter,
Objektivwahl sind drin und am Pixel 9 Pro abgenommen). **Vorbedingung:** das
Abschlussprotokoll von SPEC 4.6 (Zeitraffer & Zeitlupe) liegt vor – das Popup enthält heute
selbst „Video-Bildrate/Zeitlupe" (`ui/PopupView.java:789`), und die Moduswahl wird hier
angefasst. Maße: 2,25 px/dp. Session-Titel: `camera-app – Schritt C Kopfzeile`.

## 1. Soll – gemessen, nicht aus der Textreferenz

Alle Werte aus `docs/design-reference/pixel-kopfzeile-beschreibung.md` (Pixelmessung über
522 Einzelbilder des Screenrecords; die Textreferenz `ui-spezifikation.md` ist an diesen
Stellen **falsch** und wird nachgezogen, siehe 3.5).

| Element | Position | Größe | Farbe |
|---|---|---|---|
| Auflösungs-Chip | links, feste Achse Mitte x 38,0 dp · y 42,9 dp | „FHD" 21,3 × 16,0 dp; „4K"/„50 MP" 17,8 × 17,8 dp; **Radius ~2 dp** (nicht 6 dp); Innenabstand 3,1 dp seitlich | Füllung `#FFFFFF`, Schrift `#000000` |
| „?"-Icon | rechts, Ringmitte x 374,4 dp (42,2 dp vom rechten Rand), dieselbe Achse wie der Chip | **weißer Ring ⌀ ~20 dp**, Strich ~2 dp | `#FFFFFF` auf `#000000` |
| Kopfzeilenband | über dem Sucher | 70,7 dp (159 px) hoch | `#000000`, kein Verlauf, kein Scrim |

- **Kein Makro-Chip** – in allen 522 Bildern nicht vorhanden (Frage 3.1 ist damit erledigt).
- **Beschriftung:** Foto „50 MP" (zweizeilig), Video/Zeitlupe „4K" bzw. „FHD". Wechsel ohne
  sichtbares Menü.
- **Während einer Aufnahme ist die ganze Zeile aus:** erst das „?", ~0,25 s später der Chip;
  beim Ende umgekehrt.
- **Querformat** (belegt aus `pixel-original-querformat.png`, 2142 × 960 – der Record selbst
  zeigt durchgehend Hochformat): die Leiste wandert als 70,7 dp breite schwarze Spalte an die
  Seite, „?" oben links, **Chip unten links**, dort „12 MP".

## 2. Inventar: was heute woran hängt

**Icon-Leiste** (`activity_main.xml`, aufgebaut in `ui/MainUI.java:411-507`): 5 Kopf-Icons
(`settings`, `popup`, `exposure`, `exposure_lock`, `focus_peaking`), **13 Zustands-Icons**
(`ui/OnScreenIcons.java:57-69`), dazu `trash`, `share`, `kraken_icon`.

**Drei-Punkte-Popup** (`ui/PopupView.java`): 17 Einträge, **12 davon ohne Blattzeile**
(Fokus-Modus, Fotomodus-Liste, Rauschunterdrückung, Auto-Stabilisieren, Blende, Burst-Bilder,
Bracketing-Bilder +2 Schalter, Video-Bildrate/Zeitlupe, Repeat, Grid, Szenemodus, Farbeffekt).

**Harte Kopplungen** (vor dem Abriss lösen, Risiko absteigend):

1. `MainActivity.canOpenSettingsSheet():2676` sperrt das Blatt, solange das Popup offen ist;
   `MainUI.togglePopupSettings():2360` schließt umgekehrt das Blatt. Einseitiges Entfernen
   macht das Blatt unerreichbar.
2. `PopupView.createButtonOptions` speist auch die ISO-Knöpfe im Pro-Panel
   (`MainUI.java:1860`) – vor dem Löschen trennen.
3. `exposure`-Knopf + `sliders_container` (`MainUI.java:713-733`) neben dem Pro-Panel.
4. Tests/Fernbedienung: 43 Treffer in `MainActivityTest.java`,
   `BluetoothRemoteControl.java:169,183`, `MainUI.onKeyDown():2500`, `commandMenu*`.
5. `DrawPreview`/`Preview` haben **keine** direkten Popup-Referenzen.

**Regel:** keine Funktion darf eine Runde lang unerreichbar sein. Erst die Kopfzeile
fertigstellen, dann Icon-Leiste und Popup in **einem eigenen Commit** entfernen.

## 3. Offene Entscheidungen für Falk

1. **Triage der 25 Funktionen** (13 Icons + 12 Popup-Einträge): je Funktion Kopfzeile /
   Blatt / nur volle Einstellungen / weg. Faktenlage: 4 deckt das Blatt schon ab, 11 stehen
   in der vollen Einstellungen-Aktivität (bleiben dort), 7 brauchen eine Entscheidung
   (`exposure_lock`, `white_balance_lock`, Fokus-Modus-Liste, Blende, NR-Modus, Burst-Anzahl,
   Bracketing), 2 gehören nicht zu C (`Video-Bildrate/Zeitlupe` → 4.6; Fotomodus-Liste → 7.2).
2. **Reihenfolge-Falle:** die volle Fotomodus-Liste (DRO, HDR, Burst, Nacht) hängt heute
   **ausschließlich** am Popup – `preference_photo_mode` steht in keiner XML, die Modusleiste
   kennt nur Foto und Panorama. Reißt C das Popup ab, bevor 7.2 (Karussell) gebaut ist, sind
   diese Modi über die Oberfläche nicht mehr erreichbar. Auswege: C wartet auf 7.2, oder das
   Blatt bekommt übergangsweise eine Modus-Zeile.
3. **Querformat:** die Referenz verschiebt den Chip im Querformat in die untere Ecke – unser
   Pixel-Drehmodell (K3: Fenster bleibt Hochformat, nur Glyphen drehen) verschiebt nichts.
   Was gilt?
4. **Das „?" hat keine belegte Aktion** – im Record wird es nie angetippt. Als reine Optik
   bauen (ohne Wirkung), weglassen, oder mit einer eigenen Hilfe belegen?
5. **Widerspruch Textreferenz ↔ Messung:** gemessene Werte schlagen laut fester Entscheidung
   beide Textreferenzen. `ui-spezifikation.md` 7.1 führt für die Kopfzeile noch „dunkle
   Pille" mit 6 dp Radius und einen dunklen Kreis mit 24-dp-Glyph – das ist nach den
   Messwerten falsch und wird im Abschluss mitkorrigiert.

Nichts erfinden – was die Referenz nicht zeigt, wird nicht gebaut.

## 4. Bauen

- Kopfzeile in Code wie der Rest (`ui/MainUI.layoutUI()`); der XML-Umzug bleibt **danach**
  (Ponytail-Fund 2 – C ändert das Layout, zwei Umstellungen wären teurer als eine).
- Keine neuen Farben und Icon-Sätze: Schwarz/Weiß der Kopfzeile plus die vier vorhandenen
  Farben; das „?" als Ring aus dem vorhandenen Glyph-Satz.
- Keine neuen Animationen (erlaubt bleiben Auslöser, Glyph-Drehung, Rad-Morph, Blatt-Gleiten).
- Das Pixel-Drehmodell gilt weiter, bis 3.3 geklärt ist.

## 5. Abschluss (erst nach Falks Gerätetest)

`docs/SPEC.md` 4.5 (Offen → Erledigt, PM-Punkt 10 abschließen) und Übersicht 3,
`ui-spezifikation.md` 7.1 Punkt 4 (inkl. Korrektur aus 3.5) + 7.2-Eintrag, `docs/changelog.md`
≤ 10 Zeilen mit dem Inventar aus Abschnitt 2, `graphify update .`, Commit mit Dateipfaden +
Push, drei Zeilen für den PM-Chat. Kein nächstes Thema anfangen.
