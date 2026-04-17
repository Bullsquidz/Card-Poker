import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class PokerServer {
    private int port;
    private ServerSocket serverSocket;
    private CopyOnWriteArrayList<ClientHandler> clients;
    private Consumer<Serializable> callback;
    private boolean serverRunning;
    private ServerThread serverThread;
    private int clientCounter;

    public PokerServer(int port, Consumer<Serializable> callback) {
        this.port = port;
        this.callback = callback;
        this.clients = new CopyOnWriteArrayList<>();
        this.serverRunning = false;
        this.clientCounter = 1;
    }

    /**
     * Updates the callback for server messages
     */
    public void setCallback(Consumer<Serializable> callback) {
        this.callback = callback;
    }

    /**
     * Starts the server on the specified port
     */
    public void startServer() {
        if (serverRunning) {
            broadcastServerMessage("Server is already running");
            return;
        }

        System.out.println("=== SERVER STARTING ===");
        System.out.println("Attempting to start server on port: " + port);

        serverThread = new ServerThread();
        serverThread.start();
        serverRunning = true;

        System.out.println("=== SERVER STARTING ===");
        broadcastServerMessage("Server started on port " + port);
    }

    /**
     * Stops the server and disconnects all clients
     */
    public void stopServer() {
        if (!serverRunning) {
            return;
        }

        serverRunning = false;
        broadcastServerMessage("Server shutting down...");

        // Disconnect all clients
        for (ClientHandler client : clients) {
            try {
                client.sendToClient(new PokerInfo(PokerInfo.MessageType.DISCONNECT, 0));
            } catch (Exception e) {
                // Ignore errors during shutdown
            }
        }
        clients.clear();

        // Close server socket
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // Ignore during shutdown
            }
        }

        broadcastServerMessage("Server stopped");
    }

    /**
     * Broadcasts a message to all connected clients
     */
    public void broadcastToClients(PokerInfo pokerInfo) {
        for (ClientHandler client : clients) {
            if (client.isClientConnected()) {
                client.sendToClient(pokerInfo);
            }
        }
    }

    /**
     * Broadcasts a server status message (for server GUI)
     */
    public void broadcastServerMessage(String message) {
        if (callback != null) {
            callback.accept("[SERVER] " + message);
        }
    }

    /**
     * Called when a client disconnects
     */
    public void clientDisconnected(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastServerMessage("Client disconnected: " + clientHandler.getClientInfo());
        broadcastServerMessage("Active clients: " + clients.size());
    }

    /**
     * Gets the number of currently connected clients
     */
    public int getConnectedClientCount() {
        return clients.size();
    }

    /**
     * Gets information about all connected clients
     */
    public ArrayList<String> getClientInfo() {
        ArrayList<String> clientInfo = new ArrayList<>();
        for (ClientHandler client : clients) {
            clientInfo.add(client.toString());
        }
        return clientInfo;
    }

    /**
     * Checks if server is running
     */
    public boolean isServerRunning() {
        return serverRunning;
    }

    /**
     * Inner class that handles incoming client connections
     */
    private class ServerThread extends Thread {
        @Override
        public void run() {
            try (ServerSocket localServerSocket = new ServerSocket(port)) {
                serverSocket = localServerSocket;
                System.out.println("=== SERVER SOCKET CREATED ON PORT " + port + " ===");
                broadcastServerMessage("Server is waiting for clients on port " + port);

                while (serverRunning) {
                    try {
                        System.out.println("=== WAITING FOR CLIENT CONNECTION ===");

                        Socket clientSocket = serverSocket.accept();

                        if (!serverRunning) {
                            break;
                        }

                        // Create new client handler
                        ClientHandler clientHandler = new ClientHandler(clientSocket, clientCounter++, PokerServer.this);
                        clients.add(clientHandler);
                        clientHandler.start();

                        broadcastServerMessage("New client connected: " + clientHandler.getClientInfo());
                        broadcastServerMessage("Total clients connected: " + clients.size());

                    } catch (IOException e) {
                        if (serverRunning) {
                            broadcastServerMessage("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }

            } catch (IOException e) {
                if (serverRunning) {
                    broadcastServerMessage("Failed to start server on port " + port + ": " + e.getMessage());
                }
            } finally {
                serverRunning = false;
                broadcastServerMessage("Server thread stopped");
            }
        }
    }
}