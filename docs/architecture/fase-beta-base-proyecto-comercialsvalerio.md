# Fase Beta: base del proyecto `comercialsvalerio` desde cero

## 1. Propósito de la fase Beta
La fase Beta define la construcción de la base técnica y organizativa de `comercialsvalerio` tomando a `nose` como proyecto de referencia.

En esta fase **no se ejecutan las fases 1 a 7** del plan general. Solo se toma lo necesario de la Fase 0 para dejar un esqueleto sólido,
empresarial y trazable.

Objetivo central:
- tener una estructura inicial lista para crecer,
- conservar la trazabilidad entre original y nuevo,
- y dejar reglas claras para copiar, adaptar o mejorar componentes sin perder control.

## 2. Alcance y exclusiones
### Alcance de Beta
- Crear el proyecto nuevo desde carpetas base.
- Definir módulos y responsabilidades.
- Establecer qué se copia igual, qué se modifica y qué se crea nuevo.
- Preparar tablero de control paralelo enlazado con `docs/architecture/guia-control-paralelo-proyecto-nuevo.md`.

### Exclusiones de Beta
- No implementar funcionalidades completas de negocio.
- No desarrollar las fases 1 a 7 del documento maestro.
- No optimizar UX, rendimiento o escalamiento más allá del baseline.

## 3. Pre-requisitos de instalación
Antes de iniciar la fase Beta, instala y verifica:

### Herramientas obligatorias
- Java 21 (JDK).
- Maven 3.9+.
- Git.
- IDE Java (IntelliJ IDEA o VS Code con extensiones Java).
- PostgreSQL 15+ (si validarás scripts SQL desde el inicio).

### Herramientas recomendadas
- Docker y Docker Compose (para entorno reproducible de base de datos y servicios).
- Cliente SQL (DBeaver, DataGrip o psql).
- Postman o Insomnia (para validar endpoints cuando empiece backend).

### Verificación rápida sugerida
```bash
java -version
mvn -version
git --version
psql --version
```

## 4. Estructura objetivo de carpetas (baseline empresarial)
Crear el repositorio `comercialsvalerio` con esta estructura inicial:

```text
comercialsvalerio/
├─ pom.xml
├─ README.md
├─ .gitignore
├─ docs/
│  ├─ architecture/
│  │  ├─ guia-control-paralelo-proyecto-nuevo.md
│  │  ├─ fase-beta-base-proyecto-comercialsvalerio.md
│  │  └─ tablero-control-paralelo.md
│  ├─ domain/
│  ├─ persistence/
│  ├─ database/
│  └─ use-cases/
├─ common/
├─ domain/
├─ application/
├─ infrastructure/
├─ presentation-ui/
└─ db/
```

Esta base mantiene la separación por capas de `nose`, lo que facilita aprendizaje, migración y mantenimiento.



## 4.1 ¿La estructura de `nose` es profesional? Sí, con mejoras recomendadas
Sí, la distribución actual por capas (`common`, `domain`, `application`, `infrastructure`, `presentation-ui`, `db`) es una base
profesional y muy usada cuando se trabaja con arquitectura limpia/hexagonal.

Sin embargo, en empresas suele reforzarse con estas prácticas estándar:

- **Convención de empaquetado por dominio funcional + capa técnica**:
    - ejemplo: `sales/domain`, `sales/application`, `sales/infrastructure`.
    - evita un crecimiento desordenado cuando el sistema escala.
- **Separación estricta de contratos y adaptadores**:
    - puertos de entrada/salida en `application`.
    - implementación técnica solo en `infrastructure`.
- **Módulos con fronteras claras**:
    - cada módulo compila y versiona con dependencias mínimas.
    - no se permite que `domain` dependa de frameworks.
- **Estandarización transversal**:
    - errores, logs, observabilidad, seguridad y validaciones compartidas.
- **Gobierno documental vivo**:
    - ADRs (Architecture Decision Records), tablero de trazabilidad y convenciones de código.

Conclusión práctica: copiar `nose` "tal cual" puede servir como arranque, pero la ruta profesional es copiar **la intención de diseño**
y luego mejorar la organización para escalabilidad, mantenibilidad y trazabilidad.

## 4.2 Estructura estándar recomendada en empresas (referencia)
Para `comercialsvalerio`, una estructura empresarial común sería:

```text
comercialsvalerio/
├─ docs/
│  ├─ architecture/
│  │  ├─ adr/
│  │  ├─ guia-control-paralelo-proyecto-nuevo.md
│  │  └─ fase-beta-base-proyecto-comercialsvalerio.md
│  ├─ api/
│  └─ runbooks/
├─ modules/
│  ├─ shared/                      # utilidades transversales
│  ├─ sales/
│  │  ├─ sales-domain/
│  │  ├─ sales-application/
│  │  └─ sales-infrastructure/
│  ├─ inventory/
│  │  ├─ inventory-domain/
│  │  ├─ inventory-application/
│  │  └─ inventory-infrastructure/
│  └─ iam/
├─ apps/
│  ├─ api-rest/                    # bootstrap Spring Boot
│  └─ backoffice-ui/               # frontend
├─ db/
│  ├─ migration/
│  ├─ seed/
│  └─ scripts/
├─ pom.xml
└─ README.md
```

Si quieres un inicio simple, puedes mantener la estructura actual de `nose`; si quieres un inicio corporativo escalable,
usa esta versión modular por dominio desde el día 1.

## 5. Mapeo inicial: qué mantener igual y qué ajustar desde `nose`

| Módulo base en `nose` | Acción en `comercialsvalerio` | Decisión inicial | Nota práctica |
|---|---|---|---|
| `common` | Mantener módulo | EQUIVALENTE | Copiar utilidades puras; revisar nombres y paquetes. |
| `domain` | Mantener módulo | EQUIVALENTE / MEJORADO | Conservar entidades y reglas core; limpiar acoplamientos. |
| `application` | Mantener módulo | EQUIVALENTE | Reusar casos de uso como guía de puertos y orquestación. |
| `infrastructure` | Mantener módulo | MEJORADO | Adaptar persistencia/config a Spring Boot moderno. |
| `presentation-ui` | Mantener módulo o separar repositorio | EQUIVALENTE / MEJORADO | Definir si seguirá monorepo o frontend desacoplado. |
| `db` | Mantener módulo | IGUAL / MEJORADO | Copiar artefactos SQL críticos y migrarlos a versionado. |

## 6. Estrategia de copia y adaptación por tipo de artefacto

### 6.1 Copiar casi igual (IGUAL)
Usar para:
- scripts SQL estables,
- catálogos,
- reglas de validación simples y maduras.

Criterio:
- no cambia semántica funcional,
- solo ajustes técnicos mínimos (nombres, rutas, empaquetado).

### 6.2 Copiar con ajuste técnico (EQUIVALENTE)
Usar para:
- casos de uso,
- servicios de aplicación,
- DTOs y mapeos.

Criterio:
- mismo comportamiento esperado,
- diseño interno adaptado al estándar del proyecto nuevo.

### 6.3 Rediseñar con mejora (MEJORADO)
Usar para:
- adaptadores de infraestructura,
- manejo de errores,
- seguridad, observabilidad y configuración.

Criterio:
- objetivo funcional se conserva,
- se mejora mantenibilidad, escalabilidad o claridad.

### 6.4 Crear desde cero (NUEVO)
Usar para:
- políticas técnicas faltantes,
- quality gates,
- plantillas de documentación y automatización base.

## 7. Plan operativo de la fase Beta (paso a paso)

1. Crear repositorio y estructura de módulos vacíos.
2. Configurar `pom.xml` padre multi-módulo.
3. Crear `pom.xml` por módulo con dependencias mínimas.
4. Definir convención de paquetes base:
    - `com.comercialsvalerio.common`
    - `com.comercialsvalerio.domain`
    - `com.comercialsvalerio.application`
    - `com.comercialsvalerio.infrastructure`
5. Crear clases base mínimas (sin lógica de negocio final):
    - objeto de error común,
    - respuesta estándar para API,
    - plantilla de caso de uso,
    - configuración inicial de persistencia.
6. Preparar carpeta `db` para migraciones versionadas (`db/migration`).
7. Copiar documentación de arquitectura útil desde `nose` y ajustarla al nuevo contexto.
8. Crear tablero de control paralelo específico de Beta.
9. Registrar cada decisión usando etiquetas IGUAL/EQUIVALENTE/MEJORADO/NUEVO.
10. Validar que compila todo con:
    - `mvn -q -DskipTests package`

## 8. Clases y carpetas recomendadas para arrancar

### 8.1 `common`
Crear:
- `exception/BusinessException.java`
- `exception/TechnicalException.java`
- `response/ApiResponse.java`
- `util/DateProvider.java`

### 8.2 `domain`
Crear:
- `model/` (entidades núcleo del negocio)
- `valueobject/` (tipos inmutables con validación)
- `service/` (reglas de dominio sin dependencias técnicas)

### 8.3 `application`
Crear:
- `port/in/`
- `port/out/`
- `usecase/`
- `dto/`

### 8.4 `infrastructure`
Crear:
- `config/`
- `adapter/persistence/`
- `adapter/rest/`
- `mapper/`

### 8.5 `presentation-ui`
Crear:
- estructura mínima del frontend elegido,
- carpeta de contratos de integración con backend (`api-contracts/`).

### 8.6 `db`
Crear:
- `migration/` para scripts versionados,
- `seed/` para datos base,
- `procedures/`, `functions/`, `views/` cuando aplique.

## 9. Tablero de control paralelo para Beta
Usar una tabla dedicada para controlar la construcción base:

| ID | Sprint | Artefacto en `nose` | Artefacto en `comercialsvalerio` | Relación | Estado | Evidencia |
|---|---|---|---|---|---|---|
| BETA-001 | Beta | `pom.xml` raíz | `pom.xml` multi-módulo | EQUIVALENTE | Pendiente | - |
| BETA-002 | Beta | `domain/...` | `domain/model/...` | EQUIVALENTE | Pendiente | - |
| BETA-003 | Beta | `db/...` | `db/migration/V1__baseline.sql` | IGUAL | Pendiente | - |
| BETA-004 | Beta | `infrastructure/...` | `infrastructure/config/...` | MEJORADO | Pendiente | - |
| BETA-005 | Beta | N/A | estándar de errores unificado | NUEVO | Pendiente | - |

## 10. Definición de terminado (DoD) de fase Beta
La fase Beta se considera completada cuando:
- existe estructura multi-módulo compilable,
- están creadas carpetas y paquetes base por capa,
- hay lineamientos claros de copia/modificación/mejora,
- existe tablero paralelo con trazabilidad inicial,
- y el equipo puede iniciar la Fase 0 formal sin rehacer la base.

## 11. Enlace de control con la guía paralela principal
Durante Beta, toda decisión debe referenciar la guía:
- `docs/architecture/guia-control-paralelo-proyecto-nuevo.md`

Regla práctica:
- si un artefacto se crea en Beta, queda registrado como `BETA-xxx`;
- cuando inicien fases 0 a 7, se enlaza cada `BETA-xxx` con su `PAR-xxx` correspondiente.

## 12. Propósito formativo de este enfoque
Este enfoque te ayuda a aprender a construir proyectos empresariales porque obliga a:
- separar capas desde el inicio,
- justificar decisiones técnicas,
- mantener trazabilidad entre legado y evolución,
- y evitar crecimiento desordenado del código.

En resumen, Beta no busca terminar funcionalidades: busca construir una base profesional, repetible y escalable.
