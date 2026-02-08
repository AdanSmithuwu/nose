# Guía del módulo common

Este módulo provee utilidades CDI compartidas usadas en las demás capas.

## Compilación
- Compila solo este módulo con `mvn -q -pl common -am -DskipTests package`.
- No ejecutes pruebas ni agregues lógica de tests.

## Pautas
- Sigue el estilo definido en el `AGENTS.md` de la raíz del repositorio.
- Coloca las nuevas clases bajo el paquete `com.comercialvalerio.common`.
- Los comentarios y la documentación deben estar en español.
