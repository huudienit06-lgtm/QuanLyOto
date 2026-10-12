package dao;

import entity.ChuongTrinhKhuyenMai;

public class ChuongTrinhKhuyenMaiDAO extends BaseDAO<ChuongTrinhKhuyenMai> {
    public ChuongTrinhKhuyenMaiDAO() {
        super(ChuongTrinhKhuyenMai.class, "ChuongTrinhKhuyenMai", "maKM");
    }
}
