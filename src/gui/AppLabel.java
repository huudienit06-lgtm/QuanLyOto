package gui;

import javax.swing.JLabel;

public class AppLabel extends JLabel {
    public enum Style {
        BODY(UIConfig.FONT_REGULAR, UIConfig.COLOR_TEXT),
        MUTED(UIConfig.FONT_REGULAR, UIConfig.COLOR_MUTED),
        SECTION(UIConfig.FONT_SECTION, UIConfig.COLOR_TITLE),
        TITLE(UIConfig.FONT_TITLE, UIConfig.COLOR_TITLE);

        private final java.awt.Font font;
        private final java.awt.Color color;

        Style(java.awt.Font font, java.awt.Color color) {
            this.font = font;
            this.color = color;
        }
    }

    public AppLabel(String text) {
        this(text, Style.BODY);
    }

    public AppLabel(String text, Style style) {
        super(text);
        setFont(style.font);
        setForeground(style.color);
    }
}
