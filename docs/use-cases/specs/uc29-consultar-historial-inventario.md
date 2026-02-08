# UC29 Consultar historial de inventario

## Actores
- Administrador

## Descripción
Permite revisar los movimientos de inventario aplicando distintos filtros.

## Evento disparador
El administrador accede a **Historial de Inventario** desde el menú principal.

## Precondiciones
- El administrador inició sesión y cuenta con permisos para ver el historial.
- El campo de fecha inicial puede enfocarse con **F3**.
- El historial puede actualizarse en cualquier momento con **F5**.

## Flujo normal
1. El sistema carga los filtros de **Movimiento**, **Categoría**, **Producto**, **Fecha** y **Empleado**.
2. El administrador establece los valores iniciales de los filtros y presiona **Actualizar**.
3. El sistema muestra una superposición de espera mientras obtiene los
   movimientos que cumplen los criterios y llena la tabla ordenada por fecha.
4. El administrador puede refinar la búsqueda o limpiar los filtros en cualquier momento con **F5**.
5. El administrador puede ordenar la tabla por cualquiera de las columnas.
6. Si no hay registros que mostrar, el sistema indica **Sin datos para mostrar**.

## Flujos alternativos
- **A1: Fecha fin anterior a la fecha inicio**
  1. El sistema muestra **Fecha fin anterior a fecha inicio** y deja el campo de fecha fin vacío.
- **A2: Sin registros encontrados**
  1. El sistema mantiene la tabla vacía y permite modificar los filtros.
- **A3: Combinación de filtros no válida**
  1. El sistema indica **Revise los filtros seleccionados** y conserva los valores ingresados.
- **A4: Filtros vacíos en el paso 2**
  1. El sistema lista todos los movimientos disponibles.

## Postcondiciones
- Historial visible con los movimientos filtrados según los criterios indicados.
