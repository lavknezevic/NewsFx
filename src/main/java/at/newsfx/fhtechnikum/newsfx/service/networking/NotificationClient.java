package at.newsfx.fhtechnikum.newsfx.service.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class NotificationClient {

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private Thread readerThread;
    private volatile boolean connected;
    private Consumer<String> onMessageReceived;
    private Runnable onDisconnected;

    public NotificationClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void setOnDisconnected(Runnable callback) {
        this.onDisconnected = callback;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        connected = true;

        readerThread = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(line);
                    }
                }
            } catch (IOException e) {
                // Server disconnected or socket closed
            } finally {
                connected = false;
                if (onDisconnected != null) {
                    onDisconnected.run();
                }
            }
        }, "NotificationClient-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void send(String message) {
        if (out != null && connected) {
            out.println(message);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
}
