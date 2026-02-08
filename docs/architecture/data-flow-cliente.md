# Cadena de enlace: Registrar cliente

Este documento describe minuciosamente cómo un nuevo cliente recorre las capas del sistema desde que el operador pulsa **Registrar** en la interfaz gráfica hasta que los datos persisten en la base. Se basa en el caso de uso [UC09 Registrar cliente](../use-cases/specs/uc09-registrar-cliente.md) y muestra por qué cada responsabilidad está separada.

## 1. Presentación (Swing)

1. `DlgClienteNuevo` muestra los campos de ingreso de datos. Al presionar el botón **Registrar**, se invoca el método `registrar()` que delega en `ClienteNuevoController`.
2. El controlador valida los campos y crea un `ClienteCreateDto`. Luego llama a `UiContext.clienteSvc().registrar(dto)`, lo que devuelve inmediatamente el control a la interfaz mientras la tarea se ejecuta en segundo plano.

## 2. Proxies REST en la UI

`UiContext` administra un conjunto de proxies para los servicios remotos. Cuando `clienteSvc()` se invoca por primera vez, construye el proxy mediante `ServiceProxies.create`, el cual utiliza `RestClientFactory` para enlazar la interfaz `ClienteService` con la clase JAX‑RS `ClienteResource`.

`RestClientFactory` genera dinámicamente un `InvocationHandler` que intercepta la llamada. El handler forma la URL base desde `RestClientManager`, invoca al recurso remoto y convierte la respuesta HTTP en el tipo solicitado.

## 3. Capa de aplicación (Servidor REST)

En el servidor, `ClienteResource` recibe la solicitud POST con el DTO. El método `registrar` delega inmediatamente en `ClienteService` para aplicar la lógica de negocio. Tras persistir la entidad, devuelve un `201 Created` con la ubicación del nuevo cliente.

## 4. Servicios de aplicación

`ClienteServiceImpl` es un bean `@ApplicationScoped` con transacciones gestionadas. Su método `registrar` convierte el DTO a la entidad de dominio `Cliente`, llena la fecha de registro y, si no se especificó estado, consulta `EstadoCache` para asignar **Activo**. Finalmente, delega en `ClienteRepository` para guardar la información.

## 5. Dominio e infraestructura

`ClienteRepositoryImpl` implementa la interfaz de dominio. Dentro de un bloque transaccional valida que no exista otro DNI duplicado. Si se trata de un cliente nuevo, ejecuta el procedimiento almacenado `sp_RegistrarCliente`; de lo contrario actualiza las tablas `Persona` y `Cliente` mediante consultas JPQL. Las conexiones provienen de `DataSourceProvider`, que selecciona el pool según el rol de la solicitud.

## 6. Respuesta y actualización de la UI

Al completarse la transacción, la capa de aplicación devuelve el DTO creado. `RestClientFactory` transforma la respuesta HTTP en el objeto Java y `ClienteNuevoController` muestra la notificación **Cliente registrado**. Luego la vista se cierra y `FormClientes` refresca la tabla para reflejar el nuevo registro.

## Separación de responsabilidades

- **Presentación**: valida campos y despliega mensajes. No contiene lógica de negocio ni acceso directo a la base.
- **Proxies**: traducen invocaciones locales en llamadas HTTP sin exponer detalles de red al controlador.
- **Recursos REST**: exponen la API y orquestan servicios, devolviendo códigos y cabeceras apropiados.
- **Servicios de aplicación**: aplican reglas de negocio y transforman DTO ↔ dominio.
- **Repositorios**: encapsulan la persistencia JPA/SQL y ocultan la estructura de tablas.
- **Infraestructura**: maneja pools de conexión y adaptadores externos (PDF, Twilio, etc.).

Esta cadena clara permite probar cada elemento de forma aislada y mantener el código organizado. El flujo completo garantiza que los datos se validen en cada capa y que la base de datos se mantenga consistente.
