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

## Graphify & Archify (Codebase-Übersicht)

Zwei Übersichts-Werkzeuge für dieses Repo, initial eingerichtet über den Kickoff
`docs/kickoff/graphify-archify-setup.md`:

- **Graphify:** Wissensgraph in `graphify-out/` (Repo-Root, per globaler Regel — ein
  Graph pro Repo). Vor Codebase-Fragen erst `graphify query "<Frage>"` nutzen,
  `graphify path`/`graphify explain` für Beziehungen/Konzepte, `GRAPH_REPORT.md` nur
  für breite Architektur-Reviews. **Jeder Feature-Chat läuft nach Codeänderungen
  `graphify update .`** (AST-only, kein API-Kostenaufwand) — das ist Teil des
  Abschlussprotokolls (siehe unten), nicht optional.
- **Archify:** Architektur-/Workflow-Diagramme als Artifact. Nach architektonisch
  bedeutsamen Änderungen (neuer Kameramodus, neue Kernklasse, geänderter Datenfluss)
  im PM-Chat oder Feature-Chat neu generieren lassen — nicht nach jeder Kleinigkeit,
  das entscheidet der jeweilige Chat nach Augenmaß.

**Ergänzung zum Abschlussprotokoll (Abschnitt 2.1 in SPEC.md):** Feature-Chats führen
vor ihrem finalen Commit zusätzlich `graphify update .` aus, sofern `graphify-out/`
existiert.

## Status

Siehe `docs/SPEC.md` Abschnitte 3/3.1 (Feature-Übersicht, Reihenfolge) und 6
(Änderungsprotokoll) — nicht hier duplizieren, ändert sich zu schnell.
