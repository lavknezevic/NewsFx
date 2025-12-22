package at.newsfx.fhtechnikum.newsfx.service;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DummyNewsService implements NewsService {

    @Override
    public List<NewsItem> loadLatest() {
        return List.of(
                new NewsItem(
                        UUID.randomUUID().toString(),
                        "Welcome to NewsFx",
                        "This is a dummy news item.",
                        "This news item is used for UI development.",
                        "NewsFx",
                        LocalDateTime.now().minusHours(1),
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
                        true
                )
        );
    }
}
