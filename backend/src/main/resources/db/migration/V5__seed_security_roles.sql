IF NOT EXISTS (
    SELECT 1 FROM dbo.Rol WHERE nombre = 'CLIENTE'
)
BEGIN
    INSERT INTO dbo.Rol (nombre, descripcion, activo)
    VALUES (
        'CLIENTE',
        'Cliente que realiza y consulta reservas',
        1
    );
END;

IF NOT EXISTS (
    SELECT 1 FROM dbo.Rol WHERE nombre = 'ENCARGADO_LOCAL'
)
BEGIN
    INSERT INTO dbo.Rol (nombre, descripcion, activo)
    VALUES (
        'ENCARGADO_LOCAL',
        'Encargado de administrar una sede o local',
        1
    );
END;

IF NOT EXISTS (
    SELECT 1 FROM dbo.Rol WHERE nombre = 'ADMINISTRADOR'
)
BEGIN
    INSERT INTO dbo.Rol (nombre, descripcion, activo)
    VALUES (
        'ADMINISTRADOR',
        'Administrador general del sistema',
        1
    );
END;