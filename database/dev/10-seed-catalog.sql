SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    -- Preservar relaciones existentes de las dos sedes DEV originales.
    UPDATE dbo.Sede
    SET nombre = N'Megaplaza FC',
        direccion = N'Av. Alfredo Mendiola con Av. PacÃ­fico, CC Mega Plaza Lima Norte',
        distrito = N'Independencia',
        telefono = N'419-1919',
        activo = 1
    WHERE nombre = N'DEV - Plaza Norte';

    UPDATE dbo.Sede
    SET nombre = N'Plaza San Miguel',
        direccion = N'Av. de la Marina 2000',
        distrito = N'San Miguel',
        telefono = N'419-1919',
        activo = 1
    WHERE nombre = N'DEV - San Miguel';

    UPDATE dbo.Salon
    SET nombre = N'Ãrea de celebraciones',
        descripcion = N'Espacio preparado para celebraciones, cumpleaÃ±os y reuniones.',
        activo = 1
    WHERE nombre IN (N'DEV - Salon Norte', N'DEV - Salon Marina');

    -- En una BD nueva, conservar IDs iniciales previsibles.
    IF NOT EXISTS (SELECT 1 FROM dbo.Sede WHERE nombre = N'Megaplaza FC')
    BEGIN
        INSERT INTO dbo.Sede (nombre, direccion, distrito, telefono, activo)
        VALUES (N'Megaplaza FC',
                N'Av. Alfredo Mendiola con Av. PacÃ­fico, CC Mega Plaza Lima Norte',
                N'Independencia',
                N'419-1919',
                1);
    END;

    IF NOT EXISTS (SELECT 1 FROM dbo.Sede WHERE nombre = N'Plaza San Miguel')
    BEGIN
        INSERT INTO dbo.Sede (nombre, direccion, distrito, telefono, activo)
        VALUES (N'Plaza San Miguel',
                N'Av. de la Marina 2000',
                N'San Miguel',
                N'419-1919',
                1);
    END;

    DECLARE @Sedes TABLE (
        nombre NVARCHAR(100) NOT NULL PRIMARY KEY,
        direccion NVARCHAR(250) NOT NULL,
        distrito NVARCHAR(100) NOT NULL,
        telefono NVARCHAR(20) NULL
    );

    INSERT INTO @Sedes (nombre, direccion, distrito, telefono)
    VALUES
    (N'Las Gardenias', N'Av. Benavides 5350, Miraflores, Lima', N'Miraflores', N'419-1919'),
    (N'La Fontana', N'Av. La Molina Lt. 11 Mz. A Urb. Residencial Monterrico, La Molina, Lima', N'La Molina', N'419-1919'),
    (N'Larco', N'Av. Larco 461, Miraflores, Lima', N'Miraflores', N'1231321'),
    (N'AviaciÃ³n', N'Av. AviaciÃ³n 2800, San Borja, Lima', N'San Borja', N'419-1919'),
    (N'Mall Del Sur', N'Centro Comercial Mall del Sur, Av. Los Lirios 301, Local PCS-4058', N'San Juan de Miraflores', N'419-1919'),
    (N'Sucre', N'Av. Antonio JosÃ© de Sucre 499', N'Pueblo Libre', N'419-1919'),
    (N'Caminos del Inca', N'Av. Caminos del Inca 215', N'Santiago de Surco', N'419-1919'),
    (N'Mega Plaza Chorrillos', N'Av. Alameda Sur esq. con Av. San Marcos, Mz. II Local L201, 2do nivel', N'Chorrillos', N'419-1919'),
    (N'Metro CanadÃ¡', N'Av. CanadÃ¡ 1110 esq. con NicolÃ¡s Arriola', N'La Victoria', N'419-1919'),
    (N'Mall Aventura SJL', N'Av. Lurigancho 997', N'San Juan de Lurigancho', N'419-1919'),
    (N'Jockey Plaza', N'Av. Javier Prado Este 4200', N'Santiago de Surco', N'419-1919'),
    (N'Plaza San Miguel', N'Av. de la Marina 2000', N'San Miguel', N'419-1919'),
    (N'Monterrico', N'Av. Primavera 1701, Monterrico', N'Santiago de Surco', N'419-1919'),
    (N'Ã“valo GutiÃ©rrez', N'Av. Santa Cruz 808', N'San Isidro', N'419-1919'),
    (N'Aurora', N'Av. Alfredo Benavides 1821', N'Miraflores', N'419-1919'),
    (N'Royal Plaza Express', N'Av. Carlos Izaguirre 287-289, Urb. Panamericana Norte', N'Independencia', N'419-1919'),
    (N'Dos de Mayo', N'Av. Dos de Mayo 1599', N'San Isidro', N'419-1919'),
    (N'Centro CÃ­vico', N'CC Real Plaza Centro CÃ­vico, Av. Paseo de la RepÃºblica, Local P-02', N'Lima', N'419-1919'),
    (N'Javier Prado - Magdalena', N'Av. Javier Prado, Lts. 10B', N'Magdalena del Mar', N'419-1919'),
    (N'Real Plaza Puruchuco', N'Local Comercial 132, Av. NicolÃ¡s AyllÃ³n', N'Ate', N'419-1919'),
    (N'Plaza Center SMP Rex', N'Av. Alfredo Mendiola s/n esq. con TomÃ¡s Valle, LCE-105', N'San MartÃ­n de Porres', N'419-1919'),
    (N'JesÃºs MarÃ­a', N'Av. HÃºsares de JunÃ­n 187', N'JesÃºs MarÃ­a', N'419-1919'),
    (N'Strip La Mar', N'Av. Manuel Cipriano Dulanto & Av. Universitaria', N'San Miguel', N'419-1919'),
    (N'Larco 2', N'Av. Larco 1080', N'Miraflores', N'419-1919'),
    (N'Vea Higuereta', N'Av. AviaciÃ³n 5051', N'Santiago de Surco', N'419-1919'),
    (N'Plaza Lima Sur', N'CC Plaza Lima Sur 147 y 149, Prol. Paseo de la RepÃºblica s/n, Urb. Matellini', N'Chorrillos', N'419-1919'),
    (N'Wong Camacho', N'CC Plaza Camacho Tienda 50, Av. Javier Prado Este', N'La Molina', N'419-1919'),
    (N'Real Plaza Primavera', N'Av. Angamos Este 2681', N'San Borja', N'419-1919'),
    (N'JirÃ³n de La UniÃ³n', N'JirÃ³n de la UniÃ³n 839-841, Cercado de Lima', N'Lima', N'419-1919'),
    (N'Venturosa Chorrillos', N'Av. Guardia Civil 927, Urb. La CampiÃ±a, Local Comercial 102', N'Chorrillos', N'419-1919'),
    (N'Real Plaza Pro', N'Av. Alfredo Mendiola, PNC-01, 1er nivel del Centro Comercial', N'San MartÃ­n de Porres', N'419-1919'),
    (N'Saga', N'Av. Las Begonias 730', N'San Isidro', N'419-1919'),
    (N'Vargas Machuca IL', N'Av. RamÃ³n Vargas Machuca 475, pasaje Pedro Bertonelli 535, Urb. San Juan', N'San Juan de Miraflores', N'419-1919'),
    (N'Plaza Center VES', N'Av. Solidaridad, Plaza Center, Villa El Salvador', N'Villa El Salvador', N'419-1919'),
    (N'UTP Lima Centro', N'Av. Petit Thouars 116', N'Lima', N'419-1919'),
    (N'San Borja Plaza FC', N'Av. Javier Prado Este 2010', N'San Borja', N'419-1919'),
    (N'U Lima', N'Universidad de Lima, PabellÃ³n F1, Centro de Bienestar Universitario', N'Santiago de Surco', N'419-1919'),
    (N'San Juan', N'Av. CircunvalaciÃ³n 1801', N'San Juan de Miraflores', N'419-1919'),
    (N'Santa Anita FC', N'Av. NicolÃ¡s AyllÃ³n, Santa Anita', N'Santa Anita', N'419-1919'),
    (N'DK Magdalena (Cocina oculta)', N'Jr. Amazonas 635', N'Magdalena del Mar', N'419-1919'),
    (N'PUCP Express', N'Av. Universitaria 1801', N'San Miguel', N'419-1919'),
    (N'Callao', N'Av. SÃ¡enz PeÃ±a 1250, Tienda 2', N'Callao', N'419-1919'),
    (N'Santa Clara', N'Av. NicolÃ¡s AyllÃ³n 8694, Local Comercial 132, 2do piso', N'Ate', N'419-1919'),
    (N'Mall Plaza Santa Anita', N'Av. NicolÃ¡s AyllÃ³n, Mall Plaza Santa Anita', N'Santa Anita', N'419-1919'),
    (N'Bembos Asia', N'Boulevard de Asia, Km 97.5 Panamericana Sur, Local 76 Block N', N'Asia', N'419-1919'),
    (N'Bembos Ventanilla', N'Av. NÃ©stor Gambetta 7036', N'Ventanilla', N'419-1919'),
    (N'Ate', N'Av. NicolÃ¡s AyllÃ³n, Sector B, Mz. B Lt. 4, Zona A - Baja, Tienda 7', N'Ate', N'419-1919'),
    (N'Dark Kitchen La Molina', N'Av. La Molina 2880, Mz. E Lt. 1, Urb. El Sauce de La Rinconada', N'La Molina', N'419-1919'),
    (N'Minka', N'Av. Argentina 3257-3093, Local 38, Pasaje 3, PabellÃ³n 3', N'Callao', N'419-1919'),
    (N'Bellavista', N'Mallplaza Bellavista, Av. Ã“scar R. Benavides 3866', N'Bellavista', N'419-1919'),
    (N'DK Surquillo', N'GonzÃ¡les Prada 1025', N'Surquillo', N'419-1919'),
    (N'Cencosud La Molina FC', N'Av. RaÃºl Ferrero Rebagliati 1355, Local 5519, Urb. Sirius II Etapa', N'La Molina', N'419-1919'),
    (N'Bembos Rambla Brasil FC', N'Av. Brasil 778, Local LC-301', N'BreÃ±a', N'419-1919'),
    (N'Villa MarÃ­a del Triunfo FC', N'Av. PachacÃºtec 3721-3781, CÃ©sar Vallejo - Nueva Esperanza', N'Villa MarÃ­a del Triunfo', N'419-1919'),
    (N'Megaplaza FC', N'Av. Alfredo Mendiola con Av. PacÃ­fico, CC Mega Plaza Lima Norte', N'Independencia', N'419-1919'),
    (N'ESAN Monterrico', N'Jr. Alonso de Molina 1652', N'Santiago de Surco', N'419-1919'),
    (N'Barranco', N'Av. Pedro de Osma 108', N'Barranco', N'633-3333'),
    (N'Real Plaza Chiclayo', N'Miguel de Cervantes 300', N'Chiclayo', N'419-1919'),
    (N'Mall Aventura Chiclayo', N'Av. JosemarÃ­a EscrivÃ¡ de Balaguer 700', N'Chiclayo', N'419-1919'),
    (N'Mall Plaza Cayma', N'Av. EjÃ©rcito 795', N'Cayma', N'419-1919'),
    (N'Arequipa Cerro Colorado', N'Av. AviaciÃ³n 602', N'Cerro Colorado', N'419-1919'),
    (N'Arequipa Real Plaza', N'Av. EjÃ©rcito 1009', N'Arequipa', N'419-1919'),
    (N'Lambramani', N'Av. Los Incas, Parque Lambramani', N'Arequipa', N'419-1919'),
    (N'Trujillo Real Plaza', N'Av. FÃ¡tima 1345', N'Trujillo', N'419-1919'),
    (N'Real Plaza Cusco', N'Av. Collasuyo 2964', N'Cusco', N'419-1919'),
    (N'Real Plaza Piura', N'Av. SÃ¡nchez Cerro 234', N'Piura', N'419-1919'),
    (N'Vea Tacna', N'Parcela 9 s/n, Sector Pago Silpay', N'Tacna', N'419-1919'),
    (N'Real Plaza Cajamarca', N'Av. Evitamiento Norte, Lote 1, Barrio San Antonio', N'Cajamarca', N'419-1919'),
    (N'El Quinde Ica', N'CC El Quinde Shopping Plaza, Av. Los Maestros 206, Local PC-202/204', N'Ica', N'419-1919'),
    (N'Real Plaza Juliaca', N'JirÃ³n Tumbes 391', N'Juliaca', N'419-1919'),
    (N'Real Plaza Puno', N'Los Incas 208', N'Puno', N'419-1919'),
    (N'Mall Plaza Huancayo FC', N'Av. Ferrocarril 146, Mall Plaza Huancayo, Local LC-63', N'Huancayo', N'419-1919'),
    (N'Real Plaza Huancayo', N'Av. Ferrocarril s/n', N'Huancayo', N'419-1919'),
    (N'Real Plaza Pucallpa', N'Av. Centenario 1642', N'Yarinacocha', N'419-1919'),
    (N'Larco 3', N'Av. Larco 257-265', N'Miraflores', N'419-1919');

    MERGE dbo.Sede AS target
    USING @Sedes AS source
       ON target.nombre = source.nombre
    WHEN MATCHED THEN
        UPDATE SET
            target.direccion = source.direccion,
            target.distrito = source.distrito,
            target.telefono = source.telefono,
            target.activo = 1
    WHEN NOT MATCHED THEN
        INSERT (nombre, direccion, distrito, telefono, activo)
        VALUES (source.nombre, source.direccion, source.distrito, source.telefono, 1);

        -- R3 CLASIFICACION DE SEDES
    -- Regla interna del proyecto Bembos Events.
    UPDATE dbo.Sede
    SET
        tipoLocal =
            CASE
                WHEN nombre IN (
                    N'UTP Lima Centro',
                    N'U Lima',
                    N'PUCP Express',
                    N'ESAN Monterrico'
                )
                    THEN N'UNIVERSIDAD'
                WHEN nombre LIKE N'%Dark Kitchen%'
                  OR nombre LIKE N'DK %'
                    THEN N'DARK_KITCHEN'
                WHEN nombre LIKE N'% FC'
                    THEN N'FOOD_COURT'
                WHEN nombre LIKE N'%Express%'
                    THEN N'EXPRESS'
                ELSE N'RESTAURANTE'
            END,
        permiteEventos =
            CASE
                WHEN nombre IN (
                    N'UTP Lima Centro',
                    N'U Lima',
                    N'PUCP Express',
                    N'ESAN Monterrico'
                )
                    THEN 0
                WHEN nombre LIKE N'%Dark Kitchen%'
                  OR nombre LIKE N'DK %'
                  OR nombre LIKE N'% FC'
                  OR nombre LIKE N'%Express%'
                    THEN 0
                ELSE 1
            END
    WHERE activo = 1;
-- Un espacio reservable por sede para el demo Bembos Events.
    INSERT INTO dbo.Salon (idSede, nombre, descripcion, capacidad, activo)
    SELECT
        s.idSede,
        N'Ãrea de celebraciones',
        N'Espacio preparado para celebraciones, cumpleaÃ±os y reuniones.',
        CASE
            WHEN s.nombre LIKE N'%Express%'
              OR s.nombre LIKE N'%DK %'
              OR s.nombre LIKE N'%Dark Kitchen%'
              OR s.nombre LIKE N'% FC'
              OR s.nombre IN (N'U Lima', N'UTP Lima Centro', N'PUCP Express', N'ESAN Monterrico')
                THEN 20
            ELSE 40
        END,
        1
    FROM dbo.Sede s
    WHERE s.activo = 1
      AND s.permiteEventos = 1
      AND NOT EXISTS (
          SELECT 1
          FROM dbo.Salon sl
          WHERE sl.idSede = s.idSede
      );

    DECLARE @Dias TABLE (
        diaSemana NVARCHAR(20) NOT NULL PRIMARY KEY,
        horaInicio TIME NOT NULL,
        horaFin TIME NOT NULL
    );

    INSERT INTO @Dias (diaSemana, horaInicio, horaFin)
    VALUES
        (N'LUNES',     '11:00', '22:00'),
        (N'MARTES',    '11:00', '22:00'),
        (N'MIERCOLES', '11:00', '22:00'),
        (N'JUEVES',    '11:00', '22:00'),
        (N'VIERNES',   '11:00', '22:00'),
        (N'SABADO',    '11:00', '22:00'),
        (N'DOMINGO',   '11:00', '22:00');

    INSERT INTO dbo.HorarioSalon (
        idSalon,
        diaSemana,
        horaInicio,
        horaFin,
        activo
    )
    SELECT
        sl.idSalon,
        d.diaSemana,
        d.horaInicio,
        d.horaFin,
        1
    FROM dbo.Salon sl
    CROSS JOIN @Dias d
    WHERE sl.activo = 1
      AND NOT EXISTS (
          SELECT 1
          FROM dbo.HorarioSalon h
          WHERE h.idSalon = sl.idSalon
            AND h.diaSemana = d.diaSemana
      );

    -- Limpiar nombres DEV existentes sin romper FKs.
    ;WITH DevPaquete AS (
        SELECT idPaquete,
               ROW_NUMBER() OVER (ORDER BY idPaquete) AS rn
        FROM dbo.Paquete
        WHERE nombre LIKE N'DEV -%'
    )
    UPDATE p
       SET nombre = CASE d.rn
            WHEN 1 THEN N'Cumple ClÃ¡sico'
            WHEN 2 THEN N'Cumple Plus'
            WHEN 3 THEN N'Cumple Full'
            ELSE CONCAT(N'CelebraciÃ³n Especial ', d.rn)
       END,
       activo = 1
    FROM dbo.Paquete p
    INNER JOIN DevPaquete d
        ON d.idPaquete = p.idPaquete;

    ;WITH DevTematica AS (
        SELECT idTematica,
               ROW_NUMBER() OVER (ORDER BY idTematica) AS rn
        FROM dbo.Tematica
        WHERE nombre LIKE N'DEV -%'
    )
    UPDATE t
       SET nombre = CASE d.rn
            WHEN 1 THEN N'Bembos Party'
            WHEN 2 THEN N'HÃ©roes en AcciÃ³n'
            WHEN 3 THEN N'Aventura Espacial'
            ELSE CONCAT(N'TemÃ¡tica Especial ', d.rn)
       END,
       activo = 1
    FROM dbo.Tematica t
    INNER JOIN DevTematica d
        ON d.idTematica = t.idTematica;

    ;WITH DevServicio AS (
        SELECT idServicioAdicional,
               ROW_NUMBER() OVER (ORDER BY idServicioAdicional) AS rn
        FROM dbo.ServicioAdicional
        WHERE nombre LIKE N'DEV -%'
    )
    UPDATE s
       SET nombre = CONCAT(N'Servicio adicional ', d.rn),
           activo = 1
    FROM dbo.ServicioAdicional s
    INNER JOIN DevServicio d
        ON d.idServicioAdicional = s.idServicioAdicional;

    DECLARE @Paquetes TABLE (
        nombre NVARCHAR(100) NOT NULL PRIMARY KEY,
        descripcion NVARCHAR(500) NULL,
        precioBase DECIMAL(12,2) NOT NULL,
        cantidadPersonasBase INT NOT NULL
    );

    INSERT INTO @Paquetes
        (nombre, descripcion, precioBase, cantidadPersonasBase)
    VALUES
    (N'Cumple ClÃ¡sico', N'Una celebraciÃ³n prÃ¡ctica para grupos pequeÃ±os, con una base sencilla para personalizar sede, horario y temÃ¡tica.', 399.00, 10),
    (N'Cumple Plus', N'MÃ¡s espacio para compartir y una base flexible para sumar temÃ¡tica y servicios adicionales.', 549.00, 15),
    (N'Cumple Full', N'Una opciÃ³n completa para cumpleaÃ±os con mayor capacidad y mÃ¡s posibilidades de personalizaciÃ³n.', 749.00, 20),
    (N'Fiesta Infantil', N'Pensado para celebraciones infantiles con espacio para juegos, decoraciÃ³n y servicios adicionales.', 899.00, 25),
    (N'Fiesta Teen', N'Una propuesta para adolescentes con ambientaciÃ³n moderna y opciones de mÃºsica, fotografÃ­a y temÃ¡tica.', 999.00, 25),
    (N'CelebraciÃ³n Familiar', N'Ideal para reuniones familiares, cumpleaÃ±os y fechas especiales con un grupo amplio.', 1199.00, 30),
    (N'Evento Escolar', N'Una base para promociones, reuniones de aula y celebraciones escolares.', 1499.00, 40),
    (N'Evento Corporativo', N'Formato para reuniones de equipo, integraciones y celebraciones empresariales.', 1799.00, 40);

    MERGE dbo.Paquete AS target
    USING @Paquetes AS source
       ON target.nombre = source.nombre
    WHEN MATCHED THEN
        UPDATE SET
            target.descripcion = source.descripcion,
            target.precioBase = source.precioBase,
            target.cantidadPersonasBase = source.cantidadPersonasBase,
            target.activo = 1
    WHEN NOT MATCHED THEN
        INSERT (nombre, descripcion, precioBase, cantidadPersonasBase, activo)
        VALUES (
            source.nombre,
            source.descripcion,
            source.precioBase,
            source.cantidadPersonasBase,
            1
        );

    DECLARE @Tematicas TABLE (
        nombre NVARCHAR(100) NOT NULL PRIMARY KEY,
        descripcion NVARCHAR(500) NULL,
        precioAdicional DECIMAL(12,2) NOT NULL
    );

    INSERT INTO @Tematicas
        (nombre, descripcion, precioAdicional)
    VALUES
    (N'Bembos Party', N'AmbientaciÃ³n inspirada en una fiesta urbana, alegre y colorida.', 0.00),
    (N'HÃ©roes en AcciÃ³n', N'DecoraciÃ³n de hÃ©roes originales, ciudad, rayos y colores intensos.', 120.00),
    (N'Aventura Espacial', N'Planetas, estrellas, cohetes y una ambientaciÃ³n galÃ¡ctica.', 140.00),
    (N'Mundo Dinosaurio', N'Selva, huellas y dinosaurios para una celebraciÃ³n de aventura.', 150.00),
    (N'Princesas y Castillos', N'Castillos, coronas y tonos festivos para una celebraciÃ³n de cuento.', 160.00),
    (N'PasiÃ³n por el FÃºtbol', N'Cancha, balones y ambiente deportivo para fanÃ¡ticos del fÃºtbol.', 120.00),
    (N'Gamer Party', N'Controles, pixeles, luces y estÃ©tica de videojuegos.', 170.00),
    (N'NeÃ³n Party', N'Luces, colores fluorescentes y estilo moderno para fiestas juveniles.', 190.00),
    (N'Safari Aventurero', N'Animales, vegetaciÃ³n y exploraciÃ³n en una ambientaciÃ³n de safari.', 150.00),
    (N'Piratas del Tesoro', N'Mapas, cofres y aventura pirata para buscar el tesoro.', 150.00),
    (N'Unicornio ArcoÃ­ris', N'ArcoÃ­ris, nubes y colores suaves para una fiesta fantÃ¡stica.', 160.00),
    (N'Ciencia Divertida', N'Laboratorio creativo, experimentos visuales y decoraciÃ³n cientÃ­fica.', 180.00);

    MERGE dbo.Tematica AS target
    USING @Tematicas AS source
       ON target.nombre = source.nombre
    WHEN MATCHED THEN
        UPDATE SET
            target.descripcion = source.descripcion,
            target.precioAdicional = source.precioAdicional,
            target.activo = 1
    WHEN NOT MATCHED THEN
        INSERT (nombre, descripcion, precioAdicional, activo)
        VALUES (
            source.nombre,
            source.descripcion,
            source.precioAdicional,
            1
        );

    DECLARE @Servicios TABLE (
        nombre NVARCHAR(100) NOT NULL PRIMARY KEY,
        descripcion NVARCHAR(500) NULL,
        precio DECIMAL(12,2) NOT NULL
    );

    INSERT INTO @Servicios
        (nombre, descripcion, precio)
    VALUES
    (N'Torta personalizada', N'Torta temÃ¡tica para complementar la celebraciÃ³n.', 180.00),
    (N'DecoraciÃ³n con globos', N'Arco y composiciÃ³n de globos de acuerdo con la temÃ¡tica elegida.', 150.00),
    (N'Animador', N'AnimaciÃ³n guiada con dinÃ¡micas y juegos para los invitados.', 220.00),
    (N'FotografÃ­a del evento', N'Cobertura fotogrÃ¡fica durante la celebraciÃ³n.', 250.00),
    (N'Video recuerdo', N'Video corto con los mejores momentos del evento.', 180.00),
    (N'PiÃ±ata temÃ¡tica', N'PiÃ±ata preparada de acuerdo con la temÃ¡tica seleccionada.', 120.00),
    (N'Pinta caritas', N'Servicio de pintado facial para invitados.', 140.00),
    (N'Mesa dulce', N'Mesa con dulces y presentaciÃ³n decorativa para la celebraciÃ³n.', 250.00),
    (N'Invitaciones digitales', N'DiseÃ±o de invitaciÃ³n digital personalizada para compartir.', 60.00),
    (N'Sonido y DJ', N'MÃºsica y ambientaciÃ³n sonora durante el evento.', 300.00);

    MERGE dbo.ServicioAdicional AS target
    USING @Servicios AS source
       ON target.nombre = source.nombre
    WHEN MATCHED THEN
        UPDATE SET
            target.descripcion = source.descripcion,
            target.precio = source.precio,
            target.activo = 1
    WHEN NOT MATCHED THEN
        INSERT (nombre, descripcion, precio, activo)
        VALUES (
            source.nombre,
            source.descripcion,
            source.precio,
            1
        );

    -- ============================================================
    -- R5 CONTENIDO COMERCIAL DEMO
    -- ============================================================
    -- IMPORTANTE:
    -- Los nombres, descripciones y precios de Paquete, Tematica y
    -- ServicioAdicional son datos DEMO del proyecto Bembos Events.
    -- No representan productos, promociones ni tarifas oficiales
    -- publicadas por Bembos.
    --
    -- R5 mejora solo el contenido descriptivo. Los precios y las
    -- cantidades base se conservan sin cambios.

    UPDATE dbo.Paquete
    SET descripcion =
        CASE nombre
            WHEN N'Cumple ClÃ¡sico' THEN
                N'Una opciÃ³n prÃ¡ctica para una celebraciÃ³n pequeÃ±a y cercana. Parte de una base para 10 personas y permite sumar temÃ¡tica y servicios adicionales durante la reserva.'
            WHEN N'Cumple Plus' THEN
                N'Pensado para compartir con un grupo mediano sin complicarte. Su base para 15 personas te permite personalizar la experiencia con temÃ¡tica y servicios adicionales.'
            WHEN N'Cumple Full' THEN
                N'Una alternativa mÃ¡s amplia para celebrar con comodidad. Su base para 20 personas funciona como punto de partida para una experiencia con mayor personalizaciÃ³n.'
            WHEN N'Fiesta Infantil' THEN
                N'Creado para celebraciones infantiles con una base para 25 personas. Combina el paquete con temÃ¡ticas coloridas y servicios adicionales para darle personalidad al evento.'
            WHEN N'Fiesta Teen' THEN
                N'Una propuesta para celebraciones juveniles con base para 25 personas. Ideal para complementar con ambientaciones modernas, mÃºsica, fotografÃ­a y otros servicios disponibles.'
            WHEN N'CelebraciÃ³n Familiar' THEN
                N'Pensado para reunir a la familia en cumpleaÃ±os y fechas especiales. Su base para 30 personas ofrece espacio para personalizar la celebraciÃ³n segÃºn el estilo del grupo.'
            WHEN N'Evento Escolar' THEN
                N'Una opciÃ³n para promociones, reuniones de aula y celebraciones escolares. Parte de una base para 40 personas y permite complementar la experiencia durante la reserva.'
            WHEN N'Evento Corporativo' THEN
                N'Formato pensado para integraciones, celebraciones de equipo y encuentros empresariales. Su base para 40 personas puede complementarse con servicios adicionales.'
            ELSE descripcion
        END
    WHERE nombre IN (
        N'Cumple ClÃ¡sico',
        N'Cumple Plus',
        N'Cumple Full',
        N'Fiesta Infantil',
        N'Fiesta Teen',
        N'CelebraciÃ³n Familiar',
        N'Evento Escolar',
        N'Evento Corporativo'
    );

    UPDATE dbo.Tematica
    SET descripcion =
        CASE nombre
            WHEN N'Bembos Party' THEN
                N'La ambientaciÃ³n base de Bembos Events: colores intensos, energÃ­a de fiesta y un estilo alegre que combina con cualquier tipo de celebraciÃ³n.'
            WHEN N'HÃ©roes en AcciÃ³n' THEN
                N'Una ambientaciÃ³n de aventura con ciudad, rayos, emblemas y hÃ©roes originales. Ideal para una celebraciÃ³n dinÃ¡mica y llena de energÃ­a.'
            WHEN N'Aventura Espacial' THEN
                N'Planetas, estrellas, cohetes y detalles galÃ¡cticos transforman el espacio en una misiÃ³n por el universo.'
            WHEN N'Mundo Dinosaurio' THEN
                N'VegetaciÃ³n, huellas y dinosaurios crean una experiencia prehistÃ³rica pensada para pequeÃ±os exploradores.'
            WHEN N'Princesas y Castillos' THEN
                N'Castillos, coronas y detalles de cuento crean una celebraciÃ³n elegante, fantÃ¡stica y llena de color.'
            WHEN N'PasiÃ³n por el FÃºtbol' THEN
                N'Cancha, balones y detalles deportivos para celebrar con la emociÃ³n de un gran partido.'
            WHEN N'Gamer Party' THEN
                N'Controles, pÃ­xeles y referencias al mundo de los videojuegos para una fiesta con estÃ©tica gamer y mucha energÃ­a.'
            WHEN N'NeÃ³n Party' THEN
                N'Colores fluorescentes, contrastes intensos y una estÃ©tica moderna para una celebraciÃ³n juvenil con personalidad.'
            WHEN N'Safari Aventurero' THEN
                N'Animales, vegetaciÃ³n y detalles de exploraciÃ³n convierten el espacio en una aventura inspirada en la naturaleza.'
            WHEN N'Piratas del Tesoro' THEN
                N'Mapas, cofres y sÃ­mbolos piratas preparan el escenario para una celebraciÃ³n llena de aventura y bÃºsqueda del tesoro.'
            WHEN N'Unicornio ArcoÃ­ris' THEN
                N'ArcoÃ­ris, nubes y tonos suaves crean una ambientaciÃ³n fantÃ¡stica, luminosa y llena de detalles mÃ¡gicos.'
            WHEN N'Ciencia Divertida' THEN
                N'Elementos de laboratorio, fÃ³rmulas y experimentos visuales dan forma a una celebraciÃ³n creativa inspirada en la ciencia.'
            ELSE descripcion
        END
    WHERE nombre IN (
        N'Bembos Party',
        N'HÃ©roes en AcciÃ³n',
        N'Aventura Espacial',
        N'Mundo Dinosaurio',
        N'Princesas y Castillos',
        N'PasiÃ³n por el FÃºtbol',
        N'Gamer Party',
        N'NeÃ³n Party',
        N'Safari Aventurero',
        N'Piratas del Tesoro',
        N'Unicornio ArcoÃ­ris',
        N'Ciencia Divertida'
    );

    UPDATE dbo.ServicioAdicional
    SET descripcion =
        CASE nombre
            WHEN N'Torta personalizada' THEN
                N'Torta decorada para complementar la celebraciÃ³n y mantener una lÃ­nea visual acorde con el estilo elegido para el evento.'
            WHEN N'DecoraciÃ³n con globos' THEN
                N'ComposiciÃ³n decorativa con globos para reforzar la ambientaciÃ³n y crear un punto visual destacado dentro de la celebraciÃ³n.'
            WHEN N'Animador' THEN
                N'AnimaciÃ³n guiada con dinÃ¡micas y juegos para acompaÃ±ar a los invitados y mantener activa la celebraciÃ³n.'
            WHEN N'FotografÃ­a del evento' THEN
                N'Cobertura fotogrÃ¡fica para registrar momentos importantes de la celebraciÃ³n y conservar un recuerdo del evento.'
            WHEN N'Video recuerdo' THEN
                N'Registro en video de momentos destacados de la celebraciÃ³n, preparado como un recuerdo breve del evento.'
            WHEN N'PiÃ±ata temÃ¡tica' THEN
                N'PiÃ±ata preparada para complementar la temÃ¡tica seleccionada y sumar una actividad clÃ¡sica a la celebraciÃ³n.'
            WHEN N'Pinta caritas' THEN
                N'Servicio de pintado facial con diseÃ±os recreativos para aÃ±adir una actividad divertida durante el evento.'
            WHEN N'Mesa dulce' THEN
                N'PresentaciÃ³n decorativa de dulces para complementar la ambientaciÃ³n y aÃ±adir un punto especial dentro de la celebraciÃ³n.'
            WHEN N'Invitaciones digitales' THEN
                N'DiseÃ±o de invitaciÃ³n digital personalizada para compartir fÃ¡cilmente los datos principales de la celebraciÃ³n.'
            WHEN N'Sonido y DJ' THEN
                N'MÃºsica y ambientaciÃ³n sonora para acompaÃ±ar distintos momentos del evento y mantener una atmÃ³sfera de celebraciÃ³n.'
            ELSE descripcion
        END
    WHERE nombre IN (
        N'Torta personalizada',
        N'DecoraciÃ³n con globos',
        N'Animador',
        N'FotografÃ­a del evento',
        N'Video recuerdo',
        N'PiÃ±ata temÃ¡tica',
        N'Pinta caritas',
        N'Mesa dulce',
        N'Invitaciones digitales',
        N'Sonido y DJ'
    );
    COMMIT TRANSACTION;

    SELECT
        (SELECT COUNT(*) FROM dbo.Sede WHERE activo = 1) AS sedesActivas,
        (SELECT COUNT(*) FROM dbo.Salon WHERE activo = 1) AS salonesActivos,
        (SELECT COUNT(*) FROM dbo.Paquete WHERE activo = 1) AS paquetesActivos,
        (SELECT COUNT(*) FROM dbo.Tematica WHERE activo = 1) AS tematicasActivas,
        (SELECT COUNT(*) FROM dbo.ServicioAdicional WHERE activo = 1) AS serviciosActivos;

END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
