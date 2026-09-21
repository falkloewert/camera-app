# Kickoff: Zeitraffer & Zeitlupe (SPEC 4.6)

Zwei neue Video-Modi wie in der Pixel-App: Zeitraffer (gerafft gespeichert) und Zeitlupe
(verlangsamt gespeichert). Arbeitsstand `master` (Pro-Panel ist drin). Kein Benchmark mehr –
direkt auf `master`, Commits mit expliziten Dateipfaden, Push.
Session-Titel: `camera-app – Zeitraffer & Zeitlupe`.

## 1. Referenz auswerten (zuerst, vor jedem Code)

Falk hat Screenrecord + Screenshots in `/home/falk/Downloads/` gelegt:
`screen-20260921-205547-1790016816715.mp4` (173 MB) und
`Screenshot_20260921-205505.png`, `…-205511.png`, `…-205809.png`, `…-205813.png`.

Diese Dateien **selbst** mit Gemini auswerten (`gemini-ask -m pro "<Frage>" <datei…>` oder
den `gemini`-Subagenten), nicht Falk fragen – er hat sie absichtlich nur abgelegt. In
Abschnitten fragen, nicht alles auf einmal: (1) wo die Modi sitzen und wie man sie wählt,
(2) Auswahl der Stärke (welche Werte, wie dargestellt), (3) Beschriftungen wörtlich,
(4) was beim Starten/Stoppen und während der Aufnahme angezeigt wird, (5) Querformat,
(6) Auflösung/fps/Info-Anzeige in den Modi, (7) Verhalten nach dem Speichern (Vorschau?
Galerie?). Verlangte Ausgabe je Element: Position, Größe in px **und** dp (2,25 px/dp),
Farben als Hex, Reihenfolge, Verhalten. Ergebnis als
`docs/design-reference/pixel-zeitraffer-zeitlupe-beschreibung.md` ablegen und committen
(dauerhafte Referenz; Video und Screenshots bleiben in Downloads, nicht ins Repo).

Referenz gewinnt gegen die Text-Referenzen; zeigt sie etwas, das die App nicht kann, kurz
bei Falk nachfragen statt erfinden. Baue keine Animation/kein Element, das der Record nicht
zeigt.

## 2. Inventar, was schon da ist (Faulheits-Leiter: nichts doppelt bauen)

- Zeitraffer: Open-Camera-Unterstützung steckt in `MyApplicationInterface`, `Preview`,
  `ApplicationInterface`, `PopupView`, `DrawPreview` (Suchbegriff `TimeLapse`/`time_lapse`).
  Erst prüfen: läuft das schon, und was davon hängt am alten Popup, das abgeschafft wird?
- Zeitlupe/High-Speed: `CameraController.setVideoHighSpeed()`, `video_sizes_high_speed`,
  Preference-Keys auf `_highspeed` (`PreferenceKeys.getVideoQualityPreferenceKey(…, true)`),
  `video_high_speed` (`MyPreferenceFragment` Z. 240). Open Camera kann High-Speed-Video –
  offen ist, wie der Effekt im gespeicherten MP4 landet.

Ergebnis des Inventars (was vorhanden, was neu) gehört ins Abschlussprotokoll, damit der
nächste Chat es nicht erneut suchen muss.

## 3. Vor dem Bauen am Gerät messen (ein Log-Durchgang)

- High-Speed: unterstützte Größen und fps-Bereiche (`video_sizes_high_speed`,
  `CONTROL_AVAILABLE_HIGH_SPEED_VIDEO_CONFIGURATIONS`, `getSupportedVideoSizes` mit
  high_speed). Nicht unterstützt ⇒ Zeitlupen-Eintrag ausgrauen oder weglassen (Falk fragen),
  Zeitraffer normal bauen.
- Zeitraffer: Intervall-Bereich und Verhalten bei langer Aufnahme (Open Camera: 0,5 s … 30 min).
- Prüfen, was tatsächlich in der Datei landet: Zeitstempel/Playback-Rate des geschriebenen
  MP4 (`ffprobe` am PC) – Zeitlupe muss im Player langsam, Zeitraffer schnell abspielen,
  auch in Immich (Falk nutzt es).

Messwerte ins Abschlussprotokoll.

## 4. Bauen

- Zwei Einträge statisch in der Moduswahl ergänzen (`Foto · Panorama` → zusätzlich
  `Zeitraffer · Zeitlupe`); **kein** Karussell-Umbau, das bleibt Schritt C/7.2.
- Bedienung und Optik 1:1 nach der Referenz; vorhandene Bauteile wiederverwenden
  (`ui/SettingsSheet` für Auswahllisten, `ui/ZoomStepper`/`ui/ZoomWheel` für Knöpfe und
  Farben, Material-3-Theme). Keine neuen Farben, keine neuen Icon-Sätze.
- Zeitraffer/Zeitlupe nur für Video: Auflösung/fps aus dem Video-Blatt verwenden
  (`getVideoQualityPreferenceKey(…, high_speed)`), Selfie bleibt nutzbar.
- Beim Verlassen der Modi zurück in den normalen Video-Standard (kein hängender Intervall-/
  High-Speed-Zustand); Moduswechsel darf die gepinnte Linse und den Video-Auto-Torch (ISO)
  nicht durcheinanderbringen.

## 5. Abschluss (erst nach Falks Gerätetest)

`docs/SPEC.md` 4.6 + Übersicht/3.1, `ui-spezifikation.md` Abschnitt 5 (Moduswahl) und
7.2-Eintrag, `docs/changelog.md` ≤ 10 Zeilen, Inventar- und Messergebnisse aus Schritt 2/3,
`graphify update .`, Commit mit Dateipfaden + Push, drei Zeilen für den PM-Chat. Kein
nächstes Thema anfangen.
