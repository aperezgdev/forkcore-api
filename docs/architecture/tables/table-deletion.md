---
name: table-deletion-architecture
tags: [tables, ddd, hexagonal, spring-boot]
status: completed
date-created: 2026-06-15
---

# Architecture Design: Table Deletion

## Overview

### Summary

Esta funcionalidad introduce la baja fisica de mesas dentro del bounded context `tables`, completando la capacidad basica de gestion del modulo introducida por `table-registration`.

El objetivo es exponer `DELETE /tables/{id}` para retirar una mesa existente del sistema, devolviendo `204 No Content` en la primera baja exitosa y `404 Not Found` en cualquier llamada posterior o sobre un `id` que nunca existio, manteniendo el formato del `id` consistente con `table-registration` y la misma representacion de identificador (UUIDv7 time-ordered) que el resto del modulo.

La arquitectura sigue DDD y hexagonal en los bordes para aislar el dominio del transporte HTTP y de la persistencia. La baja se modela como una operacion de comando en el caso de uso `TableDeleter`, que reutiliza el agregado `Table` y el mismo puerto `TableRepository` ya introducido por `table-registration`. No se introduce un nuevo agregado, ni un nuevo value object, ni un evento de dominio, ni un puerto de entrada adicional. La operacion de borrado fisico es una decision de orquestacion del caso de uso, que combina una lectura por `id` con una llamada al puerto de salida.

Esta funcionalidad adopta, de forma deliberada, el modelo HTTP unificado de `catalog/product` (post-migracion de `product-deletion`) para el cuerpo de error: cuerpo literalmente vacio como placeholder neutro para `400` y `404`, casos de uso que reciben el identificador como `String` y lo validan internamente, y adaptador HTTP que traduce los `Result` por tipo de `DomainError` en lugar de delegar en un advice global. Esta decision introduce, de forma explicita y temporal, una **divergencia con `table-registration`**, que sigue usando el cuerpo estructurado `{"errors":[{"field":"...","message":"..."}]}` para `400` (CompositeValidationError) y `409` (ConflictError). `table-registration` **no se modifica en esta iteracion**: la unificacion del formato de cuerpo de error del modulo `tables` queda registrada en `## Future Improvements` y `## Deferred Decisions` de este documento, y se abordara en una iteracion posterior.

La respuesta exitosa no devuelve cuerpo y no reutiliza la representacion `TableResponse` usada por `table-registration`. El cuerpo de error de `400` y `404` es literalmente vacio (sin `null`, sin `{}`, sin espacios en blanco), como placeholder neutro; el `500` se sirve en el formato por defecto de Spring. La forma concreta de los cuerpos de error queda deliberadamente fuera del alcance de esta iteracion y se definira cuando se unifique el formato del modulo `tables`.

La validacion de formato del `id` se delega al value object compartido `Id` mediante la factoría segura `Id.from(String)`, alineada con la convencion del resto de VOs del modulo. El caso de uso `TableDeleter` recibe el `id` como `String`, lo valida con `Id.from`, y propaga cualquier `ValidationError` resultante a traves del `Result`. El adaptador HTTP no valida ni captura excepciones: traduce el `Result` del caso de uso a `204`, `400` o `404` por tipo de `DomainError`.

### Related Feature Document

* `docs/design/tables/table-deletion.md`

### Traceability Notes

* Reutiliza el agregado `Table` y los value objects `TableCode`, `TableCapacity`, `TableLocation` y `TableStatus` introducidos en `docs/architecture/tables/table-registration.md`.
* Reutiliza los tipos compartidos `Result`, `DomainError`, `NotFoundError` y `ValidationError` definidos en `src/main/java/com/forkcore/api/shared/domain/`.
* Reutiliza la factoría segura `Id.from(String)` del value object compartido `Id` (extendido en `shared/domain`), paralela a las factorías `TableCode.from`, `TableCapacity.from`, `TableStatus.from` y `TableLocation.from` del modulo `tables`.
* Amplia el puerto `TableRepository` con dos operaciones nuevas: `findById(Id)` y `delete(Table)`. No se elimina ninguna operacion existente (`save(Table)`, `findByCode(TableCode)`).
* No se reaprovecha `TableResponse`: una baja exitosa no devuelve cuerpo, por lo que la consistencia de representacion aplica unicamente a los cuerpos de error.
* `table-registration` **no se modifica en esta iteracion**. La traduccion de errores de `TableController.create(...)` mantiene su cuerpo estructurado actual; el unico cambio en `TableController` es la adicion del metodo `delete(...)` con la nueva politica de mapeo de `Result` a HTTP y cuerpo de error neutro.
* No se elimina ninguna clase existente en esta iteracion: no existe un equivalente a `ProductErrorHandler` en el modulo `tables`, por lo que no hay advice global que retirar. La traduccion de errores ya vive en `TableController` desde `table-registration`; en esta iteracion solo se anade el mapeo para el nuevo endpoint `DELETE /tables/{id}`.

---

## Affected Contexts

| Context | Type     | Impact                                                                                |
| ------- | -------- | ------------------------------------------------------------------------------------- |
| Tables  | Modified | Anade la operacion de baja fisica sobre el modulo existente de mesas                  |
| Catalog | Unchanged | Continua gobernando productos; no se modifica ni se acopla a `tables`               |
| Orders  | Unchanged | Podra consumir mesas por `id` en el futuro desde un contexto distinto, sin gobernar la baja |

### Notes

El contexto `tables` sigue siendo propietario del ciclo de vida de la mesa, incluyendo su baja. La nueva capacidad ampla el mismo modulo `tables` introducido en `table-registration` sin introducir un nuevo bounded context, un nuevo agregado, ni cambios en otros contextos.

`table-registration` queda intacto en esta iteracion. La coexistencia temporal entre el cuerpo estructurado `{"errors":[{"field":"...","message":"..."}]}` de `POST /tables` y el cuerpo neutro de `DELETE /tables/{id}` es deliberada y esta documentada en `## Module-Wide Consistency Notes`, `## Deferred Decisions` y `## Future Improvements`.

---

## Domain Model Impact

### Aggregates

| Aggregate | Type      | Purpose                                                       |
| --------- | --------- | ------------------------------------------------------------- |
| Table     | Unchanged | Se consulta unicamente para confirmar presencia previa al borrado |

### Entities

| Entity | Type      | Description                                                                            |
| ------ | --------- | -------------------------------------------------------------------------------------- |
| Table  | Unchanged | Aggregate root ya existente, leido por `id` y luego borrado por el adaptador de persistencia |

### Value Objects

| Value Object  | Type      | Description                                                                                                                                                                                                                                                                                                                                                                                          |
| ------------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| TableCode     | Unchanged | Sin cambios                                                                                                                                                                                                                                                                                                                                                                                          |
| TableCapacity | Unchanged | Sin cambios                                                                                                                                                                                                                                                                                                                                                                                          |
| TableLocation | Unchanged | Sin cambios                                                                                                                                                                                                                                                                                                                                                                                          |
| TableStatus   | Unchanged | Sin cambios                                                                                                                                                                                                                                                                                                                                                                                          |
| Id            | Unchanged | Value object compartido. Ya dispone de la factoría segura `Id.from(String): Result<Id>` introducida en una iteracion previa del modulo `shared/domain`. Ante input `null`, blank, o UUID malformado, `Id.from` devuelve `Result.failure(new ValidationError("id", "must be a valid UUID"))`, encapsulando la posible `IllegalArgumentException` del parseo. La factoría legacy `fromStringOrThrow` (renombrada desde `fromString`) se mantiene deprecada y solo la usan callers que dependen de la semantica con excepcion (`Table.fromPrimitives` y tests del VO). En el camino HTTP se usa exclusivamente `Id.from`. |

### Domain Notes

No se introduce un nuevo agregado.

La baja fisica no es un comportamiento del dominio: el agregado `Table` no conoce la operacion `delete`. Es una decision de orquestacion del caso de uso, que combina una lectura por `id` con una llamada al puerto de salida `TableRepository`. Esto preserva el principio de que el dominio `tables` no se acopla a detalles del ciclo de vida del almacenamiento.

No se anade un metodo `delete()` al agregado `Table`. La baja fisica no forma parte de la API del agregado: es una operacion de infraestructura que el caso de uso orquesta apoyandose en el puerto de salida.

---

## Application Services

### Use Cases

| Use Case     | Type | Description                                                                                                                                                                                                                                                                                                                                                                                                                  |
| ------------ | ---- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| TableDeleter | New  | Recupera una mesa por `id`; si no existe responde `NotFoundError`, y si existe solicita su baja fisica a traves del puerto de salida. El caso de uso recibe el `id` como `String` y delega su validacion en `Id.from(String)`. Si la validacion falla, propaga el `ValidationError` resultante. Si la validacion pasa, realiza `findById` + `delete` y devuelve `Result<Void>` (`success` o `failure(NotFoundError)`). El caso de uso nunca lanza excepciones. |

### Application Flow

1. El adaptador HTTP recibe `DELETE /tables/{id}`. Toma el `id` del path como `String` y no realiza ninguna validacion local.
2. El adaptador HTTP delega en `TableDeleter.run(id)`.
3. El caso de uso invoca `Id.from(id)`. Si devuelve `Result.failure(ValidationError("id", "must be a valid UUID"))`, el caso de uso devuelve ese mismo `Result.failure` y termina; el adaptador HTTP lo traduce a `400 Bad Request` con cuerpo vacio.
4. Si `Id.from` devuelve `Result.success(id)`, el caso de uso consulta la mesa a traves de `TableRepository.findById(id)`.
5. Si la mesa no existe, el caso de uso devuelve `Result.failure(new NotFoundError("Table", id.asString()))` y termina; el adaptador HTTP lo traduce a `404 Not Found` con cuerpo vacio.
6. Si la mesa existe, el caso de uso solicita su baja fisica a traves de `TableRepository.delete(table)` y devuelve `Result.success()`. El adaptador HTTP traduce `success` a `204 No Content` con cuerpo vacio.
7. El adaptador HTTP no captura ninguna excepcion en este flujo. La traduccion de `Result` a codigo HTTP es la unica logica HTTP del metodo.

---

## Domain Services

### Domain Services

Ninguno en esta iteracion.

La decision de "borrar solo si existe" cabe en la orquestacion de `TableDeleter` y no requiere un servicio de dominio.

---

## Domain Events

### Events

Ninguno en esta iteracion.

No se introduce un evento de dominio `TableDeleted` en esta iteracion. Si en el futuro otros contextos (por ejemplo `orders`) necesitan reaccionar a la baja, el caso de uso podra emitirlo sin necesidad de modificar el contrato del puerto actual.

---

## Ports

### Outgoing Ports

| Port            | Purpose                                                                                              |
| --------------- | ---------------------------------------------------------------------------------------------------- |
| TableRepository | Persistir mesas del contexto `tables`, comprobar la unicidad de `code` y dar de baja fisica una mesa existente |

### Port Contracts

`TableRepository`

* `save(Table table): Table`
* `findByCode(TableCode code): Optional<Table>`
* `findById(Id id): Optional<Table>` *(new)*
* `delete(Table table): void` *(new)*

### Port Notes

Se anaden dos metodos nuevos al puerto: `Optional<Table> findById(Id id)` y `void delete(Table table)`. `findById` es necesario para distinguir "existe" de "no existe" en el caso de uso, sin acoplar el dominio a la tecnologia de persistencia. `delete` recibe el agregado ya cargado por `findById` en lugar de recibir unicamente un `Id`.

Razones para preferir `delete(Table)` frente a `deleteById(Id)`:

* El caso de uso ya ha cargado la `Table` para distinguir "existe" de "no existe". Pasar de nuevo solo el `Id` obligaria a duplicar la lectura o a introducir un segundo metodo de puerto, ambos redundantes.
* Recibir el agregado permite, en futuras iteraciones, anadir logica de dominio alrededor del borrado (por ejemplo, eventos de dominio) sin cambiar la firma del puerto.
* Refleja la semantica tipica de los adaptadores de persistencia, donde borrar por entidad es la operacion canonica.

Razones para introducir `findById(Id)` en lugar de extender `findByCode(TableCode)` o introducir un buscador alternativo:

* El path HTTP transporta el `id` de la mesa, no su `code`. Un endpoint que recibe un `id` debe apoyarse en un metodo de puerto que reciba un `Id` para mantener la simetria entre la entrada HTTP y la operacion de dominio.
* Separar `findById` y `findByCode` deja el puerto preparado para operaciones futuras que necesiten ambos ejes de busqueda (por ejemplo, una consulta `GET /tables/{id}` que coexista con una consulta `GET /tables?code=...`).

No se introduce un nuevo puerto de salida (consulta, evento, etc.): la operacion se apoya enteramente en `TableRepository`.

No se introduce un puerto de entrada adicional: se mantiene el estilo ya usado en `TableCreator` y en `ProductCreator` / `ProductDeleter`, donde el caso de uso es directamente invocado por el adaptador HTTP.

El caso de uso `TableDeleter` no se apoya en el puerto `TableRepository` para validar el `id`. La validacion se realiza mediante la factoría `Id.from(String)` del value object `Id`.

---

## Repository Impact

### New Repositories

None.

### Modified Repositories

| Repository                    | Change                                                                                                                                  |
| ----------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| `JpaTableRepositoryAdapter`   | Anade `findById(Id)` y `delete(Table)` delegando en `JpaRepository` sobre la misma tabla `tables`                                      |
| `InMemoryTableRepository`     | Anade `findById(Id)` y `delete(Table)` para mantener la coherencia del doble de pruebas; el metodo `deleteAll()` existente se mantiene |
| `SpringDataTableJpaRepository`| No requiere cambios: hereda `findById(Id)` y `delete(entity)` de `JpaRepository`. Opcionalmente se documenta que `findByCode` ya existe |

---

## External Integrations

### New Integrations

| Integration | Purpose                                                  |
| ----------- | -------------------------------------------------------- |
| PostgreSQL  | Persistencia de mesas, incluyendo la baja fisica         |

### Changes Required

* Permitir la operacion de borrado fisico por clave primaria en el adaptador JPA
* Reutilizar la misma tabla `tables` ya definida en `V2__create_tables_table.sql`
* No se requieren migraciones, cambios de esquema, indices adicionales ni cambios en la configuracion de la conexion

---

## Data Model Impact

### New Persistence Models

None.

### Schema Changes

None.

Se reutiliza la tabla `tables` ya definida por `table-registration`. La baja elimina filas existentes en tiempo de ejecucion; la estructura de la tabla no cambia.

### Persistence Notes

El `id` del path se utiliza para localizar la fila mediante el indice de clave primaria. Una vez localizada, la fila se elimina por entidad, apoyandose en la operacion estandar del adaptador JPA. Tras la baja, el `id` deja de estar presente en la tabla y cualquier llamada posterior responde `404 Not Found`, igual que cualquier `id` que nunca fue dado de alta.

---

## REST Contract Proposal

### Endpoint

`DELETE /tables/{id}`

### Request Body

Ninguno.

El endpoint no acepta `Content-Type` ni lee cuerpo. Un `id` eventualmente presente en el body debe ignorarse por construccion, ya que el adaptador HTTP no deserializa cuerpo para esta operacion.

### Path Variable

* `id`: identificador de la mesa en el mismo formato UUIDv7 time-ordered que el resto del modulo `tables` (validado por `Id.from(String)`).

### Success Response

Status: `204 No Content`

Cuerpo: literalmente vacio. No se devuelve `TableResponse`, ni representacion parcial de la mesa eliminada, ni `null`, ni objeto JSON vacio, ni espacios en blanco.

### Error Responses

* `400 Bad Request` cuando el `id` del path no respeta el formato esperado (null, blank, o UUID malformado). Cuerpo: literalmente vacio, como placeholder neutro.
* `404 Not Found` cuando la mesa identificada por el `id` no existe en el momento de la llamada, ya sea porque nunca fue creada o porque ya fue eliminada en una llamada anterior. Cuerpo: literalmente vacio, como placeholder neutro.
* `500 Internal Server Error` para fallos inesperados de infraestructura. Cuerpo: formato por defecto de Spring (no especificado por esta arquitectura; ver `## Deferred Decisions` en el design).

En esta iteracion **no se adopta el formato `{"errors":[{"field":"...","message":"..."}]}`** que `table-registration` sigue usando para `POST /tables`. Tampoco se adopta Problem Details. La unica respuesta de error de este endpoint es cuerpo vacio.

### Other endpoints in scope

Esta iteracion **NO** migra `POST /tables` al cuerpo neutro. `table-registration` mantiene intactos:

* Su cuerpo de exito `201 Created` con cabecera `Location: /tables/{id}` y cuerpo `TableResponse`.
* Su cuerpo de error `400 Bad Request` con la lista agregada de errores de validacion `CompositeValidationError`, en formato `{"errors":[{"field":"...","message":"..."}]}`.
* Su cuerpo de error `409 Conflict` para `code` duplicado, en el mismo formato estructurado.

La coexistencia entre los dos formatos de error dentro del modulo `tables` es temporal y se resolvera cuando se unifique el formato del modulo en una iteracion posterior. La decision de no migrar `POST /tables` en esta iteracion es deliberada: mantiene el alcance de `table-deletion` paralelo a `product-deletion` y evita acoplar la decision de unificacion a la entrega de la baja.

El endpoint `GET /tables` y cualquier endpoint futuro de actualizacion o consulta del modulo tampoco entran en alcance de esta iteracion.

---

## Module-Wide Consistency Notes

Esta seccion recoge los detalles de la coherencia del modulo `tables` con el modelo HTTP unificado, sin reintroducir Problem Details en el nuevo endpoint ni tocar el alta existente. Los puntos contractuales y de comportamiento ya estan cubiertos en `## Requirements` del documento de diseno; esta seccion sirve de guia de implementacion y de contrato de los pasos a aplicar al codigo existente.

### Endpoint affected

El unico endpoint nuevo del modulo `tables` en esta iteracion es:

| Endpoint           | Use case      | Success mapping                  | Failure mapping                                                                              |
| ------------------ | ------------- | -------------------------------- | -------------------------------------------------------------------------------------------- |
| `DELETE /tables/{id}` | `TableDeleter` | `204 No Content` con cuerpo vacio | `400` para `ValidationError` (formato de `id`), `404` para `NotFoundError` (mesa inexistente) |

El `POST /tables` existente, gobernado por `TableCreator`, mantiene su contrato actual, incluido el cuerpo de error estructurado `{"errors":[{"field":"...","message":"..."}]}` para `CompositeValidationError` y `ConflictError`. En esta iteracion no se migra el alta.

### Failure-to-HTTP mapping rule (nuevo endpoint)

La regla uniforme aplicada al nuevo endpoint es:

```
switch (result) {
  case success                        -> 204 No Content con cuerpo vacio
  case failure(NotFoundError)         -> 404 Not Found  con cuerpo vacio
  case failure(DomainError)           -> 400 Bad Request con cuerpo vacio
}
```

Esta regla se implementa en el nuevo metodo de `TableController` sin `try/catch` y sin lanzar ninguna excepcion. El adaptador HTTP no captura `IllegalArgumentException` del parseo del `id` ni valida el `id` localmente. La traduccion del nuevo endpoint sigue el mismo patron ya presente en `ProductController.delete(...)`.

### Divergence with `table-registration` and future unification

En esta iteracion, el modulo `tables` queda con dos politicas de cuerpo de error conviviendo:

* `POST /tables` (`TableCreator`): cuerpo estructurado `{"errors":[{"field":"...","message":"..."}]}` para `400` (CompositeValidationError) y `409` (ConflictError). No se modifica.
* `DELETE /tables/{id}` (`TableDeleter`): cuerpo literalmente vacio para `400` y `404`. Adopta el modelo neutro de `catalog/product` (post-migracion de `product-deletion`).

Esta coexistencia es **temporal**. La unificacion del formato de cuerpo de error del modulo `tables` queda registrada en `## Future Improvements` y los detalles concretos de la decision se aplazan a `## Deferred Decisions`. La decision de no migrar `POST /tables` en esta iteracion es deliberada: mantiene el alcance de `table-deletion` paralelo a `product-deletion` y evita acoplar la decision de unificacion a la entrega de la baja.

### No new domain event, no new entry port

No se introduce un evento de dominio (`TableDeleted`) ni un puerto de entrada para `TableDeleter` en esta iteracion. Se mantiene el estilo directo caso-de-uso-llamado-por-controlador ya usado en `TableCreator`, replicando la decision ya tomada para `ProductCreator` y `ProductDeleter` en el contexto `catalog`.

### No class removal in this iteration

A diferencia de `product-deletion`, esta iteracion **no elimina ninguna clase existente** del modulo `tables`. No existe un equivalente a `ProductErrorHandler` (no hay `@RestControllerAdvice` de errores en `tables`); la traduccion de errores del alta ya vive en `TableController` desde `table-registration`. El unico cambio en `TableController` es la adicion del metodo `delete(...)` con la nueva politica de mapeo de `Result` a HTTP y cuerpo de error neutro. La seccion `Removal of <error handler>` del paralelo de `product-deletion` no aplica aqui.

### No updates to existing BDD `.feature` files

En esta iteracion **no se actualiza ningun `.feature` de BDD existente** del modulo `tables`. El unico `.feature` nuevo es `table-deletion.feature`, que cubre los escenarios de la baja con cuerpo de error neutro. La unificacion del formato de error del modulo (y la posible reescritura de `table-registration.feature` para alinearse con el cuerpo neutro) queda registrada en `## Future Improvements`.

---

## Validation and Error Handling

### Validation Rules

La unica regla de validacion de entrada del nuevo endpoint es el formato del `id`. Dicha validacion se realiza en el value object `Id` mediante la factoría `Id.from(String)` que devuelve `Result<Id>`. El caso de uso `TableDeleter` recibe el `id` como `String` y delega esa validacion. El adaptador HTTP no valida nada: traduce el `Result` del caso de uso a codigo HTTP.

### Error Mapping

* `id` del path malformado -> `Result.failure(ValidationError("id", "must be a valid UUID"))` -> `400 Bad Request` con cuerpo vacio. La deteccion se realiza dentro del caso de uso, no en el adaptador HTTP.
* Mesa inexistente -> `Result.failure(NotFoundError("Table", id.asString()))` -> `404 Not Found` con cuerpo vacio.
* Baja efectiva -> `Result.success()` -> `204 No Content` con cuerpo vacio.
* Error tecnico de persistencia no esperado -> `500 Internal Server Error` con el formato por defecto de Spring (no especificado por esta arquitectura; ver `## Deferred Decisions` en el design).

El cuerpo de error es deliberadamente neutro: las respuestas de `400` y `404` del nuevo endpoint llevan cuerpo vacio como placeholder, y el `500` se sirve en el formato por defecto de Spring. La eleccion de `Result<Void>` como tipo de retorno del caso de uso garantiza que la unica informacion que sale del nucleo es una senal de exito o un `DomainError`, dejando al adaptador HTTP entera libertad para definir la representacion HTTP del error mas adelante, sin acoplar el caso de uso al formato del cuerpo.

### Adapter Notes

* El metodo `TableController.delete(...)` sigue la politica uniforme de mapeo de `Result` a HTTP definida en `## Module-Wide Consistency Notes`. No captura `IllegalArgumentException` ni lanza ninguna excepcion.
* El caso de uso no conoce `HttpStatus`, `ResponseEntity` ni el formato del cuerpo de error. La traduccion de `Result` a codigo HTTP vive exclusivamente en el adaptador HTTP.
* El `204` debe tener el cuerpo literalmente vacio: sin `null`, sin `{}`, sin espacios en blanco. Los `400` y `404` de este endpoint tambien tienen cuerpo vacio (placeholder neutro; ver `## Deferred Decisions` en el design).
* Cualquier `IllegalArgumentException` que se filtre desde `Id.fromStringOrThrow` (lo cual no deberia ocurrir en el camino HTTP porque `Id.from` la encapsula en `Result.failure(ValidationError)` y `Id.fromStringOrThrow` no se usa en el adaptador) se considera un error inesperado y se traduce a `500 Internal Server Error` por el manejo por defecto de Spring.

---

## Security Considerations

### Risks

* `id` con formato manipulado para agotar caminos de validacion o provocar excepciones no controladas.
* Divergencia entre la validacion del `id` del path y la usada en `table-registration`, que reusara el mismo value object `Id` en una futura migracion.
* Acoplamiento futuro del cuerpo de error a un esquema concreto que filtre informacion innecesaria.
* Divergencia temporal entre los formatos de error del modulo `tables` (cuerpo neutro en `DELETE`, cuerpo estructurado en `POST`), que puede confundir a clientes no documentados.

### Mitigations

* Reutilizar el value object compartido `Id` como unica fuente de validacion de formato del `id` del path, consistente con el resto del modulo `tables`.
* Mantener el caso de uso sin acceso al cuerpo HTTP, de forma que un eventual `id` enviado en body sea ignorado por construccion.
* Definir el cuerpo de error del nuevo endpoint como un placeholder neutro (literalmente vacio), sin Problem Details ni campos sensibles, para no exponer detalles internos del fallo.
* Validacion de `id` centralizada en el value object `Id`: ningun adaptador HTTP del modulo `tables` debe capturar excepciones ni validar el `id` localmente. Esto elimina vectores de inconsistencia entre endpoints y reduce el riesgo de que un `id` malformado se traduzca a un codigo HTTP incorrecto en algun path.
* La decision de `table-registration` de no migrarse en esta iteracion evita extender el area de inconsistencia del modulo: solo se introduce un nuevo endpoint con politica uniforme, sin reescribir los `.feature` existentes del alta.

---

## Performance Considerations

### Potential Bottlenecks

* La operacion requiere una lectura por `id` seguida de un borrado fisico.
* La latencia estara dominada por el acceso a la base de datos.

### Mitigations

* Mantener el caso de uso sin integraciones remotas adicionales.
* Reutilizar el indice primario por `id` ya existente en la tabla `tables` (definido por la PK en `V2__create_tables_table.sql`).
* Evitar lecturas adicionales (por ejemplo, comprobaciones de existencia redundantes mas alla de `findById`) dentro del caso de uso.
* No introducir cache ni procesamiento asincrono: la baja es sincrona y determinista.

---

## Observability

### Metrics

* Numero de solicitudes a `DELETE /tables/{id}`.
* Ratio de `204` frente al total de solicitudes (bajas efectivas).
* Ratio de `404` frente al total de solicitudes (`id` inexistente o ya eliminado).
* Ratio de `400` frente al total de solicitudes (`id` malformado).

### Logs

* Solicitud de baja recibida.
* Baja efectiva (`id` eliminado).
* Mesa no encontrada (`id` inexistente en el momento de la llamada).
* `id` rechazado por formato invalido.
* Error inesperado de persistencia.

No se registra log de "migracion del modulo completada" porque en esta iteracion no se elimina codigo existente del modulo `tables` y `table-registration` no se modifica.

---

## Alternatives Considered

### Option A

`DELETE /tables/{id}` con `204 No Content` en la primera baja y `404 Not Found` en cualquier llamada posterior o sobre un `id` que nunca existio (idempotencia en el efecto, no en el codigo de respuesta).

#### Pros

* Diferencia claramente entre la llamada que provoco la baja y las llamadas posteriores.
* Contrato HTTP explicito y consistente con el resto del modulo `catalog/product` (post-migracion de `product-deletion`).
* El cliente puede distinguir "lo acabo de borrar yo" de "ya no estaba".
* Se mantiene idempotencia en el efecto (el recurso permanece no existente).

#### Cons

* No es idempotente en el codigo de respuesta, lo que obliga a los clientes idempotencia-aware a no asumir `204` en reintentos ciegos.

### Option B

`DELETE /tables/{id}` con `204 No Content` siempre, nunca `404` (idempotencia estricta de codigo de respuesta).

#### Pros

* Contrato mas simple para clientes que reintentan sin distinguir "primera" de "siguiente".
* Cumpliria literalmente la definicion de operacion idempotente en el sentido de HTTP.

#### Cons

* Pierde la senal de que la operacion fue efectiva en esta llamada concreta.
* Dificulta la trazabilidad de la baja (no se distingue una baja efectiva de un reintento).
* Obliga a mantener una marca logica de "ya borrado" para no mentir con el `204`, lo que contradice la decision de baja fisica aprobada.

### Option C

Baja logica mediante `deleted_at` o `status = deleted` sin eliminar la fila.

#### Pros

* Permitiria restaurar mesas y auditar la baja.
* Compatible con politicas de retencion y borrado asincrono.

#### Cons

* Introduce complejidad de mantenimiento (filtrado en todas las consultas, semantica de "ya borrado" en `404`).
* Contradice la decision aprobada a nivel de diseno de baja fisica.

### Option D

Mantener el formato de cuerpo de error estructurado `{"errors":[{"field":"...","message":"..."}]}` de `table-registration` tambien en `table-deletion` (paralelismo interno con `table-registration` en lugar de con `catalog/product`).

#### Pros

* Coherencia interna del modulo `tables`: todos los endpoints del modulo usarian el mismo formato de error.
* No introduce una divergencia temporal entre `POST /tables` y `DELETE /tables/{id}`.

#### Cons

* Introduce inconsistencia con `catalog/product` (post-migracion de `product-deletion`), rompiendo la unificacion del formato de error a nivel de toda la API.
* Obliga a reescribir el `.feature` de `table-registration` y los step definitions asociados en esta misma iteracion, acoplando dos decisiones (baja de mesa y migracion del formato de error del modulo) que se han querido mantener independientes.
* Contradice la decision de "seguir el modelo HTTP unificado de `catalog/product`" aprobada en diseno.

### Decision

Se selecciona la **Opcion A**, con cuerpo de error neutro (alineado con la Opcion A del contrato y con la decision de "seguir el modelo de `catalog/product`"). La funcionalidad debe responder `204 No Content` en la primera baja efectiva y `404 Not Found` cuando la mesa no exista en el momento de la llamada. La operacion sigue siendo idempotente en su efecto sobre el estado del recurso: la mesa permanece no existente tras cualquier llamada, y unicamente cambia la senal HTTP.

Las opciones B, C y D quedan descartadas por las razones descritas: B contradice la decision de baja fisica y pierde trazabilidad; C contradice la decision de baja fisica aprobada; D introduce inconsistencia con `catalog/product` y obliga a reescribir el `.feature` de `table-registration` en esta iteracion, lo que se aplaza a la unificacion futura del modulo.

---

## Implementation Strategy

### Recommended Order

1. Implementar el caso de uso `TableDeleter` con `run(String id): Result<Void>` siguiendo la logica: `Id.from(id)` primero; si falla, propagar `Result.failure(ValidationError)`; si pasa, `TableRepository.findById(id)`; si vacio, `Result.failure(NotFoundError("Table", id.asString()))`; si presente, `TableRepository.delete(table)` y `Result.success()`. El caso de uso no conoce Spring ni HTTP.
2. Anadir `Optional<Table> findById(Id id)` y `void delete(Table table)` al puerto de salida `TableRepository` (interfaz de dominio).
3. Implementar `findById(Id)` y `delete(Table)` en `JpaTableRepositoryAdapter` delegando en `JpaRepository` (que ya aporta `findById` y `delete(entity)` sobre la PK). No requiere cambios en `SpringDataTableJpaRepository` mas alla de lo que el adaptador necesite; opcionalmente se documenta que `findByCode` ya existe.
4. Implementar `findById(Id)` y `delete(Table)` en `InMemoryTableRepository` para mantener la coherencia del doble de pruebas. El metodo `deleteAll()` existente se mantiene.
5. Anadir `@DeleteMapping("/{id}")` a `TableController`, con mapeo uniforme de `Result` a `204` / `400` / `404` siguiendo la politica del modulo. Cuerpo vacio para los tres casos. El metodo `create(...)` existente y su traduccion de errores con cuerpo estructurado `{"errors":[...]}` no se modifican.
6. Tests unitarios del caso de uso `TableDeleter` (baja efectiva, mesa no encontrada, `id` malformado propagado como `ValidationError`) y del metodo `TableController.delete(...)` (mapeo a `204` / `400` / `404` con cuerpo vacio). Tests de los adaptadores de persistencia ampliados.
7. `.feature` BDD `table-deletion.feature` (nuevo) cubriendo los escenarios de la baja con cuerpo de error neutro, mas las extensiones minimas en `TableSharedSteps` / `TableStepSupport` para los pasos "given a table exists with id ..." y "the table response body should be empty" en el estilo de `product-deletion.feature`. **No se reescribe** `table-registration.feature` en esta iteracion.
8. Reejecutar `./gradlew test` y confirmar que la suite completa pasa sin regresiones.

### Dependencies

* `TableDeleter` depende de `TableRepository`.
* `TableController` depende de `TableCreator` (existente) y de `TableDeleter` (nuevo).
* `TableDeleter` reutiliza `Result<Void>`, `NotFoundError` y `ValidationError` del modulo `shared`.
* La validacion de `id` reutiliza el value object compartido `Id` (factoría `Id.from`).
* `Table` y sus value objects (`TableCode`, `TableCapacity`, `TableLocation`, `TableStatus`) no cambian.
* `TableResponse` no se reutiliza en el nuevo endpoint.

---

## Open Questions

Ninguna pregunta abierta. Todas las decisiones del diseno aprobado estan codificadas en este documento de arquitectura.

Nota contextual: si en el futuro se decide reintroducir un esquema de cuerpo de error concreto para `400` y `404` del modulo `tables` (en el contexto de la unificacion del formato del modulo), el adaptador HTTP de `TableController.delete(...)` tendra que revisarse. Esto no es una pregunta abierta, sino una observacion para una iteracion futura registrada en `## Future Improvements` y `## Deferred Decisions`.

---

## Implementation Notes

* El caso de uso debe retornar un tipo que permita al controlador distinguir "eliminado" de "no encontrado" sin acoplarse a HTTP. La eleccion adoptada es `Result<Void>`: `Result.success()` significa "eliminado" y `Result.failure(NotFoundError)` significa "no encontrado". El `Result<Void>` cubre tres senales: exito, `NotFoundError` y `ValidationError`. El adaptador HTTP distingue las tres por tipo de `DomainError`.
* No se debe acoplar el agregado `Table` a la operacion de borrado. La baja es una decision de orquestacion del caso de uso, que combina `findById` con `delete` sobre el mismo puerto `TableRepository`.
* La respuesta `204 No Content` debe tener el cuerpo literalmente vacio: no incluir representacion de la mesa, `null`, objeto JSON vacio ni espacios en blanco.
* La validacion de formato del `id` se realiza dentro del caso de uso `TableDeleter` mediante la factoría segura `Id.from(String)` del value object compartido `Id`. La factoría chequea `null` y blank explicitamente, y encapsula la `IllegalArgumentException` que `UUID.fromString` lanza internamente en un `Result.failure(ValidationError)`. La `Id.fromStringOrThrow` (renombrada desde `Id.fromString`) conserva la semantica con excepcion para callers que no necesitan `Result` (`Table.fromPrimitives`, tests del VO). El adaptador HTTP no valida el `id` ni captura excepciones del parseo: traduce el `Result` del caso de uso a `204`, `400` o `404` siguiendo la politica uniforme.
* El caso de uso no conoce `HttpStatus` ni `ResponseEntity`. La traduccion de `Result` a codigo HTTP vive exclusivamente en el adaptador HTTP. La forma concreta del cuerpo de error es deliberadamente neutra y queda fuera del alcance de esta iteracion (ver `## Deferred Decisions` en el design).
* El cuerpo de error de `400`, `404` y `500` es deliberadamente neutro: literalmente vacio para `400` y `404` como placeholder, y formato por defecto de Spring para `500`. No se adopta Problem Details ni el formato `{"errors":[{"field":"...","message":"..."}]}` de `table-registration`.
* No se elimina ninguna clase del modulo `tables` en esta iteracion: no existe un equivalente a `ProductErrorHandler` que retirar. La traduccion de errores del `POST /tables` existente sigue viviendo en `TableController.create(...)` con su cuerpo estructurado, y se mantiene intacta. La unica adicion en `TableController` es el metodo `delete(...)` con la nueva politica de mapeo de `Result` a HTTP y cuerpo neutro.
* La unificacion del formato de cuerpo de error del modulo `tables` (y la posible migracion de `table-registration` al cuerpo neutro, con la reescritura correspondiente de `table-registration.feature`) queda fuera del alcance de esta iteracion. Esta registrada en `## Future Improvements` y en `## Deferred Decisions` del documento de diseno. El modulo `tables` convivira temporalmente con dos politicas de error hasta que se aborde esa unificacion.
* En esta iteracion no se actualiza ningun `.feature` de BDD existente del modulo `tables`. El unico `.feature` nuevo es `table-deletion.feature`. Los step definitions `the problem response title should be {string}` y `the problem response detail should be {string}` del paralelo de `product-deletion` no existen en `tables`; en su lugar se introduciran los pasos minimos necesarios en `TableSharedSteps` / `TableStepSupport` para cubrir el nuevo endpoint.
* `InMemoryTableRepository.deleteAll()` se mantiene por compatibilidad con la infraestructura de pruebas existente; la adicion de `findById` y `delete` no altera ese metodo.
