# UC27 Ingresar stock

## Actores
- Administrador
- Empleado

## Descripción
Registra una entrada de stock para un producto existente.

## Evento disparador
El actor selecciona un producto en **Gestión de Inventario** y presiona **Ingresar**.

## Precondiciones
- El actor inició sesión y se encuentra en la pantalla **Gestión de Inventario**.
- El producto a actualizar está seleccionado y la cantidad es mayor a cero.
- El botón **Ingresar** se encuentra habilitado y puede activarse con **ALT+I**.
- La lista de productos puede recargarse con **F5** y el campo de búsqueda puede enfocarse con **F3**.

## Flujo normal
1. El actor indica la cantidad y selecciona la talla o presentación si aplica.
2. El sistema valida que la cantidad sea un número positivo dentro del rango permitido.
3. El sistema verifica que, si el producto es fraccionable, la cantidad coincida con una presentación registrada.
4. El actor confirma con **Ingresar** o la combinación **ALT+I**.
5. El sistema registra un movimiento de tipo **Entrada** asociado al usuario
   mostrando una superposición de espera.
6. El sistema recalcula el stock del producto y actualiza la tabla de inventario.

## Flujos alternativos
- **A1: El actor no ha iniciado sesión**
  1. El sistema muestra **Debe iniciar sesión para registrar movimientos** y no registra la entrada.
- **A2: Cantidad inválida en el paso 2**
  1. El sistema indica **Cantidad debe ser mayor a 0** y no registra el movimiento.
- **A3: Cantidad incompatible con presentaciones en el paso 3**
  1. El sistema muestra **La cantidad no coincide con ninguna presentación** y solicita corregir.
- **A4: El actor cierra la ventana antes de confirmar**
  1. El sistema descarta la operación sin modificar el inventario.
- **A5: Ingreso sin producto seleccionado**
  1. El botón **Ingresar** permanece deshabilitado.

## Postcondiciones
- Stock del producto incrementado y visible en el inventario.
