# Controladores de la UI

Este documento enumera las clases de control presentes en el módulo `presentation-ui`.
Se incluyen todas aquellas cuyo nombre termina en `Controller`.

| Nombre | Descripción |
| ------ | ----------- |
| AbstractController | Clase base con utilidades para tareas en segundo plano y manejo de errores. |
| AlertaStockController | Controlador de `DlgAlertasStock`; obtiene y marca las alertas mediante `sp_ListarAlertasPendientes`. |
| LoginController | Maneja la autenticación del usuario en `LoginForm` y notifica errores de acceso. |
| BitacoraLoginController | Controlador para `FormBitacoraLogin`; filtra la bitácora de accesos por empleado y fecha. |
| CategoriaController | Administra categorías con `FormCategorias`; usa `DlgCategoriaNueva` y `DlgCategoriaEditar`. |
| CategoriaNuevaController | Controla el diálogo `DlgCategoriaNueva` para registrar nuevas categorías. |
| ClienteController | Gestiona `FormClientes`; abre `DlgClienteEditar` y `DlgHistorialCliente` para editar y revisar. |
| ClienteNuevoController | Procesa el registro de clientes en `DlgClienteNuevo` validando los datos ingresados. |
| MotivoCancelacionController | Obtiene y valida el motivo de cancelación en `DlgMotivoCancelacion`. |
| ObservacionController | Guarda el texto ingresado en `DlgObservacion` y cierra el diálogo. |
| DashboardController | Actualiza las métricas del `FormDashboard` comparándolas con las metas de ventas y pedidos. |
| EmpleadoController | Controla `FormEmpleados`; usa `DlgEmpleadoEditar` y `DlgEmpleadoCredenciales`. |
| EmpleadoNuevoController | Registra empleados con `DlgEmpleadoNuevo` y muestra `DlgCredencialesGeneradas`. |
| HistorialClienteController | Muestra el historial de un cliente en `FormHistorialCliente` y permite exportarlo. |
| HistorialInventarioController | Maneja los filtros y la carga de movimientos en `FormHistorialInventario`. |
| MainController | Gestiona acciones del menú principal en `MainForm`, abre paneles y muestra `DlgAlertasStock`. |
| MantenimientoController | Ejecuta tareas de mantenimiento desde `FormMantenimiento` y reporta el resultado. |
| ParametroSistemaController | Controla `FormParametros`; lista, guarda y edita cada valor en `DlgParametroEditar`. |
| PedidoController | Base para `FormPedido`; crea pedidos y abre `DlgClienteNuevo` o `DlgObservacion`. |
| PedidoDomicilioController | Extiende `PedidoController` para pedidos a domicilio mostrados en `FormPedidoDomicilio`. Valida que la cantidad no supere el stock disponible. |
| PedidoEditarController | Permite modificar un pedido existente a través de `DlgPedidoEditar`. |
| PedidoEspecialController | Variante de `PedidoController` usada por `FormPedidoEspecial` para pedidos sin stock. |
| SeguimientoPedidosController | Controla `FormSeguimientoPedidos`; abre diálogos de detalle, pago y comprobante. |
| GestionInventarioController | Administra inventario en `FormGestionInventario` y usa `DlgObservacion` para notas. |
| ProductoController | Maneja productos en `FormGestionProductos` con `DlgProducto{Nuevo,Editar,Detalle}`. |
| ReporteDiarioController | Genera el reporte diario en `FormReporteDiario` y permite imprimirlo o guardarlo. |
| ReporteMensualController | Genera el reporte mensual en `FormReporteMensual` con opción de exportar a PDF. |
| ReporteRotacionController | Produce el reporte de rotación de inventario en `FormReporteRotacion`. |
| SeguimientoVentasController | Gestiona `FormSeguimientoVentas`; filtra ventas, imprime y abre `DlgVentaDetalle`. |
| VentaController | Gestiona `FormVenta`, carga productos y pagos; genera comprobantes en `DlgComprobante`. |
