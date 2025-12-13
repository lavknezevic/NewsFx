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

public class MainController extends BaseController {

    @FXML
    private Label titleLabel;

    @FXML
    private ListView<NewsItem> externalNewsList;

    private MainViewModel viewModel;

    @Override
    public void onViewLoaded() {
        NewsService newsService = new RssNewsService();
        viewModel = new MainViewModel(newsService);

        bindViewModel();
        loadExternalNewsAsync();
    }

    private void bindViewModel() {
        titleLabel.setText("NewsFx – External News");
        externalNewsList.setItems(viewModel.externalNewsProperty());

        externalNewsList.setCellFactory(list -> new NewsItemCell());
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