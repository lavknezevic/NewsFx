package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.model.NewsItem;
import at.newsfx.fhtechnikum.newsfx.service.NewsService;
import at.newsfx.fhtechnikum.newsfx.service.RssNewsService;
import at.newsfx.fhtechnikum.newsfx.util.error.ErrorHandler;
import at.newsfx.fhtechnikum.newsfx.viewmodel.MainViewModel;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.layout.BorderPane;

import java.util.List;

public class MainController extends BaseController {

    @FXML
    private Label titleLabel;

    @FXML
    private ListView<NewsItem> externalNewsList;

    @FXML
    private VBox internalView;

    @FXML
    private VBox externalView;

    @FXML
    private TextField externalSearchField;

    @FXML
    private ComboBox<String> externalCategoryBox;

    @FXML
    private VBox externalListPage;

    @FXML
    private BorderPane externalArticlePage;

    @FXML
    private WebView articleWebView;

    @FXML
    private Label articleTitleLabel;




    private MainViewModel viewModel;

    private FilteredList<NewsItem> filteredExternalNews;


    @Override
    public void onViewLoaded() {
        NewsService newsService = new RssNewsService();
        viewModel = new MainViewModel(newsService);

        bindViewModel();
        loadExternalNewsAsync();
    }

    private void bindViewModel() {
        titleLabel.setText("NewsFx – External News");


        filteredExternalNews = new FilteredList<>(viewModel.externalNewsProperty(), item -> true);
        externalNewsList.setItems(filteredExternalNews);

        externalNewsList.setCellFactory(list -> new NewsItemCell());

        externalNewsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openSelectedExternalArticle();
            }
        });

        externalNewsList.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> openSelectedExternalArticle();
            }
        });

        externalCategoryBox.getItems().setAll(List.of("Alle", "HR", "IT", "Sicherheit"));
        externalCategoryBox.getSelectionModel().select("Alle");


        externalSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyExternalFilter());
        externalCategoryBox.valueProperty().addListener((obs, oldVal, newVal) -> applyExternalFilter());

        applyExternalFilter();
    }

    private void applyExternalFilter() {
        String search = externalSearchField.getText();
        if (search == null) search = "";
        search = search.trim().toLowerCase();

        String selectedCategory = externalCategoryBox.getValue();

        String finalSearch = search;
        filteredExternalNews.setPredicate(item -> {
            if (item == null) return false;


            String title = safeLower(item.getTitle());
            boolean matchesSearch = finalSearch.isBlank() || title.contains(finalSearch);


            boolean matchesCategory =
                    selectedCategory == null ||
                            selectedCategory.equals("Alle") ||
                            safeLower(getCategory(item)).equals(selectedCategory.toLowerCase());

            return matchesSearch && matchesCategory;
        });
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String getCategory(NewsItem item) {
        return "Alle";
    }

    private void openSelectedExternalArticle() {
        NewsItem selected = externalNewsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String url = selected.getArticleUrl();
        if (url == null || url.isBlank()) return;

        articleTitleLabel.setText(selected.getTitle());

        WebEngine engine = articleWebView.getEngine();
        engine.load(url);


        externalListPage.setVisible(false);
        externalListPage.setManaged(false);

        externalArticlePage.setVisible(true);
        externalArticlePage.setManaged(true);
    }

    @FXML
    private void backToExternalList() {

        articleWebView.getEngine().load(null);

        externalArticlePage.setVisible(false);
        externalArticlePage.setManaged(false);

        externalListPage.setVisible(true);
        externalListPage.setManaged(true);
    }

    @FXML
    private void showInternal() {
        titleLabel.setText("NewsFx – Internal News");

        internalView.setVisible(true);
        internalView.setManaged(true);

        externalView.setVisible(false);
        externalView.setManaged(false);
    }
    @FXML
    private void showExternal() {
        titleLabel.setText("NewsFx – External News");

        internalView.setVisible(false);
        internalView.setManaged(false);

        externalView.setVisible(true);
        externalView.setManaged(true);
    }


    private void loadExternalNewsAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                viewModel.loadExternalNews();
                return null;
            }
        };

        task.setOnFailed(e ->
                ErrorHandler.showTechnicalError(
                        "Failed to load external news",
                        task.getException()
                )
        );

        new Thread(task).start();
    }
}