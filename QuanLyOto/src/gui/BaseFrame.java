package gui;

import dao.ThongKeHoatDongGaraDAO;
import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class BaseFrame extends JFrame {
	private static final Color PALE_BLUE = new Color(242, 247, 255);
	private static final Color MUTED = new Color(112, 128, 150);
	private final JLabel lblStatus = new JLabel("Sẵn sàng");
	private JPanel pnlCurrentContent;
	private TaiKhoan taiKhoanDangNhap;
	private JMenuBar menuVaiTroDangNhap;

	public void setTaiKhoanDangNhap(TaiKhoan taiKhoan) {
		this.taiKhoanDangNhap = taiKhoan;
	}

	public TaiKhoan getTaiKhoanDangNhap() {
		return taiKhoanDangNhap;
	}

	public BaseFrame() {
		this("Quản Lý Gara Oto");
	}

	protected BaseFrame(String title) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(1000, 750));
		setSize(1220, 900);
		setLocationRelativeTo(null);
		getContentPane().setBackground(Color.WHITE);
		setJMenuBar(MainMenu.create(this));
		// add(createTitleBar(), BorderLayout.NORTH);
		setContent(new FrmDangNhap(this));
		add(createStatusBar(), BorderLayout.SOUTH);
	}

	protected void onMenuAction(String featureName) {
		if ("Trang Chủ".equals(featureName)) {
			if (menuVaiTroDangNhap == null) {
				showError("Không xác định được menu theo vai trò đăng nhập.");
				return;
			}
			setJMenuBar(menuVaiTroDangNhap);
			setContent(createWorkspace());
			lblStatus.setText(laNhanVienDangNhap()
					? "Trang chủ nhân viên"
					: "Trang chủ quản lý");
			return;
		}
		if ("Đăng xuất".equals(featureName)) {
			hienThiDangNhap();
			return;
		}
		if ("Quản lý tài khoản".equals(featureName) || "Phân quyền nhân viên".equals(featureName)) {
			setContent(new FrmQuanLyTaiKhoan());
			lblStatus.setText("Quản lý tài khoản");
			return;
		}
		if ("Nhân viên".equals(featureName) || "Cập nhật nhân viên".equals(featureName)) {
			setContent(new FrmNhanVien());
			lblStatus.setText("Quản lý nhân viên");
			return;
		}
		if ("Chức vụ".equals(featureName)) {
			setContent(new FrmChucVu());
			lblStatus.setText("Quản lý chức vụ");
			return;
		}
		if ("Ca làm việc".equals(featureName)) {
			setContent(new FrmCaLamViec());
			lblStatus.setText("Quản lý ca làm việc");
			return;
		}
		if ("Lịch làm việc".equals(featureName)) {
			setContent(new FrmLichLamViec());
			lblStatus.setText("Quản lý lịch làm việc");
			return;
		}
		if ("Chính sách bảo hành".equals(featureName)) {
			setContent(new FrmChinhSachBaoHanh());
			lblStatus.setText("Quản lý chính sách bảo hành");
			return;
		}
		if ("Tiếp nhận bảo hành".equals(featureName)) {
			setContent(new FrmTiepNhanBaoHanh());
			lblStatus.setText("Tiếp nhận bảo hành");
			return;
		}
		if ("Quản lý công nợ".equals(featureName)) {
			setContent(new FrmCongNo());
			lblStatus.setText("Quản lý công nợ");
			return;
		}
		if ("Khách hàng".equals(featureName) || "Cập nhật khách hàng".equals(featureName)) {
			setContent(new FrmKhachHang());
			lblStatus.setText("Quản lý khách hàng");
			return;
		}
		if ("Báo cáo tổng hợp".equals(featureName)) {
			setContent(new FrmBaoCaoTongHop());
			lblStatus.setText("Báo cáo tổng hợp");
			return;
		}
		if ("Thống kê Doanh thu".equals(featureName)) {
			setContent(new FrmThongKeDoanhThu());
			lblStatus.setText("Thống kê doanh thu");
			return;
		}
		if ("Xe".equals(featureName) || "Cập nhật xe".equals(featureName)) {
			setContent(new FrmXe());
			lblStatus.setText("Quản lý xe");
			return;
		}
		if ("Phụ tùng".equals(featureName) || "Cập nhật phụ tùng".equals(featureName)) {
			setContent(new FrmPhuTung());
			lblStatus.setText("Quản lý phụ tùng");
			return;
		}
		if ("Nhập/Xuất phụ tùng".equals(featureName)) {
			setContent(new FrmNhapXuatPhuTung(taiKhoanDangNhap));
			lblStatus.setText("Lập phiếu nhập kho phụ tùng");
			return;
		}
		if ("Thống kê Phụ tùng".equals(featureName)) {
			setContent(new FrmThongKeTonKho());
			lblStatus.setText("Thống kê tồn kho phụ tùng");
			return;
		}
		if ("Hóa đơn".equals(featureName)) {
			setContent(new FrmTimKiemHoaDon());
			lblStatus.setText("Tìm kiếm hóa đơn");
			return;
		}
		if ("Lập hóa đơn".equals(featureName)) {
			FrmHoaDon frmHoaDon = new FrmHoaDon();
			if (taiKhoanDangNhap != null) {
				frmHoaDon.setNhanVienLapHoaDon(
						taiKhoanDangNhap.getNhanVien() == null ? null
								: taiKhoanDangNhap.getNhanVien().getMaNV(),
						taiKhoanDangNhap.getHoTenNhanVien());
			}
			setContent(frmHoaDon);
			frmHoaDon.taiDuLieuBanDauAsync();
			lblStatus.setText("Lập hóa đơn");
			return;
		}
		if ("Dịch vụ".equals(featureName)) {
			setContent(new FrmDichVu());
			lblStatus.setText("Dịch vụ sửa chữa");
			return;
		}
		if ("Khuyến mãi".equals(featureName)) {
			setContent(new FrmKhuyenMai());
			lblStatus.setText("Quản lý khuyến mãi");
			return;
		}
		if ("Lập phiếu sửa chữa".equals(featureName)) {
			setContent(new FrmPhieuSuaChua(taiKhoanDangNhap));
			lblStatus.setText("Lập phiếu sửa chữa");
			return;
		}
		if ("Tiếp nhận xe".equals(featureName) || "Phiếu tiếp nhận".equals(featureName)) {
			setContent(new FrmTiepNhan(taiKhoanDangNhap));
			lblStatus.setText("Tiếp nhận xe");
			return;
		}
		if ("Kiểm tra xe".equals(featureName) || "Phiếu kiểm tra".equals(featureName)) {
			setContent(new FrmPhieuKiemTra(taiKhoanDangNhap));
			lblStatus.setText("Quản lý phiếu kiểm tra");
			return;
		}
		if ("Bàn giao xe".equals(featureName)) {
			setContent(new FrmBanGiaoXe(taiKhoanDangNhap));
			lblStatus.setText("Bàn giao xe");
			return;
		}
		if ("Thống kê Sửa chữa".equals(featureName)) {
			setContent(new FrmThongKeSuaChua());
			lblStatus.setText("Sửa chữa");
			return;
		}
		lblStatus.setText("Đã chọn: " + featureName);
	}

	void hienThiMenu(JMenuBar menuBar, String status) {
		menuVaiTroDangNhap = menuBar;
		setJMenuBar(menuBar);
		setContent(createWorkspace());
		lblStatus.setText(status);
	}

	private void hienThiDangNhap() {
		menuVaiTroDangNhap = null;
		setJMenuBar(MainMenu.create(this));
		setContent(new FrmDangNhap(this));
		lblStatus.setText("Đã đăng xuất");
	}

	public final void setContent(JPanel pnlContent) {
		if (pnlCurrentContent != null) {
			remove(pnlCurrentContent);
		}
		pnlCurrentContent = pnlContent;
		add(pnlCurrentContent, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	private JPanel createWorkspace() {
		JPanel pnlWorkspace = new JPanel(new BorderLayout(0, 20));
		pnlWorkspace.setBackground(PALE_BLUE);
		pnlWorkspace.setBorder(new EmptyBorder(38, 48, 38, 48));

		JLabel lblTitle = new JLabel(laNhanVienDangNhap()
				? "Trang chủ nhân viên"
				: laQuanLyDangNhap() ? "Trang chủ quản lý" : "Quản Lý Gara Oto");
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
		if (laNhanVienDangNhap()) {
			pnlContent.add(createThongKeHoatDong(), BorderLayout.CENTER);
		} else if (laQuanLyDangNhap()) {
			pnlContent.add(createThongKeQuanLy(), BorderLayout.CENTER);
		} else {
			JLabel lblPrompt = new JLabel("Chọn chức năng trên thanh menu để bắt đầu", SwingConstants.CENTER);
			lblPrompt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
			lblPrompt.setForeground(MUTED);
			pnlContent.add(lblPrompt, BorderLayout.CENTER);
		}

		pnlWorkspace.add(pnlHeading, BorderLayout.NORTH);
		pnlWorkspace.add(pnlContent, BorderLayout.CENTER);
		return pnlWorkspace;
	}

	private boolean laNhanVienDangNhap() {
		return layVaiTroChuanHoa().equals("nhanvien") || layVaiTroChuanHoa().equals("employee");
	}

	private boolean laQuanLyDangNhap() {
		return layVaiTroChuanHoa().equals("quanly") || layVaiTroChuanHoa().equals("admin");
	}

	private String layVaiTroChuanHoa() {
		if (taiKhoanDangNhap == null || taiKhoanDangNhap.getVaiTro() == null) {
			return "";
		}
		return Normalizer.normalize(taiKhoanDangNhap.getVaiTro(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.replaceAll("[^a-zA-Z]", "")
				.toLowerCase(Locale.ROOT);
	}

	private JPanel createThongKeHoatDong() {
		JPanel pnlDashboard = new JPanel(new BorderLayout(0, 22));
		pnlDashboard.setOpaque(false);

		JLabel lblTieuDe = new JLabel("TÌNH HÌNH HOẠT ĐỘNG GARA");
		lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 17));
		lblTieuDe.setForeground(new Color(32, 55, 92));
		JButton btnTaiLai = new JButton("Cập nhật số liệu");
		btnTaiLai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		JPanel pnlTieuDe = new JPanel(new BorderLayout(12, 0));
		pnlTieuDe.setOpaque(false);
		pnlTieuDe.add(lblTieuDe, BorderLayout.CENTER);
		pnlTieuDe.add(btnTaiLai, BorderLayout.EAST);

		JLabel[] lblGiaTri = new JLabel[4];
		JPanel pnlThe = new JPanel(new GridLayout(2, 2, 16, 16));
		pnlThe.setOpaque(false);
		themTheThongKe(pnlThe, lblGiaTri, 0, "XE ĐÃ TIẾP NHẬN",
				"Số phiếu tiếp nhận chưa bị hủy", new Color(36, 99, 235), "Tiếp nhận xe");
		themTheThongKe(pnlThe, lblGiaTri, 1, "CHỜ KIỂM TRA",
				"Phiếu tiếp nhận chưa có phiếu kiểm tra", new Color(217, 119, 6), "Kiểm tra xe");
		themTheThongKe(pnlThe, lblGiaTri, 2, "CHỜ SỬA CHỮA",
				"Phiếu sửa chữa đang chờ xử lý", new Color(124, 87, 194), "Lập phiếu sửa chữa");
		themTheThongKe(pnlThe, lblGiaTri, 3, "CHỜ THANH TOÁN",
				"Xe hoàn thành nhưng hóa đơn chưa thanh toán đủ", new Color(38, 132, 93), "Lập hóa đơn");

		JLabel lblGhiChu = new JLabel("Số liệu được lấy trực tiếp từ các phiếu nghiệp vụ hiện có.");
		lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblGhiChu.setForeground(MUTED);
		JPanel pnlThongTin = new JPanel(new BorderLayout(0, 16));
		pnlThongTin.setOpaque(false);
		pnlThongTin.add(pnlThe, BorderLayout.CENTER);
		pnlThongTin.add(lblGhiChu, BorderLayout.SOUTH);

		pnlDashboard.add(pnlTieuDe, BorderLayout.NORTH);
		pnlDashboard.add(pnlThongTin, BorderLayout.CENTER);
		btnTaiLai.addActionListener(event -> taiThongKeHoatDong(lblGiaTri, btnTaiLai));
		taiThongKeHoatDong(lblGiaTri, btnTaiLai);
		return pnlDashboard;
	}

	private JPanel createThongKeQuanLy() {
		JPanel pnlDashboard = new JPanel(new BorderLayout(0, 22));
		pnlDashboard.setOpaque(false);

		JLabel lblTieuDe = new JLabel("TÌNH HÌNH HOẠT ĐỘNG GARA");
		lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 17));
		lblTieuDe.setForeground(new Color(32, 55, 92));
		JButton btnTaiLai = new JButton("Cập nhật số liệu");
		btnTaiLai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		JPanel pnlTieuDe = new JPanel(new BorderLayout(12, 0));
		pnlTieuDe.setOpaque(false);
		pnlTieuDe.add(lblTieuDe, BorderLayout.CENTER);
		pnlTieuDe.add(btnTaiLai, BorderLayout.EAST);

		JLabel[] lblGiaTri = new JLabel[4];
		JPanel pnlThe = new JPanel(new GridLayout(2, 2, 16, 16));
		pnlThe.setOpaque(false);
		themTheThongKe(pnlThe, lblGiaTri, 0, "NHÂN VIÊN ĐANG TRONG CA",
				"Có mặt / được phân công ở ca hiện tại", new Color(36, 99, 235), "Ca làm việc");
		themTheThongKe(pnlThe, lblGiaTri, 1, "PHIẾU TIẾP NHẬN HÔM NAY",
				"Số phiếu được lập trong ngày", new Color(217, 119, 6), "Tiếp nhận xe");
		themTheThongKe(pnlThe, lblGiaTri, 2, "XE ĐANG SỬA CHỮA",
				"Số xe duy nhất có phiếu đang sửa chữa", new Color(124, 87, 194), "Sửa chữa");
		themTheThongKe(pnlThe, lblGiaTri, 3, "HÓA ĐƠN ĐÃ LẬP",
				"Tổng số hóa đơn trong hệ thống", new Color(38, 132, 93), "Hóa đơn");

		JLabel lblGhiChu = new JLabel(
				"Số liệu nhân viên và phiếu tiếp nhận được tính theo ca hiện tại và ngày hôm nay.");
		lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblGhiChu.setForeground(MUTED);
		JPanel pnlThongTin = new JPanel(new BorderLayout(0, 16));
		pnlThongTin.setOpaque(false);
		pnlThongTin.add(pnlThe, BorderLayout.CENTER);
		pnlThongTin.add(lblGhiChu, BorderLayout.SOUTH);

		pnlDashboard.add(pnlTieuDe, BorderLayout.NORTH);
		pnlDashboard.add(pnlThongTin, BorderLayout.CENTER);
		btnTaiLai.addActionListener(event -> taiThongKeQuanLy(lblGiaTri, btnTaiLai));
		taiThongKeQuanLy(lblGiaTri, btnTaiLai);
		return pnlDashboard;
	}

	private void themTheThongKe(JPanel pnlThe, JLabel[] lblGiaTri, int viTri,
			String tieuDe, String moTa, Color mau, String tenChucNang) {
		JPanel pnlCard = new JPanel(new BorderLayout(0, 12));
		pnlCard.setBackground(Color.WHITE);
		pnlCard.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 4, 0, 0, mau),
				BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(new Color(221, 231, 245)),
						new EmptyBorder(18, 16, 18, 16))));

		JLabel lblTieuDe = new JLabel(tieuDe);
		lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblTieuDe.setForeground(MUTED);
		JLabel lblSoLuong = new JLabel("—", SwingConstants.LEFT);
		lblSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 34));
		lblSoLuong.setForeground(mau);
		JLabel lblMoTa = new JLabel("<html><div style='width:150px'>" + moTa + "</div></html>");
		lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblMoTa.setForeground(MUTED);

		JPanel pnlChiTiet = new JPanel(new BorderLayout(0, 8));
		pnlChiTiet.setOpaque(false);
		pnlChiTiet.add(lblSoLuong, BorderLayout.NORTH);
		pnlChiTiet.add(lblMoTa, BorderLayout.CENTER);
		pnlCard.add(lblTieuDe, BorderLayout.NORTH);
		pnlCard.add(pnlChiTiet, BorderLayout.CENTER);
		ganSuKienClickThe(pnlCard, tenChucNang, lblTieuDe, pnlChiTiet, lblSoLuong, lblMoTa);
		lblGiaTri[viTri] = lblSoLuong;
		pnlThe.add(pnlCard);
	}

	private void ganSuKienClickThe(JPanel pnlCard, String tenChucNang, Component... thanhPhanThe) {
		MouseAdapter suKienClick = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				onMenuAction(tenChucNang);
			}
		};
		pnlCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		pnlCard.addMouseListener(suKienClick);
		for (Component thanhPhan : thanhPhanThe) {
			thanhPhan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			thanhPhan.addMouseListener(suKienClick);
		}
	}

	private void taiThongKeHoatDong(JLabel[] lblGiaTri, JButton btnTaiLai) {
		btnTaiLai.setEnabled(false);
		new SwingWorker<ThongKeHoatDongGaraDAO.ThongKeHoatDong, Void>() {
			@Override
			protected ThongKeHoatDongGaraDAO.ThongKeHoatDong doInBackground() throws SQLException {
				return new ThongKeHoatDongGaraDAO().layThongKe();
			}

			@Override
			protected void done() {
				try {
					ThongKeHoatDongGaraDAO.ThongKeHoatDong thongKe = get();
					lblGiaTri[0].setText(String.valueOf(thongKe.xeDaTiepNhan()));
					lblGiaTri[1].setText(String.valueOf(thongKe.xeChoKiemTra()));
					lblGiaTri[2].setText(String.valueOf(thongKe.xeChoSuaChua()));
					lblGiaTri[3].setText(String.valueOf(thongKe.xeChoThanhToan()));
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
					showError("Đã ngắt khi tải thống kê hoạt động gara.");
				} catch (ExecutionException exception) {
					Throwable cause = exception.getCause();
					String message = cause == null ? exception.getMessage() : cause.getMessage();
					showError("Không thể tải thống kê hoạt động gara: " + message);
				} finally {
					btnTaiLai.setEnabled(true);
				}
			}
		}.execute();
	}

	private void taiThongKeQuanLy(JLabel[] lblGiaTri, JButton btnTaiLai) {
		btnTaiLai.setEnabled(false);
		new SwingWorker<ThongKeHoatDongGaraDAO.ThongKeQuanLy, Void>() {
			@Override
			protected ThongKeHoatDongGaraDAO.ThongKeQuanLy doInBackground() throws SQLException {
				return new ThongKeHoatDongGaraDAO().layThongKeQuanLy();
			}

			@Override
			protected void done() {
				try {
					ThongKeHoatDongGaraDAO.ThongKeQuanLy thongKe = get();
					lblGiaTri[0].setText(thongKe.nhanVienCoMat() + " / "
							+ thongKe.nhanVienDuocPhanCong());
					lblGiaTri[1].setText(String.valueOf(thongKe.phieuTiepNhanHomNay()));
					lblGiaTri[2].setText(String.valueOf(thongKe.xeDangSuaChua()));
					lblGiaTri[3].setText(String.valueOf(thongKe.tongHoaDon()));
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
					showError("Đã ngắt khi tải thống kê dành cho quản lý.");
				} catch (ExecutionException exception) {
					Throwable cause = exception.getCause();
					String message = cause == null ? exception.getMessage() : cause.getMessage();
					showError("Không thể tải thống kê dành cho quản lý: " + message);
				} finally {
					btnTaiLai.setEnabled(true);
				}
			}
		}.execute();
	}

	private void showError(String message) {
		javax.swing.JOptionPane.showMessageDialog(this, message, "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
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

	public BaseFrame(JPanel content) {
		this();
		setContent(content);
	}

}
