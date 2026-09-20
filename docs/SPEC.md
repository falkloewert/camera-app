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
| 6 | Material-3-Redesign (komplettes UI, absorbiert die alte Feature-6-Idee "Zoom-Stufen-Buttons + Moduswahl-Leiste") | **In Arbeit** – Theme/ConstraintLayout, Zoom-Stufen, Moduswahl, Auslöser-Zeile, Pixel-Drehmodell (Hochformat-Sperre, Glyphen drehen, Datei-Prüfung abgeschlossen) und **Feinjustier-Rad** (Stand 2026-09-18) gebaut; offen: siehe `ui-spezifikation.md` Abschnitt 7 (Audit 2026-09-17) | Hoch (app-weit, Design-Fundament für alle Folge-Features) |

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

**Stand (2026-09-18):** Gebaut und am Pixel 9 Pro abgenommen: Material-3-Theme (fest
dunkel) + ConstraintLayout (Phase 1); Zoom-Stufen-Pille `,5 1× 2 5` (gemessen 48/192/44 dp)
+ Feinjustier-Rad (`ui/ZoomWheel`, Pinch zeigt Rad, alter Regler weg); Moduswahl
`Foto · Panorama` (statisch, kein Karussell); Auslöser-Zeile nach Messung
(`ui/ShutterButton`, Squircles, Galerie links, `ui/PhotoVideoSwitch`); Pixel-Drehmodell
(Fenster fest Hochformat, nur Glyphen drehen, `MainUI.rotateGlyphs()`); keine Info-Toasts.
**Offen:** `ui-spezifikation.md` Abschnitt 7 (Reihenfolge dort: A Foto-Blatt → B Pro/Video
→ C Kopfzeile + Icon-Leiste/Popup weg → Karussell → Sucher-Overlays → Farben/Blur/Schrift).
**Feste Entscheidungen:** Screenshot-Spec schlägt Detail-Spec, gemessene Werte (2,25 px/dp)
schlagen beide; keine Animationen außer Auslöser, Glyph-Drehung, Rad-Morph; Roboto Flex;
Konflikte K1–K12 in `ui-spezifikation.md` 9; was die App nicht kann: 4b dort. Verlauf und
Recherche: `docs/changelog.md` (Abschnitt „Verlauf 4.5").

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
11. **Selbstauslöser-Countdown dreht noch mit** (`DrawPreview`); Videozeit ist fest.
    Konsistenz offen, vermutlich auch fest.
12. **Rad-Optik gewählt, nicht gemessen** (kein Screenshot mit offenem Rad); bei Bedarf
    über `ui/ZoomWheel`-Konstanten nachziehen.

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
