package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableModel;

final class FormUI {
    private FormUI() {
    }

    static JPanel createPage(String title, String subtitle, Component content) {
        JPanel pnlPage = new JPanel(new BorderLayout(0, 16));
        pnlPage.setBackground(UIConfig.COLOR_BACKGROUND);
        pnlPage.setBorder(new javax.swing.border.EmptyBorder(22, 28, 22, 28));

        JPanel pnlHeading = new JPanel(new BorderLayout(0, 5));
        pnlHeading.setOpaque(false);
        JLabel lblTitle = new AppLabel(title, AppLabel.Style.TITLE);
        JLabel lblSubtitle = new AppLabel(subtitle, AppLabel.Style.MUTED);
        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);

        pnlPage.add(pnlHeading, BorderLayout.NORTH);
        pnlPage.add(content, BorderLayout.CENTER);
        return pnlPage;
    }

    static AppPanel createCard() {
        return AppPanel.card(new BorderLayout(0, 12));
    }

    static JLabel createSectionTitle(String text) {
        return new AppLabel(text, AppLabel.Style.SECTION);
    }

    static void addField(JPanel pnlFields, String label, Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);
        pnlFields.add(new AppLabel(label), gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(5, 0, 5, 18);
        pnlFields.add(component, gbcInput);
    }

    static JPanel createFieldsPanel() {
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        return pnlFields;
    }

    static JTable createTable(TableModel model) {
        AppTable tblData = new AppTable(model);
        tblData.setAutoCreateRowSorter(true);
        return tblData;
    }

    static JPanel createStatus(String text) {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(new AppLabel(text, AppLabel.Style.MUTED), BorderLayout.WEST);
        return pnlStatus;
    }

    static JPanel createTableCard(String title, Component toolbar, Component table) {
        JPanel pnlCard = createCard();
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.add(createSectionTitle(title), BorderLayout.WEST);
        if (toolbar != null) {
            pnlHeader.add(toolbar, BorderLayout.EAST);
        }
        pnlCard.add(pnlHeader, BorderLayout.NORTH);
        pnlCard.add(table, BorderLayout.CENTER);
        return pnlCard;
    }

    static GridBagConstraints buttonConstraints(int column) {
        GridBagConstraints gbcButton = new GridBagConstraints();
        gbcButton.gridx = column;
        gbcButton.gridy = 0;
        gbcButton.anchor = GridBagConstraints.WEST;
        gbcButton.insets = new Insets(0, 0, 0, 8);
        return gbcButton;
    }

    static JPanel createButtonRow(AppButton... buttons) {
        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setOpaque(false);
        for (int index = 0; index < buttons.length; index++) {
            pnlButtons.add(buttons[index], buttonConstraints(index));
        }
        return pnlButtons;
    }

    public static void showError(String string) {
        JOptionPane.showMessageDialog(null, string, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
