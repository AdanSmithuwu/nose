# Guía de ubicación de consultas JPQL

Decidir si almacenar sentencias JPQL como anotaciones `@NamedQuery` o construirlas en las clases de repositorio afecta la legibilidad y el mantenimiento.

## Cuándo usar `@NamedQuery`
- Utilizar consultas nombradas para sentencias que nunca cambian en tiempo de ejecución.
- Definirlas junto a la entidad para que su propósito sea claro.
- El proveedor de persistencia valida estas consultas al iniciar la aplicación.
- Reutilizar la misma consulta nombrada siempre que sea necesario para que la cadena JPQL viva en un solo lugar.

## Cuándo construir consultas en los repositorios
- Construir las consultas de forma programática cuando los parámetros alteran la JPQL real.
- Componer fragmentos de manera dinámica para filtros u opciones de proyección.
- Evitar dispersar cadenas de consulta constantes en los repositorios, pues esto complica cambios futuros.

Confiar en las consultas nombradas para operaciones fijas como `findAll` o `countByEstado`. Para escenarios complejos con condiciones variables, construir la consulta dentro del repositorio.

## Consultas nativas sin entidad
`SessionContextQueries` agrupa las sentencias nativas utilizadas para manipular el contexto de sesión de SQL Server. Como ninguna se asocia a una entidad JPA, no se declaran como `@NamedQuery` y se ejecutan directamente desde `TransactionManager`.
