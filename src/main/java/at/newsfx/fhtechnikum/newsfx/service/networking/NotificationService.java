package at.newsfx.fhtechnikum.newsfx.service.networking;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.IOException;
import java.util.function.Consumer;

public class NotificationService {

    private NotificationServer server;
    private NotificationClient client;

    private final BooleanProperty clientConnected = new SimpleBooleanProperty(false);
    private final IntegerProperty connectedClientsCount = new SimpleIntegerProperty(0);

    private Consumer<String> onNotificationReceived;

    public void setOnNotificationReceived(Consumer<String> callback) {
        this.onNotificationReceived = callback;
    }

    public BooleanProperty clientConnectedProperty() {
        return clientConnected;
    }

    public IntegerProperty connectedClientsCountProperty() {
        return connectedClientsCount;
    }

    public void startServer(int port) throws IOException {
        if (server != null) {
            stopServer();
        }
        server = new NotificationServer(port);
        server.setOnMessageReceived(this::handleIncomingMessage);
        server.setOnClientCountChanged(count ->
                Platform.runLater(() -> connectedClientsCount.set(count))
        );
        server.start();
    }

    public void stopServer() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
        Platform.runLater(() -> connectedClientsCount.set(0));
    }

    public void connect(String host, int port) throws IOException {
        if (client != null) {
            disconnect();
        }
        client = new NotificationClient(host, port);
        client.setOnMessageReceived(this::handleIncomingMessage);
        client.setOnDisconnected(() ->
                Platform.runLater(() -> clientConnected.set(false))
        );
        client.connect();
        Platform.runLater(() -> clientConnected.set(true));
    }

    public void disconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        Platform.runLater(() -> clientConnected.set(false));
    }

    public void sendNotification(String message) {
        if (client != null && client.isConnected()) {
            client.send(message);
        }
        if (server != null) {
            server.broadcast(message);
        }
    }

    public void shutdown() {
        disconnect();
        stopServer();
    }

    private void handleIncomingMessage(String message) {
        if (onNotificationReceived != null) {
            Platform.runLater(() -> onNotificationReceived.accept(message));
        }
    }
}
