-- Inicialización automática de BembosEventsDB
-- Idempotente: no falla si la base ya existe

IF DB_ID(N'BembosEventsDB') IS NULL
BEGIN
    CREATE DATABASE [BembosEventsDB]
END

SELECT
    name,
    state_desc
FROM sys.databases
WHERE name = N'BembosEventsDB'

SELECT COUNT(*) AS TablasUsuario
FROM BembosEventsDB.sys.tables
