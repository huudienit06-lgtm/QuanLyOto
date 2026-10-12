package dao;

import entity.ChiTietPhieuSuaChua;

public class ChiTietPhieuSuaChuaDAO extends BaseDAO<ChiTietPhieuSuaChua> {
    public ChiTietPhieuSuaChuaDAO() {
        super(ChiTietPhieuSuaChua.class, "ChiTietPhieuSuaChua", "maPhieuSuaChua", "maDV");
    }
}
