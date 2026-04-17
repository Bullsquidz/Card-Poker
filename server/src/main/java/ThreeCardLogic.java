import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ThreeCardLogic {

    // Hand rankings constants
    public static final int STRAIGHT_FLUSH = 6;
    public static final int THREE_OF_A_KIND = 5;
    public static final int STRAIGHT = 4;
    public static final int FLUSH = 3;
    public static final int PAIR = 2;
    public static final int HIGH_CARD = 1;

    /**
     * Evaluates a 3-card hand and returns its ranking
     */
    public static int evalHand(ArrayList<Card> hand) {
        if (hand == null || hand.size() != 3) {
            return HIGH_CARD;
        }

        // Sort hand by card value for easier evaluation
        ArrayList<Card> sortedHand = new ArrayList<>(hand);
        Collections.sort(sortedHand, new CardComparator());

        boolean isFlush = isFlush(sortedHand);
        boolean isStraight = isStraight(sortedHand);
        boolean isThreeOfKind = isThreeOfAKind(sortedHand);

        // Check for straight flush first
        if (isFlush && isStraight) {
            return STRAIGHT_FLUSH;
        }

        // Three of a kind
        if (isThreeOfKind) {
            return THREE_OF_A_KIND;
        }

        // Straight
        if (isStraight) {
            return STRAIGHT;
        }

        // Flush
        if (isFlush) {
            return FLUSH;
        }

        // Pair
        if (hasPair(sortedHand)) {
            return PAIR;
        }

        // High card
        return HIGH_CARD;
    }

    /**
     * Calculates Pair Plus winnings based on hand and bet amount
     */
    public static int evalPPWinnings(ArrayList<Card> hand, int bet) {
        int handRank = evalHand(hand);

        switch (handRank) {
            case STRAIGHT_FLUSH:
                return bet * 40; // 40 to 1
            case THREE_OF_A_KIND:
                return bet * 30; // 30 to 1
            case STRAIGHT:
                return bet * 6;  // 6 to 1
            case FLUSH:
                return bet * 3;  // 3 to 1
            case PAIR:
                return bet * 1;  // 1 to 1
            default:
                return 0; // No pair or better - lose the bet
        }
    }

    /**
     * Compares dealer's hand to player's hand
     */
    public static int compareHands(ArrayList<Card> dealer, ArrayList<Card> player) {
        int dealerRank = evalHand(dealer);
        int playerRank = evalHand(player);

        // Compare hand ranks first
        if (playerRank > dealerRank) {
            return 1; // Player wins
        } else if (dealerRank > playerRank) {
            return 2; // Dealer wins
        }

        // Same hand rank - compare high cards
        return compareSameRankHands(dealer, player, dealerRank);
    }

    /**
     * Checks if dealer qualifies (has Queen high or better)
     */
    public static boolean dealerQualifies(ArrayList<Card> dealerHand) {
        if (dealerHand == null || dealerHand.size() != 3) {
            return false;
        }

        int dealerRank = evalHand(dealerHand);

        // If dealer has at least a pair, they qualify
        if (dealerRank >= PAIR) {
            return true;
        }

        // Check for Queen high or better in high card
        int maxCardValue = getMaxCardValue(dealerHand);
        return maxCardValue >= 12; // Queen value = 12
    }

    // Private helper methods

    private static boolean isFlush(ArrayList<Card> hand) {
        String firstSuit = hand.get(0).suit;
        return hand.stream().allMatch(card -> card.suit.equals(firstSuit));
    }

    private static boolean isStraight(ArrayList<Card> hand) {
        int val1 = getCardValue(hand.get(0));
        int val2 = getCardValue(hand.get(1));
        int val3 = getCardValue(hand.get(2));

        // Regular straight
        if (val2 == val1 + 1 && val3 == val2 + 1) {
            return true;
        }

        // Ace-low straight (A-2-3)
        if (val1 == 2 && val2 == 3 && val3 == 14) {
            return true;
        }

        return false;
    }

    private static boolean isThreeOfAKind(ArrayList<Card> hand) {
        int val1 = getCardValue(hand.get(0));
        int val2 = getCardValue(hand.get(1));
        int val3 = getCardValue(hand.get(2));
        return val1 == val2 && val2 == val3;
    }

    private static boolean hasPair(ArrayList<Card> hand) {
        int val1 = getCardValue(hand.get(0));
        int val2 = getCardValue(hand.get(1));
        int val3 = getCardValue(hand.get(2));
        return val1 == val2 || val2 == val3 || val1 == val3;
    }

    private static int compareSameRankHands(ArrayList<Card> dealer, ArrayList<Card> player, int rank) {
        ArrayList<Card> sortedDealer = new ArrayList<>(dealer);
        ArrayList<Card> sortedPlayer = new ArrayList<>(player);
        Collections.sort(sortedDealer, new CardComparator().reversed());
        Collections.sort(sortedPlayer, new CardComparator().reversed());

        switch (rank) {
            case STRAIGHT_FLUSH:
            case STRAIGHT:
                return compareStraightHands(sortedDealer, sortedPlayer);

            case THREE_OF_A_KIND:
                // For three of a kind, just compare the triple card value
                return Integer.compare(getCardValue(sortedPlayer.get(0)), getCardValue(sortedDealer.get(0)));

            case FLUSH:
            case HIGH_CARD:
                return compareHighCardHands(sortedDealer, sortedPlayer);

            case PAIR:
                return comparePairHands(sortedDealer, sortedPlayer);

            default:
                return 0;
        }
    }

    private static int compareStraightHands(ArrayList<Card> dealer, ArrayList<Card> player) {
        // Handle Ace-low straight special case
        boolean dealerAceLow = isAceLowStraight(dealer);
        boolean playerAceLow = isAceLowStraight(player);

        if (dealerAceLow && playerAceLow) return 0;
        if (dealerAceLow) return 1; // Player wins (Ace-low is lowest straight)
        if (playerAceLow) return 2; // Dealer wins

        // Compare highest card for regular straights
        int dealerHigh = getCardValue(dealer.get(0));
        int playerHigh = getCardValue(player.get(0));

        return Integer.compare(playerHigh, dealerHigh);
    }

    private static boolean isAceLowStraight(ArrayList<Card> hand) {
        return getCardValue(hand.get(0)) == 14 &&
                getCardValue(hand.get(1)) == 3 &&
                getCardValue(hand.get(2)) == 2;
    }

    private static int compareHighCardHands(ArrayList<Card> dealer, ArrayList<Card> player) {
        for (int i = 0; i < 3; i++) {
            int playerValue = getCardValue(player.get(i));
            int dealerValue = getCardValue(dealer.get(i));

            if (playerValue > dealerValue) return 1;
            if (dealerValue > playerValue) return 2;
        }
        return 0; // Complete tie
    }

    private static int comparePairHands(ArrayList<Card> dealer, ArrayList<Card> player) {
        // Find the pair values
        int dealerPairValue = findPairValue(dealer);
        int playerPairValue = findPairValue(player);

        // Compare pair values first
        if (playerPairValue > dealerPairValue) return 1;
        if (dealerPairValue > playerPairValue) return 2;

        // Same pair - compare the kicker
        int dealerKicker = findKickerValue(dealer, dealerPairValue);
        int playerKicker = findKickerValue(player, playerPairValue);

        if (playerKicker > dealerKicker) return 1;
        if (dealerKicker > playerKicker) return 2;

        return 0; // Complete tie
    }

    private static int findPairValue(ArrayList<Card> hand) {
        int val1 = getCardValue(hand.get(0));
        int val2 = getCardValue(hand.get(1));
        int val3 = getCardValue(hand.get(2));

        if (val1 == val2) return val1;
        if (val2 == val3) return val2;
        return val1; // first and last must be pair
    }

    private static int findKickerValue(ArrayList<Card> hand, int pairValue) {
        for (Card card : hand) {
            int value = getCardValue(card);
            if (value != pairValue) {
                return value;
            }
        }
        return 0;
    }

    private static int getMaxCardValue(ArrayList<Card> hand) {
        int max = 0;
        for (Card card : hand) {
            int value = getCardValue(card);
            if (value > max) max = value;
        }
        return max;
    }

    // Convert string rank to numerical value
    private static int getCardValue(Card card) {
        switch (card.rank) {
            case "2": return 2;
            case "3": return 3;
            case "4": return 4;
            case "5": return 5;
            case "6": return 6;
            case "7": return 7;
            case "8": return 8;
            case "9": return 9;
            case "10": return 10;
            case "J": return 11;
            case "Q": return 12;
            case "K": return 13;
            case "A": return 14;
            default: return 0;
        }
    }

    // Comparator for sorting cards by value
    private static class CardComparator implements Comparator<Card> {
        @Override
        public int compare(Card c1, Card c2) {
            return Integer.compare(getCardValue(c1), getCardValue(c2));
        }
    }
}