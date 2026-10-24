package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class NhanVienFrame extends JFrame {
	private static final Color BLUE = new Color(36, 99, 235);
	private static final Color PALE_BLUE = new Color(242, 247, 255);
	private static final Color TEXT = new Color(47, 62, 84);
	private static final Color MUTED = new Color(112, 128, 150);
	private final JLabel lblStatus = new JLabel("Sẵn sàng");

	public NhanVienFrame() {
		this("Quản Lý Gara Oto");
	}

	protected NhanVienFrame(String title) {
		this(title, true, true);
	}

	protected NhanVienFrame(String title, boolean showDefaultMenu, boolean showDefaultWorkspace) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(1050, 650));
		setSize(1220, 760);
		setLocationRelativeTo(null);
		getContentPane().setBackground(Color.WHITE);
		if (showDefaultMenu) {
			setJMenuBar(createMenuBar());
		}
		if (showDefaultWorkspace) {
			add(createWorkspace(), BorderLayout.CENTER);
		}
		add(createStatusBar(), BorderLayout.SOUTH);
	}

	protected void onMenuAction(String featureName) {
		lblStatus.setText("Đã chọn: " + featureName);
	}

	protected JPanel createRoleMenuWorkspace(String title, String[][] categories) {
		JPanel pnlWorkspace = new JPanel(new BorderLayout());
		pnlWorkspace.setBackground(PALE_BLUE);
		pnlWorkspace.setBorder(new EmptyBorder(12, 18, 18, 18));

		JLabel lblTitle = new JLabel(title);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblTitle.setForeground(new Color(43, 57, 112));
		lblTitle.setBorder(new EmptyBorder(0, 7, 16, 0));

		JPanel pnlMenuGrid = new JPanel(new GridLayout(1, categories.length, 8, 0));
		pnlMenuGrid.setOpaque(false);
		for (String[] category : categories) {
			pnlMenuGrid.add(createRoleMenuColumn(category));
		}

		pnlWorkspace.add(lblTitle, BorderLayout.NORTH);
		pnlWorkspace.add(pnlMenuGrid, BorderLayout.CENTER);
		return pnlWorkspace;
	}

	private JPanel createRoleMenuColumn(String[] items) {
		JPanel pnlColumn = new JPanel(new BorderLayout());
		pnlColumn.setOpaque(false);

		JLabel lblCategory = new JLabel(items[0], SwingConstants.CENTER);
		lblCategory.setOpaque(true);
		lblCategory.setBackground(BLUE);
		lblCategory.setForeground(Color.WHITE);
		lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblCategory.setPreferredSize(new Dimension(0, 36));
		pnlColumn.add(lblCategory, BorderLayout.NORTH);

		JPanel pnlItems = new JPanel();
		pnlItems.setLayout(new BoxLayout(pnlItems, BoxLayout.Y_AXIS));
		pnlItems.setBackground(Color.WHITE);
		pnlItems.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 230, 244)),
				new EmptyBorder(4, 4, 4, 4)));

		for (int index = 1; index < items.length; index++) {
			String featureName = items[index];
			JMenuItem mniFeature = new JMenuItem(featureName);
			mniFeature.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			mniFeature.setForeground(TEXT);
			mniFeature.setBackground(Color.WHITE);
			mniFeature.setOpaque(true);
			mniFeature.setBorder(new EmptyBorder(7, 6, 7, 6));
			mniFeature.setHorizontalAlignment(SwingConstants.LEFT);
			mniFeature.addActionListener(event -> onMenuAction(featureName));
			mniFeature.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent event) {
					mniFeature.setBackground(PALE_BLUE);
				}

				@Override
				public void mouseExited(MouseEvent event) {
					mniFeature.setBackground(Color.WHITE);
				}
			});
			pnlItems.add(mniFeature);
		}

		pnlColumn.add(pnlItems, BorderLayout.CENTER);
		return pnlColumn;
	}

	protected void openFeatureFrame(String featureName) {
		if ("Phân quyền nhân viên".equals(featureName)) {
			NhanVienFrame frame = new NhanVienFrame();
			frame.setLocationRelativeTo(this);
			frame.setVisible(true);
			dispose();
			return;
		}
		if ("Quản lý".equals(featureName)) {
			QuanLyFrame frame = new QuanLyFrame();
			frame.setLocationRelativeTo(this);
			frame.setVisible(true);
			dispose();
			return;
		}
		onMenuAction(featureName);
	}

	private JMenuBar createMenuBar() {
		JMenuBar mnbMain = new JMenuBar();
		mnbMain.setOpaque(true);
		mnbMain.setBackground(BLUE);
		mnbMain.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));

		addMenu(mnbMain, "Hệ thống", MenuIcon.Type.SETTINGS, new String[][] {
		 	{"Đổi mật khẩu", "KEY"}, {"Đăng xuất", "EXIT"},
		});
		addMenu(mnbMain, "Danh mục", MenuIcon.Type.DOCUMENT, new String[][] {
			{"Khách hàng", "USER"}, {"Xe", "CAR"},
			{"Phụ tùng", "PART"}, {"Dịch vụ sửa chữa", "TOOL"}
			
		});
		addMenu(mnbMain, "Cập nhật", MenuIcon.Type.BACKUP, new String[][] {
			{"Cập nhật khách hàng", "USER"}, {"Cập nhật xe", "CAR"},
			{"Cập nhật phụ tùng", "PART"},
			{"Cập nhật dịch vụ", "TOOL"}
		});
		addMenu(mnbMain, "Xử lý", MenuIcon.Type.TOOL, new String[][] {
			{"Tiếp nhận xe", "CAR"}, {"Kiểm tra xe", "SEARCH"},
			{"Lập phiếu sửa chữa", "DOCUMENT"},
			{"Xuất phụ tùng", "PART"},
			{"Lập hóa đơn", "DOCUMENT"}, {"Thanh toán", "MONEY"},
			{"Bàn giao xe", "KEY"}
		});
		addMenu(mnbMain, "Tìm kiếm/tra cứu", MenuIcon.Type.SEARCH, new String[][] {
			{"Khách hàng", "USER"}, {"Xe", "CAR"}, {"Lịch sử sửa chữa", "CLOCK"},
			{"Phiếu tiếp nhận", "DOCUMENT"}, {"Phiếu sửa chữa", "TOOL"},
			{"Hóa đơn", "DOCUMENT"}, {"Phụ tùng", "PART"}
		});
		addMenu(mnbMain, "Báo biểu/thống kê", MenuIcon.Type.CHART, new String[][] {
			{"Doanh thu", "MONEY"}, {"Sửa chữa", "TOOL"}, {"Khách hàng", "USER"},
			{"Phụ tùng tồn kho", "PART"}, {"Công nợ", "MONEY"},
			{"Số lượng xe sửa chữa", "CAR"}
		
		});
		return mnbMain;
	}

	private void addMenu(JMenuBar menuBar, String title, MenuIcon.Type icon, String[][] features) {
		JMenu mnuCategory = new JMenu(title);
		mnuCategory.setIcon(new MenuIcon(icon, Color.WHITE));
		mnuCategory.setOpaque(true);
		mnuCategory.setBackground(BLUE);
		mnuCategory.setForeground(Color.WHITE);
		mnuCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
		mnuCategory.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
		mnuCategory.setToolTipText(title);
		mnuCategory.getPopupMenu().setBackground(Color.WHITE);
		mnuCategory.getPopupMenu().setBorder(BorderFactory.createLineBorder(new Color(220, 230, 244)));

		for (String[] feature : features) {
			JMenuItem mniFeature = new JMenuItem(feature[0], new MenuIcon(MenuIcon.Type.valueOf(feature[1])));
			mniFeature.setFont(new Font("Segoe UI", Font.PLAIN, 13));
			mniFeature.setForeground(TEXT);
			mniFeature.setBackground(Color.WHITE);
			mniFeature.setOpaque(true);
			mniFeature.setPreferredSize(new Dimension(238, 36));
			mniFeature.setBorder(new EmptyBorder(4, 10, 4, 10));
			mniFeature.setToolTipText(feature[0]);
			mniFeature.addActionListener(event -> {
				if ("Phân quyền nhân viên".equals(feature[0]) || "Quản lý".equals(feature[0])) {
					openFeatureFrame(feature[0]);
					return;
				}
				onMenuAction(feature[0]);
			});
			mniFeature.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent event) {
					mniFeature.setBackground(PALE_BLUE);
				}

				@Override
				public void mouseExited(MouseEvent event) {
					mniFeature.setBackground(Color.WHITE);
				}
			});
			mnuCategory.add(mniFeature);
		}
		menuBar.add(mnuCategory);
	}

	private JPanel createWorkspace() {
		JPanel pnlWorkspace = new JPanel(new BorderLayout(0, 20));
		pnlWorkspace.setBackground(PALE_BLUE);
		pnlWorkspace.setBorder(new EmptyBorder(38, 48, 38, 48));

		JLabel lblTitle = new JLabel("Quản Lý Gara Oto");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
		lblTitle.setForeground(new Color(32, 55, 92));
		JLabel lblSubtitle = new JLabel("Hệ thống quản lý hoạt động gara ô tô");
		lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblSubtitle.setForeground(MUTED);

		JPanel pnlHeading = new JPanel(new BorderLayout(0, 7));
		pnlHeading.setOpaque(false);
		pnlHeading.add(lblTitle, BorderLayout.NORTH);
		pnlHeading.add(lblSubtitle, BorderLayout.CENTER);

		JPanel pnlContent = new JPanel(new BorderLayout());
		pnlContent.setBackground(Color.WHITE);
		pnlContent.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(221, 231, 245)),
				new EmptyBorder(26, 28, 26, 28)));
		JLabel lblPrompt = new JLabel("Chọn chức năng trên thanh menu để bắt đầu", SwingConstants.CENTER);
		lblPrompt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		lblPrompt.setForeground(MUTED);
		pnlContent.add(lblPrompt, BorderLayout.CENTER);

		pnlWorkspace.add(pnlHeading, BorderLayout.NORTH);
		pnlWorkspace.add(pnlContent, BorderLayout.CENTER);
		return pnlWorkspace;
	}

	private JPanel createStatusBar() {
		JPanel pnlStatusBar = new JPanel(new BorderLayout());
		pnlStatusBar.setBackground(Color.WHITE);
		pnlStatusBar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 230, 244)),
				new EmptyBorder(7, 14, 7, 14)));
		lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblStatus.setForeground(MUTED);
		pnlStatusBar.add(lblStatus, BorderLayout.WEST);
		return pnlStatusBar;
	}
	public static void main(String[] args) {
		new NhanVienFrame().setVisible(true);
	}
}
