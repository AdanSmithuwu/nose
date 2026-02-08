# Guía para agentes de ComercialValerio

Este repositorio es un proyecto Maven multi-módulo que usa Java 21.
Las siguientes pautas ayudan al agente al editar código y documentación.
Este proyecto tiene módulos separados para `common`, `domain`, `infrastructure`, `application`, `presentation-ui` y `db`. Cada módulo contiene un archivo `AGENTS.md` con notas específicas de la capa.

## Compilación
- Compila todos los módulos con `mvn -q -DskipTests package`.
- No ejecutes pruebas ni incluyas comandos que intenten correrlas.

## Estilo de código
- Los archivos Java usan 4 espacios de indentación y un máximo de 120 caracteres por línea.
- Coloca las llaves de apertura en la misma línea que las sentencias de control y las declaraciones.
- Los comentarios y la documentación deben estar en español.

## Estilo de commits
- Escribe los mensajes de commit en inglés usando el modo imperativo.
- Mantén la línea resumen por debajo de 72 caracteres. Agrega detalles en el cuerpo si es necesario.

## Pull requests
- Título: resumen en una línea del cambio.
- Cuerpo: describe lo que se modificó.
