# UC25 Desactivar producto

## Actores
- Administrador
- Empleado

## Descripción
Cambia el estado de un producto a inactivo sin eliminarlo del sistema.

## Evento disparador
El actor elige **Desactivar** sobre un producto de la lista.

## Precondiciones
- El producto seleccionado está activo.
- El actor inició sesión y se encuentra en la sección **Productos**.
- El botón **Desactivar** se muestra habilitado y cuenta con el atajo **ALT+D**.
- La lista se puede recargar con **F5** y el buscador admite foco con **F3**.

## Flujo normal
1. El sistema solicita confirmación con el mensaje **¿Desactivar el producto seleccionado?**.
2. El actor confirma la acción con el botón o usando **ALT+D**.
3. El sistema cambia el estado del producto a inactivo mostrando una superposición
   de espera y también desactiva sus tallas y presentaciones.
4. El sistema actualiza la interfaz ocultando las opciones que solo aplican a productos activos.
5. El sistema registra la acción y refresca la lista mostrando una notificación que varía según el tipo:
   - **Unidad fija:** *Producto desactivado*.
   - **Vestimenta:** *Las tallas asociadas fueron marcadas como Inactivo*.
   - **Fraccionable:** *Las presentaciones asociadas fueron marcadas como Inactivo*.

## Flujos alternativos
- **A1: El actor cancela en el paso 1**
  1. El sistema no modifica el estado del producto.
- **A2: El actor con rol Administrador elige eliminar**
  1. Continúa en **UC26 Eliminar producto**.
- **A3: El producto ya se encuentra inactivo**
  1. El sistema informa **El producto ya está desactivado** y no realiza cambios.
- **A4: El actor cierra la ventana de confirmación**
  1. El sistema mantiene el producto activo y regresa a la lista.
- **A5: Intento de desactivar sin seleccionar producto**
  1. El botón **Desactivar** permanece deshabilitado.

## Postcondiciones
- Producto desactivado y excluido de nuevos movimientos o ventas.
