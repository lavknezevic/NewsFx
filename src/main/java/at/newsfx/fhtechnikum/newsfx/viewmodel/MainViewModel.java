package at.newsfx.fhtechnikum.newsfx.viewmodel;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.service.DummyNewsService;
import at.newsfx.fhtechnikum.newsfx.service.NewsService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainViewModel {

    private final StringProperty title = new SimpleStringProperty();
    private final NewsService newsService;
    private final ObservableList<NewsItem> externalNews =
            FXCollections.observableArrayList();


    public MainViewModel(NewsService newsService) {
        this.newsService = newsService;
        title.set("Welcome to NewsFx");
    }

    public ObservableList<NewsItem> externalNewsProperty() {
        return externalNews;
    }

    public void loadExternalNews() {
        externalNews.setAll(newsService.loadLatest());
    }

    public StringProperty titleProperty() {
        return title;
    }
}
