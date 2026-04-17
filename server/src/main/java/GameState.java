import java.util.ArrayList;

public class GameState {
    private Deck deck;
    private ArrayList<Card> playerHand;
    private ArrayList<Card> dealerHand;
    private int anteBet;
    private int pairPlusBet;
    private int playBet;
    private boolean playerFolded;
    private boolean betsPlaced;
    private boolean cardsDealt;
    private boolean playerDecisionMade;

    public GameState() {
        this.deck = new Deck();
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        resetGame();
    }

    /**
     * Resets the game state for a new hand
     */
    public void resetGame() {
        this.deck.reset();
        this.playerHand.clear();
        this.dealerHand.clear();
        this.anteBet = 0;
        this.pairPlusBet = 0;
        this.playBet = 0;
        this.playerFolded = false;
        this.betsPlaced = false;
        this.cardsDealt = false;
        this.playerDecisionMade = false;
    }

    /**
     * Places the initial bets for the hand
     */
    public void placeBets(int anteBet, int pairPlusBet) {
        this.anteBet = anteBet;
        this.pairPlusBet = pairPlusBet;
        this.betsPlaced = true;
    }

    /**
     * Deals cards to both player and dealer
     */
    public void dealCards() {
        if (!betsPlaced) {
            throw new IllegalStateException("Bets must be placed before dealing cards");
        }

        playerHand = deck.dealHand(3);
        dealerHand = deck.dealHand(3);
        cardsDealt = true;
    }

    /**
     * Player decides to play (make play wager) or fold
     */
    public void makePlayerDecision(boolean play, int playBet) {
        if (!cardsDealt) {
            throw new IllegalStateException("Cards must be dealt before player decision");
        }

        this.playerFolded = !play;
        this.playBet = play ? playBet : 0;
        this.playerDecisionMade = true;
    }

    /**
     * Calculates the final game result and returns PokerInfo with results
     */
    public PokerInfo calculateGameResult() {
        if (!playerDecisionMade) {
            throw new IllegalStateException("Player must make decision before calculating results");
        }

        PokerInfo resultInfo = new PokerInfo();
        resultInfo.setMessageType(PokerInfo.MessageType.GAME_RESULT); // Set message type!
        resultInfo.setPlayerHand(new ArrayList<>(playerHand));
        resultInfo.setDealerHand(new ArrayList<>(dealerHand)); // Include dealer cards!
        resultInfo.setAnteBet(anteBet);
        resultInfo.setPairPlusBet(pairPlusBet);
        resultInfo.setPlayBet(playBet);

        // If player folded, they lose both bets
        if (playerFolded) {
            resultInfo.setGameResult("Player folded - lost ante and pair plus bets");
            resultInfo.setWinnings(-(anteBet + pairPlusBet));
            return resultInfo;
        }

        int totalWinnings = 0;
        StringBuilder resultMessage = new StringBuilder();

        // Calculate Pair Plus winnings (independent of dealer)
        int pairPlusWinnings = ThreeCardLogic.evalPPWinnings(playerHand, pairPlusBet);
        if (pairPlusWinnings > 0) {
            totalWinnings += pairPlusWinnings;
            resultMessage.append(String.format("Pair Plus win: $%d", pairPlusWinnings));
        } else if (pairPlusBet > 0) {
            totalWinnings -= pairPlusBet;
            resultMessage.append("Lost Pair Plus bet");
        }

        // Check if dealer qualifies
        boolean dealerQualifies = ThreeCardLogic.dealerQualifies(dealerHand);
        resultInfo.setDealerQualifies(dealerQualifies);

        if (!dealerQualifies) {
            // Dealer doesn't qualify - return play wager, push on ante
            resultMessage.append(resultMessage.length() > 0 ? " | " : "");
            resultMessage.append("Dealer doesn't qualify - play wager returned, ante push");
            totalWinnings += playBet; // Return play wager
            // Ante is push (no win/loss)
        } else {
            // Dealer qualifies - compare hands
            int comparisonResult = ThreeCardLogic.compareHands(dealerHand, playerHand);

            if (comparisonResult == 1) { // Player wins
                resultMessage.append(resultMessage.length() > 0 ? " | " : "");
                resultMessage.append("Player beats dealer");
                totalWinnings += (anteBet + playBet) * 2; // 1:1 payout on both ante and play
            } else if (comparisonResult == 2) { // Dealer wins
                resultMessage.append(resultMessage.length() > 0 ? " | " : "");
                resultMessage.append("Dealer beats player");
                totalWinnings -= (anteBet + playBet); // Lose both ante and play
            } else { // Push
                resultMessage.append(resultMessage.length() > 0 ? " | " : "");
                resultMessage.append("Push - bets returned");
                totalWinnings += (anteBet + playBet); // Return both bets
            }
        }

        resultInfo.setGameResult(resultMessage.toString());
        resultInfo.setWinnings(totalWinnings);

        return resultInfo;
    }

    /**
     * Validates bet amounts according to game rules
     */
    public static boolean validateBets(int anteBet, int pairPlusBet) {
        // Ante bet must be between $5 and $25, and multiple of 5
        boolean anteValid = (anteBet >= 5 && anteBet <= 25);// && anteBet % 5 == 0);

        // Pair Plus bet is optional, but if placed must be between $5 and $25, multiple of 5
        boolean pairPlusValid = (pairPlusBet == 0 || (pairPlusBet >= 5 && pairPlusBet <= 25));// && pairPlusBet % 5 == 0));

        System.out.println("DEBUG: Bet validation - Ante: $" + anteBet + " valid=" + anteValid +
                ", PairPlus: $" + pairPlusBet + " valid=" + pairPlusValid);

        return anteValid && pairPlusValid;
    }

    /**
     * Validates play wager amount (must equal ante bet)
     */
    public boolean validatePlayWager(int playWager) {
        return playWager == anteBet;
    }

    // Getters
    public Deck getDeck() { return deck; }
    public ArrayList<Card> getPlayerHand() { return new ArrayList<>(playerHand); }
    public ArrayList<Card> getDealerHand() { return new ArrayList<>(dealerHand); }
    public int getAnteBet() { return anteBet; }
    public int getPairPlusBet() { return pairPlusBet; }
    public int getPlayBet() { return playBet; }
    public boolean isPlayerFolded() { return playerFolded; }
    public boolean isBetsPlaced() { return betsPlaced; }
    public boolean isCardsDealt() { return cardsDealt; }
    public boolean isPlayerDecisionMade() { return playerDecisionMade; }

    /**
     * Get current game state as string for debugging
     */
    @Override
    public String toString() {
        return String.format(
                "GameState{anteBet=%d, pairPlusBet=%d, playBet=%d, folded=%s, betsPlaced=%s, cardsDealt=%s, decisionMade=%s}",
                anteBet, pairPlusBet, playBet, playerFolded, betsPlaced, cardsDealt, playerDecisionMade
        );
    }
}
