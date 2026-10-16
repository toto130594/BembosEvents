SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- =====================================================
-- FASE 7.6D.1
-- Consistencia Usuario CLIENTE -> Cliente
-- =====================================================

INSERT INTO dbo.Cliente (
    idUsuario,
    documentoIdentidad,
    fechaRegistro,
    activo
)
SELECT
    u.idUsuario,
    NULL,
    CAST(u.fechaCreacion AS DATE),
    u.activo
FROM dbo.Usuario u
WHERE EXISTS (
    SELECT 1
    FROM dbo.UsuarioRol ur
    INNER JOIN dbo.Rol r
        ON r.idRol = ur.idRol
    WHERE ur.idUsuario = u.idUsuario
      AND UPPER(r.nombre) = N'CLIENTE'
)
AND NOT EXISTS (
    SELECT 1
    FROM dbo.Cliente c
    WHERE c.idUsuario = u.idUsuario
);
GO

-- Un Usuario puede tener como maximo un perfil Cliente.
IF EXISTS (
    SELECT idUsuario
    FROM dbo.Cliente
    WHERE idUsuario IS NOT NULL
    GROUP BY idUsuario
    HAVING COUNT(*) > 1
)
BEGIN
    THROW 50001,
        'Existen perfiles Cliente duplicados para un mismo Usuario',
        1;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.Cliente')
      AND name = N'UX_Cliente_idUsuario'
)
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX UX_Cliente_idUsuario
        ON dbo.Cliente(idUsuario)
        WHERE idUsuario IS NOT NULL;
END;
GO

-- Secuencia global para codigo publico de Reserva.
-- El formato BEM-YYYY-000001 se construira en Java.
IF OBJECT_ID(N'dbo.ReservaCodigoSequence', N'SO') IS NULL
BEGIN
    EXEC(N'
        CREATE SEQUENCE dbo.ReservaCodigoSequence
        AS BIGINT
        START WITH 1
        INCREMENT BY 1
        MINVALUE 1
        NO MAXVALUE
        NO CYCLE
        CACHE 50
    ');
END;
GO
