package at.newsfx.fhtechnikum.newsfx.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class ViewManager {

    private ViewManager() {
        // utility class
    }

    public static Parent load(View view) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ViewManager.class.getResource(view.getFxmlPath())
            );
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load view: " + view.name(), e
            );
        }
    }

    public static void setRoot(Scene scene, View view) {
        scene.setRoot(load(view));
    }
}