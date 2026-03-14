# Guía del módulo application

Este módulo expone los endpoints REST y se empaqueta como un archivo WAR para WildFly.

## Compilación
- Compila con `mvn -q -pl application -am -DskipTests package`.
- No ejecutes pruebas ni agregues lógica de tests.

## Despliegue
- El despliegue remoto usa el plugin de WildFly configurado en el `pom.xml` de la raíz. Evita modificar esos ajustes salvo que sea absolutamente necesario.
- Los comentarios y la documentación deben estar en español.
