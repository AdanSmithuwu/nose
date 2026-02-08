# UC11 Desactivar cliente

## Actores
- Administrador
- Empleado

## Descripción
El actor cambia el estado de un cliente a inactivo.

## Evento disparador
El actor selecciona un cliente y elige **Desactivar** (ALT+D).

## Precondiciones
- El cliente seleccionado está activo y visible en la tabla.
- El actor se encuentra autenticado en la sección **Clientes**.
- El buscador puede enfocarse con **F3** y la lista puede refrescarse con **F5**.
- La tabla habilita los botones de acción según el estado del cliente.

## Flujo normal
1. El actor localiza al cliente con el buscador y el filtro de estado.
2. El actor selecciona la fila correspondiente.
3. El sistema consulta las dependencias y actualiza los botones, dejando **Desactivar** disponible.
4. El actor abre las acciones de **Cliente**; si es administrador también ve **Eliminar permanentemente**.
5. El actor elige **Desactivar**.
6. El sistema solicita confirmación con el mensaje **¿Desactivar el cliente seleccionado?** y las opciones *Aceptar* y *Cancelar*.
7. El actor confirma la acción y el sistema muestra una superposición de espera.
8. El sistema envía la petición para cambiar el estado y marca al cliente como inactivo.
9. El sistema registra la fecha del cambio, actualiza la tabla con **F5**, actualiza los botones y habilita **Activar** para la fila.
10. El sistema muestra la notificación **Cliente desactivado**.

## Flujos alternativos
- **A1: El actor cancela en el paso 6**
  1. El sistema no modifica el estado del cliente ni refresca la tabla.
- **A2: El actor elige *Cancelar* en el menú del paso 3**
  1. El sistema cierra el menú y no realiza ninguna acción.

## Postcondiciones
- Cliente marcado como inactivo en el sistema.
