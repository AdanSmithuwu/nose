# Entidades del módulo domain

La siguiente lista resume las clases de modelo principales definidas en el módulo `domain`. Se excluyen enumeraciones y utilidades, enumerando solo las entidades que representan información de negocio.

| Nombre | Descripción |
|-------|-------------|
| **AlertaStock** | Alerta generada cuando el stock de un producto cae por debajo de su umbral. Registra el producto afectado, cantidades y fecha de alerta. |
| **BitacoraLogin** | Registro de intentos de autenticación de empleados, indicando si fueron exitosos y la fecha del evento. |
| **BaseEntity** | Clase base genérica que define un identificador y comportamiento común para las demás entidades. |
| **Categoria** | Categoría comercial visible al cliente (por ejemplo, "Bebidas" o "Textiles"). Incluye nombre único, descripción y estado. |
| **Cliente** | Persona externa registrada como cliente. Hereda campos y validaciones de `Persona` y añade la dirección. |
| **Comprobante** | Comprobante electrónico en PDF asociado a una transacción (boleta o factura) con fecha de emisión y archivo PDF. |
| **DetalleTransaccion** | Línea de detalle de una venta o pedido: cantidad, producto, precio unitario y subtotal. |
| **Empleado** | Usuario interno que extiende `Persona`. Maneja credenciales encriptadas, rol y datos de último acceso y bloqueo. |
| **Estado** | Catálogo de estados (Activo, Inactivo, Entregado...) agrupados por módulo para controlar distintos flujos. |
| **MetodoPago** | Forma de pago aceptada (efectivo, Yape, tarjeta, etc.) identificada por nombre único. |
| **MovimientoInventario** | Registro tipo kardex que refleja entradas, salidas y ajustes de stock incluyendo fecha, cantidad y motivo. |
| **OrdenCompra** | Registro de la compra de productos solicitados en un pedido, indicando producto, cantidad, cliente y fecha cumplida. |
| **OrdenCompraPdf** | Versión PDF de una orden de compra con referencia al pedido y fecha de generación. |
| **PagoTransaccion** | Pago aplicado a una transacción con el método utilizado y monto abonado. |
| **ParametroSistema** | Par clave-valor editable en tiempo de ejecución para configurar aspectos del sistema. |
| **Pedido** | Transacción con entrega diferida que registra la dirección, el tipo de pedido (Domicilio o Especial), si usa vale de gas y los datos de entrega. |
| **Persona** | Entidad base para personas (cliente o empleado) que maneja nombres, DNI, teléfono, fecha de registro y estado. |
| **Presentacion** | Presentación de un producto que define la cantidad incluida y su precio. Puede haber varias por producto. |
| **Producto** | Producto de inventario y venta con nombre único, categoría, tipo, precios, stock y umbral de reposición. |
| **Reporte** | Historial de reportes PDF generados (Kardex, ventas diarias, etc.), con rango de fechas, filtros y usuario que lo generó. |
| **Rol** | Rol de seguridad que asigna permisos a los usuarios internos y define jerarquía mediante un nivel. |
| **TallaStock** | Control de stock por talla o variante de un producto, incluyendo su estado y cantidad disponible. |
| **TipoMovimiento** | Catálogo de tipos de movimiento de inventario (Entrada, Salida, Ajuste...). |
| **TipoProducto** | Tipo genérico de producto (Unidad fija, Vestimenta o Fraccionable) usado para clasificar artículos. |
| **Transaccion** | Representa una transacción comercial abstracta. Administra totales, estado, cliente, empleado y pagos. |
| **Venta** | Transacción de venta sin campos adicionales. Puede completarse o cancelarse según su estado. |

