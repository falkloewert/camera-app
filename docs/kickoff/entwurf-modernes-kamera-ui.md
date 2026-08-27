# Entwurf: Prompt für den Projektmanager-Chat – "Modernes Kamera-UI"

Dieser Text ist NICHT Teil des Panorama-Weitwinkel-Features. Er ist ein Vorschlag für ein
neues, eigenständiges Feature, entstanden aus Falks Feedback während des Panorama-Feature-Chats
am 2026-08-27. Falk kann diesen Text (oder eine gekürzte Fassung) in den Projektmanager-Chat
einfügen, um zu entscheiden, ob und wann das als eigenes Feature umgesetzt wird.

## Kontext

Während des Panorama-Weitwinkel-Feature-Chats hat Falk zwei App-weite UI-Wünsche geäußert,
die über das Panorama-Feature hinausgehen und alle Foto-/Video-Modi betreffen:

1. **Zoom-Stufen-Buttons wie bei modernen Kamera-Apps (z. B. Google-Kamera):** Statt nur
   eines stufenlosen Zoom-Reglers feste, antippbare Stufen (z. B. "0.5x", "1x", "2x" o. ä.,
   je nachdem was das Gerät an Linsen/Zoomstufen bietet), die auf die jeweils passende
   physische Linse bzw. den passenden Zoom-Faktor umschalten.
2. **Modus-Auswahl (Foto, Video, Panorama, zukünftige Modi) als horizontale Scroll-Leiste
   am unteren Bildschirmrand**, wie bei den meisten modernen Kamera-Apps, statt über das
   bestehende Drei-Punkte-Menü. Falk empfindet das Drei-Punkte-Menü als unschön. Offene
   Frage, die Falk selbst noch nicht final entschieden hat: Bleibt das Drei-Punkte-Menü für
   andere Einstellungen bestehen und nur die Modi wandern nach unten? Oder wird die
   Modus-Auswahl komplett aus dem Drei-Punkte-Menü entfernt? Falk tendiert dazu, dass beides
   denkbar ist, und überlässt die Detailentscheidung dem Projektmanager-Chat bzw. einem
   dedizierten Feature-Chat.

## Warum als eigenes Feature, nicht im Panorama-Chat umgesetzt

Beide Wünsche betreffen die gesamte App-UI (alle Kameramodi), nicht nur Panorama. Der
Panorama-Feature-Chat ist laut Projektregeln (SPEC.md Abschnitt 2.1) nicht befugt,
Entscheidungen über andere Features oder deren Umfang/Reihenfolge zu treffen – das bleibt
dem Projektmanager-Chat vorbehalten. Für das konkrete Panorama-Feature wurde stattdessen
eine kleine, lokale Lösung gebaut (direktes Umschalten Weitwinkel/1x per Tap statt Dialog,
siehe Änderungsprotokoll-Eintrag zu Feature 4), die unabhängig von diesem größeren UI-Umbau
funktioniert.

## Vorschlag für die Priorisierung

Da dies ein sichtbarer, app-weiter Eingriff in die Kern-UI ist (betrifft `MainUI.java`,
`activity_main.xml`, Layout- und Klick-Logik für praktisch jeden Modus), sollte die
Komplexität/Reihenfolge im Projektmanager-Chat gegen die bereits vereinbarte Feature-Reihenfolge
(SPEC.md Abschnitt 3.1) abgewogen werden, statt es einfach an die bestehende Liste anzuhängen.
