# UC24 Editar producto fraccionable

## Actores
- Administrador
- Empleado

## Descripción
Modifica un producto fraccionable y las presentaciones que maneja.

## Evento disparador
El actor selecciona un producto fraccionable y hace clic en **Editar**.

## Precondiciones
- El producto seleccionado es de tipo fraccionable.
- El actor inició sesión y se encuentra en la sección **Productos**.
- El formulario de edición muestra sus datos actuales, presentaciones cargadas y el **Umbral** configurado.
- El tipo de producto aparece deshabilitado y el stock inicial no se muestra.
- Las listas de **Categoría** y **Tipo** pueden recargarse con **F5**.
- El formulario permite cambiar **Para pedido** y la opción **Mayorista**.

## Flujo normal
1. El sistema presenta un formulario con la información vigente del producto.
2. El actor ajusta nombre, descripción, categoría, precios, **Umbral** y las opciones **Para pedido** o **Mayorista** según corresponda.
   Puede refrescar las listas con **F5**.
3. El actor edita las presentaciones existentes o agrega nuevas filas con **Cantidad** y **Precio** usando **ALT++** para añadir o **ALT+-** para quitar filas.
4. El sistema valida que no existan cantidades duplicadas y que cada valor sea numérico.
5. El actor elimina presentaciones obsoletas si es necesario.
6. El sistema confirma que exista al menos una presentación restante.
7. El actor presiona **Guardar** (**ALT+G**).
8. El sistema verifica que los campos obligatorios estén completos y que los valores numéricos,
   incluido el **Umbral**, sean válidos.
9. Si se marcó **Mayorista**, el sistema comprueba que el precio mayorista sea menor al unitario.
10. El sistema solicita confirmación con el mensaje **¿Guardar cambios?**.
11. El actor confirma la actualización.
12. El sistema guarda los cambios mostrando una superposición de espera y
    luego muestra la notificación **Producto actualizado**.
13. El sistema refresca la lista de productos.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Complete los datos requeridos** y mantiene el formulario abierto.
- **A2: Alguna presentación tiene valores no numéricos**
  1. El sistema indica que el campo correspondiente **debe ser numérico**.
- **A3: Presentación duplicada en el paso 4**
  1. El sistema muestra **La cantidad ya existe** y no guarda la edición.
- **A4: Precio mayorista no válido en el paso 7**
  1. El sistema muestra **El precio mayorista debe ser menor al precio unitario** y solicita corrección.
- **A5: Sin presentaciones válidas en el paso 6**
  1. El sistema indica **Debe mantener al menos una presentación** y no continúa.
- **A6: El actor cancela en el paso 10**
  1. El sistema descarta las modificaciones y cierra el formulario.
- **A7: El actor cierra la ventana durante la edición**
  1. El sistema pregunta **¿Descartar cambios?** y solo cierra si el actor confirma.

## Postcondiciones
- Producto fraccionable actualizado con sus presentaciones disponibles.
