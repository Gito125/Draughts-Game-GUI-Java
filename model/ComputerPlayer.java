/*
 * ============================================================================
 * File:        ComputerPlayer.java
 * Package:     model
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              
 * Course:      Data Structures and Algorithms
 * 
 * Description: Implements the AI/Computer opponent for Draughts using Minimax game tree search
 *              at a fixed depth of 3 with material-count heuristic evaluation.
 *
 * DSA Concepts Applied:
 *   - Game Trees: Nodes represent board states and edges represent legal moves.
 *   - Depth-First Search (DFS): Explores tree branches recursively down to depth 3.
 *   - Recursion & Backtracking: Evaluates subtree scores by alternating MAX and MIN layers.
 *   - Material Heuristic: O(1) state scoring based on piece count difference.
 * ============================================================================
 */

package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import logic.MoveGenerator;

/**
 * The {@code ComputerPlayer} class represents an automated AI opponent in Draughts.
 * It evaluates candidate move branches on a simulated game tree using the Minimax search algorithm.
 */
public class ComputerPlayer {

	/** Fixed lookahead depth for game tree search. */
	private static final int SEARCH_DEPTH = 3;

	/**
	 * Default constructor.
	 */
	public ComputerPlayer() {
	}

	/**
	 * Updates the game state by selecting and executing the optimal AI move.
	 * 
	 * @param game active game instance to update
	 */
	public void updateGame(Game game) {
		if (game == null || game.isGameOver()) {
			return;
		}

		Game copy = game.copy();
		List<Move> moves = getMoves(copy);

		if (moves.isEmpty()) {
			return;
		}

		Move chosenMove = selectMinimaxMove(copy, moves);

		if (chosenMove != null) {
			game.move(chosenMove.getStartIndex(), chosenMove.getEndIndex());
		}
	}

	/**
	 * Evaluates candidate moves using Minimax tree search.
	 * 
	 * @param game  current game state
	 * @param moves available legal moves
	 * @return best evaluated Move object
	 */
	private Move selectMinimaxMove(Game game, List<Move> moves) {
		Move bestMove = moves.get(0);
		double bestValue = Double.NEGATIVE_INFINITY;
		boolean isP1Turn = game.isP1Turn();

		for (Move move : moves) {
			Game simulated = game.copy();
			simulated.move(move.getStartIndex(), move.getEndIndex());

			double val = minimax(simulated, SEARCH_DEPTH - 1, false, isP1Turn);
			move.setWeight(val);

			if (val > bestValue) {
				bestValue = val;
				bestMove = move;
			}
		}

		return bestMove;
	}

	/**
	 * Recursive Minimax search algorithm.
	 * DFS traversal over game tree node levels, alternating between MAX and MIN nodes.
	 * 
	 * @param game         simulated game node state
	 * @param depth        remaining tree depth
	 * @param isMaximizing true if MAX node (AI turn), false if MIN node (opponent turn)
	 * @param isBlackAI    true if AI plays Black pieces
	 * @return heuristic score of game state
	 */
	private double minimax(Game game, int depth, boolean isMaximizing, boolean isBlackAI) {
		if (depth <= 0 || game.isGameOver()) {
			return evaluateBoard(game, isBlackAI);
		}

		List<Move> moves = getMoves(game);
		if (moves.isEmpty()) {
			return evaluateBoard(game, isBlackAI);
		}

		if (isMaximizing) {
			double maxEval = Double.NEGATIVE_INFINITY;
			for (Move move : moves) {
				Game child = game.copy();
				child.move(move.getStartIndex(), move.getEndIndex());
				double eval = minimax(child, depth - 1, false, isBlackAI);
				maxEval = Math.max(maxEval, eval);
			}
			return maxEval;
		} else {
			double minEval = Double.POSITIVE_INFINITY;
			for (Move move : moves) {
				Game child = game.copy();
				child.move(move.getStartIndex(), move.getEndIndex());
				double eval = minimax(child, depth - 1, true, isBlackAI);
				minEval = Math.min(minEval, eval);
			}
			return minEval;
		}
	}

	/**
	 * Evaluates board state using Material Count (piece count difference).
	 * Regular checkers = 1 point, Kings = 2 points.
	 * 
	 * @param game      game state to score
	 * @param isBlackAI true if evaluating for Black AI
	 * @return numerical heuristic score (positive favors AI, negative favors opponent)
	 */
	private double evaluateBoard(Game game, boolean isBlackAI) {
		if (game.isGameOver()) {
			if (game.isDraw()) {
				return 0.0;
			}
			boolean p1Wins = !game.getBoard().find(Board.WHITE_CHECKER).isEmpty() || !game.getBoard().find(Board.WHITE_KING).isEmpty();
			if (isBlackAI) {
				return p1Wins ? 1000.0 : -1000.0;
			} else {
				return p1Wins ? -1000.0 : 1000.0;
			}
		}

		Board b = game.getBoard();

		// Material Count: Regular checker = 1, King = 2
		int myCheckers = b.find(isBlackAI ? Board.BLACK_CHECKER : Board.WHITE_CHECKER).size();
		int myKings = b.find(isBlackAI ? Board.BLACK_KING : Board.WHITE_KING).size();
		int oppCheckers = b.find(isBlackAI ? Board.WHITE_CHECKER : Board.BLACK_CHECKER).size();
		int oppKings = b.find(isBlackAI ? Board.WHITE_KING : Board.BLACK_KING).size();

		double score = (myCheckers * 1.0 + myKings * 2.0) - (oppCheckers * 1.0 + oppKings * 2.0);

		return score;
	}

	/**
	 * Retrieves all legal moves for current turn.
	 * Enforces mandatory jump capture rule: if jumps exist, normal moves are excluded.
	 * 
	 * @param game game state
	 * @return List of valid Move objects
	 */
	private List<Move> getMoves(Game game) {
		List<Move> moves = new ArrayList<>();
		if (game == null) {
			return moves;
		}

		// If mid multi-jump, only allow continuation skips for skipIndex piece
		if (game.getSkipIndex() >= 0) {
			List<Point> skips = MoveGenerator.getSkips(game.getBoard(), game.getSkipIndex());
			for (Point end : skips) {
				moves.add(new Move(game.getSkipIndex(), Board.toIndex(end)));
			}
			return moves;
		}

		Board b = game.getBoard();
		List<Point> checkers = new ArrayList<>();
		if (game.isP1Turn()) {
			checkers.addAll(b.find(Board.BLACK_CHECKER));
			checkers.addAll(b.find(Board.BLACK_KING));
		} else {
			checkers.addAll(b.find(Board.WHITE_CHECKER));
			checkers.addAll(b.find(Board.WHITE_KING));
		}

		// Check for mandatory capture skips across all active pieces
		for (Point checker : checkers) {
			int index = Board.toIndex(checker);
			List<Point> skips = MoveGenerator.getSkips(b, index);
			for (Point end : skips) {
				moves.add(new Move(index, Board.toIndex(end)));
			}
		}

		// If no captures exist, populate normal 1-step diagonal moves
		if (moves.isEmpty()) {
			for (Point checker : checkers) {
				int index = Board.toIndex(checker);
				List<Point> movesEnds = MoveGenerator.getMoves(b, index);
				for (Point end : movesEnds) {
					moves.add(new Move(index, Board.toIndex(end)));
				}
			}
		}

		return moves;
	}
}
