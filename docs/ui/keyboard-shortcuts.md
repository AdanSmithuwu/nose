# Guía de atajos de teclado

Este documento resume las prácticas recomendadas para registrar y documentar atajos en la interfaz Swing.

## Principios generales

- **F5** es el único atajo para recargar datos. No se deben usar combinaciones como "Alt+F5".
- Los tooltips deben incluir la combinación de teclas entre paréntesis.
- Todos los botones y campos con atajos deben configurarse mediante `KeyUtils.setTooltipAndMnemonic`.

## Formularios Refreshable

Todo formulario que implemente la interfaz `Refreshable` debe registrar la acción de recarga con **F5** mediante `KeyUtils.registerRefreshAction`. Se prohíbe utilizar combinaciones como **Alt+A**, **Alt+F5** u otras variantes para las operaciones de refresco.

## Ejemplo

El siguiente fragmento muestra la combinación de `UIUtils.createRefreshButton` y
`KeyUtils.registerRefreshAction` para implementar la recarga:

```java
JButton btnRefresh = UIUtils.createRefreshButton(controller::refresh);
KeyUtils.registerRefreshAction(this, controller::refresh);

JButton btnGenerar = new JButton();
KeyUtils.setTooltipAndMnemonic(btnGenerar, KeyEvent.VK_G, "Generar");
```

El primer bloque crea un botón de recarga con la tecla **F5**. El segundo aplica la mnemónica **Alt+G** y muestra el tooltip `Generar (Alt+G)`.

