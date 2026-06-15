---
name: table-registration-architecture
tags: [tables, ddd, hexagonal, spring-boot]
status: completed
date-created: 2026-06-14
---

# Architecture Design: Table Registration

## Overview

### Summary

Esta funcionalidad introduce el alta de mesas dentro de un nuevo bounded context `tables`, independiente del contexto `catalog` y de cualquier futuro contexto de pedidos.

El objetivo es exponer un endpoint HTTP para registrar mesas con los campos `id`, `code`, `capacity`, `location` y `status`, aplicando las reglas de unicidad del `code` a nivel de sistema, validacion de `code` y `capacity` en el dominio, normalizacion case-insensitive del `status` al enum cerrado `available` / `occupied` / `out_of_service` y asignacion de `available` cuando `status` no venga informado.

La arquitectura sigue DDD y hexagonal en los bordes para aislar el dominio del transporte HTTP y de la persistencia, sin introducir un puerto de entrada para el caso de uso, replicando la decision ya tomada en `catalog/product`.

### Related Feature Document

* `docs/design/tables/table-registration.md`

---

## Affected Contexts

| Context | Type     | Impact                                                                 |
| ------- | -------- | ---------------------------------------------------------------------- |
| Tables  | New      | Introduce la gestion inicial de alta de mesas como contexto aislado    |
| Catalog | Unchanged| Continua gobernando productos; no se modifica ni se acopla a `tables`  |
| Orders  | Unchanged| Podra consumir mesas por `id` en el futuro desde un contexto distinto |

### Notes

El contexto `tables` es propietario del ciclo de vida de la mesa y de la unicidad de su `code`.

El contexto `catalog` no debe contener reglas de creacion ni persistencia de mesas; las mesas no son productos de catalogo.

El contexto `orders` queda intencionalmente fuera de alcance en esta iteracion; cualquier referencia futura a mesas vivira en un bounded context separado y se integrara por `id`.

`tables` es su propio bounded context, no un sub-modulo de `catalog`. Esto se refleja en el package raiz `com.forkcore.api.tables.*`, hermano de `com.forkcore.api.catalog.*`.

---

## Domain Model Impact

### Aggregates

| Aggregate | Type | Purpose                                                                  |
| --------- | ---- | ------------------------------------------------------------------------ |
| Table     | New  | Representa una mesa del restaurante y asegura sus invariantes de creacion |

### Entities

| Entity | Type | Description                          |
| ------ | ---- | ------------------------------------ |
| Table  | New  | Aggregate root del contexto `tables` |

### Value Objects

| Value Object   | Type | Description                                                                                          |
| -------------- | ---- | ---------------------------------------------------------------------------------------------------- |
| TableCode      | New  | Codigo unico a nivel de sistema: obligatorio, recortado, maximo 16 caracteres y charset `[A-Za-z0-9_-]` |
| TableCapacity  | New  | Capacidad valida de la mesa: entero mayor o igual a 1                                                |
| TableLocation  | New  | Ubicacion opcional; contenido solo con espacios en blanco se trata como ausente                      |
| TableStatus    | New  | Estado cerrado de la mesa: `available`, `occupied`, `out_of_service`                                 |
| Id             | New  | Identificador compartido ubicado en `shared`, generado como UUIDv7 (time-ordered)                    |

---

## Application Services

### Use Cases

| Use Case     | Type | Description                                                                       |
| ------------ | ---- | --------------------------------------------------------------------------------- |
| TableCreator | New  | Crea una mesa valida y la persiste, comprobando la unicidad del `code`            |

### Application Flow

1. El adaptador HTTP recibe `POST /tables`.
2. La request se transforma en los parametros requeridos por `TableCreator` (`code`, `capacity`, `location`, `status`).
3. `TableCreator` delega la creacion en el dominio mediante el factory del agregado `Table`.
4. El dominio construye `Table` aplicando las reglas:
   - `code` obligatorio, recortado, con longitud y charset validos.
   - `capacity` obligatorio, entero mayor o igual a 1.
   - `location` opcional; whitespace-only se trata como ausente.
   - `status` normalizado a minusculas; si esta ausente o en blanco, se asigna `available`; cualquier valor fuera del enum cerrado se rechaza.
   - Errores multiples se agregan con `CompositeValidationError`, replicando el patron del contexto `catalog`.
5. Antes de persistir, el caso de uso consulta `TableRepository.findByCode(code)` para detectar duplicados a nivel aplicacion.
6. Si ya existe una mesa con ese `code`, el caso de uso retorna un error de dominio de conflicto que el adaptador HTTP traduce a `409 Conflict`.
7. El puerto de salida `TableRepository.save(table)` persiste el agregado. La restriccion `UNIQUE` sobre `code` en base de datos actua como salvaguarda adicional.
8. La capa de entrada devuelve `201 Created` con la cabecera `Location: /tables/{id}` y el cuerpo `TableResponse`.

---

## Domain Services

### Domain Services

Ninguno en esta iteracion.

La logica cabe dentro del agregado `Table` y sus value objects.

---

## Domain Events

### Events

Ninguno en esta iteracion.

Se pueden introducir mas adelante si otros contextos (por ejemplo `orders`) necesitan reaccionar al alta de mesas.

---

## Ports

### Outgoing Ports

| Port            | Purpose                                                                              |
| --------------- | ------------------------------------------------------------------------------------ |
| TableRepository | Persistir mesas en el contexto `tables` y permitir la comprobacion de unicidad de `code` |

### Port Contracts

`TableRepository`

- `save(Table table): Table`
- `findByCode(TableCode code): Optional<Table>`

`findByCode` es necesario para traducir la duplicidad de `code` a `409 Conflict` desde el caso de uso, sin acoplar el dominio a la tecnologia de persistencia. La restriccion `UNIQUE` en base de datos complementa esta comprobacion como red de seguridad.

---

## Repository Impact

### New Repositories

| Repository      | Purpose                                                  |
| --------------- | -------------------------------------------------------- |
| TableRepository | Almacena las mesas del contexto `tables`                 |

### Modified Repositories

Ninguno.

`ProductRepository` y cualquier otro repositorio existente queda intacto.

---

## External Integrations

### New Integrations

| Integration | Purpose                          |
| ----------- | -------------------------------- |
| PostgreSQL  | Persistencia de mesas            |

### Changes Required

* Crear tabla `tables`
* Configurar adaptador de persistencia del contexto `tables`
* Definir una restriccion `UNIQUE` sobre la columna `code` en la tabla `tables`

---

## Data Model Impact

### New Persistence Models

| Model  | Description                          |
| ------ | ------------------------------------ |
| tables | Tabla de mesas del contexto `tables` |

### Schema Changes

#### tables

| Column   | Type            | Notes                                                                       |
| -------- | --------------- | --------------------------------------------------------------------------- |
| id       | UUID            | Primary key, generado por el dominio con `Id.create()` (UUIDv7)             |
| code     | VARCHAR(16)     | Required, `UNIQUE` a nivel de base de datos                                 |
| capacity | INTEGER         | Required, mayor o igual a 1                                                 |
| location | VARCHAR or TEXT | Nullable; whitespace-only se trata como ausente antes de persistir          |
| status   | VARCHAR         | Required; el default `available` se asigna en dominio/aplicacion            |

### Persistence Notes

El default de `status` debe vivir primero en dominio/aplicacion para mantener la regla dentro del core. El esquema puede duplicar ese default mas adelante como proteccion tecnica, pero no como unica fuente de verdad.

La unicidad de `code` se enforce en dos capas:

* Comprobacion aplicativa previa al `save` mediante `TableRepository.findByCode(code)`, que produce un `409 Conflict` determinista.
* Restriccion `UNIQUE` en la columna `code` de la tabla `tables`, que actua como salvaguarda ante carreras concurrentes y como defensa en profundidad.

Esta doble proteccion garantiza que un duplicado nunca pueda persistir, ni siquiera si dos requests concurrentes sortearan la comprobacion aplicativa.

---

## Security Considerations

### Risks

* Requests con `code` vacio, en blanco, con longitud superior a 16 o con caracteres fuera del charset permitido.
* `capacity` ausente, `null`, `0` o negativo, o no entero al llegar al adaptador HTTP.
* `status` arbitrario fuera del enum cerrado tras la normalizacion, incluido `reserved`.
* `code` duplicado contra una mesa ya persistida, que debe traducirse a `409 Conflict` y no a `400`.
* Ventana de carrera entre la comprobacion aplicativa y la escritura que permitiera persistir duplicados.
* Mensajes ambiguos en la respuesta de error de duplicidad, que dificulten al cliente distinguir `400` de `409`.

### Mitigations

* Validacion de formato en el adaptador HTTP y validacion de invariantes en value objects y agregado, replicando la doble barrera del contexto `catalog`.
* Lista explicita de estados admitidos en `TableStatus`: `available`, `occupied`, `out_of_service`. `reserved` queda excluido del enum y se rechaza como valor invalido.
* `TableCode` aplica trim, limite de longitud y charset `[A-Za-z0-9_-]` antes de aceptar el valor.
* `TableCapacity` aplica la regla de `>= 1` antes de aceptar el valor.
* `TableLocation` admite ausente y trata whitespace-only como ausente.
* La duplicidad de `code` se traduce a `409 Conflict` con un cuerpo que comunica inequivocamente que el `code` ya esta en uso.
* La restriccion `UNIQUE` en base de datos protege frente a inserciones concurrentes duplicadas como red de seguridad.
* Encapsular la generacion de UUIDv7 dentro del value object compartido `Id` para que el dominio no dependa de la libreria externa directamente.

---

## Performance Considerations

### Potential Bottlenecks

* Crecimiento del numero de mesas y consultas futuras por `code` o `status`.

### Mitigations

* Mantener el caso de uso simple y sin integraciones remotas en esta iteracion.
* Indice primario por `id` e indice `UNIQUE` sobre `code` para soportar la comprobacion de unicidad y eventuales consultas por `code` en iteraciones futuras.
* La escritura es sincrona y de baja latencia, adecuada para uso operativo interno del MVP.

---

## Observability

### Metrics

* Numero de mesas creadas
* Ratio de fallos de validacion en alta de mesa
* Numero de conflictos `409` por `code` duplicado

### Logs

* Solicitud de alta recibida
* Mesa creada correctamente
* Error de validacion agregado
* Conflicto por `code` duplicado
* Fallo inesperado de persistencia

---

## Alternatives Considered

### Option A

Modelar las mesas dentro del contexto `catalog`, como un tipo mas de producto.

#### Pros

* Menos estructura inicial.
* Reutilizacion inmediata del catalogo existente.

#### Cons

* Mezcla dos dominios con responsabilidades distintas.
* Acopla el ciclo de vida de las mesas al de los productos.
* Dificulta la evolucion independiente de `catalog` y de las mesas.
* Obliga a extender `ProductStatus` o a modelar estados en un lugar que no les corresponde.

### Option B

Devolver `400 Bad Request` cuando el `code` de la mesa ya exista en el sistema.

#### Pros

* Un unico codigo de error para cualquier rechazo del alta.

#### Cons

* La duplicidad de `code` no es una request malformada, sino un conflicto con el estado actual del sistema.
* Pierde la semantica RESTful que distingue "peticion invalida" de "recurso en conflicto".
* Obliga al cliente a parsear el cuerpo del `400` para detectar la causa real del rechazo.

### Decision

Se rechaza la Opcion A: las mesas tienen identidad, ciclo de vida y reglas de unicidad propias, por lo que viven en su propio bounded context `tables`, hermano de `catalog`.

Se rechaza la Opcion B: la duplicidad de `code` se traduce a `409 Conflict`, manteniendo `400` para errores de validacion y siguiendo la semantica REST habitual. El caso de uso detecta el duplicado mediante `TableRepository.findByCode(code)` y el adaptador HTTP lo traduce a `409`, con un cuerpo que comunica inequivocamente que el `code` ya esta en uso.

No se introduce un puerto de entrada para `TableCreator` en esta iteracion, replicando la decision ya tomada para `ProductCreator` en el contexto `catalog`.

---

## Implementation Strategy

### Recommended Order

1. Crear estructura de paquetes del contexto `tables` bajo `com.forkcore.api.tables.*`.
2. Implementar value objects `TableCode`, `TableCapacity`, `TableLocation` y `TableStatus`, junto con el agregado `Table` que agrega errores con `CompositeValidationError`.
3. Definir el puerto de salida `TableRepository` con `save(Table)` y `findByCode(TableCode)`.
4. Implementar caso de uso `TableCreator` que aplica la comprobacion de unicidad antes de delegar en el repositorio.
5. Implementar adaptador REST para `POST /tables` con DTOs `CreateTableRequest` y `TableResponse` y traduccion a `201` / `400` / `409`.
6. Implementar adaptador de persistencia JPA con la entidad `TableJpaEntity`, el `SpringDataTableJpaRepository` y el `JpaTableRepositoryAdapter`, incluyendo la restriccion `UNIQUE` sobre `code`.
7. Anadir tests unitarios del dominio y tests del caso de uso.

### Dependencies

* `TableCreator` depende de `TableRepository`.
* `TableController` depende de `TableCreator`.
* `Table` depende del value object compartido `Id` ubicado en `shared/domain`.
* `TableStatus` y `TableCode` no dependen de Spring, HTTP ni JPA.

---

## Package Proposal

```text
src/main/java/com/forkcore/api/shared/domain/
  Id.java
src/main/java/com/forkcore/api/tables/
  domain/
    Table.java
    TableRepository.java
    vo/
      TableCode.java
      TableCapacity.java
      TableLocation.java
      TableStatus.java
  application/
    TableCreator.java
  infrastructure/in/web/
    TableController.java
    CreateTableRequest.java
    TableResponse.java
  infrastructure/out/persistence/
    TableJpaEntity.java
    SpringDataTableJpaRepository.java
    JpaTableRepositoryAdapter.java
```

### Notes

El nombre exacto de clases puede ajustarse, pero la direccion de dependencias debe mantenerse hacia adentro.

El dominio no debe depender de Spring, HTTP ni JPA.

Se evita crear carpetas artificiales con un solo archivo salvo cuando aportan una agrupacion clara, como `vo/`.

`Table` y sus value objects devuelven `Result<T>` desde sus factories estaticos, replicando el patron ya usado por `Product`, `ProductName`, `ProductPrice` y `ProductStatus`.

El generador de UUIDv7 time-ordered vive en `shared.domain.Id` y se invoca desde el dominio mediante `Id.create()`. La dependencia tecnica a la libreria externa queda encapsulada en ese value object compartido.

---

## REST Contract Proposal

### Endpoint

`POST /tables`

### Request Body

```json
{
  "code": "T-01",
  "capacity": 4,
  "location": "Terraza",
  "status": "available"
}
```

* `code` es obligatorio, se recorta, se valida longitud maxima 16 y charset `[A-Za-z0-9_-]`.
* `capacity` es obligatorio, entero mayor o igual a 1.
* `location` es opcional; si llega con contenido solo de espacios en blanco, se trata como ausente.
* `status` es opcional; si se omite o llega en blanco, el sistema asigna `available`. Si viene informado, se normaliza a minusculas y debe pertenecer al enum cerrado `available`, `occupied`, `out_of_service`. `reserved` se rechaza como valor invalido.

### Success Response

Status: `201 Created`

Cabecera: `Location: /tables/{id}`

```json
{
  "id": "0f4f7f2c-6f5d-4d20-91be-0c5dc1f0f1cd",
  "code": "T-01",
  "capacity": 4,
  "location": "Terraza",
  "status": "available"
}
```

### Error Responses

* `400 Bad Request` para errores de validacion de entrada. El cuerpo agrega todos los errores detectados en una sola respuesta (por ejemplo, `code` ausente y `capacity` negativo) replicando el patron `CompositeValidationError`.
* `409 Conflict` cuando el `code` ya exista en el sistema, con un cuerpo que comunique de forma inequivoca que el `code` ya esta en uso.
* `500 Internal Server Error` para fallos inesperados de infraestructura.

Formato propuesto para el cuerpo de error de validacion (orientativo, a confirmar en la fase de aceptacion):

```json
{
  "errors": [
    { "field": "code", "message": "table code is required" },
    { "field": "capacity", "message": "table capacity must be greater than or equal to one" }
  ]
}
```

Formato propuesto para el cuerpo de error de duplicidad (orientativo, a confirmar en la fase de aceptacion):

```json
{
  "errors": [
    { "field": "code", "message": "table code already exists" }
  ]
}
```

---

## Open Questions

* Si el soft cap operativo de 50 sobre `capacity` terminara siendo enforceable desde algun punto (controlador, configuracion, alerta) o quedara solo como documentacion operativa. En esta iteracion no se enforce.

---

## Implementation Notes

No introducir logica de negocio en anotaciones del controlador mas alla de validacion basica de formato; las invariantes de dominio viven en el agregado y sus value objects.

La regla de valor por defecto de `status` (`available`) debe vivir en el core del caso de uso o del agregado, no en el adaptador de entrada.

La traduccion de `code` duplicado a `409 Conflict` debe vivir en el adaptador HTTP, que recibe del caso de uso un error de dominio de conflicto; el caso de uso no debe conocer detalles HTTP.

Evitar acoplar el agregado `Table` a estructuras de persistencia o DTOs HTTP.

No introducir `Command` ni interfaz de puerto de entrada para `TableCreator` en esta iteracion, replicando la decision tomada para `ProductCreator`.

`TableStatus` es un enum cerrado con `available`, `occupied` y `out_of_service`. `reserved` queda intencionalmente excluido y se rechaza como valor invalido. La normalizacion a minusculas se aplica antes de la comparacion.

La unicidad de `code` se enforce en dos capas: comprobacion aplicativa previa al `save` y restriccion `UNIQUE` en base de datos. Ambas son necesarias: la primera para producir un `409` determinista y un mensaje claro, la segunda como salvaguarda frente a inserciones concurrentes.
