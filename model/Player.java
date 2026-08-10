/*
 * ============================================================================
 * File:        Player.java
 * Package:     model
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Abstract base class representing a generic player in the checkers
 *              game system. Establishes the polymorphism contract for human and
 *              computer AI participants.
 *
 * DSA Concepts Applied:
 *   - Intro To DSA (Intro To DSA.pptx): Abstraction and polymorphic interfaces
 *     allowing interchangeable AI and human move controllers in the game loop.
 * ============================================================================
 */

package model;

/**
 * The {@code Player} class is an abstract base class representing an active
 * participant in a game of checkers. Subclasses define specific control strategies
 * (e.g. human UI input vs. computer AI algorithms).
 */
public abstract class Player {

	/**
	 * Determines if this player requires human UI interactions.
	 * 
	 * @return true if human-controlled via GUI mouse input; false for AI controllers
	 */
	public abstract boolean isHuman();
	
	/**
	 * Updates the game state by executing a move for the current player turn.
	 * Human players implement this as a no-op since moves are driven by UI clicks.
	 * AI players implement search algorithms (Minimax / Alpha-Beta) inside this method.
	 * 
	 * @param game the game state to evaluate and update
	 */
	public abstract void updateGame(Game game);
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[isHuman=" + isHuman() + "]";
	}
}
