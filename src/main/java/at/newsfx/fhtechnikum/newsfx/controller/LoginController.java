package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.config.AppContext;
import at.newsfx.fhtechnikum.newsfx.util.error.ErrorHandler;
import at.newsfx.fhtechnikum.newsfx.util.error.UserException;
import at.newsfx.fhtechnikum.newsfx.view.View;
import at.newsfx.fhtechnikum.newsfx.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController extends BaseController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @Override
    public void onViewLoaded() {
        statusLabel.setText("");
    }

    @FXML
    private void onLogin() {
        try {
            AppContext.get().authService().login(
                    usernameField.getText(),
                    passwordField.getText()
            );

            ViewManager.setRoot(usernameField.getScene(), View.MAIN);
        } catch (UserException e) {
            statusLabel.setText(e.getMessage());
        } catch (Exception e) {
            ErrorHandler.showTechnicalError("Login failed", e);
        }
    }

    @FXML
    private void onOpenRegister() {
        ViewManager.setRoot(usernameField.getScene(), View.REGISTER);
    }
}
