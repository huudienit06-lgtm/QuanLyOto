package dao;

import entity.PhieuBanGiao;

public class PhieuBanGiaoDAO extends BaseDAO<PhieuBanGiao> {
    public PhieuBanGiaoDAO() {
        super(PhieuBanGiao.class, "PhieuBanGiao", "maPhieuBanGiao");
    }
}
