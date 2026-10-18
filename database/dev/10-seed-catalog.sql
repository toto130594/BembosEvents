SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

------------------------------------------------------------
-- DEV DATA ONLY
-- Idempotent local development catalog.
------------------------------------------------------------

IF OBJECT_ID(N'dbo.Sede', N'U') IS NULL
    THROW 50001, 'dbo.Sede does not exist. Flyway has not completed.', 1;

IF OBJECT_ID(N'dbo.Salon', N'U') IS NULL
    THROW 50002, 'dbo.Salon does not exist. Flyway has not completed.', 1;

IF OBJECT_ID(N'dbo.HorarioSalon', N'U') IS NULL
    THROW 50003, 'dbo.HorarioSalon does not exist. Flyway has not completed.', 1;


------------------------------------------------------------
-- SEDES DEV
------------------------------------------------------------

IF NOT EXISTS (
    SELECT 1
    FROM dbo.Sede
    WHERE nombre = N'DEV - Plaza Norte'
)
BEGIN
    INSERT INTO dbo.Sede (
        nombre,
        direccion,
        distrito,
        telefono,
        activo
    )
    VALUES (
        N'DEV - Plaza Norte',
        N'Av. Alfredo Mendiola 1400',
        N'Independencia',
        N'999111111',
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.Sede
    WHERE nombre = N'DEV - San Miguel'
)
BEGIN
    INSERT INTO dbo.Sede (
        nombre,
        direccion,
        distrito,
        telefono,
        activo
    )
    VALUES (
        N'DEV - San Miguel',
        N'Av. La Marina 2000',
        N'San Miguel',
        N'999222222',
        1
    );
END;


DECLARE @IdSedeNorte INT =
(
    SELECT TOP (1) idSede
    FROM dbo.Sede
    WHERE nombre = N'DEV - Plaza Norte'
    ORDER BY idSede
);

DECLARE @IdSedeSanMiguel INT =
(
    SELECT TOP (1) idSede
    FROM dbo.Sede
    WHERE nombre = N'DEV - San Miguel'
    ORDER BY idSede
);

IF @IdSedeNorte IS NULL
    THROW 50004, 'DEV Plaza Norte could not be resolved.', 1;

IF @IdSedeSanMiguel IS NULL
    THROW 50005, 'DEV San Miguel could not be resolved.', 1;


------------------------------------------------------------
-- SALONES DEV
------------------------------------------------------------

IF EXISTS (
    SELECT 1
    FROM dbo.Salon
    WHERE idSede = @IdSedeNorte
      AND nombre = N'DEV - Salon Norte'
)
BEGIN
    UPDATE dbo.Salon
    SET descripcion =
            N'Salon DEV para pruebas funcionales del flujo de reservas.',
        capacidad = 40,
        activo = 1
    WHERE idSede = @IdSedeNorte
      AND nombre = N'DEV - Salon Norte';
END
ELSE
BEGIN
    INSERT INTO dbo.Salon (
        idSede,
        nombre,
        descripcion,
        capacidad,
        activo
    )
    VALUES (
        @IdSedeNorte,
        N'DEV - Salon Norte',
        N'Salon DEV para pruebas funcionales del flujo de reservas.',
        40,
        1
    );
END;


IF EXISTS (
    SELECT 1
    FROM dbo.Salon
    WHERE idSede = @IdSedeSanMiguel
      AND nombre = N'DEV - Salon Marina'
)
BEGIN
    UPDATE dbo.Salon
    SET descripcion =
            N'Salon DEV para pruebas funcionales del flujo de reservas.',
        capacidad = 35,
        activo = 1
    WHERE idSede = @IdSedeSanMiguel
      AND nombre = N'DEV - Salon Marina';
END
ELSE
BEGIN
    INSERT INTO dbo.Salon (
        idSede,
        nombre,
        descripcion,
        capacidad,
        activo
    )
    VALUES (
        @IdSedeSanMiguel,
        N'DEV - Salon Marina',
        N'Salon DEV para pruebas funcionales del flujo de reservas.',
        35,
        1
    );
END;


DECLARE @IdSalonNorte INT =
(
    SELECT TOP (1) idSalon
    FROM dbo.Salon
    WHERE idSede = @IdSedeNorte
      AND nombre = N'DEV - Salon Norte'
    ORDER BY idSalon
);

DECLARE @IdSalonMarina INT =
(
    SELECT TOP (1) idSalon
    FROM dbo.Salon
    WHERE idSede = @IdSedeSanMiguel
      AND nombre = N'DEV - Salon Marina'
    ORDER BY idSalon
);

IF @IdSalonNorte IS NULL
    THROW 50006, 'DEV Salon Norte could not be resolved.', 1;

IF @IdSalonMarina IS NULL
    THROW 50007, 'DEV Salon Marina could not be resolved.', 1;


------------------------------------------------------------
-- HORARIOS DEV
------------------------------------------------------------

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonNorte
      AND diaSemana = N'LUNES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonNorte,
        N'LUNES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonMarina
      AND diaSemana = N'LUNES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonMarina,
        N'LUNES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonNorte
      AND diaSemana = N'MARTES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonNorte,
        N'MARTES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonMarina
      AND diaSemana = N'MARTES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonMarina,
        N'MARTES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonNorte
      AND diaSemana = N'MIERCOLES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonNorte,
        N'MIERCOLES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonMarina
      AND diaSemana = N'MIERCOLES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonMarina,
        N'MIERCOLES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonNorte
      AND diaSemana = N'JUEVES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonNorte,
        N'JUEVES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonMarina
      AND diaSemana = N'JUEVES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonMarina,
        N'JUEVES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonNorte
      AND diaSemana = N'VIERNES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonNorte,
        N'VIERNES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonMarina
      AND diaSemana = N'VIERNES'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonMarina,
        N'VIERNES',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonNorte
      AND diaSemana = N'SABADO'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonNorte,
        N'SABADO',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonMarina
      AND diaSemana = N'SABADO'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonMarina,
        N'SABADO',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonNorte
      AND diaSemana = N'DOMINGO'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonNorte,
        N'DOMINGO',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.HorarioSalon
    WHERE idSalon = @IdSalonMarina
      AND diaSemana = N'DOMINGO'
      AND horaInicio = CAST('10:00:00' AS time)
      AND horaFin = CAST('22:00:00' AS time)
)
BEGIN
    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    VALUES (
        @IdSalonMarina,
        N'DOMINGO',
        CAST('10:00:00' AS time),
        CAST('22:00:00' AS time),
        1
    );
END;



------------------------------------------------------------
-- NORMALIZAR ESTADO DEV
------------------------------------------------------------

UPDATE dbo.Sede
SET activo = 1
WHERE idSede IN (
    @IdSedeNorte,
    @IdSedeSanMiguel
);

UPDATE dbo.Salon
SET activo = 1
WHERE idSalon IN (
    @IdSalonNorte,
    @IdSalonMarina
);

UPDATE dbo.HorarioSalon
SET activo = 1
WHERE idSalon IN (
    @IdSalonNorte,
    @IdSalonMarina
);


------------------------------------------------------------
-- VALIDACIONES
------------------------------------------------------------

IF (
    SELECT COUNT(*)
    FROM dbo.Salon
    WHERE idSalon IN (
        @IdSalonNorte,
        @IdSalonMarina
    )
) <> 2
BEGIN
    THROW 50008, 'Unexpected DEV salon count.', 1;
END;

IF (
    SELECT COUNT(*)
    FROM dbo.HorarioSalon
    WHERE idSalon IN (
        @IdSalonNorte,
        @IdSalonMarina
    )
      AND activo = 1
) <> 14
BEGIN
    THROW 50009, 'Unexpected DEV schedule count.', 1;
END;


COMMIT TRANSACTION;


PRINT 'DEV SEED OK';

SELECT
    s.idSede,
    s.nombre AS sede,
    sa.idSalon,
    sa.nombre AS salon,
    sa.capacidad,
    sa.activo
FROM dbo.Sede s
INNER JOIN dbo.Salon sa
    ON sa.idSede = s.idSede
WHERE sa.idSalon IN (
    @IdSalonNorte,
    @IdSalonMarina
)
ORDER BY
    s.idSede,
    sa.idSalon;

SELECT
    idSalon,
    COUNT(*) AS horariosActivos
FROM dbo.HorarioSalon
WHERE idSalon IN (
    @IdSalonNorte,
    @IdSalonMarina
)
  AND activo = 1
GROUP BY idSalon
ORDER BY idSalon;

-- 8.0.9G DEV PAQUETE RESERVA
-- Dataset exclusivo DEV.
-- El precio usa el monto minimo configurado por Spring Boot.
-- No modifica reglas productivas.

IF EXISTS (
    SELECT 1
    FROM dbo.Paquete
    WHERE nombre =
        N'DEV - Paquete Reserva Base'
)
BEGIN
    UPDATE dbo.Paquete
    SET
        descripcion =
            N'Paquete DEV para pruebas integrales del flujo de reserva',
        precioBase =
            500.00,
        cantidadPersonasBase =
            40,
        activo =
            1
    WHERE nombre =
        N'DEV - Paquete Reserva Base';
END
ELSE
BEGIN
    INSERT INTO dbo.Paquete (
        nombre,
        descripcion,
        precioBase,
        cantidadPersonasBase,
        activo
    )
    VALUES (
        N'DEV - Paquete Reserva Base',
        N'Paquete DEV para pruebas integrales del flujo de reserva',
        500.00,
        40,
        1
    );
END;
