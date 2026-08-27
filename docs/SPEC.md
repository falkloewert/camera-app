# Kamera-App Projekt – Spec & Roadmap

> Dieses Dokument ist die zentrale Wahrheit für das Projekt. Jeder neue Claude-Code-Chat
> (egal für welches Feature) sollte dieses Dokument zuerst lesen, um Kontext zu haben.
> Änderungen, Entscheidungen und neue Ideen werden hier nachgetragen – dieses Dokument
> wächst mit dem Projekt und wird bei jedem Fortschritt aktualisiert.

---

## 1. Projektübersicht

**Was:** Eine eigene Android-Kamera-App, basierend auf einem Fork von [Open Camera](https://opencamera.org.uk/).

**Warum:** Unzufriedenheit mit bestehenden Kamera-Apps – Kombination aus Wunsch-Features,
die es sonst nirgends zusammen gibt.

**Für wen:** Aktuell nur für den persönlichen Gebrauch. Kein Release, kein Produkt geplant.
Multi-Geräte-Support ist ein mögliches, aber unbestimmtes Zukunftsthema.

**Entwicklungsansatz:** "Vibe Coding" mit Claude Code. Der Nutzer hat keine
Programmiererfahrung, aber gute technische Grundkenntnisse und Lernbereitschaft.
→ **Wichtig für jeden Claude-Code-Chat: Schritte immer erklären, nicht nur ausführen.**

**Zielgerät:** Google Pixel 9 Pro
**Ziel-Android-Version:** Android 17 (API Level 37, stable seit 16. Juni 2026)

**Hosting:** Codeberg-Repository (`codeberg.org/falkloewert/camera-app`, privat), sauber dokumentiert.

**Basis-Repo (Upstream):** Open Camera, offizielles Quell-Repo auf SourceForge:
`https://git.code.sf.net/p/opencamera/code` (Branch `master`, Lizenz GPLv3). Im lokalen
Klon als Git-Remote `upstream` eingerichtet, um später Änderungen vom Original übernehmen
zu können (`git fetch upstream`).

**Git-Workflow-Entscheidung:** Falk möchte Commits und Pushes für dieses Projekt komplett
an Claude delegieren – er tippt diese Befehle nicht selbst, möchte aber weiterhin bei
jedem Schritt erklärt bekommen, was passiert und warum.

---

## 2. Workflow / Projektstruktur

- Dieses Dokument (`SPEC.md`) lebt im Codeberg-Repo unter `docs/SPEC.md` und ist die
  Referenz für alle Claude-Code-Sessions.
- Für jedes größere Feature wird ein **eigener, fokussierter Claude-Code-Chat** geöffnet.
  Diese Chats bekommen jeweils den relevanten Ausschnitt aus diesem Dokument als
  Kontext mit.
- Fortschritte, Entscheidungen und neu aufkommende Details aus den Feature-Chats
  werden in dieses Dokument zurückgetragen (Status-Updates, neue offene Fragen etc.).
- Rollenverteilung: Der Nutzer bringt Ideen und Feinschliff-Wünsche ein, die
  Projektmanager-Instanz (Claude, ob hier oder in Claude Code) strukturiert,
  stellt Rückfragen und leitet zur Umsetzung an. Commits/Pushes übernimmt Claude.

### 2.1 Rolle & Abschlussprotokoll für Feature-Chats

Gilt für jeden neuen, feature-spezifischen Claude-Code-Chat (nicht für diesen
Projektmanager-Chat):

- **Rolle:** Der Feature-Chat ist NICHT der Projektmanager. Er implementiert genau
  das ihm zugewiesene Feature, trifft keine Entscheidungen über andere Features,
  die Gesamt-Reihenfolge oder die Projektausrichtung – das bleibt dem
  Projektmanager-Chat vorbehalten.
- **Session-Titel:** Der Feature-Chat benennt sich zu Beginn selbst um (Tool
  `set_session_title`, `session_id: "self"`) nach dem Schema
  `camera-app – Feature: <Name>` (z. B. `camera-app – Feature: Panorama-Weitwinkel`).
  Grund: Falk öffnet für jedes Feature einen neuen Chat, die Session-Liste soll
  trotzdem übersichtlich bleiben, ohne dass er das manuell nachpflegen muss.
- **Vorgehen:** Vor Codeänderungen den relevanten Code identifizieren und Falk
  erklären, wie es aktuell funktioniert und was geändert werden soll. Änderungen in
  überschaubaren, erklärten Schritten umsetzen. Nach Änderungen von Falk auf dem
  echten Pixel 9 Pro testen lassen, bevor committet wird.
- **Abschlussprotokoll**, sobald das Feature fertig und von Falk bestätigt ist:
  1. `docs/SPEC.md` aktualisieren: Status des Features setzen, Änderungsprotokoll-
     Eintrag mit Datum ergänzen (was gebaut wurde, Entscheidungen, neue offene
     Fragen für später).
  2. Finalen Commit + Push.
  3. Falk eine kurze Zusammenfassung geben, die er in den Projektmanager-Chat
     mitnehmen kann.
  4. NICHT eigenständig das nächste Feature beginnen – Reihenfolge-Entscheidungen
     laufen über den Projektmanager-Chat.

---

## 3. Feature-Übersicht (Status)

| # | Feature | Status | Komplexität |
|---|---------|--------|--------------|
| 1 | 360°-Foto-Stitching + interaktive Ansicht | Idee / Konzept | Hoch |
| 2 | Filmchen-Modus (Snapchat-Style Clip-Tagebuch) | Idee / Konzept | Mittel-Hoch |
| 3 | Dual-Kamera (Selfie-Bubble, unabhängiger Zoom) | Idee / Konzept | Hoch (geräteabhängig) |
| 4 | Panorama mit Weitwinkel-Option | **Als Nächstes** | Niedrig-Mittel |
| 5 | Kamera während Videoaufnahme umdrehen | Idee / Notiz (nice-to-have) | Niedrig |

### 3.1 Vereinbarte Reihenfolge (festgelegt am 2026-08-27)

Kompromiss aus "Einfaches zuerst" (zum Kennenlernen von Workflow/Codebasis als
Programmier-Einsteiger) und "Riskantes früh klären" (Dual-Kamera hängt von
Tensor-Chip-Unterstützung ab, siehe Abschnitt 5):

1. **Feature 4** – Panorama mit Weitwinkel-Option
2. **Feature 5** – Kamera während Videoaufnahme umdrehen
3. **Machbarkeits-Spike Dual-Kamera** – kein volles Feature, nur klären: unterstützt
   der Pixel 9 Pro zuverlässig gleichzeitigen Zugriff auf Front- und Rückkamera
   ("Concurrent Camera")?
4. **Feature 2** – Filmchen-Modus
5. **Feature 3** – Dual-Kamera (voller Ausbau, nach positivem Spike-Ergebnis)
6. **Feature 1** – 360°-Foto-Stitching (inkl. Immich-Kompatibilität, siehe 4.1)

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
- **2026-08-27:** Entwicklungsumgebung fertig eingerichtet und erster Build erfolgreich
  auf dem echten Pixel 9 Pro getestet: Android Studio installiert (Snap, `--classic`),
  Projekt in Android Studio geöffnet, Gradle-JDK-Konflikt behoben (Auto-Fix), USB-Debugging
  auf dem Handy aktiviert, unveränderte Open-Camera-Basis erfolgreich gebaut und gestartet.
  `applicationId` auf `de.falkloewert.cameraapp` geändert, um Signatur-Konflikte mit einer
  ggf. installierten offiziellen Open-Camera-App zu vermeiden – zweiter Testlauf danach
  ohne Deinstallations-Dialog erfolgreich. Damit ist die technische Basis vollständig
  funktionsfähig; nächster Schritt: Feature-Reihenfolge mit Falk festlegen.
- **2026-08-27:** Feature-Reihenfolge gemeinsam festgelegt (siehe Abschnitt 3.1):
  Panorama-Weitwinkel → Kamera-Flip bei Video → Dual-Kamera-Machbarkeits-Spike →
  Filmchen-Modus → Dual-Kamera voll → 360°-Stitching. Kickoff-Kontext für den ersten
  Feature-Chat (Panorama mit Weitwinkel-Option) erstellt.
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

### 4.4 Panorama mit Weitwinkel-Option

**Ziel:** Bei der normalen Pixel-Kamera ist im Panorama-Modus nur 1x-Zoom (Standardlinse)
wählbar, nicht die Weitwinkellinse. Das soll in der eigenen App möglich sein.

**Anforderungen:**
- Weitwinkel-Kamera als Option im (normalen, nicht 360°-) Panorama-Modus auswählbar machen

**Offene Fragen:** Noch keine vertieft besprochen – vergleichsweise kleines,
klar umrissenes Feature.

**Komplexität/Risiko:** Niedrig-Mittel.

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
- **App-Name/Icon:** zeigen aktuell noch "Open Camera" (aus `AndroidManifest.xml`
  `android:label` bzw. Mipmap-Icons) – noch nicht angepasst, rein kosmetisch, folgt bei
  Bedarf später.
- Lokale Entwicklungsumgebung (Stand 2026-08-27): Zorin OS 18.1 (Ubuntu-basiert), noch
  kein Java/Android Studio installiert – wird als nächster Schritt eingerichtet.

---

## 6. Änderungsprotokoll

- **2026-08-27:** Erstfassung des Dokuments erstellt, basierend auf initialem Brainstorming.
  Alle vier Kernfeatures sowie Workflow-Ansatz (feature-spezifische Claude-Code-Chats,
  zentrales Spec-Dokument als Anker) festgehalten.
- **2026-08-27:** Repo-Setup abgeschlossen: Codeberg-Repo `falkloewert/camera-app` (privat)
  angelegt, Open Camera von SourceForge (`upstream`) geklont und komplette Historie nach
  Codeberg (`origin`) gepusht. `docs/SPEC.md` und `README.md` ergänzt. Entscheidung: Falk
  delegiert Commits/Pushes für dieses Projekt vollständig an Claude. Nächster Schritt:
  Android Studio installieren, um die unveränderte Basis erstmals zu bauen.
- **2026-08-27:** Anforderung zu 4.1 ergänzt: 360°-Bilder müssen auch in Immich
  (Falks self-hosted Foto-Bibliothek) korrekt als interaktive Kugel angezeigt werden,
  nicht nur im eigenen In-App-Viewer. Bekanntes, noch ungeklärtes Problem: Immich zeigt
  360°-Bilder bei Falk aktuell nicht richtig an – Ursache wird im 360°-Feature-Chat
  untersucht, nicht jetzt.
