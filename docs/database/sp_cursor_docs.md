# Procedimientos con cursores

A continuación se documentan los procedimientos almacenados de `db/SP.sql` que emplean cursores. La estructura sigue las reglas de `sp_cursor_doc_rules`.

## sp_ListarClientesFrecuentes

• **Descripción funcional:**
Devuelve el top de clientes con más compras recorriendo la vista `vw_ClientesFrecuentes`.

• **Objetivo:**
Listar los clientes frecuentes ordenados por cantidad de compras.

• **Tipo de cursor utilizado:**
`LOCAL FAST_FORWARD`, explícito y de solo lectura.

• **Explicación general del flujo:**
Se declara el cursor sobre la vista agregada, se insertan los registros en una tabla temporal y se devuelven ordenados.

• **Detalles del cursor**
Conjunto recorrido: registros de `vw_ClientesFrecuentes` filtrados por `TOP(@top)`.
Variable interna: `@id`, `@nombre`, `@num`.
Procesamiento dentro del ciclo: inserta cada fila en una tabla variable.
Manejo del cursor: apertura, recorrido con `FETCH NEXT`, cierre y liberación con `DEALLOCATE`.

• **Conclusiones**
El cursor permite controlar el orden de inserción pero puede reemplazarse por una consulta `SELECT` directa para mejorar el rendimiento.
• **Descripción:** El `FormDashboard` ejecuta este procedimiento para mostrar la tabla de clientes frecuentes.

## sp_ListarAlertasPendientes

• **Descripción funcional:**
Recorre las alertas de stock pendientes con un cursor y devuelve el listado sin
modificar su estado.

• **Objetivo:**
Obtener las alertas actuales conservando su marca de "pendiente" para que puedan
procesarse manualmente.

• **Tipo de cursor utilizado:**
`LOCAL FAST_FORWARD`, explícito.

• **Explicación general del flujo:**
El cursor lee cada alerta no procesada y la inserta en una tabla temporal. Al
final se devuelve el contenido de dicha tabla ordenado por fecha.

• **Detalles del cursor**
Conjunto recorrido: filas de `AlertaStock` con `procesada = 0`.
Variable interna: `@idAlerta`, `@idProducto`, `@stock`, `@umbral`, `@fecha`,
`@procesada`.
Procesamiento dentro del ciclo: inserción en la tabla variable `@result`.
Manejo del cursor: apertura, recorrido con `FETCH NEXT`, cierre y liberación.

• **Conclusiones**
La rutina obtiene las alertas sin modificarlas; un `SELECT` directo podría
reemplazar el cursor para mejorar el rendimiento.
• **Descripción:** El diálogo `DlgAlertasStock` usa este SP para listar las alertas antes de permitir marcarlas como atendidas.

## sp_ListarPedidosPendientes

• **Descripción funcional:**
Obtiene los pedidos en estado *En Proceso* utilizando un cursor sobre las tablas relacionadas.

• **Objetivo:**
Listar pedidos pendientes junto con sus datos principales.

• **Tipo de cursor utilizado:**
`LOCAL FAST_FORWARD`, explícito y de solo lectura.

• **Explicación general del flujo:**
Se recorre el conjunto de pedidos pendientes y se va llenando una tabla temporal que luego se devuelve ordenada.

• **Detalles del cursor**
Conjunto recorrido: resultado del `JOIN` entre `Pedido`, `Transaccion`, `Cliente` y `Estado`.
Variable interna: `@idTx`, `@fecha`, `@idEmp`, `@idCli`, `@dir`, `@tipo`, `@vale`.
Procesamiento dentro del ciclo: inserción de cada fila en tabla variable.
Manejo del cursor: apertura, recorrido, cierre y liberación.

• **Conclusiones**
El cursor simplifica la recolección de datos, aunque una consulta única podría sustituirlo para mejorar el rendimiento.
• **Descripción:** `FormSeguimientoPedidos` invoca este procedimiento para listar los pedidos en proceso y permitir acciones sobre cada uno.

## sp_RecalcularStockProductos

• **Descripción funcional:**
Recalcula el stock de productos sumando sus tallas y genera alertas cuando se rebasa el umbral.

• **Objetivo:**
Sincronizar el stock real con el calculado y activar alertas de bajo inventario.

• **Tipo de cursor utilizado:**
`LOCAL FAST_FORWARD`, explícito.

• **Explicación general del flujo:**
Tras crear tablas temporales con los totales por producto, se recorre cada registro para actualizar el stock, insertar o modificar alertas y ajustar el estado del producto.

• **Detalles del cursor**
Conjunto recorrido: contenido de la tabla temporal `#tmp` con id y stock de cada producto.
Variable interna: `@id`, `@stock`, `@umbral`, `@ignorar`.
Procesamiento dentro del ciclo: `UPDATE` de `Producto`, creación o actualización de `AlertaStock` y cambio de estado.
Manejo del cursor: apertura condicional, iteración, cierre y liberación final.

• **Conclusiones**
Permite aplicar reglas complejas a cada producto, aunque puede tardar con volúmenes grandes. Considerar operaciones en lote para minimizar el costo.
• **Descripción:** `FormMantenimiento` ejecuta este procedimiento al recalcular el stock global desde el menú de administración.

## sp_DepurarBitacoraLogin

• **Descripción funcional:**
Elimina registros de `BitacoraLogin` anteriores a una fecha límite.

• **Objetivo:**
Depurar la bitácora y devolver la cantidad de filas eliminadas.

• **Tipo de cursor utilizado:**
`LOCAL FAST_FORWARD`, explícito.

• **Explicación general del flujo:**
Dentro de una transacción se recorren los identificadores seleccionados, se elimina cada fila y se lleva un conteo acumulado.

• **Detalles del cursor**
Conjunto recorrido: identificadores de `BitacoraLogin` con fecha menor a `@maxFecha`.
Variable interna: `@id`.
Procesamiento dentro del ciclo: `DELETE` por identificador y suma a `@rowsDeleted`.
Manejo del cursor: apertura, recorrido, cierre y liberación.

• **Conclusiones**
Funciona para depuraciones puntuales. Para grandes cantidades puede reemplazarse por un `DELETE` masivo que calcule el total con `@@ROWCOUNT`.
• **Descripción:** Desde `FormMantenimiento` puede ejecutarse este SP para limpiar registros antiguos de la bitácora de inicio de sesión.

