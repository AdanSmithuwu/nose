# UC15 Editar categoría

## Actores
- Administrador

## Descripción
Permite modificar los datos de una categoría existente.

## Evento disparador
El administrador selecciona una categoría de la lista y hace clic en **Editar** (ALT+E).

## Precondiciones
- La categoría a modificar existe y está seleccionada.
- El formulario de edición muestra la información actual con los campos rellenados.
- El buscador puede enfocarse con **F3** y la lista puede refrescarse con **F5**.

## Flujo normal
1. El sistema muestra un formulario con el nombre y la descripción actuales, situando el foco en **Nombre**.
2. El administrador modifica los campos necesarios y presiona **Guardar** (ALT+G).
3. El sistema valida que el nombre no esté vacío, que respete la longitud permitida y que no exista otra categoría con el mismo nombre.
4. El sistema verifica que la descripción no exceda el tamaño máximo configurado.
5. El sistema guarda los cambios mostrando una superposición de espera y cierra el formulario.
6. El sistema notifica **Categoría actualizada** y refresca la lista.

## Flujos alternativos
- **A1: Nombre vacío en el paso 2**
  1. El sistema muestra **Ingrese el nombre de la categoría** y mantiene el formulario abierto.
- **A2: Nombre duplicado en el paso 3**
  1. El sistema muestra **Ya existe una categoría con ese nombre** y no guarda los cambios.
- **A3: Descripción demasiado larga en el paso 4**
  1. El sistema indica **La descripción supera el máximo permitido** y no guarda los cambios.
- **A4: El administrador cierra la ventana sin guardar**
  1. El sistema pregunta **¿Descartar cambios?** y solo cierra si confirma.

## Postcondiciones
- Datos de la categoría actualizados en el sistema.
