/**
 * Card suits
 *
 */
public enum Suit {
	S,
	C,
	H,
	D;
	
	public String toString() {
		switch (this) {
		case S:
			return "\u2660";
		case C:
			return "\u2663";
		case H:
			return "\u2661";
		case D:
			return "\u2662";
		default:
			throw new IllegalArgumentException();
		}
	}
}
