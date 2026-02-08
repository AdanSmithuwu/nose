# UC19 Registrar producto unidad fija

## Actores
- Administrador
- Empleado

## Descripción
Registra un nuevo producto vendido por unidades indivisibles.

## Evento disparador
El actor hace clic en **Nuevo Producto** (ALT+N) dentro de la gestión de productos.

## Precondiciones
- El actor está en la pantalla **Gestión de Productos**.
- El formulario de registro se muestra vacío con los campos obligatorios marcados.
- Las listas de **Categoría** y **Tipo** pueden recargarse con **F5**.

## Flujo normal
1. El sistema presenta campos de nombre, descripción, categoría, tipo, unidad, precios, umbral y stock inicial.
   También permite indicar si aplica **Mayorista** o **Para pedido** con su tipo por defecto.
2. El actor completa todos los campos requeridos y presiona **Registrar** (ALT+R).
3. El sistema valida que el nombre y la categoría existan y que el tipo seleccionado corresponda a **Unidad fija**.
4. El sistema comprueba que el precio unitario, el umbral y el stock inicial sean números positivos.
5. Si se marcó **Mayorista**, el sistema verifica que el precio mayorista sea menor al unitario y que el mínimo mayorista sea válido.
6. El sistema confirma que no exista otro producto con el mismo nombre.
7. El sistema guarda el producto y registra los movimientos iniciales de stock mostrando una superposición de espera.
8. El sistema cierra el formulario, muestra la notificación **Producto registrado** y refresca la lista de productos.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Complete los datos requeridos** y mantiene el formulario activo.
- **A2: Precio mayorista no válido en el paso 5**
  1. El sistema muestra **El precio mayorista debe ser menor al precio unitario** y solicita corrección.
- **A3: Nombre de producto duplicado en el paso 6**
  1. El sistema informa **Ya existe un producto con ese nombre** y no completa el registro.
- **A4: Valores numéricos inválidos en el paso 4**
  1. El sistema indica **Cantidad y precios deben ser números válidos**.
- **A5: El actor cierra la ventana**
  1. El sistema pregunta **¿Descartar cambios?** y solo cierra si confirma.

## Postcondiciones
- Producto tipo unidad fija registrado y disponible para inventario y ventas.
- Se genera un movimiento de inventario con motivo **Stock inicial** por la cantidad indicada.
