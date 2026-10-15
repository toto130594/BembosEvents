USE [BembosEventsDB];
GO

-- =============================================
-- FASE 2.5A — ÍNDICES DE RENDIMIENTO
-- =============================================
-- Este archivo contiene índices de rendimiento aprobados en DATABASE_STATES_INDEXES.md
-- Cada índice es idempotente (verifica existencia antes de crear)
-- NO duplica PRIMARY KEY, UNIQUE constraints ni índices existentes
-- =============================================

-- =============================================
-- 1. ÍNDICES DE UNICIDAD (candidatos documentados)
-- =============================================

-- Reserva.codigoReserva - ya tiene UNIQUE físico en 002_create_tables_08_14.sql
-- NO crear índice adicional (es candidato de diseño)

-- =============================================
-- 2. ÍNDICES DE RELACIONES (facilitar búsquedas y relaciones por claves foráneas)
-- =============================================

-- Salon.idSede - facilitar búsqueda por sede
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Salon') AND name = N'IX_Salon_idSede')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Salon_idSede] ON [dbo].[Salon] ([idSede])
END;
GO

-- HorarioSalon.idSalon - facilitar búsqueda por salón
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.HorarioSalon') AND name = N'IX_HorarioSalon_idSalon')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_HorarioSalon_idSalon] ON [dbo].[HorarioSalon] ([idSalon])
END;
GO

-- Reserva.idPaquete - relación con paquete
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Reserva') AND name = N'IX_Reserva_idPaquete')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Reserva_idPaquete] ON [dbo].[Reserva] ([idPaquete])
END;
GO

-- Reserva.idTematica - relación con temática
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Reserva') AND name = N'IX_Reserva_idTematica')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Reserva_idTematica] ON [dbo].[Reserva] ([idTematica])
END;
GO

-- ReservaServicio.idReserva - relación con reserva
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.ReservaServicio') AND name = N'IX_ReservaServicio_idReserva')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_ReservaServicio_idReserva] ON [dbo].[ReservaServicio] ([idReserva])
END;
GO

-- ReservaServicio.idServicioAdicional - relación con servicio adicional
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.ReservaServicio') AND name = N'IX_ReservaServicio_idServicioAdicional')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_ReservaServicio_idServicioAdicional] ON [dbo].[ReservaServicio] ([idServicioAdicional])
END;
GO

-- Pago.idReserva - relación con reserva
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Pago') AND name = N'IX_Pago_idReserva')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Pago_idReserva] ON [dbo].[Pago] ([idReserva])
END;
GO

-- HistorialReserva.idUsuarioResponsable - relación con usuario responsable
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.HistorialReserva') AND name = N'IX_HistorialReserva_idUsuarioResponsable')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_HistorialReserva_idUsuarioResponsable] ON [dbo].[HistorialReserva] ([idUsuarioResponsable])
END;
GO

-- =============================================
-- 3. ÍNDICES PARA DISPONIBILIDAD (detección de superposición)
-- =============================================

-- Reserva.idSalon + fechaEvento + horaInicio + horaFin
-- Motivo: la consulta de disponibilidad necesitará localizar rápidamente reservas del mismo salón y fecha
-- antes de evaluar superposición: inicioExistente < finNuevo AND finExistente > inicioNuevo
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Reserva') AND name = N'IX_Reserva_idSalon_fechaEvento_horaInicio_horaFin')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Reserva_idSalon_fechaEvento_horaInicio_horaFin]
    ON [dbo].[Reserva] ([idSalon], [fechaEvento], [horaInicio], [horaFin])
END;
GO

-- =============================================
-- 4. ÍNDICES PARA CONSULTAS FRECUENTES
-- =============================================

-- Reserva.estado - reservas por estado
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Reserva') AND name = N'IX_Reserva_estado')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Reserva_estado] ON [dbo].[Reserva] ([estado])
END;
GO

-- Reserva.idCliente + fechaEvento - reservas por cliente y fecha
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Reserva') AND name = N'IX_Reserva_idCliente_fechaEvento')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Reserva_idCliente_fechaEvento] ON [dbo].[Reserva] ([idCliente], [fechaEvento])
END;
GO

-- Reserva.idSede + fechaEvento - agenda por sede y fecha
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Reserva') AND name = N'IX_Reserva_idSede_fechaEvento')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Reserva_idSede_fechaEvento] ON [dbo].[Reserva] ([idSede], [fechaEvento])
END;
GO

-- Pago.estadoPago - pagos por estado
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Pago') AND name = N'IX_Pago_estadoPago')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Pago_estadoPago] ON [dbo].[Pago] ([estadoPago])
END;
GO

-- Pago.fechaPago - consultas por fecha de pago
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.Pago') AND name = N'IX_Pago_fechaPago')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_Pago_fechaPago] ON [dbo].[Pago] ([fechaPago])
END;
GO

-- HistorialReserva.fechaCambio - consultar cronológicamente el historial de una Reserva
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.HistorialReserva') AND name = N'IX_HistorialReserva_fechaCambio')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_HistorialReserva_fechaCambio] ON [dbo].[HistorialReserva] ([fechaCambio])
END;
GO

-- =============================================
-- FIN DE ÍNDICES DE RENDIMIENTO
-- =============================================
