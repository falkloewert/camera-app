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

## Feature-Chats bleiben manuell (Entscheidung 2026-09-14)

Der Projektmanager-Chat plant/dokumentiert Features und bereitet Kickoff-Prompts vor,
startet aber **keine** `coder`-Subagenten für die Feature-Implementierung selbst. Falk
öffnet weiterhin manuell einen neuen Chat pro Feature. Grund: Falks Ziel (Lernen +
Live-Kontrolle über den Gerätetest auf dem Pixel 9 Pro) braucht synchrone, dialogische
Zusammenarbeit, die ein im Hintergrund arbeitender Subagent nicht liefern kann. Per
LLM-Council geprüft und bestätigt (4 von 5 Beratern dagegen, 5 von 5 Peer-Reviews
markierten die Pro-Stimme als größten blinden Fleck) — Transkript:
`docs/council/2026-09-14-subagent-workflow.md`.

Subagenten sind für testfreie Nebenarbeiten weiterhin sinnvoll (Recherche, Doku,
SPEC-Konsistenzchecks) — nur nicht für die Kern-Feature-Implementierung mit Live-Test.

Projektmanager-Chat läuft mit Sonnet (Planung/Doku, kein schweres Code-Reasoning);
Opus eher situativ für einzelne komplexe Feature-Chats.

## Status

Siehe `docs/SPEC.md` Abschnitte 3/3.1 (Feature-Übersicht, Reihenfolge) und 6
(Änderungsprotokoll) — nicht hier duplizieren, ändert sich zu schnell.
