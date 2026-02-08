# Descripción de vistas

A continuación se detalla el propósito de cada vista definida en `db/VW.sql`.
Cada sección indica los campos expuestos y las tablas de donde proviene la información.

## vw_ClientesFrecuentes
**Descripción:** Lista los clientes con mayor cantidad de transacciones finalizadas
(estados **Completada** o **Entregada**).
Incluye `idCliente`, el nombre completo mediante `fn_NombreCompleto` y `numCompras` que cuenta cuántas operaciones tiene cada cliente.
Proviene de `Transaccion`, `Cliente`, `Persona` y `Estado`.

## vw_HistorialTransaccionesPorCliente
**Descripción:** Muestra el historial de transacciones por cliente con su estado.
Proporciona `idCliente`, `Cliente`, `idTransaccion`, `fecha`, `totalNeto`, `descuento`, `cargo`, `Estado` y `Tipo` (Venta o Pedido).
Se originan de `Transaccion`, `Venta`, `Pedido`, `Cliente` y `Estado`.

## vw_ProductosMasVendidos
**Descripción:** Presenta los cien productos con más unidades vendidas considerando
transacciones en estado **Completada** o **Entregada**.
Expone `idProducto`, `nombre`, `UnidadesVendidas` y `Ingresos`.
La información proviene de `DetalleTransaccion`, `Producto`, `Transaccion` y `Estado`.

## vw_TransaccionesPorDia
**Descripción:** Resume por día la cantidad de transacciones y montos asociados.
Incluye `Dia`, `NumTransacciones`, `NumPedidosEntregados`, `TotalBrutoDia`, `TotalNetoDia`,
`MontoEfectivo`, `MontoBilleteraDigital`, `IngresosDia` y `NumRegistros`.
Agrupa datos de `Transaccion`, `Venta`, `Pedido`, `Estado`, `PagoTransaccion` y `MetodoPago`.

## vw_PagoMetodoDia
**Descripción:** Indica el monto pagado en cada método de pago por día.
Contiene `Dia`, `idMetodoPago`, `Metodo` y `Monto`.
Procede de `Transaccion`, `PagoTransaccion`, `MetodoPago`, `Venta`, `Pedido` y `Estado`.

## vw_ReporteMensualCategoria
**Descripción:** Entrega un resumen mensual de ventas por categoría de producto.
Muestra `Anio`, `Mes`, `Categoria`, `NumTransacciones` e `IngresosCategoria`.
Proviene de `DetalleTransaccion`, `Producto`, `Categoria`, `Transaccion` y `Estado`.

## vw_ResumenMensualModalidad
**Descripción:** Resume cada mes las ventas minoristas y pedidos especiales o de domicilio.
Registra `Anio`, `Mes`, `NumTransMinorista`, `MontoMinorista`, `NumTransEspecial`,
`MontoEspecial`, `NumPedidosDomicilio` y `MontoPedidosDomicilio`.
Utiliza datos de `Transaccion`, `Venta`, `Pedido` y `Estado`.

## vw_RotacionMensual
**Descripción:** Clasifica los productos por unidades vendidas cada mes.
Devuelve `Anio`, `Mes`, `Posicion`, `idProducto`, `Producto` y `TotalUnidadesVendidas`.
Se basa en `DetalleTransaccion`, `Transaccion`, `Estado` y `Producto`.

## vw_RotacionRango
**Descripción:** Informa la rotación de productos en un rango de fechas.
Expone `Dia`, `idProducto`, `Producto`, `Categoria`, `Unidades` e `Importe`.
Los datos provienen de `DetalleTransaccion`, `Transaccion`, `Estado`, `Producto` y `Categoria`.

## vw_ClientesActivos
**Descripción:** Lista los clientes cuyo estado de persona es activo.
Incluye únicamente `idPersona` de `Cliente` cuando la tabla `Persona` tiene estado
`Activo` en el módulo `Persona`.

