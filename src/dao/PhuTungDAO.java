package dao;

import entity.PhuTung;

public class PhuTungDAO extends BaseDAO<PhuTung> {
    public PhuTungDAO() {
        super(PhuTung.class, "PhuTung", "maPhuTung");
    }
}
