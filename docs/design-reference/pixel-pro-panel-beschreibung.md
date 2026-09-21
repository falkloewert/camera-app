# Referenz: Pro-Panel der Pixel-Kamera (Screenrecord 2026-09-21)

**Quelle:** `/home/falk/Videos/pixel-pro-panel-screenrecord.mp4` (liegt außerhalb des Repos,
wird nicht mitversioniert). 960 × 2142 px, 2,25 px/dp → 427 × 952 dp, 50 fps, 74,2 s, Pixel 9 Pro.

**Verfahren:** Gemini 3.1 Pro hat Einzelbilder des Records beschrieben (Zeitachse über alle
74 Sekunden, dann Detailpässe zu Regler und Tab-Reihe). Alle Maße hier hat Claude zusätzlich am
Einzelbild nachgemessen (Pixelauswertung); sie stehen in px des 960×2142-Bildes **und** in dp
(px / 2,25). Wo Gemini unsicher war, gilt der gemessene Wert. Was nur aus der Beschreibung
stammt und nicht nachgemessen ist, steht als solches dabei.

## Was der Record zeigt – und was nicht

- Die **Pixel-Kamera-App im Pro-Modus**, durchgehend **Hochformat**.
- **Kein Querformat** in keiner Sekunde (Abschnitt 5 des Kickoffs bleibt damit offen).
- **Keinen Dock-Knopf, der ein Panel öffnet:** die Pro-Zeile ist im Pixel fester Bestandteil
  des Pro-Modus und kommt/geht beim Umschalten des Modus (Sekunde 2,1 bis 3,2). Der „Knopf"
  ist bei uns der rechte Dock-Kreis (7.1 Punkt 3, K13) – den zeigt der Record nicht.
- Aufbau im Pro-Modus: **Regler-Pille über der Auslöser-Zeile, Tab-Reihe ganz unten**. Zoom-Pille,
  Modus-Karussell und die Dock-Zeile sind dabei ausgeblendet. Die **Info-Zeile** gibt es nur im
  Pro-Modus (in der Standard-Ansicht, Sekunde 2,8, fehlt sie).
- Auswahl der Zeitachse (Gemini, Sekunden): 1–2 Pro-Ansicht · 3 kurz geschlossen · 4–12
  Helligkeit · 13–15 Schatten · 16 „Alle zurücksetzen" (nur Helligkeit+Schatten) · 17–24 Tabs
  durchgetippt · 25–28 Weißabgleich · 29–41 Fokus (Toast „Fokus-Anzeige aktiv") · 42–51
  Belichtungszeit · 52–66 ISO · 67–68 Weißabgleich · 69–73 „Auto" · 74 Aufnahmebalken.

## 1. Übergang (Sekunde 2,1–3,2)

Der Record öffnet und schließt **kein Panel über dem Sucher**, sondern blendet zwischen der
Pro-Ansicht und der Standard-Ansicht um:

- **Pro-Ansicht:** Regler-Pille schwebt über dem Sucher (dort, wo sonst die Zoom-Pille sitzt),
  Tab-Reihe ganz unten. Zoom-Pille, Modus-Karussell, Foto/Video-Umschalter und Dock-Zeile fehlen.
- **Standard-Ansicht:** Zoom-Pille `,5 1× 2 5`, Modus-Karussell (`Porträt · Foto · Nachtsicht …`),
  Dock-Zeile mit Umschalter; die Pro-Zeile fehlt.
- Der Wechsel ist ein **Überblenden an Ort und Stelle**, kein Schieben über den Schirm: im Bild
  2,4 s liegen Zoom-Pille (blass) und Pille der Pro-Ansicht übereinander, darunter blendet das
  Modus-Karussell ein. Dauer grob 0,6–0,8 s (aus den 0,2-s-Schritten geschätzt, nicht gemessen).
- **Kein Fingertipp und kein Ripple ist zu sehen** (die Aufnahme zeigt auch keine Touch-Punkte
  in diesen Sekunden) – womit der Pixel den Modus umschaltet, zeigt der Record nicht. Bei uns
  ist es der rechte Dock-Knopf (Kickoff 3), die Geste bleibt die des Einstellungen-Blattes.

## 2. Info-Zeile (nur in der Pro-Ansicht)

Streifen direkt unter der schwarzen Kopfzeile, über die volle Breite:

- Kopfzeile schwarz bis y 152 (68 dp), darunter der Streifen bis y 305 (68 dp hoch), leicht
  abgedunkelt über dem Sucher (Alpha nicht sicher bestimmbar – Gemini: „abgedunkelt, verlaufend").
- Texte weiß, von links nach rechts, je Angabe eine eigene Spalte, linksbündig:
  `Ultraweitwinkel f/1.95` (drei Zeilen: „Ultrawe" / „itwinkel" / „f/1.95" – bricht mitten im
  Wort, weil die Spalte schmal ist) · `Autofokus` · `Ⓢ 1/24s` · `Ⓢ ISO 642`.
  Das Symbol vor den beiden letzten Werten ist ein kleiner Kreis mit Schrägstrich
  (Gemini: „automatisch"), ⌀ ~26 px = 12 dp.
- Zeilenhöhe ~45 px (20 dp), Versalhöhe ~30 px (13 dp) → Schrift ~19–20 dp.
- Spaltenanfänge (px): 87 · 280 · 508 · 715.
- Die senkrechten Linien bei x 320 und 640 sind die **Sucher-Hilfslinien**, keine Trenner.
- **Bau:** die Info-Zeile gehört zur Kopfzeile (Audit-Schritt C, 7.1 Punkt 4) und ist in diesem
  Kickoff nicht als Bauteil genannt – hier nur als Referenz festgehalten.

## 3. Regler

Eine Pille trägt genau einen Regler (den des aktiven Reiters); sie schwebt über dem Sucher,
waagerecht mittig, dort wo sonst die Zoom-Pille sitzt.

**Pille** (gemessen an Sekunde 1,0):

| Maß | px | dp |
|---|---|---|
| links · rechts | 156 · 804 | 69 · 357 |
| Breite | 648 | **288** |
| oben · unten | 1314 · 1420 | 584 · 631 |
| Höhe | 107 | **48** |
| Eckenradius | 54 | **24** (voll abgerundet) |

- Füllung: **schwarz, ~33 % Deckkraft** (über hellem Holz gemessen: Ergebnis 50/36/33 bei
  Hintergrund 150/120/100). Bei uns ist die vorhandene Pille `zoom_stepper_background`
  `#99000000` (schwarz 60 %) – Kickoff: keine neuen Farben.
- **Endsymbole** links x 189–231, rechts x 730–771 (je 43 px = **19 dp**), senkrecht mittig,
  weiß, je Reiter verschieden. Im Record: Helligkeit = achtzackiger Stern (rechts mit Punkt),
  Weißabgleich = Thermometer blau „−" / rot „+", Fokus = Punktwolke / Berg, ISO und
  Belichtungszeit = durchgestrichener Kreis bzw. Stern (`+` rechts).
- **Teilstriche:** 1 px breit (~0,9 dp), **kurze 16 px (7,1 dp)**, **hohe 29 px (12,9 dp)**,
  Abstand **18 px (8 dp)**, jeder vierte hoch (**32 dp**); unten auf y 1373 bündig; nach außen
  hin schwächer (Alpha-Verlauf). Farbe weiß, teils gedimmt.
- **Mittelbalken** (Wertanzeiger): x 475–484 (**4,4 dp** breit), y 1339–1374 (**16 dp** hoch),
  weiß, abgerundet. Er **steht fest** in der Bildmitte (x 480) – die Skala wandert darunter
  durch. Nachgemessen in Sekunde 45 und 60 (Belichtungszeit bzw. ISO): der Balken liegt in
  beiden Bildern auf 480, die Zahlen wandern. (Der erste Eintrag hier behauptete das Gegenteil;
  korrigiert am 2026-09-21 am Einzelbild.)
- **Die Zahlen stehen unter den hohen Teilstrichen**, und ihre Abstände sind **ungleich** – sie
  sitzen auf den runden Werten der Pixel-Skala (ISO: 100 → 200 sind 6 Striche, 200 → 400 zehn,
  400 → 800 dreizehn). Die Skala selbst ist also gleichmäßig geteilt, die Beschriftung nicht.
- **Wert-Blase** darüber: hell (gemessen `#C5C5C5` über dunklem Sucher), **36 dp hoch**, Radius
  = halbe Höhe, Breite nach Text („1/10000s" → 190 px = 84 dp); dunkler, fetter Text mit
  Versalhöhe ~33 px (**15 dp**); Unterkante ~20 px (**9 dp**) über der Pille; erscheint **nur
  beim Verstellen** (in Sekunde 1,0 ohne Blase, in 45,0 mit).
- **Zahlen unter den hohen Teilstrichen** gibt es nur bei Belichtungszeit (250 · 60 · 30 · 15 ·
  8 · 4 · 2, also Nenner von 1/x s) und ISO (50 · 100 · 200 · 400 · 800) – Pixel-eigene Skalen.
- Bei Schatten, Weißabgleich und Fokus steht **rechts neben der Pille ein zusätzlicher runder
  Knopf** (x 822–929 = 48 dp, Kreis, Icon „Kreis mit Pfeil" = Rücksetzen/Auto auf den Wert).

## 4. Tab-Reihe (unterste Zeile, waagerecht scrollend)

| Maß | px | dp |
|---|---|---|
| Kachel | 126 × 126 | **56 × 56** |
| Abstand | 45 | **20** |
| Raster (Mitte zu Mitte) | 171 | **76** |
| Eckenradius | ~36 | ~16 |
| aktiver Kreis ⌀ | 124 | **55** |

- Ruhend: dunkles abgerundetes Quadrat `#1F1F1F` mit weißem Symbol (`#E2E2E2`).
- Aktiv: **weißer Kreis**, Symbol dunkel (`#1B1B1B`).
- Beschriftung **unter** der Kachel, zentriert, ~14 dp, `#E2E2E2`; lange Namen brechen um,
  auch mitten im Wort („Alle" / „zurücksetzen", „Belichtungsze" / „it").
- Reihenfolge im Record: `Alle zurücksetzen · Helligkeit · Schatten · Weißabgleich · Fokus ·
  Belichtungszeit · ISO`.
- **Trennstrich** zwischen „Alle zurücksetzen" und dem ersten Regler-Reiter: x 204–208
  (**2 dp**), y 1865–1927 (**28 dp**), `#575757`.
- Über der Reihe ein kleiner grauer Balken (36 × 3 dp, mittig, y ~1783): **der Griff zum
  Schließen**, dasselbe Stück wie am Einstellungen-Blatt – nicht der Scrollbalken der Reihe.
  Im Record ist kein Wisch nach unten zu sehen (Abschnitt 6), Falk kennt die Geste aber von der
  Foto-Ansicht.
- Hinter der ganzen unteren Zone liegt eine **eigene dunkle Fläche** (Abfrage der laufenden
  Google-Kamera am 2026-09-21: `manual_panel_container` = `[0,1750][960,2142]`, also **174 dp**
  hoch und bis zur Bildunterkante); die Kante liegt ~15 dp über dem Griff.
- Ein Reiter mit **manuell verstelltem** Wert zeigt seine Kachel **grau** (aufgehellt, dunkles
  Symbol) statt dunkel; im Record ab Sekunde 29,5 beim Weißabgleich zu sehen (Gemini-Beschreibung).

## 5. Querformat

**Nicht im Record** – durchgehend Hochformat (Gemini, alle 74 Bilder). Also nichts zu messen:
beim Bauen gilt das Drehmodell der App (Fenster fest Hochformat, nur Glyphen drehen,
`MainUI.rotateGlyphs()`), wie beim Einstellungen-Blatt.

## 6. Zurücksetzen und Schließen

- „Alle zurücksetzen" setzt **nur Helligkeit und Schatten** zurück; Einblendung (Sekunde 16–21)
  mit dem Text **„Helligkeit und Schatten wurden zurückgesetzt."**, weißer Text auf dunkler
  Blase, unteres Drittel über der Tab-Reihe. Einzelne Reiter haben zusätzlich den Auto-Knopf
  rechts der Pille (Abschnitt 3).
- Beim Umschalten auf den Reiter Fokus erscheint ab Sekunde 29 die Einblendung
  **„● Fokus-Anzeige aktiv"** (kleiner magenta Punkt davor) – das ist das Fokus-Peaking des
  Pixel. Es bleibt bis Sekunde 41 an. **Wir bauen dafür den vorhandenen Schalter
  `ShowFocusPeakingPreferenceKey`** (Kickoff 3).
- Schließen der Pro-Ansicht: siehe Abschnitt 1 – sie geht beim Umschalten des Modus weg, es gibt
  im Record kein eigenes Schließen (kein Wischen nach unten).

## Was wir abweichend bauen (im Record vorhanden, im Kickoff nicht genannt)

- **„Schatten"** entfällt (kein Gegenstück, `ui-spezifikation.md` 4b) – nicht neu aufmachen.
- **„Helligkeit"** der Vorlage ist unsere **Belichtung** (`R.id.exposure_seekbar`).
- **Zahlen unter den Teilstrichen** werden gebaut, aber aus **unseren** Skalen
  (`ManualSeekbars`) – runde Werte bekommen ihre Zahl, wo sie weit auseinanderliegen, füllt ein
  Zwischenwert die Lücke (sonst stünde auf einem Schirm voller Skala keine einzige Zahl). Der
  Wert steht zusätzlich in der Blase über dem Regler.
- Der **Auto-Rundknopf rechts der Pille** wird gebaut und ist **immer sichtbar** (Falk
  2026-09-21): ein Druck holt den Standardwert des aktiven Reiters zurück.
- **Beide Endsymbole** je Reiter: links das Symbol mit kleinem Minus, rechts mit kleinem Plus
  (ein Schritt des Reglers, wie die Lautstärketasten).
- Die **graue Kachel** für manuell verstellte Werte wird nicht gebaut (Kickoff nennt nur
  „weißer Kreis = aktiv").
- Reiter-Beschriftungen sind unsere Strings (`Belichtung`, `Weißabgleich`, `Fokus`, `ISO`,
  `Verschlusszeit`) statt der Pixel-Wörter; die Beschriftung darf dafür **breiter als die
  Kachel** sein (in der Vorlage steht „Weißabgleich" einzeilig unter einer schmaleren Kachel).
- **Schließen per Wisch nach unten** am Griff und am Band wird gebaut (Falk 2026-09-21) –
  im Record ist die Geste nicht zu sehen (Abschnitt 6).
