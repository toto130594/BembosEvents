USE [BembosEventsDB];
GO

-- =============================================
-- ENTIDAD 8 — PAQUETE
-- =============================================
IF OBJECT_ID(N'dbo.Paquete', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Paquete](
        [idPaquete] INT IDENTITY(1,1) NOT NULL,
        [nombre] NVARCHAR(100) NOT NULL,
        [descripcion] NVARCHAR(500),
        [precioBase] DECIMAL(12,2) NOT NULL,
        [cantidadPersonasBase] INT NOT NULL,
        [activo] BIT NOT NULL,
        CONSTRAINT [PK_Paquete] PRIMARY KEY CLUSTERED ([idPaquete])
    );
END;
GO

-- =============================================
-- ENTIDAD 9 — TEMATICA
-- =============================================
IF OBJECT_ID(N'dbo.Tematica', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Tematica](
        [idTematica] INT IDENTITY(1,1) NOT NULL,
        [nombre] NVARCHAR(100) NOT NULL,
        [descripcion] NVARCHAR(500),
        [precioAdicional] DECIMAL(12,2) NOT NULL,
        [activo] BIT NOT NULL,
        CONSTRAINT [PK_Tematica] PRIMARY KEY CLUSTERED ([idTematica])
    );
END;
GO

-- =============================================
-- ENTIDAD 10 — SERVICIO_ADICIONAL
-- =============================================
IF OBJECT_ID(N'dbo.ServicioAdicional', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[ServicioAdicional](
        [idServicioAdicional] INT IDENTITY(1,1) NOT NULL,
        [nombre] NVARCHAR(100) NOT NULL,
        [descripcion] NVARCHAR(500),
        [precio] DECIMAL(12,2) NOT NULL,
        [activo] BIT NOT NULL,
        CONSTRAINT [PK_ServicioAdicional] PRIMARY KEY CLUSTERED ([idServicioAdicional])
    );
END;
GO

-- =============================================
-- ENTIDAD 11 — RESERVA
-- =============================================
IF OBJECT_ID(N'dbo.Reserva', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Reserva](
        [idReserva] INT IDENTITY(1,1) NOT NULL,
        [codigoReserva] NVARCHAR(30) NOT NULL,
        [idCliente] INT NOT NULL,
        [idSede] INT NOT NULL,
        [idSalon] INT NOT NULL,
        [idPaquete] INT,
        [idTematica] INT,
        [fechaEvento] DATE NOT NULL,
        [horaInicio] TIME(0) NOT NULL,
        [horaFin] TIME(0) NOT NULL,
        [cantidadInvitados] INT NOT NULL,
        [montoBase] DECIMAL(12,2) NOT NULL,
        [montoServicios] DECIMAL(12,2) NOT NULL,
        [montoTotal] DECIMAL(12,2) NOT NULL,
        [porcentajeDeposito] DECIMAL(5,2) NOT NULL,
        [montoDeposito] DECIMAL(12,2) NOT NULL,
        [estado] NVARCHAR(30) NOT NULL,
        [fechaCreacion] DATETIME2(0) NOT NULL,
        [fechaActualizacion] DATETIME2(0) NOT NULL,
        CONSTRAINT [PK_Reserva] PRIMARY KEY CLUSTERED ([idReserva]),
        CONSTRAINT [UK_Reserva_codigoReserva] UNIQUE NONCLUSTERED ([codigoReserva]),
        CONSTRAINT [FK_Reserva_Cliente] FOREIGN KEY ([idCliente]) REFERENCES [dbo].[Cliente]([idCliente]),
        CONSTRAINT [FK_Reserva_Sede] FOREIGN KEY ([idSede]) REFERENCES [dbo].[Sede]([idSede]),
        CONSTRAINT [FK_Reserva_Salon] FOREIGN KEY ([idSalon]) REFERENCES [dbo].[Salon]([idSalon]),
        CONSTRAINT [FK_Reserva_Paquete] FOREIGN KEY ([idPaquete]) REFERENCES [dbo].[Paquete]([idPaquete]),
        CONSTRAINT [FK_Reserva_Tematica] FOREIGN KEY ([idTematica]) REFERENCES [dbo].[Tematica]([idTematica])
    );
END;
GO

-- =============================================
-- ENTIDAD 12 — RESERVA_SERVICIO
-- =============================================
IF OBJECT_ID(N'dbo.ReservaServicio', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[ReservaServicio](
        [idReservaServicio] INT IDENTITY(1,1) NOT NULL,
        [idReserva] INT NOT NULL,
        [idServicioAdicional] INT NOT NULL,
        [cantidad] INT NOT NULL,
        [precioUnitario] DECIMAL(12,2) NOT NULL,
        [subtotal] DECIMAL(12,2) NOT NULL,
        CONSTRAINT [CK_ReservaServicio_Subtotal] CHECK ([subtotal] = [cantidad] * [precioUnitario]),
        CONSTRAINT [PK_ReservaServicio] PRIMARY KEY CLUSTERED ([idReservaServicio]),
        CONSTRAINT [FK_ReservaServicio_Reserva] FOREIGN KEY ([idReserva]) REFERENCES [dbo].[Reserva]([idReserva]),
        CONSTRAINT [FK_ReservaServicio_ServicioAdicional] FOREIGN KEY ([idServicioAdicional]) REFERENCES [dbo].[ServicioAdicional]([idServicioAdicional])
    );
END;
GO

-- =============================================
-- ENTIDAD 13 — PAGO
-- =============================================
IF OBJECT_ID(N'dbo.Pago', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Pago](
        [idPago] INT IDENTITY(1,1) NOT NULL,
        [idReserva] INT NOT NULL,
        [monto] DECIMAL(12,2) NOT NULL,
        [metodoPago] NVARCHAR(50) NOT NULL,
        [estadoPago] NVARCHAR(30) NOT NULL,
        [referenciaExterna] NVARCHAR(150),
        [fechaPago] DATETIME2(0),
        [fechaCreacion] DATETIME2(0) NOT NULL,
        CONSTRAINT [CK_Pago_MontoPositivo] CHECK ([monto] > 0),
        CONSTRAINT [PK_Pago] PRIMARY KEY CLUSTERED ([idPago]),
        CONSTRAINT [FK_Pago_Reserva] FOREIGN KEY ([idReserva]) REFERENCES [dbo].[Reserva]([idReserva])
    );
END;
GO

-- =============================================
-- ENTIDAD 14 — HISTORIAL_RESERVA
-- =============================================
IF OBJECT_ID(N'dbo.HistorialReserva', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[HistorialReserva](
        [idHistorialReserva] INT IDENTITY(1,1) NOT NULL,
        [idReserva] INT NOT NULL,
        [estadoAnterior] NVARCHAR(30),
        [estadoNuevo] NVARCHAR(30) NOT NULL,
        [observacion] NVARCHAR(500),
        [fechaCambio] DATETIME2(0) NOT NULL,
        [idUsuarioResponsable] INT,
        CONSTRAINT [PK_HistorialReserva] PRIMARY KEY CLUSTERED ([idHistorialReserva]),
        CONSTRAINT [FK_HistorialReserva_Reserva] FOREIGN KEY ([idReserva]) REFERENCES [dbo].[Reserva]([idReserva]),
        CONSTRAINT [FK_HistorialReserva_Usuario] FOREIGN KEY ([idUsuarioResponsable]) REFERENCES [dbo].[Usuario]([idUsuario])
    );
END;
GO
