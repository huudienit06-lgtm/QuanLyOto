package dao;

import entity.ChiTietPhieuNhap;

public class ChiTietPhieuNhapDAO extends BaseDAO<ChiTietPhieuNhap> {
    public ChiTietPhieuNhapDAO() {
        super(ChiTietPhieuNhap.class, "ChiTietPhieuNhap", "maPhieuNhapXuat", "maPhuTung");
    }
}
