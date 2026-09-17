# UI-Spezifikation: 1:1-Nachbau der Pixel-Kamera-Oberfläche

Verbindliche Bau-Vorgabe für das Material-3-Redesign (SPEC.md 4.5), abgeleitet aus Falks
Referenz-Screenshots seiner echten Pixel-Kamera-App (2026-09-14 und 2026-09-15).

**Grundregel, von Falk am 2026-09-15 ausdrücklich gesetzt:** *Eins zu eins wie in der
Referenz.* Keine abgeleiteten, "geräteadaptiven" oder verbesserten Varianten, wo die
Referenz einen konkreten Zustand zeigt. Wenn etwas in der Referenz nicht erkennbar ist,
nachfragen statt erfinden.

**Vor jeder Fertigmeldung:** Abweichungen zu dieser Liste ausdrücklich aufzählen, nicht
nur "fertig" melden.

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
- *Offen:* "×" als Malzeichen (×) oder Buchstabe "x"? Aktuell umgesetzt: ×.

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
- **Drehmodell aus Abschnitt 2** (2026-09-17): starr mitdrehende Anordnung in allen
  drei von Android angebotenen Lagen; am Gerät geprüft inkl. EXIF-Drehung von Fotos in
  allen vier Lagen, Video-Rotationshinweis, Panorama-Linsenpaar. Technik: die
  Positions-Zuordnung in `MainUI.layoutUI()` (`Side`-Tabelle) war bereits die Drehung;
  die Inhalte der drei Leisten werden von `MainUI.orientBar()` umgeklappt und in der
  Reihenfolge gedreht, die Modus-Texte über `RotatedTextView` quer gezeichnet, Abstände
  über `android:divider`. Bewusst **kein** `View.setRotation()` auf ganze Leisten (siehe
  Abschnitt 8, alter Punkt).
- Alter Zoom-Schieberegler entfällt, wo die Stufen-Leiste greift

## 7. Was noch fehlt

1. Feinjustier-Rad (Abschnitt 3.2)
2. Foto-Einstellungen-Blatt (Abschnitt 4)
3. Unterste Zeile (Einstellungen-Button, Foto/Video-Umschalter, Regler-Button)
4. Kopfzeilen-Chips ("12 MP", Makrofokus) und Hilfe-Icon
5. Galerie-Miniatur und Kamera-Wechsel im Referenz-Stil (abgerundete Quadrate) und auf
   der Auslöser-Zeile (aktuell sitzt die Galerie noch in der alten Icon-Leiste)
6. Gelber Fokus-Ring statt des aktuellen Fokus-Rechtecks

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
  Zeile (Einstellungen/Umschalter/Regler, Punkt 3) noch fehlt, die in der Referenz
  außen daneben sitzt. Im Hochformat ist es der Gesten-Balken-Abstand.
- **Auslöser-Spalte:** bei uns Kamera-Wechsel / Auslöser / **Video-Umschalter**, in der
  Referenz Kamera-Wechsel / Auslöser / **Galerie** (Punkt 5; der Umschalter gehört in
  die unterste Zeile, Punkt 3).
- **Info-Overlay der Vorschau** (Batterie-/Blitz-Anzeige, von `DrawPreview` auf die
  Leinwand gezeichnet) sitzt in jeder Lage oben links, dreht also nicht mit. Das
  Gegenstück in der Referenz sind die Kopfzeilen-Chips (Punkt 4), die mitdrehen. Beim
  Bau der Chips das Overlay mitbedenken oder ersetzen.
