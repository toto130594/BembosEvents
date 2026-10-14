USE [BembosEventsDB];
GO

-- =============================================
-- TABLA: Rol
-- =============================================
IF OBJECT_ID(N'dbo.Rol', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Rol](
        [idRol] INT IDENTITY(1,1) NOT NULL,
        [nombre] NVARCHAR(100) NOT NULL,
        [descripcion] NVARCHAR(500),
        [activo] BIT NOT NULL,
        CONSTRAINT [PK_Rol] PRIMARY KEY CLUSTERED ([idRol])
    );
END;
GO

-- =============================================
-- TABLA: Usuario
-- =============================================
IF OBJECT_ID(N'dbo.Usuario', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Usuario](
        [idUsuario] INT IDENTITY(1,1) NOT NULL,
        [nombres] NVARCHAR(100) NOT NULL,
        [apellidos] NVARCHAR(100) NOT NULL,
        [correo] NVARCHAR(254) NOT NULL,
        [telefono] NVARCHAR(20),
        [passwordHash] NVARCHAR(255) NOT NULL,
        [activo] BIT NOT NULL,
        [fechaCreacion] DATETIME2(0) NOT NULL,
        [fechaActualizacion] DATETIME2(0) NOT NULL,
        CONSTRAINT [PK_Usuario] PRIMARY KEY CLUSTERED ([idUsuario])
    );
END;
GO

-- =============================================
-- TABLA: UsuarioRol
-- =============================================
IF OBJECT_ID(N'dbo.UsuarioRol', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[UsuarioRol](
        [idUsuarioRol] INT IDENTITY(1,1) NOT NULL,
        [idUsuario] INT NOT NULL,
        [idRol] INT NOT NULL,
        [fechaAsignacion] DATETIME2(0) NOT NULL,
        CONSTRAINT [PK_UsuarioRol] PRIMARY KEY CLUSTERED ([idUsuarioRol]),
        CONSTRAINT [FK_UsuarioRol_Usuario] FOREIGN KEY ([idUsuario]) REFERENCES [dbo].[Usuario]([idUsuario]),
        CONSTRAINT [FK_UsuarioRol_Rol] FOREIGN KEY ([idRol]) REFERENCES [dbo].[Rol]([idRol])
    );
END;
GO

-- =============================================
-- TABLA: Cliente
-- =============================================
IF OBJECT_ID(N'dbo.Cliente', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Cliente](
        [idCliente] INT IDENTITY(1,1) NOT NULL,
        [idUsuario] INT,
        [documentoIdentidad] NVARCHAR(20),
        [fechaRegistro] DATE NOT NULL,
        [activo] BIT NOT NULL,
        CONSTRAINT [PK_Cliente] PRIMARY KEY CLUSTERED ([idCliente]),
        CONSTRAINT [FK_Cliente_Usuario] FOREIGN KEY ([idUsuario]) REFERENCES [dbo].[Usuario]([idUsuario])
    );
END;
GO

-- =============================================
-- TABLA: Sede
-- =============================================
IF OBJECT_ID(N'dbo.Sede', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Sede](
        [idSede] INT IDENTITY(1,1) NOT NULL,
        [nombre] NVARCHAR(100) NOT NULL,
        [direccion] NVARCHAR(250) NOT NULL,
        [distrito] NVARCHAR(100) NOT NULL,
        [telefono] NVARCHAR(20),
        [activo] BIT NOT NULL,
        CONSTRAINT [PK_Sede] PRIMARY KEY CLUSTERED ([idSede])
    );
END;
GO

-- =============================================
-- TABLA: Salon
-- =============================================
IF OBJECT_ID(N'dbo.Salon', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Salon](
        [idSalon] INT IDENTITY(1,1) NOT NULL,
        [idSede] INT NOT NULL,
        [nombre] NVARCHAR(100) NOT NULL,
        [descripcion] NVARCHAR(500),
        [capacidad] INT NOT NULL,
        [activo] BIT NOT NULL,
        CONSTRAINT [PK_Salon] PRIMARY KEY CLUSTERED ([idSalon]),
        CONSTRAINT [FK_Salon_Sede] FOREIGN KEY ([idSede]) REFERENCES [dbo].[Sede]([idSede])
    );
END;
GO

-- =============================================
-- TABLA: HorarioSalon
-- =============================================
IF OBJECT_ID(N'dbo.HorarioSalon', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[HorarioSalon](
        [idHorarioSalon] INT IDENTITY(1,1) NOT NULL,
        [idSalon] INT NOT NULL,
        [diaSemana] NVARCHAR(20) NOT NULL,
        [horaInicio] TIME(0) NOT NULL,
        [horaFin] TIME(0) NOT NULL,
        [activo] BIT NOT NULL,
        CONSTRAINT [PK_HorarioSalon] PRIMARY KEY CLUSTERED ([idHorarioSalon]),
        CONSTRAINT [FK_HorarioSalon_Salon] FOREIGN KEY ([idSalon]) REFERENCES [dbo].[Salon]([idSalon])
    );
END;
GO
