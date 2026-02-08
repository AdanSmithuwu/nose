# Detalle del script Security.sql

Este documento explica cada sentencia de `db/Security.sql`, que configura los inicios de sesión, roles y permisos de la base `cv_ventas_distribucion`.

## 1. Inicio y validación

```sql
USE master;
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
```
Estas sentencias cambian al contexto de `master`, evitan mensajes de conteo de filas y fuerzan el rollback automático ante cualquier error. Luego se inicia una transacción para ejecutar todo de forma atómica.

```sql
IF DB_ID('cv_ventas_distribucion') IS NULL
    THROW 50000, 'Database cv_ventas_distribucion not found.', 1;
```
Se verifica la existencia de la base de datos objetivo y se lanza una excepción si no está disponible.

## 2. Creación de inicios de sesión

```sql
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = 'cv_admin_user')
    CREATE LOGIN cv_admin_user
        WITH PASSWORD = 'H3ll0A$tr0ngP@ssw0rd1!',
             DEFAULT_DATABASE = [cv_ventas_distribucion];

IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = 'cv_employee_user')
    CREATE LOGIN cv_employee_user
        WITH PASSWORD = 'Str0ngEmp!oyeeP@ssw0rd#2',
             DEFAULT_DATABASE = [cv_ventas_distribucion];
```
Se definen dos inicios de sesión en SQL Server solo si no existían previamente. Cada uno tiene una contraseña fuerte y apunta por defecto a la base de ventas.

## 3. Definición de roles y usuarios

```sql
USE cv_ventas_distribucion;

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'Admin')
    CREATE ROLE Admin;

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'Employee')
    CREATE ROLE Employee;
```
Dentro de la base se crean los roles `Admin` y `Employee` si aún no están presentes.

```sql
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'cv_admin_user')
    CREATE USER cv_admin_user FOR LOGIN cv_admin_user;

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'cv_employee_user')
    CREATE USER cv_employee_user FOR LOGIN cv_employee_user;
```
Estos comandos asocian los inicios de sesión con usuarios locales de la base.

## 4. Asociación de roles

```sql
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_role_members rm
        JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
        JOIN sys.database_principals m ON rm.member_principal_id = m.principal_id
    WHERE r.name = 'Admin'
      AND m.name = 'cv_admin_user'
)
    ALTER ROLE Admin ADD MEMBER cv_admin_user;

IF NOT EXISTS (
    SELECT 1
    FROM sys.database_role_members rm
        JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
        JOIN sys.database_principals m ON rm.member_principal_id = m.principal_id
    WHERE r.name = 'Employee'
      AND m.name = 'cv_employee_user'
)
    ALTER ROLE Employee ADD MEMBER cv_employee_user;
```
Se comprueba si cada usuario ya forma parte de su rol correspondiente y, de no ser así, se agrega con `ALTER ROLE`.

## 5. Asignación de permisos generales

```sql
GRANT CONTROL ON SCHEMA::dbo TO Admin;
```
Otorga control total sobre el esquema `dbo` al rol `Admin` para que administre todos los objetos.

```sql
GRANT SELECT, EXECUTE ON SCHEMA::dbo TO Employee;
REVOKE UPDATE ON SCHEMA::dbo TO Employee;
```
El rol `Employee` recibe permisos de lectura y ejecución globales, pero se revoca la capacidad de actualización hasta otorgarla tabla por tabla.

```sql
GRANT UPDATE ON dbo.Empleado (bloqueadoHasta, intentosFallidos, ultimoAcceso) TO Employee;
```
Permite a los empleados modificar únicamente las columnas de control de acceso en `Empleado`.

## 6. Permisos sobre tablas operativas

Se conceden permisos de inserción, actualización y eliminación en las tablas de operaciones habituales:

```sql
GRANT INSERT, UPDATE, DELETE ON dbo.Persona             TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.Cliente             TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.Producto            TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.TallaStock          TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.Presentacion        TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.Transaccion         TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.DetalleTransaccion  TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.PagoTransaccion     TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.Pedido              TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.Venta               TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.MovimientoInventario TO Employee;
GRANT INSERT, UPDATE, DELETE ON dbo.Comprobante         TO Employee;
```
Cada sentencia `GRANT` habilita las modificaciones en la tabla indicada para los miembros del rol `Employee`.

## 7. Finalización

```sql
COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH
GO
```
La transacción se confirma al final. Si ocurre un error dentro del bloque `TRY`, se revierte y se vuelve a lanzar la excepción. El lote termina con `GO`.

## 8. Uso desde la aplicación

Los inicios de sesión creados por este script se emplean en el módulo de
infraestructura para abrir las conexiones JDBC. Sus credenciales se definen en
`infrastructure/src/main/resources/application.properties`:

```properties
db.employee.user=cv_employee_user
db.employee.password=Str0ngEmp!oyeeP@ssw0rd#2
db.admin.user=cv_admin_user
db.admin.password=H3ll0A$tr0ngP@ssw0rd1!
```

Luego `DataSourceProvider` lee dichas propiedades para crear dos pools de
conexiones, uno por rol:

```java
// infrastructure/src/main/java/com/comercialvalerio/infrastructure/persistence/DataSourceProvider.java
@PostConstruct
public void init() {
    HikariDataSource emp = createDs("db.employee.user", "db.employee.password");
    HikariDataSource adm = createDs("db.admin.user", "db.admin.password");
    employeeDs = emp;
    adminDs = adm;
}
```

De esta manera la aplicación se autentica usando los usuarios
`cv_employee_user` y `cv_admin_user` creados por `Security.sql`.

## 9. Flujo de inicio de sesión

Cuando un cliente envía una petición `POST /empleados/login` el filtro
`SecurityFilter` procesa las cabeceras `X-Rol` y `X-IdEmpleado`. En esta ruta el
identificador puede omitirse, pero el rol determina con qué usuario de base de
datos se ejecutará la autenticación. El filtro guarda los valores en
`RequestContext` y luego el `PersistenceManager` crea el `EntityManager`
mediante `DataSourceProvider.forRole(RequestContext.rol())`.

Si el rol comienza con `Admin` se emplea el pool `adminDs`, que abre conexiones
con `cv_admin_user`. En caso contrario se utiliza `employeeDs` con
`cv_employee_user`. De esta forma el flujo de login se ejecuta con los permisos
adecuados para cada rol. Los triggers como `trg_Empleado_LoginHandling` se
ejecutan `WITH EXECUTE AS OWNER` para registrar los intentos sin importar qué
usuario esté activo.

