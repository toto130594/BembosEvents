IF OBJECT_ID(N'[dbo].[Conversacion]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Conversacion](
        [idConversacion] INT IDENTITY(1,1) NOT NULL,
        [idCliente] INT NOT NULL,
        [idSede] INT NULL,
        [estado] NVARCHAR(30) NOT NULL,
        [fechaCreacion] DATETIME2 NOT NULL
            CONSTRAINT [DF_Conversacion_fechaCreacion]
            DEFAULT SYSUTCDATETIME(),
        [fechaActualizacion] DATETIME2 NOT NULL
            CONSTRAINT [DF_Conversacion_fechaActualizacion]
            DEFAULT SYSUTCDATETIME(),
        [fechaCierre] DATETIME2 NULL,

        CONSTRAINT [PK_Conversacion]
            PRIMARY KEY CLUSTERED ([idConversacion]),

        CONSTRAINT [FK_Conversacion_Cliente]
            FOREIGN KEY ([idCliente])
            REFERENCES [dbo].[Cliente]([idCliente]),

        CONSTRAINT [FK_Conversacion_Sede]
            FOREIGN KEY ([idSede])
            REFERENCES [dbo].[Sede]([idSede]),

        CONSTRAINT [CK_Conversacion_estado]
            CHECK ([estado] IN (
                N'BOT',
                N'ESPERANDO_ENCARGADO',
                N'EN_ATENCION',
                N'CERRADO'
            ))
    );
END;
GO

IF OBJECT_ID(N'[dbo].[MensajeChat]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[MensajeChat](
        [idMensajeChat] INT IDENTITY(1,1) NOT NULL,
        [idConversacion] INT NOT NULL,
        [tipoEmisor] NVARCHAR(30) NOT NULL,
        [idUsuarioEmisor] INT NULL,
        [contenido] NVARCHAR(4000) NOT NULL,
        [fechaEnvio] DATETIME2 NOT NULL
            CONSTRAINT [DF_MensajeChat_fechaEnvio]
            DEFAULT SYSUTCDATETIME(),

        CONSTRAINT [PK_MensajeChat]
            PRIMARY KEY CLUSTERED ([idMensajeChat]),

        CONSTRAINT [FK_MensajeChat_Conversacion]
            FOREIGN KEY ([idConversacion])
            REFERENCES [dbo].[Conversacion]([idConversacion]),

        CONSTRAINT [FK_MensajeChat_Usuario]
            FOREIGN KEY ([idUsuarioEmisor])
            REFERENCES [dbo].[Usuario]([idUsuario]),

        CONSTRAINT [CK_MensajeChat_tipoEmisor]
            CHECK ([tipoEmisor] IN (
                N'CLIENTE',
                N'IA',
                N'ENCARGADO_LOCAL',
                N'SISTEMA'
            )),

        CONSTRAINT [CK_MensajeChat_contenido]
            CHECK (LEN(LTRIM(RTRIM([contenido]))) > 0)
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.Conversacion')
      AND name = N'IX_Conversacion_idCliente_fechaActualizacion'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_Conversacion_idCliente_fechaActualizacion]
    ON [dbo].[Conversacion] (
        [idCliente],
        [fechaActualizacion] DESC
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.Conversacion')
      AND name = N'IX_Conversacion_idSede_estado_fechaActualizacion'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_Conversacion_idSede_estado_fechaActualizacion]
    ON [dbo].[Conversacion] (
        [idSede],
        [estado],
        [fechaActualizacion]
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.MensajeChat')
      AND name = N'IX_MensajeChat_idConversacion_fechaEnvio'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_MensajeChat_idConversacion_fechaEnvio]
    ON [dbo].[MensajeChat] (
        [idConversacion],
        [fechaEnvio],
        [idMensajeChat]
    );
END;
GO