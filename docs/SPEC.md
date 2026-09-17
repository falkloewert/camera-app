# Kamera-App Projekt – Spec & Roadmap

> Dieses Dokument ist die zentrale Wahrheit für das Projekt. Jeder neue Claude-Code-Chat
> (egal für welches Feature) sollte dieses Dokument zuerst lesen, um Kontext zu haben.
> Änderungen, Entscheidungen und neue Ideen werden hier nachgetragen – dieses Dokument
> wächst mit dem Projekt und wird bei jedem Fortschritt aktualisiert.

---

## 1. Projektübersicht

**Was:** Eine eigene Android-Kamera-App, basierend auf einem Fork von [Open Camera](https://opencamera.org.uk/).

**Warum:** Unzufriedenheit mit bestehenden Kamera-Apps – Kombination aus Wunsch-Features,
die es sonst nirgends zusammen gibt.

**Für wen:** Aktuell nur für den persönlichen Gebrauch. Kein Release, kein Produkt geplant.
Multi-Geräte-Support ist ein mögliches, aber unbestimmtes Zukunftsthema.

**Entwicklungsansatz:** "Vibe Coding" mit Claude Code. Der Nutzer hat keine
Programmiererfahrung, aber gute technische Grundkenntnisse und Lernbereitschaft.
→ **Wichtig für jeden Claude-Code-Chat: Schritte immer erklären, nicht nur ausführen.**

**Zielgerät:** Google Pixel 9 Pro
**Ziel-Android-Version:** Android 17 (API Level 37, stable seit 16. Juni 2026)

**Hosting:** Codeberg-Repository (`codeberg.org/falkloewert/camera-app`, privat), sauber dokumentiert.

**Basis-Repo (Upstream):** Open Camera, offizielles Quell-Repo auf SourceForge:
`https://git.code.sf.net/p/opencamera/code` (Branch `master`, Lizenz GPLv3). Im lokalen
Klon als Git-Remote `upstream` eingerichtet, um später Änderungen vom Original übernehmen
zu können (`git fetch upstream`).

**Git-Workflow-Entscheidung:** Falk möchte Commits und Pushes für dieses Projekt komplett
an Claude delegieren – er tippt diese Befehle nicht selbst, möchte aber weiterhin bei
jedem Schritt erklärt bekommen, was passiert und warum.

---

## 2. Workflow / Projektstruktur

- Dieses Dokument (`SPEC.md`) lebt im Codeberg-Repo unter `docs/SPEC.md` und ist die
  Referenz für alle Claude-Code-Sessions.
- Für jedes größere Feature wird ein **eigener, fokussierter Claude-Code-Chat** geöffnet.
  Diese Chats bekommen jeweils den relevanten Ausschnitt aus diesem Dokument als
  Kontext mit.
- Fortschritte, Entscheidungen und neu aufkommende Details aus den Feature-Chats
  werden in dieses Dokument zurückgetragen (Status-Updates, neue offene Fragen etc.).
- Rollenverteilung: Der Nutzer bringt Ideen und Feinschliff-Wünsche ein, die
  Projektmanager-Instanz (Claude, ob hier oder in Claude Code) strukturiert,
  stellt Rückfragen und leitet zur Umsetzung an. Commits/Pushes übernimmt Claude.

### 2.1 Rolle & Abschlussprotokoll für Feature-Chats

Gilt für jeden neuen, feature-spezifischen Claude-Code-Chat (nicht für diesen
Projektmanager-Chat):

- **Rolle:** Der Feature-Chat ist NICHT der Projektmanager. Er implementiert genau
  das ihm zugewiesene Feature, trifft keine Entscheidungen über andere Features,
  die Gesamt-Reihenfolge oder die Projektausrichtung – das bleibt dem
  Projektmanager-Chat vorbehalten.
- **Session-Titel:** Der Feature-Chat benennt sich zu Beginn selbst um (Tool
  `set_session_title`, `session_id: "self"`) nach dem Schema
  `camera-app – Feature: <Name>` (z. B. `camera-app – Feature: Panorama-Weitwinkel`).
  Grund: Falk öffnet für jedes Feature einen neuen Chat, die Session-Liste soll
  trotzdem übersichtlich bleiben, ohne dass er das manuell nachpflegen muss.
- **Vorgehen:** Vor Codeänderungen den relevanten Code identifizieren und Falk
  erklären, wie es aktuell funktioniert und was geändert werden soll. Änderungen in
  überschaubaren, erklärten Schritten umsetzen. Nach Änderungen von Falk auf dem
  echten Pixel 9 Pro testen lassen, bevor committet wird.
- **Nur eine Sitzung gleichzeitig aktiv im Repo:** Falk hatte bei einem anderen Projekt
  (loewert-lab-TODO) bereits einen echten Konfliktfall mit zwei parallel laufenden
  Claude-Code-Sitzungen im selben Arbeitsverzeichnis – eine Sitzung hat unbemerkt eine
  fehlerhafte Datei angelegt, ohne dass die andere davon wusste. Deshalb: Vor
  Codeänderungen immer `git status` und `git fetch` prüfen, ob eine andere Sitzung schon
  unfertige Änderungen im Arbeitsverzeichnis hat. Falls ja: diese Dateien nicht anfassen
  oder committen, nur um die eigenen Dateien herum arbeiten. App-weite Themen (nicht nur
  das eigene Feature betreffend), die während der Arbeit auffallen, werden nicht selbst
  entschieden, sondern als Vorschlag unter `docs/kickoff/` abgelegt und dem
  Projektmanager-Chat überlassen (siehe Abschnitt 4.5 als Beispiel).
- **Abschlussprotokoll**, sobald das Feature fertig und von Falk bestätigt ist:
  1. `docs/SPEC.md` aktualisieren: Status des Features setzen, Änderungsprotokoll-
     Eintrag mit Datum ergänzen (was gebaut wurde, Entscheidungen, neue offene
     Fragen für später).
  2. Finalen Commit + Push.
  3. Falk eine kurze Zusammenfassung geben, die er in den Projektmanager-Chat
     mitnehmen kann.
  4. NICHT eigenständig das nächste Feature beginnen – Reihenfolge-Entscheidungen
     laufen über den Projektmanager-Chat.

---

## 3. Feature-Übersicht (Status)

| # | Feature | Status | Komplexität |
|---|---------|--------|--------------|
| 1 | 360°-Foto-Stitching + interaktive Ansicht | Idee / Konzept | Hoch |
| 2 | Filmchen-Modus (Snapchat-Style Clip-Tagebuch) | Idee / Konzept | Mittel-Hoch |
| 3 | Dual-Kamera (Selfie-Bubble, unabhängiger Zoom) | Idee / Konzept | Hoch (geräteabhängig) |
| 4 | Panorama mit Weitwinkel-Option | **Fertig** | Niedrig-Mittel |
| 5 | Kamera während Videoaufnahme umdrehen | Idee / Notiz (nice-to-have) | Niedrig |
| 6 | Material-3-Redesign (komplettes UI, absorbiert die alte Feature-6-Idee "Zoom-Stufen-Buttons + Moduswahl-Leiste") | **In Arbeit** – Theme/ConstraintLayout, Zoom-Stufen, Moduswahl und Orientierungs-Modell fertig (Stand 2026-09-17); offen: siehe `ui-spezifikation.md` Abschnitt 7 (Audit 2026-09-17), vorher Konflikte in Abschnitt 9 entscheiden | Hoch (app-weit, Design-Fundament für alle Folge-Features) |

### 3.1 Vereinbarte Reihenfolge (aktualisiert 2026-09-14: Material-3-Redesign vor Feature 5 vorgezogen)

Kompromiss aus "Einfaches zuerst" (zum Kennenlernen von Workflow/Codebasis als
Programmier-Einsteiger) und "Riskantes früh klären" (Dual-Kamera hängt von
Tensor-Chip-Unterstützung ab, siehe Abschnitt 5) — mit einer Ausnahme: das
Design-Fundament kommt vor allem UI-tragenden Folgefeatures, damit die nicht später
nochmal überarbeitet werden müssen (Falks Begründung, von Claude bestätigt: siehe 4.5).

1. **Feature 4** – Panorama mit Weitwinkel-Option — **Fertig**
2. **Feature 6** – Material-3-Redesign (siehe 4.5)
3. **Feature 5** – Kamera während Videoaufnahme umdrehen
4. **Machbarkeits-Spike Dual-Kamera** – kein volles Feature, nur klären: unterstützt
   der Pixel 9 Pro zuverlässig gleichzeitigen Zugriff auf Front- und Rückkamera
   ("Concurrent Camera")?
5. **Feature 2** – Filmchen-Modus
6. **Feature 3** – Dual-Kamera (voller Ausbau, nach positivem Spike-Ergebnis)
7. **Feature 1** – 360°-Foto-Stitching (inkl. Immich-Kompatibilität, siehe 4.1)

---

## 4. Feature-Details

### 4.1 360°-Foto-Stitching (Photo Sphere)

**Ziel:** Echtes 360°-Kugelpanorama wie beim alten Pixel 6/7 Photo Sphere – Feature,
das bei Pixel 9 nicht mehr existiert.

**Anforderungen:**
- Echtes Stitching mehrerer Aufnahmen zu einer 360°-Kugel (kein simples Weitwinkel-Panorama)
- Interaktive Betrachtung in der App (wischen/drehen wie bei Google Street View / altem Photo Sphere)
- **Immich-Kompatibilität:** Falk nutzt Immich (self-hosted auf FalksNAS, siehe
  loewert-lab) als Google-Photos-Ersatz für seine gesamte Foto-/Videobibliothek. Normale
  Fotos/Videos aus der App landen automatisch dort (Standardformate JPEG/MP4, kein
  Sonderfall). Für 360°-Bilder speziell hat Falk aktuell ein Problem: Immich zeigt
  360°-Bilder bei ihm nicht korrekt als interaktive Kugel an. Das gespeicherte
  360°-Format muss deshalb so gewählt werden, dass Immich es erkennt und richtig
  rendert – nicht nur der eigene In-App-Viewer.

**Offene Fragen (für Feature-Chat zu klären):**
- Aufnahme-Führung: Wie wird der Nutzer beim Abfotografieren der Kugel geführt (Gitter/Overlay)?
- Welche Stitching-Bibliothek/-Methode kommt zum Einsatz (on-device, ressourcenschonend)?
- Speicherformat für 360°-Bilder (z. B. equirectangular JPEG mit XMP-GPano-Metadaten,
  dem von Google Photo Sphere etablierten Standard) – muss zusätzlich konkret gegen
  Immichs 360°-Anzeige getestet werden, da dort aktuell ein bekanntes Anzeigeproblem
  besteht (Ursache noch nicht untersucht: fehlende/falsche XMP-Metadaten vs. Immich-Bug –
  das gehört in die Recherche für den 360°-Feature-Chat, nicht in dieses Setup-Gespräch).
- Viewer: eigene Implementierung oder bestehende Bibliothek nutzen?

**Komplexität/Risiko:** Hoch – eigenständiges technisches Teilprojekt, ressourcenintensiv.

---

### 4.2 Filmchen-Modus (Snapchat-artiges Clip-Tagebuch)

**Ziel:** Über den Tag verteilt (oder länger) immer wieder kurze Videoclips zu einem
Projekt hinzufügen können, mit einfachem Nachbearbeiten am Ende.

**Kernanforderungen:**
- Modus lässt sich jederzeit öffnen/schließen, Fortschritt bleibt erhalten – auch wenn
  das Handy zwischendurch länger aus ist
- **Mehrere parallele Filmchen-Projekte** möglich – Nutzer wählt beim Öffnen aus, an
  welchem Projekt weitergearbeitet wird
- Klare Trennung von der normalen Kamera – normale Fotos/Videos landen nicht im Filmchen
- Clips bleiben **fest ihrem Projekt zugeordnet** (kein Verschieben zwischen Projekten, Stand jetzt)
- Einfacher Bearbeitungsmodus am Ende:
  - Clips umsortieren
  - Clips kürzen/trimmen
  - (weitere einfache Funktionen nach Bedarf, bewusst simpel gehalten – kein voller Editor)
- **30-Tage-Papierkorb** für gelöschte Projekte/Clips (Wiederherstellbarkeit)
- Zwei Export-Wege:
  1. **Finaler Export** als fertiges MP4 → Projektdatei/Rohdaten werden danach gelöscht (landen im Papierkorb)
  2. **Zwischenexport für externe Bearbeitung** (CapCut/InShot): Clips bleiben als einzelne
     Dateien erhalten, damit sie in externen Apps weiterverwendet werden können

**Technische Eckpunkte (Stand Planung):**
- Einzelne Clips werden als **MP4 (H.264, alternativ HEVC/H.265 für weniger Speicherbedarf)**
  gespeichert – Standardformat, kompatibel mit CapCut/InShot
- Projekt-Ebene (Reihenfolge, Trimm-Punkte, Zusammengehörigkeit der Clips) wird über eine
  **Manifest-Datei (JSON)** pro Projekt abgebildet – ähnlich einer einfachen Schnittliste
- **Wichtige Klarstellung zum Export für CapCut/InShot:** Diese Apps können keine fertigen
  Projektdateien importieren (kein offener Standard dafür). Der "Export zur Weiterbearbeitung"
  bedeutet praktisch: ein Ordner mit den einzelnen, bereits geschnittenen/sortierten
  MP4-Clip-Dateien in der richtigen Reihenfolge/Benennung, die man manuell in CapCut/InShot
  reinzieht. CapCut/InShot referenzieren dabei die Originaldateien nur (kein eigenständiges
  Kopieren) – die Clip-Dateien dürfen also nicht gelöscht werden, solange sie dort noch
  gebraucht werden ("Medium fehlt"-Problem sonst).
- Hintergrund-Persistenz (Projekt bleibt über Stunden/Tage pausierbar erhalten) erfordert
  sauberes Android-Lifecycle-Handling (Prozess kann vom System beendet werden, State muss
  robust auf Disk gespeichert werden, nicht nur im Arbeitsspeicher)

**Offene Fragen (für Feature-Chat zu klären):**
- Zeitlimit pro Clip oder pro Projekt/Tag? (aktuell: offen, kein Limit geplant)
- Darstellung der Projektübersicht beim Öffnen (Liste mit Vorschaubild, Name, Datum?)
- Genaue Umfang des Mini-Editors (welche Funktionen außer Sortieren/Kürzen sind sinnvoll?)
- Automatisches Löschen/Erinnerung nach X Tagen Inaktivität eines Projekts?

**Komplexität/Risiko:** Mittel-Hoch – vor allem wegen Persistenz/Lifecycle-Handling und
Projektverwaltung, weniger wegen der Videobearbeitung selbst.

---

### 4.3 Dual-Kamera-Modus (Snapchat-Style)

**Ziel:** Wie bei Snapchat – kleine runde "Selfie-Bubble" (Frontkamera) über dem
Hauptbild (Rückkamera), aber mit einer Verbesserung gegenüber Snapchat.

**Kernanforderung / Verbesserung gegenüber Snapchat:**
- **Unabhängiger Zoom je Kamera:** Tippt man auf die Selfie-Bubble und zoomt, wird nur
  die Frontkamera gezoomt. Tippt man auf das große Hauptbild und zoomt, wird nur die
  Rückkamera gezoomt. (Bei Snapchat aktuell nicht möglich – das ist der Hauptkritikpunkt,
  den die App lösen soll.)
- Muss nur auf dem eigenen Gerät (Pixel 9 Pro) zuverlässig laufen – kein
  Kompatibilitätsanspruch für andere Geräte in dieser Phase.

**Technische Eckpunkte (Stand Planung):**
- Erfordert gleichzeitigen Zugriff auf Front- und Rückkamera ("Concurrent Camera" API,
  seit Android 11 grundsätzlich möglich, aber geräteabhängig – muss für Pixel 9 Pro
  im Feature-Chat verifiziert werden)

**Nice-to-have, separat vom Dual-Cam-Feature:**
- Möglichkeit, während einer laufenden Videoaufnahme (im normalen Einzel-Kamera-Modus)
  zwischen Front- und Rückkamera umzuschalten

**Offene Fragen (für Feature-Chat zu klären):**
- Verifizieren: Unterstützt der Pixel 9 Pro (Tensor-Chip) Concurrent Camera zuverlässig?
- UI-Detail: Größe/Position der Bubble, verschiebbar oder fest?
- Speicherformat: Wird das Ergebnis als ein zusammengesetztes Bild/Video gespeichert,
  oder bleiben beide Streams theoretisch getrennt abrufbar?

**Komplexität/Risiko:** Hoch, stark geräte-/hardwareabhängig. Wichtigster Prüfpunkt
gleich zu Beginn des Feature-Chats.

---

### 4.4 Panorama mit Weitwinkel-Option — Status: Fertig (2026-08-27)

**Ziel:** Bei der normalen Pixel-Kamera ist im Panorama-Modus nur 1x-Zoom (Standardlinse)
wählbar, nicht die Weitwinkellinse. Das soll in der eigenen App möglich sein.

**Umgesetzt:**
- Im Panorama-Modus ersetzt ein Button-Paar ("1x" / berechneter Weitwinkel-Faktor, z. B.
  "0,5x") das normale Kamera-Wechsel-Icon (`switch_multi_camera`). Direktes Antippen
  wählt die Linse direkt, kein Umschalt-Zyklus/Dialog wie sonst in der App.
- Der Weitwinkel-Faktor wird nicht geraten, sondern aus dem tatsächlichen
  Sichtwinkel-Verhältnis der beiden physischen Linsen berechnet (`MainActivity.
  updatePanoramaLensCache()`), pro Gerät automatisch korrekt (auf dem Pixel 9 Pro: ~0,5x).
- Stufenloser Zoom bleibt im Panorama-Modus wie zuvor deaktiviert (bestehendes Verhalten,
  `MyApplicationInterface.allowZoom()` – der Stitching-Algorithmus unterstützt das nicht).
- Button-Paar folgt in Position/Ausrichtung dem Auslöser-Button (wie der Rest der App
  zwischen Hoch-/Querformat neu anordnet): im Hochformat nebeneinander über dem Auslöser
  zentriert, im Querformat übereinander links daneben – jeweils mit sichtbarem Abstand.
  Bewusste Entscheidung nach Vergleich mit einer Variante, bei der die Buttons unabhängig
  von der Ausrichtung an einer fixen Bildschirmstelle bleiben (im Querformat dann aber
  losgelöst vom Auslöser mitten im Bild) – Falk hat sich für die auslöser-relative
  Variante entschieden.

**Entscheidungen:**
- App-Name testweise auf "Falks Kamera" geändert (statt "Open Camera"), damit die eigene
  App beim Testen eindeutig von einer eventuell zusätzlich installierten offiziellen
  Open-Camera-App unterscheidbar ist. Siehe offene Frage unten.

**Offene Fragen für später:**
- Soll der App-Name dauerhaft "Falks Kamera" bleiben oder wieder zurück/auf einen anderen
  Namen geändert werden? Aktuell nur als Übergangslösung zur Testzeit gedacht.
- Zwei App-weite UI-Wünsche, die während dieses Feature-Chats aufkamen, aber bewusst
  nicht hier umgesetzt wurden, sind bereits als Feature 6 aufgenommen (siehe 4.5).

**Komplexität/Risiko:** Niedrig-Mittel – wie eingeschätzt.

---

### 4.5 Material-3-Redesign (absorbiert: Modernes Kamera-UI)

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

**Stand Orientierungs-Modell (2026-09-17, fertig):** Die Bedienoberfläche verhält sich
beim Drehen jetzt wie beim Original – eine feste Hochformat-Anordnung, die als starrer
Block mitdreht (Querformat = 90° gegen den Uhrzeigersinn, umgekehrtes Querformat = 90°
im Uhrzeigersinn). Das Fenster dreht weiterhin mit (`lock_to_landscape` bleibt `false`,
so macht es auch die Pixel-Kamera, am Gerät verifiziert). Die Positions-Zuordnung in
`MainUI.layoutUI()` war bereits diese Drehung; umgebaut wurden nur die Inhalte der
drei Leisten (`MainUI.orientBar()`, `RotatedTextView`). Details, Gerätetest-Ergebnisse
(EXIF in allen vier Lagen, Video, Panorama) und verbleibende Abweichungen: Abschnitt 6
(Eintrag 2026-09-17) und `docs/design-reference/ui-spezifikation.md` Abschnitte 2 und 8.

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

**Nächster Schritt:** Kickoff für Audit-Schritt 1 (Auslöser-Zeile: Auslöser-Ring,
Kamera-Wechsel-Form/-Abstand, Galerie-Miniatur an der richtigen Position, Haptik)
erstellt.

---

## 5. Generelle technische Hinweise / Risiken (projektübergreifend)

- **Basis:** Open Camera (Java, aktiv gepflegt) als Fork – guter Startpunkt, da bereits
  strukturierte Kamera-Grundfunktionalität vorhanden ist.
- Concurrent-Camera-Funktionalität (Feature 4.3) ist der risikoreichste Punkt im
  gesamten Projekt – hängt stark von Tensor-Chip/Treiber-Unterstützung ab.
- Empfehlung: Projekt in Phasen angehen, nicht alle Features parallel gleich tief planen.
  Sinnvolle Reihenfolge grob nach Risiko/Abhängigkeit sortieren (wird mit Nutzer noch
  final festgelegt).
- **Paketname (`applicationId`):** `de.falkloewert.cameraapp` (bewusst geändert von
  `net.sourceforge.opencamera`, um Konflikte mit einer eventuell installierten offiziellen
  Open-Camera-App zu vermeiden). Der interne Java-Package-Name (`namespace` in
  `app/build.gradle`, `package net.sourceforge.opencamera;` in allen Java-Dateien) wurde
  bewusst NICHT mit umbenannt – rein kosmetisch, hätte Merge-Konflikte mit `upstream`
  erhöht, kein funktionaler Nutzen. Bei Bedarf später nachholbar.
- **App-Name/Icon:** App-Name zeigt seit 2026-08-27 testweise "Falks Kamera" (zuvor
  "Open Camera", siehe 4.4) – Icon (Mipmap) noch unverändert, rein kosmetisch, folgt bei
  Bedarf später.
- **Lokale Entwicklungsumgebung:** Zorin OS 18.1 (Ubuntu-basiert), Android Studio
  installiert (Snap, `--classic`), Build+Run auf echtem Pixel 9 Pro per USB-Debugging
  bestätigt funktionsfähig (siehe Änderungsprotokoll).

---

## 6. Änderungsprotokoll

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
