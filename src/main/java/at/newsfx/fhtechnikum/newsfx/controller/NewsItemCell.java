package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NewsItemCell extends ListCell<NewsItem> {

    private final boolean enableInternalActions;
    private final boolean enableFavorites;
    private final Consumer<NewsItem> onEdit;
    private final Consumer<NewsItem> onDelete;
    private final Consumer<NewsItem> onFavoriteToggle;
    private final Predicate<String> isFavorited;

    public NewsItemCell() {
        this(false, false, null, null, null, null);
    }

    public NewsItemCell(boolean enableInternalActions, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete) {
        this(enableInternalActions, false, onEdit, onDelete, null, null);
    }

    public NewsItemCell(boolean enableInternalActions, boolean enableFavorites, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete, Consumer<NewsItem> onFavoriteToggle) {
        this(enableInternalActions, enableFavorites, onEdit, onDelete, onFavoriteToggle, null);
    }

    public NewsItemCell(boolean enableInternalActions, boolean enableFavorites, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete, Consumer<NewsItem> onFavoriteToggle, Predicate<String> isFavorited) {
        this.enableInternalActions = enableInternalActions;
        this.enableFavorites = enableFavorites;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onFavoriteToggle = onFavoriteToggle;
        this.isFavorited = isFavorited;
    }

    @Override
    protected void updateItem(NewsItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        Label title = new Label(item.getTitle());
        title.getStyleClass().addAll("headline", "news-card-title");
        title.setWrapText(true);

        Label sourceLabel = new Label(item.getSource() != null ? item.getSource() : (item.isExternal() ? "External" : "Internal"));
        sourceLabel.getStyleClass().add("news-card-meta");

        ImageView imageView = null;
        if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            Image image = new Image(item.getImageUrl(), true);
            imageView = new ImageView(image);
            imageView.setFitWidth(420);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.getStyleClass().add("news-card-image");
        }

        // Strip HTML tags from summary
        String cleanSummary = stripHtml(item.getSummary());
        Label summary = new Label(cleanSummary);
        summary.setWrapText(true);
        summary.getStyleClass().add("news-card-summary");
        summary.setMaxWidth(Double.MAX_VALUE);
        summary.setMinHeight(Region.USE_PREF_SIZE);

        VBox header = new VBox(2);
        header.getChildren().addAll(title, sourceLabel);
        header.getStyleClass().add("news-card-header");

        VBox box = new VBox(10);
        box.getStyleClass().add("news-card");
        box.getChildren().add(header);

        if (imageView != null) {
            box.getChildren().add(imageView);
        }

        box.getChildren().add(summary);

        String articleUrl = item.getArticleUrl() != null && !item.getArticleUrl().isBlank()
                ? item.getArticleUrl()
                : item.getLinkUrl();
        if (articleUrl != null && !articleUrl.isBlank()) {
            Hyperlink link = new Hyperlink("Open original article");
            link.getStyleClass().add("news-card-link");
            link.setOnAction(e -> openLink(articleUrl));
            box.getChildren().add(link);
        }


        if (item.getPdfPath() != null && !item.getPdfPath().isBlank()) {
            Button pdfButton = new Button("Open PDF");
            pdfButton.setOnAction(e -> openPdf(item.getPdfPath()));
            box.getChildren().add(pdfButton);
        }

    HBox actions = new HBox(10);
    actions.getStyleClass().add("news-card-actions");
    actions.setAlignment(Pos.CENTER_RIGHT);

        if (enableFavorites && !item.isExternal()) {
            boolean isFav = isFavorited != null && isFavorited.test(item.getId());
            Button starButton = new Button(isFav ? "★" : "☆");
            starButton.getStyleClass().addAll("star-button", isFav ? "star-filled" : "star-empty");
            starButton.setOnAction(e -> {
                if (onFavoriteToggle != null) {
                    onFavoriteToggle.accept(item);
                    // Provide immediate feedback
                    boolean nowFav = isFavorited != null && isFavorited.test(item.getId());
                    starButton.setText(nowFav ? "★" : "☆");
                    starButton.getStyleClass().removeAll("star-filled", "star-empty");
                    starButton.getStyleClass().add(nowFav ? "star-filled" : "star-empty");
                }
            });
            actions.getChildren().add(starButton);
        }

        if (enableInternalActions && !item.isExternal()) {
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> {
                if (onEdit != null) {
                    onEdit.accept(item);
                }
            });

            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> {
                if (onDelete != null) {
                    onDelete.accept(item);
                }
            });

            actions.getChildren().addAll(editButton, deleteButton);
        }

        if (!actions.getChildren().isEmpty()) {
            box.getChildren().add(actions);
        }

        setText(null);
        setGraphic(box);
    }

    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showError("Could not open link");
        }
    }

    private void openPdf(String pdfPath) {
        try {
            Desktop.getDesktop().open(new File(new URI(pdfPath)));
        } catch (Exception e) {
            showError("Could not open PDF");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        // Remove HTML tags
        String text = html.replaceAll("<[^>]*>", "");
        // Decode HTML entities
        text = text.replace("&amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        text = text.replace("&#39;", "'");
        text = text.replace("&nbsp;", " ");
        // Trim
        text = text.trim();
        
        // Limit to approximately 3 lines (about 180 characters)
        if (text.length() > 180) {
            text = text.substring(0, 180);
            // Find last space to avoid cutting words
            int lastSpace = text.lastIndexOf(' ');
            if (lastSpace > 150) {
                text = text.substring(0, lastSpace);
            }
            text = text.trim() + "...";
        }
        return text;
    }
}
