package dao;

import entity.PhieuSuaChua;

public class PhieuSuaChuaDAO extends BaseDAO<PhieuSuaChua> {
    public PhieuSuaChuaDAO() {
        super(PhieuSuaChua.class, "PhieuSuaChua", "maPhieuSuaChua");
    }
}
