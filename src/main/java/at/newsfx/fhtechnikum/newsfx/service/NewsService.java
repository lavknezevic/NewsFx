package at.newsfx.fhtechnikum.newsfx.service;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;

import java.util.List;

public interface NewsService {
    List<NewsItem> loadLatest();
}