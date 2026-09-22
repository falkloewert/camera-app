# Kickoff Schritt G: Falks 14 Befunde aus C/E/F, Chips in die Kopfzeile

Session-Titel `camera-app – Schritt G Chips/Befunde`. Reihenfolge nach Wirkung: erst das Blatt
(was Falk täglich anfasst), dann der Panorama-Objektiv-Knoten, dann die Kopfzeile. Alle 14 Punkte
in einem Zug, ein Gerätetest, ein Commit (mit E und F zusammen, sie sind noch nicht committet).

## A – Blatt (Licht-Zeile, Video-Blitz, Timer)

1. **(7) Reihenfolge der Licht-Knöpfe:** `nichts · auto nacht · auto blitz · nacht an · blitz an`
   (heute: nichts · nacht auto · nacht an · blitz auto · blitz an). `SettingsSheet` ~790-836.
2. **(5) „Auto Blitz lässt sich bei Foto nicht auswählen"** + **(1) „Auto-Blitz (Torch) in allen
   Video-Modi verschwunden"** haben **eine** Ursache: manuelles ISO streicht `flash_auto` aus
   `supported_flash_values` (`Preview.java:3014-3029`). Die Video-Zeile schneidet ihre Knöpfe
   genau daran (`SettingsSheet ~1166`), die Foto-Zeile bietet sie zwar an (seit F hardwarebasiert),
   aber ein Tap läuft ins Leere, weil das ISO den Wert wieder wegwirft. Fix an der gemeinsamen
   Stelle: Video-Zeile bietet `off/auto/torch` wie die Foto-Zeile über `preview.hasFlashUnit()`
   an (∩ `isFlashSupportedForVideo`), und wer `flash_auto` wählt, bekommt das manuelle ISO
   ausgeschaltet (`clearISOPref()`) – Automatik und festes ISO schließen sich aus, wie Nacht und
   Blitz.
3. **(13) Panorama: kein Licht-Knopf markiert:** die Markierungen tragen Ausnahmen
   (`!night_auto && !night_on && …`), sobald zwei Zustände gleichzeitig an sind, ist keiner
   markiert. Fix: genau ein Knopf wird aus dem Zustand markiert (Nacht auto > Nacht an > Blitz
   auto > Blitz an > nichts), Tippen setzt den Rest zurück.
4. **(6) Timer-Chip:** siehe C4 (Kopfzeile).

## B – Panorama-Objektiv

5. **(9) „Weitwinkel stellt die Objektivauswahl auf Manuell":** das Panorama benutzt den
   allgemeinen `cameraIdSPhysical`-Zustand (`MainActivity.clickedPanoramaLensWide`), und das
   Blatt liest ihn als „Manuell" (`SettingsSheet ~1069`). Fix: im Panorama zeigt die Zeile
   „Automatisch / Weitwinkel" (Label aus `getPanoramaWideLensLabel()`) und schaltet über
   `clickedPanoramaLens1x/Wide`.
6. **(11) Zoom-Pille bleibt im Panorama stehen:** `MainUI.showGUI` ruft
   `setZoomStepperVisibility(zoomStepper.hasSteps() …)`, und der Stepper geht in den
   Objektiv-Modus, sobald ein physisches Objektiv gewählt ist. Fix: im Panorama immer `GONE`
   (dort führt die Panorama-Reihe das Objektiv).
7. **(10) Panorama-Objektivblase im falschen Stil:** `panorama_lens_row` benutzt noch
   `circle_background`/`icons_background_tint` (Alt-Stil wie `switch_multi_camera`), die
   Zoom-Pille hat den neuen Stil. Fix: Reihe auf den Stil der Stepper-Knöpfe umstellen.
8. **(14) Kein Nachtmodus im Weitwinkel-Panorama:** mit festgelegtem physischem Objektiv holt
   `CameraController2:1215` die Erweiterungs-Charakteristiken nicht mehr (`cameraIdSPhysical == null`),
   also gibt es keine Nacht-Sitzung. Die Nacht-Erweiterung läuft auf der Hauptkamera – ein
   „Weitwinkel-Nachtbild" wäre still ein 1×-Bild und würde das Panorama zerreissen. Fix: die
   beiden Zustände schließen sich aus, der neuere gewinnt – „Weitwinkel" schaltet die Nacht aus,
   „Nacht an/auto" setzt das Objektiv auf 1×. Beides ist in der Licht-Zeile und in der
   Objektivwahl sichtbar.
9. **(12) Nacht-Panorama kippt auf 4:3:** `panoramaResolution()` fällt auf `choosePanoramaResolution()`
   (größtes 4:3) zurück, sobald die gespeicherte Größe nicht in der Liste steht – in der
   Nacht-Sitzung (eigene Größenliste der Erweiterung) und beim physischen Objektiv (anderer
   Preferences-Schlüssel) ist das der Fall. Fix: der Rückfall hält das **Verhältnis** der
   gespeicherten Größe, 4:3 nur noch ohne gespeicherte Wahl.

## C – Kopfzeile und Chips

10. **(2) Akku-Anzeige weg:** `DrawPreview` 2351-2392 samt `show_battery_pref`, Preference und
    Eintrag in den Einstellungen.
11. **(3) Canvas-Icon-Reihe weg, ihre Chips ins Band:** die Reihe in `DrawPreview`
    (1473-1732: GPS, RAW, Gesicht, Auto-Ausrichten, Modus, Stempel, Ton aus, Tempo, Blitz)
    entfällt; doppelte Zustände (GPS/RAW/Stempel/Blitz) zeigt das Band schon. Neu ins Band
    rechts: Gesichtserkennung, Auto-Ausrichten, Ton aus. Tempo-Icons (Zeitlupe/Zeitraffer/
    High-Speed) entfallen ersatzlos – Modusleiste und fps-Chip sagen dasselbe.
12. **(8) HDR/DRO-Chip nach oben links** neben den Auflösungs-Chip (`header_mode`, DRO/HDR und
    die übrigen Modi, die keine Modusleiste haben: Expo-Burst, Serienbild, Rauschunterdrückung).
13. **(4) Mond-Chip nach oben rechts** auf den Platz des Blitz-Chips: Nacht an → `ms_dark_mode`,
    Nacht auto → `ms_night_sight_auto` (mit „A"), sonst der Blitz-Zustand wie bisher.
14. **(6) Timer-Chip rechts im Band**, tippbar: aus → 3 → 5 → 10 → aus (`ms_timer_*`), Zustand
    aus den Preferences. Im Panorama bewusst ohne Chip – wie die Timer-Zeile im Blatt.

## Gerätetest (ein Durchgang, Pixel 9 Pro, `de.falkloewert.cameraapp`)

- Foto: Licht-Zeile in neuer Reihenfolge, „Auto Blitz" bleibt markiert (auch nach ISO manuell),
  Mond-Chip rechts mit/ohne „A", HDR/DRO-Chip links, Timer-Chip zykelt, Akku weg, keine
  Icon-Reihe mehr unter dem Band.
- Video (Video/Zeitlupe/Zeitraffer): drei Blitz-Knöpfe, „Automatisch" bleibt.
- Panorama 1× und Weitwinkel: Objektivzeile „Automatisch/Weitwinkel", keine Zoom-Pille mehr,
  Blase im Stepper-Stil, immer genau ein Licht-Knopf markiert, Weitwinkel ↔ Nacht schließen
  sich aus, Verhältnis bleibt beim Nachtbild erhalten.
- Gegenprobe: Porträt/HDR (keine Licht-Zeile), Frontkamera (Selfie-Licht), Pro-Panel.

## Ergebnis (Gerätetest Pixel 9 Pro, 2026-09-22)

Alle 14 Punkte geprüft und in Ordnung; drei Befunde kamen beim Testen dazu bzw. wurden geklärt:

- **(12) Ursache gefunden:** nicht `panoramaResolution()` war schuld, sondern der Rückfall für
  Blitzserie/Erweiterung in `Preview.setupCameraParameters()` – er nahm die größte passende Größe
  **ohne** das Verhältnis zu beachten (9 MP 16:9 → 5 MP 4:3), obwohl die Nacht-Erweiterung 16:9
  sehr wohl anbietet: 2688×1512 und 2560×1280 (Log: „picture size supports extension"). Fix an der
  gemeinsamen Stelle: `Preview.pickSizeForRequirements()` hält das Verhältnis, erst ohne passende
  Größe fällt es auf die größte überhaupt zurück. Das Panorama steht in der Nacht jetzt auf 4 MP
  16:9, die gespeicherte 9 MP bleiben und greifen ohne Nacht wieder.
- **(14) bleibt unmöglich:** die Nacht-Erweiterung läuft nur auf der Hauptkamera – „Nacht an/auto"
  setzt das Objektiv deshalb auf 1×, der Mond ist im Weitwinkel-Panorama nicht zu haben. Ein
  echter Nacht-Weitwinkel ginge nur über `CONTROL_ZOOM_RATIO` in der Erweiterungssitzung
  (`postProcZoom` des Pixels); eigener Schritt, wenn Falk ihn will.
- **Neu beim Testen gefunden:** der Mond stand auch im Video im Band, obwohl die Nacht dort nicht
  läuft (`getPhotoMode()` antwortet im Video nie X_Night) – `OnScreenIcons.nightIcon()` und
  `MainUI.checkNightAuto()` sind im Video jetzt still, der Platz zeigt dort wieder den Blitz.
