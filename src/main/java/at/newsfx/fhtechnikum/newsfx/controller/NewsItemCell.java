package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

public class NewsItemCell extends ListCell<NewsItem> {

    @Override
    protected void updateItem(NewsItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        Label title = new Label(item.getTitle());
        title.getStyleClass().add("headline");

        Label summary = new Label(item.getSummary());
        summary.setWrapText(true);

        VBox box = new VBox(title, summary);
        box.setSpacing(5);

        setGraphic(box);
    }
}