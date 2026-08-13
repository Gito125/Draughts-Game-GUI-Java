/*
 * ============================================================================
 * File:        Main.java
 * Package:     ui
 * Authors:     Group 3 — Precious, Gideon, Peter
 *              
 * Course:      Data Structures and Algorithms (2205 ST) — Y2T2
 * 
 * Description: Entry point for the Draft GUI application. Configures System
 *              Look-and-Feel and instantiates the main CheckersWindow GUI.
 * ============================================================================
 */

package ui;

import javax.swing.UIManager;

/**
 * The {@code Main} class contains the application main entry point.
 */
public class Main {

	/**
	 * Main entry point method for the checkers application.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		
		// Set native operating system look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Instantiate and display main checkers window
		CheckersWindow window = new CheckersWindow();
		window.setDefaultCloseOperation(CheckersWindow.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
}
