import java.util.ArrayList;
import java.util.Random;

public class CheatingUCTCribbage implements CribbagePlayer {

	private Random rand;
	private double Cp;
	private long maxTime;
	private long startTime;
	private int maxNodes;
	private int nodesExpanded;
	
	/**
	 * A Cribbage AI player that uses UCT to choose a move
	 * Cheating because it knows all the cards in the deck and other player's hand
	 * @param Cp coefficient that determines amount of exploration
	 * @param maxTime maximum amount of time in ms to spend on a move
	 * @param maxNodes maximum number of Nodes to expand
	 * At least one of maxTime or MaxNodes should be greater than zero.
	 * @throws Exception 
	 */
	public CheatingUCTCribbage(double Cp, long maxTime, int maxNodes) throws Exception {
		rand = new Random();
		this.Cp = Cp;
		if (maxTime <= 0 && maxNodes <= 0) {
			throw new Exception("Invalid initialization of UCTCribbage: must have positive maxTime or maxNodes");
		}
		this.maxTime = maxTime * 1000000; //ms to ns
		this.maxNodes = maxNodes;
	}
	
	/**
	 * Same as normal search but returns visit counts of children of root node
	 * instead of an action. Used by Determinized UCT
	 * @param state
	 * @return int[] visit counts
	 * @throws Exception
	 */
	public int[] searchAndReturnVisitCounts(CribbageState state) throws Exception {
		startTime = System.nanoTime();
		nodesExpanded = 0;
		
		//root node
		Node node = new Node(state, -1, null);
		
		while (!timeout()) {
			Node newNode = treePolicy(node);
			double[] delta = defaultPolicy(new CribbageState(newNode.getState()));
			backup(newNode, delta);
			nodesExpanded++;
		}
		int[] visits = new int[state.getActions().length];
		int i = 0;
		for (Node n : node.getChildren()) {
			visits[i++] = n.visits;
		}
		return visits;
	}
	
	public int search(CribbageState state) throws Exception {
		startTime = System.nanoTime();
		nodesExpanded = 0;
		
		//root node
		Node node = new Node(state, -1, null);
		
		while (!timeout()) {
			Node newNode = treePolicy(node);
			double[] delta = defaultPolicy(new CribbageState(newNode.getState()));
			backup(newNode, delta);
			nodesExpanded++;
		}
		return bestChild(node, 0).getAction();
	}
	
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
	 * Descends tree: returns unexpanded node of tree if they exist,
	 * otherwise descends further using UCB to choose
	 * @param n start node
	 * @return new expanded node or a previously visited terminal node
	 * @throws Exception 
	 */
	private Node treePolicy(Node n) throws Exception {
		while (!n.getState().handOver()) {
			if (n.isFullyExpanded()) {
				n = bestChild(n, Cp);
			}
			else {
				return expand(n);
			}
		}
		return n;
	}
	
	/**
	 * Expands node as if this is a perfect information game
	 * @param n node to expand
	 * @return new child node of n
	 * @throws Exception
	 */
	private Node expand(Node n) throws Exception {
		Node newNode = null;
		int[] moves = n.getState().getActions();
		int action = moves[n.getChildren().size()];
		CribbageState newState = new CribbageState(n.getState());
		newState.applyAction(action);
		newNode = new Node(newState, action, n);
		n.addChild(newNode);
		//check for fully expanded
		if (n.getChildren().size() == moves.length) {
			n.setFullyExpanded();
		}
		
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
	 * Returns best child to visit based on UCB algorithm
	 * @param n node to find best child of
	 * @param c constant regulating amount of exploration
	 * @return the best Node to visit
	 * @throws Exception 
	 */
	private Node bestChild(Node n, double c) throws Exception {
		int player = n.getState().playerToMove();
		Node maxChild = null;
		double max = Double.NEGATIVE_INFINITY;
		for (Node child : n.getChildren()) {
			double val = child.getReward(player) / child.getVisits() + c * Math.sqrt(2 * Math.log(n.getVisits()) / child.getVisits());
			if (val > max) {
				max = val;
				maxChild = child;
			}
		}
		return maxChild;	
	}

	
	private class Node {
		private CribbageState state;
		private int action;
		private double[] rewards;
		private int visits;
		private Node parent;
		private ArrayList<Node> childNodes;
		private boolean fullyExpanded;
		
		public Node(CribbageState state, int action, Node parent) throws Exception {
			visits = 0;
			rewards = new double[2];
			childNodes = new ArrayList<Node>();
			
			this.state = state;
			this.parent = parent;
			this.action = action;
			this.fullyExpanded = false;
		}
		
		public CribbageState getState() {
			return state;
		}
		
		public void backupVisit(double[] payouts) {
			visits++;
			for (int i = 0; i < payouts.length; i++) {
				rewards[i] += payouts[i];
			}
		}
		
		public Node getParent() {
			return parent;
		}
		
		public int getAction() {
			return action;
		}
		
		public void addChild(Node child) {
			childNodes.add(child);
		}
		
		public int getVisits() {
			return visits;
		}
		
		public double getReward(int player) {
			if (player == -1) {
				return 0.0;
			}
			return rewards[player];
		}
		
		public ArrayList<Node> getChildren() {
			return childNodes;
		}
		
		public boolean isFullyExpanded() {
			return fullyExpanded;
		}
		
		public void setFullyExpanded() {
			fullyExpanded = true;
		}
	}

	public int getMove(CribbageState gameState) throws Exception {
		int[] moves = gameState.getActions();
		if (moves.length == 1) {
			return moves[0];
		}
		else {
			return search(gameState);
		}
	}
	
	public String toString() {
		return "Cheating UCT (" + Cp + ", " + (maxTime / 1000000) + ", " + maxNodes + ")";
	}
}
