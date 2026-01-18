package at.newsfx.fhtechnikum.newsfx.service;

import at.newsfx.fhtechnikum.newsfx.persistence.FavoritesRepository;

public class FavoritesService {

    private final FavoritesRepository favoritesRepository;

    public FavoritesService(FavoritesRepository favoritesRepository) {
        this.favoritesRepository = favoritesRepository;
    }

    public void addFavorite(long userId, String newsId) {
        if (!favoritesRepository.isFavorite(userId, newsId)) {
            favoritesRepository.addFavorite(userId, newsId);
        }
    }

    public void removeFavorite(long userId, String newsId) {
        favoritesRepository.removeFavorite(userId, newsId);
    }

    public boolean isFavorite(long userId, String newsId) {
        return favoritesRepository.isFavorite(userId, newsId);
    }

    public void toggleFavorite(long userId, String newsId) {
        if (isFavorite(userId, newsId)) {
            removeFavorite(userId, newsId);
        } else {
            addFavorite(userId, newsId);
        }
    }
}
