# UC43 Generar reporte mensual de transacciones

## Actores
- Administrador

## Descripción
Genera y guarda un reporte en PDF con el detalle de transacciones del mes seleccionado.

## Evento disparador
El administrador abre la opción **Reporte Mensual** y escoge el año y mes a consultar.

## Precondiciones
- Se dispone de transacciones registradas en el periodo.
- El actor tiene permisos para generar reportes.
- Al abrir la ventana los campos **Año** y **Mes** toman el periodo actual y los botones de acción están deshabilitados.
- El campo **Año** se enfoca con **F3** y la recarga se efectúa con **F5**.

## Flujo normal
1. El sistema muestra los controles **Año** y **Mes**, un botón de recarga (F5), un gráfico vacío y las tablas sin datos con el texto **Sin datos para mostrar**. La primera tabla incluye las columnas *Día*, *Transacciones* y *Monto*.
2. El administrador ajusta el año y el mes; el sistema actualiza automáticamente la tabla diaria, la tabla de categorías y el gráfico de barras con la información del periodo. La tabla de categorías muestra *Categoría*, *Transacciones* y *Monto*. Si desea refrescar los datos manualmente puede usar el botón de recarga (**F5**).
3. Además se muestran los totales por tipo de pedido y se habilitan los botones **Generar**, **Exportar a PDF** e **Imprimir**.
4. El administrador presiona **Generar** (ALT+G) para guardar el reporte en el historial y abrirlo.
5. El PDF se abre de forma automática y puede imprimirse desde los botones habilitados.
6. El administrador puede usar **Exportar a PDF** (ALT+P) para guardar el archivo o **Imprimir** (ALT+I) para enviarlo a la impresora.
7. El sistema ejecuta la acción solicitada sobre el reporte generado.

## Flujos alternativos
- **A1: Error al abrir el PDF en el paso 5 o 6**
  1. El sistema informa **No se pudo abrir el PDF** y mantiene la ventana activa.
- **A2: Error de impresión en el paso 6**
  1. El sistema muestra **Error al imprimir el reporte** y no cierra la ventana.

## Postcondiciones
- El reporte mensual queda guardado en el historial y disponible para impresión.
