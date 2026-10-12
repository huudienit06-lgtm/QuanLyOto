package dao;

import entity.TaiKhoan;

public class TaiKhoanDAO extends BaseDAO<TaiKhoan> {
    public TaiKhoanDAO() {
        super(TaiKhoan.class, "TaiKhoan", "maNV");
    }
}
