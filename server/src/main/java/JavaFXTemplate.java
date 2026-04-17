import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class JavaFXTemplate extends Application {
    private Stage primaryStage;
    private Scene startScene, serverMainScene;
    private ServerStartController startController;
    private ServerMainController mainController;
    private PokerServer pokerServer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("3 Card Poker Server");

        // Load the server start scene
        loadServerStartScene();

        primaryStage.setScene(startScene);
        primaryStage.show();

        // Set up shutdown hook
        primaryStage.setOnCloseRequest(event -> {
            shutdown();
        });
    }

    /**
     * Loads the server start scene with port configuration
     */
    private void loadServerStartScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/serverStart.fxml"));
            Parent root = loader.load();

            startController = loader.getController();
            startController.setMainApplication(this);

            startScene = new Scene(root, 600, 400);
            startScene.getStylesheets().add(getClass().getResource("/server.css").toExternalForm());

        } catch (IOException e) {
            // Fallback to basic scene if FXML fails to load
            System.err.println("Failed to load FXML: " + e.getMessage());
            createFallbackStartScene();
        }
    }

    /**
     * Loads the main server monitoring scene
     */
    private void loadServerMainScene(PokerServer server) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/serverMain.fxml"));
            Parent root = loader.load();

            mainController = loader.getController();
            mainController.setMainApplication(this);
            mainController.setPokerServer(server);  // This sets up the callback

            serverMainScene = new Scene(root, 800, 600);
            serverMainScene.getStylesheets().addAll(
                    getClass().getResource("/server.css").toExternalForm(),
                    getClass().getResource("/serverMain.css").toExternalForm()
            );

        } catch (IOException e) {
            System.err.println("Failed to load main FXML: " + e.getMessage());
            createFallbackMainScene(server);
        }
    }

    /**
     * Shows the server main scene
     */
    public void showServerMainScene(PokerServer server) {
        this.pokerServer = server;
        loadServerMainScene(server);

        if (serverMainScene != null) {
            primaryStage.setScene(serverMainScene);
            primaryStage.setTitle("3 Card Poker Server - Running");
        }
    }

    /**
     * Returns to the start scene
     */
    public void showStartScene() {
        if (startScene != null) {
            primaryStage.setScene(startScene);
            primaryStage.setTitle("3 Card Poker Server");
        }
    }

    /**
     * Fallback scene if FXML fails to load
     */
    private void createFallbackStartScene() {
        VBox root = new VBox(10);
        startScene = new Scene(root, 600, 400);
        startScene.getStylesheets().add(getClass().getResource("/server.css").toExternalForm());
    }

    /**
     * Fallback main scene if FXML fails to load
     */
    private void createFallbackMainScene(PokerServer server) {
        BorderPane root = new BorderPane();
        serverMainScene = new Scene(root, 800, 600);
        serverMainScene.getStylesheets().addAll(
                getClass().getResource("/server.css").toExternalForm(),
                getClass().getResource("/serverMain.css").toExternalForm()
        );
    }

    /**
     * Clean shutdown
     */
    private void shutdown() {
        if (startController != null) {
            startController.shutdown();
        }
        if (mainController != null) {
            mainController.shutdown();
        }
        if (pokerServer != null) {
            pokerServer.stopServer();
        }
    }
}