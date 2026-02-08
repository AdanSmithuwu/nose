# UC17 Eliminar categoría

## Actores
- Administrador

## Descripción
Elimina permanentemente una categoría del sistema.

## Evento disparador
El administrador selecciona una categoría y elige **Eliminar** (ALT+L).

## Precondiciones
- La categoría seleccionada no posee dependencias activas.
- El administrador se encuentra en la sección **Categorías**.
- El buscador puede enfocarse con **F3** y la lista puede refrescarse con **F5**.
- Al seleccionar una fila el sistema consulta sus dependencias y actualiza los botones.

## Flujo normal
1. El administrador selecciona una categoría de la tabla.
2. El sistema consulta en segundo plano si dicha categoría posee dependencias registradas y actualiza los botones disponibles.
3. El administrador elige la opción **Eliminar**.
4. El sistema solicita confirmación con el mensaje
   **¿Eliminar permanentemente la categoría seleccionada?\nEsta acción es irreversible** y las opciones *Aceptar* y *Cancelar*.
5. El administrador confirma la eliminación y el sistema muestra una superposición de espera.
6. El sistema verifica que no existan dependencias activas.
7. El sistema borra la categoría y actualiza la lista.
8. El sistema muestra la notificación **Categoría eliminada** y la tabla puede
   refrescarse con **F5** y los botones se ajustan a la nueva selección.

## Flujos alternativos
- **A1: Existen dependencias en el paso 6**
  1. El sistema muestra **No se puede eliminar, dependencias:** y detalla cada una.
  2. El caso de uso termina.
- **A2: El administrador cancela en el paso 5**
  1. La categoría permanece sin cambios.
- **A3: El administrador cierra la ventana**
  1. El sistema no realiza la eliminación.

## Postcondiciones
- Categoría eliminada del catálogo.
