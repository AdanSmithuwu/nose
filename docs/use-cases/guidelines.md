# Guía para completar la plantilla de caso de uso

## Actores
### Debe incluir
- Roles o sistemas externos que interactúan directamente (Administrador, Empleado, Sistema de Pagos).
### Debe excluir
- Nombres de clases, APIs, tablas o endpoints internos.
- Personas u organismos que no intervienen en el flujo.

## Descripción
### Debe incluir
- Objetivo resumido en una sola frase (“Registrar una venta en el sistema”).
### Debe excluir
- Detalles de UI, algoritmos o estructura de BD.
- Justificación de negocio o antecedentes.

## Evento disparador
### Debe incluir
- Hecho externo que inicia el caso (“El empleado hace clic en ‘Registrar venta’”).
### Debe excluir
- Pasos de procesamiento internos.
- Temporizadores o jobs si el actor no los percibe.

## Precondiciones
### Debe incluir
- Estado del sistema visible/verificable antes de iniciar (“El cliente está creado y activo”).
### Debe excluir
- Configuraciones de servidor, conexión a BD, hilos, colas.
- Referencias a variables técnicas.

## Flujo normal
### Debe incluir
- Pasos numerados: acción del actor ↔ respuesta observable del sistema.
- Validaciones que el actor nota (mensajes de error).
- Datos lógicos que el actor introduce o recibe (“cantidad”, “precio”).
### Debe excluir
- Sentencias SQL, nombres de tablas/columnas.
- Cálculo interno paso a paso, algoritmos, lógica de capas.
- Diseño de pantallas (posiciones, colores, fuentes).

## Flujos alternativos
### Debe incluir
- Desviaciones numeradas del flujo normal con su punto de conexión (“A1: Stock insuficiente en el paso 3”).
### Debe excluir
- Implementación de excepciones a nivel de código.
- Stack traces, logs internos o detalles de infraestructura.

## Postcondiciones
### Debe incluir
- Estado final verificable por el actor o por el negocio (“Venta registrada y stock descontado”).
### Debe excluir
- Operaciones de commit, cierre de sesión de BD.
- Limpieza de memoria, hilos o recursos técnicos.

## Límite general
Todo lo visible o verificable externamente entra en la plantilla; todo lo técnico invisible para el usuario se documenta en diseño, arquitectura o código.
