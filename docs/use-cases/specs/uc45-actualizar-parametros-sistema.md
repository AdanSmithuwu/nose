# UC45 Actualizar parámetros del sistema

## Actores
- Administrador

## Descripción
Permite modificar valores globales que intervienen en los cálculos y en la configuración de la aplicación.

## Evento disparador
El administrador abre **Parámetros** desde el menú principal o con `Ctrl+T`.

## Precondiciones
- El actor ha iniciado sesión.
- Se muestran los parámetros en una tabla con las columnas *Clave*, *Valor* y *Descripción*.
- El botón **Actualizar** (F5) permite recargar la lista.
- El botón **Editar** (ALT+E) se habilita únicamente cuando hay una fila seleccionada.

## Flujo normal
1. El sistema lista los parámetros configurables y muestra **Sin datos para mostrar** si la tabla está vacía.
2. El actor selecciona un parámetro y elige **Editar** (ALT+E) o hace doble clic sobre la fila.
3. El sistema abre el diálogo **Editar Parámetro** con los campos *Clave* y *Descripción* en solo lectura, el campo *Valor* con el marcador *Ingrese valor* y el botón **Guardar** (ALT+G).
4. El actor modifica el valor y presiona **Guardar**.
5. El sistema valida la entrada:
   - Si la clave contiene *CANTIDAD* o *MARGEN*, el valor debe ser entero.
   - En otro caso debe ser numérico.
   - Siempre debe ser mayor o igual a 0.
   - Si la validación falla, se muestra un diálogo **Dato inválido** con mensajes como **"Valor para X debe ser un número entero"**, **"Valor para X debe ser numérico"** o **"Valor para X debe ser mayor o igual a 0"**.
6. Si el valor es correcto, el sistema guarda el parámetro y cierra el diálogo.
7. La tabla se refresca mostrando el nuevo valor.
8. Si el actor intenta cerrar el diálogo con cambios sin guardar, el sistema pregunta **¿Descartar cambios?** y solo cierra si confirma.

## Flujos alternativos
- **A1: Valor no válido en el paso 5**
  1. Se muestra el mensaje correspondiente y el diálogo permanece abierto.
- **A2: Error al guardar en el paso 6**
  1. El sistema informa el problema mediante la ventana de error y mantiene los valores anteriores.

## Postcondiciones
- Los parámetros quedan actualizados y afectan de inmediato las operaciones del sistema.
