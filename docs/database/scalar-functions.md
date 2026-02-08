# Descripción de funciones escalares

A continuación se documentan todas las funciones escalares definidas en `db/DDL.sql` y `db/TVF_y_SF.sql` siguiendo las reglas de `scalar-functions_doc_rules`.

## fn_EsDniValido
- Objetivo: Validar que un DNI contenga exactamente ocho dígitos numéricos.
- Descripción funcional: Recibe `@dni CHAR(8)` y retorna `BIT` indicando 1 si el texto no es nulo, posee solo números y tiene longitud 8; de lo contrario devuelve 0.

## fn_estado
- Objetivo: Obtener el identificador de un estado según su módulo y nombre.
- Descripción funcional: Acepta `@modulo NVARCHAR(20)` y `@nombre NVARCHAR(20)` y retorna `INT` consultando la tabla `Estado` para esos valores.

## fn_NombreCompleto
- Objetivo: Construir el nombre completo de una persona para reutilizarlo en consultas y vistas.
- Descripción funcional: Recibe `@IdPersona INT` y retorna `NVARCHAR(120)` concatenando `nombres` y `apellidos` de la tabla `Persona` mediante `CONCAT_WS`.

## fn_GetParametroDecimal
- Objetivo: Recuperar el valor decimal de un parámetro del sistema con un valor por defecto.
- Descripción funcional: Recibe `@clave NVARCHAR(30)` y `@def DECIMAL(10,2)`; devuelve `DECIMAL(10,2)` usando `COALESCE` para tomar el valor registrado o el predeterminado.

## fn_CargoRepartoActual
- Objetivo: Obtener el cargo fijo por reparto configurado actualmente.
- Descripción funcional: No recibe parámetros y retorna `DECIMAL(10,2)` invocando `fn_GetParametroDecimal('CARGO_REPARTO', 0)`.

## fn_DescuentoValeGas
- Objetivo: Calcular el descuento aplicado al pagar con vale de gas.
- Descripción funcional: Sin parámetros, retorna `DECIMAL(10,2)` mediante `fn_GetParametroDecimal('DESCUENTO_VALE_GAS', 0)`.

## fn_actor_id
- Objetivo: Identificar al empleado en sesión para auditorías y validaciones.
- Descripción funcional: No recibe parámetros y devuelve `INT` utilizando `SESSION_CONTEXT('idEmpleado')` convertido con `TRY_CONVERT`.

## fn_actor_nivel
- Objetivo: Conocer el nivel de rol del empleado actual registrado en sesión.
- Descripción funcional: Sin parámetros; obtiene el id con `fn_actor_id`, consulta `Empleado` y `Rol` y retorna `INT` con el nivel o `NULL` si no existe.

## fn_Capitalizar
- Objetivo: Formatear un texto con la primera letra en mayúscula y el resto en minúscula.
- Descripción funcional: Recibe `@texto NVARCHAR(120)` y devuelve `NVARCHAR(120)` aplicando `UPPER`, `LOWER`, `LEFT` y `SUBSTRING`.

## fn_NormalizarEspacios
- Objetivo: Eliminar espacios repetidos para dejar solo uno entre palabras.
- Descripción funcional: Con parámetro `@texto NVARCHAR(200)` retorna `NVARCHAR(200)` usando `LTRIM`, `RTRIM`, un bucle `WHILE` y `REPLACE`.

## fn_EsTelefonoValido
- Objetivo: Verificar que un número telefónico tenga solo dígitos y longitud válida.
- Descripción funcional: Toma `@telefono NVARCHAR(15)` y devuelve `BIT` comprobando patrones con `LIKE` y `LEN`.

## fn_NormalizarTelefono
- Objetivo: Normalizar un teléfono quitando caracteres no numéricos.
- Descripción funcional: Recibe `@telefono NVARCHAR(30)` y retorna `NVARCHAR(15)` usando `TRANSLATE`, `REPLACE` y `SUBSTRING`.

## fn_TotalPagosTransaccion
- Objetivo: Obtener la suma de los pagos registrados para una transacción.
- Descripción funcional: Acepta `@idTx INT` y retorna `DECIMAL(12,2)` sumando `monto` en `PagoTransaccion` con `ISNULL`.

## fn_StockDisponible
- Objetivo: Conocer el stock actual disponible para un producto.
- Descripción funcional: Con parámetro `@idProducto INT` devuelve `DECIMAL(12,3)` consultando `Producto.stockActual` y usando `COALESCE` para devolver 0 si no existe.

## fn_StockDisponibleTalla
- Objetivo: Obtener el stock disponible para una talla específica.
- Descripción funcional: Recibe `@idTallaStock INT` y retorna `DECIMAL(12,3)` de la tabla `TallaStock`, o 0 si no hay registro.

## fn_EsTipoProducto
- Objetivo: Determinar si un producto pertenece a un tipo indicado.
- Descripción funcional: Acepta `@idProducto INT` y `@tipo NVARCHAR(20)`; devuelve `BIT` evaluando existencia en `Producto` y `TipoProducto`.

## fn_ClasificarPedido
- Objetivo: Clasificar un pedido como Domicilio o Especial según sus productos.
- Descripción funcional: Usa el tipo tabla `tvp_DetalleTx` como parámetro de solo lectura y retorna `NVARCHAR(20)` después de verificar `tipoPedidoDefault`.

## fn_MinCantidadMayoristaHilo
- Objetivo: Indicar la cantidad mínima para precio mayorista de hilo.
- Descripción funcional: Sin parámetros; retorna `INT` convirtiendo el valor de `fn_GetParametroDecimal('MIN_CANTIDAD_MAYORISTA_HILO', 200)`.

## fn_MaxIntentosFallidos
- Objetivo: Informar el máximo de intentos fallidos antes de bloquear una cuenta.
- Descripción funcional: No recibe parámetros y devuelve `INT` a partir de `fn_GetParametroDecimal('MAX_INTENTOS_FALLIDOS', 3)`.

## fn_MinutosBloqueoCuenta
- Objetivo: Establecer la duración del bloqueo de cuenta en minutos.
- Descripción funcional: Sin parámetros; retorna `INT` mediante `fn_GetParametroDecimal('MINUTOS_BLOQUEO_CUENTA', 5)`.

## fn_AssertEstadoModulo
- Objetivo: Validar que un estado pertenezca al módulo indicado.
- Descripción funcional: Recibe `@idEstado INT` y `@modulo NVARCHAR(20)` y devuelve `BIT` tras consultar la tabla `Estado`.

## fn_TieneStockNegativo
- Objetivo: Detectar si algún producto o talla del detalle posee stock negativo.
- Descripción funcional: Usa `@detalle dbo.tvp_DetalleTx READONLY` y retorna `INT` calculando un indicador sobre `Producto` y `TallaStock` para identificar existencias negativas.

