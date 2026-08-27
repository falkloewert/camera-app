# Kickoff-Kontext: Feature "Kamera während Videoaufnahme umdrehen"

Dies ist ein Feature-Chat für mein Kamera-App-Projekt (Fork von Open Camera). Der
vollständige Projektkontext liegt in `docs/SPEC.md` im Repo – bitte diese Datei zuerst
lesen, insbesondere Abschnitt 2.1 (deine Rolle & Abschlussprotokoll als Feature-Chat).

## Kurzfassung

- **Repo:** lokal `~/Projects/camera-app`. Remote `origin` = privates Codeberg-Repo
  `falkloewert/camera-app`, Remote `upstream` = Open Camera (Original) auf SourceForge.
- **Zielgerät:** Google Pixel 9 Pro, Android 17 (API 37). `applicationId`:
  `de.falkloewert.cameraapp`. App-Name aktuell testweise "Falks Kamera" (siehe SPEC 4.4).
- **Ich (Falk) habe keine Programmiererfahrung**, aber technisches Grundverständnis und
  Lernbereitschaft. Bitte bei jedem Schritt erklären, was du tust und warum.
- **Git-Workflow:** Du (Claude) übernimmst `git commit`/`git push` für dieses Projekt
  komplett, ohne mich vorher zu fragen.
- **Nur eine Sitzung gleichzeitig im Repo aktiv** (siehe SPEC 2.1) – prüfe zu Beginn mit
  `git status`/`git fetch`, ob eine andere Sitzung gerade unfertige Änderungen offen hat.
- **Als Allererstes:** Benenne diese Session per Tool `set_session_title`
  (`session_id: "self"`) um in `camera-app – Feature: Video-Kameraflip`.

## Aufgabe dieses Chats

**Feature 5 aus der SPEC:** Kamera während Videoaufnahme umdrehen.

**Ziel:** Möglichkeit, während einer laufenden Videoaufnahme (im normalen
Einzel-Kamera-Modus, nicht Dual-Kamera) zwischen Front- und Rückkamera umzuschalten,
ohne die Aufnahme zu unterbrechen.

**Herkunft/Kontext:** In der SPEC ursprünglich als Nice-to-have unter Abschnitt 4.3
(Dual-Kamera) notiert, aber bewusst als **eigenständiges, kleineres Feature** in die
Reihenfolge aufgenommen (SPEC 3.1) – unabhängig vom größeren, hardwareabhängigen
Dual-Kamera-Feature.

**Komplexität laut Spec:** Niedrig.

**Offene Fragen:** Noch keine vertieft besprochen. Bitte zu Beginn klären und mir
erklären:
- Wie ist ein nahtloser Kamerawechsel während laufender Aufnahme technisch überhaupt
  möglich (kurze Unterbrechung im Videostream unvermeidbar? Wie machen andere Apps das)?
- Wo im bestehenden Code wird die Videoaufnahme gesteuert, und wo wäre ein sinnvoller
  Einstiegspunkt für einen Umschalt-Button während der Aufnahme?

Bitte wie gewohnt: zuerst den relevanten Code identifizieren und den geplanten Ansatz
erklären, bevor Code geändert wird.
