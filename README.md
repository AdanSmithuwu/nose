# ComercialsValerio-maven

Este repositorio contiene el código fuente y los scripts de base de datos de la aplicación Comercial's Valerio. Es un proyecto Maven multi-módulo que utiliza Java 21.

## Configuración de la base de datos

El directorio `db/` incluye varios scripts SQL utilizados para inicializar la base de datos.
Ejecuta los archivos en el siguiente orden para que las dependencias se carguen correctamente.
Las funciones deben definirse antes de cualquier trigger que las utilice:

1. `DDL.sql` – tablas y datos base del catálogo.
2. `TVF_y_SF.sql` – tipos de tabla y funciones con o sin tabla.
3. `SP.sql` – procedimientos almacenados.
4. `Triggers.sql` – disparadores de reglas de negocio.
5. `VW.sql` – vistas.
6. `Security.sql` – inicios de sesión, roles y permisos.
7. `CatalogInserts.sql` – registros base de catálogo.
8. `ExamplePeople.sql` – empleados y clientes de ejemplo.
9. `ProductBatches.sql` – productos de muestra.
10. `InitialInventory.sql` – movimientos iniciales de stock.
11. `MaintenancePlan.sql` – crea un trabajo de SQL Agent que respalda la base a las 21:00 y elimina archivos de más de 30 días.

`InstallAll.sql` ejecuta todos estos archivos en el mismo orden, por lo que la
planeación de mantenimiento se instala automáticamente. Si no deseas crear el
trabajo de respaldo puedes comentar la última línea del script antes de correrlo.

En lugar de ejecutar cada archivo manualmente puedes ejecutar `InstallAll.sql` con
`sqlcmd` para procesarlos en secuencia:

```bash
sqlcmd -S localhost -i db/InstallAll.sql
```

También puedes ejecutar el script auxiliar para PowerShell que envía las opciones
a `sqlcmd`:

```powershell
db\install_db.ps1 -S localhost -U sa -P <password>
```
El script incluye la opción `-b` para que `sqlcmd` detenga la ejecución si
ocurre un error.

Ejecutar los scripts en este orden garantiza que todas las dependencias se resuelvan.
Todos los archivos *.sql utilizan el separador de lotes `GO`; ejecútalos con una herramienta como
SQL Server Management Studio o `sqlcmd` que reconozca `GO`, o divide los scripts en lotes individuales.

Para ver qué clases Java invocan cada función, procedimiento, vista o
trigger consulta [docs/database/usage.md](docs/database/usage.md).

## Conexiones de base de datos

`db/Security.sql` define dos inicios de sesión de SQL y los roles de base de datos que controlan sus permisos.
El script crea dos roles:

- **Admin** – recibe `CONTROL` sobre el esquema `dbo` para que sus miembros puedan administrar todos los objetos.
- **Employee** – recibe `SELECT` y `EXECUTE` sobre todo el esquema y puede modificar las tablas de personas y operaciones habituales (`Persona`, `Cliente`, `Producto`, `TallaStock`, `Presentacion`, `Transaccion`, `DetalleTransaccion`, `PagoTransaccion`, `Pedido`, `Venta`, `MovimientoInventario`, `Comprobante`). Las tablas solo administrativas siguen protegidas mediante triggers.

Los inicios de sesión se asignan a estos roles:

- **cv_admin_user** – miembro del rol `Admin` con control total sobre el
  esquema `dbo`.
- **cv_employee_user** – miembro del rol `Employee` con lectura, ejecución y capacidad de insertar, actualizar y eliminar en las tablas operativas mencionadas.
Los permisos se otorgan con sentencias `GRANT`, `REVOKE` y `DENY` al final del script.

Ejemplos de cadenas de conexión:

```properties
jdbc:sqlserver://localhost:1433;databaseName=cv_ventas_distribucion;user=cv_admin_user;password=H3ll0A$tr0ngP@ssw0rd1!;trustServerCertificate=true
```

```properties
jdbc:sqlserver://localhost:1433;databaseName=cv_ventas_distribucion;user=cv_employee_user;password=Str0ngEmp!oyeeP@ssw0rd#2;trustServerCertificate=true
```

Configura las credenciales en `application.properties`:

```properties
db.employee.user=cv_employee_user
db.employee.password=Str0ngEmp!oyeeP@ssw0rd#2
db.admin.user=cv_admin_user
db.admin.password=H3ll0A$tr0ngP@ssw0rd1!
```

Las clases de infraestructura usan `RequestContext.rol()` para elegir el pool de conexiones. Cuando el rol empieza con `Admin` los módulos abren conexiones como `cv_admin_user`; de lo contrario utilizan `cv_employee_user`. Triggers como `trg_Empleado_LoginHandling` se ejecutan `WITH EXECUTE AS OWNER` para registrar los intentos de ingreso incluso si la sesión emplea la cuenta de empleado.

`DDL.sql` también crea el índice compuesto `IX_AlertaStock_Procesada` en
`dbo.AlertaStock(procesada, fechaAlerta)` para acelerar la búsqueda de alertas. Un índice
único filtrado `IX_AlertaStock_Pendiente` garantiza sólo una alerta pendiente por
producto. Define los siguientes índices adicionales:

- `IX_OrdenCompra_Pedido` sobre `dbo.OrdenCompra(idPedido)`
- `IX_AlertaStock_Pendiente` sobre `dbo.AlertaStock(idProducto)` filtrado por `procesada = 0`
- `IX_OrdenCompra_Producto` sobre `dbo.OrdenCompra(idProducto)`
- `IX_OrdenCompra_Cliente` sobre `dbo.OrdenCompra(idCliente)`
- `IX_OrdenCompraPdf_Fecha` sobre `dbo.OrdenCompraPdf(fechaGeneracion)`
- `IX_PagoTransaccion_Metodo` sobre `dbo.PagoTransaccion(idMetodoPago)`
- `IX_OrdenCompra_FechaCumplida` sobre `dbo.OrdenCompra(fechaCumplida)`
- `IX_MovInv_Tipo` sobre `dbo.MovimientoInventario(idTipoMovimiento)`
- `IX_MovInv_Prod` sobre `dbo.MovimientoInventario(idProducto)`
- `IX_MovInv_Prod_Fecha` sobre `dbo.MovimientoInventario(idProducto, fechaHora)`
- `IX_MovInv_Fecha` sobre `dbo.MovimientoInventario(fechaHora)`
- `IX_Trans_Fecha` sobre `dbo.Transaccion(fecha)` incluyendo `idCliente`, `totalNeto`, `idEstado`
- `IX_Trans_ClienteFecha` sobre `dbo.Transaccion(idCliente, fecha)` incluyendo `totalNeto`, `idEstado`
- `IX_Empleado_Rol` sobre `dbo.Empleado(idRol)`
- `IX_Persona_Estado` sobre `dbo.Persona(idEstado)`
- `IX_DetTrans_Talla` sobre `dbo.DetalleTransaccion(idTallaStock)`
- `IX_DetTrans_Producto` sobre `dbo.DetalleTransaccion(idProducto)`
- `IX_DetTrans_TransProdTalla` único sobre `dbo.DetalleTransaccion(idTransaccion, idProducto, idTallaStockKey)`
- `IX_TallaStock_Producto` sobre `dbo.TallaStock(idProducto)`
- `IX_MovInv_Empleado` sobre `dbo.MovimientoInventario(idEmpleado)`
- `IX_Producto_Categoria` sobre `dbo.Producto(idCategoria)`
- `IX_Producto_Estado` sobre `dbo.Producto(idEstado)`
- `IX_Producto_StockUmbral` sobre `dbo.Producto(stockActual)` incluyendo `umbral`
- `IX_Producto_Mayorista` sobre `dbo.Producto(mayorista)` incluyendo `minMayorista`, `precioMayorista`
- `IX_Presentacion_Producto` sobre `dbo.Presentacion(idProducto)`
- `IX_Reporte_Empleado` sobre `dbo.Reporte(idEmpleado)`
- `IX_Reporte_FechaGen` sobre `dbo.Reporte(fechaGeneracion)`
- `IX_Pedido_EmpleadoEntrega` sobre `dbo.Pedido(idEmpleadoEntrega)`
- `IX_Pedido_Tipo` sobre `dbo.Pedido(tipoPedido)`
- `IX_Estado_ModuloNombre` sobre `dbo.Estado(modulo, nombre)`
- `IX_Bitacora_ExitosoFecha` sobre `dbo.BitacoraLogin(exitoso, fechaEvento)`

Se eliminó el índice en `dbo.DetalleTransaccion(idTransaccion)`. Una columna calculada
`idTallaStockKey` normaliza las tallas nulas y el índice único
`IX_DetTrans_TransProdTalla` evita duplicados mientras que
`IX_DetTrans_Producto` sigue acelerando las búsquedas por producto.

`DDL.sql` agrega campos para registrar la entrega. `Pedido` incluye
`fechaHoraEntrega` e `idEmpleadoEntrega` para que la base de datos guarde cuándo y quién entregó el pedido. `OrdenCompra` suma `fechaCumplida`, marcando el
momento en que se cumple cada orden de compra.


Los procedimientos almacenados y triggers dependen de la función auxiliar
`fn_actor_nivel()` que lee el id del empleado actual desde
`SESSION_CONTEXT('idEmpleado')` y devuelve el nivel de rol de ese usuario.
Asegúrate de que tu sesión establezca este valor antes de ejecutar
scripts administrativos para que las comprobaciones de permisos funcionen correctamente.

El trigger `trg_Transaccion_Update` solo valida el cambio de estado y registra
un `MovimientoInventario` al cancelar la transacción. El descuento final del
stock ocurre en los procedimientos que gestionan las entregas.

Si la base de datos `cv_ventas_distribucion` no existe, créala antes de
ejecutar `DDL.sql`:

```sql
IF DB_ID('cv_ventas_distribucion') IS NULL
    CREATE DATABASE cv_ventas_distribucion;
GO
```

## Procedimientos de base de datos


`sp_DescontarStock_Detalle` inicia con `SET NOCOUNT, XACT_ABORT ON` y envuelve
su sentencia de inserción en una transacción. También descuenta las cantidades
registradas de `Producto.stockActual` y `TallaStock.stock` para que los triggers de stock
generen alertas al superarse los umbrales. Un bloque `BEGIN TRY`/`BEGIN TRAN`
confirma en caso de éxito y revierte en el `CATCH` correspondiente.

`sp_ActualizarEstadoPedido` acepta parámetros opcionales de entrega y,
al marcar un pedido como `Entregada`, guarda `fechaHoraEntrega`, `idEmpleadoEntrega`
y establece `fechaCumplida` en su `OrdenCompra`. El PDF generado se archiva en
`OrdenCompraPdf` para reimpresiones posteriores.

Cada reporte diario en PDF también se archiva en la tabla `Reporte` para
poder reimprimirlo posteriormente.

- Los procedimientos de reporte (`sp_GenerarReporteDiario`, `sp_GenerarReporteMensual`,
  `sp_GenerarReporteRotacion`) y la vista `vw_TransaccionesPorDia` usan rangos
  `DATETIME` de inicio y fin con comparaciones `>=` y `<` en lugar de convertir
  `fecha` a `DATE`. Esto mejora el uso de índices al filtrar registros.


Los errores lanzados por procedimientos almacenados con códigos de **50000** o mayores
propagan sus mensajes mediante `BusinessRuleViolationException` para que los
llamadores reciban el texto original.



### Procedimientos de mantenimiento

Utiliza estas ayudas para mantener la base de datos en buen estado:

- `sp_RecalcularStockProductos` recalcula `Producto.stockActual` y
  `TallaStock.stock` sumando todos los movimientos de inventario. Ejecútalo después de
  importaciones masivas o ajustes manuales cuando los conteos se desincronicen.
- `sp_DepurarBitacoraLogin` elimina entradas antiguas de `BitacoraLogin` según
  el `@maxFecha` proporcionado. Ejecútalo mensualmente o cuando el registro de accesos crezca demasiado.

## Reglas de negocio

 - `trg_DetalleTransaccion_Maintenance` rechaza ventas de ovillos de 200 unidades o más.
  Cuando un detalle pertenece a una `Venta` y el producto tiene `paraPedido = 1`,
  `tipoPedidoDefault = 'Especial'` y `mayorista = 1`, el trigger lanza
  el error **50090** con el mensaje "Cantidad máxima 199 para ovillos en venta".
  - Los formularios de Pedido Domicilio deben ocultar productos cuyo `tipoPedidoDefault` es
    `Especial`; sólo se deben ofrecer artículos regulares para la entrega.
  - `FormVenta` también muestra estos productos especiales pero limita la cantidad a
    `MIN_CANTIDAD_MAYORISTA_HILO - 1`.
  - Al registrar o editar un producto se debe impedir seleccionar el tipo de pedido
    `Especial` si el nombre no inicia con `Ovillo de hilo`.
  - Al cambiar el parámetro `MIN_CANTIDAD_MAYORISTA_HILO` se actualiza
    el `minMayorista` de todos los productos cuyo nombre inicia con
    `Ovillo de hilo`.
  - Las líneas de Venta y `Pedido Domicilio` cambian automáticamente a
    `precioMayorista` cuando la cantidad es mayor o igual que el `minMayorista`
    del producto. `Pedido Especial` está excluido de esta regla.
  - Los Pedidos Domicilio descuentan inventario al registrarse mediante
    `sp_DescontarStock_Detalle`. Los Pedidos Especiales mantienen el stock intacto
    hasta que se confirma la entrega y entregar un Pedido Domicilio no
    ajusta nuevamente el inventario. Consulta la sección **Procedimientos de base de datos**
    para más detalles.
  - Cuando se cancela una transacción, el trigger `trg_Transaccion_Update`
    registra un `MovimientoInventario` cuyo `motivo` añade el
    `motivoCancelacion` de la transacción. La actualización del stock la realiza
    `trg_MovInv_ValidateAndUpdate`.

 - Los números telefónicos pueden incluir espacios, guiones, paréntesis, puntos o un `+` inicial.
   Estos caracteres se eliminan antes de la validación dejando entre 6 y 15 dígitos.

## Instrucciones de compilación

Compila todos los módulos y crea los archivos WAR:

```bash
mvn -q -DskipTests package
```

Después de empaquetar verifica que `application/target/ComercialsValerio-Business.war`
contenga `common-*.jar` dentro de `WEB-INF/lib`.

## Ejecución de la aplicación

Compila todos los módulos y arranca la interfaz con Maven:

```bash
mvn -q -DskipTests package
mvn -pl presentation-ui exec:java
```

En Windows 10 y 11 puedes ejecutar el jar empaquetado directamente. Abre
`presentation-ui/target` y ejecuta:

```bash
java -jar ComercialsValerio-UI.jar
```

Cuando ejecutes fuera de un contenedor CDI asegúrate de detener el ejecutor en
segundo plano al salir. El lanzador ya registra un shutdown hook que invoca
`BackgroundExecutors.shutdown()`, por lo que al cerrar la ventana se terminarán
limpiamente todos los hilos.

Después de salir de la interfaz puedes ejecutar `jps` para confirmar que no
quedaron procesos de Java.

La interfaz utiliza un pequeño pool de hilos para las tareas en segundo plano.
Ajusta su tamaño mediante `ui.backgroundPoolSize` en
`presentation-ui/src/main/resources/ui.properties`.
Los valores inválidos o ausentes se reemplazan por `max(2, availableProcessors)`.

La interfaz gráfica y el backend comparten una única zona horaria.
Ambos leen `app.timezone` desde
`infrastructure/src/main/resources/application.properties` y
`presentation-ui/src/main/resources/ui.properties`. El valor predeterminado es
`America/Lima`.

Define la variable de entorno `APP_TIMEZONE` o la propiedad del sistema
`app.timezone` para sobrescribir este valor. Todos los mapeadores de DTO y utilidades de fechas en la UI
usan la zona configurada al convertir `LocalDateTime` y `OffsetDateTime`, por lo que cambiarla afecta
cómo se muestran y almacenan las fechas. Los serializadores JSON personalizados emiten
marcas de tiempo con el patrón `DATETIME2` de SQL Server (`yyyy-MM-dd HH:mm:ss.SSSSSSSXXX` para valores con
desplazamiento). Un ejemplo válido es `2025-07-06 13:59:11.5670233-05:00`;
el desplazamiento `-05:00` es el esperado al utilizar `America/Lima`.
Los deserializadores siguen aceptando cadenas ISO-8601, el mismo patrón
y valores numéricos de época.
Aplican la zona configurada al interpretar estos valores, lo que asegura
persistencia y presentación coherentes. Todas las columnas `DATETIME2` deben
almacenar marcas de tiempo en esta zona.

Los reportes diarios pueden generarse automáticamente cuando `scheduler.reporteDiario.enabled` es true en `infrastructure/src/main/resources/application.properties`. El planificador se ejecuta a medianoche y guarda el reporte del día anterior.

El cliente REST lee su URL base de `presentation-ui/src/main/resources/ui.properties`.
Puedes sobrescribirla con la variable de entorno `API_BASEURL` o la propiedad `api.baseUrl` al ejecutar la interfaz.
Si las peticiones tardan demasiado y fallan con `SocketTimeoutException`, ajusta `client.connectTimeout` y `client.readTimeout`
mediante las variables `CLIENT_CONNECTTIMEOUT` y `CLIENT_READTIMEOUT` (o propiedades del sistema).
Los valores por defecto son 5000 ms y 10000 ms respectivamente.

### Consultando la API REST

Puedes probar los endpoints HTTP directamente con `curl`. Estos ejemplos
funcionan en cualquier máquina de pruebas y demuestran que la API es totalmente
interoperable:

```bash
curl http://localhost:8080/api/categorias
curl http://localhost:8080/api/empleados/5
curl -X POST -d "usuario=admin&password=secret" \
     http://localhost:8080/api/empleados/login
```

Todos los comandos envían solicitudes HTTP simples y deberían funcionar mientras
el servidor sea accesible.

### Atajos de teclado

La interfaz admite algunas acciones de teclado útiles:

- **F5** actualiza la vista actual y es el único atajo para recargar datos.
- Todos los botones y campos con atajos deben configurarse mediante `KeyUtils.setTooltipAndMnemonic`.
- Todas las pantallas utilizan F5 para recargar; no se definen alternativas con Alt.
- **F6** alterna el modo oscuro.
- **Alt+S** abre el diálogo de alertas de stock (también **F7**).
- **F11** entra o sale de pantalla completa (gestionado por `FullScreenUtils`).
- **Alt+N** crea un nuevo elemento en la mayoría de pantallas de mantenimiento.
- **Alt+E** edita la fila seleccionada.
- **Alt+G** guarda los cambios.
- **Alt+C** cancela o cierra un diálogo.
- Usa **Ctrl+[tecla]** desde cualquier pantalla para abrir una opción del menú:
  - **Ctrl+1** Dashboard
  - **Ctrl+2** Clientes
  - **Ctrl+3** Empleados
  - **Ctrl+4** Categorías
  - **Ctrl+5** Pedido Especial
  - **Ctrl+6** Seguimiento de Ventas
  - **Ctrl+7** Seguimiento de Pedidos
  - **Ctrl+8** Productos
  - **Ctrl+9** Inventario
  - **Ctrl+0** Historial de Inventario
  - **Ctrl+Q** Reporte Diario
  - **Ctrl+W** Reporte Mensual
  - **Ctrl+E** Reporte de Rotación
  - **Ctrl+R** Bitácora de Accesos
  - **Ctrl+T** Parámetros
  - **Ctrl+Y** Mantenimiento
  - **Ctrl+L** Cerrar sesión

### Ordenamiento numérico en tablas

Varias tablas usan `TableRowSorter` para permitir ordenar por columna. Las columnas con contenido
numérico requieren comparadores personalizados para que los valores se ordenen por número y no como
texto. Los siguientes modelos configuran estos comparadores mediante
`TableUtils.setNumericComparators` o `TableModelUtils.createModel`:

- `FormClientes` – columnas 2 y 3 (`DNI`, `Teléfono`).
- `FormEmpleados` – columnas 2 y 3 (`DNI`, `Teléfono`).
- `EmpleadoController` – columnas 2 y 3 al refrescar la tabla de empleados.

Otras listas de productos y ventas ya establecen comparadores numéricos en sus controladores.

### Registros de WildFly

Los endpoints REST se despliegan en un servidor WildFly. Todos los mensajes de la aplicación se escriben
en `standalone/log/server.log` dentro del directorio de instalación de WildFly. Revisa
este archivo al solucionar problemas de inicio o respuestas inesperadas del API.

### Transacciones

Los métodos de repositorio usan un gestor de transacciones sencillo ligado al hilo. Inicia una
transacción con try-with-resources y llama a `commit()` para persistir los cambios:

```java
try (var tx = TransactionManager.begin()) {
    // use repository code here
    tx.commit();
}
```


## Gestión de empleados

Los empleados pueden actualizar su usuario y contraseña. La tabla `Empleado`
registra el último cambio en la columna `fechaCambioClave`, reiniciando
los contadores de intentos fallidos cada vez que las credenciales cambian.

## Uso de reportes

Las pantallas de reporte diario, mensual y de rotación se actualizan automáticamente cada cinco
minutos mientras están abiertas. Un temporizador de Swing invoca la misma acción que el botón **Actualizar**
o la tecla **F5** para que puedas actualizar manualmente o esperar al temporizador.
El temporizador inicia cuando el formulario se vuelve visible y se detiene al cerrarse para evitar
tareas en segundo plano.

## Resumen del reporte mensual

El procedimiento `sp_GenerarReporteMensual` calcula totales separados para
ventas completadas, pedidos Domicilio entregados y pedidos Especiales. El PDF
resultante incluye una nueva tabla "Resumen por modalidad". Cada reporte mensual
también expone totales por categoría mediante el campo `categorias` que devuelve
`/reportes/mensual`.
- `POST /reportes/mensual/guardar` almacena el PDF generado en la tabla de historial.

Los procedimientos de reporte no generan directamente el PDF. Cada uno
simplemente devuelve las fechas y totales requeridos por la aplicación.
`ReporteGeneratorImpl` en la capa de infraestructura utiliza estos resultados
para componer el documento PDF.

Ejemplo de salida para abril de 2025:

```
Reporte mensual abril 2025
Día  Transacciones  Monto
1    2             200.00
(Ingresos diarios chart)
Categoría  Transacciones  Ingresos
Gas       3             500.00
Ropa      2             300.00
Resumen por modalidad
Ventas: 10 - 1500.00
Especial: 5  - 800.00
Domicilio: 2 - 300.00
```

## Impresión de PDFs desde la UI

El cliente de escritorio permite imprimir reportes directamente. Cuando hay un PDF disponible un nuevo botón **Imprimir** envía el documento a cualquier impresora instalada usando el cuadro de diálogo de Java Print Service. Los archivos PDF temporales se eliminan con `File.deleteOnExit()` para que la impresión funcione correctamente en todas las plataformas sin demoras arbitrarias. En Windows asegúrate de tener un controlador válido para que el diálogo liste tus dispositivos.

## Impresión de comprobantes y pedidos

Las pantallas de ventas y seguimiento de pedidos permiten reimprimir un PDF existente. En **Seguimiento de Ventas** selecciona una transacción completada o entregada para que el botón **Imprimir** se active. Presiónalo para descargar el comprobante y enviarlo a tu impresora. En **Seguimiento de Pedidos** puedes **Descargar Orden** o enviarla por WhatsApp además de **Imprimir Orden**. Estas acciones sólo funcionan si previamente se generó y guardó un PDF; de lo contrario aparece una advertencia indicando que no hay documento disponible.
* Las órdenes de compra generan un PDF que se guarda para futuras reimpresiones.


### Endpoints de comprobantes

Las pantallas de historial usan las siguientes llamadas API para manejar los comprobantes:

- `POST /transacciones/{id}/comprobante` – genera y almacena el PDF
- `GET /transacciones/{id}/comprobante` – obtiene los metadatos del comprobante
- `GET /transacciones/{id}/comprobante/pdf` – descarga el archivo PDF
- `POST /transacciones/{id}/comprobante/whatsapp` – envía el PDF por WhatsApp

## Almacenamiento de comprobantes y WhatsApp

Cada venta completada y cada pedido marcado como entregado genera un comprobante
PDF que se archiva en la base de datos. Las pantallas de historial recuperan este
archivo para que puedas reimprimirlo, descargarlo o enviarlo después.

Tanto el diálogo de finalización como las tablas de historial muestran acciones manuales:

- **Descargar** guarda el PDF en disco.
- **Imprimir** lo envía a la impresora predeterminada.
- **Enviar WhatsApp** pregunta por un teléfono si no está registrado y manda el
  comprobante por WhatsApp.

Si imprimir o guardar falla, la interfaz muestra mensajes como "Error al imprimir
comprobante" o "Error al guardar archivo" para que el operador sepa qué salió mal.

Pasos para enviar el comprobante por WhatsApp:
1. Completar la venta o marcar el pedido como entregado.
2. Seleccionar **Enviar por WhatsApp** e ingresar el número si es necesario.
3. Presionar **Confirmar** para generar y almacenar el PDF.
4. Utilizar **Descargar**, **Imprimir** o **Enviar WhatsApp** para acciones
   adicionales.

## Eliminación de registros

Todas las operaciones de eliminación requieren autenticación de administrador. La capa de servicio rechaza la petición con `403 Forbidden` cuando un no administrador intenta eliminar una entidad.

La API expone endpoints `GET` que listan las dependencias que impiden la eliminación:

- `/categorias/{id}/eliminable`
- `/clientes/{id}/eliminable`
- `/empleados/{id}/eliminable`
- `/productos/{id}/eliminable`


El cliente de escritorio consulta estos endpoints antes de habilitar el botón **Eliminar**. El botón se activa sólo cuando la lista devuelta está vacía.

## Actualización de productos

El endpoint `PUT /productos/{id}` utiliza `ProductoCUDto` para validar los datos.
Varios campos están anotados con `@NotNull`, por lo que la actualización requiere
enviar todos los atributos del producto, incluso si alguno no cambia.
Recupera primero la información con `GET /productos/{id}` y reenvía el mismo
contenido modificando sólo los campos necesarios. Omitir parámetros provocará
errores de validación.

## Endpoints de bitácora

Los administradores pueden revisar los intentos de inicio de sesión registrados en la bitácora usando estas operaciones:

- `GET /api/bitacoras?desde=YYYY-MM-DDTHH:MM:SS&hasta=YYYY-MM-DDTHH:MM:SS&resultado=true|false`
- `GET /api/bitacoras/empleado/{id}`

Tanto `desde` como `hasta` deben ser marcas de tiempo ISO-8601 sin zona. El filtro
opcional `resultado` selecciona intentos exitosos (`true`) o fallidos (`false`).

Ejemplo de respuesta:

```json
[
  {
    "idBitacora": 12,
    "empleadoId": 5,
    "empleadoUsuario": "jdoe",
    "fechaEvento": "2024-05-01 13:45:22.1234567-05:00",
    "exitoso": true
  }
]
```

El campo `fechaEvento` utiliza el formato `DATETIME2` de SQL Server con un desplazamiento
(`yyyy-MM-dd HH:mm:ss.SSSSSSS±HH:mm`). Al llamar al API envía las fechas en ISO-8601
para que el servidor las analice correctamente.


## Solución de problemas de reportes vacíos

Si los reportes mensual o de rotación aparecen vacíos mientras sabes que hay
transacciones completadas, verifica lo siguiente:

1. Que los campos de año y mes coincidan con las fechas de tus registros de ventas.
   La interfaz predeterminada usa el año actual, que puede diferir del conjunto de datos.
2. Cada transacción debe estar en estado **Completada** o **Entregada** para
   los pedidos. Los reportes ignoran otros estados.
3. El usuario de base de datos debe tener permiso de lectura en todas las vistas de reportes. Ejecuta
   el script `Security.sql` si creaste la base manualmente.

Después de ajustar los selectores de fecha y confirmar los permisos, recarga la
pantalla de reportes. Los datos deberían poblar las tablas y gráficos correctamente.

## Iconos
Los iconos de la barra lateral provienen del conjunto [Heroicons](https://github.com/tailwindlabs/heroicons) (licencia MIT).

## Contribuciones
Al implementar nuevos diálogos que permitan editar campos, llama a
`DialogUtils.confirmAction()` antes de cerrar si se modificaron valores.
Normalmente esta lógica vive en un método `attemptCancel()` invocado desde el
listener de ventana y el manejador de la tecla Escape.

## Guía de consultas JPQL
Consulta [docs/persistence/jpql-guidelines.md](docs/persistence/jpql-guidelines.md) para saber cuándo usar anotaciones `@NamedQuery` en lugar de construir JPQL en los repositorios.
