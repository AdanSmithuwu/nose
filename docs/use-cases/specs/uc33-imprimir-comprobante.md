# UC33 Imprimir comprobante de transacción

## Actores
- Administrador
- Empleado

## Descripción
Permite obtener en formato impreso el comprobante asociado a una venta o pedido.

## Evento disparador
El actor selecciona **Imprimir Comprobante** en la pantalla de seguimiento de ventas o pedidos.

## Precondiciones
- Existe un comprobante generado para la transacción seleccionada.
- La lista puede recargarse con **F5** y el campo **Desde** se enfoca con **F3**.

## Flujo normal
1. El actor marca la transacción deseada y selecciona **Imprimir Comprobante**.
2. El sistema verifica que el comprobante exista, descarga el PDF y muestra una vista previa.
3. El actor revisa el documento y decide continuar.
4. El actor confirma la impresión.
5. El sistema envía el archivo a la impresora predeterminada y deja disponible la descarga en caso sea necesaria.
6. El sistema notifica **Comprobante enviado a impresión**.

## Flujos alternativos
- **A1: No hay comprobante en el paso 1**
  1. El sistema muestra **No hay comprobante disponible** y cancela la impresión.
- **A2: Error de impresión en el paso 5**
  1. El sistema informa **No se pudo imprimir el comprobante** y permite descargar el PDF.
- **A3: Error al guardar el PDF**
  1. El sistema muestra **Error al guardar archivo** y mantiene la vista previa.

## Postcondiciones
- Comprobante enviado a impresión sin modificar la transacción.

