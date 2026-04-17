import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerStartController implements Initializable {
    @FXML private TextField portField;
    @FXML private Button startButton;
    @FXML private Button exitButton;

    private PokerServer pokerServer;
    private JavaFXTemplate mainApplication;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set default port
        portField.setText("5555");

        // Validate port input
        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void handleStartServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());

            if (port < 1024 || port > 65535) {
                showError("Invalid Port", "Port must be between 1024 and 65535");
                return;
            }

            // Create and start server
            pokerServer = new PokerServer(port, data -> {

            });

            pokerServer.startServer();

            // Switch to main server scene
            if (mainApplication != null) {
                mainApplication.showServerMainScene(pokerServer);
            }

        } catch (NumberFormatException e) {
            showError("Invalid Port", "Please enter a valid port number");
        } catch (Exception e) {
            showError("Server Error", "Failed to start server: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        if (pokerServer != null) {
            pokerServer.stopServer();
        }
        System.exit(0);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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