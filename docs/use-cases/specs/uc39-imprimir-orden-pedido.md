# UC39 Imprimir orden de compra de pedido

## Actores
- Administrador
- Empleado

## Descripción
Permite obtener la orden de compra en PDF para un pedido registrado.

## Evento disparador
El actor selecciona **Imprimir Orden** desde la pantalla de seguimiento de pedidos.

## Precondiciones
- Existe una orden de compra generada para el pedido.
- La lista puede recargarse con **F5** y el campo **Desde** se enfoca con **F3**.

## Flujo normal
1. El actor selecciona el pedido en la tabla de seguimiento y presiona **Imprimir Orden**.
2. El sistema verifica que la orden exista, descarga el PDF y lo muestra en vista previa.
3. El actor revisa el documento y confirma la impresión.
4. El sistema envía el archivo a la impresora configurada y deja disponible la opción de descarga.
5. El sistema notifica **Orden impresa**.

## Flujos alternativos
- **A1: Orden inexistente en el paso 2**
  1. El sistema muestra **Orden de compra no encontrada** y cancela la impresión.
- **A2: Error de impresión en el paso 4**
  1. El sistema informa **No se pudo imprimir la orden** y permite descargar el PDF.
- **A3: Error al guardar el PDF**
  1. El sistema muestra **Error al guardar archivo** y mantiene la vista previa.

## Postcondiciones
- Orden de compra impresa sin modificar el pedido original.
- El PDF queda disponible para descargar.
