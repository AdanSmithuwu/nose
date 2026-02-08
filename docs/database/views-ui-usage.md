# Vistas y su uso en la aplicación

Esta guía resume cuáles vistas de `db/VW.sql` se muestran directamente en la interfaz de usuario y cuáles funcionan solo como apoyo.

## Vistas presentadas en la UI

Las siguientes vistas se consultan desde los repositorios de lectura y sus datos
se muestran en diferentes pantallas o reportes de la interfaz:

- **vw_ClientesFrecuentes** – alimenta la tabla **Clientes Frecuentes** del
  `FormDashboard`. Los registros provienen del procedimiento
  `sp_ListarClientesFrecuentes` y se mapean con `ClienteFrecuenteEntity`.
- **vw_HistorialTransaccionesPorCliente** – provee el historial completo de
  ventas y pedidos por cliente que se despliega en la ventana **Historial por
  Cliente** (`FormHistorialCliente`), correspondiente al caso de uso UC13. Se
  mapea a `HistorialTransaccionEntity`.
- **vw_ProductosMasVendidos** – lista los cien productos con más unidades
  vendidas e ingresos. Sus datos llenan la tabla **Productos Más Vendidos** del
  `FormDashboard` mediante `ProductoMasVendidoEntity`.
- **vw_TransaccionesPorDia** – expone el resumen diario de montos y número de
  operaciones. Se usa en el `FormReporteDiario` (caso de uso UC42) por medio de
  `TransaccionesDiaEntity` y también lo consulta `sp_GenerarReporteDiario` para
  generar el PDF.
- **vw_PagoMetodoDia** – informa el monto pagado por cada método día a día. El
  `FormReporteDiario` presenta este detalle a través de `PagoMetodoDiaEntity`.
- **vw_ReporteMensualCategoria** – presenta los totales mensuales por categoría
  de producto. Se consulta en el `FormReporteMensual` por medio de
  `ResumenCategoriaEntity` y desde `sp_GenerarReporteMensual`.
- **vw_ResumenMensualModalidad** – resume las ventas y pedidos por modalidad cada
  mes. También se utiliza en `FormReporteMensual` mediante
  `ResumenModalidadEntity` y en `sp_GenerarReporteMensual`.
- **vw_RotacionMensual** y **vw_RotacionRango** – clasifican los productos por
  unidades vendidas mensualmente o en un rango de fechas. Se leen mediante
  `RotacionProductoEntity` para completar el `FormReporteRotacion`.

Estas vistas concentran cálculos y agregaciones que de otra forma deberían realizarse en cada consulta. Al obtener la información ya procesada se agilita la construcción de tablas y gráficos en la UI.

## Vista de soporte

- **vw_ClientesActivos** – devuelve únicamente los `idPersona` cuya persona está en estado **Activo** dentro del módulo `Persona`. No se muestra directamente en pantalla. En la aplicación se mapea a `ClienteActivoEntity` y se emplea como subconsulta en los `NamedQuery` de `ClienteEntity`. Por ejemplo, la consulta `Cliente.activosByNombre` filtra así:

```
WHERE c.idPersona IN (SELECT a.idPersona FROM ClienteActivo a)
```

De esta forma la lógica para determinar qué clientes están habilitados se centraliza en un solo punto, evitando repetir el filtro por estado y manteniendo las entidades de dominio libres de códigos de estado.

## ¿Por qué no se utilizan como tablas directas?

Las vistas reúnen datos de múltiples tablas y realizan cálculos como conteos, sumas o filtros por estado. Exponerlas tal cual en la interfaz implicaría mostrar datos procesados sin control adicional. En cambio, la aplicación accede a ellas mediante repositorios que mapean los resultados a objetos de dominio, aplican validaciones y permiten ajustar las consultas sin alterar la estructura física de las tablas. Así se mantiene la separación entre almacenamiento y lógica de presentación.
