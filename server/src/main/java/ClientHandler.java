import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private int clientId;
    private PokerServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private GameState gameState;
    private boolean clientConnected;
    private String clientInfo;

    public ClientHandler(Socket socket, int id, PokerServer server) {
        this.clientSocket = socket;
        this.clientId = id;
        this.server = server;
        this.gameState = new GameState();
        this.clientConnected = true;
        this.clientInfo = "Client #" + id + " (" + socket.getInetAddress() + ")";
    }

    @Override
    public void run() {
        try {
            // Initialize streams
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            clientSocket.setTcpNoDelay(true);

            // Send connection confirmation to client
            sendToClient(new PokerInfo(PokerInfo.MessageType.CONNECT, clientId));

            server.broadcastServerMessage(clientInfo + " has connected");

            // Main message handling loop
            while (clientConnected) {
                try {
                    Object receivedObject = in.readObject();

                    if (receivedObject instanceof PokerInfo) {
                        PokerInfo pokerInfo = (PokerInfo) receivedObject;
                        handleGameMessage(pokerInfo);
                    } else {
                        server.broadcastServerMessage(clientInfo + " sent invalid message type");
                    }

                } catch (ClassNotFoundException e) {
                    server.broadcastServerMessage(clientInfo + " sent unrecognized object");
                } catch (IOException e) {
                    // Client disconnected
                    break;
                }
            }

        } catch (IOException e) {
            server.broadcastServerMessage("Error setting up streams for " + clientInfo);
        } finally {
            disconnectClient();
        }
    }

    /**
     * Handles different types of game messages from client
     */
    private void handleGameMessage(PokerInfo pokerInfo) {
        switch (pokerInfo.getMessageType()) {
            case PLACE_BETS:
                handlePlaceBets(pokerInfo);
                break;

            case PLAY_FOLD:
                handlePlayFold(pokerInfo);
                break;

            case PLAY_AGAIN:
                handlePlayAgain();
                break;

            case DISCONNECT:
                disconnectClient();
                break;

            default:
                server.broadcastServerMessage(clientInfo + " sent unexpected message type: " + pokerInfo.getMessageType());
        }
    }

    /**
     * Handles initial bet placement from client
     */
    private void handlePlaceBets(PokerInfo pokerInfo) {
        int anteBet = pokerInfo.getAnteBet();
        int pairPlusBet = pokerInfo.getPairPlusBet();

        // Validate bets
        if (!GameState.validateBets(anteBet, pairPlusBet)) {
            sendToClient(createErrorInfo("Invalid bet amounts. Ante: $5-$25, Pair Plus: $0 or $5-$25 (multiples of 5)"));
            return;
        }

        try {
            // Place bets and deal cards
            gameState.placeBets(anteBet, pairPlusBet);
            gameState.dealCards();

            // Prepare response with dealt cards
            PokerInfo response = new PokerInfo(PokerInfo.MessageType.DEAL_CARDS);
            response.setPlayerHand(gameState.getPlayerHand());
            response.setDealerHand(gameState.getDealerHand()/*new ArrayList<>()*/); // Dealer cards hidden initially
            response.setAnteBet(anteBet);
            response.setPairPlusBet(pairPlusBet);

            sendToClient(response);

            server.broadcastServerMessage(clientInfo + " placed bets: Ante=$" + anteBet +
                    (pairPlusBet > 0 ? ", Pair Plus=$" + pairPlusBet : ""));

        } catch (Exception e) {
            sendToClient(createErrorInfo("Error dealing cards: " + e.getMessage()));
            server.broadcastServerMessage("Error handling bets for " + clientInfo + ": " + e.getMessage());
        }
    }

    /**
     * Handles player's decision to play or fold
     */
    private void handlePlayFold(PokerInfo pokerInfo) {
        boolean play = pokerInfo.getPlayBet() > 0;
        int playBet = pokerInfo.getPlayBet();

        try {
            // Check if cards are dealt first
            if (!gameState.isCardsDealt()) {
                sendToClient(createErrorInfo("Cards must be dealt before making a decision"));
                server.broadcastServerMessage(clientInfo + " tried to make decision before cards were dealt");
                return;
            }

            // Validate play wager if playing
            if (play && !gameState.validatePlayWager(playBet)) {
                sendToClient(createErrorInfo("Play wager must equal ante bet ($" + gameState.getAnteBet() + ")"));
                server.broadcastServerMessage(clientInfo + " attempted invalid play wager: $" + playBet);
                return;
            }

            // Record player decision
            gameState.makePlayerDecision(play, playBet);

            if (play) {
                server.broadcastServerMessage(clientInfo + " decided to PLAY with wager: $" + playBet);
            } else {
                server.broadcastServerMessage(clientInfo + " decided to FOLD - loses ante and pair plus");
            }

            // Calculate and send game results
            PokerInfo resultInfo = gameState.calculateGameResult();
            resultInfo.setMessageType(PokerInfo.MessageType.GAME_RESULT);

            // Include dealer's cards in the result
            resultInfo.setDealerHand(gameState.getDealerHand());

            sendToClient(resultInfo);

            // Log detailed results on server
            String resultMessage = clientInfo + " - " + resultInfo.getGameResult() +
                    " | Net: " + (resultInfo.getWinnings() >= 0 ? "+" : "") +
                    "$" + resultInfo.getWinnings();
            server.broadcastServerMessage(resultMessage);

            // Log dealer's hand
            ArrayList<Card> dealerHand = gameState.getDealerHand();
            server.broadcastServerMessage(clientInfo + " dealer hand: " +
                    dealerHand.get(0) + ", " + dealerHand.get(1) + ", " + dealerHand.get(2));

        } catch (Exception e) {
            sendToClient(createErrorInfo("Error processing game decision: " + e.getMessage()));
            server.broadcastServerMessage("Error processing decision for " + clientInfo + ": " + e.getMessage());
            e.printStackTrace(); // Add stack trace for debugging
        }
    }

    /**
     * Handles request to play another hand
     */
    private void handlePlayAgain() {
        try {
            gameState.resetGame();

            PokerInfo response = new PokerInfo(PokerInfo.MessageType.PLAY_AGAIN, clientId);
            sendToClient(response);

            server.broadcastServerMessage(clientInfo + " starting new hand");

        } catch (Exception e) {
            sendToClient(createErrorInfo("Error starting new game: " + e.getMessage()));
        }
    }

    /**
     * Sends a PokerInfo object to the client
     */
    public void sendToClient(PokerInfo pokerInfo) {
        if (out != null && clientConnected) {
            try {
                out.writeObject(pokerInfo);
                out.flush();
            } catch (IOException e) {
                server.broadcastServerMessage("Error sending message to " + clientInfo);
                disconnectClient();
            }
        }
    }

    /**
     * Creates an error message to send to client
     */
    private PokerInfo createErrorInfo(String errorMessage) {
        PokerInfo errorInfo = new PokerInfo(PokerInfo.MessageType.GAME_RESULT); // Set message type
        errorInfo.setGameResult("ERROR: " + errorMessage);
        errorInfo.setWinnings(0);
        return errorInfo;
    }

    /**
     * Cleanly disconnects the client
     */
    private void disconnectClient() {
        if (clientConnected) {
            clientConnected = false;

            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                // Ignore during cleanup
            }

            server.clientDisconnected(this);
            server.broadcastServerMessage(clientInfo + " has disconnected");
        }
    }

    // Getters
    public int getClientId() { return clientId; }
    public String getClientInfo() { return clientInfo; }
    public boolean isClientConnected() { return clientConnected; }

    /**
     * Get current game state summary for server display
     */
    public String getGameStatus() {
        if (!gameState.isBetsPlaced()) {
            return "Waiting for bets";
        } else if (!gameState.isCardsDealt()) {
            return "Cards dealt - waiting decision";
        } else if (!gameState.isPlayerDecisionMade()) {
            return "Decision made - calculating results";
        } else {
            return "Hand completed";
        }
    }

    @Override
    public String toString() {
        return clientInfo + " - " + getGameStatus();
    }
}
