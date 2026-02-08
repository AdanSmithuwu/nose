# Guía del módulo presentation-ui

Este módulo es un cliente de escritorio Swing que consume la API REST.

## Compilación
- Compila con `mvn -q -pl presentation-ui -am -DskipTests package`.
- No ejecutes pruebas ni agregues lógica de tests.

## Ejecución
- Inicia la interfaz localmente con `mvn -pl presentation-ui exec:java`.

## Atajos de la UI
- Todas las acciones de recarga usan **F5** (sin combinaciones con Alt).
- Todas las acciones de refresco se implementan con F5 mediante `UIUtils.createRefreshButton` o `KeyUtils.registerRefreshAction`. Otras combinaciones como "Alt+A" no deben usarse para recargar.
- Los tooltips deben mostrar el acelerador entre paréntesis.
- Utiliza `KeyUtils.setTooltipAndMnemonic` para mantener la consistencia.
- Los comentarios y la documentación deben estar en español.
