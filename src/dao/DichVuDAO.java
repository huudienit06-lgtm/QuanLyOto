package dao;

import entity.DichVu;

public class DichVuDAO extends BaseDAO<DichVu> {
    public DichVuDAO() {
        super(DichVu.class, "DichVu", "maDV");
    }
}
