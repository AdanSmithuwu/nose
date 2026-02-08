# UC04 Registrar empleado

## Actores
- Administrador

## Descripción
El administrador crea una cuenta de empleado y obtiene las credenciales iniciales.

## Evento disparador
El administrador selecciona **Nuevo Empleado** en la pantalla de gestión de empleados.

## Precondiciones
- El administrador se encuentra autenticado.
- Se encuentra en la sección **Empleados** del sistema.
- El formulario de registro se muestra sin datos previos.
- El foco inicial se ubica en **Nombre** y los campos muestran un texto de ejemplo.
- El botón **Registrar** se puede activar con **ALT+R**.
- **DNI** admite solo dígitos y **Teléfono** debe contener entre 6 y 15 dígitos.
- La casilla de contraseña permite revelar la clave y advierte si **Bloq Mayús** está activo.

## Flujo normal
1. El sistema presenta campos de nombre, apellidos, DNI, teléfono y contraseña opcional. Cada uno incluye texto guía y validaciones de longitud.
2. El administrador ingresa la información requerida y presiona **Registrar**.
3. El sistema valida que los campos obligatorios no estén vacíos.
4. El sistema comprueba que no exista otro empleado con el mismo DNI.
5. El sistema normaliza el teléfono para conservar solo dígitos.
6. Si la contraseña se deja vacía, el sistema genera una clave aleatoria y la encripta.
7. El sistema genera un nombre de usuario único a partir de las iniciales y apellidos, asigna el rol **Empleado** y muestra una superposición de espera mientras guarda la información.
8. El sistema crea el empleado con estado activo, registra la fecha de creación y la operación en la bitácora.
9. El sistema muestra el diálogo **Credenciales Generadas** con el usuario y la clave temporal.
10. El administrador toma nota de las credenciales y cierra el diálogo.
11. El nuevo empleado aparece en la lista y los botones se actualizan.

## Flujos alternativos
- **A1: Datos incompletos en el paso 2**
  1. El sistema muestra **Ingrese nombre, apellidos y DNI** y solicita corregir.
- **A2: DNI duplicado en el paso 4**
  1. El sistema muestra **Empleado ya registrado** y no permite continuar.
  2. El administrador ajusta la información y vuelve al paso 2.
- **A3: Teléfono inválido en el paso 5**
  1. El sistema muestra **Teléfono debe tener entre 6 y 15 dígitos** y solicita corregir.
  2. El administrador corrige el número y vuelve al paso 2.
- **A4: El administrador cancela en el paso 2**
  1. El sistema solicita confirmación si existen datos ingresados.
  2. Al aceptar, el formulario se cierra sin guardar la información.

## Postcondiciones
- Empleado registrado y visible en la lista de empleados.
