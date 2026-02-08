# UC30 Registrar venta

## Actores
- Administrador
- Empleado

## Descripción
Registra una venta al contado generando su comprobante.

## Evento disparador
El actor selecciona **Nueva Venta** desde el menú principal.

## Precondiciones
- El actor se encuentra autenticado.
- La ventana **Nueva Venta** lista los productos y clientes disponibles.
- Los campos de pago permanecen deshabilitados hasta elegir un método.
- La tabla de detalles está vacía.
- El formulario indica atajos de teclado: **ALT+A** para Añadir,
  **ALT+Q** para Quitar, **ALT+N** para registrar cliente y **ALT+G**
  para generar el comprobante.
- Las listas de productos y clientes pueden recargarse con **F5** y el buscador
  puede enfocarse con **F3**.

## Flujo normal
1. El actor escribe un criterio en el buscador y selecciona un producto disponible.
2. El sistema muestra el precio unitario y el stock actual del producto elegido.
3. El actor digita la cantidad deseada y presiona **Agregar**.
4. El sistema valida que el producto esté seleccionado, que la cantidad sea un
   número entero mayor que 0 y que haya stock suficiente.
5. El sistema agrega el detalle, actualiza los totales parciales y limpia los
   campos de ingreso.
6. El actor puede quitar un producto de la tabla en caso de error.
7. El actor repite los pasos 1‑5 por cada producto necesario.
8. El actor selecciona al cliente de la venta o registra uno nuevo si no existe.
9. El actor marca el método de pago (**Billetera Digital** y/o **Efectivo**).
10. El sistema habilita las cajas de monto y copia automáticamente el total si
    solo se escogió un método.
11. Opcionalmente, el actor presiona **Añadir Observación** y registra un
    comentario.
12. El actor digita o corrige los montos y presiona **Generar Comprobante**.
13. El sistema confirma que exista al menos un producto agregado y que los pagos
    registrados sumen el total de la venta.
14. El sistema muestra el resumen con el total calculado, el cliente seleccionado
    y la opción **Enviar por WhatsApp**.
15. Si el cliente tiene teléfono registrado, el sistema lo prellena y activa la
    casilla de WhatsApp.
16. El actor verifica o completa el teléfono (solo dígitos entre 6 y 15) y
    confirma si enviará el PDF al número indicado.
17. El sistema genera el comprobante y pregunta **¿Qué desea hacer con el
    comprobante?** con opciones *Imprimir* o *Descargar*.
18. Tras la acción elegida, el sistema muestra la notificación **Venta registrada**
    y limpia el formulario.

## Flujos alternativos
- **A1: Producto duplicado en el paso 3**
  1. El sistema indica **Ya existe un detalle con el mismo producto** y no lo agrega.
- **A2: Cantidad inválida en el paso 3**
  1. El sistema muestra **Cantidad debe ser un número entero mayor que 0**.
- **A3: Stock insuficiente en el paso 4**
  1. El sistema informa **Stock insuficiente** y evita el agregado.
- **A4: Sin productos en el paso 12**
  1. El sistema muestra **Agregue al menos un producto** y mantiene la ventana activa.
- **A5: Sin pagos o pagos incompletos en el paso 12**
  1. El sistema indica **Los pagos deben sumar el total de la venta** y solicita corrección.
- **A6: Teléfono inválido en el paso 16**
  1. El sistema muestra **Teléfono debe tener entre 6 y 15 dígitos** y mantiene el diálogo activo.
- **A7: Límite para ovillos superado en el paso 3**
  1. El sistema muestra **Cantidad máxima 199 para ovillos en venta** y no agrega el producto.
- **A8: Error al generar el comprobante en el paso 17**
  1. El sistema informa **Error generando comprobante** y detiene el proceso.
- **A9: Error de impresión en el paso 17**
  1. El sistema indica **No se pudo imprimir el comprobante** y mantiene disponible la descarga.
- **A10: Error al guardar el PDF en el paso 17**
  1. El sistema muestra **Error al guardar archivo** y permite intentar nuevamente.
- **A11: El actor cancela en el paso 16**
  1. El sistema no registra la venta y mantiene la información para corrección.

## Postcondiciones
- Venta registrada con su comprobante disponible para impresión o descarga.
- Stock descontado para cada producto vendido.
