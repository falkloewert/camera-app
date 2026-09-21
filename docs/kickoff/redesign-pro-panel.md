# Kickoff: Pro-Panel am rechten Dock-Button (K13)

Letztes fehlendes Teil von Schritt B. Nicht enthalten: Kopfzeile und das Abschaffen von
Icon-Leiste/Popup (Schritt C). Unten Entschiedenes nicht neu aufmachen; bei echter
Unklarheit eine kurze Frage an Falk, sonst bauen.

Arbeitsstand: `master` (`d8a5d389`, enthält das abgenommene Einstellungen-Blatt). Kein
Benchmark mehr – direkt auf `master`, Commits mit expliziten Dateipfaden, Push.
Session-Titel: `camera-app – Pro-Panel`.

## 1. Referenz erzeugen (zuerst, vor jedem Code)

Falk legt einen Screenrecord der Pixel-App ab: `/home/falk/Videos/pixel-pro-panel-screenrecord.mp4`
(liegt dort seit 2026-09-21: 960 × 2142 px wie die Referenz-Screenshots, also 2,25 px/dp,
74 s; bleibt außerhalb des Repos – kein Video committen). Er zeigt Knopf, Öffnen, jede
Regler-Position, Tab-Reihe, Hoch- und Querformat, Zurücksetzen, Schließen.

Den Record **selbst** mit Gemini bis ins Detail beschreiben lassen (nicht Falk fragen):
`gemini-ask -m pro "<Frage>" /home/falk/Videos/pixel-pro-panel-screenrecord.mp4` oder den
`gemini`-Subagenten. In Abschnitten fragen, nicht alles auf einmal: (1) Knopf + Übergang,
(2) Info-Zeile, (3) Regler inkl. Tick-Marks und Endsymbole, (4) Tab-Reihe ruhend/aktiv,
(5) Querformat, (6) Zurücksetzen/Schließen. Je Element verlangen: Position, Größe in px
**und** dp (2,25 px/dp wie die Screenshots), Farben als Hex, Beschriftungen wörtlich,
Reihenfolge, Verhalten.

Ergebnis als `docs/design-reference/pixel-pro-panel-beschreibung.md` ablegen und committen
(dauerhafte Referenz; das Video nicht). Was der Record nicht zeigt: fragen, **nicht**
erfinden. Widerspruch zur Text-Referenz unten ⇒ der Record gilt, außer bei den
Fest-Entschiedenen hier.

## 2. Vor dem Bauen am Gerät prüfen (ein Log-Durchgang)

ISO und Verschlusszeit nur, wenn der HAL manuellen Sensor erlaubt: `supports_iso_range`,
`iso_range_min/max`, `supports_exposure_time`, `exposure_time_min/max`
(`MyPreferenceFragment` Z. 192–216). Ergebnis ins Abschlussprotokoll. Nicht unterstützt ⇒
Knopf im Panel ausgrauen, Rest normal bauen.

## 3. Bauen

- Rechtes Dock-Element: laut Detail-Spec 40 dp, **gemessen** wie der linke Knopf (`#333333`,
  48 dp, `settings_photo_camera`, 7.1 Punkt 3) – Optik vom linken übernehmen, Icon „drei
  Reglerstifte". Vorbild: linker Knopf in `activity_main.xml` / `MainUI.layoutUI()`.
- Panel als **Overlay über dem Sucher** (Falk), kein zweites Blatt: Kamera bleibt sichtbar.
  Zweiter Tipp auf den Knopf und Wisch nach unten schließen es (Geste wie beim
  Einstellungen-Blatt, `Preview.gestureDetector`; `MyGestureDetector` bleibt unberührt).
- Regler (Falk: alle): Belichtung `R.id.exposure_seekbar`, Weißabgleich
  `R.id.white_balance_seekbar`, Fokus `R.id.focus_seekbar`, ISO `R.id.iso_seekbar`,
  Verschlusszeit `R.id.exposure_time_seekbar`; dazu „Alle zurücksetzen" und – nur wenn der
  Record es zeigt – Fokus-Peaking (`ShowFocusPeakingPreferenceKey`). Vorhandene Logik in
  `MainActivity` Z. 5441–5770 (`setManualFocusSeekbar`, `setManualWBSeekbar`,
  ISO-/Exposure-Time-Seekbar) wiederverwenden, nicht nachbauen.
- „Helligkeit" der Vorlage = unsere Belichtung (EV). „Schatten" bleibt weg (kein
  Gegenstück, 4b) – nicht neu diskutieren.
- Optik/Bauteile von `ui/SettingsSheet` und `ui/ZoomWheel` übernehmen (Farben, 40-dp-Knöpfe,
  weißer Kreis = aktiv). Keine neuen Farben erfinden.
- RAW/JPEG bleibt im Blatt – dort schon gebaut, nicht doppelt.

## 4. Zusammenspiel (nicht kaputt machen)

- Manueller Modus schaltet AF/AE ab: Öffnen/Schließen und „Alle zurücksetzen" müssen auf den
  Standard zurück (kontinuierlicher Fokus, wie `Preview.updateFocus()` nach dem Makro-Ausbau).
- Video-Auto-Torch hängt am gemessenen ISO (`ISOChangeCallback`, `Preview.torchAutoOn()`) –
  bei manuell gesetztem ISO darf er nicht dauerhaft zünden (prüfen, im Zweifel aus).
- Gepinnte Linse (`getCameraIdSPhysicalPref()`) unangetastet lassen.
- Blatt und Panel dürfen sich nicht widersprechen: Timer/Blitz/Auflösung bleiben im Blatt.

## 5. Abschluss (erst nach Falks Gerätetest)

`docs/SPEC.md` 4.5 (Stand, PM-Punkte 3/10), `ui-spezifikation.md` 7.1 Punkt 3 und 4b,
`docs/changelog.md` ≤ 10 Zeilen, Ergebnis der HAL-Prüfung aus Schritt 2, `graphify update .`,
Commit mit Dateipfaden + Push, drei Zeilen für den PM-Chat. Kein nächstes Thema anfangen.
