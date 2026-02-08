# Flujo de artefactos de base de datos por caso de uso

Este documento describe para cada caso de uso qué procedimientos almacenados, vistas y disparadores participan en la persistencia. Se indica el script donde se define cada objeto y la clase principal que lo invoca, siguiendo el mapeo de [docs/database/usage.md](../database/usage.md). No se detallan las capas de API ni las pantallas de la interfaz.

## UC01 Iniciar sesión
1. `EmpleadoRepositoryImpl` ejecuta `sp_AssertEmpleadoContext` (db/SP.sql) para validar la cuenta y establecer la sesión.
2. La actualización en `Empleado` activa `trg_Empleado_LoginHandling` (db/Triggers.sql), el cual registra la fecha y controla intentos en `BitacoraLogin`.

## UC02 Cerrar sesión
1. No existe procedimiento almacenado. `EmpleadoRepositoryImpl` actualiza `ultimoAcceso` directamente.
2. El `UPDATE` ejecuta `trg_Empleado_LoginHandling`, que registra la salida y reinicia contadores.

## UC03 Consultar bitácora de accesos
1. No hay procedimiento almacenado; el repositorio consulta la vista `BitacoraLogin`.
2. No se ejecutan triggers.

## UC04 Registrar empleado
1. `sp_RegistrarEmpleado` (db/SP.sql) inserta los registros en `Persona` y `Empleado`.
2. Luego `trg_Persona_ValidarEstado` comprueba el estado inicial de la persona.

## UC05 Editar empleado
1. No existe procedimiento almacenado; el repositorio actualiza las tablas.
2. `trg_Persona_ValidarEstado` valida el nuevo estado tras el `UPDATE`.

## UC06 Desactivar empleado
1. Sin procedimiento almacenado. El repositorio cambia el estado en `Persona`.
2. `trg_Persona_ValidarEstado` impide desactivaciones no permitidas.

## UC07 Actualizar credenciales de empleado
1. No existe procedimiento almacenado. `EmpleadoRepositoryImpl.updateCredenciales` ejecuta un `UPDATE` directo.
2. No hay triggers asociados.

## UC08 Eliminar empleado
1. El repositorio elimina registros sin procedimiento almacenado.
2. Si se modifican roles, el trigger `trg_Rol_AdminOnly` llama a `sp_CheckAdminTrigger` para validar privilegios.

## UC09 Registrar cliente
1. `sp_RegistrarCliente` (db/SP.sql) crea registros en `Persona` y `Cliente`.
2. `trg_Persona_ValidarEstado` valida el estado al terminar el `INSERT`.

## UC10 Editar cliente
1. No se usa procedimiento almacenado; se realiza un `UPDATE` por JPA.
2. Tras la actualización se ejecuta `trg_Persona_ValidarEstado`.

## UC11 Desactivar cliente
1. El cambio de estado se hace sin procedimiento almacenado.
2. `trg_Persona_ValidarEstado` comprueba la transición.

## UC12 Eliminar cliente
1. No existe procedimiento almacenado; el repositorio elimina la fila.
2. No se ejecutan triggers.

## UC13 Consultar historial de cliente
1. El repositorio lee la vista `vw_HistorialTransaccionesPorCliente` (db/VW.sql).
2. No intervienen triggers ni procedimientos adicionales.

## UC14 Registrar categoría
1. `sp_InsertCategoria` (db/SP.sql) inserta la nueva categoría y devuelve su id.
2. No intervienen triggers.

## UC15 Editar categoría
1. `sp_UpdateCategoria` (db/SP.sql) modifica nombre y descripción de la categoría.
2. No hay triggers.

## UC16 Desactivar categoría
1. `sp_CambiarEstadoCategoria` (db/SP.sql) cambia el estado del registro.
2. No intervienen triggers.

## UC17 Eliminar categoría
1. `sp_DeleteCategoria` (db/SP.sql) elimina la categoría y llama a `sp_CheckAdminTrigger` para verificar permisos.
2. No hay triggers.

## UC18 Gestionar productos
1. Sin procedimiento almacenado; `ProductoRepositoryImpl` lee catálogos y vistas de soporte.
2. No se ejecutan triggers.

## UC19 Registrar producto unidad fija
1. No se define procedimiento almacenado; el repositorio inserta el producto.
2. Tras la inserción, `trg_Producto_ValidateAndAdjust` ajusta el stock y valida el registro.

## UC20 Registrar producto vestimenta
1. Sin procedimiento almacenado. El producto y sus tallas se insertan por JPA.
2. Primero se ejecuta `trg_Producto_ValidateAndAdjust` y luego `trg_TallaStock_ValidateAndUpdate` para sincronizar el stock por talla.

## UC21 Registrar producto fraccionable
1. `sp_RegistrarProductoFraccionable` (db/SP.sql) inserta el producto con sus presentaciones.
2. Luego `trg_Producto_ValidateAndAdjust` valida el producto y `trg_Presentacion_Validate` revisa los precios de las presentaciones.

## UC22 Editar producto unidad fija
1. No hay procedimiento almacenado; el repositorio actualiza el producto.
2. `trg_Producto_ValidateAndAdjust` revisa el cambio y ajusta el stock si es necesario.

## UC23 Editar producto vestimenta
1. Sin procedimiento almacenado; las actualizaciones se hacen por JPA.
2. Se ejecuta primero `trg_Producto_ValidateAndAdjust` y después `trg_TallaStock_ValidateAndUpdate` para recalcular stock.

## UC24 Editar producto fraccionable
1. No existe procedimiento almacenado. El repositorio actualiza el producto y sus presentaciones.
2. `trg_Producto_ValidateAndAdjust` se dispara primero y `trg_Presentacion_Validate` valida las presentaciones después.

## UC25 Desactivar producto
1. El cambio de estado se realiza sin procedimiento almacenado.
2. `trg_Producto_ValidateAndAdjust` recalcula el stock y genera alertas.

## UC26 Eliminar producto
1. No hay procedimiento almacenado; el repositorio elimina la fila.
2. No se ejecutan triggers.

## UC27 Ingresar stock
1. `sp_AplicarAjusteInventario` (db/SP.sql) registra el movimiento de entrada.
2. El `INSERT` invoca `trg_MovInv_ValidateAndUpdate`, que actualiza el stock y valida el movimiento.

## UC28 Ajustar stock
1. `sp_AplicarAjusteInventario` se ejecuta con el tipo de movimiento adecuado.
2. `trg_MovInv_ValidateAndUpdate` valida y aplica el ajuste en inventario.

## UC29 Consultar historial de inventario
1. El repositorio consulta la vista `vw_RotacionRango` (db/VW.sql).
2. No intervienen triggers ni procedimientos.

## UC30 Registrar venta
1. `sp_RegistrarVenta` (db/SP.sql) crea la transacción y registra el detalle y los pagos.
2. Después del `INSERT`, `trg_DetalleTransaccion_Maintenance` valida cada línea, `trg_PagoTransaccion_CheckSum` verifica la suma de pagos y `trg_Venta_Insert` marca la venta como completada.

## UC31 Cancelar venta
1. `sp_CancelarVenta` (db/SP.sql) cambia el estado de la transacción y registra la cancelación.
2. El cambio activa `trg_Transaccion_Update`, que registra un `MovimientoInventario` para revertir el stock del detalle.

## UC32 Generar comprobante
1. `sp_GenerarComprobante` (db/SP.sql) almacena el PDF y los datos del comprobante.
2. `trg_Comprobante_Insert` verifica que la transacción corresponda a una venta o pedido válido.

## UC33 Imprimir comprobante
1. Lectura del PDF almacenado sin procedimiento asociado.
2. No se ejecutan triggers.

## UC34 Registrar pedido
1. `sp_RegistrarPedido` (db/SP.sql) crea la transacción y genera la orden de compra.
2. Después se ejecutan `trg_Pedido_Validate` y `trg_DetalleTransaccion_Maintenance` para validar fechas y cantidades.

## UC35 Editar pedido
1. `sp_ModificarPedido` (db/SP.sql) aplica los cambios solicitados.
2. Luego `trg_Pedido_Validate` y `trg_DetalleTransaccion_Maintenance` revisan la coherencia del pedido.

## UC36 Cancelar pedido
1. `sp_CancelarVenta` se usa con el id de la transacción del pedido.
2. `trg_Transaccion_Update` genera un `MovimientoInventario` y deja registro de la cancelación.

## UC37 Registrar pedido especial
1. Se inserta un nuevo `Pedido` sin procedimiento almacenado y sin descontar inventario.
2. `trg_Pedido_Validate` comprueba cliente y fechas válidas.

## UC38 Entregar pedido
1. `sp_ActualizarEstadoPedido` (db/SP.sql) cambia el estado a entregado.
2. El `UPDATE` ejecuta `trg_Transaccion_Update`, que valida el cierre; los movimientos de inventario se registran en otros procedimientos.

## UC39 Imprimir orden de pedido
1. Lectura del PDF guardado; no hay procedimiento asociado.
2. No se ejecutan triggers.

## UC40 Visualizar detalle de transacción
1. El repositorio lee la vista correspondiente según el tipo de transacción.
2. No intervienen procedimientos ni disparadores.

## UC41 Consultar ventas y pedidos
1. Sin procedimiento almacenado; los repositorios consultan `Venta`, `Pedido` y la vista `vw_TransaccionesPorDia`.
2. No se ejecutan triggers.

## UC42 Generar reporte diario
1. `sp_GenerarReporteDiario` (db/SP.sql) compila la información y genera el PDF.
2. No intervienen triggers.

## UC43 Generar reporte mensual
1. `sp_GenerarReporteMensual` (db/SP.sql) produce el reporte correspondiente.
2. No hay triggers.

## UC44 Generar reporte de rotación
1. `sp_GenerarReporteRotacion` (db/SP.sql) utiliza la vista `vw_RotacionMensual` para calcular la rotación.
2. No se ejecutan triggers.

## UC45 Actualizar parámetros del sistema
1. No se define procedimiento almacenado; el repositorio modifica `ParametroSistema` directamente.
2. `trg_ParametroSistema_AdminOnly` verifica que sea un administrador quien realiza el cambio.
