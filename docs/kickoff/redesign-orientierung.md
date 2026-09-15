# Kickoff-Kontext: Orientierungs-Modell umbauen (Material-3-Redesign)

Eigenständige Aufgabe innerhalb von Feature 6 (Material-3-Redesign, siehe `docs/SPEC.md`
Abschnitt 4.5). Bitte zuerst lesen:

1. `docs/design-reference/ui-spezifikation.md` — verbindliche Bau-Vorgabe, inkl. Falks
   bereits getroffener Entscheidungen. **Die Grundregel dort ganz oben gilt für diesen
   Chat besonders: eins zu eins nachbauen, nichts verallgemeinern, und vor jeder
   Fertigmeldung die Abweichungen aufzählen.**
2. `docs/SPEC.md` Abschnitte 2.1 (Rolle und Abschlussprotokoll für Feature-Chats) und 4.5
3. `CLAUDE.md` im Repo-Root

## Kurzfassung zu Falk

Keine Programmiererfahrung, aber technisch interessiert — bei jedem Schritt erklären, was
du tust und warum. Git (`add`/`commit`/`push`) übernimmst **du** komplett. Nach
Codeänderungen `graphify update .` laufen lassen. Vor Codeänderungen `git status`/
`git fetch` prüfen, es soll immer nur eine Sitzung gleichzeitig im Repo arbeiten.

Testgerät ist ein **Pixel 9 Pro**, es hängt meist per USB. Du kannst selbst bauen,
installieren und Screenshots ziehen:

```
export JAVA_HOME=/home/falk/.jdks/jbr-21.0.11
export ANDROID_HOME=/home/falk/Android/Sdk
export PATH="$ANDROID_HOME/platform-tools:$PATH"
./gradlew :app:installDebug
adb shell am start -n de.falkloewert.cameraapp/net.sourceforge.opencamera.MainActivity
adb exec-out screencap -p > /tmp/shot.png
```

Zum Vergleichen kannst du die echte Pixel-Kamera in den Vordergrund holen
(`com.google.android.GoogleCamera`) und ebenfalls abfotografieren. Für Bildvergleiche
den `gemini`-Agenten nutzen (läuft über Falks Google-Abo, nicht über Claude-Kontingent).
Es gibt auch einen eingerichteten Emulator (`pixel_preview`, headless starten mit
`-no-window`), der aber **keine Weitwinkel-Linse** hat — Zoom-Stufen lassen sich dort
nicht vollständig prüfen. Emulator nach Gebrauch beenden, er macht Falks Rechner langsam.

## Das Problem

Beim Drehen des Geräts ordnet unsere App die Bedienelemente **neu an**. Die echte
Pixel-Kamera tut das nicht: Sie dreht die gesamte Bedienoberfläche **als starren Block
mit**, so als wäre das Hochformat-Layout auf eine Karte gedruckt und die Karte würde
gedreht. Für Falk fühlt es sich deshalb an, als bliebe beim Drehen alles an Ort und
Stelle — jeder Knopf sitzt weiterhin an derselben körperlichen Position am Gerät.

**Belege liegen als Dateien im Repo** (alle vier am selben Gerät aufgenommen):

| Datei | Inhalt |
|---|---|
| `docs/design-reference/pixel-original-hochformat.png` | Original, Hochformat |
| `docs/design-reference/pixel-original-querformat.png` | Original, Querformat |
| `docs/design-reference/unsere-app-hochformat.png` | Unsere App, Hochformat |
| `docs/design-reference/unsere-app-querformat.png` | Unsere App, Querformat |

Vergleiche beide Querformat-Bilder direkt miteinander, dort sieht man den Unterschied am
deutlichsten:

- **Zoom-Leiste:** Original von oben nach unten `5 · 2 · 1× · ,5`; bei uns genau
  umgekehrt `,5 · 1× · 2 · 5`. Beim Original ist das die *gedrehte* Hochformat-Leiste
  (dort `,5 · 1× · 2 · 5` von links nach rechts), bei uns eine neu aufgebaute.
- **Moduswahl:** Im Original stehen die Beschriftungen **um 90° gedreht** in einer
  Spalte. Bei uns stehen sie aufrecht nebeneinander und fressen dadurch sehr viel
  Breite — genau das stört Falk.
- **Auslöser-Spalte:** Im Original Kamera-Wechsel (abgerundetes Quadrat) oben, Auslöser
  mittig, Galerie-Vorschau (abgerundetes Quadrat) darunter — dieselbe Abfolge wie im
  Hochformat, nur gedreht.

## Aufgabe

Das Orientierungs-Modell so umbauen, dass sich die Bedienoberfläche beim Drehen wie beim
Original verhält: **eine einzige, feste Anordnung, die als Ganzes mitgedreht wird**,
statt pro Ausrichtung neu angeordnet zu werden. Texte dürfen dabei quer stehen — das ist
im Original so und von Falk ausdrücklich gewünscht.

### Wichtiger Hinweis zum vorhandenen Code

Open Camera bringt für so etwas bereits Mechanik mit, die aktuell **abgeschaltet** ist:

```java
// MainActivity.java
//public static final boolean lock_to_landscape = true;
public static final boolean lock_to_landscape = false;
```

Mit `true` wird die Anordnung eingefroren und jede Ansicht nur noch per
`MainUI.setViewRotation()` um die eigene Achse gedreht — konzeptionell das gewünschte
Verhalten, aber auf **Querformat** festgenagelt, während die Referenz vom **Hochformat**
ausgeht. Außerdem füllt beim Original die Vorschau im Querformat weiterhin den ganzen
Bildschirm, das Fenster dreht sich dort also sehr wohl mit. Prüfe beide Wege, bevor du
dich entscheidest:

- **(a)** Fenster festnageln (`android:screenOrientation`) und alles per `setViewRotation`
  drehen — einfach, aber die Vorschau müsste trotzdem das volle Querformat ausfüllen.
- **(b)** Fenster drehen lassen, aber die Positionsberechnung in `MainUI.layoutUI()` von
  „pro Ausrichtung neu abbilden" auf „eine kanonische Anordnung starr rotieren" umstellen.

`layoutUI()` bildet Richtungen aktuell über `Side`-Paare pro Ausrichtung ab (siehe die
Variablen `align_parent_*`, `left_of`, `above` usw.) — genau diese Abbildung ist das,
was ersetzt werden muss.

### Zwingend mitprüfen

Das ist ein Eingriff in die Grundlage, auf der die ganze Oberfläche sitzt:

- **Ausrichtung gespeicherter Fotos und Videos** (EXIF-Drehung) in allen vier Lagen.
  Open Camera unterstützt von Haus aus nur zwei Modi, nicht den hier angestrebten —
  hier ist echtes Regressionsrisiko.
- **Panorama** (Gyro-Auswertung, Objektiv-Buttons aus Feature 4).
- **Vorschau-Seitenverhältnis** im Querformat.
- Die in Phase 1/2 gebauten Elemente: Zoom-Stufen-Leiste (`ZoomStepper`),
  Moduswahl-Leiste. Deren aktuelles Umklappen von waagerecht auf senkrecht ist genau das
  „Mitrotieren", das **entfallen** soll.

### Nach dem Umbau erledigt sich von selbst

Die offene Frage, ob die Zoom-Stufen im Querformat umgedreht gehören: Bei einer starr
gedrehten Anordnung ergibt sich die Reihenfolge automatisch richtig.

## Abschluss

Wenn es steht und Falk am Gerät bestätigt hat:
1. `docs/design-reference/ui-spezifikation.md` und `docs/SPEC.md` (Abschnitt 4.5 plus
   Änderungsprotokoll) aktualisieren.
2. `graphify update .`
3. Committen und pushen.
4. Kurze Zusammenfassung für den Projektmanager-Chat, **nicht** eigenmächtig das nächste
   Feature beginnen.

## Was danach noch offen ist (nicht in diesem Chat)

Feinjustier-Rad für Zoom über 5×, Foto- und Video-Einstellungs-Blätter, untere
Bedienzeile, Galerie und Kamera-Wechsel im Referenz-Stil, Raster und gelber Fokus-Ring.
Details stehen in `docs/design-reference/ui-spezifikation.md`.
