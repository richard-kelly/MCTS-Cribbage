import java.util.Arrays;

public class ScriptedCribbagePlayer implements CribbagePlayer{

	public ScriptedCribbagePlayer() {
		
	}
	
	/**
	 * makes a move by doing whatever gains the most points right away,
	 * or the best 4 card hand count if throwing
	 */
	public int getMove(CribbageState gameState) throws Exception {
		int bestAction;
		Card[] hand;
		switch (gameState.getStage()) {
		case THROW:
			hand = gameState.getFullHand(gameState.playerToMove());
			bestAction = 0;
			int maxPoints = 0;
			for (int i = 0; i < 15; i++) {
				int points = countHand(throwCards(hand, i));
				if (points > maxPoints) {
					maxPoints = points;
					bestAction = i;
				}
			}
			break;
		case PLAY:
			int[] moves = gameState.getActions();
			if (moves.length == 1) {
				return moves[0];
			}
			
			hand = gameState.getFullHand((gameState.playerToMove()));
			int playCount = gameState.getPlayCount();
			Card[] cardsPlayed = gameState.getCardsPlayed();
			
			//check each move for number of immediate points
			//if tied don't tie break
			int[] points = new int[moves.length];
			for (int i = 0; i < moves.length; i++) {
				int p = 0; // points for this card
				//15 or 31
				if (cribbageRank(hand[moves[i]]) + playCount == 15 || cribbageRank(hand[moves[i]]) + playCount == 31) {
					p += 2;
				}
				//this is null if no cards have been played yet
				if (cardsPlayed != null) {
					//pairs
					if (hand[moves[i]].pair(cardsPlayed[cardsPlayed.length - 1])) {
						p += 2;
						if (cardsPlayed.length >= 2 && cardsPlayed[cardsPlayed.length - 1].pair(cardsPlayed[cardsPlayed.length - 2])) {
							p += 4;
							if (cardsPlayed.length >= 3 && cardsPlayed[cardsPlayed.length - 2].pair(cardsPlayed[cardsPlayed.length - 3])) {
								p += 6;
							}
						}
					}
					//runs
					int numCards = cardsPlayed.length;
					int runFound = 0;
					for (int j = 3; j <= numCards + 1; j++) {
						int[] runCards = new int[j];
						runCards[0] = hand[moves[i]].getRank();
						int played = numCards - 1;
						for (int k = 1; k < runCards.length; k++) {
							runCards[k] = cardsPlayed[played--].getRank();
						}
						Arrays.sort(runCards);
						boolean run = true;
						for (int k = 1; k < runCards.length; k++) {
							if (runCards[k] != runCards[k-1] + 1) {
								run = false;
								break;
							}
						}
						if (run) {
							runFound = j;
						}
					}
					p += runFound;
				}
				points[i] = p;
			}
			
			//tie breaking is arbitrary
			int max = 0;
			for (int i = 1; i < points.length; i++) {
				if (points[i] > points[max]) {
					max = i;
				}
			}
			bestAction = moves[max];
			break;
		default:
			bestAction = gameState.getActions()[0];
		}
		return bestAction;
	}

	/**
	 * Uses formula (see CribbageState) to remove two cards from an array of Cards
	 * @param cards
	 * @return new array featuring 4 cards after removal
	 */
	private Card[] throwCards(Card[] hand, int cards) {
		Card[] newHand = new Card[4]; 
		int a;
		int b;
		if (cards < 5) {
			a = 0;
			b = cards + 1;
		}
		else if (cards < 9) {
			a = 1;
			b = cards - 3;
		}
		else if (cards < 12) {
			a = 2;
			b = cards - 6;
		}
		else if (cards < 14) {
			a = 3;
			b = cards - 8;
		}
		else {
			a = 4;
			b = 5;
		}
		int count = 0;
		for (int i = 0; i < 6; i++) {
			if (!(i == a || i == b)) {
				newHand[count++] = hand[i];
			}
		}
		return newHand;
	}
	
	/**
	 * Counts the value of a 4-card cribbage hand, to evaluate for throwing
	 * @param hand
	 * @return
	 */
	private int countHand(Card[] hand) {
		
		int points = 0;
		
		//If a 4 card run is present there are no 3 card runs.
		boolean foundRun4 = true;
		int[] runTest = new int[4];
		int temp = 0;
		//check 4 card combos for 15
		for (int i = 0; i < 4; i++) {
			temp += cribbageRank(hand[i]);
			runTest[i] = hand[i].getRank();
		}
		if (temp == 15) {
			points += 2;
		}
		//4-card run
		Arrays.sort(runTest);
		for (int i = 1; i < 4; i++) {
			if (runTest[i] != runTest[i-1]) {
				foundRun4 = false;
			}
		}
		if (foundRun4) {
			points += 4;
		}
		
		//check 3 card combos for 15 and runs
		for (int i = 0; i < 2; i++) {
			for (int j = i + 1; j < 3; j++) {
				for (int k = j + 1; k < 4; k++) {
					if (cribbageRank(hand[i]) + cribbageRank(hand[j]) + cribbageRank(hand[k]) == 15) {
						points += 2;
					}
					if (!foundRun4) {
						int[] run3 = new int[3];
						run3[0] = hand[i].getRank();
						run3[1] = hand[j].getRank();
						run3[2] = hand[k].getRank();
						Arrays.sort(run3);
						if (run3[0] + 1 == run3[1] && run3[1] + 1 == run3[2]) {
							points += 3;
						}
					}
				}
			}
		}
		
		//check each 2-combination of cards for 15, pairs
		for (int i = 0; i < 3; i++) {
			for (int j = i + 1; j < 4; j++) {
				//check two card combos for 15, pair
				if (cribbageRank(hand[i]) + cribbageRank(hand[j]) == 15) {
					points += 2;
				}
				if (hand[i].pair(hand[j])) {
					points += 2;
				}
			}
		}		
		
		//flush
		boolean flush = true;
		for (int i = 1; i < 4; i++) {
			if (!hand[i].flush(hand[i-1])) {
				flush = false;
			}
		}
		if (flush) {
			points += 4;
		}
		
		return points;
	}
	
	/**
	 * Gets rank of cards and turns face cards into 10s
	 * @param c Card
	 * @return
	 */
	private int cribbageRank(Card c) {
		if(c.getRank() >= 10) {
			return 10;
		}
		else {
			return c.getRank();
		}
	}
	
	public String toString() {
		return "Scripted Player";
	}
}
