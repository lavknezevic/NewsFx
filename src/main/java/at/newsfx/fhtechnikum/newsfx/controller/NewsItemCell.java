package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.model.Comment;
import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NewsItemCell extends ListCell<NewsItem> {

    private final boolean enableInternalActions;
    private final Consumer<NewsItem> onEdit;
    private final Consumer<NewsItem> onDelete;
    private final BiConsumer<NewsItem, String> onAddComment;
    private static final DateTimeFormatter COMMENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public NewsItemCell() {
        this(false, null, null, null);
    }

    public NewsItemCell(boolean enableInternalActions, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete,  BiConsumer<NewsItem, String> onAddComment) {
        this.enableInternalActions = enableInternalActions;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onAddComment = onAddComment;
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
        title.getStyleClass().add("headline");

        ImageView imageView = null;
        if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            Image image = new Image(item.getImageUrl(), true);
            imageView = new ImageView(image);
            imageView.setFitWidth(250);
            imageView.setPreserveRatio(true);
        }

        Label summary = new Label(item.getSummary());
        summary.setWrapText(true);
        summary.setMaxWidth(Double.MAX_VALUE);
        summary.setMinHeight(Region.USE_PREF_SIZE);

        summary.maxWidthProperty().bind(getListView().widthProperty().subtract(80));

        VBox box = new VBox(8);
        box.setFillWidth(true);
        box.getStyleClass().add("card");
        box.getChildren().add(title);

        if (imageView != null) {
            box.getChildren().add(imageView);
        }

        box.getChildren().add(summary);

        if (item.getLinkUrl() != null && !item.getLinkUrl().isBlank()) {
            Hyperlink link = new Hyperlink(item.getLinkUrl());
            link.setOnAction(e -> openLink(item.getLinkUrl()));
            box.getChildren().add(link);
        }


        if (item.getPdfPath() != null && !item.getPdfPath().isBlank()) {
            Button pdfButton = new Button("Open PDF");
            pdfButton.setOnAction(e -> openPdf(item.getPdfPath()));
            box.getChildren().add(pdfButton);
        }

        if (enableInternalActions && !item.isExternal()) {
            HBox actions = new HBox(10);

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

            if (!item.isExternal()) {
                box.getChildren().add(createCommentsSection(item));
            }

            actions.getChildren().addAll(editButton, deleteButton);
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

                setGraphic(box);
                setText(null);
            }
        });

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");

        Button addButton = new Button("Add");

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

}
