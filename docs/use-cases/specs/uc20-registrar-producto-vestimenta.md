# UC20 Registrar producto vestimenta

## Actores
- Administrador
- Empleado

## Descripción
Registra un producto de tipo vestimenta con manejo de tallas.

## Evento disparador
El actor selecciona **Nuevo Producto** (ALT+N) y elige el tipo **Vestimenta**.

## Precondiciones
- El actor se encuentra en la pantalla **Gestión de Productos**.
- Se cuenta con las tallas que tendrá el producto y el formulario se muestra vacío.
- Las listas de **Categoría** y **Tipo** pueden actualizarse con **F5**.

## Flujo normal
1. El sistema habilita la sección de tallas para ingresar **Talla** y **Stock** por fila.
   El actor añade filas con **ALT++** y puede quitarlas con **ALT+-**.
   Se muestran campos de nombre, descripción, categoría, tipo, unidad, precios, umbral y opciones de **Mayorista** o **Para pedido**.
2. El actor completa la información general y añade las tallas necesarias.
3. El actor presiona **Registrar** (ALT+R).
4. El sistema valida que los datos obligatorios estén completos y que las tallas contengan valores numéricos.
5. El sistema comprueba que no existan tallas repetidas y que el precio unitario, el umbral y el stock por talla sean positivos.
6. El sistema registra el producto con sus tallas y asigna el stock inicial por talla mostrando una superposición de espera.
7. El sistema cierra el formulario, muestra la notificación **Producto registrado** y actualiza la lista de productos.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Complete los datos requeridos** y permite corregir.
- **A2: Alguna talla tiene valores no numéricos**
  1. El sistema indica que el campo correspondiente **debe ser numérico**.
- **A3: Talla duplicada en el paso 5**
  1. El sistema muestra **La talla ya está registrada** y no completa el registro.
- **A4: El actor cierra la ventana**
  1. El sistema pregunta **¿Descartar cambios?** y solo cierra si confirma.

## Postcondiciones
- Producto de vestimenta registrado con sus tallas disponibles en inventario.
- Se genera un movimiento de inventario con motivo **Stock inicial** por la suma del stock inicial de todas las tallas.
