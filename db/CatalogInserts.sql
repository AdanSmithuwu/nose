-- Inserciones de catálogos para la base de datos de Comercial's Valerio

USE cv_ventas_distribucion;
GO

BEGIN TRY
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRAN;

    -- Deshabilita temporalmente los triggers de solo administrador durante la carga inicial
    EXEC dbo.sp_DisableSeedTriggers;

    -- Datos maestros
    MERGE dbo.Estado WITH (HOLDLOCK) AS t
    USING (VALUES
        (N'Activo',N'Persona'), (N'Inactivo',N'Persona'),
        (N'Activo',N'Producto'), (N'Inactivo',N'Producto'), (N'Desactivado',N'Producto'),
        (N'Inactivo por umbral',N'Producto'),
        (N'En Proceso',N'Transaccion'), (N'Completada',N'Transaccion'),
        (N'Entregada',N'Transaccion'), (N'Cancelada',N'Transaccion'),
        (N'Activo',N'Categoria'), (N'Inactivo',N'Categoria')
    ) AS src(nombre, modulo)
        ON t.nombre = src.nombre AND t.modulo = src.modulo
    WHEN NOT MATCHED THEN
        INSERT(nombre, modulo) VALUES(src.nombre, src.modulo);

    MERGE dbo.Rol WITH (HOLDLOCK) AS t
    USING (VALUES (N'Administrador',1),(N'Empleado',2)) AS src(nombre,nivel)
        ON t.nombre = src.nombre
    WHEN MATCHED THEN UPDATE SET nivel = src.nivel
    WHEN NOT MATCHED THEN INSERT(nombre, nivel) VALUES(src.nombre, src.nivel);
    MERGE dbo.TipoProducto WITH (HOLDLOCK) AS t
    USING (VALUES (N'Unidad fija'),(N'Vestimenta'),(N'Fraccionable')) AS src(nombre)
        ON t.nombre = src.nombre
    WHEN NOT MATCHED THEN INSERT(nombre) VALUES(src.nombre);
    DECLARE @idActCat INT =
        dbo.fn_estado(N'Categoria', N'Activo');
    IF @idActCat IS NULL
        THROW 70001, 'Estado "Activo" para Categoria no encontrado.', 1;
    MERGE dbo.Categoria WITH (HOLDLOCK) AS t
    USING (VALUES
        (N'Accesorios',              N'Artículos complementarios y adornos para vestir',             @idActCat),
        (N'Bebidas',                 N'Productos líquidos para consumo diario',                      @idActCat),
        (N'Calzado',                 N'Zapatos y sandalias para diferentes edades y estilos',        @idActCat),
        (N'Deportes',                N'Equipamiento y artículos para actividad física',              @idActCat),
        (N'Despensa',                N'Abarrotes y alimentos de uso cotidiano',                      @idActCat),
        (N'Electrodomésticos',       N'Aparatos eléctricos y electrónicos para el hogar',            @idActCat),
        (N'Ferretería',              N'Herramientas y materiales para construcción y reparación',    @idActCat),
        (N'Higiene personal',        N'Productos de cuidado y aseo personal',                        @idActCat),
        (N'Hogar',                   N'Artículos domésticos y menaje de casa',                       @idActCat),
        (N'Insumos médicos',         N'Suministros médicos y material de curación',                  @idActCat),
        (N'Limpieza',                N'Productos para limpieza y desinfección',                      @idActCat),
        (N'Locería',                 N'Vajilla, platos y utensilios de mesa',                        @idActCat),
        (N'Medicina alternativa',    N'Productos utilizados en terapias naturales',                  @idActCat),
        (N'Medicamentos inyectables',N'Fármacos de administración inyectable',                       @idActCat),
        (N'Medicamentos VO',         N'Medicamentos administrados por vía oral',                     @idActCat),
        (N'Mercería',                N'Insumos para costura y manualidades',                         @idActCat),
        (N'Snacks',                  N'Aperitivos y golosinas para consumo rápido',                  @idActCat),
        (N'Textil hogar',            N'Prendas y tejidos utilizados en el hogar',                    @idActCat),
        (N'Utensilios',              N'Herramientas de cocina y utensilios varios',                  @idActCat),
        (N'Veterinaria',             N'Productos destinados al cuidado de animales',                 @idActCat),
        (N'Vestimenta',              N'Prendas de vestir para toda ocasión',                         @idActCat)
    ) AS src(nombre, descripcion, idEstado)
        ON t.nombre = src.nombre
    WHEN MATCHED THEN
        UPDATE SET descripcion = src.descripcion,
                   idEstado = src.idEstado
    WHEN NOT MATCHED THEN
        INSERT(nombre, descripcion, idEstado)
        VALUES(src.nombre, src.descripcion, src.idEstado);
    MERGE dbo.TipoMovimiento WITH (HOLDLOCK) AS t
    USING (VALUES (N'Entrada'),(N'Salida'),(N'Ajuste'),(N'Cancelación')) AS src(nombre)
        ON t.nombre = src.nombre
    WHEN NOT MATCHED THEN INSERT(nombre) VALUES(src.nombre);
    MERGE dbo.MetodoPago WITH (HOLDLOCK) AS m
    USING (VALUES (N'Efectivo'),(N'Billetera Digital')) AS src(nombre)
        ON m.nombre = src.nombre
    WHEN NOT MATCHED THEN INSERT(nombre) VALUES(src.nombre);

    COMMIT;
    EXEC dbo.sp_EnableSeedTriggers;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    EXEC dbo.sp_EnableSeedTriggers;
    THROW;
END CATCH
GO
