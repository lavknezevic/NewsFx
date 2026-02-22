package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.config.AppConfig;
import at.newsfx.fhtechnikum.newsfx.controller.components.CommentSectionFactory;
import at.newsfx.fhtechnikum.newsfx.controller.components.ReactionBarFactory;
import at.newsfx.fhtechnikum.newsfx.controller.components.TextUtils;
import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.service.reaction.ReactionService;
import at.newsfx.fhtechnikum.newsfx.service.reaction.ReactionTargetType;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NewsItemCell extends ListCell<NewsItem> {

    private final boolean enableInternalActions;
    private final boolean enableFavorites;
    private final Consumer<NewsItem> onEdit;
    private final Consumer<NewsItem> onDelete;
    private final Consumer<NewsItem> onFavoriteToggle;
    private final Predicate<String> isFavorited;
    
    private final ReactionBarFactory reactionBarFactory;
    private final CommentSectionFactory commentSectionFactory;

    public NewsItemCell() {
        this(false, false, null, null, null, null, null, null);
    }

    public NewsItemCell(boolean enableInternalActions, boolean enableFavorites, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete, Consumer<NewsItem> onFavoriteToggle, Predicate<String> isFavorited, BiConsumer<NewsItem, String> onAddComment, ReactionService reactionService) {
        this.enableInternalActions = enableInternalActions;
        this.enableFavorites = enableFavorites;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onFavoriteToggle = onFavoriteToggle;
        this.isFavorited = isFavorited;
        this.reactionBarFactory = reactionService != null ? new ReactionBarFactory(reactionService) : null;
        this.commentSectionFactory = new CommentSectionFactory(reactionBarFactory, onAddComment);
    }

    @Override
    protected void updateItem(NewsItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        VBox box = new VBox(10);
        box.getStyleClass().add("news-card");

        box.getChildren().add(createHeader(item));

        if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            box.getChildren().add(createImageView(item.getImageUrl()));
        }

        box.getChildren().add(createSummary(item.getSummary()));

        if (!item.isExternal() && reactionBarFactory != null) {
            box.getChildren().add(reactionBarFactory.createReactionsBar(ReactionTargetType.NEWS, item.getId()));
        }

        String articleUrl = item.getArticleUrl() != null && !item.getArticleUrl().isBlank()
                ? item.getArticleUrl()
                : item.getLinkUrl();
        if (articleUrl != null && !articleUrl.isBlank()) {
            box.getChildren().add(createArticleLink(articleUrl));
        }

        if (item.getPdfPath() != null && !item.getPdfPath().isBlank()) {
            box.getChildren().add(createPdfButton(item.getPdfPath()));
        }

        if (!item.isExternal()) {
            box.getChildren().add(commentSectionFactory.createCommentsSection(item));
        }

        HBox actions = createActionButtons(item);
        if (!actions.getChildren().isEmpty()) {
            box.getChildren().add(actions);
        }

        setText(null);
        setGraphic(box);
    }

    private VBox createHeader(NewsItem item) {
        Label title = new Label(item.getTitle());
        title.getStyleClass().addAll("headline", "news-card-title");
        title.setWrapText(true);

        Label sourceLabel = new Label(item.getSource() != null ? item.getSource() : (item.isExternal() ? "External" : "Internal"));
        sourceLabel.getStyleClass().add("news-card-meta");

        VBox header = new VBox(2);
        header.getChildren().addAll(title, sourceLabel);
        header.getStyleClass().add("news-card-header");
        return header;
    }

    private ImageView createImageView(String imageUrl) {
        Image image = new Image(imageUrl, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(AppConfig.imageFitWidth());
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("news-card-image");
        return imageView;
    }

    private Label createSummary(String summaryText) {
        String cleanSummary = TextUtils.toSummary(summaryText);
        Label summary = new Label(cleanSummary);
        summary.setWrapText(true);
        summary.getStyleClass().add("news-card-summary");
        summary.setMaxWidth(Double.MAX_VALUE);
        summary.setMinHeight(Region.USE_PREF_SIZE);
        return summary;
    }

    private Hyperlink createArticleLink(String url) {
        Hyperlink link = new Hyperlink("Open original article");
        link.getStyleClass().add("news-card-link");
        link.setOnAction(e -> openLink(url));
        return link;
    }

    private Button createPdfButton(String pdfPath) {
        Button pdfButton = new Button("Open PDF");
        pdfButton.setOnAction(e -> openPdf(pdfPath));
        return pdfButton;
    }

    private HBox createActionButtons(NewsItem item) {
        HBox actions = new HBox(10);
        actions.getStyleClass().add("news-card-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (enableFavorites && !item.isExternal()) {
            actions.getChildren().add(createFavoriteButton(item));
        }

        if (enableInternalActions && !item.isExternal()) {
            if (onEdit != null) {
                Button editButton = new Button("Edit");
                editButton.setOnAction(e -> onEdit.accept(item));
                actions.getChildren().add(editButton);
            }
            if (onDelete != null) {
                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(e -> onDelete.accept(item));
                actions.getChildren().add(deleteButton);
            }
        }

        return actions;
    }

    private Button createFavoriteButton(NewsItem item) {
        boolean isFav = isFavorited != null && isFavorited.test(item.getId());
        Button starButton = new Button(isFav ? "★" : "☆");
        starButton.getStyleClass().addAll("star-button", isFav ? "star-filled" : "star-empty");
        starButton.setOnAction(e -> {
            if (onFavoriteToggle != null) {
                onFavoriteToggle.accept(item);
                boolean nowFav = isFavorited != null && isFavorited.test(item.getId());
                starButton.setText(nowFav ? "★" : "☆");
                starButton.getStyleClass().removeAll("star-filled", "star-empty");
                starButton.getStyleClass().add(nowFav ? "star-filled" : "star-empty");
            }
        });
        return starButton;
    }

    private void openLink(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showError("Could not open link");
        }
    }

    private void openPdf(String pdfPath) {
        try {
            java.awt.Desktop.getDesktop().open(new File(new URI(pdfPath)));
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
}
