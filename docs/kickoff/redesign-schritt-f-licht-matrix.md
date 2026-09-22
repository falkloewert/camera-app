# Kickoff Schritt F: Licht-Matrix, Nacht im Panorama, Panorama-Bildzahl

Session-Titel `camera-app – Schritt F Licht-Matrix/Nacht-Panorama`. Baut auf dem unfertigen
Stand von Schritt E auf (Licht-Zeile mit fünf Knöpfen, Panorama-Größen, Fotografieren während
des Stitchs) – beide Schritte gehen zusammen in einen Commit und einen Gerätetest.

## Soll

1. **Blitz-Knöpfe dürfen nicht verschwinden**, wenn Nacht gewählt ist (Falk: „man soll ja
   wechseln können"). Sie hängen nicht mehr an der gefilterten Blitzliste der laufenden
   Erweiterungssitzung, sondern an der Hardware.
2. **Nacht ist ein Zustand, kein Modus.** Nur so kann Nacht im Panorama existieren, ohne den
   Panorama-Modus zu verdrängen (bisher schrieb „Nacht an" `preference_photo_mode_x_night` und
   warf damit Panorama weg).
3. **Nacht im Panorama** nimmt jedes Einzelbild durch die Nacht-Erweiterungssitzung auf.
4. **Laderad**: bei jeder Nacht-Aufnahme zeigt ein Fortschrittsring, wie lange still zu halten
   ist (Wert kommt aus `onExtensionProgress`, 0..100). Im Panorama wird währenddessen der blaue
   Zielpunkt ausgeblendet – der Ring sagt „noch nicht drehen".
5. **Panorama endet nicht mehr nach 4-5 Bildern**: die Bildzahl bleibt fest 10, stattdessen wird
   die Stich-Auflösung an den Heap angepasst (`inSampleSize` in `ImageSaver`).

## Kombinationsmatrix

| Modus | Blitz | Nacht | Zeile zeigt |
|---|---|---|---|
| Foto | aus/auto/an | aus/auto/an (Sitzung) | alle fünf; Nacht an ⇒ Blitz wirkt nicht, Wert bleibt gemerkt |
| Panorama | aus/auto/an (pro Bild) | aus/auto/an, pro Bild | alle fünf; Nacht ⇒ Blitz aus (Plattform erzwingt es) |
| DRO | aus/auto/an | nein | Keines · Blitz auto · Blitz an |
| HDR | nein | nein | – (Zeile entfällt) |
| Porträt | nein (Erweiterungssitzung) | nein (zwei Sitzungen) | – (Zeile entfällt) |
| Video | unverändert | nein | unverändert |

Begründungen im Code geprüft: HDR ist ein 3er-Bracket und setzt `CONTROL_AE_MODE = AE_MODE_OFF`
(CameraController2.java:6615) ⇒ echter Blitz unmöglich, nur Taschenlampe. DRO ist ein Einzelbild
⇒ Blitz wirkt. Porträt und Nacht sind beide Erweiterungssitzungen ⇒ nur eine zur Zeit. Die
Nacht-Sitzung lässt der Plattform wegen nur `flash_off` zu (Preview.java:2184-2198).

## Umsetzung (Stand Code, 2026-09-22)

`getPhotoMode()` (MyApplicationInterface.java:1940) entscheidet der Reihe nach: DRO/HDR/… ,
Panorama, die übrigen Erweiterungsmodi, **zuletzt** der Nacht-Zustand ⇒ `X_Night` nur, wenn der
Modus darunter das gewöhnliche Foto ist. Alles darüber gewinnt, damit ein gemerkter Nacht-Zustand
Porträt/HDR/DRO nicht übernimmt. `getTopLevelPhotoMode()` bildet `X_Night` → `Standard` für
Moduskarussell, Blatt und Nacht-Automatik.

| Kombination | Sitzung (`isCameraExtensionPref`) | Stapel-Typ | Modusname (Karussell/Blatt) | Dateiname |
|---|---|---|---|---|
| Foto | nein (Standard) | NORMAL | Normal | `IMG_…` |
| Foto + Nacht | NIGHT | X_NIGHT | Normal | `…_Night` |
| Panorama | nein | PANORAMA | Panorama | `IMG_…` |
| Panorama + Nacht | NIGHT | PANORAMA | Panorama | `…_Night` |
| DRO / HDR | nein | NORMAL / HDR | DRO / HDR | wie bisher |
| Porträt / Auto | BOKEH / AUTOMATIC | NORMAL | Porträt / Auto | wie bisher |
| Video (alle) | nein | – | Video | wie bisher |

- `isCameraExtensionPref()`/`getCameraExtensionPref()` fragen den **aufgelösten Modus**, nicht den
  Nacht-Zustand: nur `X_Night` und `Panorama` sind dann eine Nacht-Sitzung. Ein Moduswechsel
  schaltet die Nacht für alles außer Foto und Panorama ab (`MainUI.switchPhotoMode()`), sonst
  bliebe ein unsichtbarer Nacht-Zustand stehen (`Porträt` zeigt keine Licht-Zeile).
- Der Nacht-Zustand wird als `preference_night_on` geführt; ein vorhandenes
  `preference_photo_mode_x_night` wird beim Start einmalig umgeschrieben
  (`MyApplicationInterface.migrateNightMode()`).
- `canTakeNewPhoto()` reserviert weiter für 10 Bilder (`computeRequestCost(false, 10)`), die
  Verarbeitung lädt sie kleiner (`ImageSaver.computePanoramaSampleSize()`, Heap/2 als Budget,
  Zweierpotenz, min. 1) – `panoramaPicsLimit()`/`MIN_PANORAMA_PICS` sind entfallen.
- Der `_Night`-Zusatz im Panorama wird in `ImageSaver.saveImages()` aus dem laufenden Zustand
  gelesen (kein neues Feld in `Request`); `using_camera_extensions` im Stapel ist dort `true`.
- Ring: `MyApplicationInterface.onExtensionProgress()` → `DrawPreview.setNightProgress()`; der
  blaue Panorama-Zielpunkt bleibt während einer Nacht-Aufnahme weg.
- RAW bleibt in beiden Nacht-Fällen aus (`isRawAllowed(Panorama/X_Night)` = false), die
  JPEG-Qualität für `X_Night` wie bisher 100.

## Entscheidungen Falk (2026-09-22)

- HDR: keine Lichtwahl, Blitz wird auf Keines gestellt. DRO: Blitz-Knöpfe bleiben, Nacht weg.
- Laderad bei jeder Nacht-Aufnahme (auch Einzelfoto); der Prozent-Toast entfällt dafür.
- Nacht ist kein Modus mehr, sondern Teil der Licht-Wahl (wie schon im Schritt-E-Kickoff notiert).

## Abhängigkeiten, die mitgeprüft wurden

- Erweiterungssitzung schaltet Gesichtserkennung, RAW, Zoom (falls die Erweiterung ihn nicht
  kann) und manuelles ISO ab (MyApplicationInterface.java:746, :2105, :1999; Preview.java:2940).
  Fürs Nacht-Panorama ist davon nur RAW relevant – Panorama ist ohnehin JPEG.
- Der Panorama-Einzelbildpfad ist ein Einzelbild ohne `takePictureBurst*` und ohne
  `BLOCK_FOR_EXTENSIONS` ⇒ er läuft in einer Erweiterungssitzung durch
  (CameraController2.java:6372-6378, :6435-6438).
- Jeder Nacht-Wechsel baut die Kamera neu auf (MainActivity.java:3350-3366) ⇒ die
  Nacht-Automatik darf nie während eines laufenden Panoramas umschalten, sonst reißt der
  Neuaufbau das Panorama ab.
- Die Panorama-Aufnahme-Kosten in `canTakeNewPhoto()` bleiben bei 10 Frames (Speicherreservierung
  des Stapels), die Verarbeitung rechnet kleiner (siehe Bildzahl).
- Offen für den Gerätetest: ob der Nacht-Sucher im Panorama flüssig genug ist und ob ein Frame
  in der Erweiterungssitzung wirklich ankommt. Fällt es aus, bleibt Nacht im Panorama still aus
  (normale Frames, kein Laderad) und der Befund kommt hierher.
- **Offen, mitzunehmen in denselben Gerätetest (Falk 2026-09-22):** wie viele Einzelbilder ein
  Panorama wirklich braucht. Heute fest 10 (`max_panorama_pics_c`). Die Zahl steuert die
  Schwenkweite, nicht die Schärfe – weniger Bilder heißt kürzeres Panorama und mehr Speicher
  pro Bild. Je ein Panorama bei 1× und bei 0,5× aufnehmen und die Nahtstellen ansehen; danach
  entscheidet Falk, ob die Konstante sinkt. Steht auch in SPEC 4.5 („Am 2026-09-22 notiert").

## Abschluss (nach Falks Gerätetest)

SPEC (Schritt D erledigt, Abschnitte E und F), ui-spezifikation (Licht-Zeile je Modus, Laderad,
Panorama-Bildzahl), Changelog ≤ 10 Zeilen für E und F zusammen, `graphify update .`, ein Commit
mit Dateipfaden + Push, drei Zeilen für den PM-Chat.
