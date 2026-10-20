-- ============================================================
-- V9 - MFA login challenge token
-- ============================================================
--
-- Amplia TokenSeguridadUsuario.tipo para permitir
-- challenges MFA temporales durante autenticacion.
--
-- No agrega una nueva tabla.
-- No almacena el challenge raw.
-- TokenSeguridadUsuario conserva solamente tokenHash SHA-256.
-- ============================================================

IF OBJECT_ID(N'dbo.TokenSeguridadUsuario', N'U') IS NOT NULL
BEGIN

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE [parent_object_id] =
                OBJECT_ID(N'dbo.TokenSeguridadUsuario')
          AND [name] =
                N'CK_TokenSeguridadUsuario_tipo'
    )
    BEGIN
        ALTER TABLE [dbo].[TokenSeguridadUsuario]
            DROP CONSTRAINT
                [CK_TokenSeguridadUsuario_tipo];
    END;

    ALTER TABLE [dbo].[TokenSeguridadUsuario]
        WITH CHECK
        ADD CONSTRAINT [CK_TokenSeguridadUsuario_tipo]
        CHECK (
            [tipo] IN (
                N'VERIFICACION_CORREO',
                N'RECUPERACION_PASSWORD',
                N'MFA_LOGIN_CHALLENGE'
            )
        );

    ALTER TABLE [dbo].[TokenSeguridadUsuario]
        CHECK CONSTRAINT
            [CK_TokenSeguridadUsuario_tipo];

END;
GO