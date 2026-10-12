package dao;

import entity.PhieuBaoHanh;

public class PhieuBaoHanhDAO extends BaseDAO<PhieuBaoHanh> {
    public PhieuBaoHanhDAO() {
        super(PhieuBaoHanh.class, "PhieuBaoHanh", "maPhieuBaoHanh");
    }
}
