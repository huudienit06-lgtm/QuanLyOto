package dao;

import entity.LichLamViec;

public class LichLamViecDAO extends BaseDAO<LichLamViec> {
    public LichLamViecDAO() {
        super(LichLamViec.class, "LichLamViec", "maLich");
    }
}
