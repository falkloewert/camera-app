# Kickoff: Schritt C – Kopfzeile, dann Icon-Leiste + Popup weg (SPEC 4.5, PM-Punkt 10)

Letzter großer Redesign-Schritt. Arbeitsstand `master` (Pro-Panel, Einstellungen-Blätter,
Objektivwahl sind drin und am Pixel 9 Pro abgenommen). **Vorbedingung:** das
Abschlussprotokoll von SPEC 4.6 (Zeitraffer & Zeitlupe) liegt vor – das Popup enthält heute
selbst einen Eintrag „Video-Bildrate/Zeitlupe" (`ui/PopupView.java:789`), und die Moduswahl
wird hier angefasst. Ausschnitt aus `ui-spezifikation.md` 7.1 Punkt 4. Maße: 2,25 px/dp.
Session-Titel: `camera-app – Schritt C Kopfzeile`.

## 1. Soll (Referenz gewinnt gegen Textreferenz)

| Element | Position | Größe | Farbe | Aktion |
|---|---|---|---|---|
| Auflösungs-Chip | links, oberste Zeile | Radius 6 dp (13,5 px), Kontur 1 dp (2,25 px) | dunkle Pille, Kontur `rgba(255,255,255,0.4)`, Monospace | K6: **dynamisch** die aktive Auflösung – Foto „12 MP", Video „FHD"/4K |
| Makro-Chip (Blüte + „A") | links, unter dem Auflösungs-Chip gestapelt | n. g. | wie oben | strittig, siehe 3.1 |
| „?"-Hilfe | rechts | Glyph 24 dp (54 px), Strich 2 dp (4,5 px) | Glyph `#FFFFFF`, dunkler Kreis | in der Referenz ohne Aktion, siehe 3.3 |
| Leiste | über dem Sucher, nicht schwebend | 56–64 dp (126–144 px) hoch | blickdicht Schwarz | Icons klein, links/rechts gruppiert, nicht breit verteilt |

Quellen: `ui-spezifikation.md`, `pixel-camera-detailspec.md`, `pixel-camera-screenshots.md`.
**Nicht** in die Kopfzeile: Smart Chips (schweben 80 dp über dem Auslöser,
`detailspec:130-136`); AE/AF-Sperre gehört als Long-Press in den Sucher; Regler bleiben im
Pro-Panel (K13).

## 2. Inventar: was heute woran hängt

**Icon-Leiste** (`activity_main.xml`, aufgebaut in `ui/MainUI.java:411-507`):

- 5 Kopf-Icons `settings`, `popup`, `exposure`, `exposure_lock`, `focus_peaking` – laut
  7.1 Punkt 4 ins Blatt (dort deckt das Blatt sie schon ab).
- **13 Zustands-Icons** (`ui/OnScreenIcons.java:57-69`): exposure_lock, white_balance_lock,
  cycle_raw, store_location, text_stamp, stamp, focus_peaking, auto_level, cycle_flash,
  face_detection, audio_control, cycle_lock_orientation, preview_shots.
  **Keins davon hat heute einen anderen Ort** – das ist die Kernentscheidung von C.
- Dazu in derselben Zeile: `trash`, `share`, `kraken_icon` (nur Anzeige).

**Drei-Punkte-Popup** (`ui/PopupView.java`): 17 Einträge. **12 davon ohne Blattzeile:**
Fokus-Modus, volle Fotomodus-Liste, Rauschunterdrückung, Auto-Stabilisieren, Blende,
Burst-Bilder, Bracketing-Bilder (+2 Schalter), Video-Bildrate/Zeitlupe, Repeat-Modus, Grid,
Szenemodus, Farbeffekt. Abgedeckt sind heute nur Flash, Foto-/Video-Auflösung, Weißabgleich
und Timer.

**Harte Kopplungen** (vor dem Abriss zu lösen, Risiko absteigend):

1. `MainActivity.canOpenSettingsSheet():2676` sperrt das Blatt, solange das Popup offen ist;
   `MainUI.togglePopupSettings():2360` schließt umgekehrt das Blatt. Einseitiges Entfernen
   macht das Blatt unerreichbar.
2. Geteilter Code: `PopupView.createButtonOptions` speist auch die ISO-Knöpfe im Pro-Panel
   (`MainUI.java:1860`) – vor dem Löschen trennen.
3. `exposure`-Knopf + `sliders_container` (`MainUI.java:713-733`) laufen neben dem Pro-Panel.
4. Tests und Fernbedienung: 43 Treffer auf `R.id.popup`/`settings`/`exposure` in
   `MainActivityTest.java`, `BluetoothRemoteControl.java:169,183`, `MainUI.onKeyDown():2500`,
   `commandMenuPopup()`/`commandMenuExposure()`.
5. `DrawPreview`/`Preview` haben **keine** direkten Popup-Referenzen.

**Regel:** keine Funktion darf eine Runde lang unerreichbar sein. Erst die Kopfzeile
fertigstellen, dann Icon-Leiste und Popup in **einem eigenen Commit** entfernen.

## 3. Offene Punkte – am Gerät bzw. im Screenrecord klären, Falk fragen

1. **Makro-Chip** widerspricht SPEC 4.5 (2026-09-21): der `MA`-Knopf ist weg, Makro läuft über
   die Objektivwahl WW/NO. Zeigt der Chip den Linsenzustand oder kommt ein Makro-Schalter zurück?
2. **Querformat:** Screenshots sagen „Chips in eine Ecke, ? gegenüber", K3 (Pixel-Drehmodell,
   nur Glyphen drehen) sagt Position bleibt. Screenshot-Spec schlägt Detail-Spec, aber K3 ist
   eine getroffene Entscheidung – echter Widerspruch, nur der Record löst ihn.
3. **Aktion des „?"-Icons** ist in der ganzen Doku nur als Optik beschrieben.
4. **Hex-Werte** für Leistenhintergrund, Chip-Füllung und „?"-Kreis fehlen in der Referenz.
5. **Chip-Maße** (Höhe, Padding) fehlen; belegt sind nur Radius 6 dp und 1-dp-Kontur.
6. **Die 13 Zustands-Icons:** in die Kopfzeile, ins Blatt, oder nur noch über die volle
   Einstellungen-Aktivität (die bleibt erreichbar)? Betrifft RAW, GPS, Stempel, Auto-Level,
   Gesichtserkennung, Audio, Sperrausrichtung, Vorschussbilder.
7. **Die 12 Popup-Einträge ohne Blattzeile:** welche bekommen eine Blattzeile, welche bleiben
   nur in der vollen Einstellungen, welche entfallen ganz?
8. **Verhalten bei laufender Aufnahme, Panorama, Zeitraffer und Zeitlupe** in der Kopfzeile
   ist nicht spezifiziert.
9. **Altes Info-Overlay** oben links (Batterie/Blitz, `DrawPreview`) kollidiert mit dem
   Auflösungs-Chip: ersetzen oder entfernen?

Nichts erfinden – was die Referenz nicht zeigt, wird nicht gebaut.

## 4. Bauen

- Kopfzeile in Code wie der Rest (`ui/MainUI.layoutUI()`); der XML-Umzug bleibt **danach**
  (Ponytail-Fund 2 – C ändert das Layout, zwei Umstellungen wären teurer als eine).
- Keine neuen Farben, keine neuen Icon-Sätze: vorhandene Bauteile und die vier Farben aus
  `ui/ZoomStepper`/`ui/SettingsSheet` wiederverwenden.
- Keine neuen Animationen (erlaubt bleiben nur Auslöser, Glyph-Drehung, Rad-Morph,
  Blatt-Gleiten). Das Pixel-Drehmodell gilt weiter, bis Punkt 3.2 geklärt ist.

## 5. Abschluss (erst nach Falks Gerätetest)

`docs/SPEC.md` 4.5 (Offen → Erledigt, PM-Punkt 10 abschließen) und Übersicht 3,
`ui-spezifikation.md` 7.1 Punkt 4 + 7.2-Eintrag, `docs/changelog.md` ≤ 10 Zeilen mit dem
Inventar aus Abschnitt 2, `graphify update .`, Commit mit Dateipfaden + Push, drei Zeilen für
den PM-Chat. Kein nächstes Thema anfangen.
