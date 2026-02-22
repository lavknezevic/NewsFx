package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.config.AppContext;
import at.newsfx.fhtechnikum.newsfx.view.View;
import at.newsfx.fhtechnikum.newsfx.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StartController extends BaseController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button userManagementButton;

    @Override
    public void onViewLoaded() {
        String username = AppContext.get().authService().currentUserProperty().get().getUsername();
        welcomeLabel.setText("Welcome, " + username + "!");

        boolean isAdmin = AppContext.get().authService().isAdmin();
        if (userManagementButton != null) {
            userManagementButton.setVisible(isAdmin);
            userManagementButton.setManaged(isAdmin);
        }
    }

    @FXML
    private void onExternalNews() {
        navigateToMain("external");
    }

    @FXML
    private void onInternalNews() {
        navigateToMain("internal");
    }

    @FXML
    private void onFavorites() {
        navigateToMain("favorites");
    }

    @FXML
    private void onUserManagement() {
        ViewManager.setRoot(welcomeLabel.getScene(), View.USER_MANAGEMENT);
    }

    @FXML
    private void onLogout() {
        AppContext.get().authService().logout();
        ViewManager.setRoot(welcomeLabel.getScene(), View.LOGIN);
    }

    private void navigateToMain(String section) {
        MainController.setInitialSection(section);
        ViewManager.setRoot(welcomeLabel.getScene(), View.MAIN);
    }
}
