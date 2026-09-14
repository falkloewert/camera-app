# Kickoff-Kontext: Material-3-Redesign, Phase 1 – Theme-Fundament + Layout-Migration

Dies ist Phase 1 von 3 des Material-3-Redesigns (siehe `docs/SPEC.md` Abschnitt 4.5
für den vollständigen Hintergrund, die Design-Referenz und die Begründung der
Reihenfolge). Bitte zuerst `docs/SPEC.md` (v.a. 2.1 und 4.5) und `CLAUDE.md` lesen.

## Kurzfassung

- **Ich (Falk) habe keine Programmiererfahrung**, aber technisches Grundverständnis.
  Bitte bei jedem Schritt erklären, was du tust und warum.
- **Git:** Du übernimmst `git add`/`commit`/`push` für dieses Projekt komplett selbst.
- **Nach Codeänderungen:** `graphify update .` ausführen (Teil des
  Abschlussprotokolls, siehe CLAUDE.md).
- **Agenten:** Für eng begrenzte, mechanische Teilschritte darfst du Agenten
  einsetzen (siehe SPEC.md 4.5) – du bleibst aber der durchgängige
  Gesprächspartner, der mit mir erklärt und live auf dem Pixel 9 Pro testet.
- **Nur eine Sitzung gleichzeitig im Repo** – vor Codeänderungen `git status`/
  `git fetch` prüfen (siehe SPEC.md 2.1).

## Design-Zielbild (Kurzfassung – Details in SPEC.md 4.5)

Fest dunkles Kamera-Chrome (unabhängig vom Systemtheme, wie die echte Pixel-Kamera),
kein Reinschwarz sondern eine dunkle neutrale Surface-Fläche, weißer/hellgrauer Text,
gedämpftes Grau für Sekundärtext, kaum gesättigte Akzentfarbe – "ausgewählt" wird
durchgängig über eine volle weiße/helle Füllung markiert, nicht über Farbe. Goldener
Fokusring bleibt erhalten. Stark abgerundete Formen (Pillen, Kreise) durchgängig.
Vollständige Farb-/Form-Analyse der echten Pixel-Kamera-Screenshots: SPEC.md 4.5.

## Aufgabe dieses Chats (Phase 1 von 3)

**Nur Fundament, noch keine neuen Bedienelemente** (Zoom-Stepper/Moduswahl-Leiste
kommen in Phase 2, Settings-Bottom-Sheets in Phase 3):

1. **Dependency hinzufügen:** `com.google.android.material:material:1.14.0` in
   `app/build.gradle` (AGP/Gradle im Repo sind bereits kompatibel, verifiziert).
2. **Theme-Fundament:** Neues Theme mit `Theme.Material3.Dark.NoActionBar` als
   Parent (bewusst `.Dark`, nicht `.DayNight` – das Kamera-Chrome soll ja fest dunkel
   bleiben, nicht dem Systemtheme folgen). Farbrollen (`colorSurface`,
   `colorOnSurface`, `colorPrimary` etc.) passend zur Screenshot-Referenz in
   `themes.xml`/`colors.xml` definieren – neu anlegen, `styles.xml` mit dem alten
   `AppBaseTheme`/`AppTheme` bestehen lassen oder ablösen, wie es sauberer ist,
   erklären welche Wahl und warum.
3. **`activity_main.xml`: `RelativeLayout` → `ConstraintLayout` migrieren.** Das ist
   der riskanteste Teil dieser Phase – bitte in nachvollziehbaren Zwischenschritten
   vorgehen (z. B. Constraints für die bestehenden Elemente einzeln nachbauen, nicht
   alles auf einmal), nach jedem größeren Zwischenschritt von mir auf dem Gerät
   testen lassen, ob noch alles wie vorher funktioniert (inkl. Panorama-
   Linsenwahl-Buttons aus Feature 4, die zur Laufzeit in `MainUI.java` positioniert
   werden – prüfen, ob diese Berechnung mit `ConstraintLayout` weiter funktioniert
   oder angepasst werden muss).
4. **Bestehende Buttons/Icons vorerst NICHT auf Material-Komponenten umstellen** –
   das kommt in Phase 2/3. Diese Phase soll nur sicherstellen, dass Theme + Layout-
   Migration für sich funktionieren, ohne dass optisch schon viel Neues entsteht
   (außer den neuen Grundfarben, die durch das Theme automatisch greifen).

## Abschluss dieser Phase

Wenn Theme + ConstraintLayout-Migration stehen und ich bestätigt habe, dass die App
weiterhin normal funktioniert (Fotos, Video, Panorama):
- `docs/SPEC.md` Abschnitt 4.5 aktualisieren (Phase 1 Status, was genau umgesetzt
  wurde, etwaige neue Erkenntnisse für Phase 2/3).
- Änderungsprotokoll-Eintrag.
- Committen/pushen.
- Kurze Zusammenfassung für den Projektmanager-Chat – der bereitet danach Phase 2
  (Zoom-Stepper + Moduswahl-Leiste) vor.
