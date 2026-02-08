# UC31 Cancelar venta

## Actores
- Administrador
- Empleado

## Descripción
Anula una venta existente registrando el motivo de cancelación.

## Evento disparador
El actor elige **Cancelar Venta** desde la pantalla de seguimiento de ventas.

## Precondiciones
- El actor está autenticado y visualiza la lista de ventas.
- La venta seleccionada se encuentra en estado **Completada**.
- La lista puede recargarse con **F5** y el campo **Desde** se enfoca con **F3**.

## Flujo normal
1. El actor selecciona una venta de la tabla y presiona **Cancelar**.
2. El sistema resalta la fila y abre el diálogo para ingresar el motivo de anulación.
3. El actor escribe el motivo y confirma la cancelación.
4. El sistema valida que la venta continúe en estado **Completada** y que el motivo no esté vacío.
5. El sistema registra el motivo, cambia el estado a **Cancelada** y refresca la lista con la nueva marca.
6. El sistema muestra la notificación **Venta cancelada**.


## Flujos alternativos
- **A1: Sin selección en el paso 1**
  1. El sistema muestra **Seleccione una venta**.
- **A2: Motivo vacío en el paso 3**
  1. El sistema indica **Debe indicar el motivo de cancelación** y mantiene el diálogo activo.
- **A3: Estado diferente en el paso 4**
  1. El sistema informa **Sólo ventas en estado 'Completada' pueden cancelarse** y no aplica cambios.
- **A4: El actor cierra el diálogo en el paso 2**
  1. El sistema no realiza cambios y vuelve a mostrar la lista.

## Postcondiciones
- Venta marcada como cancelada en el historial.
- Stock restituido y pagos anulados.

