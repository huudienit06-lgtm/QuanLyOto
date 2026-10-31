package util;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaTuDong {
    public static final String TIEN_TO_KHACH_HANG = "KH";
    public static final String TIEN_TO_XE = "X";
    public static final String TIEN_TO_NHAN_VIEN = "NV";
    public static final String TIEN_TO_PHU_TUNG = "PT";
    public static final String TIEN_TO_DICH_VU = "DMDV";
    public static final String TIEN_TO_CA_LAM_VIEC = "CA";
    public static final String TIEN_TO_LICH_LAM_VIEC = "LLV";
    public static final String TIEN_TO_PHIEU_TIEP_NHAN = "PTN";
    public static final String TIEN_TO_PHIEU_KIEM_TRA = "PKT";
    public static final String TIEN_TO_PHIEU_SUA_CHUA = "PSC";
    public static final String TIEN_TO_HOA_DON = "HD";
    public static final String TIEN_TO_GIAO_DICH = "GD";
    public static final String TIEN_TO_PHIEU_NHAP_KHO = "PN";
    public static final String TIEN_TO_PHIEU_XUAT_KHO = "PX";
    public static final String TIEN_TO_KHUYEN_MAI = "KM";
    public static final String TIEN_TO_PHIEU_BAO_HANH = "PBH";
    public static final String TIEN_TO_PHIEU_BAN_GIAO = "PBG";
    public static final String TIEN_TO_PHIEU_DOI_TRA = "PDT";

    private MaTuDong() {
    }

    public static <T> String sinhMa(
            String tienTo, Iterable<T> danhSach, Function<? super T, String> layMa) {
        if (tienTo == null || tienTo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiền tố mã không được để trống.");
        }
        if (danhSach == null || layMa == null) {
            throw new IllegalArgumentException("Danh sách và hàm lấy mã không được để trống.");
        }

        String tienToChuan = tienTo.trim();
        Pattern pattern = Pattern.compile("^" + Pattern.quote(tienToChuan) + "(\\d+)$",
                Pattern.CASE_INSENSITIVE);
        Set<String> cacMaDaCo = new HashSet<>();
        BigInteger soLonNhat = BigInteger.ZERO;
        int doRongSoLonNhat = 0;

        for (T phanTu : danhSach) {
            if (phanTu == null) {
                continue;
            }
            String ma = layMa.apply(phanTu);
            if (ma == null || ma.trim().isEmpty()) {
                continue;
            }

            String maChuan = ma.trim();
            cacMaDaCo.add(maChuan.toUpperCase(Locale.ROOT));
            Matcher matcher = pattern.matcher(maChuan);
            if (matcher.matches()) {
                BigInteger soHienTai = new BigInteger(matcher.group(1));
                if (soHienTai.compareTo(soLonNhat) > 0) {
                    soLonNhat = soHienTai;
                    doRongSoLonNhat = matcher.group(1).length();
                }
            }
        }

        int doRong = Math.max(3, doRongSoLonNhat);
        BigInteger soTiepTheo = soLonNhat.add(BigInteger.ONE);
        String maMoi;
        do {
            String soDuocDinhDang = soTiepTheo.toString();
            maMoi = tienToChuan + "0".repeat(Math.max(0, doRong - soDuocDinhDang.length()))
                    + soDuocDinhDang;
            soTiepTheo = soTiepTheo.add(BigInteger.ONE);
        } while (cacMaDaCo.contains(maMoi.toUpperCase(Locale.ROOT)));
        return maMoi;
    }
}
