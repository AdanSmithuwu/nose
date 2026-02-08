# Índices de DDL.sql

Esta tabla resume cada índice definido en `db/DDL.sql` y su finalidad.

| Índice | Descripción |
|--------|-------------|
| IX_AlertaStock_Pendiente | Evita duplicar alertas no procesadas para un mismo producto. |
| IX_AlertaStock_Procesada | Acelera la consulta de alertas por estado y fecha. |
| IX_Bitacora_Empleado | Filtra rápidamente eventos de inicio de sesión por empleado. |
| IX_Bitacora_ExitosoFecha | Mejora la búsqueda de eventos según éxito y fecha. |
| IX_Bitacora_Fecha | Ordena la bitácora por fecha de evento. |
| IX_DetTrans_Producto | Ubica detalles de transacción por producto. |
| IX_DetTrans_Talla | Optimiza las consultas por talla en detalles de transacción. |
| IX_DetTrans_TransProdTalla | Impide duplicar el mismo producto y talla en una transacción. |
| IX_Empleado_Rol | Permite listar empleados por rol. |
| IX_Estado_ModuloNombre | Agiliza la búsqueda de estados por módulo y nombre. |
| IX_MovInv_Empleado | Accede a movimientos de inventario por empleado. |
| IX_MovInv_Fecha | Facilita ordenar movimientos de inventario por fecha. |
| IX_MovInv_Prod | Consulta movimientos por producto. |
| IX_MovInv_Prod_Fecha | Filtra movimientos por producto y fecha. |
| IX_MovInv_Tipo | Accede a movimientos por tipo de operación. |
| IX_OrdenCompra_Cliente | Ubica órdenes de compra por cliente. |
| IX_OrdenCompra_FechaCumplida | Busca órdenes según su fecha de cumplimiento. |
| IX_OrdenCompra_Pedido | Relaciona órdenes con sus pedidos. |
| IX_OrdenCompra_Producto | Consulta órdenes por producto. |
| IX_OrdenCompraPdf_Fecha | Ordena PDFs de órdenes por fecha de generación. |
| IX_PagoTransaccion_Metodo | Obtiene pagos por método utilizado. |
| IX_Pedido_EmpleadoEntrega | Permite revisar pedidos asignados a un empleado de reparto. |
| IX_Pedido_Tipo | Acelera la búsqueda de pedidos por tipo. |
| IX_Persona_Estado | Filtra personas según su estado. |
| IX_Presentacion_Producto | Busca presentaciones por producto. |
| IX_Producto_Categoria | Ubica productos por categoría. |
| IX_Producto_Estado | Obtiene productos según su estado. |
| IX_Producto_Mayorista | Consulta ofertas mayoristas incluyendo mínimos y precios. |
| IX_Producto_StockUmbral | Detecta productos cuyo stock se acerca al umbral. |
| IX_Reporte_Empleado | Accede a reportes generados por empleado. |
| IX_Reporte_FechaGen | Ordena reportes por fecha de generación. |
| IX_TallaStock_Producto | Lista tallas disponibles por producto. |
| IX_Trans_ClienteFecha | Busca transacciones por cliente y fecha con totales. |
| IX_Trans_Fecha | Acelera listados de transacciones por fecha. |

