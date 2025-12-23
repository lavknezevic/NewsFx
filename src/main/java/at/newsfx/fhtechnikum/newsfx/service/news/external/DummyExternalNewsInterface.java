package at.newsfx.fhtechnikum.newsfx.service.news.external;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DummyExternalNewsInterface implements ExternalNewsInterface {

    @Override
    public List<NewsItem> loadExternalLatest() {
        return List.of(
                new NewsItem(
                        UUID.randomUUID().toString(),
                        "Welcome to NewsFx",
                        "This is a dummy news item.",
                        "This news item is used for UI development.",
                        "NewsFx",
                        LocalDateTime.now().minusHours(1),
                        null,
                        null,
                        null,
                        true
                ),
                new NewsItem(
                        UUID.randomUUID().toString(),
                        "External News Placeholder",
                        "More news will arrive soon.",
                        "API integration coming next.",
                        "External Source",
                        LocalDateTime.now().minusHours(2),
                        null,
                        null,
                        null,
                        true
                )
        );
    }
}
