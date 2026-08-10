/*
 * ============================================================================
 * File:        HumanPlayer.java
 * Package:     model
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Subclass of Player representing a human user interacting with the GUI.
 *
 * DSA Concepts Applied:
 *   - Intro To DSA (Intro To DSA.pptx): Concrete implementation of abstract
 *     Player interface where game updates are delegating to GUI event dispatchers.
 * ============================================================================
 */

package model;

/**
 * The {@code HumanPlayer} class represents a human user playing draughts.
 * Move execution for human players is handled asynchronously via GUI click events.
 */
public class HumanPlayer extends Player {

	/**
	 * {@inheritDoc}
	 * 
	 * @return true because this instance is controlled by a human user via UI.
	 */
	@Override
	public boolean isHuman() {
		return true;
	}

	/**
	 * No-op implementation for human players because game state updates
	 * are triggered directly by mouse clicks on the {@code CheckerBoard} GUI.
	 * 
	 * @param game the current game instance
	 */
	@Override
	public void updateGame(Game game) {
		// Human player moves are driven by GUI events in CheckerBoard
	}
}
