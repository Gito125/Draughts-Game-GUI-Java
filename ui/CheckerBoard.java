/*
 * ============================================================================
 * File:        CheckerBoard.java
 * Package:     ui
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Swing GUI component rendering the 8x8 draughts board, piece graphics,
 *              move/capture highlights, turn indicators, and game over / draw messages.
 *              Handles mouse click input for human players and manages execution timers
 *              for automated computer AI players.
 *
 * DSA Concepts Applied:
 *   - Graphs (Graphs.pptx): Visualizes the 32-tile graph board layout and renders
 *     movement path vectors.
 *   - Queues (Queues.pptx): Uses Swing Timer event dispatching for non-blocking AI turns.
 *   - Intro To DSA (Intro To DSA.pptx): GUI view rendering separated from state logic.
 * ============================================================================
 */

package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.Timer;

import logic.MoveGenerator;
import logic.MoveLogic;
import model.Board;
import model.Game;
import model.HumanPlayer;
import model.Player;

/**
 * The {@code CheckerBoard} class renders the visual board UI, handles user input,
 * and schedules computer AI turns via controlled delay timers.
 */
public class CheckerBoard extends JButton {

	private static final long serialVersionUID = -6014690893709316364L;
	
	/** Border padding (pixels). */
	private static final int PADDING = 16;

	/** Configurable millisecond delay between AI moves (default 1000ms). */
	private int timerDelay = 1000;

	/** Active checkers game state model. */
	private Game game;
	
	/** Parent CheckersWindow reference. */
	private CheckersWindow window;
	
	/** Player 1 (Black) controller. */
	private Player player1;
	
	/** Player 2 (White) controller. */
	private Player player2;
	
	/** Currently selected tile Point on the board. */
	private Point selected;
	
	/** Selection validity flag (true = green highlight, false = red highlight). */
	private boolean selectionValid;
	
	/** Light tile color. */
	private Color lightTile;

	/** Dark tile color. */
	private Color darkTile;
	
	/** Game over status flag. */
	private boolean isGameOver;
	
	/** Timer controlling AI turn delay execution (prevents timer leaks). */
	private Timer timer;
	
	public CheckerBoard(CheckersWindow window) {
		this(window, new Game(), null, null);
	}
	
	public CheckerBoard(CheckersWindow window, Game game, Player player1, Player player2) {
		super.setBorderPainted(false);
		super.setFocusPainted(false);
		super.setContentAreaFilled(false);
		super.setBackground(Color.LIGHT_GRAY);
		this.addActionListener(new ClickListener());
		
		this.game = (game == null) ? new Game() : game;
		this.lightTile = Color.WHITE;
		this.darkTile = Color.BLACK;
		this.window = window;
		setPlayer1(player1);
		setPlayer2(player2);
	}
	
	/**
	 * Configures dynamic AI turn delay speed.
	 * 
	 * @param delay delay in milliseconds (200ms to 3000ms)
	 */
	public void setTimerDelay(int delay) {
		this.timerDelay = Math.max(100, delay);
	}

	public int getTimerDelay() {
		return timerDelay;
	}

	/**
	 * Checks game termination status and repaints board component.
	 */
	public void update() {
		this.isGameOver = game.isGameOver();
		runPlayer();
		repaint();
	}
	
	/**
	 * Schedules AI move execution on a Swing Timer.
	 * Stops any existing running timer to prevent memory/timer leaks.
	 * 
	 * <p><b>DSA Reference (Queues.pptx):</b>
	 * Enqueues AI turn updates onto the Swing Event Dispatch Thread queue after specified delay.</p>
	 */
	private void runPlayer() {
		Player player = getCurrentPlayer();
		if (player == null || player.isHuman() || isGameOver) {
			return;
		}
		
		// Stop any existing active timer to fix timer leak bug
		if (this.timer != null && this.timer.isRunning()) {
			this.timer.stop();
		}
		
		this.timer = new Timer(timerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timer.stop();
				if (!game.isGameOver()) {
					getCurrentPlayer().updateGame(game);
				}
				update();
			}
		});
		this.timer.setRepeats(false);
		this.timer.start();
	}
	
	/**
	 * Draws the current draughts game state and UI highlights.
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Game game = this.game.copy();
		
		final int BOX_PADDING = 4;
		final int W = getWidth(), H = getHeight();
		final int DIM = W < H ? W : H, BOX_SIZE = (DIM - 2 * PADDING) / 8;
		final int OFFSET_X = (W - BOX_SIZE * 8) / 2;
		final int OFFSET_Y = (H - BOX_SIZE * 8) / 2;
		final int CHECKER_SIZE = Math.max(0, BOX_SIZE - 2 * BOX_PADDING);
		
		// Draw board background
		g.setColor(Color.BLACK);
		g.drawRect(OFFSET_X - 1, OFFSET_Y - 1, BOX_SIZE * 8 + 1, BOX_SIZE * 8 + 1);
		g.setColor(lightTile);
		g.fillRect(OFFSET_X, OFFSET_Y, BOX_SIZE * 8, BOX_SIZE * 8);
		g.setColor(darkTile);
		for (int y = 0; y < 8; y++) {
			for (int x = (y + 1) % 2; x < 8; x += 2) {
				g.fillRect(OFFSET_X + x * BOX_SIZE, OFFSET_Y + y * BOX_SIZE, BOX_SIZE, BOX_SIZE);
			}
		}
		
		// Highlight selected tile and legal moves
		if (Board.isValidPoint(selected)) {
			if (selectionValid) {
				// Highlight selected tile in green
				g.setColor(new Color(0, 255, 120, 130));
				g.fillRect(OFFSET_X + selected.x * BOX_SIZE, OFFSET_Y + selected.y * BOX_SIZE, BOX_SIZE, BOX_SIZE);
				g.setColor(new Color(0, 180, 80));
				g.drawRect(OFFSET_X + selected.x * BOX_SIZE, OFFSET_Y + selected.y * BOX_SIZE, BOX_SIZE - 1, BOX_SIZE - 1);
				
				// Highlight legal destination tiles
				List<Point> legalMoves = getLegalMoves(selected);
				for (Point p : legalMoves) {
					int px = OFFSET_X + p.x * BOX_SIZE;
					int py = OFFSET_Y + p.y * BOX_SIZE;
					boolean isSkip = Math.abs(p.x - selected.x) == 2;
					
					if (isSkip) {
						Point mid = Board.middle(selected, p);
						if (Board.isValidPoint(mid)) {
							int mx = OFFSET_X + mid.x * BOX_SIZE;
							int my = OFFSET_Y + mid.y * BOX_SIZE;
							g.setColor(new Color(255, 40, 40, 140));
							g.fillRect(mx, my, BOX_SIZE, BOX_SIZE);
						}
						g.setColor(new Color(255, 215, 0, 120));
						g.fillRect(px, py, BOX_SIZE, BOX_SIZE);
						g.setColor(new Color(255, 140, 0));
						g.drawRect(px, py, BOX_SIZE - 1, BOX_SIZE - 1);
					} else {
						g.setColor(new Color(50, 205, 50, 110));
						g.fillRect(px, py, BOX_SIZE, BOX_SIZE);
						g.setColor(new Color(34, 139, 34));
						g.drawRect(px, py, BOX_SIZE - 1, BOX_SIZE - 1);
					}
				}
			} else {
				// Highlight invalid selection in red
				g.setColor(Color.RED);
				g.fillRect(OFFSET_X + selected.x * BOX_SIZE, OFFSET_Y + selected.y * BOX_SIZE, BOX_SIZE, BOX_SIZE);
			}
		}

		// Draw checkers
		Board b = game.getBoard();
		for (int y = 0; y < 8; y++) {
			int cy = OFFSET_Y + y * BOX_SIZE + BOX_PADDING;
			for (int x = (y + 1) % 2; x < 8; x += 2) {
				int id = b.get(x, y);
				if (id == Board.EMPTY) {
					continue;
				}
				int cx = OFFSET_X + x * BOX_SIZE + BOX_PADDING;
				
				if (id == Board.BLACK_CHECKER) {
					g.setColor(Color.DARK_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.BLACK);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
				} else if (id == Board.BLACK_KING) {
					g.setColor(Color.DARK_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.BLACK);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.YELLOW);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
				} else if (id == Board.WHITE_CHECKER) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.WHITE);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
				} else if (id == Board.WHITE_KING) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.WHITE);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.YELLOW);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
				}
				
				if (Board.isKingChecker(id)) {
					g.setColor(new Color(255, 240, 0));
					g.drawOval(cx - 1, cy - 2, CHECKER_SIZE, CHECKER_SIZE);
				}
			}
		}
		
		// Draw turn indicator sign
		String msg = game.isP1Turn() ? "Player 1's turn (Black)" : "Player 2's turn (White)";
		int width = g.getFontMetrics().stringWidth(msg);
		Color back = game.isP1Turn() ? Color.BLACK : Color.WHITE;
		Color front = game.isP1Turn() ? Color.WHITE : Color.BLACK;
		g.setColor(back);
		g.fillRect(W / 2 - width / 2 - 5, OFFSET_Y + 8 * BOX_SIZE + 2, width + 10, 15);
		g.setColor(front);
		g.drawString(msg, W / 2 - width / 2, OFFSET_Y + 8 * BOX_SIZE + 2 + 12);
		
		// Draw game over / draw message overlay
		if (isGameOver) {
			g.setFont(new Font("Arial", Font.BOLD, 18));
			String overMsg = "Game Over!";
			if (this.game.isDraw()) {
				overMsg = this.game.getDrawReason();
			} else {
				overMsg = game.isP1Turn() ? "Game Over! Player 2 Wins!" : "Game Over! Player 1 Wins!";
			}
			int oWidth = g.getFontMetrics().stringWidth(overMsg);
			g.setColor(new Color(240, 240, 255, 230));
			g.fillRoundRect(W / 2 - oWidth / 2 - 10, OFFSET_Y + BOX_SIZE * 4 - 20, oWidth + 20, 36, 10, 10);
			g.setColor(Color.RED);
			g.drawRoundRect(W / 2 - oWidth / 2 - 10, OFFSET_Y + BOX_SIZE * 4 - 20, oWidth + 20, 36, 10, 10);
			g.drawString(overMsg, W / 2 - oWidth / 2, OFFSET_Y + BOX_SIZE * 4 + 4);
		}
	}
	
	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = (game == null) ? new Game() : game;
	}

	public CheckersWindow getWindow() {
		return window;
	}

	public void setWindow(CheckersWindow window) {
		this.window = window;
	}

	public Player getPlayer1() {
		return player1;
	}

	public void setPlayer1(Player player1) {
		this.player1 = (player1 == null) ? new HumanPlayer() : player1;
		if (game.isP1Turn() && !this.player1.isHuman()) {
			this.selected = null;
		}
	}

	public Player getPlayer2() {
		return player2;
	}

	public void setPlayer2(Player player2) {
		this.player2 = (player2 == null) ? new HumanPlayer() : player2;
		if (!game.isP1Turn() && !this.player2.isHuman()) {
			this.selected = null;
		}
	}
	
	public Player getCurrentPlayer() {
		return game.isP1Turn() ? player1 : player2;
	}

	/**
	 * Handles GUI mouse clicks for human players.
	 * 
	 * @param x click x-coordinate
	 * @param y click y-coordinate
	 */
	private void handleClick(int x, int y) {
		if (isGameOver || !getCurrentPlayer().isHuman()) {
			return;
		}
		
		Game copy = game.copy();
		
		final int W = getWidth(), H = getHeight();
		final int DIM = W < H ? W : H, BOX_SIZE = (DIM - 2 * PADDING) / 8;
		final int OFFSET_X = (W - BOX_SIZE * 8) / 2;
		final int OFFSET_Y = (H - BOX_SIZE * 8) / 2;
		x = (x - OFFSET_X) / BOX_SIZE;
		y = (y - OFFSET_Y) / BOX_SIZE;
		Point sel = new Point(x, y);
		
		if (Board.isValidPoint(sel) && Board.isValidPoint(selected)) {
			boolean change = copy.isP1Turn();
			boolean move = copy.move(selected, sel);
			if (move) {
				this.game = copy;
			}
			change = (copy.isP1Turn() != change);
			if (change) {
				this.selected = null;
			} else if (game.getSkipIndex() >= 0) {
				this.selected = Board.toPoint(game.getSkipIndex());
			} else {
				this.selected = sel;
			}
		} else {
			if (game.getSkipIndex() >= 0) {
				this.selected = Board.toPoint(game.getSkipIndex());
			} else {
				this.selected = sel;
			}
		}
		
		this.selectionValid = isValidSelection(copy.getBoard(), copy.isP1Turn(), selected);
		update();
	}
	
	private boolean isValidSelection(Board b, boolean isP1Turn, Point selected) {
		int i = Board.toIndex(selected);
		if (!Board.isValidIndex(i)) {
			return false;
		}
		int id = b.get(i);
		if (id == Board.EMPTY || id == Board.INVALID) {
			return false;
		} else if (isP1Turn ^ Board.isBlackChecker(id)) {
			return false;
		} else if (game.getSkipIndex() >= 0 && i != game.getSkipIndex()) {
			return false;
		} else if (!MoveGenerator.getSkips(b, i).isEmpty()) {
			return true;
		} else if (MoveGenerator.getMoves(b, i).isEmpty()) {
			return false;
		}
		
		List<Point> points = b.find(isP1Turn ? Board.BLACK_CHECKER : Board.WHITE_CHECKER);
		points.addAll(b.find(isP1Turn ? Board.BLACK_KING : Board.WHITE_KING));
		for (Point p : points) {
			int checker = Board.toIndex(p);
			if (checker == i) {
				continue;
			}
			if (!MoveGenerator.getSkips(b, checker).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public List<Point> getLegalMoves(Point selected) {
		List<Point> legalMoves = new ArrayList<>();
		if (game == null || selected == null || !Board.isValidPoint(selected)) {
			return legalMoves;
		}
		
		int startIndex = Board.toIndex(selected);
		Board b = game.getBoard();
		
		List<Point> skips = MoveGenerator.getSkips(b, startIndex);
		for (Point end : skips) {
			if (MoveLogic.isValidMove(game, startIndex, Board.toIndex(end))) {
				legalMoves.add(end);
			}
		}
		
		List<Point> moves = MoveGenerator.getMoves(b, startIndex);
		for (Point end : moves) {
			if (MoveLogic.isValidMove(game, startIndex, Board.toIndex(end))) {
				legalMoves.add(end);
			}
		}
		
		return legalMoves;
	}

	private class ClickListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Point m = CheckerBoard.this.getMousePosition();
			if (m != null) {
				handleClick(m.x, m.y);
			}
		}
	}
}
