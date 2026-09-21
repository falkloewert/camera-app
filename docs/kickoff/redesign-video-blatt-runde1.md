# Kickoff: Video-Blatt, Runde 1 (kleine Fixes)

Branch: `bench/blatt-deepseek-flash-2`. Nur Video-Modus. Regeln: `CLAUDE.md`, SPEC 2.1
(Gerätetest durch Falk, Abschlussprotokoll). Kein Kickoff für V2/V3 – die kommen als
eigene Runden, nachdem V1 am Gerät sitzt.

## Lesen (nur das)
- `ui-spezifikation.md` Abschnitt 4b/4c (Video-Blatt: Allgemein/Mehr) und 8 (Messung
  schlägt Detail-Spec, 2,25 px/dp).
- `ui/SettingsSheet.java`: `videoGeneralRows()` und `videoMoreRows()` (Zeilen 895–1060),
  `findQuality()` (ab 1200), `setVideoQuality/setVideoFPS/setVideoStabilization`.
- `MyApplicationInterface.java` 730 (Stabilisierungs-Default), 749/788 (FPS-Pref).

## Muss (V1)

1. **Auflösung FHD/4K nur 16:9.** `findQuality()` nimmt heute die erste Qualität mit
   passender Breite – deshalb ist „FHD" auf dem Pixel 1920×1920 (1:1). Es darf nur eine
   Qualität gewählt werden, deren Profil exakt 1920×1080, 3840×2160 bzw. 7680×4320 ist.
   *Fertig:* Wertzeile „1920x1080 FullHD" bzw. „3840x2160 4K", kein 1:1-Knopf, Aufnahme 16:9.
2. **fps-Knopf bei „Standard".** Ist der FPS-Pref „default", den realen Wert aus dem
   CamcorderProfile der aktuellen Qualität lesen und den passenden Knopf (24/30/60)
   markieren; die Wertzeile zeigt die Zahl statt „Standard".
   *Fertig:* „default" + real 30 → Knopf „30" ausgewählt, Wertzeile „30".
3. **Videostabilisierung standardmäßig an.** Default an drei Stellen auf `true`:
   `MyApplicationInterface:730`, `SettingsSheet:1038` und
   `preferences_sub_video.xml:14` (`android:defaultValue="true"`, fehlt dort).
   Gespeicherte Werte bleiben unangetastet. *Fertig:* ohne gespeicherten Wert steht die
   Zeile im Reiter „Mehr" auf „An".
4. **Selfie-Video: Auflösung entfällt.** Für die Frontkamera keine Auflösungszeile, und
   die Aufnahme läuft 16:9 (1920×1080); gespeicherte 1:1-Qualität der Frontkamera dabei
   korrigieren. *Fertig:* Selfie-Blatt ohne Auflösungszeile, Video 1920×1080.
5. **Selfie-Video: Makro entfällt.** Frontkamera bekommt keine Makro-Zeile (Rückkamera
   unverändert). *Fertig:* Selfie-Blatt ohne Makro-Zeile.
6. **Selfie auch ohne Objektivauswahl.** Die Zeile im Foto-Blatt (Reiter „Pro") entfällt
   bei der Frontkamera – sie meldet auf diesem Gerät 2 physische Objektive (Fund
   2026-09-21), die Auswahl ist dort aber sinnlos (Entscheidung Falk 2026-09-21).
   *Fertig:* Selfie-Blatt ohne Objektivauswahl; Rückkamera unverändert.

## Nicht in dieser Runde
- V2: Makro soll fixiert bleiben (Tippen darf nicht auf Autofokus zurückstellen);
  Video-Blitz „Autom." = Torch bei Dunkelheit automatisch an (Falks Wahl).
- V3: Selfiebeleuchtung – neuer Modus mit Glühbirnen-Icon, nur die schwarzen Ränder
  werden weiß (Open Cameras `flash_frontscreen_torch` färbt bisher den ganzen Schirm).

## Abschluss
- Gerätetest durch Falk; danach `graphify update .`, Commit **nur mit Dateipfaden**,
  Push auf `bench/blatt-deepseek-flash-2`, Bericht ≤ 10 Zeilen (was geprüft, was nicht).
