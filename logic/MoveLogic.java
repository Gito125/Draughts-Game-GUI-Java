/*
 * ============================================================================
 * File:        MoveLogic.java
 * Package:     logic
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Rule engine for validating legal moves in accordance with official
 *              draughts rules, enforcing mandatory capture jumps, diagonal movement,
 *              king promotion rules, and multi-jump turn locks.
 *
 * DSA Concepts Applied:
 *   - Graphs (Graphs.pptx): Validates directed edge movements between 32 tile nodes.
 *   - Trees (Trees in DSA.pptx): Prunes illegal move branches during game tree search.
 *   - Mathematical Background (Mathematical Background DSA.pptx): Manhattan vector
 *     distance calculations (|dx| == |dy|) for diagonal movement verification.
 * ============================================================================
 */

package logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.Board;
import model.Game;

/**
 * The {@code MoveLogic} class enforces all official draughts rules and move validity checks.
 */
public class MoveLogic {

	/**
	 * Determines if a proposed move in the current game state is legal.
	 * 
	 * @param game       game instance
	 * @param startIndex start tile index (0 to 31)
	 * @param endIndex   end tile index (0 to 31)
	 * @return true if move is valid under draughts rules
	 */
	public static boolean isValidMove(Game game, int startIndex, int endIndex) {
		return game != null && isValidMove(game.getBoard(), game.isP1Turn(), startIndex, endIndex, game.getSkipIndex());
	}
	
	/**
	 * Determines if a proposed move is legal given board state, turn player, and multi-jump lock state.
	 * 
	 * <p><b>DSA Reference (Trees in DSA.pptx / Graphs.pptx):</b>
	 * Validates edge transitions between state tree nodes, ensuring turn locks and forced captures.</p>
	 * 
	 * @param board      current board state
	 * @param isP1Turn   true if Player 1 (Black) turn, false if Player 2 (White) turn
	 * @param startIndex start tile index
	 * @param endIndex   end tile index
	 * @param skipIndex  index of piece locked in multi-jump (-1 if no lock)
	 * @return true if move complies with all rules
	 * @complexity O(p) where p is total number of active player pieces checked for forced captures
	 */
	public static boolean isValidMove(Board board, boolean isP1Turn, int startIndex, int endIndex, int skipIndex) {
		if (board == null || !Board.isValidIndex(startIndex) || !Board.isValidIndex(endIndex)) {
			return false;
		} else if (startIndex == endIndex) {
			return false;
		} else if (Board.isValidIndex(skipIndex) && skipIndex != startIndex) {
			return false;
		}
		
		if (!validateIDs(board, isP1Turn, startIndex, endIndex)) {
			return false;
		} else if (!validateDistance(board, isP1Turn, startIndex, endIndex)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Validates piece ownership and target tile vacancy.
	 * 
	 * @param board      board state
	 * @param isP1Turn   turn flag
	 * @param startIndex start index
	 * @param endIndex   end index
	 * @return true if piece IDs and target square match turn rules
	 */
	private static boolean validateIDs(Board board, boolean isP1Turn, int startIndex, int endIndex) {
		if (board.get(endIndex) != Board.EMPTY) {
			return false;
		}
		
		int id = board.get(startIndex);
		if ((isP1Turn && !Board.isBlackChecker(id)) || (!isP1Turn && !Board.isWhiteChecker(id))) {
			return false;
		}
		
		Point middle = Board.middle(startIndex, endIndex);
		int midID = board.get(Board.toIndex(middle));
		if (midID != Board.INVALID && ((!isP1Turn && !Board.isBlackChecker(midID)) || (isP1Turn && !Board.isWhiteChecker(midID)))) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Validates diagonal movement vectors and enforces mandatory capture jumps.
	 * 
	 * <p><b>DSA Reference (Mathematical Background DSA.pptx):</b>
	 * Checks geometric vector offsets |dx| == |dy| for diagonal movement and enforces
	 * mandatory capture rule: if any piece can jump, normal 1-step moves are prohibited.</p>
	 * 
	 * @param board      board state
	 * @param isP1Turn   turn flag
	 * @param startIndex start index
	 * @param endIndex   end index
	 * @return true if distance vector and forced capture rules pass
	 */
	private static boolean validateDistance(Board board, boolean isP1Turn, int startIndex, int endIndex) {
		Point start = Board.toPoint(startIndex);
		Point end = Board.toPoint(endIndex);
		int dx = end.x - start.x;
		int dy = end.y - start.y;
		
		if (Math.abs(dx) != Math.abs(dy) || Math.abs(dx) > 2 || dx == 0) {
			return false;
		}
		
		int id = board.get(startIndex);
		if ((id == Board.WHITE_CHECKER && dy > 0) || (id == Board.BLACK_CHECKER && dy < 0)) {
			return false;
		}
		
		Point middle = Board.middle(startIndex, endIndex);
		int midID = board.get(Board.toIndex(middle));
		
		// If attempting a normal 1-step move (midID < 0), check if any of player's pieces can jump
		if (midID < 0) {
			List<Point> checkers;
			if (isP1Turn) {
				checkers = board.find(Board.BLACK_CHECKER);
				checkers.addAll(board.find(Board.BLACK_KING));
			} else {
				checkers = board.find(Board.WHITE_CHECKER);
				checkers.addAll(board.find(Board.WHITE_KING));
			}
			
			// If a capture jump is available anywhere for this player, normal moves are invalid
			for (Point p : checkers) {
				int index = Board.toIndex(p);
				if (!MoveGenerator.getSkips(board, index).isEmpty()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Evaluates whether a piece at a specified position is currently safe from opponent captures.
	 * 
	 * @param board   board state
	 * @param checker piece position
	 * @return true if no enemy piece can capture this piece on the next turn
	 */
	public static boolean isSafe(Board board, Point checker) {
		if (board == null || checker == null) {
			return true;
		}
		int index = Board.toIndex(checker);
		if (index < 0) {
			return true;
		}
		int id = board.get(index);
		if (id == Board.EMPTY) {
			return true;
		}
		
		boolean isBlack = Board.isBlackChecker(id);
		List<Point> check = new ArrayList<>();
		MoveGenerator.addPoints(check, checker, Board.BLACK_KING, 1);
		
		for (Point p : check) {
			int start = Board.toIndex(p);
			int tid = board.get(start);
			
			if (tid == Board.EMPTY || tid == Board.INVALID) {
				continue;
			}
			
			boolean isWhite = Board.isWhiteChecker(tid);
			if (isBlack && !isWhite) {
				continue;
			}
			
			int dx = (checker.x - p.x) * 2;
			int dy = (checker.y - p.y) * 2;
			if (!Board.isKingChecker(tid) && (isWhite ^ (dy < 0))) {
				continue;
			}
			int endIndex = Board.toIndex(new Point(p.x + dx, p.y + dy));
			if (MoveGenerator.isValidSkip(board, start, endIndex)) {
				return false;
			}
		}
		
		return true;
	}
}
