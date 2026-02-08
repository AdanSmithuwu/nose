-- Movimientos de inventario iniciales

USE cv_ventas_distribucion;
GO

BEGIN TRY
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRAN;

    EXEC dbo.sp_DisableSeedTriggers;

    DECLARE @idAdminPersona INT =
        (SELECT idPersona FROM dbo.Empleado WHERE usuario=N'admin');
    DECLARE @idEntrada INT =
        (SELECT idTipoMovimiento FROM dbo.TipoMovimiento WHERE nombre='Entrada');
    IF @idEntrada IS NULL
        THROW 64001, N'El tipo de movimiento es obligatorio.', 1;

    -- Registrar los movimientos de stock inicial
    INSERT dbo.MovimientoInventario(idProducto,idTipoMovimiento,cantidad,motivo,idEmpleado)
    SELECT p.idProducto, @idEntrada, p.stockActual, N'Carga inicial', @idAdminPersona
    FROM dbo.Producto p
    WHERE p.stockActual > 0;

    INSERT dbo.MovimientoInventario(idProducto,idTallaStock,idTipoMovimiento,cantidad,motivo,idEmpleado)
    SELECT ts.idProducto, ts.idTallaStock, @idEntrada, ts.stock, N'Carga inicial', @idAdminPersona
    FROM dbo.TallaStock ts
    WHERE ts.stock > 0;

    EXEC dbo.sp_RecalcularStockProductos;

    COMMIT;
    EXEC dbo.sp_EnableSeedTriggers;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    EXEC dbo.sp_EnableSeedTriggers;
    THROW;
END CATCH
GO
