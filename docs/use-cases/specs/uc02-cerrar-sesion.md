# UC02 Cerrar sesión

## Actores
- Empleado (incluye rol Administrador)

## Descripción
El empleado finaliza su sesión y retorna a la pantalla de acceso.

## Evento disparador
El empleado selecciona **Cerrar sesión** desde el menú principal o presiona **Ctrl+L**.

## Precondiciones
- El empleado ha iniciado sesión y se encuentra en el menú principal.
- No hay procesos en curso que requieran confirmación adicional.
- Todas las operaciones pendientes fueron guardadas o canceladas.
- La opción **Cerrar sesión (Ctrl+L)** se encuentra habilitada en el menú principal.

## Flujo normal
1. El sistema solicita confirmación con el mensaje **¿Cerrar sesión?**.
2. El empleado confirma la acción.
3. El sistema verifica si existen formularios con datos sin guardar.
4. Si existen datos pendientes, el sistema avisa **Cambios sin guardar** y permite cancelar.
5. El sistema cierra los módulos abiertos y guarda la configuración temporal.
6. El sistema registra la fecha y hora de la salida en la bitácora.
7. El sistema invalida la sesión, limpia la información del usuario y reinicia las conexiones REST para futuros accesos.
8. El sistema muestra la pantalla de acceso con los campos vacíos, el selector de tema sincronizado y el foco en **Usuario**.
9. El sistema confirma **Sesión cerrada correctamente**.

## Flujos alternativos
- **A1: El empleado cancela en el paso 2**
  1. El sistema mantiene la sesión activa y vuelve al menú principal.
- **A2: Cambios sin guardar en el paso 4**
  1. El sistema permanece en el menú principal y conserva la sesión activa.
- **A3: El empleado cierra la ventana sin confirmar**
  1. El sistema cierra la aplicación y la sesión en curso sin guardar cambios.
  2. Al reiniciar la aplicación se mostrará la pantalla de acceso.

## Postcondiciones
- Sesión finalizada y campos de usuario y contraseña visibles para un nuevo inicio.
