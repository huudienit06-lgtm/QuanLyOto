package gui;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class AppTable extends JTable {
    public AppTable(TableModel model) {
        super(model);
        setRowHeight(30);
        setFont(UIConfig.FONT_REGULAR);
        setForeground(UIConfig.COLOR_TEXT);
        setGridColor(UIConfig.COLOR_TABLE_GRID);
        setSelectionBackground(UIConfig.COLOR_SELECTION);
        setSelectionForeground(UIConfig.COLOR_TEXT);
        getTableHeader().setFont(UIConfig.FONT_BOLD);
        getTableHeader().setForeground(UIConfig.COLOR_TITLE);
        getTableHeader().setBackground(UIConfig.COLOR_TABLE_HEADER);
        getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
    }
}
