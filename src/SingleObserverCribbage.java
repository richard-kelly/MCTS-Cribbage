import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SingleObserverCribbage implements CribbagePlayer {

	private int player;
	private Random rand;
	private double Cp;
	private long maxTime;
	private long startTime;
	private int maxNodes;
	private int nodesExpanded;
	
	//In this version a single state is used rather than storing them in nodes
	private CribbageState determState;
	
	/**
	 * A Cribbage AI player that uses Single Observer-Information Set MCTS algorithm
	 * @param Cp coefficient that determines amount of exploration
	 * @param maxTime maximum amount of time in ms to spend on a move
	 * @param maxNodes maximum number of Nodes to expand
	 * At least one of maxTime or MaxNodes should be greater than zero.
	 * @throws Exception 
	 */
	public SingleObserverCribbage(double Cp, long maxTime, int maxNodes) throws Exception {
		rand = new Random();
		this.Cp = Cp;
		if (maxTime <= 0 && maxNodes <= 0) {
			throw new Exception("Invalid initialization: must have positive maxTime or maxNodes");
		}
		this.maxTime = maxTime * 1000000; //ms to ns
		this.maxNodes = maxNodes;
	}
	
	public int search(CribbageState state) throws Exception {
		startTime = System.nanoTime();
		nodesExpanded = 0;
		player = state.playerToMove();
		
		//create root node with current state
		//parent is null, availability count is 0 (never used for root), action is null (never used for root)
		Node node = new Node(state.playerToMove(), null, null, 0);
		
		while (!timeout()) {
			//create determinization for this playout
			determState = new CribbageState(state);
			determState.randomize(player);
			Node newNode = treePolicy(node);
			double[] delta = defaultPolicy(determState);
			backup(newNode, delta);
			nodesExpanded++;
		}
		return findActionFromCard(state, mostVisits(node).getAction());
	}
	
	/**
	 * Calculates weather computational or time budget is up
	 * @return boolean
	 */
	private boolean timeout() {
		if (maxTime > 0 && System.nanoTime() - startTime >= maxTime) {
			return true;
		}
		if (maxNodes > 0 && nodesExpanded >= maxNodes) {
			return true;
		}
		return false;
	}

	/**
	 * Descends tree: returns unexpanded node of tree if they exist and are compatible with this determinization
	 * otherwise descends further using modified UCB1 to choose
	 * @param n start node
	 * @return new expanded node or a previously visited terminal node
	 * @throws Exception 
	 */
	private Node treePolicy(Node n) throws Exception {
		while (!determState.handOver()) {
			if (fullyExpanded(n)) {
				n = bestChild(n, Cp);
				determState.applyAction(findActionFromCard(determState, n.getAction()));
			}
			else {
				n = expand(n);
				//action should be already applied in expand()
				return n; 
			}
		}
		return n;
	}
	
	/**
	 * Expands node
	 * @param n node to expand
	 * @return new child node of n
	 * @throws Exception
	 */
	private Node expand(Node n) throws Exception {
		int[] moves = determState.getActions();
		ArrayList<Integer> notTried = new ArrayList<Integer>();
		CardTuple tup = null;
		
		//cycle through possible actions and create their corresponding cardtuples
		for (int i = 0; i < moves.length; i++) {
			switch (determState.getStage()) {
			case DEAL:
				throw new Exception("Trying to expand node at DEAL stage");
			case THROW:
				Card[] cards = determState.getCardsThrownByIndex(determState.playerToMove(), moves[i]);
				tup = new CardTuple(cards[0], cards[1]);
				break;
			case CUT:
				//only one move is possible
				tup = new CardTuple(determState.getDeck().getCard(12));
				break;
			case PLAY:
				tup = new CardTuple(determState.getFullHand(determState.playerToMove())[moves[i]]);
				break;
			}
			//for each cardtuple, increment number in parent node's available count hashmap
			if (n.getChildrenAvailability().containsKey(tup)) {
				n.getChildrenAvailability().put(tup, n.getChildrenAvailability().get(tup) + 1);
			}
			else {
				n.getChildrenAvailability().put(tup, 1);
			}
			//also store actions not yet tried
			if (!n.getChildren().containsKey(tup)) {
				notTried.add(moves[i]);
			}			
		}
		
		//choose a random action/cardtuple and create a new node with that, using parent's value for availability for that child
		int action = notTried.get(rand.nextInt(notTried.size()));
		switch (determState.getStage()) {
		case DEAL:
			throw new Exception("Trying to expand node at DEAL stage");
		case THROW:
			Card[] cards = determState.getCardsThrownByIndex(determState.playerToMove(), action);
			tup = new CardTuple(cards[0], cards[1]);
			break;
		case CUT:
			//only one move is possible
			tup = new CardTuple(determState.getDeck().getCard(12));
			break;
		case PLAY:
			tup = new CardTuple(determState.getFullHand(determState.playerToMove())[action]);
			break;
		}
		//apply action and create new node
		determState.applyAction(action);
		Node newNode = new Node(determState.playerToMove(), tup, n, n.getChildrenAvailability().get(tup));
		n.addChild(tup, newNode);
		return newNode;
	}
	
	/**
	 * Plays out game from state s with all random moves
	 * @param s state to play from
	 * @return double value of this playout.
	 * @throws Exception
	 */
	private double[] defaultPolicy(CribbageState s) throws Exception {
		while (!s.handOver()) {
			int[] moves = s.getActions();
			s.applyAction(moves[rand.nextInt(moves.length)]);
		}
		double[] rewards = new double[2];
		rewards[0] = s.getHandPointDiff(0);
		rewards[1] = s.getHandPointDiff(1);
		return rewards;
	}
	
	/**
	 * Backs up a value delta to a node n's ancestors
	 * @param n Node
	 * @param delta double
	 */
	private void backup(Node n, double[] delta) {
		while (n != null) {
			n.backupVisit(delta);
			n = n.getParent();
		}
	}
	
	/**
	 * Returns best child to visit based on modified UCB1 algorithm
	 * This method also increments child availability count, which would normally be done on the way up in backup(),
	 * but can't be because of the SO-IS algorithm
	 * @param n node to find best child of
	 * @param c constant regulating amount of exploration
	 * @return the best Node to visit
	 * @throws Exception 
	 */
	private Node bestChild(Node n, double c) throws Exception {
		int player = n.getPlayer();
		Node maxChild = null;
		double max = Double.NEGATIVE_INFINITY;
		CardTuple tup = null;
		for (int action : determState.getActions()) {
			//get cardTuple representing the action
			switch (determState.getStage()) {
			case DEAL:
				throw new Exception("Trying to choose best child node at DEAL stage");
			case THROW:
				Card[] cards = determState.getCardsThrownByIndex(determState.playerToMove(), action);
				tup = new CardTuple(cards[0], cards[1]);
				break;
			case CUT:
				//only one move is possible
				tup = new CardTuple(determState.getDeck().getCard(12));
				break;
			case PLAY:
				tup = new CardTuple(determState.getFullHand(determState.playerToMove())[action]);
				break;
			}
			Node child = n.getChildren().get(tup);
			//increment availability:
			child.addAvailable();
			//get value
			double val = child.getReward(player) / child.getVisits() + c * Math.sqrt(2 * Math.log(child.getAvailability()) / child.getVisits());
			if (val > max) {
				max = val;
				maxChild = child;
			}
		}
		return maxChild;
	}
	
	/**
	 * Finds the child node with the most visits.
	 * Needed for choosing action to return after search is complete
	 * because of changes to bestChild for handling IS determinizations
	 * @param n
	 * @return
	 * @throws Exception
	 */
	private Node mostVisits(Node n) throws Exception {
		Node maxChild = null;
		double max = Double.NEGATIVE_INFINITY;
		for (Node child : n.getChildren().values()) {
			double val = child.getVisits();
			if (val > max) {
				max = val;
				maxChild = child;
			}
		}
		return maxChild;	
	}
	
	/**
	 * Compares possible moves from current state to children of a node
	 * and returns true if all the possible moves exist as child nodes of the node.
	 * Essentially fully expanded for this determinization, not necessarily fully expanded for all determinizations 
	 * @param n
	 * @return
	 * @throws Exception
	 */
	private boolean fullyExpanded(Node n) throws Exception {
		int[] moves;
		switch(determState.getStage()) {
		case DEAL:
			throw new Exception("Trying to count children at DEAL stage");
		case THROW:
			moves = determState.getActions();
			for (int i = 0; i < moves.length; i++) {
				Card[] cards = determState.getCardsThrownByIndex(determState.playerToMove(), moves[i]);
				if (!n.getChildren().containsKey(new CardTuple(cards[0], cards[1]))) {
					return false;
				}
			}
			return true;
		case CUT:
			if (n.getChildren().containsKey(new CardTuple(determState.getDeck().getCard(12)))) {
				return true;
			}
			return false;
		case PLAY:
			moves = determState.getActions();
			for (int i = 0; i < moves.length; i++) {
				Card card = determState.getFullHand(determState.playerToMove())[moves[i]];
				if (!n.getChildren().containsKey(new CardTuple(card))) {
					return false;
				}
			}
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * Because of early decisions to treat all actions in gamestate as integers that didn't really represent what the actions meant
	 * it's necessary for this algorithm to convert those integers into Card representations and then back into integers,
	 * which is what this method does. 
	 * @param state CribbageState
	 * @param actionTup CardTuple representing an action
	 * @return integer that the state will accept as an action that corresponds to the card(s) in actionTup
	 * @throws Exception
	 */
	private int findActionFromCard(CribbageState state, CardTuple actionTup) throws Exception {
		switch (state.getStage()) {
		case DEAL:
			throw new Exception("Trying to find action at DEAL stage");
		case THROW:
			for (int action : state.getActions()) {
				Card[] cards = state.getCardsThrownByIndex(state.playerToMove(), action);
				CardTuple tup = new CardTuple(cards[0], cards[1]);
				if (tup.equals(actionTup)) {
					return action;
				}
			}
			break;
		case CUT:
			//only one move is possible
			return 0;
		case PLAY:
			for (int action : state.getActions()) {
				CardTuple tup = new CardTuple(state.getFullHand(state.playerToMove())[action]);
				if (tup.equals(actionTup)) {
					return action;
				}
			}
			break;
		default:
			throw new Exception("Couldn't find action");
		}
		return -1;
	}
	
	/**
	 * Holds all info needed for a node in the tree
	 *
	 */
	private class Node {
		private int player;
		private CardTuple action;
		private double[] rewards;
		private int visits;
		private int available;
		private Node parent;
		private HashMap<CardTuple, Node> childNodes;
		private HashMap<CardTuple, Integer> childAvailabilityCounts; //for use until the child is actually created
		
		/**
		 * 
		 * @param playerToMove player to act at this state
		 * @param action action taken to get to this node
		 * @param parent parent Node
		 * @param availabilityCount number of times this node has already been available (including now)
		 * @throws Exception
		 */
		public Node(int playerToMove, CardTuple action, Node parent, int availabilityCount) throws Exception {
			visits = 0;
			rewards = new double[2];
			childNodes = new HashMap<CardTuple, Node>();
			childAvailabilityCounts = new HashMap<CardTuple, Integer>();
			
			this.player = playerToMove;
			this.parent = parent;
			this.action = action;
			available = availabilityCount;
		}
		
		public int getPlayer() {
			return player;
		}
		
		public void backupVisit(double[] payouts) {
			visits++;
			for (int i = 0; i < payouts.length; i++) {
				rewards[i] += payouts[i];
			}
		}
		
		public HashMap<CardTuple, Integer> getChildrenAvailability() {
			return childAvailabilityCounts;
		}
		
		public Node getParent() {
			return parent;
		}
		
		public CardTuple getAction() {
			return action;
		}
		
		public void addChild(CardTuple tup, Node child) {
			childNodes.put(tup, child);
		}
		
		public int getVisits() {
			return visits;
		}
		
		public void addAvailable() {
			available++;
		}
		
		public int getAvailability() {
			return available;
		}
		
		public double getReward(int player) {
			if (player == -1) {
				return 0.0;
			}
			return rewards[player];
		}
		
		public HashMap<CardTuple, Node> getChildren() {
			return childNodes;
		}
		
	}

	/**
	 * This method calls the search unless the move is just an option of one action
	 */
	public int getMove(CribbageState gameState) throws Exception {
		int[] moves = gameState.getActions();
		if (moves.length == 1) {
			return moves[0];
		}
		else {
			return search(gameState);
		}
	}
	
	/**
	 * Holds up to two Cards, Equality is defined as consisting of same cards, but order doesn't matter
	 * This class is necessary because the CribbageState class treats actions as meaningless integers, more or less
	 * The SO-IS MCTS algorithm really needs more meaningful representations of actions
	 * (that are distinguishable across different determinizations), and being able to hash those representations is very helpful
	 * Also sometimes an action is a single card played, while other times an action is two cards played simultaneously,
	 * so we need one OR two cards in a tuple.
	 */
	private class CardTuple {
		private Card first;
		private Card second;
		
		public CardTuple(Card first) {
			this.first = first;
			second = null;
		}
		
		public CardTuple(Card a, Card b) {
			//set first to the smaller card
			if (a.compareTo(b) > 0) {
				first = b;
				second = a;
			}
			else {
				first = a;
				second = b;
			}
		}
		
		public boolean equals(Object o) {
			CardTuple other = (CardTuple) o;
			if (other == null) {
				return false;
			}
			if (second == null) {
				if (other.second == null) {
					if (first.equals(other.first)) {
						return true;
					}
					return false;
				}
				return false;
			}
			else if ((first.equals(other.first) && second.equals(other.second)) || (first.equals(other.second) && second.equals(other.first)) ) {
				return true;
			}
			return false;
		}
		
		public int hashCode() {
			int result = 1;
			int p = 37;
			result = p * result + first.getRank();
			result = p * result + first.getSuit().ordinal() + 1;
			if (second != null) {
				result = p * result + second.getRank();
				result = p * result + second.getSuit().ordinal() + 1;
			}
			return result;
		}
		
	}
	
	public String toString() {
		return "SO-ISMCTS (" + Cp + ", " + (maxTime / 1000000) + ", " + maxNodes + ")";
	}
}
