# UC26 Eliminar producto

## Actores
- Administrador

## Descripción
Elimina de forma permanente un producto del sistema. Solo puede realizarse
cuando el único movimiento de inventario registrado es el motivo
"Stock inicial".

## Evento disparador
El administrador selecciona **Eliminar permanentemente** sobre un producto.

## Precondiciones
- El administrador inició sesión y se encuentra en la sección **Productos**.
- El producto solo puede eliminarse si su único movimiento de inventario es el motivo "Stock inicial" y no tiene transacciones ni órdenes de compra vigentes.
- El botón **Eliminar permanentemente** solo está disponible para administradores.
- La lista se puede recargar con **F5** y el buscador admite foco con **F3**.

## Flujo normal
1. El sistema solicita confirmación con el mensaje
   **¿Eliminar permanentemente el producto seleccionado?\nEsta acción es irreversible**.
2. El administrador confirma la eliminación.
3. El sistema verifica nuevamente que el producto no tenga movimientos de inventario distintos al motivo "Stock inicial" ni registros asociados.
4. El sistema borra el producto mostrando una superposición de espera y
   refresca la lista.
5. El sistema registra la acción y muestra la notificación **Producto eliminado**.

## Flujos alternativos
- **A1: El administrador cancela en el paso 1**
  1. El sistema no realiza ninguna eliminación.
- **A2: Existen dependencias en el paso 3**
  1. El sistema muestra **No se puede eliminar: existen movimientos o transacciones relacionadas** y conserva el producto.
- **A3: El usuario no posee privilegios de administrador**
  1. El sistema indica **Solo un administrador puede eliminar productos** y cancela la operación.
- **A4: El administrador cierra la ventana de confirmación**
  1. El sistema no elimina el producto y conserva la lista sin cambios.
- **A5: El administrador intenta eliminar sin seleccionar producto**
  1. El botón **Eliminar permanentemente** permanece deshabilitado.

## Postcondiciones
- El producto ya no está disponible en el catálogo ni en el inventario.
