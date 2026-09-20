# Pixel-Kamera: Video-Einstellungen-Blatt und volle Einstellungsliste (Textbeschreibung)

Von Falk am 2026-09-20 als Datei eingebracht (Beschreibung seiner Pixel-Kamera-App im
Videomodus, Querformat). Wörtlich übernommen. Welche Zeilen wir bauen, steht in
`docs/kickoff/redesign-einstellungen-blatt-benchmark.md` – Google-spezifische Funktionen
(Video-Optimierung, 10-Bit-HDR, Sprachqualität, Stabilisierungs-Stufen) entfallen.

---

Hier ist die vollständige, technische Spezifikation des Kamera-Interfaces (Landscape- und Portrait-Verhalten) zum exakten Nachbau:

## 1. Grundlegendes Viewport- & UI-Layout (Landscape-Ausrichtung)

### 1.1 Viewfinder (Sucherbereich)
 * Sucher-Fläche: Nimmt den Großteil des Bildschirms ein (3:4- bzw. 16:9-Preview) mit optionalem Raster-Overlay (3×3-Gitter).
 * Overlay-Elemente:
   * Oben links: Hilfe-Icon (eingekreistes ?).
   * Unten links: Aktiver Auflösungs-Badge (z. B. Pill-Badge mit Text FHD, 4K oder 8K).
   * Zentrum: Fokus-/Belichtungsring bei Fingertipp (inkl. Fokus-Target).
   * Dynamische Toasts / Info-Banner: Erscheinen horizontal zentriert im oberen Sucherbereich (dunkelgraue/blaue Pille, z. B. "Makrofokus ist gesperrt", "Die Optimierung des Videos kann...").

### 1.2 Haupt-Bedienelemente (Rechte Seitenleiste)
Von links nach rechts angeordnet:
 * Zoom-Leiste (Pill-Container):
   * Vertikal gestreckte Kapsel mit kreisförmigen Stufen: .5, 1x, 2, 5.
   * Die aktive Stufe wird weiß gefüllt mit schwarzer Schrift hervorgehoben.
 * Haupt-Auslöser-Spalte:
   * Oben: Kamerawechsel-Button (Rund, Pfeile im Kreis).
   * Mitte: Shutter-Button (Großer runder weißer Button).
   * Unten: Galerie-Vorschau (Rundes Thumbnail des letzten Bildes).
 * Modus-Karussell (Horizontales/Vertikales Textband):
   * Scrollbare Textliste: z. B. Nachtsichtmodus, Video, Weichzeichnen.
   * Der aktive Modus (Video) befindet sich mittig in einer weißen Pille mit dunklem Text.
 * Äußerste Kontrollleiste:
   * Oben: Pro-/Schnelleinstellungs-Icon (Schieberegler-Symbol).
   * Mitte: Foto/Video-Umschalter (Pille mit Fotoapparat- und Videokamera-Icon; das aktive Symbol ist ausgewählt).
   * Unten: Einstellungen-Zahnrad (öffnet das Schnellmenü).

## 2. Schnellmenü / Bottom-Sheet: „Videoeinstellungen"
Das Menü schiebt sich bei Klick auf das Einstellungs-Zahnrad oder Wischen von der Bedienelement-Seite als Overlay über die Steuerelemente.

### 2.1 Kopfzeile
 * Titel: Videoeinstellungen.
 * Oben rechts: ...-Menübutton (führt zu den vollständigen Kameraeinstellungen).
 * Segmented Control / Tabs: Zwei Reiter: Allgemein (aktiv, weiße Pille) und Mehr.

### 2.2 Tab 1: „Allgemein"
Jede Zeile besteht links aus Label + aktuellem Statuswert (teilweise mit Info-?) und rechts aus einer Button-Gruppe:
 * Blitz:
   * Optionen: Aus (durchgestrichener Blitz), An (Blitz-Symbol).
 * Video-Optimierung:
   * Optionen: Aus (durchgestrichene Filmklappe/Stern), An (aktive Klappe).
 * Auflösung:
   * Optionen: FHD, 4K, 8K (als zusammenhängendes Segmented Control).
 * fps (Bilder pro Sekunde):
   * Optionen: 24, 30, 60.
 * 10-Bit-HDR-Video:
   * Optionen: Aus (durchgestrichenes HDR), An (HDR).
   * Status-Feedback: Bei 60 fps oder 8K schaltet die Option auf Nicht verfügbar und wird ausgegraut.
 * Makrofokus:
   * Optionen: Aus (durchgestrichene Blume), Automatisch (Blume mit 'A'), An (Blume aktiv).

### 2.3 Tab 2: „Mehr"
 * Videostabilisierung:
   * Optionen (4 Stufen):
     * Standard (Hand-Symbol).
     * Gesperrt (Vorhängeschloss / Fokus-Rahmen; zeigt Sucher-Hinweis: "Aufnahme starten, um die Szene zu fixieren").
     * Aktiv (Laufender Mensch / Action).
     * Kinoeffekt (Schwenk-Symbol).
 * Verbesserte Sprachqualität:
   * Optionen: Aus (durchgestrichenes Profil/Sprach-Icon), An (Sprechende Person mit Wellen).

## 3. Vollständige „Kameraeinstellungen" (Detail-Screen)
Header mit Zurück-Pfeil (←) und Titel Kameraeinstellungen. Die Liste ist in 5 Kategorien unterteilt:

### 3.1 Kategorie: Allgemein
 * Standort speichern: Toggle-Switch | Subtitle: „Andere Apps sehen möglicherweise die Informationen zum Aufnahmeort von Fotos und Videos".
 * Kameratöne: Toggle-Switch.
 * Gerätespeicher: Listenelement mit Chevron > (Untermenü zur Speicherverwaltung).
 * Scannen via Kamera: Listenelement.
 * Tipps zur Bildkomposition: Toggle-Switch | Subtitle: „Tools und Tipps auf dem Bildschirm, wie beispielsweise der virtuelle Horizont und Tipps zum Fokussieren".
 * Rastertyp: Listenelement mit Chevron > | Wert: 3 × 3.
 * Selfies wie angezeigt speichern: Toggle-Switch | Subtitle: „Selfies so speichern, wie sie auf dem Display angezeigt werden".
 * Schritt-für-Schritt-Foto: Listenelement mit Chevron > | Subtitle: „Für Menschen mit Sehbehinderung oder eingeschränktem Sehvermögen...".

### 3.2 Kategorie: Schnellzugriff
 * Einstellungen für Schnellzugriff: Toggle-Switch | Subtitle: „Auf den Sucher tippen, um Weißabgleich, Helligkeit und Schatten anzupassen".
 * Aktion für Lautstärketaste: Listenelement mit Chevron > | Wert: Auslösen.
 * Selfie-Timer per Handzeichen: Toggle-Switch | Subtitle: „Halte deine Handfläche vor die Front- oder Rückkamera...".

### 3.3 Kategorie: Foto
 * Nuancenreiche Farbdarstellung in Fotos: Toggle-Switch | Subtitle: „Durch die Verwendung des Display-P3-Farbraums (DCI-P3)...".
 * Ultra HDR: Toggle-Switch | Subtitle: „Ultra HDR hellt die hellsten Bereiche von Bildern noch mehr auf...".
 * Tiefendaten für Apps für soziale Medien: Toggle-Switch.
 * Oft fotografierte Gesichter: Listenelement mit Pfeil > und Toggle-Switch.
 * Astrofotografie: Listenelement.

### 3.4 Kategorie: Video
 * Videostabilisierung: Listenelement/Toggle | Subtitle: „Du kannst Kamerabewegungen reduzieren, damit Videos schärfer werden...".
 * Audio-Zoom: Toggle-Switch | Subtitle: „Wenn du ein Objekt heranzoomst, wird der Ton verstärkt und Hintergrundgeräusche werden reduziert".
 * Videos platzsparend speichern: Toggle-Switch | Subtitle: „Du kannst statt H.264/AVC das H.265/HEVC-Format verwenden...".
 * Bilder pro Sekunde optimieren: Toggle-Switch | Subtitle: „Automatisch von 60 fps auf 30 fps anpassen, um die Aufnahme bei Bedarf zu optimieren".

### 3.5 Kategorie: Erweiterte Aufnahme
 * Timer-Licht: Toggle-Switch | Subtitle: „Licht der Kamera auf der Rückseite blinkt während des Timer-Countdowns."
 * Warnung bei verschmutzter Linse: Toggle-Switch | Subtitle: „Du siehst eine Warnmeldung, wenn das Objektiv verschmutzt zu sein scheint..."
 * Unterwasserfotos und -videos: Toggle-Switch | Subtitle: „Stellt die echten Farben wieder her, wenn du Fotos und Videos unter Wasser aufnimmst..."

### 3.6 Kategorie: Hilfe & Feedback
 * Feedback geben: Listenelement mit Sprechblasen-Icon.
 * Hilfe: Listenelement mit Fragezeichen-Icon.

## 4. UI-Verhalten & Reaktionslogik
 * Landscape vs. Portrait-Drehung:
   * Im Landscape-Modus dockt das Einstellungs-Sheet rechts an.
   * Im Portrait-Modus schiebt es sich von unten nach oben ein (klassisches Bottom Sheet).
   * Die Zoom-Pille und Modus-Schalter rotieren ihre Labels stets passend zur Geräteorientierung.
 * Gegenseitige Parameter-Abhängigkeiten:
   * Aktivieren von 8K oder 60 fps deaktiviert 10-Bit-HDR-Video (Nicht verfügbar).
   * Video-Optimierung = An blockiert bestimmte Bildraten bzw. erzwingt Cloud-Postprocessing-Hinweise im Banner.
   * Makrofokus = An sperrt manuelle Zoomstufen oder gibt bei Nicht-Fokussierbarkeit ein Toast-Feedback (Makrofokus ist gesperrt) aus.
 * Dimm-/Overlay-Logik: Sobald das Sheet geöffnet ist, wird der Viewfinder leicht abgedunkelt, bleibt aber im Hintergrund interaktiv sichtbar. Ein Klick in den freien Sucherbereich schließt das Sheet wieder.
   *(Anmerkung 2026-09-20: Die Foto-Screenshots vom 2026-09-18 zeigen **keine** Abdunklung
   des Suchers – gemessen, siehe Benchmark-Kickoff. Screenshot gilt.)*
