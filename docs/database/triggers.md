# Descripción de triggers

A continuación se documentan los disparadores definidos en `db/Triggers.sql` siguiendo las reglas de `triggers_doc_rules`.

## trg_Transaccion_Update
• **Descripción funcional:** Valida estados y registra un movimiento de inventario cuando se cancela `Transaccion`. Se dispara tras **INSERT** o **UPDATE**.

• **Objetivo:** Garantizar que las transacciones respeten las reglas de negocio y revertir el stock al anular una operación.

• **Tipo de trigger:** AFTER INSERT, UPDATE.

• **Tabla asociada:** `Transaccion`.

• **Condición o lógica de ejecución:** Verifica que el estado pertenezca al módulo, exige que exista una `Venta` o `Pedido`, impide cambios en transacciones cerradas y, al cancelar, inserta un `MovimientoInventario`. El descuento final del stock ocurre en otros procesos.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 7‑171.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `UPDATE Transaccion SET idEstado = ...`.
• **Resultado esperado:** Valida la operación y crea el movimiento de cancelación cuando corresponde.

## trg_Persona_ValidarEstado
• **Descripción funcional:** Controla cambios de estado en `Persona` y evita desactivaciones no permitidas. Se ejecuta en **INSERT** y **UPDATE**.

• **Objetivo:** Garantizar integridad del estado de las personas y respetar jerarquías de usuario.

• **Tipo de trigger:** AFTER INSERT, UPDATE.

• **Tabla asociada:** `Persona`.

• **Condición o lógica de ejecución:** Rechaza estados fuera del módulo y prohíbe que un empleado se desactive a sí mismo o a un par/superior salvo que sea `admin`.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 176‑239.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `UPDATE Persona SET idEstado = ...`.
• **Resultado esperado:** Se bloquea la operación si viola las reglas; de lo contrario el cambio continúa.

## trg_Producto_ValidateAndAdjust
• **Descripción funcional:** Valida productos, actualiza stock según tallas y genera alertas de umbral bajo. Actúa en **INSERT** y **UPDATE**.

• **Objetivo:** Mantener la coherencia de productos y automatizar ajustes por tipo de producto.

• **Tipo de trigger:** AFTER INSERT, UPDATE.

• **Tabla asociada:** `Producto`.

• **Condición o lógica de ejecución:** Verifica el estado del producto, fuerza `precioUnitario` nulo en fraccionables, recalcula stock de vestimenta, crea alertas si el stock cae bajo el umbral y, cuando `stockActual` llega a `0` con `ignorarUmbralHastaCero` activado, restablece dicho indicador a `0`.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 242‑384.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `INSERT INTO Producto ...`.
• **Resultado esperado:** Se aplican los ajustes indicados y se crean alertas cuando corresponde.

## trg_TallaStock_ValidateAndUpdate
• **Descripción funcional:** Valida tallas de producto y actualiza el stock global al modificarse `TallaStock`. Se dispara en **INSERT**, **UPDATE** y **DELETE**.

• **Objetivo:** Asegurar que solo productos de vestimenta tengan tallas y mantener el stock sincronizado.

• **Tipo de trigger:** AFTER INSERT, UPDATE, DELETE.

• **Tabla asociada:** `TallaStock`.

• **Condición o lógica de ejecución:** Comprueba el estado del producto, prohíbe tallas en productos no vestimenta y recalcula el stock total del producto afectado.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 387‑441.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `UPDATE TallaStock SET stock = ...`.
• **Resultado esperado:** Se valida la operación y se actualiza el stock del producto en `Producto`.

## trg_Presentacion_Validate
• **Descripción funcional:** Aplica validaciones de estado y precio para las presentaciones de productos fraccionables. Se ejecuta en **INSERT** y **UPDATE**.

• **Objetivo:** Garantizar coherencia en las presentaciones y evitar precios mayores al proporcional.

• **Tipo de trigger:** AFTER INSERT, UPDATE.

• **Tabla asociada:** `Presentacion`.

• **Condición o lógica de ejecución:** Requiere que el producto sea fraccionable y que el precio no supere la cantidad por el precio unitario del producto.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 444‑491.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `INSERT INTO Presentacion ...`.
• **Resultado esperado:** La operación es rechazada si incumple las reglas.

## trg_Venta_Insert
• **Descripción funcional:** Garantiza relación uno a uno entre `Venta` y `Pedido`. Se dispara tras insertar en **Venta**.

• **Objetivo:** Evitar que una transacción sea venta y pedido simultáneamente y actualizar estado a completada.

• **Tipo de trigger:** AFTER INSERT.

• **Tabla asociada:** `Venta`.

• **Condición o lógica de ejecución:** Si existe un `Pedido` con la misma transacción arroja error; además cambia el estado de la transacción a `Completada`.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 494‑513.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `INSERT INTO Venta ...`.
• **Resultado esperado:** Se actualiza el estado y se impide la doble relación con `Pedido`.

## trg_Pedido_Validate
• **Descripción funcional:** Valida reglas de pedido, fechas y cliente. Corre en **INSERT** y **UPDATE**.

• **Objetivo:** Asegurar que los pedidos tengan cliente y fechas válidas, y que no se dupliquen con ventas.

• **Tipo de trigger:** AFTER INSERT, UPDATE.

• **Tabla asociada:** `Pedido`.

• **Condición o lógica de ejecución:** Rechaza pedidos que ya tengan venta asociada, que carezcan de cliente o con fecha de entrega anterior a la transacción; ajusta estado a `En Proceso`.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 516‑574.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `INSERT INTO Pedido ...`.
• **Resultado esperado:** Solo se permiten pedidos válidos y se actualiza el estado de la transacción.

## trg_PagoTransaccion_CheckSum
• **Descripción funcional:** Revisa que la suma de pagos coincida con el total neto de transacciones cerradas. Opera en **INSERT**, **UPDATE** y **DELETE**.

• **Objetivo:** Mantener la integridad de pagos para transacciones completadas o entregadas.

• **Tipo de trigger:** AFTER INSERT, UPDATE, DELETE.

• **Tabla asociada:** `PagoTransaccion`.

• **Condición o lógica de ejecución:** Obtiene las transacciones afectadas y lanza error si los pagos no suman el total neto cuando el estado está cerrado.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 577‑604.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `INSERT INTO PagoTransaccion ...`.
• **Resultado esperado:** Si la suma de pagos no coincide, la operación se revierte.

## trg_Comprobante_Insert
• **Descripción funcional:** Impide generar comprobantes para algo distinto de una venta o pedido. Se dispara en **INSERT**.

• **Objetivo:** Garantizar que los comprobantes solo estén asociados a ventas o pedidos reales.

• **Tipo de trigger:** AFTER INSERT.

• **Tabla asociada:** `Comprobante`.

• **Condición o lógica de ejecución:** Verifica que la transacción tenga una entrada en `Venta` o `Pedido`; si no la tiene, arroja error.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 607‑622.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `INSERT INTO Comprobante ...`.
• **Resultado esperado:** Se rechaza el comprobante si no hay venta o pedido asociado.

## trg_DetalleTransaccion_Maintenance
• **Descripción funcional:** Valida y mantiene coherencia de detalles de transacción, recalculando totales y controlando stock. Se ejecuta en **INSERT**, **UPDATE** y **DELETE**.

• **Objetivo:** Prevenir inconsistencias de stock, precios y cantidades en cada transacción.

• **Tipo de trigger:** AFTER INSERT, UPDATE, DELETE.

• **Tabla asociada:** `DetalleTransaccion`.

• **Condición o lógica de ejecución:** Verifica tallas, producto activo, precios mayoristas, stock suficiente y cantidad máxima; impide cambios si la transacción está cerrada y actualiza el total bruto.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 625‑880.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `INSERT INTO DetalleTransaccion ...`.
• **Resultado esperado:** Se valida cada detalle y se actualiza el total de la transacción.

## trg_MovInv_ValidateAndUpdate
• **Descripción funcional:** Valida movimientos de inventario y ajusta stock. Se dispara en **INSERT** y **UPDATE**.

• **Objetivo:** Mantener la consistencia de inventario y evitar registros erróneos.

• **Tipo de trigger:** AFTER INSERT, UPDATE.

• **Tabla asociada:** `MovimientoInventario`.

• **Condición o lógica de ejecución:** Comprueba que solo vestimenta use tallas, valida correspondencia con el producto, exige motivo para ajustes e incrementa o reduce stock según el tipo de movimiento.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 883‑1003.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `INSERT INTO MovimientoInventario ...`.
• **Resultado esperado:** Se actualiza el stock y se impide que quede negativo.

## trg_Empleado_LoginHandling
• **Descripción funcional:** Registra en bitácora los intentos de acceso y gestiona el bloqueo de cuentas. Se ejecuta en **UPDATE**.

• **Objetivo:** Llevar un control de accesos e impedir nuevos intentos tras varios fallos.

• **Tipo de trigger:** AFTER UPDATE.

• **Tabla asociada:** `Empleado`.

• **Condición o lógica de ejecución:** Al actualizar `ultimoAcceso` registra login exitoso y reinicia contadores; al incrementar `intentosFallidos` registra intentos y bloquea temporalmente cuando se alcanza el límite.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 1006‑1053.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `UPDATE Empleado SET ultimoAcceso = SYSDATETIME()`.
• **Resultado esperado:** Se agrega registro a `BitacoraLogin` y se gestionan bloqueos si aplica.

## trg_Rol_AdminOnly
• **Descripción funcional:** Restringe modificaciones al catálogo de roles solo a administradores. Se ejecuta en **INSERT**, **UPDATE** y **DELETE**.

• **Objetivo:** Proteger la configuración de roles del sistema.

• **Tipo de trigger:** AFTER INSERT, UPDATE, DELETE.

• **Tabla asociada:** `Rol`.

• **Condición o lógica de ejecución:** Llama al procedimiento `sp_CheckAdminTrigger` que verifica si el usuario actual es administrador.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 1056‑1067.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `DELETE FROM Rol WHERE ...`.
• **Resultado esperado:** Se bloquea la operación si quien la ejecuta no es administrador.

## trg_ParametroSistema_AdminOnly
• **Descripción funcional:** Solo un administrador puede modificar `ParametroSistema`. Se dispara en **INSERT**, **UPDATE** y **DELETE**.

• **Objetivo:** Evitar cambios no autorizados en parámetros globales.

• **Tipo de trigger:** AFTER INSERT, UPDATE, DELETE.

• **Tabla asociada:** `ParametroSistema`.

• **Condición o lógica de ejecución:** Invoca `sp_CheckAdminTrigger` para validar privilegios de administrador.

• **Script SQL del trigger:** Consultar `db/Triggers.sql` líneas 1070‑1081.

• **Ejemplo de ejecución del trigger:**

    o Sentencia que lo activa: `UPDATE ParametroSistema SET ...`.
• **Resultado esperado:** Se impide cualquier modificación si el usuario no es administrador.

