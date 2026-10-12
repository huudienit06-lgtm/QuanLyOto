package entity;

public class Xe {
    private String maXe;
    private String bienSo;
    private String hangXe;
    private String dongXe;
    private String maKhachHang; // Khóa ngoại liên kết với bảng Khách hàng

    // Constructor mặc định
    public Xe() {
    }

    // Constructor đầy đủ tham số
    public Xe(String maXe, String bienSo, String hangXe, String dongXe, String maKhachHang) {
        this.maXe = maXe;
        this.bienSo = bienSo;
        this.hangXe = hangXe;
        this.dongXe = dongXe;
        this.maKhachHang = maKhachHang;
    }

    // Getter và Setter
    public String getMaXe() {
        return maXe;
    }

    public void setMaXe(String maXe) {
        this.maXe = maXe;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        this.bienSo = bienSo;
    }

    public String getHangXe() {
        return hangXe;
    }

    public void setHangXe(String hangXe) {
        this.hangXe = hangXe;
    }

    public String getDongXe() {
        return dongXe;
    }

    public void setDongXe(String dongXe) {
        this.dongXe = dongXe;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    @Override
    public String toString() {
        return "Xe [maXe=" + maXe + ", bienSo=" + bienSo + ", hangXe=" + hangXe + ", dongXe=" + dongXe
                + ", maKhachHang=" + maKhachHang + "]";
    }
}