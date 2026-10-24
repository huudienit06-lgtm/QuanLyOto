package gui;

import java.awt.Color;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public final class MainMenu extends JMenuBar {
	private static final Color BLUE = new Color(36, 99, 235);

	private MainMenu() {
	}

	private void initialize(BaseFrame frame) {
		JMenu mnuSystem = MenuBarSupport.createMenu("Hệ thống", MenuIcon.Type.SETTINGS);
		JMenu mnuCatalog = MenuBarSupport.createMenu("Danh mục", MenuIcon.Type.DOCUMENT);
		JMenu mnuUpdate = MenuBarSupport.createMenu("Cập nhật", MenuIcon.Type.BACKUP);
		JMenu mnuProcess = MenuBarSupport.createMenu("Xử lý", MenuIcon.Type.TOOL);
		JMenu mnuSearch = MenuBarSupport.createMenu("Tìm kiếm", MenuIcon.Type.SEARCH);
		JMenu mnuReports = MenuBarSupport.createMenu("Báo biểu", MenuIcon.Type.CHART);

		addItems(frame, mnuSystem, "Quản lý tài khoản", "Phân quyền nhân viên",
				"Quản lý", "Đổi mật khẩu", "Đăng xuất");
		addItems(frame, mnuCatalog, "Khách hàng", "Xe", "Nhân viên", "Phụ tùng",
				"Dịch vụ sửa chữa", "Khuyến mãi", "Ca làm việc");
		addItems(frame, mnuUpdate, "Cập nhật khách hàng", "Cập nhật xe",
				"Cập nhật nhân viên", "Cập nhật phụ tùng", "Cập nhật giá phụ tùng",
				"Cập nhật dịch vụ", "Cập nhật khuyến mãi", "Cập nhật lịch làm việc");
		addItems(frame, mnuProcess, "Tiếp nhận xe", "Kiểm tra xe", "Lập phiếu sửa chữa",
				"Nhập phụ tùng", "Xuất phụ tùng", "Lập hóa đơn",
				"Thanh toán", "Quản lý công nợ", "Bàn giao xe");
		addItems(frame, mnuSearch, "Khách hàng", "Xe", "Lịch sử sửa chữa",
				"Phiếu tiếp nhận", "Phiếu sửa chữa", "Hóa đơn", "Phụ tùng", "Nhân viên");
		addItems(frame, mnuReports, "Doanh thu", "Sửa chữa", "Khách hàng",
				"Phụ tùng tồn kho", "Công nợ", "Số lượng xe sửa chữa", "Báo cáo tổng hợp");

		MenuBarSupport.configure(this,
				new JMenu[] {mnuSystem, mnuCatalog, mnuUpdate, mnuProcess, mnuSearch, mnuReports},
				null);
		setBackground(BLUE);
	}

	private void addItems(BaseFrame frame, JMenu menu, String... labels) {
		for (String label : labels) {
			JMenuItem mniItem = MenuBarSupport.createItem(label, frame);
			menu.add(mniItem);
		}
	}

	public static JMenuBar create(BaseFrame frame) {
		MainMenu mainMenu = new MainMenu();
		mainMenu.initialize(frame);
		return mainMenu;
	}
}
