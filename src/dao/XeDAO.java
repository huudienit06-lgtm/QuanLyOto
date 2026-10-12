package dao;

import entity.Xe;
import java.sql.SQLException;
import java.util.List;

public class XeDAO extends BaseDAO<Xe> {
    public XeDAO() {
        super(Xe.class, "Xe", "maXe");
    }

    public List<Xe> getAllCacXe() {
        try {
            return findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public boolean createXe(Xe xe) {
        try {
            return insert(xe);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateXe(Xe xe) {
        try {
            return update(xe);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteXe(String maXe) {
        try {
            return deleteById(maXe);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}