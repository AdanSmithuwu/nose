# Descripción de procedimientos almacenados

Los procedimientos que emplean cursores (`sp_ListarClientesFrecuentes`,
`sp_ListarAlertasPendientes`, `sp_ListarPedidosPendientes`,
`sp_RecalcularStockProductos`, `sp_DepurarBitacoraLogin`) se documentan por
separado en [sp_cursor_docs.md](sp_cursor_docs.md).

## sp_DisableSeedTriggers
- Descripción funcional: Deshabilita temporalmente los triggers de protección para realizar la
  carga inicial de datos.
- Objetivo: Permitir la inserción de registros sin las validaciones de los triggers.
- Explicación de parámetros:
(no tiene parámetros)
- Explicación clara de su función: Verifica si existen los triggers `trg_Rol_AdminOnly`,
  `trg_ParametroSistema_AdminOnly` y `trg_Persona_ValidarEstado`; de ser así los deshabilita.
- Resultado esperado del procedimiento: Los triggers indicados quedan deshabilitados.
- Tipo de operación: Write
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: tablas `Rol`, `ParametroSistema`, `Persona` y sus triggers asociados.

## sp_EnableSeedTriggers
- Descripción funcional: Activa nuevamente los triggers de protección después de la carga inicial.
- Objetivo: Restaurar las validaciones de los triggers tras la carga de datos.
- Explicación de parámetros:
(no tiene parámetros)
- Explicación clara de su función: Si existen `trg_Rol_AdminOnly`, `trg_ParametroSistema_AdminOnly`
  y `trg_Persona_ValidarEstado`, los habilita.
- Resultado esperado del procedimiento: Los triggers quedan habilitados.
- Tipo de operación: Write
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: tablas `Rol`, `ParametroSistema`, `Persona` y sus triggers asociados.

## sp_SetSessionFlags
- Descripción funcional: Establece indicadores en la sesión para que los triggers omitan ciertas
  validaciones.
- Objetivo: Controlar el comportamiento de los triggers durante operaciones específicas.
- Explicación de parámetros:
  - @skipSubtipoCheck (BIT): Indica si se omite la verificación de subtipo de pedido.
  - @skipBrutoUpdate (BIT): Indica si se evita actualizar el total bruto en disparadores.
- Explicación clara de su función: Valida que los parámetros sean 0 o 1 y los guarda en la sesión
  mediante `sp_set_session_context`.
- Resultado esperado del procedimiento: Los valores quedan disponibles para los triggers en la
  sesión actual.
- Tipo de operación: Write
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Ninguna.
- Descripción: `LoginForm` establece el contexto de sesión y este SP se invoca desde los servicios para asegurar que el usuario siga autenticado.

## sp_ValidarEstado
- Descripción funcional: Verifica que un identificador de estado corresponda al módulo especificado.
- Objetivo: Evitar inconsistencias al asociar estados con módulos incorrectos.
- Explicación de parámetros:
  - @idEstado (INT): Identificador del estado a validar.
  - @modulo (NVARCHAR(20)): Nombre del módulo al que debe pertenecer el estado.
  - @error (INT): Código de error a lanzar si no se cumple la validación.
- Explicación clara de su función: Utiliza `fn_AssertEstadoModulo` y si el resultado es cero genera
  una excepción personalizada.
- Resultado esperado del procedimiento: Lanza error cuando el estado no pertenece al módulo indicado.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Tabla `Estado` y función `fn_AssertEstadoModulo`.

## sp_PrepararTransaccion
- Descripción funcional: Calcula totales y valida los datos de una transacción antes de registrarla.
- Objetivo: Obtener el monto bruto, descuentos, cargos y validar los pagos recibidos.
- Explicación de parámetros:
  - @detalle (tvp_DetalleTx READONLY): Detalle de productos y cantidades.
  - @pagos (tvp_PagoTx READONLY): Lista de pagos recibidos.
  - @usaValeGas (BIT): Indica si se aplica descuento de vale de gas.
  - @cargo (DECIMAL(10,2)): Cargo manual a aplicar.
  - @validarPagos (BIT): Si es 1 compara la suma de pagos con el total bruto.
  - @checkEntero (BIT): Valida cantidades enteras cuando es 1.
  - @errorDetalle (INT): Código de error si el detalle está vacío.
  - @errorEntero (INT): Código de error para cantidades no enteras.
  - @errorCargo (INT): Código de error si el cargo es negativo.
  - @errorPagos (INT): Código de error cuando los pagos no coinciden con el total.
  - @totalBruto (DECIMAL(10,2) OUTPUT): Total sin descuentos ni cargos.
  - @descuento (DECIMAL(10,2) OUTPUT): Descuento aplicado por vale de gas.
  - @cargoCalculado (DECIMAL(10,2) OUTPUT): Cargo calculado según parámetro o cargo actual.
  - @totalPagos (DECIMAL(10,2) OUTPUT): Suma de los pagos recibidos.
- Explicación clara de su función: Verifica que exista detalle, valida cantidades, calcula el total
  bruto y pagos, opcionalmente compara totales y determina cargo y descuento.
- Resultado esperado del procedimiento: Devuelve totales listos para registrar una transacción o
  genera errores de validación.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Funciones `fn_CargoRepartoActual` y `fn_DescuentoValeGas`.

## sp_AssertEmpleadoContext
- Descripción funcional: Valida que la sesión actual tenga asociado un empleado.
- Objetivo: Garantizar que todas las operaciones se ejecuten con un contexto de empleado válido.
- Explicación de parámetros:
(no tiene parámetros)
- Explicación clara de su función: Comprueba `SESSION_CONTEXT('idEmpleado')` y lanza el error 50083
  si no existe.
- Resultado esperado del procedimiento: Se detiene la ejecución cuando no hay un empleado en la
  sesión.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Ninguna.

## sp_AssertAdmin
- Descripción funcional: Comprueba que el empleado de la sesión tenga nivel de administrador.
- Objetivo: Restringir operaciones a usuarios con rol de administrador.
- Explicación de parámetros:
(no tiene parámetros)
- Explicación clara de su función: Llama a `sp_AssertEmpleadoContext`, obtiene el nivel con
  `fn_actor_nivel` y lanza errores 50084 o 50085 según corresponda.
- Resultado esperado del procedimiento: Solo continúa si el usuario es administrador.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Función `fn_actor_nivel` y procedimiento `sp_AssertEmpleadoContext`.

## sp_CheckAdminTrigger
- Descripción funcional: Procedimiento auxiliar para invocar desde disparadores y verificar
  privilegios de administrador.
- Objetivo: Evitar modificaciones no autorizadas cuando un disparador lo requiere.
- Explicación de parámetros:
(no tiene parámetros)
- Explicación clara de su función: Ejecuta `sp_AssertAdmin` para validar el nivel del actor.
- Resultado esperado del procedimiento: Genera error si quien ejecuta el disparador no es
  administrador.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Procedimiento `sp_AssertAdmin`.

## sp_AplicarAjusteInventario
- Descripción funcional: Registra movimientos de inventario según el detalle de una transacción.
- Objetivo: Actualizar el stock mediante un movimiento de tipo entrada o salida.
- Explicación de parámetros:
  - @idTransaccion (INT): Transacción cuyo detalle se procesa.
  - @tipoMovimiento (NVARCHAR(20)): Nombre del tipo de movimiento a aplicar.
- Explicación clara de su función: Obtiene el id del tipo de movimiento, construye un motivo
  contextual y registra cada línea del detalle en `MovimientoInventario` junto con el empleado de la
  transacción.
- Resultado esperado del procedimiento: Filas insertadas en `MovimientoInventario` y el disparador
  asociado actualiza stock.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Tablas `DetalleTransaccion`, `Transaccion`, `Pedido`, `Venta`,
  `MovimientoInventario`, `TipoMovimiento` y disparador `trg_MovInv_ValidateAndUpdate`.
- Descripción: `FormGestionInventario` utiliza este procedimiento para aplicar entradas o salidas manuales al confirmar un ajuste.

## sp_DescontarStock_Detalle
- Descripción funcional: Descuenta unidades de inventario según el detalle de una transacción.
- Objetivo: Registrar la salida de stock de forma transaccional.
- Explicación de parámetros:
  - @idTransaccion (INT): Transacción cuyas líneas disminuyen el stock.
- Explicación clara de su función: Abre una transacción si no existe, llama a
  `sp_AplicarAjusteInventario` con tipo `Salida` y confirma o revierte según el resultado.
- Resultado esperado del procedimiento: El inventario queda ajustado y la transacción se confirma.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Procedimiento `sp_AplicarAjusteInventario` y tablas de inventario
  relacionadas.

## sp_InsertCategoria
- Descripción funcional: Inserta una nueva categoría de producto.
- Objetivo: Registrar categorías con su descripción inicial en estado activo.
- Explicación de parámetros:
  - @nombre (NVARCHAR(40)): Nombre de la categoría.
  - @descripcion (NVARCHAR(120)): Descripción detallada.
  - @newIdCategoria (INT OUTPUT): Identificador asignado a la nueva categoría.
- Explicación clara de su función: Valida que el nombre no exista, inserta el registro y devuelve
  el id generado.
- Resultado esperado del procedimiento: Una fila en `Categoria` y el id de salida establecido.
- Tipo de operación: Write
- Nivel de impacto: Bajo
- Transaccionalidad: Sí
- Dependencias externas: Tabla `Categoria` y función `fn_estado`.
- Descripción: `DlgCategoriaNueva` invoca este procedimiento al confirmar el registro para crear la categoría mostrada luego en `FormCategorias`.

## sp_UpdateCategoria
- Descripción funcional: Actualiza los datos de una categoría existente.
- Objetivo: Modificar nombre y descripción manteniendo la integridad de la tabla.
- Explicación de parámetros:
  - @idCategoria (INT): Identificador de la categoría a modificar.
  - @nombre (NVARCHAR(40)): Nuevo nombre a asignar.
  - @descripcion (NVARCHAR(120)): Nueva descripción.
- Explicación clara de su función: Comprueba duplicados, actualiza el registro y notifica si la
  categoría no existe.
- Resultado esperado del procedimiento: La categoría queda actualizada o se lanza un error.
- Tipo de operación: Write
- Nivel de impacto: Bajo
- Transaccionalidad: Sí
- Dependencias externas: Tabla `Categoria`.
- Descripción: `DlgCategoriaEditar` utiliza este procedimiento para guardar los cambios realizados desde `FormCategorias`.

## sp_DeleteCategoria
- Descripción funcional: Elimina una categoría siempre que no tenga productos asociados.
- Objetivo: Mantener la consistencia evitando borrar categorías en uso.
- Explicación de parámetros:
  - @idCategoria (INT): Categoría que se desea eliminar.
- Explicación clara de su función: Valida privilegios de administrador, verifica que no existan
  productos relacionados y ejecuta la eliminación.
- Resultado esperado del procedimiento: La categoría es removida de la tabla o se notifica el motivo
  del fallo.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Categoria`, `Producto` y procedimiento `sp_AssertAdmin`.
- Descripción: `FormCategorias` usa este procedimiento al eliminar una fila para quitarla permanentemente de la base de datos.

## sp_CambiarEstadoCategoria
- Descripción funcional: Cambia el estado de una categoría y opcionalmente actualiza los productos
  vinculados.
- Objetivo: Gestionar la activación o desactivación de categorías y sus productos.
- Explicación de parámetros:
  - @idCategoria (INT): Categoría a modificar.
  - @nuevoEstado (NVARCHAR(30)): Nombre del estado destino.
  - @actualizarProductos (BIT): Si es 1 modifica el estado de los productos relacionados.
  - @numProductos (INT OUTPUT): Cantidad de productos afectados.
- Explicación clara de su función: Obtiene los identificadores de estado, actualiza la categoría y
  según las opciones actualiza también los productos contando las filas modificadas.
- Resultado esperado del procedimiento: Estado de la categoría modificado y número de productos
  actualizados devuelto.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Categoria`, `Producto` y función `fn_estado`.
- Descripción: Desde `FormCategorias` se utiliza para activar o desactivar registros junto con sus productos relacionados.

## sp_RegistrarVenta
- Descripción funcional: Registra una venta directa con sus pagos y movimientos de inventario.
- Objetivo: Crear la transacción de venta dejando constancia de detalle y cobros.
- Explicación de parámetros:
  - @idEmpleado (INT): Empleado que realiza la venta.
  - @idCliente (INT): Cliente asociado.
  - @observacion (NVARCHAR(120)): Comentario opcional.
  - @detalle (tvp_DetalleTx READONLY): Productos vendidos.
  - @pagos (tvp_PagoTx READONLY): Detalle de pagos de la venta.
  - @idTransaccion (INT OUTPUT): Identificador de la transacción creada.
  - Explicación clara de su función: Valida datos, calcula totales con `sp_PrepararTransaccion`,
    inserta la transacción, el detalle y los pagos, registra la fila en `Venta` y
    finalmente descuenta inventario con `sp_DescontarStock_Detalle`.
- Resultado esperado del procedimiento: Venta registrada y `@idTransaccion` devuelto.
- Tipo de operación: Write
- Nivel de impacto: Alto
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Transaccion`, `DetalleTransaccion`, `PagoTransaccion`, `Venta` y
  procedimiento `sp_DescontarStock_Detalle`.
- Descripción: `FormVenta` ejecuta este procedimiento al registrar la venta y luego muestra el comprobante generado.

## sp_RegistrarPedido
- Descripción funcional: Registra un pedido y rebaja inventario si es a domicilio.
- Objetivo: Crear la transacción de pedido calculando cargos y descuentos.
- Explicación de parámetros:
  - @idEmpleado (INT): Empleado que registra el pedido.
  - @idCliente (INT): Cliente destinatario.
  - @observacion (NVARCHAR(120)): Nota opcional del pedido.
  - @direccionEntrega (NVARCHAR(120)): Dirección donde se entregará.
  - @usaValeGas (BIT): Indica si aplica descuento por vale de gas.
  - @cargo (DECIMAL(10,2)): Cargo por envío o manipulación.
  - @detalle (tvp_DetalleTx READONLY): Detalle de productos solicitados.
  - @idTransaccion (INT OUTPUT): Identificador creado.
- Explicación clara de su función: Calcula totales con `sp_PrepararTransaccion`, inserta la
  transacción y el pedido, registra el detalle y si corresponde descuenta inventario.
- Resultado esperado del procedimiento: Pedido registrado y `@idTransaccion` devuelto.
- Tipo de operación: Write
- Nivel de impacto: Alto
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Transaccion`, `Pedido`, `DetalleTransaccion` y procedimiento
  `sp_DescontarStock_Detalle`.
- Descripción: Los formularios `FormPedido` y variantes llaman a este procedimiento al registrar la orden para actualizar inventario y mostrar el resumen.

## sp_ModificarPedido
- Descripción funcional: Actualiza un pedido existente reasignando inventario según corresponda.
- Objetivo: Permitir cambios de detalle, dirección y opciones de un pedido en proceso.
- Explicación de parámetros:
  - @idTransaccion (INT): Pedido a modificar.
  - @observacion (NVARCHAR(120)): Observación nueva.
  - @direccionEntrega (NVARCHAR(120)): Dirección actualizada.
  - @usaValeGas (BIT): Indica si aplica descuento por vale de gas.
  - @cargo (DECIMAL(10,2)): Nuevo cargo por envío.
  - @detalle (tvp_DetalleTx READONLY): Nuevo detalle del pedido.
- Explicación clara de su función: Verifica que el pedido esté en proceso, calcula totales y
  actualiza transacción y pedido. Devuelve stock previo si era a domicilio y aplica el nuevo detalle.
- Resultado esperado del procedimiento: Pedido modificado y stock ajustado de acuerdo al tipo actual.
- Tipo de operación: Write
- Nivel de impacto: Alto
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Transaccion`, `Pedido`, `DetalleTransaccion` y procedimientos
  `sp_AplicarAjusteInventario`, `sp_DescontarStock_Detalle`.
- Descripción: `DlgPedidoEditar` invoca este procedimiento para recalcular totales y actualizar el pedido mostrado en `FormSeguimientoPedidos`.

## sp_CancelarVenta
- Descripción funcional: Cambia el estado de una venta a cancelada y guarda el motivo.
- Objetivo: Permitir la anulación de ventas registradas.
- Explicación de parámetros:
  - @idTransaccion (INT): Transacción de venta a cancelar.
  - @motivoCancelacion (NVARCHAR(120)): Razón de la cancelación.
- Explicación clara de su función: Verifica que la transacción sea una venta, actualiza su estado
  y almacena el motivo; los triggers revertirán stock y cargos.
- Resultado esperado del procedimiento: Venta en estado cancelado.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Transaccion`, `Venta` y triggers de ajuste de stock.
- Descripción: `DlgMotivoCancelacion` ejecuta este procedimiento para anular ventas o pedidos desde `FormSeguimientoPedidos`.

## sp_ActualizarEstadoPedido
- Descripción funcional: Modifica el estado de un pedido registrando información de entrega o
  cancelación.
- Objetivo: Gestionar el ciclo de vida de los pedidos según su progreso.
- Explicación de parámetros:
  - @idTransaccion (INT): Pedido a actualizar.
  - @nuevoEstado (NVARCHAR(20)): Estado destino.
  - @comentario (NVARCHAR(120)): Observación opcional.
  - @fechaHoraEntrega (DATETIME2): Fecha real de entrega si aplica.
  - @idEmpleadoEntrega (INT): Empleado que entrega el pedido.
- Explicación clara de su función: Valida el nuevo estado, obtiene el empleado de entrega,
  actualiza la transacción y la tabla `Pedido`. Si se marca como entregada también actualiza
  `OrdenCompra`.
- Resultado esperado del procedimiento: Pedido con su estado y datos de entrega actualizados.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Pedido`, `Transaccion`, `Estado`, `OrdenCompra` y función
  `fn_actor_id`.
- Descripción: `FormSeguimientoPedidos` llama este SP para marcar pedidos como entregados o cancelados según la acción del usuario.

## sp_GenerarReporteDiario
- Descripción funcional: Obtiene el resumen de transacciones realizadas en una fecha específica.
- Objetivo: Proporcionar métricas diarias de ventas y pagos.
- Explicación de parámetros:
  - @fecha (DATE): Día del reporte.
- Explicación clara de su función: Valida la fecha y consulta la vista `vw_TransaccionesPorDia`
  devolviendo cifras de ventas, montos y métodos de pago.
- Resultado esperado del procedimiento: Conjunto de registros con datos resumidos para la fecha dada.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Vista `vw_TransaccionesPorDia`.
- Descripción: `FormReporteDiario` solicita los datos a través de este procedimiento y luego permite imprimir o exportar el reporte.

## sp_GenerarReporteMensual
- Descripción funcional: Genera reportes de ventas y resúmenes por categoría en un mes determinado.
- Objetivo: Analizar el desempeño mensual con la opción de incluir un resumen general.
- Explicación de parámetros:
  - @anio (INT): Año del reporte.
  - @mes (INT): Mes del reporte.
  - @conResumen (BIT): Si es 1 agrega la sección de resumen por modalidades.
- Explicación clara de su función: Calcula el rango de fechas del mes, obtiene ventas por día, por
  categoría y opcionalmente un resumen de modalidades desde distintas vistas.
- Resultado esperado del procedimiento: Tres conjuntos de resultados con estadísticas mensuales.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Vistas `vw_ReporteMensualCategoria` y `vw_ResumenMensualModalidad`.
- Descripción: `FormReporteMensual` usa este procedimiento para cargar el resumen del mes seleccionado y ofrecer la exportación a PDF.

## sp_GenerarReporteRotacion
- Descripción funcional: Obtiene el ranking de productos más vendidos en un rango de fechas.
- Objetivo: Conocer la rotación de inventario y los ingresos generados.
- Explicación de parámetros:
  - @desde (DATETIME2): Fecha inicial del periodo.
  - @hasta (DATETIME2): Fecha final, debe ser mayor a la inicial.
  - @top (INT): Número máximo de productos a mostrar; si es NULL muestra todos.
- Explicación clara de su función: Valida las fechas y el parámetro `@top`, calcula una tabla
  ordenada por unidades vendidas y retorna las filas según el límite indicado.
- Resultado esperado del procedimiento: Lista de productos con sus unidades e importe total dentro
  del periodo.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Tablas `DetalleTransaccion`, `Transaccion`, `Producto`, `Categoria` y vista
  de estados.
- Descripción: `FormReporteRotacion` utiliza este procedimiento para generar el ranking que se muestra en pantalla y permite su exportación.

## sp_ValidarPersonaBasica
- Descripción funcional: Verifica datos esenciales de una persona antes de su registro.
- Objetivo: Garantizar la unicidad del DNI y la validez del teléfono y estado.
- Explicación de parámetros:
  - @dni (CHAR(8)): Documento nacional de identidad.
  - @telefono (NVARCHAR(15) OUTPUT): Teléfono normalizado devuelto.
  - @idEstado (INT OUTPUT): Estado final validado o asignado.
- Explicación clara de su función: Comprueba que no exista el DNI, valida formato de DNI y
  teléfono, normaliza el número y determina el estado por defecto cuando no se provee.
- Resultado esperado del procedimiento: Sin errores si los datos son válidos y parámetros de salida
  ajustados.
- Tipo de operación: Read
- Nivel de impacto: Bajo
- Transaccionalidad: No
- Dependencias externas: Funciones `fn_EsDniValido`, `fn_NormalizarTelefono`, `fn_EsTelefonoValido` y
  `fn_estado`; tabla `Persona`.

## sp_RegistrarCliente
- Descripción funcional: Inserta una nueva persona y la registra como cliente.
- Objetivo: Crear un cliente con validaciones completas de identidad y dirección.
- Explicación de parámetros:
  - @nombres (NVARCHAR(60)): Nombres de la persona.
  - @apellidos (NVARCHAR(60)): Apellidos de la persona.
  - @dni (CHAR(8)): Documento nacional de identidad.
  - @telefono (NVARCHAR(15)): Número telefónico opcional.
  - @direccion (NVARCHAR(120)): Dirección del cliente.
  - @idEstado (INT): Estado inicial opcional de la persona.
  - @newIdPersona (INT OUTPUT): Identificador resultante.
- Explicación clara de su función: Valida los datos básicos con `sp_ValidarPersonaBasica`, revisa
  que la dirección sea obligatoria y luego inserta en `Persona` y `Cliente`.
- Resultado esperado del procedimiento: Cliente registrado y id de persona devuelto.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Persona`, `Cliente` y procedimiento `sp_ValidarPersonaBasica`.
- Descripción: `DlgClienteNuevo` llama a este procedimiento al registrar un nuevo cliente desde `FormClientes`.

## sp_RegistrarEmpleado
- Descripción funcional: Registra un nuevo empleado con su usuario y rol.
- Objetivo: Crear personal autorizado verificando jerarquía y datos personales.
- Explicación de parámetros:
  - @nombres (NVARCHAR(60)): Nombres del empleado.
  - @apellidos (NVARCHAR(60)): Apellidos del empleado.
  - @dni (CHAR(8)): Documento de identidad.
  - @telefono (NVARCHAR(15)): Teléfono de contacto.
  - @fechaRegistro (DATE): Fecha de inicio, por defecto la actual.
  - @idEstado (INT): Estado inicial del empleado.
  - @usuario (NVARCHAR(30)): Nombre de usuario único.
  - @hashClave (NVARCHAR(120)): Contraseña cifrada.
  - @idRol (INT): Rol asignado.
  - @newIdPersona (INT OUTPUT): Id de la persona creada.
- Explicación clara de su función: Valida datos básicos, asegura que el usuario sea único y que
  el actor tenga nivel superior al rol que registra; luego inserta en `Persona` y `Empleado`.
- Resultado esperado del procedimiento: Empleado registrado con su cuenta asociada.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Tablas `Persona`, `Empleado`, `Rol` y procedimiento
  `sp_ValidarPersonaBasica`.
- Descripción: `DlgEmpleadoNuevo` usa este procedimiento para registrar empleados desde `FormEmpleados` y mostrar sus credenciales.

## sp_AgregarPagosTransaccion
- Descripción funcional: Inserta pagos adicionales para una transacción existente.
- Objetivo: Registrar abonos y completar el total neto de la transacción.
- Explicación de parámetros:
  - @idTransaccion (INT): Transacción destino de los pagos.
  - @pagos (tvp_PagoTx READONLY): Lista de pagos a insertar.
- Explicación clara de su función: Verifica existencia de la transacción, valida monto positivo,
  métodos no repetidos y que la suma no supere el total neto; luego registra los pagos.
- Resultado esperado del procedimiento: Pagos agregados y total de la transacción actualizado.
- Tipo de operación: Write
- Nivel de impacto: Medio
- Transaccionalidad: Sí
- Dependencias externas: Tabla `PagoTransaccion`, función `fn_TotalPagosTransaccion` y tabla
  `Transaccion`.
- Descripción: `DlgPagoPedido` invoca este procedimiento para agregar abonos pendientes y actualizar la información mostrada en `FormSeguimientoPedidos`.

