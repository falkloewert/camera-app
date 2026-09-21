# Kickoff Foto-Blatt Runde 1 (2026-09-21) – Falk, Feature-Chat

Arbeitspaket aus Falks Ansage „Foto-Modus und Selfie, erst mal nur normal". Branch
`bench/blatt-deepseek-flash-2` (nach 45a87e18, Video-Blatt Runde 2).

## Gerätefakten (aus `dumpsys media.camera`, Pixel 9 Pro)

- Rückkamera: **6 physikalische IDs, real 3 Linsen** – 6,9 mm/Nahgrenze 10,5 cm
  (Normal), 17,9 mm/80 cm (Tele), 2,02 mm/**2 cm** (Weitwinkel = zugleich Makrolinse;
  es gibt keine eigene Makrolinse).
- Front: Video bis 3840×2160 (4K), Foto bis 3840×2736 (4:3) / 3840×2160 (16:9).
- **Alle** Objektive melden `focus_mode_macro` – deshalb steht die Makro-Zeile bisher
  auch im Selfie-Blatt.

## Entscheidungen Falk (2026-09-21)

1. **Vier Linsenknöpfe: WW · NO · MA · TE.** MA = Weitwinkel-Linse mit
   `focus_mode_macro`, WW = dieselbe Linse mit Normal-Autofokus.
2. **Selfie bekommt Auflösungs-Auswahl** (FHD/4K), im Foto- **und** Video-Blatt.

## Aufgaben

1. **Verhältnis-Sprung (Foto + Video).** Ursache: `MainActivity.cameraSetup()` →
   `MainUI.layoutUI()` → `rotateGlyphs()` → `SettingsSheet.onUIRotationChanged()` →
   `place()` + `buildContent()` bei **jedem** Layout-Durchlauf, auch bei unveränderter
   Drehung und ohne den `cameraReady()`-Wächter, den `refresh()` hat. Während des
   Kamera-Neustarts fallen die kameraabhängigen Zeilen weg und kommen wieder → zweimal
   springen, plus zwei volle Rebuilds = Verzögerung. Fix: Rebuild nur bei echter
   Drehungsänderung, und über `refresh()` leiten.
2. **Timer-Icons.** Referenz (`pixel-sheet-allgemein-hochformat.png`): Aus = Stoppuhr mit
   Schrägstrich, 3/5/10 = Stoppuhr **mit Ziffer im Ring**. `ms_timer_3`/`ms_timer_10` sind
   die ziffernlosen Material-Glyphen → aus dem Ring von `ms_timer_5` + den offiziellen
   Ziffern neu bauen. Selbst gewählte Skalierung → `ui-spezifikation.md` Abschnitt 8.
3. **Makro-Modus raus.** Zeile „Makrofokus" im Foto-Blatt Allgemein löschen (beide
   Kameras). Der Modus kommt nur noch über den MA-Knopf.
4. **Objektivauswahl = Pille.** Statt der Zoom-Stufen zeigt die Pille im Manuell-Modus
   vier Knöpfe `WW · NO · MA · TE` im selben Stil. Die 6 IDs werden auf 3 Linsen
   zusammengefasst (gleicher Bildwinkel = gleiche Linse, jeweils die ID mit dem größeren
   Sensor – sonst verlöre man die 50-MP-Größe).
5. **Linse bleibt gepinnt.** Autofokus nur auf der gewählten Linse, kein Zurückspringen
   beim Antippen. **Erst am Gerät mitloggen** (Logcat beim Tippen), dann fixen.
6. **Selfie: Makro-Zeile raus** (fällt mit 3 weg).
7. **Selfie-Foto: Blitz → Selfiebeleuchtung.** `Preview.updateSelfieLight()` von der
   `is_video`-Sperre befreien, Front-Screen-Blitz aus, Zeile „Aus/An" wie im Video-Blatt.
8. **Auflösungs-Zeile Selfie.** Foto: zwei Größen der aktuellen Verhältnis-Klasse
   (Beschriftung in MP wie im Pro-Reiter des Rückkameras). Video: die bestehende
   Auflösungs-Zeile auch für die Frontkamera freigeben (FHD/4K).

## Test

Gebündelt am Pixel 9 Pro (adb), App-ID `de.falkloewert.cameraapp`: Punkte 1–4, 6–8 auf
einmal; Punkt 5 danach mit Logcat.

## Nachtrag (Falk, 2026-09-21)

Punkt 8 präzisiert: Selfie-**Foto** ohne Auswahl (wie die native App – die Zeile zeigt die
eine Größe, die Kamera wird auf ihre größte gestellt), Selfie-**Video** FHD plus die größte
Qualität, die die Frontkamera hergibt (hier "6MP" = 3264×1836; 4K gibt es dort nicht).
Verhältnisse bleiben immer 16:9 oder 4:3.
