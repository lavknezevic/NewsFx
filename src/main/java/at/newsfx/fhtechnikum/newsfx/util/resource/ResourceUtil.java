package at.newsfx.fhtechnikum.newsfx.util.resource;

import java.net.URL;
import java.util.Objects;

public final class ResourceUtil {

    private ResourceUtil() {
        // utility
    }

    public static URL get(String path) {
        return Objects.requireNonNull(
                ResourceUtil.class.getResource(path),
                "Resource not found: " + path
        );
    }
}