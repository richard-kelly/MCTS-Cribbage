import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Deck of 52 cards
 *
 */
public class Deck {
	
	private Card[] deck;
	private int count;
	private final int DECK_SIZE = 52;
	
	//unshuffled deck
	public Deck() {
		deck = new Card[DECK_SIZE];
		int i = 0;
		for (Suit suit : Suit.values()) {
			for (int rank = 1; rank <= 13; rank++) {
				deck[i] = new Card(suit, rank);
				i++;
			}
		}
		count = 0;
	}
	
	//copy constructor
	public Deck(Deck source) {
		this.count = source.count;
		this.deck = new Card[DECK_SIZE];
		for (int i = 0; i < deck.length; i++) {
			this.deck[i] = source.deck[i];
		}
	}
	
	public Card draw() throws Exception {
		if (count < DECK_SIZE) {
			return deck[count++];
		}
		else {
			throw new Exception("deck empty");
		}
	}
	
	public Card getCard(int index) {
		return deck[index];
	}
	
	public Card[] draw(int n) throws Exception {
		if (count + n < DECK_SIZE) {
			Card[] cards = new Card[n];
			for (int i = 0; i < n; i++) {
				cards[i] = deck[count++];
			}
			return cards;
		}
		else {
			throw new Exception("not enough cards in deck");
		}
	}
	
	public void shuffle() {
		deck = shuffle(deck);
		count = 0;
	}
	
	/**
	 * Shuffles the deck, holding the cards in indices in noShuffle constant
	 * @param noShuffle
	 */
	public void partialShuffle(int[] noShuffle) {
		//sort in ascending order
		Arrays.sort(noShuffle);
		int noIndex = 0;
		Card[] toShuffle = new Card[DECK_SIZE - noShuffle.length];
		int j = 0;
		for (int i = 0; i < DECK_SIZE; i++) {
			if (i != noShuffle[noIndex]) {
				toShuffle[j++] = deck[i];
			}
			else if (noIndex < noShuffle.length - 1) {
				noIndex++;
			}
		}
		
		toShuffle = shuffle(toShuffle);
		
		noIndex = 0;
		j = 0;
		for (int i = 0; i < DECK_SIZE; i++) {
			if (i != noShuffle[noIndex]) {
				deck[i] = toShuffle[j++];
			}
			else if (noIndex < noShuffle.length - 1) {
				noIndex++;
			}
		}
	}
	
	private Card[] shuffle(Card[] deck) {
		ArrayList<Card> d = new ArrayList<Card>(Arrays.asList(deck));
		Collections.shuffle(d);
		return (Card[]) d.toArray(new Card[deck.length]);
	}
}
