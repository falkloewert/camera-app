# Kickoff: Schritt D – Moduswahl als Karussell, zurückgestellte Foto-Modi zurück (Audit-Schritt 3)

Schritt C ist fertig (Kopfzeile, alte Icon-Leiste und Drei-Punkte-Popup entfernt, 2026-09-22).
Damit sind die Foto-Modi aus dem Popup (DRO, HDR, Nacht …) **unerreichbar** – in C bewusst so
entschieden, weil dieses Karussell sie aufnehmen soll. Genau das ist dieser Schritt.
Arbeitsstand `master` (sauber, gepusht). Maße: 2,25 px/dp.
Session-Titel: `camera-app – Schritt D Moduswahl-Karussell`.

## 1. Soll (ui-spezifikation.md 7.2 „Moduswahl-Leiste" + Abschnitt 5)

- Aus dem `LinearLayout` wird ein **waagerechtes Paging mit Magneteffekt**: ein Tipp auf ein
  Nachbarwort zieht es in die Mitte, beim Einrasten `CLOCK_TICK`-Haptik, Alpha-Verläufe an
  beiden Rändern. Alle vier Punkte fehlen heute.
- **Pillenhöhe 32 dp** statt heute ~39 dp (8 dp Padding) – dadurch sitzt das Zentrum 4 dp zu
  hoch. Gilt für die Modus-Einträge; die Dock-Pille (`PhotoVideoSwitch`, 92 × 40 dp) bleibt
  unverändert (Dock ist bis auf diesen Punkt fertig, 7.1 Punkt 3).
- **Einträge** – nur echte Modi (Falk 2026-09-15), Vorschlag des PM-Chats:
  `Foto · Panorama · HDR · DRO · Porträt · Nachtsicht`. Porträt und Nachtsicht gibt es als
  Kamera-Erweiterungen (`PhotoMode.X_Bokeh`, `X_Night`); sie erscheinen **nur, wenn die Kamera
  sie meldet** – gleiche Regel wie Zeitlupe/Zeitraffer in SPEC 4.6 („ein Modus, den die Kamera
  nicht kann, fehlt statt tot dazustehen"). **Nicht** ins Karussell: Serienbild (Falk: halten
  reicht), Rauschunterdrückung (in C gestrichen), Belichtungs- und Fokus-Bracketing (bleiben im
  Einstellungen-Blatt).
- Video bleibt wie gebaut: `Video · Zeitlupe · Zeitraffer`, während einer Aufnahme ist die
  Leiste ganz weg.

## 2. Inventar (Faulheits-Leiter: was gibt es schon?)

- `ui/MainUI.setupModeBar():983` (Einträge verdrahten), `setModeBarEntry():999`,
  `updateModeBar():1066` (Sichtbarkeit + Styling), `applyModeBarStyle()`; XML-Einträge
  `mode_bar_*` in `activity_main.xml` ab ~1006.
- Das Setzen läuft schon: `MainUI.switchPhotoMode("preference_photo_mode_…")` schreibt
  `PreferenceKeys.PhotoModePreferenceKey`; gelesen wird sie in
  `MyApplicationInterface.getPhotoMode():1851`, samt Zuordnung auf die Erweiterungen
  (1736/1749/1752). Für neue Einträge genügt also der richtige Preference-Wert.
- **Vor dem Bauen prüfen:** was `switchPhotoMode()` auslöst (Kamera-Neuaufbau? Preview-Neustart?).
  Ein Karussell darf nicht bei jedem Rastschritt die Kamera neu aufbauen – umgeschaltet wird
  erst beim Einrasten.

## 3. Entscheidungen Falk

- Serienbild ist kein Modus mehr (Auslöser halten).
- Die Liste in Abschnitt 1 ist ein Vorschlag; Falk kann Einträge streichen oder ergänzen.
- Alles Übrige wie im PM-Vorschlag, wenn nicht widersprochen.

## 4. Bauen und Fallstricke

- Pixel-Drehmodell (K3) gilt: nur die Glyphen drehen. Ein waagerecht scrollendes Karussell im
  **Querformat** ist der Knackpunkt – die Referenz zeigt es dort nicht, also am Gerät ansehen
  und dann entscheiden.
- Keine neuen Farben und Schriften (das ist Schritt 7.3), keine neuen Animationen außer der
  Rast-Bewegung und der Haptik.
- `mode_bar` wird an mehreren Stellen ein- und ausgeblendet (`MainUI.java:1454` und `:1569`) –
  beim Umbau mitziehen.

## 5. Abschluss (erst nach Falks Gerätetest)

`docs/SPEC.md` 4.5 (Schritt D als erledigt, „Offen"-Zeile kürzen) und Übersicht 3,
`ui-spezifikation.md` 7.2 „Moduswahl-Leiste" (Ist → Soll auflösen) und 7.1 Punkt 3
(Pillenhöhe), `docs/changelog.md` ≤ 10 Zeilen mit dem Inventar aus Abschnitt 2,
`graphify update .`, Commit mit Dateipfaden + Push, drei Zeilen für den PM-Chat.
Kein nächstes Thema anfangen.
