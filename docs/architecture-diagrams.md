# Architektur-Diagramme (Archify)

Diagramme werden mit dem `archify`-Skill aus echter Code-Evidenz erzeugt und als
Claude-Artifact veröffentlicht (nicht im Repo abgelegt — nur der Link hier, damit
spätere Chats wissen, dass es existiert).

## camera-app Architekturübersicht (2026-09-14)

https://claude.ai/code/artifact/d8bb081f-f6cb-4057-b7bc-86b84c53f890

Zeigt die bestehende Foto/Video/Panorama-Architektur (MainActivity → MainUI/Preview/
MyApplicationInterface, Preview → CameraController1/2, MyApplicationInterface →
ApplicationInterface/ImageSaver/PanoramaProcessor) sowie, deutlich als "geplant /
noch nicht gebaut" markiert, wo die künftigen Modi aus SPEC.md Abschnitt 4
(Filmchen-Modus, Dual-Kamera, 360°-Foto-Stitching) andocken sollen. Basiert auf
Commit `4bfc10cb` (Repository-Evidenz von Archify verifiziert).

Bei architektonisch bedeutsamen Änderungen (siehe CLAUDE.md) neu generieren lassen
und diesen Eintrag durch den neuen Artifact-Link ersetzen bzw. ergänzen.
