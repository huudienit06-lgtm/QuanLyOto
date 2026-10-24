package gui;

import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class AppTextField extends JTextField {
    public AppTextField() {
        this(0);
    }

    public AppTextField(int columns) {
        super(columns);
        setFont(UIConfig.FONT_REGULAR);
        setForeground(UIConfig.COLOR_TEXT);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConfig.COLOR_BORDER, 1, true),
                new EmptyBorder(6, 9, 6, 9)));
    }
}
