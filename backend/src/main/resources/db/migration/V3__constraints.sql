USE [BembosEventsDB];
GO

SET ANSI_NULLS ON;
GO
SET ANSI_PADDING ON;
GO
SET ANSI_WARNINGS ON;
GO
SET ARITHABORT ON;
GO
SET CONCAT_NULL_YIELDS_NULL ON;
GO
SET QUOTED_IDENTIFIER ON;
GO
SET NUMERIC_ROUNDABORT OFF;
GO

-- =============================================
-- CONSTRAINT: UX_Cliente_idUsuario
-- =============================================
-- Un Usuario puede tener como máximo un perfil Cliente.
-- idUsuario NULL es permitido (múltiples clientes sin usuario).
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_Cliente_idUsuario'
      AND object_id = OBJECT_ID(N'dbo.Cliente')
)
BEGIN
    CREATE UNIQUE INDEX [UX_Cliente_idUsuario]
    ON [dbo].[Cliente]([idUsuario])
    WHERE [idUsuario] IS NOT NULL;
END;
GO

-- =============================================
-- CONSTRAINT: CK_Salon_CapacidadPositiva
-- =============================================
IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'CK_Salon_CapacidadPositiva'
      AND parent_object_id = OBJECT_ID(N'dbo.Salon')
)
BEGIN
    ALTER TABLE [dbo].[Salon]
    ADD CONSTRAINT [CK_Salon_CapacidadPositiva]
    CHECK ([capacidad] > 0);
END;
GO

-- =============================================
-- CONSTRAINT: CK_HorarioSalon_HorasValidas
-- =============================================
IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'CK_HorarioSalon_HorasValidas'
      AND parent_object_id = OBJECT_ID(N'dbo.HorarioSalon')
)
BEGIN
    ALTER TABLE [dbo].[HorarioSalon]
    ADD CONSTRAINT [CK_HorarioSalon_HorasValidas]
    CHECK ([horaInicio] < [horaFin]);
END;
GO

-- =============================================
-- CONSTRAINT: UX_Usuario_correo
-- =============================================
-- Un usuario puede tener como máximo un correo electrónico.
-- Restricción UNIQUE idempotente para evitar duplicados de correo.
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_Usuario_correo'
      AND object_id = OBJECT_ID(N'dbo.Usuario')
)
BEGIN
    CREATE UNIQUE INDEX [UX_Usuario_correo]
    ON [dbo].[Usuario]([correo]);
END;
GO

-- =============================================
-- CONSTRAINT: UX_UsuarioRol_idUsuario_idRol
-- =============================================
-- Un usuario puede tener como máximo un rol por tipo de rol.
-- Impide que un mismo usuario tenga asignado dos veces el mismo rol.
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_UsuarioRol_idUsuario_idRol'
      AND object_id = OBJECT_ID(N'dbo.UsuarioRol')
)
BEGIN
    CREATE UNIQUE INDEX [UX_UsuarioRol_idUsuario_idRol]
    ON [dbo].[UsuarioRol]([idUsuario], [idRol]);
END;
GO

-- =============================================
-- CONSTRAINT: UX_Rol_nombre
-- =============================================
-- Un rol debe tener un nombre único en todo el sistema.
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_Rol_nombre'
      AND object_id = OBJECT_ID(N'dbo.Rol')
)
BEGIN
    CREATE UNIQUE INDEX [UX_Rol_nombre]
    ON [dbo].[Rol]([nombre]);
END;
GO
