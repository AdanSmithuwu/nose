# UC41 Consultar ventas y pedidos

## Actores
- Administrador
- Empleado

## Descripción
Permite filtrar y revisar las transacciones registradas en el sistema.

## Evento disparador
El actor ingresa a **Seguimiento de Ventas** o **Seguimiento de Pedidos** desde el menú principal.

## Precondiciones
- El actor está autenticado.
- Al abrir la pantalla los campos de fecha toman el día actual y las listas muestran **Todas** las categorías y **Todos** los productos.
- La lista puede recargarse con **F5** y el campo **Desde** se enfoca con **F3**.

## Flujo normal
1. El sistema presenta los filtros **Desde**, **Hasta**, **Categoría** y **Producto** junto a una tabla vacía con las columnas *Fecha*, *Cliente*, *Empleado*, *Total* y *Estado*; la primera columna de ID se mantiene oculta.
2. El actor selecciona el rango de fechas y la categoría de interés.
3. El sistema carga los productos asociados a la categoría elegida.
4. Opcionalmente el actor elige un producto de la lista desplegable.
5. Al modificar los filtros la tabla se actualiza automáticamente. El actor puede usar **Actualizar** (F5) en cualquier momento para recargar los datos.
6. El sistema verifica que la fecha final no sea anterior a la inicial.
7. Luego consulta las transacciones que cumplen con los criterios y llena la tabla ordenada por fecha, ocultando la columna de ID y ajustando los anchos automáticamente.
8. El actor puede ordenar la tabla por cualquiera de las columnas para revisar la información.
9. Si la búsqueda no arroja resultados se muestra **Sin datos para mostrar**.
10. El actor puede repetir los pasos 2 a 9 para refinar la consulta o limpiar los filtros.
11. Con una fila seleccionada, el actor puede abrir el detalle mediante doble clic o la tecla **Enter**.

## Flujos alternativos
- **A1: Rango de fechas inválido en el paso 6**
  1. El sistema muestra **Fecha fin anterior a fecha inicio** con el título **Rango de fechas inválido** y borra la fecha final.

## Postcondiciones
- La lista queda filtrada para que el actor realice otras operaciones.
