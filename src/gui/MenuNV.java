package gui;

import entity.TaiKhoan;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MenuNV extends JMenuBar {
	private final BaseFrame frame;
	private final TaiKhoan taiKhoan;

	public MenuNV(BaseFrame frame, TaiKhoan taiKhoan) {
		this.frame = frame;
		this.taiKhoan = taiKhoan;
		taoMenu();
	}

	private void taoMenu() {
		JMenu mnuSystem = MenuBarSupport.createMenu("Hệ thống", MenuIcon.Type.SETTINGS);
		JMenu mnuCatalog = MenuBarSupport.createMenu("Danh mục", MenuIcon.Type.DOCUMENT);
		JMenu mnuUpdate = MenuBarSupport.createMenu("Cập nhật", MenuIcon.Type.BACKUP);
		JMenu mnuProcess = MenuBarSupport.createMenu("Xử lý", MenuIcon.Type.TOOL);
		JMenu mnuSearch = MenuBarSupport.createMenu("Tìm kiếm", MenuIcon.Type.SEARCH);
		JMenu mnuReports = MenuBarSupport.createMenu("Báo biểu", MenuIcon.Type.CHART);

		themItem(mnuSystem, "Đổi mật khẩu", "Đăng xuất");
		themItem(mnuCatalog, "Khách hàng", "Xe", "Phụ tùng", "Dịch vụ sửa chữa");
		themItem(mnuUpdate, "Cập nhật khách hàng", "Cập nhật xe",
				"Cập nhật phụ tùng", "Cập nhật dịch vụ");
		themItem(mnuProcess, "Tiếp nhận xe", "Kiểm tra xe", "Lập phiếu sửa chữa",
				"Xuất phụ tùng", "Lập hóa đơn", "Thanh toán",
				"Bàn giao xe");
		themItem(mnuSearch, "Khách hàng", "Xe", "Lịch sử sửa chữa", "Phiếu tiếp nhận",
				"Phiếu sửa chữa", "Hóa đơn", "Phụ tùng");
		themItem(mnuReports, "Phiếu tiếp nhận", "Phiếu kiểm tra", "Phiếu sửa chữa", "Hóa đơn");

		MenuBarSupport.configure(this,
				new JMenu[] {mnuSystem, mnuCatalog, mnuUpdate, mnuProcess, mnuSearch, mnuReports},
				taiKhoan);
	}

	private void themItem(JMenu menu, String... labels) {
		for (String label : labels) {
			menu.add(MenuBarSupport.createItem(label, frame));
		}
	}
}
