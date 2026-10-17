package main;

import javax.swing.SwingUtilities;

import gui.FrmMain;
import gui.UIConfig;

public class Application {
	public static void main(String[] args) {
		UIConfig.installTheme();
		SwingUtilities.invokeLater(() -> new FrmMain().setVisible(true));
	}
}
