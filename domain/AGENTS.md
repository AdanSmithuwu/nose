# Guía del módulo domain

Este módulo define las entidades de negocio y objetos de valor principales. Su versión publicada es `1.0.0-SNAPSHOT`.

## Compilación
- Compila solo este módulo con `mvn -q -pl domain -am -DskipTests package`.
- No ejecutes pruebas ni agregues lógica de tests.

## Pautas
- Mantén las clases de dominio libres de preocupaciones de persistencia o UI.
- Sigue las convenciones de estilo del `AGENTS.md` de la raíz.
- Los comentarios y la documentación deben estar en español.
