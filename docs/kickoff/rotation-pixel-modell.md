# Kickoff-Vorschlag: Drehmodell wie die Pixel-Kamera (Hochformat-Sperre, nur Glyphen drehen)

Vorschlag aus dem Feature-Chat „Audit-Schritt 1 – Auslöser-Zeile" (2026-09-17), abgelegt
für den Projektmanager-Chat gemäß SPEC.md 2.1 (app-weites Thema, nicht vom Feature-Chat
entschieden). Auslöser: Falk hat beim Vergleich mit der Pixel-App festgestellt, dass sich
beim Drehen „die gesamte UI-Hierarchie mitbewegt", und eine (fremde) Analyse samt Prompt
eingebracht. **K3 in `ui-spezifikation.md` Abschnitt 9 ist damit wieder offen.**

## Was wirklich anders ist (Diagnose gegen unseren Code geprüft)

| Behauptung der eingebrachten Analyse | Befund |
|---|---|
| „Rohe Sensordaten gehen ungedämpft an die Views", „fehlende Hysterese" | **Falsch.** Kein Sensorwert erreicht die UI. `lock_to_landscape = false` → `MainUI.onOrientationChanged()` tut nichts; die UI reagiert nur auf die diskreten System-Orientierungen (`onConfigurationChanged` → `layoutUI()`). Open Camera hat für den Sensor-Pfad sogar eine 60°-Hysterese. |
| „Falsche Drehpunkte" | Falsch, alle Views drehen um ihre Mitte. |
| „Google-App ist auf Hochformat gesperrt, nur Symbole/Texte drehen; bei uns dreht die ganze Hierarchie" | **Richtig, und das ist der ganze Unterschied.** Unsere Activity ist nicht gesperrt (kein `screenOrientation` im Manifest): Android dreht das ganze Fenster inkl. Sucher mit der Systemanimation, danach berechnet `layoutUI()` alle Positionen neu. Die Pixel-App sperrt auf Hochformat; die Anordnung klebt physisch am Gerät, jedes Symbol / jeder Text dreht sich an Ort und Stelle. |

Die **Endlagen** beider Modelle sind identisch (das war die K3-Verifikation: Vergleich der
Screenshots), der **Übergang** nicht. Das Pixel-Modell ist außerdem *einfacher* als unser
heutiges: eine feste Hochformat-Anordnung statt der Orientierungs-Tabelle (`Side`-Zuordnung)
in `layoutUI()`, kein `orientBar()`-Umstapeln, keine `RotatedTextView`-Sonderfälle – dafür
Glyph-Rotation pro View (`setViewRotation()` existiert bereits) und Sucher-Handling wie bei
Open Cameras `lock_to_landscape = true`, nur mit Hochformat als Basis.

## Aufgabe (Vorschlag)

1. Activity auf Hochformat sperren (`android:screenOrientation="portrait"` bzw. Open Cameras
   `lock_to_landscape`-Mechanik auf Hochformat umstellen). Sucher darf bei physischer Drehung
   weder neu konfiguriert noch neu gerendert werden.
2. Orientierung nur noch aus dem vorhandenen `OrientationEventListener` in vier diskreten
   Stufen (Hysterese ist da), `ui_rotation` daraus ableiten.
3. `layoutUI()` auf **eine** feste Anordnung reduzieren (die heutige Hochformat-Lage);
   pro Orientierung nur noch `setViewRotation()` auf die Blatt-Elemente (Icons, Ziffern,
   Modus-Texte, Galerie-Bild, Umschalter-Glyphen), 300 ms, `cubic-bezier(0.2,0,0,1)`
   (Detail-Spec 10). `PhotoVideoSwitch.setLayoutRotation()` und `orientBar()` entfallen.
4. Alles neu am Gerät verifizieren, was der letzte Orientierungs-Umbau verifiziert hatte:
   EXIF-Drehung von Fotos in allen vier Lagen, Video-Rotationshinweis, Panorama-Linsenpaar,
   `DrawPreview`-Texte/Overlays, Popup, Seekbars, Immersive-Modus.

## Aufwand / Risiko

Eine eigene Sitzung, vergleichbar mit dem Orientierungs-Umbau vom 2026-09-17
(Commit `2045620a`), den sie ersetzt. Risiko in der Kamera-Pipeline (Sucher-Transform,
Bildausrichtung), nicht in der Optik. Unabhängig von den übrigen Audit-Schritten – die
Auslöser-Zeile positioniert alles relativ zum Auslöser bzw. fest am Rand und übersteht
den Umbau unverändert.
