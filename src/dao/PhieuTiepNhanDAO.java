package dao;

import entity.PhieuTiepNhan;

public class PhieuTiepNhanDAO extends BaseDAO<PhieuTiepNhan> {
    public PhieuTiepNhanDAO() {
        super(PhieuTiepNhan.class, "PhieuTiepNhan", "maPhieuTiepNhan");
    }
}
