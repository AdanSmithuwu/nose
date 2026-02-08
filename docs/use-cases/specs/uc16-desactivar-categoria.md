# UC16 Desactivar categoría

## Actores
- Administrador

## Descripción
El administrador marca una categoría como inactiva.

## Evento disparador
El administrador selecciona una categoría y elige **Desactivar** (ALT+D).

## Precondiciones
- La categoría seleccionada está activa.
- El administrador se encuentra en la sección **Categorías**.
- El buscador puede enfocarse con **F3** y la lista puede refrescarse con **F5**.
- La tabla muestra cuántos productos están asociados a cada categoría.

## Flujo normal
1. El administrador selecciona una categoría de la lista.
2. El sistema calcula cuántos productos están asociados a dicha categoría.
3. El sistema solicita confirmación con el mensaje
   **¿Desactivar la categoría seleccionada?** e informa el número de productos relacionados.
4. El administrador confirma la operación y el sistema muestra una superposición de espera.
5. El sistema cambia el estado de la categoría y de los productos afectados.
6. El sistema actualiza la lista de categorías y productos visibles. La vista puede refrescarse con **F5** y los botones se ajustan al nuevo estado.
7. El sistema muestra la notificación **Categoría desactivada**.

## Flujos alternativos
- **A1: El administrador cancela en el paso 4**
  1. El sistema no realiza cambios y mantiene el estado anterior.
- **A2: El administrador cierra la ventana**
  1. El sistema no aplica cambios y la categoría permanece activa.

## Postcondiciones
- Categoría marcada como inactiva y productos asociados actualizados.
