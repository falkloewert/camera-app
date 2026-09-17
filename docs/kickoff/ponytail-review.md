# Kickoff-Kontext: Ponytail-Review über den eigenen Code

Kein Feature-Chat, sondern ein Qualitäts-Check (reines Lesen/Bewerten, kein
Live-Gerätetest nötig). Wiederverwendbar – bei Bedarf auch zu einem späteren
Zeitpunkt nochmal mit diesem Prompt (Basis-Commit ggf. anpassen).

## Zuerst prüfen, bevor irgendwas läuft

`git status` und `git fetch origin` ausführen. **Falls das Arbeitsverzeichnis nicht
sauber ist** (unfertige, uncommittete Änderungen eines anderen, evtl. noch laufenden
Chats) **oder `HEAD` nicht auf `origin/master` steht: sofort stoppen und mir sagen,
dass gewartet werden muss.** Kein Review über einen halbfertigen Stand.
**Ausnahme (ergänzt 2026-09-18):** uncommittete Änderungen, die ausschließlich unter
`docs/` liegen (z. B. vom parallel laufenden Doku-Abschluss eines Feature-Chats), sind
für ein Code-Review unkritisch – dann weitermachen, bewertet wird ohnehin nur der
committete Stand `HEAD`.

## Kontext

Projekt `~/Projects/camera-app`, Fork von Open Camera. Der Fork-Basis-Commit ist
`0dd4cbe7` ("Update for release.") – alles danach ist unser eigener Code
(Panorama-Feature, Material-3-Redesign). **Wichtig:** Ponytail soll NUR unseren
eigenen Diff bewerten, nicht die komplette Open-Camera-Codebasis – die stammt von
Mark Harman, wird bewusst nicht umgebaut (Upstream-Merge-Konflikte vermeiden), und
ein Review über fremden Code würde nur Rauschen erzeugen.

## Aufgabe

`/ponytail-review` (oder den entsprechenden Skill direkt) auf den Diff
`0dd4cbe7..HEAD` anwenden – nicht `/ponytail-audit` (das wäre das ganze Repo
inklusive Upstream-Code).

**Danach:**
- Ergebnis wie gewohnt im Ponytail-Format (Fundstelle, was raus soll, wodurch
  ersetzen), aber bei jedem Fund einen zusätzlichen kurzen Satz ergänzen, der mir
  (keine Programmiererfahrung) erklärt, warum das ein Problem ist – nicht nur
  Fachjargon.
- **Keine Fixes selbst anwenden.** Das ist ein reiner Bericht. Findings, die
  behoben werden sollen, laufen über einen normalen Feature-Chat mit Gerätetest,
  genau wie jede andere Code-Änderung an dieser App.
- Ergebnis mir hier im Projektmanager-Chat zusammenfassen, damit wir gemeinsam
  entscheiden, was davon behoben wird und was nicht (nicht jeder Fund muss
  zwingend behoben werden).

Kein Abschlussprotokoll/Commit nötig, falls nichts geändert wird – nur der Bericht.
