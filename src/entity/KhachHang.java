package entity;

public class KhachHang {
	private String maKH;
	private String hoTen;
	private String soDT;
	private String email;
	private String diaChi;
	private Double congNo;

	public KhachHang() {
	}

	public String getMaKH() { return maKH; }
	public void setMaKH(String maKH) { this.maKH = maKH; }
	public String getHoTen() { return hoTen; }
	public void setHoTen(String hoTen) { this.hoTen = hoTen; }
	public String getSoDT() { return soDT; }
	public void setSoDT(String soDT) { this.soDT = soDT; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getDiaChi() { return diaChi; }
	public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
	public Double getCongNo() { return congNo; }
	public void setCongNo(Double congNo) { this.congNo = congNo; }
}
