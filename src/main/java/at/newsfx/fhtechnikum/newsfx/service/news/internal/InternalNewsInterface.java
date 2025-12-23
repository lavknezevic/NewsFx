package at.newsfx.fhtechnikum.newsfx.service.news.internal;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;

import java.util.List;

public interface InternalNewsInterface {
    List<NewsItem> loadInternalNews();
    void addInternalNews(NewsItem newsItem);

}
