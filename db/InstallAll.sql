-- Script maestro de instalación para la base de datos de Comercial's Valerio
-- Ejecuta todos los scripts en el orden correcto usando sqlcmd :r include.
-- Incluye el trigger trg_Pedido_Validate

:ON ERROR EXIT
:r DDL.sql
PRINT 'DDL.sql ejecutado correctamente';
GO
:r TVF_y_SF.sql
PRINT 'TVF_y_SF.sql ejecutado correctamente';
GO
:r SP.sql
PRINT 'SP.sql ejecutado correctamente';
GO
:r Triggers.sql
PRINT 'Triggers.sql ejecutado correctamente';
GO
:r VW.sql
PRINT 'VW.sql ejecutado correctamente';
GO
:r Security.sql
PRINT 'Security.sql ejecutado correctamente';
GO
:r CatalogInserts.sql
PRINT 'CatalogInserts.sql ejecutado correctamente';
GO
:r ExamplePeople.sql
PRINT 'ExamplePeople.sql ejecutado correctamente';
GO
:r ProductBatches.sql
PRINT 'ProductBatches.sql ejecutado correctamente';
GO
:r InitialInventory.sql
PRINT 'InitialInventory.sql ejecutado correctamente';
GO
:r MaintenancePlan.sql
PRINT 'MaintenancePlan.sql ejecutado correctamente';
GO
