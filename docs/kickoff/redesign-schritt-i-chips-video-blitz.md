# Kickoff Schritt I: Chip-Größe, Video-Blitz, Google-Dock für Zeitraffer/Zeitlupe

Session-Titel `camera-app – Schritt I Chips/Video`. Falks sechs Punkte nach H, alle in einem
Zug, ein Gerätetest am Pixel 9 Pro, ein Commit am Ende. Reihenfolge nach Wirkung.

## A – Chips (Punkt 1)

**Alle Chips im Kopfband bekommen genau den Kasten des Auflösungs-Chips** („9 MP"), der als
Vergleich und Dauer-Maß gilt („auch zukünftig sollen alle chips so aussehen"). Der
Auflösungs-Chip ist `header_chip_square_dp` (17,8 dp) mit `header_chip_background` (weiß, 2 dp
Radius); die Icon-Chips standen auf 32 dp / 8 dp Innenabstand. Fix: im Layout alle zehn
Icon-Buttons auf 18 dp / 3 dp, `header_chip_background_icon` durch `header_chip_background`
ersetzt; das Zeichen schrumpft mit (≈11,8 dp). `header_indicators_height_dp` folgt jetzt
`header_chip_square_dp` – eine Quelle für Bandhöhe und Kastenmaß.

## B – Video-Blitz (Punkt 2)

Automatik **komplett raus**, nur noch **Aus/Taschenlicht** – „macht nur Probleme und ist
Bloat". Entfernt (`Preview.java`): der Auto-Zweig in `updateFlashForVideo`, `updateTorchAuto`,
`setTorchForISO`, `flashValueForCamera`, `torch_auto_registered`/`torch_auto_on`; im Blatt die
dritte Option und das ISO-Aufräumen (`SettingsSheet.setVideoFlash`). Ein gemerktes
`flash_auto` fällt im Video auf **Aus** zurück. `AUTO_ISO_ON`/`AUTO_ISO_OFF` und
`autoLightOn()` bleiben (braucht „Nacht auto" im Foto); `CameraController.setISOChangeCallback()`
bleibt liegen (generische Einrichtung). Nebeneffekt: **H8** (Auto-Torch in Zeitraffer/
Zeitlupe) ist damit erledigt.

## C – Pro-Knopf in Zeitraffer/Zeitlupe (Punkt 3)

Referenz ist Falks Screenrecording der Google-Kamera; gemini hat **genau dieses Feature**
analysiert. Ergebnis: Google **verschmilzt die Knöpfe nicht**, sondern **wechselt die Belegung
der untersten Reihe**. Zu (Vorgabe): nur die Geschwindigkeits-Pille unten rechts, keine
Pro-Knöpfe. Pille antippen → Modusleiste und Foto/Video-Umschalter verschwinden, die
Stärke-Reihe rückt mittig über den Auslöser, die **Pro-Knöpfe nehmen die freigewordene untere
Reihe**. Der Pro-Knopf öffnet einen **schwebenden Regler über dem Auslöser**, kein Blatt.
Umsetzung: `video_speed_button` und `pro_panel_button` teilen sich dieselbe Ecke
(`MainUI.layoutUI`), die offene Reihe versteckt Leiste und Umschalter
(`refreshVideoSpeedRowDock()`), der Pro-Knopf bleibt im Video auf **Belichtung + Weißabgleich**
(Schritt H). **Merker `speed_row_dock_hidden`**: nur was die Reihe selbst weggenommen hat, holt
sie zurück – ohne ihn blieben Leiste und Umschalter nach dem Schließen weg (am Gerät gefunden).
Tipp auf die schon gewählte Stärke schließt die Reihe.

## D – Kamera-Neustart (Falk-Frage 4)

Nicht weglassbar bei **Nacht**: das ist eine Kamera-Erweiterung, und
`MainActivity.updateForSettings()` öffnet die Kamera dann zwangsweise neu – Erweiterungs-
Sitzungen können beim Umkonfigurieren hängen (Upstream-Arbeitsweise). Beim **Blitz** war der
Neustart unnötig: `MainUI.applyLightSettings()` vergleicht jetzt Zustand gegen Preferences und
ruft bei reiner Blitz-Änderung nur `Preview.updateFlashQuiet()` (Sitzungswechsel, kein
Neuaufbau). Nur wenn die Blitz-Automatik manuell gesetzte ISO/Zeit wegräumt, bleibt er.

## E – Panorama (Punkte 5 und 6)

5. **Nacht erzwingt Blitz Aus:** `applyLightSettings()` überschreibt bei `night_on`/`night_auto`
   den Blitzwert auf `flash_off`; am Gerät geprüft (Chip wechselt von Blitz auf Mond).
6. **RAW-Chip fehlt in Panorama – kein Defekt:** `MyApplicationInterface.isRawAllowed()` gibt
   RAW nur für Standard/DRO/Belichtungsreihe/HDR/Fokusreihe frei, Panorama fällt auf `false`.
   Upstream-Verhalten, kein Chip zu bauen.

## Verifikation (Pixel 9 Pro, 2026-09-22)

Kopfband-Chips per uiautomator-Dump alle 40×40 px (= 18 dp, gleich dem „FHD"-Chip). Zeitlupe
und Zeitraffer je zu (Pille allein) und offen (Leiste und Umschalter weg, Pro-Knopf in der
Ecke); Panel mit Back geschlossen → Reihe wieder offen; Stärke erneut getippt → Reihe zu,
Leiste und Umschalter zurück. Video-Blatt: „Blitz" mit genau zwei Knöpfen. **Offen:** Falks
Abnahme. Frage: `res/drawable/header_chip_background_icon.xml` ist unbenutzt – darf sie weg?
