# Guía para los scripts de base de datos

Los archivos SQL inicializan la base de datos `cv_ventas_distribucion` para SQL Server.

## Ejecución de scripts
Ejecuta los scripts con una herramienta que reconozca el separador de lotes `GO` en este orden. Carga las funciones antes de los triggers para que existan las definiciones al crearlos:
1. `DDL.sql`
2. `TVF_y_SF.sql`
3. `SP.sql`
4. `Triggers.sql`
5. `VW.sql`
6. `Security.sql`
7. `CatalogInserts.sql`
8. `ExamplePeople.sql`
9. `ProductBatches.sql`
10. `InitialInventory.sql`

Si se crean scripts adicionales de vistas, ejecútalos después de `SP.sql` y antes de cargar cualquier dato de ejemplo para que los procedimientos puedan depender de esas vistas.

`DDL.sql` crea índices como `IX_AlertaStock_Procesada` y primero verifica si la base de datos `cv_ventas_distribucion` existe. Si no existe la crea automáticamente, por lo que el script puede ejecutarse varias veces sin error.
- Los comentarios y la documentación deben estar en español.
