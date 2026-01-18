# NewsFx

JavaFX News-Anwendung (Maven), die externe News aus einem RSS-Feed lädt und interne News lokal verwalten kann.
Zusätzlich gibt es ein einfaches Login/Registrierungs-Feature inkl. Rollen (USER/EDITOR/ADMIN) und User-Management.

## Features

- **External News (RSS)**: Lädt aktuelle Einträge aus einem konfigurierbaren RSS-Feed und zeigt Artikel per **WebView**.
- **Suche & Filter**: Suche im Titel, einfache Kategorieauswahl.
- **Internal News**: Erstellen/Bearbeiten/Löschen inkl. optionalem Link, Bild und PDF-Anhang (lokale Dateien).
  - **with Comments**
  - **with Reactions**
  - **with favorization**
- **Login/Register/Logout**: Accounts lokal in einer DB, Passwörter werden gehasht.
- **User Management (Admin)**: Rollen für User verwalten.

## Demo Accounts

Beim ersten Start (leere DB) werden Demo-User angelegt (konfigurierbar in `application.properties`):

- `admin` / `admin`
- `editor` / `editor`
- `user` / `user`

Demo-User können deaktiviert werden via `demo.users.enabled=false`.

## Voraussetzungen

- **Java 21**
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

### App & UI

| Key | Beschreibung | Default |
|-----|--------------|---------|
| `app.name` | Anzeigename | `NewsFx` |
| `ui.window.title` | Fenstertitel | `NewsFx` |
| `ui.window.width` | Fensterbreite | `1000` |
| `ui.window.height` | Fensterhöhe | `700` |
| `ui.window.minWidth` | Minimale Breite | `900` |
| `ui.window.minHeight` | Minimale Höhe | `600` |

### News & RSS

| Key | Beschreibung | Default |
|-----|--------------|---------|
| `news.http.timeoutSeconds` | HTTP Timeout | `10` |
| `news.http.userAgent` | User-Agent Header | `NewsFx` |
| `news.rss.sources` | RSS-Quellen (Format: `name\|url\|displayName,...`) | 4 vorkonfigurierte Quellen |
| `news.rss.maxItems` | Max. Einträge pro Feed | `50` |
| `news.summary.maxLength` | Max. Länge der Summary | `180` |
| `news.image.fitWidth` | Bildbreite in News-Cards | `420` |
| `news.defaultCategory` | Standard-Kategorie | `General` |

### Sicherheit

| Key | Beschreibung | Default |
|-----|--------------|---------|
| `security.password.iterations` | PBKDF2 Iterationen | `600000` |
| `security.password.keyLengthBits` | Key-Länge in Bits | `256` |
| `security.password.saltBytes` | Salt-Länge in Bytes | `16` |

### Demo-User

| Key | Beschreibung | Default |
|-----|--------------|---------|
| `demo.users.enabled` | Demo-User anlegen | `true` |
| `demo.users.admin.username` | Admin Username | `admin` |
| `demo.users.admin.password` | Admin Passwort | `admin` |
| `demo.users.editor.username` | Editor Username | `editor` |
| `demo.users.editor.password` | Editor Passwort | `editor` |
| `demo.users.user.username` | User Username | `user` |
| `demo.users.user.password` | User Passwort | `user` |

### Datenbank

| Key | Beschreibung | Default |
|-----|--------------|---------|
| `db.url` | JDBC URL | `jdbc:h2:file:./newsfx-db/newsfx;AUTO_SERVER=TRUE` |
| `db.user` | DB User | `sa` |
| `db.password` | DB Passwort | (leer) |

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
- **JavaFX / Module**: Das Projekt nutzt `module-info.java`. Falls man Module ändert, sicherstellen, dass benötigte `requires`/`opens` korrekt sind.
- **Kein RSS**: Prüfe `news.rss.sources` in der Konfiguration, Proxy/Firewall und Internetverbindung.
- **DB locked**: Stelle sicher, dass keine zweite Instanz läuft; ggf. `*.lock.db`/`*.trace.db` löschen.
