# Kickoff Schritt J: Stärke im Pro-Panel, Taschenlampe, Panorama-Reste

Session-Titel `camera-app – Schritt J Stärke-Panel`. Falks Korrektur zu Schritt I: der
Dock-Tausch dort war falsch (Umschalter und Modusleiste verschwanden „bis ich die App neu
starte", die Einstellungen waren über den Knopf gar nicht erreichbar). Ein Gerätetest.

## A – Stärke-Knopf öffnet das Pro-Panel (Hauptpunkt)

Falks Wortlaut: *„Die geschwindigkeitseinstellung mit dem gleichen Symbol wieder. … Auslöse
oben rechts oben in der Mitte entsteht die Blase … mit den geschwindigkeitsmodis. Und wenn ich
die pro Einstellung wieder schließe? Ist unten rechts wieder wie normal."* Gewählte
Reihenfolge (Rückfrage): **`[Alle zurücksetzen] [Geschwindigkeit] |—| [Belichtung]
[Weißabgleich]`**.

Gebaut:
- neuer Reiter `ProPanel.TAB_SPEED` an Index 1 (`N_TABS` 8), Angebot über
  `MainUI.videoSpeedOffered()` (nur wo es Stärken gibt, also Zeitraffer/Zeitlupe), Symbol wie
  der Knopf (`MainUI.getVideoSpeedIcon()`), Beschriftung `pro_panel_speed`;
- `MainActivity.clickedVideoSpeed()` → `ProPanel.openSpeedSetting()` (bzw. schließen);
- `ProPanel.updatePanelContent()`: auf dem Stärke-Reiter bleibt **die Pille weg** (samt
  Automatik-Knopf), `syncSpeedBar()` zeigt die Reihe der Raten über dem Auslöser; beim
  Schließen geht sie mit. `R.id.video_speed_bar` fliegt aus `ProPanel.hideWhileOpen()`.

**Weg damit:** `speed_row_dock_hidden`, `refreshVideoSpeedRowDock()`, `proButtonRowsWithSpeed()`,
`toggleVideoSpeedBar()` – der Mechanismus aus Schritt I, der Falks Fehler verursacht hat.
Damit verschwindet die Modusleiste beim Öffnen nicht mehr, sie geht nur wie bei jedem Panel
hinter das Panel und kommt beim Schließen zurück.

## B – Video-Zeile „Blitz" → „Taschenlampe"

Name und Zeichen: Taschenlampe und durchgestrichene Taschenlampe (Material Symbols,
`ms_flashlight_on/off.xml`), auch der Band-Chip im Video
(`OnScreenIcons.updateCycleFlashIcon`) – „so wie es auch im Chip dann am Ende aussieht".

## C – Panorama: Blitz bei Nacht-Auto

Falk: *„der autoplatz löst immer im Blitz aus … obwohl es hell war"*, nach App-Neustart weg.
Ursache: der erzwungene `flash_off` ging nur in die Einstellungen; die laufende Sitzung bekam
ihn nur über den Neuaufbau, und `MainActivity.updateForSettings()` steigt aus, solange der
Sucher noch startet (`preview is still starting`) – genau das Fenster direkt nach einem
Moduswechsel. Fix: `applyLightSettings()` schiebt den Wert **immer** in die Sitzung
(`Preview.updateFlashQuiet()`).

## D – RAW in den vollen Einstellungen

Gemeint ist `preference_raw` in den Foto-Einstellungen (Rückfrage geklärt). Er verschwindet
jetzt dort, wo RAW nichts bewirkt: `PreferenceSubPhoto` fragt
`isRawAllowed(getTopLevelPhotoMode())` – dieselbe Stelle, die auch beim Speichern entscheidet
(Panorama/Porträt kein Eintrag, Nacht zählt als Foto).

## E – Prüfen am Gerät (Falk)

Zeitlupe **und** Zeitraffer: Knopf unten rechts öffnet das Panel mit
`[Alle zurücksetzen] [Geschwindigkeit] | [Belichtung] [Weißabgleich]`, die Raten stehen über
dem Auslöser, Pille/Automatik weg; **schließen** → Knopf, Modusleiste und Umschalter wieder da
(kein Neustart). Video-Blatt: „Taschenlampe", Zeichen wie im Chip. Panorama mit Nacht-Auto im
Hellen: kein Blitz. Foto-Einstellungen: RAW fehlt im Panorama, ist im Foto da.

**Offen:** darf `res/drawable/header_chip_background_icon.xml` (seit Schritt I unbenutzt)
gelöscht werden?
