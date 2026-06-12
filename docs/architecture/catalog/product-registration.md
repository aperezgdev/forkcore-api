---
name: product-registration-architecture
tags: [catalog, products, ddd, hexagonal, spring-boot]
status: done
date-created: 2026-06-09
---

# Architecture Design: Product Registration

## Overview

### Summary

Esta funcionalidad introduce el alta de productos dentro del bounded context `catalog`.

El objetivo es exponer un endpoint HTTP para registrar productos con los campos `id`, `name`, `description`, `price` y `status`, aplicando la regla de negocio de asignar `active` cuando `status` no venga informado.

La arquitectura sigue DDD y hexagonal en los bordes para aislar el dominio del transporte HTTP y de la persistencia, sin introducir un puerto de entrada para el caso de uso.

### Related Feature Document

* `docs/design/catalog/product-registration.md`

---

## Affected Contexts

| Context | Type | Impact |
| ------- | ---- | ------ |
| Catalog | New | Introduce la gestion inicial de alta de productos |
| Orders | Unchanged | Podra consumir productos mas adelante, sin gobernarlos |

### Notes

El contexto `catalog` es propietario del ciclo de vida del producto.

El contexto de pedidos no debe contener reglas de creacion ni persistencia del producto.

---

## Domain Model Impact

### Aggregates

| Aggregate | Type | Purpose |
| --------- | ---- | ------- |
| Product | New | Representa un producto del catalogo y asegura sus invariantes de creacion |

### Entities

| Entity | Type | Description |
| ------ | ---- | ----------- |
| Product | New | Aggregate root del contexto `catalog` |

### Value Objects

| Value Object | Type | Description |
| ------------ | ---- | ----------- |
| ProductName | New | Nombre valido del producto |
| ProductDescription | New | Descripcion del producto, admite vacio |
| ProductPrice | New | Precio valido del producto |
| ProductStatus | New | Estado del producto |
| Id | New | Identificador compartido ubicado en `shared`, generado como UUIDv7 |

---

## Application Services

### Use Cases

| Use Case | Type | Description |
| -------- | ---- | ----------- |
| ProductCreator | New | Crea un producto valido y lo persiste en el catalogo |

### Application Flow

1. El adaptador HTTP recibe `POST /products`.
2. La request se transforma en los parametros requeridos por `ProductCreator`.
3. `ProductCreator` delega la creacion en el dominio.
4. El dominio construye `Product` y aplica la regla `status = active` cuando no viene informado.
5. El puerto de salida `ProductRepository` persiste el agregado.
6. La capa de entrada devuelve `201 Created` con el producto creado.

---

## Domain Services

### Domain Services

Ninguno en esta iteracion.

La logica cabe dentro del agregado `Product` y sus value objects.

---

## Domain Events

### Events

Ninguno en esta iteracion.

Se pueden introducir mas adelante si otros contextos necesitan reaccionar al alta de productos.

---

## Ports

### Outgoing Ports

| Port | Purpose |
| ---- | ------- |
| ProductRepository | Persistir y recuperar productos del catalogo |

### Port Contracts

`ProductRepository`

- `save(Product product): Product`

---

## Repository Impact

### New Repositories

| Repository | Purpose |
| ---------- | ------- |
| ProductRepository | Almacena los productos del catalogo |

### Modified Repositories

None.

---

## External Integrations

### New Integrations

| Integration | Purpose |
| ----------- | ------- |
| PostgreSQL | Persistencia de productos |

### Changes Required

* Crear tabla `products`
* Configurar adaptador de persistencia del contexto `catalog`

---

## Data Model Impact

### New Persistence Models

| Model | Description |
| ----- | ----------- |
| products | Tabla de productos del catalogo |

### Schema Changes

#### products

| Column | Type | Notes |
| ------ | ---- | ----- |
| id | UUID | Primary key |
| name | VARCHAR | Required |
| description | VARCHAR or TEXT | Optional, puede venir vacia |
| price | NUMERIC | Required, no negativo |
| status | VARCHAR | Required, default `active` a nivel aplicacion |

### Persistence Notes

El default de `status` debe vivir primero en dominio/aplicacion para mantener la regla dentro del core.

El esquema puede duplicar ese default mas adelante como proteccion tecnica, pero no como unica fuente de verdad.

---

## Security Considerations

### Risks

* Requests con campos vacios o mal formateados
* Precio invalido o manipulacion de valores negativos
* Estados arbitrarios no soportados por dominio
* Dependencia externa para generacion de UUIDv7

### Mitigations

* Validacion de formato en el adaptador HTTP
* Validacion de invariantes en value objects y agregado
* Lista explicita de estados admitidos en `ProductStatus`
* Encapsular la libreria de UUIDv7 dentro del value object compartido `Id`

---

## Performance Considerations

### Potential Bottlenecks

* Ninguno relevante en esta iteracion por tratarse de una unica escritura sincrona

### Mitigations

* Mantener el caso de uso simple y sin integraciones remotas
* Usar un indice primario por `id`

---

## Observability

### Metrics

* Numero de productos creados
* Ratio de fallos de validacion en alta de producto

### Logs

* Solicitud de alta recibida
* Producto creado correctamente
* Error de validacion o persistencia

---

## Alternatives Considered

### Option A

Crear el endpoint dentro del contexto de pedidos.

#### Pros

* Menos estructura inicial

#### Cons

* Mezcla responsabilidades
* Dificulta evolucionar catalogo por separado

### Option B

Crear un contexto `catalog` separado con puertos y adaptadores propios.

#### Pros

* Limites de dominio claros
* Mejor mantenibilidad y evolucion
* Encaja con DDD y hexagonal

#### Cons

* Mas estructura desde el inicio

### Decision

Se selecciona la opcion B.

La gestion del producto pertenece a `catalog` y debe implementarse como un contexto aislado con su propio agregado, value objects, caso de uso y adaptadores.

---

## Implementation Strategy

### Recommended Order

1. Crear estructura de paquetes del contexto `catalog`
2. Implementar value objects y agregado `Product`
3. Definir el puerto de salida `ProductRepository`
4. Implementar caso de uso `ProductCreator`
5. Implementar adaptador REST para `POST /products`
6. Implementar adaptador de persistencia PostgreSQL
7. Anadir tests unitarios del dominio y tests del caso de uso

### Dependencies

* `ProductCreator` depende de `ProductRepository`
* `ProductController` depende de `ProductCreator`
* `Product` depende del value object compartido `shared` para el identificador

---

## Package Proposal

```text
src/main/java/com/forkcore/api/shared/domain/
  Id.java
src/main/java/com/forkcore/api/catalog/product/
  domain/
    Product.java
    ProductRepository.java
    vo/
      ProductName.java
      ProductDescription.java
      ProductPrice.java
      ProductStatus.java
  application/
    ProductCreator.java
  infrastructure/in/web/
    ProductController.java
    CreateProductRequest.java
    ProductResponse.java
  infrastructure/out/persistence/
    ProductEntity.java
    JpaProductRepository.java
    ProductRepositoryAdapter.java
```

### Notes

El nombre exacto de clases puede ajustarse, pero la direccion de dependencias debe mantenerse hacia adentro.

El dominio no debe depender de Spring, HTTP ni JPA.

Se evita crear carpetas artificiales con un solo archivo salvo cuando aportan una agrupacion clara, como `vo/`.

Se acepta el acoplamiento tecnico a una libreria externa para UUIDv7, siempre encapsulado en `shared/domain/Id.java`.

---

## REST Contract Proposal

### Endpoint

`POST /products`

### Request Body

```json
{
  "name": "Hamburguesa clasica",
  "description": "Pan brioche, carne y queso",
  "price": 12.50,
  "status": "active"
}
```

`status` es opcional. Si no viene, el sistema asigna `active`.

`description` puede venir vacia.

### Success Response

Status: `201 Created`

```json
{
  "id": "0f4f7f2c-6f5d-4d20-91be-0c5dc1f0f1cd",
  "name": "Hamburguesa clasica",
  "description": "Pan brioche, carne y queso",
  "price": 12.50,
  "status": "active"
}
```

### Error Responses

* `400 Bad Request` para errores de validacion de entrada
* `500 Internal Server Error` para fallos inesperados de infraestructura

---

## Open Questions

* Si `status` permitira solo `active` e `inactive` en esta primera etapa
* Que libreria concreta de Google se utilizara finalmente para UUIDv7

---

## Implementation Notes

No introducir logica de negocio en anotaciones del controlador mas alla de validacion basica de formato.

La regla de valor por defecto de `status` debe vivir en el core del caso de uso o del agregado.

Evitar acoplar el agregado `Product` a estructuras de persistencia o DTOs HTTP.

No introducir `Command` ni interfaz de puerto de entrada para `ProductCreator` en esta iteracion.
