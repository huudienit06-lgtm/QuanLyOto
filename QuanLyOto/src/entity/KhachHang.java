package entity;

import java.util.Objects;

public class KhachHang {
	private String maKH;
	private String hoTen;
	private String soDT;
	private String email;
	private String diaChi;
	private Double congNo;

	public KhachHang() {
	}

	public KhachHang(String maKH) {
		this.maKH = maKH;
	}

	public KhachHang(String maKH, String hoTen, String soDT, String email, String diaChi, Double congNo) {
		this.maKH = maKH;
		this.hoTen = hoTen;
		this.soDT = soDT;
		this.email = email;
		this.diaChi = diaChi;
		this.congNo = congNo;
	}

	public KhachHang(String maKH, String hoTen, String soDT) {
		this(maKH, hoTen, soDT, null, null, null);
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

	@Override
	public String toString() {
		return maKH + " - " + hoTen;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof KhachHang other)) return false;
		return maKH != null && maKH.equals(other.maKH);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(maKH);
	}
}
