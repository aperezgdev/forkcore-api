# ForkCore API

## Espanol

ForkCore API es una API backend para la operativa interna de restaurantes. Su objetivo es ayudar a gestionar mesas, pedidos y productos de forma mas clara, trazable y preparada para automatizacion futura.

Este repositorio se encuentra en una fase temprana. La base tecnica del proyecto ya existe, mientras que el alcance funcional y los flujos de negocio siguen en definicion.

### Estado del proyecto

- Estado actual: fase inicial
- Enfoque principal: operativa interna del restaurante
- Licencia: Apache-2.0

### Vision

ForkCore API busca convertirse en la base de un sistema de gestion operativa para restaurantes, centrado en el servicio de sala y el seguimiento de pedidos.

La idea inicial es ofrecer una API que permita:

- gestionar mesas
- crear y actualizar pedidos
- asociar pedidos a mesas
- gestionar productos
- hacer seguimiento del estado de cada pedido
- preparar el sistema para futuras automatizaciones y analitica operativa

### Problema que quiere resolver

Muchos restaurantes siguen operando con procesos manuales o herramientas poco adaptadas al flujo real del servicio. Eso suele traducirse en:

- poca visibilidad del estado de los pedidos
- errores al tomar comandas
- dificultad para relacionar pedidos con mesas
- poca trazabilidad sobre el servicio
- baja capacidad para automatizar tareas operativas

ForkCore API nace para servir como base backend de una solucion orientada a ordenar ese flujo.

### Alcance inicial del MVP

El MVP esta orientado a cubrir las necesidades operativas mas inmediatas:

- gestion de mesas
- alta de pedidos
- actualizacion de pedidos
- asociacion de pedidos a una mesa
- gestion basica de productos
- seguimiento del estado del pedido

Estados de pedido previstos en esta primera etapa:

- `pending`
- `in_progress`
- `ready`
- `delivered`
- `cancelled`

Estos estados representan una primera propuesta y podran ajustarse a medida que evolucione el modelo de dominio.

### Roadmap inicial

Lineas de trabajo previstas para las siguientes iteraciones:

- modelado mas detallado del dominio de mesas, pedidos y productos
- definicion de reglas operativas del ciclo de vida de un pedido
- validacion de flujos de trabajo de sala
- persistencia y consultas sobre pedidos y mesas
- evolucion de la API REST

### Ideas futuras

Posibles evoluciones del producto fuera del MVP inicial:

- sistema de metricas operativas
- analitica de tiempos de preparacion y entrega
- seguimiento de volumen de pedidos por tramo horario
- rotacion y ocupacion de mesas
- historial operativo por mesa
- roles de usuario como sala, cocina, gerente o administrador
- integracion con cocina o pantallas de seguimiento
- codigos QR para pedidos o consulta desde mesa
- reservas y asignacion de mesas
- division de cuenta y cierre de servicio

### Stack actual

La base tecnica actual del repositorio incluye:

- Java 25
- Gradle
- Spring Boot 4
- Spring Web MVC
- PostgreSQL
- JUnit

### Puesta en marcha local

Requisitos:

- Java 25
- entorno compatible con Gradle Wrapper
- Docker y Docker Compose plugin para levantar PostgreSQL localmente

Comandos utiles:

```bash
docker compose up -d
./gradlew test
./gradlew bootRun
```

Variables de entorno esperadas por la API:

- `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5432/forkcore`)
- `SPRING_DATASOURCE_USERNAME` (default `forkcore`)
- `SPRING_DATASOURCE_PASSWORD` (default `forkcore`)

La base de datos local se versiona con Flyway y el fichero `compose.yaml` solo levanta PostgreSQL.

Los tests de aceptacion usan Testcontainers para arrancar un PostgreSQL real durante la ejecucion.

La API tambien incluye un `Dockerfile` multi-stage listo para generar una imagen de despliegue.

Nota: el proyecto esta en una fase muy inicial y todavia no expone funcionalidad de negocio implementada.

### Estructura del repositorio

```text
.
|- src/
|  |- main/
|  |- test/
|- docs/
|  |- design/
|  |- architecture/
|- build.gradle
|- settings.gradle
|- AGENTS.md
```

### Documentacion relacionada

- `docs/design/feature-template.md`: plantilla para diseno funcional
- `docs/architecture/template.md`: plantilla para diseno tecnico
- `AGENTS.md`: flujo de trabajo y reglas del repositorio

### Contribucion

Este repositorio esta en una etapa de arranque. Antes de implementar cambios relevantes, conviene alinear:

- alcance funcional
- reglas de negocio
- decisiones de arquitectura
- criterios de prueba

El flujo documental del repositorio favorece definir primero el diseno funcional y despues el diseno de arquitectura.

### Licencia

Este proyecto se distribuye bajo la licencia `Apache-2.0`.

## English

ForkCore API is a backend API for internal restaurant operations. Its goal is to help restaurants manage tables, orders, and products in a clearer, more traceable way, while preparing the platform for future automation.

This repository is currently in an early stage. The technical foundation is already in place, while the functional scope and business workflows are still being defined.

### Project status

- Current status: early stage
- Primary focus: internal restaurant operations
- License: Apache-2.0

### Vision

ForkCore API aims to become the foundation of an operational management system for restaurants, focused on table service and order tracking.

The initial goal is to provide an API that can:

- manage tables
- create and update orders
- associate orders with tables
- manage products
- track the status of each order
- prepare the system for future automation and operational analytics

### Problem it aims to solve

Many restaurants still rely on manual processes or tools that do not fit the real service workflow. That often leads to:

- poor visibility into order status
- mistakes while taking orders
- weak relation between orders and tables
- limited service traceability
- low capacity for operational automation

ForkCore API is intended to serve as the backend foundation for a solution that brings structure to that workflow.

### Initial MVP scope

The MVP is focused on the most immediate operational needs:

- table management
- order creation
- order updates
- linking orders to tables
- basic product management
- order status tracking

Expected order states in this first stage:

- `pending`
- `in_progress`
- `ready`
- `delivered`
- `cancelled`

These states are an initial proposal and may evolve as the domain model becomes more defined.

### Initial roadmap

Planned work for the next iterations:

- deeper domain modeling for tables, orders, and products
- definition of operational rules for the order lifecycle
- validation of dining room workflows
- persistence and query capabilities for orders and tables
- evolution of the REST API

### Future ideas

Possible product evolutions beyond the initial MVP:

- operational metrics system
- preparation and delivery time analytics
- order volume tracking by time window
- table occupancy and rotation insights
- operational history per table
- user roles such as waiter, kitchen, manager, or administrator
- kitchen integration or tracking screens
- QR-based table interactions
- reservations and table assignment
- bill splitting and service closing flows

### Current stack

The current technical foundation of the repository includes:

- Java 25
- Gradle
- Spring Boot 4
- Spring Web MVC
- PostgreSQL
- JUnit

### Local setup

Requirements:

- Java 25
- environment compatible with the Gradle Wrapper
- Docker and the Docker Compose plugin to run PostgreSQL locally

Useful commands:

```bash
docker compose up -d
./gradlew test
./gradlew bootRun
```

Environment variables expected by the API:

- `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5432/forkcore`)
- `SPRING_DATASOURCE_USERNAME` (default `forkcore`)
- `SPRING_DATASOURCE_PASSWORD` (default `forkcore`)

The local database schema is versioned with Flyway, and `compose.yaml` only starts PostgreSQL.

Acceptance tests use Testcontainers to run a real PostgreSQL instance during execution.

The API also includes a multi-stage `Dockerfile` ready for deployment image builds.

Note: the project is still at a very early stage and does not yet expose implemented business functionality.

### Repository structure

```text
.
|- src/
|  |- main/
|  |- test/
|- docs/
|  |- design/
|  |- architecture/
|- build.gradle
|- settings.gradle
|- AGENTS.md
```

### Related documentation

- `docs/design/feature-template.md`: functional design template
- `docs/architecture/template.md`: technical design template
- `AGENTS.md`: repository workflow and operating rules

### Contributing

This repository is still at an early stage. Before implementing major changes, it is best to align on:

- functional scope
- business rules
- architecture decisions
- testing criteria

The repository workflow favors defining the functional design first and the architecture design afterwards.

### License

This project is distributed under the `Apache-2.0` license.
