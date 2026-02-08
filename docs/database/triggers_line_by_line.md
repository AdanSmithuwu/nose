# Documentación detallada de triggers

Este documento explica paso a paso el código de los disparadores
seleccionados de `db/Triggers.sql`.

## trg_Comprobante_Insert

1. `/* Impide comprobante para algo que no sea Venta o Pedido */`
   - Comentario que indica el propósito del trigger.
2. `CREATE OR ALTER TRIGGER trg_Comprobante_Insert`
   - Crea o reemplaza el disparador con ese nombre.
3. `ON dbo.Comprobante AFTER INSERT`
   - Lo asocia a la tabla `Comprobante` y se ejecuta después de insertar.
4. `AS`
   - Inicio del cuerpo del trigger.
5. `BEGIN`
   - Delimita el bloque de instrucciones.
6. `    SET NOCOUNT ON;`
   - Evita que se devuelva la cuenta de filas afectadas.
7. `    SET XACT_ABORT ON;`
   - Hace que cualquier error cancele la transacción automáticamente.
8. `    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;`
   - Sale de inmediato si no hay filas afectadas.
9. `    IF EXISTS(`
   - Inicio de la validación.
10. `      SELECT 1`
    - Selección simple para evaluar existencia.
11. `      FROM   inserted i`
    - Usa el alias `i` para las filas insertadas.
12. `      LEFT  JOIN dbo.Venta  v ON v.idTransaccion  = i.idTransaccion`
    - Une con `Venta` por `idTransaccion`.
13. `      LEFT  JOIN dbo.Pedido p ON p.idTransaccion = i.idTransaccion`
    - Une con `Pedido` por `idTransaccion`.
14. `      WHERE v.idTransaccion IS NULL AND p.idTransaccion IS NULL`
    - Verifica que no exista ni venta ni pedido asociado.
15. `    ) THROW 50040, 'Comprobante solo para Venta o Pedido.', 1;`
    - Si se cumple la condición, arroja un error personalizado.
16. `END;`
    - Cierre del bloque del trigger.
17. `GO`
    - Separador de lote de T‑SQL.

## trg_Empleado_LoginHandling

1. `/* Manejo de login: bitácora y bloqueo de cuenta */`
   - Comentario descriptivo del trigger.
2. `CREATE OR ALTER TRIGGER trg_Empleado_LoginHandling`
   - Crea o reemplaza el disparador con dicho nombre.
3. `ON dbo.Empleado`
   - Actúa sobre la tabla `Empleado`.
4. `WITH EXECUTE AS OWNER`
   - Se ejecuta con los permisos del propietario.
5. `AFTER UPDATE`
   - Se dispara después de actualizaciones.
6. `AS`
   - Inicio del cuerpo del trigger.
7. `BEGIN`
   - Comienza el bloque de instrucciones.
8. `    SET NOCOUNT ON;`
   - Deshabilita el conteo de filas.
9. `    SET XACT_ABORT ON;`
   - Cancela la transacción ante errores.
10. `    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;`
    - Finaliza si no hubo filas modificadas.
11. `    /* login exitoso ------------------------------------------------*/`
    - Comentario para la primera lógica.
12. `    IF UPDATE(ultimoAcceso)`
    - Comprueba si se actualizó `ultimoAcceso`.
13. `    BEGIN`
    - Inicia bloque de login exitoso.
14. `        INSERT INTO dbo.BitacoraLogin(idEmpleado,exitoso)`
    - Registra en la bitácora que hubo acceso.
15. `        SELECT i.idPersona, 1`
    - Inserta el identificador y marca como exitoso.
16. `        FROM inserted i`
    - Usa las filas nuevas.
17. `        JOIN deleted d ON d.idPersona = i.idPersona`
    - Compara con las filas previas.
18. `        WHERE i.ultimoAcceso IS NOT NULL`
    - Solo cuando la fecha de acceso es válida.
19. `          AND (d.ultimoAcceso IS NULL OR i.ultimoAcceso <> d.ultimoAcceso);`
    - Evita duplicar registros si no cambió la fecha.
20. `        UPDATE dbo.Empleado`
    - Reinicia contadores en la tabla.
21. `           SET intentosFallidos = 0,`
    - Restablece intentos fallidos.
22. `               bloqueadoHasta   = NULL`
    - Limpia el bloqueo temporal.
23. `        FROM dbo.Empleado e`
    - Aplica a las filas correspondientes.
24. `        JOIN inserted i ON i.idPersona = e.idPersona`
    - Une por el mismo empleado.
25. `        WHERE i.ultimoAcceso IS NOT NULL;`
    - Solo afecta registros válidos.
26. `    END;`
    - Fin de bloque de login exitoso.
27. `    /* intento fallido ----------------------------------------------*/`
    - Comentario para la segunda lógica.
28. `    IF UPDATE(intentosFallidos)`
    - Se ejecuta si cambió `intentosFallidos`.
29. `    BEGIN`
    - Inicia bloque de intento fallido.
30. `        INSERT INTO dbo.BitacoraLogin(idEmpleado,exitoso)`
    - Registra el intento en la bitácora.
31. `        SELECT i.idPersona, 0`
    - Marca como no exitoso.
32. `        FROM inserted i`
    - Filas actualizadas.
33. `        JOIN deleted d ON d.idPersona = i.idPersona`
    - Compara valores anteriores.
34. `        WHERE i.intentosFallidos > d.intentosFallidos;`
    - Solo si aumentó el contador.
35. `        DECLARE @maxFallidos INT = dbo.fn_MaxIntentosFallidos();`
    - Obtiene el máximo permitido.
36. `        DECLARE @minBloqueo INT = dbo.fn_MinutosBloqueoCuenta();`
    - Minutos de bloqueo.
37. `        UPDATE e`
    - Aplica bloqueo si procede.
38. `           SET bloqueadoHasta = DATEADD(MINUTE, @minBloqueo, SYSDATETIME())`
    - Utiliza `DATEADD` para sumar `@minBloqueo` minutos a la fecha y hora
      actual obtenida con `SYSDATETIME()`. El resultado indica el instante exacto
      hasta el que permanece bloqueada la cuenta.
39. `        FROM dbo.Empleado e`
    - En la tabla principal.
40. `        JOIN inserted i ON i.idPersona = e.idPersona`
    - Filas afectadas.
41. `        WHERE i.intentosFallidos = @maxFallidos;`
    - Sólo cuando alcanzó el máximo.
42. `    END;`
    - Fin de bloque de intento fallido.
43. `END;`
    - Cierre del trigger.
44. `GO`
    - Separador de lote.

## trg_Persona_ValidarEstado

1. `/* Valida que el estado corresponda al módulo Persona y evita`
   `   desactivaciones no permitidas */`
   - Comentario que resume la funcionalidad.
2. `CREATE OR ALTER TRIGGER trg_Persona_ValidarEstado`
   - Crea o reemplaza el disparador.
3. `ON dbo.Persona AFTER INSERT, UPDATE`
   - Se ejecuta después de insertar o actualizar `Persona`.
4. `AS`
   - Inicio del bloque.
5. `BEGIN`
   - Delimita el cuerpo del trigger.
6. `    SET NOCOUNT ON;`
   - Desactiva el conteo de filas.
7. `    SET XACT_ABORT ON;`
   - Cancela la transacción en caso de error.
8. `    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;`
   - Sale si no hubo filas afectadas.
9. `    -- Validar que el estado pertenezca al módulo Persona para`
   `    -- cualquier inserción o actualización`
   - Comentario sobre la primera validación.
10. `    DECLARE @invPers INT = (`
    - Prepara variable para estado inválido.
11. `        SELECT TOP 1 i.idEstado`
    - Obtiene un ejemplo de estado no válido.
12. `          FROM inserted i`
    - Fuente de datos insertados o actualizados.
13. `         WHERE dbo.fn_AssertEstadoModulo(i.idEstado, N'Persona') = 0`
    - Llama a función de validación de estado.
14. `    );`
    - Fin de la asignación.
15. `    IF @invPers IS NOT NULL`
    - Si se halló estado inválido...
16. `        EXEC dbo.sp_ValidarEstado @invPers, N'Persona', 50001;`
    - Lanza error específico mediante procedimiento.
17. `    -- Para actualizaciones, impedir la desactivación del propio`
   `    -- usuario o de alguien de igual o mayor jerarquía`
   - Comentario de la segunda validación.
18. `    IF UPDATE(idEstado)`
    - Solo aplica en actualizaciones del estado.
19. `    BEGIN`
    - Inicio del bloque condicional.
20. `        DECLARE @actor INT = dbo.fn_actor_id();`
    - Obtiene el id del empleado que ejecuta la operación.
21. `        IF @actor IS NULL`
    - Si no está definido...
22. `            THROW 50080, 'SESSION_CONTEXT(''idEmpleado'') no establecido.', 1;`
    - Arroja error por falta de contexto.
23. `        DECLARE @idInactivo INT =`
    - Variable para el estado inactivo.
24. `               (SELECT idEstado FROM dbo.Estado`
    - Consulta en la tabla `Estado`.
25. `                WHERE modulo = N'Persona' AND nombre = N'Inactivo');`
    - Busca el valor 'Inactivo' del módulo Persona.
26. `        DECLARE @actorUsuario NVARCHAR(50) =`
    - Variable para el usuario actual.
27. `               (SELECT usuario FROM dbo.Empleado WHERE idPersona = @actor);`
    - Consulta el nombre de usuario.
28. `        DECLARE @tgt TABLE (idPersona INT);`
    - Tabla temporal para los objetivos a desactivar.
29. `        INSERT INTO @tgt (idPersona)`
    - Llena la tabla temporal.
30. `        SELECT i.idPersona`
    - Toma los ids de las filas afectadas.
31. `        FROM inserted i`
    - Desde la imagen nueva.
32. `        JOIN deleted d ON d.idPersona = i.idPersona`
    - Solo aquellas que estaban antes.
33. `        WHERE i.idEstado = @idInactivo`
    - Filtra las que pasan a inactivo.
34. `          AND d.idEstado <> @idInactivo;`
    - Y que antes no lo estaban.
35. `        IF @actorUsuario <> N'admin'`
    - Si el usuario no es el administrador...
36. `        BEGIN`
    - Inicio del control de permisos.
37. `            /* 3.1 – no puede desactivarse a sí mismo */`
    - Comentario de la regla.
38. `            IF EXISTS (SELECT 1 FROM @tgt WHERE idPersona = @actor)`
    - Verifica si se intenta desactivar a sí mismo.
39. `                THROW 50081, 'No puede desactivar su propia cuenta.', 1;`
    - Lanza error correspondiente.
40. `            /* 3.2 – no puede desactivar par/superior */`
    - Comentario de la segunda regla.
41. `            IF EXISTS (`
    - Inicio de la consulta de jerarquía.
42. `                SELECT 1`
    - Verificación de existencia.
43. `                  FROM @tgt tgt`
    - Toma los objetivos guardados.
44. `                  JOIN dbo.Empleado eTgt ON eTgt.idPersona = tgt.idPersona`
    - Asocia cada objetivo con su empleado.
45. `                  JOIN dbo.Empleado eAct ON eAct.idPersona = @actor`
    - Une con el empleado que ejecuta la acción.
46. `                  JOIN dbo.Rol rT ON rT.idRol = eTgt.idRol`
    - Rol del objetivo.
47. `                  JOIN dbo.Rol rA ON rA.idRol = eAct.idRol`
    - Rol del actor.
48. `                 WHERE rT.nivel <= rA.nivel`
    - Impide desactivar pares o superiores.
49. `            )`
    - Fin de la consulta.
50. `                THROW 50082, 'No puede desactivar a un usuario de igual o mayor jerarquía.', 1;`
    - Emite el error si la condición se cumple.
51. `        END;`
    - Cierra el bloque de permisos.
52. `    END;`
    - Finaliza el bloque de actualización.
53. `END;`
    - Cierre total del trigger.
54. `GO`
    - Separador de lote.

## trg_ParametroSistema_AdminOnly

1. `/* Solo administradores pueden modificar parametros del sistema */`
   - Comentario sobre la protección.
2. `CREATE OR ALTER TRIGGER trg_ParametroSistema_AdminOnly`
   - Crea o reemplaza el trigger.
3. `ON dbo.ParametroSistema`
   - Asociado a la tabla `ParametroSistema`.
4. `WITH EXECUTE AS OWNER`
   - Ejecuta con los permisos del propietario.
5. `AFTER INSERT, UPDATE, DELETE`
   - Se dispara en cualquier modificación.
6. `AS`
   - Inicio del bloque.
7. `BEGIN`
   - Comienzo de las instrucciones.
8. `    SET NOCOUNT ON;`
   - Suprime cuenta de filas.
9. `    SET XACT_ABORT ON;`
   - Cancela la transacción ante un error.
10. `    IF NOT EXISTS (SELECT 1 FROM inserted) AND NOT EXISTS (SELECT 1 FROM deleted) RETURN;`
    - Finaliza si no hubo cambios.
11. `    EXEC dbo.sp_CheckAdminTrigger;`
    - Llama al procedimiento que valida si el usuario es administrador.
12. `END;`
    - Fin del trigger.
13. `GO`
    - Fin del lote de T‑SQL.
