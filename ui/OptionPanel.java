/*
 * ============================================================================
 * File:        OptionPanel.java
 * Package:     ui
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: User interface control panel allowing players to start/restart games,
 *              select player controllers (Human vs Computer), set AI difficulty
 *              levels (Easy, Medium, Hard), and adjust dynamic move delay speed.
 *
 * DSA Concepts Applied:
 *   - Queues (Queues.pptx): Event queue handling for Swing UI ActionListeners.
 *   - Intro To DSA (Intro To DSA.pptx): User interface abstraction and dynamic
 *     component visibility management.
 * ============================================================================
 */

package ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.ComputerPlayer;
import model.ComputerPlayer.Difficulty;
import model.HumanPlayer;
import model.Player;

/**
 * The {@code OptionPanel} class provides Swing controls for configuring draughts game settings,
 * including controller types, AI difficulty levels, start/restart buttons, and game execution speed.
 */
public class OptionPanel extends JPanel {

	private static final long serialVersionUID = -4763875452164030755L;

	/** Parent CheckersWindow reference. */
	private CheckersWindow window;
	
	/** Button to start a new game session. */
	private JButton startBtn;

	/** Button to restart the game session. */
	private JButton restartBtn;
	
	/** Player 1 controller selection combo box ("Human", "Computer"). */
	private JComboBox<String> player1Opts;
	
	/** Player 1 AI difficulty combo box ("Easy", "Medium", "Hard"). */
	private JComboBox<String> player1Diff;

	/** Player 2 controller selection combo box ("Human", "Computer"). */
	private JComboBox<String> player2Opts;

	/** Player 2 AI difficulty combo box ("Easy", "Medium", "Hard"). */
	private JComboBox<String> player2Diff;
	
	/** Slider controlling AI turn delay in milliseconds (200ms to 3000ms). */
	private JSlider speedSlider;

	/** Label displaying current speed slider delay value. */
	private JLabel speedLabel;
	
	/**
	 * Constructs a new OptionPanel for the specified window.
	 * 
	 * @param window parent window
	 */
	public OptionPanel(CheckersWindow window) {
		super(new GridLayout(0, 1));
		this.window = window;
		
		OptionListener ol = new OptionListener();
		final String[] playerTypeOpts = {"Human", "Computer"};
		final String[] diffOpts = {"Easy", "Medium", "Hard"};

		this.startBtn = new JButton("Start Game");
		this.restartBtn = new JButton("Restart Game");
		this.startBtn.addActionListener(ol);
		this.restartBtn.addActionListener(ol);

		this.player1Opts = new JComboBox<>(playerTypeOpts);
		this.player2Opts = new JComboBox<>(playerTypeOpts);
		this.player1Opts.addActionListener(ol);
		this.player2Opts.addActionListener(ol);

		this.player1Diff = new JComboBox<>(diffOpts);
		this.player2Diff = new JComboBox<>(diffOpts);
		this.player1Diff.setSelectedItem("Medium");
		this.player2Diff.setSelectedItem("Medium");
		this.player1Diff.addActionListener(ol);
		this.player2Diff.addActionListener(ol);
		this.player1Diff.setVisible(false);
		this.player2Diff.setVisible(false);

		// Speed slider setup: 200ms (fast) to 3000ms (slow), default 1000ms
		this.speedSlider = new JSlider(JSlider.HORIZONTAL, 200, 3000, 1000);
		this.speedLabel = new JLabel("Speed: 1.0s");
		this.speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int delay = speedSlider.getValue();
				speedLabel.setText(String.format("Speed: %.1fs", delay / 1000.0));
				if (OptionPanel.this.window != null && OptionPanel.this.window.getBoard() != null) {
					OptionPanel.this.window.getBoard().setTimerDelay(delay);
				}
			}
		});

		JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel middle = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		top.add(startBtn);
		top.add(restartBtn);
		
		middle.add(new JLabel("(black) P1: "));
		middle.add(player1Opts);
		middle.add(new JLabel("AI: "));
		middle.add(player1Diff);

		bottom.add(new JLabel("(white) P2: "));
		bottom.add(player2Opts);
		bottom.add(new JLabel("AI: "));
		bottom.add(player2Diff);

		speedPanel.add(new JLabel("AI Speed: "));
		speedPanel.add(speedSlider);
		speedPanel.add(speedLabel);

		this.add(top);
		this.add(middle);
		this.add(bottom);
		this.add(speedPanel);
	}

	public CheckersWindow getWindow() {
		return window;
	}

	public void setWindow(CheckersWindow window) {
		this.window = window;
	}

	/**
	 * Instantiates a new Player object based on selection boxes.
	 * 
	 * @param playerOpts controller selection box
	 * @param diffOpts   difficulty selection box
	 * @return Player instance (HumanPlayer or ComputerPlayer)
	 */
	private static Player getPlayer(JComboBox<String> playerOpts, JComboBox<String> diffOpts) {
		if (playerOpts == null) {
			return new HumanPlayer();
		}
		
		String type = "" + playerOpts.getSelectedItem();
		if (type.equals("Computer")) {
			Difficulty diff = Difficulty.MEDIUM;
			if (diffOpts != null) {
				String sel = "" + diffOpts.getSelectedItem();
				if (sel.equals("Easy")) diff = Difficulty.EASY;
				else if (sel.equals("Hard")) diff = Difficulty.HARD;
			}
			return new ComputerPlayer(diff);
		}
		
		return new HumanPlayer();
	}
	
	/**
	 * ActionListener responding to component selection events.
	 */
	private class OptionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (window == null) {
				return;
			}
			
			Object src = e.getSource();

			if (src == startBtn || src == restartBtn) {
				window.restart();
			} else if (src == player1Opts || src == player1Diff) {
				boolean isComp = "Computer".equals(player1Opts.getSelectedItem());
				player1Diff.setVisible(isComp);
				Player player = getPlayer(player1Opts, player1Diff);
				window.setPlayer1(player);
				revalidate();
				repaint();
			} else if (src == player2Opts || src == player2Diff) {
				boolean isComp = "Computer".equals(player2Opts.getSelectedItem());
				player2Diff.setVisible(isComp);
				Player player = getPlayer(player2Opts, player2Diff);
				window.setPlayer2(player);
				revalidate();
				repaint();
			}
		}
	}
}
