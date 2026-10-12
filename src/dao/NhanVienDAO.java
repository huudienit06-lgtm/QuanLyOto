package dao;

import entity.NhanVien;

public class NhanVienDAO extends BaseDAO<NhanVien> {
    public NhanVienDAO() {
        super(NhanVien.class, "NhanVien", "maNV");
    }
}
