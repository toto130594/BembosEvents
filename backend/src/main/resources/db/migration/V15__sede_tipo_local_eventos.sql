SET NOCOUNT ON;
SET XACT_ABORT ON;

IF COL_LENGTH(N'dbo.Sede', N'tipoLocal') IS NULL
BEGIN
    EXEC(N'
        ALTER TABLE dbo.Sede
        ADD tipoLocal NVARCHAR(30) NOT NULL
            CONSTRAINT DF_Sede_tipoLocal
            DEFAULT N''RESTAURANTE'';
    ');
END;

IF COL_LENGTH(N'dbo.Sede', N'permiteEventos') IS NULL
BEGIN
    EXEC(N'
        ALTER TABLE dbo.Sede
        ADD permiteEventos BIT NOT NULL
            CONSTRAINT DF_Sede_permiteEventos
            DEFAULT 1;
    ');
END;

-- SQL dinamico porque SQL Server compila el batch completo antes
-- de que las columnas agregadas arriba sean visibles al UPDATE.
EXEC(N'
    UPDATE dbo.Sede
    SET
        tipoLocal =
            CASE
                WHEN nombre IN (
                    N''UTP Lima Centro'',
                    N''U Lima'',
                    N''PUCP Express'',
                    N''ESAN Monterrico''
                )
                    THEN N''UNIVERSIDAD''

                WHEN nombre LIKE N''%Dark Kitchen%''
                  OR nombre LIKE N''DK %''
                    THEN N''DARK_KITCHEN''

                WHEN nombre LIKE N''% FC''
                    THEN N''FOOD_COURT''

                WHEN nombre LIKE N''%Express%''
                    THEN N''EXPRESS''

                ELSE N''RESTAURANTE''
            END,

        permiteEventos =
            CASE
                WHEN nombre IN (
                    N''UTP Lima Centro'',
                    N''U Lima'',
                    N''PUCP Express'',
                    N''ESAN Monterrico''
                )
                    THEN 0

                WHEN nombre LIKE N''%Dark Kitchen%''
                  OR nombre LIKE N''DK %''
                  OR nombre LIKE N''% FC''
                  OR nombre LIKE N''%Express%''
                    THEN 0

                ELSE 1
            END
    WHERE activo = 1;
');

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'CK_Sede_tipoLocal'
      AND parent_object_id =
            OBJECT_ID(N'dbo.Sede')
)
BEGIN
    EXEC(N'
        ALTER TABLE dbo.Sede
        ADD CONSTRAINT CK_Sede_tipoLocal
        CHECK (
            tipoLocal IN (
                N''RESTAURANTE'',
                N''FOOD_COURT'',
                N''EXPRESS'',
                N''DARK_KITCHEN'',
                N''UNIVERSIDAD''
            )
        );
    ');
END;