# Kickoff: Stabilitäts-Fixes aus dem Mehrpass-Review (2026-09-24)

Quelle: `docs/reviews/2026-09-24-multipass-stability-review.md` (Gemini, 6 Durchläufe, Funde
1–17). Arbeitsstand `master`. Session-Titel: `camera-app – Stabilitäts-Fixes`.

## 1. Regeln

- **Jeden Fund erst selbst im Code nachvollziehen**, bevor du ihn fixt. Die Zeilennummern
  stammen von Gemini und können verrutscht sein; zwei Erstfunde waren schon falsch. Hält ein
  Fund nicht, streichen und im Abschluss mit einem Satz Begründung aufführen.
- Bugfix = Ursache: vor dem Fix alle Aufrufer greppen, an der gemeinsamen Stelle fixen.
- Kleinster Fix, der trägt. **Kein Umbau**, keine neuen Klassen, kein Aufräumen nebenbei.
- Ein Commit je Gruppe (unten), Fund-Nummern im Commit-Text, Dateipfade explizit.

## 2. Umfang, in dieser Reihenfolge

**Gruppe A – eigener Material-3-Code (höchste Priorität, am Gerät nachstellbar):**
- 8: `SettingsSheet` und `ProPanel` in `MainActivity.onPause()` schließen (wie früher
  `closePopup()`).
- 9: `SettingsSheet.update_settings_runnable` in `close()` und beim Pausieren abbrechen.
- 6: `ModeBar` – `removeCallbacks(settle_check)` vor jedem neuen `postDelayed`.
- 7: Animatoren in `PhotoVideoSwitch` und `ZoomWheel` in `onDetachedFromWindow()` abbrechen
  (Vorlage: `RulerSlider`).

**Gruppe B – Aufhänger und Abstürze im Kern:**
- 1: `Preview` ~8312 `refreshPreviewBitmapTask.get()` mit Timeout wie der Nachbar (3000 ms);
  `ImageSaver.waitUntilDone()` in `onDestroy()` prüfen – nur begrenzen, wenn das keine
  halb gespeicherten Bilder riskiert (Datenverlust geht vor ANR, im Zweifel Falk fragen).
- 5: `CameraController2.onError()` unter `background_camera_lock`. `CameraController1` nur,
  wenn es auf dem Pixel überhaupt benutzt wird (Camera2 ist Standard) – sonst lassen.

**Gruppe C – kleine Einzeiler (Null-Checks, fehlendes `return`, Aufräumen vor frühem Return):**
12 (`SaveLocationHandler`), 13/14/15 (Bluetooth-Fernbedienung), 16 (`LocationSupplier`),
17 (`new File(dir, name)`).

**Gruppe D – Bildfehler im Upstream-Code, nur nach Rückfrage:**
2 (Panorama-Clamp), 3 (Panorama-Speicher – hängt mit der Bildzahl-Frage in SPEC 4.5
zusammen), 4 (Pre-Shots), 10/11 (Auto-Ausrichtung). Erst prüfen, ob der Fund auf dem Pixel
wirkt, dann Falk mit je einem Satz fragen, ob gefixt werden soll.

**Nicht Teil:** die neun Ponytail-Punkte (großer Umbau von Upstream-Code, eigene Entscheidung),
Security-/IPC-Analyse (im Review offen), Schritte M und N.

## 3. Gerätetest (ein Durchgang, gebündelt)

- Einstellungen-Blatt offen → App in den Hintergrund → zurück; dasselbe mit dem Pro-Panel.
- Einstellung antippen und sofort sperren.
- Karussell schnell hin und her wischen.
- Foto-/Video-Umschalter und Zoom-Rad bedienen, während die App geschlossen wird.
- Panorama bei 1× und 0,5× (gleich die offene Bildzahl-Frage aus SPEC 4.5 mit ansehen).

## 4. Abschluss (nach Falks Gerätetest)

Im Review-Dokument je Fund den Status setzen (behoben in `<commit>` / verworfen, weil … /
offen). SPEC 4.5 kurzer Eintrag, `docs/changelog.md` ≤ 10 Zeilen, `graphify update .`,
Commit mit Dateipfaden + Push, drei Zeilen für den PM-Chat.
