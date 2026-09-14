# Council-Transkript: coder-Subagenten statt manueller Feature-Chats?

**Datum:** 2026-09-14
**Auslöser:** Falk kehrte nach 3 Wochen Pause zurück, inzwischen mit global konfigurierten
Claude-Code-Subagenten (coder, reviewer, evaluator, researcher, gemini, server-ops) und
mehreren Skills (qa-loop, wrap-up, graphify, archify, llm-council). Vorschlag: der
Projektmanager-Chat soll künftig selbst `coder`-Subagenten pro Feature lostschicken,
statt dass Falk manuell einen neuen Chat pro Feature öffnet. Zusatzfrage: Opus oder
Sonnet für den PM-Chat?

**Visueller Bericht:** siehe verlinktes Artifact (vom Projektmanager-Chat veröffentlicht,
2026-09-14).

---

## Framed Question

Bisheriger, bereits einmal erfolgreich durchlaufener Workflow: Ein "Projektmanager"-Chat
pflegt eine lebendige Spezifikation (`docs/SPEC.md`) und schreibt für jedes Feature einen
Kickoff-Prompt. Falk öffnet dafür manuell einen neuen, separaten Claude-Code-Chat, fügt
den Prompt ein; dieser Chat implementiert das Feature mit ausführlichen Erklärungen
(Falk will verstehen, nicht nur ein Ergebnis bekommen), lässt Falk live auf dem Gerät
testen, aktualisiert die Spec und committet/pusht selbst.

Entscheidungsfrage: Soll der Projektmanager-Chat stattdessen selbst über das Agent-Tool
`coder`-Subagenten pro Feature lostschicken, damit der komplette Ablauf in einem
einzigen Chat läuft? Bekanntes Risiko: zwei parallel laufende Claude-Code-Sitzungen im
selben Arbeitsverzeichnis haben früher schon zu einem unbemerkten Konflikt geführt.
Welches Modell (Opus/Sonnet) sollte der PM-Chat nutzen?

---

## Berater-Antworten

### Contrarian — Nein
Der fatale Fehler steckt nicht im Coder-Subagenten, sondern in der Prämisse, dass ein
einziger Chat alle Rollen gleichzeitig gut spielen kann: Planer, Lehrer, Testkoordinator,
Doku-Schreiber. Ein Subagent berichtet nur ein Ergebnis zurück — die ausführlichen
Erklärungen gehen komprimiert oder ganz verloren. Der Geräte-Test-Bruch ist ein
Widerspruch im Design: ein Subagent kann nicht auf Falks Live-Test warten, während er
läuft. Das bekannte Konflikt-Risiko wird verschärft, nicht entschärft, weil die
bisherige Hemmschwelle ("neuer Chat öffnen") wegfällt. Opus wäre der Fall für
Orchestrierung, aber der Mehrwert ist fraglich.

### First Principles Thinker — Falsch gestellte Frage
Der manuelle Chat-Wechsel ist kein Reibungsverlust, sondern der Lernmechanismus — ein
Checkpoint, kein Bug. Ein coder-Subagent entfernt genau diesen Checkpoint. Wer schreibt
im neuen Modell die ausführlichen Erklärungen — der Subagent, an den PM-Chat, der sie an
Falk weiterreicht? Das ist Erklärung aus zweiter Hand. Das GUI-Session-Problem ist kein
zu lösendes Detail, sondern der Beweis, dass Automatisierung hier grundsätzlich falsch
ansetzt. Die eigentliche Frage: welches Problem hat der aktuelle Workflow tatsächlich,
das eine Änderung rechtfertigt? Falk hat keins genannt außer "jetzt gibt's Subagenten".

### Expansionist — Ja, sogar ausbauen
Der eigentliche Engpass ist der manuelle Chat-Wechsel selbst (Kontextverlust,
Copy-Paste-Spec-Sync). Ein coder-Subagent pro Feature, orchestriert vom PM-Chat, gibt
Falk einen durchgängigen Erzählfaden über das ganze Projekt. Für unabhängige, kleine
Features könnten sogar mehrere Subagenten parallel Planung/Code-Diff vorbereiten,
seriell gebaut/getestet — der Flaschenhals bleibt ja Falks GUI-Testsession. Ein
Subagent mit expliziter Anweisung "erkläre ausführlich" verliert nichts an Didaktik.
Opus für den PM-Chat, Kosten sind bei einem Hobbyprojekt vernachlässigbar.

### Outsider — Nein, fundamentaler Widerspruch
Ein Subagent im Hintergrund ist strukturell dafür gebaut, dass man NICHT hinschaut,
während er arbeitet — das ist der Sinn von Delegation. Für eine Person, deren erklärtes
Ziel "den Prozess sehen" ist, beißt sich das fundamental. Niemand hat gefragt, warum
überhaupt Automatisierung nötig ist — das manuelle Öffnen kostet vielleicht 30 Sekunden,
bei einem Hobbyprojekt ohne Deadline. Das Council optimiert ein Problem weg, das keins
ist, und riskiert dafür das Einzige, was diesem Projekt einen Zweck gibt.

### Executor — Nein
Der bekannte Parallel-Konflikt ist genau das Muster, das hier entsteht. Der Kern des
bisherigen Workflows ist synchrone Rückkopplung — ein Subagent kann nicht warten, bis
Falk getestet hat, ohne die Erklär-Schleife kaputtzumachen. Montagmorgen-Empfehlung:
Workflow unverändert lassen. coder-Subagenten sind gut für mechanische Nebenarbeiten
ohne Gerätetest (Doku, Boilerplate), nicht für Features mit Live-Test. Sonnet reicht für
den PM-Chat, da er keine Code-Arbeit macht.

---

## Peer-Review-Highlights (anonymisiert, 5 unabhängige Durchgänge)

- **Review 1:** stärkste Antwort Outsider; größter blinder Fleck Expansionist
  (unbelegte Behauptung zur Erklärqualität, Konfliktrisiko weggewischt). Übersehen:
  Git-Worktrees als technische Absicherung gegen den bekannten Konflikt.
- **Review 2:** stärkste Antwort First Principles; größter blinder Fleck Expansionist
  (ignoriert Konfliktrisiko fast komplett). Übersehen: warum blieb der frühere Konflikt
  *unbemerkt* — fehlende Absicherung (Locking/Worktrees/Statusanzeige) ist das
  eigentlich Fixable.
- **Review 3:** stärkste Antwort First Principles; größter blinder Fleck Expansionist
  (Selbstwiderspruch bei "nur einer schreibt"). Übersehen: dritte Option — Subagent
  läuft synchron im Dialog mit Falk statt im Hintergrund.
- **Review 4:** stärkste Antwort Contrarian (zeigt konkrete Konsequenzen); größter
  blinder Fleck Expansionist (klärt nie, wie der Subagent auf den Live-Test wartet).
  Übersehen: ob blockierende Rückfrage vom Subagent an den Menschen technisch überhaupt
  möglich ist — eine reine Tool-Fähigkeitsfrage, die die Debatte vorentscheidet.
- **Review 5:** stärkste Antwort Outsider (Inkompatibilität auf Tool-Ebene, kein
  Geschmacksurteil); größter blinder Fleck Expansionist. Übersehen: Ursachen-Diagnose
  des früheren Konflikts fehlt; mildere Lösung (Subagent nur lesend/planend, kein
  Schreibzugriff während Falk testet) nie geprüft.

**Bemerkenswert:** Die Expansionist-Antwort (einzige Pro-Stimme) wurde von 5 von 5
unabhängigen Reviews als größter blinder Fleck markiert.

---

## Chairman-Verdikt

**Einigkeit:** Live-Gerätetest + ausführliche Erklärung sind mit reiner
Hintergrund-Delegation strukturell unvereinbar. Das bekannte Parallel-Konflikt-Risiko
wird durch PM-gesteuerte Subagenten eher verschärft als entschärft. Die einzige
Pro-Antwort hat laut Peer-Review den größten blinden Fleck — ein seltenes 5/5-Signal.

**Dissens:** Ist die Frage überhaupt richtig gestellt (First Principles/Outsider: der
manuelle Wechsel ist der Lernmechanismus) oder technisch schlicht zu verneinen
(Contrarian/Executor)? Modellwahl hängt an dieser Vorfrage: Sonnet, wenn keine
Orchestrierung stattfindet; Opus wäre nur relevant, falls doch orchestriert würde —
diese Grundlage entfällt mit der Empfehlung.

**Blind Spots (erst durch Peer-Review sichtbar):**
1. Ungeklärt, ob ein Subagent technisch überhaupt blockierend auf menschliches Feedback
   warten kann — entscheidet die ganze Debatte vor, wurde nie geprüft.
2. Die Ursache des früheren Parallel-Konflikts wurde nie diagnostiziert (welcher
   Mechanismus genau?) — ohne Diagnose bleibt "riskant" eine Vermutung.
3. Eine Mittellösung wurde nirgends durchdacht: Subagenten nur für testfreie
   Nebenarbeiten (Recherche, Doku, SPEC-Konsistenz), oder in einem isolierten
   Git-Worktree statt im gemeinsamen Arbeitsverzeichnis.

**Empfehlung:** Beim bisherigen Workflow bleiben (Projektmanager-Chat plant/dokumentiert,
Falk öffnet manuell einen neuen Chat pro Feature). Grund: Falks Ziel (Lernen + Live-
Kontrolle über Gerätetests) braucht strukturell synchrone, dialogische Zusammenarbeit,
die ein Hintergrund-Subagent nicht liefern kann, ohne den Wert des Workflows zu
verlieren. Sonnet reicht für die PM-Rolle (Planung/Doku/Konsistenzprüfung, kein
schweres Code-Reasoning); Opus lohnt sich eher situativ für einzelne komplexe
Feature-Chats selbst (z. B. Dual-Kamera-Konzeptarbeit). Falls Falk dennoch etwas
automatisieren will: Subagenten gezielt für Nebenarbeiten ohne Live-Test (SPEC-Checks,
API-Recherche, Doku-Aufräumen) einsetzen, nicht für die Kern-Feature-Implementierung.

**Der eine nächste Schritt:** Nichts umbauen. Feature 5 (Kamera-Flip bei Video) im
bewährten Workflow starten — Kickoff-Prompt liegt bereits bereit.
