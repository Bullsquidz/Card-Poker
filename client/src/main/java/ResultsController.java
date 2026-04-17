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
public class ResultsController implements Initializable {

	@FXML
	private BorderPane borderPane;
	@FXML
	private TextField reward;

	@FXML
	private Button playAgain;

	@FXML
	private Button exit;

	private Client client;
	void setClient(Client client){
		this.client = client;
		
		Scene scene = borderPane.getScene();
		if (client.won){
			scene.getStylesheets().add("/styles/resultsWonStyle.css");
		}else{
			scene.getStylesheets().add("/styles/resultsLostStyle.css");
		}
		
		reward.setText(Integer.toString(client.reward));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources){
		playAgain.setOnAction(e -> {
         try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/GameFXML.fxml"));
                Parent root = loader.load();
    
                Stage stage = (Stage)borderPane.getScene().getWindow();
    
                Scene scene = new Scene(root,900, 500);
                scene.getStylesheets().clear();

								if (client.enableNewLook){
									scene.getStylesheets().add("/styles/gameStyle.css");
								}

                GameController gameController = loader.getController();
                gameController.setClient(client);
    
                stage.setScene(scene);//new Scene(root, 900, 500));
    
            } catch(Exception e1) {
                e1.printStackTrace();
                System.out.println("[DANI] Failure to get to next scene!");
                System.exit(1);
         }

		});

        exit.setOnAction(e->{
            try {
                if (client != null && client.socket != null && !client.socket.isClosed()) {
                    client.socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });
	}

}
