# Guía del módulo infrastructure

Esta capa implementa la persistencia, seguridad e integraciones como Twilio, Google Drive y la generación de PDF.

## Compilación
- Compila con `mvn -q -pl infrastructure -am -DskipTests package`.
- No ejecutes pruebas ni agregues lógica de tests.

## Recursos
- `src/main/resources/application.properties` contiene las credenciales de servicios externos.
- `src/main/resources/META-INF/persistence.xml` configura JPA.

El proveedor de conexiones `DataSourceProvider` es un bean `@ApplicationScoped`
que crea los pools en `@PostConstruct` y los cierra en `@PreDestroy`.

## Pautas
- Los comentarios y la documentación deben estar en español.
