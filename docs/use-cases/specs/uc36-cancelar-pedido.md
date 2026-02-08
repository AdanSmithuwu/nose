# UC36 Cancelar pedido

## Actores
- Administrador
- Empleado

## Descripción
Permite anular un pedido que aún no ha sido entregado.

## Evento disparador
El actor selecciona **Cancelar Pedido** desde la pantalla de seguimiento de pedidos.

## Precondiciones
- El pedido está en estado **En Proceso**.

- La lista puede recargarse con **F5** y el campo **Desde** se enfoca con **F3**.
## Flujo normal
1. El actor marca un pedido en la tabla y elige **Cancelar**.
2. El sistema abre el cuadro para indicar el motivo de la anulación.
3. El actor escribe el motivo y confirma.
4. El sistema valida que el pedido permanezca en estado **En Proceso** y que el motivo no esté vacío.
5. El sistema registra el motivo, cambia el estado a **Cancelada**, actualiza la lista y muestra la notificación **Pedido cancelado**.

## Flujos alternativos
- **A1: Sin selección en el paso 1**
  1. El sistema muestra **Seleccione un pedido**.
- **A2: Estado no válido en el paso 4**
  1. El sistema muestra **Sólo pedidos en 'En Proceso' pueden cancelarse** y no aplica cambios.
- **A3: Motivo vacío en el paso 3**
  1. El sistema indica **Debe indicar el motivo de cancelación** y mantiene el diálogo abierto.
- **A4: El actor cierra el diálogo en el paso 2**
  1. El sistema no modifica el pedido y regresa a la lista.

## Postcondiciones
- Pedido registrado como cancelado en el historial.
- Stock restituido si se descontó al crear o modificar el pedido.

