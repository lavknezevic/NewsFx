package at.newsfx.fhtechnikum.newsfx.config;

import at.newsfx.fhtechnikum.newsfx.util.error.TechnicalException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class AppConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream is = AppConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new TechnicalException(
                        "application.properties not found",
                        null
                );
            }

            PROPERTIES.load(is);

        } catch (Exception e) {
            throw new TechnicalException(
                    "Failed to load application configuration",
                    e
            );
        }
    }

    private AppConfig() {
    }

    private static String getRequired(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new TechnicalException(
                    "Missing required config key: " + key,
                    null
            );
        }
        return value;
    }

    private static String getOptional(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private static int getInt(String key, int defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public static String appName() {
        return getRequired("app.name");
    }

    public static String windowTitle() {
        return getRequired("ui.window.title");
    }

    public static int windowWidth() {
        return getInt("ui.window.width", 1000);
    }

    public static int windowHeight() {
        return getInt("ui.window.height", 700);
    }

    public static int windowMinWidth() {
        return getInt("ui.window.minWidth", 900);
    }

    public static int windowMinHeight() {
        return getInt("ui.window.minHeight", 600);
    }

    public static int httpTimeoutSeconds() {
        return getInt("news.http.timeoutSeconds", 10);
    }

    public static String httpUserAgent() {
        return getOptional("news.http.userAgent", "NewsFx");
    }

    public static int rssMaxItems() {
        return getInt("news.rss.maxItems", 50);
    }

    public static int summaryMaxLength() {
        return getInt("news.summary.maxLength", 180);
    }

    public static int imageFitWidth() {
        return getInt("news.image.fitWidth", 420);
    }

    public static String defaultCategory() {
        return getOptional("news.defaultCategory", "General");
    }

    public static List<String[]> rssSources() {
        String sources = getOptional("news.rss.sources", "");
        List<String[]> result = new ArrayList<>();
        if (sources.isBlank()) {
            return result;
        }
        for (String entry : sources.split(",")) {
            String[] parts = entry.split("\\|");
            if (parts.length == 3) {
                result.add(parts);
            }
        }
        return result;
    }

    public static int passwordIterations() {
        return getInt("security.password.iterations", 600_000);
    }

    public static int passwordKeyLengthBits() {
        return getInt("security.password.keyLengthBits", 256);
    }

    public static int passwordSaltBytes() {
        return getInt("security.password.saltBytes", 16);
    }

    public static boolean demoUsersEnabled() {
        return getBoolean("demo.users.enabled", true);
    }

    public static String demoAdminUsername() {
        return getOptional("demo.users.admin.username", "admin");
    }

    public static String demoAdminPassword() {
        return getOptional("demo.users.admin.password", "admin");
    }

    public static String demoEditorUsername() {
        return getOptional("demo.users.editor.username", "editor");
    }

    public static String demoEditorPassword() {
        return getOptional("demo.users.editor.password", "editor");
    }

    public static String demoUserUsername() {
        return getOptional("demo.users.user.username", "user");
    }

    public static String demoUserPassword() {
        return getOptional("demo.users.user.password", "user");
    }

    public static String dbUrl() {
        return getRequired("db.url");
    }

    public static String dbUser() {
        return getOptional("db.user", "sa");
    }

    public static String dbPassword() {
        return getOptional("db.password", "");
    }
}