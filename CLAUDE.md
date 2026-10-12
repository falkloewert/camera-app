# camera-app — Regeln für Claude

Fork von Open Camera (Java, GPLv3), Pixel 9 Pro, Android 17/API 37, nur Eigengebrauch.
Master-Quelle: `docs/SPEC.md` (Feature-Chats: nur 2.1 + eigener Abschnitt 4.x).
Historie: `docs/changelog.md`. Remotes: `origin` GitHub `falkloewert/camera-app` (privat, seit 12.10.2026; vorher Codeberg), `upstream` SourceForge.

## Token-Sparen (seit 2026-09-18, Pro-Kontingent)
- Keine Erklärungen, keine Narration, keine Zusammenfassungen. Ergebnis oder Frage.
- Nur lesen, was der Kickoff nennt (Abschnitte/Zeilenbereiche), nie ganze Dateien.
- Modell Sonnet; Opus nur bei nachgewiesenem Scheitern.
- Gerätetests bündeln, nicht nach jedem Handgriff. Kein Review-Chat ohne Anlass.
- Docs kurz: Changelog-Eintrag ≤ 10 Zeilen, Kickoffs ≤ 60 Zeilen.

## Git
- Claude committet und pusht selbst, ohne zu fragen – **nur mit expliziter Dateiangabe**
  (`git commit -m "…" -- <Dateien>`), nie pfadlos (Vorfall 2026-09-17: fremde Änderungen
  einer Parallel-Session mitcommittet).
- Vor Codeänderungen `git status` + `git fetch origin`; fremde unfertige Dateien nicht
  anfassen. Nur eine Sitzung gleichzeitig im Repo.

## Rollen
- PM-Chat: plant, pflegt SPEC/Kickoffs, kein App-Code. Feature-Chat: baut genau den
  Kickoff, Falk testet am Gerät, Abschlussprotokoll SPEC 2.1. Keine PM-orchestrierten
  coder-Subagenten für Feature-Code (Council 2026-09-14,
  `docs/council/2026-09-14-subagent-workflow.md`).
- Neuer PM-Chat: SPEC.md + diese Datei lesen, `git status`/`git fetch`, dann weiter.

## Gerät (adb)
- App immer über die eigene Package-ID starten/prüfen: `de.falkloewert.cameraapp`
  (Activity `net.sourceforge.opencamera.MainActivity`). Die Upstream-App
  `net.sourceforge.opencamera` ist parallel installiert – **nie** die starten
  (Fehler 2026-09-21).

## Graphify
- `graphify-out/` (git-ignoriert). Vor Codebase-Fragen `graphify query "<Frage>"`.
  Nach Codeänderungen `graphify update .` (AST-only; `--force` nach Löschungen).
- `.graphifyignore` nicht löschen (schließt ~360 Icon-Dateien aus, sonst teure
  Vision-Extraktion).
- Archify-Diagramm: `docs/architecture-diagrams.md`, nur bei Architekturänderung neu.
