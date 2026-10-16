IF OBJECT_ID(N'[dbo].[UsuarioSede]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[UsuarioSede](
        [idUsuarioSede] INT IDENTITY(1,1) NOT NULL,
        [idUsuario] INT NOT NULL,
        [idSede] INT NOT NULL,
        [fechaAsignacion] DATETIME2 NOT NULL,
        [activo] BIT NOT NULL,

        CONSTRAINT [PK_UsuarioSede]
            PRIMARY KEY CLUSTERED ([idUsuarioSede]),

        CONSTRAINT [FK_UsuarioSede_Usuario]
            FOREIGN KEY ([idUsuario])
            REFERENCES [dbo].[Usuario]([idUsuario]),

        CONSTRAINT [FK_UsuarioSede_Sede]
            FOREIGN KEY ([idSede])
            REFERENCES [dbo].[Sede]([idSede]),

        CONSTRAINT [UX_UsuarioSede_idUsuario]
            UNIQUE ([idUsuario])
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_UsuarioSede_idSede'
      AND object_id = OBJECT_ID(N'[dbo].[UsuarioSede]')
)
BEGIN
    CREATE NONCLUSTERED INDEX [IX_UsuarioSede_idSede]
        ON [dbo].[UsuarioSede] ([idSede]);
END;
GO