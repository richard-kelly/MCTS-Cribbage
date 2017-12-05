
/**
 * Represents a card in a 52 card deck
 */
public class Card implements Comparable<Object> {
	
	private Suit suit;
	private int rank;
	
	public Card(Suit s, int r) {
		suit = s;
		rank = r;
	}
	
	public Suit getSuit() {
		return suit;
	}
	
	public int getRank() {
		return rank;
	}
	
	/**
	 * Do these cards have the same rank?
	 * @param other
	 * @return
	 */
	public boolean pair(Card other) {
		if (other.getRank() == rank) {
			return true;
		}
		return false;
	}
	
	/**
	 * Do these cards have the same suit?
	 * @param other
	 * @return
	 */
	public boolean flush(Card other) {
		if (other.getSuit() == suit) {
			return true;
		}
		return false;
	}
	
	/**
	 * Equality is identical cards, rank and suit
	 * @param other
	 * @return
	 */
	public boolean equals(Card other) {
		if (other.rank == rank && other.suit == suit) {
			return true;
		}
		return false;
	}

	/**
	 * Turns card into nice string, with suit symbol
	 */
	public String toString() {
		String card = "";
		switch (rank) {
		case 1:
			card += "A";
			break;
		case 11:
			card += "J";
			break;
		case 12:
			card += "Q";
			break;
		case 13:
			card += "K";
			break;
		default:
			card += rank;
			break;
		}
		card += suit.toString();
		return card;
	}

	/**
	 * order of suits is arbitrary
	 */
	public int compareTo(Object o) {
		Card other = (Card) o;
		if (rank > other.rank) {
			return 1;
		}
		else if (rank < other.rank) {
			return -1;
		}
		else if (suit.ordinal() > other.suit.ordinal()) {
			return 1;
		}
		else if (suit.ordinal() < other.suit.ordinal()) {
			return -1;
		}
		return 0;
	}
}
