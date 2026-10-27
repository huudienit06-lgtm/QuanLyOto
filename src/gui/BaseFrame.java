package gui;

import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class BaseFrame extends JFrame {
	private static final Color PALE_BLUE = new Color(242, 247, 255);
	private static final Color MUTED = new Color(112, 128, 150);
	private final JLabel lblStatus = new JLabel("Sẵn sàng");
	private JPanel pnlCurrentContent;
	private TaiKhoan taiKhoanDangNhap;

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
		if ("Đăng xuất".equals(featureName)) {
			hienThiDangNhap();
			return;
		}
		if ("Nhân viên".equals(featureName) || "Cập nhật nhân viên".equals(featureName)) {
			setContent(new FrmNhanVien());
			lblStatus.setText("Quản lý nhân viên");
			return;
		}
		if ("Ca làm việc".equals(featureName)) {
			setContent(new FrmCaLamViec());
			lblStatus.setText("Quản lý ca làm việc");
			return;
		}
		if ("Báo cáo tổng hợp".equals(featureName)) {
			setContent(new FrmBaoCaoTongHop());
			lblStatus.setText("Báo cáo tổng hợp");
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
			// FrmDangNhap taiKhoan =  new FrmDangNhap(this);
			// taiKhoanDangNhap = taiKhoan.phanQuyen();
			setContent(new FrmNhapXuatPhuTung(taiKhoanDangNhap));
			setContent(new FrmNhapXuatPhuTung(FrmDangNhap.taiKhoanDangNhap));
            lblStatus.setText("Lập phiếu nhập kho phụ tùng");
            return;
        }
		if ("Phụ tùng tồn kho".equals(featureName)) {
			setContent(new FrmThongKeTonKho());
			lblStatus.setText("Thống kê tồn kho phụ tùng");
        	return;
    }
		if ("Hóa đơn".equals(featureName) || "Lập hóa đơn".equals(featureName)) {
			setContent(new FrmHoaDon());
			lblStatus.setText("Lập hóa đơn");
			return;
		}
		if ("Dịch vụ sửa chữa".equals(featureName)) {
			setContent(new FrmDichVu());
			lblStatus.setText("Dịch vụ sửa chữa");
			return;
		}
		if ("Lập phiếu sửa chữa".equals(featureName)) {
			setContent(new FrmPhieuSuaChua());
			lblStatus.setText("Lập phiếu sửa chữa");
			return;
		}
		if ("Bàn giao xe".equals(featureName)) {
			setContent(new FrmBanGiaoXe());
			lblStatus.setText("Bàn giao xe");
			return;
		}
		lblStatus.setText("Đã chọn: " + featureName);
	}

	void hienThiMenu(JMenuBar menuBar, String status) {
		setJMenuBar(menuBar);
		setContent(createWorkspace());
		lblStatus.setText(status);
	}

	private void hienThiDangNhap() {
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

	public BaseFrame(JPanel content) {
		this();
		setContent(content);
	}

}
