-- Vistas para la base de datos de Comercial's Valerio
-- Proveen consultas de reporte.

USE cv_ventas_distribucion;
GO

/* 1) Clientes con más compras -------------------------------------*/
CREATE OR ALTER VIEW dbo.vw_ClientesFrecuentes
WITH SCHEMABINDING
AS
SELECT  C.idPersona AS idCliente,
        dbo.fn_NombreCompleto(C.idPersona) AS nombre,
        COUNT_BIG(*) AS numCompras
FROM    dbo.Transaccion  AS T
JOIN    dbo.Cliente      AS C ON C.idPersona = T.idCliente
JOIN    dbo.Persona      AS P ON P.idPersona = C.idPersona
JOIN    dbo.Estado       AS E ON E.idEstado = T.idEstado
                                 AND E.modulo = N'Transaccion'
                                 AND E.nombre IN (N'Completada', N'Entregada')
GROUP BY C.idPersona, P.nombres, P.apellidos;
GO

/* 2) Historial de transacciones por cliente ---------------------*/
CREATE OR ALTER VIEW dbo.vw_HistorialTransaccionesPorCliente
WITH SCHEMABINDING
AS
SELECT  C.idPersona            AS idCliente,
        dbo.fn_NombreCompleto(C.idPersona) AS Cliente,
        T.idTransaccion,
        T.fecha,
        T.totalNeto,
        T.descuento,
        T.cargo,
        E.nombre              AS Estado,
        CASE WHEN V.idTransaccion IS NOT NULL THEN 'Venta'
             WHEN P.idTransaccion IS NOT NULL THEN 'Pedido'
        END                   AS Tipo
FROM    dbo.Transaccion AS T
LEFT JOIN dbo.Venta   AS V ON V.idTransaccion   = T.idTransaccion
LEFT JOIN dbo.Pedido  AS P ON P.idTransaccion   = T.idTransaccion
JOIN    dbo.Cliente   AS C ON C.idPersona       = T.idCliente
JOIN    dbo.Estado    AS E ON E.idEstado        = T.idEstado;
GO

/* 3) Productos más vendidos -------------------------------------*/
CREATE OR ALTER VIEW dbo.vw_ProductosMasVendidos
WITH SCHEMABINDING
AS
SELECT  TOP 100
        P.idProducto,
        P.nombre,
        SUM(DT.cantidad)        AS UnidadesVendidas,
        SUM(DT.subtotal)        AS Ingresos
FROM    dbo.DetalleTransaccion AS DT
JOIN    dbo.Producto           AS P  ON P.idProducto = DT.idProducto
JOIN    dbo.Transaccion        AS T  ON T.idTransaccion = DT.idTransaccion
JOIN    dbo.Estado             AS E  ON E.idEstado = T.idEstado
WHERE   E.nombre IN (N'Completada', N'Entregada')
        AND E.modulo=N'Transaccion'
GROUP BY P.idProducto,P.nombre
ORDER BY UnidadesVendidas DESC;
GO

/* 4) Transacciones por día --------------------------------------*/
CREATE OR ALTER VIEW dbo.vw_TransaccionesPorDia
WITH SCHEMABINDING
AS
WITH PagosAgg AS (
    SELECT  PT.idTransaccion,
            SUM(CASE WHEN MP.nombre = N'Efectivo' THEN PT.monto ELSE 0 END) AS MontoEfectivo,
            SUM(CASE WHEN MP.nombre = N'Billetera Digital' THEN PT.monto ELSE 0 END) AS MontoBilleteraDigital
    FROM    dbo.PagoTransaccion AS PT
    JOIN    dbo.MetodoPago      AS MP ON MP.idMetodoPago = PT.idMetodoPago
    GROUP BY PT.idTransaccion
)
SELECT  T.fechaDia AS Dia,
        SUM(CASE WHEN V.idTransaccion IS NOT NULL THEN 1 ELSE 0 END) AS NumTransacciones,
        SUM(CASE WHEN P.idTransaccion IS NOT NULL THEN 1 ELSE 0 END) AS NumPedidosEntregados,
        SUM(T.totalBruto) AS TotalBrutoDia,
        SUM(T.totalNeto)  AS TotalNetoDia,
        SUM(ISNULL(Pagos.MontoEfectivo,0))         AS MontoEfectivo,
        SUM(ISNULL(Pagos.MontoBilleteraDigital,0)) AS MontoBilleteraDigital,
        SUM(CASE WHEN V.idTransaccion IS NOT NULL THEN T.totalNeto ELSE 0 END) AS IngresosDia,
        COUNT_BIG(*) AS NumRegistros
FROM    dbo.Transaccion AS T
LEFT JOIN dbo.Venta  AS V ON V.idTransaccion  = T.idTransaccion
LEFT JOIN dbo.Pedido AS P ON P.idTransaccion  = T.idTransaccion
JOIN    dbo.Estado  AS E ON E.idEstado = T.idEstado
                           AND E.modulo  = N'Transaccion'
LEFT JOIN PagosAgg AS Pagos ON Pagos.idTransaccion = T.idTransaccion
WHERE   E.nombre IN (N'Completada', N'Entregada')
GROUP BY T.fechaDia;
GO

/* 5) Monto pagado por método en el día --------------------------*/
CREATE OR ALTER VIEW dbo.vw_PagoMetodoDia
WITH SCHEMABINDING
AS
SELECT  T.fechaDia      AS Dia,
        PT.idMetodoPago,
        MP.nombre       AS Metodo,
        SUM(CASE
                WHEN V.idTransaccion IS NOT NULL THEN PT.monto
                WHEN P.idTransaccion IS NOT NULL THEN PT.monto
                ELSE 0
            END)        AS Monto
FROM    dbo.Transaccion     AS T
JOIN    dbo.PagoTransaccion AS PT ON PT.idTransaccion = T.idTransaccion
JOIN    dbo.MetodoPago      AS MP ON MP.idMetodoPago = PT.idMetodoPago
LEFT JOIN dbo.Venta         AS V  ON V.idTransaccion  = T.idTransaccion
LEFT JOIN dbo.Pedido        AS P  ON P.idTransaccion  = T.idTransaccion
JOIN    dbo.Estado          AS E  ON E.idEstado = T.idEstado
                                 AND E.modulo  = N'Transaccion'
WHERE   E.nombre IN (N'Completada', N'Entregada')
GROUP BY T.fechaDia, PT.idMetodoPago, MP.nombre;
GO

/* 6) Reporte mensual por categoría */
CREATE OR ALTER VIEW dbo.vw_ReporteMensualCategoria
WITH SCHEMABINDING
AS
SELECT
    YEAR(T.fecha)   AS Anio,
    MONTH(T.fecha)  AS Mes,
    C.nombre        AS Categoria,
    COUNT(DISTINCT T.idTransaccion) AS NumTransacciones,
    SUM(DT.subtotal) AS IngresosCategoria
FROM
    dbo.DetalleTransaccion AS DT
    JOIN dbo.Producto       AS P ON P.idProducto    = DT.idProducto
    JOIN dbo.Categoria      AS C ON C.idCategoria   = P.idCategoria
    JOIN dbo.Transaccion    AS T ON T.idTransaccion = DT.idTransaccion
    JOIN dbo.Estado         AS E ON E.idEstado      = T.idEstado
                         AND E.modulo       = N'Transaccion'
WHERE
    E.nombre IN (N'Completada', N'Entregada')
GROUP BY
    YEAR(T.fecha), MONTH(T.fecha), C.nombre;
GO

/* 7) Resumen mensual por modalidad */
CREATE OR ALTER VIEW dbo.vw_ResumenMensualModalidad
WITH SCHEMABINDING
AS
SELECT
    YEAR(T.fecha) AS Anio,
    MONTH(T.fecha) AS Mes,
    SUM(CASE WHEN V.idTransaccion IS NOT NULL
                 AND E.nombre = N'Completada'
             THEN 1 ELSE 0 END) AS NumTransMinorista,
    SUM(CASE WHEN V.idTransaccion IS NOT NULL
                 AND E.nombre = N'Completada'
             THEN T.totalNeto ELSE 0 END) AS MontoMinorista,
    SUM(CASE WHEN P.idTransaccion IS NOT NULL
                 AND P.tipoPedido = N'Especial'
                 AND E.nombre = N'Entregada'
             THEN 1 ELSE 0 END) AS NumTransEspecial,
    SUM(CASE WHEN P.idTransaccion IS NOT NULL
                 AND P.tipoPedido = N'Especial'
                 AND E.nombre = N'Entregada'
             THEN T.totalNeto ELSE 0 END) AS MontoEspecial,
    SUM(CASE WHEN P.idTransaccion IS NOT NULL
                 AND P.tipoPedido = N'Domicilio'
                 AND E.nombre = N'Entregada'
             THEN 1 ELSE 0 END) AS NumPedidosDomicilio,
    SUM(CASE WHEN P.idTransaccion IS NOT NULL
                 AND P.tipoPedido = N'Domicilio'
                 AND E.nombre = N'Entregada'
             THEN T.totalNeto ELSE 0 END) AS MontoPedidosDomicilio
FROM
    dbo.Transaccion AS T
    LEFT JOIN dbo.Venta  AS V ON V.idTransaccion = T.idTransaccion
    LEFT JOIN dbo.Pedido AS P ON P.idTransaccion = T.idTransaccion
    JOIN dbo.Estado     AS E ON E.idEstado = T.idEstado
                             AND E.modulo = N'Transaccion'
GROUP BY
    YEAR(T.fecha),
    MONTH(T.fecha);
GO

/* 8) Rotacion mensual de productos */
CREATE OR ALTER VIEW dbo.vw_RotacionMensual
WITH SCHEMABINDING
AS
WITH Datos AS (
    SELECT
        YEAR(t.fecha) AS Anio,
        MONTH(t.fecha) AS Mes,
        p.idProducto,
        p.nombre AS Producto,
        SUM(dt.cantidad) AS TotalUnidadesVendidas
    FROM dbo.DetalleTransaccion AS dt
    JOIN dbo.Transaccion        AS t ON t.idTransaccion = dt.idTransaccion
    JOIN dbo.Estado             AS e ON e.idEstado = t.idEstado
                                     AND e.modulo = N'Transaccion'
                                     AND e.nombre IN (N'Completada', N'Entregada')
    JOIN dbo.Producto           AS p ON p.idProducto = dt.idProducto
    GROUP BY YEAR(t.fecha), MONTH(t.fecha), p.idProducto, p.nombre
)
SELECT
    Anio,
    Mes,
    ROW_NUMBER() OVER (PARTITION BY Anio, Mes
                       ORDER BY TotalUnidadesVendidas DESC) AS Posicion,
    idProducto,
    Producto,
    TotalUnidadesVendidas
FROM Datos;
GO

/* 9) Rotacion por rango de fechas */
CREATE OR ALTER VIEW dbo.vw_RotacionRango
WITH SCHEMABINDING
AS
SELECT
    t.fechaDia                                   AS Dia,
    p.idProducto,
    p.nombre                                     AS Producto,
    c.nombre                                     AS Categoria,
    SUM(dt.cantidad)                             AS Unidades,
    SUM(dt.cantidad * dt.precioUnitario)         AS Importe
FROM dbo.DetalleTransaccion AS dt
JOIN dbo.Transaccion        AS t ON t.idTransaccion = dt.idTransaccion
JOIN dbo.Estado             AS e ON e.idEstado = t.idEstado
                                 AND e.modulo = N'Transaccion'
                                 AND e.nombre IN (N'Completada', N'Entregada')
JOIN dbo.Producto           AS p ON p.idProducto = dt.idProducto
JOIN dbo.Categoria          AS c ON c.idCategoria = p.idCategoria
GROUP BY t.fechaDia, p.idProducto, p.nombre, c.nombre;
GO

/* 10) Clientes activos ------------------------------------------------*/
CREATE OR ALTER VIEW dbo.vw_ClientesActivos
WITH SCHEMABINDING
AS
SELECT c.idPersona
FROM   dbo.Cliente AS c
       JOIN dbo.Persona AS p ON p.idPersona = c.idPersona
       JOIN dbo.Estado  AS e ON e.idEstado = p.idEstado
WHERE  e.modulo = N'Persona'
  AND  e.nombre = N'Activo'
WITH CHECK OPTION;
GO
