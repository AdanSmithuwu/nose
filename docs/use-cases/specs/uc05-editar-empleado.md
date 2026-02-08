# UC05 Editar empleado

## Actores
- Administrador

## Descripción
Permite modificar los datos de un empleado existente.

## Evento disparador
El administrador hace doble clic sobre un empleado en la lista o selecciona **Editar**.

## Precondiciones
- El empleado a modificar existe y está seleccionado en la tabla.
- El administrador se encuentra en la sección **Empleados**.
- El formulario de edición muestra la información actual del empleado.
- El foco inicial se ubica en **Nombre** y los campos muestran texto de ejemplo.
- El botón **Guardar** se puede activar con **ALT+G** y la opción
  **Credenciales...** con **ALT+C**.
- **DNI** es de solo lectura y el campo **Teléfono** admite entre 6 y 15 dígitos.
- El rol se selecciona de una lista desplegable.

## Flujo normal
1. El sistema muestra un formulario con los datos actuales del empleado. El campo **DNI** se presenta solo para lectura.
2. El administrador modifica la información necesaria y presiona **Guardar**.
3. El sistema valida que los campos obligatorios estén completos.
4. El sistema comprueba que el nuevo DNI o usuario no estén asignados a otro empleado.
5. El sistema verifica el formato del teléfono y notifica si es incorrecto.
6. El sistema solicita confirmación con el mensaje **¿Guardar cambios?**.
7. El administrador confirma la actualización.
8. El sistema valida que se haya modificado al menos un dato.
9. El sistema muestra una superposición de espera mientras envía la actualización.
10. El sistema guarda los cambios y muestra la notificación **Empleado actualizado**.
11. El sistema registra la edición en la bitácora con fecha y hora.
12. El sistema cierra el formulario, actualiza la tabla y habilita las acciones correspondientes.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Ingrese nombre, apellidos y DNI** y mantiene el formulario abierto.
- **A2: DNI o usuario ya registrado en el paso 4**
  1. El sistema informa **Empleado con datos duplicados** y no guarda los cambios.
  2. El administrador corrige la información y vuelve al paso 2.
- **A3: Teléfono inválido en el paso 5**
  1. El sistema muestra **Teléfono debe tener entre 6 y 15 dígitos** y mantiene el formulario abierto.
  2. El administrador corrige el número y vuelve al paso 2.
- **A4: El administrador cancela en el paso 6**
  1. El sistema solicita confirmación si existen cambios sin guardar.
  2. Al aceptar, se descartan las modificaciones y el formulario se cierra.

## Postcondiciones
- Datos del empleado actualizados en el sistema.
