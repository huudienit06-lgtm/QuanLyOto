package dao;

import entity.HoaDon;

public class HoaDonDAO extends BaseDAO<HoaDon> {
    public HoaDonDAO() {
        super(HoaDon.class, "HoaDon", "maHoaDon");
    }
}
