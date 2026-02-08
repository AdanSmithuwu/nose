# UC32 Generar comprobante de transacción

## Actores
- Administrador
- Empleado

## Descripción
Emite el comprobante electrónico para una venta o pedido concluido.

## Evento disparador
El actor confirma la generación del comprobante en el diálogo correspondiente.

## Precondiciones
- La transacción se encuentra en estado **Completada** o **Entregada**.
- No existe un comprobante previo para la transacción.

## Flujo normal
1. El sistema muestra el sub‑total, el total final y la casilla **Enviar por WhatsApp**.
2. Si la transacción tiene un teléfono registrado, el sistema lo coloca en el campo correspondiente y marca la casilla.
3. El actor decide si enviarlo e ingresa o corrige el número de destino, el cual
   debe contener entre 6 y 15 dígitos.
4. El actor presiona **Confirmar**.
5. El sistema genera el PDF y, de haberse indicado un número, lo envía por WhatsApp.
6. El sistema habilita las opciones para imprimir o descargar el archivo.
7. El sistema pregunta **¿Qué desea hacer con el comprobante?** y ofrece *Imprimir* o *Descargar*.
8. El actor elige una opción y el sistema realiza la acción solicitada.
9. El sistema notifica **Comprobante generado**.

## Flujos alternativos
- **A1: La transacción ya posee comprobante**
  1. El sistema informa **La transacción ya tiene comprobante** y no genera uno nuevo.
- **A2: Transacción sin el estado requerido**
  1. El sistema indica **La transacción debe estar Completada o Entregada para emitir comprobante**.
- **A3: Error al enviar WhatsApp en el paso 5**
  1. El sistema muestra **No fue posible enviar el mensaje** y permite continuar con la descarga o impresión.
- **A4: Error de impresión en el paso 8**
  1. El sistema informa **No se pudo imprimir el comprobante** y mantiene disponible la descarga.
- **A5: Teléfono con formato inválido en el paso 3**
  1. El sistema indica **Teléfono debe tener entre 6 y 15 dígitos** y solicita corrección.
- **A6: Error al generar el PDF en el paso 5**
  1. El sistema informa **Error generando comprobante** y detiene el proceso.

## Postcondiciones
- Comprobante generado y disponible para impresión, descarga o envío por WhatsApp.

