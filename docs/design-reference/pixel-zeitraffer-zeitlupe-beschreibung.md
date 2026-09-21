# Pixel-Kamera: Zeitraffer & Zeitlupe (aus Screenrecord + Screenshots)

Ausgewertet am 2026-09-21 per Gemini aus Falks Referenz in `/home/falk/Downloads/`
(`screen-20260921-205547-…mp4`, 960×2142 px, 130 s; `Screenshot_20260921-205501/-205511/-205809/-205813.png`).
Umrechnung 2,25 px/dp, alle px-Werte ±10 px. Quellen bleiben in Downloads, nicht im Repo.

Zeitachse des Videos: Zeitlupe 0–62 s · 64–88 s Zeitraffer-Modus (Aufnahme nie gestartet) ·
96–120 s Zeitlupe · ab 122 s Immich.

## 1. Modi und Moduswahl

- Waagerechte, **scrollbare** Textleiste unter dem Sucher, oberhalb der Dock-Zeile,
  Texthöhe ≈ 31 dp (y 1771–1841 px). Sichtbar darin: `Nachtsichtmodus` · `Zeitlupe` · `Zeitraffer`
  (Foto/Video/Portrait liegen weiter links außerhalb des Bildes).
- Inaktiv: reiner Text `#FFFFFF`, 14 sp, keine Fläche.
- Aktiv: **hellgraue Pille `#C6C6C6`, kein Rand**, Text `#1B1B1B`, fett, horizontal zentriert
  (Pille „Zeitlupe" 218×70 px = 97×31 dp).
- Beim Wechsel in Zeitlupe/Zeitraffer **kein Hinweis/Toast** (Frames 2–10 s).

## 2. Stärke („Geschwindigkeit")

Ruft man über den rechten Dock-Knopf auf; die Chip-Leiste erscheint dann an der Stelle der
Zoom-Pille über dem Auslöser (y 1310–1385 px, ≈ 30 dp hoch) und ersetzt sie.

- **Zeitraffer:** dunkle Pille x 120–870 px, darin scrollbare Chips
  `Autom.` (ausgewählt, mit ▷▷-Symbol) · `5×` · `10×` · `30×` · `120×`.
  Ausgewählter Chip = helle Pille `#C6C6C6` mit dünnem Rand, schwarzes Label, ≈ 89×36 dp;
  die übrigen weißer Text ohne Fläche.
- **Zeitlupe:** **zweigeteilter Umschalter** in einer dunklen Pille (162×30 dp):
  links `1/8×` (inaktiv, weiß) · rechts `◌▷ 1/4×` (aktiv, helle innere Pille 87×36 dp).
- Der aktuell wirksame Wert steht zusätzlich dauerhaft auf dem rechten Dock-Knopf.

## 3. Beschriftungen (wörtlich)

`Nachtsichtmodus` · `Zeitlupe` · `Zeitraffer` · `Autom.` · `5×` · `10×` · `30×` · `120×` ·
`1/8×` · `1/4×` · Auflösungs-Badge `4K` (Zeitraffer) / `FHD` (Zeitlupe) · Zoom `,5` `1×` `2` `5`.
Timer während der Aufnahme: `00:02 → 00:08` (links Echtzeit, rechts die zu erwartende
Videolänge; bei 1/4× ist rechts = 4× links).

## 4. Dock-Zeile (unterste Reihe)

Links Settings-Kachel, Mitte der Foto/Video-Umschalter, rechts der **Stärke-Knopf**: dunkle
Pille `#333333` (≈ 89×48 dp) mit weißem Symbol und Wert — `▷▷ Autom.` im Zeitraffer,
`◌▷ 1/4×` in der Zeitlupe. Auslöser: dunkler Ring Ø 84 dp, weiße Scheibe Ø 53 dp mit dem
Symbol des Modus (▷▷ bzw. gestrichelter Kreis mit Play).

## 5. Während der Aufnahme

- Statt der weißen Scheibe ein **rotes, stark gerundetes Quadrat** `#ED0000`, ≈ 46×46 dp.
- **Rote Timer-Pille** `#ED0000` (147×24 dp) unter dem Auslöser, weiße Schrift,
  Format `00:0X → 00:0Y` (Beispiel 1/4×: `00:04 → 00:16`).
- Links vom Auslöser **Pause-Knopf** (54×54 dp, halbtransparent schwarz, zwei weiße Balken)
  an der Stelle des Thumbnails; rechts der weiße Foto-Kreis.
- Ausgeblendet während der Aufnahme: Auflösungs-Badge, `?`, Modus-Leiste, Zoom-Pille
  (blendet sich zeitweise selbst aus, bei Berührung wieder da).
- Kein Fortschrittsbalken, kein roter Punkt, keine zusätzliche Info-Zeile.

## 6. Nach dem Stopp

Sofort wieder weißer Auslöser, Timer und Pause/Foto verschwinden, Modus-Leiste und Badge
kommen zurück. **Keine** automatische Vorschau, **kein** Toast. Nur das Thumbnail unten links
(52×52 dp) zeigt kurz eine Ladeanimation und dann das neue Video. In Immich erscheinen die
Clips als normale Kacheln mit Videolänge, **ohne** Zeitraffer-/Zeitlupen-Kennzeichnung.

## 7. Querformat

Der Record zeigt durchgehend Hochformat (auch alle vier Screenshots) – zur Drehung der
Elemente in diesen Modi sagt die Referenz nichts.

## 8. Entscheidungen für unseren Nachbau

- **„Autom." entfällt** (Falk, 2026-09-21): Google wählt die Stärke nach der Aufnahmelänge,
  unsere Stärke steht beim Start fest (sie geht als Aufnahme-Bildrate ins MP4). Angeboten
  werden `5× · 10× · 30× · 120×` (Zeitraffer, 5× vorausgewählt) und das, was die Kamera an
  Zeitlupe kann (Pixel 9 Pro: `1/8× · 1/4×`, 1/4× vorausgewählt).
- **Die Stärke-Auswahl sitzt nicht im Pro-Panel**, sondern in einer eigenen kleinen Leiste an
  derselben Stelle (Falk, 2026-09-21): unser Pro-Panel ist foto-only (Kickoff 4, Video-Auto-Torch
  hängt an ISO/Verschlusszeit), in Video ist der rechte Dock-Knopf aus. Die Reiterzeile der
  Referenz entfällt damit.
- Modus-Leiste bei uns (nicht scrollbar): `Video · Zeitlupe · Zeitraffer` im Videomodus,
  `Foto · Panorama` bleibt im Fotomodus.
