USE a1;
GO

IF COL_LENGTH('dbo.ChiTietPhieuSuaChua', 'tienCong') IS NOT NULL
BEGIN
    UPDATE dbo.ChiTietPhieuSuaChua
    SET donGia = donGia + tienCong
    WHERE tienCong <> 0;

    IF OBJECT_ID(N'dbo.CK_CTPSC_MotLoai', N'C') IS NOT NULL
        ALTER TABLE dbo.ChiTietPhieuSuaChua DROP CONSTRAINT CK_CTPSC_MotLoai;

    ALTER TABLE dbo.ChiTietPhieuSuaChua DROP COLUMN tienCong;

    ALTER TABLE dbo.ChiTietPhieuSuaChua
        ADD CONSTRAINT CK_CTPSC_MotLoai CHECK (
            (maPhuTung IS NOT NULL AND maDV IS NULL)
            OR (maPhuTung IS NULL AND maDV IS NOT NULL)
        );
END;
GO

IF OBJECT_ID(N'dbo.CK_CTHD_MotLoai', N'C') IS NOT NULL
    ALTER TABLE dbo.ChiTietHoaDon DROP CONSTRAINT CK_CTHD_MotLoai;
GO

IF OBJECT_ID(N'dbo.CK_CTHD_ItNhatMotLoai', N'C') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon
        ADD CONSTRAINT CK_CTHD_ItNhatMotLoai
        CHECK (maPhuTung IS NOT NULL OR maDV IS NOT NULL);
GO
