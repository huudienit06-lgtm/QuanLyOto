package gui;

import javax.swing.JComboBox;

public class AppComboBox<E> extends JComboBox<E> {
    public AppComboBox() {
        super();
        setFont(UIConfig.FONT_REGULAR);
        setForeground(UIConfig.COLOR_TEXT);
    }
}
