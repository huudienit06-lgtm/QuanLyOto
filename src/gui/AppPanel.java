package gui;

import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AppPanel extends JPanel {
    public AppPanel(LayoutManager layout) {
        super(layout);
    }

    public static AppPanel card(LayoutManager layout) {
        AppPanel pnlCard = new AppPanel(layout);
        pnlCard.setBackground(UIConfig.COLOR_SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConfig.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        return pnlCard;
    }
}
