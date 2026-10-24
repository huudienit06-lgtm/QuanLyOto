package gui;

import java.awt.Color;
import java.awt.Cursor;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class AppButton extends JButton {
    public enum Style {
        PRIMARY(UIConfig.COLOR_PRIMARY, Color.WHITE),
        SUCCESS(UIConfig.COLOR_SUCCESS, Color.WHITE),
        DANGER(UIConfig.COLOR_DANGER, Color.WHITE),
        SECONDARY(UIConfig.COLOR_SECONDARY, UIConfig.COLOR_TEXT);

        private final Color background;
        private final Color foreground;

        Style(Color background, Color foreground) {
            this.background = background;
            this.foreground = foreground;
        }
    }

    public AppButton(String text) {
        this(text, Style.PRIMARY);
    }

    public AppButton(String text, Style style) {
        super(text);
        setFont(UIConfig.FONT_BOLD);
        setBackground(style.background);
        setForeground(style.foreground);
        setFocusPainted(false);
        setBorder(new EmptyBorder(8, 13, 8, 13));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
