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

public class RegisterController extends BaseController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @Override
    public void onViewLoaded() {
        statusLabel.setText("");
    }

    @FXML
    private void onBack() {
        ViewManager.setRoot(usernameField.getScene(), View.LOGIN);
    }

    @FXML
    private void onRegister() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirm = confirmPasswordField.getText();

            if (password == null || password.isBlank()) {
                throw new UserException("Password is required.");
            }
            if (!password.equals(confirm)) {
                throw new UserException("Passwords do not match.");
            }

            AppContext.get().authService().register(username, password);

            statusLabel.setText("Account created. You can now log in.");

            // Go back to login and prefill username
            ViewManager.setRoot(usernameField.getScene(), View.LOGIN);
        } catch (UserException e) {
            statusLabel.setText(e.getMessage());
        } catch (Exception e) {
            ErrorHandler.showTechnicalError("Registration failed", e);
        }
    }
}
