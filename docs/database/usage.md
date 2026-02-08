# Enlaces de uso de la base de datos

Esta tabla enumera cada función, procedimiento, vista y disparador de los scripts de `db` y los archivos Java o SQL que los invocan. En total se definen 2 tipos de tabla, 22 funciones, 31 procedimientos, 10 vistas y 14 disparadores.

| Objeto | Definido en | Usado por |
|-------|-------------|---------|
| tvp_DetalleTx | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarVenta`, `sp_RegistrarPedido`, `sp_ModificarPedido`; clase `SqlServerTvpBuilder` |
| tvp_PagoTx | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarVenta` y `sp_AgregarPagosTransaccion`; clase `SqlServerTvpBuilder` |
| fn_NombreCompleto | script `TVF_y_SF.sql` | vistas `vw_ClientesFrecuentes` y `vw_HistorialTransaccionesPorCliente`; consultas `Cliente.activosByNombre` y `Cliente.activosByTelefono` en `ClienteEntity` |
| fn_GetParametroDecimal | script `TVF_y_SF.sql` | Funciones `fn_CargoRepartoActual`, `fn_DescuentoValeGas`, `fn_MinCantidadMayoristaHilo`, `fn_MaxIntentosFallidos`, `fn_MinutosBloqueoCuenta`; clase `ParametroSistemaEntity` |
| fn_CargoRepartoActual | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarPedido`, `sp_ModificarPedido` |
| fn_DescuentoValeGas | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarPedido`, `sp_ModificarPedido` |
| fn_actor_id | script `TVF_y_SF.sql` | Disparador `trg_Persona_ValidarEstado`; procedimiento `sp_ActualizarEstadoPedido` |
| fn_actor_nivel | script `TVF_y_SF.sql` | Procedimientos `sp_AssertAdmin` y `sp_RegistrarEmpleado` |
| fn_Capitalizar | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarCliente`, `sp_RegistrarEmpleado` |
| fn_NormalizarEspacios | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarCliente`, `sp_RegistrarEmpleado` |
| fn_EsTelefonoValido | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarCliente`, `sp_RegistrarEmpleado` |
| fn_NormalizarTelefono | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarCliente`, `sp_RegistrarEmpleado` |
| fn_EsDniValido | script `DDL.sql` | Procedimiento `sp_ValidarPersonaBasica` |
| fn_estado | script `DDL.sql` | Procedimientos `sp_InsertCategoria`, `sp_CambiarEstadoCategoria`, `sp_RegistrarVenta`, `sp_RegistrarPedido`, `sp_CancelarVenta`, `sp_ActualizarEstadoPedido`, `sp_ValidarPersonaBasica`, `sp_RecalcularStockProductos` |
| fn_TotalPagosTransaccion | script `TVF_y_SF.sql` | Disparador `trg_PagoTransaccion_CheckSum`; procedimiento `sp_RecalcularStockProductos` |
| fn_StockDisponible | script `TVF_y_SF.sql` | Disparador `trg_Producto_ValidateAndAdjust` |
| fn_StockDisponibleTalla | script `TVF_y_SF.sql` | Disparador `trg_TallaStock_ValidateAndUpdate` |
| fn_EsTipoProducto | script `TVF_y_SF.sql` | Triggers `trg_Producto_ValidateAndAdjust`, `trg_TallaStock_ValidateAndUpdate`, `trg_Presentacion_Validate`, `trg_DetalleTransaccion_Maintenance`, `trg_MovInv_ValidateAndUpdate` |
| fn_ClasificarPedido | script `TVF_y_SF.sql` | Procedimientos `sp_RegistrarPedido`, `sp_ModificarPedido` |
| fn_MinCantidadMayoristaHilo | script `TVF_y_SF.sql` | Disparador `trg_MovInv_ValidateAndUpdate` |
| fn_MaxIntentosFallidos | script `TVF_y_SF.sql` | Disparador `trg_Empleado_LoginHandling` |
| fn_MinutosBloqueoCuenta | script `TVF_y_SF.sql` | Disparador `trg_Empleado_LoginHandling` |
| fn_AssertEstadoModulo | script `TVF_y_SF.sql` | Disparadores `trg_Transaccion_Update`, `trg_Persona_ValidarEstado`, `trg_Producto_ValidateAndAdjust`, `trg_TallaStock_ValidateAndUpdate`, `trg_Presentacion_Validate` |
| fn_TieneStockNegativo | script `TVF_y_SF.sql` | Disparador `trg_Transaccion_Update` |
| sp_ValidarEstado | script `SP.sql` | Disparadores `trg_Transaccion_Update`, `trg_Persona_ValidarEstado`, `trg_Producto_ValidateAndAdjust`, `trg_TallaStock_ValidateAndUpdate`, `trg_Presentacion_Validate` |
| vw_ClientesFrecuentes | script `VW.sql` | Procedimiento `sp_ListarClientesFrecuentes`; clase `ClienteFrecuenteEntity` |
| vw_HistorialTransaccionesPorCliente | script `VW.sql` | clase `HistorialTransaccionEntity` |
| vw_ProductosMasVendidos | script `VW.sql` | clase `ProductoMasVendidoEntity` |
| vw_TransaccionesPorDia | script `VW.sql` | Procedimientos `sp_GenerarReporteDiario`, `sp_GenerarReporteRotacion`; clase `TransaccionesDiaEntity` |
| vw_PagoMetodoDia | script `VW.sql` | clase `PagoMetodoDiaEntity` |
| vw_ReporteMensualCategoria | script `VW.sql` | Procedimiento `sp_GenerarReporteMensual`; clase `ResumenCategoriaEntity` |
| vw_ResumenMensualModalidad | script `VW.sql` | Procedimiento `sp_GenerarReporteMensual`; clase `ResumenModalidadEntity` |
| vw_RotacionMensual | script `VW.sql` | clase `RotacionProductoEntity` |
| vw_RotacionRango | script `VW.sql` | clase `RotacionProductoEntity` |
| vw_ClientesActivos | script `VW.sql` | clase `ClienteActivoEntity` |
| sp_ListarClientesFrecuentes | `ClienteFrecuenteEntity` | `ClienteFrecuenteRepositoryImpl` |
| sp_DisableSeedTriggers | script `SP.sql` | Scripts `CatalogInserts.sql`, `ExamplePeople.sql`, `ProductBatches.sql`, `InitialInventory.sql` |
| sp_EnableSeedTriggers | script `SP.sql` | Scripts `CatalogInserts.sql`, `ExamplePeople.sql`, `ProductBatches.sql`, `InitialInventory.sql` |
| sp_SetSessionFlags | script `SP.sql` | Procedimientos `sp_RegistrarVenta`, `sp_RegistrarPedido`, `sp_ModificarPedido` |
| sp_PrepararTransaccion | script `SP.sql` | Procedimientos `sp_RegistrarVenta`, `sp_RegistrarPedido`, `sp_ModificarPedido` |
| sp_ListarAlertasPendientes | `AlertaStockEntity` | `AlertaStockRepositoryImpl.findPendientes` |
| sp_ListarPedidosPendientes | `PedidoEntity` | `PedidoRepositoryImpl` |
| sp_AssertEmpleadoContext | script `SP.sql` | Procedimientos `sp_DescontarStock_Detalle`, `sp_InsertCategoria`, `sp_UpdateCategoria`, `sp_DeleteCategoria`, `sp_CambiarEstadoCategoria`, `sp_RegistrarVenta`, `sp_RegistrarPedido`, `sp_ModificarPedido`, `sp_CancelarVenta`, `sp_ActualizarEstadoPedido`, `sp_GenerarReporteDiario`, `sp_GenerarReporteMensual`, `sp_GenerarReporteRotacion`, `sp_RegistrarCliente`, `sp_RegistrarEmpleado`, `sp_AgregarPagosTransaccion`, `sp_AssertAdmin` |
| sp_AssertAdmin | script `SP.sql` | Procedimientos `sp_CheckAdminTrigger` y `sp_DeleteCategoria` |
| sp_CheckAdminTrigger | script `SP.sql` | Triggers `trg_Rol_AdminOnly` y `trg_ParametroSistema_AdminOnly` |
| sp_AplicarAjusteInventario | script `SP.sql` | Procedimientos `sp_ModificarPedido`, `sp_CancelarVenta` |
| sp_DescontarStock_Detalle | script `SP.sql` | Procedimientos `sp_RegistrarVenta`, `sp_RegistrarPedido`, `sp_ModificarPedido`, `sp_CancelarVenta` |
| sp_InsertCategoria | `CategoriaEntity` | `CategoriaRepositoryImpl` |
| sp_UpdateCategoria | `CategoriaEntity` | `CategoriaRepositoryImpl` |
| sp_DeleteCategoria | `CategoriaEntity` | `CategoriaRepositoryImpl` |
| sp_CambiarEstadoCategoria | `CategoriaEntity` | `CategoriaRepositoryImpl` |
| sp_RegistrarVenta | `VentaEntity` | `VentaRepositoryImpl` |
| sp_RegistrarPedido | `PedidoEntity` | `PedidoRepositoryImpl` |
| sp_ModificarPedido | `PedidoEntity` | `PedidoRepositoryImpl` |
| sp_CancelarVenta | `VentaEntity` | `TransaccionRepositoryImpl` |
| sp_ActualizarEstadoPedido | `PedidoEntity` | `TransaccionRepositoryImpl` |
| sp_GenerarReporteDiario | `ReporteEntity` | `ReporteGeneratorImpl` |
| sp_GenerarReporteMensual | `ReporteEntity` | `ReporteGeneratorImpl` |
| sp_GenerarReporteRotacion | `ReporteEntity` | `ReporteGeneratorImpl` |
| sp_ValidarPersonaBasica | script `SP.sql` | Procedimientos `sp_RegistrarCliente` y `sp_RegistrarEmpleado` |
| sp_RegistrarCliente | `ClienteEntity` | `ClienteRepositoryImpl` |
| sp_RegistrarEmpleado | `EmpleadoEntity` | `EmpleadoRepositoryImpl` |
| sp_AgregarPagosTransaccion | `PedidoEntity` | `PedidoRepositoryImpl` |
| sp_RecalcularStockProductos | `ProductoEntity` | script `InitialInventory.sql` |
| sp_DepurarBitacoraLogin | `BitacoraLoginEntity` | - |
| trg_Transaccion_Update | script `Triggers.sql` | se ejecuta en actualizaciones de la tabla `Transaccion` |
| trg_Persona_ValidarEstado | script `Triggers.sql` | se ejecuta al insertar o actualizar `Persona` |
| trg_Producto_ValidateAndAdjust | script `Triggers.sql` | se ejecuta al insertar o actualizar `Producto` |
| trg_TallaStock_ValidateAndUpdate | script `Triggers.sql` | se ejecuta al insertar, actualizar o eliminar `TallaStock` |
| trg_Presentacion_Validate | script `Triggers.sql` | se ejecuta al insertar o actualizar `Presentacion` |
| trg_Venta_Insert | script `Triggers.sql` | se ejecuta después de insertar en `Venta` |
| trg_Pedido_Validate | script `Triggers.sql` | se ejecuta después de insertar o actualizar `Pedido` |
| trg_PagoTransaccion_CheckSum | script `Triggers.sql` | se ejecuta al actualizar `PagoTransaccion` |
| trg_Comprobante_Insert | script `Triggers.sql` | se ejecuta después de insertar en `Comprobante` |
| trg_DetalleTransaccion_Maintenance | script `Triggers.sql` | se ejecuta al modificar `DetalleTransaccion` |
| trg_MovInv_ValidateAndUpdate | script `Triggers.sql` | se ejecuta al insertar o actualizar `MovimientoInventario`; actualiza el stock en nuevos registros |
| trg_Empleado_LoginHandling | script `Triggers.sql` | se ejecuta después de actualizar `Empleado` |
| trg_Rol_AdminOnly | script `Triggers.sql` | se ejecuta al modificar `Rol` |
| trg_ParametroSistema_AdminOnly | script `Triggers.sql` | se ejecuta al modificar `ParametroSistema` |

