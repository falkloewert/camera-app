# Kickoff: Schritt C – Kopfzeile, dann Icon-Leiste + Popup weg (SPEC 4.5, PM-Punkt 10)

Letzter großer Redesign-Schritt. Arbeitsstand `master` (Pro-Panel, Einstellungen-Blätter,
Objektivwahl sind drin und am Pixel 9 Pro abgenommen). **Vorbedingung:** das
Abschlussprotokoll von SPEC 4.6 (Zeitraffer & Zeitlupe) liegt vor – das Popup enthält heute
selbst „Video-Bildrate/Zeitlupe" (`ui/PopupView.java:789`), und die Moduswahl wird hier
angefasst. Maße: 2,25 px/dp. Session-Titel: `camera-app – Schritt C Kopfzeile`.

## 1. Soll – gemessen, nicht aus der Textreferenz

Alle Werte aus `docs/design-reference/pixel-kopfzeile-beschreibung.md` (Pixelmessung über
522 Einzelbilder des Screenrecords; die Textreferenz `ui-spezifikation.md` ist an diesen
Stellen **falsch** und wird im Abschluss nachgezogen).

| Element | Position | Größe | Farbe |
|---|---|---|---|
| Auflösungs-Chip | links, feste Achse Mitte x 38,0 dp · y 42,9 dp | „FHD" 21,3 × 16,0 dp; „4K"/„50 MP" 17,8 × 17,8 dp; **Radius ~2 dp** (nicht 6 dp); Innenabstand 3,1 dp seitlich | Füllung `#FFFFFF`, Schrift `#000000` |
| Makro-Chip (Blüte + „A") | **rechts neben** dem Auflösungs-Chip, gleiche Mittelachse – **nicht** darunter gestapelt, wie die Textreferenz behauptet | 19,1 × 21,3 dp (43 × 48 px), Abstand zur Chip-Kante 34,2 dp | Glyphe `#FFFFFF` auf `#000000`, **keine Pille, keine Füllung** |
| Kopfzeilenband | über dem Sucher | 70,7 dp (159 px) hoch | `#000000`, kein Verlauf, kein Scrim |

- **Das „?" entfällt** – es ist Googles Hilfe-Symbol, Falk will es nicht (Entscheidung
  2026-09-21). Der Record zeigt ohnehin keine Aktion darauf.
- **Beschriftung:** Foto „50 MP" (zweizeilig), Video/Zeitlupe „4K" bzw. „FHD". Bei uns zeigt
  der Chip unsere Auflösung (12 MP Standard), nicht die 50 MP der Referenz.
- **Während einer Aufnahme ist die ganze Zeile aus:** erst das „?", ~0,25 s später der Chip;
  beim Ende umgekehrt.
- **Querformat** (belegt aus `pixel-original-querformat.png`, 2142 × 960): die Leiste wandert
  als 70,7 dp breite schwarze Spalte an die Seite – „?" oben, Makro-Symbol in der Mitte, Chip
  unten links. **Bei uns bleibt es beim Pixel-Drehmodell (K3)** – Entscheidung Falk 2026-09-21.

## 2. Inventar: was heute woran hängt

**Icon-Leiste** (`activity_main.xml`, aufgebaut in `ui/MainUI.java:411-507`): 5 Kopf-Icons
(`settings`, `popup`, `exposure`, `exposure_lock`, `focus_peaking`), **13 Zustands-Icons**
(`ui/OnScreenIcons.java:57-69`), dazu `trash`, `share`, `kraken_icon`.

**Drei-Punkte-Popup** (`ui/PopupView.java`): 17 Einträge, **12 davon ohne Blattzeile.**

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

## 3. Entschieden von Falk (2026-09-21)

1. **Grundregel:** was doppelt ist, fliegt raus; was in der vollen Einstellungen-Aktivität
   steht, bleibt dort; alles Übrige ins Einstellungen-Blatt.
2. **Belichtungssperre:** streichen – die Belichtung lässt sich im Pro-Panel einstellen, das
   deckt es ab. **Kein Doppeltippen auf den Sucher** (siehe 6.1).
3. **Weißabgleich-Sperre, Blende, Rauschunterdrückung:** streichen (kein gespeicherter Wert,
   kein Pixel-Pendant).
4. **Fokus-Modus-Liste, Serienbild-Anzahl, Fokus-Bracketing:** ins Einstellungen-Blatt.
5. **Serienbilder:** unbegrenzt, solange der Auslöser gehalten wird (Einstellung im Blatt;
   heute schon so bedienbar). Das Ablegen als **Stapel in der Galerie** ist ein eigener
   Wunsch, siehe 6.2.
6. **Fotomodus-Liste** (DRO, HDR, Nacht …): **keine Übergangszeile** – Falk nutzt die App noch
   nicht aktiv, die Modi dürfen bis zum Karussell (7.2) unerreichbar bleiben.
7. **Makro-Chip:** kommt, Googles Blüten-Symbol – in den Referenz-PNGs gefunden und vermessen
   (Abschnitt 1 und `pixel-kopfzeile-beschreibung.md` Abschnitt 9). Erscheint dort nur im
   einfachen 12-MP-Fotomodus; im Record (Video/50 MP) fehlt er. **Am Gerät prüfen, wann unsere
   App ihn zeigen soll** – vermutlich analog zur Objektivwahl, sobald WW oder NO nah herangeht.
8. Alles nicht ausdrücklich Anderslautende wird wie im PM-Vorschlag umgesetzt.

## 4. Bauen

- Kopfzeile in Code wie der Rest (`ui/MainUI.layoutUI()`); der XML-Umzug bleibt **danach**
  (Ponytail-Fund 2 – C ändert das Layout, zwei Umstellungen wären teurer als eine).
- Keine neuen Farben und Icon-Sätze; Schwarz/Weiß der Kopfzeile plus die vier vorhandenen
  Farben.
- Keine neuen Animationen (erlaubt bleiben Auslöser, Glyph-Drehung, Rad-Morph, Blatt-Gleiten).
- Pixel-Drehmodell (K3) gilt weiter.

## 5. Abschluss (erst nach Falks Gerätetest)

`docs/SPEC.md` 4.5 (Offen → Erledigt, PM-Punkt 10 abschließen) und Übersicht 3,
`ui-spezifikation.md` 7.1 Punkt 4 (inkl. Korrektur der falschen Messwerte) + 7.2-Eintrag,
`docs/changelog.md` ≤ 10 Zeilen mit dem Inventar aus Abschnitt 2, `graphify update .`, Commit
mit Dateipfaden + Push, drei Zeilen für den PM-Chat. Kein nächstes Thema anfangen.

## 6. Neue Wünsche aus dieser Abstimmung – **nicht** Teil von C

1. **Doppeltippen auf den Sucher = Kamera wechseln** (Selfie ↔ hinten). Heute ist
   Doppeltippen anders belegt: `Preview.onDoubleTap():919` löst bei aktivierter Einstellung
   ein Foto aus, und `:933` lässt Doppeltippen-plus-Ziehen als Zoom-Geste durch. Beides müsste
   weichen (Zoomen per Pinch bleibt). Eigener kleiner Chat, gehört in die SPEC-Übersicht.
2. **Serienbilder als Stapel in der Galerie** statt einzelner Bilder. Offen ist der Weg:
   Android/Google Fotos gruppieren Serien über Metadaten, Immich über eigene Stapel – zu
   klären, was davon die App schreiben kann. Eigener Chat, vorher Recherche.
