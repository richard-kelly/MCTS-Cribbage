# MCTS-Cribbage
This project contains a few MCTS implementations for 2-player Cribbage, originally created for my CS honours project in winter 2017. This might be useful to anyone who wants to see an MCTS implementation, and specifically the Information Set MCTS, which works quite well for this game.

Information Set MCTS algorithm adapted from:

P. I. Cowling, E. J. Powley, and D. Whitehouse, “Information Set Monte Carlo Tree Search,” *IEEE Transactions on Computational Intelligence and AI in Games*, vol. 4, no. 2, pp. 120–143, 2012.

## Instructions
Compile and run CribbageGUI.java to play against the AI players with a really simple GUI.

Edit, compile, and run Cribbage.java to run a series of games with different parameters.

## Updates
When I have time I'd like to move the parameters for the testing functionality into a JSON file. I'd also like to completely separate the game code from the MCTS code, so that the MCTS classes could be more easily reused for other projects. I started with that in mind, which is apparent when you look at some of the interfaces used, but some early choices I made when designing the Cribbage module made that difficult.

## MCTS Players

Included in this repo are:
* Random Player
* Scripted player - Throws based on top 4-card hand, and otherwise greedily takes points.
* Cheating UCT - MCTS with knowledge of upcoming cards in deck and opponent's cards.
* Determinized UCT player - Runs individual MCTS on a set number of determinizations. Takes most visited action summing from all determinizations.
* SOIS-MCTS - Single Observer-Information Set MCTS builds one tree using a different determinization in each traversal.

All MCTS-based players search until the end of the current hand, including the cheating player.

Budgets can be set as numer of traversals or a time limit.
