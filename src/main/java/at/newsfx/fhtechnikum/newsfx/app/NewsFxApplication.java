package at.newsfx.fhtechnikum.newsfx.app;

import at.newsfx.fhtechnikum.newsfx.config.AppConfig;
import at.newsfx.fhtechnikum.newsfx.view.View;
import at.newsfx.fhtechnikum.newsfx.view.ViewManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;


public class NewsFxApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = ViewManager.load(View.MAIN);

        Scene scene = new Scene(root, 1000, 700);

        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm()
        );

        stage.setTitle(AppConfig.windowTitle());
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
