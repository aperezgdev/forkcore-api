---
name: product-retrieval-architecture
tags: [catalog, products, ddd, hexagonal, spring-boot]
status: done
date-created: 2026-06-10
---

# Architecture Design: Product Retrieval

## Overview

### Summary

Esta funcionalidad introduce la recuperacion de productos dentro del bounded context `catalog`.

El objetivo es exponer un endpoint HTTP `GET /products` para devolver el catalogo completo o filtrado por `status`, manteniendo el mismo contrato de salida ya definido por `product-registration`.

La arquitectura sigue DDD y hexagonal en los bordes para aislar el dominio del transporte HTTP y de la persistencia, sin introducir complejidad adicional como paginacion en esta iteracion.

### Related Feature Document

* `docs/design/catalog/product-retrieval.md`

---

## Affected Contexts

| Context | Type | Impact |
| ------- | ---- | ------ |
| Catalog | Modified | Anade consulta de productos sobre el modulo existente |
| Orders | Unchanged | Podra consumir productos recuperados mas adelante |

### Notes

El contexto `catalog` sigue siendo propietario del ciclo de vida y consulta del producto.

La nueva capacidad reutiliza el agregado existente y amplia sus puertos de salida para lectura.

---

## Domain Model Impact

### Aggregates

| Aggregate | Type | Purpose |
| --------- | ---- | ------- |
| Product | Modified | Pasa a ser recuperable como proyeccion directa del catalogo |

### Entities

| Entity | Type | Description |
| ------ | ---- | ----------- |
| Product | Modified | Aggregate root ya existente, reutilizado para consultas |

### Value Objects

| Value Object | Type | Description |
| ------------ | ---- | ----------- |
| ProductStatus | Modified | Fuente de validacion del filtro opcional `status` |
| ProductName | Unchanged | Sin cambios |
| ProductDescription | Unchanged | Sin cambios |
| ProductPrice | Unchanged | Sin cambios |
| Id | Unchanged | Sin cambios |

---

## Application Services

### Use Cases

| Use Case | Type | Description |
| -------- | ---- | ----------- |
| ProductRetriever | New | Recupera todos los productos o filtra por estado |

### Application Flow

1. El adaptador HTTP recibe `GET /products` con o sin query param `status`.
2. El controlador delega en `ProductRetriever` pasando el filtro opcional.
3. Si `status` viene informado, la aplicacion lo valida reutilizando las reglas del dominio.
4. `ProductRetriever` consulta `ProductRepository` para obtener todos los productos o los del estado solicitado.
5. La capa de entrada transforma los productos en `ProductResponse` y responde `200 OK` con una lista, incluso si esta vacia.
6. Si el filtro es invalido, la capa de error existente responde `400 Bad Request`.

---

## Domain Services

### Domain Services

Ninguno en esta iteracion.

La validacion necesaria cabe dentro de `ProductStatus` y la orquestacion dentro del caso de uso de aplicacion.

---

## Domain Events

### Events

Ninguno en esta iteracion.

La consulta no introduce eventos de dominio ni integraciones reactivas.

---

## Ports

### Outgoing Ports

| Port | Purpose |
| ---- | ------- |
| ProductRepository | Persistir y recuperar productos del catalogo |

### Port Contracts

`ProductRepository`

- `save(Product product): Product`
- `findAll(): List<Product>`
- `findByStatus(String status): List<Product>`

---

## Repository Impact

### New Repositories

None.

### Modified Repositories

| Repository | Change |
| ---------- | ------ |
| ProductRepository | Anade operaciones de lectura total y filtrada por estado |

---

## External Integrations

### New Integrations

| Integration | Purpose |
| ----------- | ------- |
| PostgreSQL | Consulta de productos persistidos |

### Changes Required

* Permitir consultas sobre la tabla `products`
* Prever filtro por `status` en el adaptador de persistencia

---

## Data Model Impact

### New Persistence Models

None.

### Schema Changes

None en esta iteracion.

Se reutiliza el modelo `products` ya definido por `product-registration`.

### Persistence Notes

La lectura debe devolver la coleccion completa cuando no exista filtro.

Cuando exista filtro por `status`, la persistencia debe aplicar coincidencia exacta sobre el valor normalizado por dominio.

---

## Security Considerations

### Risks

* Filtros `status` no soportados por dominio
* Respuestas ambiguas entre filtro invalido y ausencia real de resultados
* Crecimiento del catalogo sin paginacion

### Mitigations

* Validar `status` con `ProductStatus`
* Responder `400` ante filtros invalidos y `200 []` ante ausencia de resultados
* Mantener el contrato simple para incorporar paginacion mas adelante

---

## Performance Considerations

### Potential Bottlenecks

* Listado completo sin paginacion cuando el catalogo crezca
* Filtrado frecuente por `status` sin optimizacion en persistencia

### Mitigations

* Mantener el caso de uso simple en esta primera iteracion
* Prever indice por `status` si el acceso filtrado se vuelve frecuente

---

## Observability

### Metrics

* Numero de consultas a `GET /products`
* Numero de consultas filtradas por `status`
* Ratio de filtros invalidos

### Logs

* Solicitud de listado recibida
* Consulta filtrada por `status`
* Error de validacion del filtro

---

## Alternatives Considered

### Option A

Resolver el filtro invalido como lista vacia.

#### Pros

* Menor friccion para algunos clientes

#### Cons

* Oculta errores de contrato
* Mezcla entradas invalidas con ausencia legitima de resultados

### Option B

Responder `400 Bad Request` cuando `status` no sea valido.

#### Pros

* Contrato HTTP explicito
* Consistente con la validacion del dominio existente
* Facilita detectar errores de integracion

#### Cons

* Requiere validacion explicita del query param

### Decision

Se selecciona la opcion B.

El filtro `status` debe validarse de forma consistente con el dominio y producir `400 Bad Request` cuando no sea admitido.
