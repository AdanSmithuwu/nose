# Formularios y diálogos de la UI

Este documento enumera las clases de la interfaz de usuario presentes en el módulo `presentation-ui`.
Se incluyen únicamente aquellas cuyo nombre termina en `Form`, `Dlg` o `Dialog`.

## Forms

| Nombre | Descripción |
| ------ | ----------- |
| BaseForm | Formulario base que provee disposiciones y utilidades comunes. |
| LoginForm | Formulario de acceso para Comercial's Valerio. |
| MainForm | Contenedor principal que carga los paneles y registra atajos. |
| FormBitacoraLogin | Bitácora de accesos filtrable por fecha, empleado y resultado. |
| FormCategorias | Panel para listar y administrar categorías. |
| FormClientes | Panel para listar y administrar clientes. |
| FormDashboard | Dashboard con métricas y tablas de productos más vendidos. |
| FormEmpleados | Panel para listar y administrar empleados. |
| FormHistorialCliente | Muestra el historial de transacciones de un cliente. |
| FormHistorialInventario | Historial de movimientos de inventario con múltiples filtros. |
| FormMantenimiento | Herramientas avanzadas de mantenimiento para administradores. |
| FormParametros | Lista editable de parámetros del sistema. |
| FormPedido | Formulario base para registrar pedidos. |
| FormPedidoDomicilio | Versión de FormPedido para pedidos de entrega a domicilio. No permite superar el stock disponible. |
| FormPedidoEspecial | Versión de FormPedido para pedidos "especiales". |
| FormSeguimientoPedidos | Seguimiento de pedidos con filtros por producto y fechas. |
| FormGestionInventario | Gestión de stock y tallas disponibles en bodega. |
| FormGestionProductos | Gestión de productos y tipos asociados. |
| FormReporteDiario | Reporte diario de ventas, pedidos y pagos. |
| FormReporteMensual | Reporte mensual con totales y resumen por categoría. |
| FormReporteRotacion | Reporte de rotación o movimiento de productos. |
| FormSeguimientoVentas | Seguimiento de ventas con filtros por categoría y producto. |
| FormVenta | Formulario para registrar ventas al contado. |

## Dialogs

| Nombre | Descripción |
| ------ | ----------- |
| BaseDialog | Diálogo base que registra atajos comunes y permite fijar tamaño. |
| DlgAlertasStock | Muestra alertas de productos con stock pendiente. |
| DlgCategoriaEditar | Editar una categoría existente; reutiliza los campos del diálogo de creación. |
| DlgCategoriaNueva | Registrar una nueva categoría. |
| DlgClienteEditar | Editar datos de un cliente reutilizando el diálogo de nuevo cliente. |
| DlgClienteNuevo | Registrar un nuevo cliente. |
| DlgMotivoCancelacion | Ingresar el motivo de cancelación de un pedido o venta. |
| DlgObservacion | Editar o registrar una observación multilínea. |
| DlgCredencialesGeneradas | Muestra las credenciales generadas para un empleado. |
| DlgEmpleadoCredenciales | Actualizar usuario y contraseña de un empleado. |
| DlgEmpleadoEditar | Editar un empleado existente. |
| DlgEmpleadoNuevo | Registrar un nuevo empleado. |
| DlgHistorialCliente | Muestra el historial de transacciones para un cliente. |
| DlgParametroEditar | Editar un parámetro del sistema. |
| DlgComprobantePedido | Generar el comprobante tras entregar un pedido. |
| DlgPagoPedido | Registrar los montos de pago para marcar un pedido entregado. |
| DlgPedidoDetalle | Muestra el detalle completo de un pedido. |
| DlgPedidoEditar | Editar un pedido existente usando la misma disposición del alta. |
| DlgProductoDetalle | Muestra información del producto junto a sus presentaciones. |
| DlgProductoEditar | Editar un producto reutilizando el diálogo de registro. |
| DlgProductoNuevo | Registrar un nuevo producto. |
| DlgComprobante | Confirmar la generación de una boleta o comprobante de venta. |
| DlgVentaDetalle | Muestra el detalle completo de una venta. |
