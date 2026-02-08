# UC06 Desactivar empleado

## Actores
- Administrador

## Descripción
El administrador cambia el estado de un empleado a inactivo.

## Evento disparador
El administrador selecciona un empleado y elige **Desactivar**.

## Precondiciones
- El empleado seleccionado está activo.
- El administrador se encuentra en la sección **Empleados**.
- El empleado seleccionado no coincide con la cuenta en uso, salvo que la sesión pertenezca al usuario "admin".
- El botón **Desactivar** se puede activar con **ALT+D**.
- La lista resalta la fila seleccionada y muestra el estado actual del empleado.

## Flujo normal
1. El sistema solicita confirmación con el mensaje **¿Desactivar el empleado seleccionado?**.
2. El administrador confirma la acción.
3. El sistema verifica que el administrador posea jerarquía suficiente para realizar la baja.
4. El sistema muestra una superposición de espera mientras aplica el cambio.
5. El sistema cambia el estado del empleado a inactivo e impide nuevos accesos.
6. El sistema registra la operación en la bitácora y actualiza la fecha de modificación.
7. El sistema cierra cualquier sesión abierta de ese empleado.
8. El sistema refresca la lista y actualiza los botones disponibles.

## Flujos alternativos
- **A1: El administrador cancela en el paso 1**
  1. El sistema no realiza cambios y mantiene el estado anterior.
- **A2: El administrador intenta desactivar su propia cuenta**
  1. El sistema muestra **No puede desactivar su propia cuenta**.
  2. El caso de uso termina.
- **A3: Jerarquía insuficiente en el paso 3**
  1. El sistema informa **No puede desactivar a un usuario de igual o mayor jerarquía**.
  2. El caso de uso termina.

## Postcondiciones
- Empleado marcado como inactivo en el sistema.
