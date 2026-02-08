# Guía para llamadas a procedimientos almacenados

Decidir entre declarar procedimientos mediante `@NamedStoredProcedureQuery` o ejecutarlos con JDBC directo afecta la mantenibilidad y portabilidad. Estas notas reflejan el enfoque de la [guía de ubicación de consultas JPQL](jpql-guidelines.md).

## Cuándo usar `@NamedStoredProcedureQuery`
- El nombre del procedimiento y su estructura de parámetros nunca varían.
- La validación al iniciar la aplicación ayuda a detectar nombres o tipos de parámetros incorrectos.
- Múltiples repositorios reutilizan la misma llamada al procedimiento.
- Basta con mapear resultados directamente a entidades o DTO sencillos.

## Cuándo usar JDBC directo
- El procedimiento devuelve varios conjuntos de resultados o tipos de datos complejos.
- Se requieren características específicas del proveedor o mejoras de rendimiento.
- Los parámetros o la forma de los resultados dependen fuertemente de condiciones de ejecución.
- Se necesita procesamiento en streaming o por lotes de los resultados.
