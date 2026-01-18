package at.newsfx.fhtechnikum.newsfx.service.news.internal;

import at.newsfx.fhtechnikum.newsfx.model.Comment;
import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.persistence.FavoritesRepository;
import at.newsfx.fhtechnikum.newsfx.persistence.InternalNewsRepository;
import at.newsfx.fhtechnikum.newsfx.service.auth.AuthService;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class InternalNewsService implements InternalNewsInterface {

    private final InternalNewsRepository internalNewsRepository;
    private final FavoritesRepository favoritesRepository;
    private final AuthService authService;

    public InternalNewsService(AuthService authService, InternalNewsRepository internalNewsRepository, FavoritesRepository favoritesRepository) {
        this.authService = Objects.requireNonNull(authService, "authService");
        this.internalNewsRepository = Objects.requireNonNull(internalNewsRepository, "internalNewsRepository");
        this.favoritesRepository = Objects.requireNonNull(favoritesRepository, "favoritesRepository");
    }

    @Override
    public List<NewsItem> loadInternalNews() {

        List<NewsItem> news = internalNewsRepository.findAll()
                .stream()
                .map(InternalNewsRepository.NewsItemRow::toNewsItem)
                .toList();

        Map<String, NewsItem> newsById = news.stream()
                .collect(Collectors.toMap(NewsItem::getId, n -> n));

        List<InternalNewsRepository.CommentRow> comments =
                internalNewsRepository.findAllComments();

        for (InternalNewsRepository.CommentRow row : comments) {
            NewsItem item = newsById.get(row.newsId());
            if (item != null) {
                item.addComment(new Comment(
                        row.id(),
                        row.newsId(),
                        row.text(),
                        row.createdAt(),
                        row.createdBy(),
                        row.createdByUsername()
                ));
            }
        }

        return news;
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

        favoritesRepository.removeAllFavoritesForNews(id);
        internalNewsRepository.deleteById(id);
    }

    @Override
    public void addComment(Comment comment) {

        if (comment == null) {
            throw new IllegalArgumentException("Comment must not be null");
        }

        if (comment.getText() == null || comment.getText().isBlank()) {
            throw new IllegalArgumentException("Comment text must not be empty");
        }

        if (comment.getNewsId() == null) {
            throw new IllegalArgumentException("Comment must reference a news item");
        }


        InternalNewsRepository.CommentRow row =
                new InternalNewsRepository.CommentRow(
                        comment.getId(),
                        comment.getNewsId(),
                        comment.getText(),
                        comment.getCreatedAt(),
                        comment.getCreatedBy(),
                        comment.getCreatedByUsername()
                );

        internalNewsRepository.insertComment(row);
    }


    private void requireManageInternal() {
        if (!authService.canManageInternalNews()) {
            throw new UserException("You are not allowed to create/edit/delete internal news.");
        }
    }
}