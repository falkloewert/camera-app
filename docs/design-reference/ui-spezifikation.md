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

**Drehmodell (verbindlich, seit 2026-09-17 umgesetzt):** Es gibt **eine** feste
Anordnung – die aus Abschnitt 1. Beim Drehen des Geräts dreht sie als **starrer Block**
mit, so als wäre das Hochformat-Layout auf eine Karte gedruckt und die Karte würde
gedreht. Jeder Knopf behält dadurch seine körperliche Position am Gerät. Nichts wird pro
Ausrichtung neu angeordnet. Konkret:

- Oberkante links (das normale Querformat): Hochformat-Karte um **90° gegen den
  Uhrzeigersinn** gedreht – oben → links, links → unten, unten → rechts, rechts → oben.
- Oberkante rechts (umgekehrtes Querformat): um **90° im Uhrzeigersinn** – oben → rechts,
  links → oben.
- Hochformat auf dem Kopf bietet Android in diesem Fenstermodus nicht an; die Oberfläche
  bleibt dann in der zuletzt zulässigen Lage stehen (gespeicherte Fotos sind trotzdem
  richtig herum, die Drehung kommt vom Lagesensor).
- **Symbole und Zoom-Ziffern** werden dabei zurückgedreht und stehen aufrecht;
  **Modus-Beschriftungen nicht** – sie stehen quer, wie auf der Karte gedruckt.
- Das Fenster selbst dreht mit (wie bei der Pixel-Kamera, am Gerät verifiziert:
  bei erzwungener Display-Drehung liefert `screencap` 2142×960). Deshalb sitzt der
  Gesten-Balken immer an der physischen Unterkante.

Ergebnis für die Elemente (Oberkante links), aus den Referenzbildern abgelesen:

- Zoom-Stufen-Leiste **senkrecht**, Reihenfolge von oben nach unten: `5 · 2 · 1× · ,5`
  (größter Wert oben), Ziffern aufrecht.
- Moduswahl-Leiste als **senkrechte Spalte am rechten Rand**, Beschriftungen um 90°
  **gedreht** (von unten nach oben lesbar), Reihenfolge von unten nach oben wie im
  Hochformat von links nach rechts; aktiver Modus in heller **senkrechter** Pille.
- Haupt-Bedienzeile senkrecht gestapelt am rechten Rand: Kamera-Wechsel oben, Auslöser
  mittig, Galerie-Miniatur unten.
- Einstellungen-/Regler-Buttons am äußersten rechten Rand, senkrecht verteilt (Regler
  oben, Foto/Video-Umschalter mittig, Einstellungen unten).
- Chips ("12 MP", Makro) und "?" wandern mit: Kopfzeile wird zur linken Spalte
  ("12 MP" unten links, "?" oben links).

Bei Oberkante rechts alles spiegelbildlich dazu (Zoom `,5` oben, Beschriftungen von
oben nach unten lesbar, Auslöser-Spalte links).

Belege nach dem Umbau (unsere App, Pixel 9 Pro, 2026-09-17):
`unsere-app-querformat-nach-umbau-2026-09-17.png` (Oberkante links) und
`unsere-app-querformat-umgekehrt-nach-umbau-2026-09-17.png` (Oberkante rechts). Die
älteren `unsere-app-*.png` ohne Datum zeigen den Zustand **vor** dem Umbau (Beweise aus
dem Kickoff).

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
  **Einschränkung (Audit 2026-09-17, siehe 7.2):** "Video" ist dort nur Anzeige, kein
  Umschalt-Eintrag; kein Scrollen/Einrasten; Farben weichen ab.
- **Drehmodell aus Abschnitt 2** (2026-09-17): starr mitdrehende Anordnung in allen
  drei von Android angebotenen Lagen; am Gerät geprüft inkl. EXIF-Drehung von Fotos in
  allen vier Lagen, Video-Rotationshinweis, Panorama-Linsenpaar. Technik: die
  Positions-Zuordnung in `MainUI.layoutUI()` (`Side`-Tabelle) war bereits die Drehung;
  die Inhalte der drei Leisten werden von `MainUI.orientBar()` umgeklappt und in der
  Reihenfolge gedreht, die Modus-Texte über `RotatedTextView` quer gezeichnet, Abstände
  über `android:divider`. Bewusst **kein** `View.setRotation()` auf ganze Leisten (siehe
  Abschnitt 8, alter Punkt).
- Alter Zoom-Schieberegler entfällt, wo die Stufen-Leiste greift

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
   genau, ist in 4b offen). Folge: `switch_video` verschwindet von der Auslöser-Zeile.
4. **Kopfzeile (Abschnitt 1, Punkt 1).** Fehlt. Stattdessen läuft die alte Icon-Leiste
   (`settings`, `popup`, `exposure`, `exposure_lock` …, je `onscreen_button_size` = 60 dp,
   `MainUI.layoutUI()` L537–650) samt `PopupView` (Drei-Punkte-Menü). Soll: 56–64 dp hoch,
   blickdicht Schwarz; links Chips (Screenshot: „12 MP" + Makro-Chip; Detail-Spec:
   RAW-/50-MP- bzw. FHD-/4K-Badge, Radius 6 dp, 1 dp Kontur 40 % Weiß, Monospace –
   Konflikt K6); rechts `?` 24 dp, Strich 2 dp, Weiß; Icons klein, links/rechts
   gruppiert. Beim Bau mitbedenken: das `DrawPreview`-Info-Overlay oben links
   (Batterie/Blitz, Abschnitt 8) und wohin die Funktionen der alten Icons (Belichtung,
   Lock, Popup-Optionen) wandern – in das Blatt aus Punkt 2.
5. **Galerie-Miniatur auf der Auslöser-Zeile.** Ist: 44 dp Rechteck, bei `ui_top` Teil
   der alten Icon-Leiste (`MainUI.layoutUI()` L539–546), im Hochformat-Screenshot rechts
   unten; Speichern-Animation ist nur ein pulsierender Farbfilter
   (`MainActivity.savingImage()` L4388). Soll: 56 × 56 dp Squircle, Radius 22 dp,
   beschnitten, Inhalt `centerCrop`, Zentrum 64 dp links vom Auslöser-Zentrum; bei neuem
   Medium Scale-Bounce 1,1 → 1,0 plus Cross-Fade.
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
10. **Haptik-Profil.** Ist: die einzigen Aufrufe sind die alten SeekBar-Ticks
    (`MainUI.performHapticFeedback()` L3359). Soll: Heavy Click beim Auslösen und bei
    Video-Start/-Stopp; `CLOCK_TICK` bei Zoom-Stufe, Ruler-Strich, Modus-Snap; `CONFIRM`
    beim Einrasten von Horizont bzw. Fadenkreuz.
11. **Kamera-Wechsel-Animation.** Fehlt. Soll: 180° Drehung, 350 ms,
    `cubic-bezier(0.4, 0.0, 0.2, 1)`.
12. **Doppel-Tap.** Ist: löst Foto aus (`Preview.onDoubleTap()` L884, per Preference).
    Detail-Spec: Kamerawechsel oder Sprung auf 2×. Konflikt K8.
13. **Hardware-Tasten.** Lautstärke = Auslösen gibt es als Preference; „Halten startet
    Video/Serienbild" nicht – klären, ob gewollt.

### 7.2 Abweichungen an vorhandenen Bausteinen (Ist → Soll)

**Auslöser** (`take_photo`, `take_photo_shutter.xml`, `take_video_*.xml`,
`animator/button_animation.xml`)
- Geometrie ist invertiert: View 100 dp mit 10 dp Padding → 80 dp **weißer Vollkreis mit
  5 dp dunklem Rand `#1B1B1B`**. Soll: Außenring ⌀ 88 dp mit weißer 2-dp-Kontur, 4 dp
  transparente Lücke, Kern ⌀ 72 dp Reinweiß. (Screenshot-Spec 1b: „breiter,
  halbtransparenter grauer Außenring" – Konflikt K10; einig sind sich beide, dass der
  harte dunkle Rand weg muss.)
- Tap-Feedback: Scale **1,1** (wächst), 100 ms linear, hin und zurück. Soll: Scale 0,92
  mit Feder-Rückstellung (tension 300, friction 20).
- Video-Kern: `#F44336` → `#E53935`. `take_video_shutter.xml` legt einen 16-dp-Stroke
  auf ein 48-dp-Shape, vom Rot bleibt ein Punkt → gleiche Ringgeometrie wie Foto, Kern rot.
- Aufnahme läuft: harter Drawable-Tausch auf rotes Quadrat mit Radius 3 dp
  (`take_video_recording.xml`). Soll: Feder-Morph zu 32 × 32 dp, Radius 8 dp.

**Kamera-Wechsel** (`switch_camera`, `circle_background.xml`)
- 60 dp Kreis, Hintergrund `icons_background` = `#20000000` (12,5 % **Schwarz**),
  Padding 10 dp. Soll: 56 dp, `rgba(255,255,255,0.12)`, Icon 24 dp Weiß.
- Klebt mit 0 dp am Auslöser (`MainUI.layoutUI()` L696–701) → Zentrumsabstand 80 dp.
  Soll: 64 dp.
- Form Kreis vs. Squircle: Konflikt K1.
- Links vom Auslöser sitzt `switch_video`, nicht die Galerie (7.1 Punkte 3 und 5).

**Zoom-Stufen-Pille** (`zoom_stepper`, `ZoomStepper.java`, `dimens.xml`,
`pill_background.xml`, `pill_selected_background.xml`)
- Stufe `zoom_step_size` 44 dp + `zoom_step_margin` 4 dp → Pille **52 dp** hoch, aktiver
  Kreis **44 dp**. Soll: Pille 40 dp hoch, 180–220 dp breit, aktiver Kreis ⌀ 36 dp.
- Abstand zum Auslöser 12 dp (`MainUI.layoutUI()` L782). Soll: ca. 32 dp.
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
- **„Video" ist kein Eintrag, sondern eine Anzeige:** im Fotomodus sind nur Foto/Panorama
  sichtbar, im Videomodus nur „Video" allein (L1337–1340); Tipp auf Video tut nichts
  (Kommentar L1243), Foto ↔ Video läuft über `switch_video`. Soll: Foto · Video · Panorama
  immer sichtbar, Tipp auf Video schaltet um.
- Inaktiv `#F2F2F2` (`md_dark_on_surface`), normal. Soll: `#8E918F`, 14 dp, Gewicht 500.
- Aktiv: Volltonfüllung `#F2F2F2` + dunkler Text `#1B1B1B` + `BOLD` 700. Beide Specs sagen
  **nicht volltonweiß** – Detail-Spec `rgba(255,255,255,0.15)` + weißer Text 600,
  Screenshot-Spec helles transparentes Grau + dunkle Schrift (Konflikt K2).
- Passt: Padding 8 dp senkrecht / 16 dp waagerecht, 14 sp, Pillen-Radius 9999.
- Klebt im Querformat mit 0 dp am Rand, weil die unterste Zeile fehlt (7.1 Punkt 3).

**Drittel-Raster** (`DrawPreview` L972–977)
- `Color.WHITE` mit voller Deckkraft; Standard der Preference ist `preference_grid_none`
  (L710), das Raster ist also normalerweise aus. Soll: 1 dp, `rgba(255,255,255,0.25)`,
  immer sichtbar (Screenshot-Spec 1b).

**Rotation** (`MainUI.setViewRotation()` L199, `view_rotate_animation_duration` L84)
- 100 ms mit `AccelerateDecelerateInterpolator`. Soll: 300 ms,
  `cubic-bezier(0.2, 0.0, 0.0, 1.0)` (als `PathInterpolator`). Das Drehmodell selbst
  bleibt das aus Abschnitt 2 (Konflikt K3).

### 7.3 Farb-, Blur- und Schrift-Abweichungen (global)

| Element | Ist | Soll (Detail-Spec) |
|---|---|---|
| Kopf-/Fußfläche | `windowBackground` = `#1B1B1B` (`md_dark_surface`; Kommentar in `colors.xml`: „not pure black") | `#000000` |
| Overlay-Container (Zoom-Pille, Ruler, Chips) | `#99000000`, kein Blur | `rgba(20,20,20,0.65)` + Blur 20 px |
| Aktiver Akzent | `#F2F2F2`; `md_dark_primary` `#B0A695` definiert, aber ungenutzt; kein `DynamicColors` | Material-You-Pastell `#C2E7D9`–`#D3E8D8` bzw. `#E2E3E0`, dynamisch |
| Inaktive Icons / Sekundärtext | `#F2F2F2` bzw. Weiß | `#C4C7C5` bzw. `rgba(255,255,255,0.7)`; Modus-Karussell inaktiv `#8E918F` |
| Nivellierung dynamisch | Grün `#14e715` (Preference) | Signalgelb `#FEE75C` / `#F9AB00` |
| Fokus | Yellow 500 `#FFEB3B`, Rechteck | Weiß 1,5 dp Ring (Detail-Spec) / dünn goldgelb (Screenshot) – K9 |
| Video-Rot | `#F44336` | `#E53935` |
| Schriftfamilie | keine deklariert (kein `fontFamily`, kein `res/font/`) | Google Sans / Roboto Flex. Google Sans ist nicht frei lizenziert → Roboto Flex (OFL) als Font-Ressource bündeln oder Systemschrift bewusst akzeptieren – Entscheidung Falk |
| Schriftgewicht 600 | `Typeface.BOLD` = 700 | 600 über `Typeface.create(tf, 600, false)` (API 28+, hier verfügbar) |
| Dezimal-Komma | `Locale.getDefault()` in `ZoomStepper.formatRatio()` – stimmt nur, weil das Gerät auf `de` steht | ausdrücklich deutsch formatieren, oder bewusst so lassen und im Code vermerken |

Zu Blur: `View.setRenderEffect(RenderEffect.createBlurEffect(…))` (Android 12+)
zeichnet nur den Inhalt der View selbst weich, nicht das, was dahinter liegt. „Milchglas"
über der Kamera-Vorschau braucht eine eigene Lösung (Vorschau-Ausschnitt kopieren und
blurren) und kostet Frames. Vor dem Bau prüfen, ob 65 % Deckkraft ohne Blur genügt.

## 8. Bewusste Abweichungen von der Referenz

- ~~**Moduswahl im Querformat:** Beschriftungen stehen aufrecht statt um 90° gedreht.~~
  **Erledigt 2026-09-17** – nicht über den Rand-Trick (`View.setRotation()` +
  `MainUI.setFixedRotation`), sondern über eine `RotatedTextView`, die beim Messen
  Breite und Höhe vertauscht; damit stimmt der Platzbedarf und die Pille wird von selbst
  senkrecht.

### Beim Orientierungs-Umbau (2026-09-17) festgestellt, nicht orientierungsbedingt

Diese Punkte sind Folgen noch nicht gebauter Teile aus Abschnitt 7, keine
Entscheidungen. Sie lösen sich auf, wenn die jeweiligen Elemente kommen:

- **Moduswahl klebt im Querformat am Bildschirmrand** (Abstand 0), weil die unterste
  Zeile (Einstellungen/Umschalter/Regler) noch fehlt, die in der Referenz
  außen daneben sitzt (7.1 Punkt 3). Im Hochformat ist es der Gesten-Balken-Abstand.
- **Auslöser-Spalte:** bei uns Kamera-Wechsel / Auslöser / **Video-Umschalter**, in der
  Referenz Kamera-Wechsel / Auslöser / **Galerie** (7.1 Punkt 5; der Umschalter gehört in
  die unterste Zeile, 7.1 Punkt 3).
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
| K3 | Drehmodell | Sucher fix, Bedienelemente drehen isoliert um die eigene Achse, Karussell ordnet sich an der Kante neu | starrer Block, Fenster dreht mit (2) – **am Gerät verifiziert** | **Screenshot-Spec** (bereits so gebaut, bestätigt) |
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

Kein Konflikt, nur zur Klarheit: Die Detail-Spec hat **sowohl** „Video" im Karussell
**als auch** den Foto/Video-Umschalter im Dock – so ist es auch in der Referenz, beides
bauen.
