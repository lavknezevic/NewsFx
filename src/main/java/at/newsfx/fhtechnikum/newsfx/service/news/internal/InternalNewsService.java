package at.newsfx.fhtechnikum.newsfx.service.news.internal;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class InternalNewsService implements InternalNewsInterface {

    private final List<NewsItem> internalNews = new ArrayList<>();

    @Override
    public List<NewsItem> loadInternalNews() {
        return internalNews;
    }

    @Override
    public void addInternalNews(NewsItem newsItem) {
        internalNews.add(0, newsItem);
    }
}