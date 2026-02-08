# UC34 Registrar pedido

## Actores
- Administrador
- Empleado

## Descripción
Registra un pedido con entrega a domicilio.

## Evento disparador
El actor selecciona **Nuevo Pedido** en la sección de pedidos.

## Precondiciones
- El actor se encuentra autenticado.
- La ventana de registro muestra clientes y productos disponibles.
- El formulario permite atajos: **ALT+A** para Añadir, **ALT+Q** para Quitar,
  **ALT+N** para registrar cliente y **ALT+R** para registrar el pedido.
- Las listas de productos y clientes pueden recargarse con **F5** y el buscador puede enfocarse con **F3**.

## Flujo normal
1. El actor selecciona un cliente con dirección registrada.
2. El sistema muestra la dirección en modo solo lectura y habilita el detalle del pedido.
3. El actor busca un producto, indica la cantidad y presiona **Agregar**.
4. El sistema valida la cantidad, evita duplicados y comprueba el stock disponible.
5. El sistema agrega el artículo, actualiza los totales y limpia los campos de ingreso.
6. El actor puede quitar productos del detalle en caso de error.
7. El actor repite los pasos 3‑5 por cada artículo requerido.
8. El actor marca si utilizará vale de gas y confirma la dirección de entrega.
9. Opcionalmente, el actor presiona **Añadir Observación** y escribe un comentario.
10. El actor presiona **Registrar**.
11. El sistema calcula el cargo por reparto, aplica el descuento por vale si corresponde y registra el pedido.
12. El sistema muestra la notificación **Pedido registrado** y limpia el formulario para un nuevo ingreso.

## Flujos alternativos
- **A1: Cliente sin dirección en el paso 1**
  1. El sistema muestra **El cliente no tiene dirección registrada** y detiene el proceso.
- **A2: Cantidad inválida en el paso 3**
  1. El sistema muestra **Cantidad inválida** y no agrega el producto.
- **A3: Stock insuficiente en el paso 4**
  1. El sistema muestra **Stock insuficiente** y solicita otra cantidad.
- **A4: Producto repetido en el paso 3**
  1. El sistema indica **Ya existe un detalle con el mismo producto** y evita el duplicado.
- **A5: Cantidad total inferior al mínimo mayorista**
  1. El sistema muestra **Debe alcanzar el mínimo mayorista** y no registra el pedido.
- **A6: Sin cliente seleccionado en el paso 1**
  1. El sistema muestra **Seleccione un cliente** y no continúa.
- **A7: Cliente inválido en el paso 1**
  1. El sistema indica **Cliente inválido** y detiene el registro.

## Postcondiciones
- Pedido registrado y listado en el seguimiento de pedidos.
- Si es para entrega a domicilio, el stock se descuenta de inmediato.

