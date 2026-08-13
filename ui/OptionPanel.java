/*
 * ============================================================================
 * File:        OptionPanel.java
 * Package:     ui
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              (Original Author: Devon McGrath)
 * Course:      Data Structures and Algorithms
 * 
 * Description: User interface control panel allowing players to start/restart games
 *              and select player controllers (Human vs Computer).
 *
 * DSA Concepts Applied:
 *   - Event Dispatching: Handles Swing UI ActionListeners for user interaction.
 *   - GUI Component Layout: Modular UI control panel layout.
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

/**
 * The {@code OptionPanel} class provides Swing controls for configuring draughts game settings,
 * including controller types (Human/Computer) and start/restart buttons.
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

	/** Player 2 controller selection combo box ("Human", "Computer"). */
	private JComboBox<String> player2Opts;
	
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

		this.startBtn = new JButton("Start Game");
		this.restartBtn = new JButton("Restart Game");
		this.startBtn.addActionListener(ol);
		this.restartBtn.addActionListener(ol);

		this.player1Opts = new JComboBox<>(playerTypeOpts);
		this.player2Opts = new JComboBox<>(playerTypeOpts);
		this.player1Opts.addActionListener(ol);
		this.player2Opts.addActionListener(ol);

		JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel middle = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));

		top.add(startBtn);
		top.add(restartBtn);
		
		middle.add(new JLabel("(black) P1: "));
		middle.add(player1Opts);

		bottom.add(new JLabel("(white) P2: "));
		bottom.add(player2Opts);

		this.add(top);
		this.add(middle);
		this.add(bottom);
	}

	public CheckersWindow getWindow() {
		return window;
	}

	public void setWindow(CheckersWindow window) {
		this.window = window;
	}

	/**
	 * Returns true if the selected option for a player is "Computer".
	 * 
	 * @param playerOpts combo box selection
	 * @return true if Computer selected
	 */
	private static boolean isComputer(JComboBox<String> playerOpts) {
		if (playerOpts == null) {
			return false;
		}
		return "Computer".equals(playerOpts.getSelectedItem());
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
			} else if (src == player1Opts) {
				window.setP1IsComputer(isComputer(player1Opts));
			} else if (src == player2Opts) {
				window.setP2IsComputer(isComputer(player2Opts));
			}
		}
	}
}
