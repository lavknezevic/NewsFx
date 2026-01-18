package at.newsfx.fhtechnikum.newsfx.app;

import at.newsfx.fhtechnikum.newsfx.config.AppConfig;
import at.newsfx.fhtechnikum.newsfx.util.resource.ResourceUtil;
import at.newsfx.fhtechnikum.newsfx.view.View;
import at.newsfx.fhtechnikum.newsfx.view.ViewManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class NewsFxApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        at.newsfx.fhtechnikum.newsfx.config.AppContext.get();

        Parent root = ViewManager.load(View.LOGIN);

        Scene scene = new Scene(root, 1000, 700);

        scene.getStylesheets().add(
                ResourceUtil.get("/css/application.css").toExternalForm()
        );

        stage.getIcons().add(
            new Image(ResourceUtil.get("/icons/newsfx.png").toExternalForm())
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
