package at.newsfx.fhtechnikum.newsfx.service.news.external;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;

import java.util.List;

public interface ExternalNewsInterface {
    List<NewsItem> loadExternalLatest();
}