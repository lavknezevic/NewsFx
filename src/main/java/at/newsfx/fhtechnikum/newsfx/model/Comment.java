package at.newsfx.fhtechnikum.newsfx.model;

import java.time.LocalDateTime;

public class Comment {

    private final String id;
    private final String newsId;
    private final String text;
    private final LocalDateTime createdAt;
    private final long createdBy;
    private final String createdByUsername;

    public Comment(
            String id,
            String newsId,
            String text,
            LocalDateTime createdAt,
            long createdBy,
            String createdByUsername
    ) {
        this.id = id;
        this.newsId = newsId;
        this.text = text;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.createdByUsername = createdByUsername;
    }

    @Override
    public String toString() {
        return text;
    }

    public String getId() {
        return id;
    }

    public String getNewsId() {
        return newsId;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public long getCreatedBy() {
        return createdBy;
    }
}
