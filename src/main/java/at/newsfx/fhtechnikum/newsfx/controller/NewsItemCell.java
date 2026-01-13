package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.function.Consumer;

public class NewsItemCell extends ListCell<NewsItem> {

    private final boolean enableInternalActions;
    private final boolean enableFavorites;
    private final Consumer<NewsItem> onEdit;
    private final Consumer<NewsItem> onDelete;
    private final Consumer<NewsItem> onFavoriteToggle;

    public NewsItemCell() {
        this(false, false, null, null, null);
    }

    public NewsItemCell(boolean enableInternalActions, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete) {
        this(enableInternalActions, false, onEdit, onDelete, null);
    }

    public NewsItemCell(boolean enableInternalActions, boolean enableFavorites, Consumer<NewsItem> onEdit, Consumer<NewsItem> onDelete, Consumer<NewsItem> onFavoriteToggle) {
        this.enableInternalActions = enableInternalActions;
        this.enableFavorites = enableFavorites;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onFavoriteToggle = onFavoriteToggle;
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

        VBox box = new VBox(8);
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

        HBox actions = new HBox(10);

        if (enableFavorites && !item.isExternal()) {
            Button favoriteButton = new Button("★ Favorite");
            favoriteButton.getStyleClass().add("favorite-button");
            favoriteButton.setOnAction(e -> {
                if (onFavoriteToggle != null) {
                    onFavoriteToggle.accept(item);
                }
            });
            actions.getChildren().add(favoriteButton);
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
}
