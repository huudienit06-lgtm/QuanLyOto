package dao;

import entity.GiaoDichThanhToan;

public class GiaoDichThanhToanDAO extends BaseDAO<GiaoDichThanhToan> {
    public GiaoDichThanhToanDAO() {
        super(GiaoDichThanhToan.class, "GiaoDichThanhToan", "maGD");
    }
}
