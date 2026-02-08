# Resumen de artefactos de la base de datos

- Los scripts SQL bajo `db/` inicializan la base `cv_ventas_distribucion`. Los siguientes conteos se obtuvieron usando `grep` sobre los scripts.

- **Funciones escalares:** 22 definiciones (20 en `TVF_y_SF.sql` y 2 en `DDL.sql`).
- **Tipos de tabla:** 2 definiciones en `TVF_y_SF.sql`.
- **Vistas:** 10 definiciones en `VW.sql`.
- **Procedimientos almacenados:** 31 definiciones en `SP.sql`.
- **Disparadores:** 14 definiciones en `Triggers.sql`.
- **Índices:** 34 definiciones `CREATE` contenidas en `db/DDL.sql`.

Cinco procedimientos almacenados utilizan cursores: `sp_ListarClientesFrecuentes`, `sp_ListarAlertasPendientes`, `sp_ListarPedidosPendientes`, `sp_RecalcularStockProductos`, `sp_DepurarBitacoraLogin`.

`InstallAll.sql` orquesta la instalación de la base utilizando inclusiones `sqlcmd` en este orden:

1. `DDL.sql`
2. `TVF_y_SF.sql`
3. `SP.sql`
4. `Triggers.sql`
5. `VW.sql`
6. `Security.sql`
7. `CatalogInserts.sql`
8. `ExamplePeople.sql`
9. `ProductBatches.sql`
10. `InitialInventory.sql`
11. `MaintenancePlan.sql` crea un trabajo diario de respaldo a las 21:00 y depura archivos antiguos.

`InstallAll.sql` incluye todos estos archivos, por lo que la tarea de mantenimiento se crea automáticamente. Comenta su última línea si prefieres omitirla.

Los procedimientos de mantenimiento para actualizar tablas maestras (por ejemplo `sp_InsertCategoria`, `sp_UpdateCategoria`) se encuentran en `SP.sql` bajo la sección *Mantenimiento de tablas maestras*.
