# NewsFx

JavaFX News-Anwendung (Maven), die externe News aus einem RSS-Feed lädt und interne News lokal verwalten kann.
Zusätzlich gibt es ein einfaches Login/Registrierungs-Feature inkl. Rollen (USER/EDITOR/ADMIN) und User-Management.

## Features

- **External News (RSS)**: Lädt aktuelle Einträge aus einem konfigurierbaren RSS-Feed und zeigt Artikel per **WebView**.
- **Suche & Filter**: Suche im Titel, einfache Kategorieauswahl.
- **Internal News**: Erstellen/Bearbeiten/Löschen inkl. optionalem Link, Bild und PDF-Anhang (lokale Dateien).
- **Login/Register/Logout**: Accounts lokal in einer DB, Passwörter werden gehasht.
- **User Management (Admin)**: Rollen für User verwalten.
- **(Modernes UI) xD**: JavaFX + FXML + CSS.

## Demo Accounts

Beim ersten Start (leere DB) werden Demo-User angelegt:

- `admin` / `admin`
- `editor` / `editor`
- `user` / `user`

## Voraussetzungen

- **Java 21** (gemäß `pom.xml`)
- Internetzugang für RSS-Feed (Default: ORF RSS)

Optional:
- Lokale DB (H2) wird automatisch per `db.url` angelegt.

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

Local DB:

- `db.url`: z.B. `jdbc:h2:file:./newsfx-db/newsfx;AUTO_SERVER=TRUE`
- `db.user`: Default `sa`
- `db.password`: Default leer

Wenn ein Key fehlt oder leer ist, bricht die App beim Start mit einer technischen Exception ab.

## Bedienung

- **Login**: App startet im Login-Screen.
- **External News**: In der Sidebar „External News“ öffnen, Eintrag doppelklicken oder `Enter` drücken.
- **Internal News**:
	- Nur **EDITOR/ADMIN** können erstellen/bearbeiten/löschen.
	- „Internal News“ öffnen → „News Erstellen“ → Formular ausfüllen → „Save“.
	- Für bestehende interne Einträge stehen **Edit/Delete** zur Verfügung.
- **User Management**: Nur **ADMIN** sieht den Button in der Sidebar und kann Rollen ändern.

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
	- Internal (DB): `at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsService`
	- Auth: `at.newsfx.fhtechnikum.newsfx.service.auth.AuthService`
- Persistence
	- DB/Schema: `at.newsfx.fhtechnikum.newsfx.persistence.Database`
	- Repositories: `at.newsfx.fhtechnikum.newsfx.persistence.*Repository`
- AppContext
	- Singleton-Wiring + Demo-Seeding: `at.newsfx.fhtechnikum.newsfx.config.AppContext`

## Bekannte Einschränkungen

- **Kategorie-Filter**: Kategorien sind vorhanden; die Zuordnung kann je nach News-Quelle noch eingeschränkt sein.
- **PDF/Images**: Es werden Pfade/URIs zu lokalen Dateien gespeichert, kein Upload.
- **DB Reset**: Zum Zurücksetzen kann der Ordner `./newsfx-db/` gelöscht werden (beim nächsten Start wird Schema + Demo-User neu erzeugt).

## Troubleshooting

- **Java-Version**: Prüfe `java -version` (muss Java 21 sein) + JavaFx Library (in unserem Fall Zulu).
- **JavaFX / Module**: Das Projekt nutzt `module-info.java`. Falls du Module änderst, stelle sicher, dass benötigte `requires`/`opens` korrekt sind.
- **Kein RSS**: Prüfe `news.rss.feedUrl`, Proxy/Firewall und Internetverbindung.
- **DB locked**: Stelle sicher, dass keine zweite Instanz läuft; ggf. `*.lock.db`/`*.trace.db` löschen.
