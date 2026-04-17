
import java.io.Serializable;
import java.util.ArrayList;

public class PokerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        CONNECT,
        PLACE_BETS,
        DEAL_CARDS,
        PLAY_FOLD,
        GAME_RESULT,
        DISCONNECT,
        PLAY_AGAIN
    }

    private MessageType messageType;
    private ArrayList<Card> playerHand;
    private ArrayList<Card> dealerHand;
    private int anteBet;
    private int pairPlusBet;
    private int playBet;
    private String gameResult;
    private int winnings;
    private boolean playAgain;
    private int clientId;
    private boolean dealerQualifies;

    // Constructors
    public PokerInfo() {
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
    }

    public PokerInfo(MessageType messageType) {
        this();
        this.messageType = messageType;
    }

    public PokerInfo(MessageType messageType, int clientId) {
        this();
        this.messageType = messageType;
        this.clientId = clientId;
    }

    // Getters and Setters (all the same as server)
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public ArrayList<Card> getPlayerHand() { return playerHand; }
    public void setPlayerHand(ArrayList<Card> playerHand) { this.playerHand = playerHand; }

    public ArrayList<Card> getDealerHand() { return dealerHand; }
    public void setDealerHand(ArrayList<Card> dealerHand) { this.dealerHand = dealerHand; }

    public int getAnteBet() { return anteBet; }
    public void setAnteBet(int anteBet) { this.anteBet = anteBet; }

    public int getPairPlusBet() { return pairPlusBet; }
    public void setPairPlusBet(int pairPlusBet) { this.pairPlusBet = pairPlusBet; }

    public int getPlayBet() { return playBet; }
    public void setPlayBet(int playBet) { this.playBet = playBet; }

    public String getGameResult() { return gameResult; }
    public void setGameResult(String gameResult) { this.gameResult = gameResult; }

    public int getWinnings() { return winnings; }
    public void setWinnings(int winnings) { this.winnings = winnings; }

    public boolean isPlayAgain() { return playAgain; }
    public void setPlayAgain(boolean playAgain) { this.playAgain = playAgain; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public boolean isDealerQualifies() { return dealerQualifies; }
    public void setDealerQualifies(boolean dealerQualifies) { this.dealerQualifies = dealerQualifies; }
}
