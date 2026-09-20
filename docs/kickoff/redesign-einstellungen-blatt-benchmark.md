# Kickoff (Benchmark-Fassung): Einstellungen-Blätter Foto + Video, linker Dock-Button, Swipe

Ersetzt für diesen Lauf `redesign-einstellungen-blatt-foto.md` (Schritt A) **und** den
Blatt-Anteil von Schritt B (Pro-Reiter, Video-Blatt). Nicht enthalten: rechter Dock-Button
(Regler), Kopfzeile, Abschaffung von Icon-Leiste/Popup (Schritt C). Alle Vorab-Fragen sind
von Falk entschieden (2026-09-18/20) – **nichts davon neu aufmachen**. Bei echter
Unklarheit eine kurze Frage, sonst bauen.

Dieser Prompt wird mit zwei Modellen ausgeführt (Benchmark). Deshalb: **eigener Branch**
`bench/blatt-<modellname>` ab `master` (`git switch -c …`), Commits mit expliziten Dateipfaden,
Push nur dieses Branches (`git push -u origin bench/blatt-<modellname>`), **nie** auf `master`.

Session-Titel: `camera-app – Einstellungen-Blatt (Benchmark <modellname>)`.
Regeln: `CLAUDE.md` (kurz, keine Erklärungen), `docs/SPEC.md` 2.1.

## 1. Lesen (nur das)

- `docs/design-reference/ui-spezifikation.md`: Abschnitt 4 (Zeilen), 4b (Entscheidungen, was die
  App nicht kann), 7.1 Punkte 2–3, Abschnitt 8 (Messung schlägt Detail-Spec, 2,25 px/dp;
  nichts verschiebt sich), Abschnitt 9 nur K8 (Doppel-Tap bleibt).
- Referenz-Screenshots (960 × 2142 px = **2,25 px/dp**, Gerät 426,7 × 952 dp, Gesten-Inset
  unten 24 dp): `pixel-sheet-allgemein-hochformat.png`, `pixel-sheet-pro-hochformat.png`,
  `pixel-sheet-allgemein-querformat.png`, `pixel-sheet-pro-querformat.png`,
  `pixel-original-hochformat-2026-09-18.png` (Dock-Zeile ohne Blatt). Alle Maße unten sind
  daraus gemessen; bei Zweifel selbst nachmessen (Python/PIL), Screenshot gilt.
- `docs/design-reference/pixel-video-blatt-beschreibung.md` Abschnitt 2 (Video-Blatt, Text).
- Code (Stand `b8f14e67`):
  - `ui/PopupView.java`: Z. 128–150 Blitz (`preview.updateFlash(option)` + `setPopupIcon()` +
    `destroyPopup()`), Z. 156–185 Fokus (`preview.updateFocus(option, false, true)`), Z. 378–460
    Foto-Auflösung (`getResolutionPreferenceKey` → `editor.putString("w h")` → nach 400 ms
    `updateForSettings(true, "", true, false)`), Z. 459–520 Video-Auflösung
    (`preview.getSupportedVideoQuality(fps)`, `getVideoQualityPreferenceKey(…, fpsIsHighSpeed())`),
    Z. 868–905 Timer (`TimerPreferenceKey`, Werte `"0" "3" "5" "10"`).
  - `PreferenceKeys.java`: Z. 52–62 (`getFlashPreferenceKey`, `getFocusPreferenceKey`,
    `getResolutionPreferenceKey`), 66 (`getVideoQualityPreferenceKey`), 96 (`RawPreferenceKey`,
    Werte `preference_raw_no` / `preference_raw_yes`), 324 (`VideoStabilizationPreferenceKey`,
    boolean), 332 (`getVideoFPSPreferenceKey`, Werte `"24" "30" "60"` oder `"default"`), 372
    (`TimerPreferenceKey`).
  - `MainActivity.java`: `openSettings()` Z. 2678; `userSwitchToCamera(cameraId, physicalId)`
    Z. 2005; `updatePanoramaLensCache()` Z. 2303 (Vorbild: physische Linsen nach Sichtwinkel,
    `preview.getPhysicalCameras()`, `getCameraControllerManager().getDescription(info, …,
    physical_id, false, true)` → `info.view_angle`); `switchPanoramaLens()` Z. 2370;
    `updateForSettings(...)` Z. 3002; `enablePopupOnBackPressedCallback()` Z. 3267 (Vorbild
    Zurück-Taste); `lockScreen()` Z. 4948 + `MyGestureDetector` Z. 4977 (**nur bei gesperrtem
    Bildschirm aktiv – nicht anfassen**).
  - `preview/Preview.java`: `gestureDetector` Z. 493 (`SimpleOnGestureListener`, nur
    Doppel-Tap-Listener gesetzt), `touchEvent()` Z. 621 (Wischer werden heute ignoriert, Z. 662–681),
    `DoubleTapListener` Z. 928; `updateFlash()` Z. 5231, `updateFocus()` Z. 5365,
    `getCurrentFlashValue()` 5350, `getCurrentFocusValue()` 5452, `getSupportedPictureSizes(true)`
    7868, `getCurrentPictureSize()` 7901, `getSupportedVideoQuality(fps)` 7917,
    `getSupportedFlashValues()` 7999, `isFlashSupportedForVideo()`, `supportsRaw()` 7764,
    `supportsVideoStabilization()` 7493, `hasPhysicalCameras()` 9365, `canSwitchCamera()` 4775.
  - `MyApplicationInterface.java`: `touchEvent()` Z. 2133 (schließt Popup bei Sucher-Berührung),
    `getVideoFPSPref()` 749, `fpsIsHighSpeed()` 692, `getCameraIdSPhysicalPref()`.
  - `ui/MainUI.java`: `glyphViews()` Z. 251 (Liste der drehenden Glyphen), `rotateGlyphs()` 275,
    `resetView/centerHorizontally/pinToBottom` 302–328, Fußzeilen in `layoutUI()` Z. 486–545
    (Umschalter: `pinToBottom(cs, id, navigation_gap, 37 dp)` = Zentrum 85 dp), `setImmersiveMode()`
    1071, `showGUI()` 1147 (Moduswahl/Umschalter bei Aufnahme `INVISIBLE`, Z. ~1111/1224).
  - `ui/ZoomStepper.java` (Stufen als Verhältnisse, `back_steps`/`front_steps` Z. 39/43,
    `getStepButtons()` Z. 70) und `ui/ZoomWheel.java` (Pille + Rad) – für die Objektivauswahl.
  - `ui/PhotoVideoSwitch.java` (Vorbild: View, die sich selbst entlang der gedrehten Achse
    zeichnet), `res/drawable/squircle_background.xml`, `res/values/colors.xml` (`shutter_row_scrim`,
    `photo_video_switch_pill` = `#333333`), `res/layout/activity_main.xml` (Root
    `activity_main_root`, ConstraintLayout).
  - `app/build.gradle`: Material 1.14.0 eingebunden. **Kein** `BottomSheetDialog(Fragment)` –
    Begründung unten (Querformat).

## 2. Gemessene Referenz (verbindlich; Detail-Spec-Werte gelten nicht, wo sie abweichen)

### 2.1 Dock-Zeile (unterste Zeile, `pixel-original-hochformat-2026-09-18.png`)
- Linker Button: **Kreis ⌀ 48 dp, Füllung `#333333`** (= `photo_video_switch_pill`; **nicht**
  `rgba(255,255,255,0.10)` / 40 dp der Detail-Spec). Zentrum **40 dp** vom linken Rand,
  **85 dp** vom unteren Rand (Zeile des `PhotoVideoSwitch`: `pinToBottom(…, 37 dp)` bei 48 dp Höhe).
- Icon: Material Symbols **`settings_photo_camera`** (Zahnrad mit kleiner Kamera unten rechts),
  weiß, Glyph ≈ 21 × 20 dp (24-dp-Vektor mit Standard-Innenabstand → ImageView 24 dp).
- Rechter Button (spiegelgleich, 40 dp vom rechten Rand) ist **nicht** Teil dieses Auftrags.

### 2.2 Blatt, Hochformat (`pixel-sheet-allgemein-hochformat.png`; Pro-Blatt identisch groß)
- **Paneel:** volle Bildschirmbreite, **feste Höhe 538 dp** (unabhängig vom Inhalt, bei
  Allgemein und Pro gleich), am unteren Bildschirmrand (reicht bis unter die Gestenleiste),
  obere Ecken **Radius 24 dp**, Füllung **Schwarz 92 %** (`#EB000000`). Was darunter liegt
  (Auslöser, Pille, Moduswahl), bleibt an Ort und Stelle und schimmert dunkel durch.
- **Keine Abdunklung** außerhalb des Paneels: Sucher, Raster und Kopfzeilen-Chips sind bei
  offenem Blatt exakt so hell wie ohne (gemessen; die Spec-Sätze „abgedunkelt" sind falsch).
- **Ziehgriff:** 36 × 4 dp, Radius 2, `#919191`, waagerecht zentriert, Oberkante 16 dp unter
  der Paneel-Oberkante.
- **Titel** „Fotoeinstellungen" / „Videoeinstellungen": 24 sp, `#FFFFFF`, regular, linksbündig
  16 dp, Textmitte **65 dp** unter der Paneel-Oberkante.
- **„…"-Button:** Stadion-**Kontur** 52 × 40 dp (Radius 20), Strich 1 dp `#919191`, keine
  Füllung, Glyph `more_horiz` weiß (drei Punkte, ≈ 13 × 3 dp), rechter Rand 16 dp, Mitte
  **64 dp** unter der Paneel-Oberkante. Tipp → `MainActivity.openSettings()` (volle
  Einstellungsliste), Blatt schließt vorher.
- **Reiter-Umschalter:** Oberkante **96 dp** unter der Paneel-Oberkante, Höhe **44 dp**, Rand
  links/rechts 16 dp, zwei gleich breite Pillen (je 196 dp bei 426,7 dp Breite) mit **2 dp**
  Lücke, Radius 22. Aktiv: Füllung `#FFFFFF`, Text `#1B1B1B`; inaktiv: Füllung `#2A2A2A`,
  Text `#E2E2E2`. Text 16 sp, medium, zentriert. Umschalten ohne Animation. Beim Öffnen ist
  immer der linke Reiter aktiv.
- **Zeilen:** Raster **56 dp** je Zeile, Mitte der ersten Zeile **182 dp** unter der
  Paneel-Oberkante (Reiter-Unterkante 140 + 42). Links: Bezeichnung **14 sp `#E2E2E2`**
  (Mitte 10 dp über der Zeilenmitte), darunter Wert **20 sp `#FFFFFF`** (Mitte 13 dp unter
  der Zeilenmitte), beide linksbündig 16 dp, regular.
- **Options-Knöpfe** (rechts in jeder Zeile): **40 × 40 dp**, Raster **48 dp**, rechtsbündig –
  der rechte Rand des letzten Knopfs liegt **20 dp** vom Bildschirmrand (Zelle 48 dp, letzte
  Zelle endet bei 16 dp). Reihenfolge wie die Werte-Liste (links = erste Option).
  - **ausgewählt:** weißer **Kreis** `#FFFFFF`, Icon `#1B1B1B`.
  - **nicht ausgewählt:** abgerundetes Quadrat `#1F1F1F`, **Radius 12 dp** (wie
    `squircle_background.xml`), Icon `#E2E2E2`.
  - Icon-Glyph ≈ 20 dp (24-dp-Material-Symbol in 24-dp-ImageView).
- Kein Ripple/Aufblähen beim Tippen, keine Vibration bei Optionswahl. Mehr als 4 Zeilen: Inhalt
  ab Reiter-Unterkante scrollbar (im Hochformat mit unseren Zeilen nicht nötig).

### 2.3 Blatt, Querformat (`pixel-sheet-allgemein-querformat.png`, Gerät gegen den Uhrzeigersinn
gedreht, physische Unterkante rechts)
- Das Blatt ist ein **Seitenpaneel an der physischen Unterkante des Geräts** (dort, wo das Dock
  liegt): Breite **470,7 dp** (= 36 + 394,7 + 40), volle Bildschirmhöhe, Ecken zum Sucher hin
  Radius 24 dp, gleiche Füllung. **Der gesamte Inhalt ist quer gesetzt** (Titel, Reiter,
  Zeilen aufrecht lesbar) – nicht nur die Glyphen drehen.
- Ziehgriff: **senkrechter** Strich 4 × 36 dp `#919191`, 16 dp vom sucherseitigen Paneel-Rand,
  auf halber Bildschirmhöhe.
- Inhaltsblock exakt so breit wie im Hochformat (**394,7 dp** = 426,7 − 32): Einzug
  **36 dp** vom sucherseitigen Rand, **40 dp** vom äußeren Rand (Reiter, „…", Knöpfe: letzter
  Knopf endet 44 dp vom äußeren Rand). Senkrechte Positionen **20 dp höher** als im Hochformat
  (kein Griff über dem Titel): Titelmitte 45 dp, Reiter-Oberkante 76 dp, erste Zeilenmitte
  162 dp ab Bildschirm-Oberkante (in Querformat-Koordinaten).
- Andere Querlage (Gerät im Uhrzeigersinn gedreht): Paneel spiegelbildlich an der linken Kante.
  Auf dem Kopf (180°): Paneel an der Bildschirm-Oberkante, Inhalt um 180° gedreht. Regel:
  **immer an der physischen Unterkante, Inhalt aufrecht zur aktuellen `ui_rotation`.**

### 2.4 Referenz-Icons (Material Symbols, Apache 2.0, aus `google/material-design-icons`
`symbols/android/<name>/materialsymbolsoutlined/<name>_24px.xml` als Vektor-Drawable holen –
Standardstil, Fill 0, Weight 400)
| Verwendung | Symbol |
|---|---|
| Dock links | `settings_photo_camera` |
| „…" | `more_horiz` |
| Blitz aus / „Keines" (⊘) | `block` |
| Blitz auto | `flash_auto` |
| Blitz an | `flash_on` (Referenz: gefüllter Blitz) |
| Timer aus / 3 / 5 / 10 | `timer_off`, `timer_3`, `timer_5`, `timer_10` |
| Makro aus / an | `macro_off`, `local_florist` |
| Verhältnis 16:9 / 4:3 | selbst zeichnen: Rechteck-Kontur 22 dp hoch, Strich 2 dp, Radius 2; 16:9 → 12,4 dp breit, 4:3 → 16,5 dp breit (Hochformat-Rechtecke wie in der Referenz) |
| Auflösung 12 MP / max | selbst zeichnen wie Referenz-Badge: Text zweizeilig `12` / `MP` (bzw. `50` / `MP`), ca. 8 sp fett, Glyph ≈ 18 × 18 dp |
| RAW aus / an | Text `RAW` ≈ 18 × 6 dp fett; „aus" = derselbe Text mit diagonalem Strich |
| Objektiv automatisch / manuell | „A" in Kreis-Kontur (≈ 20 dp, Strich 1,5 dp) / `camera` (Blende) |
| Video: Auflösung FHD / 4K / 8K | Text `FHD` / `4K` / `8K` fett ≈ 9 sp |
| Video: fps 24 / 30 / 60 | Text `24` / `30` / `60` fett |
| Video: Stabilisierung aus / an | `block` / `video_stable` |

## 3. Bauen

### 3.1 Linker Dock-Button
- `ImageButton` in `activity_main.xml`, Maße/Farben aus 2.1, `contentDescription` neu
  (`@string/settings_sheet_open`), Klick → Blatt öffnen (Foto- oder Video-Blatt je nach
  `preview.isVideo()`).
- Verankerung in `MainUI.layoutUI()` wie der Umschalter: `resetView`, links `PARENT_ID` mit
  Abstand 16 dp (40 − 24), `pinToBottom(cs, id, navigation_gap, 37 dp)`. **Nicht** an andere
  Views gekettet.
- In `glyphViews()` aufnehmen (Glyph dreht, der Kreis ist rotationssymmetrisch).
- Sichtbarkeit wie Moduswahl/Umschalter in `showGUI()`: während Video-/Panorama-Aufnahme
  `INVISIBLE` (Platz bleibt). Alte Zahnrad-Taste `settings` oben **bleibt**.

### 3.2 Blatt (Container) – eigene View, kein Material-Dialog
- Grund: Der `BottomSheetDialog` klebt an der Unterkante des Fensters; die Referenz will im
  Querformat ein Seitenpaneel mit quer gesetztem Inhalt (2.3). Unser Fenster ist auf Hochformat
  gesperrt, also wird das Paneel **als Ganzes** gedreht.
- Aufbau in `activity_main.xml` (letztes Kind des Root-ConstraintLayouts, damit es über allem
  liegt; `GONE` wenn zu): ein bildschirmfüllender Container (`settings_sheet`, fängt Tipps
  außerhalb des Paneels ab und schließt das Blatt; Berührungen gehen nicht an den Sucher durch)
  mit dem Paneel (`settings_sheet_panel`, vertikales LinearLayout: Griff, Kopfzeile, Reiter,
  Zeilen-Container).
- Lage/Drehung: Bei `ui_rotation` 0 Paneel 426,7 × 538 dp unten. Bei 90/270 wird das Paneel mit
  **Breite = Bildschirmhöhe, Höhe = 470,7 dp** gemessen/gelegt, per `setRotation(±90)` gedreht
  und per Translation an die physische Unterkante gelegt (Vorbild `PhotoVideoSwitch`; Android
  bildet Touches auf gedrehten Views korrekt ab). Bei 180: `setRotation(180)`, Paneel oben.
  Innenabstände je Lage aus 2.2/2.3 (Griff-Position und Einzüge unterscheiden sich). Beim
  Drehen des Geräts mit offenem Blatt: Blatt sofort in der neuen Lage neu legen (keine
  Zwischenanimation nötig) – Aufruf aus `MainUI.rotateGlyphs()`.
- Öffnen: Dock-Button oder Wisch (3.6). Schließen: Tipp außerhalb, Wisch auf dem Paneel in
  Richtung physische Unterkante (Zieh-Schwelle wie Material: > 50 % der Paneelhöhe oder Fling),
  Zurück-Taste (`OnBackPressedCallback` nach dem Vorbild von `enablePopupOnBackPressedCallback`,
  nur aktiv solange offen), erneuter Tipp auf den Dock-Button, Kamerawechsel-Button
  (Blatt schließt, weil der Zustand wechselt), Start einer Aufnahme.
- **Animation (Entscheidung Falk 2026-09-20, „leichtgewichtig"):** Paneel gleitet entlang seiner
  lokalen y-Achse von ganz verdeckt nach sichtbar, **250 ms**, Kurve
  `MainUI.glyph_rotation_interpolator` (`cubic-bezier(0.2, 0, 0, 1)`), ohne Fade/Bounce; beim
  Schließen umgekehrt 200 ms. Sonst keine Animation (Reiter, Knöpfe, Werte schalten hart).
- Während das Blatt offen ist, läuft die Vorschau weiter; Pinch-Zoom und Fokus-Tipp sind
  außerhalb **nicht** erreichbar (Tipp schließt nur). Popup (`PopupView`) beim Öffnen des Blatts
  schließen (`closePopup()`); umgekehrt schließt `togglePopupSettings()` das Blatt.
- Immersive-Modus: Blatt liegt im Activity-Fenster, keine Sonderbehandlung; `setImmersiveMode(true)`
  (GUI ausblenden) schließt ein offenes Blatt.
- Beim Öffnen und nach jedem `updateForSettings()`/`cameraSetup()` (Kamera wird neu geöffnet,
  z. B. nach Auflösungswechsel) liest das Blatt **alle Zeilen neu** aus Preview/Preferences –
  es hält keinen eigenen Zustand. Das Blatt darf durch `updateForSettings()` nicht geschlossen
  werden (nur das Popup wird dort zerstört).

### 3.3 Foto-Blatt, Reiter „Allgemein" (Titel „Fotoeinstellungen", Reiter Allgemein / Pro)
Zeilen in dieser Reihenfolge, nur diese:
1. **Mehr Licht** – Optionen **Keines · Automatisch · Blitz** (Wert-Text genau so). Werte
   `flash_off` / `flash_auto` / `flash_on`; Frontkamera: `flash_off` / `flash_frontscreen_auto` /
   `flash_frontscreen_on` – d. h. je Position der Wert aus `preview.getSupportedFlashValues()`,
   der passt; Knöpfe für nicht unterstützte Werte weglassen; Zeile weglassen, wenn < 2 Werte.
   Anwenden: `preview.updateFlash(value)`, dann `mainUI.setPopupIcon()` und `mainUI.destroyPopup()`
   (exakt wie `PopupView` Z. 144–146). Ausgewählt = `preview.getCurrentFlashValue()`.
2. **Timer** – **Aus · 3 s · 5 s · 10 s**; `TimerPreferenceKey` Werte `"0" "3" "5" "10"` (Strings
   wie im Popup schreiben, `editor.apply()`, sonst nichts). Steht ein anderer Wert drin (z. B.
   `"15"` aus dem Popup): kein Knopf ausgewählt, Wert-Text zeigt den echten Wert („15 s").
   Nicht bei Panorama (Zeile ausblenden, wie Popup).
3. **Makrofokus** – **Aus · An**. An = `preview.updateFocus("focus_mode_macro", false, true)`;
   Aus = `preview.updateFocus("focus_mode_continuous_picture", false, true)` (Open-Camera-
   Standard, **nicht** `focus_mode_auto`). Ausgewählt: An wenn `getCurrentFocusValue()` ==
   `focus_mode_macro`, sonst Aus. Danach `destroyPopup()` wie im Popup (Z. 182). Zeile weglassen,
   wenn Makro nicht unterstützt (`getSupportedFocusValues()`).
4. **Verhältnis** – **Breitbild (16:9) · Vollbild (4:3)** (Wert-Text genau so). Bezugsliste
   `preview.getSupportedPictureSizes(true)`. Tipp schreibt die **größte Auflösung mit diesem
   Verhältnis innerhalb der aktuellen MP-Klasse** (siehe Pro/Auflösung; ohne Pro-Wahl = größte
   überhaupt mit diesem Verhältnis) als `"w h"` in
   `getResolutionPreferenceKey(preview.getCameraId(), getCameraIdSPhysicalPref())`, dann
   `updateForSettings(true, "", true, false)` (Verzögerung 400 ms wie Popup ist ok). Verhältnis-
   Toleranz ±0,02. Ausgewählt = Verhältnis von `getCurrentPictureSize()`; passt keins (z. B. 1:1):
   kein Knopf, Wert-Text z. B. „1:1". Nicht bei Panorama.

### 3.4 Foto-Blatt, Reiter „Pro"
Zeilen (aus `pixel-sheet-pro-hochformat.png`; die Regler-Beschreibung in `ui-spezifikation.md`
Abschnitt 4 „Reiter Pro" gehört zum **rechten** Dock-Button und ist hier nicht gemeint –
als Konflikt K13 in Abschnitt 9 eintragen):
1. **Auflösung** – zwei Knöpfe **12 MP** und **<Maximum> MP** (Zahl = gerundete Megapixel der
   jeweils größten Auflösung der Klasse im aktuellen Verhältnis). Klassen: „12" = größte
   Auflösung ≤ 13 MP; „Max" = größte Auflösung überhaupt. Meldet die Kamera nichts über 13 MP,
   **Zeile ausblenden**. Wert-Text „12MP" / „50MP" (ohne Leerzeichen, wie Referenz). Schreiben +
   `updateForSettings` wie bei Verhältnis (dieselbe Preference; Verhältnis bleibt erhalten).
   Falk vermutet, dass 50 MP am Pixel 9 Pro nur der Google-App vorbehalten ist – beim ersten
   Gerätetest die Liste ausgeben (Log oder Popup-Auflösungsliste) und Falk nennen.
2. **RAW/JPEG** – **Nur JPEG · RAW** (Wert-Text „Nur JPEG" / „RAW + JPEG"). `RawPreferenceKey`
   `preference_raw_no` / `preference_raw_yes`, danach `updateForSettings(true, "", true, false)`
   (RAW ändert die Aufnahme-Konfiguration). Nur wenn `preview.supportsRaw()`, sonst Zeile weg.
3. **Objektivauswahl** – **Automatisch · Manuell** (Wert-Text genau so). Nur bei Kameras mit
   `hasPhysicalCameras()` (Rückkamera), sonst Zeile weg.
   - Automatisch = logische Kamera: `userSwitchToCamera(getActualCameraId(), null)` wenn aktuell
     eine physische gewählt ist (`getCameraIdSPhysicalPref() != null`). Zoom-Pille zeigt die
     normalen Stufen.
   - Manuell = **die Zoom-Pille zeigt statt `,5 1× 2 5` die Beschriftungen `UW · W · T`**
     (Ultraweit, Weit = Hauptlinse, Tele), je ein Knopf pro physischer Linse der Rück-Kamera,
     Zuordnung nach Sichtwinkel absteigend (Vorbild `updatePanoramaLensCache()`); gibt es nur
     zwei Linsen, nur zwei Knöpfe. Tipp = `userSwitchToCamera(getActualCameraId(), physicalId)`
     (wie `switchPanoramaLens()`), aktiver Knopf = weißer Kreis wie bei den Zoomstufen. Beim
     Wechsel auf Manuell zunächst „W" (Hauptlinse: die physische Linse, deren Sichtwinkel dem
     der logischen Kamera am nächsten ist). Das Rad (Ziehen auf der Pille) ist im Manuell-Modus
     aus; Pinch-Zoom bleibt wie Open Camera es auf der physischen Kamera anbietet.
   - Zustand „Manuell" = `getCameraIdSPhysicalPref() != null` (Open Camera speichert das bereits);
     Frontkamera-Wechsel und zurück: Open-Camera-Verhalten, nichts erfinden. Panorama nutzt
     weiterhin sein eigenes Linsenpaar (nicht anfassen).
   - Nicht aus der Referenz bekannt (in Abschnitt 8 als „gewählt" notieren): Rad-Sperre,
     Startlinse „W".

### 3.5 Video-Blatt (Titel „Videoeinstellungen", Reiter Allgemein / Mehr) – im Videomodus
Gleiche Geometrie wie das Foto-Blatt (kein Video-Screenshot; Beschreibung
`pixel-video-blatt-beschreibung.md` 2.2/2.3). Google-spezifische Zeilen entfallen
(Video-Optimierung, 10-Bit-HDR, Sprachqualität, Stabilisierungs-Stufen).
**Allgemein:**
1. **Blitz** – **Aus · An**: Werte aus `getSupportedFlashValues()` gefiltert mit
   `Preview.isFlashSupportedForVideo()` (erwartet `flash_off` / `flash_torch`); `updateFlash`
   wie oben. Icons `flash_off` / `flash_on`.
2. **Auflösung** – **FHD · 4K (· 8K)**: Kandidaten aus `preview.getSupportedVideoQuality(
   getVideoFPSPref())` (Fallback unfiltered wie Popup Z. 462–467), je Knopf der Eintrag mit
   Bildgröße 1920×1080 / 3840×2160 / 7680×4320 (`preview.getVideoQualityHander()` liefert die
   Größe zum Quality-String); fehlende Größen = kein Knopf. Schreiben wie Popup Z. 507
   (`getVideoQualityPreferenceKey(cameraId, physical, fpsIsHighSpeed())`), dann
   `updateForSettings(true, "", true, false)`. Wert-Text „FHD" / „4K" / „8K".
3. **fps** – **24 · 30 · 60**: `getVideoFPSPreferenceKey(cameraId, physical)` Werte `"24"`,
   `"30"`, `"60"`; nur anbieten, was `preview` für die aktuelle Auflösung unterstützt (dieselbe
   Quelle, die die Einstellungsliste nutzt: `MyApplicationInterface.getSupportedVideoCaptureRates()`
   / `preview.getVideoQualityHander()`); danach `updateForSettings`. Ist `"default"` gespeichert:
   kein Knopf ausgewählt, Wert-Text „Automatisch".
4. **Makrofokus** – **Aus · An**: wie Foto, Aus = `focus_mode_continuous_video`.
**Mehr:**
5. **Videostabilisierung** – **Aus · An**: `VideoStabilizationPreferenceKey` (boolean), danach
   `updateForSettings(true, "", true, false)`; nur wenn `supportsVideoStabilization()`.
Während der Videoaufnahme ist der Dock-Button unsichtbar und der Wisch öffnet nichts.

### 3.6 Wisch-Geste auf dem Sucher
- Ort: **`Preview.gestureDetector`** (Z. 493) – dort kommen alle Sucher-Berührungen an; das
  bestehende `SimpleOnGestureListener`-Objekt bekommt `onFling()`. `MainActivity.MyGestureDetector`
  (nur bei gesperrtem Bildschirm) **bleibt unverändert**.
- Auslöser: Fling in Richtung **physische Unterkante** (Hochformat: nach unten; Querformat:
  zur Dock-Seite; auf dem Kopf: nach oben – aus `ui_rotation` ableiten), Mindestweg 160 dp,
  Mindestgeschwindigkeit `ViewConfiguration.getScaledMinimumFlingVelocity()` (wie
  `MyGestureDetector`), Ein-Finger. Weitermeldung über `ApplicationInterface` (neue Methode, in
  `BasicApplicationInterface` leer) → `MainActivity` öffnet das passende Blatt.
- Nicht während Video-/Panorama-Aufnahme, nicht bei gesperrtem Bildschirm, nicht wenn das
  Popup offen ist (Popup schließt durch die Berührung ohnehin), nicht während Pinch.

### 3.7 Synchronität mit Icon-Leiste und Popup (bis Schritt C)
Blatt und `PopupView` schreiben dieselben Preferences über dieselben Funktionen und lesen
beim Öffnen frisch. Nach jeder Änderung im Blatt: `mainUI.destroyPopup()` (das Popup baut sich
beim nächsten Öffnen neu) und `setPopupIcon()` bei Blitz. Icon-Leiste, `PopupView`,
`settings`-Taste, Farben (7.3) **nicht anfassen**.

### 3.8 Strings
Neue Texte in `values/strings.xml` **und** `values-de/strings.xml` (Deutsch ist die
Referenz): Fotoeinstellungen, Videoeinstellungen, Allgemein, Pro, Mehr, Mehr Licht, Keines,
Automatisch, Blitz, Timer, Aus, An, Makrofokus, Verhältnis, Breitbild (16:9), Vollbild (4:3),
Auflösung, RAW/JPEG, Nur JPEG, RAW + JPEG, Objektivauswahl, Manuell, fps, Videostabilisierung,
UW, W, T, contentDescriptions. Bestehende Strings (`flash_off` = „Ohne Blitz" usw.) **nicht**
verwenden – Wortlaut wie Referenz.

## 4. Nicht bauen
Rechter Dock-Button, Kopfzeile, Abschaffung von Icon-Leiste/Popup, Karussell, Fokus-Ring,
„?"-Hilfeknöpfe neben Werten (Referenz hat sie bei Makrofokus – weglassen, keine Hilfetexte),
Top-Foto, Nachtsicht, Objektiv-Automatik-Erkennung, weitere Verhältnisse, Doppel-Tap-Änderung (K8),
Farben/Blur/Schrift über das hier Genannte hinaus.

## 5. Abnahme am Pixel 9 Pro (Falk, gebündelt nach Fertigstellung, nicht nach jedem Handgriff)
1. Screenshot-Vergleich Hochformat Allgemein/Pro gegen die Referenz-PNGs (Overlay bei 2,25 px/dp):
   Paneel-Oberkante, Griff, Titel, „…", Reiter, Zeilenraster, Knopf-Positionen.
2. Querformat beide Richtungen + auf dem Kopf: Paneel an der physischen Unterkante, Inhalt
   aufrecht, Griff senkrecht, Einzüge 36/40 dp.
3. Jede Zeile in beide Richtungen: Blatt → Popup zeigt denselben Wert; Popup → Blatt zeigt
   denselben Wert (Blitz, Fokus, Timer, Auflösung, Video-Auflösung, fps, Stabilisierung, RAW).
4. Kamerawechsel vorne/hinten mit offenem Blatt → Blatt schließt; erneut öffnen → Werte der
   neuen Kamera (Blitz vorne = Frontblitz-Werte, keine Objektiv-Zeile).
5. Foto mit Timer 3 s; Makro-Nahaufnahme; 16:9 ↔ 4:3 sichtbar im Sucher; 12 MP ↔ Max (Dateigröße);
   RAW+JPEG erzeugt DNG; Manuell → Pille `UW · W · T`, jede Linse schaltet hörbar/sichtbar,
   Automatisch → Stufen zurück.
6. Video: FHD ↔ 4K, 30 ↔ 60 fps, Stabilisierung, Blitz an (Taschenlampe) – Datei prüfen.
7. Öffnen per Button und per Wisch; Schließen per Wisch/Tipp daneben/Zurück/Button; während
   Aufnahme kein Öffnen; Immersive-Modus; Popup und Blatt nie gleichzeitig offen.
8. Drehen des Geräts mit offenem Blatt.

## 6. Abschluss
- `ui-spezifikation.md`: Abschnitt 4 (gemessene Werte aus 2.2/2.3 eintragen, „NOCH NICHT
  UMGESETZT" entfernen, Pro-Zeilen korrigieren), 4a (Video-Blatt: gebaute Zeilen), 4b („…" →
  `openSettings()`, Objektivauswahl-Manuell-Definition), 6 (gebaut), 7.1 Punkte 2–3
  (erledigt bis auf rechten Button), 8 (Abweichungen: keine Abdunklung, keine „?"-Knöpfe,
  Rad-Sperre/Startlinse gewählt, Gleit-Animation als Ausnahme, Video-Zeilen ohne Screenshot),
  9 (K13: Pro-Reiter-Beschreibung ≠ Screenshot).
- `docs/SPEC.md` 4.5: Stand + PM-Punkt 10 (A+B-Blattanteil erledigt, offen: rechter Dock-Button,
  dann C). `docs/changelog.md` ≤ 10 Zeilen.
- `graphify update .`, Commit **mit Dateipfaden** auf dem Bench-Branch, Push des Branches.
- Bericht (max. 15 Zeilen): was gebaut, was abweicht, was offen; Liste der Auflösungen/fps, die
  die Kamera gemeldet hat.
