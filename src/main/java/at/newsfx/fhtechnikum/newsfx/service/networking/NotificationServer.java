package at.newsfx.fhtechnikum.newsfx.service.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class NotificationServer {

    private final int port;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private volatile boolean running;
    private Consumer<String> onMessageReceived;
    private Consumer<Integer> onClientCountChanged;

    public NotificationServer(int port) {
        this.port = port;
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void setOnClientCountChanged(Consumer<Integer> callback) {
        this.onClientCountChanged = callback;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        Thread acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    clients.add(handler);
                    notifyClientCountChanged();
                    handler.start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client: " + e.getMessage());
                    }
                }
            }
        }, "NotificationServer-Accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public void shutdown() {
        running = false;
        for (ClientHandler client : clients) {
            client.close();
        }
        clients.clear();
        notifyClientCountChanged();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    private void removeClient(ClientHandler client) {
        clients.remove(client);
        notifyClientCountChanged();
    }

    private void notifyClientCountChanged() {
        if (onClientCountChanged != null) {
            onClientCountChanged.accept(clients.size());
        }
    }

    private class ClientHandler {
        private final Socket socket;
        private PrintWriter out;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        void start() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                close();
                return;
            }

            Thread reader = new Thread(() -> {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (onMessageReceived != null) {
                            onMessageReceived.accept(line);
                        }
                        for (ClientHandler other : clients) {
                            if (other != this) {
                                other.send(line);
                            }
                        }
                    }
                } catch (IOException ignored) {
                } finally {
                    close();
                    removeClient(ClientHandler.this);
                }
            }, "NotificationServer-Client-" + socket.getRemoteSocketAddress());
            reader.setDaemon(true);
            reader.start();
        }

        void send(String message) {
            if (out != null && !socket.isClosed()) {
                out.println(message);
            }
        }

        void close() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
