-- Procedimientos almacenados para la base de datos de Comercial's Valerio
-- Incluye manejo de transacciones y reportes.

USE cv_ventas_distribucion;
GO

-- Clientes más frecuentes: devuelve el top N de clientes con más compras
-- Utiliza un cursor para recorrer el resultado de la vista agregada
CREATE OR ALTER PROCEDURE dbo.sp_ListarClientesFrecuentes
  @top INT = 5
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        IF @top IS NULL OR @top <= 0
            THROW 60300, '@top debe ser mayor a cero.', 1;
        DECLARE cli_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT TOP (@top) idCliente, nombre, numCompras
          FROM dbo.vw_ClientesFrecuentes
          ORDER BY numCompras DESC;

    DECLARE @id INT, @nombre NVARCHAR(120), @num INT;
    DECLARE @t TABLE(idCliente INT, nombre NVARCHAR(120), numCompras INT);

        OPEN cli_cursor;
        FETCH NEXT FROM cli_cursor INTO @id, @nombre, @num;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            INSERT INTO @t VALUES(@id, @nombre, @num);
            FETCH NEXT FROM cli_cursor INTO @id, @nombre, @num;
        END;
        CLOSE cli_cursor; DEALLOCATE cli_cursor;

        SELECT idCliente, nombre, numCompras
          FROM @t
         ORDER BY numCompras DESC;
    END TRY
    BEGIN CATCH
        IF CURSOR_STATUS('variable','cli_cursor') >= -1
        BEGIN
            CLOSE cli_cursor;
            DEALLOCATE cli_cursor;
        END;
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

-- Deshabilita los triggers que protegen la carga de datos iniciales
CREATE OR ALTER PROCEDURE dbo.sp_DisableSeedTriggers
AS
BEGIN
    SET NOCOUNT ON;
    IF OBJECT_ID('dbo.trg_Rol_AdminOnly','TR') IS NOT NULL
        DISABLE TRIGGER trg_Rol_AdminOnly ON dbo.Rol;
    IF OBJECT_ID('dbo.trg_ParametroSistema_AdminOnly','TR') IS NOT NULL
        DISABLE TRIGGER trg_ParametroSistema_AdminOnly ON dbo.ParametroSistema;
    IF OBJECT_ID('dbo.trg_Persona_ValidarEstado','TR') IS NOT NULL
        DISABLE TRIGGER trg_Persona_ValidarEstado ON dbo.Persona;
END;
GO

-- Vuelve a habilitar los triggers deshabilitados para la carga inicial
CREATE OR ALTER PROCEDURE dbo.sp_EnableSeedTriggers
AS
BEGIN
    SET NOCOUNT ON;
    IF OBJECT_ID('dbo.trg_Rol_AdminOnly','TR') IS NOT NULL
        ENABLE TRIGGER trg_Rol_AdminOnly ON dbo.Rol;
    IF OBJECT_ID('dbo.trg_ParametroSistema_AdminOnly','TR') IS NOT NULL
        ENABLE TRIGGER trg_ParametroSistema_AdminOnly ON dbo.ParametroSistema;
    IF OBJECT_ID('dbo.trg_Persona_ValidarEstado','TR') IS NOT NULL
        ENABLE TRIGGER trg_Persona_ValidarEstado ON dbo.Persona;
END;
GO

-- Procedimiento auxiliar para manejar flags de sesión usados por triggers.
-- Errores:
--   50087 - Valor inválido para @skipSubtipoCheck.
--   50088 - Valor inválido para @skipBrutoUpdate.
CREATE OR ALTER PROCEDURE dbo.sp_SetSessionFlags
  @skipSubtipoCheck BIT = NULL,
  @skipBrutoUpdate  BIT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF @skipSubtipoCheck IS NOT NULL AND @skipSubtipoCheck NOT IN (0, 1)
        THROW 50087, '@skipSubtipoCheck debe ser 0 o 1.', 1;
    IF @skipBrutoUpdate IS NOT NULL AND @skipBrutoUpdate NOT IN (0, 1)
        THROW 50088, '@skipBrutoUpdate debe ser 0 o 1.', 1;
    EXEC sp_set_session_context N'skipSubtipoCheck', @skipSubtipoCheck;
    EXEC sp_set_session_context N'skipBrutoUpdate',  @skipBrutoUpdate;
END;
GO

-- Valida que un idEstado pertenezca al módulo especificado
CREATE OR ALTER PROCEDURE dbo.sp_ValidarEstado
  @idEstado INT,
  @modulo   NVARCHAR(20),
  @error    INT = 50000
AS
BEGIN
    SET NOCOUNT ON;

    IF dbo.fn_AssertEstadoModulo(@idEstado, @modulo) = 0
    BEGIN
        DECLARE @msg NVARCHAR(60) = N'Estado inv\u00e1lido para ' + @modulo + N'.';
        THROW @error, @msg, 1;
    END;
END;
GO

-- Calcula totales de una transacción y realiza validaciones comunes
CREATE OR ALTER PROCEDURE dbo.sp_PrepararTransaccion
  @detalle       dbo.tvp_DetalleTx  READONLY,
  @pagos         dbo.tvp_PagoTx    READONLY,
  @usaValeGas    BIT              = NULL,
  @cargo         DECIMAL(10,2)    = NULL,
  @validarPagos  BIT              = 0,
  @checkEntero   BIT              = 0,
  @errorDetalle  INT,
  @errorEntero   INT              = 60003,
  @errorCargo    INT              = 60155,
  @errorPagos    INT              = 60000,
  @totalBruto    DECIMAL(10,2)    OUTPUT,
  @descuento     DECIMAL(10,2)    OUTPUT,
  @cargoCalculado DECIMAL(10,2)   OUTPUT,
  @totalPagos    DECIMAL(10,2)    OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM @detalle)
        THROW @errorDetalle, N'Detalle de transacción vacío', 1;

    IF @checkEntero = 1
       AND EXISTS (SELECT 1 FROM @detalle WHERE cantidad <> FLOOR(cantidad))
        THROW @errorEntero, N'La cantidad debe ser entera', 1;

    IF @cargo IS NOT NULL AND @cargo < 0
        THROW @errorCargo, N'@cargo no puede ser negativo', 1;

    SELECT @totalBruto = SUM(cantidad * precioUnitario)
      FROM @detalle;
    IF @totalBruto IS NULL SET @totalBruto = 0;

    IF EXISTS (SELECT 1 FROM @pagos)
        SELECT @totalPagos = COALESCE(SUM(monto), 0)
          FROM @pagos;

    IF @validarPagos = 1 AND EXISTS (SELECT 1 FROM @pagos)
       AND @totalPagos <> @totalBruto
        THROW @errorPagos, N'Suma de pagos ≠ total neto', 1;

    IF @usaValeGas IS NOT NULL OR @cargo IS NOT NULL
    BEGIN
        SET @cargoCalculado = CASE
                                WHEN @cargo IS NULL OR @cargo = 0
                                THEN dbo.fn_CargoRepartoActual()
                                ELSE @cargo
                              END;
        SET @descuento = CASE
                           WHEN @usaValeGas = 1
                           THEN dbo.fn_DescuentoValeGas()
                           ELSE 0
                         END;
    END;
    ELSE
    BEGIN
        SET @cargoCalculado = NULL;
        SET @descuento = 0;
    END;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ListarAlertasPendientes
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE alerta_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT idAlerta, idProducto, stockActual, umbral, fechaAlerta, procesada
          FROM dbo.AlertaStock
         WHERE procesada = 0
         ORDER BY fechaAlerta;

    DECLARE @idAlerta    INT,
            @idProducto  INT,
            @stock       DECIMAL(12,3),
            @umbral      DECIMAL(12,3),
            @fecha       DATETIME2,
            @procesada   BIT;

    DECLARE @result TABLE(
        idAlerta    INT,
        idProducto  INT,
        stockActual DECIMAL(12,3),
        umbral      DECIMAL(12,3),
        fechaAlerta DATETIME2,
        procesada   BIT
    );

    OPEN alerta_cursor;
    FETCH NEXT FROM alerta_cursor
        INTO @idAlerta, @idProducto, @stock, @umbral, @fecha, @procesada;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        INSERT INTO @result
            (idAlerta, idProducto, stockActual, umbral, fechaAlerta, procesada)
        VALUES (@idAlerta, @idProducto, @stock, @umbral, @fecha, @procesada);

        FETCH NEXT FROM alerta_cursor
            INTO @idAlerta, @idProducto, @stock, @umbral, @fecha, @procesada;
    END;
    CLOSE alerta_cursor;
    DEALLOCATE alerta_cursor;

    SELECT idAlerta, idProducto, stockActual, umbral, fechaAlerta, procesada
      FROM @result
     ORDER BY fechaAlerta;
END;
GO

-- Lista pedidos pendientes en estado 'En Proceso'
-- Utiliza un cursor para recorrer cada pedido pendiente.
CREATE OR ALTER PROCEDURE dbo.sp_ListarPedidosPendientes
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        DECLARE ped_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT P.idTransaccion,
               T.fecha,
               T.idEmpleado,
               T.idCliente,
               P.direccionEntrega,
               P.tipoPedido,
               P.usaValeGas
          FROM dbo.Pedido      AS P
          JOIN dbo.Transaccion AS T ON T.idTransaccion = P.idTransaccion
          JOIN dbo.Cliente     AS C ON C.idPersona     = T.idCliente
          JOIN dbo.Estado      AS E ON E.idEstado      = T.idEstado
                                    AND E.modulo = N'Transaccion'
         WHERE E.nombre = N'En Proceso'
         ORDER BY T.fecha;

    DECLARE @idTx   INT,
            @fecha DATETIME2,
            @idEmp INT,
            @idCli INT,
            @dir   NVARCHAR(120),
            @tipo  NVARCHAR(20),
            @vale  BIT;

    DECLARE @t TABLE(
        idTransaccion    INT,
        fecha            DATETIME2,
        idEmpleado       INT,
        idCliente        INT,
        direccionEntrega NVARCHAR(120),
        tipoPedido       NVARCHAR(20),
        usaValeGas       BIT
    );

        OPEN ped_cursor;
        FETCH NEXT FROM ped_cursor INTO @idTx, @fecha, @idEmp, @idCli, @dir, @tipo, @vale;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            INSERT INTO @t VALUES(@idTx, @fecha, @idEmp, @idCli, @dir, @tipo, @vale);
            FETCH NEXT FROM ped_cursor INTO @idTx, @fecha, @idEmp, @idCli, @dir, @tipo, @vale;
        END;
        CLOSE ped_cursor; DEALLOCATE ped_cursor;

        SELECT idTransaccion,
               fecha,
               idEmpleado,
               idCliente,
               direccionEntrega,
               tipoPedido,
               usaValeGas
          FROM @t
         ORDER BY fecha;
    END TRY
    BEGIN CATCH
        IF CURSOR_STATUS('variable','ped_cursor') >= -1
        BEGIN
            CLOSE ped_cursor;
            DEALLOCATE ped_cursor;
        END;
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

-- Verifica que la sesión cuente con un idEmpleado definido.
-- Errores:
--   50083 - SESSION_CONTEXT('idEmpleado') no establecido.
CREATE OR ALTER PROCEDURE dbo.sp_AssertEmpleadoContext
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  IF SESSION_CONTEXT(N'idEmpleado') IS NULL
      THROW 50083, 'SESSION_CONTEXT(''idEmpleado'') no establecido.', 1;
END;
GO

-- Valida que el actor sea administrador
-- Errores:
--   50084 - Empleado de la sesi\u00f3n inexistente.
--   50085 - Solo administradores pueden realizar esta operaci\u00f3n.
CREATE OR ALTER PROCEDURE dbo.sp_AssertAdmin
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  DECLARE @nivel INT = dbo.fn_actor_nivel();
  IF @nivel IS NULL
      THROW 50084, 'Empleado de la sesión inexistente.', 1;
  IF @nivel <> 1
      THROW 50085, 'Solo administradores pueden realizar esta operación.', 1;
END;
GO

-- Garantiza que los triggers sean ejecutados solo por un administrador.
CREATE OR ALTER PROCEDURE dbo.sp_CheckAdminTrigger
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertAdmin;
END;
GO

-- Aplica movimientos de inventario para el detalle de una transacción.
-- El tipo de movimiento determina si se suman o restan unidades.
-- Errores:
--   60500 - TipoMovimiento no válido.
CREATE OR ALTER PROCEDURE dbo.sp_AplicarAjusteInventario
  @idTransaccion   INT,
  @tipoMovimiento  NVARCHAR(20)
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  BEGIN TRY
      DECLARE @idTipo INT = (
          SELECT idTipoMovimiento
            FROM dbo.TipoMovimiento
           WHERE nombre = @tipoMovimiento);
      IF @idTipo IS NULL
          THROW 60500, 'TipoMovimiento no válido', 1;

      DECLARE @motivo NVARCHAR(80);
      IF @tipoMovimiento = N'Ajuste'
          SET @motivo = CONCAT('Ajuste Pedido ', @idTransaccion);
      ELSE IF EXISTS (SELECT 1 FROM dbo.Venta WHERE idTransaccion = @idTransaccion)
          SET @motivo = CONCAT('Venta ', @idTransaccion);
      ELSE
          SELECT @motivo = CONCAT('Pedido ', ped.tipoPedido, ' ', @idTransaccion)
            FROM dbo.Pedido ped WHERE ped.idTransaccion = @idTransaccion;

      INSERT INTO dbo.MovimientoInventario(
        idProducto,
        idTallaStock,
        idTipoMovimiento,
        cantidad,
        motivo,
        idEmpleado
      )
      SELECT dt.idProducto,
             dt.idTallaStock,
             @idTipo,
             dt.cantidad,
             @motivo,
             t.idEmpleado
        FROM dbo.DetalleTransaccion dt
        JOIN dbo.Transaccion t ON t.idTransaccion = dt.idTransaccion
       WHERE dt.idTransaccion = @idTransaccion;
      IF @@ROWCOUNT = 0
          THROW 60501, 'Fallo al registrar movimiento de inventario', 1;

      -- El disparador trg_MovInv_ValidateAndUpdate ajusta el stock
      -- de Producto y TallaStock al registrar el movimiento.
  END TRY
  BEGIN CATCH
      THROW;
  END CATCH
END;
GO

-- Descuenta del inventario los productos incluidos en una transacción.
-- Parámetros:
--   @idTransaccion - identificador de la transacción a procesar.
CREATE OR ALTER PROCEDURE dbo.sp_DescontarStock_Detalle
  @idTransaccion INT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  DECLARE @started BIT = 0;
  BEGIN TRY
    IF @@TRANCOUNT = 0
    BEGIN
      BEGIN TRAN;
      SET @started = 1;
    END;
      EXEC dbo.sp_AplicarAjusteInventario @idTransaccion, N'Salida';
    IF @started = 1 COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
END;
GO

-- Mantenimiento de tablas maestras ---------------------------------------

CREATE OR ALTER PROCEDURE dbo.sp_InsertCategoria
  @nombre      NVARCHAR(40),
  @descripcion NVARCHAR(120),
  @newIdCategoria INT OUTPUT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
      BEGIN TRAN;
      IF EXISTS (SELECT 1 FROM dbo.Categoria WHERE nombre = @nombre)
          THROW 80006, 'Nombre de categoría duplicado.', 1;
      INSERT INTO dbo.Categoria(nombre, descripcion, idEstado)
      VALUES(@nombre, @descripcion,
             dbo.fn_estado(N'Categoria', N'Activo'));
      IF @@ROWCOUNT = 0
          THROW 80007, 'Fallo al insertar categoría', 1;
      SET @newIdCategoria = SCOPE_IDENTITY();
    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
END;
GO

-- Actualiza el nombre y la descripción de una categoría.
-- Parámetros:
--   @idCategoria - identificador de la categoría a modificar.
--   @nombre - nuevo nombre de la categoría.
--   @descripcion - texto descriptivo opcional.
CREATE OR ALTER PROCEDURE dbo.sp_UpdateCategoria
  @idCategoria INT,
  @nombre      NVARCHAR(40),
  @descripcion NVARCHAR(120)
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
    BEGIN TRAN;
      IF EXISTS (SELECT 1 FROM dbo.Categoria
                 WHERE nombre = @nombre AND idCategoria <> @idCategoria)
          THROW 80006, 'Nombre de categoría duplicado.', 1;
      UPDATE dbo.Categoria
         SET nombre = @nombre,
             descripcion = @descripcion
       WHERE idCategoria = @idCategoria;
      IF @@ROWCOUNT = 0
          THROW 80001, 'Categoria inexistente', 1;
    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
END;
GO

-- Elimina una categoría si no está asociada a productos.
-- Parámetros:
--   @idCategoria - identificador de la categoría a borrar.
CREATE OR ALTER PROCEDURE dbo.sp_DeleteCategoria
  @idCategoria INT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
    BEGIN TRAN;
      EXEC dbo.sp_AssertAdmin;
      IF EXISTS(SELECT 1 FROM dbo.Producto WHERE idCategoria=@idCategoria)
          THROW 80002, 'Categoria con productos asociados', 1;
      DELETE FROM dbo.Categoria WHERE idCategoria=@idCategoria;
      IF @@ROWCOUNT = 0
          THROW 80003, 'Categoria inexistente', 1;
    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
END;
GO

-- Cambia el estado de una categoría y opcionalmente actualiza sus productos.
-- Parámetros:
--   @idCategoria - categoría a modificar.
--   @nuevoEstado - nombre del nuevo estado.
--   @actualizarProductos - si es 1, actualiza el estado de los productos.
--   @numProductos - cantidad de productos afectados.
CREATE OR ALTER PROCEDURE dbo.sp_CambiarEstadoCategoria
  @idCategoria        INT,
  @nuevoEstado        NVARCHAR(30),
  @actualizarProductos BIT = 0,
  @numProductos       INT OUTPUT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
    BEGIN TRAN;
      DECLARE @idNuevo INT,
              @idActivo INT,
              @idDesactivado INT;
      SELECT @idNuevo = dbo.fn_estado(N'Categoria', @nuevoEstado),
             @idActivo = dbo.fn_estado(N'Producto', N'Activo'),
             @idDesactivado = dbo.fn_estado(N'Producto', N'Desactivado');

      IF @idNuevo IS NULL
          THROW 80004, 'Estado no válido', 1;

      UPDATE dbo.Categoria
         SET idEstado = @idNuevo
       WHERE idCategoria=@idCategoria;
      IF @@ROWCOUNT = 0
          THROW 80005, 'Categoria inexistente', 1;

      SET @numProductos = 0;

      IF @nuevoEstado = N'Inactivo'
      BEGIN
          UPDATE dbo.Producto
             SET idEstado = @idDesactivado
           WHERE idCategoria=@idCategoria;
          SET @numProductos = @@ROWCOUNT;
      END;
      ELSE IF @actualizarProductos = 1
      BEGIN
          UPDATE dbo.Producto
             SET idEstado = @idActivo
           WHERE idCategoria=@idCategoria;
          SET @numProductos = @@ROWCOUNT;
      END;
    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
END;
GO

-- Venta ---------------------------------------------------------------
-- Registra una venta directa sin aplicar descuentos ni cargos
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarVenta
  @idEmpleado      INT,
  @idCliente       INT,
  @observacion     NVARCHAR(120)      = NULL,
  @detalle         dbo.tvp_DetalleTx  READONLY,
  @pagos           dbo.tvp_PagoTx     READONLY,
  @idTransaccion   INT OUTPUT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  EXEC dbo.sp_SetSessionFlags 1, 1;
  BEGIN TRY
    BEGIN TRAN;

    IF @idCliente IS NULL
       THROW 60002, 'Cliente obligatorio', 1;

    -- En ventas directas no se aplican descuentos ni cargos
    DECLARE @totalBruto DECIMAL(10,2),
            @totalPagos DECIMAL(10,2),
            @tmpDesc   DECIMAL(10,2),
            @tmpCargo  DECIMAL(10,2);

    EXEC dbo.sp_PrepararTransaccion
        @detalle        = @detalle,
        @pagos          = @pagos,
        @validarPagos   = 1,
        @errorDetalle   = 60001,
        @errorPagos     = 60000,
        @totalBruto     = @totalBruto OUTPUT,
        @descuento      = @tmpDesc OUTPUT,
        @cargoCalculado = @tmpCargo OUTPUT,
        @totalPagos     = @totalPagos OUTPUT;

  -- 2) Insertar Transaccion en estado 'En Proceso' usando
  --    dbo.fn_estado('Transaccion','En Proceso') como valor por defecto
  INSERT dbo.Transaccion(
    fecha, idEmpleado, idCliente, observacion, totalBruto, descuento, cargo )
  VALUES (
    SYSDATETIME(),
    @idEmpleado, @idCliente, @observacion,
    @totalBruto, 0, 0
  );
  IF @@ROWCOUNT = 0
      THROW 60004, 'Fallo al insertar transacción', 1;

  SELECT @idTransaccion = SCOPE_IDENTITY();

  -- 3) Detalle
  INSERT dbo.DetalleTransaccion( idTransaccion, idProducto, idTallaStock, cantidad, precioUnitario )
  SELECT @idTransaccion, idProducto, idTallaStock, cantidad, precioUnitario
  FROM @detalle;
  IF @@ROWCOUNT = 0
      THROW 60005, 'Fallo al insertar detalle de transacción', 1;

  -- 4) Pagos
  INSERT dbo.PagoTransaccion(
    idTransaccion, idMetodoPago, monto
  )
  SELECT
    @idTransaccion, idMetodoPago, monto
  FROM @pagos;
  IF @@ROWCOUNT = 0
      THROW 60006, 'Fallo al registrar pagos', 1;

  -- 5) Insertar Venta (cierra transacción)
  INSERT INTO dbo.Venta(idTransaccion)
  VALUES(@idTransaccion);
  IF @@ROWCOUNT = 0
      THROW 60007, 'Fallo al insertar venta', 1;
  EXEC dbo.sp_DescontarStock_Detalle @idTransaccion;
  COMMIT;
  EXEC dbo.sp_SetSessionFlags NULL, NULL;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    EXEC dbo.sp_SetSessionFlags NULL, NULL;
    THROW;
  END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RegistrarPedido
  @idEmpleado       INT,
  @idCliente        INT,
  @observacion      NVARCHAR(120)      = NULL,
  @direccionEntrega NVARCHAR(120),
  @usaValeGas       BIT,
  @cargo            DECIMAL(10,2),
  @detalle          dbo.tvp_DetalleTx  READONLY,
  @idTransaccion    INT OUTPUT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  EXEC dbo.sp_SetSessionFlags 1, 1;
  BEGIN TRY
    BEGIN TRAN;

    IF @idCliente IS NULL
       THROW 60002, 'Cliente obligatorio', 1;

      DECLARE @bruto      DECIMAL(10,2),
              @cargoCalc  DECIMAL(10,2),
              @descCalc   DECIMAL(10,2),
              @tmpPagos   DECIMAL(10,2),
              @pagosVacios dbo.tvp_PagoTx;

      EXEC dbo.sp_PrepararTransaccion
          @detalle       = @detalle,
          @pagos         = @pagosVacios,
          @usaValeGas    = @usaValeGas,
        @cargo         = @cargo,
        @checkEntero   = 1,
        @errorDetalle  = 60011,
        @errorEntero   = 60003,
        @errorCargo    = 60155,
        @totalBruto    = @bruto OUTPUT,
        @descuento     = @descCalc OUTPUT,
        @cargoCalculado= @cargoCalc OUTPUT,
        @totalPagos    = @tmpPagos OUTPUT;

    -- 1) Insertar Transaccion en estado 'En Proceso' utilizando
    --    dbo.fn_estado('Transaccion','En Proceso') y guardando la observación
    INSERT INTO dbo.Transaccion(
      idEmpleado,
      idCliente,
      observacion,
      fecha,
      totalBruto,
      descuento,
      cargo,
      idEstado
    )
    VALUES(
      @idEmpleado,
      @idCliente,
      @observacion,
      SYSDATETIME(),
      @bruto,
      @descCalc,
      @cargoCalc,
      dbo.fn_estado(N'Transaccion', N'En Proceso')
    );
    IF @@ROWCOUNT = 0
        THROW 60113, 'Fallo al insertar transacción', 1;

    SET @idTransaccion = SCOPE_IDENTITY();

    DECLARE @computedTipo NVARCHAR(20) =
        dbo.fn_ClasificarPedido(@detalle);

    -- 2) Insertar Pedido
    INSERT INTO dbo.Pedido(
      idTransaccion,
      direccionEntrega,
      tipoPedido,
      usaValeGas
   )
   VALUES(
      @idTransaccion,
      @direccionEntrega,
      @computedTipo,
      @usaValeGas
   );
    IF @@ROWCOUNT = 0
        THROW 60114, 'Fallo al insertar pedido', 1;

    -- 3) DetalleTransaccion y descuento inmediato de stock
    INSERT INTO dbo.DetalleTransaccion(
      idTransaccion,
      idProducto,
      idTallaStock,
      cantidad,
      precioUnitario
    )
    SELECT
      @idTransaccion,
      d.idProducto,
      d.idTallaStock,
      d.cantidad,
      d.precioUnitario
    FROM @detalle AS d;
    IF @@ROWCOUNT = 0
        THROW 60115, 'Fallo al insertar detalle de pedido', 1;
    -- rebaja de inventario solo aplica a pedidos a domicilio
    IF @computedTipo = N'Domicilio'
        EXEC dbo.sp_DescontarStock_Detalle @idTransaccion;
  COMMIT;
  EXEC dbo.sp_SetSessionFlags NULL, NULL;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    EXEC dbo.sp_SetSessionFlags NULL, NULL;
    THROW;
  END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ModificarPedido
  @idTransaccion    INT,
  @observacion      NVARCHAR(120)      = NULL,
  @direccionEntrega NVARCHAR(120),
  @usaValeGas       BIT,
  @cargo            DECIMAL(10,2),
  @detalle          dbo.tvp_DetalleTx  READONLY
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  EXEC dbo.sp_SetSessionFlags NULL, 1;
  BEGIN TRY
    BEGIN TRAN;

    -- tipo y empleado actuales de la transacción
    DECLARE @prevTipo    NVARCHAR(20) = (SELECT tipoPedido FROM dbo.Pedido WHERE idTransaccion = @idTransaccion);
    IF @prevTipo IS NULL
        THROW 60110, 'Pedido inexistente', 1;
    DECLARE @idEmpleado  INT = (SELECT idEmpleado FROM dbo.Transaccion WHERE idTransaccion = @idTransaccion);

    DECLARE @idEst INT;
    SELECT @idEst = e.idEstado
    FROM   dbo.Transaccion t
    JOIN   dbo.Estado e ON e.idEstado = t.idEstado
    WHERE  t.idTransaccion = @idTransaccion
      AND e.modulo = N'Transaccion'
      AND e.nombre = N'En Proceso';
    IF @idEst IS NULL
        THROW 60111, N'Solo pedidos en proceso pueden modificarse', 1;

      DECLARE @bruto      DECIMAL(10,2),
              @cargoCalc  DECIMAL(10,2),
              @descCalc   DECIMAL(10,2),
              @tmpPagos   DECIMAL(10,2),
              @pagosVacios dbo.tvp_PagoTx;

      EXEC dbo.sp_PrepararTransaccion
          @detalle       = @detalle,
          @pagos         = @pagosVacios,
          @usaValeGas    = @usaValeGas,
        @cargo         = @cargo,
        @checkEntero   = 1,
        @errorDetalle  = 60112,
        @errorEntero   = 60003,
        @errorCargo    = 60155,
        @totalBruto    = @bruto OUTPUT,
        @descuento     = @descCalc OUTPUT,
        @cargoCalculado= @cargoCalc OUTPUT,
        @totalPagos    = @tmpPagos OUTPUT;

    UPDATE dbo.Transaccion
       SET observacion = @observacion,
           totalBruto  = @bruto,
           descuento   = @descCalc,
           cargo       = @cargoCalc
    WHERE idTransaccion = @idTransaccion;

    DECLARE @newTipo NVARCHAR(20) =
        dbo.fn_ClasificarPedido(@detalle);

    UPDATE dbo.Pedido
       SET direccionEntrega = @direccionEntrega,
           tipoPedido       = @newTipo,
           usaValeGas       = @usaValeGas
     WHERE idTransaccion = @idTransaccion;

    -- revertir stock actual si el pedido anterior era a domicilio
    IF @prevTipo = N'Domicilio'
        EXEC dbo.sp_AplicarAjusteInventario @idTransaccion, N'Cancelación';

    -- sincronizar el detalle con las nuevas líneas
    MERGE dbo.DetalleTransaccion WITH (HOLDLOCK) AS tgt
    USING @detalle AS src
       ON tgt.idTransaccion = @idTransaccion
      AND tgt.idProducto    = src.idProducto
      AND ISNULL(tgt.idTallaStock, -1) = ISNULL(src.idTallaStock, -1)
    WHEN MATCHED THEN
        UPDATE SET cantidad       = src.cantidad,
                   precioUnitario = src.precioUnitario
    WHEN NOT MATCHED BY TARGET THEN
        INSERT (idTransaccion, idProducto, idTallaStock, cantidad, precioUnitario)
        VALUES (@idTransaccion, src.idProducto, src.idTallaStock, src.cantidad, src.precioUnitario)
    WHEN NOT MATCHED BY SOURCE AND tgt.idTransaccion = @idTransaccion THEN
        DELETE;
    -- descontar inventario nuevamente si continúa siendo a domicilio
    IF @newTipo = N'Domicilio'
        EXEC dbo.sp_DescontarStock_Detalle @idTransaccion;

    COMMIT;
    EXEC dbo.sp_SetSessionFlags NULL, NULL;
  END TRY
  BEGIN CATCH
    EXEC dbo.sp_SetSessionFlags NULL, NULL;
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
END;
GO

-- Cancela una venta existente indicando el motivo.
-- Parámetros:
--   @idTransaccion - identificador de la venta.
--   @motivoCancelacion - texto que explica la razón.
CREATE OR ALTER PROCEDURE dbo.sp_CancelarVenta
    @idTransaccion      INT,
    @motivoCancelacion NVARCHAR(120)
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
    BEGIN TRAN;
      IF NOT EXISTS (SELECT 1 FROM dbo.Venta WHERE idTransaccion = @idTransaccion)
          THROW 60021, 'Transacción no es una venta', 1;
      UPDATE dbo.Transaccion
         SET idEstado           = dbo.fn_estado(N'Transaccion', N'Cancelada'),
             motivoCancelacion  = @motivoCancelacion
       WHERE idTransaccion = @idTransaccion;
      IF @@ROWCOUNT = 0 THROW 60020, 'Transaccion inexistente', 1;
    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
  -- Los triggers definidos en el DDL (e.g. trg_Transaccion_Update)
  -- se encargarán de revertir stock y cargos automáticamente.
END;
GO

-- Actualiza el estado de un pedido y registra información adicional.
-- Parámetros:
--   @idTransaccion - identificador del pedido.
--   @nuevoEstado - nombre del nuevo estado.
--   @comentario - observaciones opcionales.
--   @fechaHoraEntrega - fecha de entrega si aplica.
--   @idEmpleadoEntrega - repartidor que entrega el pedido.
CREATE OR ALTER PROCEDURE dbo.sp_ActualizarEstadoPedido
    @idTransaccion       INT,
    @nuevoEstado         NVARCHAR(20),
    @comentario          NVARCHAR(120) = NULL,
    @fechaHoraEntrega    DATETIME2 = NULL,
    @idEmpleadoEntrega   INT       = NULL
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
    BEGIN TRAN;
      DECLARE @idEst INT = dbo.fn_estado(N'Transaccion', @nuevoEstado);
      IF @idEst IS NULL
          THROW 60101, 'Estado no válido', 1;

      IF NOT EXISTS (SELECT 1 FROM dbo.Pedido WHERE idTransaccion = @idTransaccion)
          THROW 60102, 'Pedido inexistente', 1;

      DECLARE @empleadoEntrega INT = (
              SELECT COALESCE(
                         @idEmpleadoEntrega,
                         TRY_CONVERT(INT, SESSION_CONTEXT(N'idEmpleado')),
                         idEmpleadoEntrega)
              FROM dbo.Pedido
              WHERE idTransaccion = @idTransaccion);

      IF @nuevoEstado = N'Entregada' AND @empleadoEntrega IS NULL
          THROW 60103, 'Empleado que entrega obligatorio', 1;

      UPDATE dbo.Transaccion
         SET idEstado = @idEst
       WHERE idTransaccion = @idTransaccion;

      -- Si se canceló el pedido registrar el motivo de cancelación
      IF @nuevoEstado = N'Cancelada'
          UPDATE dbo.Transaccion
             SET motivoCancelacion = @comentario
           WHERE idTransaccion = @idTransaccion;

      UPDATE dbo.Pedido
         SET comentarioCancelacion = @comentario,
             fechaHoraEntrega   = CASE WHEN @nuevoEstado = N'Entregada'
                                       THEN ISNULL(@fechaHoraEntrega, SYSDATETIME())
                                       ELSE fechaHoraEntrega END,
            idEmpleadoEntrega  = CASE WHEN @nuevoEstado = N'Entregada'
                                       THEN COALESCE(
                                            @idEmpleadoEntrega,
                                            dbo.fn_actor_id(),
                                            idEmpleadoEntrega
                                       )
                                       ELSE idEmpleadoEntrega END
       WHERE idTransaccion = @idTransaccion;

      IF @nuevoEstado = N'Entregada'
          UPDATE dbo.OrdenCompra
             SET fechaCumplida = ISNULL(@fechaHoraEntrega, SYSDATETIME())
           WHERE idPedido = @idTransaccion;
    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
  -- Los triggers en el DDL se encargarán de ajustar inventario o procesos de entrega.
END;
GO

-- Diario
CREATE OR ALTER PROCEDURE dbo.sp_GenerarReporteDiario
    @fecha DATE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;

    BEGIN TRY
        IF @fecha IS NULL
            THROW 60152, 'Fecha inválida', 1;
        SELECT
            Dia                         AS Fecha,
            NumTransacciones            AS NumVentas,
            NumPedidosEntregados,
            TotalBrutoDia,
            TotalNetoDia,
            MontoEfectivo,
            MontoBilleteraDigital
        FROM dbo.vw_TransaccionesPorDia
        WHERE Dia = @fecha;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

-- Mensual
CREATE OR ALTER PROCEDURE dbo.sp_GenerarReporteMensual
    @anio       INT,
    @mes        INT,
    @conResumen BIT = 1
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;

    BEGIN TRY
        IF @mes IS NULL OR @mes < 1 OR @mes > 12
            THROW 60150, 'Mes inválido', 1;
        IF @anio IS NULL OR @anio <= 0
            THROW 60151, 'Año inválido', 1;
        IF @conResumen NOT IN (0, 1)
            THROW 60153, '@conResumen debe ser 0 o 1.', 1;

        DECLARE @desde DATETIME2 = DATEFROMPARTS(@anio, @mes, 1);
        DECLARE @hasta DATETIME2 = DATEADD(DAY, 1, EOMONTH(@desde));

        SELECT
            T.fechaDia           AS Dia,
            COUNT(*)             AS NumTransacciones,
            SUM(T.totalNeto)     AS IngresosDia
        FROM dbo.Transaccion AS T
        JOIN dbo.Estado AS E ON E.idEstado = T.idEstado
                            AND E.modulo = N'Transaccion'
        WHERE E.nombre IN (N'Completada', N'Entregada')
          AND T.fecha >= @desde AND T.fecha < @hasta
        GROUP BY T.fechaDia
        ORDER BY Dia;

        SELECT Categoria,
               NumTransacciones,
               IngresosCategoria
        FROM dbo.vw_ReporteMensualCategoria
        WHERE Anio = @anio AND Mes = @mes;

        IF @conResumen = 1
        BEGIN
            SELECT NumTransMinorista,
                   MontoMinorista,
                   NumTransEspecial,
                   MontoEspecial,
                   NumPedidosDomicilio,
                   MontoPedidosDomicilio
            FROM dbo.vw_ResumenMensualModalidad
            WHERE Anio = @anio AND Mes = @mes;
        END;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

-- Reporte de rotación de productos
-- Errores:
--   60160 - @hasta debe ser mayor que @desde.
--   60161 - @top debe ser mayor a cero.
CREATE OR ALTER PROCEDURE dbo.sp_GenerarReporteRotacion
    @desde DATETIME2,
    @hasta DATETIME2,
    @top   INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;

    IF @desde IS NULL OR @hasta IS NULL OR @hasta <= @desde
        THROW 60160, '@hasta debe ser mayor que @desde.', 1;
    IF @top IS NOT NULL AND @top <= 0
        THROW 60161, '@top debe ser mayor a cero.', 1;

    ;WITH Ranked AS (
        SELECT
            ROW_NUMBER() OVER(ORDER BY SUM(dt.cantidad) DESC) AS Posicion,
            p.idProducto,
            p.nombre                        AS [Producto],
            c.nombre                        AS [Categoria],
            SUM(dt.cantidad)                AS TotalUnidadesVendidas,
            SUM(dt.cantidad * dt.precioUnitario) AS ImporteTotal
        FROM dbo.DetalleTransaccion AS dt
        JOIN dbo.Transaccion        AS t ON t.idTransaccion = dt.idTransaccion
        JOIN dbo.Estado             AS e ON e.idEstado = t.idEstado
                                          AND e.modulo = N'Transaccion'
                                          AND e.nombre IN (N'Completada', N'Entregada')
        JOIN dbo.Producto           AS p ON p.idProducto = dt.idProducto
        JOIN dbo.Categoria          AS c ON c.idCategoria = p.idCategoria
        WHERE t.fecha >= @desde AND t.fecha < @hasta
        GROUP BY p.idProducto, p.nombre, c.nombre
    )
    SELECT Posicion, idProducto, [Producto], [Categoria],
           TotalUnidadesVendidas, ImporteTotal
    FROM Ranked
    WHERE @top IS NULL OR Posicion <= @top
    ORDER BY Posicion;
END;
GO

-- Verifica la existencia de una persona y obtiene datos básicos.
-- Parámetros:
--   @dni - documento de identidad a buscar.
--   @telefono - número de teléfono encontrado.
--   @idEstado - estado actual de la persona.
CREATE OR ALTER PROCEDURE dbo.sp_ValidarPersonaBasica
  @dni      CHAR(8),
  @telefono NVARCHAR(15) OUTPUT,
  @idEstado INT OUTPUT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;

  IF EXISTS (SELECT 1 FROM dbo.Persona WHERE dni = @dni)
  BEGIN
      DECLARE @msg NVARCHAR(80) = 'Ya existe una persona con DNI ' + @dni;
      THROW 50000, @msg, 1;
  END;

  IF dbo.fn_EsDniValido(@dni) = 0
      THROW 50004, 'DNI invalido', 1;

  SET @telefono = dbo.fn_NormalizarTelefono(@telefono);

  IF @telefono IS NOT NULL AND dbo.fn_EsTelefonoValido(@telefono) = 0
      THROW 50003, N'Tel\u00e9fono inv\u00e1lido', 1;

  IF @idEstado IS NULL
  BEGIN
      SELECT @idEstado = dbo.fn_estado(N'Persona', N'Activo');
      IF @idEstado IS NULL
          THROW 50001, 'No existe estado "Activo" para Persona', 1;
  END;
  ELSE IF NOT EXISTS (
      SELECT 1 FROM dbo.Estado
       WHERE idEstado = @idEstado
         AND modulo   = N'Persona'
  )
      THROW 50002, 'Estado inválido para Persona', 1;
END;
GO

-- Registra un nuevo cliente en el sistema.
-- Parámetros:
--   @nombres - nombres del cliente.
--   @apellidos - apellidos del cliente.
--   @dni - documento nacional de identidad.
--   @telefono - contacto opcional.
--   @direccion - dirección de referencia.
--   @idEstado - estado inicial (Persona).
--   @newIdPersona - id generado para el nuevo registro.
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarCliente
    @nombres       NVARCHAR(60),
    @apellidos     NVARCHAR(60),
    @dni           CHAR(8),
    @telefono      NVARCHAR(15)    = NULL,
    @direccion     NVARCHAR(120),
    @idEstado      INT             = NULL,       -- FK a dbo.Estado(modulo='Persona')
    @newIdPersona  INT            OUTPUT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
    BEGIN TRAN;

    EXEC dbo.sp_ValidarPersonaBasica
      @dni,
      @telefono OUTPUT,
      @idEstado OUTPUT;

    -- 1.6) Dirección obligatoria
    IF @direccion IS NULL OR LTRIM(RTRIM(@direccion)) = '' OR LEN(@direccion) > 120
        THROW 50005, 'Direcci\u00f3n obligatoria', 1;

  -- 3) Insertar en Persona
  INSERT INTO dbo.Persona
    (nombres, apellidos, dni, telefono, fechaRegistro, idEstado)
  VALUES
    (dbo.fn_Capitalizar(dbo.fn_NormalizarEspacios(@nombres)),
     dbo.fn_Capitalizar(dbo.fn_NormalizarEspacios(@apellidos)),
     @dni, @telefono, SYSDATETIME(), @idEstado);
  IF @@ROWCOUNT = 0
      THROW 50027, 'Fallo al insertar persona', 1;

  SET @newIdPersona = SCOPE_IDENTITY();

  -- 4) Insertar en Cliente
  INSERT INTO dbo.Cliente(idPersona, direccion)
  VALUES(@newIdPersona, dbo.fn_NormalizarEspacios(@direccion));
  IF @@ROWCOUNT = 0
      THROW 50028, 'Fallo al insertar cliente', 1;

    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
END;
GO

-- Registra un nuevo empleado
-- Errores:
--   50086 - Jerarquía insuficiente para registrar este empleado.

CREATE OR ALTER PROCEDURE dbo.sp_RegistrarEmpleado
  @nombres        NVARCHAR(60),
  @apellidos      NVARCHAR(60),
  @dni            CHAR(8),
  @telefono       NVARCHAR(15),
  @fechaRegistro  DATE = NULL,
  @idEstado       INT,
  @usuario        NVARCHAR(30),
  @hashClave      NVARCHAR(120),
  @idRol          INT,
  @newIdPersona   INT OUTPUT
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
    SET @fechaRegistro = ISNULL(@fechaRegistro, CAST(SYSDATETIME() AS DATE));
    BEGIN TRAN;

    EXEC dbo.sp_ValidarPersonaBasica
      @dni,
      @telefono OUTPUT,
      @idEstado OUTPUT;

    -- 3) Usuario único
    IF EXISTS (
      SELECT 1 FROM dbo.Empleado e
       WHERE e.usuario = @usuario
    )
      THROW 50025, 'Ya existe un empleado con ese usuario', 1;

    -- 4) Rol existente
    IF NOT EXISTS (
      SELECT 1 FROM dbo.Rol r WHERE r.idRol = @idRol
    )
      THROW 50026, 'Rol inexistente', 1;

    -- 4.1) Jerarquía: actor debe ser superior
    DECLARE @nivelActor  INT  = dbo.fn_actor_nivel();
    IF @nivelActor IS NULL
    BEGIN
        IF SESSION_CONTEXT(N'idEmpleado') IS NULL
            THROW 50083, 'SESSION_CONTEXT(''idEmpleado'') no establecido.', 1;
        ELSE
            THROW 50084, 'Empleado de la sesión inexistente.', 1;
    END;
    DECLARE @nivelNuevo  INT  = (SELECT nivel FROM dbo.Rol WHERE idRol = @idRol);
    IF @nivelActor >= @nivelNuevo
        THROW 50086, 'Jerarquía insuficiente para registrar este empleado', 1;

  -- 5) Inserción en Persona
  INSERT INTO dbo.Persona
    (nombres, apellidos, dni, telefono, fechaRegistro, idEstado)
  VALUES
    (dbo.fn_Capitalizar(dbo.fn_NormalizarEspacios(@nombres)),
     dbo.fn_Capitalizar(dbo.fn_NormalizarEspacios(@apellidos)),
     @dni, @telefono, @fechaRegistro, @idEstado);
  IF @@ROWCOUNT = 0
      THROW 50027, 'Fallo al insertar persona', 1;

  SET @newIdPersona = SCOPE_IDENTITY();

  -- 6) Inserción en Empleado
  INSERT INTO dbo.Empleado
    (idPersona, usuario, hashClave, idRol)
  VALUES
    (@newIdPersona, @usuario, @hashClave, @idRol);
  IF @@ROWCOUNT = 0
      THROW 50029, 'Fallo al insertar empleado', 1;

    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH

END;
GO
-- Agrega pagos a una transacción existente.
-- Parámetros:
--   @idTransaccion - identificador de la transacción.
--   @pagos - conjunto de pagos a insertar.
CREATE OR ALTER PROCEDURE dbo.sp_AgregarPagosTransaccion
  @idTransaccion INT,
  @pagos         dbo.tvp_PagoTx READONLY
AS
BEGIN
  SET NOCOUNT ON;
  SET XACT_ABORT ON;
  EXEC dbo.sp_AssertEmpleadoContext;
  BEGIN TRY
    BEGIN TRAN;

    -- Validaciones
    IF NOT EXISTS (SELECT 1 FROM dbo.Transaccion WHERE idTransaccion = @idTransaccion)
        THROW 60201, 'Transaccion inexistente', 1;

    DECLARE @numPagos  INT,
            @sumaPagos DECIMAL(12,2),
            @minPago   DECIMAL(12,2),
            @dupMetodo BIT;

    SELECT @numPagos  = COUNT(*),
           @sumaPagos = COALESCE(SUM(monto), 0),
           @minPago   = MIN(monto)
      FROM @pagos;

    SET @dupMetodo = CASE WHEN EXISTS (
        SELECT 1 FROM @pagos GROUP BY idMetodoPago HAVING COUNT(*) > 1
    ) THEN 1 ELSE 0 END;

    IF @numPagos = 0
        THROW 60202, 'Lista de pagos vac\u00eda', 1;

    IF @minPago <= 0
        THROW 60203, 'Monto de pago debe ser positivo', 1;

    IF @dupMetodo = 1
        THROW 60204, 'M\u00e9todo de pago duplicado', 1;

    DECLARE @prevTotal DECIMAL(12,2) =
        dbo.fn_TotalPagosTransaccion(@idTransaccion);

    DECLARE @insertTotal DECIMAL(12,2) = @sumaPagos;
    DECLARE @nuevoTotal DECIMAL(12,2) = @prevTotal + ISNULL(@insertTotal,0);
    DECLARE @totalNeto  DECIMAL(10,2) = (
        SELECT t.totalNeto
          FROM dbo.Transaccion t
         WHERE t.idTransaccion = @idTransaccion
    );

    IF @nuevoTotal > @totalNeto
    BEGIN
        THROW 60205, 'Pagos superan total neto', 1;
    END;

  INSERT INTO dbo.PagoTransaccion(
    idTransaccion,
    idMetodoPago,
    monto
  )
  SELECT
    @idTransaccion,
    p.idMetodoPago,
    p.monto
  FROM @pagos AS p;
  IF @@ROWCOUNT = 0
      THROW 60206, 'No se insertó ningún pago', 1;

    COMMIT;
  END TRY
  BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK;
    THROW;
  END CATCH
END;
GO
-- Recalcula el stock actual de cada producto sumando sus tallas y
-- genera alertas cuando queda por debajo del umbral.
-- Útil después de cargas masivas o ajustes manuales de inventario.
-- Ejemplo de uso:
--    EXEC dbo.sp_RecalcularStockProductos;
CREATE OR ALTER PROCEDURE dbo.sp_RecalcularStockProductos
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY

        BEGIN TRAN;

        SELECT idProducto,
               SUM(stock) AS totalStock
          INTO #stockPorProducto
          FROM dbo.TallaStock
      GROUP BY idProducto;

        SELECT p.idProducto,
               COALESCE(ts.totalStock, p.stockActual) AS stock,
               p.umbral,
               p.ignorarUmbralHastaCero AS ignorar
        INTO #tmp
        FROM dbo.Producto AS p
        LEFT JOIN #stockPorProducto AS ts ON ts.idProducto = p.idProducto;

        DECLARE prod_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT idProducto, stock, umbral, ignorar FROM #tmp;

        DECLARE @id INT;
        DECLARE @stock  DECIMAL(12,3);
        DECLARE @umbral DECIMAL(12,3);
        DECLARE @ignorar BIT;
        DECLARE @idInUmbral INT = dbo.fn_estado(N'Producto', N'Inactivo por umbral');
        DECLARE @idActivo  INT = dbo.fn_estado(N'Producto', N'Activo');
        IF @idInUmbral IS NULL
            THROW 62000, N'Estado "Inactivo por umbral" no encontrado.', 1;

        IF EXISTS (SELECT 1 FROM #tmp)
        BEGIN
            OPEN prod_cursor;
            FETCH NEXT FROM prod_cursor INTO @id, @stock, @umbral, @ignorar;
            WHILE @@FETCH_STATUS = 0
            BEGIN
                UPDATE dbo.Producto SET stockActual = @stock WHERE idProducto = @id;

                IF @stock < @umbral AND @ignorar = 0
                BEGIN
                    UPDATE dbo.AlertaStock
                       SET stockActual = @stock,
                           umbral = @umbral,
                           fechaAlerta = SYSDATETIME()
                     WHERE idProducto = @id AND procesada = 0;

                    IF @@ROWCOUNT = 0
                        INSERT INTO dbo.AlertaStock(idProducto, stockActual, umbral)
                        VALUES(@id, @stock, @umbral);
                    -- Se omite la verificación de @@ROWCOUNT para evitar falsos errores

                    UPDATE dbo.Producto SET idEstado = @idInUmbral WHERE idProducto = @id;
                END;
                ELSE IF @stock >= @umbral
                    UPDATE dbo.Producto
                       SET idEstado = @idActivo
                     WHERE idProducto = @id
                       AND idEstado = @idInUmbral;

                FETCH NEXT FROM prod_cursor INTO @id, @stock, @umbral, @ignorar;
            END;
        END;

        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH

    IF CURSOR_STATUS('variable','prod_cursor') >= -1
    BEGIN
        CLOSE prod_cursor;
        DEALLOCATE prod_cursor;
    END;
    DROP TABLE IF EXISTS #tmp;
    DROP TABLE IF EXISTS #stockPorProducto;
END;
GO

-- Elimina registros antiguos de la bitácora de logins
-- Errores:
--   63000 - @maxFecha obligatorio.
--   63001 - @maxFecha no puede estar en el futuro.
CREATE OR ALTER PROCEDURE dbo.sp_DepurarBitacoraLogin
  @maxFecha    DATETIME2,
  @rowsDeleted INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        IF @maxFecha IS NULL
            THROW 63000, '@maxFecha obligatorio.', 1;
        IF @maxFecha > SYSDATETIME()
            THROW 63001, '@maxFecha no puede estar en el futuro.', 1;
        BEGIN TRAN;
        SET @rowsDeleted = 0;
        DECLARE bit_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT idBitacora FROM dbo.BitacoraLogin WHERE fechaEvento < @maxFecha;

    DECLARE @id INT;

        OPEN bit_cursor;
        FETCH NEXT FROM bit_cursor INTO @id;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            DELETE FROM dbo.BitacoraLogin WHERE idBitacora = @id;
            SET @rowsDeleted = @rowsDeleted + @@ROWCOUNT;
            FETCH NEXT FROM bit_cursor INTO @id;
        END;
        CLOSE bit_cursor;
        DEALLOCATE bit_cursor;
        COMMIT;
    END TRY
    BEGIN CATCH
        IF CURSOR_STATUS('variable','bit_cursor') >= -1
        BEGIN
            CLOSE bit_cursor;
            DEALLOCATE bit_cursor;
        END;
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO
