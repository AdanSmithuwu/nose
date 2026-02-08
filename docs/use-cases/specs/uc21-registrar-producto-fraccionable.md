# UC21 Registrar producto fraccionable

## Actores
- Administrador
- Empleado

## Descripción
Registra un producto fraccionable con distintas presentaciones. Estas
presentaciones solo son formas de descontar stock, no almacenamientos
separados.

## Evento disparador
El actor selecciona **Nuevo Producto** y elige el tipo **Fraccionable**.

## Precondiciones
- El actor inició sesión y se encuentra en la pantalla **Gestión de Productos**.
- El formulario de registro se muestra vacío.
- Se definieron las presentaciones que tendrá el producto.
- Las listas **Categoría** y **Tipo** están cargadas y pueden actualizarse con **F5**.
- El formulario muestra el campo **Umbral** y permite marcar **Para pedido** con su **Tipo** por defecto.

## Flujo normal
1. El sistema habilita la sección de presentaciones con columnas **Cantidad** y **Precio**,
   y muestra campos de nombre, descripción, categoría, unidad, precios base,
   **Stock inicial** y **Umbral**.
2. El actor ingresa nombre, descripción, categoría, unidad, precios base,
   el **Stock inicial** y el **Umbral** de stock.
   Puede marcar **Para pedido** y escoger el tipo correspondiente.
   Puede refrescar las listas con **F5** y usar **ALT++** para añadir filas o
   **ALT+-** para quitarlas.
3. El actor añade una o más presentaciones indicando **Cantidad** y **Precio** por fila.
4. El sistema valida que las cantidades sean numéricas mayores a cero y que no existan duplicados.
5. El actor marca si aplica **Mayorista** e ingresa el mínimo y precio mayorista.
6. El sistema comprueba que el precio mayorista sea menor al unitario y que los montos ingresados,
   incluido el **Umbral** y el **Stock inicial**, sean válidos.
7. El actor revisa que la lista de presentaciones esté completa y presiona **Registrar** (**ALT+R**).
8. El sistema verifica que los datos obligatorios estén completos y que exista al menos una presentación válida.
9. El sistema confirma que no exista otro producto con el mismo nombre.
10. El sistema guarda el producto mostrando una superposición de espera y
    registra **un movimiento de Stock inicial** con la cantidad indicada,
    dejando el estado **Activo** por defecto.
11. El sistema muestra la notificación **Producto registrado** y actualiza la
    lista de productos.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Complete los datos requeridos** y permite corregir.
- **A2: Alguna presentación tiene valores no numéricos**
  1. El sistema indica que el campo correspondiente **debe ser numérico**.
- **A3: Precio mayorista no válido en el paso 6**
  1. El sistema muestra **El precio mayorista debe ser menor al precio unitario** y solicita corrección.
- **A4: Valores numéricos inválidos en el paso 6**
  1. El sistema indica **Cantidades y montos deben ser números válidos**.
- **A5: Nombre de producto duplicado en el paso 9**
  1. El sistema informa **Ya existe un producto con ese nombre** y no completa el registro.
- **A6: Sin presentaciones válidas en el paso 8**
  1. El sistema muestra **Debe ingresar al menos una presentación** y mantiene el formulario.
- **A7: El actor cierra el formulario sin guardar**
  1. El sistema pregunta **¿Descartar cambios?** y, si confirma, cierra la ventana.

## Postcondiciones
- Producto fraccionable registrado con una sola existencia en inventario y sus
  presentaciones disponibles para la venta.
- Se genera un movimiento de inventario con motivo **Stock inicial** por la cantidad indicada.
