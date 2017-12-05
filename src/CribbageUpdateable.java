/**
 * Interface for objects that want to hear about what's happening in a Cribbage game.
 *
 */
public interface CribbageUpdateable {
	public void receiveUpdate(CribbageEvent type, int player, int points);
}
