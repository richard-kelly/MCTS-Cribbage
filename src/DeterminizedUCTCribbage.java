public class DeterminizedUCTCribbage implements CribbagePlayer {

	private int player;
	private double Cp;
	private long maxTime;
	private long startTime;
	private int maxNodes;
	private int nodesExpanded;
	private int dets;
	
	/**
	 * A Cribbage AI player that uses Determinized UCT to choose a move
	 * @param Cp coefficient that determines amount of exploration
	 * @param maxTime maximum amount of time in ms to spend on a move
	 * @param maxNodes maximum number of Nodes to expand
	 * @param dets int: number of determinizations to divide search into
	 * At least one of maxTime or MaxNodes should be greater than zero.
	 * @throws Exception 
	 */
	public DeterminizedUCTCribbage(double Cp, long maxTime, int maxNodes, int dets) throws Exception {
		this.Cp = Cp;
		if (maxTime <= 0 && maxNodes <= 0) {
			throw new Exception("Invalid initialization of UCTCribbage: must have positive maxTime or maxNodes");
		}
		this.maxTime = maxTime * 1000000; //ms to ns
		this.maxNodes = maxNodes;
		this.dets = dets;
	}
	
	/**
	 * Chooses a move using Cheating UCT agent over a number of determinizations.
	 * Known Bug: if the number of determinizations is larger than the number of ms,
	 * it breaks because of integer division somewhere. Not hard to fix...
	 * @param state
	 * @return
	 * @throws Exception
	 */
	public int search(CribbageState state) throws Exception {
		startTime = System.nanoTime();
		nodesExpanded = 0;
		player = state.playerToMove();
		int[] actions = state.getActions();
		int[] totalVisits = new int[actions.length];
		long msMaxTime = maxTime / 1000000 / dets;
		int nodes = maxNodes / dets;
		
		//divide processing time between determinized MCTS trees
		while (!timeout()) {
			CheatingUCTCribbage p = new CheatingUCTCribbage(Cp, msMaxTime, nodes);
			nodesExpanded += nodes;
			CribbageState newState = new CribbageState(state);
			newState.randomize(player);
			
			//all visit counts for children of root node are summed
			int[] visits = p.searchAndReturnVisitCounts(newState);
			for (int i = 0; i < visits.length; i++) {
				totalVisits[i] += visits[i];
			}
		}
		
		//return action with most visits across all trees 
		int max = 0;
		for (int i = 1; i < actions.length; i++) {
			if (totalVisits[i] > totalVisits[i - 1]) {
				max = i;
			}
		}
		return actions[max];
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

	@Override
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
		return "Determinized UCT (" + Cp + ", " + (maxTime / 1000000) + ", " + maxNodes + ", " + dets + ")";
	}
}
