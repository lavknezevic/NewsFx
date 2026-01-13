package at.newsfx.fhtechnikum.newsfx.persistence;

import at.newsfx.fhtechnikum.newsfx.config.AppConfig;
import at.newsfx.fhtechnikum.newsfx.util.error.TechnicalException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private Database() {
        // utility
    }

    public static Connection getConnection() {
        try {
            ensureDbDirectoryExists();
            return DriverManager.getConnection(
                    AppConfig.dbUrl(),
                    AppConfig.dbUser(),
                    AppConfig.dbPassword()
            );
        } catch (SQLException e) {
            throw new TechnicalException("Failed to connect to database", e);
        }
    }

    private static void ensureDbDirectoryExists() {
        // For jdbc:h2:file:./newsfx-db/newsfx;AUTO_SERVER=TRUE ensure ./newsfx-db exists.
        String url = AppConfig.dbUrl();
        if (url == null) {
            return;
        }

        String marker = "jdbc:h2:file:";
        int idx = url.indexOf(marker);
        if (idx < 0) {
            return;
        }

        String pathPart = url.substring(idx + marker.length());
        int semicolon = pathPart.indexOf(';');
        if (semicolon >= 0) {
            pathPart = pathPart.substring(0, semicolon);
        }

        if (pathPart.startsWith("./")) {
            pathPart = pathPart.substring(2);
        }

        int lastSlash = pathPart.lastIndexOf('/');
        if (lastSlash <= 0) {
            return;
        }

        String dir = pathPart.substring(0, lastSlash);
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Path.of(dir));
        } catch (Exception e) {
            throw new TechnicalException("Failed to create DB directory: " + dir, e);
        }
    }

    public static void initSchema() {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id IDENTITY PRIMARY KEY,
                    username VARCHAR(64) NOT NULL UNIQUE,
                    password_hash VARCHAR(256) NOT NULL,
                    role VARCHAR(16) NOT NULL
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS internal_news (
                    id VARCHAR(64) PRIMARY KEY,
                    title VARCHAR(256) NOT NULL,
                    summary VARCHAR(256) NOT NULL,
                    content CLOB NOT NULL,
                    source VARCHAR(64) NOT NULL,
                    published_at TIMESTAMP NOT NULL,
                    image_url VARCHAR(2048),
                    link_url VARCHAR(2048),
                    pdf_path VARCHAR(2048),
                    created_by BIGINT NOT NULL,
                    last_modified_by BIGINT,
                    CONSTRAINT fk_internal_news_created_by FOREIGN KEY (created_by) REFERENCES users(id),
                    CONSTRAINT fk_internal_news_last_modified_by FOREIGN KEY (last_modified_by) REFERENCES users(id)
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS user_favorites (
                    user_id BIGINT NOT NULL,
                    news_id VARCHAR(64) NOT NULL,
                    PRIMARY KEY (user_id, news_id),
                    CONSTRAINT fk_user_favorites_user_id FOREIGN KEY (user_id) REFERENCES users(id),
                    CONSTRAINT fk_user_favorites_news_id FOREIGN KEY (news_id) REFERENCES internal_news(id)
                )
            """);
        } catch (SQLException e) {
            throw new TechnicalException("Failed to initialize database schema", e);
        }
    }
}
