package controller;

import dao.ThongKeDoanhThuDAO;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.table.TableModel;

/**
 * Controller của màn hình Thống kê doanh thu: giữ dữ liệu đã tải, lọc theo
 * phạm vi, tính tổng / công nợ / dữ liệu biểu đồ và xuất Excel.
 * Giao diện (FrmThongKeDoanhThu) chỉ gọi các hàm ở đây.
 */
public class ThongKeDoanhThuController {

    // Chế độ thống kê
    public static final int CHE_DO_NGAY = 0;
    public static final int CHE_DO_THANG = 1;
    public static final int CHE_DO_NAM = 2;
    // Phạm vi thống kê
    public static final int PHAM_VI_TAT_CA = 0;
    public static final int PHAM_VI_NHAN_VIEN = 1;
    public static final int PHAM_VI_KHACH_HANG = 2;

    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private static final DateTimeFormatter GIO_NGAY = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final String[] COT_NGAY = { "Mã hóa đơn", "Nhân viên", "Khách hàng", "Biển số xe",
            "Tổng tiền hóa đơn", "Còn nợ", "Trạng thái thanh toán" };
    private static final String BIEN_SO_MAC_DINH = "Chưa xác định";
    private static final String KHACH_MAC_DINH = "Chưa có thông tin khách hàng";

    // KẾT QUẢ TRẢ VỀ CHO GIAO DIỆN (không phải entity)
    public static class KetQuaThongKe {
        public String[] tieuDeCot;
        public List<Object[]> dong = new ArrayList<>();
        // Dòng i của bảng ứng với hóa đơn i (chế độ ngày) hoặc khóa ngày i (chế độ
        // tháng/năm)
        public List<HoaDon> dongHoaDon = new ArrayList<>();
        public List<LocalDate> dongKhoa = new ArrayList<>();
        public long soHoaDon, tongTien, noTrongKy, noLuyKe;
        public DuLieuBieuDo bieuDo; // null ở chế độ ngày
    }

    public static class DuLieuBieuDo {
        public String[] nhan;
        public long[][] giaTri; // {đã thu, nợ trong kỳ, nợ lũy kế}
        public long[] soHoaDon;
        public String tenTrucX;
    }

    // DỮ LIỆU
    private final ThongKeDoanhThuDAO dao = new ThongKeDoanhThuDAO();
    private final Map<String, NhanVien> dsNhanVien = new LinkedHashMap<>();
    private final Map<String, KhachHang> dsKhachHang = new LinkedHashMap<>();
    private final List<HoaDon> dsHoaDon = new ArrayList<>();
    private final Map<String, Long> daThuTheoHoaDon = new HashMap<>();

    public void taiDuLieu() throws SQLException {
        dsNhanVien.clear();
        dsKhachHang.clear();
        dsHoaDon.clear();
        daThuTheoHoaDon.clear();

        for (NhanVien nv : dao.layDanhSachNhanVien()) {
            dsNhanVien.put(nv.getMaNV(), nv);
        }
        for (KhachHang kh : dao.layDanhSachKhachHang()) {
            dsKhachHang.put(kh.getMaKH(), kh);
        }
        dsHoaDon.addAll(dao.layDanhSachHoaDon());
        daThuTheoHoaDon.putAll(dao.layTongDaThuTheoHoaDon());
    }

    public Collection<NhanVien> getDanhSachNhanVien() {
        return dsNhanVien.values();
    }

    public Collection<KhachHang> getDanhSachKhachHang() {
        return dsKhachHang.values();
    }

    // maNV -> số hóa đơn đã lập
    public Map<String, Integer> demHoaDonTheoNhanVien() {
        Map<String, Integer> kq = new HashMap<>();
        for (HoaDon hd : dsHoaDon) {
            String ma = maNV(hd);
            if (ma != null) {
                kq.merge(ma, 1, Integer::sum);
            }
        }
        return kq;
    }

    // THÔNG TIN MỘT HÓA ĐƠN
    public static String formatTien(long soTien) {
        return MONEY.format(soTien) + " đ";
    }

    private static String maNV(HoaDon hd) {
        return hd.getNhanVien() == null ? null : hd.getNhanVien().getMaNV();
    }

    private static String maKH(HoaDon hd) {
        return hd.getKhachHang() == null ? null : hd.getKhachHang().getMaKH();
    }

    public String tenNhanVien(HoaDon hd) {
        String ma = maNV(hd);
        if (ma == null) {
            return "";
        }
        NhanVien nv = dsNhanVien.get(ma);
        return nv == null ? ma : nv.getHoTen();
    }

    public String tenKhachHang(HoaDon hd) {
        KhachHang kh = hd.getKhachHang();
        return kh == null || kh.getHoTen() == null ? KHACH_MAC_DINH : kh.getHoTen();
    }

    public String soDienThoai(HoaDon hd) {
        KhachHang kh = hd.getKhachHang();
        return kh == null || kh.getSoDT() == null ? "" : kh.getSoDT();
    }

    public String bienSoXe(HoaDon hd) {
        return BIEN_SO_MAC_DINH;
    }

    public long tongTien(HoaDon hd) {
        return hd.getSoTien() == null ? 0L : hd.getSoTien().longValue();
    }

    // Tiền đã thu thành công, giới hạn trong [0, tổng tiền]
    public long daThu(HoaDon hd) {
        long daThu = daThuTheoHoaDon.getOrDefault(hd.getMaHoaDon(), 0L);
        return Math.max(0L, Math.min(daThu, tongTien(hd)));
    }

    public long conPhaiThu(HoaDon hd) {
        return Math.max(0L, tongTien(hd) - daThu(hd));
    }

    public String trangThai(HoaDon hd) {
        long daThu = daThu(hd);
        if (daThu <= 0) {
            return "Chưa thanh toán";
        }
        if (daThu < tongTien(hd)) {
            return "Thanh toán một phần";
        }
        return "Đã thanh toán";
    }

    private static LocalDate ngay(HoaDon hd) {
        return hd.getNgayLap() == null
                ? null
                : hd.getNgayLap().toLocalDateTime().toLocalDate();
    }

    // Lịch sử thanh toán của hóa đơn, mỗi dòng: {STT, thời gian, phương thức, số
    // tiền, trạng thái}
    public List<Object[]> layLichSuThanhToan(String maHoaDon) throws SQLException {
        List<Object[]> ds = new ArrayList<>();
        int stt = 1;
        for (Object[] gd : dao.layLichSuGiaoDich(maHoaDon)) {
            Timestamp thoiGian = (Timestamp) gd[0];
            ds.add(new Object[] {
                    String.valueOf(stt++),
                    thoiGian == null ? "-" : thoiGian.toLocalDateTime().format(GIO_NGAY),
                    gd[1] == null ? "-" : gd[1],
                    formatTien((Long) gd[2]),
                    gd[3] == null ? "-" : gd[3] });
        }
        return ds;
    }

    // THỐNG KÊ
    // maNV / maKH chỉ dùng khi phạm vi tương ứng; null nghĩa là chưa chọn ai
    public KetQuaThongKe tinhThongKe(int cheDo, int nam, int thang, int ngay, int phamVi,
            String maNV, String maKH) {
        YearMonth ym = YearMonth.of(nam, thang);
        ngay = Math.min(ngay, ym.lengthOfMonth());

        LocalDate batDau, ketThuc;
        if (cheDo == CHE_DO_NGAY) {
            batDau = ketThuc = ym.atDay(ngay);
        } else if (cheDo == CHE_DO_THANG) {
            batDau = ym.atDay(1);
            ketThuc = ym.atEndOfMonth();
        } else {
            batDau = LocalDate.of(nam, 1, 1);
            ketThuc = LocalDate.of(nam, 12, 31);
        }

        List<HoaDon> dsLoc = locTheoPhamVi(phamVi, maNV, maKH);
        KetQuaThongKe kq = new KetQuaThongKe();

        // Công nợ: còn nợ của hóa đơn lập trong kỳ, và lũy kế đến cuối kỳ
        for (HoaDon hd : dsLoc) {
            if (!ngay(hd).isBefore(batDau) && !ngay(hd).isAfter(ketThuc)) {
                kq.noTrongKy += conPhaiThu(hd);
            }
            if (!ngay(hd).isAfter(ketThuc)) {
                kq.noLuyKe += conPhaiThu(hd);
            }
        }

        kq.tieuDeCot = layTieuDeCot(cheDo);
        if (cheDo == CHE_DO_NGAY) {
            napBangNgay(kq, dsLoc, batDau);
        } else {
            napBangNhom(kq, cheDo, dsLoc, batDau, ketThuc);
            kq.bieuDo = tinhBieuDo(cheDo, dsLoc, ym, nam);
        }
        return kq;
    }

    private List<HoaDon> locTheoPhamVi(int phamVi, String maNV, String maKH) {
        List<HoaDon> dsLoc = new ArrayList<>();
        for (HoaDon hd : dsHoaDon) {
            boolean hopLe;
            if (phamVi == PHAM_VI_NHAN_VIEN) {
                hopLe = maNV != null && maNV.equals(maNV(hd));
            } else if (phamVi == PHAM_VI_KHACH_HANG) {
                hopLe = maKH != null && maKH.equals(maKH(hd));
            } else {
                hopLe = true;
            }
            if (hopLe) {
                dsLoc.add(hd);
            }
        }
        return dsLoc;
    }

    private String[] layTieuDeCot(int cheDo) {
        if (cheDo == CHE_DO_NGAY) {
            return COT_NGAY;
        }
        String cotDau = cheDo == CHE_DO_THANG ? "Ngày" : "Tháng";
        return new String[] { cotDau, "Số hóa đơn", "Tổng tiền hóa đơn", "Tổng tiền đã thu", "Tổng tiền nợ" };
    }

    // Chế độ ngày: liệt kê từng hóa đơn trong ngày
    private void napBangNgay(KetQuaThongKe kq, List<HoaDon> dsLoc, LocalDate ngay) {
        List<HoaDon> dsNgay = new ArrayList<>();
        for (HoaDon hd : dsLoc) {
            if (ngay(hd).equals(ngay)) {
                dsNgay.add(hd);
            }
        }
        dsNgay.sort((a, b) -> a.getNgayLap().toLocalDateTime().toLocalTime()
                .compareTo(b.getNgayLap().toLocalDateTime().toLocalTime()));
        for (HoaDon hd : dsNgay) {
            kq.dong.add(new Object[] {
                    hd.getMaHoaDon(),
                    tenNhanVien(hd),
                    tenKhachHang(hd),
                    bienSoXe(hd),
                    formatTien(tongTien(hd)),
                    formatTien(conPhaiThu(hd)),
                    trangThai(hd)
            });
            kq.dongHoaDon.add(hd);
            kq.tongTien += tongTien(hd);
        }
        kq.soHoaDon = dsNgay.size();
    }

    // Chế độ tháng / năm: gom theo ngày hoặc theo tháng
    private void napBangNhom(KetQuaThongKe kq, int cheDo, List<HoaDon> dsLoc, LocalDate batDau, LocalDate ketThuc) {
        // Mỗi nhóm: {số hóa đơn, tổng tiền, tiền đã thu, còn nợ}
        TreeMap<LocalDate, long[]> dsNhom = new TreeMap<>();
        for (HoaDon hd : dsLoc) {
            LocalDate ngay = ngay(hd);
            if (ngay.isBefore(batDau) || ngay.isAfter(ketThuc)) {
                continue;
            }
            LocalDate khoa = cheDo == CHE_DO_THANG ? ngay : ngay.withDayOfMonth(1);
            long[] nhom = dsNhom.computeIfAbsent(khoa, k -> new long[4]);
            nhom[0]++;
            nhom[1] += tongTien(hd);
            nhom[2] += daThu(hd);
            nhom[3] += conPhaiThu(hd);
        }

        for (Map.Entry<LocalDate, long[]> muc : dsNhom.entrySet()) {
            long[] nhom = muc.getValue();
            String nhan = String.valueOf(cheDo == CHE_DO_THANG
                    ? muc.getKey().getDayOfMonth()
                    : muc.getKey().getMonthValue());
            kq.dong.add(new Object[] { nhan, nhom[0], formatTien(nhom[1]), formatTien(nhom[2]),
                    formatTien(nhom[3]) });
            kq.dongKhoa.add(muc.getKey());
            kq.soHoaDon += nhom[0];
            kq.tongTien += nhom[1];
        }
    }

    // Dữ liệu biểu đồ: doanh thu đã thu, nợ trong kỳ, nợ lũy kế theo ngày (tháng)
    // hoặc theo tháng (năm)
    private DuLieuBieuDo tinhBieuDo(int cheDo, List<HoaDon> dsLoc, YearMonth ym, int nam) {
        boolean theoThang = cheDo == CHE_DO_THANG;
        int soCot = theoThang ? ym.lengthOfMonth() : 12;

        DuLieuBieuDo bd = new DuLieuBieuDo();
        bd.tenTrucX = theoThang ? "Ngày" : "Tháng";
        bd.nhan = new String[soCot];
        bd.giaTri = new long[3][soCot];
        bd.soHoaDon = new long[soCot];

        for (int i = 0; i < soCot; i++) {
            LocalDate tu, den;
            if (theoThang) {
                tu = den = ym.atDay(i + 1);
            } else {
                YearMonth thang = YearMonth.of(nam, i + 1);
                tu = thang.atDay(1);
                den = thang.atEndOfMonth();
            }
            bd.nhan[i] = String.valueOf(i + 1);
            for (HoaDon hd : dsLoc) {
                LocalDate ngay = ngay(hd);
                if (!ngay.isBefore(tu) && !ngay.isAfter(den)) {
                    bd.giaTri[0][i] += daThu(hd);
                    bd.giaTri[1][i] += conPhaiThu(hd);
                    bd.soHoaDon[i]++;
                }
                if (!ngay.isAfter(den)) {
                    bd.giaTri[2][i] += conPhaiThu(hd);
                }
            }
        }
        return bd;
    }

    // XUẤT EXCEL
    private static final int LOAI_CHU_TRAI = 0;
    private static final int LOAI_CHU_GIUA = 1;
    private static final int LOAI_TIEN = 2;
    private static final int LOAI_SO = 3;

    // Chỉ số kiểu ô trong XLSX_STYLES (cellXfs)
    private static final int KIEU_TIEU_DE = 1;
    private static final int KIEU_CHU_TRAI = 2;
    private static final int KIEU_GIUA = 3;
    private static final int KIEU_TIEN = 4;

    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    private static final String OOXML = "http://schemas.openxmlformats.org/";
    private static final String REL_DOC = OOXML + "officeDocument/2006/relationships";

    // Kiểu dáng: chỉ có viền đen mỏng + chữ in đậm cho tiêu đề, không màu
    private static final String XLSX_STYLES = XML_HEAD
            + "<styleSheet xmlns=\"" + OOXML + "spreadsheetml/2006/main\">"
            + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"#,##0&quot; đ&quot;\"/></numFmts>"
            + "<fonts count=\"2\">"
            + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
            + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
            + "</fonts>"
            + "<fills count=\"2\">"
            + "<fill><patternFill patternType=\"none\"/></fill>"
            + "<fill><patternFill patternType=\"gray125\"/></fill>"
            + "</fills>"
            + "<borders count=\"2\">"
            + "<border><left/><right/><top/><bottom/><diagonal/></border>"
            + "<border><left style=\"thin\"><color auto=\"1\"/></left><right style=\"thin\"><color auto=\"1\"/></right>"
            + "<top style=\"thin\"><color auto=\"1\"/></top><bottom style=\"thin\"><color auto=\"1\"/></bottom><diagonal/></border>"
            + "</borders>"
            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
            + "<cellXfs count=\"5\">"
            // 0 mặc định
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
            // 1 tiêu đề cột: in đậm, giữa, có viền
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
            // 2 chữ căn trái, có viền
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"
            // 3 căn giữa, có viền
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
            // 4 tiền, căn phải, có viền
            + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>"
            + "</cellXfs>"
            + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
            + "</styleSheet>";

    // Xác định loại cột theo tên cột và nội dung
    private int xacDinhLoaiCot(TableModel model, int cot) {
        String ten = model.getColumnName(cot);
        if ("Số hóa đơn".equals(ten)) {
            return LOAI_SO;
        }
        for (int dong = 0; dong < model.getRowCount(); dong++) {
            Object giaTri = model.getValueAt(dong, cot);
            if (giaTri instanceof String && ((String) giaTri).endsWith(" đ")) {
                return LOAI_TIEN;
            }
        }
        if ("Ngày".equals(ten) || "Tháng".equals(ten) || "Mã hóa đơn".equals(ten)
                || "Biển số xe".equals(ten) || "Trạng thái thanh toán".equals(ten)) {
            return LOAI_CHU_GIUA;
        }
        return LOAI_CHU_TRAI;
    }

    // Đọc số tiền từ chuỗi "1.234.000 đ"
    private static long docTien(Object giaTri) {
        String s = giaTri == null ? "" : giaTri.toString().replaceAll("[^0-9-]", "");
        if (s.isEmpty() || s.equals("-")) {
            return 0L;
        }
        return Long.parseLong(s);
    }

    // Số cột -> tên cột Excel (0 -> A, 26 -> AA)
    private static String tenCotExcel(int cot) {
        StringBuilder s = new StringBuilder();
        for (cot++; cot > 0; cot = (cot - 1) / 26) {
            s.insert(0, (char) ('A' + (cot - 1) % 26));
        }
        return s.toString();
    }

    private static String thoatXml(String chu) {
        if (chu == null) {
            return "";
        }
        return chu.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static void ghiOChu(StringBuilder sb, int cot, int dong, String chu, int kieu) {
        sb.append("<c r=\"").append(tenCotExcel(cot)).append(dong).append("\" s=\"").append(kieu)
                .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(thoatXml(chu))
                .append("</t></is></c>");
    }

    private static void ghiOSo(StringBuilder sb, int cot, int dong, long so, int kieu) {
        sb.append("<c r=\"").append(tenCotExcel(cot)).append(dong).append("\" s=\"").append(kieu)
                .append("\"><v>").append(so).append("</v></c>");
    }

    private static void ghiZip(ZipOutputStream zos, String ten, String noiDung) throws IOException {
        zos.putNextEntry(new ZipEntry(ten));
        zos.write(noiDung.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    // Ghi bảng đang hiển thị ra file .xlsx: cột STT + các cột của bảng, có viền,
    // tiền lưu dạng số
    public void xuatExcel(File tep, TableModel model) throws IOException {
        final int soCotNguon = model.getColumnCount();
        final int soDong = model.getRowCount();
        final int soCot = soCotNguon + 1; // thêm cột STT

        String[] dsTieuDe = new String[soCot];
        int[] dsLoai = new int[soCot];
        int[] dsRong = new int[soCot];
        dsTieuDe[0] = "STT";
        dsLoai[0] = LOAI_CHU_GIUA;
        for (int c = 0; c < soCotNguon; c++) {
            dsTieuDe[c + 1] = model.getColumnName(c);
            dsLoai[c + 1] = xacDinhLoaiCot(model, c);
        }
        for (int c = 0; c < soCot; c++) {
            dsRong[c] = dsTieuDe[c].length() + 4;
        }
        dsRong[0] = 7;

        StringBuilder dsDongXml = new StringBuilder();

        // Dòng tiêu đề (dòng 1)
        dsDongXml.append("<row r=\"1\">");
        for (int c = 0; c < soCot; c++) {
            ghiOChu(dsDongXml, c, 1, dsTieuDe[c], KIEU_TIEU_DE);
        }
        dsDongXml.append("</row>");

        // Dữ liệu
        for (int d = 0; d < soDong; d++) {
            int dongExcel = 2 + d;
            dsDongXml.append("<row r=\"").append(dongExcel).append("\">");
            ghiOSo(dsDongXml, 0, dongExcel, d + 1, KIEU_GIUA);
            for (int c = 1; c < soCot; c++) {
                Object giaTri = model.getValueAt(d, c - 1);
                String chu = giaTri == null ? "" : giaTri.toString();
                int doDai = chu.length();
                if (dsLoai[c] == LOAI_TIEN) {
                    long tien = docTien(giaTri);
                    ghiOSo(dsDongXml, c, dongExcel, tien, KIEU_TIEN);
                    doDai = MONEY.format(tien).length() + 4;
                } else if (dsLoai[c] == LOAI_SO) {
                    long so = giaTri instanceof Number ? ((Number) giaTri).longValue() : docTien(giaTri);
                    ghiOSo(dsDongXml, c, dongExcel, so, KIEU_GIUA);
                } else {
                    ghiOChu(dsDongXml, c, dongExcel, chu,
                            dsLoai[c] == LOAI_CHU_GIUA ? KIEU_GIUA : KIEU_CHU_TRAI);
                }
                dsRong[c] = Math.max(dsRong[c], Math.min(doDai + 3, 45));
            }
            dsDongXml.append("</row>");
        }

        // Sheet: giữ đường kẻ ô mặc định của Excel, cố định dòng tiêu đề
        StringBuilder sheet = new StringBuilder();
        sheet.append(XML_HEAD)
                .append("<worksheet xmlns=\"").append(OOXML).append("spreadsheetml/2006/main\">")
                .append("<sheetViews><sheetView workbookViewId=\"0\">")
                .append("<pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/>")
                .append("</sheetView></sheetViews>")
                .append("<sheetFormatPr defaultRowHeight=\"15\"/><cols>");
        for (int c = 0; c < soCot; c++) {
            sheet.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                    .append("\" width=\"").append(dsRong[c]).append("\" customWidth=\"1\"/>");
        }
        sheet.append("</cols><sheetData>").append(dsDongXml).append("</sheetData></worksheet>");

        // Đóng gói các phần của file .xlsx (thực chất là file zip chứa XML)
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tep))) {
            ghiZip(zos, "[Content_Types].xml", XML_HEAD
                    + "<Types xmlns=\"" + OOXML + "package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                    + "</Types>");
            ghiZip(zos, "_rels/.rels", XML_HEAD
                    + "<Relationships xmlns=\"" + OOXML + "package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"" + REL_DOC + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            ghiZip(zos, "xl/workbook.xml", XML_HEAD
                    + "<workbook xmlns=\"" + OOXML + "spreadsheetml/2006/main\" xmlns:r=\"" + REL_DOC + "\">"
                    + "<sheets><sheet name=\"Hóa đơn\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            ghiZip(zos, "xl/_rels/workbook.xml.rels", XML_HEAD
                    + "<Relationships xmlns=\"" + OOXML + "package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"" + REL_DOC + "/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "<Relationship Id=\"rId2\" Type=\"" + REL_DOC + "/styles\" Target=\"styles.xml\"/>"
                    + "</Relationships>");
            ghiZip(zos, "xl/styles.xml", XLSX_STYLES);
            ghiZip(zos, "xl/worksheets/sheet1.xml", sheet.toString());
        }
    }
}