package gui;

import entity.TaiKhoan;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

final class MenuBarSupport {
	private static final Color BLUE = new Color(36, 99, 235);
	private static final Color HOVER_BLUE = new Color(225, 239, 255);
	private static final Color TEXT = new Color(47, 62, 84);

	private MenuBarSupport() {
	}

	static JMenu createMenu(String title, MenuIcon.Type iconType) {
		JMenu mnuMenu = new JMenu(title);
		mnuMenu.setIcon(new MenuIcon(iconType, Color.WHITE));
		mnuMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
		mnuMenu.setForeground(Color.WHITE);
		mnuMenu.setOpaque(true);
		mnuMenu.setBackground(BLUE);
		mnuMenu.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
		mnuMenu.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		mnuMenu.getPopupMenu().setBackground(Color.WHITE);
		mnuMenu.getPopupMenu().setBorder(BorderFactory.createLineBorder(new Color(220, 230, 244)));
		return mnuMenu;
	}

	static JMenuItem createItem(String title, BaseFrame frame) {
		JMenuItem mniItem = new JMenuItem(title);
		mniItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mniItem.setPreferredSize(new Dimension(250, 36));
		mniItem.setBackground(Color.WHITE);
		mniItem.setForeground(TEXT);
		mniItem.setBorder(new EmptyBorder(5, 12, 5, 12));
		mniItem.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent event) {
				mniItem.setBackground(HOVER_BLUE);
			}

			@Override
			public void mouseExited(MouseEvent event) {
				mniItem.setBackground(Color.WHITE);
			}
		});
		mniItem.addActionListener(event -> frame.onMenuAction(title));
		return mniItem;
	}

	static void configure(JMenuBar menuBar, JMenu[] menus, TaiKhoan taiKhoan) {
		menuBar.removeAll();
		menuBar.setLayout(new GridBagLayout());
		menuBar.setOpaque(true);
		menuBar.setBackground(BLUE);
		menuBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;
		gbc.insets = new Insets(0, 1, 0, 1);
		for (int index = 0; index < menus.length; index++) {
			gbc.gridx = index;
			gbc.weightx = 0.7 / menus.length;
			menuBar.add(menus[index], gbc);
		}

		gbc.gridx = menus.length;
		gbc.weightx = 0.3;
		gbc.fill = GridBagConstraints.BOTH;
		menuBar.add(createAccountPanel(taiKhoan), gbc);
		menuBar.setPreferredSize(new Dimension(0, 58));
	}

	private static JPanel createAccountPanel(TaiKhoan taiKhoan) {
		JPanel pnlAccount = new JPanel(new GridBagLayout());
		pnlAccount.setOpaque(true);
		pnlAccount.setBackground(new Color(28, 78, 166));
		pnlAccount.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(100, 151, 241)),
				new EmptyBorder(4, 12, 4, 12)));

		JLabel lblIcon = new JLabel(new MenuIcon(MenuIcon.Type.USER, Color.WHITE));
		lblIcon.setBorder(new EmptyBorder(0, 0, 0, 10));
		String text;
		if (taiKhoan == null) {
			text = "<html><b>Chào mừng</b><br>Đăng nhập để sử dụng các chức năng</html>";
		} else {
			String hoTenNhanVien = escapeHtml(taiKhoan.getHoTenNhanVien());
			String tenDangNhap = escapeHtml(taiKhoan.getTenDangNhap());
			String maNhanVien = escapeHtml(taiKhoan.getMaNV());
			String vaiTro = escapeHtml(taiKhoan.getVaiTro());
			String tenHienThi = "—".equals(hoTenNhanVien) ? tenDangNhap : hoTenNhanVien;
			text = "<html><b>Xin chào, " + tenHienThi + "</b><br>Mã NV: "
					+ maNhanVien + " &nbsp;|&nbsp; " + vaiTro + "</html>";
		}
		JLabel lblAccountInfo = new JLabel(text, SwingConstants.LEFT);
		lblAccountInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		lblAccountInfo.setForeground(Color.WHITE);
		GridBagConstraints gbcIcon = new GridBagConstraints();
		gbcIcon.gridx = 0;
		gbcIcon.gridy = 0;
		gbcIcon.insets = new Insets(0, 0, 0, 8);
		pnlAccount.add(lblIcon, gbcIcon);
		GridBagConstraints gbcInfo = new GridBagConstraints();
		gbcInfo.gridx = 1;
		gbcInfo.gridy = 0;
		gbcInfo.weightx = 1;
		gbcInfo.fill = GridBagConstraints.HORIZONTAL;
		gbcInfo.anchor = GridBagConstraints.WEST;
		pnlAccount.add(lblAccountInfo, gbcInfo);
		pnlAccount.setToolTipText(taiKhoan == null
				? "Đăng nhập để sử dụng các chức năng"
				: "Thông tin tài khoản đang đăng nhập");
		return pnlAccount;
	}

	private static String escapeHtml(String value) {
		if (value == null || value.isBlank()) {
			return "—";
		}
		return value.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
