package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class BaseFrame extends JFrame {
	private static final Color BLUE = new Color(36, 99, 235);
	private static final Color PALE_BLUE = new Color(242, 247, 255);
	private static final Color TEXT = new Color(47, 62, 84);
	private static final Color MUTED = new Color(112, 128, 150);
	private final JLabel statusLabel = new JLabel("Sẵn sàng");

	public BaseFrame() {
		this("Quản Lý Gara Oto");
	}

	protected BaseFrame(String title) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(1050, 650));
		setSize(1220, 760);
		setLocationRelativeTo(null);
		getContentPane().setBackground(Color.WHITE);
		setJMenuBar(createMenuBar());
		add(createWorkspace(), BorderLayout.CENTER);
		add(createStatusBar(), BorderLayout.SOUTH);
	}

	protected void onMenuAction(String featureName) {
		statusLabel.setText("Đã chọn: " + featureName);
	}

	private JMenuBar createMenuBar() {
		JMenuBar bar = new JMenuBar();
		bar.setOpaque(true);
		bar.setBackground(BLUE);
		bar.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));

		addMenu(bar, "Hệ thống", MenuIcon.Type.SETTINGS, new String[][] {
			{"Đăng nhập", "USER"}, {"Đăng xuất", "EXIT"}, {"Đổi mật khẩu", "KEY"},
			{"Quản lý tài khoản", "USERS"}, {"Phân quyền", "SECURITY"},
			{"Sao lưu dữ liệu", "BACKUP"}, {"Khôi phục dữ liệu", "BACKUP"},
			{"Tham số hệ thống", "SETTINGS"}
		});
		addMenu(bar, "Danh mục", MenuIcon.Type.DOCUMENT, new String[][] {
			{"Khách hàng", "USER"}, {"Xe", "CAR"}, {"Nhân viên", "USERS"},
			{"Phụ tùng", "PART"}, {"Dịch vụ sửa chữa", "TOOL"},
			{"Khuyến mãi", "TAG"}, {"Ca làm việc", "CLOCK"}
		});
		addMenu(bar, "Cập nhật", MenuIcon.Type.BACKUP, new String[][] {
			{"Cập nhật khách hàng", "USER"}, {"Cập nhật xe", "CAR"},
			{"Cập nhật nhân viên", "USERS"}, {"Cập nhật phụ tùng", "PART"},
			{"Cập nhật giá phụ tùng", "MONEY"}, {"Cập nhật dịch vụ", "TOOL"},
			{"Cập nhật khuyến mãi", "TAG"}, {"Cập nhật lịch làm việc", "CLOCK"}
		});
		addMenu(bar, "Xử lý", MenuIcon.Type.TOOL, new String[][] {
			{"Tiếp nhận xe", "CAR"}, {"Kiểm tra xe", "SEARCH"},
			{"Lập phiếu sửa chữa", "DOCUMENT"}, {"Phân công sửa chữa", "USERS"},
			{"Nhập phụ tùng", "PART"}, {"Xuất phụ tùng", "PART"},
			{"Lập hóa đơn", "DOCUMENT"}, {"Thanh toán", "MONEY"},
			{"Quản lý công nợ", "MONEY"}, {"Bàn giao xe", "KEY"}
		});
		addMenu(bar, "Tìm kiếm/tra cứu", MenuIcon.Type.SEARCH, new String[][] {
			{"Khách hàng", "USER"}, {"Xe", "CAR"}, {"Lịch sử sửa chữa", "CLOCK"},
			{"Phiếu tiếp nhận", "DOCUMENT"}, {"Phiếu sửa chữa", "TOOL"},
			{"Hóa đơn", "DOCUMENT"}, {"Phụ tùng", "PART"}, {"Nhân viên", "USERS"}
		});
		addMenu(bar, "Báo biểu/thống kê", MenuIcon.Type.CHART, new String[][] {
			{"Doanh thu", "MONEY"}, {"Sửa chữa", "TOOL"}, {"Khách hàng", "USER"},
			{"Phụ tùng tồn kho", "PART"}, {"Công nợ", "MONEY"},
			{"Hiệu suất nhân viên", "CHART"}, {"Số lượng xe sửa chữa", "CAR"},
			{"Báo cáo tổng hợp", "CHART"}
		});
		return bar;
	}

	private void addMenu(JMenuBar bar, String title, MenuIcon.Type icon, String[][] features) {
		JMenu menu = new JMenu(title);
<<<<<<< HEAD
		menu.setIcon(new MenuIcon(icon));
=======
		menu.setIcon(new MenuIcon(icon, Color.WHITE));
>>>>>>> feature/Entity_Dao
		menu.setOpaque(true);
		menu.setBackground(BLUE);
		menu.setForeground(Color.WHITE);
		menu.setFont(new Font("Segoe UI", Font.BOLD, 14));
		menu.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
		menu.setToolTipText(title);
		menu.getPopupMenu().setBackground(Color.WHITE);
		menu.getPopupMenu().setBorder(BorderFactory.createLineBorder(new Color(220, 230, 244)));

		for (String[] feature : features) {
			JMenuItem item = new JMenuItem(feature[0], new MenuIcon(MenuIcon.Type.valueOf(feature[1])));
			item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
			item.setForeground(TEXT);
			item.setBackground(Color.WHITE);
			item.setOpaque(true);
			item.setPreferredSize(new Dimension(238, 36));
			item.setBorder(new EmptyBorder(4, 10, 4, 10));
			item.setToolTipText(feature[0]);
			item.addActionListener(event -> onMenuAction(feature[0]));
			item.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent event) {
					item.setBackground(PALE_BLUE);
				}

				@Override
				public void mouseExited(MouseEvent event) {
					item.setBackground(Color.WHITE);
				}
			});
			menu.add(item);
		}
		bar.add(menu);
	}

	private JPanel createWorkspace() {
		JPanel workspace = new JPanel(new BorderLayout(0, 20));
		workspace.setBackground(PALE_BLUE);
		workspace.setBorder(new EmptyBorder(38, 48, 38, 48));

		JLabel title = new JLabel("Quản Lý Gara Oto");
		title.setFont(new Font("Segoe UI", Font.BOLD, 28));
		title.setForeground(new Color(32, 55, 92));
		JLabel subtitle = new JLabel("Hệ thống quản lý hoạt động gara ô tô");
		subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		subtitle.setForeground(MUTED);

		JPanel heading = new JPanel(new BorderLayout(0, 7));
		heading.setOpaque(false);
		heading.add(title, BorderLayout.NORTH);
		heading.add(subtitle, BorderLayout.CENTER);

		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(221, 231, 245)),
				new EmptyBorder(26, 28, 26, 28)));
		JLabel prompt = new JLabel("Chọn chức năng trên thanh menu để bắt đầu", SwingConstants.CENTER);
		prompt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		prompt.setForeground(MUTED);
		content.add(prompt, BorderLayout.CENTER);

		workspace.add(heading, BorderLayout.NORTH);
		workspace.add(content, BorderLayout.CENTER);
		return workspace;
	}

	private JPanel createStatusBar() {
		JPanel statusBar = new JPanel(new BorderLayout());
		statusBar.setBackground(Color.WHITE);
		statusBar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 230, 244)),
				new EmptyBorder(7, 14, 7, 14)));
		statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		statusLabel.setForeground(MUTED);
		statusBar.add(statusLabel, BorderLayout.WEST);
		return statusBar;
	}
}
