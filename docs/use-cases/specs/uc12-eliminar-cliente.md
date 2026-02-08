# UC12 Eliminar cliente

## Actores
- Administrador
- Empleado

## Descripción
Elimina de forma permanente a un cliente del sistema.

## Evento disparador
El actor selecciona un cliente y elige **Eliminar** (ALT+L).

## Precondiciones
- El cliente no tiene dependencias activas que impidan su eliminación.
- El actor se encuentra autenticado en la sección **Clientes**.
- El buscador puede enfocarse con **F3** y la tabla puede refrescarse con **F5**.
- Al seleccionar una fila el sistema consulta sus dependencias y actualiza los botones.

## Flujo normal
1. El actor localiza un cliente mediante el buscador y lo selecciona en la tabla.
2. El sistema verifica en segundo plano si tiene ventas, pedidos o pagos vinculados y muestra un resumen, habilitando o no la opción **Eliminar**.
3. El actor abre las acciones disponibles; si es administrador también ve la opción **Desactivar**.
4. El actor elige **Eliminar**.
5. El sistema solicita confirmación con el mensaje
   **¿Eliminar permanentemente el cliente seleccionado?\nEsta acción es irreversible** y las opciones *Aceptar* y *Cancelar*.
6. El actor confirma la acción y el sistema muestra una superposición de espera.
7. El sistema verifica que no existan dependencias activas.
8. El sistema elimina al cliente y refresca la lista.
9. El sistema muestra la notificación **Cliente eliminado** y la tabla puede
   actualizarse con **F5**.
10. El sistema actualiza los botones según la nueva selección.

## Flujos alternativos
- **A1: Existen dependencias en el paso 7**
  1. El sistema muestra **No se puede eliminar, dependencias:** seguido de la lista encontrada.
  2. El caso de uso termina.
- **A2: El actor cancela en el paso 6**
  1. El sistema no realiza ninguna eliminación.
- **A3: El actor elige *Cancelar* en el menú del paso 3**
  1. El sistema cierra el menú y mantiene la selección sin cambios.

## Postcondiciones
- El cliente ya no está disponible en el sistema.
