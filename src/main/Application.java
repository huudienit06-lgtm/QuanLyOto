package main;


import gui.FrmMain;
import gui.UIConfig;
import javax.swing.SwingUtilities;
public class Application {
	public static void main(String[] args) {
		UIConfig.installTheme();
		SwingUtilities.invokeLater(() -> new FrmMain().setVisible(true));
	}
}
