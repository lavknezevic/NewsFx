package at.newsfx.fhtechnikum.newsfx.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class NewsFxApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("/view/MainView.fxml"))
        );

        Scene scene = new Scene(root, 1000, 700);

        stage.setTitle("NewsFx");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
