import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.net.URL;
import java.util.ResourceBundle;

import java.io.Serializable;

public class ServerMainController implements Initializable {
    @FXML private ListView<String> gameLog;
    @FXML private Label clientCountLabel;
    @FXML private Button stopServerButton;
    @FXML private Button refreshButton;

    private PokerServer pokerServer;
    private JavaFXTemplate mainApplication;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize with empty state
        gameLog.getItems().add("Server started - waiting for connections...");
    }

    /**
     * Sets the poker server and starts receiving updates
     */
    public void setPokerServer(PokerServer server) {
        this.pokerServer = server;

        // Set up the callback to receive server messages
        pokerServer.setCallback(this::handleServerMessage);

        updateClientCount();
    }

    /**
     * Handles messages from the server
     */
    private void handleServerMessage(Serializable data) {
        if (data instanceof String) {
            String message = (String) data;
            addGameLogMessage(message);
        }
    }

    @FXML
    private void handleStopServer() {
        if (pokerServer != null) {
            pokerServer.stopServer();
        }

        if (mainApplication != null) {
            mainApplication.showStartScene();
        }
    }

    @FXML
    private void handleRefresh() {
        updateClientCount();
        if (pokerServer != null) {
            addGameLogMessage("Manual refresh - checking server status");
        }
    }

    /**
     * Updates the client count display
     */
    private void updateClientCount() {
        if (pokerServer != null) {
            int count = pokerServer.getConnectedClientCount();
            Platform.runLater(() -> {
                clientCountLabel.setText("Connected Clients: " + count);
            });
        }
    }

    /**
     * Adds a message to the game log
     */
    public void addGameLogMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            gameLog.getItems().add("[" + timestamp + "] " + message);

            // Auto-scroll to bottom and limit log size
            gameLog.scrollTo(gameLog.getItems().size() - 1);
            if (gameLog.getItems().size() > 1000) {
                gameLog.getItems().remove(0, 100); // Remove oldest 100 entries
            }
        });
    }

    /**
     * Updates client list information
     */
    public void updateClientInfo() {
        if (pokerServer != null) {
            updateClientCount();

            // Add client details to log
            java.util.ArrayList<String> clientInfo = pokerServer.getClientInfo();
            if (!clientInfo.isEmpty()) {
                addGameLogMessage("=== Connected Clients ===");
                for (String info : clientInfo) {
                    addGameLogMessage("  " + info);
                }
            }
        }
    }

    // Setter for main application reference
    public void setMainApplication(JavaFXTemplate mainApplication) {
        this.mainApplication = mainApplication;
    }

    public void shutdown() {
        if (pokerServer != null) {
            pokerServer.stopServer();
        }
    }
}