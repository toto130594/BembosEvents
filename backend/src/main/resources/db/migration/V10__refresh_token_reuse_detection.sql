-- ============================================================
-- V10 - Refresh token reuse detection
-- Stores only SHA-256 hashes of already rotated refresh tokens.
-- Raw refresh tokens are never persisted.
-- ============================================================

IF OBJECT_ID(N'dbo.RefreshTokenHistory', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[RefreshTokenHistory] (
        [idRefreshTokenHistory] BIGINT IDENTITY(1,1) NOT NULL,
        [idSesionUsuario] BIGINT NOT NULL,
        [refreshTokenHash] CHAR(64) NOT NULL,
        [fechaRotacion] DATETIME2(7) NOT NULL,
        [fechaExpiracion] DATETIME2(0) NOT NULL,

        CONSTRAINT [PK_RefreshTokenHistory]
            PRIMARY KEY CLUSTERED (
                [idRefreshTokenHistory]
            ),

        CONSTRAINT [FK_RefreshTokenHistory_SesionUsuario]
            FOREIGN KEY (
                [idSesionUsuario]
            )
            REFERENCES [dbo].[SesionUsuario](
                [idSesionUsuario]
            )
            ON DELETE CASCADE,

        CONSTRAINT [CK_RefreshTokenHistory_expiracion]
            CHECK (
                [fechaExpiracion] > [fechaRotacion]
            )
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE [object_id] =
        OBJECT_ID(N'dbo.RefreshTokenHistory')
      AND [name] =
        N'UX_RefreshTokenHistory_refreshTokenHash'
)
BEGIN
    CREATE UNIQUE INDEX
        [UX_RefreshTokenHistory_refreshTokenHash]
    ON [dbo].[RefreshTokenHistory](
        [refreshTokenHash]
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE [object_id] =
        OBJECT_ID(N'dbo.RefreshTokenHistory')
      AND [name] =
        N'IX_RefreshTokenHistory_sesion'
)
BEGIN
    CREATE INDEX
        [IX_RefreshTokenHistory_sesion]
    ON [dbo].[RefreshTokenHistory](
        [idSesionUsuario],
        [fechaExpiracion]
    );
END;