# UC03 Consultar bitácora de accesos

## Actores
- Administrador

## Descripción
El administrador revisa los intentos de inicio de sesión registrados en el sistema.

## Evento disparador
El administrador abre la opción **Bitácora de Accesos** desde el menú principal.

## Precondiciones
- El administrador se encuentra autenticado en el menú principal.
- Existen registros de bitácora almacenados.
- El sistema está conectado a la base de datos de auditoría.
- La tecla **F3** enfoca el campo **Desde** y **F5** recarga la información.
- Al abrir la pantalla se cargan los empleados disponibles en el filtro.
- Los campos **Desde** y **Hasta** utilizan calendarios emergentes para seleccionar fechas.

## Flujo normal
1. El sistema presenta filtros de **Desde**, **Hasta**, **Empleado** y **Resultado** con la fecha actual y la opción **Todos** preseleccionada.
2. El administrador ajusta el rango usando los calendarios emergentes y elige un empleado o **Todos** para filtrar.
3. El administrador indica si desea ver accesos exitosos, fallidos o ambos.
4. El administrador presiona **Actualizar** (F5).
5. El sistema muestra una superposición de espera mientras consulta la bitácora.
6. El sistema lista los eventos en una tabla con columnas **Fecha**, **Empleado** y **Resultado**, ordenados por fecha.
7. El sistema muestra el número total de registros encontrados debajo de la tabla.
8. El administrador puede ordenar por empleado o resultado para revisar los detalles.
9. El sistema oculta la superposición y mantiene la tabla disponible para nuevos filtros.

## Flujos alternativos
- **A1: No hay registros que cumplan los filtros**
  1. El sistema muestra la leyenda **Sin datos para mostrar**.
- **A2: Rango de fechas inválido en el paso 2**
  1. El sistema advierte **La fecha inicial no puede ser posterior a la final**.
  2. El administrador corrige los valores y vuelve al paso 2.

## Postcondiciones
- La bitácora permanece disponible para nuevas consultas.
