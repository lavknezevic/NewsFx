package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label titleLabel;

    private final MainViewModel viewModel = new MainViewModel();

    @FXML
    private void initialize() {
        titleLabel.textProperty().bind(viewModel.titleProperty());
        viewModel.loadInitialData();
    }
}
