import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class CribbageState {
	
	private Card[] crib;
	private Card cut;
	private Deck deck;
	private CribbageStage stage;
	private int playerToMove;
	private int dealer;
	private Player[] players;
	
	//play information
	private int playCount;
	private Card[] cardsPlayed;
	private int numCardsPlayed;
	
	private boolean handOver;
	
	private Collection<CribbageUpdateable> toUpdate;
	
	public CribbageState() {
		
		players = new Player[2];
		players[0] = new Player();
		players[1] = new Player();
		
		dealer = 0;
		playerToMove = -1; //represents game to move (deal)
		stage = CribbageStage.DEAL;
		deck = new Deck();
		handOver = true;
		
		newHand();
		
		//play initialization
		resetPlay();
		
		toUpdate = new ArrayList<CribbageUpdateable>();
	}
	
	/**
	 * Copy constructor, copies entire game state, excluding listeners
	 * @param source game state to copy
	 */
	CribbageState(CribbageState source) {
		if (source.crib == null) {
			this.crib = null;
		}
		else {
			this.crib = new Card[source.crib.length];
			for (int i = 0; i < source.crib.length; i++) {
				//need I check if null?
				this.crib[i] = source.crib[i];
			}
		}
		
		if (source.cardsPlayed == null) {
			this.cardsPlayed = null;
		}
		else {
			this.cardsPlayed = new Card[source.cardsPlayed.length];
			for (int i = 0; i < source.cardsPlayed.length; i++) {
				//need I check if null?
				this.cardsPlayed[i] = source.cardsPlayed[i];
			}
		}
		
		if (source.players == null) {
			this.players = null;
		}
		else {
			this.players = new Player[2];
			this.players[0] = new Player(source.players[0]);
			this.players[1] = new Player(source.players[1]);
		}
		
		if (source.deck == null) {
			this.deck = null;
		}
		else {
			this.deck = new Deck(source.deck);	
		}
		
		this.stage = source.stage;
		this.playerToMove = source.playerToMove;
		this.dealer = source.dealer;
		this.cut = source.cut;
		this.playCount = source.playCount;
		this.numCardsPlayed = source.numCardsPlayed;
		this.handOver = source.handOver;
		
		//don't copy this, because we don't want to send updates from copies
		this.toUpdate = new ArrayList<CribbageUpdateable>();
	}
	
	public int getScore(int player) {
		return players[player].getScore();
	}

	public int playerToMove() {
		return playerToMove;
	}

	public int getDealer() {
		return dealer;
	}

	public int getWinner() { 
		if (players[0].getScore() >= 121) {
			return 0;
		}
		else if (players[1].getScore() >= 121) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
	/**
	 * Gets the net gain(loss) in points for player during current hand
	 * @param player int
	 * @return
	 */
	public int getHandPointDiff(int player) {
		return players[player].getHandScore() - players[(player + 1) % 2].getHandScore();
	}
	
	/**
	 * Returns true only when a hand is finished but the next one hasn't been dealt yet
	 * @return
	 */
	public boolean handOver() {
		return handOver;
	}

	public Deck getDeck() {
		return deck;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	public CribbageStage getStage() {
		return stage;
	}

	public Card[] getCrib() {
		return crib; 
	}

	public Card getCut() {
		return cut;
	}
	
	/**
	 * Gets array of cards played in play stage of game
	 * @return
	 */
	public Card[] getCardsPlayed() {
		if (numCardsPlayed > 0) {
			Card[] actualCardsPlayed = new Card[numCardsPlayed];
			for (int i = 0; i < numCardsPlayed; i++) {
				actualCardsPlayed[i] = cardsPlayed[i];
			}
			return actualCardsPlayed;
		}
		
		return null;
	}
	
	public Card getLastCardPlayed() {
		if (numCardsPlayed > 0) {
			return cardsPlayed[numCardsPlayed - 1];
		}
		return null;
	}
	
	/**
	 * Sum of ranks of cards played in play stage
	 * @return
	 */
	public int getPlayCount() {
		return playCount;
	}

	/**
	 * Returns the cards that the player has not thrown or played in this hand 
	 * @param player
	 * @return
	 */
	public Card[] getPlayableHand(int player) {
		return players[player].getHandNotPlayed();
	}
	
	/**
	 * Gets cards that the player has not thrown in this hand
	 * @param player
	 * @return
	 */
	public Card[] getCurrentHand(int player) {
		return players[player].getCurrentHand();
	}
	
	/**
	 * Returns full player hand, with null for cards thrown after throw stage
	 * @param player
	 * @return
	 */
	public Card[] getFullHand(int player) {
		return players[player].getFullHand();
	}
	
	/**
	 * Gets indexes of cards played by this player (0-5) in player's hand
	 * @param player
	 * @return
	 * @throws Exception
	 */
	public int[] getPlayedIndexes(int player) throws Exception {
		return players[player].getPlayedCardIndexes();
	}
	
	/**
	 * Gets array of booleans indicating if a card has been played yet (true) for player player
	 * @param player
	 * @return
	 */
	public boolean[] getPlayed(int player) {
		return players[player].getPlayed();
	}
	
	/**
	 * Gets the two cards selected by the index throwIndex,
	 * which is an int 0-14 corresponding to 6 choose 2 ways to throw 6 cards
	 * @param player
	 * @param throwIndex
	 * @return
	 */
	public Card[] getCardsThrownByIndex(int player, int throwIndex) {
		return players[player].getCardsThrownBy(throwIndex);
	}
	
	/**
	 * Returns an integer array with ints corresponding to every legal action for player to move
	 * @return int[]
	 * @throws Exception
	 */
	public int[] getActions() throws Exception {
		if (getWinner() != -1) {
			throw new Exception("Game Over");
		}	
		int[] actions;
		switch (stage) {
		case DEAL:
			//only one thing to do
			actions = new int[1];
			actions[0] = 0;
			break;
		case THROW:
			//15 ways to throw 2 cards
			actions = new int[15];
			for (int i = 0; i < 15; i++) {
				actions[i] = i;
			}
			break;
		case CUT:
			// only one legal action
			actions = new int[1];
			actions[0] = 0;
			break;
		case PLAY:
			//indexes are position in Card[] of player hand
			actions = players[playerToMove].getPlayableCardIndexes(playCount);
			break;
		default:
			throw new Exception("Invalid stage");
		}
		return actions;
	}

	/**
	 * Applies an action in the form of an integer to the game state
	 * @param a
	 * @return
	 * @throws Exception
	 */
	public CribbageState applyAction(int a) throws Exception {
		if (getWinner() != -1) {
			throw new Exception("Game Over");
		}	
		int[] moves = getActions();
		boolean illegal = true;
		for (int i = 0; i < moves.length; i++) {
			if (a == moves[i]) {
				illegal = false;
				break;
			}
		}
		if (illegal) {
			throw new Exception("Illegal action: " + a);
		}
		
		switch (stage) {
		case DEAL:
			newHand();
			players[0].initHand(deck.draw(6));
			players[1].initHand(deck.draw(6));
			reportUpdate(CribbageEvent.DEAL, -1, 0);
			playerToMove = (dealer + 1) % 2;
			stage = CribbageStage.THROW;
			handOver = false;
			break;
		case THROW:
			//new hands dealt, receiving cards to be thrown
			//expecting number between 0-14
			//0 is 0th card and 1st card, 1 is 0th and 2nd card, 6 is 0th and 5th card,
			//7 is 1st and 2nd card, etc.
			Card[] cardsForCrib = players[playerToMove].throwCards(a);
			crib[0 + 2 * playerToMove] = cardsForCrib[0];
			crib[1 + 2 * playerToMove] = cardsForCrib[1];
			reportUpdate(CribbageEvent.THROW, playerToMove, 0);
			//non-dealer throws first, then dealer
			//(really simultaneous, but done in order for convenience)
			if (playerToMove == dealer) {
				playerToMove = -1; //game to move
				stage = CribbageStage.CUT;
			}
			else {
				playerToMove = (playerToMove + 1) % 2;
			}
			break;
		case CUT:
			if (a != 0) {
				throw new Exception("Illegal action");
			}
			cut = deck.draw();
			if (cut.getRank() == 11) {
				addPoints(dealer, 2);
				reportUpdate(CribbageEvent.CUT, dealer, 2);
			}
			else {
				reportUpdate(CribbageEvent.CUT, -1, 0);
			}
			playerToMove = (dealer + 1) % 2;
			stage = CribbageStage.PLAY;
			break;	
		case PLAY:
			//starting with non-dealer, players alternate playing cards			
			Card cardPlayed = players[playerToMove].play(a);
			playCount += cribbageRank(cardPlayed);
			cardsPlayed[numCardsPlayed++] = cardPlayed;
			reportUpdate(CribbageEvent.PLAY, playerToMove, 0);
			
			if (playCount == 15) {
				addPoints(playerToMove, 2);
				reportUpdate(CribbageEvent.FIFTEEN, playerToMove, 2);
			}
			else if (playCount == 31) {
				addPoints(playerToMove, 2);
				reportUpdate(CribbageEvent.THIRTYONE, playerToMove, 2);
			}
			
			int run = checkPlayRuns();
			if (run > 0) {
				addPoints(playerToMove, run);
				reportUpdate(CribbageEvent.RUN, playerToMove, run);
			}
			else {
				int pair = checkPlayPairs();
				if (pair > 0) {
					addPoints(playerToMove, pair);
					reportUpdate(CribbageEvent.PAIR, playerToMove, 2);
				}
			}
			
			//next play... have to check if either player can play, if there's a go, point for last, etc.
			
			if (players[(playerToMove + 1) % 2].canPlay(playCount)) {
				//other player can play
				playerToMove = (playerToMove + 1) % 2;
			}
			else if (players[playerToMove].canPlay(playCount)) {
				//go, this player can keep playing
				reportUpdate(CribbageEvent.GO, (playerToMove + 1) % 2, 0);
			}
			else {
				if (playCount != 31 && !players[(playerToMove + 1) % 2].canPlay(playCount) && players[(playerToMove + 1) % 2].getHandNotPlayed().length > 0) {
					reportUpdate(CribbageEvent.GO, (playerToMove + 1) % 2, 0);
				}
				
				//this round of play ended, award go point, reset, and switch player if possible
				if (playCount != 31) {
					addPoints(playerToMove, 1);
					reportUpdate(CribbageEvent.LAST, playerToMove, 1);
				}
				resetPlay();
				if (players[(playerToMove + 1) % 2].canPlay(playCount)) {
					playerToMove = (playerToMove + 1) % 2;
				}
				else if (players[playerToMove].canPlay(playCount)) {
					//this player starts new session
				}
				else {
					//Play is over, count, change dealer, new hand if not winnner.
					reportUpdate(CribbageEvent.SHOW, -1, 0);
					
					//non-dealer
					int handScore;
					handScore = countHand(players[(dealer + 1) % 2].getCurrentHand(), cut, false);
					addPoints((dealer + 1) % 2, handScore);
					reportUpdate(CribbageEvent.COUNT, (dealer + 1) % 2, handScore);
					
					//dealer
					handScore = countHand(players[dealer].getCurrentHand(), cut, false);
					addPoints(dealer, handScore);
					reportUpdate(CribbageEvent.COUNT, dealer, handScore);
					
					//crib
					handScore = countHand(crib, cut, true);
					addPoints(dealer, handScore);
					reportUpdate(CribbageEvent.CRIB, dealer, handScore);
					
					//hand is over
					handOver = true;
					
					if (getWinner() == -1) {
						dealer = (dealer + 1) % 2;
						stage = CribbageStage.DEAL;
						playerToMove = -1;
					}
					else {
						//display winner
						reportUpdate(CribbageEvent.WIN, getWinner(), 0);
					}
				}
			}
			break;
		default:
			throw new Exception("Invalid stage");
		}
			
		return this;
	}

	/**
	 * Adds updateable object to list of objects that receive updates about points and other game events
	 * @param updateable
	 */
	public void registerForUpdates(CribbageUpdateable updateable) {
		toUpdate.add(updateable);
	}
	
	/**
	 * randomizes cards not yet seen by player player
	 * @param player integer either 0 or 1
	 */
	public void randomize(int player) {
		int[] noShuffle;
		int i;
		Card[] newHand;
		Card[] cardsForCrib;
		switch (stage) {
		case DEAL:
			deck.shuffle();
			break;
		case THROW:
			//player 0 has cards 0-5 in deck. player 1 has cards 6-11.
			noShuffle = new int[6];
			i = 0 + 6 * player;
			for (int j = 0; j < 6; j++) {
				noShuffle[j] = i++;
			}
			deck.partialShuffle(noShuffle);
			
			//deal new 6 cards to other player
			i = 0 + 6 * ((player + 1) % 2);
			newHand = new Card[6];
			for (int j = 0; j < 6; j++) {
				newHand[j] = deck.getCard(i++);
			}
			players[(player + 1) % 2].initHand(newHand);
			
			//handle cards in crib
			//if other player has already thrown, this is assuming a random throw
			//(i.e. not weighing by what other player likely would have thrown)
			if (playerToMove == dealer && player == playerToMove) {
				//other player has already thrown, need to update cards in crib
				//throw any 2 cards
				cardsForCrib = players[(player + 1) % 2].throwCards(0);
				crib[0 + 2 * ((player + 1) % 2)] = cardsForCrib[0];
				crib[1 + 2 * ((player + 1) % 2)] = cardsForCrib[1];
			}
			break;
		case CUT:
			//player 0 has cards 0-5 in deck. player 1 has cards 6-11.
			noShuffle = new int[6];
			i = 0 + 6 * player;
			for (int j = 0; j < 6; j++) {
				noShuffle[j] = i++;
			}
			deck.partialShuffle(noShuffle);
			
			//deal new 6 cards to other player
			i = 0 + 6 * ((player + 1) % 2);
			newHand = new Card[6];
			for (int j = 0; j < 6; j++) {
				newHand[j] = deck.getCard(i++);
			}
			players[(player + 1) % 2].initHand(newHand);
			
			//handle cards in crib
			//make random throw for other player
			//(i.e. not weighing by what other player likely would have thrown)
			cardsForCrib = players[(player + 1) % 2].throwCards(0);
			crib[0 + 2 * ((player + 1) % 2)] = cardsForCrib[0];
			crib[1 + 2 * ((player + 1) % 2)] = cardsForCrib[1];
			break;
		case PLAY:
			//if opponent has played, need to not shuffle those cards
			boolean oppPlayed = true;
			int[] oppPlayedCardIndexes = new int[0];
			try {
				oppPlayedCardIndexes = players[(player + 1) % 2].getPlayedCardIndexes();
			}
			catch (Exception e) {
				oppPlayed = false;
			}
			
			if (oppPlayed) {
				noShuffle = new int[7 + oppPlayedCardIndexes.length];
				
				//player's cards
				i = 0 + 6 * player;
				for (int j = 0; j < 6; j++) {
					noShuffle[j] = i++;
				}
				
				//cut card
				noShuffle[6] = 12; //index of cut card in the deck
				
				//opponent's cards
				i = 7;
				for (int j = 0; j < oppPlayedCardIndexes.length; j++) {
					noShuffle[i++] = oppPlayedCardIndexes[j] + 6 * ((player + 1) % 2);
				}
				
				deck.partialShuffle(noShuffle);
				
				//deal new 6 cards to other player
				i = 0 + 6 * ((player + 1) % 2);
				newHand = new Card[6];
				for (int j = 0; j < 6; j++) {
					newHand[j] = deck.getCard(i++);
				}
				int throwIndex = players[(player + 1) % 2].getThrowIndex(); 
				players[(player + 1) % 2].initHand(newHand);
				
				//throw same cards (that we haven't seen)
				cardsForCrib = players[(player + 1) % 2].throwCards(throwIndex);
				crib[0 + 2 * ((player + 1) % 2)] = cardsForCrib[0];
				crib[1 + 2 * ((player + 1) % 2)] = cardsForCrib[1];
				
				//play cards already played
				for (int j = 0; j < oppPlayedCardIndexes.length; j++) {
					players[(player + 1) % 2].play(oppPlayedCardIndexes[j]);
				}
			}
			else {
				noShuffle = new int[7];
				
				//player's cards
				i = 0 + 6 * player;
				for (int j = 0; j < 6; j++) {
					noShuffle[j] = i++;
				}
				
				//cut card
				noShuffle[6] = 12; //index of cut card in the deck
				
				deck.partialShuffle(noShuffle);
				
				//deal new 6 cards to other player
				i = 0 + 6 * ((player + 1) % 2);
				newHand = new Card[6];
				for (int j = 0; j < 6; j++) {
					newHand[j] = deck.getCard(i++);
				}
				int throwIndex = players[(player + 1) % 2].getThrowIndex(); 
				players[(player + 1) % 2].initHand(newHand);
				
				//throw same cards (that we haven't seen)
				cardsForCrib = players[(player + 1) % 2].throwCards(throwIndex);
				crib[0 + 2 * ((player + 1) % 2)] = cardsForCrib[0];
				crib[1 + 2 * ((player + 1) % 2)] = cardsForCrib[1];
			}
			break;
		default:
			break;
			
		}
	}

	
	private void addPoints(int player, int points) {
		players[player].addPoints(points);
		if (getWinner() != -1) {
			handOver = true;
		}
	}
	
	/**
	 * Counts a hand according to Cribbage rules
	 * @param hand Card[]
	 * @param cut Card
	 * @param crib boolean is this hand a crib (affects flush)
	 * @return
	 */
	private int countHand(Card[] hand, Card cut, boolean crib) {
		Card[] fullHand;
		fullHand = new Card[5];
		for (int i = 0; i < 4; i++) {
			fullHand[i] = hand[i];
		}
		fullHand[4] = cut;
		int points = 0;
		
		//runs - if a 5 card run is present no other runs exist.
		//If a 4 card run is present there are no 3 card runs.
		boolean foundRun5 = false;
		boolean foundRun4 = false;
		int[] runTest = new int[5];
		
		//5 card 15 or run
		int temp = 0;
		for (int i = 0; i < 5; i++) {
			temp += cribbageRank(fullHand[i]);
			runTest[i] = fullHand[i].getRank();
		}
		if (temp == 15) {
			points +=2;
		}
		Arrays.sort(runTest);
		boolean run5 = true;
		for (int i = 1; i < 5; i++) {
			if (runTest[i] != runTest[i-1]) {
				run5 = false;
			}
		}
		if (run5) {
			foundRun5 = true;
			points += 5;
		}
		
		//check 4 card combos for 15
		boolean run4 = true;
		for (int i = 0; i < 5; i++) {
			int temp4 = 0;
			if (!foundRun5) {
				runTest = new int[5];
				runTest[i] = -1;
				run4 = true;
			}
			for (int k = 0; k < 5; k++) {
				if (k != i) {
					temp4 += cribbageRank(fullHand[k]);
					if (!foundRun5) {
						runTest[k] = fullHand[k].getRank();
					}
				}
			}
			if (temp4 == 15) {
				points += 2;
			}
			if (!foundRun5) {
				Arrays.sort(runTest);
				for (int j = 2; j < 5; j++) {
					if (runTest[j] != runTest[j-1]) {
						run4 = false;
					}
				}
				if (run4) {
					foundRun4 = true;
					points += 4;
				}
			}
		}
		
		//check 3 card combos for 15
		for (int i = 0; i < 3; i++) {
			for (int j = i + 1; j < 4; j++) {
				for (int k = j + 1; k < 5; k++) {
					if (cribbageRank(fullHand[i]) + cribbageRank(fullHand[j]) + cribbageRank(fullHand[k]) == 15) {
						points += 2;
					}
					if (!(foundRun4 || foundRun5)) {
						int[] run3 = new int[3];
						run3[0] = fullHand[i].getRank();
						run3[1] = fullHand[j].getRank();
						run3[2] = fullHand[k].getRank();
						Arrays.sort(run3);
						if (run3[0] + 1 == run3[1] && run3[1] + 1 == run3[2]) {
							points += 3;
						}
					}
				}
			}
		}
		
		//check each 2-combination of cards for 15, pairs
		for (int i = 0; i < 4; i++) {
			for (int j = i + 1; j < 5; j++) {
				//check two card combos for 15, pair
				if (cribbageRank(fullHand[i]) + cribbageRank(fullHand[j]) == 15) {
					points += 2;
				}
				if (fullHand[i].pair(fullHand[j])) {
					points += 2;
				}
			}
		}		
		
		//flush
		boolean flush = true;
		for (int i = 1; i < 4; i++) {
			if (!fullHand[i].flush(fullHand[i-1])) {
				flush = false;
			}
		}
		if (flush) {
			if (fullHand[3].flush(fullHand[4])) {
				points += 5;
			}
			else if (!crib) {
				points += 4;
			}
		}
		
		//his nob
		for (int i = 0; i < 4; i++) {
			if (hand[i].getRank() == 11 && hand[i].getSuit() == cut.getSuit()) {
				points += 1;
			}
		}
		
		return points;
	}

	/**
	 * Resets some things for a new hand
	 */
	private void newHand() {
		resetPlay();
		cut = null;
		crib = new Card[4];
		deck.shuffle();
	}
	
	/**
	 * resets some things for new hand or after a "Go"
	 */
	private void resetPlay() {
		playCount = 0;
		cardsPlayed = new Card[8];
		numCardsPlayed = 0;
	}
	
	/**
	 * Returns point value of largest run achieved by latest play
	 * @return
	 */
	private int checkPlayRuns() {
		int bestRun = 0;
		for (int i = 3; i <= numCardsPlayed; i++) {
			int[] run = new int[i];
			for (int j = numCardsPlayed - 1; j >= numCardsPlayed - i; j--) {
				run[numCardsPlayed - 1 - j] = cardsPlayed[j].getRank();
			}
			Arrays.sort(run);
			boolean foundRun = true;
			for (int j = 1; j < run.length; j++) {
				if (run[j] - run[j-1] != 1) {
					foundRun = false;
					break;
				}
			}
			if (foundRun) {
				bestRun = i;
			}
		}
		return bestRun;
	}
	
	/**
	 * Returns points earned by latest play if there is a pair, 3-of-a-kind, etc.
	 * @return
	 */
	private int checkPlayPairs() {
		int highestPair = 0;
		
		for (int i = numCardsPlayed - 2; i >= numCardsPlayed -4 && i >= 0; i--) {
			if (cardsPlayed[i + 1].getRank() == cardsPlayed[i].getRank()) {
				highestPair++;
			}
			else {
				break;
			}
		}
		switch(highestPair) {
			case 0:
				return 0;
			case 1:
				return 2;
			case 2:
				return 6;
			case 3:
				return 12;
			default:
				return 0;
		}
	}
	
	private int cribbageRank(Card c) {
		if(c.getRank() >= 10) {
			return 10;
		}
		else {
			return c.getRank();
		}
	}

	private void reportUpdate(CribbageEvent type, int player, int points) {
		for (CribbageUpdateable updateable : toUpdate) {
			updateable.receiveUpdate(type, player, points);
		}
	}
	
	/**
	 * Represents a Cribbage player's hand, score, etc.
	 *
	 */
	private class Player {
		private Card[] hand;
		private boolean[] played;
		private int score;
		private int handScore;
		int cardsToPlay; //number of cards left for play
		boolean thrown;
		int throwIndex;
		
		/**
		 * Creates Player object ready for new game
		 */
		public Player() {
			this.score = 0;
		}
		
		/**
		 * Copy constructor
		 * @param source
		 */
		public Player(Player source) {
			this.score = source.score;
			this.cardsToPlay = source.cardsToPlay;
			this.thrown = source.thrown;
			this.throwIndex = source.throwIndex;
			
			if (source.played == null) {
				this.played = null;
			}
			else {
				this.played = new boolean[source.played.length];
				for (int i = 0; i < source.played.length; i++) {
					this.played[i] = source.played[i];
				}
			}
			
			if (source.hand == null) {
				this.hand = null;
			}
			else {
				this.hand = new Card[source.hand.length];
				for (int i = 0; i < source.hand.length; i++) {
					this.hand[i] = source.hand[i];
				}
			}
		}
		
		/**
		 * Prepare player for new hand
		 * @param hand
		 */
		public void initHand(Card[] hand) {
			this.hand = hand;
			played = new boolean[6];
			cardsToPlay = 6;
			thrown = false;
			throwIndex = -1;
			handScore = 0;
		}
		
		/**
		 * Removes two cards from hand according to counting system
		 * Cards: 0, 1, 2, 3, 4, 5
		 * index: 	0->0,1	1->0,2	2->0,3	 3->0,4	 4->0,5
		 * 					5->1,2	6->1,3	 7->1,4	 8->1,5
		 * 							9->2,3	10->2,4	11->2,5
		 * 									12->3,4	13->3,5
		 * 											14->4,5
		 * @param cards
		 * @return
		 */
		public Card[] throwCards(int cards) {
			throwIndex = cards;
			Card[] cribCards = new Card[2]; 
			if (cards < 5) {
				cribCards[0] = remove(0);
				cribCards[1] = remove(cards + 1);
			}
			else if (cards < 9) {
				cribCards[0] = remove(1);
				cribCards[1] = remove(cards - 3);
			}
			else if (cards < 12) {
				cribCards[0] = remove(2);
				cribCards[1] = remove(cards - 6);
			}
			else if (cards < 14) {
				cribCards[0] = remove(3);
				cribCards[1] = remove(cards - 8);
			}
			else {
				cribCards[0] = remove(4);
				cribCards[1] = remove(5);
			}
			thrown = true;
			return cribCards;
		}
		
		/**
		 * Find out which Cards would be thrown by a throwIndex (see throwCards())
		 * @param throwIndex
		 * @return
		 */
		public Card[] getCardsThrownBy(int throwIndex) {
			Card[] result = new Card[2];
			if (throwIndex < 5) {
				result[0] = hand[0];
				result[1] = hand[throwIndex + 1];
			}
			else if (throwIndex < 9) {
				result[0] = hand[1];
				result[1] = hand[throwIndex - 3];
			}
			else if (throwIndex < 12) {
				result[0] = hand[2];
				result[1] = hand[throwIndex - 6];
			}
			else if (throwIndex < 14) {
				result[0] = hand[3];
				result[1] = hand[throwIndex - 8];
			}
			else {
				result[0] = hand[4];
				result[1] = hand[5];
			}
			return result;
		}
		
		/**
		 * Returns index of action that threw cards for this player earlier in hand.
		 * @return int between 0-14
		 */
		public int getThrowIndex() {
			return throwIndex;
		}
		
		/**
		 * Determines if the player has any unplayed cards that can be played given current count
		 * @param playCount
		 * @return
		 */
		public boolean canPlay(int playCount) {
			boolean result = false;
			for (int i = 0; i < hand.length; i++) {
				if (!played[i] && hand[i].getRank() + playCount <= 31) {
					result = true;
				}
			}
			return result;
		}
		
		/**
		 * Marks card indicated by index card as played
		 * @param card
		 * @return
		 */
		public Card play(int card) {
			Card playedCard = hand[card];
			played[card] = true;
			cardsToPlay--;
			return playedCard;
		}
		
		/**
		 * Gets a list of indexes of playable cards in hand given playCount
		 * The indexes are from 0-5, representing the original 6 card hand
		 * @param playCount
		 * @return
		 */
		public int[] getPlayableCardIndexes(int playCount) {
			int[] playable = new int[cardsToPlay];
			int count = 0;
			for (int i = 0; i < 6; i++) {
				if (!played[i] && cribbageRank(hand[i]) + playCount <= 31) {
					playable[count++] = i;
				}
			}
			int[] legal = new int[count];
			int count2 = 0;
			for (int i = 0; i < count; i++) {
				if (playable[i] != 0) {
					legal[count2++] = playable[i];
				}
			}
			return legal;
		}
		
		/**
		 * Gets indexes of cards in player's hand that have been revealed/played
		 * @return int[] of size 1-3 with numbers in range 0-5
		 * @throws Exception if no cards have been revealed yet
		 */
		public int[] getPlayedCardIndexes() throws Exception {
			if (cardsToPlay >= 4) {
				throw new Exception("no cards revealed yet");
			}
			int[] playedIndexes = new int[4 - cardsToPlay];
			int j = 0;
			for (int i = 0; i < 6; i++) {
				if (hand[i] != null && played[i]) {
					playedIndexes[j++] = i;
				}
			}
			return playedIndexes;
		}
		
		/**
		 * Gets array of booleans indicating if a card has been played yet (true)
		 * @return
		 */
		public boolean[] getPlayed() {
			return played;
		}
		
		/**
		 * Gets player's score
		 * @return
		 */
		public int getScore() {
			return score;
		}
		
		/**
		 * Gets player's score earned for this hand only
		 * @return
		 */
		public int getHandScore() {
			return handScore;
		}
		
		/**
		 * Adds points to player's score
		 * @param points
		 */
		public void addPoints(int points) {
			score += points;
			handScore += points;
		}
		
		/**
		 * Gets all 6 cards of hand or 4 if THROW has happened.
		 * Won't give anything useful if no hand dealt.
		 * @return
		 */
		public Card[] getCurrentHand() {
			if (thrown) {
				Card[] inHand = new Card[4];
				int count = 0;
				for (int i = 0; i < 6; i++) {
					if (hand[i] != null) {
						inHand[count++] = hand[i];
					}
				}
				return inHand;
			}
			else {
				return hand;
			}
		}
		
		/**
		 * Gets full 6 card hand with null for cards that have been thrown.
		 * @return
		 */
		public Card[] getFullHand() {
			return hand;
		}
		
		/**
		 * Gets cards in player's hand that haven't been played yet.
		 * @return
		 */
		public Card[] getHandNotPlayed() {
			Card[] inHand = new Card[cardsToPlay];
			int count = 0;
			for (int i = 0; i < 6; i++) {
				if (!played[i]) {
					inHand[count++] = hand[i];
				}
			}
			return inHand;
		}
		
		
		/**
		 * Removes a card from hand (sets it to null), marks card as played
		 * @param card
		 * @return Card
		 */
		private Card remove(int card) {
			Card removedCard = hand[card];
			hand[card] = null;
			played[card] = true;
			cardsToPlay--;
			return removedCard;
		}
	}
	
	/**
	 * Makes random moves.
	 *
	 */
	public class RandomPlayer implements CribbagePlayer {
		public int getMove(CribbageState gameState) throws Exception {
			int[] moves = gameState.getActions();
			Random rand = new Random();
			return moves[rand.nextInt(moves.length)];
		}
		
		public String toString() {
			return "Random Player";
		}
	}
}
