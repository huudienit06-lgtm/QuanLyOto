package entity;

public class PhuTung {
    
    // TÊN CÁC BIẾN NÀY PHẢI KHỚP 100% VỚI TÊN CỘT TRONG SQL SERVER
    private String maPhuTung;
    private String tenPhuTung;
    private String donViTinh;
    private double giaNhap;
    private double giaBan;
    private int soLuongTon;
    private String nhaCungCap;
    private String trangThai;
    // Constructor rỗng
    public PhuTung() {
    }

    // Constructor đầy đủ tham số để lấy dữ liệu từ Form
    public PhuTung(String maPhuTung, String tenPhuTung, String donViTinh, double giaNhap, double giaBan, int soLuongTon, String nhaCungCap, String trangThai) {
        this.maPhuTung = maPhuTung;
        this.tenPhuTung = tenPhuTung;
        this.donViTinh = donViTinh;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        this.soLuongTon = soLuongTon;
        this.nhaCungCap = nhaCungCap;
        this.trangThai = trangThai;
    }
    // Getter và Setter cho tất cả các thuộc tính
    public String getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(String maPhuTung) { this.maPhuTung = maPhuTung; }

    public String getTenPhuTung() { return tenPhuTung; }
    public void setTenPhuTung(String tenPhuTung) { this.tenPhuTung = tenPhuTung; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public String getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}