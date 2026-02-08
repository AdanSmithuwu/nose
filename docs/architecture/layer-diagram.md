# Diagrama de capas

Este documento describe los pasos para construir un diagrama de capas en Enterprise Architect para el proyecto Comercial's Valerio.

## Alcance
El diagrama debe representar las dependencias entre los módulos:
- Presentation (`presentation-ui`).
- Application.
- Domain.
- Infrastructure.
- Common.
- Database (`db`).

## Elementos necesarios
1. **Presentación**
   - Interfaz de usuario Swing ubicada en `presentation-ui`.
2. **Aplicación**
   - Controladores REST y lógica de orquestación del módulo `application`.
3. **Dominio**
   - Entidades y lógica de negocio definidas en `domain`.
4. **Infraestructura**
   - Persistencia, seguridad e integraciones provistas por `infrastructure`.
5. **Común**
   - Utilidades compartidas en `common`.
6. **Base de datos**
   - Scripts SQL agrupados en `db`.

## Relaciones
- Presentación depende de Aplicación y puede utilizar Común.
- Aplicación depende de Dominio e Infraestructura y también puede usar Común.
- Dominio utiliza Común.
- Infraestructura depende de Dominio y Base de datos; también utiliza Común.
- Común no depende de las demás capas.
- Las capas superiores no deben depender de las inferiores fuera de estas reglas.
## Pasos en Enterprise Architect
1. Crea un nuevo diagrama de componentes y cambia su tipo a "Layered View".
2. Inserta un elemento de capa por cada módulo.
3. Organiza las capas de arriba hacia abajo:

   Presentación → Aplicación → Dominio → Infraestructura → Base de datos

    La capa Común se ubica a un costado conectando con todas las capas.
4. Usa conectores de dependencia para reflejar las relaciones.
5. Aplica el estereotipo «layer» a cada elemento.
6. Añade notas breves si es necesario.

## Resultado
El diagrama final sirve como guía para mantener la separación de responsabilidades al agregar nuevas clases o relaciones.
