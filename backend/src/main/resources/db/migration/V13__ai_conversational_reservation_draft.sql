IF OBJECT_ID(N'[dbo].[BorradorReservaConversacional]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[BorradorReservaConversacional](
        [idBorrador] INT IDENTITY(1,1) NOT NULL,
        [idConversacion] INT NOT NULL,
        [idCliente] INT NOT NULL,
        [idSede] INT NULL,
        [idSalon] INT NULL,
        [idPaquete] INT NULL,
        [idTematica] INT NULL,
        [fechaEvento] DATE NULL,
        [horaInicio] TIME(0) NULL,
        [horaFin] TIME(0) NULL,
        [cantidadInvitados] INT NULL,
        [estado] NVARCHAR(20) NOT NULL
            CONSTRAINT [DF_BorradorReservaConversacional_estado]
            DEFAULT N'ABIERTO',
        [idReservaConfirmada] INT NULL,
        [fechaCreacion] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_BorradorReservaConversacional_fechaCreacion]
            DEFAULT SYSUTCDATETIME(),
        [fechaActualizacion] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_BorradorReservaConversacional_fechaActualizacion]
            DEFAULT SYSUTCDATETIME(),
        [fechaConfirmacion] DATETIME2(0) NULL,

        CONSTRAINT [PK_BorradorReservaConversacional]
            PRIMARY KEY CLUSTERED ([idBorrador]),

        CONSTRAINT [UQ_BorradorReservaConversacional_Conversacion]
            UNIQUE ([idConversacion]),

        CONSTRAINT [FK_BorradorReservaConversacional_Conversacion]
            FOREIGN KEY ([idConversacion])
            REFERENCES [dbo].[Conversacion]([idConversacion]),

        CONSTRAINT [FK_BorradorReservaConversacional_Cliente]
            FOREIGN KEY ([idCliente])
            REFERENCES [dbo].[Cliente]([idCliente]),

        CONSTRAINT [FK_BorradorReservaConversacional_Sede]
            FOREIGN KEY ([idSede])
            REFERENCES [dbo].[Sede]([idSede]),

        CONSTRAINT [FK_BorradorReservaConversacional_Salon]
            FOREIGN KEY ([idSalon])
            REFERENCES [dbo].[Salon]([idSalon]),

        CONSTRAINT [FK_BorradorReservaConversacional_Paquete]
            FOREIGN KEY ([idPaquete])
            REFERENCES [dbo].[Paquete]([idPaquete]),

        CONSTRAINT [FK_BorradorReservaConversacional_Tematica]
            FOREIGN KEY ([idTematica])
            REFERENCES [dbo].[Tematica]([idTematica]),

        CONSTRAINT [FK_BorradorReservaConversacional_Reserva]
            FOREIGN KEY ([idReservaConfirmada])
            REFERENCES [dbo].[Reserva]([idReserva]),

        CONSTRAINT [CK_BorradorReservaConversacional_estado]
            CHECK ([estado] IN (N'ABIERTO', N'CONFIRMADO')),

        CONSTRAINT [CK_BorradorReservaConversacional_cantidad]
            CHECK ([cantidadInvitados] IS NULL OR [cantidadInvitados] > 0),

        CONSTRAINT [CK_BorradorReservaConversacional_horario]
            CHECK (
                [horaInicio] IS NULL
                OR [horaFin] IS NULL
                OR [horaInicio] < [horaFin]
            )
    );
END;
GO

IF OBJECT_ID(N'[dbo].[BorradorReservaServicio]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[BorradorReservaServicio](
        [idBorradorReservaServicio] INT IDENTITY(1,1) NOT NULL,
        [idBorrador] INT NOT NULL,
        [idServicioAdicional] INT NOT NULL,
        [cantidad] INT NOT NULL,

        CONSTRAINT [PK_BorradorReservaServicio]
            PRIMARY KEY CLUSTERED ([idBorradorReservaServicio]),

        CONSTRAINT [FK_BorradorReservaServicio_Borrador]
            FOREIGN KEY ([idBorrador])
            REFERENCES [dbo].[BorradorReservaConversacional]([idBorrador])
            ON DELETE CASCADE,

        CONSTRAINT [FK_BorradorReservaServicio_Servicio]
            FOREIGN KEY ([idServicioAdicional])
            REFERENCES [dbo].[ServicioAdicional]([idServicioAdicional]),

        CONSTRAINT [UQ_BorradorReservaServicio_BorradorServicio]
            UNIQUE ([idBorrador], [idServicioAdicional]),

        CONSTRAINT [CK_BorradorReservaServicio_cantidad]
            CHECK ([cantidad] > 0)
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_BorradorReservaConversacional_ClienteEstado'
      AND object_id = OBJECT_ID(N'[dbo].[BorradorReservaConversacional]')
)
BEGIN
    CREATE INDEX [IX_BorradorReservaConversacional_ClienteEstado]
        ON [dbo].[BorradorReservaConversacional]
        ([idCliente], [estado], [fechaActualizacion] DESC);
END;
GO