# Diagrama de componentes

Esta guía explica paso a paso cómo modelar en Enterprise Architect los componentes del proyecto Comercial's Valerio.

## Alcance
El diagrama cubre todos los módulos Maven y las dependencias internas necesarias.

## Elementos
A continuación se detalla cada componente con sus subelementos y el estereotipo que debe asignarse.

### Presentación
- Crear un componente **«Presentación»** con el estereotipo `component`.
- Agregar los subcomponentes:
  - **«Controladores»** `component`
  - **«Formularios Swing»** `component`
  - **«Cliente REST»** `component`
  - **«Notificación UI»** `component`
- Todos se agrupan en el módulo `presentation-ui`.

### Aplicación
- Componente **«Aplicación»** `component`.
- Subcomponentes:
  - **«Controladores REST»** `component`
  - **«Servicios de aplicación»** `component`
  - **«Planificador»** `component`
  - **«Seguridad API»** `component`
- Ubicados en el módulo `application`.

### Dominio
- Componente **«Dominio»** `component`.
- Subcomponentes:
  - **«Modelos»** `component`
  - **«Servicios de dominio»** `component`
  - **«Repositorios (interfaces)»** `component`
  - **«Notificaciones de dominio»** `component`
  - **«Seguridad de dominio»** `component`
- Corresponden al código bajo `domain`.

### Infraestructura
- Componente **«Infraestructura»** `component`.
- Subcomponentes:
  - **«Persistencia»** `component`
  - **«Seguridad infra»** `component`
  - **«Notificaciones infra»** `component`
  - **«Generación PDF»** `component`
  - **«Transacciones»** `component`
- Implementa las interfaces de Dominio y reside en `infrastructure`.

### Común
- Componente **«Común»** `component`.
- Subcomponentes:
  - **«Utilidades»** `component`
  - **«Configuración»** `component`
  - **«Manejo transaccional»** `component`
  - **«JSON»** `component`
  - **«Tiempo»** `component`
- Representa el módulo `common`.

### Base de datos
- Componente **«Base de datos»** con estereotipo `database`.
- Usa los scripts de `db`.

## Relaciones
Conecta los elementos mediante dependencias (`Usage`):
- «Presentación» usa «Aplicación» y puede usar «Común».
- «Aplicación» usa «Dominio» y «Infraestructura»; también puede usar «Común».
- «Dominio» usa «Común».
- «Infraestructura» realiza las interfaces de «Dominio» y usa «Común» y «Base de datos».
- «Común» no depende de los demás componentes.
## Pasos en Enterprise Architect
1. Crea un nuevo diagrama de componentes.
2. Inserta cada componente listado y asigna el estereotipo indicado.
3. Añade los subcomponentes dentro de su elemento padre utilizando la vista jerárquica.
4. Dibuja los conectores de dependencia según la sección Relaciones.
5. Organiza los elementos de izquierda a derecha: Presentación → Aplicación → Dominio → Infraestructura → Base de datos. Coloca Común debajo como componente compartido para todas las capas.
6. Guarda el diagrama para futuras referencias.

## Resultado
El diagrama obtenido describe de forma precisa la estructura de componentes y sus interacciones para todo el sistema.
