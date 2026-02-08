-- Script DDL para la base de datos de Comercial's Valerio
-- Crea las tablas, funciones e índices.

-- Verifica si la base de datos ya existe para poder ejecutar el script varias veces
IF DB_ID('cv_ventas_distribucion') IS NULL
BEGIN
    CREATE DATABASE cv_ventas_distribucion COLLATE Latin1_General_CI_AI;
    PRINT N'Base de datos cv_ventas_distribucion creada';
END;
ELSE
    PRINT N'La base de datos cv_ventas_distribucion ya existía';
GO

USE cv_ventas_distribucion;
GO

-- 1. CATÁLOGOS
DROP TABLE IF EXISTS dbo.Estado;
CREATE TABLE dbo.Estado(
    idEstado INT IDENTITY PRIMARY KEY,
    nombre   NVARCHAR(20) NOT NULL,
    modulo   NVARCHAR(20) NOT NULL,
    CONSTRAINT UQ_Estado UNIQUE(nombre, modulo)
);

DROP TABLE IF EXISTS dbo.Rol;
CREATE TABLE dbo.Rol(
    idRol  INT IDENTITY PRIMARY KEY,
    nombre NVARCHAR(20) NOT NULL UNIQUE,
    nivel  TINYINT      NOT NULL UNIQUE
);

DROP TABLE IF EXISTS dbo.TipoProducto;
CREATE TABLE dbo.TipoProducto(
    idTipoProducto INT IDENTITY PRIMARY KEY,
    nombre         NVARCHAR(20) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS dbo.Categoria;
CREATE TABLE dbo.Categoria(
    idCategoria INT IDENTITY PRIMARY KEY,
    nombre      NVARCHAR(40) NOT NULL UNIQUE,
    descripcion NVARCHAR(120) NULL,
    idEstado    INT NOT NULL,
    CONSTRAINT FK_Categoria_Estado FOREIGN KEY(idEstado) REFERENCES dbo.Estado(idEstado)
);

DROP TABLE IF EXISTS dbo.TipoMovimiento;
CREATE TABLE dbo.TipoMovimiento(
    idTipoMovimiento INT IDENTITY PRIMARY KEY,
    nombre           NVARCHAR(20) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS dbo.MetodoPago;
CREATE TABLE dbo.MetodoPago(
    idMetodoPago INT IDENTITY PRIMARY KEY,
    nombre       NVARCHAR(20) NOT NULL UNIQUE
);
GO

-- 2. PERSONAS / CLIENTES / EMPLEADOS
CREATE OR ALTER FUNCTION dbo.fn_EsDniValido(@dni CHAR(8))
RETURNS BIT
WITH SCHEMABINDING
AS
BEGIN
    RETURN CASE
               WHEN @dni IS NULL OR @dni LIKE '%[^0-9]%' OR LEN(@dni) <> 8
               THEN 0 ELSE 1
           END;
END;
GO

DROP TABLE IF EXISTS dbo.Persona;
CREATE TABLE dbo.Persona(
    idPersona      INT IDENTITY PRIMARY KEY,
    nombres        NVARCHAR(60) NOT NULL,
    apellidos      NVARCHAR(60) NOT NULL,
    dni            CHAR(8)     NOT NULL UNIQUE,
    CONSTRAINT CK_Persona_Dni CHECK (dbo.fn_EsDniValido(dni) = 1),
    telefono       NVARCHAR(15),
    CONSTRAINT CK_Persona_Telefono
        CHECK (
            telefono IS NULL OR
            (telefono NOT LIKE '%[^0-9]%' AND LEN(telefono) BETWEEN 6 AND 15)
        ),
    fechaRegistro  DATE NOT NULL DEFAULT CAST(SYSDATETIME() AS DATE),
    idEstado       INT         NOT NULL,
    CONSTRAINT FK_Persona_Estado FOREIGN KEY (idEstado) REFERENCES dbo.Estado(idEstado)
);

DROP TABLE IF EXISTS dbo.Cliente;
CREATE TABLE dbo.Cliente(
    idPersona INT PRIMARY KEY,
    direccion NVARCHAR(120) NOT NULL,
    CONSTRAINT FK_Cliente_Persona FOREIGN KEY (idPersona) REFERENCES dbo.Persona(idPersona) ON DELETE CASCADE
);

DROP TABLE IF EXISTS dbo.Empleado;
CREATE TABLE dbo.Empleado(
    idPersona        INT PRIMARY KEY,
    usuario          NVARCHAR(30) NOT NULL UNIQUE,
    hashClave        NVARCHAR(120) NOT NULL,
    fechaCambioClave DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    idRol            INT          NOT NULL,
    ultimoAcceso     DATETIME2     NULL,
    -- Contador de intentos fallidos de inicio de sesión
    intentosFallidos INT          NOT NULL DEFAULT 0 CHECK (intentosFallidos >= 0),
    bloqueadoHasta   DATETIME2     NULL,
    CONSTRAINT FK_Empleado_Persona FOREIGN KEY (idPersona) REFERENCES dbo.Persona(idPersona) ON DELETE CASCADE,
    CONSTRAINT FK_Empleado_Rol     FOREIGN KEY (idRol)     REFERENCES dbo.Rol(idRol)
);
GO

-- 3. PRODUCTOS, TALLAS, PRESENTACIONES
DROP TABLE IF EXISTS dbo.Producto;
CREATE TABLE dbo.Producto(
    idProducto          INT IDENTITY PRIMARY KEY,
    nombre              NVARCHAR(90)   NOT NULL UNIQUE,
    descripcion         NVARCHAR(120),
    idCategoria         INT           NOT NULL,
    idTipoProducto      INT           NOT NULL,
    unidadMedida        NVARCHAR(10)   NOT NULL,
    precioUnitario      DECIMAL(10,2) NULL CHECK(precioUnitario >= 0),
    mayorista           BIT           NOT NULL DEFAULT 0,
    minMayorista        INT           NULL CHECK(minMayorista > 0),
    precioMayorista     DECIMAL(10,2) NULL CHECK(precioMayorista >= 0),
    paraPedido          BIT           NOT NULL DEFAULT 0,
    ignorarUmbralHastaCero BIT       NOT NULL DEFAULT 0,
    tipoPedidoDefault   NVARCHAR(20)   NULL CHECK(tipoPedidoDefault IS NULL OR tipoPedidoDefault IN ('Domicilio','Especial')),
    stockActual         DECIMAL(12,3) NULL CHECK(stockActual >= 0),
    umbral              DECIMAL(12,3) NOT NULL DEFAULT 0 CHECK(umbral >= 0),
    idEstado            INT           NOT NULL,
    CONSTRAINT FK_Producto_Tipo      FOREIGN KEY(idTipoProducto) REFERENCES dbo.TipoProducto(idTipoProducto),
    CONSTRAINT FK_Producto_Estado    FOREIGN KEY(idEstado)       REFERENCES dbo.Estado(idEstado),
    CONSTRAINT FK_Producto_Categoria FOREIGN KEY(idCategoria)    REFERENCES dbo.Categoria(idCategoria),
    CONSTRAINT CK_Producto_MayoristaParams CHECK((mayorista = 1 AND minMayorista IS NOT NULL AND precioMayorista IS NOT NULL)
       OR (mayorista = 0 AND minMayorista IS NULL  AND precioMayorista IS NULL)),
    CONSTRAINT CK_PrecioMayoristaMenor CHECK(precioMayorista IS NULL OR precioMayorista < precioUnitario)
);
GO

-- Función para obtener idEstado
CREATE OR ALTER FUNCTION dbo.fn_estado(
    @modulo NVARCHAR(20),
    @nombre NVARCHAR(20)
)
RETURNS INT
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN (
        SELECT idEstado
          FROM dbo.Estado
         WHERE modulo = @modulo
           AND nombre = @nombre
    );
END;
GO

DROP TABLE IF EXISTS dbo.TallaStock;
CREATE TABLE dbo.TallaStock(
    idTallaStock INT IDENTITY PRIMARY KEY,
    idProducto   INT           NOT NULL,
    talla        NVARCHAR(6)    NOT NULL,
    stock        DECIMAL(12,3) NOT NULL CHECK(stock >= 0),
    idEstado     INT           NOT NULL DEFAULT (dbo.fn_estado(N'Producto', N'Activo')),
    CONSTRAINT FK_TallaStock_Producto FOREIGN KEY(idProducto) REFERENCES dbo.Producto(idProducto) ON DELETE CASCADE,
    CONSTRAINT FK_TallaStock_Estado FOREIGN KEY(idEstado) REFERENCES dbo.Estado(idEstado),
    CONSTRAINT UQ_TallaStock UNIQUE(idProducto, talla)
);

DROP TABLE IF EXISTS dbo.Presentacion;
CREATE TABLE dbo.Presentacion(
    idPresentacion INT IDENTITY PRIMARY KEY,
    idProducto     INT           NOT NULL,
    cantidad       DECIMAL(8,3)  NOT NULL CHECK(cantidad > 0),
    precio         DECIMAL(10,2) NOT NULL CHECK(precio >= 0),
    idEstado       INT           NOT NULL DEFAULT (dbo.fn_estado(N'Producto', N'Activo')),
    CONSTRAINT FK_Presentacion_Producto FOREIGN KEY(idProducto) REFERENCES dbo.Producto(idProducto) ON DELETE CASCADE,
    CONSTRAINT FK_Presentacion_Estado FOREIGN KEY(idEstado) REFERENCES dbo.Estado(idEstado),
    CONSTRAINT UQ_Presentacion UNIQUE(idProducto, cantidad)
);
GO

-- 4. TRANSACCIÓN – VENTA / PEDIDO
DROP TABLE IF EXISTS dbo.Transaccion;
CREATE TABLE dbo.Transaccion(
    idTransaccion     INT IDENTITY PRIMARY KEY,
    fecha             DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    fechaDia          AS CAST(fecha AS DATE) PERSISTED,
    idEstado          INT           NOT NULL DEFAULT (dbo.fn_estado(N'Transaccion', N'En Proceso')),
    totalBruto        DECIMAL(10,2) NOT NULL CHECK (totalBruto >= 0),
    descuento         DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK(descuento >= 0),
    CONSTRAINT CK_Transaccion_DescuentoMenor CHECK (descuento <= totalBruto),
    cargo             DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK(cargo     >= 0),
    totalNeto         AS CAST(totalBruto - descuento + cargo AS DECIMAL(10,2)) PERSISTED,
    observacion       NVARCHAR(120),
    motivoCancelacion NVARCHAR(120),
    idEmpleado        INT NOT NULL,
    idCliente         INT NOT NULL,
    CONSTRAINT FK_Trans_Estado   FOREIGN KEY(idEstado)   REFERENCES dbo.Estado(idEstado),
    CONSTRAINT FK_Trans_Empleado FOREIGN KEY(idEmpleado) REFERENCES dbo.Empleado(idPersona),
    CONSTRAINT FK_Trans_Cliente  FOREIGN KEY(idCliente)  REFERENCES dbo.Cliente(idPersona)
);
GO

DROP TABLE IF EXISTS dbo.Venta;
CREATE TABLE dbo.Venta(
    idTransaccion INT PRIMARY KEY,
    CONSTRAINT FK_Venta_Trans FOREIGN KEY(idTransaccion) REFERENCES dbo.Transaccion(idTransaccion) ON DELETE CASCADE
);

DROP TABLE IF EXISTS dbo.Pedido;
CREATE TABLE dbo.Pedido(
    idTransaccion INT  PRIMARY KEY,
    direccionEntrega      NVARCHAR(120) NOT NULL,
    fechaHoraEntrega     DATETIME2    NULL,
    idEmpleadoEntrega    INT          NULL,
    tipoPedido            NVARCHAR(20)  NOT NULL CHECK(tipoPedido IN ('Domicilio','Especial')),
    usaValeGas            BIT          NOT NULL DEFAULT 0,
    comentarioCancelacion NVARCHAR(120),
    CONSTRAINT FK_Pedido_Trans FOREIGN KEY(idTransaccion) REFERENCES dbo.Transaccion(idTransaccion) ON DELETE CASCADE,
    CONSTRAINT FK_Pedido_EmpleadoEntrega FOREIGN KEY(idEmpleadoEntrega) REFERENCES dbo.Empleado(idPersona)
);

DROP TABLE IF EXISTS dbo.OrdenCompra;
CREATE TABLE dbo.OrdenCompra(
    idOrdenCompra INT IDENTITY PRIMARY KEY,
    idPedido      INT           NOT NULL,
    idCliente     INT           NOT NULL,
    idProducto    INT           NOT NULL,
    cantidad      DECIMAL(12,3) NOT NULL CHECK(cantidad > 0),
    fechaCumplida DATETIME2     NULL,
    CONSTRAINT FK_Orden_Pedido   FOREIGN KEY(idPedido)   REFERENCES dbo.Pedido(idTransaccion) ON DELETE CASCADE,
    CONSTRAINT FK_Orden_Cliente  FOREIGN KEY(idCliente)  REFERENCES dbo.Cliente(idPersona) ON DELETE CASCADE,
    CONSTRAINT FK_Orden_Producto FOREIGN KEY(idProducto) REFERENCES dbo.Producto(idProducto)
);

-- 5. DETALLE, PAGOS
DROP TABLE IF EXISTS dbo.DetalleTransaccion;
CREATE TABLE dbo.DetalleTransaccion(
    idDetalle      INT IDENTITY PRIMARY KEY,
    idTransaccion  INT           NOT NULL,
    idProducto     INT           NOT NULL,
    idTallaStock   INT           NULL,
    idTallaStockKey AS ISNULL(idTallaStock,-1) PERSISTED,
    cantidad       DECIMAL(12,3) NOT NULL CHECK(cantidad > 0),
    precioUnitario DECIMAL(10,2) NOT NULL CHECK(precioUnitario >= 0),
    subtotal AS CAST(cantidad * precioUnitario AS DECIMAL(22,5)) PERSISTED,
    CONSTRAINT FK_DetTrans_Trans  FOREIGN KEY(idTransaccion) REFERENCES dbo.Transaccion(idTransaccion) ON DELETE CASCADE,
    CONSTRAINT FK_DetTrans_Prod   FOREIGN KEY(idProducto)     REFERENCES dbo.Producto(idProducto),
    CONSTRAINT FK_DetTrans_Talla  FOREIGN KEY(idTallaStock)   REFERENCES dbo.TallaStock(idTallaStock)
);

DROP TABLE IF EXISTS dbo.PagoTransaccion;
CREATE TABLE dbo.PagoTransaccion(
    idPago        INT IDENTITY PRIMARY KEY,
    idTransaccion INT           NOT NULL,
    idMetodoPago  INT           NOT NULL,
    monto         DECIMAL(10,2) NOT NULL CHECK(monto > 0),
    CONSTRAINT FK_PagoTrans_Trans  FOREIGN KEY(idTransaccion) REFERENCES dbo.Transaccion(idTransaccion) ON DELETE CASCADE,
    CONSTRAINT FK_PagoTrans_Metodo FOREIGN KEY(idMetodoPago) REFERENCES dbo.MetodoPago(idMetodoPago),
    CONSTRAINT UQ_PagoTrans UNIQUE(idTransaccion, idMetodoPago)
);
GO

-- 6. MOVIMIENTOS / PARÁMETROS / EVIDENCIA
DROP TABLE IF EXISTS dbo.MovimientoInventario;
CREATE TABLE dbo.MovimientoInventario(
    idMovimiento     INT IDENTITY PRIMARY KEY,
    idProducto       INT           NOT NULL,
    idTallaStock     INT           NULL,
    idTipoMovimiento INT           NOT NULL,
    cantidad         DECIMAL(12,3) NOT NULL CHECK(cantidad > 0),
    motivo           NVARCHAR(80)   NULL,
    fechaHora        DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    idEmpleado       INT           NOT NULL,
    CONSTRAINT FK_MovInv_Producto  FOREIGN KEY(idProducto) REFERENCES dbo.Producto(idProducto) ON DELETE CASCADE,
    CONSTRAINT FK_MovInv_Talla     FOREIGN KEY(idTallaStock) REFERENCES dbo.TallaStock(idTallaStock),
    CONSTRAINT FK_MovInv_Tipo      FOREIGN KEY(idTipoMovimiento) REFERENCES dbo.TipoMovimiento(idTipoMovimiento),
    CONSTRAINT FK_MovInv_Empleado  FOREIGN KEY(idEmpleado) REFERENCES dbo.Empleado(idPersona)
);

DROP TABLE IF EXISTS dbo.Comprobante;
CREATE TABLE dbo.Comprobante(
    idComprobante INT IDENTITY PRIMARY KEY,
    idTransaccion INT NOT NULL,
    fechaEmision  DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    bytesPdf      VARBINARY(MAX) NOT NULL,
    CONSTRAINT FK_Comprobante_Trans FOREIGN KEY(idTransaccion) REFERENCES dbo.Transaccion(idTransaccion) ON DELETE CASCADE,
    CONSTRAINT UQ_Comprobante_Trans UNIQUE(idTransaccion)
);

DROP TABLE IF EXISTS dbo.Reporte;
CREATE TABLE dbo.Reporte(
    idReporte       INT IDENTITY PRIMARY KEY,
    tipoReporte     NVARCHAR(20)   NOT NULL CHECK(tipoReporte IN ('Diario','Mensual','Rotacion')),
    idEmpleado      INT           NOT NULL,
    desde           DATE          NOT NULL,
    hasta           DATE          NOT NULL,
    filtros         NVARCHAR(200),
    bytesPdf        VARBINARY(MAX) NOT NULL,
    fechaGeneracion DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT CHK_HastaMayorIgualDesde CHECK(hasta >= desde),
    CONSTRAINT FK_Reporte_Empleado FOREIGN KEY(idEmpleado) REFERENCES dbo.Empleado(idPersona)
);

DROP TABLE IF EXISTS dbo.OrdenCompraPdf;
CREATE TABLE dbo.OrdenCompraPdf(
    idOrdenCompra  INT IDENTITY PRIMARY KEY,
    idPedido       INT           NOT NULL,
    bytesPdf       VARBINARY(MAX) NOT NULL,
    fechaGeneracion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_OrdenPdf_Pedido FOREIGN KEY(idPedido) REFERENCES dbo.Pedido(idTransaccion) ON DELETE CASCADE,
    CONSTRAINT UQ_OrdenPdf_Pedido UNIQUE(idPedido)
);
DROP TABLE IF EXISTS dbo.ParametroSistema;
CREATE TABLE dbo.ParametroSistema(
    clave        NVARCHAR(30) PRIMARY KEY,
    valor        DECIMAL(10,2) NOT NULL CHECK (valor >= 0),
    descripcion  NVARCHAR(120),
    actualizado  DATETIME2    NOT NULL DEFAULT SYSDATETIME(),
    idEmpleado   INT NOT NULL,
    CONSTRAINT FK_Parametro_Empleado FOREIGN KEY(idEmpleado) REFERENCES dbo.Empleado(idPersona)
);

DROP TABLE IF EXISTS dbo.BitacoraLogin;
CREATE TABLE dbo.BitacoraLogin(
    idBitacora  INT IDENTITY PRIMARY KEY,
    idEmpleado  INT       NOT NULL,
    fechaEvento DATETIME2  NOT NULL DEFAULT SYSDATETIME(),
    exitoso     BIT       NOT NULL,
    CONSTRAINT FK_Bitacora_Emp FOREIGN KEY(idEmpleado) REFERENCES dbo.Empleado(idPersona) ON DELETE CASCADE
);

DROP TABLE IF EXISTS dbo.AlertaStock;
CREATE TABLE dbo.AlertaStock(
    idAlerta    INT IDENTITY PRIMARY KEY,
    idProducto  INT           NOT NULL,
    stockActual DECIMAL(12,3) NOT NULL,
    umbral      DECIMAL(12,3) NOT NULL,
    fechaAlerta DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    procesada   BIT           NOT NULL DEFAULT 0,
    CONSTRAINT FK_AlertaStock_Producto FOREIGN KEY(idProducto)
        REFERENCES dbo.Producto(idProducto)
);
GO

-- 7. ÍNDICES
-- Los índices se crean al final para minimizar bloqueos.
DROP INDEX IF EXISTS IX_AlertaStock_Pendiente ON dbo.AlertaStock;
CREATE UNIQUE INDEX IX_AlertaStock_Pendiente ON dbo.AlertaStock(idProducto) WHERE procesada = 0;
DROP INDEX IF EXISTS IX_AlertaStock_Procesada ON dbo.AlertaStock;
CREATE INDEX IX_AlertaStock_Procesada ON dbo.AlertaStock(procesada, fechaAlerta);
DROP INDEX IF EXISTS IX_Bitacora_Empleado ON dbo.BitacoraLogin;
CREATE INDEX IX_Bitacora_Empleado ON dbo.BitacoraLogin(idEmpleado);
DROP INDEX IF EXISTS IX_Bitacora_ExitosoFecha ON dbo.BitacoraLogin;
CREATE NONCLUSTERED INDEX IX_Bitacora_ExitosoFecha ON dbo.BitacoraLogin(exitoso, fechaEvento);
DROP INDEX IF EXISTS IX_Bitacora_Fecha ON dbo.BitacoraLogin;
CREATE INDEX IX_Bitacora_Fecha ON dbo.BitacoraLogin(fechaEvento);
DROP INDEX IF EXISTS IX_DetTrans_Producto ON dbo.DetalleTransaccion;
CREATE INDEX IX_DetTrans_Producto ON dbo.DetalleTransaccion(idProducto);
DROP INDEX IF EXISTS IX_DetTrans_Talla ON dbo.DetalleTransaccion;
CREATE INDEX IX_DetTrans_Talla ON dbo.DetalleTransaccion(idTallaStock);
DROP INDEX IF EXISTS IX_DetTrans_TransProdTalla ON dbo.DetalleTransaccion;
CREATE UNIQUE INDEX IX_DetTrans_TransProdTalla ON dbo.DetalleTransaccion(idTransaccion, idProducto, idTallaStockKey);
DROP INDEX IF EXISTS IX_Empleado_Rol ON dbo.Empleado;
CREATE INDEX IX_Empleado_Rol ON dbo.Empleado(idRol);
DROP INDEX IF EXISTS IX_Estado_ModuloNombre ON dbo.Estado;
CREATE INDEX IX_Estado_ModuloNombre ON dbo.Estado(modulo, nombre);
DROP INDEX IF EXISTS IX_MovInv_Empleado ON dbo.MovimientoInventario;
CREATE INDEX IX_MovInv_Empleado ON dbo.MovimientoInventario(idEmpleado);
DROP INDEX IF EXISTS IX_MovInv_Fecha ON dbo.MovimientoInventario;
CREATE INDEX IX_MovInv_Fecha ON dbo.MovimientoInventario(fechaHora);
DROP INDEX IF EXISTS IX_MovInv_Prod ON dbo.MovimientoInventario;
CREATE INDEX IX_MovInv_Prod ON dbo.MovimientoInventario(idProducto);
DROP INDEX IF EXISTS IX_MovInv_Prod_Fecha ON dbo.MovimientoInventario;
CREATE INDEX IX_MovInv_Prod_Fecha ON dbo.MovimientoInventario(idProducto, fechaHora);
DROP INDEX IF EXISTS IX_MovInv_Tipo ON dbo.MovimientoInventario;
CREATE INDEX IX_MovInv_Tipo ON dbo.MovimientoInventario(idTipoMovimiento);
DROP INDEX IF EXISTS IX_OrdenCompra_Cliente ON dbo.OrdenCompra;
CREATE INDEX IX_OrdenCompra_Cliente ON dbo.OrdenCompra(idCliente);
DROP INDEX IF EXISTS IX_OrdenCompra_FechaCumplida ON dbo.OrdenCompra;
CREATE INDEX IX_OrdenCompra_FechaCumplida ON dbo.OrdenCompra(fechaCumplida);
DROP INDEX IF EXISTS IX_OrdenCompra_Pedido ON dbo.OrdenCompra;
CREATE INDEX IX_OrdenCompra_Pedido ON dbo.OrdenCompra(idPedido);
DROP INDEX IF EXISTS IX_OrdenCompra_Producto ON dbo.OrdenCompra;
CREATE INDEX IX_OrdenCompra_Producto ON dbo.OrdenCompra(idProducto);
DROP INDEX IF EXISTS IX_OrdenCompraPdf_Fecha ON dbo.OrdenCompraPdf;
CREATE INDEX IX_OrdenCompraPdf_Fecha ON dbo.OrdenCompraPdf(fechaGeneracion);
DROP INDEX IF EXISTS IX_PagoTransaccion_Metodo ON dbo.PagoTransaccion;
CREATE INDEX IX_PagoTransaccion_Metodo ON dbo.PagoTransaccion(idMetodoPago);
DROP INDEX IF EXISTS IX_Pedido_EmpleadoEntrega ON dbo.Pedido;
CREATE NONCLUSTERED INDEX IX_Pedido_EmpleadoEntrega ON dbo.Pedido(idEmpleadoEntrega);
DROP INDEX IF EXISTS IX_Pedido_Tipo ON dbo.Pedido;
CREATE INDEX IX_Pedido_Tipo ON dbo.Pedido(tipoPedido);
DROP INDEX IF EXISTS IX_Persona_Estado ON dbo.Persona;
CREATE INDEX IX_Persona_Estado ON dbo.Persona(idEstado);
DROP INDEX IF EXISTS IX_Presentacion_Producto ON dbo.Presentacion;
CREATE INDEX IX_Presentacion_Producto ON dbo.Presentacion(idProducto);
DROP INDEX IF EXISTS IX_Producto_Categoria ON dbo.Producto;
CREATE INDEX IX_Producto_Categoria ON dbo.Producto(idCategoria);
DROP INDEX IF EXISTS IX_Producto_Estado ON dbo.Producto;
CREATE INDEX IX_Producto_Estado ON dbo.Producto(idEstado);
DROP INDEX IF EXISTS IX_Producto_Mayorista ON dbo.Producto;
CREATE INDEX IX_Producto_Mayorista ON dbo.Producto(mayorista) INCLUDE(minMayorista, precioMayorista);
DROP INDEX IF EXISTS IX_Producto_StockUmbral ON dbo.Producto;
CREATE INDEX IX_Producto_StockUmbral ON dbo.Producto(stockActual) INCLUDE(umbral);
DROP INDEX IF EXISTS IX_Reporte_Empleado ON dbo.Reporte;
CREATE INDEX IX_Reporte_Empleado ON dbo.Reporte(idEmpleado);
DROP INDEX IF EXISTS IX_Reporte_FechaGen ON dbo.Reporte;
CREATE INDEX IX_Reporte_FechaGen ON dbo.Reporte(fechaGeneracion);
DROP INDEX IF EXISTS IX_TallaStock_Producto ON dbo.TallaStock;
CREATE INDEX IX_TallaStock_Producto ON dbo.TallaStock(idProducto);
DROP INDEX IF EXISTS IX_Trans_ClienteFecha ON dbo.Transaccion;
CREATE INDEX IX_Trans_ClienteFecha ON dbo.Transaccion(idCliente, fecha) INCLUDE(totalNeto, idEstado);
DROP INDEX IF EXISTS IX_Trans_Fecha ON dbo.Transaccion;
CREATE INDEX IX_Trans_Fecha ON dbo.Transaccion(fecha) INCLUDE(idCliente, totalNeto, idEstado);
GO
