package gui;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.Normalizer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class FrmDangNhap extends JPanel {
	private static final Color BLUE = new Color(36, 99, 235);
	private static final Color PALE_BLUE = new Color(242, 247, 255);
	private static final Color TEXT = new Color(47, 62, 84);
	private static final Color MUTED = new Color(112, 128, 150);
	private static final Color BORDER = new Color(221, 231, 245);

	private final BaseFrame frame;
	private final JTextField txtTenDangNhap = new JTextField(22);
	private final JPasswordField pwdMatKhau = new JPasswordField(22);
	private final JCheckBox chkHienMatKhau = new JCheckBox("Hiển thị mật khẩu");

	public FrmDangNhap(BaseFrame frame) {
		this.frame = frame;
		setLayout(new GridBagLayout());
		setBackground(PALE_BLUE);
		setBorder(new EmptyBorder(28, 24, 28, 24));

		JPanel pnlCard = new JPanel(new BorderLayout());
		pnlCard.setBackground(Color.WHITE);
		pnlCard.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(BORDER, 1, true),
				new EmptyBorder(0, 0, 26, 0)));
		pnlCard.setPreferredSize(new Dimension(440, 430));

		JPanel pnlHeader = new JPanel(new BorderLayout(0, 7));
		pnlHeader.setBackground(BLUE);
		pnlHeader.setBorder(new EmptyBorder(24, 30, 22, 30));
		JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
		lblTitle.setForeground(Color.WHITE);
		JLabel lblSubtitle = new JLabel("Hệ thống quản lý gara ô tô", SwingConstants.CENTER);
		lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblSubtitle.setForeground(new Color(225, 235, 255));
		pnlHeader.add(lblTitle, BorderLayout.CENTER);
		pnlHeader.add(lblSubtitle, BorderLayout.SOUTH);

		JPanel pnlForm = new JPanel(new GridBagLayout());
		pnlForm.setOpaque(false);
		pnlForm.setBorder(new EmptyBorder(22, 32, 0, 32));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 7, 0);

		JLabel lblTenDangNhap = createFieldLabel("Tên đăng nhập");
		pnlForm.add(lblTenDangNhap, gbc);
		gbc.gridy++;
		txtTenDangNhap.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		styleTextField(txtTenDangNhap);
		pnlForm.add(txtTenDangNhap, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(16, 0, 7, 0);
		JLabel lblMatKhau = createFieldLabel("Mật khẩu");
		pnlForm.add(lblMatKhau, gbc);
		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 4, 0);
		pwdMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		styleTextField(pwdMatKhau);
		pnlForm.add(pwdMatKhau, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(2, 0, 12, 0);
		chkHienMatKhau.setOpaque(false);
		chkHienMatKhau.setForeground(MUTED);
		chkHienMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		chkHienMatKhau.addActionListener(event ->
				pwdMatKhau.setEchoChar(chkHienMatKhau.isSelected() ? (char) 0 : '\u2022'));
		pnlForm.add(chkHienMatKhau, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(5, 0, 8, 0);
		JButton btnDangNhap = new JButton("Đăng Nhập");
		btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnDangNhap.setForeground(Color.WHITE);
		btnDangNhap.setBackground(BLUE);
		btnDangNhap.setFocusPainted(false);
		btnDangNhap.setBorder(new EmptyBorder(10, 16, 10, 16));
		btnDangNhap.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		btnDangNhap.addActionListener(event -> phanQuyen());
		pnlForm.add(btnDangNhap, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 0, 0);
		JButton btnQuenMatKhau = new JButton("Quên mật khẩu?");
		btnQuenMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		btnQuenMatKhau.setForeground(BLUE);
		btnQuenMatKhau.setContentAreaFilled(false);
		btnQuenMatKhau.setBorderPainted(false);
		btnQuenMatKhau.setFocusPainted(false);
		btnQuenMatKhau.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		btnQuenMatKhau.addActionListener(event -> JOptionPane.showMessageDialog(
				this,
				"Vui lòng liên hệ quản trị viên để được hỗ trợ đặt lại mật khẩu.",
				"Quên mật khẩu",
				JOptionPane.INFORMATION_MESSAGE));
		pnlForm.add(btnQuenMatKhau, gbc);

		pnlCard.add(pnlHeader, BorderLayout.NORTH);
		pnlCard.add(pnlForm, BorderLayout.CENTER);
		add(pnlCard, new GridBagConstraints());
		txtTenDangNhap.addActionListener(event -> pwdMatKhau.requestFocusInWindow());
		pwdMatKhau.addActionListener(event -> phanQuyen());
	}

	private JLabel createFieldLabel(String text) {
		JLabel lblField = new JLabel(text);
		lblField.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblField.setForeground(TEXT);
		return lblField;
	}

	private void styleTextField(JTextField txtField) {
		txtField.setForeground(TEXT);
		txtField.setCaretColor(BLUE);
		txtField.setBackground(Color.WHITE);
		txtField.setPreferredSize(new Dimension(360, 40));
		txtField.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(BORDER, 1, true),
				new EmptyBorder(7, 11, 7, 11)));
	}

	public void phanQuyen() {
		String tenDangNhap = txtTenDangNhap.getText().trim();
		char[] matKhauChars = pwdMatKhau.getPassword();
		if (tenDangNhap.isEmpty() || matKhauChars.length == 0) {
			java.util.Arrays.fill(matKhauChars, '\0');
			JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập và mật khẩu.",
					"Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			TaiKhoan taiKhoan = new TaiKhoanDAO().dangNhap(tenDangNhap, new String(matKhauChars));
			if (taiKhoan == null) {
				JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng.",
						"Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!taiKhoanDangHoatDong(taiKhoan.getTrangThai())) {
				JOptionPane.showMessageDialog(this,
						"Tài khoản hiện không hoạt động. Trạng thái trong TaiKhoan: ["
								+ taiKhoan.getTrangThai() + "].",
						"Không thể đăng nhập", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String vaiTro = chuanHoa(taiKhoan.getVaiTro()).replaceAll("\\s+", "");
			if (vaiTro.contains("quanly") || vaiTro.contains("admin")) {
				frame.hienThiMenu(new MenuQL(frame, taiKhoan), "Đăng nhập với quyền quản lý");
			} else if (vaiTro.contains("nhanvien") || vaiTro.contains("employee")) {
				frame.hienThiMenu(new MenuNV(frame, taiKhoan), "Đăng nhập với quyền nhân viên");
			} else {
				JOptionPane.showMessageDialog(this,
						"Vai trò tài khoản chưa được hỗ trợ: " + taiKhoan.getVaiTro(),
						"Không thể phân quyền", JOptionPane.ERROR_MESSAGE);
			}
		} catch (SQLException exception) {
			JOptionPane.showMessageDialog(this,
					"Không thể xác thực tài khoản do lỗi cơ sở dữ liệu: " + exception.getMessage(),
					"Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
		} finally {
			java.util.Arrays.fill(matKhauChars, '\0');
		}
	}

	private boolean taiKhoanDangHoatDong(String trangThai) {
		// Giá trị null hoặc chuỗi rỗng được coi là không ràng buộc (mặc định cho phép)
		if (trangThai == null || trangThai.isBlank()) {
			return true;
		}
		// Chuẩn hoá Unicode và loại bỏ dấu, sau đó loại bỏ mọi ký tự không phải chữ/ số
		String trangThaiChuanHoa = chuanHoa(trangThai)
				.replaceAll("[^a-z0-9\\s]", " ") // thay ký tự lạ bằng khoảng trắng
				.replaceAll("\\s+", " ") // gộp khoảng trắng liên tiếp
				.trim();

		// Các giá trị hợp lệ (hỗ trợ nhiều biến thể và trường hợp có ký tự vô hình)
		if (trangThaiChuanHoa.equals("hoat dong") || trangThaiChuanHoa.equals("dang hoat dong")
				|| trangThaiChuanHoa.equals("active") || trangThaiChuanHoa.equals("1")) {
			return true;
		}

		// Nếu chuỗi chứa cả 'hoat' và 'dong' ở bất kỳ vị trí nào thì vẫn coi là hoạt động
		if (trangThaiChuanHoa.contains("hoat") && trangThaiChuanHoa.contains("dong")) {
			return true;
		}
		return false;
	}

	private String chuanHoa(String giaTri) {
		if (giaTri == null) {
			return "";
		}
		return Normalizer.normalize(giaTri, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase()
				.trim();
	}
}
