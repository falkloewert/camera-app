# Referenz: Kopfzeile der Pixel-Kamera (Screenrecord 2026-09-21)

**Quelle:** `/home/falk/Downloads/screen-20260921-205547-1790016816715.mp4` (173 MB, liegt
außerhalb des Repos, wird nicht mitversioniert) und die vier Screenshots
`Screenshot_20260921-2055{01,11}.png` / `2058{09,13}.png` aus demselben Ordner.
960 × 2142 px, 2,25 px/dp → 427 × 952 dp, 130,5 s, Pixel 9 Pro.

**Verfahren:** Alle 130 Sekunden mit `ffmpeg` in Einzelbilder zerlegt (2 fps und 4 fps über
die volle Länge, zusätzlich 4 fps um die Aufnahme-Fenster) und die Kopfzeile per
Pixelauswertung vermessen (Weißanteile, Bounding-Boxen, Spalten-/Zeilenprofile). Alle Maße
stehen in px des 960×2142-Bildes **und** in dp (px / 2,25); Farben sind aus den Bildpixeln
gelesen. Zusätzlich Gemini 3.1 Pro über Einzelbilder befragt – seine Antworten deckten sich
mit der Messung, die Zahlen hier sind aber alle gemessen. Wo nur das Kontaktraster
herangezogen wurde, war die Modell-Antwort unzuverlässig (siehe Abschnitt 6).

## Was der Record zeigt – und was nicht

- Durchgehend **Hochformat**. Kein einziges der 522 geprüften Bilder (4 fps, volle Länge)
  zeigt eine gedrehte Oberfläche; der Videostrom bleibt über alle 130 s 960 × 2142.
  **Zum Querformat sagt der Record also nichts** – siehe Abschnitt 1.
- Kopfzeile = **zwei Elemente**: Auflösungs-Chip links, „?"-Icon rechts. Sonst nichts.
- **Kein Makro-Chip** und keine zweite Chip-Variante mit Symbol (alle 522 Bilder geprüft).
- Das **„?" wird nie angetippt** (kein Ripple, kein Hilfe-Fenster in keiner Sekunde).
- **Kein Menü/Dialog aus der Kopfzeile** – auch beim Wechsel der Chip-Beschriftung nicht.
- Bei laufender Aufnahme ist die **Kopfzeile komplett ausgeblendet** (Abschnitt 6).
- Fremdes im Bild: Sekunden 0–1 und 119–130 zeigen andere Apps (Launcher, Galerie);
  93–98 s schiebt sich die System-Benachrichtigungsleiste über die Kopfzeile. Nicht die App.

## 1. Querformat

Der Record zeigt **kein Querformat**, die Frage bleibt damit offen. Belegt ist sie aber durch
das bereits im Repo liegende Referenzbild `pixel-original-querformat.png` (2142 × 960 px,
dieselbe Pixel-Kamera-App) – dessen Zahlen sind hier mitgemessen, Quelle ist ausdrücklich
dieses Bild, nicht der Record:

- Das Display ist im Querformat eine **schwarze Spalte links (0–158 px = 70,7 dp breit)** und
  rechts daneben der Sucher. Die Spalte ist genauso breit wie die Kopfzeile im Hochformat hoch
  ist (beides 159 px) – die Leiste wandert also um 90° gedreht an die Seite.
- **„?" sitzt im Querformat oben links in dieser Spalte** (Ring-Mitte 95,5 | 116,5 px =
  42,4 | 51,8 dp), also an derselben Ecke wie im Hochformat oben rechts – nur gespiegelt.
- **Das Makro-Symbol sitzt im Querformat in der Mitte der Spalte** (x 77–118 · y 733–781 px),
  74 px = 32,9 dp über der Chip-Oberkante (Abschnitt 9).
- **Der Auflösungs-Chip sitzt im Querformat unten links** in derselben Spalte (Chip
  78–116 × 855–893 px, Mitte 43,1 | 388,4 dp, also 38,2 dp über dem unteren Rand) und zeigt
  dort „12 MP".
- Die Chip-Position ist im Querformat also **nicht** dieselbe wie im Hochformat: der Chip
  wandert von der oberen in die untere Ecke, das „?" bleibt in der Ecke. Damit stimmt die
  Fassung der Textreferenz („oben links Hilfe-Icon, unten links Auflösungs-Badge") und nicht
  die widersprechende Variante.

## 2. Maße der Chips

Umrechnung 2,25 px/dp.

| Element | px | dp | Füllung / Glyphe |
|---|---|---|---|
| Chip „FHD" | x 62–109 (48 breit) · y 79–114 (36 hoch) | 21,3 × 16,0 | weiß / schwarz |
| Chip „4K" | x 66–105 (40) · y 77–116 (40) | 17,8 × 17,8 | weiß / schwarz |
| Chip „50 MP" | x 66–105 (40) · y 77–116 (40) | 17,8 × 17,8 | weiß / schwarz, zwei Zeilen |
| „?"-Ring | ⌀ 44–46 px | ⌀ 19,6–20,4 | weiß auf Schwarz, Ringstrich ~2 dp |
| Kopfzeilenband | y 0–158 (159 hoch) | 70,7 | Schwarz |

- **Alle drei Chip-Varianten haben denselben Mittelpunkt:** x 85,5 px = 38,0 dp,
  y 96,5 px = 42,9 dp. Die Beschriftung wächst also **symmetrisch um eine feste Achse**
  („FHD" links 62, „4K"/„50 MP" links 66) – der Chip sitzt nicht bündig am linken Rand,
  sondern zentriert in einem linken Slot.
- **Eckenradius:** aus dem Breitenverlauf der obersten Zeilen (48 px → 46 → 44 in den ersten
  drei Zeilen, gleiches Muster beim 4K-Chip) ergibt sich r ≈ 4 px = **1,8 dp ≈ 2 dp**.
  Die angenommene 6 dp ist damit **zu groß** und zu korrigieren. Die Ecken sind nur leicht
  gerundet, kein Pillenradius.
- **Innenabstand:** „FHD" hat 7 px = 3,1 dp links/rechts und 11 px = 4,9 dp oben/unten
  (Glyphe 34 × 14 px = 15,1 × 6,2 dp). „4K" hat 7,5 px = 3,3 dp links/rechts (Glyphe
  25 × 14 px). „50 MP": Glyphe 26 × 29 px, zwei Zeilen übereinander, Zeilenhöhe ~16 px.
- **Das Band ist exakt schwarz (#000000)**, ohne Verlauf und ohne Scrim.
- Die Chip-Oberkante (77–79 px = 34–35 dp) liegt unterhalb einer gedachten 24-dp-Statusleiste;
  eine Statusleiste ist in der App aber nicht eingeblendet (nur wenn die Benachrichtigungsleiste
  heruntergezogen wird, 93–98 s).

## 3. Farben (aus den Bildpixeln)

| Fläche | Hex |
|---|---|
| Hintergrund der Leiste | `#000000` (reines Schwarz, keine Transparenz messbar) |
| Füllung aller Chips | `#FFFFFF` |
| Beschriftung der Chips | `#000000` |
| „?"-Ring und „?"-Glyphe | `#FFFFFF` |
| Hintergrund hinter dem „?" | `#000000` |

Kein Grauton, kein Material-3-`surfaceVariant` – die Leiste ist schlicht schwarz, die Chips
sind invertiert (weiß mit schwarzer Schrift).

## 4. Aktion des „?"

**Wird im Record nie angetippt.** Geprüft über alle 522 Bilder (4 fps): die weiße Fläche im
Bereich des Icons bleibt konstant bei ~656 px; ein Ripple (Touch-Ziel ~48 dp ≈ 108 px Ø) oder
ein geöffnetes Hilfe-Fenster hätte den Wert sofort verändert. Es gibt genau zwei Ausreißer
(93–98 s Benachrichtigungsleiste, 123 s anderes Programm) – beide sind fremde System-UI.
Was das „?" öffnet, sagt der Record also **nicht**.

## 5. Makro-Chip

**Im Record nicht sichtbar, in den Referenz-PNGs aber vorhanden** – Googles Makrofokus-Symbol:
eine weiße Blüte mit Blättern und einem kleinen „A". Der Record lief durchgehend im Video-
bzw. 50-MP-Modus; dort fehlt das Symbol in allen 522 Bildern, ebenso in den vier Screenshots.
Messwerte und Fundstellen in Abschnitt 9.

## 6. Inhalt der Leiste in anderen Modi

- **Foto-Modus** (Sekunde ~2): Chip „50 MP" (zweizeilig), rechts das „?". Sonst unverändert.
- **Video- und Zeitlupen-Modi** (der weitaus größte Teil des Records): Chip „FHD" bzw. „4K",
  rechts das „?".
- Der Chip **wechselt die Beschriftung**, ohne dass ein Menü oder Dialog zu sehen ist:
  „4K" bis ~5 s, „FHD" von ~7 bis ~64 s (mit kurzen 4K-Phasen bei 53–57 s), „4K" ab ~65 s.
  „FHD" statt einer MP-Angabe ist also genau der im Kickoff vermutete Fall.
- **Panorama-Modus: nicht sichtbar.** Im Modus-Karussell taucht „Panorama" nur als Eintrag
  auf, nie als aktiver Modus. Zur Kopfzeile im Panorama-Modus sagt der Record nichts.
- **Während einer laufenden Aufnahme ist die Kopfzeile leer.** In den beiden
  Aufnahme-Fenstern (~23,0–27,0 s und ~32,5–37,3 s) verschwinden beide Elemente:
  zuerst das „?" (bei 23,00 s), rund 0,25 s später der Chip (23,25 s); beim Ende kommt
  zuerst das „?" zurück (27,00 s), dann der Chip (27,50 s). Übergang ohne sichtbare
  Zwischenstufe (≤ 0,25 s). Am unteren Rand läuft dabei der rote Aufnahme-Indikator.
- Hinweis zur Methode: ein Gemini-Durchlauf über das 1-Sekunden-Kontaktraster hat die Modi
  falsch benannt (es sah „Zeitraffer" und erfundene Zahnrad-/Ordner-Symbole in der Kopfzeile).
  Verlässlich sind nur die Einzelbild-Auswertungen; die Kopfzeile enthält außer Chip und „?"
  kein weiteres Symbol.

## 7. Altes Info-Overlay (Batterie, Blitz o. ä.)

**Nicht vorhanden.** Oben links sitzt ausschließlich der Auflösungs-Chip; über die ganze
Breite der Leiste gibt es keine Batterie-, Blitz-, Timer- oder Rasteranzeige, in keiner
Sekunde und in keinem der vier Screenshots. Auch der in `pixel-pro-panel-beschreibung.md`
beschriebene Info-Streifen (Ultraweitwinkel f/1.95 · Autofokus · 1/24s · ISO 642) ist in
diesem Record nicht zu sehen – er gehört zur Foto-Pro-Ansicht des anderen Records.

Nicht Teil der Leiste, nur als Abgrenzung: bei ~4–5 s und ~90 s liegt ein Hinweis-Banner
(dunkle, durchscheinende Fläche mit weißem, zentriertem Text, z. B. „Die Optimierung des
Nachtsichtvideos kann eine Weile dauern") **unterhalb** der Leiste im oberen Sucherbereich.
Maße nicht vermessen.

## 8. Reihenfolge und Anordnung (Hochformat)

Von links nach rechts, eine Zeile, vertikal auf derselben Achse:

1. **Auflösungs-Chip** – linke Kante 62 px (27,6 dp) bei „FHD", 66 px (29,3 dp) bei „4K"/
   „50 MP"; Mitte x 85,5 px = 38,0 dp. Vertikale Mitte y 96,5 px = 42,9 dp.
2. **Makro-Symbol** (nur im 12-MP-Fotomodus, Abschnitt 9) – x 182–224 px, Mitte x 203,0 px =
   90,2 dp, dieselbe Mittelachse wie der Chip; Abstand zur Chip-Rechtskante 77 px = 34,2 dp.
3. **„?"-Icon** – Ring 820–865 px, Mitte x 842,5 px = 374,4 dp; rechte Ringkante 95 px =
   **42,2 dp vom rechten Displayrand**; vertikale Mitte y 95,5 px = 42,4 dp (also dieselbe
   Achse wie der Chip, Abstand der Mitten 0,9 px = 0,4 dp).

Abstände: ohne Makro-Symbol liegen zwischen Chip und „?" 712 px (316 dp) – die beiden sitzen
an den gegenüberliegenden Enden, es gibt dann kein drittes Element dazwischen.
Randabstände gemessen an den sichtbaren Kanten: links 27,6–29,3 dp bis zur Chip-Kante,
rechts 42,2 dp bis zur Ring-Außenkante. Sitzt das „?" wie bei Material üblich in einem
24-dp-Viewport, liegt dessen rechtes Ende bei ~386 dp (41 dp Rand); ein 48-dp-Touchziel
endet bei ~398 dp (29 dp Rand).

**Sichtbares Verhalten:** Chip-Beschriftung wechselt mit Modus/Auflösung (50 MP · 4K · FHD,
Hochformat) bzw. 12 MP im Querformat; während einer Aufnahme wird die ganze Zeile
aus- und wieder eingeblendet. Ein Antippen von Chip oder „?" ist im Record nicht zu sehen.

## 9. Makro-Chip – Messwerte (Nachtrag 2026-09-21)

In den Referenz-PNGs vorhanden: eine **weiße Blüte mit Blättern und einem kleinen „A"**
(Automatik) – Googles Makrofokus-Symbol. Ergänzt Abschnitt 5.

**Wo (gemessen, Pixelauswertung Weißanteil > 200):**

| Datei | Makro-Symbol | Auflösungs-Chip |
|---|---|---|
| `pixel-original-hochformat.png` | x 182–224 · y 73–120 | „12 MP", x 66–105 · y 77–116 |
| `pixel-original-hochformat-2026-09-18.png` | dito (identisch) | dito |
| `pixel-sheet-allgemein-hochformat.png` | dito | dito |
| `pixel-sheet-pro-hochformat.png` | dito | dito |
| `pixel-original-querformat.png` | x 77–118 · y 733–781 | „12 MP", x 78–116 · y 855–893 |
| `pixel-sheet-allgemein-querformat.png` | dito | dito |
| `pixel-sheet-pro-querformat.png` | **fehlt** | „12 MP", x 78–116 · y 855–893 |
| die 4 Screenshots + Record | **fehlt** (Video-Modus) | „4K" / „FHD" / „50 MP" |

**Maße Hochformat** (960 × 2142, 2,25 px/dp):

- Symbol gesamt: **43 × 48 px = 19,1 × 21,3 dp** (Bbox x 182–224, y 73–120); Blüte+Blätter
  y 79–120, das „A" sitzt oben rechts (x ~197–224, y 73–91) und überschneidet die Blüte.
- Position: linker Rand → Symbol 182 px = **80,9 dp**; Oberkante → Symbol 73 px = **32,4 dp**.
  Der Auflösungs-Chip beginnt bei y 77 px = 34,2 dp, das Symbol sitzt also 4 px höher.
- **Keine Pille, keine Füllung:** schwarzer Hintergrund `#000000`, Glyphe **`#FFFFFF`**
  (reines Weiß; nur Kanten-Aliasing in Graustufen). Im Gegensatz zum invertierten
  Auflösungs-Chip (weißer Block, schwarze Schrift).
- Abstand zum Auflösungs-Chip: Chip-Rechtskante 105 px → Symbol-Linkskante 182 px =
  **77 px = 34,2 dp**.
- Beide sitzen auf **derselben Mittelachse**: Chip-Mitte 85,5 | 96,5 px, Symbol-Mitte
  203,0 | 96,5 px → Δx **117,5 px = 52,2 dp**, Δy 0.

**Maße Querformat** (2142 × 960, um 90° gedreht, Kopfzeile = linke schwarze Spalte 0–158 px):
„?" oben (y 95–138), Makro-Symbol in der Mitte (x 77–118 · y 733–781), „12 MP"-Chip unten
(y 855–893). Symbol **42 × 49 px = 18,7 × 21,8 dp**, linker Rand → Symbol 77 px = 34,2 dp;
Abstand Symbol-Unterkante 781 → Chip-Oberkante 855 = **74 px = 32,9 dp**.

**Wann erscheint er?** Alle Bilder mit Makro-Symbol zeigen den Chip als **„12 MP"**. Wo der
Chip „50 MP" / „4K" / „FHD" zeigt (Record + die 4 Screenshots, alle im Video- bzw.
50-MP-Modus), fehlt das Symbol. *Hypothese (nicht belegt):* das Makrofokus-Symbol wird nur
im einfachen 12-MP-Fotomodus eingeblendet, nicht bei High-Res/Video. Der Record zeigt keinen
Ein-/Ausblend-Moment; er lief durchgehend im Video-Modus.

**Offen:** `pixel-sheet-pro-querformat.png` hat den „12 MP"-Chip, aber kein Makro-Symbol –
unklar, ob das Makrofokus dort auf „Aus" stand oder im Pro-Tab anders dargestellt wird.
