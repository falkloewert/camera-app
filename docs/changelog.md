# camera-app – Änderungsprotokoll

Ausgelagert aus `docs/SPEC.md` am 2026-09-18 (Token-Sparen: Feature-Chats lesen das nicht).
Neue Einträge unten anhängen, ≤ 10 Zeilen, nur Fakten (was, Commits, Entscheidungen, offene Punkte).

## Einträge


- **2026-08-27:** Erstfassung des Dokuments erstellt, basierend auf initialem Brainstorming.
  Alle vier Kernfeatures sowie Workflow-Ansatz (feature-spezifische Claude-Code-Chats,
  zentrales Spec-Dokument als Anker) festgehalten.
- **2026-08-27:** Repo-Setup abgeschlossen: Codeberg-Repo `falkloewert/camera-app` (privat)
  angelegt, Open Camera von SourceForge (`upstream`) geklont und komplette Historie nach
  Codeberg (`origin`) gepusht. `docs/SPEC.md` und `README.md` ergänzt. Entscheidung: Falk
  delegiert Commits/Pushes für dieses Projekt vollständig an Claude. Nächster Schritt:
  Android Studio installieren, um die unveränderte Basis erstmals zu bauen.
- **2026-08-27:** Anforderung zu 4.1 ergänzt: 360°-Bilder müssen auch in Immich
  (Falks self-hosted Foto-Bibliothek) korrekt als interaktive Kugel angezeigt werden,
  nicht nur im eigenen In-App-Viewer. Bekanntes, noch ungeklärtes Problem: Immich zeigt
  360°-Bilder bei Falk aktuell nicht richtig an – Ursache wird im 360°-Feature-Chat
  untersucht, nicht jetzt.
- **2026-08-27:** Entwicklungsumgebung fertig eingerichtet und erster Build erfolgreich
  auf dem echten Pixel 9 Pro getestet: Android Studio installiert (Snap, `--classic`),
  Projekt in Android Studio geöffnet, Gradle-JDK-Konflikt behoben (Auto-Fix), USB-Debugging
  auf dem Handy aktiviert, unveränderte Open-Camera-Basis erfolgreich gebaut und gestartet.
  `applicationId` auf `de.falkloewert.cameraapp` geändert, um Signatur-Konflikte mit einer
  ggf. installierten offiziellen Open-Camera-App zu vermeiden – zweiter Testlauf danach
  ohne Deinstallations-Dialog erfolgreich. Damit ist die technische Basis vollständig
  funktionsfähig.
- **2026-08-27:** Feature-Reihenfolge gemeinsam festgelegt (siehe Abschnitt 3.1):
  Panorama-Weitwinkel → Kamera-Flip bei Video → Dual-Kamera-Machbarkeits-Spike →
  Filmchen-Modus → Dual-Kamera voll → 360°-Stitching. Kickoff-Kontext für den ersten
  Feature-Chat (Panorama mit Weitwinkel-Option) erstellt (`docs/kickoff/panorama-weitwinkel.md`).
- **2026-08-27:** Rolle & Abschlussprotokoll für Feature-Chats dokumentiert (Abschnitt
  2.1): Feature-Chats sind keine Projektmanager, benennen ihre Session selbst um
  (`camera-app – Feature: <Name>`) und folgen beim Abschluss einem festen Protokoll
  (SPEC aktualisieren, committen/pushen, Zusammenfassung an Falk).
- **2026-08-27:** Panorama-Feature-Chat gestartet (parallel zu diesem
  Projektmanager-Chat). Während der Arbeit kam Falks Wunsch nach einem app-weiten
  UI-Redesign auf (Zoom-Stufen-Buttons, Moduswahl-Leiste) – als eigenständiges Feature 6
  aufgenommen (siehe 4.5, 3.1), nicht im Panorama-Chat umgesetzt. Dabei aufgefallen und
  hier korrigiert: zwei frühere Änderungsprotokoll-Einträge waren durch einen falschen
  Bearbeitungs-Ankerpunkt versehentlich in Abschnitt 4.1 statt hier gelandet.
- **2026-08-27:** Feature 4 (Panorama mit Weitwinkel-Option) fertiggestellt und von Falk
  bestätigt. Umsetzung: direktes Linsen-Wahl-Button-Paar ("1x" / berechneter
  Weitwinkel-Faktor) im Panorama-Modus statt Dialog/Umschalt-Button, siehe Details und
  offene Fragen in 4.4. App-Name testweise auf "Falks Kamera" geändert.
  Diagnose-Logging (`MyDebug.LOG`), das während der Fehlersuche temporär aktiviert war,
  wieder deaktiviert. Panorama-Feature-Chat damit abgeschlossen.
- **2026-08-27:** Feature 6 konkretisiert: Die Zoom-Stufen-Buttons sollen sich nicht nur
  auf Panorama beschränken, sondern den generischen Kamera-Wechsel-Button im normalen
  Foto-/Video-Modus ersetzen – mit exakt demselben Verhalten wie das für Panorama
  gebaute Button-Paar (direktes Antippen statt Umschalt-Zyklus, live berechneter
  Wide-Faktor, auslöser-relative Position). Reihenfolge-Platzierung von Feature 6
  (direkt nach Feature 5) von Falk final bestätigt. Kickoff-Kontext für Feature 5
  (Video-Kameraflip) erstellt (`docs/kickoff/video-kameraflip.md`).
- **2026-09-14:** Nach 3 Wochen Pause: Council-Entscheidung zu PM-orchestrierten
  Subagenten (siehe CLAUDE.md, `docs/council/2026-09-14-subagent-workflow.md`) —
  Ergebnis: manueller Feature-Chat-Workflow bleibt bestehen. Kickoff-Kontext für
  Graphify- (Wissensgraph) und Archify- (Architektur-Diagramm) Setup erstellt
  (`docs/kickoff/graphify-archify-setup.md`), inkl. dauerhafter Pflegeregel in
  CLAUDE.md (`graphify update .` als Teil des Feature-Chat-Abschlussprotokolls).
- **2026-09-14:** Graphify/Archify-Setup abgeschlossen. Wissensgraph in
  `graphify-out/` gebaut: 4101 Knoten/15505 Kanten aus 110 Java-Dateien (kostenlos
  per AST) + 8 Projekt-Docs (ein Claude-Subagent-Durchlauf, ~100k/14k Tokens). Dabei
  aufgefallen: das Repo enthält 361 App-Icons in `res/drawable-*`/`mipmap-*`
  (5-fach dupliziert je Bildschirmdichte) plus Lizenz-Boilerplate, die für den
  Graphen wertlos sind und bei jedem `update .` erneut teure Vision-Extraktion
  ausgelöst hätten — dauerhaft per `.graphifyignore` (Repo-Root) ausgeschlossen.
  Erstes Archify-Architekturdiagramm veröffentlicht (Link in
  `docs/architecture-diagrams.md`): zeigt die bestehende Foto/Video/Panorama-Kette
  (MainActivity → MainUI/Preview/MyApplicationInterface → CameraController1/2,
  ApplicationInterface, ImageSaver, PanoramaProcessor) und markiert klar, wo die
  geplanten Modi (Filmchen, Dual-Kamera, 360°-Stitching) andocken sollen — auf
  Basis echter Repository-Evidenz (Commit `4bfc10cb`), von Archify verifiziert.
- **2026-09-14:** Falk möchte ein komplettes visuelles Redesign der App nach
  Material 3 (Google, https://m3.material.io/) statt der bisherigen Open-Camera-Optik,
  Ziel: Look der modernen Standard-Android-Kamera-App mit eigenen Zusatz-Features.
  Absorbiert die alte Feature-6-Idee (Zoom-Stufen-Buttons/Moduswahl-Leiste) als
  Teilmenge. Reihenfolge aktualisiert: Material-3-Redesign auf Position 2 vorgezogen,
  vor Feature 5 (Begründung: Design-Fundament vor UI-tragenden Folgefeatures, siehe
  4.5). Researcher-Agent für Material-3-/Repo-Gap-Analyse gestartet, Ergebnis noch
  ausstehend — Kickoff-Prompt für den Umsetzungs-Chat folgt danach.
- **2026-09-14:** Recherche-Ergebnis eingetroffen (Details in 4.5): Repo nutzt aktuell
  keine Material-Components-Bibliothek, das Redesign ist eine Ersteinführung des
  Designsystems, kein Theme-Tausch. Geschätzter Aufwand 2–3 gebündelte Feature-Chat-
  Sessions statt einer. Zwei offene Entscheidungen an Falk gestellt (dunkles
  Kamera-Chrome vs. Systemtheme; RelativeLayout→ConstraintLayout-Migration ja/nein).
- **2026-09-14:** Beide Entscheidungen von Falk getroffen (dunkles Chrome bestätigt,
  ConstraintLayout-Migration ja) und mit einer konkreten Screenshot-Referenz der
  echten Pixel-Kamera-App unterlegt (Analyse in 4.5: Farbe, Form, Zoom-Stepper,
  Moduswahl-Leiste, Settings-Bottom-Sheets, volle Einstellungsliste). Screenshots
  selbst nicht im Repo gesichert (nur im Chat geteilt) – bei Bedarf für
  Pixelgenauigkeit erneut als Dateien anfordern. Als Nächstes: gestaffelte
  Kickoff-Prompts erstellen.
- **2026-09-14:** Kickoff-Prompt für Phase 1 (Theme-Fundament + ConstraintLayout-
  Migration) erstellt (`docs/kickoff/redesign-phase1-theme-layout.md`). Phase 2
  (Zoom-Stepper + Moduswahl-Leiste) und Phase 3 (Settings/Icons) folgen nach Phase 1.
- **2026-09-15:** Phase 1 und ein großer Teil von Phase 2 umgesetzt und auf dem Pixel 9
  Pro getestet. Phase 1: `activity_main.xml` auf ConstraintLayout migriert – das war
  kein reiner XML-Tausch, weil `MainUI.layoutUI()` (~800 Zeilen) jede Schaltfläche zur
  Laufzeit über RelativeLayout-Regeln positioniert; komplett auf `ConstraintSet`
  portiert. Zwei Fehler dabei gefunden, die nur im Gerätetest auffielen:
  `ConstraintSet.clear()` löscht auch Größe und Sichtbarkeit einer Ansicht mit, und der
  unsichtbare Anker `gui_anchor` blähte sich mit `0dp` (unter ConstraintLayout =
  „fülle den Platz") auf volle Bildschirmhöhe auf und schob alle daran hängenden Icons
  vom Bildschirm. Phase 2: Zoom-Stufen-Leiste (`ZoomStepper`, Stufen ,5/1×/2/5 aus dem
  echten Zoombereich der Kamera), kontextabhängige Moduswahl-Leiste (Foto/Panorama
  bzw. Video – nie Video-Modi im Fotomodus), monochrome Bedienelemente statt der blauen
  Open-Camera-Optik, roter Punkt im Video-Auslöser, Objektiv-Auswahlknopf abgeschaltet,
  Info-Textzeilen (Uhrzeit/Speicher/ISO) standardmäßig aus. Verbindliche Bau-Vorgabe für
  den Rest: `docs/design-reference/ui-spezifikation.md` (inkl. Falks Entscheidungen zu
  Einstellungs-Blättern und der Liste dessen, was die App dafür noch nicht kann).
  Ähnlichkeit zum Original per Bildvergleich (Gemini, Screenshot gegen Screenshot):
  von 3/10 auf 6/10.
- **2026-09-15:** Offen und als Nächstes dran: (1) **Orientierungs-Modell umstellen** –
  die Pixel-Kamera ordnet beim Drehen nichts neu an, sie dreht nur die Symbole um die
  eigene Achse. Open Camera kann das bereits (`MainActivity.lock_to_landscape`), nagelt
  aber auf Querformat fest; wir bräuchten dieselbe Mechanik auf Hochformat. Das ist ein
  Eingriff in die Grundlage und betrifft auch die Ausrichtung gespeicherter Fotos/Videos.
  (2) Feinjustier-Rad für Zoom über 5×. (3) Foto- und Video-Einstellungs-Blätter.
  (4) Untere Bedienzeile, Galerie/Kamera-Wechsel im Referenz-Stil, Raster und gelber
  Fokus-Ring.
- **2026-09-17:** **Orientierungs-Modell umgebaut** (Kickoff
  `docs/kickoff/redesign-orientierung.md`, Feature-Chat „Redesign: Orientierung").
  Zwei Annahmen des Kickoffs haben sich am Gerät bzw. im Code als falsch erwiesen:
  (a) Die Pixel-Kamera nagelt das Fenster **nicht** fest – bei erzwungener
  Display-Drehung (`user_rotation`) dreht ihr Fenster mit, `screencap` liefert
  2142×960. Damit fiel Weg „Fenster festnageln + `lock_to_landscape`-Mechanik" weg
  (hätte Gesten-Balken und Popups an der falschen Kante gelassen); `lock_to_landscape`
  bleibt `false`. (b) Die `Side`-Übersetzungstabelle in `MainUI.layoutUI()` **ist
  bereits** die starre Drehung einer einzigen Anordnung (Hochformat = Querformat um 90°
  im Uhrzeigersinn) – sie musste nicht ersetzt werden. Nicht starr waren nur die
  *Inhalte* der drei LinearLayout-Leisten (Zoom-Stufen, Moduswahl, Panorama-Linsenpaar):
  Sie klappten auf senkrecht um, behielten aber die Reihenfolge. Lösung ohne den als
  fragil dokumentierten `setRotation`-Rand-Trick: neuer Helfer `MainUI.orientBar()`
  klappt um **und** kehrt im Querformat (Oberkante links) die Kinder-Reihenfolge um
  (im umgekehrten Querformat bleibt sie, das ergibt die Drehung im Uhrzeigersinn);
  Abstände zwischen Einträgen per `android:divider`/`showDividers` statt Rand-Code,
  damit sie bei umgekehrter Reihenfolge stimmen; Modus-Beschriftungen quer über eine
  kleine `RotatedTextView` (vertauscht Mess-Breite/-Höhe, zeichnet Text gedreht – Pille
  wird automatisch senkrecht, Tippen braucht keine Umrechnung); Zoom-Ziffern bleiben
  aufrecht wie in der Referenz. `ZoomStepper.update()` hängt seine Buttons über
  denselben Helfer ein (läuft nach `layoutUI()`). Nebenfund aus Phase 1: Seit dem
  Material-3-Theme wird `<Button>` als `MaterialButton` mit 24 dp Seitenabstand
  aufgeblasen, die Panorama-Linsen-Beschriftung „1x" brach im 60-dp-Kreis um – behoben
  wie beim ZoomStepper (Padding/MinSize 0). **Gerätetest Pixel 9 Pro:** Screenshots in
  Hochformat, Querformat links und rechts stimmen mit dem Referenz-Drehmodell überein
  (Gemini-Bildvergleich: Zoom-Leiste, Leserichtung, Auslöser-Spalte übereinstimmend;
  seine übrigen Punkte betreffen noch nicht gebaute Elemente). EXIF in allen vier Lagen
  korrekt (Hochformat 6, Quer links 1, Quer rechts 1 mit 180° in Pixeln, Hochformat
  auf dem Kopf 8), Video-Rotationshinweis 90° im Hochformat korrekt, Panorama-Linsenpaar
  gedreht und funktionsfähig. Hinweise: Beim Fotografieren senkrecht nach unten liefert
  der Lagesensor „unbekannt" und die letzte Drehung bleibt (Standardverhalten, auch bei
  der Pixel-Kamera). „Hochformat auf dem Kopf" bietet Android in diesem Fenstermodus
  nicht an, die Oberfläche bleibt dann in der letzten zulässigen Lage. Verbleibende
  Abweichungen (nicht orientierungsbedingt): siehe `ui-spezifikation.md` Abschnitt 8.
- **2026-09-17:** **Gesamtabgleich gegen die Detail-Spec** (Review-Chat). Falk brachte
  eine Detail-Spezifikation der Pixel-Kamera-UI mit konkreten Maßen, Farbwerten,
  Animationen und Haptik-Profilen ein; wörtlich abgelegt als
  `docs/design-reference/pixel-camera-detailspec.md` (Rang: ergänzt die Screenshot-Spec,
  bei Widerspruch gilt bis zur Entscheidung die Screenshot-Spec). Code, Layout,
  Drawables, Theme und Screenshots wurden Punkt für Punkt dagegen geprüft; Ergebnis in
  `ui-spezifikation.md` Abschnitt 7 (7.1 fehlende Bausteine, 7.2 Ist→Soll an
  vorhandenen Bausteinen mit Zeilenangaben, 7.3 Farben/Blur/Schrift) und Abschnitt 9
  (elf Spec-Konflikte K1–K11, von Falk zu entscheiden). Wichtigste Befunde: Auslöser-
  Ring ist invertiert (dunkler Rand statt weißer Ring mit Lücke), Tap-Feedback wächst
  statt zu schrumpfen, Galerie sitzt noch in der alten Icon-Leiste, Kamera-Wechsel
  klebt mit 0 dp am Auslöser, „Video" in der Moduswahl ist nur Anzeige (kein Karussell,
  kein Scrollen), Grundfläche `#1B1B1B` statt `#000000`, keinerlei Blur/Akzentfarbe/
  Schriftfamilie, Haptik nur die alten SeekBar-Ticks, Fokus weiterhin Rechteck,
  Nivellierung grün statt gelb. Nichts davon wurde in diesem Chat gebaut – nur
  dokumentiert.
- **2026-09-17 (Projektmanager-Chat):** Alle elf Spec-Konflikte aus
  `ui-spezifikation.md` Abschnitt 9 entschieden (Details dort und in 4.5 oben) –
  größtenteils Screenshot-Spec, Ausnahmen K6 (kein echter Widerspruch), K7
  (Detail-Spec) und K8 (Doppel-Tap bleibt vorerst wie heute). Schriftfamilie auf
  Roboto Flex festgelegt (Google Sans nicht frei lizenzierbar). Kickoff für
  Audit-Schritt 1 (Auslöser-Zeile) erstellt
  (`docs/kickoff/redesign-schritt1-ausloeser-zeile.md`).
- **2026-09-17 (Feature-Chat Audit-Schritt 1, Auslöser-Zeile):** Fertig und am Pixel 9 Pro
  geprüft. **Vorgehen:** Referenz-Screenshot pixelgenau vermessen (Gerät läuft mit
  960 × 2142 px bei Dichte 360 = 2,25 px/dp; Galerie und Kamera-Wechsel messen exakt 56 dp,
  was die Umrechnung bestätigt); dabei fünf Widersprüche zur Detail-Spec gefunden
  (Auslöser 74/6/86 statt 72/8/88 dp, Squircle-Radius 12 statt 22 dp, Nachbar-Abstand 124
  statt 64 dp – 64 läge im Ring –, Nachbar-Hintergrund Schwarz 60 % statt Weiß 12 %,
  Umschalter-Pille 92 × 40 / Knopf 48 statt 96 × 40 / 34 dp); Falk entschied: Messung gilt.
  **Gebaut:** `ui/ShutterButton` (Ring als Hintergrund `shutter_ring.xml`, Kern als
  `GradientDrawable` mit Feder-Morph Kreis ↔ 32-dp-Quadrat bei Videoaufnahme, Feder-Tap auf
  92 %, `androidx.dynamicanimation` kommt transitiv mit Material), Heavy Click
  (`MainUI.performHeavyClick()`, `HapticFeedbackConstants.CONFIRM`), `squircle_background.xml`
  (Radius 12 dp, `shutter_row_scrim` `#99000000`) für Kamera-Wechsel und Galerie,
  `MainUI.flankShutter()` (Nachbarn 124 dp vom Auslöser-Zentrum, auch Pause /
  Schnappschuss / Panorama-Abbruch), Galerie mit 2-dp-Rahmen per `clipToOutline`,
  `ui/PhotoVideoSwitch` (selbstzeichnende Pille, Querformat senkrecht), drei Fußzeilen auf
  Referenzhöhen (Zentren 241 / 149 / 85 dp vom Rand) jede für sich am Rand verankert.
  Alte Drawables `take_photo_*` / `take_video_*` gelöscht, `switch_video` unsichtbar.
  **Unterwegs korrigiert:** Absturz durch freigegebenes Bitmap im Cross-Fade (dann mit
  dem Cross-Fade ganz gestrichen), Wechsel-Icon zu klein (PNG-Leerraum, Padding 10,5 dp),
  Auslöser-Zeile sprang beim Fotografieren an den Rand (Moduswahl ging `GONE`, Auslöser
  hing daran – jetzt `INVISIBLE` und ohnehin nicht mehr verkettet).
  **Falks Entscheidungen im Chat:** Messung vor Detail-Spec; Umschalter „wie in der
  Referenz" = Pille in der untersten Zeile (zuerst als Moduswahl-Eintrag missverstanden);
  keine Animationen außer am Auslöser (Flug in die Galerie, Cross-Fade/Bounce, Icon-Drehung
  gestrichen); kein „Foto"/„Video" in der Moduswahl, dort nur Untermodi des aktiven
  Hauptmodus (Panorama als Umschalter, Video leer); Bedienelemente dürfen einander nie
  verschieben. Abweichungsliste: `ui-spezifikation.md` Abschnitt 8.
  **Offen für den PM-Chat:** Drehmodell K3 wieder offen (Kickoff-Vorschlag
  `docs/kickoff/rotation-pixel-modell.md`), Auslöse-Verzögerung durch Fokus-/Blitz-Standard
  (im HAL-Log belegt), Selfie-Weitwinkel (0,896× gefiltert), Panorama bei Frontkamera,
  Galerie leer nach Sperrbildschirm, Sichtbarkeit auf Schwarz, Sucher-Position (K4) –
  Details in 4.5. Tests: vier Tag-Prüfungen in `TestUtils` entfernt (Content-Description
  deckt dieselben Zustände ab). Emulator `pixel_preview` bekam eine emulierte Frontkamera
  (AVD-Konfiguration), damit der Kamera-Wechsel dort testbar ist.
  **Nachtrag:** Falk lieferte ein Video der Drehung; Frames belegen Glitsch-Zwischenzustand
  und Versatz (siehe 4.5, Punkt 1). Falk entschied: Pixel-Drehmodell als **nächster
  Feature-Chat**, Kickoff `docs/kickoff/rotation-pixel-modell.md` ausformuliert. Foto-
  Verzögerung bestand fort, weil Fokus/Blitz auf dem Gerät noch auf „auto" standen.
- **2026-09-17 (Projektmanager-Chat):** Recherche zu Milchglas/Blur-Machbarkeit
  dokumentiert (`ui-spezifikation.md` Abschnitt 7.3) – Empfehlung: erstmal 65 %
  Deckkraft ohne echten Blur, echter Blur bliebe spätere API-31+-Verbesserung.
  Kickoff für einen `ponytail-review`-Durchlauf über den eigenen Diff seit dem
  Fork-Basis-Commit vorbereitet (`docs/kickoff/ponytail-review.md`), noch nicht
  ausgeführt (Auslöser-Zeile-Chat lief zu dem Zeitpunkt noch).
  **Eigener Fehler dabei:** Ein pfadloses `git commit` hat versehentlich bereits
  von der noch laufenden Auslöser-Zeile-Sitzung vorgemerkte Datei-Löschungen (7
  alte Drawables) mit in den Ponytail-Kickoff-Commit gezogen (`0cdc22f6`). Kein
  Datenverlust, die eigentlichen unfertigen Änderungen der anderen Sitzung blieben
  unangetastet – nur die Commit-Historie ist an der Stelle unsauber. Nicht per
  Force-Push korrigiert (Risiko höher als der Nutzen). Regel dagegen in `CLAUDE.md`
  ergänzt: Commits in diesem Repo nur noch mit expliziter Dateiangabe.
- **2026-09-18 (neuer Projektmanager-Chat):** Übernahme vom Vorgänger-PM-Chat. Beim
  Abgleich `git log` ↔ SPEC festgestellt: Der im Übergabe-Prompt als „nächster
  Feature-Chat" genannte Drehmodell-Kickoff war **bereits umgesetzt** – der Feature-Chat
  „Redesign: Drehmodell" lief am 2026-09-17 nach dem letzten SPEC-Eintrag (Commits
  `8dcd8fc3`, `1a247924`, `ae011cb6` plus Folgethemen `86f3f3ec`, `00cd3e4c`, `a47d7df5`),
  hatte SPEC/ui-spezifikation aber noch nicht nachgezogen und brach vor der
  Datei-Prüfung ab (Chat archiviert). Der Vorgänger-PM-Chat konnte das nicht sehen (sein
  Arbeitsverzeichnis war `/home/falk`, nicht das Repo). Aufgeräumt: die im
  Arbeitsverzeichnis liegende, von Falk am Gerät abgenommene Toast-Entfernung als
  `9327410c` committet (Zwischenstand in 4.5 dokumentiert, PM-Liste aktualisiert: Punkt 1
  gebaut/Abschluss offen, Punkt 4 erledigt, neu Punkt 8 Tele-Verzögerung). Neuer
  Wiederaufnahme-Kickoff `docs/kickoff/rotation-pixel-modell-abschluss.md` (nur
  Datei-Prüfung + Doku, kein Neubau); alter Kickoff mit Statusvermerk versehen.
  Reihenfolge danach: Ponytail-Review (`docs/kickoff/ponytail-review.md`, braucht sauberes
  Arbeitsverzeichnis – jetzt gegeben), dann die kleineren PM-Punkte aus 4.5 priorisieren.
- **2026-09-18 (Feature-Chat „Redesign: Drehmodell (Abschluss)"):** Wiederaufnahme nach
  `docs/kickoff/rotation-pixel-modell-abschluss.md` – **nur** Datei-Prüfung und Doku, kein
  Neubau. Gerät neu gebaut/installiert (Arbeitsverzeichnis war bereits auf `HEAD`, alle
  Gradle-Tasks `UP-TO-DATE`). **Datei-Prüfung bestanden:** vier Fotos in allen Lagen
  (`exiftool -Orientation -n`: 6/1/8/3, visuell korrekt aufgerichtet, von Falk in der
  Galerie bestätigt), ein Video im Hochformat (Rotations-Tag -90°, Frame-Extraktion
  korrekt), ein Panorama (kein Orientation-Tag nötig, korrekt breit/aufrecht). Zusätzlich
  am Gerät bestätigt: Immersive-Modus, Panorama-Linsenpaar in beiden Querlagen, schnelle
  180°-Drehung ohne Hängenbleiben. Damit ist das Pixel-Drehmodell (gebaut 2026-09-17)
  vollständig abgeschlossen, Konflikt K3 endgültig entschieden. **Doku nachgezogen:**
  dieser Abschnitt, 4.5 (Feature-Tabelle, „Stand Pixel-Drehmodell", PM-Liste Punkt 1),
  `ui-spezifikation.md` Abschnitte 2 (Querformat-Aufbau neu gefasst: Fenster gesperrt statt
  mitdrehend), 6 (Technik-Absatz: `rotateGlyphs()` statt `orientBar()`/`RotatedTextView`;
  Moduswahl „Foto · Panorama"), 7.2 (Rotation als erledigt markiert), 8 („Foto"-Eintrag als
  zurückgenommen markiert, alter `RotatedTextView`-Punkt als überholt markiert, neue Punkte
  Popup/Slider drehen nicht + keine Info-Toasts), 9-K3 (endgültig entschieden), sowie
  `docs/kickoff/rotation-pixel-modell.md` (Abschluss-Commit ergänzt). Keine neuen
  Abweichungen zur Spec festgestellt – die bereits dokumentierten Entscheidungen (Popup/
  Slider drehen nicht, „Foto"-Eintrag zurück, keine Toasts) sind die einzigen bewussten
  Abweichungen und waren bereits vor diesem Chat entschieden. Kein neues Feature
  begonnen, nichts Neues beim Testen aufgefallen.
- **2026-09-18 (Projektmanager-Chat):** Ponytail-Review gelaufen (eigener Chat, Bericht
  vollständig in `docs/reviews/2026-09-18-ponytail-review.md`; der Review-Chat hatte nur
  Fund 1 als PM-Punkt 9 eingetragen). Drehmodell-Abschluss-Chat fertig (`9f6fac32`), Repo
  sauber. Nächster Feature-Chat festgelegt: **Feinjustier-Rad** vor Kopfzeile/Dock
  (Begründung PM-Punkt 10), Kickoff `docs/kickoff/redesign-feinjustier-rad.md` mit vier
  Vorab-Fragen, die Falk am echten Pixel klärt (Pillen-Anzeige nach Loslassen über 5×,
  Selfie-Stufen, Rad-Screenshots zum Messen, Morph-Animation ja/nein).
- **2026-09-18 (Feature-Chat „Redesign: Feinjustier-Rad"):** Kickoff
  `docs/kickoff/redesign-feinjustier-rad.md` umgesetzt und von Falk am Pixel 9 Pro
  abgenommen (Foto/Video/Front, Pinch, Ziehen, quer, Lautstärketasten). Vorab-Fragen am
  Gerät geklärt: (1) nach Loslassen schließt das Rad immer, der nächstliegende Slot zeigt
  den genauen Wert, Tipp darauf → Nennwert (K12); (2) Selfie: Pixel zeigt `,7 · 1×`, Falk
  wählte `,9 · 1(=1,25×) · 2 · 5`, Rad bis 10×, Öffnen auf „1"; (3) kein Rad-Screenshot
  nötig – Optik nach Detail-Spec plus gewählte Werte; (4) Morph-Animation **ja**, 120 ms
  „snappy", danach 100 ms Einblenden der Knöpfe. Commits: `73094e5f` (Pillenmaße gemessen
  48/192/44 dp, Abstand zum Ring **36 dp** – korrigiert die 24 dp aus Audit-Schritt 1;
  Open-Camera-Zoom-Text im Sucher standardmäßig aus), `e9a95d90` (Rad `ui/ZoomWheel` +
  `ui/ZoomStepper`, Badge `zoom_badge`, Haptik, Pinch zeigt das Rad, alter `zoom_seekbar`
  samt Einstellung und drei Tests entfernt, Lautstärketasten direkt auf `Preview.zoomTo()`,
  `MainUI.setSeekbarZoom()` → `onZoomChanged()`, Pille bleibt bei Videoaufnahme sichtbar,
  Videozeit dreht nicht mehr mit), `a13c5b78` (Front bis 10×, „1"-Label für 1,25×,
  „,51" → „,5", Einblenden nach dem Schrumpfen), `5a8f42a3` (Frontkamera öffnet auf „1"),
  `5a04573c` (Videozeit klein oben rechts unter der Icon-Leiste – sie lag sonst hinter der
  jetzt sichtbaren Pille).
  Gelernt: Touch-Koordinaten beim Aufklappen in Bildschirm-Koordinaten nehmen
  (`getRawX()`), weil die View unter dem Finger wächst; Stufen als Verhältnisse statt
  Listen-Indizes halten (Zoom-Liste ändert beim Videostart ihre Form, `setZoomSticky`).
  Doku: 4.5 (Stand, PM-Punkte 3 erledigt, 10 ergänzt, neu 11 Countdown-Drehung, 12
  Rad-Optik ungemessen), `ui-spezifikation.md` 3.1/3.2/3.3/6/7/8/9 (K7 umgesetzt, K12
  neu), `pixel-camera-screenshots.md` Abnahme-Punkt 2, Kickoff mit Statusvermerk.
  Ponytail-Fund 3 bewusst nicht miterledigt (Farb-Schritt). **PM-Punkt 8
  (Tele-Verzögerung, optionaler Kickoff-Punkt 7) nicht angefasst** – Falk hat nach der
  Abnahme den Abschluss gewählt; bleibt offener PM-Punkt für einen eigenen kleinen Chat.
- **2026-09-18 (Projektmanager-Chat):** Feinjustier-Rad-Chat abgeschlossen (`e2c49e47`),
  Repo sauber. Block „Einstellungen-Blatt / unterste Zeile / Kopfzeile" in drei Chats
  zerlegt (PM-Punkt 10: A Blatt-Allgemein + Dock links + Swipe-Down, B Pro + Video + Dock
  rechts, C Kopfzeile + Abschaffung Icon-Leiste/Popup) – Begründung: die Kopfzeile kann
  die Icon-Leiste erst ersetzen, wenn das Blatt deren Funktionen trägt. Kickoff für A
  erstellt (`docs/kickoff/redesign-einstellungen-blatt-foto.md`) mit vier Vorab-Fragen an
  Falk (Sheet-Screenshots zum Messen, „…"-Button → `openSettings()` als Empfehlung,
  Verhalten bei quer gehaltenem Gerät, Dock-Button im Videomodus). Reihenfolge in
  `ui-spezifikation.md` Abschnitt 7 entsprechend angepasst.
- **2026-09-20 (Feature-Chat, Modell-Benchmark „deepseek-flash"):** Kickoff
  `docs/kickoff/redesign-einstellungen-blatt-benchmark.md` umgesetzt (ganzer Blatt-Anteil von
  A+B in einem Lauf): `ui/SettingsSheet` (Foto- und Video-Blatt, Hoch-/Querformat/Kopf,
  Options-Knöpfe, „…" → `openSettings()`), linker Dock-Button ⌀ 48 dp, Wisch-Geste in
  `Preview.gestureDetector` → `ApplicationInterface.settingsSheetSwipe()`, Objektivauswahl
  „Manuell" (Zoom-Pille `UW · W · T`, Rad gesperrt, Startlinse W), 4 neue Drawables, 14
  Material-Symbol-Vektoren. Branch `bench/blatt-deepseek-flash-2` (der Wunschname
  `bench/blatt-deepseek-flash` war im anderen Worktree belegt). **Nicht am Gerät
  abgenommen.** Offen: rechter Dock-Button, Schritt C. Details: `ui-spezifikation.md`
  4/4a/4b/6/7.1/8/9 (K13).

- **2026-09-21 (Feature-Chat, Video-Blatt Runde 1):** Kickoff
  `docs/kickoff/redesign-video-blatt-runde1.md` umgesetzt: Auflösungszeile trifft nur noch
  exakt 1920×1080/3840×2160/7680×4320 (`findQuality` prüft jetzt auch die Höhe) – „FHD" war
  zuvor 1920×1920 (1:1); bei FPS-Pref „default" markiert der fps-Knopf den echten
  CamcorderProfile-Wert, die Wertzeile zeigt die Zahl statt „Standard"; Videostabilisierung
  default `true` (Code, Blatt, XML); Frontkamera ohne Auflösungs-, Makro- und
  Objektivauswahl-Zeile, gespeicherte 1:1-Videoqualität der Frontkamera wird auf 1080p
  korrigiert. Gerätetest: „FHD" → Wertzeile „1920x1080 FullHD", Selfie-Video 1920×1080,
  „Mehr" zeigt Stabilisierung „An", Rückkamera unverändert. Offen: Falk testet am Gerät.
- **2026-09-21 (Feature-Chat, Video-Blatt Runde 2):** Kickoff
  `docs/kickoff/redesign-video-blatt-runde2.md` umgesetzt: (1) Videomakro „An" blieb nicht
  stehen – `updateFocusForVideo()` (Samsung-S5-Workaround) setzte es beim Schließen/Öffnen
  der Kamera auf `continuous_video` zurück; Makro zählt dort jetzt als Video-Fokusmodus,
  und im Makromodus löst ein Tipp keinen Autofokus mehr aus (Gerätelog vor dem Fix:
  `updateFocusForVideo focus_is_video=false … ui=focus_mode_macro`). (2) Videoblitz
  „Autom." (Rückkamera) ist kein Kamerablitz mehr, sondern die Taschenlampe per
  `TYPE_LIGHT`-Sensor: an unter 10 lx, aus darüber; gespeichert bleibt „Autom.",
  `flashVideo()` pulst in diesem Zustand nicht. (3) Frontkamera-Videoblitzzeile durch
  „Selfiebeleuchtung" ersetzt (Glühbirnen-Symbole, Aus/An): `ui/SelfieLightView` malt die
  schwarzen Flächen über/unter dem Sucher weiß, Bild und Bedienelemente bleiben unberührt,
  eigener Pref `preference_video_selfie_light`. Gerätetest (Pixel 9 Pro): Makro bleibt nach
  Foto↔Video im Blatt „An"; „Autom." → Taschenlampe im Dunkeln an, Blatt bleibt „Autom.";
  Selfiebeleuchtung „An" → nur die zwei Streifen weiß. Blitz-Pref bleibt zwischen Foto und
  Video geteilt (Videokorrekturen daher `save=false`). Offen: Falk testet am Gerät.

- **2026-09-21 (Feature-Chat, Video-Blatt Runde 2, Nachtrag):** Korrektur zu Punkt 1 nach
  Falks Rückmeldung: das Ziel war nie „Makro bleibt über Foto↔Video an", sondern im
  Makromodus **nur die nahe Linse** zu benutzen und trotzdem automatisch scharfzustellen
  (vorher war das Bild unscharf und blieb es). Die Tap-Blockade aus Runde 2 ist wieder
  raus. Stattdessen pinnt `Preview.openCameraCore()` bei Videomakro die physische
  Nahlinse (`CameraControllerManager.getNearLensId()` = höchste
  `LENS_INFO_MINIMUM_FOCUS_DISTANCE`) in die Session, und `setFocusValue()` bildet
  `focus_mode_macro` im Video auf `continuous_video` ab (Makro ist am Pixel nur
  trigger-fähig, kein Dauer-AF). Blattzeile ruft `Preview.setVideoMacro()`; die Kamera
  wird nur geöffnet neu gestartet, wenn nicht aufgenommen wird.
  Gerätetest (Pixel 9 Pro): Makro „An" → aktive physische Linse 3 (Nahlinse, 2,02 mm),
  `af_mode_req=3`, AF läuft von allein; Makro „Aus" → wieder Linse 2 (Hauptlinse).
  Bekannte Grenzen: im Makromodus verschwindet die Zoom-Pille (keine Zoomstufen bei
  gepinnter Linse); Umschalten startet die Kamera neu (~1 s); läuft eine Aufnahme schon,
  greift der Pin erst beim nächsten Kamera-Öffnen. Die Nahlinse ist das Ultraweitwinkel,
  also weiter im Bildwinkel und detailärmer als die Hauptlinse.

- **2026-09-21 (Feature-Chat, Video-Blatt Runde 2, Nachtrag 2):** Makro-Zeile im Video-Blatt
  wieder **entfernt** – Falks Entscheidung: der kontinuierliche Autofokus kommt von allein
  nah genug heran, die Kamera wählt bei < ~10 cm selbst die nahe Linse, und Weitwinkel/
  Normal bleiben über die Zoom-Stufen wählbar (wie in der Pixel-App). Damit fällt der ganze
  Nachtrag-1-Apparat weg: `getNearLensId()` in `CameraControllerManager{,1,2}`, das Pinnen
  der Nahlinse in `Preview.openCameraCore()` und `setVideoMacro()`. Die Zeile in
  `setFocusValue()`, die ein altes `focus_mode_macro` im Video auf `continuous_video`
  abbildet, bleibt: sonst würde eine auf dem Gerät noch gespeicherte Makro-Einstellung die
  Kamera in den trigger-only-Makromodus schicken (genau der unscharfe Zustand von vorher).


- **2026-09-21 (Feature-Chat, Foto-Blatt Runde 1):** Kickoff `docs/kickoff/foto-blatt-fixes-runde1.md`
  abgearbeitet. Verhältnis-Sprung behoben (Blatt-Neubau nur bei echter Drehungsänderung, über
  `refresh()`), Timer-Icons 3/5/10 als Stoppuhr mit Ziffer im Ring, Makro-Zeile raus,
  Objektivauswahl als Pille `WW · NO · MA · TE`, Auflösung: Rückkamera zwei Größen der
  Verhältnis-Klasse ("2MP"/"9MP"), Selfie ohne Auswahl, Selfie-Beleuchtung statt Frontblitz,
  Video-Wertzeile nur noch in MP. **Punkt 5 am Gerät geloggt:** die Linse bleibt beim Tippen
  gepinnt (`pinned`/`contributing`/`LENS_FOCAL_LENGTH` konstant) – kein Fix nötig, Sonden wieder
  entfernt. Nicht erreichbar mit Camera2: 50 MP (nur RAW10, kein JPEG) und 4K-Selfie-Video.
- **2026-09-21 (Feature-Chat, Nachtrag):** 50 MP bleiben draußen - die Kamera bietet 8160x6144
  auf allen vier Linsen nur als RAW10 an (MaximumResolution-Session), JPEG/YUV enden bei
  4080x3072; Googles 50-MP-JPEG entsteht in deren eigener Pipeline. Stattdessen Foto-Standard
  auf das native 12-MP-Raster gelegt (`CameraController.STANDARD_PICTURE_WIDTH` = 4032):
  16:9 startet mit 4032x2268 (9 MP), 4:3 mit 4032x3024 (12 MP), die 4080er Vollauslesung bleibt
  über die vollen Einstellungen erreichbar (`standardSize()` im Blatt, Default in `Preview`).
  Review-Befund behoben: die Objektiv-Pille `WW · NO · MA · TE` erscheint nur noch im Foto -
  `getCameraIdSPhysicalPref()` liefert im Video `null`, dort gilt wieder die normale Zoom-Pille
  und die Kamera wählt die Linse selbst (die gemerkte Linse kommt beim Zurückwechseln wieder).
  Aufgeräumt: unbenutztes `ms_macro_off.xml`, String `settings_sheet_macro`, toter Kommentar
  in `CameraController2.onCaptureCompleted`.
- **2026-09-21 (Feature-Chat, Objektivwahl):** Der zweite Ultraweit-Knopf `MA` (dasselbe
  physische Objektiv wie `WW`, nur mit Makrofokus) ist weg – die Pille zeigt `WW · NO · TE`.
  Falk: „beide sollen Makro können" → die Knöpfe wählen nur noch die Linse; WW und NO kommen über
  den kontinuierlichen Autofokus von selbst bis an ihre Nahtgrenze (2 cm / ca. 10 cm).
  `CONTROL_AF_MODE_MACRO` ist wieder raus – der Test am Gerät zeigte genau den Zustand von vorher
  (trigger-only, beide Linsen unscharf), und `Lens.macro`/`updateFocusForLens()` entfallen damit.
  `Preview.updateFocus()` korrigiert ein gespeichertes Makro in Foto und Video auf den
  kontinuierlichen Fokus. `ZoomStepper.setLensSelection()` ist auf den ID-Vergleich eingekürzt,
  `macroSelected()` und der String `settings_sheet_lens_ma` entfallen.
  Davor aus dem Ponytail-Review: `Lens.view_angle`, der unbenutzte Parameter `mp_limit` und
  `Preview.light_sensor` als Feld entfernt; der `backgroundTint`-Umbau der fünf Shape-Dateien
  wurde am Gerät verworfen (Tint kam nicht an) und in `866115cd` zurückgerollt.

- **2026-09-21 (Feature-Chat, Blatt-Feinschliff):** (1) Video-Blitz „Autom." hing am
  `TYPE_LIGHT`-Sensor der Displayseite – am Gerät gemessen 152/230 lx gegen die Schwelle
  10 lx, die Lampe ging nie an. Jetzt liefert `SENSOR_SENSITIVITY` je Frame die
  Szenenhelligkeit (`CameraController.ISOChangeCallback`, `Preview.torchAutoOn()`): an ab
  ISO 800, aus unter 200, Hysterese; Startauflösung aller Modi ist 16:9, wenn nichts
  gespeichert ist; `88409de0`. (2) Kürzel-Badges (MP, FHD/4K/8K) tragen den inversen Chip
  der Referenz (Screenshots nachgemessen; die 44-dp-Breite eines Agenten-Laufs war falsch
  und ist zurückgebaut), fps zeigt die Zahl groß über der Referenz-Skala. (3) Jede Wertzeile
  mit Kürzel nennt die Auflösung in Klammern (`9MP (4032x2268)`, `FHD (1920x1080)`);
  Selfie-Video behält FHD, 3264x1836 heißt `3K` statt `6MP` (Falk-Entscheidung).
  Offen: Falk testet am Gerät.

- **2026-09-21 (PM-Chat, Blatt abgenommen und gemergt):** Blatt-Anteil von Schritt A+B am
  Pixel 9 Pro abgenommen (Falk: „ausgiebig getestet", Thema abgeschlossen). Branch
  `bench/blatt-deepseek-flash-2` (HEAD `ca3a8f3d`) per Fast-Forward nach `master` gemergt,
  damit ist der Modell-Benchmark entschieden; die installierte App stammt aus dem Worktree
  `camera-app-ab-b` (APK 2026-09-21 15:24). Verlierer-Branch `bench/blatt-deepseek-flash`
  (HEAD `2854b881`, Inhalt der Runde-2-Fixes zusätzlich in `review/alpha` `caf148b7`) lokal
  und auf `origin` gelöscht. Nächstes offenes Bauteil: rechter Dock-Button (Pro-Panel, K13),
  danach Schritt C.

- **2026-09-21 (Feature-Chat, Pro-Panel):** Rechter Dock-Button (drei Reglerstifte,
  `ms_tune`) gebaut; er öffnet `ui/ProPanel` als **Overlay über dem Sucher** (Falk), nicht
  als zweites Blatt. Enthält Info-Zeile, Regler mit eigenem Maßband (`ui/RulerSlider`) für
  Belichtung, Weißabgleich, Fokus, ISO und Verschlusszeit, je Reiter „Automatik"/
  Zurücksetzen, dazu Fokus-Peaking; schließt per Knopf, Wisch nach unten und Automatik-Knopf.
  Referenz vorher per Gemini aus dem Screenrecord ausgewertet
  (`docs/design-reference/pixel-pro-panel-beschreibung.md`: Regler 288 × 48 dp, Radius 24,
  Tab-Kachel 56 × 56 dp, aktiver Kreis ⌀ 55 dp). Gerätefunde behoben: „Unendlich" als Zeichen
  am Maßband, Belichtungsnull, Blase mittig, Zahlen gerundet. Commits `5789cc82`…`bbb5ad27`,
  von Falk am Gerät abgenommen. Offen: Schritt C (Kopfzeile, dann Icon-Leiste + Popup weg).

- **2026-09-21 (PM-Chat, Benchmark-Reste aufgeräumt):** Nach dem entschiedenen Benchmark die
  übrigen Läufe entfernt – Worktrees `camera-app-ab-a`, `camera-app-review-alpha`,
  `camera-app-review-beta` samt Branches `ab/a` (`3176b4d3`), `review/alpha` (`caf148b7`),
  `review/beta` (`d79b8488`), `ab/b` (`30049b20`) und `bench/blatt-deepseek-flash-2`; `ab/a`
  zusätzlich auf `origin`. Nur `ab/a` und `review/alpha` hatten Commits, die nicht in `master`
  stehen – die SHAs stehen hier, damit sie auffindbar bleiben (der Inhalt ist durch das
  abgenommene Blatt überholt). Einziger verbleibender Baum: `~/Projects/camera-app` auf
  `master` (die abgenommene App ist auf dem Pixel installiert).

- **2026-09-21 (Feature-Chat, Zeitraffer & Zeitlupe, SPEC 4.6):** Referenz (Screenrecord +
  4 Screenshots) zuerst per Gemini ausgewertet → `docs/design-reference/pixel-zeitraffer-
  zeitlupe-beschreibung.md`, Commit `30c02449`. Gebaut: Modusleiste `Video · Zeitlupe ·
  Zeitraffer`, Stärke-Leiste am Zoom-Pillen-Platz mit dunkler Pille im rechten Dock
  (Vorgabe 5×/1/4×), Auslöser in allen Video-Arten rot und kleiner (62 dp) mit Modussymbol,
  Videotimer zeigt `00:0X → 00:0Y`, während der Aufnahme ist die Modusleiste weg.
  Entscheidungen Falk: „Autom." entfällt, Auflösung + Bildrate in beiden Modi gestellt statt
  gewählt (Zeitraffer max. normal, Zeitlupe max. High-Speed) – nur beim Lesen übersteuert,
  die Video-Einstellungen bleiben unberührt; Blitz bleibt wählbar. Messwerte (ffprobe am
  Gerät): Zeitraffer 5× 10 s → 1,87 s (4K), Zeitlupe 1/4× 3,6 s → 14,4 s (1080p High-Speed),
  Video 4K + Ton 1:1. Commit `67afc3b3` (Modusleiste + Stärke-Leiste) und der
  Abschluss-Commit dieser Runde (Auslöser-Symbole, Zoom-Blase, automatische
  Auflösung/Bildrate), von Falk am Gerät abgenommen.

- **2026-09-22 (Feature-Chat, Schritt C Kopfzeile):** Kopfband gebaut (70,7 dp, links der
  Auflösungs-Chip, im Video 4 dp daneben die Bildrate, rechts die fünf Zustandsanzeigen) und die
  alte Icon-Leiste samt Popup entfernt (`ui/PopupView.java` gelöscht, 17 Einträge – 12 davon ohne
  Blattzeile); Verteilung nach Kickoff §2/§3: Doppeltes weg, was in der vollen
  Einstellungen-Aktivität steht, bleibt dort, das Übrige in Blätter bzw. Pro-Panel;
  Belichtungs-/Weißabgleich-Sperre, Blende und Rauschunterdrückung gestrichen, die Fotomodi
  bleiben bis zum Karussell unerreichbar (3.6). Neu: `header_fps` zeigt die Aufnahmerate aus
  `Preview.getVideoProfile()` (Zeitlupe 60/120 statt der 30 des Films, im Foto leer); Blitz-Anzeige
  immer an, wenn das Gerät einen Blitz hat (Schalter `preference_show_cycle_flash` entfällt); kein
  Makro-Symbol. Am Pixel 9 Pro abgenommen. Offen: 7.1 Punkte 6–9 + 7.3, PM-Punkte 13/14.

- **2026-09-22 (Feature-Chat, Schritte D/E/F/G):** Moduswahl als Karussell (D), Foto-Blatt mit
  **Licht-Zeile** und Video-Blitz (E/F): die Reihenfolge `nichts · auto nacht · auto blitz ·
  nacht an · blitz an` ist die einzige Schreibstelle der Lichtzustände (`MainUI.applyLightSettings`),
  genau einer ist markiert, die Nacht ist ein **Zustand** statt eines Fotomodus (nur Foto und
  Panorama, mit Nacht-Automatik nach gemessenem ISO), und wer `auto blitz` wählt, verliert das
  manuelle ISO (es strich `flash_auto` aus den Kamerawerten – Falks Befund 5/1). Panorama:
  Objektivzeile „Automatisch/Weitwinkel" in Pille statt „Manuell", keine Zoom-Pille, Nacht und
  festes Weitwinkel schließen sich aus. Schritt G räumt Falks 14 Befunde ab: Akku-Anzeige und die
  Sucher-Icon-Reihe entfallen, Gesicht/Ausrichten/Ton aus/Modus/Timer stehen als Chips im Kopfband
  (Timer tippbar aus→3→5→10), der Mond sitzt auf dem Blitz-Platz. Nacht-Panorama behält das
  Verhältnis: `Preview.pickSizeForRequirements()` hält beim Ausweichen auf Erweiterungs- oder
  Seriengrößen das Verhältnis (die Nacht-Erweiterung kann 16:9 nur bis 2688×1512, vorher wurde
  daraus 4:3). Am Pixel 9 Pro getestet; Falks eigene Abnahme von D–G offen.

- **2026-09-22 (Feature-Chat, Schritt H – Falks neun Befunde):** Licht-Zeile hält ihre Knöpfe und
  ihre Markierung aus dem gespeicherten Zustand statt aus der laufenden Sitzung (die Nacht-Knöpfe
  verschwanden und die Markierung fiel beim Tippen auf den Blitz zurück, `SettingsSheet`); alle
  Chips im Kopfband bekommen den weißen Kasten mit runden Ecken und dunkle Zeichen
  (`header_chip_background_icon.xml`, `activity_main.xml`); der RAW-Chip hängt am RAW-Zustand statt
  an einer eigenen Anzeige-Präferenz; „Nacht an" im Blatt zeigt die Mond-**Kontur**
  (`ms_moon_outline.xml`); die Zoom-Blase bleibt weg, solange das Pro-Panel offen ist;
  **Pro-Panel auch im Video** (unten rechts, nur Belichtung und Weißabgleich – `ProPanel.tabOffered()`),
  der Stärke-Knopf von Zeitraffer/Zeitlupe rückt dafür links daneben. Mitbehoben: Kamera-Neustart
  stürzte ab, wenn die Zeitlupe gespeichert war und die Kamera im Foto-Modus aufgebaut wurde
  (`VideoQualityHandler.getMaxSupportedVideoSizeHighSpeed()` lief in eine NPE). Offen: H1
  (Timer-Chip) war nicht nachstellbar, H8 (Auto-Torch in Zeitraffer/Zeitlupe) ist ungeklärt –
  siehe `docs/kickoff/redesign-schritt-h-befunde.md`.
- **2026-09-22 (Feature-Chat, Schritt I – Chip-Größe, Video-Blitz, Dock für Zeitraffer/Zeitlupe):**
  alle Chips im Kopfband auf den Kasten des Auflösungs-Chips „9 MP" (18 dp, gemeinsamer
  Hintergrund, `activity_main.xml`); **Video-Blitz nur noch Aus/Taschenlicht** – Automatik samt
  Auto-Torch-Einrichtung entfernt (`Preview.java`, `SettingsSheet.java`), damit ist H8 erledigt;
  **Google-Vorbild für Zeitraffer/Zeitlupe**: Stärke- und Pro-Knopf teilen sich die untere rechte
  Ecke, die offene Stärke-Reihe nimmt Modusleiste und Umschalter weg (`MainUI.refreshVideoSpeedRowDock()`);
  **Kamera-Neustart** beim Blitzwechsel entfällt, nur die Nacht-Erweiterung braucht ihn weiter;
  **Nacht erzwingt Blitz Aus**. Offen: RAW ist in Panorama upstream nicht erlaubt. Kickoff:
  `docs/kickoff/redesign-schritt-i-chips-video-blitz.md`.
- **2026-09-22 (Feature-Chat, Schritt J – Stärke im Pro-Panel, Taschenlampe, Panorama):** Falks
  Korrektur zu Schritt I. Der **Dock-Tausch ist wieder heraus** (`MainUI.refreshVideoSpeedRowDock()`
  entfernt) – er ließ Foto/Video-Umschalter und Modusleiste verschwinden, bis die App neu startete.
  Stattdessen öffnet der Stärke-Knopf in Zeitraffer/Zeitlupe das **Pro-Panel auf dem neuen Reiter
  „Geschwindigkeit"** (`ProPanel.TAB_SPEED`, zweiter Eintrag, Symbol wie der Knopf); die Reihe der
  Raten steht dabei über dem Auslöser, Pille und Auto-Knopf bleiben weg, und beim Schließen ist
  unten rechts alles wie vorher. **Video-Zeile „Blitz" → „Taschenlampe"** samt neuer Zeichen
  (`ms_flashlight_on/off.xml`, auch im Band-Chip). **Panorama:** der erzwungene „Blitz Aus" geht
  jetzt immer auch in die laufende Sitzung (`applyLightSettings()`; der Neuaufbau entfällt, wenn
  der Sucher noch startet – genau dort blitzte es bei Nacht-Auto), und der **RAW-Eintrag
  verschwindet** in den vollen Einstellungen, wo RAW nichts bewirkt. Kickoff:
  `docs/kickoff/redesign-schritt-j-staerke-im-pro-panel.md`.
- **2026-09-22 (Feature-Chat, Schritt K – Falks fünf Befunde zu Schritt J):** **Taschenlampen-
  Zeichen waren unsichtbar** – die y-nach-oben-Vektoren aus Schritt J brauchten
  `<group android:translateY="960">` (`ms_flashlight_on/off.xml`); jetzt in Blatt und Chip zu
  sehen. **RAW fehlt auch im Pro-Reiter des Blattes**, wo es nichts bewirkt
  (`SettingsSheet.photoProRows()`, dieselbe Frage `isRawAllowed(getTopLevelPhotoMode())`).
  **„Alle zurücksetzen" gilt jetzt im Video** (`ProPanel.tabOffered()`, Zeitlupe/Zeitraffer).
  **Der Knopf je Reiter zeigt den Zurück-Pfeil statt „Auto"** (`ms_restart_alt`, ersetzt die
  Beschriftung aus Schritt C). Nachgezogen: **Stärke-Pille und Pro-Knopf lagen in Zeitlupe/
  Zeitraffer übereinander** (Falk: „der pro einstellungsbutton ist noch kaputt"), die Pille rückt
  jetzt 72 dp nach links (`MainUI.layoutUI()`) und zeigt ihren Wert. `header_chip_background_icon.xml`
  **gelöscht**. Kickoff: `docs/kickoff/redesign-schritt-k-j-befunde.md`.

## Verlauf 4.5 Material-3-Redesign (aus SPEC 4.5 ausgelagert, 2026-09-18)

**Recherche-Ergebnis (2026-09-14, Researcher-Agent):**

- **IST-Zustand:** Das Repo nutzt aktuell **reines AppCompat 1.7.1, keine
  Material-Components-Bibliothek** (weder M2 noch M3 – verifiziert per Grep, keine
  Treffer). Kein Farbrollen-System (`colors.xml` hat nur 7 hartcodierte Hex-Werte),
  kein `themes.xml`. `activity_main.xml` ist ein flaches `RelativeLayout` mit vielen
  `ImageButton`/`Button`-Geschwistern, Positionierung teils zur Laufzeit in
  `MainUI.java` berechnet (z. B. die Panorama-Linsenwahl-Buttons aus Feature 4).
  → Das ist keine "Theme austauschen"-Aufgabe, sondern die **Ersteinführung** eines
  Designsystems.
- **AGP/Gradle:** 8.13.2/8.13 im Repo, passt zu Material Components 1.14.0
  (verlangt AGP 8.11.1+/Gradle 8.13+) – keine Vorarbeit nötig.
- **minSdk 23:** exakt die Untergrenze, die Material Components 1.14.0 verlangt –
  passt, aber ohne Puffer. Für dieses Ein-Geräte-Projekt unkritisch.
- **Empfohlene Migrationsreihenfolge** (geringstes Regressions-Risiko zuerst):
  1. `com.google.android.material:material:1.14.0` als Dependency hinzufügen.
  2. **Theme-Fundament:** `Theme.Material3.DayNight.NoActionBar` als Parent,
     Farbrollen per Material Theme Builder (m3.material.io/theme-builder) exportieren,
     `DynamicColors.applyToActivitiesIfAvailable()` (auf Pixel 9 Pro/Android 17 immer
     aktiv, da Dynamic Color API 31+ voraussetzt).
  3. **Komponente für Komponente, mit Gerätetest dazwischen:** Zoom-Stufen-Buttons
     (Panorama-Paar + Ausweitung auf Foto/Video) → `MaterialButtonToggleGroup`;
     Moduswahl-Leiste unten → `FloatingToolbarLayout` oder Chip-`RecyclerView`;
     Settings-Overlay (Drei-Punkte-Menü-Nachfolger) → `BottomSheetDialogFragment`;
     übrige Icon-Buttons restylen.
  4. Icons (Material-Symbols-konsistent) – niedrigste Priorität, kosmetisch,
     aufschiebbar.
- **Aufwandsschätzung:** mittel-hoch, realistisch **2–3 gebündelte Feature-Chat-
  Sessions** (Theme+Grundfarben / Zoom- und Moduswahl-Leiste / Settings-Overlays
  und Icons) statt ein einziger Umbau.
- **Nicht geklärt:** genauer Mechanismus des aktuellen Drei-Punkte-Settings-Menüs
  (welche Klasse rendert das Popup) – muss im jeweiligen Feature-Chat selbst
  nachgesehen werden.

**Entscheidungen (2026-09-14, von Falk getroffen):**
1. **Dunkles Kamera-Chrome:** bestätigt – fest dunkel, unabhängig vom Systemtheme,
   wie bei der echten Pixel-Kamera. Konkretisiert durch die Screenshot-Referenz unten.
2. **`RelativeLayout` → `ConstraintLayout`:** wird im Rahmen dieses Redesigns
   migriert (mehr Aufwand jetzt, sauberere Basis für spätere Features wie die
   Dual-Kamera-Bubble).

**Visuelle Referenz (Screenshots der echten Pixel-Kamera-App, von Falk am 2026-09-14
bereitgestellt):** Falk hat 9 Screenshots seiner Pixel-Kamera-App geteilt (Sucher,
Zoom-Stepper, Moduswahl-Leiste, Foto-/Video-Einstellungs-Sheets, volle
Einstellungsliste). Analyse:

- **Farbe:** durchgehend sehr dunkler, fast schwarzer Grund (kein reines Schwarz –
  eine dunkle neutrale Fläche/"Surface"-Tönung), weißer/hellgrauer Text, gedämpftes
  Grau für Sekundärtext (z. B. Einstellungs-Untertitel). Kaum gesättigte Akzentfarbe –
  "ausgewählt" wird fast durchgängig über eine **volle weiße/helle Füllung** markiert
  (Pille oder Kreis), nicht über eine Akzentfarbe. Fokusring in Gold/Gelb. Switches in
  der vollen Einstellungsliste nutzen einen gedämpften Akzentton im "An"-Zustand.
  → Bestätigt die Chrome-Entscheidung: bewusst zurückhaltend, fast monochrom,
  Farbe wird nur für Selektion/Fokus eingesetzt, nicht dekorativ.
- **Form:** durchgehend stark abgerundet – Pillen/Kapseln für Zoom-Stepper,
  Moduswahl-Highlight und Tab-Umschalter (Allgemein/Pro), Kreise für Icon-Buttons,
  Auslöser und Einstellungen-Zahnrad, große abgerundete Ecken + Ziehgriff bei den
  Bottom-Sheets.
- **Zoom-Stepper:** horizontale (im Querformat vertikale) Pillen-Leiste mit den
  Stufen ",5 / 1x / 2 / 5" – die aktive Stufe ist eine volle weiße Kreis-Pille mit
  dunkler Schrift, inaktive Stufen sind reiner heller Text auf dunklem Grund. Exakt
  das gleiche Prinzip wie unser Panorama-Button-Paar (Feature 4), nur als
  durchgängige Leiste mit mehr Stufen statt nur zwei Buttons.
- **Moduswahl-Leiste:** horizontale scrollbare Text-Zeile unter dem Auslöser
  ("Porträt · Foto · Nachtsicht · Panorama" etc.), der aktive Modus bekommt eine
  helle, abgerundete Pille als Hintergrund, alle anderen sind reiner Text ohne
  Hintergrund – kein separates Icon je Modus, nur Beschriftung.
- **Haupt-Bedienzeile:** Galerie-Miniatur (abgerundetes Quadrat) – Auslöser (großer
  voller weißer Kreis mit dunklem Ring) – Kamera-Wechsel-Button (dunkles
  abgerundetes Quadrat mit Wechsel-Icon), Auslöser deutlich größer als die beiden
  Nachbar-Elemente.
- **Einstellungen (Drei-Punkte-/Zahnrad-Menü-Nachfolger):** zwei Ebenen.
  1. Schnelleinstellungen als **Bottom-Sheet** direkt über dem Sucher (Ziehgriff,
     Titel z. B. "Fotoeinstellungen", Tab-Umschalter Allgemein/Pro bzw.
     Allgemein/Mehr), jede Zeile = Label + grauer Wert-Untertitel + eine Reihe
     kleiner Icon-Buttons rechts (ausgewählte Option = volle weiße Kreisfüllung).
  2. Vollständige Einstellungsliste als **gruppierte, scrollbare Liste** (Abschnitte
     "Allgemein", "Komposition", "Schnellzugriff", "Foto", "Video"), Zeilen mit Icon
     links, Titel + optionalem Erklärtext, Material-**Switch** rechts für An/Aus,
     Chevron für Zeilen, die zu einem Unterbildschirm führen.
- **Persistente Ecken:** oben links kleine Info-Pillen (z. B. "12 MP", Makro-Symbol),
  oben rechts ein Hilfe-"?"-Icon – alle als kleine dunkle abgerundete Chips.
- **Raster:** dünne weiße 3×3-Rasterlinien im Sucher, dezent.

**Wichtiger Hinweis zur Weiterverwendung:** Diese Screenshots wurden nur im Chat
geteilt, nicht als Dateien gespeichert – ich habe keinen Dateipfad, den ich ins Repo
committen könnte. Diese Textanalyse hier ist deshalb die einzige Form, in der die
Referenz für künftige Feature-Chats verfügbar ist (die sehen den Chat-Verlauf nicht).
**Falls pixelgenaue Places wichtig sind: Screenshots zusätzlich als Dateien schicken**,
dann lege ich sie unter `docs/design-reference/` ab, wo jeder Feature-Chat sie lesen
kann.

**Agenten-Einsatz laut Falk:** Die Analyse-/Recherche-Phase darf und soll Agenten
nutzen (reine Nebenarbeit ohne Live-Test, siehe Council-Entscheidung
`docs/council/2026-09-14-subagent-workflow.md`). Für die Umsetzung selbst hat Falk
erlaubt, dass der Feature-Chat "wenn das Sinn macht" ebenfalls Agenten einsetzt.
**Einordnung dazu:** passt zur Council-Entscheidung, solange es um eng begrenzte,
mechanische Teilschritte geht (z. B. "dieses eine Layout auf Material-3-Komponenten
umstellen") – der Feature-Chat selbst bleibt aber der durchgängige Gesprächspartner,
der erklärt und mit Falk live auf dem Gerät testet, kein reiner Verteiler an
Hintergrund-Subagenten.

**Komplexität/Risiko:** Hoch – app-weit, betrifft praktisch jede Layout-/Style-Datei,
plus die Unsicherheit, wie umfangreich eine Material-3-Migration in einem
View-basierten (nicht Compose-basierten) Altprojekt tatsächlich ist. Genauere
Einschätzung nach Abschluss der laufenden Recherche.

**Start dieses Feature-Chats:** Entscheidungen getroffen, Screenshot-Referenz liegt
vor – als Nächstes erstellt der Projektmanager-Chat die gestaffelten Kickoff-Prompts
(1. Theme/Farben/ConstraintLayout-Migration, 2. Zoom-/Moduswahl-Leiste, 3.
Settings/Icons).

**Stand Orientierungs-Modell:** siehe „Stand Pixel-Drehmodell" unten – das hier
ursprünglich beschriebene Modell (Fenster dreht mit, `MainUI.orientBar()`,
`RotatedTextView`) war ein **Zwischenstand vom 2026-09-17** und ist durch das
Pixel-Drehmodell (Fenster gesperrt, nur Glyphen drehen) ersetzt.

**Stand Gesamtabgleich (2026-09-17, Review-Chat):** Vollständiges Audit gegen die
Screenshot-Spec und die neu eingebrachte Detail-Spec
(`docs/design-reference/pixel-camera-detailspec.md`, Maße/Farben/Animationen): fertig
sind Zoom-Stufen-Pille, statische Moduswahl-Leiste und Drehmodell – grob ein Viertel.
Alles Weitere (Kopfzeile, unterste Zeile, Karussell-Mechanik, Feinjustier-Rad,
Einstellungen-Blatt, Fokus-Ring/Nivellierung, Haptik, Farben/Blur/Schrift) fehlt oder
weicht ab; Ist/Soll mit Zeilenangaben in `ui-spezifikation.md` Abschnitt 7, empfohlene
Reihenfolge dort am Anfang.

**Alle elf Spec-Konflikte entschieden (2026-09-17, Projektmanager-Chat, von Falk
bestätigt):** Details/Begründung in `ui-spezifikation.md` Abschnitt 9. Kurzfassung:
größtenteils Screenshot-Spec gewinnt (Grundregel „eins zu eins wie in der Referenz"),
Ausnahmen: K6 kein echter Widerspruch (Badge zeigt aktive Auflösung dynamisch), K7
Ruler-Hauptmarken nach Detail-Spec (mit „,5"), K8 Doppel-Tap-Verhalten bleibt vorerst
wie heute (Foto auslösen). Schriftfamilie ebenfalls geklärt: Roboto Flex statt Google
Sans (Lizenzgrund, keine Rückfrage nötig).

**Stand Audit-Schritt 1 – Auslöser-Zeile (2026-09-17, fertig):** Auslöser (transluzenter
Ring, Kern als animierbare Form, Feder-Tap, Aufnahme-Morph, Heavy Click), Kamera-Wechsel
als Squircle, Galerie-Miniatur links vom Auslöser, **Foto/Video-Umschalter-Pille** (aus
der untersten Zeile vorgezogen, ersetzt `switch_video`), Moduswahl auf „Panorama" reduziert,
alle drei Fußzeilen auf den im Referenz-Screenshot gemessenen Höhen und jede für sich
fest am Bildschirmrand. Werte durchgängig **gemessen** (2,25 px/dp) statt aus der
Detail-Spec, weil diese an fünf Stellen sichtbar abweicht (Entscheidung Falk); Liste der
Abweichungen und Falks Vereinfachungs-Entscheidungen (keine Animationen außer am
Auslöser, kein „Foto"/„Video" in der Moduswahl): `ui-spezifikation.md` Abschnitt 8. Am
Pixel 9 Pro geprüft (Hochformat pixelgenau gegen die Referenz, beide Querformate, Foto,
Video Start/Stopp, Panorama). Details: Abschnitt 6 (Eintrag 2026-09-17, Audit-Schritt 1).

**Stand Pixel-Drehmodell (2026-09-17 gebaut, 2026-09-18 abgeschlossen):** Der
Feature-Chat „Redesign: Drehmodell" hat den Kickoff `docs/kickoff/rotation-pixel-modell.md`
umgesetzt – Fenster fest auf Hochformat (`8dcd8fc3`), ein einziges festes Layout ohne
Orientierungs-Tabelle/`orientBar()`/`RotatedTextView` (`1a247924`, ~900 Zeilen weniger),
Glyphen drehen an Ort und Stelle in 300 ms mit der Referenz-Kurve (`ae011cb6`). Dazu drei
Folgethemen: „Foto"-Eintrag zurück in der Moduswahl (`86f3f3ec`, Falk revidiert die
Entscheidung aus Audit-Schritt 1 – ohne „Foto" gibt es keinen klaren Ausweg aus Panorama),
kein Panorama bei der Frontkamera (`00cd3e4c`), Bugfix Panorama-Weitwinkel nach
Kamerawechsel (`a47d7df5`), und die Entfernung der Open-Camera-Info-Toasts (`9327410c`,
von Falk am 2026-09-18 abgenommen). Entscheidungen Falk im Chat: Popup und
Belichtungs-Slider drehen nicht mit (alte Elemente, die das Redesign ersetzt);
`DrawPreview`-Overlays bleiben an Ort und Stelle.

**Datei-Prüfung (2026-09-18, Abschluss-Chat, am Gerät bestätigt):** Vier Fotos (Hochformat,
Querformat links, Querformat rechts, Hochformat auf dem Kopf) per `adb pull` geholt und mit
`exiftool -Orientation -n` geprüft – vier unterschiedliche Orientation-Werte (6/1/8/3, die
konkreten Zahlen dürfen laut Kickoff vom Vorgänger-Modell abweichen), visuell korrekt
aufgerichtet (Falk hat es zusätzlich in der Galerie bestätigt). Ein Video im Hochformat:
Rotations-Tag -90°, extrahierter Frame korrekt aufgerichtet. Ein Panorama: kein
Orientation-Tag nötig (normal bei fertig gestitchten Panoramen), Bild korrekt breit und
aufrecht. Zusätzlich am Gerät bestätigt: Immersive-Modus, Panorama-Linsenpaar in beiden
Querlagen, schnelle 180°-Drehung ohne hängenbleiben. Damit ist Konflikt K3
(`ui-spezifikation.md` Abschnitt 9) endgültig entschieden und umgesetzt; Doku nachgezogen
in Abschnitt 6 unten sowie `ui-spezifikation.md` Abschnitte 2, 6, 7.2, 8, 9-K3.

**Stand Feinjustier-Rad (2026-09-18, Feature-Chat „Redesign: Feinjustier-Rad", fertig):**
Kickoff `docs/kickoff/redesign-feinjustier-rad.md` umgesetzt, am Pixel 9 Pro abgenommen.
Zoom-Pille auf Referenzmaß (48 dp hoch, 192 dp breit, 36 dp über dem Auslöser-Ring –
gemessen; `73094e5f`), Rad `ui/ZoomWheel` + `ui/ZoomStepper` (`e9a95d90`, `a13c5b78`,
`5a8f42a3`): Ziehen auf der Pille oder Pinch → 300 × 48 dp Lineal mit logarithmischer
Skala, Hauptmarken `,5 1 2 5 10 30` (K7), Badge mit Wert, `CLOCK_TICK`, Morph in 120 ms
(Entscheidung Falk: zweite Animations-Ausnahme). Nach Loslassen zeigt der nächstliegende
Slot den genauen Wert („6,7×" im 5er-Slot; K12, am Gerät geklärt). Frontkamera
`,9 · 1(=1,25×) · 2 · 5`, Rad bis 10×, öffnet auf „1". Alter `zoom_seekbar` samt
Einstellung und Tests entfernt, Lautstärketasten direkt auf `zoomTo()`. Beifang auf Falks
Wunsch: Open-Camera-Zoom-Text im Sucher standardmäßig aus, Pille bleibt bei Videoaufnahme
sichtbar, Videozeit klein oben rechts und ohne Drehung (`DrawPreview`, `5a04573c`). Alle
Entscheidungen und selbst gewählten Werte:
`ui-spezifikation.md` Abschnitt 8 („Feinjustier-Rad"), Abschnitte 3.1/3.2/6/7 nachgezogen.
Ponytail-Fund 3 (doppelter „ausgewählt"-Stil) wurde **nicht** miterledigt – bleibt beim
Farb-Schritt.

### 4.5 Kopf (Entstehung/Ziel/Anforderungen/Einordnung, aus SPEC ausgelagert 2026-09-18)

**Entstehung/Verlauf:** Ursprünglich am 2026-08-27 als kleinerer Vorschlag aus dem
Panorama-Feature-Chat aufgenommen (Zoom-Stufen-Buttons + Moduswahl-Leiste, siehe
`docs/kickoff/entwurf-modernes-kamera-ui.md`). Am 2026-09-14 von Falk zu einem
vollständigen visuellen Redesign der App ausgeweitet: weg von der aktuellen
Open-Camera-Optik, hin zu Googles **Material-3-Designsprache**
(https://m3.material.io/), damit die App wie die moderne Standard-Android/Pixel-
Kamera-App aussieht – mit Falks eigenen Zusatz-Features. Die ursprüngliche
Zoom-Buttons/Moduswahl-Idee ist darin als Teilmenge enthalten, nicht mehr separat.

**Ziel:** Komplettes UI-Redesign nach Material 3 – Farbsystem, Typografie,
Komponenten (Buttons, Moduswahl, Icons), Elevation/Surface-Tönung, Motion – nicht nur
die zwei ursprünglich genannten Elemente.

**Ziel/Anforderungen:**
1. **Zoom-Stufen-Buttons** wie bei modernen Kamera-Apps (z. B. Google-Kamera): feste,
   antippbare Zoomstufen (z. B. "0.5x", "1x", "2x", je nach verfügbaren Linsen/Zoomstufen
   des Geräts) statt nur eines stufenlosen Reglers, die direkt auf die passende Linse
   bzw. den passenden Zoom-Faktor umschalten.
   **Konkretisierung von Falk (2026-08-27):** Das gilt explizit auch für den normalen
   Foto-/Video-Modus – dort ersetzt Open Camera die Linsenauswahl aktuell durch einen
   generischen Kamera-Wechsel-Button (Umschalt-Icon, zyklisch), den Falk nicht mag.
   Er soll sich **exakt so verhalten wie das für Panorama gebaute Button-Paar** (siehe
   4.4/Änderungsprotokoll): direkt antippbare Buttons pro Linse statt Umschalt-Zyklus,
   Wide-Faktor live aus dem tatsächlichen Sichtwinkel-Verhältnis berechnet (nicht fest
   hinterlegt), Position folgt dem Auslöser-Button bei Drehung. Die Panorama-Umsetzung
   dient als Referenzimplementierung, die auf die anderen Modi übertragen wird.
2. **Modus-Auswahl** (Foto, Video, Panorama, künftige Modi) als horizontale
   Scroll-Leiste am unteren Bildschirmrand statt über das bestehende Drei-Punkte-Menü,
   das Falk als unschön empfindet.

**Offene Frage (noch nicht von Falk entschieden, für den Feature-Chat):** Bleibt das
Drei-Punkte-Menü für andere Einstellungen bestehen und nur die Modi wandern in die neue
Leiste? Oder wird die Modus-Auswahl komplett daraus entfernt? Falk hält beides für
denkbar und überlässt die Detailentscheidung dem Feature-Chat.

**Warum als eigenes Feature statt Anhängsel an Panorama:** Betrifft die gesamte
App-UI (alle Kameramodi), nicht nur einen. Laut Projektregel (Abschnitt 2.1) nicht
Aufgabe eines einzelnen Feature-Chats, app-weite Entscheidungen zu treffen.

**Einordnung in die Reihenfolge (siehe 3.1):** am 2026-09-14 vorgezogen auf Position 2,
**vor** Feature 5 (Kamera-Flip bei Video) und allen weiteren Features. Begründung
(Falks Vorschlag, von Claude bestätigt): Ein Design-Fundament zuerst zu bauen und alle
Folge-Features direkt darauf aufzusetzen ist sinnvoller, als sie später einzeln
nachträglich umzustylen. Feature 5 ist klein genug, dass eine Verschiebung kaum
Nachteile hat; Filmchen-Modus und Dual-Kamera hätten dagegen jeweils eigene UI-Elemente
(Mini-Editor, Selfie-Bubble), die sonst zweimal gebaut würden.


### PM-Punkt 2 (geklärt 2026-09-17, aus SPEC ausgelagert)

2. **Auslöse-Verzögerung ~1–2 s – geklärt (2026-09-17):** im Kamera-Log belegt –
   Fokusmodus „auto" löst beim Tippen einen 0,7–0,9-s-Fokus-Scan aus, Blitz „auto"
   verhindert den ZSL-Puffer des Pixel-HAL („can't find a ZSL buffer … fall back to real
   time request", ~0,3 s). Mit **Fokus „Kontinuierlich" + Blitz „Aus"** kommt die
   Aufnahme-Anforderung sofort beim Tipp (gemessen). „Kontinuierlich" ist bereits der
   Open-Camera-Standard (`Preview.setFocusPref()`); das gespeicherte „auto" auf Falks
   Gerät kam von einem Tipp auf „AF" im Popup (erstes Symbol der Fokus-Zeile) – Panorama/
   Weitwinkel als Ursache am Gerät ausgeschlossen. Kein Code nötig; Blitz bleibt Falks
   Wahl („Aus"). Beim Einstellungen-Blatt (7.1 Punkt 2) entfällt die Fokus-Zeile ohnehin,
   die Pixel-App bietet keine Fokusmodus-Wahl.

- **2026-09-23 (PM-Chat):** Schritte D–K von Falk am Pixel 9 Pro abgenommen (ein Durchgang).
  Offen bleibt die Panorama-Bildzahl (fest 10, SPEC 4.5). Nächster Schritt: Sucher-Overlays,
  Kickoff `docs/kickoff/redesign-sucher-overlays.md`.

- **2026-09-24 (Schritt L, Sucher-Overlays):** gebaut und am Pixel 9 Pro abgenommen:
  Fokus-Ring (⌀ 64 dp, 1,5 dp, Signalgelb, Puls, 4 s) statt gelbem Rechteck, Drittel-Raster
  1 dp Weiß 25 % und **Standard an**, Wasserwaage Modus A + B statt grüner Winkellinie.
  **Sucher-Lage (PM-Punkt 7):** 4:3 endet auf der Linie 178 dp über der Bildschirmunterkante
  – `[0,461][960,1741]`, 16:9 unverändert; Ursache war `line_y` in Anzeige-Koordinaten,
  jetzt `MainUI.layoutUI_window_h`. Smart Chips entfallen, Regler/AE-AF-Sperre bewusst nicht
  gebaut. Kickoff `docs/kickoff/redesign-sucher-overlays.md`. Offen: Zoom-Pille **„0,5" wechselt
  nicht mehr auf die Ultraweitwinkel-Linse** (Belege und Fix-Wege in SPEC 4.5) – die Mechanik
  selbst wurde nicht angefasst. Nächster Schritt des Redesigns: 7.3 (Farben/Blur/Schrift).

- **2026-09-25 (Panorama-Nacharbeit, `8a26a0a2`):** Falk: „immernoch ein Bild zu kurz",
  „vertikal gestretcht und verzogen", Enden „nicht zusammengestitcht". Fixes: 17 Bilder +
  Außenkanten-Wächter (n·Schritt + Schritt/2), Gyro-Führung hält die Kippung vom Start,
  `createProjectedBitmap()` resampelt auch waagerecht auf die Winkelspalten (tan/Winkel liefen
  1,4° = 40 px auseinander – das war das Verzogene), `panorama()` kürzt auf genau 360° (17
  Scheiben = 3,7° Überschuss, rechter Rand trug den linken doppelt), Photosphere-Angabe ohne
  negative Crop-Werte. Panorama ist fest 4:3, Format-Zeile im Blatt im Panoramamodus
  ausgeblendet (`ui/SettingsSheet`). Am Pixel 9 Pro abgenommen: Länge/Kippung/Runde ok, die
  Naht am Ende nur „ok" – Restdrift als PM-Punkt 16 in SPEC 4.5.

- **2026-09-24/25 (Feature-Chat, Stabilitäts-Fixes):** Mehrpass-Review (Gemini) abgearbeitet,
  Kickoff `docs/kickoff/fix-stabilitaet-review.md`. A `583de612`: Blatt/Pro-Panel schließen bei
  `onPause`, gequeuter Blatt-Update abbrechen, ModeBar-Runnables, Animatoren beim Detach. B
  `0b3d4b88`: Preview-Timeout, Kamera-Übergabe unter Lock. C `08b92c70`: Null-Checks, Bluetooth-/
  Standort-Aufräumen. D `c7e280ab`: Panorama-Clamp, Pre-Shot-Cleanup, Selfie-Kippkorrektur.
  Fund 3 zurückgestellt (`ponytail:`), Fund 10 verworfen. PM-Nachtrag: Status je Fund im Review,
  doppelte PM-Nummer 15 → Naht am Panorama-Ende ist jetzt 16.
- **2026-09-25 (Schritt M, Fehlbefund):** Der in Schritt L gemeldete „0,5-Knopf wechselt nicht
  auf die Ultraweitwinkel-Linse" (PM-Punkt 15) war ein **Fehlschluss** – die Linse wechselt.
  Am Gerät ohne vorherigen Linsenknopf-Tipp geprüft: Foto 4:3 und Foto 16:9 wechseln in beiden
  Fällen sichtbar auf Ultraweitwinkel. Grund: fehlender Sitzungs-Neubau/`DestroyPipelines` ist
  beim Weg über `CONTROL_ZOOM_RATIO` normal (die HAL tauscht ohne Neubau, nur der Pin baut neu).
  Kein Code-Regress: `ui/ZoomStepper` → `Preview.zoomTo()` → `CameraController2.setZoom()` seit
  dem Rad-Commit (`e9a95d90`) unverändert, die 13 Commits seit `beacee07` fassen den Zoom-/
  Linsen-Pfad nicht an, 16:9-Standard (`88409de0`) entlastet. **Kein Code geändert.** Nicht
  geprüft: Rad-/Pinch-Zoom über 1× ↔ 0,5 (Falk: Rad bleibt wie es ist), Video, Panorama.

- **2026-10-06 (Schritt N – Farben, Blur, Schrift; letzter Redesign-Schritt):** Kickoff
  `docs/kickoff/redesign-farben-schrift.md`, am Pixel 9 Pro abgenommen („alles wie gewollt"),
  Redesign-Restliste aus `ui-spezifikation.md` 7.3 damit leer. Kopf-/Fußfläche rein schwarz;
  alle Überlagerungsflächen auf `rgba(20,20,20,0.65)` (Zoom-Pille, Badges, Maßband, Tempo-Chips,
  Stärke-Leiste), **ohne Blur** (braucht API 31+, bleibt spätere Verbesserung). Aktiver Akzent
  durchgehend Salbei `#D3E8D8` mit schwarzer Schrift, `md_dark_primary` entfernt; inaktiv
  `#C4C7C5`, Karussell/Tempo-Chips `#8E918F`. Schrift **Roboto Flex** (OFL) app-weit übers
  Theme, `BOLD` durch Gewicht 600 ersetzt (`ui/AppFont`, XML `textFontWeight`).
  `formatRatio()` formatiert fest deutsch. Ponytail-Fund 3 erledigt (`applyModeBarStyle` →
  `applyPillStyle`). Drei eigene Entscheidungen: Karussell inaktiv dunkler als die Zoom-Pille,
  Blatt-Reiter bleiben weiß, Auslöser-Scrim unverändert.

- **2026-10-07 (Panorama senkrecht + Selfie):** Kickoff `docs/kickoff/panorama-vertikal-selfie.md`,
  am Pixel 9 Pro abgenommen. Selfie-Panorama: `supportsPanorama()` ohne `FACING_FRONT`-Sperre.
  Achse automatisch aus Geräte-Orientierung (hochkant→waagerecht, quer→senkrecht), kein Umschalter;
  senkrecht projiziert der Stitcher um die liegende Achse, Photosphere-Angabe trägt die Achse
  (Pitch 0..180°), Schwenk-Limit 180°. Commits `0aa6154d`, `ff8b5800`, `61c78f4`, `5565f6e7`.

- **2026-10-11 (Feature 5 abgeblasen):** Kamera-Umschalten während laufender Videoaufnahme
  wird nicht weiterverfolgt. Am 2026-10-07 war dafür eine eigene Aufnahme-Engine auf
  `MediaCodec` begonnen worden (Schritte 1–3, ~890 Zeilen, `25e8604d`:
  `preview/MediaCodecVideoRecorder.java` neu + `Preview`/`CameraController*`), sie erwies
  sich als zu instabil und wurde in `a99399d9` vollständig zurückgenommen – im Repo liegt
  davon nichts mehr. Für später gemerkt (SPEC 4.7): nicht wieder bei MediaCodec ansetzen,
  erst prüfen, ob ein Wechsel über die vorhandene Kette geht. Komplexität war zu optimistisch
  eingeschätzt. Nebenbei Dokumentation bereinigt: Redesign-Status in SPEC 3 stand noch auf
  „Schritt N offen", obwohl am 2026-10-06 abgenommen.

- **2026-10-11 (PM-Chat, vier neue Todos von Falk):** als PM-Punkte 17–20 in SPEC 4.4/4.5
  festgehalten, damit sie nicht im Chat verloren gehen. **17** Panorama-Qualität: volle
  Auflösung statt Verkleinerung (Ursache bekannt: `max_panorama_width_c` 2080 +
  `computePanoramaSampleSize()`; erst Speicher/Dauer messen, dann zwischen größeren Rahmen,
  Rahmen auf Platte statt Heap, oder weniger Bildern wählen). **18** senkrechtes Panorama
  wird falsch verarbeitet – in der Galerie quer und gestreckt (die Abnahme vom 2026-10-07
  gilt damit nur noch für waagerecht und Selfie; erst die Zielmarken-Spiegelungen
  `33eccb13`/`13a70396` prüfen, dann Projektion und Photosphere-Angabe). **19** Fokus-Tippen
  mit Helligkeit + Warm/Kalt wie in der Google-Kamera (neues Feature 10, Bausteine sind
  vorhanden; der in Schritt L bewusst weggelassene Ring-Regler ist damit wieder aktuell).
  **20** Recherche, wie man eine fremde App reverse engineert, um Referenzwerte zu messen
  statt aus Screenshots zu schätzen.

- **2026-10-11 (Nachtrag, PM-Punkt 17 verschärft):** Falk legt volle Auflösung als **harte
  Anforderung** fest – „sonst will ich das gar nicht". Damit ist die Verkleinerung aus
  Schritt E/F (`max_panorama_width_c` 2080) überholt und PM-Punkt 17 keine Abwägung mehr;
  die Vorgabe bindet auch Feature 1 (360°-Stitching), wo dieselbe Speicherfrage mit mehr
  Bildern auftritt. Weg muss volle Auflösung liefern (Rahmen auf Platte statt Heap,
  streifenweise stitchen, oder `ImageReader`/`HardwareBuffer`), vorher messen.
