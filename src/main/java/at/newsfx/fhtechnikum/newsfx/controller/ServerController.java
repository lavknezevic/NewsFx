package at.newsfx.fhtechnikum.newsfx.controller;

import at.newsfx.fhtechnikum.newsfx.config.AppConfig;
import at.newsfx.fhtechnikum.newsfx.config.AppContext;
import at.newsfx.fhtechnikum.newsfx.service.networking.NotificationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServerController extends BaseController {

    @FXML
    private Label statusLabel;
    @FXML
    private Label portLabel;
    @FXML
    private Label clientsLabel;
    @FXML
    private ListView<String> logList;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void onViewLoaded() {
        NotificationService notificationService = AppContext.get().notificationService();
        int port = AppConfig.notificationPort();

        notificationService.connectedClientsCountProperty().addListener((obs, oldVal, count) ->
                clientsLabel.setText("Connected clients: " + count.intValue())
        );

        notificationService.setOnNotificationReceived(message ->
                Platform.runLater(() -> log(message))
        );

        try {
            notificationService.startServer(port);
            statusLabel.setText("Running");
            portLabel.setText("Port: " + port);
            log("Server started on port " + port);
        } catch (Exception e) {
            statusLabel.setText("Failed to start");
            log("ERROR: " + e.getMessage());
        }
    }

    private void log(String message) {
        String timestamp = LocalTime.now().format(TIME_FMT);
        logList.getItems().addFirst("[" + timestamp + "] " + message);
    }
}
