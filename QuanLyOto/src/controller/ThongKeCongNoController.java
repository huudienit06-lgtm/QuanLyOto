package controller;

import dao.ThongKeCongNoDAO;
import gui.FrmThongKeCongNo;
import java.sql.SQLException;
import java.util.List;

public class ThongKeCongNoController {
    private final FrmThongKeCongNo view;
    private final ThongKeCongNoDAO thongKeCongNoDao;

    public ThongKeCongNoController(FrmThongKeCongNo view) {
        this.view = view;
        this.thongKeCongNoDao = new ThongKeCongNoDAO();
    }

    public void taiDuLieu() throws SQLException {
        view.napDuLieu(thongKeCongNoDao.layDuLieu());
    }

    public List<ThongKeCongNoDAO.GiaoDichData> taiGiaoDich(String maHoaDon) throws SQLException {
        return thongKeCongNoDao.layGiaoDich(maHoaDon);
    }
}