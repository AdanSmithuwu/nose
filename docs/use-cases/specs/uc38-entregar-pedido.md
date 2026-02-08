# UC38 Entregar pedido

## Actores
- Administrador
- Empleado

## Descripción
Marca un pedido como entregado registrando los pagos correspondientes.

## Evento disparador
El actor selecciona **Marcar como entregado** en la pantalla de seguimiento de pedidos.

## Precondiciones
- El pedido está en estado **En Proceso**.

- La lista puede recargarse con **F5** y el campo **Desde** se enfoca con **F3**.
## Flujo normal
1. El actor selecciona un pedido pendiente de entrega.
2. El sistema abre el diálogo para registrar los pagos recibidos.
3. El actor ingresa uno o más pagos y presiona **Guardar**.
4. El sistema verifica que la suma de los pagos sea igual al total del pedido.
5. El sistema verifica que exista stock suficiente cuando el pedido es para recojo en tienda.
6. El sistema cambia el estado a **Entregada**, registra la fecha y muestra el diálogo para generar el comprobante.
7. El actor decide si imprimir, descargar o enviar por WhatsApp el comprobante.

## Flujos alternativos
- **A1: Sin selección en el paso 1**
  1. El sistema muestra **Seleccione un pedido**.
- **A2: Estado diferente en el paso 1**
  1. El sistema muestra **Sólo pedidos en 'En Proceso' pueden entregarse** y cancela la operación.
- **A3: Sin pagos en el paso 3**
  1. El sistema muestra **Ingrese un pago** y mantiene el diálogo abierto.
- **A4: Pagos que no cubren el total en el paso 4**
  1. El sistema indica **Los pagos deben igualar el total del pedido** y solicita corrección.
- **A5: Stock insuficiente en el paso 5**
  1. El sistema indica **Stock insuficiente para completar la entrega** y detiene el proceso.
- **A6: El actor cierra el diálogo sin guardar**
  1. El sistema no cambia el estado del pedido.
- **A7: Error al generar comprobante en el paso 6**
  1. El sistema informa **Error generando comprobante** y mantiene el pedido en estado **En Proceso**.

## Postcondiciones
- Pedido entregado con sus pagos registrados y comprobante listo para emitir.
- Si el pedido era para recojo en tienda, el stock se descuenta al finalizar la entrega.

