package at.newsfx.fhtechnikum.newsfx.model;

import java.time.LocalDateTime;

public class NewsItem {

    private final String id;
    private final String title;
    private final String summary;
    private final String content;
    private final String source;
    private final LocalDateTime publishedAt;
    private final String imageUrl;
    private final boolean external;
    private final String linkUrl;
    private final String pdfPath;
    private final String articleUrl;


    public NewsItem(
            String id,
            String title,
            String summary,
            String content,
            String source,
            LocalDateTime publishedAt,
            String imageUrl,
            String linkUrl,
            String pdfPath,
            boolean external,
            String articleUrl
    ) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.source = source;
        this.publishedAt = publishedAt;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.pdfPath = pdfPath;
        this.external = external;
        this.articleUrl = articleUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isExternal() {
        return external;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public String getArticleUrl() {
        return articleUrl;
    }
}
