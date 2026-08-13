/*
 * ============================================================================
 * File:        Move.java
 * Package:     model
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              
 * Course:      Data Structures and Algorithms
 * 
 * Description: Represents a single move operation on the checkerboard, storing
 *              start/end tile indices and a heuristic evaluation weight used by
 *              the Minimax decision algorithm.
 *
 * DSA Concepts Applied:
 *   - Move Representation: Data object holding start tile index, end tile index, and evaluation weight.
 * ============================================================================
 */

package model;

import java.awt.Point;

/**
 * The {@code Move} class represents a move action between board tiles and
 * maintains an evaluation weight for AI move selection algorithms.
 */
public class Move {
	
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
