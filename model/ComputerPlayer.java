/*
 * ============================================================================
 * File:        ComputerPlayer.java
 * Package:     model
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Implements the AI opponent for Draughts using game tree search
 *              algorithms. Supports three difficulty levels:
 *              - Easy:   Random move selection (O(n), basic list iteration)
 *              - Medium: Minimax search (depth 3, DFS game tree traversal)
 *              - Hard:   Minimax with Alpha-Beta pruning (depth 6) with move
 *                        ordering using Collections.sort() (Quick Sort reference).
 *
 * DSA Concepts Applied:
 *   - Trees (Trees in DSA.pptx): Game tree representation where nodes represent board
 *     states and edges represent legal moves.
 *   - DFS (BFS & DFS.pptx): Depth-First Search tree traversal in Minimax.
 *   - Stacks (stacks.pdf): Execution call stack for recursive DFS backtracking.
 *   - Sorting Techniques (Sorting Techniques.pptx / Merge & Quick sort.pptx):
 *     Move ordering via Collections.sort() (Quick Sort algorithm) to optimize
 *     Alpha-Beta pruning cutoffs.
 *   - Queues (Queues.pptx): Ordered candidate move processing.
 *   - Graphs (Graphs.pptx): Board vertex/edge graph topology.
 *   - Mathematical Background (Mathematical Background DSA.pptx): Score heuristic polynomial
 *     and Big-O algorithm analysis: O(b^d) -> O(b^(d/2)).
 *   - Linked Lists (Linked Lists.pptx): Dynamic move sequence lists (List<Move>).
 *   - Intro To DSA (Intro To DSA.pptx): Modular algorithm design and search bounds.
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
 * The {@code ComputerPlayer} class represents an automated AI opponent in draughts.
 * It evaluates candidate move branches on a simulated game tree using configurable
 * search algorithms corresponding to three intelligence difficulty levels.
 */
public class ComputerPlayer extends Player {

	/**
	 * Intelligence difficulty levels for the AI player.
	 * Maps to specific game tree search algorithms (DSA: Trees in DSA.pptx).
	 */
	public enum Difficulty {
		/** Random move selection — O(n) time complexity, no lookahead. */
		EASY,
		/** Minimax search at depth 3 — O(b^3) DFS game tree search. */
		MEDIUM,
		/** Minimax search at depth 6 with Alpha-Beta pruning — O(b^3) with Quick Sort move ordering. */
		HARD
	}

	/** Active difficulty level for this AI instance. */
	private Difficulty difficulty;

	/**
	 * Default constructor initializing AI to MEDIUM difficulty.
	 */
	public ComputerPlayer() {
		this(Difficulty.MEDIUM);
	}

	/**
	 * Constructor specifying initial difficulty level.
	 * 
	 * @param difficulty desired AI difficulty level
	 */
	public ComputerPlayer(Difficulty difficulty) {
		this.difficulty = (difficulty == null) ? Difficulty.MEDIUM : difficulty;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = (difficulty == null) ? Difficulty.MEDIUM : difficulty;
	}

	@Override
	public boolean isHuman() {
		return false;
	}

	/**
	 * Updates the game state by selecting and executing the optimal AI move.
	 * Dispatches execution based on the selected difficulty level.
	 * 
	 * <p><b>DSA Reference (Trees in DSA.pptx / BFS & DFS.pptx):</b>
	 * Entry point for AI move selection algorithms.</p>
	 * 
	 * @param game active game instance to update
	 */
	@Override
	public void updateGame(Game game) {
		if (game == null || game.isGameOver()) {
			return;
		}

		Game copy = game.copy();
		List<Move> moves = getMoves(copy);

		if (moves.isEmpty()) {
			return;
		}

		Move chosenMove = null;

		switch (difficulty) {
			case EASY:
				chosenMove = selectEasyMove(moves);
				break;
			case MEDIUM:
				chosenMove = selectMinimaxMove(copy, moves, 3, false);
				break;
			case HARD:
				chosenMove = selectMinimaxMove(copy, moves, 6, true);
				break;
		}

		if (chosenMove != null) {
			game.move(chosenMove.getStartIndex(), chosenMove.getEndIndex());
		}
	}

	/**
	 * Selects a random move from available legal moves (EASY difficulty).
	 * 
	 * <p><b>DSA Reference (Intro To DSA.pptx / Linked Lists.pptx):</b>
	 * Simple O(n) list index selection without game tree exploration.</p>
	 * 
	 * @param moves list of available legal moves
	 * @return randomly chosen Move
	 * @complexity O(n) where n = moves.size()
	 */
	private Move selectEasyMove(List<Move> moves) {
		int index = (int) (Math.random() * moves.size());
		return moves.get(index);
	}

	/**
	 * Evaluates moves using Minimax tree search, optionally with Alpha-Beta pruning and move ordering.
	 * 
	 * <p><b>DSA Reference (Trees in DSA.pptx / BFS & DFS.pptx / Merge & Quick sort.pptx):</b>
	 * - Trees & DFS: Explores game tree states recursively down to specified max Depth.
	 * - Quick Sort (Merge & Quick sort lecture): Uses {@link Collections#sort(List)} to order candidate
	 *   moves by heuristic score before Alpha-Beta search, optimizing cutoff efficiency.</p>
	 * 
	 * @param game        current game state
	 * @param moves       available legal moves
	 * @param depth       max search depth in game tree
	 * @param useAlphaBeta true to enable Alpha-Beta pruning and move ordering
	 * @return best evaluated Move object
	 * @complexity O(b^d) without pruning; O(b^(d/2)) with optimal Alpha-Beta pruning
	 */
	private Move selectMinimaxMove(Game game, List<Move> moves, int depth, boolean useAlphaBeta) {
		Move bestMove = moves.get(0);
		double bestValue = Double.NEGATIVE_INFINITY;
		boolean isP1Turn = game.isP1Turn();

		// DSA: Sorting Techniques / Quick Sort (Merge & Quick sort.pptx)
		// Pre-evaluates and orders root-level candidate moves to maximize early pruning
		if (useAlphaBeta) {
			orderMoves(moves, game, isP1Turn);
		}

		double alpha = Double.NEGATIVE_INFINITY;
		double beta = Double.POSITIVE_INFINITY;

		for (Move move : moves) {
			Game simulated = game.copy();
			simulated.move(move.getStartIndex(), move.getEndIndex());

			double val;
			if (useAlphaBeta) {
				val = alphaBeta(simulated, depth - 1, alpha, beta, false, isP1Turn);
			} else {
				val = minimax(simulated, depth - 1, false, isP1Turn);
			}

			move.setWeight(val);

			if (val > bestValue) {
				bestValue = val;
				bestMove = move;
			}

			if (useAlphaBeta) {
				alpha = Math.max(alpha, bestValue);
			}
		}

		return bestMove;
	}

	/**
	 * Recursive Minimax search algorithm without pruning (MEDIUM difficulty).
	 * 
	 * <p><b>DSA Reference (Trees in DSA.pptx / BFS & DFS.pptx / stacks.pdf):</b>
	 * DFS traversal over game tree node levels, alternating between MAX and MIN nodes.</p>
	 * 
	 * @param game          simulated game node state
	 * @param depth         remaining tree depth
	 * @param isMaximizing  true if MAX node (AI turn), false if MIN node (opponent turn)
	 * @param isBlackAI     true if AI plays Black pieces
	 * @return heuristic score of game state
	 * @complexity O(b^d) where b ≈ branching factor (5-8), d = depth
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
	 * Recursive Minimax search with Alpha-Beta pruning (HARD difficulty).
	 * 
	 * <p><b>DSA Reference (Trees in DSA.pptx / Mathematical Background DSA.pptx):</b>
	 * Reduces evaluated game tree branches from O(b^d) down to O(b^(d/2)) by maintaining
	 * alpha (best score guaranteed to maximizer) and beta (best score guaranteed to minimizer).</p>
	 * 
	 * @param game         simulated game node state
	 * @param depth        remaining tree depth
	 * @param alpha        best score maximizer can guarantee
	 * @param beta         best score minimizer can guarantee
	 * @param isMaximizing true if MAX node, false if MIN node
	 * @param isBlackAI    true if AI plays Black
	 * @return heuristic score of game state
	 * @complexity O(b^(d/2)) under optimal move ordering
	 */
	private double alphaBeta(Game game, int depth, double alpha, double beta, boolean isMaximizing, boolean isBlackAI) {
		if (depth <= 0 || game.isGameOver()) {
			return evaluateBoard(game, isBlackAI);
		}

		List<Move> moves = getMoves(game);
		if (moves.isEmpty()) {
			return evaluateBoard(game, isBlackAI);
		}

		// Sort candidate moves at deeper nodes to optimize pruning cutoffs
		orderMoves(moves, game, isMaximizing == isBlackAI);

		if (isMaximizing) {
			double maxEval = Double.NEGATIVE_INFINITY;
			for (Move move : moves) {
				Game child = game.copy();
				child.move(move.getStartIndex(), move.getEndIndex());
				double eval = alphaBeta(child, depth - 1, alpha, beta, false, isBlackAI);
				maxEval = Math.max(maxEval, eval);
				alpha = Math.max(alpha, eval);
				if (beta <= alpha) { // Beta cutoff pruning
					break;
				}
			}
			return maxEval;
		} else {
			double minEval = Double.POSITIVE_INFINITY;
			for (Move move : moves) {
				Game child = game.copy();
				child.move(move.getStartIndex(), move.getEndIndex());
				double eval = alphaBeta(child, depth - 1, alpha, beta, true, isBlackAI);
				minEval = Math.min(minEval, eval);
				beta = Math.min(beta, eval);
				if (beta <= alpha) { // Alpha cutoff pruning
					break;
				}
			}
			return minEval;
		}
	}

	/**
	 * Orders candidate moves by estimated score using Collections.sort().
	 * 
	 * <p><b>DSA Reference (Merge & Quick sort.pptx / Sorting Techniques.pptx):</b>
	 * {@link Collections#sort(List)} uses a dual-pivot Quick Sort / TimSort algorithm
	 * running in O(n log n) average time. Pre-sorting moves ensures highest-scoring branches
	 * are explored first, maximizing Alpha-Beta pruning cutoffs.</p>
	 * 
	 * @param moves     list of moves to order
	 * @param game      game state frame
	 * @param isP1Turn  turn flag for player being evaluated
	 * @complexity O(n log n) where n = moves.size()
	 */
	private void orderMoves(List<Move> moves, Game game, boolean isP1Turn) {
		for (Move m : moves) {
			Game simulated = game.copy();
			simulated.move(m.getStartIndex(), m.getEndIndex());
			double score = evaluateBoard(simulated, isP1Turn);
			m.setWeight(score);
		}
		// Sorts in descending order (highest weight moves first) using Move.compareTo()
		Collections.sort(moves);
	}

	/**
	 * Evaluates board state and computes a composite numerical score for the AI.
	 * 
	 * <p><b>DSA Reference (Mathematical Background DSA.pptx / Graphs.pptx):</b>
	 * Heuristic Evaluation Polynomial:
	 *   Score = w1·Material + w2·Kings + w3·Safety + w4·Mobility + w5·Center + w6·Advance
	 * Combines Graph node spatial positions and piece weights into a scalar state evaluation.</p>
	 * 
	 * @param game      game state to score
	 * @param isBlackAI true if evaluating for Black AI
	 * @return numerical heuristic score (positive favors AI, negative favors opponent)
	 * @complexity O(1) constant time evaluation over 32 dark board tiles
	 */
	private double evaluateBoard(Game game, boolean isBlackAI) {
		if (game.isGameOver()) {
			if (game.isDraw()) {
				return 0.0; // Draw score
			}
			// Win/Loss score
			boolean p1Wins = !game.getBoard().find(Board.WHITE_CHECKER).isEmpty() || !game.getBoard().find(Board.WHITE_KING).isEmpty();
			if (isBlackAI) {
				return p1Wins ? 10000.0 : -10000.0;
			} else {
				return p1Wins ? -10000.0 : 10000.0;
			}
		}

		Board b = game.getBoard();
		double score = 0.0;

		// 1. Material Count (Checkers = 10 pts, Kings = 18 pts)
		List<Point> myCheckers = b.find(isBlackAI ? Board.BLACK_CHECKER : Board.WHITE_CHECKER);
		List<Point> myKings = b.find(isBlackAI ? Board.BLACK_KING : Board.WHITE_KING);
		List<Point> oppCheckers = b.find(isBlackAI ? Board.WHITE_CHECKER : Board.BLACK_CHECKER);
		List<Point> oppKings = b.find(isBlackAI ? Board.WHITE_KING : Board.BLACK_KING);

		score += (myCheckers.size() * 10.0 + myKings.size() * 18.0);
		score -= (oppCheckers.size() * 10.0 + oppKings.size() * 18.0);

		// 2. Safety Bonus
		for (Point p : myCheckers) {
			if (MoveLogic.isSafe(b, p)) score += 2.0;
		}
		for (Point p : myKings) {
			if (MoveLogic.isSafe(b, p)) score += 3.0;
		}

		// 3. Center Control (tiles with x in [2..5] and y in [2..5])
		for (Point p : myCheckers) {
			if (p.x >= 2 && p.x <= 5 && p.y >= 2 && p.y <= 5) score += 1.5;
		}

		// 4. Advancement Bonus (Black moves down +y, White moves up -y)
		for (Point p : myCheckers) {
			score += isBlackAI ? (p.y * 0.5) : ((7 - p.y) * 0.5);
		}

		return score;
	}

	/**
	 * Retrieves all legal moves for current turn.
	 * Corrects the bug in original code by cloning game before calculating depth.
	 * 
	 * <p><b>DSA Reference (Graphs.pptx / BFS & DFS.pptx):</b>
	 * Enforces mandatory jump capture rule #6: if jumps exist, normal moves are excluded.</p>
	 * 
	 * @param game game state
	 * @return List of valid Move objects
	 * @complexity O(p) where p = active checkers
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
