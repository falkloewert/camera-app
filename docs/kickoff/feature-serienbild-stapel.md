# Kickoff: Feature 9 – Serienbilder als Stapel in der Galerie

**Dieser Chat ist der erste Parallelversuch**: er läuft in einem **eigenen Worktree** auf
Branch `feature/serienbild-stapel`, während der Chat für Schritt C parallel auf `master`
arbeitet. Zweck ist auch, den Worktree-Betrieb selbst zu erproben – wenn die Trennung
irgendwo hakt, das im Abschluss melden statt dagegen anzukämpfen. Session-Titel:
`camera-app – Feature 9 Serienbild-Stapel`.

## 1. Ziel

Falk: „serienbilder gerne unendlich viele solange ich auf den auslöser drücke, das mit lange
auf foto auslöser drücken und dann serie klappt ja schon – nur hätte ich gerne, dass sie alle
in der galerie als **stapel** auftauchen und nicht einzeln."

Ergebnis: Eine Serie erscheint in der Galerie und in Immich als **ein** Stapel mit einer
Vorschaugruppe, nicht als N Einzelbilder. Die Einzelbilder bleiben natürlich erhalten.

## 2. Zuerst klären, bevor eine Zeile Code entsteht

Der Weg ist offen – deshalb steht die Recherche vor dem Bauen. Wenn es keinen praktikablen
Weg gibt, ist „geht nicht, weil …" eine gültige Antwort (dann Falk fragen, nichts erfinden).

- **Wie gruppieren Galerie-Apps Serienbilder?** Google Fotos und die Systemgalerie nutzen
  Metadaten in der Bilddatei (XMP: Serien-/Sequenzkennung). Welche Felder genau, und liest
  sie die Android-Galerie überhaupt, oder nur Google Fotos?
- **Was kann unsere App schreiben?** Open Camera nutzt `ExifInterface` – kann das XMP
  schreiben, oder braucht es eine Bibliothek? Was hängt dann an Größe/Lizenz (GPLv3!)?
- **Was macht Immich?** Immich hat eine eigene Stapel-Funktion – greift die auf Datei-
  Metadaten, auf Dateinamen oder nur auf manuelles Stapeln? Falk nutzt Immich aktiv, das
  Ergebnis muss dort ebenfalls stimmen.
- Ergebnis nach `docs/technik/serienbild-stapel.md` (neuer Ordner) **und** ins
  Abschlussprotokoll.

## 3. Inventar im Code (Faulheits-Leiter: was gibt es schon?)

- Wo entstehen Serienbilder heute: `MyApplicationInterface`, `ImageSaver`,
  `preference_fast_burst_n_images`, `preference_burst_mode` – wie wird die Datei benannt und
  geschrieben, gibt es schon eine laufende Nummer je Serie?
- Wird beim Speichern ein `MediaStore`-Eintrag gesetzt? Gibt es dort eine Gruppenkennung?
- Welche Metadaten schreibt die App heute (EXIF? XMP gar nicht?) – Suchbegriffe `ExifInterface`,
  `XMP`, `burst`.

## 4. Am Gerät messen (ein Durchgang)

Eine Serie aufnehmen, die Dateien auf den PC ziehen und mit `exiftool` ansehen, was wirklich
drinsteht. Danach in Google Fotos und in Immich prüfen, wie die Serie dargestellt wird –
**vorher und nachher**, damit der Unterschied belegt ist. Messwerte ins Abschlussprotokoll.

## 5. Bauen

Erst nach 2–4. Vorhandene Bauteile nutzen, keine neue Bibliothek ohne Rückfrage (Lizenz).
Die Anzahl der Serienbilder („unendlich, solange gedrückt") ist **nicht** Teil dieses Chats –
die Einstellung zieht Schritt C ins Einstellungen-Blatt um.

## 6. Spielregeln für den Parallelbetrieb

- **Nur in diesem Worktree arbeiten**, nie im Hauptordner `/home/falk/Projects/camera-app`.
- Schritt C fasst parallel an: `MainUI.java`, `MainActivity.java`, `PopupView.java`,
  `SettingsSheet.java`, `OnScreenIcons.java`, `activity_main.xml`. **Diese Dateien nicht
  anfassen.**
- Berührungspunkte, wenn unvermeidbar: `MyApplicationInterface.java` (hier **nur** der
  Bildspeicher-Teil, nicht die Popup-Zeilen um 2148/2166/2497/2952) und `strings.xml`
  (nur eigene, neue Einträge anhängen, nichts umsortieren).
- Vor dem Abschluss `git fetch origin && git rebase origin/master`; Konflikte selbst lösen.
- Commit mit expliziten Dateipfaden, Push auf `feature/serienbild-stapel`.

## 7. Abschluss (erst nach Falks Gerätetest)

`docs/SPEC.md` Übersicht 3 (Feature 9 auf Fertig) + neuer Abschnitt **4.7**,
`docs/changelog.md` ≤ 10 Zeilen mit Recherche- und Messergebnis, `graphify update .`,
Commit + Push, drei Zeilen für den PM-Chat – darunter **eine Zeile dazu, wie sich der
Parallelbetrieb angefühlt hat** (Konflikte, Reibung, Zeitgewinn). Kein nächstes Thema
anfangen.
