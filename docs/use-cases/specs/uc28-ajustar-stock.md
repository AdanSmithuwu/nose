# UC28 Ajustar stock

## Actores
- Administrador

## Descripción
Realiza un ajuste manual al stock registrado de un producto.

## Evento disparador
El administrador selecciona un producto en **Gestión de Inventario** y presiona **Ajustar**.

## Precondiciones
- El administrador inició sesión y se encuentra en la pantalla **Gestión de Inventario**.
- El producto a ajustar está seleccionado y posee stock registrado.
- El botón **Ajustar** está disponible solo para administradores y puede activarse con **ALT+A**.
- La lista de productos puede recargarse con **F5** y el campo de búsqueda puede enfocarse con **F3**.

## Flujo normal
1. El sistema verifica que la cantidad no supere el stock actual del producto.
   - Si supera el stock, muestra **Stock insuficiente** y cancela la operación.
2. El sistema solicita el motivo del ajuste y la cantidad a registrar.
3. El administrador ingresa la observación, la cantidad y confirma con **Ajustar** o la combinación **ALT+A**.
4. El sistema valida que la cantidad sea numérica y que el motivo no esté vacío.
5. El sistema comprueba que el ajuste no deje el stock en negativo.
6. El sistema registra un movimiento de tipo **Ajuste** con el usuario actual
   mostrando una superposición de espera.
7. El sistema recalcula el stock del producto y refresca la tabla de inventario.

## Flujos alternativos
- **A1: El usuario no tiene privilegios de administrador**
  1. El sistema muestra **Solo un administrador puede ajustar el stock** y cancela la operación.
- **A2: El administrador no ha iniciado sesión**
  1. El sistema muestra **Debe iniciar sesión para registrar movimientos** y no realiza el ajuste.
- **A3: Datos inválidos en el paso 3**
  1. El sistema indica **Motivo obligatorio y cantidad numérica** y permite corregir.
- **A4: Resultado negativo en el paso 4**
  1. El sistema informa **El ajuste no puede dejar el stock negativo** y cancela la operación.
- **A5: El administrador cierra el cuadro de observación**
  1. El sistema no registra el ajuste y mantiene los datos sin cambios.
- **A6: Ajuste sin producto seleccionado**
  1. El botón **Ajustar** permanece deshabilitado.

## Postcondiciones
- Stock del producto ajustado según la operación registrada.
