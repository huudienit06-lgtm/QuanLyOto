package entity;

public class Xe {
    private String maXe;
    private String bienSo;
    private String hangXe;
    private String dongXe;
    private Integer namSanXuat;
    private String soKhung;
    private String soMay;
    private String maKH;

    // Constructor mặc định
    public Xe() {
    }

    // Constructor đầy đủ tham số
    public Xe(String maXe, String bienSo, String hangXe, String dongXe, Integer namSanXuat,
            String soKhung, String soMay, String maKH) {
        this.maXe = maXe;
        this.bienSo = bienSo;
        this.hangXe = hangXe;
        this.dongXe = dongXe;
        this.namSanXuat = namSanXuat;
        this.soKhung = soKhung;
        this.soMay = soMay;
        this.maKH = maKH;
    }

    public Xe(String maXe, String bienSo, String hangXe, String dongXe, String maKH) {
        this(maXe, bienSo, hangXe, dongXe, null, null, null, maKH);
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

    public Integer getNamSanXuat() {
        return namSanXuat;
    }

    public void setNamSanXuat(Integer namSanXuat) {
        this.namSanXuat = namSanXuat;
    }

    public String getSoKhung() {
        return soKhung;
    }

    public void setSoKhung(String soKhung) {
        this.soKhung = soKhung;
    }

    public String getSoMay() {
        return soMay;
    }

    public void setSoMay(String soMay) {
        this.soMay = soMay;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getMaKhachHang() {
        return maKH;
    }

    public void setMaKhachHang(String maKH) {
        this.maKH = maKH;
    }

    @Override
    public String toString() {
        return "Xe [maXe=" + maXe + ", bienSo=" + bienSo + ", hangXe=" + hangXe + ", dongXe=" + dongXe
                + ", namSanXuat=" + namSanXuat + ", soKhung=" + soKhung + ", soMay=" + soMay
                + ", maKH=" + maKH + "]";
    }
}