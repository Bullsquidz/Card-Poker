import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;
import javafx.application.Platform;

public class Client extends Thread {
    Socket socket;

    ObjectOutputStream out;
    ObjectInputStream in;

    private Consumer<Serializable> callback;

    String ip;
    int port;

    int anteWager;
    int pairPlusWager;
    int playWager;
    int totalWinning;
    int reward;
    boolean won = false;
    List<String> history;

    GameController gameController;
    boolean enableNewLook = false;

    Card cCard1;
    Card cCard2;
    Card cCard3;
    Card dCard1;
    Card dCard2;
    Card dCard3;

    Client(String ip, int port, Consumer<Serializable> call) {
        this.ip = ip;
        this.port = port;
        this.callback = call;

        anteWager = 0;
        pairPlusWager = 0;
        playWager = 0;
        totalWinning = 0;
        history = new ArrayList<>();

        // Initialize with placeholder cards
        cCard1 = new Card("-1", "-1");
        cCard2 = new Card("-1", "-1");
        cCard3 = new Card("-1", "-1");
        dCard1 = new Card("-1", "-1");
        dCard2 = new Card("-1", "-1");
        dCard3 = new Card("-1", "-1");
    }

    public void run() {
        System.out.println("Attempting Connection to " + ip + ":" + port);
        try {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            socket.setTcpNoDelay(true);

            // Start listening for server messages
            listenForServerMessages();

        } catch (Exception e) {
            Platform.runLater(() ->
                    callback.accept("Connection failed: " + e.getMessage())
            );
        }
    }

    private void listenForServerMessages() {
        while (socket != null && !socket.isClosed()) {
            try {
                Object receivedObject = in.readObject();

                if (receivedObject instanceof PokerInfo) {
                    PokerInfo pokerInfo = (PokerInfo) receivedObject;
                    handleServerMessage(pokerInfo);
                } else {
                    Platform.runLater(() ->
                            callback.accept("Received unknown object type: " + receivedObject.getClass())
                    );
                }

            } catch (IOException e) {
                Platform.runLater(() ->
                        callback.accept("Connection lost: " + e.getMessage())
                );
                break;
            } catch (ClassNotFoundException e) {
                Platform.runLater(() ->
                        callback.accept("Protocol error: " + e.getMessage())
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                        callback.accept("Unexpected error: " + e.getMessage())
                );
                break;
            }
        }
    }

    private void handleServerMessage(PokerInfo pokerInfo) {
        Platform.runLater(() -> {
            try {
                System.out.println("DEBUG: Handling server message: " + pokerInfo.getMessageType());

                switch (pokerInfo.getMessageType()) {
                    case CONNECT:
                        callback.accept("Connected to server! Client ID: " + pokerInfo.getClientId());
                        break;

                    case DEAL_CARDS:
                        if (gameController != null) {
                            handleDealCards(pokerInfo);
                        } else {
                            System.out.println("ERROR: gameController is null in DEAL_CARDS handler");
                            callback.accept("ERROR: Game controller not initialized");
                        }
                        break;

                    case GAME_RESULT:
                        if (gameController != null) {
                            handleGameResult(pokerInfo);
                        } else {
                            System.out.println("ERROR: gameController is null in GAME_RESULT handler");
                            callback.accept("ERROR: Game controller not initialized");
                        }
                        break;

                    case PLAY_AGAIN:
                        callback.accept("Ready for new game");
                        break;

                    default:
                        callback.accept("Server message: " + pokerInfo.getMessageType());
                }
            } catch (Exception e) {
                System.out.println("ERROR: Exception in handleServerMessage: " + e.getMessage());
                e.printStackTrace();
                callback.accept("Error processing server message: " + e.getMessage());
            }
        });
    }

    private void handleDealCards(PokerInfo pokerInfo) {
        System.out.println("DEBUG: Handling DEAL_CARDS message");
        System.out.println("DEBUG: Player hand size: " + pokerInfo.getPlayerHand().size());
        System.out.println("DEBUG: Dealer hand size: " + pokerInfo.getDealerHand().size());

        // Store cards from server
        ArrayList<Card> playerHand = pokerInfo.getPlayerHand();
        ArrayList<Card> dealerHand = pokerInfo.getDealerHand();

        // Convert to your card objects
        if (playerHand.size() >= 3) {
            cCard1 = playerHand.get(0);
            cCard2 = playerHand.get(1);
            cCard3 = playerHand.get(2);
            System.out.println("DEBUG: Player cards: " + cCard1 + ", " + cCard2 + ", " + cCard3);
        }

        if (dealerHand.size() >= 3) {
            dCard1 = dealerHand.get(0);
            dCard2 = dealerHand.get(1);
            dCard3 = dealerHand.get(2);
            System.out.println("DEBUG: Dealer cards: " + dCard1 + ", " + dCard2 + ", " + dCard3);
        }

        // Update UI - with null check
        if (gameController != null) {
            gameController.showPlayerCards();
        } else {
            System.out.println("ERROR: gameController is null in handleDealCards!");
            callback.accept("ERROR: Cannot display cards - game controller not available");
        }
    }

    private void handleGameResult(PokerInfo pokerInfo) {
        System.out.println("DEBUG: Handling GAME_RESULT message");
        System.out.println("DEBUG: Game result: " + pokerInfo.getGameResult());
        System.out.println("DEBUG: Winnings: " + pokerInfo.getWinnings());

        // Store results from server
        this.reward = pokerInfo.getWinnings();
        this.totalWinning += reward;
        this.won = (reward > 0);

        // Add to history
        addToHistory(pokerInfo.getGameResult() + " - Winnings: $" + reward);

        // Show dealer cards and transition to results - with null check
        if (gameController != null) {
            gameController.showDealerCards();
        } else {
            System.out.println("ERROR: gameController is null in handleGameResult!");
            callback.accept("ERROR: Cannot show results - game controller not available");
        }
    }

    //Send bets to server using PokerInfo
    public void sendBets(int anteBet, int pairPlusBet) {
        try {
            PokerInfo betInfo = new PokerInfo(PokerInfo.MessageType.PLACE_BETS);
            betInfo.setAnteBet(anteBet);
            betInfo.setPairPlusBet(pairPlusBet);
            out.writeObject(betInfo);
            out.flush();
            System.out.println("Sent bets to server: Ante=$" + anteBet + ", PairPlus=$" + pairPlusBet);
        } catch (IOException e) {
            Platform.runLater(() ->
                    callback.accept("Failed to send bets: " + e.getMessage())
            );
        }
    }

    //Send play/fold decision to server
    public void sendPlayDecision(boolean play, int playBet) {
        try {
            PokerInfo decisionInfo = new PokerInfo(PokerInfo.MessageType.PLAY_FOLD);
            decisionInfo.setPlayBet(play ? playBet : 0);
            out.writeObject(decisionInfo);
            out.flush();
            System.out.println("Sent decision to server: " + (play ? "PLAY" : "FOLD") + " wager=$" + playBet);
        } catch (IOException e) {
            Platform.runLater(() ->
                    callback.accept("Failed to send decision: " + e.getMessage())
            );
        }
    }

    //Send play again request
    public void sendPlayAgain() {
        try {
            PokerInfo playAgainInfo = new PokerInfo(PokerInfo.MessageType.PLAY_AGAIN);
            out.writeObject(playAgainInfo);
            out.flush();
            System.out.println("Sent play again request to server");
        } catch (IOException e) {
            Platform.runLater(() ->
                    callback.accept("Failed to send play again: " + e.getMessage())
            );
        }
    }

    //Send fresh start request
    public void sendFreshStart() {
        try {
            // Reset local state
            totalWinning = 0;
            history.clear();

            System.out.println("Fresh start - reset winnings to 0");

        } catch (Exception e) {
            Platform.runLater(() ->
                    callback.accept("Failed to reset game: " + e.getMessage())
            );
        }
    }

    //Safe method to set gameController
    public void setGameController(GameController controller) {
        this.gameController = controller;
        System.out.println("DEBUG: GameController set successfully");
    }

    void addToHistory(String s) {
        history.add(s);
        if (gameController != null && gameController.hist != null) {
            Platform.runLater(() -> {
                try {
                    gameController.hist.getItems().add(s);
                } catch (Exception e) {
                    System.out.println("ERROR: Failed to add to history: " + e.getMessage());
                }
            });
        } else {
            System.out.println("WARNING: Cannot add to history - gameController or hist is null");
        }
    }

    void GetCards(Card c1, Card c2, Card c3, Card d1, Card d2, Card d3) {
        cCard1 = c1;
        cCard2 = c2;
        cCard3 = c3;
        dCard1 = d1;
        dCard2 = d2;
        dCard3 = d3;
        if (gameController != null) {
            gameController.showPlayerCards();
        }
    }

    void ResetCards() {
        cCard1 = new Card("-1", "-1");
        cCard2 = new Card("-1", "-1");
        cCard3 = new Card("-1", "-1");
        dCard1 = new Card("-1", "-1");
        dCard2 = new Card("-1", "-1");
        dCard3 = new Card("-1", "-1");
    }

    // Getters for UI updates
    public int getTotalWinning() { return totalWinning; }
    public List<String> getHistory() { return history; }
    public int getReward() { return reward; }
    public boolean isWon() { return won; }
}