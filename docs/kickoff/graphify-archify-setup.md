# Kickoff-Kontext: Graphify + Archify Setup (Codebase-Übersicht)

Dies ist ein Chat für mein Kamera-App-Projekt (Fork von Open Camera, Repo
`~/Projects/camera-app`) — aber kein Feature-Chat im üblichen Sinn, sondern
Infrastruktur/Doku-Arbeit ohne Live-Gerätetest. Bitte trotzdem zuerst `docs/SPEC.md`
und `CLAUDE.md` im Repo lesen für Kontext.

## Kurzfassung

- **Ich (Falk) habe keine Programmiererfahrung**, aber technisches Grundverständnis.
  Bitte trotzdem erklären, was du tust und warum — auch bei Tooling-Setup.
- **Git:** Du übernimmst `git commit`/`git push` für dieses Projekt komplett selbst.
- **Kein Live-Gerätetest nötig für diese Aufgabe** — das ist reine
  Codebase-/Dokumentations-Arbeit, kein Feature mit Nutzerinteraktion auf dem Handy.

## Aufgabe dieses Chats

Zwei dauerhafte Übersichts-Werkzeuge für dieses Repo aufbauen und eine Routine
etablieren, damit sie aktiv gepflegt bleiben, statt einmalig zu veralten:

### 1. Graphify — Wissensgraph

Baue mit dem `graphify`-Skill einen initialen Wissensgraphen für `~/Projects/camera-app`
(Ausgabe in `graphify-out/` im Repo-Root, wie in der globalen Regel vorgesehen — ein
Graph pro Repo, keine repo-übergreifenden Graphen). Danach:

- Kurz erklären, was der Graph zeigt (god nodes, Community-Struktur, wichtige
  Datei-Beziehungen) — auf einer Ebene, die ich als Nicht-Programmierer verstehe.
- Die laufende Pflege ist bereits in `CLAUDE.md` als Regel hinterlegt: jeder
  Feature-Chat soll künftig nach Codeänderungen `graphify update .` ausführen, bevor
  er committet. Prüfe, ob diese Regel sinnvoll platziert ist, und ergänze sie bei
  Bedarf um das, was du beim Aufbau des Graphen über das Projekt gelernt hast.

### 2. Archify — Architektur-Diagramm

Baue mit dem `archify`-Skill ein erstes Architektur-/Übersichtsdiagramm der App:
grobe Modulstruktur, wie die bestehenden Kameramodi (Foto, Video, Panorama) und die
schon geplanten künftigen Modi (Filmchen, Dual-Kamera, 360°, siehe `docs/SPEC.md`
Abschnitt 4) strukturell zusammenhängen bzw. andocken sollen. Auf Basis von echtem
Code (Repository-Evidenz), nicht spekulativ.

- Als Artifact veröffentlichen.
- Kurz erklären, was zu sehen ist.
- In `docs/` einen kurzen Verweis/Link auf das Artifact hinterlegen (die
  Diagramm-Datei selbst muss nicht ins Repo, nur der Link/Kontext, damit ein
  späterer Chat weiß, dass es existiert und wo).

## Abschluss

Wenn beides steht: `docs/SPEC.md` Änderungsprotokoll ergänzen (Datum, was gebaut
wurde, Link zum Archify-Artifact), committen/pushen, und mir eine kurze
Zusammenfassung für den Projektmanager-Chat geben — inklusive, ob/wie ich als Nutzer
künftig selbst mal `graphify query "..."` stellen kann, falls mich das interessiert.
