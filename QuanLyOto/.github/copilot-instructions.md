# GitHub Copilot Instructions for UngDungQLGaraOto

## 1. Environment & Tech Stack

- Project Type: Java Desktop Application (Java Swing)
- JDK Version: Java 17
- Database: MySQL / SQL Server using JDBC
- Architecture: 3-Tier Architecture (Entity -> DAO -> BUS/Services -> GUI)

## 2. Naming Conventions (Quy tắc đặt tên)

### A. General Naming Conventions

- **Class names**: PascalCase (e.g., `XeDAO`, `ThanhToanFrame`, `NhanVien`).
- **Methods & Standard Variables**: camelCase (e.g., `layDanhSachXe()`, `maHoaDon`).
- **Constants**: UPPER_SNAKE_CASE (e.g., `DATABASE_URL`, `MAX_CAPACITY`).

### B. UI Component Prefixes (Hungarian Notation - BẮT BUỘC CHỦ ĐỘNG ÁP DỤNG)

Khi khai báo hoặc khởi tạo các thành phần giao diện Java Swing, BẮT BUỘC phải thêm tiền tố (prefix) tương ứng với loại component vào trước tên biến:

- `btn` cho **JButton** (e.g., `btnThem`, `btnXoa`, `btnSua`, `btnLuu`, `btnHuy`, `btnThoat`)
- `txt` cho **JTextField** (e.g., `txtMaXe`, `txtTenKhachHang`, `txtSoDienThoai`)
- `pwd` cho **JPasswordField** (e.g., `pwdMatKhau`, `pwdXacNhanMatKhau`)
- `lbl` cho **JLabel** (e.g., `lblTieuDe`, `lblTongTien`, `lblTrangThai`)
- `cbo` cho **JComboBox** (e.g., `cboLoaiXe`, `cboChucVu`, `cboGara`)
- `tbl` cho **JTable** (e.g., `tblDanhSachXe`, `tblHoaDon`)
- `chk` cho **JCheckBox** (e.g., `chkDaThanhToan`, `chkGhiNhoDangNhap`)
- `rdo` cho **JRadioButton** (e.g., `rdoNam`, `rdoNu`)
- `pnl` cho **JPanel** (e.g., `pnlMain`, `pnlHeader`, `pnlButton`)
- `scr` cho **JScrollPane** (e.g., `scrTableXe`, `scrContent`)
- `txa` cho **JTextArea** (e.g., `txaGhiChu`, `txaMoTaLoi`)
- `dlg` cho **JDialog** (e.g., `dlgThemKhachHang`)

## 3. Architecture & Coding Guidelines

- Every GUI Frame must extend `src/gui/BaseFrame.java`.
- SQL and Database logic must ONLY be placed inside the `dao` package.
- UI elements must NOT execute direct database queries; pass requests through `bus` or `dao` classes.
- Use `PreparedStatement` for all database queries to prevent SQL Injection.
- Always use `try-with-resources` when dealing with `Connection`, `PreparedStatement`, and `ResultSet`.

## 4. Code Style & Documentation

- Comments and Javadoc must be written in Vietnamese.
- Provide clear try-catch blocks with descriptive error messages when handling exceptions.
- Do not use deprecated AWT/Swing methods.
