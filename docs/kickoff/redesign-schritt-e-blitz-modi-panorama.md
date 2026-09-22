# Kickoff: Schritt E – Blitz-Zeile, HDR/DRO ins Blatt, Panorama-Größen, Fotografieren beim Stich

Falks Wünsche vom 2026-09-22, direkt nach dem Bau von Schritt D (Karussell, noch nicht am Gerät
abgenommen). Arbeitsstand `master` `58e259a3` plus Schritt D im Baum (`ui/ModeBar.java` neu).
Session-Titel: `camera-app – Schritt E Blitz/Modi/Panorama`. Maße 2,25 px/dp (Pixel 9 Pro).

## 1. Soll (Falk 2026-09-22)

- **Blitz-Zeile fünf Knöpfe**: `Keines · Nacht auto · Nacht an · Blitz auto · Blitz an`. Nacht ist
  kein Modus mehr, sondern Teil dieser Wahl. „Nacht auto“ = die Nacht-Erweiterung schaltet sich
  bei Dunkelheit selbst ein und bei Helligkeit wieder aus.
- **HDR und DRO sind keine Modi mehr**: drei Knöpfe `Normal · HDR · DRO` im **Pro-Reiter**.
- **Nachtsicht verlässt das Karussell** → `Foto · Panorama · Porträt` (Porträt nur, wenn die
  Kamera die Erweiterung meldet).
- **Panorama**: Verhältnis und Größe sind zwei unabhängige Knöpfe (4:3/16:9 × „größte“/„FHD“),
  echtes 4K wird bewusst mitgetestet. Die harte 4:3-Regel und die 2080er Grenze fallen.
- **Während das Panorama gerechnet wird, weiter fotografieren** (ein zweites Panorama nicht).

## 2. Inventar (Faulheits-Leiter: was gibt es schon?)

- Blatt-Zeilen sind reines Java (`ui/SettingsSheet`): `Row`/`Option` `:568-598`, `createRow()`
  `:601`, Markierung `:678`. Vorlage für die Blitz-Zeile ist die heutige Blitz-Zeile
  (`photoGeneralRows()` `:777-811`, ruft `preview.updateFlashQuiet()`), für HDR/DRO die Timer-Zeile
  `:813-836` (Preference + Knöpfe).
- Moduswechsel: `MainUI.switchPhotoMode()` `:1075` schreibt `PhotoModePreferenceKey`, leert den
  Cache in `DrawPreview` und ruft `updateForSettings()` – die eine Stelle, die das Blatt mitbenutzt.
- Nacht: `PhotoModePreferenceKey = preference_photo_mode_x_night`, `getPhotoMode()` `:1851`,
  Erweiterung in `Preview.setupCamera()` `:2184`. Automatik-Messung: der ISO-Weg der
  Video-Taschenlampe (`Preview.java:5266` Schwellen 800/200, `updateTorchAuto()` `:5283`,
  `CameraController.setISOChangeCallback` `:744`).
- Panorama: `choosePanoramaResolution()` `MyApplicationInterface.java:539` (4:3 hart bei `:549`,
  Grenze 2080 bei `:542`), Verhältnis-Zeile im Blatt ausgeblendet (`SettingsSheet.java:842`),
  Auflösungs-Zeile ohne Panorama-Schutz; Helfer `standardSize()`/`savingSize()`/`aspectSize()`
  und `STANDARD_PICTURE_WIDTH`/`SAVING_PICTURE_WIDTH` gibt es schon.
- Auslöser-Sperre: `canTakeNewPhoto()` `:1545-1551` (Panorama verlangt leere Schlange) und
  `ImageSaver.addRequest()` `:823` (Stapel belegt zehn Plätze).

## 3. Bauen und Fallstricke

- Kein neues Review, keine neuen Farben/Schriften. Fünf 40-dp-Knöpfe passen nicht neben eine
  Wertzeile → die Blitz-Zeile bekommt **keine Wertzeile**; am Gerät ansehen.
- `Nacht auto` baut die Kamera beim Umschalten neu auf: Hysterese, Mindestabstand, nie während
  einer Aufnahme. Rückweg nach hell nur, wenn die Erweiterungssitzung noch ISO-Werte liefert
  (`ponytail:`-Marker im Code, am Gerät prüfen).
- Panorama: Speicherwächter statt Absturz (erst Bildzahl senken, dann Größe), `OutOfMemoryError`
  abfangen; `UnitTest.testPanoramaResolutions()` `:447-516` mitziehen.
- Der Auslöser wird nur entsperrt, wenn die Warteschlange Platz hat – die Speicherreservierung
  des Panorama-Stapels bleibt bestehen.

## 4. Abschluss (nach Falks Gerätetest)

Schritt D mit abschließen: `docs/SPEC.md` (4.5 Schritt D erledigt, neuer 4.x-Abschnitt für E,
Übersicht 3), `ui-spezifikation.md` 7.2 + 7.1 Punkt 3, `docs/changelog.md` ≤ 10 Zeilen,
`graphify update .`, Commit mit Dateipfaden + Push, drei Zeilen für den PM-Chat.
