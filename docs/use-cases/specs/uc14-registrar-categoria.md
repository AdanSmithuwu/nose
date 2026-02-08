# UC14 Registrar categoría

## Actores
- Administrador

## Descripción
Se registra una nueva categoría de productos.

## Evento disparador
El administrador selecciona **Nueva Categoría** (ALT+N) en la pantalla de categorías.

## Precondiciones
- El administrador se encuentra autenticado.
- El formulario de registro se muestra vacío con texto de ejemplo.
- El buscador puede enfocarse con **F3** y la lista puede refrescarse con **F5**.

## Flujo normal
1. El sistema presenta un formulario con campos **Nombre** y **Descripción**, ubicando el foco en **Nombre**.
2. El administrador ingresa el nombre y opcionalmente la descripción.
   Puede confirmar con **Registrar** (ALT+R).
3. El sistema valida que el nombre no esté vacío, que respete la longitud permitida y que no exista otra categoría con el mismo nombre.
4. El sistema verifica que la descripción no exceda el tamaño máximo configurado.
5. El sistema guarda la categoría mostrando una superposición de espera y cierra el formulario.
6. El sistema notifica **Categoría registrada**, actualiza la lista y resalta la nueva entrada.

## Flujos alternativos
- **A1: Nombre vacío en el paso 2**
  1. El sistema muestra **Ingrese el nombre de la categoría** y mantiene el formulario activo.
- **A2: Nombre duplicado en el paso 3**
  1. El sistema informa **Ya existe una categoría con ese nombre** y no guarda la nueva entrada.
- **A3: El administrador cierra el formulario**
  1. El sistema pregunta **¿Descartar cambios?** y solo cierra si confirma.

## Postcondiciones
- Categoría agregada al catálogo del sistema.
