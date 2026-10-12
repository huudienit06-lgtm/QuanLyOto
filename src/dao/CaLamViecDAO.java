package dao;

import entity.CaLamViec;

public class CaLamViecDAO extends BaseDAO<CaLamViec> {
    public CaLamViecDAO() {
        super(CaLamViec.class, "CaLamViec", "maCa");
    }
}
