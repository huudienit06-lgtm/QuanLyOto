package dao;

import entity.ChiTietHoaDon;

public class ChiTietHoaDonDAO extends BaseDAO<ChiTietHoaDon> {
    public ChiTietHoaDonDAO() {
        super(ChiTietHoaDon.class, "ChiTietHoaDon", "maHoaDon", "maPhieuSuaChua");
    }
}
