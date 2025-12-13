package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController extends BaseController {

    @FXML
    private Label titleLabel;

    private final MainViewModel viewModel = new MainViewModel();

    @Override
    public void onViewLoaded() {
        bindViewModel();
        viewModel.loadInitialData();
    }

    private void bindViewModel() {
        titleLabel.textProperty().bind(viewModel.titleProperty());
    }
}