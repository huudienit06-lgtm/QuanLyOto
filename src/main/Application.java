package main;


import gui.FrmMain;
import javax.swing.SwingUtilities;
public class Application {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new FrmMain().setVisible(true));
	}
}
