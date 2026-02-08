# UC08 Eliminar empleado

## Actores
- Administrador

## Descripción
Elimina de forma permanente a un empleado del sistema.

## Evento disparador
El administrador selecciona un empleado y elige **Eliminar**.

## Precondiciones
- El empleado no tiene dependencias activas que impidan su eliminación.
- El administrador se encuentra en la sección **Empleados**.
- La vista indica si existen dependencias asociadas al empleado seleccionado.
- El botón **Eliminar** se puede activar con **ALT+L**.
- Las dependencias pueden ser transacciones, entregas, movimientos, bitácora o reportes.

## Flujo normal
1. El sistema solicita confirmación con el mensaje
   **¿Eliminar permanentemente el empleado seleccionado? Esta acción es irreversible**.
2. El sistema verifica por última vez que no existan dependencias relacionadas.
3. El administrador confirma la acción.
4. El sistema muestra una superposición de espera mientras ejecuta la eliminación.
5. El sistema elimina al empleado y registra el evento en la bitácora.
6. El sistema muestra la notificación **Empleado eliminado**, refresca la lista y actualiza los botones disponibles.

## Flujos alternativos
- **A1: Existen dependencias en el paso 2**
  1. El sistema muestra **No se puede eliminar, dependencias:** seguido de la lista encontrada.
  2. El caso de uso termina.
- **A2: El administrador cancela en el paso 1**
  1. El sistema no realiza ninguna eliminación.

## Postcondiciones
- El empleado ya no está disponible en el sistema.
