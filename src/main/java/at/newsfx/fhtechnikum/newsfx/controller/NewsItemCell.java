package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.model.Comment;
import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.service.reaction.ReactionService;
import at.newsfx.fhtechnikum.newsfx.service.reaction.ReactionTargetType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.Node;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NewsItemCell extends ListCell<NewsItem> {

    private static final String[] DEFAULT_EMOJIS = new String[]{"👍", "❤️", "😂", "😮", "😢"};

    private final boolean enableInternalActions;
    private final boolean enableFavorites;
    private final Consumer<NewsItem> onEdit;
    private final Consumer<NewsItem> onDelete;
    private final BiConsumer<NewsItem, String> onAddComment;
    private static final DateTimeFormatter COMMENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final Consumer<NewsItem> onFavoriteToggle;
    private final Predicate<String> isFavorited;
    private final ReactionService reactionService;

    public NewsItemCell() {
        this(false, false, null, null, null, null, null, null);
    }

    public NewsItemCell(ReactionService reactionService) {
        this(false, false, null, null, null, null, null, reactionService);
    }

    public NewsItemCell(boolean enableInternalActions, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete) {
        this(enableInternalActions, false, onEdit, onDelete, null, null);
    }

    public NewsItemCell(boolean enableInternalActions, boolean enableFavorites, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete, Consumer<NewsItem> onFavoriteToggle) {
        this(enableInternalActions, enableFavorites, onEdit, onDelete, onFavoriteToggle, (Predicate<String>) null);
    }

    public NewsItemCell(boolean enableInternalActions, boolean enableFavorites, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete, Consumer<NewsItem> onFavoriteToggle, Predicate<String> isFavorited) {
        this(enableInternalActions, enableFavorites, onEdit, onDelete, onFavoriteToggle, isFavorited, null, null);
    }

    public NewsItemCell(boolean enableInternalActions, boolean enableFavorites, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete, Consumer<NewsItem> onFavoriteToggle, Predicate<String> isFavorited, BiConsumer<NewsItem, String> onAddComment) {
        this(enableInternalActions, enableFavorites, onEdit, onDelete, onFavoriteToggle, isFavorited, onAddComment, null);
    }

    public NewsItemCell(boolean enableInternalActions, boolean enableFavorites, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete, Consumer<NewsItem> onFavoriteToggle, Predicate<String> isFavorited, BiConsumer<NewsItem, String> onAddComment, ReactionService reactionService) {
        this.enableInternalActions = enableInternalActions;
        this.enableFavorites = enableFavorites;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onAddComment = onAddComment;
        this.onFavoriteToggle = onFavoriteToggle;
        this.isFavorited = isFavorited;
        this.reactionService = reactionService;
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

        // Reactions below each post (internal only)
        if (!item.isExternal() && reactionService != null) {
            box.getChildren().add(createReactionsBar(ReactionTargetType.NEWS, item.getId()));
        }

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

        // Comments are available for internal news for all roles
        if (!item.isExternal()) {
            box.getChildren().add(createCommentsSection(item));
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

    private VBox createCommentsSection(NewsItem item) {

        VBox section = new VBox(6);
        section.getStyleClass().add("comments-section");

        Label title = new Label("Comments");
        title.getStyleClass().add("comment-title");

        ListView<Comment> commentsList = new ListView<>();
        commentsList.setItems(item.getComments());
        commentsList.setPrefHeight(110);
        commentsList.setMaxHeight(160);


        commentsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);

                if (empty || comment == null) {
                    setGraphic(null);
                    return;
                }

                Label meta = new Label(comment.getCreatedByUsername() + " · " +
                        comment.getCreatedAt().format(COMMENT_DATE_FORMAT));
                meta.getStyleClass().add("comment-meta");


                Label text = new Label(comment.getText());
                text.setWrapText(true);

                VBox box = new VBox(4, meta, text);
                box.getStyleClass().add("comment-item");

                // Reactions below each comment
                if (reactionService != null) {
                    box.getChildren().add(createReactionsBar(ReactionTargetType.COMMENT, comment.getId()));
                }

                setGraphic(box);
                setText(null);
            }
        });

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");

        Button addButton = new Button("Add");

        // Allow read-only comments rendering in contexts where adding isn't wired.
        boolean canAdd = onAddComment != null;
        commentField.setDisable(!canAdd);
        addButton.setDisable(!canAdd);

        addButton.setOnAction(e -> {
            String text = commentField.getText();
            if (text == null || text.isBlank()) return;

            if (onAddComment != null) {
                onAddComment.accept(item, text);
            }

            commentField.clear();
        });

        HBox input = new HBox(6, commentField, addButton);
        HBox.setHgrow(commentField, Priority.ALWAYS);

        section.getChildren().addAll(title, commentsList, input);
        return section;
    }

    private HBox createReactionsBar(ReactionTargetType targetType, String targetId) {
        HBox bar = new HBox(6);
        bar.getStyleClass().add("reactions-bar");

        if (reactionService == null || targetId == null || targetId.isBlank()) {
            return bar;
        }

        ReactionService.ReactionSummary summary = reactionService.getSummaryForCurrentUser(targetType, targetId);
        Map<String, Integer> counts = summary.countsByEmoji();

        for (String emoji : DEFAULT_EMOJIS) {
            int count = counts.getOrDefault(emoji, 0);

            HBox chip = new HBox(6);
            chip.getStyleClass().add("reaction-chip");

            Button button = new Button();
            button.setUserData(emoji);
            button.getStyleClass().add("reaction-button");
            button.setFocusTraversable(false);

            String emojiClass = switch (emoji) {
                case "👍" -> "reaction-like";
                case "❤️" -> "reaction-love";
                case "😂" -> "reaction-laugh";
                case "😮" -> "reaction-wow";
                case "😢" -> "reaction-sad";
                default -> null;
            };
            if (emojiClass != null) {
                button.getStyleClass().add(emojiClass);
            }

            if (summary.reactedEmojis().contains(emoji)) {
                button.getStyleClass().add("reacted");
            }

            Node emojiNode = createEmojiNode(emoji);
            button.setGraphic(emojiNode);

            boolean show = count > 0;
            Label countLabel = new Label(show ? String.valueOf(count) : "");
            countLabel.getStyleClass().add("reaction-count");
            countLabel.setManaged(show);
            countLabel.setVisible(show);

            button.getProperties().put("countLabel", countLabel);

            button.setOnAction(e -> {
                reactionService.toggle(targetType, targetId, emoji);
                refreshReactionBar(bar, targetType, targetId);
            });

            chip.getChildren().addAll(button, countLabel);
            bar.getChildren().add(chip);
        }

        return bar;
    }

    private void refreshReactionBar(HBox bar, ReactionTargetType targetType, String targetId) {
        if (reactionService == null) {
            return;
        }

        ReactionService.ReactionSummary summary = reactionService.getSummaryForCurrentUser(targetType, targetId);
        Map<String, Integer> counts = summary.countsByEmoji();
        for (javafx.scene.Node node : bar.getChildren()) {
            if (node instanceof HBox chip) {
                for (javafx.scene.Node child : chip.getChildren()) {
                    if (child instanceof Button btn) {
                        Object data = btn.getUserData();
                        if (data instanceof String emoji) {
                            int count = counts.getOrDefault(emoji, 0);

                            btn.getStyleClass().remove("reacted");
                            if (summary.reactedEmojis().contains(emoji)) {
                                btn.getStyleClass().add("reacted");
                            }

                            Object labelObj = btn.getProperties().get("countLabel");
                            if (labelObj instanceof Label countLabel) {
                                boolean show = count > 0;
                                countLabel.setText(show ? String.valueOf(count) : "");
                                countLabel.setManaged(show);
                                countLabel.setVisible(show);
                            }
                        }
                    }
                }
            }
        }
    }

    private Node createEmojiNode(String emoji) {
        String resourcePath = switch (emoji) {
            case "👍" -> "/icons/reactions/like.png";
            case "❤️" -> "/icons/reactions/love.png";
            case "😂" -> "/icons/reactions/laugh.png";
            case "😮" -> "/icons/reactions/wow.png";
            case "😢" -> "/icons/reactions/sad.png";
            default -> null;
        };

        if (resourcePath != null) {
            try (var stream = getClass().getResourceAsStream(resourcePath)) {
                if (stream != null) {
                    Image image = new Image(stream);
                    ImageView view = new ImageView(image);
                    view.setFitWidth(16);
                    view.setFitHeight(16);
                    view.setPreserveRatio(true);
                    view.getStyleClass().add("reaction-icon");
                    return view;
                }
            } catch (Exception ignored) {
                // fallback to text emoji
            }
        }

        Label emojiLabel = new Label(emoji);
        emojiLabel.getStyleClass().add("reaction-emoji");
        return emojiLabel;
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
