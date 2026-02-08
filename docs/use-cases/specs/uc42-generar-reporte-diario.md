# UC42 Generar reporte diario de transacciones

## Actores
- Administrador

## Descripción
Genera un resumen diario de ventas y pedidos en formato PDF.

## Evento disparador
El administrador abre la opción **Reporte Diario** y selecciona una fecha.

## Precondiciones
- Existe al menos una transacción registrada en la fecha indicada.
- El actor tiene permisos para generar reportes.
- Al iniciar la ventana la fecha se sitúa en el día actual y los botones **Generar**, **Exportar a PDF** e **Imprimir** están deshabilitados.
- El selector de fecha puede enfocarse con **F3** y la recarga se realiza con **F5**.

## Flujo normal
1. El sistema muestra el selector de fecha, un botón de recarga (F5) y las tablas vacías con el mensaje **Sin datos para mostrar**.
2. El administrador elige la fecha a consultar. La información se actualiza de inmediato y puede usar el botón de recarga (**F5**) para refrescarla manualmente.
3. El sistema llena la tabla principal con la cantidad de ventas, pedidos, monto bruto y neto de la fecha indicada y ajusta sus columnas.
4. El sistema también muestra en una segunda tabla los pagos agrupados por método y actualiza el estado de los botones según la cantidad de filas.
5. Tras revisar los datos el administrador presiona **Generar** (ALT+G).
6. El sistema crea el PDF con el resumen, lo guarda temporalmente y lo abre automáticamente en el visor predeterminado.
7. Después de generar se habilitan las acciones **Exportar a PDF** (ALT+P) e **Imprimir** (ALT+I) para el reporte creado.
8. El administrador puede volver a abrir el archivo con **Exportar a PDF** o enviarlo a la impresora con **Imprimir**.
9. El sistema ejecuta la acción solicitada sobre el PDF generado.

## Flujos alternativos
- **A1: El administrador intenta exportar o imprimir sin haber generado**
  1. El sistema muestra **Genere el reporte primero** con el título **Reporte no generado**.
- **A2: Error al abrir el PDF en el paso 6 u 8**
  1. El sistema informa **No se pudo abrir el PDF** y mantiene la ventana activa.
- **A3: Error de impresión en el paso 8**
  1. El sistema muestra **Error al imprimir el reporte** y no cierra la ventana.

## Postcondiciones
- El reporte diario queda disponible para guardar o imprimir.
