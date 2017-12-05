/**
 * Interface for all objects that can play a Cribbage game, by AI or human interface
 *
 */
public interface CribbagePlayer {
	int getMove(CribbageState gameState) throws Exception;
}
