# UC13 Consultar historial de transacciones por cliente

## Actores
- Administrador
- Empleado

## Descripción
Permite revisar las ventas y pedidos asociados a un cliente específico.

## Evento disparador
El actor selecciona un cliente y hace clic en **Historial** (ALT+H).

## Precondiciones
- El cliente existe y está seleccionado en la lista.
- El actor se encuentra autenticado en la sección **Clientes**.
- El buscador puede enfocarse con **F3** y la lista puede refrescarse con **F5**.
- La ventana de historial comienza vacía y muestra **Sin datos para mostrar**.

## Flujo normal
1. El actor selecciona un cliente y presiona **Historial**.
2. El sistema abre la ventana **Historial por Cliente**, enfoca la tabla y muestra una animación de carga mientras obtiene los datos.
3. El sistema lista las transacciones ordenadas por fecha más reciente y habilita el ordenamiento por columna.
4. El sistema indica **Sin datos para mostrar** si el cliente no posee transacciones registradas y mantiene las acciones deshabilitadas.
5. El actor recorre la tabla y selecciona una transacción para consultar sus detalles.
6. El sistema habilita las acciones **Imprimir**, **Descargar**, **Descargar Orden** y **Enviar WhatsApp** para la fila seleccionada.
7. El actor puede ordenar la tabla por cualquier columna y refrescar la información en cualquier momento con **F5**.

## Flujos alternativos
- **A1: El cliente indicado no existe**
  1. El sistema muestra **Cliente no encontrado** y la ventana no se abre.
- **A2: El actor cierra la ventana**
  1. El historial se descarta y el caso de uso termina.

## Postcondiciones
- El historial permanece disponible para nuevas consultas.
