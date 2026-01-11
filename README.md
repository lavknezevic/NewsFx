# NewsFx

JavaFX News-Anwendung (Maven), die externe News aus einem RSS-Feed lädt und interne News lokal in der App verwalten kann.

## Features

- **External News (RSS)**: Lädt aktuelle Einträge aus einem konfigurierbaren RSS-Feed und zeigt Artikel per **WebView**.
- **Suche & Filter**: Suche im Titel, einfache Kategorieauswahl (derzeit UI-seitig).
- **Internal News**: Erstellen von internen Beiträgen inkl. optionalem Link, Bild und PDF-Anhang (lokale Dateien).
- **(Modernes UI) xD**: JavaFX + FXML + CSS.

## Voraussetzungen

- **Java 21** (gemäß `pom.xml`)
- Internetzugang für RSS-Feed (Default: ORF RSS)

Optional:
- Maven ist nicht nötig (Maven Wrapper ist enthalten: `mvnw`, `mvnw.cmd`).

## Starten

### Windows (PowerShell)

- App starten:
	- `./mvnw.cmd clean javafx:run`

### macOS / Linux

- App starten:
	- `./mvnw clean javafx:run`

Hinweis: Die Main-Klasse ist in Maven bereits konfiguriert (JavaFX Maven Plugin).

## Build & Tests

- Build (ohne UI Start):
	- `./mvnw.cmd clean package`
- Tests:
	- `./mvnw.cmd test`

## Konfiguration

Die App liest ihre Konfiguration aus [src/main/resources/application.properties](src/main/resources/application.properties).

Wichtige Keys:

- `app.name`: Anzeigename
- `ui.window.title`: Fenstertitel
- `news.rss.feedUrl`: RSS Feed URL (Default: `https://rss.orf.at/news.xml`)
- `news.http.timeoutSeconds`: HTTP Timeout in Sekunden

Wenn ein Key fehlt oder leer ist, bricht die App beim Start mit einer technischen Exception ab.

## Bedienung

- **External News**: In der Sidebar „External News“ öffnen, Eintrag doppelklicken oder `Enter` drücken.
- **Internal News**: „Internal News“ öffnen → „News Erstellen“ → Formular ausfüllen → „Save“.

## Architektur (Überblick)

- UI
	- FXML: [src/main/resources/view/MainView.fxml](src/main/resources/view/MainView.fxml)
	- Styles: [src/main/resources/css/application.css](src/main/resources/css/application.css)
- Einstiegspunkt
	- JavaFX `Application`: `at.newsfx.fhtechnikum.newsfx.app.NewsFxApplication`
- MVVM-ähnlich
	- Controller: `at.newsfx.fhtechnikum.newsfx.controller.*`
	- ViewModel: `at.newsfx.fhtechnikum.newsfx.viewmodel.MainViewModel`
- Services
	- External RSS: `at.newsfx.fhtechnikum.newsfx.service.news.external.RssExternalNewsInterface`
	- Internal (in-memory): `at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsService`

## Bekannte Einschränkungen

- **Internal News Persistenz**: Aktuell nur im Speicher (nach App-Neustart leer).
- **Kategorie-Filter**: Kategorien sind aktuell UI-seitig vorhanden; die Zuordnung der Artikel ist noch nicht implementiert.
- **PDF/Images**: Es werden Pfade/URIs zu lokalen Dateien gespeichert, kein Upload.

## Troubleshooting

- **Java-Version**: Prüfe `java -version` (muss Java 21 sein).
- **JavaFX / Module**: Das Projekt nutzt `module-info.java`. Falls du Module änderst, stelle sicher, dass benötigte `requires`/`opens` korrekt sind.
- **Kein RSS**: Prüfe `news.rss.feedUrl`, Proxy/Firewall und Internetverbindung.
