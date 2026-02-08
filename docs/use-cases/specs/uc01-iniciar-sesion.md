# UC01 Iniciar sesión

## Actores
- Empleado (incluye rol Administrador)

## Descripción
El empleado ingresa sus credenciales para autenticar su identidad y acceder al sistema.

## Evento disparador
El empleado hace clic en **Iniciar sesión** desde la pantalla de acceso.

## Precondiciones
- El empleado posee una cuenta registrada y activa.
- El contador de intentos fallidos está por debajo del límite de bloqueo.
- La cuenta se bloquea tras **3** intentos fallidos durante **5 minutos**.
- La pantalla de acceso muestra los campos **Usuario** y **Contraseña** vacíos con texto de ejemplo para cada uno.
- La casilla de contraseña se muestra enmascarada, indica si **Bloq Mayús** está activo y permite revelar la clave.
- El cambio de modo claro/oscuro se puede realizar con **F6**.
- El empleado puede enfocar el campo **Usuario** presionando **F3**.
- Los campos de texto no permiten ingresar emojis.
- El foco inicial se encuentra en **Usuario** y el botón **Iniciar sesión** se habilita solo cuando ambos campos contienen datos.
- El empleado recuerda su usuario y contraseña vigentes.

## Flujo normal
1. El sistema solicita **Usuario** y **Contraseña**.
2. El sistema indica si **Bloq Mayús** está activo y permite revelar la contraseña temporalmente.
3. El empleado introduce ambas credenciales y confirma con **Enter** o pulsando el botón predeterminado de inicio de sesión.
4. El sistema muestra una superposición de espera mientras valida la información.
5. El sistema verifica que el usuario existe.
6. El sistema valida que la cuenta esté activa.
7. El sistema comprueba que la cuenta no esté bloqueada.
8. El sistema valida que la contraseña sea correcta.
9. El sistema registra el intento en la bitácora de accesos con el usuario y el resultado.
10. El sistema reinicia el contador de fallos.
11. El sistema registra la fecha y hora del acceso exitoso.
12. El sistema habilita las opciones disponibles según el rol del empleado y abre el menú principal.
13. El sistema oculta la superposición y muestra la notificación **Acceso concedido**.

## Flujos alternativos
- **A1: Campos vacíos en el paso 3**
  1. El sistema muestra **Ingrese usuario y contraseña**.
  2. El empleado vuelve al paso 1.
- **A2: Credenciales inválidas en el paso 8**
  1. El sistema incrementa el contador de intentos fallidos.
  2. El sistema registra el intento fallido en la bitácora.
  3. Si se alcanza el tercer intento fallido, la cuenta se bloquea por 5 minutos.
  4. El sistema muestra **Credenciales inválidas** y permite reintentar.
  5. El sistema limpia el campo **Contraseña** para el siguiente intento.
- **A3: Cuenta bloqueada en el paso 6**
  1. El sistema registra el intento fallido en la bitácora.
  2. El sistema muestra **Cuenta bloqueada (_X_ minutos restantes)**.
  3. El caso de uso termina.
- **A4: Cuenta inactiva en el paso 5**
  1. El sistema registra el intento fallido en la bitácora.
  2. El sistema indica que la cuenta está inactiva.
  3. El caso de uso termina.
- **A5: El empleado cierra la pantalla de acceso**
  1. El empleado sale de la aplicación antes de ingresar credenciales.
  2. El caso de uso termina sin registrar ningún intento.

## Postcondiciones
- Sesión iniciada y menú principal disponible para el empleado.
- Registro de la fecha, hora y resultado del intento de inicio de sesión.

