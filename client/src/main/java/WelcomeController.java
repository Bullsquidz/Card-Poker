import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
public class WelcomeController implements Initializable {

    @FXML
    private BorderPane borderPane;

    @FXML
    private TextField serverIP;
    
    @FXML
    private TextField serverPort;
    
    @FXML
    private Button joinServer;
	
    public Client client;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        joinServer.setOnAction(e -> {
            try {
                Integer.parseInt(serverPort.getText());
            } catch (Exception zz) {
                System.out.println("Port must contain only numbers");
                return;
            }

            if (serverPort.getText().isEmpty() || serverIP.getText().isEmpty()) {
                System.out.println("Please enter server IP and port");
                return;
            }

            // Create client but DON'T start it yet
            client = new Client(
                    serverIP.getText(),
                    Integer.parseInt(serverPort.getText()),
                    data -> {
                        Platform.runLater(() -> {
                            System.out.println("Server: " + data);
                        });
                    }
            );

            // First load the game scene and set the client
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/GameFXML.fxml"));
                Parent root = loader.load();
                GameController gameController = loader.getController();

                // SET THE CLIENT FIRST
                gameController.setClient(client);

                // NOW start the client thread
                client.start();

                Stage stage = (Stage)joinServer.getScene().getWindow();
                Scene scene = new Scene(root, 900, 500);
                scene.getStylesheets().clear();
                stage.setScene(scene);

            } catch(Exception e1) {
                e1.printStackTrace();
                System.out.println("Failed to load game scene!");
            }
        });
    }
}
