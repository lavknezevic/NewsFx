package at.newsfx.fhtechnikum.newsfx.viewmodel;

import at.newsfx.fhtechnikum.newsfx.service.DummyNewsService;
import at.newsfx.fhtechnikum.newsfx.service.NewsService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MainViewModel {

    private final StringProperty title = new SimpleStringProperty();
    private final NewsService newsService;

    public MainViewModel(NewsService newsService) {
        this.newsService = newsService;
        title.set("Welcome to NewsFx");
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void loadInitialData() {
        // placeholder for future logic
    }
}
