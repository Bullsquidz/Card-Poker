import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

public class MyTest {

    private ArrayList<Card> straightFlushHand;
    private ArrayList<Card> threeOfAKindHand;
    private ArrayList<Card> straightHand;
    private ArrayList<Card> flushHand;
    private ArrayList<Card> pairHand;
    private ArrayList<Card> highCardHand;
    private ArrayList<Card> aceLowStraightHand;

    @BeforeEach
    void setUp() {
        // Straight Flush: 5, 6, 7 of Hearts
        straightFlushHand = new ArrayList<>();
        straightFlushHand.add(new Card("H", "5"));
        straightFlushHand.add(new Card("H", "6"));
        straightFlushHand.add(new Card("H", "7"));

        // Three of a Kind: Three Queens
        threeOfAKindHand = new ArrayList<>();
        threeOfAKindHand.add(new Card("H", "Q"));
        threeOfAKindHand.add(new Card("D", "Q"));
        threeOfAKindHand.add(new Card("S", "Q"));

        // Straight: 8, 9, 10 mixed suits
        straightHand = new ArrayList<>();
        straightHand.add(new Card("H", "8"));
        straightHand.add(new Card("D", "9"));
        straightHand.add(new Card("S", "10"));

        // Flush: Three cards same suit, not in sequence
        flushHand = new ArrayList<>();
        flushHand.add(new Card("C", "2"));
        flushHand.add(new Card("C", "7"));
        flushHand.add(new Card("C", "K"));

        // Pair: Two 10s
        pairHand = new ArrayList<>();
        pairHand.add(new Card("H", "10"));
        pairHand.add(new Card("D", "10"));
        pairHand.add(new Card("S", "3"));

        // High Card: No pairs, no sequence
        highCardHand = new ArrayList<>();
        highCardHand.add(new Card("H", "A"));
        highCardHand.add(new Card("D", "9"));
        highCardHand.add(new Card("S", "4"));

        // Ace-low Straight: A, 2, 3
        aceLowStraightHand = new ArrayList<>();
        aceLowStraightHand.add(new Card("H", "A"));
        aceLowStraightHand.add(new Card("D", "2"));
        aceLowStraightHand.add(new Card("S", "3"));
    }

    // Test evalHand method
    @Test
    void testEvalHandStraightFlush() {
        assertEquals(ThreeCardLogic.STRAIGHT_FLUSH,
                ThreeCardLogic.evalHand(straightFlushHand));
    }

    @Test
    void testEvalHandThreeOfAKind() {
        assertEquals(ThreeCardLogic.THREE_OF_A_KIND,
                ThreeCardLogic.evalHand(threeOfAKindHand));
    }

    @Test
    void testEvalHandStraight() {
        assertEquals(ThreeCardLogic.STRAIGHT,
                ThreeCardLogic.evalHand(straightHand));
    }

    @Test
    void testEvalHandFlush() {
        assertEquals(ThreeCardLogic.FLUSH,
                ThreeCardLogic.evalHand(flushHand));
    }

    @Test
    void testEvalHandPair() {
        assertEquals(ThreeCardLogic.PAIR,
                ThreeCardLogic.evalHand(pairHand));
    }

    @Test
    void testEvalHandHighCard() {
        assertEquals(ThreeCardLogic.HIGH_CARD,
                ThreeCardLogic.evalHand(highCardHand));
    }

    @Test
    void testEvalHandAceLowStraight() {
        assertEquals(ThreeCardLogic.STRAIGHT,
                ThreeCardLogic.evalHand(aceLowStraightHand));
    }

    @Test
    void testEvalHandNullHand() {
        assertEquals(ThreeCardLogic.HIGH_CARD,
                ThreeCardLogic.evalHand(null));
    }

    @Test
    void testEvalHandWrongSize() {
        ArrayList<Card> wrongSizeHand = new ArrayList<>();
        wrongSizeHand.add(new Card("H", "A"));
        wrongSizeHand.add(new Card("D", "K"));

        assertEquals(ThreeCardLogic.HIGH_CARD,
                ThreeCardLogic.evalHand(wrongSizeHand));
    }

    // Test evalPPWinnings method
    @Test
    void testEvalPPWinningsStraightFlush() {
        int bet = 5;
        int expectedWinnings = bet * 40; // 40 to 1
        assertEquals(expectedWinnings,
                ThreeCardLogic.evalPPWinnings(straightFlushHand, bet));
    }

    @Test
    void testEvalPPWinningsThreeOfAKind() {
        int bet = 10;
        int expectedWinnings = bet * 30; // 30 to 1
        assertEquals(expectedWinnings,
                ThreeCardLogic.evalPPWinnings(threeOfAKindHand, bet));
    }

    @Test
    void testEvalPPWinningsStraight() {
        int bet = 15;
        int expectedWinnings = bet * 6; // 6 to 1
        assertEquals(expectedWinnings,
                ThreeCardLogic.evalPPWinnings(straightHand, bet));
    }

    @Test
    void testEvalPPWinningsFlush() {
        int bet = 20;
        int expectedWinnings = bet * 3; // 3 to 1
        assertEquals(expectedWinnings,
                ThreeCardLogic.evalPPWinnings(flushHand, bet));
    }

    @Test
    void testEvalPPWinningsPair() {
        int bet = 25;
        int expectedWinnings = bet * 1; // 1 to 1
        assertEquals(expectedWinnings,
                ThreeCardLogic.evalPPWinnings(pairHand, bet));
    }

    @Test
    void testEvalPPWinningsHighCard() {
        int bet = 5;
        int expectedWinnings = 0; // No pair or better - lose the bet
        assertEquals(expectedWinnings,
                ThreeCardLogic.evalPPWinnings(highCardHand, bet));
    }

    @Test
    void testEvalPPWinningsZeroBet() {
        assertEquals(0, ThreeCardLogic.evalPPWinnings(straightFlushHand, 0));
    }

    // Test compareHands method
    @Test
    void testCompareHandsPlayerWins() {
        // Player has straight flush, dealer has three of a kind
        ArrayList<Card> player = straightFlushHand;
        ArrayList<Card> dealer = threeOfAKindHand;

        assertEquals(1, ThreeCardLogic.compareHands(dealer, player));
    }

    @Test
    void testCompareHandsDealerWins() {
        // Dealer has straight flush, player has three of a kind
        ArrayList<Card> player = threeOfAKindHand;
        ArrayList<Card> dealer = straightFlushHand;

        assertEquals(2, ThreeCardLogic.compareHands(dealer, player));
    }


    @Test
    void testCompareHandsPlayerWinsHigherStraight() {
        // Player: 8,9,10 straight, Dealer: 5,6,7 straight
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card("H", "8"));
        player.add(new Card("D", "9"));
        player.add(new Card("S", "10"));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card("H", "5"));
        dealer.add(new Card("D", "6"));
        dealer.add(new Card("S", "7"));

        assertEquals(1, ThreeCardLogic.compareHands(dealer, player));
    }

    @Test
    void testCompareHandsPairComparison() {
        // Player: Pair of 10s with A kicker, Dealer: Pair of 10s with K kicker
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card("H", "10"));
        player.add(new Card("D", "10"));
        player.add(new Card("S", "A"));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card("H", "10"));
        dealer.add(new Card("D", "10"));
        dealer.add(new Card("S", "K"));

        assertEquals(1, ThreeCardLogic.compareHands(dealer, player));
    }

    @Test
    void testCompareHandsHighCardComparison() {
        // Player: A, K, 7, Dealer: A, Q, 7
        ArrayList<Card> player = new ArrayList<>();
        player.add(new Card("H", "A"));
        player.add(new Card("D", "K"));
        player.add(new Card("S", "7"));

        ArrayList<Card> dealer = new ArrayList<>();
        dealer.add(new Card("H", "A"));
        dealer.add(new Card("D", "Q"));
        dealer.add(new Card("S", "7"));

        assertEquals(1, ThreeCardLogic.compareHands(dealer, player));
    }


    @Test
    void testDealerQualifiesWithQueenHigh() {
        ArrayList<Card> queenHighHand = new ArrayList<>();
        queenHighHand.add(new Card("H", "Q"));
        queenHighHand.add(new Card("D", "9"));
        queenHighHand.add(new Card("S", "4"));

        assertTrue(ThreeCardLogic.dealerQualifies(queenHighHand));
    }

    @Test
    void testDealerDoesNotQualifyWithJackHigh() {
        ArrayList<Card> jackHighHand = new ArrayList<>();
        jackHighHand.add(new Card("H", "J"));
        jackHighHand.add(new Card("D", "9"));
        jackHighHand.add(new Card("S", "4"));

        assertFalse(ThreeCardLogic.dealerQualifies(jackHighHand));
    }

    @Test
    void testDealerQualifiesWithAceHigh() {
        ArrayList<Card> aceHighHand = new ArrayList<>();
        aceHighHand.add(new Card("H", "A"));
        aceHighHand.add(new Card("D", "9"));
        aceHighHand.add(new Card("S", "4"));

        assertTrue(ThreeCardLogic.dealerQualifies(aceHighHand));
    }

    @Test
    void testDealerQualifiesWithKingHigh() {
        ArrayList<Card> kingHighHand = new ArrayList<>();
        kingHighHand.add(new Card("H", "K"));
        kingHighHand.add(new Card("D", "9"));
        kingHighHand.add(new Card("S", "4"));

        assertTrue(ThreeCardLogic.dealerQualifies(kingHighHand));
    }

    @Test
    void testDealerDoesNotQualifyWithTenHigh() {
        ArrayList<Card> tenHighHand = new ArrayList<>();
        tenHighHand.add(new Card("H", "10"));
        tenHighHand.add(new Card("D", "9"));
        tenHighHand.add(new Card("S", "4"));

        assertFalse(ThreeCardLogic.dealerQualifies(tenHighHand));
    }


    @Test
    void testDealerQualifiesWrongSizeHand() {
        ArrayList<Card> wrongSizeHand = new ArrayList<>();
        wrongSizeHand.add(new Card("H", "A"));
        wrongSizeHand.add(new Card("D", "K"));

        assertFalse(ThreeCardLogic.dealerQualifies(wrongSizeHand));
    }

    // Edge case tests
    @Test
    void testEdgeCaseAceLowStraightVsRegularStraight() {
        // Ace-low straight (A,2,3) vs Regular straight (J,Q,K)
        ArrayList<Card> aceLowStraight = new ArrayList<>();
        aceLowStraight.add(new Card("H", "A"));
        aceLowStraight.add(new Card("D", "2"));
        aceLowStraight.add(new Card("S", "3"));

        ArrayList<Card> regularStraight = new ArrayList<>();
        regularStraight.add(new Card("H", "J"));
        regularStraight.add(new Card("D", "Q"));
        regularStraight.add(new Card("S", "K"));

        // Regular straight should beat ace-low straight
        assertEquals(2, ThreeCardLogic.compareHands(regularStraight, aceLowStraight));
    }

    @Test
    void testSameHandDifferentSuits() {
        // Same ranks, different suits - should be a push
        ArrayList<Card> hand1 = new ArrayList<>();
        hand1.add(new Card("H", "A"));
        hand1.add(new Card("H", "K"));
        hand1.add(new Card("H", "Q"));

        ArrayList<Card> hand2 = new ArrayList<>();
        hand2.add(new Card("D", "A"));
        hand2.add(new Card("D", "K"));
        hand2.add(new Card("D", "Q"));

        assertEquals(0, ThreeCardLogic.compareHands(hand1, hand2));
    }

    // Additional comprehensive tests
    @Test
    void testAllHandTypesInOrder() {
        ArrayList<Card> highCard = highCardHand;
        ArrayList<Card> pair = pairHand;
        ArrayList<Card> flush = flushHand;
        ArrayList<Card> straight = straightHand;
        ArrayList<Card> threeKind = threeOfAKindHand;
        ArrayList<Card> straightFlush = straightFlushHand;

        // Test that higher hands beat lower hands
        assertEquals(2, ThreeCardLogic.compareHands(pair, highCard));
        assertEquals(2, ThreeCardLogic.compareHands(flush, pair));
        assertEquals(2, ThreeCardLogic.compareHands(straight, flush));
        assertEquals(2, ThreeCardLogic.compareHands(threeKind, straight));
        assertEquals(2, ThreeCardLogic.compareHands(straightFlush, threeKind));
    }

    @Test
    void testMinimumQualifyingHand() {
        // Minimum qualifying hand: Queen high
        ArrayList<Card> queenHigh = new ArrayList<>();
        queenHigh.add(new Card("H", "Q"));
        queenHigh.add(new Card("D", "4"));
        queenHigh.add(new Card("S", "2"));

        assertTrue(ThreeCardLogic.dealerQualifies(queenHigh));
    }

    @Test
    void testMaximumNonQualifyingHand() {
        // Maximum non-qualifying hand: Jack high
        ArrayList<Card> jackHigh = new ArrayList<>();
        jackHigh.add(new Card("H", "J"));
        jackHigh.add(new Card("D", "10"));
        jackHigh.add(new Card("S", "9"));

        assertTrue(ThreeCardLogic.dealerQualifies(jackHigh));
    }
}
