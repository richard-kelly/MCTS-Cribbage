import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JToggleButton;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class CribbageGUI implements CribbageUpdateable {

	private CribbageGUI theApp = this;
	private CribbagePlayer[] players;
	private JToggleButton[][] playerButtons;
	private JToggleButton[] cribButtons;
	private JButton[] actionButtons;
	
	private JFrame frame;
	private JTextField p1Score;
	private JTextField p2Score;
	private JTextArea scoreLog;
	
	private CribbageState state; 
	private JPanel optionsPanel;
	private JLabel optionsInfoLabel;
	private JLabel p1Label;
	private JComboBox p1Combo;
	private JLabel p2Label;
	private JComboBox p2Combo;
	private JButton newGameButton;
	private JPanel scorePanel;
	private Component horizontalStrut;
	private Component horizontalStrut_1;
	private JPanel p1HandPanel;
	private JToggleButton p1Card0Button;
	private JToggleButton p1Card1Button;
	private JToggleButton p1Card2Button;
	private JToggleButton p1Card3Button;
	private JToggleButton p1Card4Button;
	private JToggleButton p1Card5Button;
	private JPanel p2HandPanel;
	private JToggleButton p2Card0Button;
	private JToggleButton p2Card1Button;
	private JToggleButton p2Card2Button;
	private JToggleButton p2Card3Button;
	private JToggleButton p2Card4Button;
	private JToggleButton p2Card5Button;
	private JPanel gameTablePanel;
	private JPanel deckPanel;
	private JLabel lblCut;
	private JToggleButton cutButton;
	private JPanel cribArea;
	private JLabel lblCrib;
	private JToggleButton crib0Button;
	private JToggleButton crib1Button;
	private JToggleButton crib2Button;
	private JToggleButton crib3Button;
	private JButton nextHandButton;
	private JButton p1ActionButton;
	private JButton p2ActionButton;
	private JScrollPane scrollPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CribbageGUI window = new CribbageGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CribbageGUI() {
		initialize();
		playerButtons = new JToggleButton[2][6];
		cribButtons = new JToggleButton[4];
		actionButtons = new JButton[2];
		
		p1ActionButton = new JButton("P1 Action");
		p1ActionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				applyHumanAction(0);
			}
		});
		p1HandPanel.add(p1ActionButton);
		
		p2ActionButton = new JButton("P2 Action");
		p2ActionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				applyHumanAction(1);
			}
		});
		p2HandPanel.add(p2ActionButton);
		
		playerButtons[0][0] = p1Card0Button;
		playerButtons[0][1] = p1Card1Button;
		playerButtons[0][2] = p1Card2Button;
		playerButtons[0][3] = p1Card3Button;
		playerButtons[0][4] = p1Card4Button;
		playerButtons[0][5] = p1Card5Button;
		playerButtons[1][0] = p2Card0Button;
		playerButtons[1][1] = p2Card1Button;
		playerButtons[1][2] = p2Card2Button;
		playerButtons[1][3] = p2Card3Button;
		playerButtons[1][4] = p2Card4Button;
		playerButtons[1][5] = p2Card5Button;
		
		cribButtons[0] = crib0Button;
		cribButtons[1] = crib1Button;
		cribButtons[2] = crib2Button;
		cribButtons[3] = crib3Button;
		
		actionButtons[0] = p1ActionButton;
		actionButtons[1] = p2ActionButton;
		
		for (JToggleButton[] player : playerButtons) {
			for (JToggleButton b : player) {
				b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 40));
			}
		}
		for (JToggleButton b : cribButtons) {
			b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 40));
		}
		cutButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 40));
		
		p1ActionButton.setEnabled(false);
		p2ActionButton.setEnabled(false);
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 750, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		optionsPanel = new JPanel();
		frame.getContentPane().add(optionsPanel, BorderLayout.NORTH);
		
		optionsInfoLabel = new JLabel("(Player 1 Deals First)");
		optionsPanel.add(optionsInfoLabel);
		
		p1Label = new JLabel("Player 1:");
		optionsPanel.add(p1Label);
		
		p1Combo = new JComboBox();
		p1Combo.setModel(new DefaultComboBoxModel(new String[] {"Human", "Random", "Scripted", "CheatingUCT (1 second)", "DeterminizedUCT (1 second)", "SO-IS-MCTS (1 second)"}));
		optionsPanel.add(p1Combo);
		
		p2Label = new JLabel("Player 2:");
		optionsPanel.add(p2Label);
		
		p2Combo = new JComboBox();
		p2Combo.setModel(new DefaultComboBoxModel(new String[] {"Human", "Random", "Scripted", "CheatingUCT (1 second)", "DeterminizedUCT (1 second)", "SO-IS-MCTS (1 second)"}));
		optionsPanel.add(p2Combo);
		
		newGameButton = new JButton("New Game");
		newGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state = new CribbageState();
				//register this object to receive updates
				state.registerForUpdates(theApp);
				players = new CribbagePlayer[2];
				try {
					players[0] = getPlayer(p1Combo.getSelectedIndex());
					players[1] = getPlayer(p2Combo.getSelectedIndex());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				scoreLog.setText("");
				p2Score.setText("0");
				p1Score.setText("0");
				resetHand();
				nextHandButton.setEnabled(true);
			}
		});
		optionsPanel.add(newGameButton);
		
		scorePanel = new JPanel();
		frame.getContentPane().add(scorePanel, BorderLayout.SOUTH);
		
		p1Score = new JTextField();
		p1Score.setEditable(false);
		p1Score.setForeground(Color.RED);
		p1Score.setText("0");
		p1Score.setFont(new Font("Century Gothic", Font.BOLD, 30));
		scorePanel.add(p1Score);
		p1Score.setColumns(3);
		
		horizontalStrut = Box.createHorizontalStrut(20);
		scorePanel.add(horizontalStrut);
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scorePanel.add(scrollPane);
		
		scoreLog = new JTextArea();
		scrollPane.setViewportView(scoreLog);
		scoreLog.setColumns(40);
		scoreLog.setTabSize(4);
		scoreLog.setRows(5);
		scoreLog.setWrapStyleWord(true);
		scoreLog.setEditable(false);
		
		horizontalStrut_1 = Box.createHorizontalStrut(20);
		scorePanel.add(horizontalStrut_1);
		
		p2Score = new JTextField();
		p2Score.setEditable(false);
		p2Score.setText("0");
		p2Score.setForeground(Color.RED);
		p2Score.setFont(new Font("Century Gothic", Font.BOLD, 30));
		scorePanel.add(p2Score);
		p2Score.setColumns(3);
		
		p1HandPanel = new JPanel();
		frame.getContentPane().add(p1HandPanel, BorderLayout.WEST);
		p1HandPanel.setLayout(new GridLayout(7, 1, 0, 0));
		
		p1Card0Button = new JToggleButton("---");
		p1HandPanel.add(p1Card0Button);
		
		p1Card1Button = new JToggleButton("---");
		p1HandPanel.add(p1Card1Button);
		
		p1Card2Button = new JToggleButton("---");
		p1HandPanel.add(p1Card2Button);
		
		p1Card3Button = new JToggleButton("---");
		p1HandPanel.add(p1Card3Button);
		
		p1Card4Button = new JToggleButton("---");
		p1HandPanel.add(p1Card4Button);
		
		p1Card5Button = new JToggleButton("---");
		p1HandPanel.add(p1Card5Button);
		
		p2HandPanel = new JPanel();
		frame.getContentPane().add(p2HandPanel, BorderLayout.EAST);
		p2HandPanel.setLayout(new GridLayout(7, 1, 0, 0));
		
		p2Card0Button = new JToggleButton("---");
		p2HandPanel.add(p2Card0Button);
		
		p2Card1Button = new JToggleButton("---");
		p2HandPanel.add(p2Card1Button);
		
		p2Card2Button = new JToggleButton("---");
		p2HandPanel.add(p2Card2Button);
		
		p2Card3Button = new JToggleButton("---");
		p2HandPanel.add(p2Card3Button);
		
		p2Card4Button = new JToggleButton("---");
		p2HandPanel.add(p2Card4Button);
		
		p2Card5Button = new JToggleButton("---");
		p2HandPanel.add(p2Card5Button);
		
		gameTablePanel = new JPanel();
		frame.getContentPane().add(gameTablePanel, BorderLayout.CENTER);
		gameTablePanel.setLayout(new BorderLayout(0, 0));
		
		deckPanel = new JPanel();
		deckPanel.setBackground(new Color(0, 100, 0));
		gameTablePanel.add(deckPanel, BorderLayout.NORTH);
		
		lblCut = new JLabel("Cut:");
		lblCut.setFont(new Font("Century Gothic", Font.BOLD, 25));
		lblCut.setForeground(new Color(255, 255, 0));
		deckPanel.add(lblCut);
		
		cutButton = new JToggleButton("???");
		cutButton.setBackground(Color.WHITE);
		cutButton.setEnabled(true);
		deckPanel.add(cutButton);
		
		cribArea = new JPanel();
		cribArea.setBackground(new Color(0, 100, 0));
		gameTablePanel.add(cribArea, BorderLayout.CENTER);
		cribArea.setLayout(null);
		
		crib0Button = new JToggleButton("---");
		crib0Button.setVisible(false);
		crib0Button.setBounds(159, 58, 132, 63);
		cribArea.add(crib0Button);
		
		crib1Button = new JToggleButton("---");
		crib1Button.setVisible(false);
		crib1Button.setBounds(301, 58, 132, 63);
		cribArea.add(crib1Button);
		
		crib2Button = new JToggleButton("---");
		crib2Button.setVisible(false);
		crib2Button.setBounds(159, 132, 132, 63);
		cribArea.add(crib2Button);
		
		crib3Button = new JToggleButton("---");
		crib3Button.setVisible(false);
		crib3Button.setBounds(301, 132, 132, 63);
		crib3Button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 40));
		cribArea.add(crib3Button);
		
		nextHandButton = new JButton("Next Hand");
		nextHandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//dealing new hand
					nextHandButton.setEnabled(false);
					resetHand();
					advanceGame();
					updateHands();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		nextHandButton.setBounds(159, 217, 274, 23);
		nextHandButton.setEnabled(false);
		cribArea.add(nextHandButton);
		
		lblCrib = new JLabel("Crib");
		lblCrib.setBounds(268, 11, 51, 32);
		cribArea.add(lblCrib);
		lblCrib.setForeground(new Color(255, 255, 0));
		lblCrib.setFont(new Font("Century Gothic", Font.BOLD, 25));
	}
	
	/**
	 * Formats a JToggleButton to display the text of a card in the right colour
	 * @param c the Card to display
	 * @param b the JToggleButton to format
	 */
	private void attachCard(Card c, JToggleButton b) {
		//b.setText(cardToSymbol(c));
		b.setText(c.toString());
		switch (c.getSuit()) {
		case S:
			b.setForeground(Color.BLACK);
			break;
		case C:
			b.setForeground(Color.BLACK);
			break;
		case D:
			b.setForeground(Color.RED);
			break;
		case H:
			b.setForeground(Color.RED);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Prints messages from the game state about the game play to the console and a text box in the GUI
	 */
	public void receiveUpdate(CribbageEvent type, int player, int points) {
		String message = "";
		switch (type) {
		case DEAL:
			message += "------------------------";
			message += "New Hand. Player 1: " + state.getScore(0) + ", Player 2: " + state.getScore(1) + ".";
			message += " Dealer is Player " + (state.getDealer() + 1) + ".";
			break;
		case THROW:
			message += "Player " + (player + 1) + " threw two cards.";
			break;
		case CUT:
			message += "Deck is cut: " + state.getCut().toString() + ".";
			if (points != 0) {
				message += " Dealer (Player " + (state.getDealer() + 1) + ") gets 2 points.";
			}
			break;
		case PLAY:
			message += "Player " + (player + 1) + " plays " + state.getLastCardPlayed().toString() + " for " + state.getPlayCount() + "."; 
			break;
		case FIFTEEN:
			message += "Player " + (player + 1) + " scores fifteen for 2. Score: " + state.getScore(player) + ".";
			break;
		case THIRTYONE:
			message += "Player " + (player + 1) + " scores thirty one for 2. Score: " + state.getScore(player) + ".";
			break;
		case PAIR:
			if (points == 2) {
				message += "Player " + (player + 1) + " scores a pair for 2. Score: " + state.getScore(player) + ".";
			}
			else if (points == 6) {
				message += "Player " + (player + 1) + " scores three of a kind for 6. Score: " + state.getScore(player) + ".";
			}
			else {
				message += "Player " + (player + 1) + " scores four of a kind for 6. Score: " + state.getScore(player) + ".";
			}
			break;
		case RUN:
			message += "Player " + (player + 1) + " scores a run of " + points + " for " + points + " points. Score: " + state.getScore(player) + ".";
			break;
		case GO:
			message += "Player " + (player + 1) + ": Go.";
			break;
		case LAST:
			message += "Player " + (player + 1) + " scores one for last. Score: " + state.getScore(player) + ".";
			break;
		case SHOW:
			message += "Play is finished.";
			break;
		case COUNT:
			message += "Player " + (player + 1) + " scores " + points + " with " + cardsToString(state.getCurrentHand(player)) + "and cut " + state.getCut().toString() + ".";
			break;
		case CRIB:
			message += "Player " + (player + 1) + " scores " + points + " on the crib with " + cardsToString(state.getCrib()) + "and cut " + state.getCut().toString() + ".";
			break;
		case WIN:
			message += "Player " + (player + 1) + " wins " + state.getScore(player) + " to " + state.getScore((player + 1) % 2) + ".";
			break;
		default:
			break;
		}
		System.out.println(message);
		scoreLog.setText(scoreLog.getText() + "\n" + message);
		p1Score.setText("" + state.getScore(0));
		p2Score.setText("" + state.getScore(1));
	}
	
	/**
	 * Prints an array of cards in a nice way
	 * @param cards
	 * @return
	 */
	private String cardsToString(Card[] cards) {
		String s = "";
		for (int i = 0; i < cards.length; i++) {
			s += cards[i].toString() + " ";
		}
		return s;
	}
	
	/**
	 * Resets the view of the player hands for when there's no cards dealt
	 */
	private void resetHand() {
		for (JToggleButton[] player : playerButtons) {
			for (JToggleButton b : player) {
				b.setText("---");
				b.setEnabled(false);
			}
		}
		for (JToggleButton b : cribButtons) {
			b.setText("---");
			b.setEnabled(false);
		}
		cutButton.setText("???");
		cutButton.setForeground(Color.BLACK);
		
		for (int i = 0; i < actionButtons.length; i++) {
			actionButtons[i].setEnabled(false);
		}
	}
	
	/**
	 * Updates the vie of the player hands
	 * @throws Exception
	 */
	private void updateHands() throws Exception {
		for (int player = 0; player < players.length; player++ ) {
			Card[] cards = state.getFullHand(player);
			boolean[] played = state.getPlayed(player);
			
			if (players[player] == null) {				 
				for (int i = 0; i < 6; i++) {
					if (cards[i] == null) {
						playerButtons[player][i].setText("---");
						playerButtons[player][i].setForeground(Color.BLACK);
						playerButtons[player][i].setEnabled(false);
					}
					else {
						attachCard(cards[i], playerButtons[player][i]);
						if (played[i]) {
							playerButtons[player][i].setEnabled(false);
						}
						else {
							playerButtons[player][i].setEnabled(true);
						}
					}
					playerButtons[player][i].setSelected(false);
				}
			}
			else {
				for (int i = 0; i < 6; i++) {
					if (cards[i] == null) {
						playerButtons[player][i].setText("---");
						playerButtons[player][i].setForeground(Color.BLACK);
					}
					else if (played[i]) {
						attachCard(cards[i], playerButtons[player][i]);
						playerButtons[player][i].setEnabled(false);
					}
					else {
						playerButtons[player][i].setText("???");
						playerButtons[player][i].setForeground(Color.BLACK);
					}
				}
			}
		}
	}
	
	/**
	 * Advances the game when the AI players have to act or when the game has to act
	 * @throws Exception
	 */
	private void advanceGame() throws Exception {
		switch (state.getStage()) {
		case DEAL:
			state.applyAction(0);
			advanceGame();
			break;
		case THROW:
			if (players[state.playerToMove()] == null) {
				//human player
				actionButtons[state.playerToMove()].setText("Throw 2 Cards");
				actionButtons[state.playerToMove()].setEnabled(true);
			}
			else {
				state.applyAction(players[state.playerToMove()].getMove(state));
				advanceGame();
			}
			break;
		case CUT:
			state.applyAction(0);
			attachCard(state.getCut(), cutButton);
			advanceGame();
			break;
		case PLAY:
			if (players[state.playerToMove()] == null) {
				//human player
				actionButtons[state.playerToMove()].setText("Play Card");
				actionButtons[state.playerToMove()].setEnabled(true);
			}
			else {
				state.applyAction(players[state.playerToMove()].getMove(state));
				updateHands();
				if (state.getWinner() != -1) {
					//game over
					nextHandButton.setEnabled(false);
				}
				else if (!state.handOver()) {
					//only advance game if hand not over.
					//when hand is over wait for human to press new hand button
					advanceGame();
				}
				else {
					Card[] crib = state.getCrib();
					for (int i = 0; i < 4; i++) {
						attachCard(crib[i], cribButtons[i]);
						cribButtons[i].setVisible(true);
						cribButtons[i].setEnabled(true);
					}
					nextHandButton.setEnabled(true);
				}
				
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * List of AI players
	 * 
	 */
	private CribbagePlayer getPlayer(int i) throws Exception {
		switch(i) {
		case 0:
			return null;
		case 1:
			return state.new RandomPlayer();
		case 2:
			return new ScriptedCribbagePlayer();
		case 3:
			return new CheatingUCTCribbage(1.75, 1000, 0);
		case 4:
			return new DeterminizedUCTCribbage(1.75, 1000, 0, 500);
		case 5:
			return new SingleObserverCribbage(2.0, 1000, 0);
		default:
			return null;
		}
	}
	
	/**
	 * Converts two integers representing cards in hand to a "throw index" recognizable by the Cribbage State
	 * @param a int
	 * @param b int
	 * @return int 0-14
	 */
	private int getThrowIndex(int a, int b) {
		switch (a) {
		case 0:
			return b - 1;
		case 1:
			return b + 3;
		case 2:
			return b + 6;
		case 3:
			return b + 8;
		case 4:
			return 14;
		default:
			return -1; // cause an error
		}
	}
	
	/**
	 * Prompts a human player to take an action
	 * @param player int the player to act
	 */
	private void applyHumanAction(int player) {
		if (players[player] == null && state.playerToMove() == player) {
			if (state.getStage() == CribbageStage.THROW) {
				//check for exactly two cards selected
				int count = 0;
				int[] toThrow = new int[2];
				for (int i = 0; i < playerButtons[player].length; i++) {
					if (playerButtons[player][i].isSelected()) {
						if (count < 2) {
							toThrow[count++] = i;
						}
						else {
							count++;
						}
					}
				}
				if (count != 2) {
					scoreLog.setText(scoreLog.getText() + "\nPlease select exactly two cards to throw");
				}
				else {
					//calculate throw index and apply action
					try {
						state.applyAction(getThrowIndex(toThrow[0], toThrow[1]));
						actionButtons[player].setEnabled(false);
						updateHands();
						advanceGame();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else if (state.getStage() == CribbageStage.PLAY) {
				//check for exactly one card selected
				int count = 0;
				int toThrow = 0;
				for (int i = 0; i < playerButtons[player].length; i++) {
					if (playerButtons[player][i].isSelected()) {
						if (count < 1) {
							toThrow = i;
							count++;
						}
						else {
							count++;
						}
					}
				}
				if (count != 1) {
					scoreLog.setText(scoreLog.getText() + "\nPlease select one card to throw");
				}
				else {
					//apply action
					try {
						state.applyAction(toThrow);
						actionButtons[player].setEnabled(false);
						updateHands();
						if (state.getWinner() != -1) {
							nextHandButton.setEnabled(false);
						}
						else if (!state.handOver()) {
							//only advance game if hand not over.
							//when hand is over wait for human to press new hand button
							advanceGame();
						}
						else {
							nextHandButton.setEnabled(true);
							Card[] crib = state.getCrib();
							for (int i = 0; i < 4; i++) {
								attachCard(crib[i], cribButtons[i]);
								cribButtons[i].setVisible(true);
								cribButtons[i].setEnabled(true);
							}
						}
						
						
					} catch (Exception e) {
						scoreLog.setText(scoreLog.getText() + "\nIllegal Play. Play a different card.");
						e.printStackTrace();
					}
				}
			}
		}
	}
}
