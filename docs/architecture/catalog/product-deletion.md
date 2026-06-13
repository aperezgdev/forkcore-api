---
name: product-deletion-architecture
tags: [catalog, products, ddd, hexagonal, spring-boot]
status: completed
date-created: 2026-06-13
---

# Architecture Design: Product Deletion

## Overview

### Summary

Esta funcionalidad introduce la baja fisica de productos dentro del bounded context `catalog`.

El objetivo es exponer `DELETE /products/{id}` para retirar un producto existente del catalogo, devolviendo `204 No Content` en la primera baja exitosa y `404 Not Found` en cualquier llamada posterior o sobre un `id` que nunca existio, manteniendo el formato del `id` consistente con `product-registration`, `product-retrieval` y `product-update`.

La arquitectura sigue DDD y hexagonal en los bordes para aislar el dominio del transporte HTTP y de la persistencia. La baja se modela como una operacion de comando en el caso de uso `ProductDeleter`, que reutiliza el agregado `Product` y el mismo puerto `ProductRepository` ya introducido por el resto del modulo. No se introduce un nuevo agregado, ni un nuevo value object, ni un evento de dominio, ni un puerto de entrada adicional. Adicionalmente, esta funcionalidad completa la migracion del modulo `catalog/product` a un modelo HTTP unificado: el formato de errores del modulo deja de ser Problem Details y se adopta cuerpo vacio como placeholder neutro, los casos de uso reciben los identificadores como `String` y los validan internamente, y el adaptador HTTP traduce los `Result` por tipo de `DomainError` en lugar de delegar en un advice global. La clase `ProductErrorHandler` y las inner exceptions `InvalidProductException` y `ProductNotFoundException` se eliminan del modulo.

La respuesta exitosa no devuelve cuerpo y no reutiliza la representacion `ProductResponse` usada por alta, consulta y actualizacion. Las respuestas de error devuelven un cuerpo JSON simple y neutro, sin acoplarse a Problem Details, RFC 9457 ni a un esquema concreto de campos. La forma concreta de ese cuerpo queda deliberadamente fuera del alcance de esta iteracion y se definira cuando se unifique el formato de errores del modulo `catalog/product` (ver `## Deferred Decisions` en el documento de diseno).

La validacion de formato del id se delega al value object compartido `Id` mediante la nueva factoría segura `Id.from(String)`, alineada con la convencion del resto de VOs del modulo. El caso de uso `ProductDeleter` recibe el id como `String`, lo valida con `Id.from`, y propaga cualquier `ValidationError` resultante a traves del `Result`. El adaptador HTTP no valida ni captura excepciones: traduce el `Result` del caso de uso a `204`, `400` o `404` por tipo de `DomainError`.

### Related Feature Document

* `docs/design/catalog/product-deletion.md`

### Traceability Notes

* Reutiliza el agregado y value objects introducidos en `docs/architecture/catalog/product-registration.md`
* Reutiliza el puerto `ProductRepository` ampliado en `docs/architecture/catalog/product-update.md` con la operacion de recuperacion por `id`
* Reutiliza los tipos compartidos `Result`, `DomainError` y `NotFoundError` definidos en `src/main/java/com/forkcore/api/shared/domain/`
* No se reaprovecha `ProductResponse`: una baja exitosa no devuelve cuerpo
* Reutiliza la factoría segura `Id.from(String)` introducida como extension del value object compartido `Id` en `shared/domain`, paralela a las factorías `ProductName.from`, `ProductPrice.from` y `ProductStatus.from` del modulo `catalog/product`.
* Elimina `ProductErrorHandler` (y sus inner exceptions `InvalidProductException` y `ProductNotFoundException`) del modulo `catalog/product`, simplificando la trazabilidad del mapeo de errores a codigos HTTP.

---

## Affected Contexts

| Context | Type | Impact |
| ------- | ---- | ------ |
| Catalog | Modified | Anade la operacion de baja fisica sobre el modulo existente de productos |
| Orders | Unchanged | Podra consumir el catalogo ya sin productos eliminados, sin gobernarlos |

### Notes

El contexto `catalog` sigue siendo propietario del ciclo de vida del producto, incluyendo su baja.

La nueva capacidad ampla el mismo modulo `catalog/product` sin introducir un nuevo bounded context, un nuevo agregado ni cambios en otros contextos. Dentro del contexto `Catalog`, la migracion afecta a los cuatro endpoints del modulo `catalog/product`: `GET /products`, `POST /products`, `PATCH /products/{id}` y `DELETE /products/{id}`.

---

## Domain Model Impact

### Aggregates

| Aggregate | Type | Purpose |
| --------- | ---- | ------- |
| Product | Unchanged | Se consulta unicamente para confirmar presencia previa al borrado |

### Entities

| Entity | Type | Description |
| ------ | ---- | ----------- |
| Product | Unchanged | Aggregate root ya existente, leido por `id` y luego borrado por el adaptador de persistencia |

### Value Objects

| Value Object | Type | Description |
| ------------ | ---- | ----------- |
| ProductName | Unchanged | Sin cambios |
| ProductDescription | Unchanged | Sin cambios |
| ProductPrice | Unchanged | Sin cambios |
| ProductStatus | Unchanged | Sin cambios |
| Id | Unchanged | Value object compartido. Se anade la factoría segura `Id.from(String)` que devuelve `Result<Id>`. Implementacion: `Id.from(null)` y `Id.from(blank)` devuelven `Result.failure(new ValidationError("id", "must be a valid UUID"))`; `Id.from(malformedUuid)` (no null, no blank, pero no es UUID valido) tambien devuelve el mismo `Result.failure`. La factoría existente `fromString` se renombra a `fromStringOrThrow` y mantiene su semantica de lanzar `IllegalArgumentException` ante input malformado (no chequea `null` para preservar la semantica original). Se marca `@Deprecated(forRemoval = true)`. Se preserva unica y exclusivamente para los callers que hoy dependen del contrato con excepcion (`Product.fromPrimitives` y `IdTest`). |

### Domain Notes

No se introduce un nuevo agregado.

La baja fisica no es un comportamiento del dominio: el agregado `Product` no conoce la operacion `delete`. Es una decision de orquestacion del caso de uso, que combina una lectura por `id` con una llamada al puerto de salida `ProductRepository`. Esto preserva el principio de que el dominio `catalog/product` no se acopla a detalles del ciclo de vida del almacenamiento.

---

## Application Services

### Use Cases

| Use Case | Type | Description |
| -------- | ---- | ----------- |
| ProductDeleter | New | Recupera un producto por `id`; si no existe responde `NotFoundError`, y si existe solicita su baja fisica a traves del puerto de salida. El caso de uso recibe el id como `String` y delega su validacion en `Id.from(String)`. Si la validacion falla, propaga el `ValidationError` resultante. Si la validacion pasa, realiza `findById` + `delete` y devuelve `Result<Void>` (`success` o `failure(NotFoundError)`). El caso de uso nunca lanza excepciones. |

### Application Flow

1. El adaptador HTTP recibe `DELETE /products/{id}`. Toma el id del path como `String` y no realiza ninguna validacion local.
2. El adaptador HTTP delega en `ProductDeleter.run(id)`.
3. El caso de uso invoca `Id.from(id)`. Si devuelve `Result.failure(ValidationError("id", "<mensaje>"))`, el caso de uso devuelve ese mismo `Result.failure` y termina; el adaptador HTTP lo traduce a `400 Bad Request` con cuerpo vacio.
4. Si `Id.from` devuelve `Result.success(id)`, el caso de uso consulta el producto a traves de `ProductRepository.findById(id)`.
5. Si el producto no existe, el caso de uso devuelve `Result.failure(new NotFoundError("Product", id.asString()))` y termina; el adaptador HTTP lo traduce a `404 Not Found` con cuerpo neutro pendiente de definicion (ver `## Deferred Decisions` en el design).
6. Si el producto existe, el caso de uso solicita su baja fisica a traves de `ProductRepository.delete(product)` y devuelve `Result.success()`. El adaptador HTTP traduce `success` a `204 No Content` con cuerpo vacio.
7. El adaptador HTTP no captura ninguna excepcion en este flujo. La traduccion de `Result` a codigo HTTP es la unica logica HTTP del metodo.

---

## Domain Services

### Domain Services

Ninguno en esta iteracion.

La decision de "borrar solo si existe" cabe en la orquestacion de `ProductDeleter` y no requiere un servicio de dominio.

---

## Domain Events

### Events

Ninguno en esta iteracion.

No se introduce un evento de dominio `ProductDeleted` en el MVP. Si en el futuro otros contextos necesitan reaccionar a la baja, el caso de uso podra emitirlo sin necesidad de modificar el contrato del puerto actual.

---

## Ports

### Outgoing Ports

| Port | Purpose |
| ---- | ------- |
| ProductRepository | Recuperar y persistir productos del catalogo, y dar de baja fisica un producto existente |

### Port Contracts

`ProductRepository`

* `save(Product product): Product`
* `findAll(): List<Product>`
* `findByStatus(String status): List<Product>`
* `findById(Id id): Optional<Product>`
* `delete(Product product): void` *(new)*

### Port Notes

Se anade un unico metodo nuevo al puerto: `void delete(Product product)`. Recibe el agregado ya cargado por `findById` en lugar de recibir unicamente un `Id`.

Razones para preferir `delete(Product)` frente a `deleteById(Id)`:

* El caso de uso ya ha cargado el `Product` para distinguir "existe" de "no existe". Pasar de nuevo solo el `Id` obligaria a duplicar la lectura o a introducir un segundo metodo de puerto, ambos redundantes.
* Recibir el agregado permite, en futuras iteraciones, anadir logica de dominio alrededor del borrado (por ejemplo, eventos de dominio) sin cambiar la firma del puerto.
* Refleja la semantica tipica de los adaptadores de persistencia, donde borrar por entidad es la operacion canonica.

No se introduce un nuevo puerto de salida (consulta, evento, etc.): la operacion se apoya enteramente en `ProductRepository`.

No se introduce un puerto de entrada adicional: se mantiene el estilo ya usado en `ProductCreator` y `ProductUpdater`, donde el caso de uso es directamente invocado por el adaptador HTTP.

El caso de uso `ProductDeleter` no se apoya en el puerto `ProductRepository` para validar el id. La validacion se realiza mediante la factoría `Id.from(String)` del value object `Id`.

---

## Repository Impact

### New Repositories

None.

### Modified Repositories

| Repository | Change |
| ---------- | ------ |
| `JpaProductRepositoryAdapter` | Anade `delete(Product)` delegando en `JpaRepository.delete(entity)` sobre la misma tabla `products` |
| `InMemoryProductRepository` | Anade `delete(Product)` eliminando del mapa en memoria; necesario para mantener la coherencia del doble de pruebas |
| `SpringDataProductJpaRepository` | No requiere cambios: hereda `delete(entity)` de `JpaRepository` |

---

## External Integrations

### New Integrations

| Integration | Purpose |
| ----------- | ------- |
| PostgreSQL | Persistencia de productos, incluyendo la baja fisica |

### Changes Required

* Permitir la operacion de borrado fisico por clave primaria en el adaptador JPA
* Reutilizar la misma tabla `products` ya definida en `product-registration`
* No se requieren migraciones, cambios de esquema, indices adicionales ni cambios en la configuracion de la conexion

---

## Data Model Impact

### New Persistence Models

None.

### Schema Changes

None.

Se reutiliza la tabla `products` ya definida por `product-registration`. La baja elimina filas existentes en tiempo de ejecucion; la estructura de la tabla no cambia.

### Persistence Notes

El `id` del path se utiliza para localizar la fila mediante el indice de clave primaria. Una vez localizada, la fila se elimina por entidad, apoyandose en la operacion estandar del adaptador JPA. Tras la baja, el `id` deja de estar presente en la tabla y cualquier llamada posterior responde `404 Not Found`, igual que cualquier `id` que nunca fue dado de alta.

---

## REST Contract Proposal

### Endpoint

`DELETE /products/{id}`

### Request Body

Ninguno.

El endpoint no acepta `Content-Type` ni lee cuerpo. Un `id` eventualmente presente en el body debe ignorarse por construccion, ya que el adaptador HTTP no deserializa cuerpo para esta operacion.

### Path Variable

* `id`: identificador del producto en el mismo formato UUIDv7 que el resto del modulo.

### Success Response

Status: `204 No Content`

Cuerpo: literalmente vacio. No se devuelve `ProductResponse`, ni representacion parcial del producto eliminado, ni `null`, ni objeto JSON vacio.

### Error Responses

* `400 Bad Request` cuando el `id` del path no respeta el formato esperado. El cuerpo de error es un objeto JSON simple y neutro; la forma concreta de sus campos queda fuera del alcance de esta iteracion y se definira cuando se unifique el formato de errores del modulo `catalog/product` (ver `## Deferred Decisions` en `docs/design/catalog/product-deletion.md`).
* `404 Not Found` cuando el producto identificado por el `id` no existe en el momento de la llamada, ya sea porque nunca fue creado o porque ya fue eliminado en una llamada anterior. El cuerpo de error es un objeto JSON simple y neutro con la misma consideracion sobre la forma concreta aplazada al unificado de errores del modulo.
* `500 Internal Server Error` para fallos inesperados de infraestructura. Cuerpo de error con la misma consideracion sobre la forma concreta aplazada.

En esta iteracion **no se adopta Problem Details**. El adaptador HTTP esta modelado para poder cambiar el cuerpo de error concreto sin reescribir el caso de uso ni el dominio.

### Other endpoints in scope

The migration of `ProductController` affects the other three endpoints in the module. Their HTTP contracts remain unchanged externally (same paths, same methods, same response codes), but their **error body format** changes from `ProblemDetail` to an empty body.

* `GET /products`: 200 with a JSON array of `ProductResponse`. 400 with empty body if the `status` query parameter is not one of the allowed values (validated by `ProductStatus.from`). No 404 path under the current `ProductRetriever` design (it returns an empty list for a valid status filter that matches no products; a future iteration may add 404 for a non-existing status code path).
* `POST /products`: 201 with `Location: /products/{id}` and a `ProductResponse` body. 400 with empty body if any of `name`, `description`, `price`, `status` is invalid (validated by `ProductName.from` / `ProductDescription` / `ProductPrice.from` / `ProductStatus.from`). No 404 path.
* `PATCH /products/{id}`: 200 with a `ProductResponse` body. 400 with empty body if the path `id` is malformed (`Id.from` fails) or if any field in the body is invalid. 404 with empty body if the product does not exist.
* `DELETE /products/{id}`: 204 with empty body. 400 with empty body if the path `id` is malformed. 404 with empty body if the product does not exist or was already deleted.

The full request and response schemas for the other three endpoints are not duplicated here because they are out of scope for this architecture doc (they are documented in the corresponding feature design documents, which will be updated to remove Problem Details references as part of the migration).

---

## Module-Wide Migration Notes

### Uniform Result-to-HTTP mapping

All four controller methods of `ProductController` follow the same pattern:

```
if (result.isFailure()) {
    if (result.error() instanceof NotFoundError) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.badRequest().build();
}
// success path: build the appropriate 2xx response with the success value
```

For `get` and `update`, the success path returns `ResponseEntity.ok(value)`. For `create`, the success path returns `ResponseEntity.created(URI.create("/products/" + value.id())).body(value)`. For `delete`, the success path returns `ResponseEntity.noContent().build()`. No `try/catch` is used. No exception is thrown. The `ProductErrorHandler.invalidProduct(...)` and `ProductErrorHandler.productNotFound(...)` static factories are not called.

### Removal of `ProductErrorHandler`

The following classes are removed from `src/main/java/com/forkcore/api/catalog/product/infrastructure/in/web/` after the migration:

* `ProductErrorHandler` (the `@RestControllerAdvice` class).
* `ProductErrorHandler.InvalidProductException` (the inner `RuntimeException`).
* `ProductErrorHandler.ProductNotFoundException` (the inner `RuntimeException`).

Rationale: the only callers of `ProductErrorHandler` are inside `ProductController`. After the controller is migrated to direct `ResponseEntity` mapping, no class in the project depends on `ProductErrorHandler` or its inner exceptions. Keeping the dead code is not justified.

The removal is verified by a `rg "ProductErrorHandler\."` over `src/` returning zero matches after the migration.

### Updates to the existing BDD `.feature` files

The three existing acceptance `.feature` files are updated to remove the Problem Details assertions:

* `src/test/resources/features/catalog/product-registration.feature`: the Scenario Outline that asserts `"the problem response title should be \"Invalid product\""` and `"the problem response detail should be ..."` is updated to keep only the status-code assertion (and optionally an empty-body assertion, matching the style of `product-deletion.feature`).
* `src/test/resources/features/catalog/product-update.feature`: the Scenario Outline `Reject updates with invalid validated fields` is updated the same way.
* `src/test/resources/features/catalog/product-retrieval.feature`: the Scenario that asserts Problem Details for an invalid `status` filter is updated the same way.

The BDD step definitions `the problem response title should be {string}` and `the problem response detail should be {string}` are removed from `ProductSharedSteps` (no remaining callers). The step `the product response status code should be {int}` and the step `the product response body should be empty` (introduced by `product-deletion`) are kept.

### Updated Use Case signatures (for traceability)

| Use case | Signature change |
| -------- | ---------------- |
| `ProductDeleter` | `run(String id): Result<Void>` (was `run(Id id): Result<Void>`) |
| `ProductUpdater` | `run(String id, ...): Result<Product>` (was `run(Id id, ...): Result<Product>`) |
| `ProductCreator` | unchanged signature; receives its `id` via `Id.create()` inside the use case, not from a path |
| `ProductRetriever` | unchanged signature; receives its `status` filter as `String` (already), validates via `ProductStatus.from` inside the use case |

---

## Validation and Error Handling

### Validation Rules

La unica regla de validacion de entrada comun al modulo es el formato del id. Dicha validacion se realiza en el value object `Id` mediante la factoría `Id.from(String)` que devuelve `Result<Id>`. Las validaciones de campos (`name`, `description`, `price`, `status`) se realizan dentro de los casos de uso (`ProductCreator.run` y `ProductUpdater.run`) y producen `ValidationError` o `CompositeValidationError` a traves de las factorías seguras de los VOs (`ProductName.from`, `ProductPrice.from`, `ProductStatus.from`). El adaptador HTTP no valida nada; traduce el `Result` del caso de uso a codigo HTTP.

### Error Mapping

* id del path malformado -> `Result.failure(ValidationError("id", "must be a valid UUID"))` -> `400 Bad Request` con cuerpo vacio. La deteccion se realiza dentro del caso de uso, no en el adaptador HTTP.
* Producto inexistente -> `Result.failure(NotFoundError("Product", id.asString()))` -> `404 Not Found` con cuerpo vacio.
* Validacion de campo invalida (en `create` o `update`) -> `Result.failure(ValidationError(field, message))` o `Result.failure(CompositeValidationError)` -> `400 Bad Request` con cuerpo vacio. La deteccion se realiza dentro del caso de uso, no en el adaptador HTTP.
* Error tecnico de persistencia no esperado -> `500 Internal Server Error` con el formato por defecto de Spring (no especificado por esta arquitectura; ver `## Deferred Decisions` en el design).

El cuerpo de error es deliberadamente neutro: las respuestas de `400` y `404` del modulo llevan cuerpo vacio como placeholder, y el `500` se sirve en el formato por defecto de Spring. La eleccion de `Result` como tipo de retorno de los casos de uso garantiza que la unica informacion que sale del nucleo es una senal de exito o un `DomainError`, dejando al adaptador HTTP entera libertad para definir la representacion HTTP del error mas adelante, sin acoplar el caso de uso al formato del cuerpo.

### Adapter Notes

* Todos los metodos de `ProductController` siguen la politica uniforme de mapeo de `Result` a HTTP definida en `## Module-Wide Migration Notes`. Ninguno captura `IllegalArgumentException` ni lanza `InvalidProductException` / `ProductNotFoundException`.
* El caso de uso no conoce `HttpStatus`, `ResponseEntity` ni el formato del cuerpo de error. La traduccion de `Result` a codigo HTTP vive exclusivamente en el adaptador HTTP.
* El `204` debe tener el cuerpo literalmente vacio: sin `Content-Length` con cuerpo residual, sin `null`, sin `{}`, sin espacios en blanco. Los `400` y `404` de este modulo tambien tienen cuerpo vacio (placeholder neutro; ver `## Deferred Decisions` en el design).
* Cualquier `IllegalArgumentException` que se filtre desde `Id.fromStringOrThrow` (lo cual no deberia ocurrir en el camino de los endpoints del modulo, porque `Id.from` la encapsula en `Result.failure(ValidationError)` y `Id.fromStringOrThrow` no se usa en el camino HTTP tras la migracion) se considera un error inesperado y se traduce a `500 Internal Server Error` por el manejo por defecto de Spring.

---

## Security Considerations

### Risks

* `id` con formato manipulado para agotar caminos de validacion o provocar excepciones no controladas
* Divergencia entre la validacion del `id` del path y la usada en `product-registration` / `product-update`
* Acoplamiento futuro del cuerpo de error a un esquema concreto que filtre informacion innecesaria

### Mitigations

* Reutilizar el value object compartido `Id` como unica fuente de validacion de formato del `id` del path, consistente con el resto del modulo
* Mantener el caso de uso sin acceso al cuerpo HTTP, de forma que un eventual `id` enviado en body sea ignorado por construccion
* Definir el cuerpo de error como un objeto JSON simple y neutro, sin Problem Details ni campos sensibles, para no exponer detalles internos del fallo
* Validacion de id centralizada en el value object `Id`: ningun adaptador HTTP del modulo debe capturar excepciones ni validar el id localmente. Esto elimina vectores de inconsistencia entre endpoints y reduce el riesgo de que un id malformado se traduzca a un codigo HTTP incorrecto en algun path.
* Eliminar `ProductErrorHandler` no reduce la postura de seguridad: la traduccion de errores a 400/404 sigue siendo explicita y trazable, y la eliminacion de `ProblemDetail` reduce la exposicion de informacion sensible en las respuestas de error (los `ProblemDetail` previos incluian `title`, `detail`, y en algunos casos `errors` con la lista de campos invalidos).

---

## Performance Considerations

### Potential Bottlenecks

* La operacion requiere una lectura por `id` seguida de un borrado fisico
* La latencia estara dominada por el acceso a la base de datos

### Mitigations

* Mantener el caso de uso sin integraciones remotas adicionales
* Reutilizar el indice primario por `id` ya existente en la tabla `products`
* Evitar lecturas adicionales (por ejemplo, comprobaciones de existencia redundantes mas alla de `findById`) dentro del caso de uso
* No introducir cache ni procesamiento asincrono: la baja es sincrona y determinista

---

## Observability

### Metrics

* Numero de solicitudes a `DELETE /products/{id}`
* Ratio de `204` frente al total de solicitudes (bajas efectivas)
* Ratio de `404` frente al total de solicitudes (id inexistente o ya eliminado)
* Ratio de `400` frente al total de solicitudes (id malformado)

### Logs

* Solicitud de baja recibida
* Baja efectiva (id eliminado)
* Producto no encontrado (id inexistente en el momento de la llamada)
* `id` rechazado por formato invalido
* Error inesperado de persistencia
* Migracion del modulo completada: `ProductErrorHandler` y las inner exceptions eliminadas.

---

## Alternatives Considered

### Option A

`DELETE /products/{id}` con `204 No Content` en la primera baja y `404 Not Found` en cualquier llamada posterior o sobre un `id` que nunca existio.

#### Pros

* Diferencia claramente entre la llamada que provoco la baja y las llamadas posteriores
* Contrato HTTP explicito y consistente con el resto del modulo
* El cliente puede distinguir "lo acabo de borrar yo" de "ya no estaba"
* Se mantiene idempotencia en el efecto (el recurso permanece no existente)

#### Cons

* No es idempotente en el codigo de respuesta, lo que obliga a los clientes idempotencia-aware a no asumir `204` en reintentos ciegos

### Option B

`DELETE /products/{id}` con `204 No Content` siempre, nunca `404` (idempotencia estricta de codigo de respuesta).

#### Pros

* Contrato mas simple para clientes que reintentan sin distinguir "primera" de "siguiente"
* Cumpliria literalmente la definicion de operacion idempotente en el sentido de HTTP

#### Cons

* Pierde la senal de que la operacion fue efectiva en esta llamada concreta
* Dificulta la trazabilidad de la baja (no se distingue una baja efectiva de un reintento)
* Obliga a mantener una marca logica de "ya borrado" para no mentir con el `204`, lo que contradice la decision de baja fisica

### Option C

Baja logica mediante `deleted_at` o `status = deleted` sin eliminar la fila.

#### Pros

* Permitiria restaurar productos y auditar la baja
* Compatible con politicas de retencion y borrado asincrono

#### Cons

* Introduce complejidad de mantenimiento (filtrado en todas las consultas, semantica de "ya borrado" en `404`)
* Contradice la decision aprobada a nivel de diseno de baja fisica

### Option D

Mantener `ProductErrorHandler` y `ProblemDetail` para `create`, `get` y `update`, y solo migrar `delete` al nuevo patron uniforme.

#### Pros

* Menos trabajo de migracion.
* No rompe los `.feature` existentes de `product-registration`, `product-update` y `product-retrieval`, que seguirian con sus aserciones de `ProblemDetail`.

#### Cons

* El modulo queda con dos politicas de error conviviendo: Problem Details para tres endpoints, cuerpo vacio para uno.
* Esa coexistencia es exactamente la inconsistencia que se quiere evitar al unificar el formato de errores del modulo.
* Rompe la trazabilidad uniforme: parte del mapeo de `Result` a HTTP vive en el adaptador HTTP y parte vive en el advice global.

#### Decision

Rejected.

### Decision

Se selecciona la opcion A.

La funcionalidad debe responder `204 No Content` en la primera baja efectiva y `404 Not Found` cuando el producto no exista en el momento de la llamada. La operacion sigue siendo idempotente en su efecto sobre el estado del recurso: el producto permanece no existente tras cualquier llamada, y unicamente cambia la senal HTTP. Las opciones B y C quedan descartadas por las razones descritas y por contradecir decisiones aprobadas en `docs/design/catalog/product-deletion.md`.

---

## Implementation Strategy

### Recommended Order

1. Renombrar `Id.fromString` a `Id.fromStringOrThrow` (sin cambio de comportamiento) y marcarla `@Deprecated(forRemoval = true)`. Anadir la factoría segura `Id.from(String): Result<Id>` que devuelve `Result.failure(new ValidationError("id", "must be a valid UUID"))` ante input null, blank, o malformado. Mensaje constante y definitivo (no diferido).
2. Actualizar `IdTest` para reflejar las dos factorías: `fromStringOrThrow` con la semantica anterior de excepcion, y `from` con la nueva semantica de `Result` (camino valido, null, blank, malformado).
3. Cambiar la firma de `ProductDeleter.run(Id id)` a `ProductDeleter.run(String id)`. El caso de uso invoca `Id.from(id)` como primer paso y propaga cualquier `ValidationError`.
4. Cambiar la firma de `ProductUpdater.run(Id id, ...)` a `ProductUpdater.run(String id, ...)`. Misma logica: `Id.from(id)` como primer paso, propagacion de `ValidationError`.
5. Reescribir `ProductController.delete(...)` para que no valide localmente, no capture excepciones, no use `ProductErrorHandler`. Mapeo uniforme: `Result.success` -> 204; `Result.failure(NotFoundError)` -> 404 con cuerpo vacio; cualquier otro `Result.failure(DomainError)` -> 400 con cuerpo vacio.
6. Reescribir `ProductController.update(...)` con la misma politica: pasar el `id` como `String` a `productUpdater.run(id, ...)`; mapear `Result` de la misma forma que `delete`. Eliminar el `Id.fromString(id)` y los `throw ProductErrorHandler.invalidProduct(...)` / `throw ProductErrorHandler.productNotFound(...)` existentes.
7. Reescribir `ProductController.create(...)` con la misma politica: eliminar el `throw ProductErrorHandler.invalidProduct(...)`; mapear `Result` de la misma forma (success -> 201 con Location, `ValidationError` -> 400 con cuerpo vacio). `NotFoundError` es inalcanzable en `create` y se trata como 400 (fallthrough).
8. Reescribir `ProductController.get(...)` con la misma politica: eliminar el `throw ProductErrorHandler.invalidProduct(...)`; mapear `Result` de la misma forma (success -> 200, `ValidationError` -> 400 con cuerpo vacio). `NotFoundError` es inalcanzable en `get` con el diseno actual y se trata como 400 (fallthrough).
9. Eliminar las clases `ProductErrorHandler`, `ProductErrorHandler.InvalidProductException` y `ProductErrorHandler.ProductNotFoundException` de `src/main/java/com/forkcore/api/catalog/product/infrastructure/in/web/`. Verificar con `rg "ProductErrorHandler\."` que no quedan callers.
10. Actualizar los tests unitarios y de integracion: anadir test de `Id.from` (camino valido, null, blank, malformado); anadir test de `ProductDeleter` para el caso de id malformado (propaga `ValidationError`); actualizar los tests de `ProductController.delete`, `update`, `create`, `get` para reflejar el nuevo mapeo (sin `ProblemDetail`, sin `assertThrows` de las inner exceptions); eliminar los tests que dependian de `ProductErrorHandler`.
11. Actualizar las tres `.feature` existentes (`product-registration.feature`, `product-update.feature`, `product-retrieval.feature`): eliminar las aserciones `the problem response title should be ...` y `the problem response detail should be ...`; mantener las aserciones de codigo de estado. Anadir aserciones de cuerpo vacio donde tenga sentido, en el mismo estilo que `product-deletion.feature`.
12. Eliminar los step definitions `the problem response title should be {string}` y `the problem response detail should be {string}` de `ProductSharedSteps`. Verificar con `rg` que no quedan referencias en `.feature`.
13. Reejecutar `./gradlew test` y confirmar que la suite completa pasa sin regresiones. Verificar que `rg "ProductErrorHandler"` sobre `src/` no produce matches.

### Dependencies

* `ProductDeleter` depende de `ProductRepository`
* `ProductController` depende de `ProductDeleter`
* `ProductDeleter` reutiliza `Result<Void>` y `NotFoundError` del modulo `shared`
* La validacion de `id` reutiliza el value object compartido `Id`

---

## Implementation Notes

* El caso de uso debe retornar un tipo que permita al controlador distinguir "eliminado" de "no encontrado" sin acoplarse a HTTP. La eleccion adoptada es `Result<Void>`: `Result.success()` significa "eliminado" y `Result.failure(NotFoundError)` significa "no encontrado". El `Result<Void>` cubre tres senales: exito, `NotFoundError` y `ValidationError`. El adaptador HTTP distingue las tres por tipo de `DomainError`.
* No se debe acoplar el agregado `Product` a la operacion de borrado. La baja es una decision de orquestacion del caso de uso, que combina `findById` con `delete` sobre el mismo puerto.
* La respuesta `204 No Content` debe tener el cuerpo literalmente vacio: no incluir representacion del producto, `null`, objeto JSON vacio ni espacios en blanco.
* La validacion de formato del `id` se realiza dentro de los casos de uso `ProductDeleter` y `ProductUpdater` mediante la factoría segura `Id.from(String)` del value object compartido `Id`. La factoría chequea `null` y blank explicitamente, y encapsula la `IllegalArgumentException` que `UUID.fromString` lanza internamente en un `Result.failure(ValidationError)`. La `Id.fromStringOrThrow` (renombrada desde `Id.fromString`) conserva la semantica con excepcion para callers que no necesitan `Result` (`Product.fromPrimitives`, `IdTest`). El adaptador HTTP no valida el id ni captura excepciones del parseo: traduce el `Result` del caso de uso a `204`, `400` o `404` siguiendo la politica uniforme del modulo.
* El caso de uso no conoce `HttpStatus` ni `ResponseEntity`. La traduccion de `Result` a codigo HTTP vive exclusivamente en el adaptador HTTP. La forma concreta del cuerpo de error es deliberadamente neutra y queda fuera del alcance de esta iteracion (ver `## Deferred Decisions` en el design).
* El cuerpo de error de `400`, `404` y `500` es deliberadamente neutro: un objeto JSON simple sin acoplamiento a Problem Details ni a un esquema concreto de campos, cuya forma concreta queda fuera del alcance de esta iteracion (ver `## Deferred Decisions` en `docs/design/catalog/product-deletion.md`).
* Eliminacion de `ProductErrorHandler`: tras la migracion, las inner exceptions `InvalidProductException` y `ProductNotFoundException` tambien se eliminan. El adaptador HTTP no lanza ninguna excepcion de validacion ni de no-encontrado; el mapeo de `Result` a HTTP es la unica fuente de codigos de error del modulo. Esto rompe cualquier expectativa de que una respuesta de error del modulo lleve `ProblemDetail`; los clientes deben consumir solo el codigo de estado HTTP y, si lo necesitan, un cuerpo vacio.
