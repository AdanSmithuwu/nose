# UC07 Actualizar credenciales de empleado

## Actores
- Administrador

## Descripción
Permite modificar el usuario o la contraseña de un empleado.

## Evento disparador
El administrador hace clic en **Credenciales...** desde el formulario de edición de empleado.

## Precondiciones
- Existe un empleado seleccionado para editar.
- El administrador se encuentra en la sección **Empleados** y con el formulario de edición abierto.
- El formulario de credenciales muestra el usuario actual del empleado.
- El foco inicial se ubica en **Usuario** y la casilla de contraseña permite revelar la clave mostrando si **Bloq Mayús** está activo.
- El botón **Guardar** se puede activar con **ALT+G**.

## Flujo normal
1. El sistema muestra los campos **Usuario** y **Nueva contraseña** con el usuario actual precargado.
2. El administrador ingresa los cambios y presiona **Guardar**.
3. El sistema verifica que el nuevo usuario no exista en otra cuenta.
4. Si la contraseña se deja en blanco, el sistema genera una nueva clave aleatoria.
5. El sistema solicita confirmación con el mensaje **¿Actualizar credenciales del empleado?**.
6. El administrador confirma.
7. El sistema muestra una superposición de espera mientras se aplican los cambios.
8. El sistema encripta la contraseña y actualiza los datos.
9. Si se generó una nueva clave, el sistema muestra
   **Credenciales actualizadas** junto con **Nueva contraseña: _clave_**.
10. El sistema reinicia los intentos fallidos y cualquier bloqueo vigente.
11. El administrador anota la nueva clave para el empleado.
12. El sistema registra la modificación en la bitácora.
13. El sistema cierra el diálogo, refresca la lista de empleados y habilita las opciones según el nuevo usuario.

## Flujos alternativos
- **A1: El administrador cancela en el paso 5**
  1. El sistema solicita confirmación si se ingresó una nueva clave o usuario.
  2. Al aceptar, se descartan los cambios y se mantienen las credenciales previas.
- **A2: Usuario duplicado en el paso 3**
  1. El sistema muestra **Nombre de usuario ya registrado**.
  2. El administrador ingresa otro usuario y vuelve al paso 2.

## Postcondiciones
- Credenciales del empleado actualizadas según lo solicitado.
