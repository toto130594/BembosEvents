-- FASE 11.0.6 - Mercado Pago provider webhook events
-- Idempotency authority: UX_EventoPagoProveedor_proveedor_evento

IF OBJECT_ID(N'dbo.EventoPagoProveedor', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[EventoPagoProveedor] (
        [idEventoPagoProveedor] BIGINT IDENTITY(1,1) NOT NULL,
        [proveedor] NVARCHAR(40) NOT NULL,
        [idEventoExterno] NVARCHAR(200) NOT NULL,
        [idRecursoExterno] NVARCHAR(150) NOT NULL,
        [tipoEvento] NVARCHAR(80) NULL,
        [accion] NVARCHAR(120) NULL,
        [requestId] NVARCHAR(150) NULL,
        [liveMode] BIT NULL,
        [fechaRecepcion] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_EventoPagoProveedor_fechaRecepcion]
            DEFAULT (SYSUTCDATETIME()),
        [procesado] BIT NOT NULL
            CONSTRAINT [DF_EventoPagoProveedor_procesado]
            DEFAULT (0),
        [fechaProcesamiento] DATETIME2(0) NULL,
        [resultado] NVARCHAR(50) NOT NULL
            CONSTRAINT [DF_EventoPagoProveedor_resultado]
            DEFAULT (N'RECIBIDO'),
        CONSTRAINT [PK_EventoPagoProveedor]
            PRIMARY KEY CLUSTERED ([idEventoPagoProveedor])
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE [object_id] = OBJECT_ID(N'dbo.EventoPagoProveedor')
      AND [name] = N'UX_EventoPagoProveedor_proveedor_evento'
)
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX
        [UX_EventoPagoProveedor_proveedor_evento]
    ON [dbo].[EventoPagoProveedor] (
        [proveedor],
        [idEventoExterno]
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE [object_id] = OBJECT_ID(N'dbo.EventoPagoProveedor')
      AND [name] = N'IX_EventoPagoProveedor_recurso_fecha'
)
BEGIN
    CREATE NONCLUSTERED INDEX
        [IX_EventoPagoProveedor_recurso_fecha]
    ON [dbo].[EventoPagoProveedor] (
        [idRecursoExterno],
        [fechaRecepcion]
    );
END;
GO