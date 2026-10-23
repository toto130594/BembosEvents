IF OBJECT_ID(N'[dbo].[AiAnalyticsEvent]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[AiAnalyticsEvent](
        [idAiAnalyticsEvent] BIGINT IDENTITY(1,1) NOT NULL,
        [idConversacion] INT NOT NULL,
        [idSede] INT NULL,
        [intentType] NVARCHAR(60) NOT NULL,
        [toolType] NVARCHAR(60) NULL,
        [executionRoute] NVARCHAR(20) NOT NULL,
        [exitoso] BIT NOT NULL,
        [durationMs] BIGINT NOT NULL,
        [errorType] NVARCHAR(120) NULL,
        [fechaEvento] DATETIME2(0) NOT NULL
            CONSTRAINT [DF_AiAnalyticsEvent_fechaEvento]
            DEFAULT SYSUTCDATETIME(),

        CONSTRAINT [PK_AiAnalyticsEvent]
            PRIMARY KEY CLUSTERED ([idAiAnalyticsEvent]),

        CONSTRAINT [FK_AiAnalyticsEvent_Conversacion]
            FOREIGN KEY ([idConversacion])
            REFERENCES [dbo].[Conversacion]([idConversacion]),

        CONSTRAINT [FK_AiAnalyticsEvent_Sede]
            FOREIGN KEY ([idSede])
            REFERENCES [dbo].[Sede]([idSede]),

        CONSTRAINT [CK_AiAnalyticsEvent_executionRoute]
            CHECK ([executionRoute] IN (N'TOOL', N'PROVIDER')),

        CONSTRAINT [CK_AiAnalyticsEvent_durationMs]
            CHECK ([durationMs] >= 0),

        CONSTRAINT [CK_AiAnalyticsEvent_error]
            CHECK (
                ([exitoso] = 1 AND [errorType] IS NULL)
                OR
                ([exitoso] = 0)
            )
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE [name] = N'IX_AiAnalyticsEvent_fecha_intent'
      AND [object_id] = OBJECT_ID(N'[dbo].[AiAnalyticsEvent]')
)
BEGIN
    CREATE INDEX [IX_AiAnalyticsEvent_fecha_intent]
        ON [dbo].[AiAnalyticsEvent]
        ([fechaEvento] DESC, [intentType], [exitoso]);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE [name] = N'IX_AiAnalyticsEvent_sede_fecha'
      AND [object_id] = OBJECT_ID(N'[dbo].[AiAnalyticsEvent]')
)
BEGIN
    CREATE INDEX [IX_AiAnalyticsEvent_sede_fecha]
        ON [dbo].[AiAnalyticsEvent]
        ([idSede], [fechaEvento] DESC);
END;
GO