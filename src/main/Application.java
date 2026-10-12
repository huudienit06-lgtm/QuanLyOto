package main;

import javax.swing.SwingUtilities;

import gui.FrmMain;

public class Application {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new FrmMain().setVisible(true));
	}
}
