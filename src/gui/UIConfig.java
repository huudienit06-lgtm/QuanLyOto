package gui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

public final class UIConfig {
    public static final Color COLOR_PRIMARY = new Color(36, 99, 235);
    public static final Color COLOR_SUCCESS = new Color(38, 132, 93);
    public static final Color COLOR_DANGER = new Color(210, 65, 65);
    public static final Color COLOR_BACKGROUND = new Color(242, 247, 255);
    public static final Color COLOR_SURFACE = Color.WHITE;
    public static final Color COLOR_TEXT = new Color(47, 62, 84);
    public static final Color COLOR_TITLE = new Color(32, 55, 92);
    public static final Color COLOR_MUTED = new Color(112, 128, 150);
    public static final Color COLOR_BORDER = new Color(221, 231, 245);
    public static final Color COLOR_TABLE_HEADER = new Color(239, 245, 253);
    public static final Color COLOR_TABLE_GRID = new Color(232, 238, 247);
    public static final Color COLOR_SELECTION = new Color(225, 239, 255);
    public static final Color COLOR_SECONDARY = new Color(235, 241, 250);

    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 23);

    private UIConfig() {
    }

    public static void installTheme() {
        UIManager.put("defaultFont", FONT_REGULAR);
        UIManager.put("Label.font", FONT_REGULAR);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("TextField.font", FONT_REGULAR);
        UIManager.put("ComboBox.font", FONT_REGULAR);
        UIManager.put("Table.font", FONT_REGULAR);
        UIManager.put("TableHeader.font", FONT_BOLD);
        UIManager.put("Component.focusColor", COLOR_PRIMARY);
    }
}
