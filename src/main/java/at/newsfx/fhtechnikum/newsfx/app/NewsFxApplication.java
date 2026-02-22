package at.newsfx.fhtechnikum.newsfx.app;

import at.newsfx.fhtechnikum.newsfx.config.AppConfig;
import at.newsfx.fhtechnikum.newsfx.config.AppContext;
import at.newsfx.fhtechnikum.newsfx.util.resource.ResourceUtil;
import at.newsfx.fhtechnikum.newsfx.view.View;
import at.newsfx.fhtechnikum.newsfx.view.ViewManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class NewsFxApplication extends Application {

    private static boolean serverMode;

    @Override
    public void start(Stage stage) throws Exception {
        serverMode = getParameters().getRaw().contains("--server");

        AppContext.get();

        Parent root = serverMode
                ? ViewManager.load(View.SERVER)
                : ViewManager.load(View.LOGIN);

        Scene scene = new Scene(root, AppConfig.windowWidth(), AppConfig.windowHeight());

        scene.getStylesheets().add(
                ResourceUtil.get("/css/application.css").toExternalForm()
        );

        stage.getIcons().add(
            new Image(ResourceUtil.get("/icons/newsfx.png").toExternalForm())
        );

        String title = serverMode
                ? AppConfig.windowTitle() + " [SERVER]"
                : AppConfig.windowTitle();
        stage.setTitle(title);
        stage.setMinWidth(AppConfig.windowMinWidth());
        stage.setMinHeight(AppConfig.windowMinHeight());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        AppContext.get().notificationService().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
