-- Chay mot lan tren database a1 cu de chuyen chucVu text thanh khoa ngoai maChucVu.
-- Migration giu nguyen nhan vien va tao danh muc ChucVu tu cac gia tri dang co.
USE a1;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.NhanVien', N'chucVu') IS NULL
    BEGIN
        IF COL_LENGTH(N'dbo.NhanVien', N'maChucVu') IS NOT NULL
            AND OBJECT_ID(N'dbo.ChucVu', N'U') IS NOT NULL
        BEGIN
            COMMIT TRANSACTION;
            RETURN;
        END;

        BEGIN
            ;THROW 50010, N'Khong tim thay cot chucVu cu trong dbo.NhanVien.', 1;
        END;
    END;

    IF OBJECT_ID(N'dbo.ChucVu', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.ChucVu (
            maChucVu VARCHAR(20) NOT NULL PRIMARY KEY,
            tenChucVu NVARCHAR(50) NOT NULL UNIQUE
        );
    END;

    DECLARE @maCuoi INT = ISNULL((
        SELECT MAX(TRY_CONVERT(INT, SUBSTRING(maChucVu, 3, 8)))
        FROM dbo.ChucVu
        WHERE maChucVu LIKE 'CV[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'
    ), 0);

    ;WITH ChucVuCanThem AS (
        SELECT DISTINCT LTRIM(RTRIM(nv.chucVu)) AS tenChucVu
        FROM dbo.NhanVien nv
        WHERE NULLIF(LTRIM(RTRIM(nv.chucVu)), N'') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1 FROM dbo.ChucVu cv
                WHERE cv.tenChucVu = LTRIM(RTRIM(nv.chucVu))
            )
    ), ChucVuDanhSo AS (
        SELECT tenChucVu, ROW_NUMBER() OVER (ORDER BY tenChucVu) AS thuTu
        FROM ChucVuCanThem
    )
    INSERT INTO dbo.ChucVu (maChucVu, tenChucVu)
    SELECT 'CV' + RIGHT('00000000' + CONVERT(VARCHAR(8), @maCuoi + thuTu), 8), tenChucVu
    FROM ChucVuDanhSo;

    ALTER TABLE dbo.NhanVien ADD maChucVu VARCHAR(20) NULL;

    UPDATE nv
    SET nv.maChucVu = cv.maChucVu
    FROM dbo.NhanVien nv
    JOIN dbo.ChucVu cv ON cv.tenChucVu = LTRIM(RTRIM(nv.chucVu));

    IF EXISTS (SELECT 1 FROM dbo.NhanVien WHERE maChucVu IS NULL)
    BEGIN
        ;THROW 50011, N'Co nhan vien chua duoc gan chuc vu; migration da duoc rollback.', 1;
    END;

    ALTER TABLE dbo.NhanVien ALTER COLUMN maChucVu VARCHAR(20) NOT NULL;
    ALTER TABLE dbo.NhanVien ADD CONSTRAINT FK_NhanVien_ChucVu
        FOREIGN KEY (maChucVu) REFERENCES dbo.ChucVu(maChucVu);
    ALTER TABLE dbo.NhanVien DROP COLUMN chucVu;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO