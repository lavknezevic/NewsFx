package at.newsfx.fhtechnikum.newsfx.controller.components;

import at.newsfx.fhtechnikum.newsfx.model.Comment;
import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.service.reaction.ReactionTargetType;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;

public class CommentSectionFactory {

    private static final DateTimeFormatter COMMENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ReactionBarFactory reactionBarFactory;
    private final BiConsumer<NewsItem, String> onAddComment;

    public CommentSectionFactory(ReactionBarFactory reactionBarFactory, BiConsumer<NewsItem, String> onAddComment) {
        this.reactionBarFactory = reactionBarFactory;
        this.onAddComment = onAddComment;
    }

    public VBox createCommentsSection(NewsItem item) {
        VBox section = new VBox(6);
        section.getStyleClass().add("comments-section");

        Label title = new Label("Comments");
        title.getStyleClass().add("comment-title");

        ListView<Comment> commentsList = new ListView<>();
        commentsList.setItems(item.getComments());
        commentsList.setPrefHeight(110);
        commentsList.setMaxHeight(160);

        commentsList.setCellFactory(list -> createCommentCell());

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");

        Button addButton = new Button("Add");

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

    private ListCell<Comment> createCommentCell() {
        return new ListCell<>() {
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

                if (reactionBarFactory != null) {
                    box.getChildren().add(
                        reactionBarFactory.createReactionsBar(ReactionTargetType.COMMENT, comment.getId())
                    );
                }

                setGraphic(box);
                setText(null);
            }
        };
    }
}
