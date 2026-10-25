package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class FrmPhieuKiemTra extends JPanel{
    
    public FrmPhieuKiemTra() {
        setLayout(new BorderLayout(0,8));
        setBackground(UIConfig.COLOR_BACKGROUND);

        setBorder(new EmptyBorder(14,22,14,22));
        
        add(createHeading() , BorderLayout.NORTH);
        add(createContent() , BorderLayout.CENTER);
    }

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel(new BorderLayout(0,5));
        pnlHeading.setOpaque(false);

        JLabel lblTitle = new JLabel("Phiếu kiểm tra");
        lblTitle.setFont(UIConfig.FONT_TITLE);
        lblTitle.setForeground(UIConfig.COLOR_TITLE);
        JLabel lblSubtitle = new AppLabel("Thông tin phiếu kiểm tra và danh sách phiếu kiểm tra");

        pnlHeading.add(lblTitle, BorderLayout.NORTH);
        pnlHeading.add(lblSubtitle, BorderLayout.CENTER);
        return pnlHeading;
    }

    private JPanel createContent() {
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);

        JPanel pnlForm = createFormPanel();
        pnlContent.setBorder(new EmptyBorder(0,0,0,0));

        return pnlContent;
    }

    private JPanel createFormPanel() {
        JPanel pnlFormCard = AppPanel.card(new BorderLayout());
        pnlFormCard.setLayout(new BorderLayout(0,3));
        pnlFormCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConfig.COLOR_BORDER),
        
        
                new EmptyBorder(7,12,7,12)));
        JLabel lblFormTitle = new JLabel("Thông tin phiếu kiểm tra");
        lblFormTitle.setFont(UIConfig.FONT_SECTION);
        lblFormTitle.setForeground(UIConfig.COLOR_TITLE);

        JPanel pnlFields =  new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);

        
        
        return pnlFormCard;
    }

    private void addField(JPanel pnlFields, String label, Component component, int column, int row) {
        GridBagConstraints gbcLabel = new GridBagConstraints();

        gbcLabel.gridx = column;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(2,0,2,8);
        JLabel lblField = new JLabel(label);
        lblField.setFont(UIConfig.FONT_REGULAR);
        lblField.setForeground(UIConfig.COLOR_TEXT);
        pnlFields.add(lblField, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = column + 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(2, 0, 2, 12);
        pnlFields.add(component, gbcInput);
    }

    

}
