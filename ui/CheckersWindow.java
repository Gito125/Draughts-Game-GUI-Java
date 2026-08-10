/*
 * ============================================================================
 * File:        CheckersWindow.java
 * Package:     ui
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Top-level Swing JFrame window hosting the CheckerBoard display
 *              component and OptionPanel control panel.
 *
 * DSA Concepts Applied:
 *   - Intro To DSA (Intro To DSA.pptx): Software design modularity, separating
 *     GUI layout components from core model state and logic engines.
 * ============================================================================
 */

package ui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import model.Player;

/**
 * The {@code CheckersWindow} class manages the main application GUI window.
 */
public class CheckersWindow extends JFrame {

	private static final long serialVersionUID = 8782122389400590079L;
	
	/** Default window width (pixels). */
	public static final int DEFAULT_WIDTH = 500;
	
	/** Default window height (pixels). */
	public static final int DEFAULT_HEIGHT = 640;
	
	/** Application title string. */
	public static final String DEFAULT_TITLE = "Drafts or Draughts (Checkers) Game";
	
	/** Checkerboard GUI board component. */
	private CheckerBoard board;
	
	/** Control options panel component. */
	private OptionPanel opts;
	
	/**
	 * Default constructor initializing window with standard dimensions and title.
	 */
	public CheckersWindow() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TITLE);
	}
	
	/**
	 * Constructor initializing window with specific starting players.
	 * 
	 * @param player1 Black player instance
	 * @param player2 White player instance
	 */
	public CheckersWindow(Player player1, Player player2) {
		this();
		setPlayer1(player1);
		setPlayer2(player2);
	}
	
	/**
	 * Constructor initializing window dimensions and title.
	 * 
	 * @param width  window width
	 * @param height window height
	 * @param title  window title
	 */
	public CheckersWindow(int width, int height, String title) {
		super(title);
		super.setSize(width, height);
		super.setLocationByPlatform(true);
		
		JPanel layout = new JPanel(new BorderLayout());
		this.board = new CheckerBoard(this);
		this.opts = new OptionPanel(this);
		
		layout.add(board, BorderLayout.CENTER);
		layout.add(opts, BorderLayout.SOUTH);
		this.add(layout);
	}
	
	public CheckerBoard getBoard() {
		return board;
	}

	public OptionPanel getOptionPanel() {
		return opts;
	}

	/**
	 * Updates the Player 1 controller.
	 * 
	 * @param player1 new Player instance
	 */
	public void setPlayer1(Player player1) {
		this.board.setPlayer1(player1);
		this.board.update();
	}
	
	/**
	 * Updates the Player 2 controller.
	 * 
	 * @param player2 new Player instance
	 */
	public void setPlayer2(Player player2) {
		this.board.setPlayer2(player2);
		this.board.update();
	}
	
	/**
	 * Resets the checkers game to starting state.
	 */
	public void restart() {
		this.board.getGame().restart();
		this.board.update();
	}
	
	public void setGameState(String state) {
		this.board.getGame().setGameState(state);
	}
}
