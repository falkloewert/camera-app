# Kickoff: Schritt M – 0,5-Knopf wechselt nicht auf die Ultraweitwinkel-Linse (PM-Punkt 15)

Kleiner Fix-Chat. Arbeitsstand `master` (`beacee07`, Schritt L abgenommen).
Session-Titel: `camera-app – Schritt M 0,5-Knopf`.

## 1. Befund (SPEC 4.5 „Offen aus Schritt L", dort mit Logs)

Der Tipp auf „,5" kommt an (`ZoomTo(): 0`, `zoom to: 0.51`, Pille zeigt „,5×"), aber die
HAL baut die Sitzung nicht neu auf – kein Linsenwechsel, das Bild bleibt Hauptkamera.
Ausdrückliches Pinnen (`switchToPhysicalLens: 3`) wechselt korrekt.
**„Nicht mehr"** – das ging früher (Feinjustier-Rad 2026-09-18, Panorama-Weitwinkel).

## 2. Zuerst Ursache, dann Fix (Bugfix = Ursache, nicht Symptom)

1. **Seit wann?** Letzten Stand finden, an dem 0,5 wechselte (`git log` über `ui/ZoomStepper`,
   `ui/ZoomWheel`, `Preview.zoomTo()`, `CameraController2` Zoom-/Sitzungs-Teile; bei Bedarf
   `git bisect` mit Falk am Gerät – dann gebündelt, nicht Commit für Commit).
2. **Matrix-Test in einem Gerätedurchgang:** 0,5 in Foto 4:3 · Foto 16:9 · Video 4:3 ·
   Video 16:9, dazu Panorama. Verdächtig laut SPEC: Auflösung/Verhältnis/Format/RAW/fps
   verschieben die Grenze, ab der die HAL tauscht (die Schritte E–L haben Panorama-Größen,
   16:9-Standard und Nacht-Größen geändert).
3. **Fix nach Ursache.** Die zwei Wege aus der SPEC nur, wenn die Ursache nicht direkt behebbar
   ist: (a) 0,5-Stufe über den vorhandenen Pin fahren statt über den Zoom-Wert, oder
   (b) `CONTROL_ZOOM_RATIO` aus dem Capture-Result zurücklesen und bei Nichtwirkung auf den Pin
   ausweichen. Den Knopf auszublenden ist nur die letzte Lösung – dann Falk fragen.
   Preis von (a) beachten: gepinnte Linse = keine Low-Light-Automatik der HAL (wie PM-Punkt 8).

## 3. Grenzen

- Kein UI-Umbau, keine neuen Farben. Die Zoom-Pille sieht danach aus wie vorher.
- Pinch- und Rad-Zoom über die Grenze 1× ↔ 0,5 müssen danach genauso wechseln wie der Knopf.
- Tele „5" (PM-Punkt 8) nicht mit anfassen, nur notieren, falls der Fix ihn berührt.

## 4. Abschluss (nach Falks Gerätetest)

SPEC 4.5 („Offen aus Schritt L" → erledigt, PM-Punkt 15 schließen), `docs/changelog.md`
≤ 10 Zeilen mit Ursache und Matrix-Ergebnis, `graphify update .`, Commit mit Dateipfaden +
Push, drei Zeilen für den PM-Chat.
