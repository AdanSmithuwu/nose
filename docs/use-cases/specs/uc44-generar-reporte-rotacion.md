# UC44 Generar reporte de rotación de productos

## Actores
- Administrador

## Descripción
Crea un informe PDF de los productos más vendidos en un rango de fechas.

## Evento disparador
El administrador accede a **Reporte de Rotación** e indica fechas y el valor de **Top N**.

## Precondiciones
- Existe historial de ventas y pedidos dentro del rango elegido.
- El actor tiene permisos para generar reportes.
- Al abrir la ventana los campos **Desde** y **Hasta** muestran el día actual, **Top N** se establece en 0 y los botones de acción están deshabilitados.
- El campo **Desde** se enfoca con **F3** y la consulta se actualiza con **F5**.

## Flujo normal
1. El sistema presenta los campos **Desde**, **Hasta** y **Top N** con un botón de recarga (F5) y la tabla vacía con el mensaje **Sin datos para mostrar**. La tabla incluye las columnas *#*, *Producto*, *Categoría*, *Unidades* e *Importe*.
2. El administrador define el rango de fechas y el valor de **Top N**. La consulta se ejecuta de inmediato y también puede usarse el botón de recarga (**F5**) para actualizar.
3. El sistema llena la tabla de rotación con los productos más vendidos ordenados por unidades, ajusta los anchos y muestra el importe asociado a cada uno.
4. Si la tabla contiene filas se habilitan los botones **Generar PDF** e **Imprimir**.
5. El administrador presiona **Generar PDF** (ALT+G) para producir el archivo, guardarlo en el historial y elegir dónde almacenarlo.
6. También puede usar **Imprimir** (ALT+I) para generar el PDF, guardarlo y enviarlo directamente a la impresora.
7. El sistema ejecuta la acción solicitada con el PDF creado.

## Flujos alternativos
- **A1: Error al guardar el archivo en el paso 5**
  1. El sistema muestra **Error al guardar archivo** y mantiene la ventana activa.
- **A2: Error de impresión en el paso 6**
  1. El sistema muestra **Error al imprimir el reporte** y no cierra la ventana.
- **A3: Rango de fechas inválido en el paso 2**
  1. El sistema muestra **El rango 'desde' no puede ser posterior a 'hasta'** y mantiene la tabla sin cambios.

## Postcondiciones
- El reporte de rotación queda disponible para consulta e impresión.
