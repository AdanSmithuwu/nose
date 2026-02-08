# UC22 Editar producto unidad fija

## Actores
- Administrador
- Empleado

## Descripción
Modifica los datos de un producto vendido por unidades indivisibles.

## Evento disparador
El actor selecciona un producto de la lista y hace clic en **Editar**.

## Precondiciones
- El producto a modificar existe y está seleccionado.
- El actor inició sesión y se encuentra en la sección **Productos**.
- El formulario de edición muestra la información actual con el **Umbral** configurado.
- El tipo y el stock inicial aparecen deshabilitados.
- Las listas de **Categoría** y **Tipo** pueden recargarse con **F5**.
- El formulario permite cambiar **Para pedido** y la opción **Mayorista**.

## Flujo normal
1. El sistema presenta un formulario con los datos actuales del producto.
2. El actor modifica nombre, descripción, categoría, unidad, precios y **Umbral** según corresponda.
   Puede activar o desactivar **Para pedido** y **Mayorista**.
   Puede actualizar las listas con **F5**.
3. El actor confirma los cambios con **Guardar** (**ALT+G**).
4. El sistema valida que los campos obligatorios estén completos y que el nombre no se repita en otro producto.
5. El sistema verifica que los valores numéricos, incluido el **Umbral**, sean válidos.
6. Si se marcó **Mayorista**, el sistema comprueba que el precio mayorista sea menor al unitario.
7. El sistema solicita confirmación con el mensaje **¿Guardar cambios?**.
8. El actor acepta la operación.
9. El sistema guarda los cambios mostrando una superposición de espera y
   luego muestra la notificación **Producto actualizado**.
10. El sistema refresca la lista de productos.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Complete los datos requeridos** y mantiene el formulario abierto.
- **A2: Nombre duplicado en el paso 4**
  1. El sistema indica **Ya existe un producto con ese nombre** y no guarda la edición.
- **A3: Precio mayorista no válido en el paso 6**
  1. El sistema muestra **El precio mayorista debe ser menor al precio unitario** y solicita corrección.
- **A4: Valores numéricos inválidos en el paso 5**
  1. El sistema muestra **Cantidad y precios deben ser números válidos**.
- **A5: El actor cancela en el paso 7**
  1. El sistema descarta las modificaciones y cierra el formulario sin guardar.
- **A6: El actor cierra la ventana durante la edición**
  1. El sistema pregunta **¿Descartar cambios?** y solo cierra si el actor confirma.

## Postcondiciones
- Producto unidad fija actualizado en el sistema.
