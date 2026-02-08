# UC37 Registrar pedido especial

## Actores
- Administrador
- Empleado

## Descripción
Registra un pedido especial para recojo posterior en tienda.

## Evento disparador
El actor selecciona **Pedido Especial** en el menú de transacciones.

## Precondiciones
- El actor está autenticado.
- La ventana de registro lista clientes y solo productos de tipo **ovillo de hilo** habilitados para pedidos especiales.
- El formulario ofrece atajos: **ALT+A** para Añadir, **ALT+Q** para Quitar,
  **ALT+N** para registrar cliente y **ALT+R** para registrar el pedido.
- Las listas de productos y clientes pueden recargarse con **F5** y el buscador puede enfocarse con **F3**.

## Flujo normal
1. El actor elige un cliente existente.
2. El actor añade productos válidos indicando la cantidad requerida.
3. El sistema valida cada cantidad, verifica el stock disponible, evita duplicados y actualiza los totales.
4. El actor puede eliminar un producto si se equivocó y continuar agregando más artículos.
5. El actor revisa el subtotal calculado.
6. Opcionalmente, el actor presiona **Añadir Observación** y escribe un comentario.
7. El actor presiona **Registrar**.
8. El sistema guarda el pedido y muestra la notificación **Pedido Especial registrado**.
9. El sistema limpia los campos para un nuevo registro.

## Flujos alternativos
- **A1: Cantidad fuera de rango en el paso 2**
  1. El sistema muestra **Cantidad inválida** y no agrega el producto.
- **A2: No se alcanza el mínimo mayorista**
  1. El sistema muestra **Debe alcanzar el mínimo mayorista** y detiene el registro.
- **A3: Producto repetido en el paso 2**
  1. El sistema indica **Ya existe un detalle con el mismo producto** y evita el duplicado.
- **A4: Producto no permitido en el paso 2**
  1. El sistema muestra **Solo puede añadir ovillos de hilo** y no agrega el producto.
- **A5: El actor cancela antes de registrar**
  1. El sistema descarta los datos ingresados y vuelve a la lista de pedidos.

## Postcondiciones
- Pedido especial almacenado para su posterior seguimiento.

