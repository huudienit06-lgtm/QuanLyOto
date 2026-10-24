package gui;

import entity.TaiKhoan;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MenuQL extends JMenuBar {
	private final BaseFrame frame;
	private final TaiKhoan taiKhoan;

	public MenuQL(BaseFrame frame, TaiKhoan taiKhoan) {
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

		themItem(mnuSystem, "Quản lý tài khoản", "Phân quyền", "Sao lưu dữ liệu",
				"Đổi mật khẩu", "Đăng xuất");
		themItem(mnuCatalog, "Khách hàng", "Xe", "Nhân viên", "Phụ tùng",
				"Dịch vụ sửa chữa", "Khuyến mãi", "Ca làm việc");
		themItem(mnuUpdate, "Cập nhật khách hàng", "Cập nhật xe", "Cập nhật nhân viên",
				"Cập nhật phụ tùng", "Cập nhật giá phụ tùng", "Cập nhật dịch vụ",
				"Cập nhật khuyến mãi", "Cập nhật lịch làm việc");
		themItem(mnuProcess, "Tiếp nhận xe", "Kiểm tra xe", "Lập phiếu sửa chữa",
				"Nhập phụ tùng", "Xuất phụ tùng", "Lập hóa đơn",
				"Thanh toán", "Quản lý công nợ", "Bàn giao xe");
		themItem(mnuSearch, "Khách hàng", "Xe", "Lịch sử sửa chữa", "Phiếu tiếp nhận",
				"Phiếu sửa chữa", "Hóa đơn", "Phụ tùng", "Tìm kiếm nhân viên");
		themItem(mnuReports, "Doanh thu", "Sửa chữa", "Khách hàng", "Phụ tùng tồn kho",
				"Công nợ", "Số lượng xe sửa chữa", "Báo cáo tổng hợp");

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
