# UC23 Editar producto vestimenta

## Actores
- Administrador
- Empleado

## Descripción
Permite modificar un producto de tipo vestimenta y sus tallas asociadas.

## Evento disparador
El actor selecciona un producto vestimenta y hace clic en **Editar**.

## Precondiciones
- El producto seleccionado es de tipo vestimenta.
- El actor inició sesión y se encuentra en la sección **Productos**.
- El formulario de edición muestra sus datos actuales, tallas cargadas y el **Umbral** configurado.
- El tipo de producto aparece deshabilitado y el stock inicial no se muestra.
- Las listas de **Categoría** y **Tipo** pueden actualizarse con **F5**.
- El formulario permite cambiar **Para pedido** y la opción **Mayorista**.

## Flujo normal
1. El sistema muestra un formulario con la información existente del producto.
2. El actor ajusta nombre, descripción, categoría, precios, **Umbral** y las opciones **Para pedido** o **Mayorista** según corresponda.
   Puede refrescar las listas con **F5**.
3. El actor modifica las tallas existentes o agrega nuevas filas con **Talla** y **Stock** usando **ALT++** para añadir o **ALT+-** para quitar filas.
4. El sistema valida que cada talla sea única para el producto y que la cantidad sea numérica.
5. El actor elimina tallas si es necesario.
6. El sistema advierte si alguna talla con stock no puede eliminarse.
7. El actor presiona **Guardar** (**ALT+G**).
8. El sistema verifica que los campos obligatorios estén completos y que los valores numéricos,
   incluido el **Umbral**, sean válidos.
9. Si se marcó **Mayorista**, el sistema comprueba que el precio mayorista sea menor al unitario.
10. El sistema solicita confirmación con el mensaje **¿Guardar cambios?**.
11. El actor confirma la actualización.
12. El sistema guarda el producto mostrando una superposición de espera y
    luego muestra la notificación **Producto actualizado**.
13. El sistema refresca la lista de productos.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Complete los datos requeridos** y mantiene el formulario abierto.
- **A2: Alguna talla tiene valores no numéricos**
  1. El sistema indica que el campo correspondiente **debe ser numérico**.
- **A3: Talla duplicada en el paso 4**
  1. El sistema muestra **La talla ya está registrada** y no guarda la edición.
- **A4: Precio mayorista no válido en el paso 7**
  1. El sistema muestra **El precio mayorista debe ser menor al precio unitario** y solicita corrección.
- **A5: Intento de eliminar talla con stock en el paso 5**
  1. El sistema informa **No se puede eliminar talla con stock** y mantiene la fila.
- **A6: Sin tallas registradas en el paso 7**
  1. El sistema indica **Debe registrar al menos una talla** y no guarda la edición.
- **A7: El actor cancela en el paso 10**
  1. El sistema descarta las modificaciones y cierra el formulario.
- **A8: El actor cierra la ventana durante la edición**
  1. El sistema pregunta **¿Descartar cambios?** y solo cierra si el actor confirma.

## Postcondiciones
- Producto vestimenta actualizado y disponible con sus tallas.
