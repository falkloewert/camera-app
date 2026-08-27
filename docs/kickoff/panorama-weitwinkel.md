# Kickoff-Kontext: Feature "Panorama mit Weitwinkel-Option"

Dies ist ein Feature-Chat für mein Kamera-App-Projekt (Fork von Open Camera). Der
vollständige Projektkontext liegt in `docs/SPEC.md` im Repo – bitte diese Datei zuerst
lesen, bevor wir anfangen.

## Kurzfassung

- **Repo:** lokal `~/Projects/camera-app`. Remote `origin` = privates Codeberg-Repo
  `falkloewert/camera-app`, Remote `upstream` = Open Camera (Original) auf SourceForge.
- **Zielgerät:** Google Pixel 9 Pro, Android 17 (API 37). Die App läuft bereits
  erfolgreich auf dem Gerät (`applicationId`: `de.falkloewert.cameraapp`).
- **Ich (Falk) habe keine Programmiererfahrung**, aber technisches Grundverständnis und
  Lernbereitschaft. Bitte bei jedem Schritt erklären, was du tust und warum – nicht nur
  ausführen, sondern mitnehmen.
- **Git-Workflow:** Du (Claude) übernimmst `git commit`/`git push` für dieses Projekt
  komplett, ohne mich vorher zu fragen. Ich tippe diese Befehle nicht selbst.
- **Dokumentationspflicht:** Du pflegst `docs/SPEC.md` als lebendiges Master-Dokument
  – Status-Updates, neue offene Fragen, Änderungsprotokoll-Einträge.

## Aufgabe dieses Chats

**Feature 4 aus der SPEC:** Panorama mit Weitwinkel-Option.

**Ziel:** Bei der normalen Pixel-Kamera ist im Panorama-Modus nur die Standardlinse
(1x-Zoom) wählbar, nicht die Weitwinkellinse. In unserer App soll die
Weitwinkel-Kamera zusätzlich als Option im (normalen, nicht 360°) Panorama-Modus
auswählbar sein.

**Komplexität laut Spec:** Niedrig-Mittel. Bewusst als erstes Feature gewählt, um
Workflow (Android Studio, Build, Test auf echtem Gerät, Git) an einer überschaubaren
Aufgabe kennenzulernen, bevor es an die komplexeren Features (Dual-Kamera,
360°-Stitching, Filmchen-Modus) geht.

**Offene Fragen:** Bisher keine vertieft besprochen. Bitte zu Beginn des Chats zuerst
die relevante Stelle im Open-Camera-Code identifizieren (Panorama-Modus, Kamera-/
Linsen-Auswahl) und mir erklären, wie diese aktuell funktioniert und wie der geplante
Ansatz aussieht, bevor Code geändert wird.
