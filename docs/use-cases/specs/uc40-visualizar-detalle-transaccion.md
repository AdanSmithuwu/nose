# UC40 Visualizar detalle de transacción

## Actores
- Administrador
- Empleado

## Descripción
Muestra toda la información de una venta o pedido seleccionado.

## Evento disparador
El actor hace doble clic en una fila de **Seguimiento de Ventas** o **Seguimiento de Pedidos**.

## Precondiciones
- El actor se encuentra en la pantalla correspondiente.
- La lista puede recargarse en cualquier momento con **F5** y el campo **Desde** se enfoca con **F3**.
- La tabla contiene al menos una transacción listada y alguna de ellas está seleccionada.

## Flujo normal
1. El actor hace doble clic sobre la transacción a revisar o presiona **Enter** con la fila seleccionada.
2. El sistema obtiene el ID desde la tabla y consulta
   `GET /api/ventas/{id}` o `GET /api/pedidos/{id}` para recuperar los datos de
   cabecera junto con los productos y pagos asociados.
3. Si la transacción existe, abre un diálogo de solo lectura titulado **Detalle de Venta** o **Detalle de Pedido**.
4. En la parte superior del diálogo se muestran el ID, las fechas de registro y entrega (si aplica), el cliente, el empleado, la dirección, el tipo de pedido, el total neto, el estado y la observación. Cuando corresponde se indica el monto de **Vale Gas** recibido.
5. El sistema llena la tabla de productos con las columnas *Producto*, *Talla*, *Cant*, *P.Unit* y *Sub* y ajusta sus anchos automáticamente.
6. Luego carga la tabla de pagos indicando el método utilizado y el monto y ajusta sus columnas.
7. Cuando alguna tabla queda vacía se muestra **Sin datos para mostrar**.
8. Si no hay pagos y el estado es **En Proceso**, se indica **Aún no hay pagos registrados** en la sección de pagos.
9. El actor revisa la información y cierra el diálogo con el botón **Cerrar** (ALT+C) o la tecla **Esc**.
10. Las tablas permiten ordenar la información haciendo clic en sus encabezados.

## Flujos alternativos
- **A1: La transacción ya no existe en el paso 1**
  1. El sistema muestra **Transacción no encontrada** y refresca la tabla.

## Postcondiciones
- Se visualiza el detalle completo de la transacción sin modificarla.
