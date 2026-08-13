/*
 * ============================================================================
 * File:        CheckersWindow.java
 * Package:     ui
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms
 * 
 * Description: Top-level Swing JFrame window hosting the CheckerBoard display
 *              component and OptionPanel control panel.
 *
 * DSA Concepts Applied:
 *   - Software Modularity: Separates GUI window shell from core game components.
 * ============================================================================
 */

package ui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The {@code CheckersWindow} class manages the main application GUI window.
 */
public class CheckersWindow extends JFrame {

	private static final long serialVersionUID = 8782122389400590079L;
	
	/** Default window width (pixels). */
	public static final int DEFAULT_WIDTH = 500;
	
	/** Default window height (pixels). */
	public static final int DEFAULT_HEIGHT = 600;
	
	/** Application title string. */
	public static final String DEFAULT_TITLE = "Draft Game";
	
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
	 * Constructor initializing window with specific player types.
	 * 
	 * @param p1IsComputer true if P1 is Computer AI
	 * @param p2IsComputer true if P2 is Computer AI
	 */
	public CheckersWindow(boolean p1IsComputer, boolean p2IsComputer) {
		this();
		setP1IsComputer(p1IsComputer);
		setP2IsComputer(p2IsComputer);
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
	 * Updates Player 1 controller type.
	 * 
	 * @param p1IsComputer true if computer
	 */
	public void setP1IsComputer(boolean p1IsComputer) {
		this.board.setP1IsComputer(p1IsComputer);
	}
	
	/**
	 * Updates Player 2 controller type.
	 * 
	 * @param p2IsComputer true if computer
	 */
	public void setP2IsComputer(boolean p2IsComputer) {
		this.board.setP2IsComputer(p2IsComputer);
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
