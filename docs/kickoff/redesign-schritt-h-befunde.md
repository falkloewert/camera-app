# Kickoff Schritt H: Falks neun Befunde nach D–G

Session-Titel `camera-app – Schritt H Befunde`. Neun Punkte, in dieser Reihenfolge nach Wirkung
(erst das Blatt, das Falk täglich anfasst; dann das Kopfband; dann Panorama und Video). Alle in
einem Zug, ein Gerätetest am Pixel 9 Pro, ein Commit am Ende.

## A – Blatt (Licht-Zeile)

1. **(H7, Panorama 0,5× + „auto nacht")** Drei Symptome, vermutlich eine Ursache: die Wahl der
   Nacht nimmt die Objektiv-Festlegung zurück (`MainUI.applyLightSettings`), der Neuaufbau fragt
   aber `MainActivity.supportsCameraExtension(NIGHT)` – die Charakteristiken holt `CameraController2`
   nur **ohne** physisches Objektiv, während des Aufbaus antwortet sie also „nein", und
   `getCameraIdSPhysicalPref()` ist gerade genullt. Folge: `night_possible` kippt auf false →
   „auto nacht"/„nacht" **verschwinden**, die Markierung fällt auf den Blitz zurück
   („es wird in auto blitz gewechselt"), das Panorama steht auf 1×, während die Objektiv-Zeile
   noch 0,5× markiert. Fix: `night_possible` zusätzlich aus dem **Zustand** speisen
   (`night_auto`/`night_on` in den Preferences) – ein gesetzter Zustand hält seine Knöpfe.
2. **(H2, Markierung springt)** dieselbe Stelle: die Markierung hängt an
   `Preview.getCurrentFlashValue()`, das während des Neuaufbaus noch den alten Sitzungswert
   liefert. Beim Tippen springt sie deshalb kurz auf „nichts"/den Blitz und erst danach auf den
   gewählten Knopf. Fix: die Markierung aus den Preferences ableiten (wie Nacht), Sitzungswerte
   nur als Rückfall. Am Gerät nachstellen, ehe fixiert wird.
3. **(H5, Mond im Blatt gefüllt)** „nacht an" zeigt `ms_dark_mode` weiß gefüllt; Falk will die
   **Mond-Kontur**. Neues `ms_moon_outline.xml` (derselbe Pfad, `strokeColor` statt `fillColor`),
   nur im Blatt tauschen.

## B – Kopfband (Chips)

4. **(H3, weißer Kasten)** Alle Chips im Band bekommen den weißen Kasten mit runden Ecken wie der
   Auflösungs-Chip „MP" (weiße Fläche, dunkles Zeichen). Ein gemeinsames Hintergrund-Drawable,
   die Glyphen werden dunkel getönt – heute sind sie weiß auf dem Sucher. Gilt für Modus-,
   Timer-, Blitz-/Mond-, RAW-, GPS-, Sperr-, Stempel-, Gesichts-, Auto-Ausricht- und Ton-Chip.
5. **(H1, Timer-Chip)** Er zeigt immer nur „Aus": die Glyphen `ms_timer_3/5/10` existieren,
   `updateCycleTimerIcon()`/`clickedCycleTimer()` sind zu prüfen – Verdacht: der Chip wird nach
   dem Tippen nicht neu gezeichnet oder `TIMER_ICONS` läuft an der View vorbei. Am Gerät
   nachstellen (Screenshot je Tipp).
6. **(H4, RAW-Chip)** Es gibt noch keinen: der Chip soll **nur** bei laufendem RAW erscheinen,
   sonst gar nicht (heute hängt er an `showIndicator(R.id.cycle_raw)`, das zu prüfen ist).

## C – Panorama

7. **(H6, Zoom-Blase)** Die Zoom-Pille bleibt stehen, solange das Pro-Panel offen ist. Fix in
   `MainUI.showGUI`/`setZoomStepperVisibility`: im Panorama immer `GONE` (dort führt die
   Panorama-Objektiv-Zeile), und/oder beim Öffnen des Pro-Panels ausblenden.

## D – Video

8. **(H8, Auto-Blitz bei Dunkelheit)** In Zeitraffer und Zeitlupe schaltet der automatische
   Blitz/die Taschenlampe bei Dunkelheit nicht ein (in Video schon). Die ISO-Auswertung
   (`MainUI.checkNightAuto`, Video-Zweig der Blitz-Automatik) hängt vermutlich am normalen
   Video-Pfad; die beiden Highspeed-Modi bauen die Vorschau anders auf.
9. **(H9, Video-Pro-Panel)** Unten rechts fehlt das Pro-Panel im Video; die native Referenz hat
   dort nur **Belichtung** und **Weißabgleich**. Aus dem Foto-Panel ableiten
   (`ui/ProPanel`, `ui/RulerSlider`), in **allen** Video-Modi (Video, Zeitlupe, Zeitraffer,
   Highspeed).

## Gerätetest (ein Durchgang, `de.falkloewert.cameraapp`)

Foto: Licht-Zeile tippen (jeder Knopf, Markierung bleibt stehen), Mond-Kontur; Band: weiße Chips,
Timer-Chip zykelt mit den vier Zeichen, RAW-Chip nur bei RAW. Panorama: 0,5× + „auto nacht"
bleibt bei genau einer Markierung, Knöpfe bleiben, Zoom-Blase unter dem Pro-Panel weg.
Zeitraffer/Zeitlupe im Dunkeln: Taschenlampe kommt. Video: Pro-Panel unten rechts mit Belichtung
und Weißabgleich.

## Ergebnis (2026-09-22)

**Am Gerät bestätigt:** H2 (Markierung steht bei jedem Tipp fest, über vier Abtastungen in 2 s),
H3 (weißer Kasten, dunkle Zeichen), H4 (RAW-Chip erscheint/schwindet mit dem RAW-Zustand),
H5 (Mond-Kontur im Blatt), H9 (Video-Panel: nur Belichtung und Weißabgleich, EV-Maßband und
**Weißabgleich 4000–6000 K** im Video bedienbar, auch während einer Aufnahme).
H1 war **nicht nachstellbar**: der Chip zykelt am Gerät Aus → 3 → 5 → 10 → Aus und die Preference
folgt. Gefunden und behoben wurde der Nachbarfall: eine Timer-Änderung **im Blatt** zeichnete den
Chip nicht neu (jetzt `updateOnScreenIcons()` nach dem Speichern).

**Geändert:** `SettingsSheet` (Licht-Zeile: `night_possible` aus dem Zustand, Markierung aus
`getFlashPref()`, Timer-Chip nachziehen), `OnScreenIcons` (RAW-Chip am Zustand), `MainUI`
(Zoom-Blase hinter dem Panel, Pro-Knopf im Video, Stärke-Knopf links daneben), `ZoomStepper`
(Blase bleibt weg, solange das Panel offen ist), `ProPanel` (`tabOffered()` + `buildTabs()` beim
Öffnen), `activity_main.xml`/`header_chip_background_icon.xml`/`ms_moon_outline.xml`.

**Zwei Nebenfunde, mitbehoben:** (1) Kamera-Neustart **stürzte ab**, wenn die Zeitlupe gespeichert
war und die Kamera im Foto-Modus aufgebaut wurde: `getMaxSupportedVideoSizeHighSpeed()` lief in
eine NPE, weil die High-Speed-Größen im Foto-Modus nicht abgefragt sind (`VideoQualityHandler`).
(2) Der Stärke-Knopf von Zeitraffer/Zeitlupe lag über der Reiter-Reihe des offenen Panels; er steht
jetzt links neben dem Pro-Knopf und weicht dem Panel.

**Offen:** H6 nur im Code belegt (im Video geprüft, dass die Blase unter dem Panel nicht
zurückkommt; der Panorama-Fall ist noch nicht am Gerät nachgestellt). **H8 ist ungeklärt** – im
Torch-Pfad ließ sich kein Unterschied zwischen gewöhnlichem Video und Zeitraffer/Zeitlupe finden;
im Verdacht steht der Blitz-Filter bei manuellem ISO (`Preview` ~2979: er streicht `flash_auto`,
und bei Highspeed wird das manuelle ISO erst *danach* abgeschaltet ~3337), das erklärt aber nur
den Fall mit gespeichertem manuellem ISO – Falks Preferences stehen auf Auto. Braucht einen
Geräteversuch im Dunkeln mit eingeschaltetem Log.
