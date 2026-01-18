package at.newsfx.fhtechnikum.newsfx.service.news.external;

import java.util.Objects;

public class RssSource {
    private final String name;
    private final String url;
    private final String displayName;

    public RssSource(String name, String url, String displayName) {
        this.name = Objects.requireNonNull(name);
        this.url = Objects.requireNonNull(url);
        this.displayName = Objects.requireNonNull(displayName);
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
