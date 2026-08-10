/*
 * ============================================================================
 * File:        Game.java
 * Package:     model
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Manages checkers game state, turn progression, move execution,
 *              king promotions, and game termination rules including draw detection
 *              via move counters and board state history tracking.
 *
 * DSA Concepts Applied:
 *   - Stacks (stacks.pdf): Maintains stateHistory as a stack/sequence of board states
 *     for 3-fold repetition draw detection.
 *   - Intro To DSA (Intro To DSA.pptx): State serialization and deserialization
 *     (getGameState / setGameState) for state persistence and deep cloning.
 *   - Mathematical Background (Mathematical Background DSA.pptx): Move counters
 *     and complexity analysis of state history search O(k).
 * ============================================================================
 */

package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.MoveGenerator;
import logic.MoveLogic;

/**
 * The {@code Game} class encapsulates the state of a checkers game, enforcing standard
 * rules, turn progression, king promotions, and draw detection mechanisms.
 */
public class Game {

	/** Limit of consecutive moves without a capture before triggering a draw. */
	public static final int NO_CAPTURE_DRAW_LIMIT = 40;

	/** Number of identical board state occurrences required to trigger a 3-fold repetition draw. */
	public static final int REPETITION_DRAW_LIMIT = 3;

	/** Absolute upper bound on total moves per game to prevent infinite loops. */
	public static final int MAX_TOTAL_MOVES = 200;

	/** Current board layout instance. */
	private Board board;
	
	/** Flag indicating if it is Player 1's (Black) turn. */
	private boolean isP1Turn;
	
	/** Index of piece locked in a multi-jump sequence (-1 if no multi-jump active). */
	private int skipIndex;

	/** Number of consecutive moves executed without any piece capture. */
	private int movesSinceCapture;

	/** Total number of turns/moves executed in this game session. */
	private int totalMoves;

	/**
	 * History sequence of serialized board states for 3-fold repetition detection.
	 * 
	 * <p><b>DSA Reference (stacks.pdf):</b>
	 * Acts as a LIFO state stack history for tracking game trajectory frames.</p>
	 */
	private List<String> stateHistory;
	
	/**
	 * Constructs a new Game initialized to standard starting layout.
	 */
	public Game() {
		this.stateHistory = new ArrayList<>();
		restart();
	}
	
	/**
	 * Constructs a Game initialized from a serialized state string.
	 * 
	 * @param state serialized game state string
	 */
	public Game(String state) {
		this.stateHistory = new ArrayList<>();
		setGameState(state);
	}
	
	/**
	 * Constructs a Game from specific component states.
	 * 
	 * @param board     board state
	 * @param isP1Turn  turn flag
	 * @param skipIndex multi-jump lock index
	 */
	public Game(Board board, boolean isP1Turn, int skipIndex) {
		this.board = (board == null) ? new Board() : board;
		this.isP1Turn = isP1Turn;
		this.skipIndex = skipIndex;
		this.movesSinceCapture = 0;
		this.totalMoves = 0;
		this.stateHistory = new ArrayList<>();
		this.stateHistory.add(this.board.toString());
	}
	
	/**
	 * Creates a deep copy of this Game instance.
	 * 
	 * <p><b>DSA Reference (Trees in DSA.pptx):</b>
	 * Used extensively during recursive Minimax search to simulate future states
	 * without mutating the active game state.</p>
	 * 
	 * @return exact independent copy of this Game object
	 * @complexity O(1) constant time copy
	 */
	public Game copy() {
		Game g = new Game();
		g.board = board.copy();
		g.isP1Turn = isP1Turn;
		g.skipIndex = skipIndex;
		g.movesSinceCapture = movesSinceCapture;
		g.totalMoves = totalMoves;
		g.stateHistory = new ArrayList<>(stateHistory);
		return g;
	}
	
	/**
	 * Resets the game to initial state with Black moving first.
	 */
	public void restart() {
		this.board = new Board();
		this.isP1Turn = true;
		this.skipIndex = -1;
		this.movesSinceCapture = 0;
		this.totalMoves = 0;
		if (this.stateHistory != null) {
			this.stateHistory.clear();
			this.stateHistory.add(this.board.toString());
		}
	}
	
	/**
	 * Attempts to execute a move from start Point to end Point.
	 * 
	 * @param start start tile Point
	 * @param end   end tile Point
	 * @return true if move was valid and executed
	 */
	public boolean move(Point start, Point end) {
		if (start == null || end == null) {
			return false;
		}
		return move(Board.toIndex(start), Board.toIndex(end));
	}
	
	/**
	 * Attempts to execute a move between startIndex and endIndex.
	 * Updates board state, performs piece capture removal, manages king promotions,
	 * handles multi-jump locks, updates draw counters, and toggles turns.
	 * 
	 * <p><b>DSA Reference (Graphs.pptx / Linear Search):</b>
	 * Updates graph nodes and evaluates multi-jump continuation edges.</p>
	 * 
	 * @param startIndex start tile index (0 to 31)
	 * @param endIndex   end tile index (0 to 31)
	 * @return true if move was executed successfully
	 * @complexity O(1) constant time execution
	 */
	public boolean move(int startIndex, int endIndex) {
		// Validate move using rule engine
		if (!MoveLogic.isValidMove(this, startIndex, endIndex)) {
			return false;
		}
		
		// Perform movement
		Point middle = Board.middle(startIndex, endIndex);
		int midIndex = Board.toIndex(middle);
		boolean isCapture = Board.isValidIndex(midIndex);
		
		this.board.set(endIndex, board.get(startIndex));
		if (isCapture) {
			this.board.set(midIndex, Board.EMPTY);
		}
		this.board.set(startIndex, Board.EMPTY);
		
		// Check King promotion
		Point end = Board.toPoint(endIndex);
		int id = board.get(endIndex);
		boolean switchTurn = false;
		
		if (end.y == 0 && id == Board.WHITE_CHECKER) {
			this.board.set(endIndex, Board.WHITE_KING);
			switchTurn = true; // Kinging ends turn immediately (Rule 4)
		} else if (end.y == 7 && id == Board.BLACK_CHECKER) {
			this.board.set(endIndex, Board.BLACK_KING);
			switchTurn = true; // Kinging ends turn immediately (Rule 4)
		}
		
		// Manage multi-skip continuation
		if (isCapture) {
			this.skipIndex = endIndex;
			this.movesSinceCapture = 0; // Reset no-capture draw counter
		} else {
			this.movesSinceCapture++;
		}
		
		// Determine if turn should switch
		if (!isCapture || MoveGenerator.getSkips(board.copy(), endIndex).isEmpty()) {
			switchTurn = true;
		}
		
		if (switchTurn) {
			this.isP1Turn = !isP1Turn;
			this.skipIndex = -1;
			this.totalMoves++;
			// Push current board state to state history stack for repetition detection
			this.stateHistory.add(this.board.toString());
		}
		
		return true;
	}
	
	/**
	 * Returns a copy of the current board layout.
	 * 
	 * @return Board copy
	 */
	public Board getBoard() {
		return board.copy();
	}
	
	/**
	 * Evaluates whether the game has reached a draw condition:
	 * 1. 40 consecutive moves without a piece capture
	 * 2. 3-fold board state repetition
	 * 3. 200 total move limit reached
	 * 
	 * <p><b>DSA Reference (stacks.pdf / Intro To DSA):</b>
	 * Uses frequency count over stateHistory stack sequence to detect 3-fold repetition.</p>
	 * 
	 * @return true if game is a draw
	 * @complexity O(N) where N is number of moves in stateHistory
	 */
	public boolean isDraw() {
		// Condition 1: 40 moves without capture
		if (movesSinceCapture >= NO_CAPTURE_DRAW_LIMIT) {
			return true;
		}
		
		// Condition 2: Max total moves cap
		if (totalMoves >= MAX_TOTAL_MOVES) {
			return true;
		}
		
		// Condition 3: 3-fold board repetition
		if (stateHistory != null && !stateHistory.isEmpty()) {
			String current = stateHistory.get(stateHistory.size() - 1);
			int occurrences = Collections.frequency(stateHistory, current);
			if (occurrences >= REPETITION_DRAW_LIMIT) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Gets a human-readable message describing the reason for a draw game,
	 * or empty string if not a draw.
	 * 
	 * @return draw status explanation string
	 */
	public String getDrawReason() {
		if (movesSinceCapture >= NO_CAPTURE_DRAW_LIMIT) {
			return "Draw: 40 moves without a capture!";
		}
		if (totalMoves >= MAX_TOTAL_MOVES) {
			return "Draw: Maximum 200 move limit reached!";
		}
		if (stateHistory != null && !stateHistory.isEmpty()) {
			String current = stateHistory.get(stateHistory.size() - 1);
			if (Collections.frequency(stateHistory, current) >= REPETITION_DRAW_LIMIT) {
				return "Draw: 3-fold board repetition!";
			}
		}
		return "";
	}
	
	/**
	 * Determines if the game has concluded (either by player elimination, no available moves, or draw).
	 * 
	 * @return true if game is over
	 * @complexity O(p) checking moves for active player pieces
	 */
	public boolean isGameOver() {
		// Check draw conditions first
		if (isDraw()) {
			return true;
		}

		// Ensure at least one checker of each color exists
		List<Point> black = board.find(Board.BLACK_CHECKER);
		black.addAll(board.find(Board.BLACK_KING));
		if (black.isEmpty()) {
			return true;
		}
		List<Point> white = board.find(Board.WHITE_CHECKER);
		white.addAll(board.find(Board.WHITE_KING));
		if (white.isEmpty()) {
			return true;
		}
		
		// Verify current turn player has at least one legal move
		List<Point> test = isP1Turn ? black : white;
		for (Point p : test) {
			int i = Board.toIndex(p);
			if (!MoveGenerator.getMoves(board, i).isEmpty() || !MoveGenerator.getSkips(board, i).isEmpty()) {
				return false;
			}
		}
		
		// Current player has no available moves
		return true;
	}
	
	public boolean isP1Turn() {
		return isP1Turn;
	}
	
	public void setP1Turn(boolean isP1Turn) {
		this.isP1Turn = isP1Turn;
	}
	
	public int getSkipIndex() {
		return skipIndex;
	}
	
	public int getMovesSinceCapture() {
		return movesSinceCapture;
	}

	public int getTotalMoves() {
		return totalMoves;
	}
	
	/**
	 * Serializes current game state into a compact string representation.
	 * 
	 * @return 34+ character game state string
	 */
	public String getGameState() {
		StringBuilder state = new StringBuilder();
		for (int i = 0; i < 32; i++) {
			state.append(board.get(i));
		}
		state.append(isP1Turn ? "1" : "0");
		state.append(skipIndex);
		return state.toString();
	}
	
	/**
	 * Deserializes game state from string.
	 * 
	 * @param state serialized game state string
	 */
	public void setGameState(String state) {
		restart();
		if (state == null || state.isEmpty()) {
			return;
		}
		int n = state.length();
		for (int i = 0; i < 32 && i < n; i++) {
			try {
				int id = Integer.parseInt("" + state.charAt(i));
				this.board.set(i, id);
			} catch (NumberFormatException e) {}
		}
		if (n > 32) {
			this.isP1Turn = (state.charAt(32) == '1');
		}
		if (n > 33) {
			try {
				this.skipIndex = Integer.parseInt(state.substring(33));
			} catch (NumberFormatException e) {
				this.skipIndex = -1;
			}
		}
	}
}
