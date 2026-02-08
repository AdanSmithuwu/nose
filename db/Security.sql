-- Configuración de seguridad de la base de datos de Comercial's Valerio
-- Crea inicios de sesión y roles SQL y asigna permisos.

USE master;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

IF DB_ID('cv_ventas_distribucion') IS NULL
    THROW 50000, 'Database cv_ventas_distribucion not found.', 1;

-- Inicios de sesión para los roles de la aplicación
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = 'cv_admin_user')
    CREATE LOGIN cv_admin_user
        WITH PASSWORD = 'H3ll0A$tr0ngP@ssw0rd1!',
             DEFAULT_DATABASE = [cv_ventas_distribucion];

IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = 'cv_employee_user')
    CREATE LOGIN cv_employee_user
        WITH PASSWORD = 'Str0ngEmp!oyeeP@ssw0rd#2',
             DEFAULT_DATABASE = [cv_ventas_distribucion];

USE cv_ventas_distribucion;

-- Roles de base de datos
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'Admin')
    CREATE ROLE Admin;

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'Employee')
    CREATE ROLE Employee;

-- Usuarios asociados a los inicios de sesión
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'cv_admin_user')
    CREATE USER cv_admin_user FOR LOGIN cv_admin_user;

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'cv_employee_user')
    CREATE USER cv_employee_user FOR LOGIN cv_employee_user;

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

-- Los administradores pueden gestionar todo en dbo
GRANT CONTROL ON SCHEMA::dbo TO Admin;

-- Los empleados tienen lectura y ejecución y pueden modificar las tablas operativas
GRANT SELECT, EXECUTE ON SCHEMA::dbo TO Employee;
-- Quitar cualquier denegación previa para que surtan efecto las concesiones específicas
REVOKE UPDATE ON SCHEMA::dbo TO Employee;
-- Permitir que los empleados actualicen columnas de acceso en Empleado
GRANT UPDATE ON dbo.Empleado (bloqueadoHasta, intentosFallidos, ultimoAcceso) TO Employee;

-- Permisos de modificación para operaciones habituales
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
COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH
GO
