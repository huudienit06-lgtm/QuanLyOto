package gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

public class AppScrollPane extends JScrollPane {
    public AppScrollPane(Component component) {
        super(component);
        setBorder(BorderFactory.createLineBorder(UIConfig.COLOR_BORDER));
        getViewport().setBackground(Color.WHITE);
    }
}
