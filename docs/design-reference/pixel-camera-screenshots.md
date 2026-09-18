# Pixel-Kamera-App – Referenz-Screenshots (Textbeschreibung)

Von Falk am 2026-09-15 im Redesign-Phase-1-Chat geteilt (8 Screenshots der echten
Google-Kamera-App auf einem Pixel). Die Bilder selbst kamen nur als Chat-Anhänge an,
nicht als Dateien – deshalb hier eine möglichst genaue Text-Rekonstruktion. **Falls die
Originalbilder je als Dateien verfügbar werden, hier ablegen** (z. B.
`pixel-camera-01-landscape-photo.png` etc.) und diese Beschreibung als Ergänzung
stehen lassen, nicht ersetzen.

Deckt **Phase 2** (Zoom-Stepper, Moduswahl-Leiste) und **Phase 3** (Settings-Sheets,
volle Einstellungsliste) ab – mehr Detail als die ursprüngliche Analyse in
`docs/SPEC.md` Abschnitt 4.5.

---

## 1. Hochformat, Foto-Modus (wichtigste Referenz)

Von oben nach unten:

1. **Oben links:** kleine dunkle Pillen-Chips gestapelt – "12MP" (Auflösung), darunter
   ein Makro-Blüten-Icon mit kleinem "A" (Automatisch).
2. **Oben rechts:** "?"-Hilfe-Icon, dunkler Kreis-Chip.
3. **Sucher:** dünne weiße 3×3-Rasterlinien, dünner goldener/orangener Fokus-Ring.
4. **Zoom-Stepper** (schwebt über der Haupt-Bedienzeile, horizontal zentriert):
   dunkle Pillen-Leiste mit vier Stufen ",5 / 1x / 2 / 5". Aktive Stufe = volle
   weiße Kreis-Füllung mit dunkler Schrift ("1x" im Beispiel), inaktive Stufen =
   heller Text ohne Hintergrund, alle in einer Reihe mit gleichmäßigem Abstand.
5. **Moduswahl-Leiste** (horizontal scrollbare Textzeile, unter dem Zoom-Stepper):
   "zufügen · Porträt · **Foto** · Nachtsicht · Pan[orama]" – aktiver Modus bekommt
   eine helle, abgerundete Pillen-Füllung als Hintergrund, alle anderen sind reiner
   Text ohne Hintergrund, kein Icon je Modus.
6. **Haupt-Bedienzeile:** Galerie-Miniatur (abgerundetes Quadrat, links) – Auslöser
   (großer voller weißer Kreis mit dünnem dunklem Rand, Mitte, deutlich größer als
   Nachbar-Elemente) – Kamera-Wechsel-Button (dunkles abgerundetes Quadrat mit
   Wechsel-Pfeil-Icon, rechts).
7. **Unterste Zeile** (kleiner, unter der Haupt-Bedienzeile): Einstellungen-Zahnrad
   (links) – Foto-Icon (Mitte) – Video-Icon (rechts) – vermutlich Schnellzugriff
   zum direkten Umschalten Foto/Video ohne die Moduswahl-Leiste zu scrollen.

## 2. Hochformat, Video-Modus

Fast identisch zu Foto-Modus, Unterschiede:

- Auflösungs-Chip oben links zeigt "FHD" statt "12MP".
- Moduswahl-Leiste zeigt "Weichzeichnen · **Video** · Nachtsichtmodus" (auf Video
  zentriert gescrollt).
- Auslöser-Button ist ein **Ring** (dunkler Rand um weiße Fläche oder umgekehrt,
  "Aufnahme bereit"-Optik) statt der vollen Kreisfüllung im Foto-Modus – visuell klar
  unterscheidbar vom Foto-Auslöser.
- Zoom-Stepper und Haupt-Bedienzeile sonst gleich aufgebaut.

## 3. Querformat, Foto-Modus

Gleiche Elemente wie Hochformat, aber um 90° gedreht angeordnet:

- Zoom-Stepper als **vertikale** Pillen-Leiste (Stufen von oben nach unten:
  "5 / 2 / 1x / ,5").
- Moduswahl-Leiste als **vertikale** Text-Spalte am rechten Rand ("Pan… / Nachtsicht /
  **Foto** / Porträt / zufügen").
- Haupt-Bedienzeile (Auslöser, Kamera-Wechsel, Galerie) am rechten Rand vertikal
  gestapelt statt horizontal.
- "12MP"/Makro-Chips wandern an eine Ecke, "?"-Icon an die gegenüberliegende Ecke.

## 4. Foto-Einstellungen – Bottom-Sheet, Tab "Allgemein"

- Ziehgriff oben, Titel "Fotoeinstellungen" + "···"-Mehr-Icon rechts daneben.
- Tab-Umschalter "Allgemein" (aktiv, helle Pille) / "Pro" (inaktiv, dunkel).
- Zeilen-Schema (durchgängig für alle Settings-Zeilen in Bottom-Sheets): Titel (fett,
  weiß) + graue Untertitel-Zeile mit aktuellem Wert, rechts eine Reihe kleiner
  Icon-Buttons – ausgewählte Option = volle weiße Kreis-Füllung, andere = dunkel/gedämpft.
  Beispiele: "Mehr Licht" (Keines/Nachtsicht/Blitz), "Top-Foto" (Aus/Automatisch/
  Verbessert), "Timer" (Aus/3s/5s/10s), "Makrofokus" (Aus/Automatisch/An),
  "Verhältnis" (Breitbild 16:9/Quadratisch).

## 5. Foto-Einstellungen – Tab "Pro" (manuelle Regler)

- Info-Leiste über dem Sucher: "Ultraweitwinkel f/1.95 · Autofokus · (A)1/375s ·
  (A)ISO 683".
- Horizontaler Regler (Helligkeit/Belichtung): Sonnen-Symbole an beiden Enden,
  Tick-Marks entlang der Leiste, ein Schieberegler-Punkt.
- Darunter eine horizontal scrollbare Reihe aus Icon+Text-Tabs: "Alle zurücksetzen /
  **Helligkeit** / Schatten / Weißabgleich / Fokus / Belichtung…" – aktiver Tab
  ist zentriert/hervorgehoben, der Regler darüber wechselt je nach gewähltem Tab.

## 6. Video-Einstellungen – Bottom-Sheet, Tab "Allgemein"

- Gleiches Grundschema wie Foto-Settings. Titel "Videoeinstellungen", Tabs
  "Allgemein"/"Mehr".
- Zeilen: "Blitz" (Aus/An), "Video-Optimierung" (Aus/An, mit "ⓘ"-Info-Icon neben dem
  Wert), "Auflösung" (FHD/4K/8K), "fps" (24/30/60), "10-Bit-HDR-Video" (Aus/An),
  "Makrofokus" (Aus/Automatisch/An).

## 7. Video-Einstellungen – Tab "Mehr"

- "Videostabilisierung" (Standard/Aktiv[Lauf-Symbol]/Extra-aktiv[Schloss-Symbol]),
  "Mikrofon" (Smartphone-Mikrofon/Bluetooth), "Verbesserte Sprachqualität" (Aus/An).

## 8. Vollständige Einstellungsliste (gruppiert, scrollbar)

Abschnitte mit Überschriften, jede Zeile: Icon links, Titel fett + optionaler
grauer Erklärtext darunter, rechts entweder ein **Switch** (An-Zustand = gedämpfter
Akzentton, kein reines Weiß/Grau) oder ein **Chevron ">"** für Zeilen, die zu einem
Unterbildschirm führen.

- **Allgemein:** Standort speichern (An), Kameratöne (An), Gerätespeicher (Chevron),
  Scannen via Kamera (An)
- **Komposition:** Tipps zur Bildkomposition (An), Rastertyp "3×3" (Chevron), Selfies
  wie angezeigt speichern (Aus), Schritt-für-Schritt-Foto (Aus, Chevron + Tutorial-Link)
- **Schnellzugriff:** Einstellungen für Schnellzugriff (An), Aktion für
  Lautstärketaste "Auslösen" (Chevron), Selfie-Timer per Handzeichen (An)
- **Foto:** Nuancenreiche Farbdarstellung in Fotos (Aus), Ultra HDR (An), Tiefendaten
  für Apps für soziale Medien (Aus), Oft fotografierte Gesichter (An, Chevron),
  Astrofotografie (Chevron)
- **Video:** Videostabilisierung (An, weitere Zeilen vermutlich beim Scrollen
  sichtbar, nicht im Screenshot enthalten)

---

## Abnahme-Kriterien Zoom & Moduswahl (von Falk am 2026-09-15 präzisiert)

Prüfbare Punkte – gegen diese Liste ist zu vergleichen, bevor etwas als fertig gilt:

1. **Zoom-Stufen sind genau vier: 0,5 / 1 / 2 / 5.** Keine Stufe darüber (kein 10x),
   keine algorithmisch abgeleiteten Werte. 1 ist Standard, 0,5 ist die
   Weitwinkel-Linse. Alles über 5 wird über das Feinjustier-Rad gemacht, nicht über
   Stufen-Buttons.
2. **Feinjustier-Rad:** Zieht man an der Zoom-Stufen-Leiste (oder ist man auf einer
   hohen Stufe), verwandelt sie sich in ein Rad/Lineal mit Teilstrichen und
   Beschriftungen (1 / 2 / 5 / 10 / 30), an dem sich der Zoom stufenlos einstellen
   lässt. Der aktuelle Wert steht daneben in einer Pille, z. B. "6,7×". Im Querformat
   steht dieses Lineal senkrecht.
   *Am Gerät präzisiert (Falk, 2026-09-18, umgesetzt):* Das Rad bleibt **nicht** auf hohen
   Stufen offen – nach dem Loslassen kehrt immer die Stufen-Pille zurück, und der
   nächstliegende Slot zeigt den genauen Wert („6,7×" im 5er-Slot). Hauptmarken mit „,5"
   (K7). Verbindlich ist `ui-spezifikation.md` 3.1/3.2.
3. **Pinch-Zoom** (zwei Finger) muss weiter funktionieren und Stufen/Rad mitführen.
4. **Moduswahl-Leiste liegt auf dem schwarzen Rand, nicht über dem Bild.** In den
   Referenz-Screenshots sitzt sie in der schwarzen Chrome-Fläche neben/unter dem
   Sucher – dadurch ist sie immer lesbar. Sie darf nicht halbtransparent über dem
   Motiv schweben.

## Für Phase 2 direkt relevant (Zoom-Stepper + Moduswahl-Leiste)

Aus Abschnitt 1–3 destilliert:

- Zoom-Stepper: eine durchgängige Pillen-Leiste mit beliebig vielen Stufen (nicht nur
  zwei wie beim bestehenden Panorama-Button-Paar), aktive Stufe = volle weiße
  Kreis-Füllung + dunkle Schrift, Rest = heller Text ohne Füllung. Horizontal in
  Hochformat, vertikal in Querformat (gleiches Rotationsprinzip wie das bestehende
  Panorama-Button-Paar aus Feature 4).
- Moduswahl-Leiste: horizontal scrollbare Textzeile (vertikal in Querformat), aktiver
  Modus = helle Pillen-Füllung, kein Icon je Modus, nur Beschriftung.
- Zoom-Stepper sitzt über der Moduswahl-Leiste (Hochformat) bzw. spiegelbildlich
  positioniert (Querformat) – beide zusammen bilden einen neuen UI-Block oberhalb/
  neben der bestehenden Haupt-Bedienzeile.
