-- ============================================================
-- V8 - SECURITY ADVANCED BASE
-- FASE 9.0.3
-- ============================================================

-- ============================================================
-- UsuarioSeguridad
-- ============================================================

IF OBJECT_ID(N'dbo.UsuarioSeguridad', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[UsuarioSeguridad] (
        [idUsuario] INT NOT NULL,
        [correoVerificado] BIT NOT NULL
            CONSTRAINT [DF_UsuarioSeguridad_correoVerificado]
            DEFAULT (0),
        [intentosFallidos] INT NOT NULL
            CONSTRAINT [DF_UsuarioSeguridad_intentosFallidos]
            DEFAULT (0),
        [bloqueadoHasta] DATETIME2(0) NULL,
        [mfaHabilitado] BIT NOT NULL
            CONSTRAINT [DF_UsuarioSeguridad_mfaHabilitado]
            DEFAULT (0),
        [totpSecretProtegido] NVARCHAR(1000) NULL,
        [fechaCreacion] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_UsuarioSeguridad_fechaCreacion]
            DEFAULT (SYSUTCDATETIME()),
        [fechaActualizacion] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_UsuarioSeguridad_fechaActualizacion]
            DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT [PK_UsuarioSeguridad]
            PRIMARY KEY CLUSTERED ([idUsuario]),

        CONSTRAINT [FK_UsuarioSeguridad_Usuario]
            FOREIGN KEY ([idUsuario])
            REFERENCES [dbo].[Usuario]([idUsuario]),

        CONSTRAINT [CK_UsuarioSeguridad_intentosFallidos]
            CHECK ([intentosFallidos] >= 0)
    );
END;
GO


-- Usuarios creados antes de FASE 9 se consideran verificados
-- para preservar compatibilidad con las cuentas existentes.

INSERT INTO [dbo].[UsuarioSeguridad] (
    [idUsuario],
    [correoVerificado],
    [intentosFallidos],
    [bloqueadoHasta],
    [mfaHabilitado],
    [totpSecretProtegido],
    [fechaCreacion],
    [fechaActualizacion]
)
SELECT
    u.[idUsuario],
    1,
    0,
    NULL,
    0,
    NULL,
    SYSUTCDATETIME(),
    SYSUTCDATETIME()
FROM [dbo].[Usuario] u
WHERE NOT EXISTS (
    SELECT 1
    FROM [dbo].[UsuarioSeguridad] us
    WHERE us.[idUsuario] = u.[idUsuario]
);
GO


-- ============================================================
-- TokenSeguridadUsuario
-- ============================================================

IF OBJECT_ID(N'dbo.TokenSeguridadUsuario', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[TokenSeguridadUsuario] (
        [idTokenSeguridad] INT IDENTITY(1,1) NOT NULL,
        [idUsuario] INT NOT NULL,
        [tipo] NVARCHAR(40) NOT NULL,
        [tokenHash] CHAR(64) NOT NULL,
        [fechaCreacion] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_TokenSeguridadUsuario_fechaCreacion]
            DEFAULT (SYSUTCDATETIME()),
        [fechaExpiracion] DATETIME2(0) NOT NULL,
        [fechaUso] DATETIME2(0) NULL,
        [fechaRevocacion] DATETIME2(0) NULL,

        CONSTRAINT [PK_TokenSeguridadUsuario]
            PRIMARY KEY CLUSTERED ([idTokenSeguridad]),

        CONSTRAINT [FK_TokenSeguridadUsuario_Usuario]
            FOREIGN KEY ([idUsuario])
            REFERENCES [dbo].[Usuario]([idUsuario]),

        CONSTRAINT [CK_TokenSeguridadUsuario_tipo]
            CHECK (
                [tipo] IN (
                    N'VERIFICACION_CORREO',
                    N'RECUPERACION_PASSWORD'
                )
            ),

        CONSTRAINT [CK_TokenSeguridadUsuario_expiracion]
            CHECK ([fechaExpiracion] > [fechaCreacion])
    );
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE
        [object_id] = OBJECT_ID(N'dbo.TokenSeguridadUsuario')
        AND [name] = N'UX_TokenSeguridadUsuario_tokenHash'
)
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX
        [UX_TokenSeguridadUsuario_tokenHash]
    ON [dbo].[TokenSeguridadUsuario]([tokenHash]);
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE
        [object_id] = OBJECT_ID(N'dbo.TokenSeguridadUsuario')
        AND [name] = N'IX_TokenSeguridadUsuario_usuario_tipo'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_TokenSeguridadUsuario_usuario_tipo]
    ON [dbo].[TokenSeguridadUsuario](
        [idUsuario],
        [tipo],
        [fechaExpiracion]
    );
END;
GO


-- ============================================================
-- SesionUsuario
-- ============================================================

IF OBJECT_ID(N'dbo.SesionUsuario', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[SesionUsuario] (
        [idSesionUsuario] BIGINT IDENTITY(1,1) NOT NULL,
        [idUsuario] INT NOT NULL,
        [refreshTokenHash] CHAR(64) NOT NULL,
        [fechaCreacion] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_SesionUsuario_fechaCreacion]
            DEFAULT (SYSUTCDATETIME()),
        [fechaExpiracion] DATETIME2(0) NOT NULL,
        [fechaUltimoUso] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_SesionUsuario_fechaUltimoUso]
            DEFAULT (SYSUTCDATETIME()),
        [fechaRevocacion] DATETIME2(0) NULL,
        [userAgent] NVARCHAR(500) NULL,
        [ipOrigen] NVARCHAR(64) NULL,

        CONSTRAINT [PK_SesionUsuario]
            PRIMARY KEY CLUSTERED ([idSesionUsuario]),

        CONSTRAINT [FK_SesionUsuario_Usuario]
            FOREIGN KEY ([idUsuario])
            REFERENCES [dbo].[Usuario]([idUsuario]),

        CONSTRAINT [CK_SesionUsuario_expiracion]
            CHECK ([fechaExpiracion] > [fechaCreacion])
    );
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE
        [object_id] = OBJECT_ID(N'dbo.SesionUsuario')
        AND [name] = N'UX_SesionUsuario_refreshTokenHash'
)
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX
        [UX_SesionUsuario_refreshTokenHash]
    ON [dbo].[SesionUsuario]([refreshTokenHash]);
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE
        [object_id] = OBJECT_ID(N'dbo.SesionUsuario')
        AND [name] = N'IX_SesionUsuario_usuario_revocacion'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_SesionUsuario_usuario_revocacion]
    ON [dbo].[SesionUsuario](
        [idUsuario],
        [fechaRevocacion],
        [fechaExpiracion]
    );
END;
GO


-- ============================================================
-- MfaRecoveryCode
-- ============================================================

IF OBJECT_ID(N'dbo.MfaRecoveryCode', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[MfaRecoveryCode] (
        [idRecoveryCode] BIGINT IDENTITY(1,1) NOT NULL,
        [idUsuario] INT NOT NULL,
        [codigoHash] CHAR(64) NOT NULL,
        [fechaCreacion] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_MfaRecoveryCode_fechaCreacion]
            DEFAULT (SYSUTCDATETIME()),
        [fechaUso] DATETIME2(0) NULL,

        CONSTRAINT [PK_MfaRecoveryCode]
            PRIMARY KEY CLUSTERED ([idRecoveryCode]),

        CONSTRAINT [FK_MfaRecoveryCode_Usuario]
            FOREIGN KEY ([idUsuario])
            REFERENCES [dbo].[Usuario]([idUsuario])
    );
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE
        [object_id] = OBJECT_ID(N'dbo.MfaRecoveryCode')
        AND [name] = N'UX_MfaRecoveryCode_codigoHash'
)
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX
        [UX_MfaRecoveryCode_codigoHash]
    ON [dbo].[MfaRecoveryCode]([codigoHash]);
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE
        [object_id] = OBJECT_ID(N'dbo.MfaRecoveryCode')
        AND [name] = N'IX_MfaRecoveryCode_usuario_uso'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_MfaRecoveryCode_usuario_uso]
    ON [dbo].[MfaRecoveryCode](
        [idUsuario],
        [fechaUso]
    );
END;
GO


-- ============================================================
-- AuditoriaSeguridad
-- ============================================================

IF OBJECT_ID(N'dbo.AuditoriaSeguridad', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[AuditoriaSeguridad] (
        [idAuditoriaSeguridad] BIGINT IDENTITY(1,1) NOT NULL,
        [idUsuario] INT NULL,
        [tipoEvento] NVARCHAR(80) NOT NULL,
        [exitoso] BIT NOT NULL,
        [fechaEvento] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_AuditoriaSeguridad_fechaEvento]
            DEFAULT (SYSUTCDATETIME()),
        [ipOrigen] NVARCHAR(64) NULL,
        [userAgent] NVARCHAR(500) NULL,
        [detalleSeguro] NVARCHAR(1000) NULL,

        CONSTRAINT [PK_AuditoriaSeguridad]
            PRIMARY KEY CLUSTERED ([idAuditoriaSeguridad]),

        CONSTRAINT [FK_AuditoriaSeguridad_Usuario]
            FOREIGN KEY ([idUsuario])
            REFERENCES [dbo].[Usuario]([idUsuario])
    );
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE
        [object_id] = OBJECT_ID(N'dbo.AuditoriaSeguridad')
        AND [name] = N'IX_AuditoriaSeguridad_usuario_fecha'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_AuditoriaSeguridad_usuario_fecha]
    ON [dbo].[AuditoriaSeguridad](
        [idUsuario],
        [fechaEvento]
    );
END;
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE
        [object_id] = OBJECT_ID(N'dbo.AuditoriaSeguridad')
        AND [name] = N'IX_AuditoriaSeguridad_tipo_fecha'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_AuditoriaSeguridad_tipo_fecha]
    ON [dbo].[AuditoriaSeguridad](
        [tipoEvento],
        [fechaEvento]
    );
END;
GO
