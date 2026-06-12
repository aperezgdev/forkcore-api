---
name: product-update-architecture
tags: [catalog, products, ddd, hexagonal, spring-boot]
status: completed
date-created: 2026-06-11
---

# Architecture Design: Product Update

## Overview

### Summary

Esta funcionalidad introduce la actualizacion parcial de productos dentro del bounded context `catalog`.

El objetivo es exponer `PATCH /products/{id}` para modificar un producto existente reutilizando el modelo, las validaciones y la representacion ya definidos en `product-registration` y reutilizados en `product-retrieval`.

La arquitectura sigue DDD y hexagonal en los bordes para aislar el dominio del transporte HTTP y de la persistencia, manteniendo la semantica PATCH: solo cambian los campos presentes en la request y `id` siempre se toma del path.

### Related Feature Document

* `docs/design/catalog/product-update.md`

### Traceability Notes

* Reutiliza el agregado y value objects introducidos en `docs/architecture/catalog/product-registration.md`
* Mantiene el contrato de salida ya usado por `docs/architecture/catalog/product-retrieval.md`

---

## Affected Contexts

| Context | Type | Impact |
| ------- | ---- | ------ |
| Catalog | Modified | Anade actualizacion parcial sobre el modulo existente de productos |
| Orders | Unchanged | Podra consumir productos actualizados mas adelante |

### Notes

El contexto `catalog` sigue siendo propietario del ciclo de vida del producto.

La nueva capacidad amplia el mismo modulo `catalog/product` sin introducir un nuevo bounded context ni nuevos modelos externos.

---

## Domain Model Impact

### Aggregates

| Aggregate | Type | Purpose |
| --------- | ---- | ------- |
| Product | Modified | Pasa a soportar actualizacion parcial preservando invariantes existentes |

### Entities

| Entity | Type | Description |
| ------ | ---- | ----------- |
| Product | Modified | Aggregate root ya existente, ahora actualizable |

### Value Objects

| Value Object | Type | Description |
| ------------ | ---- | ----------- |
| ProductName | Unchanged | Reutiliza la validacion de nombre existente |
| ProductDescription | Unchanged | Sigue admitiendo `null` y cadena vacia |
| ProductPrice | Unchanged | Reutiliza la validacion de precio existente |
| ProductStatus | Unchanged | Reutiliza la validacion de estado existente |
| Id | Unchanged | Se sigue usando solo como identificador del recurso |

### Domain Notes

No se introduce un agregado nuevo.

La actualizacion debe apoyarse en el propio agregado `Product` y en sus value objects para evitar duplicar reglas de negocio entre alta y edicion.

---

## Application Services

### Use Cases

| Use Case | Type | Description |
| -------- | ---- | ----------- |
| ProductUpdater | New | Recupera un producto existente, aplica cambios parciales validos y persiste el resultado |

### Application Flow

1. El adaptador HTTP recibe `PATCH /products/{id}`.
2. El controlador toma `id` exclusivamente desde el path e ignora cualquier `id` presente en el body.
3. La request se transforma en un objeto de actualizacion parcial capaz de distinguir entre campo omitido y campo informado.
4. `ProductUpdater` consulta `ProductRepository` por `id`.
5. Si el producto no existe, la capa de entrada responde `404 Not Found`.
6. Si existe, el caso de uso aplica solo los cambios presentes en la request.
7. `name`, `price` y `status` se validan reutilizando las mismas reglas del dominio usadas en `product-registration`.
8. `description` puede actualizarse a texto, `null` o `""`.
9. El producto resultante se persiste mediante `ProductRepository`.
10. La capa de entrada devuelve `200 OK` con `ProductResponse`, consistente con `product-registration` y `product-retrieval`.

---

## Domain Services

### Domain Services

Ninguno en esta iteracion.

La logica cabe dentro del agregado `Product`, sus value objects y la orquestacion del caso de uso `ProductUpdater`.

---

## Domain Events

### Events

Ninguno en esta iteracion.

La actualizacion no requiere eventos para cumplir el alcance aprobado.

---

## Ports

### Outgoing Ports

| Port | Purpose |
| ---- | ------- |
| ProductRepository | Recuperar y persistir productos del catalogo |

### Port Contracts

`ProductRepository`

* `save(Product product): Product`
* `findById(Id id): Optional<Product>`
* `findAll(): List<Product>`
* `findByStatus(String status): List<Product>`

### Port Notes

`findById` se anade para soportar la actualizacion por identificador y puede ser reutilizado por futuras consultas puntuales.

No se introduce puerto de entrada adicional; se mantiene el estilo ya usado en las arquitecturas existentes del modulo.

---

## Repository Impact

### New Repositories

None.

### Modified Repositories

| Repository | Change |
| ---------- | ------ |
| ProductRepository | Anade recuperacion por `id` para soportar actualizacion parcial |

---

## External Integrations

### New Integrations

| Integration | Purpose |
| ----------- | ------- |
| PostgreSQL | Leer y actualizar productos persistidos |

### Changes Required

* Permitir lectura puntual por `id` en el adaptador de persistencia
* Reutilizar la misma tabla `products` ya definida en `product-registration`
* Mantener el mapeo de salida consistente con el ya usado en registro y consulta

---

## Data Model Impact

### New Persistence Models

None.

### Schema Changes

None en esta iteracion.

Se reutiliza la tabla `products` ya definida por `product-registration`.

### Persistence Notes

La persistencia debe actualizar solo un producto existente identificado por su clave primaria.

El `id` almacenado nunca debe modificarse aunque venga informado en la request.

Si la request no contiene cambios efectivos, la operacion puede devolver la representacion actual sin alterar el registro persistido.

---

## REST Contract Proposal

### Endpoint

`PATCH /products/{id}`

### Request Body

```json
{
  "name": "Hamburguesa doble",
  "description": "",
  "price": 13.50,
  "status": "inactive",
  "id": "ignored-if-present"
}
```

Notas del contrato:

* Todos los campos del body son opcionales.
* `id` puede venir, pero debe ignorarse siempre.
* Los campos omitidos conservan su valor actual.
* `description` puede informarse como `null` o `""`.

### Success Response

Status: `200 OK`

```json
{
  "id": "0f4f7f2c-6f5d-4d20-91be-0c5dc1f0f1cd",
  "name": "Hamburguesa doble",
  "description": "",
  "price": 13.50,
  "status": "inactive"
}
```

### Error Responses

* `400 Bad Request` para errores de validacion de los campos informados
* `404 Not Found` cuando el producto objetivo no existe
* `500 Internal Server Error` para fallos inesperados de infraestructura

---

## Validation and Error Handling

### Validation Rules

* `name`, cuando venga informado, debe validarse con `ProductName`
* `price`, cuando venga informado, debe validarse con `ProductPrice`
* `status`, cuando venga informado, debe validarse con `ProductStatus`
* `description`, cuando venga informada, debe aceptar texto, `null` y cadena vacia
* `id` del body no participa en validacion funcional porque se ignora

### Error Mapping

* Producto inexistente -> `404 Not Found`
* Campo presente pero invalido segun reglas del dominio -> `400 Bad Request`
* Error tecnico de persistencia no esperado -> `500 Internal Server Error`

### Adapter Notes

El DTO de entrada debe poder distinguir entre:

* campo omitido
* campo presente con valor no nulo
* campo presente con `null`

Esta distincion es necesaria para implementar correctamente PATCH, especialmente en `description`.

---

## Security Considerations

### Risks

* Intento de cambiar el recurso objetivo enviando un `id` distinto en el body
* Divergencia de validaciones entre alta y actualizacion
* Ambiguedad entre campo omitido y campo explicitamente vaciado

### Mitigations

* Tomar siempre el `id` desde el path
* Reutilizar `ProductName`, `ProductPrice` y `ProductStatus` como unica fuente de validacion
* Modelar la request PATCH con presencia explicita de campos

---

## Performance Considerations

### Potential Bottlenecks

* La operacion requiere una lectura por `id` seguida de una escritura

### Mitigations

* Mantener el caso de uso sin integraciones remotas adicionales
* Reutilizar el indice primario por `id`
* Evitar escritura innecesaria cuando no existan cambios efectivos

---

## Observability

### Metrics

* Numero de solicitudes a `PATCH /products/{id}`
* Ratio de `404` en actualizacion de productos
* Ratio de errores de validacion en actualizacion

### Logs

* Solicitud de actualizacion recibida
* Producto actualizado correctamente
* Producto no encontrado
* Error de validacion o persistencia

---

## Alternatives Considered

### Option A

Tratar la operacion como reemplazo completo con `PUT`.

#### Pros

* Contrato mas simple de procesar internamente

#### Cons

* No cumple la semantica parcial aprobada
* Obliga al cliente a reenviar campos sin cambios
* Hace mas dificil distinguir limpieza explicita de omision

### Option B

Mantener `PATCH` con presencia explicita de campos y lectura previa del producto.

#### Pros

* Respeta el alcance funcional aprobado
* Preserva campos omitidos sin logica adicional del cliente
* Permite ignorar `id` del body sin ambiguedad

#### Cons

* Requiere representar internamente la presencia de cada campo
* Introduce una lectura previa antes de persistir

### Decision

Se selecciona la opcion B.

La funcionalidad debe mantenerse como actualizacion parcial sobre el agregado existente, reutilizando el dominio actual y preservando el contrato de salida ya establecido.

---

## Implementation Strategy

### Recommended Order

1. Ampliar `ProductRepository` con recuperacion por `id`
2. Definir el caso de uso `ProductUpdater`
3. Extender el agregado o su logica asociada para aplicar cambios parciales con invariantes existentes
4. Incorporar DTO/adaptador HTTP para `PATCH /products/{id}` con presencia explicita de campos
5. Ajustar el adaptador de persistencia para lectura puntual y actualizacion
6. Reutilizar `ProductResponse` o el mapper equivalente del modulo
7. Anadir tests unitarios y de integracion enfocados en PATCH parcial, validacion y `404`

### Dependencies

* `ProductUpdater` depende de `ProductRepository`
* `ProductController` depende de `ProductUpdater`
* La validacion depende de los value objects ya existentes del dominio `catalog/product`

---

## Implementation Notes

No introducir reglas de negocio nuevas para `name`, `price` o `status`; deben reutilizarse exactamente las ya aprobadas en `product-registration`.

Mantener la representacion HTTP de salida alineada con `product-registration` y `product-retrieval`.

Evitar acoplar el agregado `Product` a DTOs HTTP o detalles de persistencia.

La semantica de PATCH debe implementarse de forma explicita, no inferida a partir de valores por defecto del framework.
