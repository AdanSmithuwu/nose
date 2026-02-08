-- Lotes de productos de ejemplo

USE cv_ventas_distribucion;
GO

-- Deshabilitar triggers de protección
EXEC dbo.sp_DisableSeedTriggers;

-- ===== Productos de ejemplo =====
BEGIN TRY
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRAN;

IF NOT EXISTS (SELECT 1 FROM dbo.Producto)
BEGIN
DECLARE @idAdminPersona INT =
    (SELECT idPersona FROM dbo.Empleado WHERE usuario=N'admin');
IF @idAdminPersona IS NULL
    THROW 64000, 'Admin user not found for seeding.', 1;
EXEC sp_set_session_context N'idEmpleado', @idAdminPersona;
DECLARE
  @Accesorios          INT,
  @Bebidas             INT,
  @Calzado             INT,
  @Deportes            INT,
  @Despensa            INT,
  @Electrodomesticos   INT,
  @Ferreteria          INT,
  @HigienePersonal     INT,
  @Hogar               INT,
  @InsumosMedicos      INT,
  @Limpieza            INT,
  @Loceria             INT,
  @MedicinaAlternativa INT,
  @MedicInyectables    INT,
  @MedicVO             INT,
  @Merceria            INT,
  @Snacks              INT,
  @TextilHogar         INT,
  @Utensilios          INT,
  @Veterinaria         INT,
  @Vestimenta          INT;

DECLARE @catNames TABLE(nombre NVARCHAR(50));
INSERT INTO @catNames(nombre) VALUES
  (N'Accesorios'),
  (N'Bebidas'),
  (N'Calzado'),
  (N'Deportes'),
  (N'Despensa'),
  (N'Electrodomésticos'),
  (N'Ferretería'),
  (N'Higiene personal'),
  (N'Hogar'),
  (N'Insumos médicos'),
  (N'Limpieza'),
  (N'Locería'),
  (N'Medicina alternativa'),
  (N'Medicamentos inyectables'),
  (N'Medicamentos VO'),
  (N'Mercería'),
  (N'Snacks'),
  (N'Textil hogar'),
  (N'Utensilios'),
  (N'Veterinaria'),
  (N'Vestimenta');

SELECT
  @Accesorios          = MAX(CASE WHEN c.nombre = N'Accesorios' THEN c.idCategoria END),
  @Bebidas             = MAX(CASE WHEN c.nombre = N'Bebidas' THEN c.idCategoria END),
  @Calzado             = MAX(CASE WHEN c.nombre = N'Calzado' THEN c.idCategoria END),
  @Deportes            = MAX(CASE WHEN c.nombre = N'Deportes' THEN c.idCategoria END),
  @Despensa            = MAX(CASE WHEN c.nombre = N'Despensa' THEN c.idCategoria END),
  @Electrodomesticos   = MAX(CASE WHEN c.nombre = N'Electrodomésticos' THEN c.idCategoria END),
  @Ferreteria          = MAX(CASE WHEN c.nombre = N'Ferretería' THEN c.idCategoria END),
  @HigienePersonal     = MAX(CASE WHEN c.nombre = N'Higiene personal' THEN c.idCategoria END),
  @Hogar               = MAX(CASE WHEN c.nombre = N'Hogar' THEN c.idCategoria END),
  @InsumosMedicos      = MAX(CASE WHEN c.nombre = N'Insumos médicos' THEN c.idCategoria END),
  @Limpieza            = MAX(CASE WHEN c.nombre = N'Limpieza' THEN c.idCategoria END),
  @Loceria             = MAX(CASE WHEN c.nombre = N'Locería' THEN c.idCategoria END),
  @MedicinaAlternativa = MAX(CASE WHEN c.nombre = N'Medicina alternativa' THEN c.idCategoria END),
  @MedicInyectables    = MAX(CASE WHEN c.nombre = N'Medicamentos inyectables' THEN c.idCategoria END),
  @MedicVO             = MAX(CASE WHEN c.nombre = N'Medicamentos VO' THEN c.idCategoria END),
  @Merceria            = MAX(CASE WHEN c.nombre = N'Mercería' THEN c.idCategoria END),
  @Snacks              = MAX(CASE WHEN c.nombre = N'Snacks' THEN c.idCategoria END),
  @TextilHogar         = MAX(CASE WHEN c.nombre = N'Textil hogar' THEN c.idCategoria END),
  @Utensilios          = MAX(CASE WHEN c.nombre = N'Utensilios' THEN c.idCategoria END),
  @Veterinaria         = MAX(CASE WHEN c.nombre = N'Veterinaria' THEN c.idCategoria END),
  @Vestimenta          = MAX(CASE WHEN c.nombre = N'Vestimenta' THEN c.idCategoria END)
FROM dbo.Categoria c
JOIN @catNames n ON n.nombre COLLATE Latin1_General_CI_AI = c.nombre;

-- Verificar que todas las categorías existen
IF @Accesorios IS NULL OR @Bebidas IS NULL OR @Calzado IS NULL OR
   @Deportes IS NULL OR @Despensa IS NULL OR @Electrodomesticos IS NULL OR
   @Ferreteria IS NULL OR @HigienePersonal IS NULL OR @Hogar IS NULL OR
   @InsumosMedicos IS NULL OR @Limpieza IS NULL OR @Loceria IS NULL OR
   @MedicinaAlternativa IS NULL OR @MedicInyectables IS NULL OR
   @MedicVO IS NULL OR @Merceria IS NULL OR @Snacks IS NULL OR
   @TextilHogar IS NULL OR @Utensilios IS NULL OR @Veterinaria IS NULL OR
   @Vestimenta IS NULL
    THROW 64001, 'Required categories not found for seeding.', 1;

DECLARE @idEntrada INT =
    (SELECT idTipoMovimiento FROM dbo.TipoMovimiento WHERE nombre=N'Entrada');
IF @idEntrada IS NULL
    THROW 64001, N'El tipo de movimiento es obligatorio.', 1;

-- BLOQUE 2: LOCERÍA, UTENSILIOS Y ELECTRO (34 ítems)
SET NOCOUNT ON;
DECLARE @idActProd INT=(SELECT idEstado FROM dbo.Estado WHERE nombre=N'Activo' AND modulo=N'Producto');
DECLARE @tipoFijo  INT=(SELECT idTipoProducto FROM dbo.TipoProducto WHERE nombre=N'Unidad fija');
DECLARE @minMayoristaHilo INT=(SELECT CAST(valor AS INT) FROM dbo.ParametroSistema WHERE clave=N'MIN_CANTIDAD_MAYORISTA_HILO');
-- LOCERÍA
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Plato porcelana blanco',N'Utensilio de cocina - Plato porcelana blanco',@Loceria,@tipoFijo,N'unid',4.30,36,2,@idActProd),
 (N'Plato porcelana decorado',N'Utensilio de cocina - Plato porcelana decorado',@Loceria,@tipoFijo,N'unid',5.00,24,2,@idActProd),
 (N'Juego de ollas domésticas x 6',N'Utensilio de cocina - Juego de ollas domésticas x 6',@Loceria,@tipoFijo,N'juego',280.00,2,1,@idActProd),
 (N'Juego de ollas anodizado x 6',N'Utensilio de cocina - Juego de ollas anodizado x 6',@Loceria,@tipoFijo,N'juego',300.00,2,1,@idActProd),
 (N'Olla aluminio N° 500 000',N'Utensilio de cocina - Olla aluminio N° 500 000',@Loceria,@tipoFijo,N'unid',205.00,3,1,@idActProd),
 (N'Olla aluminio N° 100 000',N'Utensilio de cocina - Olla aluminio N° 100 000',@Loceria,@tipoFijo,N'unid',135.00,4,1,@idActProd),
 (N'Olla aluminio N° 80 000',N'Utensilio de cocina - Olla aluminio N° 80 000',@Loceria,@tipoFijo,N'unid',125.00,4,1,@idActProd),
 (N'Olla aluminio N° 60 000',N'Utensilio de cocina - Olla aluminio N° 60 000',@Loceria,@tipoFijo,N'unid',118.00,4,1,@idActProd),
 (N'Olla aluminio N° 40 000',N'Utensilio de cocina - Olla aluminio N° 40 000',@Loceria,@tipoFijo,N'unid',105.00,4,1,@idActProd),
 (N'Olla aluminio N° 20 000',N'Utensilio de cocina - Olla aluminio N° 20 000',@Loceria,@tipoFijo,N'unid',90.00,4,1,@idActProd),
 (N'Olla aluminio N° 10 000',N'Utensilio de cocina - Olla aluminio N° 10 000',@Loceria,@tipoFijo,N'unid',70.00,4,1,@idActProd),
 (N'Olla fierro N° 400',N'Utensilio de cocina - Olla fierro N° 400',@Loceria,@tipoFijo,N'unid',45.00,4,1,@idActProd),
 (N'Olla fierro N° 200',N'Utensilio de cocina - Olla fierro N° 200',@Loceria,@tipoFijo,N'unid',40.00,4,1,@idActProd),
 (N'Olla fierro N° 100',N'Utensilio de cocina - Olla fierro N° 100',@Loceria,@tipoFijo,N'unid',35.00,4,1,@idActProd),
 (N'Olla fierro N° 80',N'Utensilio de cocina - Olla fierro N° 80',@Loceria,@tipoFijo,N'unid',30.00,4,1,@idActProd),
 (N'Olla fierro N° 60',N'Utensilio de cocina - Olla fierro N° 60',@Loceria,@tipoFijo,N'unid',25.00,4,1,@idActProd),
 (N'Olla paila N° 30',N'Utensilio de cocina - Olla paila N° 30',@Loceria,@tipoFijo,N'unid',80.00,3,1,@idActProd),
 (N'Olla paila N° 20',N'Utensilio de cocina - Olla paila N° 20',@Loceria,@tipoFijo,N'unid',70.00,3,1,@idActProd),
 (N'Olla paila N° 22',N'Utensilio de cocina - Olla paila N° 22',@Loceria,@tipoFijo,N'unid',60.00,3,1,@idActProd),
 (N'Tapers herméticos colores (set 6)',N'Utensilio de cocina - Tapers herméticos colores (set 6)',@Loceria,@tipoFijo,N'set',15.00,6,2,@idActProd),
 (N'Set tazas vidrio decorado (6)',N'Utensilio de cocina - Set tazas vidrio decorado (6)',@Loceria,@tipoFijo,N'set',35.00,2,1,@idActProd),
 (N'Jarra JC acero inoxidable',N'Utensilio de cocina - Jarra JC acero inoxidable',@Loceria,@tipoFijo,N'unid',84.00,12,2,@idActProd),
 (N'Perol aluminio #60',N'Utensilio de cocina - Perol aluminio #60',@Loceria,@tipoFijo,N'unid',36.00,6,2,@idActProd);
-- UTENSILIOS
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Cuchillo cocina Titanio 7"',N'Herramienta culinaria - Cuchillo cocina Titanio 7"',@Utensilios,@tipoFijo,N'unid',6.00,12,2,@idActProd),
 (N'Cuchillo cocina Tramontina',N'Herramienta culinaria - Cuchillo cocina Tramontina',@Utensilios,@tipoFijo,N'unid',12.00,10,2,@idActProd),
 (N'Colador "Florentina" 22 cm',N'Herramienta culinaria - Colador "Florentina" 22 cm',@Utensilios,@tipoFijo,N'unid',13.00,6,2,@idActProd),
 (N'Termo 1.2 L marca Yean',N'Herramienta culinaria - Termo 1.2 L marca Yean',@Utensilios,@tipoFijo,N'unid',15.00,3,1,@idActProd),
 (N'Termo 1.2 L marca Vogue',N'Herramienta culinaria - Termo 1.2 L marca Vogue',@Utensilios,@tipoFijo,N'unid',58.00,3,1,@idActProd),
 (N'Molino manual "Victoria" cromado',N'Herramienta culinaria - Molino manual "Victoria" cromado',@Utensilios,@tipoFijo,N'unid',128.00,7,1,@idActProd);
-- ELECTRODOMÉSTICOS
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Cocina 2 hornillas acero "Surge"',N'Equipo para cocina - Cocina 2 hornillas acero "Surge"',@Electrodomesticos,@tipoFijo,N'unid',75.00,2,1,@idActProd),
 (N'Cocina 2 hornillas pintada "Surge"',N'Equipo para cocina - Cocina 2 hornillas pintada "Surge"',@Electrodomesticos,@tipoFijo,N'unid',70.00,2,1,@idActProd),
 (N'Cocina 2 hornillas encendido automático',N'Equipo para cocina - Cocina 2 hornillas encendido automático',@Electrodomesticos,@tipoFijo,N'unid',150.00,2,1,@idActProd),
 (N'Cocina eléctrica 2 H de mesa IMACO',N'Equipo para cocina - Cocina eléctrica 2 H de mesa IMACO',@Electrodomesticos,@tipoFijo,N'unid',150.00,3,1,@idActProd),
 (N'Olla arrocera eléctrica 1.8 L',N'Equipo para cocina - Olla arrocera eléctrica 1.8 L',@Electrodomesticos,@tipoFijo,N'unid',72.00,3,1,@idActProd);
PRINT N'>>> BLOQUE 2 cargado (34 productos)';

-- BLOQUE 3: MEDICINA ALTERNATIVA (27 productos)
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Aceite Copaiba 15 mL',N'Remedio natural - Aceite Copaiba 15 mL',@MedicinaAlternativa,@tipoFijo,N'fr',10.00,8,2,@idActProd),
 (N'Timol 75 mL',N'Remedio natural - Timol 75 mL',@MedicinaAlternativa,@tipoFijo,N'fr',4.50,12,2,@idActProd),
 (N'Vinagre Bully 100 mL',N'Remedio natural - Vinagre Bully 100 mL',@MedicinaAlternativa,@tipoFijo,N'fr',4.50,12,2,@idActProd),
 (N'Colonia Claveles 70 mL',N'Remedio natural - Colonia Claveles 70 mL',@MedicinaAlternativa,@tipoFijo,N'fr',10.00,5,1,@idActProd),
 (N'Colonia Rosas 70 mL',N'Remedio natural - Colonia Rosas 70 mL',@MedicinaAlternativa,@tipoFijo,N'fr',10.00,5,1,@idActProd),
 (N'Colonia Violetas 70 mL',N'Remedio natural - Colonia Violetas 70 mL',@MedicinaAlternativa,@tipoFijo,N'fr',10.00,5,1,@idActProd),
 (N'Colonia Lavanda 75 mL',N'Remedio natural - Colonia Lavanda 75 mL',@MedicinaAlternativa,@tipoFijo,N'fr',10.00,2,1,@idActProd),
 (N'Maravilla Curativa 180 mL',N'Remedio natural - Maravilla Curativa 180 mL',@MedicinaAlternativa,@tipoFijo,N'fr',3.50,10,2,@idActProd),
 (N'Agua Florida 70 mL',N'Remedio natural - Agua Florida 70 mL',@MedicinaAlternativa,@tipoFijo,N'fr',5.00,10,2,@idActProd),
 (N'Sangre de grado 30 mL',N'Remedio natural - Sangre de grado 30 mL',@MedicinaAlternativa,@tipoFijo,N'fr',6.00,5,1,@idActProd),
 (N'Tabú colonia',N'Remedio natural - Tabú colonia',@MedicinaAlternativa,@tipoFijo,N'fr',15.00,12,2,@idActProd),
 (N'Perfume 7 Iglesias',N'Remedio natural - Perfume 7 Iglesias',@MedicinaAlternativa,@tipoFijo,N'fr',20.00,4,1,@idActProd),
 (N'Colonia 7 Iglesias 220 mL',N'Remedio natural - Colonia 7 Iglesias 220 mL',@MedicinaAlternativa,@tipoFijo,N'fr',10.00,6,1,@idActProd),
 (N'Agua Florida 270 mL',N'Remedio natural - Agua Florida 270 mL',@MedicinaAlternativa,@tipoFijo,N'fr',12.00,5,1,@idActProd),
 (N'Aceite de oliva E/V 200 mL',N'Remedio natural - Aceite de oliva E/V 200 mL',@MedicinaAlternativa,@tipoFijo,N'fr',12.00,6,1,@idActProd),
 (N'Flores de Kananga 67 mL',N'Remedio natural - Flores de Kananga 67 mL',@MedicinaAlternativa,@tipoFijo,N'fr',4.50,12,2,@idActProd),
 (N'Violeta de genciana 30 mL',N'Remedio natural - Violeta de genciana 30 mL',@MedicinaAlternativa,@tipoFijo,N'fr',5.00,12,2,@idActProd),
 (N'Curarina 90 mL',N'Remedio natural - Curarina 90 mL',@MedicinaAlternativa,@tipoFijo,N'fr',10.00,6,1,@idActProd),
 (N'Franja Negra 125 mL',N'Remedio natural - Franja Negra 125 mL',@MedicinaAlternativa,@tipoFijo,N'fr',18.00,5,1,@idActProd),
 (N'Agua de susto 30 mL',N'Remedio natural - Agua de susto 30 mL',@MedicinaAlternativa,@tipoFijo,N'fr',2.50,12,2,@idActProd),
 (N'Tintura valeriana 30 mL',N'Remedio natural - Tintura valeriana 30 mL',@MedicinaAlternativa,@tipoFijo,N'fr',3.00,17,2,@idActProd),
 (N'Agua de los 7 Espíritus 30 mL',N'Remedio natural - Agua de los 7 Espíritus 30 mL',@MedicinaAlternativa,@tipoFijo,N'fr',2.50,18,2,@idActProd),
 (N'Violeta de genciana 60 mL',N'Remedio natural - Violeta de genciana 60 mL',@MedicinaAlternativa,@tipoFijo,N'fr',4.00,12,2,@idActProd),
 (N'Agua de Carmen 30 mL',N'Remedio natural - Agua de Carmen 30 mL',@MedicinaAlternativa,@tipoFijo,N'fr',3.00,12,2,@idActProd),
 (N'Aceite de almendras 30 mL',N'Remedio natural - Aceite de almendras 30 mL',@MedicinaAlternativa,@tipoFijo,N'fr',3.00,12,2,@idActProd),
 (N'Barras de azufre (pack)',N'Remedio natural - Barras de azufre (pack)',@MedicinaAlternativa,@tipoFijo,N'paq',7.00,12,2,@idActProd),
 (N'Colonia Cariño 70 mL',N'Remedio natural - Colonia Cariño 70 mL',@MedicinaAlternativa,@tipoFijo,N'fr',15.00,10,2,@idActProd);
PRINT N'>>> BLOQUE 3 cargado (27 productos)';

-- BLOQUE 4: MEDICAMENTOS VO (35 productos)
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Dolo-Neurobion Forte 500 mg tableta',N'Medicamento oral - Dolo-Neurobion Forte 500 mg tableta',@MedicVO,@tipoFijo,N'tabl',2.00,10,5,@idActProd),
 (N'Bactrim 800 mg tableta',N'Medicamento oral - Bactrim 800 mg tableta',@MedicVO,@tipoFijo,N'tabl',2.50,10,5,@idActProd),
 (N'Altalgina 500 mg tableta',N'Medicamento oral - Altalgina 500 mg tableta',@MedicVO,@tipoFijo,N'tabl',1.00,10,5,@idActProd),
 (N'Toban 200 mg tableta',N'Medicamento oral - Toban 200 mg tableta',@MedicVO,@tipoFijo,N'tabl',2.00,10,5,@idActProd),
 (N'Panadol 500 mg tableta',N'Medicamento oral - Panadol 500 mg tableta',@MedicVO,@tipoFijo,N'tabl',2.00,20,5,@idActProd),
 (N'Buscapina 10 mg tableta',N'Medicamento oral - Buscapina 10 mg tableta',@MedicVO,@tipoFijo,N'tabl',2.00,12,5,@idActProd),
 (N'Dicloxacilina 500 mg cápsula',N'Medicamento oral - Dicloxacilina 500 mg cápsula',@MedicVO,@tipoFijo,N'cap',1.50,5,2,@idActProd),
 (N'Clorfenamina 4 mg tableta',N'Medicamento oral - Clorfenamina 4 mg tableta',@MedicVO,@tipoFijo,N'tabl',0.50,10,5,@idActProd),
 (N'Plidan compuesta tableta',N'Medicamento oral - Plidan compuesta tableta',@MedicVO,@tipoFijo,N'tabl',2.50,10,5,@idActProd),
 (N'Prednisona 5 mg tableta',N'Medicamento oral - Prednisona 5 mg tableta',@MedicVO,@tipoFijo,N'tabl',1.00,10,5,@idActProd),
 (N'DayFlu cápsula',N'Medicamento oral - DayFlu cápsula',@MedicVO,@tipoFijo,N'cap',3.00,10,5,@idActProd),
 (N'Dexametasona 4 mg tableta',N'Medicamento oral - Dexametasona 4 mg tableta',@MedicVO,@tipoFijo,N'tabl',1.00,20,5,@idActProd),
 (N'Ampicilina 500 mg cápsula',N'Medicamento oral - Ampicilina 500 mg cápsula',@MedicVO,@tipoFijo,N'cap',1.50,40,5,@idActProd),
 (N'Apronax 550 mg tableta',N'Medicamento oral - Apronax 550 mg tableta',@MedicVO,@tipoFijo,N'tabl',2.50,12,5,@idActProd),
 (N'Piridium 100 mg tableta',N'Medicamento oral - Piridium 100 mg tableta',@MedicVO,@tipoFijo,N'tabl',1.50,50,5,@idActProd),
 (N'Ketorolaco 10 mg tableta',N'Medicamento oral - Ketorolaco 10 mg tableta',@MedicVO,@tipoFijo,N'tabl',1.50,10,5,@idActProd),
 (N'Urodixil cápsula',N'Medicamento oral - Urodixil cápsula',@MedicVO,@tipoFijo,N'cap',2.00,15,5,@idActProd),
 (N'Dolocordralan 500 mg tableta',N'Medicamento oral - Dolocordralan 500 mg tableta',@MedicVO,@tipoFijo,N'tabl',1.50,10,5,@idActProd),
 (N'Eritromicina 500 mg cápsula',N'Medicamento oral - Eritromicina 500 mg cápsula',@MedicVO,@tipoFijo,N'cap',2.00,16,5,@idActProd),
 (N'Mejoral 500 mg tableta',N'Medicamento oral - Mejoral 500 mg tableta',@MedicVO,@tipoFijo,N'tabl',1.00,10,5,@idActProd),
 (N'Amoxicilina 500 mg cápsula',N'Medicamento oral - Amoxicilina 500 mg cápsula',@MedicVO,@tipoFijo,N'cap',1.00,10,5,@idActProd),
 (N'Cloranfenicol 500 mg cápsula',N'Medicamento oral - Cloranfenicol 500 mg cápsula',@MedicVO,@tipoFijo,N'cap',1.50,10,5,@idActProd),
 (N'Clofenamina 4 mg tableta',N'Medicamento oral - Clofenamina 4 mg tableta',@MedicVO,@tipoFijo,N'tabl',0.50,50,5,@idActProd),
 (N'Tramedil 1 mg tableta',N'Medicamento oral - Tramedil 1 mg tableta',@MedicVO,@tipoFijo,N'tabl',5.50,10,5,@idActProd),
 (N'Vimil 75 mg cápsula',N'Medicamento oral - Vimil 75 mg cápsula',@MedicVO,@tipoFijo,N'cap',4.50,6,2,@idActProd),
 (N'Vitacerebrina tónico 100 mL',N'Medicamento oral - Vitacerebrina tónico 100 mL',@MedicVO,@tipoFijo,N'fr',2.50,7,2,@idActProd),
 (N'Dolo-Quimagésico 75 mg tableta',N'Medicamento oral - Dolo-Quimagésico 75 mg tableta',@MedicVO,@tipoFijo,N'tabl',9.00,3,1,@idActProd),
 (N'Broncopar Plus jarabe 120 mL',N'Medicamento oral - Broncopar Plus jarabe 120 mL',@MedicVO,@tipoFijo,N'fr',1.50,18,3,@idActProd),
 (N'Gotero Visine 15 mL',N'Medicamento oral - Gotero Visine 15 mL',@MedicVO,@tipoFijo,N'fr',25.00,3,1,@idActProd),
 (N'Jarabe Repriman 120 mL',N'Medicamento oral - Jarabe Repriman 120 mL',@MedicVO,@tipoFijo,N'fr',17.00,8,2,@idActProd),
 (N'Leche de Magnesia Phillips 350 mL',N'Medicamento oral - Leche de Magnesia Phillips 350 mL',@MedicVO,@tipoFijo,N'fr',13.00,5,1,@idActProd),
 (N'Mentol Sikura pomada 30 g',N'Medicamento oral - Mentol Sikura pomada 30 g',@MedicVO,@tipoFijo,N'tub',4.50,30,5,@idActProd),
 (N'Mentol Benzo Derma pomada 30 g',N'Medicamento oral - Mentol Benzo Derma pomada 30 g',@MedicVO,@tipoFijo,N'tub',3.50,20,4,@idActProd),
 (N'Mentol Vaporal pomada 30 g',N'Medicamento oral - Mentol Vaporal pomada 30 g',@MedicVO,@tipoFijo,N'tub',3.00,20,4,@idActProd),
 (N'Pomada Chuchuguaza 20 g',N'Medicamento oral - Pomada Chuchuguaza 20 g',@MedicVO,@tipoFijo,N'tub',2.50,6,2,@idActProd);
PRINT N'>>> BLOQUE 4 cargado (35 productos)';

-- BLOQUE 5: MEDICAMENTOS INYECTABLES E INSUMOS (13 ítems)
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Diclofenaco 75 mg ampolla',N'Medicamento inyectable - Diclofenaco 75 mg ampolla',@MedicInyectables,@tipoFijo,N'amp',4.00,12,2,@idActProd),
 (N'Complejo B Forte 10 mL ampolla',N'Medicamento inyectable - Complejo B Forte 10 mL ampolla',@MedicInyectables,@tipoFijo,N'amp',25.00,5,1,@idActProd),
 (N'Betametasona 4 mg ampolla',N'Medicamento inyectable - Betametasona 4 mg ampolla',@MedicInyectables,@tipoFijo,N'amp',3.50,3,1,@idActProd),
 (N'Agua estéril p/ inyección 5 mL',N'Medicamento inyectable - Agua estéril p/ inyección 5 mL',@MedicInyectables,@tipoFijo,N'amp',1.50,12,2,@idActProd),
 (N'Ameurin B12 2 mL ampolla',N'Medicamento inyectable - Ameurin B12 2 mL ampolla',@MedicInyectables,@tipoFijo,N'amp',3.50,10,2,@idActProd),
 (N'Fleveral 2 mL ampolla',N'Medicamento inyectable - Fleveral 2 mL ampolla',@MedicInyectables,@tipoFijo,N'amp',4.50,15,2,@idActProd),
 (N'Diclofix 3 mL ampolla',N'Medicamento inyectable - Diclofix 3 mL ampolla',@MedicInyectables,@tipoFijo,N'amp',4.80,7,2,@idActProd),
 (N'Cemin 500 mg ampolla',N'Medicamento inyectable - Cemin 500 mg ampolla',@MedicInyectables,@tipoFijo,N'amp',1.50,5,1,@idActProd),
 (N'Ramimel 50 mg ampolla',N'Medicamento inyectable - Ramimel 50 mg ampolla',@MedicInyectables,@tipoFijo,N'amp',4.50,10,2,@idActProd),
 (N'Suero cloruro de sodio 0.9% 500 mL',N'Medicamento inyectable - Suero cloruro de sodio 0.9% 500 mL',@MedicInyectables,@tipoFijo,N'amp',5.00,10,2,@idActProd),
 (N'Jeringa + aguja 21G x 1½" (pack 50)',N'Insumo medico - Jeringa + aguja 21G x 1½" (pack 50)',@InsumosMedicos,@tipoFijo,N'paq',0.50,50,5,@idActProd),
 (N'Jeringa descartable 10 mL',N'Insumo medico - Jeringa descartable 10 mL',@InsumosMedicos,@tipoFijo,N'unid',1.00,20,5,@idActProd),
 (N'Algodón Corona 25 g',N'Insumo medico - Algodón Corona 25 g',@InsumosMedicos,@tipoFijo,N'paq',1.50,10,3,@idActProd);
PRINT N'>>> BLOQUE 5 cargado (13 ítems)';

-- BLOQUE 6: FERRETERÍA, DEPORTE Y ACCESORIOS (6 ítems)
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Balón de fútbol sintético',N'Articulo deportivo - Balón de fútbol sintético',@Deportes,@tipoFijo,N'unid',75.00,7,2,@idActProd),
 (N'Balón de fútbol de cuero',N'Articulo deportivo - Balón de fútbol de cuero',@Deportes,@tipoFijo,N'unid',60.00,5,2,@idActProd),
 (N'Paraguas estándar',N'Accesorio personal - Paraguas estándar',@Accesorios,@tipoFijo,N'unid',25.00,5,1,@idActProd),
 (N'Hoz agrícola',N'Herramienta de ferreteria - Hoz agrícola',@Ferreteria,@tipoFijo,N'unid',22.00,10,2,@idActProd),
 (N'Machete Tramontina "Gavilán"',N'Herramienta de ferreteria - Machete Tramontina "Gavilán"',@Ferreteria,@tipoFijo,N'unid',25.00,24,3,@idActProd),
 (N'Barreta 1,5 m',N'Herramienta de ferreteria - Barreta 1,5 m',@Ferreteria,@tipoFijo,N'unid',75.00,10,2,@idActProd);
PRINT N'>>> BLOQUE 6 cargado (6 ítems)';

-- BLOQUE 7: MEDICINA VETERINARIA (6 ítems)
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Biomisoma Dorada L.A. 100 mL',N'Medicina veterinaria - Biomisoma Dorada L.A. 100 mL',@Veterinaria,@tipoFijo,N'fr',50.00,3,1,@idActProd),
 (N'Biomisoma Dorada L.A. 50 mL',N'Medicina veterinaria - Biomisoma Dorada L.A. 50 mL',@Veterinaria,@tipoFijo,N'fr',35.00,5,1,@idActProd),
 (N'Aceite alcanforado etéreo 100 mL',N'Medicina veterinaria - Aceite alcanforado etéreo 100 mL',@Veterinaria,@tipoFijo,N'fr',35.00,3,1,@idActProd),
 (N'Ispersic 1.3 L.A. 500 mL',N'Medicina veterinaria - Ispersic 1.3 L.A. 500 mL',@Veterinaria,@tipoFijo,N'fr',28.00,6,1,@idActProd),
 (N'Zerobichos 10 mL',N'Medicina veterinaria - Zerobichos 10 mL',@Veterinaria,@tipoFijo,N'fr',6.00,2,1,@idActProd),
 (N'Immotrin 100 mL',N'Medicina veterinaria - Immotrin 100 mL',@Veterinaria,@tipoFijo,N'fr',12.00,3,1,@idActProd);
PRINT N'>>> BLOQUE 7 cargado (6 ítems)';

-- BLOQUE 8: LIMPIEZA, HOGAR E HIGIENE (18 ítems)
-- LIMPIEZA & HOGAR
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Sapolio multiuso 360 mL',N'Producto de limpieza - Sapolio multiuso 360 mL',@Limpieza,@tipoFijo,N'fr',8.00,12,2,@idActProd),
 (N'Jabón "Lavasorion" barra',N'Producto de limpieza - Jabón "Lavasorion" barra',@Limpieza,@tipoFijo,N'bar',2.50,20,5,@idActProd),
 (N'Papel higiénico Noble – "La Pareja"',N'Articulo para el hogar - Papel higiénico Noble – "La Pareja"',@Hogar,@tipoFijo,N'rol',2.00,30,5,@idActProd),
 (N'Papel higiénico Noble Plus (pack 6)',N'Articulo para el hogar - Papel higiénico Noble Plus (pack 6)',@Hogar,@tipoFijo,N'paq',6.00,20,5,@idActProd),
 (N'Pilas Panasonic AA 2 uds',N'Articulo para el hogar - Pilas Panasonic AA 2 uds',@Hogar,@tipoFijo,N'blí',2.00,30,5,@idActProd),
 (N'Pilas Panasonic AAA 2 uds',N'Articulo para el hogar - Pilas Panasonic AAA 2 uds',@Hogar,@tipoFijo,N'blí',4.50,20,5,@idActProd),
 (N'Paquete cajas de fósforos "Inti" (10)',N'Articulo para el hogar - Paquete cajas de fósforos "Inti" (10)',@Hogar,@tipoFijo,N'paq',13.00,10,2,@idActProd);
-- HIGIENE PERSONAL
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Champú Suavitel 38 mL',N'Cuidado personal - Champú Suavitel 38 mL',@HigienePersonal,@tipoFijo,N'sob',1.50,12,3,@idActProd),
 (N'Enjuague Bolívar 60 mL',N'Cuidado personal - Enjuague Bolívar 60 mL',@HigienePersonal,@tipoFijo,N'sob',1.00,12,3,@idActProd),
 (N'Champú Head & Shoulders 10 mL',N'Cuidado personal - Champú Head & Shoulders 10 mL',@HigienePersonal,@tipoFijo,N'sob',1.20,12,3,@idActProd),
 (N'Champú Pantene 18 mL',N'Cuidado personal - Champú Pantene 18 mL',@HigienePersonal,@tipoFijo,N'sob',1.00,12,3,@idActProd),
 (N'Desodorante Rexona hombre 25 mL',N'Cuidado personal - Desodorante Rexona hombre 25 mL',@HigienePersonal,@tipoFijo,N'fr',1.50,12,3,@idActProd),
 (N'Colgate 150 mL',N'Cuidado personal - Colgate 150 mL',@HigienePersonal,@tipoFijo,N'tub',10.00,12,5,@idActProd),
 (N'Colgate 75 mL',N'Cuidado personal - Colgate 75 mL',@HigienePersonal,@tipoFijo,N'tub',6.00,8,2,@idActProd),
 (N'Kolinos 75 mL',N'Cuidado personal - Kolinos 75 mL',@HigienePersonal,@tipoFijo,N'tub',8.00,20,5,@idActProd),
 (N'Sapolio lavavajillas 450 mL',N'Producto de limpieza - Sapolio lavavajillas 450 mL',@Limpieza,@tipoFijo,N'fr',8.00,12,2,@idActProd),
 (N'Lejía San Miguel 130 mL',N'Producto de limpieza - Lejía San Miguel 130 mL',@Limpieza,@tipoFijo,N'fr',0.50,50,5,@idActProd),
 (N'Lavavajilla líquido Lesly 750 mL',N'Producto de limpieza - Lavavajilla líquido Lesly 750 mL',@Limpieza,@tipoFijo,N'fr',8.00,12,2,@idActProd),
 (N'Clorox 638 mL',N'Producto de limpieza - Clorox 638 mL',@Limpieza,@tipoFijo,N'fr',2.50,15,5,@idActProd);
PRINT N'>>> BLOQUE 8 cargado (18 ítems)';

-- BLOQUE 9: DESPENSA Y SNACKS (20 ítems)
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Atún Pimentel 170 g',N'Producto alimenticio - Atún Pimentel 170 g',@Despensa,@tipoFijo,N'lata',3.50,20,5,@idActProd),
 (N'Fideos Marco Polo (bolsa 500 g)',N'Producto alimenticio - Fideos Marco Polo (bolsa 500 g)',@Despensa,@tipoFijo,N'paq',4.50,20,5,@idActProd),
 (N'Fideos canuto Alianza 235 g',N'Producto alimenticio - Fideos canuto Alianza 235 g',@Despensa,@tipoFijo,N'paq',1.50,15,5,@idActProd),
 (N'Galletas Soda',N'Snack o dulce - Galletas Soda',@Snacks,@tipoFijo,N'unid',0.50,30,5,@idActProd),
 (N'Galletas Rellenita',N'Snack o dulce - Galletas Rellenita',@Snacks,@tipoFijo,N'unid',0.50,16,5,@idActProd),
 (N'Galleta Saltica',N'Snack o dulce - Galleta Saltica',@Snacks,@tipoFijo,N'unid',1.50,20,5,@idActProd),
 (N'Galleta Wafer grande',N'Snack o dulce - Galleta Wafer grande',@Snacks,@tipoFijo,N'unid',1.50,18,5,@idActProd),
 (N'Chupete Globo-Pop',N'Snack o dulce - Chupete Globo-Pop',@Snacks,@tipoFijo,N'unid',0.50,24,5,@idActProd),
 (N'Sibarita 32 g sobre',N'Producto alimenticio - Sibarita 32 g sobre',@Despensa,@tipoFijo,N'sob',1.00,42,5,@idActProd),
 (N'Té de canela y clavo (bolsa)',N'Producto alimenticio - Té de canela y clavo (bolsa)',@Despensa,@tipoFijo,N'unid',0.10,100,10,@idActProd),
 (N'Vinagre tinto 1 L',N'Producto alimenticio - Vinagre tinto 1 L',@Despensa,@tipoFijo,N'bot',12.00,12,2,@idActProd),
 (N'Sillao 1 L',N'Producto alimenticio - Sillao 1 L',@Despensa,@tipoFijo,N'bot',12.00,12,2,@idActProd),
 (N'Chicles Chiclets',N'Snack o dulce - Chicles Chiclets',@Snacks,@tipoFijo,N'unid',0.30,40,5,@idActProd),
 (N'Chicles Boogie',N'Snack o dulce - Chicles Boogie',@Snacks,@tipoFijo,N'unid',0.20,40,5,@idActProd),
 (N'Azúcar blanca paquete 1 kg',N'Producto alimenticio - Azúcar blanca paquete 1 kg',@Despensa,@tipoFijo,N'kg',3.50,2,1,@idActProd),
 (N'Harina sin preparar paquete 1 kg',N'Producto alimenticio - Harina sin preparar paquete 1 kg',@Despensa,@tipoFijo,N'kg',4.00,2,1,@idActProd),
 (N'Escoba "Lorito"',N'Producto de limpieza - Escoba "Lorito"',@Limpieza,@tipoFijo,N'unid',12.00,5,2,@idActProd),
 (N'Ace detergente Marcella 730 mL',N'Producto de limpieza - Ace detergente Marcella 730 mL',@Limpieza,@tipoFijo,N'fr',9.00,12,2,@idActProd),
 (N'Ace detergente Patito 150 mL',N'Producto de limpieza - Ace detergente Patito 150 mL',@Limpieza,@tipoFijo,N'fr',1.30,8,2,@idActProd),
 (N'Atún Cardinal 170 g',N'Producto alimenticio - Atún Cardinal 170 g',@Despensa,@tipoFijo,N'lata',4.00,20,5,@idActProd),
 (N'Leche Gloria evaporada 390 g',N'Producto alimenticio - Leche Gloria evaporada 390 g',@Despensa,@tipoFijo,N'lata',4.50,10,3,@idActProd),
 (N'Leche Gloria evaporada 170 g',N'Producto alimenticio - Leche Gloria evaporada 170 g',@Despensa,@tipoFijo,N'lata',2.50,10,3,@idActProd),
 (N'Sal marina 1 kg (paquete)',N'Producto alimenticio - Sal marina 1 kg (paquete)',@Despensa,@tipoFijo,N'kg',1.00,15,3,@idActProd),
 (N'Aceite Costa Rey 1 L',N'Producto alimenticio - Aceite Costa Rey 1 L',@Despensa,@tipoFijo,N'bot',10.00,8,2,@idActProd);
PRINT N'>>> BLOQUE 9 cargado (20 ítems)';

-- BLOQUE 10: HILOS (49 registros)
 -- Ovillos S/6
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,mayorista,minMayorista,precioMayorista,paraPedido,tipoPedidoDefault,stockActual,umbral,idEstado) VALUES
 (N'Ovillo de hilo - Amarillo oro',N'Material de costura - Ovillo de hilo - Amarillo oro',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Verde',N'Material de costura - Ovillo de hilo - Verde',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Celeste',N'Material de costura - Ovillo de hilo - Celeste',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Negro',N'Material de costura - Ovillo de hilo - Negro',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Azul',N'Material de costura - Ovillo de hilo - Azul',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Amarillo patito',N'Material de costura - Ovillo de hilo - Amarillo patito',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Celeste turquesa',N'Material de costura - Ovillo de hilo - Celeste turquesa',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Amarillo bebe',N'Material de costura - Ovillo de hilo - Amarillo bebe',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Celeste bebe',N'Material de costura - Ovillo de hilo - Celeste bebe',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Verde bebe',N'Material de costura - Ovillo de hilo - Verde bebe',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Rosado bebe',N'Material de costura - Ovillo de hilo - Rosado bebe',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Morado',N'Material de costura - Ovillo de hilo - Morado',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Verde mosca',N'Material de costura - Ovillo de hilo - Verde mosca',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Verde periquillo',N'Material de costura - Ovillo de hilo - Verde periquillo',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Verde fluorescente',N'Material de costura - Ovillo de hilo - Verde fluorescente',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Blanco',N'Material de costura - Ovillo de hilo - Blanco',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',16,10,@idActProd),
 (N'Ovillo de hilo - Rojo',N'Material de costura - Ovillo de hilo - Rojo',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Rosado',N'Material de costura - Ovillo de hilo - Rosado',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Marrón',N'Material de costura - Ovillo de hilo - Marrón',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Color sandía',N'Material de costura - Ovillo de hilo - Color sandía',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Morado bebe',N'Material de costura - Ovillo de hilo - Morado bebe',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Rosado fucsia',N'Material de costura - Ovillo de hilo - Rosado fucsia',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd),
 (N'Ovillo de hilo - Color rata',N'Material de costura - Ovillo de hilo - Color rata',@Merceria,@tipoFijo,N'unid',6.00,1,@minMayoristaHilo,4,1,N'Especial',11,10,@idActProd);
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 -- Madejas S/4
 (N'Madeja de hilo - Amarillo oro',N'Material de costura - Madeja de hilo - Amarillo oro',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Verde',N'Material de costura - Madeja de hilo - Verde',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Celeste',N'Material de costura - Madeja de hilo - Celeste',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Negro',N'Material de costura - Madeja de hilo - Negro',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Azul',N'Material de costura - Madeja de hilo - Azul',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Amarillo patito',N'Material de costura - Madeja de hilo - Amarillo patito',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Celeste turquesa',N'Material de costura - Madeja de hilo - Celeste turquesa',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Amarillo bebe',N'Material de costura - Madeja de hilo - Amarillo bebe',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Celeste bebe',N'Material de costura - Madeja de hilo - Celeste bebe',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Verde bebe',N'Material de costura - Madeja de hilo - Verde bebe',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Rosado bebe',N'Material de costura - Madeja de hilo - Rosado bebe',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Morado',N'Material de costura - Madeja de hilo - Morado',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Verde mosca',N'Material de costura - Madeja de hilo - Verde mosca',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Verde periquillo',N'Material de costura - Madeja de hilo - Verde periquillo',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Verde fluorescente',N'Material de costura - Madeja de hilo - Verde fluorescente',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Blanco',N'Material de costura - Madeja de hilo - Blanco',@Merceria,@tipoFijo,N'unid',4.00,16,10,@idActProd),
 (N'Madeja de hilo - Rojo',N'Material de costura - Madeja de hilo - Rojo',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Rosado',N'Material de costura - Madeja de hilo - Rosado',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Marrón',N'Material de costura - Madeja de hilo - Marrón',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Color sandía',N'Material de costura - Madeja de hilo - Color sandía',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Morado bebe',N'Material de costura - Madeja de hilo - Morado bebe',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Rosado fucsia',N'Material de costura - Madeja de hilo - Rosado fucsia',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 (N'Madeja de hilo - Color rata',N'Material de costura - Madeja de hilo - Color rata',@Merceria,@tipoFijo,N'unid',4.00,11,10,@idActProd),
 -- Otros formatos
 (N'Hilo Perlita 100 g',N'Material de costura - Hilo Perlita 100 g',@Merceria,@tipoFijo,N'roll',4.00,40,10,@idActProd),
 (N'Hilo Silvita (cono)',N'Material de costura - Hilo Silvita (cono)',@Merceria,@tipoFijo,N'cono',8.00,70,10,@idActProd),
 (N'Ovillo Perlita (ovillo 25 g)',N'Material de costura - Ovillo Perlita (ovillo 25 g)',@Merceria,@tipoFijo,N'unid',4.00,150,15,@idActProd);
PRINT N'>>> BLOQUE 10 cargado (49 registros)';

-- BLOQUE 11: CALZADO (13 productos con tallas)
DECLARE @tipoVest  INT=(SELECT idTipoProducto FROM dbo.TipoProducto WHERE nombre=N'Vestimenta');
DECLARE @prodID INT;
-- Zapatilla Goloflex 30-35
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Zapatilla Goloflex',N'Calzado - Zapatilla Goloflex',@Calzado,@tipoVest,N'par',35.00,44,5,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'30',11),(@prodID,N'31',6),(@prodID,N'32',3),(@prodID,N'33',12),(@prodID,N'34',6),(@prodID,N'35',6);
-- Zapatilla Goloflex Pro 36-42
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Zapatilla Goloflex Pro',N'Calzado - Zapatilla Goloflex Pro',@Calzado,@tipoVest,N'par',40.00,42,5,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'36',4),(@prodID,N'37',5),(@prodID,N'38',6),(@prodID,N'39',6),(@prodID,N'40',6),(@prodID,N'41',9),(@prodID,N'42',6);
-- Chimpunes 37-42
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Chimpunes',N'Calzado - Chimpunes',@Calzado,@tipoVest,N'par',40.00,40,5,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'37',5),(@prodID,N'38',7),(@prodID,N'39',4),(@prodID,N'40',7),(@prodID,N'41',9),(@prodID,N'42',8);
-- Zapato escolar Hombre 37-42
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Zapato escolar – Hombre',N'Calzado - Zapato escolar – Hombre',@Calzado,@tipoVest,N'par',75.00,17,3,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'37',1),(@prodID,N'38',1),(@prodID,N'39',3),(@prodID,N'40',3),(@prodID,N'41',6),(@prodID,N'42',3);
-- Zapato escolar Mujer 36-40
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Zapato escolar – Mujer',N'Calzado - Zapato escolar – Mujer',@Calzado,@tipoVest,N'par',45.00,20,3,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'36',2),(@prodID,N'37',2),(@prodID,N'38',3),(@prodID,N'39',5),(@prodID,N'40',8);
-- Zapato escolar Niña 31-35
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Zapato escolar – Niña',N'Calzado - Zapato escolar – Niña',@Calzado,@tipoVest,N'par',35.00,18,3,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'31',7),(@prodID,N'32',3),(@prodID,N'33',2),(@prodID,N'34',2),(@prodID,N'35',4);
-- Chancletas 35-42
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Chancletas playa',N'Calzado - Chancletas playa',@Calzado,@tipoVest,N'par',18.00,30,5,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'35',1),(@prodID,N'36',4),(@prodID,N'37',4),(@prodID,N'38',3),(@prodID,N'39',9),(@prodID,N'40',3),(@prodID,N'41',3),(@prodID,N'42',3);
-- Sandalia Cuofu Hombre 36-44
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Sandalia Cuofu – Hombre',N'Calzado - Sandalia Cuofu – Hombre',@Calzado,@tipoVest,N'par',25.00,22,4,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'36',5),(@prodID,N'37',0),(@prodID,N'38',2),(@prodID,N'39',3),(@prodID,N'40',1),(@prodID,N'41',1),(@prodID,N'42',4),(@prodID,N'43',2),(@prodID,N'44',4);
-- Sandalia Cuofu Mujer 36-44
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Sandalia Cuofu – Mujer',N'Calzado - Sandalia Cuofu – Mujer',@Calzado,@tipoVest,N'par',25.00,30,4,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'36',4),(@prodID,N'37',4),(@prodID,N'38',4),(@prodID,N'39',5),(@prodID,N'40',4),(@prodID,N'41',1),(@prodID,N'42',4),(@prodID,N'43',2),(@prodID,N'44',2);
-- Sandalia Croos Hombre 37-41
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Sandalia Croos – Hombre',N'Calzado - Sandalia Croos – Hombre',@Calzado,@tipoVest,N'par',25.00,10,2,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'37',2),(@prodID,N'38',1),(@prodID,N'39',3),(@prodID,N'40',2),(@prodID,N'41',2);
-- Sandalia Croos Niño 32-36
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Sandalia Croos – Niño',N'Calzado - Sandalia Croos – Niño',@Calzado,@tipoVest,N'par',20.00,4,1,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'32',0),(@prodID,N'33',2),(@prodID,N'34',1),(@prodID,N'35',0),(@prodID,N'36',1);
-- Sandalia Croos Niña 32-36
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Sandalia Croos – Niña',N'Calzado - Sandalia Croos – Niña',@Calzado,@tipoVest,N'par',20.00,3,1,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'32',0),(@prodID,N'33',1),(@prodID,N'34',1),(@prodID,N'35',1),(@prodID,N'36',0);
-- Bota Venus 38-42
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Bota Venus',N'Calzado - Bota Venus',@Calzado,@tipoVest,N'par',38.00,15,3,@idActProd);
SET @prodID=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@prodID,N'38',4),(@prodID,N'39',4),(@prodID,N'40',2),(@prodID,N'41',2),(@prodID,N'42',3);
PRINT N'>>> BLOQUE 11 cargado (13 productos)';

-- BLOQUE 12: VESTIMENTA Y ACCESORIOS (41 productos)
DECLARE @p INT;
-- Camisa Van Hutch (niños) 4-6
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Camisa m/l blanca Van Hutch (niños)',N'Prenda de vestir - Camisa m/l blanca Van Hutch (niños)',@Vestimenta,@tipoVest,N'prenda',25.00,5,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'4',3),(@p,N'6',2);
-- Camisa Van Hutch 28-32
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Camisa m/l blanca Van Hutch',N'Prenda de vestir - Camisa m/l blanca Van Hutch',@Vestimenta,@tipoVest,N'prenda',28.00,3,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'28',1),(@p,N'32',2);
-- Camisa escolar Malca M
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Camisa escolar Malca',N'Prenda de vestir - Camisa escolar Malca',@Vestimenta,@tipoVest,N'prenda',25.00,10,2,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'M',10);
-- Pantalón escolar Romy Giardini 14-28
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Pantalón escolar Romy Giardini',N'Prenda de vestir - Pantalón escolar Romy Giardini',@Vestimenta,@tipoVest,N'prenda',60.00,10,2,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'14',5),(@p,N'28',5);
-- Pantalón jean niño Tarso 8
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Pantalón jean niño Tarso',N'Prenda de vestir - Pantalón jean niño Tarso',@Vestimenta,@tipoVest,N'prenda',38.00,5,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'8',5);
-- Pantalón jean hombre Gedwar 28-30-32
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Pantalón jean hombre Gedwar',N'Prenda de vestir - Pantalón jean hombre Gedwar',@Vestimenta,@tipoVest,N'prenda',75.00,15,3,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'28',5),(@p,N'30',5),(@p,N'32',5);
-- Polo Rif Radical M-L
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Polo Rif Radical',N'Prenda de vestir - Polo Rif Radical',@Vestimenta,@tipoVest,N'prenda',35.00,6,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'M',3),(@p,N'L',3);
-- Camisa hombre Mirbal S
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Camisa hombre Mirbal',N'Prenda de vestir - Camisa hombre Mirbal',@Vestimenta,@tipoVest,N'prenda',38.00,5,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'S',5);
-- Camisa hombre R. Ugarte L
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Camisa hombre R. Ugarte',N'Prenda de vestir - Camisa hombre R. Ugarte',@Vestimenta,@tipoVest,N'prenda',38.00,4,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'L',4);
-- Polo Galantʼs 14-16-M
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Polo Galantʼs',N'Prenda de vestir - Polo Galantʼs',@Vestimenta,@tipoVest,N'prenda',26.00,25,3,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'14',8),(@p,N'16',8),(@p,N'M',9);
-- Sudadera Reebok M
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Sudadera Reebok',N'Prenda de vestir - Sudadera Reebok',@Vestimenta,@tipoVest,N'prenda',35.00,5,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'M',5);
-- Sudadera Fila S
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Sudadera Fila',N'Prenda de vestir - Sudadera Fila',@Vestimenta,@tipoVest,N'prenda',48.00,4,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'S',4);
-- Polo niño básico 10-12
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Polo niño básico',N'Prenda de vestir - Polo niño básico',@Vestimenta,@tipoVest,N'prenda',10.00,10,2,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'10',4),(@p,N'12',6);
-- Pantalón Redford 32
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Pantalón Redford',N'Prenda de vestir - Pantalón Redford',@Vestimenta,@tipoVest,N'prenda',65.00,4,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'32',4);
-- Jean mujer Lia 26-28
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Jean mujer Lia',N'Prenda de vestir - Jean mujer Lia',@Vestimenta,@tipoVest,N'prenda',22.00,4,1,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'26',2),(@p,N'28',2);
-- Falda escolar Ceytex 6-8-12-19
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Falda escolar Ceytex',N'Prenda de vestir - Falda escolar Ceytex',@Vestimenta,@tipoVest,N'prenda',32.00,10,2,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'6',3),(@p,N'8',2),(@p,N'12',3),(@p,N'19',2);
-- Medias escolares Prayci 10
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Media escolar Prayci',N'Prenda de vestir - Media escolar Prayci',@Vestimenta,@tipoVest,N'par',12.00,12,2,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'10',12);
-- Medias niño Sports 9-11
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Media niño Sports',N'Prenda de vestir - Media niño Sports',@Vestimenta,@tipoVest,N'par',35.00,15,2,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'9-11',15);
-- Tobillera deportiva 39-42
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Tobillera deportiva',N'Prenda de vestir - Tobillera deportiva',@Vestimenta,@tipoVest,N'par',10.00,30,3,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'39-42',30);
-- Ropa interior niño Kids 10
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Ropa interior niño Kids',N'Prenda de vestir - Ropa interior niño Kids',@Vestimenta,@tipoVest,N'prenda',3.50,15,3,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'10',15);
-- Ropa interior niño PRIVEX 12
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Ropa interior niño PRIVEX',N'Prenda de vestir - Ropa interior niño PRIVEX',@Vestimenta,@tipoVest,N'prenda',5.00,20,3,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'12',20);
-- Boxer hombre CK S-M
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Boxer hombre Calvin Klein',N'Prenda de vestir - Boxer hombre Calvin Klein',@Vestimenta,@tipoVest,N'prenda',10.00,20,3,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'S',10),(@p,N'M',10);
-- Conjuntos bebé Gomes
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Conjunto bebé Gomes (H)',N'Prenda de vestir - Conjunto bebé Gomes (H)',@Vestimenta,@tipoVest,N'set',20.00,5,1,@idActProd);
SET @p = SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'H',5);
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Conjunto bebé Gomes (M)',N'Prenda de vestir - Conjunto bebé Gomes (M)',@Vestimenta,@tipoVest,N'set',20.00,7,1,@idActProd);
SET @p = SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'M',7);
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Blusa de mujer básica',N'Prenda de vestir - Blusa de mujer básica',@Vestimenta,@tipoFijo,N'prenda',25.00,10,2,@idActProd);
-- Textil hogar sin talla
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Frazada bebé Baby Beby',N'Ropa de hogar - Frazada bebé Baby Beby',@TextilHogar,@tipoFijo,N'unid',45.00,5,1,@idActProd);
-- Sostén mujer Condeza Erika 36C-44C
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Sostén mujer Condeza Erika',N'Prenda de vestir - Sostén mujer Condeza Erika',@Vestimenta,@tipoVest,N'prenda',25.00,20,4,@idActProd);
SET @p=SCOPE_IDENTITY();
INSERT dbo.TallaStock(idProducto,talla,stock) VALUES (@p,N'36C',4),(@p,N'38C',4),(@p,N'40C',4),(@p,N'42',4),(@p,N'44C',4);
-- Mochilas, bolsos, toalla y sabana
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Mochila Provex',N'Accesorio personal - Mochila Provex',@Accesorios,@tipoFijo,N'unid',68.00,3,1,@idActProd),
 (N'Mochila Adidas Reh',N'Accesorio personal - Mochila Adidas Reh',@Accesorios,@tipoFijo,N'unid',60.00,2,1,@idActProd),
 (N'Mochila Caterpillar réplica',N'Accesorio personal - Mochila Caterpillar réplica',@Accesorios,@tipoFijo,N'unid',60.00,2,1,@idActProd),
 (N'Mochila niño',N'Accesorio personal - Mochila niño',@Accesorios,@tipoFijo,N'unid',52.00,5,1,@idActProd),
 (N'Mochila niña',N'Accesorio personal - Mochila niña',@Accesorios,@tipoFijo,N'unid',52.00,3,1,@idActProd),
 (N'Toalla algodón premium',N'Ropa de hogar - Toalla algodón premium',@TextilHogar,@tipoFijo,N'unid',28.00,3,1,@idActProd),
 (N'Sábana "Nancy" 2 plazas',N'Ropa de hogar - Sábana "Nancy" 2 plazas',@TextilHogar,@tipoFijo,N'juego',75.00,2,1,@idActProd);
-- Gorras y sombrero
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Gorra hombre Quimber',N'Accesorio personal - Gorra hombre Quimber',@Accesorios,@tipoFijo,N'unid',15.00,10,2,@idActProd),
 (N'Gorra hombre Marcaps',N'Accesorio personal - Gorra hombre Marcaps',@Accesorios,@tipoFijo,N'unid',25.00,15,2,@idActProd),
 (N'Sombrero mujer Mazze',N'Accesorio personal - Sombrero mujer Mazze',@Accesorios,@tipoFijo,N'unid',25.00,5,1,@idActProd);
PRINT N'>>> BLOQUE 12 cargado (41 productos)';

-- BLOQUE 13: FRACCIONABLES Y BEBIDAS (22 ítems)
DECLARE @tipoFrac  INT=(SELECT idTipoProducto FROM dbo.TipoProducto WHERE nombre=N'Fraccionable');
DECLARE @prod INT;
-- Soga multiuso 100 m
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Soga multiuso',N'Herramienta de ferreteria - Soga multiuso',@Ferreteria,@tipoFrac,N'm',3.50,100,10,@idActProd);
SET @prod=SCOPE_IDENTITY();
INSERT dbo.Presentacion(idProducto,cantidad,precio) VALUES (@prod,1,3.50),(@prod,0.5,1.70);
-- Manguera de agua 100 m
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Manguera de agua',N'Herramienta de ferreteria - Manguera de agua',@Ferreteria,@tipoFrac,N'm',2.00,100,10,@idActProd);
SET @prod=SCOPE_IDENTITY();
INSERT dbo.Presentacion(idProducto,cantidad,precio) VALUES (@prod,1,2.00),(@prod,0.5,1.00);
-- Azúcar a granel 50 kg
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES(N'Azúcar blanca a granel',N'Producto alimenticio - Azúcar blanca a granel',@Despensa,@tipoFrac,N'kg',3.60,50,10,@idActProd);
SET @prod=SCOPE_IDENTITY();
INSERT dbo.Presentacion(idProducto,cantidad,precio) VALUES (@prod,1,3.60),(@prod,0.5,1.80),(@prod,0.25,0.80);
-- Bebidas unitarias
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,stockActual,umbral,idEstado) VALUES
 (N'Spore 500 mL',N'Bebida refrescante - Spore 500 mL',@Bebidas,@tipoFijo,N'bot',2.50,12,3,@idActProd),
 (N'Agua San Carlos 500 mL',N'Bebida refrescante - Agua San Carlos 500 mL',@Bebidas,@tipoFijo,N'bot',1.00,24,5,@idActProd),
 (N'Volt 500 mL',N'Bebida refrescante - Volt 500 mL',@Bebidas,@tipoFijo,N'bot',2.50,12,3,@idActProd),
 (N'Inka Kola 500 mL',N'Bebida refrescante - Inka Kola 500 mL',@Bebidas,@tipoFijo,N'bot',2.50,15,3,@idActProd),
 (N'Agua San Carlos 3 L',N'Bebida refrescante - Agua San Carlos 3 L',@Bebidas,@tipoFijo,N'bot',3.50,4,1,@idActProd),
 (N'Guaraná 450 mL',N'Bebida refrescante - Guaraná 450 mL',@Bebidas,@tipoFijo,N'bot',2.00,15,3,@idActProd),
 (N'Guaranita 300 mL',N'Bebida refrescante - Guaranita 300 mL',@Bebidas,@tipoFijo,N'bot',1.50,15,3,@idActProd),
 (N'Cifrut 350 mL',N'Bebida refrescante - Cifrut 350 mL',@Bebidas,@tipoFijo,N'bot',1.50,30,5,@idActProd),
 (N'Pepsi 600 mL',N'Bebida refrescante - Pepsi 600 mL',@Bebidas,@tipoFijo,N'bot',2.00,12,3,@idActProd),
 (N'Agua Mía 500 mL',N'Bebida refrescante - Agua Mía 500 mL',@Bebidas,@tipoFijo,N'bot',1.00,15,3,@idActProd),
 (N'Guaraná 3 L',N'Bebida refrescante - Guaraná 3 L',@Bebidas,@tipoFijo,N'bot',10.00,12,3,@idActProd),
 (N'Inka Kola 2,5 L',N'Bebida refrescante - Inka Kola 2,5 L',@Bebidas,@tipoFijo,N'bot',10.00,3,1,@idActProd),
 (N'Cerveza Cristal lata 473 mL',N'Bebida refrescante - Cerveza Cristal lata 473 mL',@Bebidas,@tipoFijo,N'lata',5.50,6,1,@idActProd),
 (N'Ron Cortavío Black 250 mL',N'Bebida refrescante - Ron Cortavío Black 250 mL',@Bebidas,@tipoFijo,N'bot',10.00,10,2,@idActProd),
 (N'Vino Oporto 750 mL',N'Bebida refrescante - Vino Oporto 750 mL',@Bebidas,@tipoFijo,N'bot',15.00,6,1,@idActProd),
 (N'Vino Sauternes 750 mL',N'Bebida refrescante - Vino Sauternes 750 mL',@Bebidas,@tipoFijo,N'bot',15.00,6,1,@idActProd);
INSERT dbo.Producto(nombre,descripcion,idCategoria,idTipoProducto,unidadMedida,precioUnitario,paraPedido,tipoPedidoDefault,stockActual,umbral,idEstado) VALUES
 (N'Agua de bidón 20 L',N'Bebida refrescante - Agua de bidón 20 L',@Bebidas,@tipoFijo,N'bid',7.00,1,N'Domicilio',5,2,@idActProd),
 (N'Balón de gas doméstico 10 kg',N'Articulo para el hogar - Balón de gas doméstico 10 kg',@Hogar,@tipoFijo,N'unid',50.00,1,N'Domicilio',5,2,@idActProd);
END;
    COMMIT;
    EXEC dbo.sp_EnableSeedTriggers;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    EXEC dbo.sp_EnableSeedTriggers;
    EXEC sp_set_session_context N'idEmpleado', NULL;
    THROW;
END CATCH
GO
EXEC sp_set_session_context N'idEmpleado', NULL;
GO
