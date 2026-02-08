# UC18 Gestionar productos

## Actores
- Administrador
- Empleado

## Descripción
Panel principal para filtrar, visualizar y administrar productos.

## Evento disparador
El actor ingresa a la opción **Productos** desde el menú principal.

## Precondiciones
- El actor se encuentra autenticado.
- El buscador puede enfocarse con **F3** y los filtros de categoría y tipo están cargados.
- Los filtros pueden actualizarse con **F5**.

## Flujo normal
1. El sistema muestra un buscador y los filtros de **Categoría** y **Tipo de producto** junto a la tabla de resultados.
2. El actor selecciona una categoría o un tipo de la lista y escribe texto en el buscador si es necesario.
3. El actor presiona **Actualizar** (F5) para aplicar los filtros.
4. El sistema consulta los productos que coinciden, actualiza la tabla y los ordena por nombre.
5. El sistema indica **Sin datos para mostrar** cuando no existen coincidencias y mantiene las acciones deshabilitadas.
6. Al seleccionar una fila el sistema carga sus dependencias y habilita las acciones disponibles: **Ver detalle** (ALT+V), **Editar** (ALT+E), **Activar** (ALT+A) o **Desactivar** (ALT+D).
7. El actor puede editar haciendo doble clic sobre una fila o usar los botones.
8. También puede registrar un producto con **Nuevo Producto** (ALT+N) o crear una categoría con **ALT+C**.
9. El actor puede refrescar la tabla en cualquier momento con **F5** para obtener la información más reciente.

## Flujos alternativos
- **A1: El actor intenta editar sin seleccionar un producto**
  1. El sistema mantiene el botón **Editar** deshabilitado.
- **A2: El actor presiona Actualizar sin filtros**
  1. El sistema lista todos los productos disponibles.

## Postcondiciones
- La gestión de productos queda lista para registrar o modificar información adicional.
