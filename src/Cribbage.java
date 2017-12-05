import java.util.Scanner;

public class Cribbage implements CribbageUpdateable {

	private CribbageState gameState;
	
	public Cribbage(CribbageState gameState) {
		this.gameState = gameState;
		gameState.registerForUpdates(this);
	}
	
	public static void main(String[] args) throws Exception {
		
		//outer loops for testing parameters
		//add more elements to these arrays to run tests with different values in blocks of numGames games
		double[] explorationConstant = {2.0};
		int[] timeLimit = {500}; //ms
		int[] maxNodes = {0};
		int[] determinizations = {100}; //for DeterminizedUCTCribbage
		
		int numGames = 2; //make it even so that players have equal number of turns as first dealer
		
		for (double c : explorationConstant) {
			for (int t : timeLimit) {
				for (int n : maxNodes) {
					for (int d : determinizations) {
						
						CribbagePlayer[] players = new CribbagePlayer[2];
						
						//Edit the players below to run different tests
						
						players[0] = new SingleObserverCribbage(c, t, n);
						//players[0] = new DeterminizedUCTCribbage(c, t, n, d);
						//players[0] = new CheatingUCTCribbage(c, t, n);
						
						players[1] = new ScriptedCribbagePlayer();
						//players[1] = gameState.new RandomPlayer();
						//players[1] = new DeterminizedUCTCribbage(1.75, 1000, 0, 500);
						//players[1] = new CheatingUCTCribbage(1.75, 1000, 0);
						//players[1] = new SingleObserverCribbage(2.0, 1000, 0);
						
						System.out.println(players[0].toString() + " vs " + players[1].toString());
						
						//match loop - plays numGames/2 of each player starting as dealer
						int j = 0;
						int[] wins = {0, 0};
						for (int i = 0; i < numGames; i++) {
							CribbageState gameState = new CribbageState();
							
							//game loop
							try {
								int toMove;
								while (gameState.getWinner() == -1) {
									toMove = gameState.playerToMove();
									if (toMove == -1) {
										gameState.applyAction(0);
									}
									else {
										gameState.applyAction(players[(toMove + j)%2].getMove(gameState));
									}
								}
							}
							catch (Exception e) {
								e.printStackTrace();
								break;
							}
							
							//record result
							int winner = gameState.getWinner();
							if (winner == 0) {
								wins[j]++;
							}
							else if (winner == 1){
								wins[(j+1)%2]++;
							}
							j = (j + 1) % 2;
						}
						
						//print results of numGames games with current settings:
						System.out.println(wins[0] + ", " + wins[1]);
					}
				}
			}
		}
	}
	
	/**
	 * Prints messages from the game state to the console 
	 */
	public void receiveUpdate(CribbageEvent type, int player, int points) {
		String message = "";
		switch (type) {
		case DEAL:
			message += "------------------------";
			message += "New Hand. Player 0: " + gameState.getScore(0) + ", Player 1: " + gameState.getScore(1) + ".";
			message += " Dealer is Player " + gameState.getDealer() + ".";
			break;
		case THROW:
			message += "Player " + player + " threw two cards.";
			break;
		case CUT:
			message += "Deck is cut: " + gameState.getCut().toString() + ".";
			if (points != 0) {
				message += " Dealer (Player " + gameState.getDealer() + ") gets 2 points.";
			}
			break;
		case PLAY:
			message += "Player " + player + " plays " + gameState.getLastCardPlayed().toString() + " for " + gameState.getPlayCount() + "."; 
			break;
		case FIFTEEN:
			message += "Player " + player + " scores fifteen for 2. Score: " + gameState.getScore(player) + ".";
			break;
		case THIRTYONE:
			message += "Player " + player + " scores thirty one for 2. Score: " + gameState.getScore(player) + ".";
			break;
		case PAIR:
			if (points == 2) {
				message += "Player " + player + " scores a pair for 2. Score: " + gameState.getScore(player) + ".";
			}
			else if (points == 6) {
				message += "Player " + player + " scores three of a kind for 6. Score: " + gameState.getScore(player) + ".";
			}
			else {
				message += "Player " + player + " scores four of a kind for 6. Score: " + gameState.getScore(player) + ".";
			}
			break;
		case RUN:
			message += "Player " + player + " scores a run of " + points + " for " + points + " points. Score: " + gameState.getScore(player) + ".";
			break;
		case GO:
			message += "Player " + player + ": Go.";
			break;
		case LAST:
			message += "Player " + player + " scores one for last. Score: " + gameState.getScore(player) + ".";
			break;
		case SHOW:
			message += "Play is finished.";
			break;
		case COUNT:
			message += "Player " + player + " scores " + points + " with " + cardsToString(gameState.getCurrentHand(player)) + "and cut " + gameState.getCut().toString() + ".";
			break;
		case CRIB:
			message += "Player " + player + " scores " + points + " on the crib with " + cardsToString(gameState.getCrib()) + "and cut " + gameState.getCut().toString() + ".";
			break;
		case WIN:
			if (points == -1) {
				message += "Tie at " + gameState.getScore(0) + " points.";
			}
			else {
				message += "Player " + player + " wins " + gameState.getScore(player) + " to " + gameState.getScore((player + 1) % 2) + ".";
			}
			break;
		default:
			break;
		}
		System.out.println(message);
	}
	
	/**
	 * Prints an array of cards in a nice way
	 * @param cards array of Cards
	 * @return string
	 */
	public String cardsToString(Card[] cards) {
		String s = "";
		for (int i = 0; i < cards.length; i++) {
			s += cards[i].toString() + " ";
		}
		return s;
	}
	
	/**
	 * Interface for playing by console
	 * Used briefly for testing... otherwise a really terrible way to play a game
	 */
	public class TextPlayer implements CribbagePlayer {
		private Scanner in;
		
		public TextPlayer() {
			in = new Scanner(System.in);
		}
		
		public int getMove(CribbageState gameState) throws Exception {
			int[] moves = gameState.getActions();
			switch (gameState.getStage()) {
			case THROW:
				System.out.println("Player " + gameState.playerToMove() + " to throw from: " + cardsToString(gameState.getCurrentHand(gameState.playerToMove())));
				break;
			case PLAY:
				System.out.println("Player " + gameState.playerToMove() + " to play from: " + cardsToString(gameState.getPlayableHand(gameState.playerToMove())));
				break;
			default:
				throw new Exception("Game to move, not player");
			}
			System.out.print("Choose from ");
			for (int i = 0; i < moves.length; i++) {
				System.out.print(moves[i] + ", ");
			}
			System.out.print(": ");
			int action = in.nextInt();
			return action;
		}
	}
}
