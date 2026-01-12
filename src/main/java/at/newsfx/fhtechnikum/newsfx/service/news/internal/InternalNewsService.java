package at.newsfx.fhtechnikum.newsfx.service.news.internal;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.persistence.InternalNewsRepository;
import at.newsfx.fhtechnikum.newsfx.service.auth.AuthService;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;

import java.util.List;
import java.util.Objects;

public class InternalNewsService implements InternalNewsInterface {

    private final InternalNewsRepository internalNewsRepository;
    private final AuthService authService;

    public InternalNewsService(AuthService authService, InternalNewsRepository internalNewsRepository) {
        this.authService = Objects.requireNonNull(authService, "authService");
        this.internalNewsRepository = Objects.requireNonNull(internalNewsRepository, "internalNewsRepository");
    }

    @Override
    public List<NewsItem> loadInternalNews() {
        return internalNewsRepository.findAll()
                .stream()
                .map(InternalNewsRepository.NewsItemRow::toNewsItem)
                .toList();
    }

    @Override
    public void addInternalNews(NewsItem newsItem) {
        requireManageInternal();
        if (newsItem == null) {
            throw new UserException("News item is required.");
        }

        long userId = authService.requireUser().getId();
        internalNewsRepository.insert(new InternalNewsRepository.NewsItemRow(
                newsItem.getId(),
                newsItem.getTitle(),
                newsItem.getSummary(),
                newsItem.getContent(),
                "Internal",
                newsItem.getPublishedAt(),
                newsItem.getImageUrl(),
                newsItem.getLinkUrl(),
                newsItem.getPdfPath(),
                userId,
                null
        ));
    }

    @Override
    public void updateInternalNews(NewsItem newsItem) {
        requireManageInternal();
        if (newsItem == null || newsItem.getId() == null || newsItem.getId().isBlank()) {
            throw new UserException("News item id is required.");
        }

        long userId = authService.requireUser().getId();
        internalNewsRepository.update(new InternalNewsRepository.NewsItemRow(
                newsItem.getId(),
                newsItem.getTitle(),
                newsItem.getSummary(),
                newsItem.getContent(),
                "Internal",
                newsItem.getPublishedAt(),
                newsItem.getImageUrl(),
                newsItem.getLinkUrl(),
                newsItem.getPdfPath(),
                userId,
                userId
        ));
    }

    @Override
    public void deleteInternalNews(String id) {
        requireManageInternal();
        if (id == null || id.isBlank()) {
            throw new UserException("News item id is required.");
        }
        internalNewsRepository.deleteById(id);
    }

    private void requireManageInternal() {
        if (!authService.canManageInternalNews()) {
            throw new UserException("You are not allowed to create/edit/delete internal news.");
        }
    }
}