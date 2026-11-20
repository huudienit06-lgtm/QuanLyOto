package entity;

public class DongXe {
    private String tenHangXe;
    private String tenDongXe;

    public DongXe() {
    }

    public DongXe(String tenHangXe, String tenDongXe) {
        this.tenHangXe = tenHangXe;
        this.tenDongXe = tenDongXe;
    }

    public String getTenHangXe() {
        return tenHangXe;
    }

    public void setTenHangXe(String tenHangXe) {
        this.tenHangXe = tenHangXe;
    }

    public String getTenDongXe() {
        return tenDongXe;
    }

    public void setTenDongXe(String tenDongXe) {
        this.tenDongXe = tenDongXe;
    }

    @Override
    public String toString() {
        return tenDongXe;
    }
}
