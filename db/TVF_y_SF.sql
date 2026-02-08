-- Funciones escalares y con valores de tabla para la base de datos de Comercial's Valerio

USE cv_ventas_distribucion;
GO

-- Parámetros tipo tabla utilizados por funciones y procedimientos
IF TYPE_ID(N'dbo.tvp_DetalleTx') IS NOT NULL
    DROP TYPE dbo.tvp_DetalleTx;
GO
CREATE TYPE dbo.tvp_DetalleTx AS TABLE(
    idProducto     INT           NOT NULL,
    idTallaStock   INT           NULL,
    cantidad       DECIMAL(12,3) NOT NULL CHECK (cantidad > 0),
    precioUnitario DECIMAL(10,2) NOT NULL CHECK (precioUnitario >= 0)
);
GO

IF TYPE_ID(N'dbo.tvp_PagoTx') IS NOT NULL
    DROP TYPE dbo.tvp_PagoTx;
GO
    CREATE TYPE dbo.tvp_PagoTx AS TABLE(
        idMetodoPago INT           NOT NULL,
        monto        DECIMAL(10,2) NOT NULL CHECK (monto > 0),
        PRIMARY KEY(idMetodoPago)
    );
GO

-- Nombre completo de una persona
CREATE OR ALTER FUNCTION dbo.fn_NombreCompleto ( @IdPersona INT )
RETURNS NVARCHAR(120)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS BEGIN
   RETURN (
       SELECT CONCAT_WS(' ', p.nombres, p.apellidos)
        FROM   dbo.Persona AS p
        WHERE  p.idPersona = @IdPersona
   );
END;
GO

-- Obtiene el valor decimal de un ParametroSistema
CREATE OR ALTER FUNCTION dbo.fn_GetParametroDecimal(
    @clave NVARCHAR(30),
    @def   DECIMAL(10,2)
)
RETURNS DECIMAL(10,2)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN COALESCE(
        (SELECT valor FROM dbo.ParametroSistema WHERE clave = @clave),
        @def
    );
END;
GO

--Cargo fijo por reparto
CREATE OR ALTER FUNCTION dbo.fn_CargoRepartoActual()
RETURNS DECIMAL(10,2)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN dbo.fn_GetParametroDecimal(N'CARGO_REPARTO', 0);
END;
GO

--Descuento con vale de gas
CREATE OR ALTER FUNCTION dbo.fn_DescuentoValeGas()
RETURNS DECIMAL(10,2)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN dbo.fn_GetParametroDecimal(N'DESCUENTO_VALE_GAS', 0);
END;
GO

CREATE OR ALTER FUNCTION dbo.fn_actor_id()
RETURNS INT
WITH SCHEMABINDING
AS
BEGIN
    RETURN TRY_CONVERT(INT, SESSION_CONTEXT(N'idEmpleado'));
END;
GO

CREATE OR ALTER FUNCTION dbo.fn_actor_nivel()
RETURNS INT
WITH SCHEMABINDING
AS
BEGIN
    DECLARE @idEmp INT = dbo.fn_actor_id();
    IF @idEmp IS NULL
        RETURN NULL;  -- contexto no establecido

    DECLARE @nivel INT = (
        SELECT r.nivel
        FROM dbo.Empleado e
        JOIN dbo.Rol r ON r.idRol = e.idRol
        WHERE e.idPersona = @idEmp
    );

    RETURN @nivel;  -- puede ser NULL si el empleado no existe
END;
GO

-- Capitaliza un texto (primera letra en mayúscula, resto en minúscula)
CREATE OR ALTER FUNCTION dbo.fn_Capitalizar(@texto NVARCHAR(120))
RETURNS NVARCHAR(120)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN UPPER(LEFT(@texto,1)) + LOWER(SUBSTRING(@texto,2,LEN(@texto)));
END;
GO

-- Normaliza espacios extras en un texto colapsando cualquier
-- secuencia de espacios en uno solo
CREATE OR ALTER FUNCTION dbo.fn_NormalizarEspacios(@texto NVARCHAR(200))
RETURNS NVARCHAR(200)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    DECLARE @clean NVARCHAR(200) =
        LTRIM(RTRIM(REPLACE(@texto, CHAR(160), ' ')));
    WHILE CHARINDEX('  ', @clean) > 0
        SET @clean = REPLACE(@clean, '  ', ' ');
    RETURN @clean;
END;
GO

-- Valida que un teléfono contenga solo dígitos y longitud correcta
CREATE OR ALTER FUNCTION dbo.fn_EsTelefonoValido(@telefono NVARCHAR(15))
RETURNS BIT
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    IF @telefono LIKE '%[^0-9]%' OR LEN(@telefono) < 6 OR LEN(@telefono) > 15
        RETURN 0;
    RETURN 1;
END;
GO

-- Normaliza un telefono removiendo espacios, '+' y '.' y signos comunes
CREATE OR ALTER FUNCTION dbo.fn_NormalizarTelefono(@telefono NVARCHAR(30))
RETURNS NVARCHAR(15)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    DECLARE @tmp NVARCHAR(30) =
        TRANSLATE(@telefono, ' ()-.+', '      '); -- six spaces
    SET @tmp = REPLACE(@tmp, ' ', '');
    RETURN SUBSTRING(@tmp, 1, 15);
END;
GO

-- Suma total de pagos asociados a una transaccion
CREATE OR ALTER FUNCTION dbo.fn_TotalPagosTransaccion(@idTx INT)
RETURNS DECIMAL(12,2)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN (
        SELECT ISNULL(SUM(pt.monto), 0)
        FROM dbo.PagoTransaccion AS pt
        WHERE pt.idTransaccion = @idTx
    );
END;
GO

-- Stock disponible para un producto
CREATE OR ALTER FUNCTION dbo.fn_StockDisponible(@idProducto INT)
RETURNS DECIMAL(12,3)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN COALESCE(
        (
            SELECT p.stockActual
            FROM dbo.Producto p
            WHERE p.idProducto = @idProducto
        ),
        0
    );
END;
GO

-- Stock disponible para una talla especifica
CREATE OR ALTER FUNCTION dbo.fn_StockDisponibleTalla(@idTallaStock INT)
RETURNS DECIMAL(12,3)
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN COALESCE(
        (
            SELECT ts.stock
            FROM dbo.TallaStock AS ts
            WHERE ts.idTallaStock = @idTallaStock
        ),
        0
    );
END;
GO

-- Indica si un producto pertenece al tipo especificado
CREATE OR ALTER FUNCTION dbo.fn_EsTipoProducto(
    @idProducto INT,
    @tipo       NVARCHAR(20)
)
RETURNS BIT
WITH SCHEMABINDING, RETURNS NULL ON NULL INPUT
AS
BEGIN
    RETURN CASE WHEN EXISTS (
            SELECT 1
              FROM dbo.Producto      p
              JOIN dbo.TipoProducto tp ON tp.idTipoProducto = p.idTipoProducto
             WHERE p.idProducto = @idProducto
               AND tp.nombre    = @tipo
        )
        THEN 1 ELSE 0 END;
END;
GO

-- Determina si un pedido debe clasificarse como Domicilio o Especial
CREATE OR ALTER FUNCTION dbo.fn_ClasificarPedido(@detalle dbo.tvp_DetalleTx READONLY)
RETURNS NVARCHAR(20)
WITH SCHEMABINDING
AS
BEGIN
    IF EXISTS (SELECT 1
                 FROM @detalle d
                 JOIN dbo.Producto p ON p.idProducto = d.idProducto
                WHERE p.tipoPedidoDefault = N'Especial')
        RETURN N'Especial';

    RETURN N'Domicilio';
END;
GO

-- Cantidad mínima de ovillos para precio mayorista de hilo
CREATE OR ALTER FUNCTION dbo.fn_MinCantidadMayoristaHilo()
RETURNS INT
WITH SCHEMABINDING
AS
BEGIN
    RETURN CAST(dbo.fn_GetParametroDecimal(N'MIN_CANTIDAD_MAYORISTA_HILO', 200) AS INT);
END;
GO

-- Intentos fallidos permitidos antes de bloquear la cuenta
CREATE OR ALTER FUNCTION dbo.fn_MaxIntentosFallidos()
RETURNS INT
WITH SCHEMABINDING
AS
BEGIN
    RETURN CAST(dbo.fn_GetParametroDecimal(N'MAX_INTENTOS_FALLIDOS', 3) AS INT);
END;
GO

-- Duración del bloqueo de cuenta en minutos
CREATE OR ALTER FUNCTION dbo.fn_MinutosBloqueoCuenta()
RETURNS INT
WITH SCHEMABINDING
AS
BEGIN
    RETURN CAST(dbo.fn_GetParametroDecimal(N'MINUTOS_BLOQUEO_CUENTA', 5) AS INT);
END;
GO

-- Valida que un Estado pertenezca al módulo especificado
CREATE OR ALTER FUNCTION dbo.fn_AssertEstadoModulo(
    @idEstado INT,
    @modulo   NVARCHAR(20)
)
RETURNS BIT
WITH SCHEMABINDING
AS
BEGIN
    IF EXISTS (
        SELECT 1 FROM dbo.Estado e
        WHERE e.idEstado = @idEstado
          AND e.modulo = @modulo
    )
        RETURN 1;

    RETURN 0;
END;
GO

-- Verifica si algún producto o talla tiene stock negativo
CREATE OR ALTER FUNCTION dbo.fn_TieneStockNegativo(
    @detalle dbo.tvp_DetalleTx READONLY
)
RETURNS INT
WITH SCHEMABINDING
AS
BEGIN
    DECLARE @flag INT;

    SELECT @flag =
            CASE WHEN MAX(CASE WHEN p.stockActual < 0 THEN 1 ELSE 0 END) > 0 THEN 1 ELSE 0 END
          + CASE WHEN MAX(CASE WHEN ts.stock < 0 THEN 1 ELSE 0 END) > 0 THEN 2 ELSE 0 END
      FROM (SELECT DISTINCT idProducto, idTallaStock FROM @detalle) d
      LEFT JOIN dbo.Producto p
        ON d.idProducto = p.idProducto
      LEFT JOIN dbo.TallaStock ts
        ON d.idTallaStock = ts.idTallaStock;

    RETURN @flag;
END;
GO
