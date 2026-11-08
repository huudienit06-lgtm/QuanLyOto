-- Chay mot lan tren database a1 cu de them tienDo cho phieu tiep nhan.
USE a1;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.PhieuTiepNhan', N'tienDo') IS NULL
    BEGIN
        ALTER TABLE dbo.PhieuTiepNhan
        ADD tienDo NVARCHAR(40) NOT NULL
            CONSTRAINT DF_PTN_tienDo DEFAULT N'Đã tiếp nhận' WITH VALUES;
    END;

    UPDATE ptn
    SET tienDo = CASE
        WHEN EXISTS (
            SELECT 1
            FROM dbo.PhieuKiemTra pkt
            JOIN dbo.PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra
            JOIN dbo.HoaDon hd ON hd.maPhieuSuaChua = psc.maPhieuSuaChua
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
                AND hd.trangThai = N'Đã thanh toán'
        ) THEN N'Hoàn thành'
        WHEN EXISTS (
            SELECT 1
            FROM dbo.PhieuKiemTra pkt
            JOIN dbo.PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra
            JOIN dbo.HoaDon hd ON hd.maPhieuSuaChua = psc.maPhieuSuaChua
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
        ) THEN N'Đã lập hóa đơn'
        WHEN EXISTS (
            SELECT 1
            FROM dbo.PhieuKiemTra pkt
            JOIN dbo.PhieuSuaChua psc ON psc.maPhieuKiemTra = pkt.maPhieuKiemTra
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
        ) THEN N'Đã lập phiếu sửa chữa'
        WHEN EXISTS (
            SELECT 1 FROM dbo.PhieuKiemTra pkt
            WHERE pkt.maPhieuTiepNhan = ptn.maPhieuTiepNhan
        ) THEN N'Đã lập phiếu kiểm tra'
        ELSE N'Đã tiếp nhận'
    END
    FROM dbo.PhieuTiepNhan ptn;

    IF NOT EXISTS (
        SELECT 1 FROM sys.check_constraints WHERE name = N'CK_PTN_tienDo'
    )
    BEGIN
        ALTER TABLE dbo.PhieuTiepNhan WITH CHECK
        ADD CONSTRAINT CK_PTN_tienDo CHECK (tienDo IN (
            N'Đã tiếp nhận', N'Đã lập phiếu kiểm tra', N'Đã lập phiếu sửa chữa',
            N'Đã lập hóa đơn', N'Hoàn thành'
        ));
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO