# UC35 Editar pedido

## Actores
- Administrador
- Empleado

## Descripción
Permite modificar los productos o cantidades de un pedido pendiente de entrega.

## Evento disparador
El actor selecciona un pedido de la lista y elige **Editar**.

## Precondiciones
- El pedido está en estado **En Proceso**.
- Se muestran los datos y detalles actuales en el formulario de edición.
- El formulario indica atajos: **ALT+A** para Añadir, **ALT+Q** para Quitar y
  **ALT+G** para guardar los cambios.
- El buscador de productos puede enfocarse con **F3**.
- La lista de productos puede recargarse con **F5**.
- Si se trata de un **Pedido Especial**, la lista solo permite ovillos de hilo.

## Flujo normal
1. El actor agrega o quita productos según sea necesario.
2. El sistema valida la cantidad de cada artículo, evita duplicados, comprueba el stock y actualiza los totales.
3. Tras cada ajuste la tabla se refresca con el subtotal actualizado.
4. El actor ajusta la opción de vale de gas si corresponde.
5. El actor revisa el nuevo total calculado.
6. El actor presiona **Guardar**.
7. El sistema registra los cambios y muestra **Pedido actualizado** o **Pedido Especial actualizado** según el tipo.
8. El sistema cierra el formulario de edición.

## Flujos alternativos
- **A1: Cantidad insuficiente para mayorista en el paso 2**
  1. El sistema muestra **Debe alcanzar el mínimo mayorista** y mantiene el pedido en edición.
- **A2: Producto repetido en el paso 1**
  1. El sistema indica **Ya existe un detalle con el mismo producto** y no lo agrega.
- **A3: Producto no permitido en un pedido especial**
  1. El sistema muestra **Solo puede añadir ovillos de hilo** y mantiene el pedido en edición.
- **A4: El actor cierra el formulario sin guardar**
  1. El sistema descarta los cambios y mantiene el pedido original.

## Postcondiciones
- Pedido actualizado y visible con los nuevos datos en el seguimiento.
- Para pedidos a domicilio, el stock queda ajustado según las cantidades finales.

