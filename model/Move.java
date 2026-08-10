/*
 * ============================================================================
 * File:        Move.java
 * Package:     model
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Represents a single move operation on the checkerboard, storing
 *              start/end tile indices and a heuristic weight value used by the
 *              AI evaluation search. Implements Comparable for move ordering.
 *
 * DSA Concepts Applied:
 *   - Linked Lists (Linked Lists.pptx): Move paths represent discrete step pairs.
 *   - Sorting Techniques (Sorting Techniques.pptx / Merge & Quick sort.pptx):
 *     Implements Comparable<Move> to enable sorting moves by heuristic score,
 *     which optimizes Alpha-Beta pruning performance in Minimax tree search.
 *   - Mathematical Background (Mathematical Background DSA.pptx):
 *     Stores floating-point evaluation weights for decision boundary ranking.
 * ============================================================================
 */

package model;

import java.awt.Point;

/**
 * The {@code Move} class represents a move action between board tiles and
 * maintains an evaluation weight for AI move selection algorithms.
 */
public class Move implements Comparable<Move> {
	
	/** The weight corresponding to an invalid or illegal move. */
	public static final double WEIGHT_INVALID = Double.NEGATIVE_INFINITY;

	/** The start index of the move (0 to 31). */
	private byte startIndex;
	
	/** The end index of the move (0 to 31). */
	private byte endIndex;
	
	/** The heuristic weight associated with evaluating this move. */
	private double weight;
	
	/**
	 * Constructs a Move with specified start and end board indices.
	 * 
	 * @param startIndex start tile index (0 to 31)
	 * @param endIndex   end tile index (0 to 31)
	 */
	public Move(int startIndex, int endIndex) {
		setStartIndex(startIndex);
		setEndIndex(endIndex);
		this.weight = 0.0;
	}
	
	/**
	 * Constructs a Move using 2D grid Points.
	 * 
	 * @param start start Point (x, y)
	 * @param end   end Point (x, y)
	 */
	public Move(Point start, Point end) {
		setStartIndex(Board.toIndex(start));
		setEndIndex(Board.toIndex(end));
		this.weight = 0.0;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	
	public void setStartIndex(int startIndex) {
		this.startIndex = (byte) startIndex;
	}
	
	public int getEndIndex() {
		return endIndex;
	}
	
	public void setEndIndex(int endIndex) {
		this.endIndex = (byte) endIndex;
	}
	
	public Point getStart() {
		return Board.toPoint(startIndex);
	}
	
	public void setStart(Point start) {
		setStartIndex(Board.toIndex(start));
	}
	
	public Point getEnd() {
		return Board.toPoint(endIndex);
	}
	
	public void setEnd(Point end) {
		setEndIndex(Board.toIndex(end));
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public void changeWeight(double delta) {
		this.weight += delta;
	}

	/**
	 * Compares two moves based on their heuristic weights in descending order
	 * (higher weight moves come first). Used by sorting algorithms.
	 * 
	 * <p><b>DSA Reference (Merge & Quick sort.pptx / Sorting Techniques.pptx):</b>
	 * Facilitates quick comparison operations required by sorting algorithms
	 * like Quick Sort (used by Collections.sort()) to order moves for optimal
	 * Alpha-Beta tree pruning.</p>
	 * 
	 * @param other the move to compare against
	 * @return negative if this weight > other weight, positive if less, 0 if equal
	 * @complexity O(1) constant time comparison
	 */
	@Override
	public int compareTo(Move other) {
		return Double.compare(other.weight, this.weight); // Descending order
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Move move = (Move) obj;
		return startIndex == move.startIndex && endIndex == move.endIndex;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[start=" + startIndex + ", "
				+ "end=" + endIndex + ", weight=" + weight + "]";
	}
}
