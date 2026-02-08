# UC10 Editar cliente

## Actores
- Administrador
- Empleado

## Descripción
Permite actualizar los datos de un cliente existente.

## Evento disparador
El actor selecciona un cliente de la lista y hace clic en **Editar**.

## Precondiciones
- El cliente a modificar existe y está seleccionado.
- El actor se encuentra en la sección **Clientes**.
- El formulario de edición muestra la información actual del cliente.
- El foco inicial se ubica en **Nombre** y los campos presentan texto de ejemplo.
- El botón **Guardar** se puede activar con **ALT+R**.
- **DNI** es de solo lectura y **Teléfono** debe contener entre 6 y 15 dígitos.
- El campo **Dirección** tiene una longitud máxima definida.

## Flujo normal
1. El sistema muestra un formulario con la información actual del cliente. El campo **DNI** es solo de lectura.
2. El actor modifica los datos necesarios y presiona **Guardar**.
3. El sistema valida que la información obligatoria esté completa.
4. El sistema verifica que el nuevo DNI o teléfono no pertenezcan a otro cliente.
5. El sistema confirma el formato del teléfono.
6. El sistema solicita confirmación con el mensaje **¿Guardar cambios?**.
7. El actor confirma la actualización.
8. El sistema verifica que se haya modificado al menos un dato del cliente.
9. El sistema muestra una superposición de espera mientras se envían los datos.
10. El sistema guarda los cambios y muestra la notificación **Cliente actualizado**.
11. El sistema registra la edición, refresca la lista y actualiza los botones disponibles.
12. El sistema cierra el formulario.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Ingrese nombre, apellidos, DNI y dirección** y mantiene el formulario abierto.
- **A2: DNI o teléfono existente en el paso 4**
  1. El sistema informa **Cliente con datos duplicados** y no guarda la edición.
  2. El actor corrige la información y vuelve al paso 2.
- **A3: Teléfono inválido en el paso 5**
  1. El sistema muestra **Teléfono debe tener entre 6 y 15 dígitos** y mantiene el formulario abierto.
  2. El actor corrige el número y vuelve al paso 2.
- **A4: El actor cancela en el paso 6**
  1. El sistema solicita confirmación si existen datos modificados sin guardar.
  2. Al aceptar, se descartan los cambios y se cierra el formulario sin modificar al cliente.

## Postcondiciones
- Información del cliente actualizada correctamente.
