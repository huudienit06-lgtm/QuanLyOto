-- Chay sau Migration_TienDoPhieuTiepNhan.sql tren database a1 cu.
USE a1;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.PhieuTiepNhan', N'tienDo') IS NULL
    BEGIN
        ;THROW 50020, N'Can chay Migration_TienDoPhieuTiepNhan.sql truoc.', 1;
    END;

    IF COL_LENGTH(N'dbo.PhieuKiemTra', N'trangThai') IS NULL
    BEGIN
        ALTER TABLE dbo.PhieuKiemTra
        ADD trangThai NVARCHAR(20) NOT NULL
            CONSTRAINT DF_PKT_trangThai DEFAULT N'Chờ kiểm tra' WITH VALUES;
    END;

    UPDATE pkt
    SET trangThai = CASE
        WHEN EXISTS (
            SELECT 1 FROM dbo.PhieuSuaChua psc
            WHERE psc.maPhieuKiemTra = pkt.maPhieuKiemTra
        ) THEN N'Đã kiểm tra'
        WHEN pkt.trangThai IN (N'Chờ kiểm tra', N'Đã kiểm tra') THEN pkt.trangThai
        ELSE N'Chờ kiểm tra'
    END
    FROM dbo.PhieuKiemTra pkt;

    UPDATE dbo.PhieuSuaChua
    SET trangThai = CASE
        WHEN trangThai IN (N'Đã sửa chữa', N'Hoàn thành') THEN N'Đã sửa chữa'
        ELSE N'Chờ sửa chữa'
    END;

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_PKT_trangThai')
    BEGIN
        ALTER TABLE dbo.PhieuKiemTra WITH CHECK
        ADD CONSTRAINT CK_PKT_trangThai
        CHECK (trangThai IN (N'Chờ kiểm tra', N'Đã kiểm tra'));
    END;

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_PSC_trangThai')
    BEGIN
        ALTER TABLE dbo.PhieuSuaChua WITH CHECK
        ADD CONSTRAINT CK_PSC_trangThai
        CHECK (trangThai IN (N'Chờ sửa chữa', N'Đã sửa chữa'));
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO