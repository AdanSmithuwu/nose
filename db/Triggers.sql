-- Triggers para la base de datos de Comercial's Valerio
-- Aplican reglas de negocio y consistencia de datos.

USE cv_ventas_distribucion;
GO

/* Validaciones de estado y ajustes de inventario en Transaccion */
CREATE OR ALTER TRIGGER trg_Transaccion_Update
ON dbo.Transaccion
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted)
        RETURN;

    DECLARE @ins TABLE (idTransaccion INT, estado NVARCHAR(20));
    INSERT INTO @ins(idTransaccion, estado)
    SELECT i.idTransaccion, e.nombre
      FROM inserted i
      JOIN dbo.Estado e ON e.idEstado = i.idEstado
                       AND e.modulo = N'Transaccion';

    DECLARE @totales TABLE (
        idTransaccion INT PRIMARY KEY,
        total         DECIMAL(12,2)
    );

    INSERT INTO @totales(idTransaccion, total)
    SELECT i.idTransaccion,
           dbo.fn_TotalPagosTransaccion(i.idTransaccion)
      FROM inserted i;

    -- Validar que el estado pertenezca al módulo Transaccion
    DECLARE @invEstado INT = (
        SELECT TOP 1 i.idEstado
          FROM inserted i
         WHERE dbo.fn_AssertEstadoModulo(i.idEstado, N'Transaccion') = 0
    );
    IF @invEstado IS NOT NULL
        EXEC dbo.sp_ValidarEstado @invEstado, N'Transaccion', 50003;

    -- Exigir Venta o Pedido a menos que la verificación se omita
    IF ISNULL(SESSION_CONTEXT(N'skipSubtipoCheck'), 0) <> 1
       AND EXISTS(
            SELECT 1
              FROM inserted i
             WHERE NOT EXISTS (SELECT 1 FROM dbo.Venta  v WHERE v.idTransaccion = i.idTransaccion)
               AND NOT EXISTS (SELECT 1 FROM dbo.Pedido p WHERE p.idTransaccion = i.idTransaccion)
        )
        THROW 50014, 'Transaccion sin Venta ni Pedido.', 1;

    IF UPDATE(idEstado)
    BEGIN
        IF EXISTS (
            SELECT 1
              FROM inserted i
              JOIN @ins s     ON s.idTransaccion = i.idTransaccion
              JOIN @totales t ON t.idTransaccion = i.idTransaccion
             WHERE s.estado IN (N'Completada', N'Entregada')
               AND t.total <> i.totalNeto
        )
            THROW 50062, 'Para cerrar la transacción los pagos deben igualar el total neto.', 1;

        IF EXISTS (
            SELECT 1
              FROM inserted i
              JOIN @ins s ON s.idTransaccion = i.idTransaccion
             WHERE s.estado IN (N'Completada', N'Entregada')
               AND NOT EXISTS (SELECT 1 FROM dbo.PagoTransaccion pt WHERE pt.idTransaccion = i.idTransaccion)
        )
            THROW 50067, 'Para cerrar la transacción debe registrarse al menos un método de pago.', 1;

        IF EXISTS (
            SELECT 1
              FROM inserted i
              JOIN @ins s ON s.idTransaccion = i.idTransaccion
             WHERE s.estado IN (N'Completada', N'Entregada')
               AND NOT EXISTS (SELECT 1 FROM dbo.DetalleTransaccion d WHERE d.idTransaccion = i.idTransaccion)
        )
            THROW 50068, 'Para cerrar la transacción debe existir al menos un detalle.', 1;

        IF EXISTS (
            SELECT 1
              FROM inserted i
              JOIN dbo.Venta v ON v.idTransaccion = i.idTransaccion
              JOIN @ins s ON s.idTransaccion = i.idTransaccion
             WHERE s.estado = N'Cancelada'
               AND i.motivoCancelacion IS NULL
        )
            THROW 50050, 'Motivo de cancelación obligatorio para Venta.', 1;
    END;

    IF EXISTS (
        SELECT 1
          FROM inserted i
          JOIN deleted d ON d.idTransaccion = i.idTransaccion
          JOIN dbo.Estado e ON e.idEstado = d.idEstado
         WHERE e.nombre IN (N'Completada', N'Entregada', N'Cancelada')
           AND (
                i.totalBruto  <> d.totalBruto OR
                i.descuento   <> d.descuento  OR
                i.cargo       <> d.cargo      OR
                i.totalNeto   <> d.totalNeto  OR
                i.idCliente   <> d.idCliente  OR
                i.observacion <> d.observacion
           )
    )
        THROW 50061, 'Transacción cerrada: sólo se permite cambiar estado o motivo.', 1;

    IF NOT UPDATE(idEstado) OR NOT EXISTS(SELECT 1 FROM deleted) RETURN;

    DECLARE @idCancel      INT = dbo.fn_estado(N'Transaccion', N'Cancelada');
    DECLARE @idCancelacion INT = (SELECT idTipoMovimiento FROM dbo.TipoMovimiento WHERE nombre = 'Cancelación');

    DECLARE @canc TABLE(idTransaccion INT PRIMARY KEY);
    INSERT INTO @canc(idTransaccion)
    SELECT i.idTransaccion
      FROM inserted i
      JOIN deleted d ON d.idTransaccion = i.idTransaccion
     WHERE i.idEstado = @idCancel
       AND d.idEstado <> @idCancel;

    IF EXISTS (SELECT 1 FROM @canc)
    BEGIN
        INSERT INTO dbo.MovimientoInventario
              (idProducto, idTallaStock, idTipoMovimiento, cantidad, motivo, idEmpleado)
        SELECT dt.idProducto,
               dt.idTallaStock,
               @idCancelacion,
               dt.cantidad,
               CASE
                   WHEN v.idTransaccion IS NOT NULL
                       THEN 'Reversión Venta ' + CAST(c.idTransaccion AS VARCHAR(10)) + ' – ' + t.motivoCancelacion
                    WHEN ped.tipoPedido = N'Domicilio'
                       THEN 'Reversión Pedido Domicilio ' + CAST(c.idTransaccion AS VARCHAR(10)) + ' – ' + t.motivoCancelacion
               END,
               t.idEmpleado
          FROM @canc c
          JOIN dbo.Transaccion t      ON t.idTransaccion = c.idTransaccion
          LEFT JOIN dbo.Venta  v      ON v.idTransaccion  = c.idTransaccion
          LEFT JOIN dbo.Pedido ped    ON ped.idTransaccion = c.idTransaccion
          JOIN dbo.DetalleTransaccion dt ON dt.idTransaccion = c.idTransaccion
           WHERE v.idTransaccion IS NOT NULL
              OR ped.tipoPedido = N'Domicilio';

        UPDATE ped
           SET ped.comentarioCancelacion = t.motivoCancelacion
          FROM dbo.Pedido ped
          JOIN dbo.Transaccion t ON t.idTransaccion = ped.idTransaccion
          JOIN @canc c       ON c.idTransaccion = ped.idTransaccion
         WHERE ped.tipoPedido = N'Especial';

        -- El disparador trg_MovInv_ValidateAndUpdate actualiza el stock
        -- al insertar el movimiento de cancelación.
    END;

    DECLARE @cierre TABLE(idTransaccion INT PRIMARY KEY);
    INSERT INTO @cierre(idTransaccion)
      SELECT i.idTransaccion
      FROM inserted i
      JOIN deleted d ON d.idTransaccion = i.idTransaccion
      JOIN @ins s ON s.idTransaccion = i.idTransaccion
     WHERE s.estado IN (N'Completada', N'Entregada')
       AND d.idEstado <> i.idEstado;

    -- El stock se descontará cuando se registren los movimientos de
    -- inventario correspondientes.
END;
GO

-- TRIGGERS (reglas de negocio y consistencia)

/* Valida que el estado corresponda al módulo Persona y evita
   desactivaciones no permitidas */
CREATE OR ALTER TRIGGER trg_Persona_ValidarEstado
ON dbo.Persona AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    -- Validar que el estado pertenezca al módulo Persona para
    -- cualquier inserción o actualización
    DECLARE @invPers INT = (
        SELECT TOP 1 i.idEstado
          FROM inserted i
         WHERE dbo.fn_AssertEstadoModulo(i.idEstado, N'Persona') = 0
    );
    IF @invPers IS NOT NULL
        EXEC dbo.sp_ValidarEstado @invPers, N'Persona', 50001;

    -- Para actualizaciones, impedir la desactivación del propio
    -- usuario o de alguien de igual o mayor jerarquía
    IF UPDATE(idEstado)
    BEGIN
        DECLARE @actor INT = dbo.fn_actor_id();
        IF @actor IS NULL
            THROW 50080, 'SESSION_CONTEXT(''idEmpleado'') no establecido.', 1;

        DECLARE @idInactivo INT =
               (SELECT idEstado FROM dbo.Estado
                WHERE modulo = N'Persona' AND nombre = N'Inactivo');

        DECLARE @actorUsuario NVARCHAR(50) =
               (SELECT usuario FROM dbo.Empleado WHERE idPersona = @actor);

        DECLARE @tgt TABLE (idPersona INT);

        INSERT INTO @tgt (idPersona)
        SELECT i.idPersona
        FROM inserted i
        JOIN deleted d ON d.idPersona = i.idPersona
        WHERE i.idEstado = @idInactivo
          AND d.idEstado <> @idInactivo;

        IF @actorUsuario <> N'admin'
        BEGIN
            /* 3.1 – no puede desactivarse a sí mismo */
            IF EXISTS (SELECT 1 FROM @tgt WHERE idPersona = @actor)
                THROW 50081, 'No puede desactivar su propia cuenta.', 1;

            /* 3.2 – no puede desactivar par/superior */
            IF EXISTS (
                SELECT 1
                  FROM @tgt tgt
                  JOIN dbo.Empleado eTgt ON eTgt.idPersona = tgt.idPersona
                  JOIN dbo.Empleado eAct ON eAct.idPersona = @actor
                  JOIN dbo.Rol rT ON rT.idRol = eTgt.idRol
                  JOIN dbo.Rol rA ON rA.idRol = eAct.idRol
                 WHERE rT.nivel <= rA.nivel
            )
                THROW 50082, 'No puede desactivar a un usuario de igual o mayor jerarquía.', 1;
        END;
    END;
END;
GO

/* Valida estado y aplica ajustes automáticos según TipoProducto */
CREATE OR ALTER TRIGGER trg_Producto_ValidateAndAdjust
ON dbo.Producto AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    -- Copiar filas afectadas para reutilizarlas en las validaciones
    DECLARE @afectados TABLE(
        idProducto  INT PRIMARY KEY,
        idEstado    INT,
        idTipoProducto INT,
        umbral      DECIMAL(12,3),
        ignorarUmbralHastaCero BIT,
        prevStock   DECIMAL(12,3),
        prevUmbral  DECIMAL(12,3)
    );

    INSERT INTO @afectados
        (idProducto, idEstado, idTipoProducto, umbral, ignorarUmbralHastaCero,
         prevStock, prevUmbral)
    SELECT i.idProducto, i.idEstado, i.idTipoProducto, i.umbral,
           i.ignorarUmbralHastaCero, d.stockActual, d.umbral
      FROM inserted i
      LEFT JOIN deleted d ON d.idProducto = i.idProducto;

    DECLARE @tipo TABLE(
        idProducto INT PRIMARY KEY,
        esFrac     BIT,
        esVest     BIT
    );

    INSERT INTO @tipo(idProducto, esFrac, esVest)
    SELECT a.idProducto,
           dbo.fn_EsTipoProducto(a.idProducto, N'Fraccionable'),
           dbo.fn_EsTipoProducto(a.idProducto, N'Vestimenta')
      FROM @afectados a;

    -- Validar que el estado corresponda al módulo Producto
    DECLARE @invProd INT = (
        SELECT TOP 1 i.idEstado
          FROM @afectados i
         WHERE dbo.fn_AssertEstadoModulo(i.idEstado, N'Producto') = 0
    );
    IF @invProd IS NOT NULL
        EXEC dbo.sp_ValidarEstado @invProd, N'Producto', 50002;

    -- Forzar precioUnitario nulo en Fraccionables
    IF EXISTS(SELECT 1 FROM @tipo t WHERE t.esFrac = 1)
    BEGIN
        UPDATE p
           SET p.precioUnitario = NULL
          FROM dbo.Producto p
          JOIN @tipo t ON t.idProducto = p.idProducto
         WHERE t.esFrac = 1
           AND p.precioUnitario IS NOT NULL;
    END;

    -- Recalcular stock en Vestimenta a partir de TallaStock
    IF EXISTS(SELECT 1 FROM @tipo t WHERE t.esVest = 1)
    BEGIN
        UPDATE p
           SET p.stockActual = COALESCE((SELECT SUM(ts.stock)
                                           FROM dbo.TallaStock ts
                                          WHERE ts.idProducto = p.idProducto),0)
          FROM dbo.Producto p
          JOIN @tipo t ON t.idProducto = p.idProducto
         WHERE t.esVest = 1
           AND p.stockActual <> COALESCE((SELECT SUM(ts.stock)
                                          FROM dbo.TallaStock ts
                                         WHERE ts.idProducto = p.idProducto),0);
    END;

    -- Generar alerta cuando el stock cae por debajo de su umbral
    DECLARE @datos TABLE(
        idProducto  INT PRIMARY KEY,
        stockActual DECIMAL(12,3),
        umbral      DECIMAL(12,3)
    );

    INSERT INTO @datos(idProducto, stockActual, umbral)
    SELECT a.idProducto,
           s.stockActual,
           a.umbral
      FROM @afectados a
      JOIN @tipo t ON t.idProducto = a.idProducto
      CROSS APPLY (SELECT dbo.fn_StockDisponible(a.idProducto) AS stockActual) s
     WHERE s.stockActual < a.umbral
       AND a.ignorarUmbralHastaCero = 0
       AND (a.prevStock IS NULL OR a.prevStock >= a.prevUmbral)
       AND NOT (
            a.prevStock IS NULL
            AND t.esVest = 1
       );

    MERGE dbo.AlertaStock WITH (HOLDLOCK) AS t
    USING @datos AS s
       ON t.idProducto = s.idProducto
      AND t.procesada = 0
    WHEN MATCHED THEN
        UPDATE SET stockActual = s.stockActual,
                   umbral      = s.umbral,
                   fechaAlerta = SYSDATETIME()
    WHEN NOT MATCHED THEN
        INSERT (idProducto, stockActual, umbral)
        VALUES (s.idProducto, s.stockActual, s.umbral);

    DECLARE @idInUmbral INT = dbo.fn_estado(N'Producto', N'Inactivo por umbral');
    IF EXISTS (SELECT 1 FROM @datos)
    BEGIN
        UPDATE p
           SET p.idEstado = @idInUmbral
          FROM dbo.Producto p
          JOIN @datos d ON d.idProducto = p.idProducto
         WHERE p.idEstado <> @idInUmbral;
    END;

    DECLARE @idActivo INT = dbo.fn_estado(N'Producto', N'Activo');
    UPDATE p
       SET p.idEstado = @idActivo
      FROM dbo.Producto p
      JOIN @afectados a ON a.idProducto = p.idProducto
     WHERE p.idEstado = @idInUmbral
       AND p.stockActual >= p.umbral;

    UPDATE a
       SET a.procesada = 1
      FROM dbo.AlertaStock a
      JOIN dbo.Producto p ON p.idProducto = a.idProducto
      JOIN @afectados af ON af.idProducto = a.idProducto
     WHERE a.procesada = 0
       AND p.stockActual >= p.umbral;

    -- Restablecer la bandera cuando el stock llegue a cero
    UPDATE p
       SET p.ignorarUmbralHastaCero = 0
      FROM dbo.Producto p
      JOIN @afectados a ON a.idProducto = p.idProducto
     WHERE p.ignorarUmbralHastaCero = 1
       AND p.stockActual = 0;
END;
GO

/* Valida estado y actualiza stock de TallaStock */
CREATE OR ALTER TRIGGER trg_TallaStock_ValidateAndUpdate
ON dbo.TallaStock AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    -- Validar que el estado corresponda al módulo Producto
    DECLARE @invTalla INT = (
        SELECT TOP 1 i.idEstado
          FROM inserted i
         WHERE dbo.fn_AssertEstadoModulo(i.idEstado, N'Producto') = 0
    );
    IF @invTalla IS NOT NULL
        EXEC dbo.sp_ValidarEstado @invTalla, N'Producto', 50092;

    DECLARE @tipo TABLE(
        idProducto INT PRIMARY KEY,
        esFrac     BIT,
        esVest     BIT
    );

    INSERT INTO @tipo(idProducto, esFrac, esVest)
    SELECT DISTINCT i.idProducto,
           dbo.fn_EsTipoProducto(i.idProducto, N'Fraccionable'),
           dbo.fn_EsTipoProducto(i.idProducto, N'Vestimenta')
      FROM inserted i;

    -- Validar que solo productos de tipo Vestimenta permitan tallas
    IF EXISTS(
        SELECT 1
          FROM inserted i
          JOIN @tipo t ON t.idProducto = i.idProducto
         WHERE t.esVest = 0
    )
        THROW 50021, 'Sólo productos de tipo Vestimenta permiten tallas.', 1;

    -- Recalcular stock global para productos afectados
    WITH afectados AS (
        SELECT DISTINCT idProducto FROM inserted
        UNION
        SELECT DISTINCT idProducto FROM deleted
    )
    UPDATE p
       SET p.stockActual = COALESCE(
            (
                SELECT SUM(ts.stock)
                  FROM dbo.TallaStock ts
                 WHERE ts.idProducto = p.idProducto
            ), 0)
      FROM dbo.Producto p
      JOIN afectados a ON a.idProducto = p.idProducto;
END;
GO

/* Valida estado y reglas de tipo y precio en Presentacion */
CREATE OR ALTER TRIGGER trg_Presentacion_Validate
ON dbo.Presentacion AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    -- A) el estado debe pertenecer al módulo Producto
    DECLARE @invPres INT = (
        SELECT TOP 1 i.idEstado
          FROM inserted i
         WHERE dbo.fn_AssertEstadoModulo(i.idEstado, N'Producto') = 0
    );
    IF @invPres IS NOT NULL
        EXEC dbo.sp_ValidarEstado @invPres, N'Producto', 50093;

    DECLARE @tipo TABLE(
        idProducto INT PRIMARY KEY,
        esFrac     BIT,
        esVest     BIT
    );

    INSERT INTO @tipo(idProducto, esFrac, esVest)
    SELECT DISTINCT i.idProducto,
           dbo.fn_EsTipoProducto(i.idProducto, N'Fraccionable'),
           dbo.fn_EsTipoProducto(i.idProducto, N'Vestimenta')
      FROM inserted i;

    -- B) solo productos Fraccionables permiten presentaciones
    IF EXISTS(
        SELECT 1
          FROM inserted i
          JOIN @tipo t ON t.idProducto = i.idProducto
         WHERE t.esFrac = 0
    )
        THROW 50022, 'Sólo productos de tipo Fraccionable permiten presentaciones.', 1;

    -- C) el precio de la presentación no debe superar el proporcional
    IF EXISTS(
        SELECT 1
          FROM inserted i
          JOIN dbo.Producto p ON p.idProducto = i.idProducto
         WHERE i.precio > i.cantidad * p.precioUnitario
    )
        THROW 50091, 'El precio de la presentación excede el proporcional del precio unitario.', 1;
END;
GO

/* Garantiza relación uno‑a‑uno: al insertar Venta no debe existir Pedido */
CREATE OR ALTER TRIGGER trg_Venta_Insert
ON dbo.Venta AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;
    IF EXISTS(
        SELECT 1
        FROM   inserted i
        JOIN   dbo.Pedido   p ON p.idTransaccion = i.idTransaccion)
        THROW 50010,'No puede ser Venta y Pedido a la vez.',1;
    UPDATE t
       SET t.idEstado = e.idEstado
    FROM  dbo.Transaccion t
    JOIN  inserted    i ON i.idTransaccion = t.idTransaccion
    JOIN  dbo.Estado      e ON e.modulo=N'Transaccion' AND e.nombre=N'Completada'
    WHERE t.idEstado <> e.idEstado;
END;
GO

/* Valida reglas de Pedido y fecha de entrega */
CREATE OR ALTER TRIGGER trg_Pedido_Validate
ON dbo.Pedido
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    -- Validaciones exclusivas para nuevas filas
    IF EXISTS (
        SELECT 1
          FROM inserted i
          LEFT JOIN deleted d ON d.idTransaccion = i.idTransaccion
          JOIN dbo.Venta v ON v.idTransaccion = i.idTransaccion
         WHERE d.idTransaccion IS NULL
    )
        THROW 50011, 'No puede ser Pedido y Venta a la vez.', 1;

    IF EXISTS (
        SELECT 1
          FROM inserted i
          LEFT JOIN deleted d ON d.idTransaccion = i.idTransaccion
          JOIN dbo.Transaccion t ON t.idTransaccion = i.idTransaccion
         WHERE d.idTransaccion IS NULL
           AND t.idCliente IS NULL
    )
        THROW 50012, 'Pedido requiere Cliente.', 1;

    IF EXISTS (
        SELECT 1
          FROM inserted i
          LEFT JOIN deleted d ON d.idTransaccion = i.idTransaccion
          JOIN dbo.Transaccion t ON t.idTransaccion = i.idTransaccion
         WHERE d.idTransaccion IS NULL
           AND i.tipoPedido = N'Domicilio'
           AND t.idCliente IS NULL
    )
        THROW 50015, 'La venta con reparto requiere un Cliente registrado.', 1;

    UPDATE t
       SET t.idEstado = e.idEstado
      FROM dbo.Transaccion t
      JOIN inserted i ON i.idTransaccion = t.idTransaccion
      LEFT JOIN deleted d ON d.idTransaccion = i.idTransaccion
      JOIN dbo.Estado e ON e.modulo = N'Transaccion' AND e.nombre = N'En Proceso'
     WHERE d.idTransaccion IS NULL
       AND t.idEstado <> e.idEstado;

    IF EXISTS (
        SELECT 1
          FROM inserted i
          JOIN dbo.Transaccion t ON t.idTransaccion = i.idTransaccion
         WHERE i.fechaHoraEntrega IS NOT NULL
           AND i.fechaHoraEntrega < t.fecha
    )
        THROW 50016, 'Fecha de entrega anterior a la transacción.', 1;
END;
GO

/* Mantiene pagos = total en transacciones cerradas */
CREATE OR ALTER TRIGGER trg_PagoTransaccion_CheckSum
ON dbo.PagoTransaccion
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    DECLARE @ids TABLE(idTransaccion INT PRIMARY KEY);

    INSERT INTO @ids(idTransaccion)
    SELECT idTransaccion FROM inserted
    UNION
    SELECT idTransaccion FROM deleted;

    IF EXISTS (
        SELECT 1
        FROM   @ids        a
        JOIN   dbo.Transaccion t ON t.idTransaccion = a.idTransaccion
        JOIN   dbo.Estado      e ON e.idEstado = t.idEstado
        WHERE  e.modulo = N'Transaccion'
          AND  e.nombre IN (N'Completada', N'Entregada')
          AND  dbo.fn_TotalPagosTransaccion(t.idTransaccion) <> t.totalNeto
    )
        THROW 50030,'La suma de pagos debe coincidir con el total neto para transacciones cerradas.',1;
END;
GO

/* Impide comprobante para algo que no sea Venta o Pedido */
CREATE OR ALTER TRIGGER trg_Comprobante_Insert
ON dbo.Comprobante AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;
    IF EXISTS(
      SELECT 1
      FROM   inserted i
      LEFT  JOIN dbo.Venta  v ON v.idTransaccion  = i.idTransaccion
      LEFT  JOIN dbo.Pedido p ON p.idTransaccion = i.idTransaccion
      WHERE v.idTransaccion IS NULL AND p.idTransaccion IS NULL
    ) THROW 50040, 'Comprobante solo para Venta o Pedido.', 1;
END;
GO

/* Bloqueo de transacciones cerradas y recálculo de totales */
CREATE OR ALTER TRIGGER trg_DetalleTransaccion_Maintenance
ON dbo.DetalleTransaccion
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    IF EXISTS (SELECT 1 FROM inserted)
    BEGIN
        -- Copiar filas afectadas para usar en las validaciones
        DECLARE @det TABLE(
            idDetalle      INT,
            idTransaccion  INT,
            idProducto     INT,
            idTallaStock   INT,
            cantidad       DECIMAL(12,3),
            precioUnitario DECIMAL(10,2)
        );

        INSERT INTO @det
            (idDetalle, idTransaccion, idProducto, idTallaStock,
             cantidad, precioUnitario)
        SELECT idDetalle, idTransaccion, idProducto, idTallaStock,
               cantidad, precioUnitario
          FROM inserted;

        -- Parámetro mínimo de ovillos para pedido mayorista de hilo
        DECLARE @minHilo INT = dbo.fn_MinCantidadMayoristaHilo();

        -- Stock disponible de los productos y tallas afectadas
        DECLARE @stockProd TABLE(
            idProducto INT PRIMARY KEY,
            stock      DECIMAL(12,3)
        );

        INSERT INTO @stockProd(idProducto, stock)
        SELECT p.idProducto, p.stockActual
          FROM dbo.Producto p
          JOIN (
                SELECT DISTINCT idProducto
                  FROM @det
               ) AS d ON d.idProducto = p.idProducto;

        DECLARE @stockTalla TABLE(
            idTallaStock INT PRIMARY KEY,
            stock        DECIMAL(12,3)
        );

        INSERT INTO @stockTalla(idTallaStock, stock)
        SELECT ts.idTallaStock, ts.stock
          FROM dbo.TallaStock ts
          JOIN (
                SELECT DISTINCT idTallaStock
                  FROM @det
                 WHERE idTallaStock IS NOT NULL
               ) AS d ON d.idTallaStock = ts.idTallaStock;

        DECLARE @tipo TABLE(
            idProducto INT PRIMARY KEY,
            esFrac     BIT,
            esVest     BIT
        );

        INSERT INTO @tipo(idProducto, esFrac, esVest)
        SELECT DISTINCT d.idProducto,
               dbo.fn_EsTipoProducto(d.idProducto, N'Fraccionable'),
               dbo.fn_EsTipoProducto(d.idProducto, N'Vestimenta')
          FROM @det d;

        /* 0 ‑ Validación de tallas --------------------------------*/
        IF EXISTS (
            SELECT 1
              FROM @det i
              JOIN @tipo t ON t.idProducto = i.idProducto
             WHERE (t.esVest = 1 AND i.idTallaStock IS NULL)
                OR (t.esVest = 0 AND i.idTallaStock IS NOT NULL)
        )
            THROW 50064,
                  'Para Vestimenta se requiere idTallaStock; en otros productos debe ser NULL.', 1;

        /* 0.1 ‑ idTallaStock pertenece al mismo producto ----------*/
        IF EXISTS (
            SELECT 1
              FROM @det       i
              JOIN dbo.TallaStock ts ON ts.idTallaStock = i.idTallaStock
             WHERE ts.idProducto <> i.idProducto
        )
            THROW 50065, 'El idTallaStock no corresponde al producto.', 1;

        /* 1 ‑ Producto desactivado no puede venderse --------------*/
        IF EXISTS (
            SELECT 1
              FROM @det      i
              JOIN dbo.Producto p ON p.idProducto = i.idProducto
              JOIN dbo.Estado   e ON e.idEstado   = p.idEstado
             WHERE e.modulo = N'Producto'
               AND e.nombre IN (N'Desactivado', N'Inactivo por umbral')
        )
            THROW 50026, 'El producto está inactivo y no puede venderse.', 1;

        /* 2 - Precio mayorista automatico para Venta, Domicilio y Especial */
        UPDATE dt
               SET dt.precioUnitario = p.precioMayorista
              FROM dbo.DetalleTransaccion dt
              JOIN @det           i   ON i.idDetalle     = dt.idDetalle
              JOIN dbo.Transaccion     t   ON t.idTransaccion = i.idTransaccion
              LEFT JOIN dbo.Pedido     ped ON ped.idTransaccion = t.idTransaccion
              JOIN dbo.Producto        p   ON p.idProducto    = i.idProducto
             WHERE p.mayorista       = 1
               AND p.minMayorista    IS NOT NULL
               AND p.precioMayorista IS NOT NULL
               AND (   (   ped.idTransaccion IS NULL
                        OR ped.tipoPedido = N'Domicilio')
                       AND i.cantidad >= p.minMayorista
                    OR ped.tipoPedido = N'Especial'
                   );

        /* 2.1 - Validacion precio unitario segun regla mayorista */
        IF EXISTS (
            SELECT 1
              FROM @det i
              JOIN dbo.Transaccion  t   ON t.idTransaccion = i.idTransaccion
              LEFT JOIN dbo.Pedido  ped ON ped.idTransaccion = t.idTransaccion
              JOIN dbo.Producto     p   ON p.idProducto    = i.idProducto
             WHERE p.mayorista       = 1
               AND p.minMayorista    IS NOT NULL
               AND p.precioMayorista IS NOT NULL
               AND (
                        (   (ped.idTransaccion IS NULL
                            OR ped.tipoPedido = N'Domicilio')
                         AND (   (i.cantidad >= p.minMayorista  AND i.precioUnitario <> p.precioMayorista)
                              OR (i.cantidad <  p.minMayorista AND i.precioUnitario<> p.precioUnitario) )
                        )
                    OR (ped.tipoPedido = N'Especial' AND i.precioUnitario <> p.precioMayorista)
                   )
        )
            THROW 50091, 'Precio unitario no cumple la regla mayorista.', 1;

        /* 2.2 - Producto mayorista sin parametros completos -------*/
        IF EXISTS (
            SELECT 1
              FROM @det i
              JOIN dbo.Producto p ON p.idProducto = i.idProducto
             WHERE p.mayorista = 1
               AND (p.minMayorista IS NULL OR p.precioMayorista IS NULL)
       )
            THROW 50095, 'Producto mayorista sin configuracion completa.', 1;

        /* 3 ‑ Presentación exacta en Fraccionables ----------------*/
        IF EXISTS (
            SELECT 1
              FROM @det i
              JOIN @tipo t ON t.idProducto = i.idProducto
             WHERE t.esFrac = 1
               AND NOT EXISTS ( SELECT 1
                                  FROM dbo.Presentacion pr
                                 WHERE pr.idProducto = i.idProducto
                                   AND pr.cantidad   = i.cantidad )
        )
            THROW 50024,
                  'La cantidad no coincide con ninguna presentación registrada.', 1;

        /* 4 ‑ Mínimo mayorista de hilo ----------------------------*/
        IF EXISTS (
                SELECT 1
                  FROM @det            i
                  JOIN dbo.Producto      p   ON p.idProducto    = i.idProducto
                  JOIN dbo.Categoria     c   ON c.idCategoria   = p.idCategoria
                  JOIN dbo.Transaccion   t   ON t.idTransaccion = i.idTransaccion
                  JOIN dbo.Pedido        ped ON ped.idTransaccion = t.idTransaccion
                 WHERE ped.tipoPedido = N'Especial'
                   AND c.nombre       = 'Mercería'
                 GROUP BY i.idTransaccion
                HAVING SUM(i.cantidad) < @minHilo
            )
            THROW 50025,
                  'El pedido mayorista de hilo debe alcanzar la cantidad mínima configurada.', 1;

        /* 5 ‑ Stock suficiente Ventas y pedidos Domicilio deben tener stock disponible.
           Pedidos Especiales de hilo pueden ignorar esta regla      */
        IF EXISTS (
            SELECT 1
              FROM @det i
              JOIN dbo.Transaccion  t   ON t.idTransaccion = i.idTransaccion
              LEFT JOIN dbo.Pedido  ped ON ped.idTransaccion = t.idTransaccion
              JOIN @stockProd sp   ON sp.idProducto = i.idProducto
             WHERE (   ped.idTransaccion IS NULL            -- dbo.Venta
                    OR ped.tipoPedido = N'Domicilio')        -- dbo.Pedido Domicilio
               AND  sp.stock < i.cantidad
       )
            THROW 50020, 'Stock insuficiente para el producto.', 1;

        /* 5.1 ‑ Stock por talla (Vestimenta) -----------------------*/
        IF EXISTS (
            SELECT 1
              FROM @det       i
              JOIN dbo.TallaStock  ts ON ts.idTallaStock = i.idTallaStock
              JOIN dbo.Producto    p  ON p.idProducto    = ts.idProducto
              JOIN @stockTalla st ON st.idTallaStock = ts.idTallaStock
              JOIN @tipo       t  ON t.idProducto     = p.idProducto
             WHERE t.esVest = 1
               AND st.stock < i.cantidad
       )
            THROW 50066, 'Stock insuficiente en la talla seleccionada.', 1;

        /* 5.2 ‑ Límite venta ovillos (máximo 199) -----------------*/
        IF EXISTS (
            SELECT 1
              FROM @det i
              JOIN dbo.Transaccion t   ON t.idTransaccion = i.idTransaccion
              LEFT JOIN dbo.Pedido ped ON ped.idTransaccion = t.idTransaccion
              JOIN dbo.Producto p      ON p.idProducto    = i.idProducto
             WHERE ped.idTransaccion IS NULL
               AND p.paraPedido = 1
               AND p.tipoPedidoDefault = N'Especial'
               AND p.mayorista = 1
               AND i.cantidad >= 200
       )
            THROW 50090, 'Cantidad máxima 199 para ovillos en venta', 1;
    END;

    -- Impedir modificaciones cuando la transacción está cerrada
    IF EXISTS (
        SELECT 1
          FROM ( SELECT idTransaccion FROM inserted
                 UNION
                 SELECT idTransaccion FROM deleted ) d
          JOIN dbo.Transaccion t ON t.idTransaccion = d.idTransaccion
          JOIN dbo.Estado      e ON e.idEstado      = t.idEstado
         WHERE e.nombre IN (N'Completada', N'Entregada', N'Cancelada')
    )
        THROW 50060,'No se pueden modificar / eliminar detalles de una transacción cerrada.',1;

    -- Ajustar el totalBruto de la transacción
    IF SESSION_CONTEXT(N'skipBrutoUpdate') = 1 RETURN;
    ;WITH cambios AS (
        SELECT idTransaccion, SUM(delta) AS delta
        FROM (
            SELECT idTransaccion, SUM(subtotal) AS delta
              FROM inserted
             GROUP BY idTransaccion
            UNION ALL
            SELECT idTransaccion, -SUM(subtotal) AS delta
              FROM deleted
             GROUP BY idTransaccion
        ) x
        GROUP BY idTransaccion
    )
    UPDATE t
       SET totalBruto = t.totalBruto + c.delta
      FROM dbo.Transaccion t
      JOIN cambios c ON c.idTransaccion = t.idTransaccion;
END;
GO

/* Valida y actualiza stock en MovimientoInventario */
CREATE OR ALTER TRIGGER trg_MovInv_ValidateAndUpdate
ON dbo.MovimientoInventario AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    DECLARE @tipo TABLE(
        idProducto INT PRIMARY KEY,
        esFrac     BIT,
        esVest     BIT
    );

    INSERT INTO @tipo(idProducto, esFrac, esVest)
    SELECT DISTINCT i.idProducto,
           dbo.fn_EsTipoProducto(i.idProducto, N'Fraccionable'),
           dbo.fn_EsTipoProducto(i.idProducto, N'Vestimenta')
      FROM inserted i;

    -- Sólo productos de tipo Vestimenta pueden registrar idTallaStock
    IF EXISTS (
        SELECT 1
          FROM inserted i
          JOIN @tipo t ON t.idProducto = i.idProducto
         WHERE i.idTallaStock IS NOT NULL
           AND t.esVest = 0
    )
        THROW 50100,
              'Sólo productos de tipo Vestimenta pueden llevar idTallaStock en movimiento.',
              1;

    -- idTallaStock debe pertenecer al mismo producto indicado
    IF EXISTS (
        SELECT 1
          FROM inserted   i
          JOIN dbo.TallaStock ts ON ts.idTallaStock = i.idTallaStock
         WHERE i.idTallaStock IS NOT NULL
           AND ts.idProducto  <> i.idProducto
    )
        THROW 50101,
              'El idTallaStock no corresponde al producto indicado en el movimiento.',
              1;

    -- Motivo requerido cuando el tipo de movimiento es Ajuste
    IF EXISTS (
        SELECT 1
          FROM inserted i
          JOIN dbo.TipoMovimiento tm ON tm.idTipoMovimiento = i.idTipoMovimiento
         WHERE tm.nombre = 'Ajuste' AND i.motivo IS NULL
    )
        THROW 50102, 'El motivo es obligatorio para movimientos de ajuste.', 1;

    -- Motivo predeterminado para Entradas sin motivo
    UPDATE mi
       SET mi.motivo = N'Ingreso manual'
      FROM dbo.MovimientoInventario mi
      JOIN inserted i       ON i.idMovimiento = mi.idMovimiento
      LEFT JOIN deleted d   ON d.idMovimiento = i.idMovimiento
      JOIN dbo.TipoMovimiento tm ON tm.idTipoMovimiento = i.idTipoMovimiento
     WHERE d.idMovimiento IS NULL
       AND tm.nombre = N'Entrada'
       AND (i.motivo IS NULL OR LTRIM(RTRIM(i.motivo)) = '');

    DECLARE @ins TABLE(
        idMovimiento     INT PRIMARY KEY,
        idProducto       INT,
        idTallaStock     INT,
        idTipoMovimiento INT,
        cantidad         DECIMAL(12,3)
    );

    INSERT INTO @ins(idMovimiento, idProducto, idTallaStock, idTipoMovimiento, cantidad)
    SELECT i.idMovimiento, i.idProducto, i.idTallaStock, i.idTipoMovimiento, i.cantidad
      FROM inserted i
      LEFT JOIN deleted d ON d.idMovimiento = i.idMovimiento
     WHERE d.idMovimiento IS NULL;

    IF EXISTS (SELECT 1 FROM @ins)
    BEGIN
        DECLARE @mov TABLE(
            idProducto  INT,
            idTallaStock INT,
            delta       DECIMAL(12,3)
        );

        INSERT INTO @mov(idProducto, idTallaStock, delta)
        SELECT i.idProducto,
               i.idTallaStock,
               CASE WHEN tm.nombre IN (N'Entrada', N'Cancelación')
                     THEN i.cantidad ELSE -i.cantidad END
          FROM @ins i
          JOIN dbo.TipoMovimiento tm ON tm.idTipoMovimiento = i.idTipoMovimiento;

        UPDATE p
           SET p.stockActual = p.stockActual + m.delta
          FROM dbo.Producto p
          JOIN (SELECT idProducto, SUM(delta) AS delta
                  FROM @mov GROUP BY idProducto) m
            ON m.idProducto = p.idProducto;

        UPDATE ts
           SET ts.stock = ts.stock + m.delta
          FROM dbo.TallaStock ts
          JOIN (SELECT idTallaStock, SUM(delta) AS delta
                  FROM @mov WHERE idTallaStock IS NOT NULL GROUP BY idTallaStock) m
            ON m.idTallaStock = ts.idTallaStock;

        DECLARE @check dbo.tvp_DetalleTx;
        -- Cantidad ficticia para respetar la restricción CHECK de tvp_DetalleTx
        INSERT INTO @check(idProducto, idTallaStock, cantidad, precioUnitario)
        SELECT DISTINCT idProducto, idTallaStock, 1, 0 FROM @mov;

        DECLARE @neg INT = dbo.fn_TieneStockNegativo(@check);
        IF (@neg & 1) = 1
            THROW 60110, N'Stock negativo tras movimiento.', 1;
        IF (@neg & 2) = 2
            THROW 60111, N'Stock negativo (talla) tras movimiento.', 1;
    END;
END;
GO

/* Manejo de login: bitácora y bloqueo de cuenta */
CREATE OR ALTER TRIGGER trg_Empleado_LoginHandling
ON dbo.Empleado
WITH EXECUTE AS OWNER
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;

    /* login exitoso ------------------------------------------------*/
    IF UPDATE(ultimoAcceso)
    BEGIN
        INSERT INTO dbo.BitacoraLogin(idEmpleado,exitoso)
        SELECT i.idPersona, 1
        FROM inserted i
        JOIN deleted d ON d.idPersona = i.idPersona
        WHERE i.ultimoAcceso IS NOT NULL
          AND (d.ultimoAcceso IS NULL OR i.ultimoAcceso <> d.ultimoAcceso);

        UPDATE dbo.Empleado
           SET intentosFallidos = 0,
               bloqueadoHasta   = NULL
        FROM dbo.Empleado e
        JOIN inserted i ON i.idPersona = e.idPersona
        WHERE i.ultimoAcceso IS NOT NULL;
    END;

    /* intento fallido ----------------------------------------------*/
    IF UPDATE(intentosFallidos)
    BEGIN
        INSERT INTO dbo.BitacoraLogin(idEmpleado,exitoso)
        SELECT i.idPersona, 0
        FROM inserted i
        JOIN deleted d ON d.idPersona = i.idPersona
        WHERE i.intentosFallidos > d.intentosFallidos;

        DECLARE @maxFallidos INT = dbo.fn_MaxIntentosFallidos();
        DECLARE @minBloqueo INT = dbo.fn_MinutosBloqueoCuenta();

        UPDATE e
           SET bloqueadoHasta = DATEADD(MINUTE, @minBloqueo, SYSDATETIME())
        FROM dbo.Empleado e
        JOIN inserted i ON i.idPersona = e.idPersona
        WHERE i.intentosFallidos = @maxFallidos;
    END;
END;
GO

/* Solo administradores pueden modificar catalogo de roles */
CREATE OR ALTER TRIGGER trg_Rol_AdminOnly
ON dbo.Rol
WITH EXECUTE AS OWNER
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;
    EXEC dbo.sp_CheckAdminTrigger;
END;
GO

/* Solo administradores pueden modificar parametros del sistema */
CREATE OR ALTER TRIGGER trg_ParametroSistema_AdminOnly
ON dbo.ParametroSistema
WITH EXECUTE AS OWNER
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;
    EXEC dbo.sp_CheckAdminTrigger;
END;
GO
