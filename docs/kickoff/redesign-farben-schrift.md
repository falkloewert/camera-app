# Kickoff: Schritt N – Farben, Blur, Schrift (`ui-spezifikation.md` 7.3) – letzter Redesign-Schritt

Arbeitsstand `master` nach Schritt M. Maße 2,25 px/dp.
Session-Titel: `camera-app – Schritt N Farben/Schrift`.

## 1. Soll (Tabelle `ui-spezifikation.md` 7.3; erledigt sind dort schon Nivellierung, Fokus, Video-Rot)

1. **Kopf-/Fußfläche** `#000000` statt `#1B1B1B` (`windowBackground`, `md_dark_surface`).
2. **Overlay-Flächen** (Zoom-Pille, Ruler, Chips, Stärke-Leiste): `rgba(20,20,20,0.65)` statt
   `#99000000`, **ohne Blur** (Recherche 2026-09-17 in 7.3: echter Blur über der Kamera-Vorschau
   braucht einen Snapshot-Umweg und geht vor API 31 nicht – bleibt eine spätere Verbesserung).
3. **Aktiver Akzent**: fester Pastellton `#D3E8D8` statt `#F2F2F2` – **kein** `DynamicColors`
   (das Theme ist seit Phase 1 fest dunkel). Ungenutztes `md_dark_primary` `#B0A695` weg.
4. **Inaktiv**: Icons/Sekundärtext `#C4C7C5` bzw. Weiß 70 %, Karussell inaktiv `#8E918F`.
5. **Schrift Roboto Flex** (SPEC 4.5, feste Entscheidung; OFL) als Font-Ressource bündeln und
   app-weit setzen; Gewicht 600 über `Typeface.create(tf, 600, false)` statt `BOLD` (700).
6. **Dezimal-Komma** in `ZoomStepper.formatRatio()` ausdrücklich deutsch formatieren.

## 2. Vorschläge PM (gelten, wenn Falk nicht widerspricht)

- Punkt 3: fester Salbei-Ton statt Farben aus dem Hintergrundbild – die App bleibt immer gleich.
- Kontrast prüfen: dunkle Schrift auf `#D3E8D8` (aktiver Zoom-Kreis, Karussell, Blatt-Knöpfe).

## 3. Inventar und Fallstricke

- `res/values/colors.xml`, `themes.xml`, `dimens.xml`; hart kodierte Farben in `ui/*.java`
  (`Color.parseColor`, `0x…`) mit `grep` finden und auf Ressourcen umstellen – Ponytail-Fund 3
  (Stil-Dopplung, `docs/reviews/2026-09-18-ponytail-review.md`) gehört genau hierher.
- Weiße Chips im Kopfband bleiben weiß mit schwarzer Schrift (gemessen, Schritt C) – nicht auf
  den Akzent umstellen. Signalgelb (Fokus, Wasserwaage) und Video-Rot bleiben.
- Roboto Flex ist eine variable Schrift: prüfen, dass Gewicht 600 am Gerät wirklich greift.
- Keine Layout- und Größenänderungen außer denen, die die neue Schrift erzwingt (Textbreiten im
  Karussell und in den Chips am Gerät ansehen).

## 4. Im selben Gerätetest nebenbei (Rest aus Schritt M, kein Code geplant)

0,5 per **Rad und Pinch** über die Grenze 1× ↔ 0,5, dazu 0,5 in **Video 4:3 / 16:9** und im
**Panorama**: wechselt die Linse sichtbar? Nur beobachten und in SPEC 4.5 eintragen; fällt
etwas aus, eigener kleiner Fix-Chat.

## 5. Abschluss (nach Falks Gerätetest)

SPEC 4.5 (Redesign-Restliste leer → Feature 6 prüfen, ob „Fertig"), Übersicht 3,
`ui-spezifikation.md` 7.3, `docs/changelog.md` ≤ 10 Zeilen, `graphify update .`, Commit mit
Dateipfaden + Push, drei Zeilen für den PM-Chat.
