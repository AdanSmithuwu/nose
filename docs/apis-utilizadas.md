# APIs utilizadas en el sistema

Esta guía resume las bibliotecas y servicios externos empleados en Comercial's Valerio.
Se indican los módulos que las consumen y el propósito de cada una.

| API o biblioteca | Módulos | Uso principal |
|------------------|---------|---------------|
| **RESTEasy** (JAX-RS) | application, presentation-ui | Exponer y consumir el API REST que orquesta los casos de uso. |
| **EclipseLink JPA** | infrastructure | Implementar repositorios y consultas ORM. |
| **HikariCP** | infrastructure | Administrar el pool de conexiones JDBC. |
| **Google Drive API** | infrastructure | Respaldar y compartir documentos PDF en la nube. |
| **Twilio API** | infrastructure | Enviar comprobantes por WhatsApp mediante plantillas. |
| **OpenPDF** | infrastructure | Generar reportes y comprobantes en formato PDF. |
| **JFreeChart** | infrastructure | Graficar indicadores dentro de los PDFs. |
| **Argon2-jvm** | infrastructure | Hash de contraseñas para los empleados. |
| **MapStruct** | application, infrastructure | Convertir entidades de dominio a DTO y viceversa. |
| **SQL Server JDBC** | infrastructure | Conectar con la base de datos `cv_ventas_distribucion`. |
| **jakarta.* APIs** | todos | Soporte de CDI, validación y servicios web. |

Estas APIs permiten integrar funcionalidades avanzadas sin reescribir soluciones comunes, 
al mismo tiempo que facilitan el mantenimiento modular del proyecto.
