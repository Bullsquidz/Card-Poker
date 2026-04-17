import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Deck implements Serializable {
    private ArrayList<Card> cards;
    private int currentCardIndex;

    public Deck() {
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        cards = new ArrayList<>();

        // Create all cards
        String[] suits = {"C", "H", "S", "D"};  // Clubs, Hearts, Spades, Diamonds
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new Card(suit, rank));
            }
        }

        currentCardIndex = 0;
    }

    public void shuffle() {
        Collections.shuffle(cards, new Random());
        currentCardIndex = 0;
    }

    public Card dealCard() {
        if (currentCardIndex >= cards.size()) {
            shuffle(); // Reshuffle if needed
        }
        return cards.get(currentCardIndex++);
    }

    public ArrayList<Card> dealHand(int numCards) {
        ArrayList<Card> hand = new ArrayList<>();
        for (int i = 0; i < numCards; i++) {
            hand.add(dealCard());
        }
        return hand;
    }

    public void reset() {
        initializeDeck();
        shuffle();
    }

    public int cardsRemaining() {
        return cards.size() - currentCardIndex;
    }
}