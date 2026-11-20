package entity;

public class HangXe {
    private String tenHangXe;

    public HangXe() {
    }

    public HangXe(String tenHangXe) {
        this.tenHangXe = tenHangXe;
    }

    public String getTenHangXe() {
        return tenHangXe;
    }

    public void setTenHangXe(String tenHangXe) {
        this.tenHangXe = tenHangXe;
    }

    @Override
    public String toString() {
        return tenHangXe;
    }
}
