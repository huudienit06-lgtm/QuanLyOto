package dao;

import entity.CaLamViec;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class CaLamViecDAO extends BaseDAO<CaLamViec> {
    public CaLamViecDAO() {
        super(CaLamViec.class, "CaLamViec", "maCa");
    }

    @Override
    public List<CaLamViec> findAll() throws SQLException {
        List<CaLamViec> danhSachCa = super.findAll();
        danhSachCa.sort(Comparator.comparing(CaLamViec::getGioBatDau,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return danhSachCa;
    }
}
