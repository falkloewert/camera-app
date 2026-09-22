# Kamera-App Projekt – Spec & Roadmap

> Master-Quelle. **Feature-Chats lesen nur 2.1 und den Abschnitt ihres Features (4.x)**,
> PM-Chats alles. Historie: `docs/changelog.md`. Kurz halten – jeder Token zählt.

---

## 1. Projektübersicht

**Was:** Eine eigene Android-Kamera-App, basierend auf einem Fork von [Open Camera](https://opencamera.org.uk/).

**Warum:** Unzufriedenheit mit bestehenden Kamera-Apps – Kombination aus Wunsch-Features,
die es sonst nirgends zusammen gibt.

**Für wen:** Aktuell nur für den persönlichen Gebrauch. Kein Release, kein Produkt geplant.
Multi-Geräte-Support ist ein mögliches, aber unbestimmtes Zukunftsthema.

**Entwicklungsansatz:** Claude Code baut, Falk testet am Gerät. Falk hat keine
Programmiererfahrung. **Seit 2026-09-18: keine Erklärungen, keine Narration, keine
Zusammenfassungen – nur Ergebnis/Frage. Pro-Kontingent, jeder Token zählt.**

**Zielgerät:** Google Pixel 9 Pro
**Ziel-Android-Version:** Android 17 (API Level 37, stable seit 16. Juni 2026)

**Hosting:** Codeberg-Repository (`codeberg.org/falkloewert/camera-app`, privat), sauber dokumentiert.

**Basis-Repo (Upstream):** Open Camera, offizielles Quell-Repo auf SourceForge:
`https://git.code.sf.net/p/opencamera/code` (Branch `master`, Lizenz GPLv3). Im lokalen
Klon als Git-Remote `upstream` eingerichtet, um später Änderungen vom Original übernehmen
zu können (`git fetch upstream`).

**Git:** Commits/Pushes macht Claude (nur mit expliziter Dateiangabe, siehe `CLAUDE.md`).

---

## 2. Workflow / Projektstruktur

- Ein Chat pro Feature, gestartet mit einem Kickoff aus `docs/kickoff/`. Ein
  PM-Chat plant, pflegt dieses Dokument, schreibt Kickoffs. Modell: Sonnet (Opus nur bei
  nachgewiesenem Scheitern).

### 2.1 Rolle & Abschlussprotokoll für Feature-Chats

- **Rolle:** nur das zugewiesene Feature; keine Entscheidungen über Reihenfolge/andere
  Features (→ PM-Liste in 4.5).
- **Session-Titel:** `set_session_title` (`session_id: "self"`) → `camera-app – <Name>`.
- **Vorgehen:** Kickoff lesen (nur die dort genannten Abschnitte), bauen, Falk am Pixel
  9 Pro testen lassen (gebündelt, nicht nach jedem Handgriff), committen. Keine
  Erklärungen, keine Zwischenstände.
- **Nur eine Sitzung gleichzeitig im Repo:** vor Codeänderungen `git status` + `git fetch`;
  fremde unfertige Dateien nicht anfassen. App-weite Themen nicht selbst entscheiden,
  sondern in die PM-Liste (4.5) eintragen.
- **Abschlussprotokoll**, sobald das Feature fertig und von Falk bestätigt ist:
  1. `docs/SPEC.md` 4.5 (Stand, PM-Liste) + `docs/changelog.md` (Eintrag ≤ 10 Zeilen) +
     die im Kickoff genannten Abschnitte der `ui-spezifikation.md`.
  2. `graphify update .`, Commit (mit Dateipfaden) + Push.
  3. Drei Zeilen für den PM-Chat. Kein nächstes Feature beginnen.

---

## 3. Feature-Übersicht (Status)

| # | Feature | Status | Komplexität |
|---|---------|--------|--------------|
| 1 | 360°-Foto-Stitching + interaktive Ansicht | Idee / Konzept | Hoch |
| 2 | Filmchen-Modus (Snapchat-Style Clip-Tagebuch) | Idee / Konzept | Mittel-Hoch |
| 3 | Dual-Kamera (Selfie-Bubble, unabhängiger Zoom) | Idee / Konzept | Hoch (geräteabhängig) |
| 4 | Panorama mit Weitwinkel-Option | **Fertig** | Niedrig-Mittel |
| 5 | Kamera während Videoaufnahme umdrehen | Idee / Notiz (nice-to-have) | Niedrig |
| 7 | Zeitraffer & Zeitlupe (Video-Modi wie in der Pixel-App, siehe 4.6) | **Fertig** (2026-09-21) | Mittel |
| 8 | Doppeltippen auf den Sucher wechselt die Kamera (Selfie ↔ hinten) | Idee (2026-09-21) | Niedrig |
| 9 | Serienbilder als Stapel in der Galerie statt einzelner Bilder | Idee (2026-09-21, Recherche nötig) | Mittel |
| 6 | Material-3-Redesign (komplettes UI, absorbiert die alte Feature-6-Idee "Zoom-Stufen-Buttons + Moduswahl-Leiste") | **In Arbeit** – Theme/ConstraintLayout, Zoom-Stufen, Moduswahl, Auslöser-Zeile, Pixel-Drehmodell (Hochformat-Sperre, Glyphen drehen, Datei-Prüfung abgeschlossen), **Feinjustier-Rad** (2026-09-18), Einstellungen-Blätter + Pro-Panel (2026-09-21) und **Kopfzeile; alte Icon-Leiste + Drei-Punkte-Popup entfernt** (Schritt C, 2026-09-22) gebaut; offen: Rest `ui-spezifikation.md` Abschnitt 7 (Punkt 3 Dock, Karussell, Sucher-Overlays, Farben/Blur/Schrift) | Hoch (app-weit, Design-Fundament für alle Folge-Features) |

### 3.1 Vereinbarte Reihenfolge (aktualisiert 2026-09-14: Material-3-Redesign vor Feature 5 vorgezogen)

Kompromiss aus "Einfaches zuerst" (zum Kennenlernen von Workflow/Codebasis als
Programmier-Einsteiger) und "Riskantes früh klären" (Dual-Kamera hängt von
Tensor-Chip-Unterstützung ab, siehe Abschnitt 5) — mit einer Ausnahme: das
Design-Fundament kommt vor allem UI-tragenden Folgefeatures, damit die nicht später
nochmal überarbeitet werden müssen (Falks Begründung, von Claude bestätigt: siehe 4.5).

1. **Feature 4** – Panorama mit Weitwinkel-Option — **Fertig**
2. **Feature 6** – Material-3-Redesign (siehe 4.5)
3. **Feature 5** – Kamera während Videoaufnahme umdrehen
4. **Machbarkeits-Spike Dual-Kamera** – kein volles Feature, nur klären: unterstützt
   der Pixel 9 Pro zuverlässig gleichzeitigen Zugriff auf Front- und Rückkamera
   ("Concurrent Camera")?
5. **Feature 2** – Filmchen-Modus
6. **Feature 3** – Dual-Kamera (voller Ausbau, nach positivem Spike-Ergebnis)
7. **Feature 1** – 360°-Foto-Stitching (inkl. Immich-Kompatibilität, siehe 4.1)
8. **Feature 7** – Zeitraffer & Zeitlupe (siehe 4.6) — **fertig 2026-09-21**, von Falk
   vorgezogen; das Redesign (Feature 6) läuft daneben weiter (Schritt C noch offen).
9. **Feature 8 und 9** (neu 2026-09-21, aus der Schritt-C-Abstimmung) – noch nicht
   eingeordnet: Feature 8 ist klein (Doppeltippen ist heute anders belegt, siehe Kickoff
   `redesign-kopfzeile.md` Abschnitt 6.1), Feature 9 braucht vorher eine Recherche, woran
   Galerie und Immich einen Serienbild-Stapel erkennen.

---

## 4. Feature-Details

### 4.1 360°-Foto-Stitching (Photo Sphere)

**Ziel:** Echtes 360°-Kugelpanorama wie beim alten Pixel 6/7 Photo Sphere – Feature,
das bei Pixel 9 nicht mehr existiert.

**Anforderungen:**
- Echtes Stitching mehrerer Aufnahmen zu einer 360°-Kugel (kein simples Weitwinkel-Panorama)
- Interaktive Betrachtung in der App (wischen/drehen wie bei Google Street View / altem Photo Sphere)
- **Immich-Kompatibilität:** Falk nutzt Immich (self-hosted auf FalksNAS, siehe
  loewert-lab) als Google-Photos-Ersatz für seine gesamte Foto-/Videobibliothek. Normale
  Fotos/Videos aus der App landen automatisch dort (Standardformate JPEG/MP4, kein
  Sonderfall). Für 360°-Bilder speziell hat Falk aktuell ein Problem: Immich zeigt
  360°-Bilder bei ihm nicht korrekt als interaktive Kugel an. Das gespeicherte
  360°-Format muss deshalb so gewählt werden, dass Immich es erkennt und richtig
  rendert – nicht nur der eigene In-App-Viewer.

**Offene Fragen (für Feature-Chat zu klären):**
- Aufnahme-Führung: Wie wird der Nutzer beim Abfotografieren der Kugel geführt (Gitter/Overlay)?
- Welche Stitching-Bibliothek/-Methode kommt zum Einsatz (on-device, ressourcenschonend)?
- Speicherformat für 360°-Bilder (z. B. equirectangular JPEG mit XMP-GPano-Metadaten,
  dem von Google Photo Sphere etablierten Standard) – muss zusätzlich konkret gegen
  Immichs 360°-Anzeige getestet werden, da dort aktuell ein bekanntes Anzeigeproblem
  besteht (Ursache noch nicht untersucht: fehlende/falsche XMP-Metadaten vs. Immich-Bug –
  das gehört in die Recherche für den 360°-Feature-Chat, nicht in dieses Setup-Gespräch).
- Viewer: eigene Implementierung oder bestehende Bibliothek nutzen?

**Komplexität/Risiko:** Hoch – eigenständiges technisches Teilprojekt, ressourcenintensiv.

---

### 4.2 Filmchen-Modus (Snapchat-artiges Clip-Tagebuch)

**Ziel:** Über den Tag verteilt (oder länger) immer wieder kurze Videoclips zu einem
Projekt hinzufügen können, mit einfachem Nachbearbeiten am Ende.

**Kernanforderungen:**
- Modus lässt sich jederzeit öffnen/schließen, Fortschritt bleibt erhalten – auch wenn
  das Handy zwischendurch länger aus ist
- **Mehrere parallele Filmchen-Projekte** möglich – Nutzer wählt beim Öffnen aus, an
  welchem Projekt weitergearbeitet wird
- Klare Trennung von der normalen Kamera – normale Fotos/Videos landen nicht im Filmchen
- Clips bleiben **fest ihrem Projekt zugeordnet** (kein Verschieben zwischen Projekten, Stand jetzt)
- Einfacher Bearbeitungsmodus am Ende:
  - Clips umsortieren
  - Clips kürzen/trimmen
  - (weitere einfache Funktionen nach Bedarf, bewusst simpel gehalten – kein voller Editor)
- **30-Tage-Papierkorb** für gelöschte Projekte/Clips (Wiederherstellbarkeit)
- Zwei Export-Wege:
  1. **Finaler Export** als fertiges MP4 → Projektdatei/Rohdaten werden danach gelöscht (landen im Papierkorb)
  2. **Zwischenexport für externe Bearbeitung** (CapCut/InShot): Clips bleiben als einzelne
     Dateien erhalten, damit sie in externen Apps weiterverwendet werden können

**Technische Eckpunkte (Stand Planung):**
- Einzelne Clips werden als **MP4 (H.264, alternativ HEVC/H.265 für weniger Speicherbedarf)**
  gespeichert – Standardformat, kompatibel mit CapCut/InShot
- Projekt-Ebene (Reihenfolge, Trimm-Punkte, Zusammengehörigkeit der Clips) wird über eine
  **Manifest-Datei (JSON)** pro Projekt abgebildet – ähnlich einer einfachen Schnittliste
- **Wichtige Klarstellung zum Export für CapCut/InShot:** Diese Apps können keine fertigen
  Projektdateien importieren (kein offener Standard dafür). Der "Export zur Weiterbearbeitung"
  bedeutet praktisch: ein Ordner mit den einzelnen, bereits geschnittenen/sortierten
  MP4-Clip-Dateien in der richtigen Reihenfolge/Benennung, die man manuell in CapCut/InShot
  reinzieht. CapCut/InShot referenzieren dabei die Originaldateien nur (kein eigenständiges
  Kopieren) – die Clip-Dateien dürfen also nicht gelöscht werden, solange sie dort noch
  gebraucht werden ("Medium fehlt"-Problem sonst).
- Hintergrund-Persistenz (Projekt bleibt über Stunden/Tage pausierbar erhalten) erfordert
  sauberes Android-Lifecycle-Handling (Prozess kann vom System beendet werden, State muss
  robust auf Disk gespeichert werden, nicht nur im Arbeitsspeicher)

**Offene Fragen (für Feature-Chat zu klären):**
- Zeitlimit pro Clip oder pro Projekt/Tag? (aktuell: offen, kein Limit geplant)
- Darstellung der Projektübersicht beim Öffnen (Liste mit Vorschaubild, Name, Datum?)
- Genaue Umfang des Mini-Editors (welche Funktionen außer Sortieren/Kürzen sind sinnvoll?)
- Automatisches Löschen/Erinnerung nach X Tagen Inaktivität eines Projekts?

**Komplexität/Risiko:** Mittel-Hoch – vor allem wegen Persistenz/Lifecycle-Handling und
Projektverwaltung, weniger wegen der Videobearbeitung selbst.

---

### 4.3 Dual-Kamera-Modus (Snapchat-Style)

**Ziel:** Wie bei Snapchat – kleine runde "Selfie-Bubble" (Frontkamera) über dem
Hauptbild (Rückkamera), aber mit einer Verbesserung gegenüber Snapchat.

**Kernanforderung / Verbesserung gegenüber Snapchat:**
- **Unabhängiger Zoom je Kamera:** Tippt man auf die Selfie-Bubble und zoomt, wird nur
  die Frontkamera gezoomt. Tippt man auf das große Hauptbild und zoomt, wird nur die
  Rückkamera gezoomt. (Bei Snapchat aktuell nicht möglich – das ist der Hauptkritikpunkt,
  den die App lösen soll.)
- Muss nur auf dem eigenen Gerät (Pixel 9 Pro) zuverlässig laufen – kein
  Kompatibilitätsanspruch für andere Geräte in dieser Phase.

**Technische Eckpunkte (Stand Planung):**
- Erfordert gleichzeitigen Zugriff auf Front- und Rückkamera ("Concurrent Camera" API,
  seit Android 11 grundsätzlich möglich, aber geräteabhängig – muss für Pixel 9 Pro
  im Feature-Chat verifiziert werden)

**Nice-to-have, separat vom Dual-Cam-Feature:**
- Möglichkeit, während einer laufenden Videoaufnahme (im normalen Einzel-Kamera-Modus)
  zwischen Front- und Rückkamera umzuschalten

**Offene Fragen (für Feature-Chat zu klären):**
- Verifizieren: Unterstützt der Pixel 9 Pro (Tensor-Chip) Concurrent Camera zuverlässig?
- UI-Detail: Größe/Position der Bubble, verschiebbar oder fest?
- Speicherformat: Wird das Ergebnis als ein zusammengesetztes Bild/Video gespeichert,
  oder bleiben beide Streams theoretisch getrennt abrufbar?

**Komplexität/Risiko:** Hoch, stark geräte-/hardwareabhängig. Wichtigster Prüfpunkt
gleich zu Beginn des Feature-Chats.

---

### 4.4 Panorama mit Weitwinkel-Option — Status: Fertig (2026-08-27)

**Ziel:** Bei der normalen Pixel-Kamera ist im Panorama-Modus nur 1x-Zoom (Standardlinse)
wählbar, nicht die Weitwinkellinse. Das soll in der eigenen App möglich sein.

**Umgesetzt:**
- Im Panorama-Modus ersetzt ein Button-Paar ("1x" / berechneter Weitwinkel-Faktor, z. B.
  "0,5x") das normale Kamera-Wechsel-Icon (`switch_multi_camera`). Direktes Antippen
  wählt die Linse direkt, kein Umschalt-Zyklus/Dialog wie sonst in der App.
- Der Weitwinkel-Faktor wird nicht geraten, sondern aus dem tatsächlichen
  Sichtwinkel-Verhältnis der beiden physischen Linsen berechnet (`MainActivity.
  updatePanoramaLensCache()`), pro Gerät automatisch korrekt (auf dem Pixel 9 Pro: ~0,5x).
- Stufenloser Zoom bleibt im Panorama-Modus wie zuvor deaktiviert (bestehendes Verhalten,
  `MyApplicationInterface.allowZoom()` – der Stitching-Algorithmus unterstützt das nicht).
- Button-Paar folgt in Position/Ausrichtung dem Auslöser-Button (wie der Rest der App
  zwischen Hoch-/Querformat neu anordnet): im Hochformat nebeneinander über dem Auslöser
  zentriert, im Querformat übereinander links daneben – jeweils mit sichtbarem Abstand.
  Bewusste Entscheidung nach Vergleich mit einer Variante, bei der die Buttons unabhängig
  von der Ausrichtung an einer fixen Bildschirmstelle bleiben (im Querformat dann aber
  losgelöst vom Auslöser mitten im Bild) – Falk hat sich für die auslöser-relative
  Variante entschieden.

**Entscheidungen:**
- App-Name testweise auf "Falks Kamera" geändert (statt "Open Camera"), damit die eigene
  App beim Testen eindeutig von einer eventuell zusätzlich installierten offiziellen
  Open-Camera-App unterscheidbar ist. Siehe offene Frage unten.

**Offene Fragen für später:**
- Soll der App-Name dauerhaft "Falks Kamera" bleiben oder wieder zurück/auf einen anderen
  Namen geändert werden? Aktuell nur als Übergangslösung zur Testzeit gedacht.
- Zwei App-weite UI-Wünsche, die während dieses Feature-Chats aufkamen, aber bewusst
  nicht hier umgesetzt wurden, sind bereits als Feature 6 aufgenommen (siehe 4.5).

**Komplexität/Risiko:** Niedrig-Mittel – wie eingeschätzt.

---

### 4.5 Material-3-Redesign (absorbiert: Modernes Kamera-UI)

**Ziel:** 1:1-Nachbau der Pixel-Kamera-Oberfläche (Material 3, fest dunkel) auf
Open-Camera-Basis, plus Falks Zusatz-Features. Verbindliche Bau-Vorgabe:
`docs/design-reference/ui-spezifikation.md` (+ `pixel-camera-detailspec.md` für Zahlen,
`pixel-camera-screenshots.md`, Referenz-PNGs). Entstehung/Begründung: `docs/changelog.md`.

**Stand (2026-09-20):** Gebaut und am Pixel 9 Pro abgenommen: Material-3-Theme (fest
dunkel) + ConstraintLayout (Phase 1); Zoom-Stufen-Pille `,5 1× 2 5` (gemessen 48/192/44 dp)
+ Feinjustier-Rad (`ui/ZoomWheel`, Pinch zeigt Rad, alter Regler weg); Moduswahl
`Foto · Panorama` (statisch, kein Karussell); Auslöser-Zeile nach Messung
(`ui/ShutterButton`, Squircles, Galerie links, `ui/PhotoVideoSwitch`); Pixel-Drehmodell
(Fenster fest Hochformat, nur Glyphen drehen, `MainUI.rotateGlyphs()`); keine Info-Toasts.
**Neu (2026-09-20, am Gerät abgenommen 2026-09-21):** Einstellungen-Blätter Foto + Video
(`ui/SettingsSheet`, Hoch- und Querformat), linker Dock-Button, Wisch-Geste auf dem Sucher,
Objektivauswahl "Manuell" (Zoom-Pille zeigt `WW · NO · TE`).
**Foto-Blatt Runde 1 (2026-09-21):** Verhältnis-Sprung weg (Blatt-Neubau nur noch bei echter
Drehungsänderung über `refresh()`), Timer-Icons einheitlich (Stoppuhr mit Ziffer), Makro-Zeile
raus (Makro nur noch über den MA-Knopf), Auflösungs-Zeile: Rückkamera zwei Größen der
Verhältnis-Klasse in MP, Selfie ohne Auswahl, Video-Blatt Auflösung als MP-Wertzeile;
Selfie-Beleuchtung statt Frontblitz auch im Foto-Blatt. Punkt 5 (AF bleibt auf der gewählten
Linse, auch beim Tippen ins Bild) am Gerät mitgeloggt: die Linse bleibt gepinnt
(`MC probe`-Logs, 2026-09-21) – kein Code nötig.
**Nachtrag (2026-09-21):** 50 MP bleiben draußen (nur RAW10, kein JPEG auf allen Linsen).
Foto-Standard ist das native 12-MP-Raster (`CameraController.STANDARD_PICTURE_WIDTH` = 4032):
16:9 startet mit 4032x2268 (9 MP), 4:3 mit 4032x3024 (12 MP); die 4080er Vollauslesung bleibt
über die vollen Einstellungen erreichbar. Die manuelle Objektivwahl gilt nur im Foto – im Video
liefert `getCameraIdSPhysicalPref()` `null`, dort wählt die Kamera die Linse und die Zoom-Pille
zeigt wieder `,5 1× 2 5`.
**Objektivwahl (2026-09-21):** Der Makro-Knopf `MA` ist weg, die Pille zeigt `WW · NO · TE`.
Falk: „beide sollen Makro können" – die Knöpfe wählen jetzt nur noch die Linse: WW (Ultraweit,
bis 2 cm) und NO (Haupt, bis ca. 10 cm) kommen von selbst so nah heran, weil der kontinuierliche
Autofokus die ganze Fokusspanne der Linse abfährt. `CONTROL_AF_MODE_MACRO` ist raus: er stellt nur
einmal scharf und hält dann, womit beide Linsen am Gerät unscharf standen (Falk 2026-09-21).
`Preview.updateFocus()` korrigiert einen noch gespeicherten Makro-Wert auf den kontinuierlichen
Fokus (Foto: `continuous_picture`, Video: `continuous_video`). `ZoomStepper.setLensSelection()`
markiert nur noch über die physische ID, `macroSelected()` und `Lens.macro` entfallen.
**Blatt-Feinschliff (2026-09-21):** Kürzel-Badges (MP, Videoauflösung) tragen den inversen
Kürzel-Chip der Referenz – Kürzel in Knopf-Füllfarbe auf einem Chip in Glyph-Farbe, beides
aus den vier vorhandenen Farben (am Screenshot nachgemessen); alle Knöpfe sind 40 dp,
ausgewählt weißer Kreis, nicht ausgewählt dunkles abgerundetes Quadrat. Die fps-Knöpfe
zeigen die Zahl groß über der Referenz-Skala (drei Striche, ein breiter). Jede Wertzeile
mit Kürzel nennt die genaue Auflösung in Klammern (`settings_sheet_with_resolution`,
`9MP (4032x2268)`, `FHD (1920x1080)`); Selfie-Video behält FHD und nennt seine größte
Qualität `3K` (3264x1836) statt `6MP`. Video-Blitz „Autom." hängt am gemessenen ISO
(an ab 800, aus unter 200, Hysterese) statt am Lichtsensor. Startauflösung aller Modi ist
16:9, solange nichts gespeichert ist; eine gespeicherte Wahl gewinnt immer.
**Erledigt (2026-09-21):** rechter Dock-Button + Pro-Panel als Overlay über dem Sucher
(`ui/ProPanel`, `ui/RulerSlider`) – Regler für Belichtung, Weißabgleich, Fokus, ISO und
Verschlusszeit, je Reiter Automatik/Zurücksetzen, Fokus-Peaking; Maße aus dem Screenrecord
in `docs/design-reference/pixel-pro-panel-beschreibung.md`.
**Schritt C – Kopfzeile, alte Icon-Leiste und Popup (2026-09-22): Erledigt, am Pixel 9 Pro
abgenommen** (Kickoff `docs/kickoff/redesign-kopfzeile.md`). Kopfzeile: schwarzes Band 70,7 dp
über dem Sucher; darin links der Auflösungs-Chip auf der gemessenen Achse (Mitte 38,0 | 42,9 dp;
„FHD“ 21,3 × 16,0 dp, „4K“/„12 MP“ 17,8 × 17,8 dp, Radius 2 dp, Schrift 9 dp), im Video 4 dp
daneben der Bildraten-Chip, rechts auf derselben Achse die fünf Zustandsanzeigen (Blitz, RAW,
GPS, Ausrichtungssperre, Textstempel). Das „?“ entfällt; das Makro-Symbol der Referenz ebenfalls
– Makro ist hier eine Linse, kein Zustand (Falk 2026-09-22: „lieber gar keins“), seinen Platz
nimmt der Bildraten-Chip. Die Blitz-Anzeige steht immer, sobald das Gerät einen Blitz hat (keine
Modus- oder Preference-Sperre mehr, der Schalter „Blitz-Symbol anzeigen“ ist entfallen), die
übrigen vier bleiben Preference-gesteuert. Der Bildraten-Chip zeigt die **Aufnahmerate** (Zeitlupe
60/120 statt der 30 des fertigen Films, Quelle `Preview.getVideoProfile()`). Während einer
Aufnahme ist die ganze Zeile aus. Die alte Icon-Leiste und `ui/PopupView.java` (17 Einträge,
12 davon ohne Blattzeile) sind entfernt, die Funktionen nach Falks Regel verteilt: Doppeltes weg;
was in der vollen Einstellungen-Aktivität steht, bleibt dort (Raster, Szenen-Modus, Farb-Effekt,
Serienbild-Modus, Gesichtserkennung, Auto-Ausrichtung, Audio-Steuerung, Vorschau-Größe); das
Übrige in den Blättern bzw. im Pro-Panel (Blitz, Fokus-Modus, Auflösung, Video-Qualität, fps,
Timer, Weißabgleich); Belichtungs-/Weißabgleich-Sperre, Blende und Rauschunterdrückung gestrichen
(Kickoff 3); die Fotomodi (DRO/HDR/Nacht) bleiben bis zum Karussell unerreichbar (Kickoff 3.6).
Gemessene Werte und Abweichungen: `ui-spezifikation.md` 7.1 Punkt 4 + 7.2.

**Schritte D, E, F, G (2026-09-22): gebaut, am Pixel 9 Pro getestet, Falks Abnahme offen.**
D: Moduswahl als Karussell (`ui/ModeBar`, die zurückgestellten Fotomodi wieder drin).
E/F: Foto-Blatt mit Licht-Zeile in Falks Reihenfolge `nichts · auto nacht · auto blitz · nacht an ·
blitz an`, genau einer markiert, alle Schreibvorgänge über `MainUI.applyLightSettings()`; die Nacht
ist ein **Zustand** statt eines Modus (nur Foto und Panorama, Automatik nach gemessenem ISO), und
wer `auto blitz` wählt, verliert das manuelle ISO (es strich `flash_auto` aus den Kamerawerten –
Falks Befunde 1 und 5 haben dieselbe Ursache). Video-Blitz `aus/auto/torch` in allen Video-Modi.
Panorama: Objektivzeile „Automatisch/Weitwinkel" statt „Manuell", keine Zoom-Pille, Nacht und
festes Objektiv schließen sich aus. G räumt Falks 14 Befunde ab (Kickoff
`docs/kickoff/redesign-schritt-g-bugs-chips.md`): Akku-Anzeige und Sucher-Icon-Reihe entfallen,
Chips im Kopfband (Modus links neben dem Auflösungs-Chip, Gesicht/Ausrichten/Ton aus/Timer rechts,
Mond auf dem Blitz-Platz, Timer tippbar aus→3→5→10, im Video kein Mond), Nacht-Panorama behält das
Verhältnis (`Preview.pickSizeForRequirements()`; die Nacht-Erweiterung kann 16:9 nur bis
2688×1512, vorher wurde daraus 4:3). Nicht möglich: echte Nacht im Weitwinkel-Panorama – die
Nacht-Erweiterung läuft nur auf der Hauptkamera, der Mond setzt das Objektiv deshalb auf 1×.

**Offen:** Rest `ui-spezifikation.md` Abschnitt 7 (Punkt 3 Dock, Karussell 7.2, Sucher-Overlays
7.1 Punkte 6–9, Farben/Blur/Schrift 7.3) und die Punkte 13/14 unten.

**Am 2026-09-22 notiert, damit es nicht untergeht:**
- **Abnahme von D, E, F und G:** Schritt D (Karussell) wurde **nicht einzeln** am Gerät getestet –
  Falk nimmt D zusammen mit E, F und G in **einem** Durchgang ab (Entscheidung 2026-09-22); der
  Gerätetest dieser Runde lief am Pixel 9 Pro bereits durch (alle 14 Befunde geprüft), Falks
  eigenes Abnehmen steht aus. Erst danach gilt SPEC 2.1 als erfüllt und der Eintrag hier wird
  durch den Abschluss ersetzt.
  Kickoffs: `docs/kickoff/redesign-moduswahl-karussell.md`,
  `docs/kickoff/redesign-schritt-e-blitz-modi-panorama.md`,
  `docs/kickoff/redesign-schritt-f-licht-matrix.md`,
  `docs/kickoff/redesign-schritt-g-bugs-chips.md`.
- **Panorama-Bildzahl offen:** nach dem Gerätetest entscheiden, wie viele Einzelbilder ein
  Panorama wirklich braucht. Heute fest `max_panorama_pics_c = 10`
  (`MyApplicationInterface.java:101`). Die Zahl steuert die **Schwenkweite**, nicht die Schärfe;
  weniger Bilder heißt kürzeres Panorama und mehr Speicher pro Bild (also mehr Schärfe).
  Test: je ein Panorama bei 1× und bei 0,5×, Nahtstellen ansehen, dann entscheiden.
- **Schritt H (Falks neun Befunde, 2026-09-22):** gebaut und im Gerätetest geprüft: Licht-Zeile
  hält Knöpfe und Markierung, weiße Chip-Kästen, RAW-Chip nur bei RAW, Mond-Kontur, Zoom-Blase
  hinter dem Panel weg, **Pro-Panel im Video** (Belichtung + Weißabgleich), Stärke-Knopf daneben.
  Nebenbei behoben: **Absturz beim Kamera-Neustart**, wenn die Zeitlupe gespeichert war und die
  Kamera im Foto-Modus aufgebaut wurde. **Offen:** H1 (Timer-Chip) ließ sich nicht nachstellen –
  der Chip zykelt am Gerät korrekt; H8 (Auto-Torch in Zeitraffer/Zeitlupe) ist ungeklärt und
  braucht einen Geräteversuch im Dunkeln. Kickoff: `docs/kickoff/redesign-schritt-h-befunde.md`.
- **Schritt I (Falks sechs Punkte, 2026-09-22):** gebaut und im Gerätetest geprüft: alle Chips im
  Kopfband so groß wie der Auflösungs-Chip „9 MP" (18 dp, gemeinsamer Hintergrund), **Video-Blitz
  nur noch Aus/Taschenlicht**, **Pro-Knopf in Zeitraffer/Zeitlupe nach Google-Vorbild** (Stärke-Pille
  und Pro-Knopf wechseln sich in der unteren rechten Ecke ab, die offene Stärke-Reihe nimmt
  Modusleiste und Foto/Video-Umschalter weg), Kamera-Neustart beim Blitzwechsel entfällt.
  **Falk-Frage beantwortet:** der Neustart bleibt nur bei der **Nacht** (Kamera-Erweiterung, kann
  beim Umkonfigurieren hängen). **Panorama:** Nacht erzwingt Blitz Aus; RAW ist dort upstream nicht
  erlaubt (`isRawAllowed()`), der fehlende Chip ist also korrekt. **Offen:** Falks Abnahme; Frage,
  ob `res/drawable/header_chip_background_icon.xml` (unbenutzt) gelöscht werden darf. Kickoff:
  `docs/kickoff/redesign-schritt-i-chips-video-blitz.md`.
- **Schritt J (Falks Korrektur zu Schritt I, 2026-09-22):** gebaut, Gerätetest durch Falk offen.
  Der Dock-Tausch aus Schritt I ist **wieder heraus** (er ließ Foto/Video-Umschalter und
  Modusleiste verschwinden, bis die App neu startete): der Stärke-Knopf öffnet jetzt das
  **Pro-Panel auf dem Reiter „Geschwindigkeit"** (zweiter Eintrag hinter „Alle zurücksetzen",
  gleiches Symbol wie der Knopf), die Reihe der Raten steht dabei über dem Auslöser, und beim
  Schließen ist unten rechts alles wie vorher. **Video-Zeile heißt „Taschenlampe"** (Zeichen:
  Taschenlampe und durchgestrichene Taschenlampe, auch im Band-Chip). **Panorama:** der
  erzwungene „Blitz Aus" geht jetzt immer auch in die laufende Sitzung (Falks Befund: bei
  Nacht-Auto blitzte das Panorama im Hellen, weg erst nach App-Neustart), und der **RAW-Eintrag
  verschwindet in den vollen Einstellungen**, wo RAW nichts bewirkt (`isRawAllowed()` des obersten
  Modus). Kickoff: `docs/kickoff/redesign-schritt-j-staerke-im-pro-panel.md`.
- **Schritt K (Falks fünf Befunde zu Schritt J, 2026-09-22):** gebaut, am Gerät geprüft (Zeitlupe
  und Zeitraffer, Video-Blatt, Chip, Panorama und Foto). **„Alle zurücksetzen" gilt jetzt auch im
  Video** – in Zeitlupe/Zeitraffer steht `[Alle zurücksetzen] [Geschwindigkeit] |—|
  [Belichtung] [Weißabgleich]`. **Der Knopf je Reiter zeigt den Zurück-Pfeil statt „Auto"**
  (`ms_restart_alt`, ersetzt die Notiz aus Schritt C). **Die Taschenlampen-Zeichen waren
  unsichtbar** (y-nach-oben-Vektoren ohne `translateY`; jetzt im Blatt und im Chip zu sehen).
  **RAW fehlt auch im Pro-Reiter des Blattes**, wo es nichts bewirkt (Panorama/Porträt), und
  `res/drawable/header_chip_background_icon.xml` ist **gelöscht** (Falks Antwort auf die offene
  Frage aus Schritt I). Kickoff: `docs/kickoff/redesign-schritt-k-j-befunde.md`.
**Feste Entscheidungen:** Screenshot-Spec schlägt Detail-Spec, gemessene Werte (2,25 px/dp)
schlagen beide; keine Animationen außer Auslöser, Glyph-Drehung, Rad-Morph, Blatt-Gleiten;
Roboto Flex; Konflikte K1–K13 in `ui-spezifikation.md` 9; was die App nicht kann: 4b dort.
Verlauf und Recherche: `docs/changelog.md` (Abschnitt „Verlauf 4.5").

**Offene Punkte für den Projektmanager-Chat** (erledigte/geklärte: `docs/changelog.md`):
5. **Galerie leer nach Sperrbildschirm** (Open-Camera-Altverhalten, lädt beim nächsten
   Resume) – nicht angefasst.
6. **Ring/Squircles auf Schwarz unsichtbar** (Videomodus, 16:9): Falk vergleicht mit der
   Pixel-App, ggf. `shutter_row_scrim` heller.
7. **Sucher-Position (K4/1a):** 4:3-Sucher endet bei uns über der Moduswahl, Referenz
   reicht bis unter die Auslöser-Zeile → Sucher-Overlays-Schritt (7.1 Punkt 8).
8. **Tele-Verzögerung beim „5"-Knopf** (~1 s, Treiber entscheidet Linsenwechsel).
   Alternative: physische Tele-Linse direkt anfordern wie das Panorama-Linsenpaar
   (`userSwitchToCamera(id, physicalId)`), Preis: keine Low-Light-Automatik. Eigener
   kleiner Chat, Falk entscheidet am Gerät.
9. **Toter Code `if(!lock_to_portrait)`** (`MainActivity` ~Z. 1589/1649/1732, bewusst
   für Upstream-Nähe behalten, `8dcd8fc3`): lassen oder streichen? Weitere
   Ponytail-Funde: `docs/reviews/2026-09-18-ponytail-review.md` (2: Layout→XML nach
   Schritt C; 3: Stil-Dopplung im Farb-Schritt).
10. **Reihenfolge Audit-Schritte:** A Foto-Blatt Allgemein + Dock links + Swipe-Down
    (`docs/kickoff/redesign-einstellungen-blatt-foto.md`, **nächster Chat**) → B Pro +
    Video-Blatt + Dock rechts → C Kopfzeile, dann Icon-Leiste + Popup weg. Bis C bleiben
    beide und müssen mit dem Blatt synchron sein.
    **Geändert (Falk, 2026-09-20):** A + Blatt-Anteil von B (Pro-Reiter, Video-Blatt,
    Objektivauswahl) in **einem** Lauf als Modell-Benchmark, je Modell ein Branch
    `bench/blatt-<modell>`: `docs/kickoff/redesign-einstellungen-blatt-benchmark.md`
    (enthält alle Messwerte und Entscheidungen). Offen danach: rechter Dock-Button, dann C.
    **Blatt-Anteil gebaut (2026-09-20) und am 2026-09-21 am Pixel 9 Pro abgenommen**
    (Falk: ausgiebig getestet, Thema abgeschlossen); Branch `bench/blatt-deepseek-flash-2`
    per Fast-Forward nach `master` gemergt (`ca3a8f3d`), Verlierer-Branch
    `bench/blatt-deepseek-flash` gelöscht. **Rechter Dock-Button gebaut und am 2026-09-21
    abgenommen** (`ui/ProPanel`). **C gebaut und am 2026-09-22 am Pixel 9 Pro abgenommen**
    (Kopfzeile, Icon-Leiste + Popup entfernt) – der Schnitt A → B → C ist damit durch.
11. **Selbstauslöser-Countdown dreht noch mit** (`DrawPreview`); Videozeit ist fest.
    Konsistenz offen, vermutlich auch fest.
12. **Rad-Optik gewählt, nicht gemessen** (kein Screenshot mit offenem Rad); bei Bedarf
    über `ui/ZoomWheel`-Konstanten nachziehen.
13. **Serienbild- und Fokus-Bracketing-Anzahl ohne Bedienung** (Fund beim Abriss von C,
    2026-09-22): `preference_fast_burst_n_images` (Vorgabe 5), `preference_focus_bracketing_n_images`
    (3), `focus_bracketing_add_infinity` und `focus_bracketing_auto_source_distance` standen
    nur im Drei-Punkte-Popup und waren nie in der vollen Einstellungen-Aktivität. Die App liest
    sie weiter (`MyApplicationInterface`), sie sind aber nicht mehr einstellbar. Kickoff 3.4
    hatte sie ins Einstellungen-Blatt gelegt. Entscheidung nötig: Blatt-Zeilen nachziehen
    (Serienbild-Anzahl wäre mit Kickoff 3.5 „unbegrenzt, solange gehalten" zu verbinden),
    streichen oder bewusst so lassen. Fokus-Bracketing selbst ist ohnehin ein Fotomodus und
    damit bis zum Karussell unerreichbar.
14. **`./gradlew build` bricht beim Konfigurieren ab** (2026-09-22): `Could not create task
    ':app:testDebugUnitTest' … Type T not present` – Gradle 8.13/AGP 8.13.2 gegen JDK 25.0.3.
    `assembleDebug`, `assembleDebugAndroidTest` und `compileDebugUnitTestJavaWithJavac` laufen;
    nur die Test-Tasks lassen sich nicht anlegen. Entscheidung nötig: Wrapper auf Gradle 9
    hochziehen oder ein JDK 21 für Gradle setzen.

---

### 4.6 Zeitraffer & Zeitlupe — Status: Fertig (2026-09-21)

**Ziel:** Zwei Video-Modi wie in der Pixel-App: **Zeitraffer** (lange Aufnahme, gerafft
gespeichert) und **Zeitlupe** (Aufnahme mit hoher Bildrate, verlangsamt gespeichert).

**Anforderungen:**
- Beide als eigene Einträge in der Moduswahl. Details (Stärken, Beschriftungen, Ort und
  Ablauf der Bedienung) kommen aus Falks Referenz: Screenrecord + Screenshots in
  `/home/falk/Downloads/` (Feature-Chat wertet sie per Gemini aus).
- Stärken wählbar wie in der Referenz (Zeitraffer bis 120x, Zeitlupe 1/4x und 1/8x o. ä.).
- Ergebnis ist eine normale MP4-Datei für Galerie und Immich, die dort **korrekt schnell bzw.
  langsam abspielt** – der Effekt muss im gespeicherten Video stecken, nicht nur in der App.
- Nur Video; Foto bleibt unberührt. Material-3-Theme, Hochformat-Sperre und die vorhandenen
  UI-Bauteile gelten.

**Was die Codebasis schon hat (nicht neu bauen):**
- Zeitraffer: Open-Camera-Reste in `MyApplicationInterface`, `Preview`, `ApplicationInterface`,
  `PopupView`, `DrawPreview` (Suchbegriff `TimeLapse`/`time_lapse`) – Inventar zuerst.
- High-Speed-Video als Grundlage der Zeitlupe: `CameraController.setVideoHighSpeed()`,
  `video_sizes_high_speed`, Preference-Keys auf `_highspeed`, `video_high_speed`.

**Offene Fragen (Feature-Chat, am Gerät bzw. aus der Referenz):**
- Erlaubt der Pixel-9-Pro-HAL High-Speed-Video für Dritt-Apps? Wenn nein: Zeitlupe ausgrauen
  oder weglassen?
- Wie kommt der Effekt ins MP4 (Aufnahme-Bildrate vs. Zeitstempel/Playback-Rate) und spielen
  Immich/Systemplayer das richtig ab?
- Moduswahl ist heute statisch `Foto · Panorama`: die zwei Einträge kommen statisch dazu, das
  echte Karussell bleibt Schritt C/`ui-spezifikation.md` 7.2.

**Stand (2026-09-21): Fertig, am Pixel 9 Pro abgenommen.** Referenz-Auswertung (Screenrecord
+ 4 Screenshots): `docs/design-reference/pixel-zeitraffer-zeitlupe-beschreibung.md`.
Gebaut: Modusleiste im Video `Video · Zeitlupe · Zeitraffer` (statisch, kein Karussell); die
Stärke sitzt in einer eigenen Leiste am Platz der Zoom-Pille, geöffnet über eine dunkle Pille
im rechten Dock (Symbol + Wert, Vorgabe 5× bzw. 1/4×). Der Effekt steckt im MP4 über die
vorhandene Aufnahme-Rate des Recorders (`MediaRecorder.setCaptureRate`) – nicht in der App –,
Ton ist in beiden Modi automatisch aus. Am Gerät gemessen (ffprobe): Zeitraffer 5× → 10 s
Aufnahme = 1,87 s Video (4K, 140 Bilder); Zeitlupe 1/4× → 3,6 s = 14,4 s (1080p High-Speed,
432 Bilder @ 30); gewöhnliches Video 4K + Ton, 1:1. Der HAL liefert High-Speed (1080p/720p bis
240 fps, `dumpsys media.camera`), damit sind 1/8× und 1/4× möglich; Zeitraffer bietet
5/10/30/120×. **Entscheidungen (Falk 2026-09-21):** „Autom." entfällt (die Stärke steht beim
Öffnen der Datei fest); Auflösung und Bildrate werden in beiden Modi gestellt statt gewählt
(Zeitraffer: grösste normale Auflösung + Profil-Bildrate, Zeitlupe: grösste High-Speed-Auflösung
+ die für die Stärke nötige Bildrate) – nur beim Lesen übersteuert, die gespeicherten Werte des
gewöhnlichen Videos bleiben unberührt; Blitz bleibt wählbar. Auslöser in den Video-Arten rot,
aber kleiner (62 dp statt 74 dp) und mit dem Symbol des Modus (▷▷ / ◌▷, Referenz 4).
Offene Fragen damit beantwortet: HAL kann High-Speed (ja), Effekt steckt als Aufnahme-Bildrate
in der Datei, Moduswahl bleibt statisch.

**Komplexität/Risiko:** Mittel – UI klein; Risiko liegt in der HAL-Unterstützung für
High-Speed und in der Datei-Semantik (Playback-Rate).

---

## 5. Generelle technische Hinweise / Risiken (projektübergreifend)

- **Basis:** Open Camera (Java, aktiv gepflegt) als Fork – guter Startpunkt, da bereits
  strukturierte Kamera-Grundfunktionalität vorhanden ist.
- Concurrent-Camera-Funktionalität (Feature 4.3) ist der risikoreichste Punkt im
  gesamten Projekt – hängt stark von Tensor-Chip/Treiber-Unterstützung ab.
- Empfehlung: Projekt in Phasen angehen, nicht alle Features parallel gleich tief planen.
  Sinnvolle Reihenfolge grob nach Risiko/Abhängigkeit sortieren (wird mit Nutzer noch
  final festgelegt).
- **Paketname (`applicationId`):** `de.falkloewert.cameraapp` (bewusst geändert von
  `net.sourceforge.opencamera`, um Konflikte mit einer eventuell installierten offiziellen
  Open-Camera-App zu vermeiden). Der interne Java-Package-Name (`namespace` in
  `app/build.gradle`, `package net.sourceforge.opencamera;` in allen Java-Dateien) wurde
  bewusst NICHT mit umbenannt – rein kosmetisch, hätte Merge-Konflikte mit `upstream`
  erhöht, kein funktionaler Nutzen. Bei Bedarf später nachholbar.
- **App-Name/Icon:** App-Name zeigt seit 2026-08-27 testweise "Falks Kamera" (zuvor
  "Open Camera", siehe 4.4) – Icon (Mipmap) noch unverändert, rein kosmetisch, folgt bei
  Bedarf später.
- **Lokale Entwicklungsumgebung:** Zorin OS 18.1 (Ubuntu-basiert), Android Studio
  installiert (Snap, `--classic`), Build+Run auf echtem Pixel 9 Pro per USB-Debugging
  bestätigt funktionsfähig (siehe Änderungsprotokoll).

---

## 6. Änderungsprotokoll

Ausgelagert nach `docs/changelog.md` (nur für PM-Chats; Feature-Chats lesen es nicht).
