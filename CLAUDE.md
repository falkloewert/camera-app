# camera-app — Projektkontext & Claude-Workflow

Fork von Open Camera (Java, GPLv3), Zielgerät Pixel 9 Pro, Android 17/API 37.
Kein Release geplant, nur persönlicher Gebrauch. Volles Feature-Konzept und
Status: `docs/SPEC.md` — das ist die lebendige Master-Quelle, nicht diese Datei.

**Remotes:** `origin` = privates Codeberg-Repo `git@codeberg.org:falkloewert/camera-app.git`,
`upstream` = Open-Camera-Original auf SourceForge
(`https://git.code.sf.net/p/opencamera/code`, Branch `master`).

## Git-Workflow (Ausnahme von der generellen Regel)

Falk möchte `git commit`/`git push` für **dieses Projekt** nicht selbst tippen —
das übernimmt Claude komplett, in jeder Session. Trotzdem weiterhin jeden Schritt
ausführlich erklären, nur die Ausführung der Git-Befehle selbst übernehmen statt
sie ihm zum Ausführen zu geben.

**Warum:** Explizite Ansage, um sich nicht um Git-Mechanik kümmern zu müssen — er
will beim eigentlichen Bauen/Verstehen der App-Logik lernen, nicht bei Git-Befehlen.

**Wie anwenden:** Nach Änderungen direkt selbst `git add`/`git commit`/`git push`
ausführen, ohne vorher zu fragen oder die Befehle ihm zum Ausführen zu geben.
Gilt **nur für dieses Repo**, nicht generell für andere Projekte von Falk — dort
gilt weiterhin die normale Zurückhaltung bei Commits/Pushes.

## Status (Stand 2026-08-27)

Repo-Setup abgeschlossen (Klon + Push + docs/SPEC.md + README committet).
Nächster Schritt: Android Studio auf Zorin OS 18.1 installieren, dann Open Camera
unverändert bauen/starten, bevor Features angegangen werden.
