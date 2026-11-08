package controller;

import dao.BaoCaoTongHopDAO;
import gui.FrmBaoCaoTongHop;

public class BaoCaoTongHopController {
    private final FrmBaoCaoTongHop view;
    private final BaoCaoTongHopDAO baoCaoTongHopDao;

    public BaoCaoTongHopController(FrmBaoCaoTongHop view) {
        this.view = view;
        this.baoCaoTongHopDao = new BaoCaoTongHopDAO();
    }

    public void khoiTao() {
        view.getBtnTaoBaoCao().addActionListener(event -> view.taoBaoCao(baoCaoTongHopDao));
        view.getBtnXuatBaoCao().addActionListener(event -> view.xuatBaoCao());
    }
}