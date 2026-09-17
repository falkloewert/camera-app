# Detail-Spezifikation Pixel-Kamera-UI (Maße, Farben, Animationen)

Von Falk am 2026-09-17 in den Review-Chat eingebracht und hier wörtlich abgelegt (nur die
Abschnittsnummern als Markdown-Überschriften formatiert), damit die konkreten Zahlen – dp,
Farbwerte, Zeiten, Animationskurven – im Repo nachschlagbar sind.

**Rang:** Ergänzt `ui-spezifikation.md` um Werte, die aus den Screenshots nicht ablesbar
sind. Wo diese Datei der Screenshot-Spec widerspricht, gilt bis zu Falks Entscheidung die
Screenshot-Spec (Grundregel „eins zu eins wie in der Referenz"). Die Konfliktliste steht
in `ui-spezifikation.md` Abschnitt 9. Feature-Chats leiten daraus keine eigenen
Entscheidungen ab.

---

## 1. Designsprache, Farbsystem & Typografie

* Designsystem: Google Material You (Material 3). Dominanz von Pillenformen (`rounded-full`), Squircles und kreisförmigen Touch-Targets. Kontraststarkes Zusammenspiel aus absolutem Schwarz und halbtransparenten Milchglas-Layern.
* Farbpalette:
   * Hintergrund Haupt-UI (Top- & Bottom-Dock): Blickdichtes Schwarz (`#000000`).
   * Overlay-Container (Zoom-Pille, Ruler-Hintergrund, Toasts/Chips): Tiefes Anthrazit/Schwarz mit 65 % Deckkraft (`rgba(20, 20, 20, 0.65)`) kombiniert mit `backdrop-filter: blur(20px)`.
   * Aktiver Akzent / Selektion: Dynamisches Material-You-Pastell, Standard Salbeigrün/Pastell-Mint (`#C2E7D9` bis `#D3E8D8`) bzw. Hellgrau-Weiß (`#E2E3E0`).
   * Inaktive Icons & sekundäre Texte: Gedämpftes Hellgrau (`#C4C7C5` bzw. `rgba(255, 255, 255, 0.7)`).
   * Primär-Icons, Shutter-Kern, Titel: Reinweiß (`#FFFFFF`).
   * Nivellierungs-System (Leveler): Statische Achsen/Fadenkreuz in Weiß (`#FFFFFF`), dynamische Neigungsachse/Neigungskreuz in Signalgelb (`#FEE75C` / `#F9AB00`).
   * Videomodus-Akzent: Signalrot (`#E53935`).
* Typografie:
   * Schriftfamilie: Google Sans / Roboto Flex.
   * Schnitte & Größen:
      * Zoom-Labels & Ruler-Indikator: 13–15 dp, `font-weight: 600` bis `700`, zentriert.
      * Modus-Karussell: 14 dp, `font-weight: 500` (inaktiv) / `font-weight: 600` (aktiv).
      * Badges & Chips: 11–12 dp, `font-weight: 500`.
      * Zahlenformatierung: Deutsche Lokalisierung (Dezimal-Komma statt Punkt: `,5` bzw. `1,5×`).

## 2. Bildschirmaufteilung, Layout & Z-Index

* Layer 0 (Canvas-Basis): Blickdichtes Schwarz (`#000000`), Vollbild.
* Layer 1 (Sucher / Camera Preview):
   * Clipping & Maskierung: `border-radius: 28dp`, `overflow: hidden`.
   * 4:3 Foto-Format: Zentriert im Viewport zwischen Top-Bar und unterer Kontrollleiste. Ränder oben und unten bleiben frei (sichtbares Layer 0).
   * 16:9 Format / Video: Dehnt sich vertikal aus und schiebt sich halbtransparent hinter die Steuerleisten (Translucent Bar Mode).
* Layer 2 (Sucher-Overlays): Drittel-Raster, Wasserwaagen-System, Fokus-/Belichtungsringe.
* Layer 3 (Kontextuelle HUD-Elemente): Zoom-Pille / Ruler, Smart-Chips, Quick-Settings-Menü.
* Layer 4 (Bedienelemente & Navigation): Top-Bar, Shutter-Reihe, Modus-Karussell, unteres Dock.

## 3. Sucher-Overlays (Grid, Leveler, Fokus & Belichtung)

* Drittel-Raster (Grid Lines):
   * Zwei vertikale und zwei horizontale Linien mit 1 dp Stärke (`rgba(255, 255, 255, 0.25)`), die den aktiven Sucherbereich in neun Rechtecke teilen.
* Nivellierungs-System (Dual-Mode Leveler):
   * Modus A: Horizontaler Horizont (aufrechte Haltung):
      * Zweigeteilte horizontale Linie (1 px Stärke) mit statischer Mittelmarkierung und dynamisch kippender Achse inklusive digitaler Grad-Anzeige (z. B. `0°`, `-2°`).
      * Bei exakter Ausrichtung (0°) verschmelzen die Linien, leuchten Signalgelb auf und lösen haptisches Feedback aus.
   * Modus B: Top-Down / Vogelperspektive (z. B. Dokumenten- oder Bodenshot):
      * Statisches Fadenkreuz (Weiß): Fixiert im optischen Zentrum, 12×12 dp, 1.5 dp Stärke.
      * Toleranzkreis: Feiner weißer Kreisring (`radius: 34dp`, 1 dp Stärke, `rgba(255, 255, 255, 0.6)`), zentriert um das statische Kreuz.
      * Dynamisches Fadenkreuz (Gelb): 12×12 dp, 1.5 dp Stärke. Wandert mit der Geräte-Neigung.
      * Snapping: Erreicht das gelbe Kreuz die Mitte, rasten beide Fadenkreuze deckungsgleich ein, färben sich vollständig Gelb/Weiß und triggern einen Haptic-Tick.
* Fokus- und Belichtungssteuerung (Tap-to-Focus):
   * Fokus-Ring: Erscheint bei Antippen des Suchers am Berührungspunkt (`diameter: 64dp`, 1.5 dp weiße Kontur), pulsiert kurz (`scale(1.15) -> scale(1.0)`). Verschwindet automatisch nach 4 Sekunden ohne Interaktion.
   * Slider (Dual Exposure Controls): Vertikale Balken mit Sonnensymbol (Belichtungskompensation) und Thermometer (Farbtemperatur) docken seitlich an den Fokusring an.
   * AE/AF Lock: Long-Press auf den Sucher fixiert Fokus und Belichtung dauerhaft; am Fokus-Ring erscheint ein Schloss-Symbol samt Banner „AE/AF-Sperre“.

## 4. Obere Leiste (Top Bar)

* Dimensionen & Layout: Höhe 56–64 dp, oberhalb des Suchers platziert, blickdicht schwarz.
* Element Links (Format- & Qualitäts-Badges):
   * Foto: RAW- oder 50-MP-Badge (Rechteck mit `border-radius: 6dp`, 1 dp Kontur in `rgba(255, 255, 255, 0.4)`, Monospace-Schrift).
   * Video: FHD- oder 4K-Badge (Pillen-Icon, gefüllt oder konturiert).
* Element Rechts (Hilfe & Einstellungen):
   * Hilfe-Icon (Fragezeichen im Kreis `?`, 24×24 dp, Linienstärke 2 dp, Farbe Weiß).
   * Bei geöffnetem Dropdown alternativ Zahnrad-Symbol für allgemeine Einstellungen.

## 5. Zoom-Steuerungssystem (Zoom-Pille & Ruler Wheel)

* Ruhezustand (Zoom-Pille):
   * Position & Maße: Ca. 32 dp oberhalb des Hauptauslösers schwebend, Höhe 40 dp, Breite ca. 180–220 dp, Kapselform (`border-radius: 9999px`), Hintergrund `rgba(20, 20, 20, 0.65)` mit Blur.
   * Stufen-Labels: `,5` | `1` | `2` | `5` (entspricht 0.5x Ultraweit, 1x Hauptsensor, 2x Digitalzoom, 5x Tele).
   * Inaktive Stufen: Weißer Text, keine Umrandung.
   * Aktive Stufe: Gefüllter Kreis (`diameter: 36dp`), Hintergrund Salbeigrün/Hellgrau (`#D3E8D8`), Textfarbe Schwarz (`#000000`), fettes Schriftgewicht mit angehängtem `x` (z. B. `,5x`, `1x`, `2x`, `5x`).
* Aktiver Zustand (Feinjustierungs-Skala / Ruler Wheel):
   * Trigger: Gedrückthalten und horizontales Ziehen über die Zoom-Pille.
   * Morphing-Verhalten: Die Pille expandiert in einen horizontalen Scroll-Container (Breite 280–320 dp, Höhe 48 dp) mit weicher Alpha-Maske (Fade-Out links und rechts).
   * Floating Badge: Oberhalb der Skala schwebt eine separate Kapsel (`height: 28dp`, `border-radius: 9999px`, Padding horizontal 12 dp), die den Dezimalzoom zentriert über dem Fixpunkt anzeigt (z. B. `1,5×`, `2,5×`, `5,5×`).
   * Skalen-Striche (Ticks):
      * Vertikale Striche, 1.5 dp Breite an Hauptmarken (`,5`, `1`, `2`, `5`, `10`, `30`).
      * Feinere Zwischenstriche (0.75 dp) dazwischen.
      * Zahlenwerte stehen in reduzierter Deckkraft (`rgba(255, 255, 255, 0.6)`) mittig unter den Hauptmarken.
   * Zentraler Zeiger: Ein weiß leuchtender, statischer Mittelbalken, unter dem sich die Skala horizontal hindurchbewegt.

## 6. Untere Haupt-Steuerungsebene (Controls Area)

* Shutter Button (Zentrum):
   * Dimensionen: Äußerer Ring mit 88 dp Durchmesser (weiße Kontur 2 dp, transparenter Abstand nach innen von 4 dp). Innerer Vollkreis mit 72 dp Durchmesser.
   * Fotomodus: Kern in Reinweiß (`#FFFFFF`).
   * Videomodus: Kern in Signalrot (`#E53935`), der beim Start der Aufnahme mit Feder-Animation zu einem roten Quadrat mit abgerundeten Ecken (Stop-Icon, 32×32 dp, `border-radius: 8dp`) morpht.
   * Tap-Feedback: `transform: scale(0.92)` mit Spring-Rückstellung (`tension: 300, friction: 20`).
* Galerie-Vorschau (Links vom Shutter):
   * Position: Horizontal 64 dp links vom Shutter-Zentrum.
   * Form & Dimension: Squircle mit 56×56 dp und `border-radius: 22dp`, `overflow: hidden`.
   * Inhalt: Zuletzt aufgenommenes Foto/Video (`object-fit: cover`).
   * Update-Animation: Scale-Bounce (`scale(1.1) -> scale(1.0)`) kombiniert mit Cross-Fade bei neuem Medieneingang.
* Kamera-Wechsel (Rechts vom Shutter):
   * Position: Horizontal 64 dp rechts vom Shutter-Zentrum.
   * Form & Dimension: Kreisförmiger Button (`border-radius: 50%`), 56×56 dp.
   * Stil: Dunkelgrau hinterlegt (`rgba(255, 255, 255, 0.12)`).
   * Icon: Weißes Flip-Camera-Icon (zwei Pfeile im Kreis, 24×24 dp).
   * Klick-Animation: Drehung um 180° (`rotate(180deg)`) über 350 ms mit `cubic-bezier(0.4, 0.0, 0.2, 1)`.

## 7. Modus-Karussell (Horizontaler Text-Slider)

* Position: Direkt unterhalb der Haupttasten (Shutter, Galerie, Switcher).
* Format: Einzeiliges horizontales Karussell mit horizontalen Alpha-Verläufen an den Außenrändern gegen hartes Abschneiden.
* Einträge: Nachtsicht | Porträt | Foto | Video | Kinoeffekt / Pan.
* Interaktions-Zustände:
   * Inaktiv: `font-size: 14dp`, `font-weight: 500`, Farbe Grau (`#8E918F`).
   * Aktiv: Sitzt in einer Kapsel (`border-radius: 9999px`, Padding 8 dp vertikal, 16 dp horizontal). Kapsel-Hintergrund: `rgba(255, 255, 255, 0.15)`. Textfarbe: Reinweiß (`#FFFFFF`), `font-weight: 600`.
   * Snapping: Horizontales Paging mit Magnet-Effekt (Snap-to-Center). Ein Klick auf ein Nachbarwort scrollt dieses ins Zentrum.

## 8. Unterstes Navigations-Dock (System-Ebene)

* Position: Am untersten Displayrand, direkt oberhalb der Android-Gestenleiste. Gesamthöhe 52 dp.
* Element Links (Schnelleinstellungen):
   * Kreisrunde 40×40 dp Touch-Fläche (`background: rgba(255, 255, 255, 0.1)`).
   * Icon: Schieberegler-/Zahnrad-Symbol, Weiß, 22×22 dp. Öffnet ein Sheet für Timer, Aspect Ratio und Blitz.
* Element Mitte (Foto/Video Segmented Switcher):
   * Container: Dunkelgraue Pille (`#1C1B1F`), Höhe 40 dp, Breite 96 dp, `border-radius: 9999px`.
   * Icons: Fotoapparat-Icon links, Filmkamera-Icon rechts (jeweils 20×20 dp).
   * Aktiver Schieber: Weiß/Mint gefüllter Kreis (`diameter: 34dp`), der per Federungsanimation (`spring-translation`) horizontal zwischen den Hälften wechselt. Das selektierte Icon färbt sich Schwarz, das inaktive Hellgrau.
* Element Rechts (Pro-Controls):
   * Kreisrunde 40×40 dp Touch-Fläche (`background: rgba(255, 255, 255, 0.1)`).
   * Icon: Drei vertikale Reglerstifte. Öffnet das manuelle Steuerpanel für Verschlusszeit, ISO, Weißabgleich, RAW-Auswahl und Fokus-Peaking.

## 9. Kontextuelle Overlays (Smart Chips & Quick Settings Sheet)

* Smart Chips / Vorschläge:
   * Position: Zentral oberhalb der Zoom-Pille schwebend (ca. 80 dp über Shutter-Zentrum).
   * Container: Pillenform (`border-radius: 9999px`), Höhe 36 dp, Padding 6 dp vertikal, 14 dp horizontal, Hintergrund `#1E1E1E`, 1 dp Border (`rgba(255, 255, 255, 0.1)`).
   * Aufbau: Lupe-/Feature-Icon links (16×16 dp) | Hinweis-Text Mitte (12 dp, Weiß) | Schließen-Kreuz (`✕`, 14×14 dp, Hellgrau) rechts.
   * Transitions: Slide-Up mit Fade-In (`translateY(12px -> 0px)`, `opacity: 0 -> 1`), automatisches Ausblenden nach 4 Sekunden.
* Quick Settings Pull-Down-Sheet:
   * Öffnet sich durch einen vertikalen Wisch nach unten auf dem Kamerasucher oder Klick auf das linke Dock-Icon.
   * Halbtransparentes Card-Overlay über dem oberen Sucherbereich mit Optionen für Nachtsicht (Auto/Aus), Timer (Aus, 3s, 10s), Seitenverhältnis (4:3, 16:9) und Blitz (Auto, An, Aus).

## 10. Gestensteuerung, Orientierung & Haptik

* Gesten auf dem Viewport:
   * Pinch-to-Zoom: Spreizen oder Zusammenziehen mit zwei Fingern öffnet direkt das feine Ruler-Wheel.
   * Doppel-Tap: Schnelles Umschalten zwischen Front- und Rückkamera (oder wahlweise Sofort-Sprung auf 2× Digitalzoom).
   * Swipe-Down: Öffnet die Schnelleinstellungen.
   * Hardware-Tasten: Lautstärke Leiser/Lauter löst ein Foto aus (Hold startet Videoaufnahme oder Serienbild).
* Bildschirmdrehung (Auto-Rotation Handling):
   * Der eigentliche Kamerasucher bleibt geometrisch fixiert und unterbricht den Stream nicht.
   * Sämtliche Bedienelemente und Glyphen (Icons, Zoom-Zahlen, Shutter-Symbole, Quick-Settings) rotieren isoliert um ihre eigene Achse um 90° bzw. 270°.
   * Das Modus-Karussell richtet sich bei Querformat entlang der Display-Kante neu aus.
   * Rotations-Dauer: 300 ms mit Easing `cubic-bezier(0.2, 0.0, 0.0, 1.0)`.
* Haptisches Feedback (Motor-Profile):
   * Confirm / Snap (`HapticFeedbackConstants.CONFIRM`): Kräftiger Klick beim Einrasten der Horizontlinie bzw. wenn das dynamische gelbe Fadenkreuz das weiße Zentrum trifft.
   * Tick (`HapticFeedbackConstants.CLOCK_TICK`): Hochfrequentes, sehr kurzes Ticken beim Überstreichen jedes einzelnen Skalenstrichs auf dem Zoom-Ruler sowie beim Snappen im Modus-Karussell.
   * Heavy Click: Spürbarer Stoß beim Auslösen des Shutter-Buttons und Starten/Stoppen von Videoaufnahmen.
