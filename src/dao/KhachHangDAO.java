package dao;

import entity.KhachHang;

public class KhachHangDAO extends BaseDAO<KhachHang> {
    public KhachHangDAO() {
        super(KhachHang.class, "KhachHang", "maKH");
    }
}
