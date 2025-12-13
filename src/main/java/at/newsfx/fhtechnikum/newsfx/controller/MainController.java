package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.service.DummyNewsService;
import at.newsfx.fhtechnikum.newsfx.service.NewsService;
import at.newsfx.fhtechnikum.newsfx.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController extends BaseController {

    @FXML
    private Label titleLabel;

    private MainViewModel viewModel;

    @Override
    public void onViewLoaded() {
        NewsService newsService = new DummyNewsService();
        viewModel = new MainViewModel(newsService);

        bindViewModel();
        viewModel.loadInitialData();
    }

    private void bindViewModel() {
        titleLabel.textProperty().bind(viewModel.titleProperty());
    }
}