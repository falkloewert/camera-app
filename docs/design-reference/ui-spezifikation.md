# UI-Spezifikation: 1:1-Nachbau der Pixel-Kamera-Oberfläche

Verbindliche Bau-Vorgabe für das Material-3-Redesign (SPEC.md 4.5), abgeleitet aus Falks
Referenz-Screenshots seiner echten Pixel-Kamera-App (2026-09-14 und 2026-09-15).

**Grundregel, von Falk am 2026-09-15 ausdrücklich gesetzt:** *Eins zu eins wie in der
Referenz.* Keine abgeleiteten, "geräteadaptiven" oder verbesserten Varianten, wo die
Referenz einen konkreten Zustand zeigt. Wenn etwas in der Referenz nicht erkennbar ist,
nachfragen statt erfinden.

**Vor jeder Fertigmeldung:** Abweichungen zu dieser Liste ausdrücklich aufzählen, nicht
nur "fertig" melden.

**Ergänzende Zahlen:** `pixel-camera-detailspec.md` (Maße, Farben, Animationen; von Falk
2026-09-17 eingebracht). Bei Widerspruch zu dieser Datei gilt Abschnitt 9.

---

## 1. Aufbau Hochformat (oben nach unten)

1. **Schwarze Kopfzeile** (über dem Sucher, nicht darüber schwebend):
   - links: Auflösungs-Chip "12 MP", daneben Makrofokus-Chip (Blüte mit "A")
   - rechts: Hilfe-Icon "?"
2. **Sucher**: dünnes weißes 3×3-Raster, gelber/goldener Fokus-Ring (dünner Kreis).
3. **Zoom-Stufen-Leiste**: dunkle Pille, **schwebt über dem unteren Bildrand** (noch im
   Bildbereich, nicht auf der schwarzen Fläche). Stufen: `,5  1×  2  5`.
4. **Schwarze Fußfläche** – darin, in dieser Reihenfolge:
   - **Haupt-Bedienzeile**: Galerie-Miniatur (links, abgerundetes Quadrat) – Auslöser
     (Mitte, großer weißer Kreis) – Kamera-Wechsel (rechts, dunkles abgerundetes
     Quadrat mit Wechsel-Pfeilen)
   - **Moduswahl-Leiste**: `zufügen · Porträt · Foto · Nachtsicht · Pan…`, aktiver Modus
     in heller Pille, alle anderen reiner Text, waagerecht scrollbar
   - **Unterste Zeile**: Einstellungen-Button (links, dunkler Kreis mit Zahnrad-Kamera-
     Symbol) – Foto/Video-Umschalter (Mitte, Pille mit zwei Symbolen, aktives in weißem
     Kreis) – Regler-Button (rechts, dunkler Kreis mit Schieberegler-Symbol)

### 1a. Wichtig: die Bedienelemente hängen am Bildschirmrand, nicht am Sucherrand

Aus dem Direktvergleich am 2026-09-15 (Screenshot der echten Pixel-Kamera neben unserem
Nachbau): Zoom-Leiste, Auslöser-Zeile und Moduswahl sind **am unteren Bildschirmrand
ausgerichtet und liegen bei Bedarf über dem Sucherbild**. Bei 4:3 reicht die Vorschau
fast bis unten, dann schweben sie über dem Bild; bei 16:9 endet die Vorschau höher und
dieselben Elemente liegen auf Schwarz. Es sind also **nicht zwei verschiedene Layouts** –
die Reihenfolge Auslöser → Moduswahl bleibt identisch.

### 1b. Feinheiten der Optik (aus dem Direktvergleich)

- **Auslöser:** breiter, halbtransparenter grauer Außenring – nicht der harte dunkle
  Rand, den wir aktuell haben.
- **Zoom-Leiste:** deutlich durchsichtiger (das Motiv schimmert durch), wirkt weich
  abgesetzt. Unsere Pille ist zu deckend.
- **Aktive Modus-Pille:** helles, leicht transparentes Grau mit dunkler Schrift – nicht
  volltonweiß.
- **Kamera-Wechsel:** abgerundetes Quadrat („Squircle"), nicht Kreis.
- **Galerie:** abgerundetes Quadrat mit Vorschaubild, **links neben dem Auslöser**.
- **Obere Leiste:** kleine, filigrane Icons, links und rechts gruppiert – nicht
  gleichmäßig über die Breite verteilt.
- **Sucher:** 3×3-Raster und dünner goldener Fokus-Ring sind dort immer sichtbar.

## 2. Aufbau Querformat

**Drehmodell (verbindlich, seit 2026-09-17 als Pixel-Modell umgesetzt, Datei-Prüfung
2026-09-18 abgeschlossen):** Das Fenster ist **fest auf Hochformat gesperrt** (wie bei
der echten Pixel-Kamera). Es gibt dadurch nur **eine** Anordnung – die aus Abschnitt 1 –,
die niemals neu layoutet wird. Beim Drehen des Geräts drehen sich ausschließlich die
**Glyphen** (Icons, Zoom-Ziffern, Modus-Text, Galerie-Bild, Auslöser-Kern,
Umschalter-Symbole) an Ort und Stelle, so als läge eine durchsichtige Folie mit den
Symbolen über dem unbewegten Hochformat-Layout. Kein Knopf, keine Pille und kein Ring
wechselt Position oder Größe. Konkret:

- Oberkante links (normales Querformat): Glyphen drehen **90° gegen den Uhrzeigersinn**
  zurück in die aufrechte Lage.
- Oberkante rechts (umgekehrtes Querformat): Glyphen drehen **90° im Uhrzeigersinn**.
- Hochformat auf dem Kopf (180°): Glyphen drehen entsprechend um 180°. Anders als beim
  Vorgänger-Modell (siehe unten) bietet das gesperrte Fenster diese Lage jetzt selbst an,
  weil nicht mehr das Fenster, sondern nur die Glyphen gedreht werden.
- **Endlagen unverändert gegenüber dem Vorgänger-Modell:** Symbole und Zoom-Ziffern stehen
  in jeder Lage aufrecht; **Modus-Beschriftungen nicht** – sie stehen im Querformat quer,
  wie auf einer Karte gedruckt (unverändert, jetzt aber ohne die dafür vorher nötige
  eigene View-Klasse, siehe Abschnitt 6).
- Das Fenster selbst dreht **nicht** mehr mit – anders als der ursprüngliche Befund vom
  2026-09-17 (`screencap` 2142×960 bei erzwungener Display-Drehung) nahelegte. Die
  Pixel-Kamera-App ist tatsächlich auf Hochformat gesperrt; der damalige Befund betraf das
  System-Verhalten allgemein, nicht die Pixel-App selbst. Der Gesten-Balken sitzt dadurch
  immer an derselben Fensterkante (unten im Hochformat-Koordinatensystem), unabhängig von
  der physischen Gerätelage.

Ergebnis für die Elemente (Oberkante links), aus den Referenzbildern abgelesen – die
**Positionen** bleiben exakt die aus Abschnitt 1 (Hochformat), nur die **Glyphen darin**
zeigen dieses Bild:

- Zoom-Ziffern bleiben aufrecht, die Zoom-Pille selbst bleibt an ihrem Hochformat-Platz.
- Modus-Beschriftungen stehen quer (90° gedreht), lesbar von unten nach oben.
- Icons (Galerie, Kamera-Wechsel, Einstellungen, Umschalter-Glyphen) drehen zurück in die
  aufrechte Lage.

Bei Oberkante rechts alles spiegelbildlich dazu (Beschriftungen von oben nach unten
lesbar).

**Vorgänger-Modell (2026-09-17, Commit `2045620a`, seit 2026-09-17/18 durch das
Pixel-Modell ersetzt):** Ordnete die drei Fußzeilen bei jeder Drehung tatsächlich neu an
(Fenster drehte mit, Inhalte klappten über `MainUI.orientBar()` um). Funktionierte in den
Endlagen, hatte aber einen sichtbaren Zwischenzustand-Glitch beim Drehen (siehe
`docs/kickoff/rotation-pixel-modell.md`). Die alten Belege
`unsere-app-querformat-nach-umbau-2026-09-17.png` /
`unsere-app-querformat-umgekehrt-nach-umbau-2026-09-17.png` zeigen dieses **Vorgänger**-
Modell, nicht den aktuellen Stand.

## 3. Zoom-Bedienung

### 3.1 Stufen
- **Genau vier Stufen: `,5` (Weitwinkel) · `1×` (Standard) · `2` · `5`.** Keine Stufe
  darüber. Nichts algorithmisch ableiten.
- **Beschriftung:** führende Null entfällt (",5" statt "0,5"); **nur die aktive Stufe
  trägt das "×"** ("1×" aktiv, daneben schlicht "2" und "5").
- Aktive Stufe: voll gefüllter heller Kreis mit dunkler Schrift. Inaktive: reiner heller
  Text ohne Füllung.
- *Offen:* "×" als Malzeichen (×) oder Buchstabe "x"? Aktuell umgesetzt: ×. Die
  Detail-Spec schreibt `1x`/`,5x` mit Buchstabe (Konflikt K11).

### 3.2 Feinjustier-Rad  — NOCH NICHT UMGESETZT
- Zieht man **an der Zoom-Stufen-Leiste** (Wischen statt Tippen), verwandelt sie sich in
  ein **Lineal/Rad** mit Teilstrichen.
- Beschriftete Hauptmarken: `1 · 2 · 5 · 10 · 30`, dazwischen feine Teilstriche.
- Der aktuelle Wert erscheint **daneben in einer hellen Pille**, z. B. `6,7×`.
- Im Querformat steht das Lineal **senkrecht** (siehe Referenzbild), im Hochformat
  waagerecht.
- Alles oberhalb von 5× wird ausschließlich hierüber eingestellt, nicht per Stufen-Button.
- Nach Loslassen kehrt die Anzeige wieder zu den vier Stufen zurück.

### 3.3 Pinch-Zoom
- Zwei-Finger-Zoom muss weiter funktionieren und Stufen bzw. Rad mitführen.
  (Grundfunktion ist in der App vorhanden; die Stufen-Markierung folgt ihr bereits.)

## 4. Foto-Einstellungen (Bottom-Sheet)  — NOCH NICHT UMGESETZT

Erreichbar über den **Einstellungen-Button unten links**. Schiebt sich als Blatt von
unten über den Sucher; die Kamera-Oberfläche bleibt abgedunkelt dahinter sichtbar.

**Kopf:** Ziehgriff, Titel "Fotoeinstellungen", rechts ein "…"-Button im Kreis.
**Umschalter:** zwei Reiter "Allgemein" (aktiv: weiße Pille) / "Pro" (dunkel).

**Zeilenschema:** links Bezeichnung (klein, grau) mit darunter dem aktuellen Wert (groß,
weiß); rechts eine Reihe runder Icon-Buttons, ausgewählter = voll weiß gefüllt.

**Zeilen im Reiter "Allgemein" – exakt diese, in dieser Reihenfolge:**

| Zeile | Werte / Icons |
|---|---|
| **Mehr Licht** | drei Optionen: Keines (⊘) · Nachtsicht-Automatik (Mond mit „A") · Blitz (Blitzsymbol) |
| **Timer** | vier Optionen: Aus · 3 s · 5 s · 10 s |
| **Makrofokus** | drei Optionen: Aus · Automatisch · An |
| **Verhältnis** | **nur zwei Optionen:** Breitbild (16:9) · Vollbild (4:3) |

**Ausdrücklich NICHT übernehmen:**
- **"Top-Foto"** – beruht auf Google-KI, gibt es in dieser App nicht.
- Weitere Verhältnis-Optionen außer 16:9 und 4:3.

### Reiter "Pro" — Entscheidung Falk 2026-09-15: **alles übernehmen**

Wie in der Referenz: Kamera-Infozeile oben (Objektiv, Blende, Fokusmodus, Belichtungszeit,
ISO), darunter ein Regler mit Teilstrichen, darunter eine waagerecht scrollbare Reihe aus
Icon+Text-Reitern, die bestimmen, was der Regler einstellt:
`Alle zurücksetzen · Helligkeit · Schatten · Weißabgleich · Fokus · Belichtung`.

## 4a. Video-Einstellungen-Blatt — Entscheidung Falk 2026-09-15: **ja**

Gleiches Grundschema, Titel "Videoeinstellungen", Reiter "Allgemein" / "Mehr".
- **Allgemein:** Blitz · Video-Optimierung · Auflösung (FHD/4K/8K) · fps (24/30/60) ·
  10-Bit-HDR-Video · Makrofokus
- **Mehr:** Videostabilisierung (Standard/Aktiv/Extra) · Mikrofon (Smartphone/Bluetooth) ·
  Verbesserte Sprachqualität

## 4b. Abweichungen, weil die App die Funktion nicht hat

Beim Abgleich mit dem Code aufgefallen. **Alle Punkte von Falk am 2026-09-15 so
entschieden** – nicht erneut zur Diskussion stellen, sondern so bauen:

| Referenz-Zeile | Stand in der App | Entscheidung |
|---|---|---|
| **Makrofokus** (Aus/Automatisch/An) | Nur der manuelle Fokusmodus `focus_mode_macro`. Pixels „Automatisch" (erkennt Nahaufnahme, wechselt selbst auf die Weitwinkel-Linse) existiert nicht. | **Nur Aus/An** anbieten. Automatik wäre ein eigenes Feature, kommt nicht hier hinein. |
| **Verhältnis** (16:9 / 4:3) | Es gibt **keine** Seitenverhältnis-Einstellung. Die App wählt eine konkrete *Auflösung*, das Verhältnis fällt dabei ab. (`preference_crop_guide` ist nur ein Hilfslinien-Overlay.) | Umschalter schreibt die **größte verfügbare Auflösung mit dem gewünschten Verhältnis** in die Auflösungs-Einstellung. |
| **Schatten** (Pro-Reiter) | Kein Gegenstück. | Reiter **weglassen**, der Rest des Pro-Bereichs wie Referenz. |
| **Video-Optimierung**, **Verbesserte Sprachqualität** | Kein Gegenstück. | **Weglassen.** |
| **Mehr Licht** (Keines / Nachtsicht / Blitz) | Blitz aus/auto/an existiert; „Nachtsicht" ist ein Herstellermodus (X_Night), nicht garantiert verfügbar. | Zeile bildet **Blitz aus / auto / an** ab. |

**Weiterhin offen:** Was sollen der **"…"-Button** oben rechts im Blatt und der
**Regler-Button unten rechts** in der Fußzeile öffnen?

## 5. Moduswahl-Leiste — Entscheidung Falk 2026-09-15

**Nur echte Modi anzeigen.** Also `Foto · Video · Panorama` – keine Platzhalter für
Porträt/Nachtsicht/zufügen, solange die App diese Modi nicht wirklich beherrscht.
(So bereits umgesetzt.)

## 6. Was bereits umgesetzt ist

- Material-3-Fundament, fest dunkles Theme (Phase 1)
- `activity_main.xml` von RelativeLayout auf ConstraintLayout migriert (Phase 1)
- Auslöser und Video-Umschalter monochrom (weiß/dunkel) statt blau
- Zoom-Stufen-Leiste: Stufen ,5/1×/2/5 (kein 10×), Beschriftung wie Referenz (führende
  Null entfällt, nur die aktive Stufe trägt das "×"); im Querformat senkrecht mit der
  Reihenfolge der gedrehten Hochformat-Leiste (`5` oben bei Oberkante links)
- Moduswahl-Leiste (Foto/Video/Panorama) mit funktionierendem Umschalten, **an der
  Referenzposition**: äußerster Platz am Bildschirmrand, der Auslöser sitzt davor,
  die Zoom-Stufen davor über dem Bildrand. Die Knöpfe neben dem Auslöser
  (Video-Umschalter, Pause, Panorama-Abbruch) sind auf den Auslöser ausgerichtet statt
  auf den Bildschirmrand, damit sie mit ihm auf einer Linie bleiben. Im Querformat
  Beschriftungen quer und in senkrechter Pille (Abschnitt 2).
  **Einschränkung (Audit 2026-09-17, siehe 7.2):** kein Scrollen/Einrasten; Farben
  weichen ab. Einträge: „Foto · Panorama" im Fotomodus (siehe unten, `86f3f3ec`), leer
  im Videomodus.
- **Pixel-Drehmodell aus Abschnitt 2** (gebaut 2026-09-17, Datei-Prüfung 2026-09-18
  abgeschlossen): Fenster fest auf Hochformat gesperrt, nur die Glyphen drehen sich an
  Ort und Stelle; am Gerät geprüft inkl. EXIF-Drehung von Fotos in allen vier Lagen,
  Video-Rotationshinweis, Panorama. Technik: `MainActivity` sperrt das Fenster
  (`lock_to_portrait`, Manifest `portrait`); `MainUI.layoutUI()` positioniert einmalig
  fest, ohne Orientierungs-Tabelle; `MainActivity.onOrientationChanged()` (aus dem
  `OrientationEventListener`, 60°-Hysterese, geviertelt) ruft
  `MainUI.rotateGlyphs()`, das jede Glyphen-View auf `ui_rotation` dreht (300 ms,
  `cubic-bezier(0.2, 0, 0, 1)`). `MainUI.orientBar()` und `RotatedTextView` gibt es nicht
  mehr – Modus-Beschriftungen sind wieder eine gewöhnliche `TextView`, die wie jede
  andere Glyphe gedreht wird. `DrawPreview`-Overlays bleiben ortsfest (nur Videozeit und
  Selbstauslöser-Countdown drehen mit); Popup-Menü und Belichtungs-Slider drehen nicht
  (Abschnitt 8).
- Alter Zoom-Schieberegler entfällt, wo die Stufen-Leiste greift
- **Auslöser-Zeile (Audit-Schritt 1, 2026-09-17)** – nach den im Referenz-Screenshot
  *gemessenen* Werten (Gerät: 960 × 2142 px bei Dichte 360 = **2,25 px/dp**; die Detail-Spec
  weicht an mehreren Stellen ab, Entscheidung Falk: Messung gilt, Abweichungen in
  Abschnitt 8):
  - **Auslöser:** Kern ⌀ 74 dp Reinweiß, Ring 6 dp breit (außen ⌀ 86 dp) aus
    **Schwarz mit 60 % Deckkraft** (`shutter_row_scrim` `#99000000`; an einem dunklen und
    einem hellen Hintergrund gelöst). Ring = Hintergrund des Buttons
    (`shutter_ring.xml`), Kern = zur Laufzeit gezeichnetes `GradientDrawable`
    (`ui/ShutterButton`), Touch-Fläche bleibt 100 dp. Video: Kern `#E53935`; Aufnahme
    läuft: Feder-Morph zu 32 × 32 dp / Radius 8 dp und zurück (Detail-Spec 6, nicht
    messbar). Tap: Feder auf 92 % und zurück (Stiffness 300, Dämpfung 0,58 – Lesart der
    Spec-Angabe „tension 300, friction 20"). Heavy Click per
    `HapticFeedbackConstants.CONFIRM` (= `EFFECT_HEAVY_CLICK` in Androids Zuordnung, ohne
    VIBRATE-Berechtigung) bei jedem Auslöser-Tipp (Foto, Video-Start/-Stopp, Panorama).
  - **Kamera-Wechsel:** 56-dp-Squircle mit **Kreisbogen-Ecken 12 dp** (gemessen; 22 dp
    sichtbar zu rund, Superellipse hätte gewölbte Kanten), Hintergrund dasselbe Schwarz
    60 %, Pfeil-Glyph 24 dp (Padding 10,5 dp, weil die PNG-Grafik Leerraum hat). Zentrum
    **124 dp** rechts vom Auslöser-Zentrum (`MainUI.flankShutter()`).
  - **Galerie:** 56-dp-Squircle (Radius 12 dp) links vom Auslöser, Zentrum 124 dp, Bild
    `centerCrop` mit 2 dp Rahmen im selben Schwarz 60 % (per `clipToOutline` auf die
    Squircle-Form beschnitten). Bei neuem Medium wird das Bild schlicht getauscht (siehe 8).
    Während Videoaufnahme ausgeblendet – ihren Platz nimmt der Foto-Schnappschuss-Knopf.
  - **Foto/Video-Umschalter (Element aus der untersten Zeile, vorgezogen):**
    `ui/PhotoVideoSwitch`, gemessen: Pille 92 × 40 dp `#333333`, weißer Knopf ⌀ **48 dp**
    (überragt die Pille um 4 dp), Glyphen ~20 dp, Zentren 26 dp vom jeweiligen Pillenende,
    Glyph auf dem Knopf `#1B1B1B`, sonst weiß; Knopf federt beim Umschalten. Zeichnet sich
    selbst entlang der gedrehten Achse (Querformat: Pille senkrecht, Glyphen aufrecht).
    Der alte `switch_video`-Button neben dem Auslöser ist unsichtbar (bleibt nur für Tests
    im Layout).
  - **Feste Plätze der drei Fußzeilen** (Zentren von unten, Referenz gemessen): Umschalter
    85 dp, Moduswahl 149 dp, Auslöser 241 dp (bei 24 dp Gesten-Inset). Jede Zeile hängt
    **einzeln** am Bildschirmrand, nichts ist aneinander gekettet – Entscheidung Falk:
    Bedienelemente dürfen einander nie verschieben. Moduswahl und Umschalter werden bei
    Video-/Panorama-Aufnahme nur `INVISIBLE` (Platz bleibt), beim Fotografieren gar nicht
    ausgeblendet.
  - **Moduswahl:** zeigt nur die Untermodi des aktuellen Hauptmodus – im Fotomodus
    „Panorama" (nochmal antippen = zurück zum normalen Foto), im Videomodus leer; kein
    „Foto"- und kein „Video"-Eintrag (Entscheidung Falk, siehe 8).

## 7. Was noch fehlt — Abweichungs-Audit vom 2026-09-17

Vollständiger Abgleich Code ↔ Screenshot-Spec (diese Datei) ↔ Detail-Spec
(`pixel-camera-detailspec.md`). Geprüft: `activity_main.xml`, alle Drawables,
`themes.xml`/`colors.xml`/`dimens.xml`, `ZoomStepper.java`, `MainUI.layoutUI()`,
`DrawPreview.java`, `MainActivity`-Gesten, Screenshots `unsere-app-hochformat.png` und
`unsere-app-querformat-nach-umbau-2026-09-17.png`. Zeilenangaben: Stand Commit `2045620a`.

**Ergebnis in einem Satz:** Fertig sind die Zoom-Stufen-Pille, eine statische
Moduswahl-Leiste und das Drehmodell – grob ein Viertel der Detail-Spec. Alles andere fehlt
oder weicht ab; im Hochformat dominiert optisch weiterhin die alte Open-Camera-Icon-Leiste.

**Empfohlene Reihenfolge** – zuerst Abschnitt 9 entscheiden, dann:

1. Auslöser-Zeile richtig bauen (kleinster Eingriff, größter sichtbarer Effekt) → 7.2
   Auslöser / Kamera-Wechsel und 7.1 Punkt 5 (Galerie), Punkt 10 (Haptik).
2. Kopfzeile + unterste Zeile; dafür alte Icon-Leiste und Drei-Punkte-Popup abschaffen
   → 7.1 Punkte 3 und 4.
3. Moduswahl als echtes Karussell → 7.2 Moduswahl.
4. Feinjustier-Rad → 7.1 Punkt 1.
5. Einstellungen-Blätter + Swipe-Down → 7.1 Punkt 2.
6. Sucher-Overlays → 7.1 Punkte 6–8.
7. Farben / Blur / Schrift → 7.3.

### 7.1 Fehlende Bausteine (noch nicht gebaut)

Soll-Werte aus der Detail-Spec, außer wo die Screenshot-Spec etwas anderes sagt.

1. **Feinjustier-Rad (Abschnitt 3.2).** Ist: kein Drag-Handler auf `zoom_stepper`, kein
   Morphing, keine Ticks, kein Badge; über 5× kommt man nur per Pinch. Soll: Auslöser =
   Gedrückthalten + waagerechtes Ziehen auf der Pille **oder** Pinch im Sucher; die Pille
   morpht zu einem Scroll-Container 280–320 dp × 48 dp mit Alpha-Maske links/rechts;
   Hauptmarken-Striche 1,5 dp, Zwischenstriche 0,75 dp, Zahlen 60 % Weiß mittig unter den
   Hauptmarken; statischer, weiß leuchtender Mittelbalken; Floating-Badge darüber (28 dp
   hoch, Radius 9999, Padding 12 dp waagerecht, Wert wie `2,5×`); `CLOCK_TICK` pro
   überstrichenem Strich; nach Loslassen zurück zu den vier Stufen; im Querformat
   senkrecht. Hauptmarken: Konflikt K7.
2. **Foto-/Video-Einstellungen-Blatt (Abschnitte 4, 4a, 4b)** samt **Swipe-Down-Geste**
   auf dem Sucher. Ist: `MainActivity.MyGestureDetector.onFling()` (L5020) macht nur
   Screen-Unlock. Die Detail-Spec nennt es „Quick Settings Pull-Down-Sheet" mit
   Nachtsicht/Timer/Verhältnis/Blitz – Inhalt bleibt wie Abschnitt 4, die
   Falk-Entscheidungen aus 4b gelten.
3. **Unterste Zeile / Dock (Abschnitt 1, Punkt 4 c).** Fehlt vollständig. Soll: 52 dp
   hoch, direkt über der Gestenleiste. Links: Kreis 40 dp, `rgba(255,255,255,0.10)`,
   weißes Schieberegler-/Zahnrad-Icon 22 dp → öffnet das Blatt. Mitte: Pille `#1C1B1F`
   96 × 40 dp, Fotoapparat-Icon links / Filmkamera-Icon rechts je 20 dp, weißer (oder
   Mint-) Schieber ⌀ 34 dp mit Federanimation, aktives Icon schwarz, inaktives hellgrau.
   Rechts: Kreis 40 dp wie links, Icon „drei senkrechte Reglerstifte" → Pro-Panel (was
   genau, ist in 4b offen). ~~Folge: `switch_video` verschwindet von der Auslöser-Zeile.~~
   **Teilweise erledigt (Audit-Schritt 1):** der Umschalter in der Mitte ist gebaut
   (`PhotoVideoSwitch`, gemessene Maße in Abschnitt 6 – Pille 92 × 40 dp, Knopf 48 dp,
   nicht 96 × 40 / 34 dp), `switch_video` ist weg. Offen: die beiden Kreis-Buttons links
   und rechts.
4. **Kopfzeile (Abschnitt 1, Punkt 1).** Fehlt. Stattdessen läuft die alte Icon-Leiste
   (`settings`, `popup`, `exposure`, `exposure_lock` …, je `onscreen_button_size` = 60 dp,
   `MainUI.layoutUI()` L537–650) samt `PopupView` (Drei-Punkte-Menü). Soll: 56–64 dp hoch,
   blickdicht Schwarz; links Chips (Screenshot: „12 MP" + Makro-Chip; Detail-Spec:
   RAW-/50-MP- bzw. FHD-/4K-Badge, Radius 6 dp, 1 dp Kontur 40 % Weiß, Monospace –
   Konflikt K6); rechts `?` 24 dp, Strich 2 dp, Weiß; Icons klein, links/rechts
   gruppiert. Beim Bau mitbedenken: das `DrawPreview`-Info-Overlay oben links
   (Batterie/Blitz, Abschnitt 8) und wohin die Funktionen der alten Icons (Belichtung,
   Lock, Popup-Optionen) wandern – in das Blatt aus Punkt 2.
5. ~~**Galerie-Miniatur auf der Auslöser-Zeile.**~~ **Erledigt (Audit-Schritt 1)**, mit
   gemessenen Werten statt der Detail-Spec: Radius 12 dp (nicht 22), Zentrum 124 dp (nicht
   64 – das wäre im Ring), 2-dp-Rahmen. Bounce/Cross-Fade bewusst weggelassen (Abschnitt 8).
   Der pulsierende Farbfilter beim Speichern (`savingImage()`) ist geblieben.
6. **Fokus-Ring, Belichtungs-Slider, AE/AF-Sperre (Detail-Spec 3).** Ist: gelbes
   Rechteck, Yellow 500 (`DrawPreview` L1233 f.). Soll: Ring ⌀ 64 dp, Kontur 1,5 dp
   (Farbe: Konflikt K9), erscheint am Berührungspunkt, Puls 1,15 → 1,0, verschwindet
   nach 4 s ohne Interaktion; senkrechte Slider mit Sonne (Belichtungskorrektur) und
   Thermometer (Farbtemperatur) seitlich am Ring; Long-Press auf den Sucher = AE/AF-Sperre
   mit Schloss-Symbol am Ring und Banner „AE/AF-Sperre" (Open Camera hat die Sperre nur
   als Icon-Button).
7. **Nivellierung (Detail-Spec 3).** Modus A ist: alte Winkellinie mit grünem Highlight
   `#14e715` (`DrawPreview` L683, Preference). Soll: zweigeteilte 1-px-Linie, statische
   Mittelmarke, kippende Achse, Grad-Anzeige (`0°`, `-2°`), bei 0° verschmelzen die Linien,
   werden Signalgelb und lösen `CONFIRM` aus. Modus B (Top-Down) fehlt ganz: statisches
   weißes Fadenkreuz 12 × 12 dp / 1,5 dp im optischen Zentrum, Toleranzkreis Radius 34 dp
   / 1 dp / 60 % Weiß, dynamisches gelbes Kreuz 12 × 12 dp / 1,5 dp wandert mit der
   Neigung, beim Treffen rasten beide ein + `CONFIRM`.
8. **Sucher-Clipping 28 dp und Translucent-Bar-Modus (16:9).** `preview` ist ein
   `FrameLayout` ohne Outline/Clip (`activity_main.xml` L13). Ob überhaupt gewollt:
   Konflikt K4.
9. **Smart Chips.** Fehlen. Soll: Pille 36 dp hoch, Padding 6 dp / 14 dp, `#1E1E1E`,
   1 dp Border 10 % Weiß; Icon 16 dp | Text 12 dp Weiß | ✕ 14 dp Hellgrau; Slide-Up
   12 px + Fade-In, weg nach 4 s; ca. 80 dp über dem Auslöser-Zentrum.
10. **Haptik-Profil.** Ist: SeekBar-Ticks (`MainUI.performHapticFeedback()`) und – seit
    Audit-Schritt 1 – **Heavy Click beim Auslösen / Video-Start/-Stopp**
    (`MainUI.performHeavyClick()`). Offen: `CLOCK_TICK` bei Zoom-Stufe, Ruler-Strich,
    Modus-Snap; `CONFIRM` beim Einrasten von Horizont bzw. Fadenkreuz.
11. ~~**Kamera-Wechsel-Animation.** Fehlt. Soll: 180° Drehung, 350 ms,
    `cubic-bezier(0.4, 0.0, 0.2, 1)`.~~ **Bewusst nicht gebaut** (war kurz drin, wieder
    entfernt – Entscheidung Falk 2026-09-17, keine Animationen außer am Auslöser selbst,
    Abschnitt 8).
12. **Doppel-Tap.** Ist: löst Foto aus (`Preview.onDoubleTap()` L884, per Preference).
    Detail-Spec: Kamerawechsel oder Sprung auf 2×. Konflikt K8.
13. **Hardware-Tasten.** Lautstärke = Auslösen gibt es als Preference; „Halten startet
    Video/Serienbild" nicht – klären, ob gewollt.

### 7.2 Abweichungen an vorhandenen Bausteinen (Ist → Soll)

**Auslöser** – ~~alle vier Punkte~~ **erledigt (Audit-Schritt 1)**, siehe Abschnitt 6.
Gemessen statt Detail-Spec: Kern 74 dp / Ring 6 dp / außen 86 dp (nicht 72 / 8 / 88);
Ring ist Schwarz 60 %, keine weiße Kontur (K10). Die alten Drawables
`take_photo_*.xml` / `take_video_*.xml` sind gelöscht, den Kern zeichnet `ui/ShutterButton`.

**Kamera-Wechsel** – **erledigt (Audit-Schritt 1)**, siehe Abschnitt 6. Gemessen statt
Detail-Spec: Hintergrund Schwarz 60 % (nicht `rgba(255,255,255,0.12)` – der Button ist in
der Referenz *dunkler* als das Motiv), Eckradius 12 dp, Zentrumsabstand 124 dp (64 dp
läge im Ring). `circle_background.xml` bleibt für Pause/Schnappschuss/Panorama-Abbruch.

**Zoom-Stufen-Pille** (`zoom_stepper`, `ZoomStepper.java`, `dimens.xml`,
`pill_background.xml`, `pill_selected_background.xml`)
- Stufe `zoom_step_size` 44 dp + `zoom_step_margin` 4 dp → Pille **52 dp** hoch, aktiver
  Kreis **44 dp**. Soll: Pille 40 dp hoch, 180–220 dp breit, aktiver Kreis ⌀ 36 dp.
- Abstand zum Auslöser 12 dp zum 100-dp-View (= 19 dp zum Ring). Detail-Spec sagt ca.
  32 dp, **im Screenshot gemessen: 24 dp** zwischen Pillen-Unterkante und Ring-Oberkante
  (Audit-Schritt 1) – Screenshot gilt.
- Hintergrund `zoom_stepper_background` `#99000000`, flach. Soll: `rgba(20,20,20,0.65)`
  mit Blur 20 px (Blur: siehe 7.3).
- Aktive Füllung `#F2F2F2`, Text `#1B1B1B`. Soll: `#D3E8D8` (Salbei, Material You) bzw.
  `#E2E3E0`, Text `#000000`.
- Passt: inaktiv Weiß, 13 sp (Soll 13–15), aktiv `BOLD` = 700 (Soll 600–700), Stufen
  `,5 1× 2 5`, `×` nur an der aktiven, Pinch führt die Markierung mit.
- Keine Haptik beim Antippen (Soll `CLOCK_TICK`).
- Doku-Drift: der Klassenkommentar sagt „steps derived from the zoom range", Abschnitt 3.1
  sagt „nichts algorithmisch ableiten" – Kommentar angleichen oder Stufen fest verdrahten.
- Die alte `zoom_seekbar` bleibt im Layout und ist nur versteckt, solange Stufen
  existieren (`MainUI` L1526–1529, `MainActivity` L5195–5212) – nach dem Rad entfernen.

**Moduswahl-Leiste** (`mode_bar`, `MainUI.setupModeBar()` L1227, `updateModeBar()` L1328)
- Ist ein `LinearLayout`: kein Scrollen, kein Snap-to-Center, keine Alpha-Verläufe an
  den Rändern. Soll: waagerechtes Paging mit Magnet-Effekt, Tipp auf ein Nachbarwort
  scrollt es in die Mitte, `CLOCK_TICK` beim Einrasten.
- **Einträge (Stand Audit-Schritt 1):** nur „Panorama" im Fotomodus (Umschalter: nochmal
  tippen = normales Foto), im Videomodus leer. Foto ↔ Video läuft über die
  `PhotoVideoSwitch`-Pille darunter. Entscheidung Falk 2026-09-17 (Abschnitt 8): kein
  „Foto"-Eintrag (die Pille sagt schon „Foto"), kein „Video"-Eintrag (der
  Referenz-Screenshot zeigt im Karussell „zufügen · Porträt · Foto · Nachtsicht · Pan…",
  kein „Video" – die Anmerkung unter Abschnitt 9, die Detail-Spec und Referenz hätten beide
  „Video" im Karussell, stimmt für den Screenshot nicht). Wenn später Video-Untermodi
  kommen, stehen sie in derselben Leiste an derselben Stelle.
- Inaktiv `#F2F2F2` (`md_dark_on_surface`), normal. Soll: `#8E918F`, 14 dp, Gewicht 500.
- Aktiv: Volltonfüllung `#F2F2F2` + dunkler Text `#1B1B1B` + `BOLD` 700. Beide Specs sagen
  **nicht volltonweiß** – Detail-Spec `rgba(255,255,255,0.15)` + weißer Text 600,
  Screenshot-Spec helles transparentes Grau + dunkle Schrift (Konflikt K2).
- Passt: Padding 8 dp senkrecht / 16 dp waagerecht, 14 sp, Pillen-Radius 9999.
- ~~Klebt im Querformat mit 0 dp am Rand, weil die unterste Zeile fehlt.~~ Erledigt: sitzt
  jetzt auf der Referenzhöhe (Zentrum 149 dp vom Rand), die Pille darunter (7.1 Punkt 3).
- Pillenhöhe: bei uns ~39 dp (8 dp Padding), Referenz gemessen **32 dp** – Zentrum liegt
  dadurch 4 dp zu hoch; erledigt sich mit der Pillen-Anpassung in Audit-Schritt 3.

**Drittel-Raster** (`DrawPreview` L972–977)
- `Color.WHITE` mit voller Deckkraft; Standard der Preference ist `preference_grid_none`
  (L710), das Raster ist also normalerweise aus. Soll: 1 dp, `rgba(255,255,255,0.25)`,
  immer sichtbar (Screenshot-Spec 1b).

**Rotation** (`MainUI.rotateGlyphs()`) — **erledigt** (Pixel-Drehmodell, 2026-09-17/18):
300 ms mit `PathInterpolator(0.2, 0, 0, 1)`, wie gefordert. Das Drehmodell selbst ist
das aus Abschnitt 2 (Konflikt K3, dort endgültig entschieden).

### 7.3 Farb-, Blur- und Schrift-Abweichungen (global)

| Element | Ist | Soll (Detail-Spec) |
|---|---|---|
| Kopf-/Fußfläche | `windowBackground` = `#1B1B1B` (`md_dark_surface`; Kommentar in `colors.xml`: „not pure black") | `#000000` |
| Overlay-Container (Zoom-Pille, Ruler, Chips) | `#99000000`, kein Blur | `rgba(20,20,20,0.65)` + Blur 20 px |
| Aktiver Akzent | `#F2F2F2`; `md_dark_primary` `#B0A695` definiert, aber ungenutzt; kein `DynamicColors` | Material-You-Pastell `#C2E7D9`–`#D3E8D8` bzw. `#E2E3E0`, dynamisch |
| Inaktive Icons / Sekundärtext | `#F2F2F2` bzw. Weiß | `#C4C7C5` bzw. `rgba(255,255,255,0.7)`; Modus-Karussell inaktiv `#8E918F` |
| Nivellierung dynamisch | Grün `#14e715` (Preference) | Signalgelb `#FEE75C` / `#F9AB00` |
| Fokus | Yellow 500 `#FFEB3B`, Rechteck | Weiß 1,5 dp Ring (Detail-Spec) / dünn goldgelb (Screenshot) – K9 |
| Video-Rot | ~~`#F44336`~~ `#E53935` (erledigt, Audit-Schritt 1) | `#E53935` |
| Schriftfamilie | keine deklariert (kein `fontFamily`, kein `res/font/`) | Google Sans / Roboto Flex. Google Sans ist nicht frei lizenziert → Roboto Flex (OFL) als Font-Ressource bündeln oder Systemschrift bewusst akzeptieren – Entscheidung Falk |
| Schriftgewicht 600 | `Typeface.BOLD` = 700 | 600 über `Typeface.create(tf, 600, false)` (API 28+, hier verfügbar) |
| Dezimal-Komma | `Locale.getDefault()` in `ZoomStepper.formatRatio()` – stimmt nur, weil das Gerät auf `de` steht | ausdrücklich deutsch formatieren, oder bewusst so lassen und im Code vermerken |

**Zu Blur – geklärt durch Recherche-Agent (2026-09-17), Details siehe unten:**
`View.setRenderEffect(RenderEffect.createBlurEffect(…))` (Android 12+/API 31+) zeichnet
nur den Inhalt der View selbst weich, nicht das, was dahinter liegt. „Milchglas" über
der Kamera-Vorschau (App nutzt `MyTextureView` via Camera2, siehe `Preview.java`
Z. 470–486) braucht einen Snapshot-Umweg (RenderNode + RenderEffect auf API 31+) und
funktioniert auf API 23–30 praktisch nicht (TextureView/SurfaceView dort nicht
blurrbar). **Empfehlung: erstmal ohne Blur bauen** – 65 % Deckkraft ohne
Weichzeichnung (eine `alpha`/`ColorDrawable`-Zeile, API-unabhängig, kein
Performance-Risiko). Optischer Unterschied bei kleinen Pillen-/Chip-Flächen gering;
echter Blur bliebe eine spätere API-31+-Verbesserung mit Opacity-Fallback für
API 23–30, keine Grundvoraussetzung. Wie die echte Pixel-Kamera das löst, ließ sich
nicht verifizieren (closed source).

## 8. Bewusste Abweichungen von der Referenz

- ~~**Moduswahl im Querformat:** Beschriftungen stehen aufrecht statt um 90° gedreht.~~
  ~~**Erledigt 2026-09-17** – nicht über den Rand-Trick (`View.setRotation()` +
  `MainUI.setFixedRotation`), sondern über eine `RotatedTextView`, die beim Messen
  Breite und Höhe vertauscht; damit stimmt der Platzbedarf und die Pille wird von selbst
  senkrecht.~~ **Überholt durch das Pixel-Drehmodell (2026-09-17/18):** Es wird keine
  Leiste mehr umgeklappt und keine Reihenfolge mehr umgekehrt – das ganze Layout steht
  fest, nur die Glyphen drehen sich (`MainUI.rotateGlyphs()`, Abschnitt 6). Die
  `RotatedTextView` ist entfallen, die Modus-Beschriftung ist wieder eine normale
  `TextView`, die wie jede andere Glyphe per `setRotation()` gedreht wird.

### Audit-Schritt 1 (Auslöser-Zeile, 2026-09-17) – Entscheidungen Falk

- **Keine Animationen außer am Auslöser selbst.** Falk will es „so simpel wie möglich":
  weggelassen sind die Open-Camera-Flug-Animation des Fotos in die Galerie (Standard der
  Einstellung `preference_thumbnail_animation` jetzt aus, Option bleibt), der
  Cross-Fade/Scale-Bounce der Galerie-Miniatur (Detail-Spec 6; das Bild wird schlicht
  getauscht) und die 180°-Drehung des Kamera-Wechsel-Icons (Detail-Spec 6). Geblieben:
  Feder beim Tippen und Kreis-↔-Quadrat-Morph des Auslösers, Ton, Vibration, federnder
  Knopf im Umschalter.
- ~~**Kein „Foto"-Eintrag in der Moduswahl** (die Referenz hat einen): die Pille darunter
  sagt schon „Foto"; „Panorama" ist deshalb ein Umschalter.~~ **Zurückgenommen
  (`86f3f3ec`, 2026-09-17, Falk):** Ohne einen eigenen „Foto"-Eintrag gab es keinen
  klaren Ausweg aus Panorama – oder aus künftigen weiteren Modi. Die Moduswahl zeigt
  jetzt „Foto · Panorama" nebeneinander, der aktive in der hellen Pille. Im Videomodus
  bleibt die Leiste leer (die Referenz zeigt dort ihre Video-Untermodi, die wir nicht
  haben).
- **Feste Plätze statt Verkettung:** die drei Fußzeilen (Auslöser, Moduswahl, Umschalter)
  hängen jede für sich am Bildschirmrand, damit kein Ein-/Ausblenden je etwas verschiebt.
  Sichtbar gleiche Positionen wie in der Referenz, nur technisch anders verankert.
- **Kamera-Wechsel ohne Aufbläh-Effekt beim Tippen** (der alte `stateListAnimator` mit
  Scale 1,1 ist entfernt; die Referenz zeigt keinen) – gilt nur für diesen Button, die
  übrigen alten Buttons haben ihn noch.
- **Auslöser-Tipp färbt den Kern nicht mehr grau** (`shutter_icon_pressed` entfernt; die
  Spec nennt nur die Feder).
- **Panorama-Abschluss-Haken** (Check-Icon) sitzt jetzt innerhalb des transluzenten
  Rings statt frei; Panorama ist in der Referenz nicht abgebildet.
- **Gemessene Werte schlagen die Detail-Spec** (Entscheidung Falk, K10-Präzedenz):
  Auslöser 74/6/86 dp statt 72/8/88; Squircle-Radius 12 dp statt 22; Nachbar-Abstand
  124 dp statt 64; Nachbar-Hintergrund Schwarz 60 % statt Weiß 12 %; Umschalter-Pille
  92 × 40 / Knopf 48 dp statt 96 × 40 / 34 dp. Die Detail-Spec bleibt wörtlich, die
  Zahlen in Abschnitt 6 sind maßgeblich.
- **Sichtbarkeit auf Schwarz:** Ring, Squircles und Galerie-Rahmen sind transluzentes
  Schwarz und darum auf der schwarzen Fläche (16:9, Videomodus) unsichtbar – so ergibt es
  sich aus der Messung über dem Sucherbild. Ob die Pixel-App dort einen minimal helleren
  Ton nutzt, ist aus den Screenshots nicht ableitbar; **Falk prüft am Gerät**, dann ggf.
  `shutter_row_scrim` anpassen.

### Pixel-Drehmodell (2026-09-17/18) – Entscheidungen Falk

- **Popup-Menü und Belichtungs-Slider drehen nicht mit.** Beides sind alte
  Open-Camera-Elemente, die das Redesign ohnehin ersetzt (siehe Abschnitt 7) – der
  Aufwand, sie an die Glyphen-Drehung anzupassen, lohnt sich für Bauteile nicht, die
  bald verschwinden.
- **Keine Info-Toasts mehr** (`9327410c`): weder Moduswechsel noch Kamera-/Linsenwechsel
  noch der App-Start zeigen eine Text-Einblendung – die Mode-Leiste und der
  Foto/Video-Umschalter zeigen den Zustand bereits an. Fehler-Toasts und die
  einmaligen HDR-/Panorama-Infodialoge bleiben, da sie keine Zustands-Ankündigung sind.

### Beim Orientierungs-Umbau (2026-09-17) festgestellt, nicht orientierungsbedingt

Diese Punkte sind Folgen noch nicht gebauter Teile aus Abschnitt 7, keine
Entscheidungen. Sie lösen sich auf, wenn die jeweiligen Elemente kommen:

- ~~**Moduswahl klebt im Querformat am Bildschirmrand**~~ Erledigt in Audit-Schritt 1
  (feste Referenzhöhe, Umschalter außen daneben).
- ~~**Auslöser-Spalte:** bei uns Kamera-Wechsel / Auslöser / **Video-Umschalter**~~
  Erledigt in Audit-Schritt 1: Kamera-Wechsel / Auslöser / Galerie, Umschalter darunter.
- **Info-Overlay der Vorschau** (Batterie-/Blitz-Anzeige, von `DrawPreview` auf die
  Leinwand gezeichnet) sitzt in jeder Lage oben links, dreht also nicht mit. Das
  Gegenstück in der Referenz sind die Kopfzeilen-Chips (7.1 Punkt 4), die mitdrehen. Beim
  Bau der Chips das Overlay mitbedenken oder ersetzen.

## 9. Offene Spec-Konflikte (Stand 2026-09-17) — von Falk zu entscheiden

Die Detail-Spec (`pixel-camera-detailspec.md`) widerspricht an diesen Stellen der aus den
Screenshots abgeleiteten Vorgabe oben. Bis zur Entscheidung gilt die Screenshot-Spec
(Grundregel „eins zu eins wie in der Referenz"). Feature-Chats bauen nichts, das von
einem offenen Punkt abhängt, ohne vorher nachzufragen.

| # | Punkt | Detail-Spec | Screenshot-Spec (oben) | Entscheidung (Falk, 2026-09-17) |
|---|---|---|---|---|
| K1 | Kamera-Wechsel-Form | Kreis, 56 dp | Squircle (1b) | **Squircle** – Direktvergleich sticht Zahlen |
| K2 | Aktive Modus-Pille | `rgba(255,255,255,0.15)`, **weißer** Text, Gewicht 600 | helles, leicht transparentes Grau, **dunkle** Schrift (1b) | **Screenshot-Spec** (helles Grau + dunkler Text) |
| K3 | Drehmodell | Sucher fix, Bedienelemente drehen isoliert um die eigene Achse, Karussell ordnet sich an der Kante neu | starrer Block, Fenster dreht mit (2) – **am Gerät verifiziert** | **Endgültig entschieden und umgesetzt (2026-09-17/18):** Detail-Spec gewinnt. Fenster fest auf Hochformat gesperrt, nur die Glyphen drehen sich an Ort und Stelle (`MainUI.rotateGlyphs()`) – kein Karussell-Umordnen, das entfällt hier (Moduswahl ist ohnehin statisch, siehe 7.2). Datei-Prüfung (EXIF/Video/Panorama in allen vier Lagen) am 2026-09-18 bestanden, siehe SPEC.md 4.5 und 6. |
| K4 | Sucher-Layout 4:3 | zentriert zwischen Kopf- und Fußfläche, freie Ränder, 28 dp Eckradius; 16:9 schiebt sich transluzent hinter die Leisten | Bedienelemente hängen am Bildschirmrand und liegen bei 4:3 **über** dem Bild (1a) | **Screenshot-Spec** (Grundregel eins-zu-eins) |
| K5 | Modus-Einträge | Nachtsicht · Porträt · Foto · Video · Kinoeffekt/Pan | nur echte Modi Foto · Video · Panorama (5, Falk 2026-09-15) | bestätigt, keine Änderung |
| K6 | Kopfzeilen-Badges | RAW / 50 MP (Foto), FHD / 4K (Video) | „12 MP" + Makro-Chip (1) | **Kein echter Widerspruch** – Badge zeigt die jeweils aktuell aktive Auflösungs-Einstellung dynamisch an; „12 MP" war nur der Standardzustand im Screenshot |
| K7 | Ruler-Hauptmarken | `,5 · 1 · 2 · 5 · 10 · 30` | `1 · 2 · 5 · 10 · 30` (3.2) | **Detail-Spec** (mit „,5", Konsistenz zu den Stufen-Buttons) |
| K8 | Doppel-Tap | Kamerawechsel oder Sprung auf 2× | nicht spezifiziert; App heute: Foto auslösen | **offen – bleibt vorerst wie heute** (Foto auslösen), da Verhaltensänderung, keine reine Optik-Frage. Feature-Chats bauen nichts, das davon abhängt, ohne erneut nachzufragen |
| K9 | Fokus-Ring-Farbe | Weiß, 1,5 dp | gelb/golden, dünn (1, 1b) | **Screenshot-Spec** (Gold/Gelb, direkt aus Falks Fotos ersichtlich) |
| K10 | Auslöser-Außenring | weiße 2-dp-Kontur, 4 dp Lücke, 88 dp | breiter, halbtransparenter grauer Außenring (1b) | **Screenshot-Spec** (transluzent-grau) |
| K11 | Zoom-Zeichen | Buchstabe `x` (`1x`, `,5x`) | `×` (3.1, „offen") | **Screenshot-Spec** (`×`, bereits so umgesetzt) |

**Schriftfamilie (Zeile in 7.3, kein K-Punkt):** Google Sans ist nicht frei lizenzierbar
→ **Roboto Flex (OFL)** als gebündelte Font-Ressource, keine weitere Rückfrage nötig.

~~Kein Konflikt, nur zur Klarheit: Die Detail-Spec hat **sowohl** „Video" im Karussell
**als auch** den Foto/Video-Umschalter im Dock – so ist es auch in der Referenz, beides
bauen.~~ **Korrigiert in Audit-Schritt 1:** der Referenz-Screenshot zeigt im Karussell
kein „Video" (Abschnitt 1: „zufügen · Porträt · Foto · Nachtsicht · Pan…"), nur den
Umschalter im Dock. Gebaut ist der Umschalter; das Karussell hat keinen Video-Eintrag
(Abschnitt 8).
