package at.newsfx.fhtechnikum.newsfx.config;

import at.newsfx.fhtechnikum.newsfx.util.error.TechnicalException;

import java.io.InputStream;
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
        // utility
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

    public static String appName() {
        return getRequired("app.name");
    }

    public static String windowTitle() {
        return getRequired("ui.window.title");
    }

    public static String newsApiBaseUrl() {
        return getRequired("news.api.baseUrl");
    }

    public static int newsApiTimeoutSeconds() {
        return Integer.parseInt(
                getRequired("news.api.timeoutSeconds")
        );
    }
}