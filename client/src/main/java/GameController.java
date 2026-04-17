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
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextArea;
import javafx.application.Platform;

import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.ToolBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import javafx.scene.image.Image;

import javafx.scene.layout.Background;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

public class GameController implements Initializable {

    @FXML
    private BorderPane borderPane;

    @FXML
    private MenuItem freshStart;

    @FXML
    private MenuItem newLook;

    @FXML
    private MenuItem exit;

    @FXML
    private TextField anteWager;

    @FXML
    private TextField pairPlusWager;

    @FXML
    private TextField playWager;

    @FXML
    public TextField totalWinning;

    @FXML
    private ImageView dealerCard1;

    @FXML
    private ImageView dealerCard2;

    @FXML
    private ImageView dealerCard3;

    @FXML
    private Button deal;

    @FXML
    private Button play;

    @FXML
    private Button fold;

    @FXML
    private ImageView clientCard1;

    @FXML
    private ImageView clientCard2;

    @FXML
    private ImageView clientCard3;

    @FXML
    public ListView<String> hist;

    private Client client;

    void setClient(Client client){
        this.client = client;
        this.client.gameController = this;
        playWager.setText(Integer.toString(client.playWager));
        totalWinning.setText(Integer.toString(client.totalWinning));

        hist.getItems().addAll(client.history);
        System.out.println("DEBUG: Client set in GameController");
    }

    void showPlayerCards(){
        try {
            System.out.println("DEBUG: Showing player cards");

            // Show player cards
            if (client.cCard1 != null && !client.cCard1.suit.equals("-1")) {
                System.out.println("DEBUG: Loading player card 1: " + client.cCard1.print());
                clientCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/" + client.cCard1.print() + ".png")));
            } else {
                System.out.println("DEBUG: Player card 1 is null or placeholder");
                clientCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            }

            if (client.cCard2 != null && !client.cCard2.suit.equals("-1")) {
                System.out.println("DEBUG: Loading player card 2: " + client.cCard2.print());
                clientCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/" + client.cCard2.print() + ".png")));
            } else {
                System.out.println("DEBUG: Player card 2 is null or placeholder");
                clientCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            }

            if (client.cCard3 != null && !client.cCard3.suit.equals("-1")) {
                System.out.println("DEBUG: Loading player card 3: " + client.cCard3.print());
                clientCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/" + client.cCard3.print() + ".png")));
            } else {
                System.out.println("DEBUG: Player card 3 is null or placeholder");
                clientCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            }

            // Show dealer cards as face down initially
            dealerCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            dealerCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            dealerCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));

        } catch (Exception e) {
            System.out.println("ERROR: Error loading card images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void showDealerCards(){
        try {
            System.out.println("DEBUG: Showing dealer cards");

            // Show dealer cards face up
            if (client.dCard1 != null && !client.dCard1.suit.equals("-1")) {
                System.out.println("DEBUG: Loading dealer card 1: " + client.dCard1.print());
                dealerCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/" + client.dCard1.print() + ".png")));
            } else {
                System.out.println("DEBUG: Dealer card 1 is null or placeholder");
                dealerCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            }

            if (client.dCard2 != null && !client.dCard2.suit.equals("-1")) {
                System.out.println("DEBUG: Loading dealer card 2: " + client.dCard2.print());
                dealerCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/" + client.dCard2.print() + ".png")));
            } else {
                System.out.println("DEBUG: Dealer card 2 is null or placeholder");
                dealerCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            }

            if (client.dCard3 != null && !client.dCard3.suit.equals("-1")) {
                System.out.println("DEBUG: Loading dealer card 3: " + client.dCard3.print());
                dealerCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/" + client.dCard3.print() + ".png")));
            } else {
                System.out.println("DEBUG: Dealer card 3 is null or placeholder");
                dealerCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            }

            // Wait 2 seconds then go to results scene
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished( e-> {
                System.out.println("DEBUG: Waited for 2 seconds, going to results scene.");
                goToResultsScene();
            });
            pause.play();

        } catch (Exception e) {
            System.out.println("ERROR: Error loading dealer card images: " + e.getMessage());
            e.printStackTrace();
            // Still go to results scene even if images fail
            goToResultsScene();
        }
    }

    private void goToResultsScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ResultsFXML.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) deal.getScene().getWindow();

            Scene scene = new Scene(root, 900, 500);
            scene.getStylesheets().clear();

            ResultsController resultsController = loader.getController();
            resultsController.setClient(client);

            stage.setScene(scene);

        } catch(Exception e1) {
            e1.printStackTrace();
            System.out.println("ERROR: Failure to get to results scene!");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        // Initialize card placeholders
        try {
            clientCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            clientCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            clientCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            dealerCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            dealerCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            dealerCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
        } catch (Exception e) {
            System.out.println("ERROR: Could not load placeholder card images");
        }

        // Initialize button states
        play.setDisable(true);
        fold.setDisable(true);
        anteWager.setEditable(true);
        pairPlusWager.setEditable(true);
        playWager.setEditable(false);

        // Deal button action
        deal.setOnAction(e-> {
            try {
                // Get and validate input
                String anteText = anteWager.getText().trim();
                String pairPlusText = pairPlusWager.getText().trim();

                if (anteText.isEmpty() || pairPlusText.isEmpty()) {
                    System.out.println("ERROR: Please enter both ante and pair plus wagers");
                    return;
                }

                client.anteWager = Integer.parseInt(anteText);
                client.pairPlusWager = Integer.parseInt(pairPlusText);

                System.out.println("DEBUG: Placing bets - Ante: $" + client.anteWager + ", PairPlus: $" + client.pairPlusWager);

                // Validate bets locally first
                if (client.anteWager < 5 || client.anteWager > 25){// || client.anteWager % 5 != 0) {
                    System.out.println("ERROR: Ante must be $5 - $25");//, $10, $15, $20, or $25");
                    hist.getItems().add("ERROR: Ante must be $5 - $25");//"//, $10, $15, $20, or $25");
                    return;
                }

                if (/*client.pairPlusWager != 0 && */(client.pairPlusWager < 5 || client.pairPlusWager > 25)){// || client.pairPlusWager % 5 != 0)) {
                    System.out.println("ERROR: Pair Plus must be $5 - $25");//$0, $5, $10, $15, $20, or $25");
                    hist.getItems().add("ERROR: Pair Plus must be $5 - $25");//$0, $5, $10, $15, $20, or $25");
                    return;
                }

                // Send bets to server using new protocol
                client.sendBets(client.anteWager, client.pairPlusWager);

                // Update UI state
                deal.setDisable(true);
                anteWager.setEditable(false);
                pairPlusWager.setEditable(false);
                play.setDisable(false);
                fold.setDisable(false);

                hist.getItems().add("Bets placed: Ante=$" + client.anteWager + ", Pair Plus=$" + client.pairPlusWager);

            } catch (NumberFormatException ex) {
                System.out.println("ERROR: Please enter numbers only for bets");
                hist.getItems().add("ERROR: Please enter valid numbers for bets");
            } catch (Exception ex) {
                System.out.println("ERROR: Invalid wager input!");
                ex.printStackTrace();
                hist.getItems().add("ERROR: Failed to place bets");
            }
        });

        // Play button action
        play.setOnAction(e-> {
            try {
                // Update UI immediately
                playWager.setText(Integer.toString(client.anteWager));
                client.playWager = client.anteWager;

                System.out.println("DEBUG: Playing hand with wager: $" + client.playWager);

                // Send play decision to server
                client.sendPlayDecision(true, client.playWager);

                // Update button states
                fold.setDisable(true);
                play.setDisable(true);

                hist.getItems().add("Playing hand with wager: $" + client.playWager);

            } catch (Exception e1) {
                System.out.println("ERROR: Cannot communicate with server!");
                e1.printStackTrace();
                hist.getItems().add("ERROR: Failed to send play decision");
            }
        });

        // Fold button action
        fold.setOnAction(e-> {
            try {
                // Update UI immediately
                playWager.setText("0");
                client.playWager = 0;

                System.out.println("DEBUG: Folding hand");

                // Send fold decision to server
                client.sendPlayDecision(false, 0);

                // Update button states
                fold.setDisable(true);
                play.setDisable(true);

                hist.getItems().add("Folded hand - lost ante and pair plus");

            } catch (Exception e2) {
                System.out.println("ERROR: Cannot communicate with server!");
                e2.printStackTrace();
                hist.getItems().add("ERROR: Failed to send fold decision");
            }
        });

        // Fresh Start menu item
        freshStart.setOnAction(e -> {
            try {
                System.out.println("DEBUG: Fresh start requested");
                client.sendFreshStart();
                totalWinning.setText("0");
                hist.getItems().clear();
                resetGameState();
                hist.getItems().add("Fresh start - reset winnings to $0");
            } catch (Exception e3) {
                System.out.println("ERROR: Cannot reset game!");
                e3.printStackTrace();
            }
        });

        // New Look menu item
        newLook.setOnAction(e -> {
            try {
                client.enableNewLook = !client.enableNewLook;
                Scene scene = play.getScene();
                if (client.enableNewLook) {
                    scene.getStylesheets().add("/styles/gameStyle.css");
                    System.out.println("DEBUG: New look enabled");
                    hist.getItems().add("New look enabled");
                } else {
                    scene.getStylesheets().clear();
                    System.out.println("DEBUG: New look disabled");
                    hist.getItems().add("New look disabled");
                }
            } catch (Exception ex) {
                System.out.println("ERROR: Cannot change look");
            }
        });

        // Exit menu item
        exit.setOnAction(e -> {
            try {
                System.out.println("DEBUG: Exit requested");
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

    private void resetGameState() {
        // Reset UI state
        deal.setDisable(false);
        play.setDisable(true);
        fold.setDisable(true);

        anteWager.setEditable(true);
        pairPlusWager.setEditable(true);

        anteWager.clear();
        pairPlusWager.clear();
        playWager.clear();

        // Reset card displays
        try {
            clientCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            clientCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            clientCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            dealerCard1.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            dealerCard2.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
            dealerCard3.setImage(new Image(getClass().getResourceAsStream("/images/Cards/00.png")));
        } catch (Exception e) {
            System.out.println("ERROR: Could not reset card images");
        }

        // Reset client card state
        if (client != null) {
            client.ResetCards();
        }

        System.out.println("DEBUG: Game state reset");
    }

    // Getters for Client access
    public TextField getTotalWinningField() { return totalWinning; }
    public ListView<String> getHistoryListView() { return hist; }
}
