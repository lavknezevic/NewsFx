package at.newsfx.fhtechnikum.newsfx.util.error;

import javafx.scene.control.Alert;

public final class ErrorHandler {

    private ErrorHandler() {
    }

    public static void showUserError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showTechnicalError(String message, Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Unexpected Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText(message);
        t.printStackTrace();
        alert.showAndWait();
    }
}