package at.newsfx.fhtechnikum.newsfx.viewmodel;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.persistence.FavoritesRepository;
import at.newsfx.fhtechnikum.newsfx.service.FavoritesService;
import at.newsfx.fhtechnikum.newsfx.service.news.external.ExternalNewsInterface;
import at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsInterface;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class MainViewModel {

    private final StringProperty title = new SimpleStringProperty();
    private final ExternalNewsInterface externalNewsInterface;
    private final InternalNewsInterface internalNewsInterface;
    private final FavoritesService favoritesService;
    private final FavoritesRepository favoritesRepository;
    private long currentUserId;

    private final ObservableList<NewsItem> externalNews =
            FXCollections.observableArrayList();

    private final ObservableList<NewsItem> internalNews =
            FXCollections.observableArrayList();

    private final ObservableList<NewsItem> favoritesNews =
            FXCollections.observableArrayList();


    public MainViewModel(ExternalNewsInterface externalNewsInterface, InternalNewsInterface internalNewsInterface, FavoritesService favoritesService, FavoritesRepository favoritesRepository) {
        this.externalNewsInterface = externalNewsInterface;
        this.internalNewsInterface = internalNewsInterface;
        this.favoritesService = favoritesService;
        this.favoritesRepository = favoritesRepository;
        title.set("Welcome to NewsFx");
    }

    public ObservableList<NewsItem> externalNewsProperty() {
        return externalNews;
    }

    public ObservableList<NewsItem> internalNewsProperty() {
        return internalNews;
    }

    public ObservableList<NewsItem> favoritesNewsProperty() {
        return favoritesNews;
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public void loadExternalNews() {
        externalNews.setAll(externalNewsInterface.loadExternalLatest());
    }

    public void loadInternalNews() {
        internalNews.setAll(internalNewsInterface.loadInternalNews());
    }

    public void loadFavorites() {
        List<String> favoriteIds = favoritesRepository.getFavoritesByUser(currentUserId);
        List<NewsItem> favorites = internalNews.stream()
                .filter(item -> favoriteIds.contains(item.getId()))
                .toList();
        favoritesNews.setAll(favorites);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void addFavorite(String newsId) {
        favoritesService.addFavorite(currentUserId, newsId);
        loadFavorites();
    }

    public void removeFavorite(String newsId) {
        favoritesService.removeFavorite(currentUserId, newsId);
        loadFavorites();
    }

    public void toggleFavorite(String newsId) {
        favoritesService.toggleFavorite(currentUserId, newsId);
        loadFavorites();
    }

    public boolean isFavorite(String newsId) {
        return favoritesService.isFavorite(currentUserId, newsId);
    }

    public void addInternalNewsRuntime(NewsItem newsItem) {
        internalNewsInterface.addInternalNews(newsItem);
        internalNews.add(0, newsItem);
    }

    public void updateInternalNewsRuntime(NewsItem newsItem) {
        internalNewsInterface.updateInternalNews(newsItem);

        for (int i = 0; i < internalNews.size(); i++) {
            NewsItem existing = internalNews.get(i);
            if (existing != null && existing.getId().equals(newsItem.getId())) {
                internalNews.set(i, newsItem);
                return;
            }
        }

        // Fallback: if not found locally, refresh from persistence
        loadInternalNews();
    }

    public void deleteInternalNewsRuntime(String id) {
        internalNewsInterface.deleteInternalNews(id);
        internalNews.removeIf(item -> item != null && id.equals(item.getId()));
    }
}
