# UC09 Registrar cliente

## Actores
- Administrador
- Empleado

## Descripción
Se registra un nuevo cliente para futuras transacciones.

## Evento disparador
El actor selecciona **Nuevo Cliente** en la pantalla de clientes.

## Precondiciones
- El actor se encuentra autenticado con permisos para registrar clientes.
- Se encuentra en la sección **Clientes** del sistema.
- El formulario de registro se muestra vacío y listo para ingresar datos.
- El foco inicial se ubica en **Nombre** y los campos presentan texto de ejemplo.
- El botón **Registrar** se puede activar con **ALT+R**.
- **DNI** acepta solo dígitos y **Teléfono** debe contener entre 6 y 15 dígitos.
- El campo **Dirección** cuenta con una longitud máxima predefinida.

## Flujo normal
1. El sistema muestra un formulario con los campos nombre, apellidos, DNI, teléfono y dirección. Los campos incluyen texto guía y validaciones de longitud.
2. El actor ingresa los datos y confirma con **Registrar**.
3. El sistema valida que la información obligatoria sea correcta.
4. El sistema comprueba que no exista otro cliente con el mismo DNI o teléfono.
5. El sistema verifica el formato del teléfono.
6. El sistema muestra una superposición de espera mientras guarda la información.
7. El sistema guarda al cliente y asigna un identificador interno.
8. El sistema muestra la notificación **Cliente registrado**.
9. El sistema registra la creación y actualiza la lista para incluir al nuevo cliente.
10. El sistema cierra el formulario y habilita las acciones correspondientes.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Ingrese nombre, apellidos, DNI y dirección** y mantiene el formulario activo.
- **A2: Cliente duplicado en el paso 4**
  1. El sistema advierte **Cliente ya registrado** y no permite guardar.
  2. El actor revisa los datos y vuelve al paso 2.
- **A3: Teléfono inválido en el paso 5**
  1. El sistema muestra **Teléfono debe tener entre 6 y 15 dígitos** y mantiene el formulario activo.
  2. El actor corrige el número y vuelve al paso 2.
- **A4: El actor cancela en el paso 2**
  1. El sistema solicita confirmación si existen datos ingresados.
  2. Al aceptar, el formulario se cierra sin guardar la información.

## Postcondiciones
- Cliente agregado al catálogo y disponible para operaciones posteriores.
