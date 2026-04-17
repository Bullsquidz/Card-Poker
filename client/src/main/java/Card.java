import java.io.Serializable;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;  // MUST MATCH SERVER

    public String suit;
    public String rank;

    public Card(String s, String r) {
        suit = s;
        rank = r;
    }

    public String print() {
        return suit + rank;
    }

    @Override
    public String toString() {
        return suit + " " + rank;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return suit.equals(card.suit) && rank.equals(card.rank);
    }

    @Override
    public int hashCode() {
        return 31 * suit.hashCode() + rank.hashCode();
    }
}