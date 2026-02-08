-- Ejemplo de empleados y clientes y parámetros del sistema

USE cv_ventas_distribucion;
GO

BEGIN TRY
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRAN;

    -- Deshabilitar triggers de protección
    EXEC dbo.sp_DisableSeedTriggers;

    DECLARE @idEstadoPersona INT =
        dbo.fn_estado(N'Persona', N'Activo');

    DECLARE @people TABLE(
        personaKey NVARCHAR(20) PRIMARY KEY,
        nombres    NVARCHAR(60),
        apellidos  NVARCHAR(60),
        dni        CHAR(8),
        telefono   NVARCHAR(15),
        idEstado   INT,
        esEmpleado BIT,
        usuario    NVARCHAR(30) NULL,
        hashClave  NVARCHAR(120) NULL,
        rol        NVARCHAR(20) NULL,
        direccion  NVARCHAR(120) NULL
    );

    INSERT INTO @people(personaKey, nombres, apellidos, dni, telefono, idEstado,
                        esEmpleado, usuario, hashClave, rol, direccion)
    VALUES
        (N'admin', N'Administrador', N'General', N'00000001', N'000000000',
         @idEstadoPersona, 1, N'admin',
         N'$argon2id$v=19$m=100000,t=4,p=8$YU91Wk9BYXhHY2hDd2tvaQ$l5CViqNC2mE/Yd7v/4Io/g',
         N'Administrador', NULL),
        (N'empleado', N'Empleado', N'Demo', N'00000002', N'999888777',
         @idEstadoPersona, 1, N'empleado',
         N'$argon2id$v=19$m=100000,t=4,p=8$YU91Wk9BYXhHY2hDd2tvaQ$sRtM+iG5gjTWqZ+HiFP4hA',
         N'Empleado', NULL),
        (N'cli_default', N'Cliente', N'Generico', N'00000000', NULL,
         @idEstadoPersona, 0, NULL, NULL, NULL, N'N/A'),
        (N'cli1', N'Juan', N'Pérez', N'00000003', N'987654321',
         @idEstadoPersona, 0, NULL, NULL, NULL, N'Avenida Principal 123'),
        (N'cli2', N'María', N'Gómez', N'00000004', N'912345678',
         @idEstadoPersona, 0, NULL, NULL, NULL, N'Calle Secundaria 456'),
        (N'cli3', N'Carlos', N'López', N'00000005', N'900111222',
         @idEstadoPersona, 0, NULL, NULL, NULL, N'Avenida Central 789');

    DECLARE @ids TABLE(personaKey NVARCHAR(20) PRIMARY KEY, idPersona INT);

    MERGE dbo.Persona WITH (HOLDLOCK) AS p
    USING @people src
        ON p.dni = src.dni
    WHEN NOT MATCHED THEN
        INSERT(nombres, apellidos, dni, telefono, idEstado)
        VALUES(src.nombres, src.apellidos, src.dni, src.telefono, src.idEstado)
    OUTPUT src.personaKey, inserted.idPersona INTO @ids(personaKey, idPersona);

    INSERT INTO @ids(personaKey, idPersona)
    SELECT src.personaKey, p.idPersona
    FROM dbo.Persona p
    JOIN @people src ON p.dni = src.dni
    WHERE NOT EXISTS (SELECT 1 FROM @ids i WHERE i.personaKey = src.personaKey);

    DECLARE
        @idAdminPersona INT = (SELECT idPersona FROM @ids WHERE personaKey = N'admin'),
        @idEmpPersona   INT = (SELECT idPersona FROM @ids WHERE personaKey = N'empleado'),
        @idCliDefault   INT = (SELECT idPersona FROM @ids WHERE personaKey = N'cli_default'),
        @idCli1         INT = (SELECT idPersona FROM @ids WHERE personaKey = N'cli1'),
        @idCli2         INT = (SELECT idPersona FROM @ids WHERE personaKey = N'cli2'),
        @idCli3         INT = (SELECT idPersona FROM @ids WHERE personaKey = N'cli3');

    IF @idAdminPersona IS NULL
        THROW 64000, 'Admin user not found for seeding.', 1;

    MERGE dbo.Empleado WITH (HOLDLOCK) AS e
    USING (
        SELECT i.idPersona, p.usuario, p.hashClave,
               (SELECT idRol FROM dbo.Rol WHERE nombre = p.rol COLLATE Latin1_General_CI_AI) AS idRol
        FROM @people p
        JOIN @ids i ON p.personaKey = i.personaKey
        WHERE p.esEmpleado = 1
    ) src
        ON e.usuario = src.usuario COLLATE Latin1_General_CI_AI
    WHEN NOT MATCHED THEN
        INSERT(idPersona, usuario, hashClave, idRol)
        VALUES(src.idPersona, src.usuario, src.hashClave, src.idRol);

    MERGE dbo.Cliente WITH (HOLDLOCK) AS c
    USING (
        SELECT i.idPersona, p.direccion
        FROM @people p
        JOIN @ids i ON p.personaKey = i.personaKey
        WHERE p.esEmpleado = 0
    ) src
        ON c.idPersona = src.idPersona
    WHEN NOT MATCHED THEN
        INSERT(idPersona, direccion) VALUES(src.idPersona, src.direccion);

    -- Establece contexto de administrador para inserciones protegidas
    EXEC sp_set_session_context N'idEmpleado', @idAdminPersona;
    -- Parámetros del sistema
    MERGE dbo.ParametroSistema WITH (HOLDLOCK) AS p
    USING (VALUES
        (N'CARGO_REPARTO',2.00,N'Cargo fijo por entrega a domicilio',@idAdminPersona),
        (N'DESCUENTO_VALE_GAS',25.00,N'Descuento fijo con vale de gas',@idAdminPersona),
        (N'MIN_CANTIDAD_MAYORISTA_HILO',200,N'Cantidad mínima de ovillos para aplicar precio mayorista en pedidos de hilo',@idAdminPersona),
        (N'MAX_INTENTOS_FALLIDOS',3,N'Intentos fallidos antes de bloquear cuenta',@idAdminPersona),
        (N'MINUTOS_BLOQUEO_CUENTA',5,N'Duración del bloqueo de cuenta en minutos',@idAdminPersona),
        (N'ID_CLIENTE_GENERICO',@idCliDefault,N'ID del cliente genérico por defecto',@idAdminPersona)
    ) AS src(clave, valor, descripcion, idEmpleado)
        ON p.clave = src.clave
    WHEN MATCHED THEN
        UPDATE SET valor = src.valor,
                   descripcion = src.descripcion,
                   idEmpleado = src.idEmpleado
    WHEN NOT MATCHED THEN
        INSERT(clave, valor, descripcion, idEmpleado)
        VALUES(src.clave, src.valor, src.descripcion, src.idEmpleado);

    EXEC sp_set_session_context N'idEmpleado', NULL;
    COMMIT;
    EXEC dbo.sp_EnableSeedTriggers;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    EXEC dbo.sp_EnableSeedTriggers;
    EXEC sp_set_session_context N'idEmpleado', NULL;
    THROW;
END CATCH
GO
