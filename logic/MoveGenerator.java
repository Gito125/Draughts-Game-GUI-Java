/*
 * ============================================================================
 * File:        MoveGenerator.java
 * Package:     logic
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              
 * Course:      Data Structures and Algorithms
 * 
 * Description: Generates legal single-step move destination coordinates and multi-step
 *              capture skip destination coordinates for pieces on the checkerboard.
 *
 * DSA Concepts Applied:
 *   - Graph Adjacency: Explores board graph adjacency vertices for diagonal edges.
 *   - Child Node Generator: Serves as child-node expansion generator during DFS game tree traversal.
 *   - Dynamic Lists: Dynamically populates and filters List<Point> move destinations.
 * ============================================================================
 */

package logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.Board;

/**
 * The {@code MoveGenerator} class provides static methods to generate and filter
 * candidate diagonal move endpoints and capture jump endpoints for checkers.
 */
public class MoveGenerator {

	/**
	 * Gets a list of legal 1-step diagonal move destination points for a piece at the start point.
	 * 
	 * @param board board instance to query
	 * @param start start Point(x, y)
	 * @return List of valid move destination points
	 */
	public static List<Point> getMoves(Board board, Point start) {
		return getMoves(board, Board.toIndex(start));
	}
	
	/**
	 * Gets a list of legal 1-step diagonal move destination points for a piece at startIndex.
	 * 
	 * @param board      board instance to query
	 * @param startIndex start tile index (0 to 31)
	 * @return List of empty destination Points
	 */
	public static List<Point> getMoves(Board board, int startIndex) {
		List<Point> endPoints = new ArrayList<>();
		if (board == null || !Board.isValidIndex(startIndex)) {
			return endPoints;
		}
		
		int id = board.get(startIndex);
		Point p = Board.toPoint(startIndex);
		addPoints(endPoints, p, id, 1);
		
		// Filter out non-empty destination tiles
		for (int i = 0; i < endPoints.size(); i++) {
			Point end = endPoints.get(i);
			if (board.get(end.x, end.y) != Board.EMPTY) {
				endPoints.remove(i--);
			}
		}
		
		return endPoints;
	}
	
	/**
	 * Gets a list of candidate 2-step capture skip destination points for a piece at start point.
	 * 
	 * @param board board instance to query
	 * @param start start Point(x, y)
	 * @return List of valid jump destination points
	 */
	public static List<Point> getSkips(Board board, Point start) {
		return getSkips(board, Board.toIndex(start));
	}
	
	/**
	 * Gets a list of candidate 2-step capture skip destination points for a piece at startIndex.
	 * 
	 * @param board      board instance to query
	 * @param startIndex start tile index (0 to 31)
	 * @return List of valid jump destination Points
	 */
	public static List<Point> getSkips(Board board, int startIndex) {
		List<Point> endPoints = new ArrayList<>();
		if (board == null || !Board.isValidIndex(startIndex)) {
			return endPoints;
		}
		
		int id = board.get(startIndex);
		Point p = Board.toPoint(startIndex);
		addPoints(endPoints, p, id, 2);
		
		// Filter out invalid skips (must jump enemy piece onto an empty square)
		for (int i = 0; i < endPoints.size(); i++) {
			Point end = endPoints.get(i);
			if (!isValidSkip(board, startIndex, Board.toIndex(end))) {
				endPoints.remove(i--);
			}
		}

		return endPoints;
	}
	
	/**
	 * Validates whether a candidate skip from startIndex to endIndex is legal.
	 * Requires the destination to be EMPTY and the jumped middle tile to contain an enemy piece.
	 * 
	 * @param board      board instance to query
	 * @param startIndex start tile index (0 to 31)
	 * @param endIndex   end tile index (0 to 31)
	 * @return true if skip is legal under Draughts rules
	 */
	public static boolean isValidSkip(Board board, int startIndex, int endIndex) {
		if (board == null) {
			return false;
		}

		// Destination must be EMPTY
		if (board.get(endIndex) != Board.EMPTY) {
			return false;
		}
		
		// Middle tile must contain an opponent piece
		int id = board.get(startIndex);
		int midID = board.get(Board.toIndex(Board.middle(startIndex, endIndex)));
		if (id == Board.INVALID || id == Board.EMPTY) {
			return false;
		} else if (midID == Board.INVALID || midID == Board.EMPTY) {
			return false;
		} else if (Board.isBlackChecker(midID) ^ Board.isWhiteChecker(id)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Helper method to calculate candidate diagonal offset points for normal pieces and kings.
	 * Black normal checkers move down (+y), White normal checkers move up (-y), Kings move both.
	 * 
	 * @param points output list to receive generated points
	 * @param p      center Point coordinate
	 * @param id     piece ID
	 * @param delta  offset distance (1 for moves, 2 for skips)
	 */
	public static void addPoints(List<Point> points, Point p, int id, int delta) {
		boolean isKing = Board.isKingChecker(id);
		
		// Downward diagonal offsets (+y) for Black checkers and Kings
		if (isKing || id == Board.BLACK_CHECKER) {
			points.add(new Point(p.x + delta, p.y + delta));
			points.add(new Point(p.x - delta, p.y + delta));
		}
		
		// Upward diagonal offsets (-y) for White checkers and Kings
		if (isKing || id == Board.WHITE_CHECKER) {
			points.add(new Point(p.x + delta, p.y - delta));
			points.add(new Point(p.x - delta, p.y - delta));
		}
	}
}
