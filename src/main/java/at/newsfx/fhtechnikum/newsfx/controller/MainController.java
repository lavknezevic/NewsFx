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

public class MainController extends BaseController {

    @FXML
    private Label titleLabel;

    @FXML
    private ListView<NewsItem> externalNewsList;

    @FXML
    private VBox internalView;

    @FXML
    private VBox externalView;

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