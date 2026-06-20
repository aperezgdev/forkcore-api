# TODO

Plan de ejecucion del proyecto ForkCore API. Abordaje directo: cada tarea se
implementa con tests unitarios, tests de controlador (MockMvc) y tests BDD
(Cucumber + Testcontainers), siguiendo los patrones ya existentes.

## Fase 0 — Higiene y cimientos

- [x] 0.1 CI/CD: anadir `.github/workflows/ci.yml` que ejecute `./gradlew test`
      en push y PR. El runner necesita Docker (Testcontainers). Cache de Gradle.
- [x] 0.2 Limpieza repo: revisar `bin/` (parece artefacto generado) y anadirlo a
      `.gitignore` si procede. Crear `HELP.md` o eliminar la referencia del README.
- [x] 0.3 `compose.yaml`: anadir servicio `api` que construya desde el
      `Dockerfile` y dependa de `postgres` para despliegue local en un comando.

## Fase 1 — Observabilidad y docs de API

- [x] 1.1 Actuator: `spring-boot-starter-actuator`, exponer `health` e `info`.
- [x] 1.2 OpenAPI: `springdoc-openapi-starter-webmvc` (verificar compatibilidad
      con Spring Boot 4) para `/openapi.yaml` y Swagger UI.
- [x] 1.3 Logging estructurado: configuracion minima consistente con
      `OrderErrorAdvice`.

## Fase 2 — Robustez tecnica

- [ ] 2.1 Unificar body de error: migrar `table-registration` al body vacio del
      resto del proyecto. Ajustar tests y `.feature` correspondientes.
- [ ] 2.2 Idempotency persistente: migracion `V4__create_idempotency_keys_table.sql`
      + `JpaIdempotencyKeyStore`. Mantener `InMemoryIdempotencyKeyStore` para
      tests unitarios.
- [ ] 2.3 Concurrencia en `orders`: columna `version BIGINT` (migracion V5) +
      `@Version` en `OrderJpaEntity`. Traducir
      `OptimisticLockingFailureException` a `409`. Revisar `OrderStatusUpdater`.

## Fase 3 — Cerrar gaps del MVP

- [ ] 3.1 `GET /tables/{id}` y `GET /tables`: `TableRetriever` (con paginacion
      minima).
- [ ] 3.2 `PATCH /tables/{id}`: `TableUpdater` reutilizando `FieldUpdate`
      (capacity, code, location, status).
- [ ] 3.3 `GET /orders/{id}` y `GET /orders`: `OrderRetriever`.
- [ ] 3.4 `PATCH /orders/{id}`: `OrderUpdater` para `notes` y `tableId`. No
      edita lines, total ni status (status vive en su endpoint).
- [ ] 3.5 Integracion `orders -> tables`: validar `tableId` en `OrderCreator`
      contra `tables` (nuevo puerto `TableExistenceChecker` o lookup). 400 si no
      existe. Revoca el FR11 out-of-scope de `order-registration`.
- [ ] 3.6 Cerrar deuda `order-status-update`: actualizar `status:` de los docs a
      `completed`/`bdd-completed` para reflejar la realidad (codigo ya commiteado).

## Fase 4 — Producto futuro (fuera de MVP)

- [ ] 4.1 Roles y autenticacion (Spring Security).
- [ ] 4.2 Metricas operativas (tiempos de preparacion/entrega, volumen por
      tramo horario, rotacion de mesas).
- [ ] 4.3 Reservas y asignacion de mesas.
- [ ] 4.4 Codigos QR para pedidos o consulta desde mesa.
- [ ] 4.5 Division de cuenta y cierre de servicio.

## Notas

- Testing: cada feature nueva sigue el patron existente — test unitario del use
  case, test de dominio, `*ControllerTest` (MockMvc), `.feature` + step
  definitions Cucumber con Testcontainers.
- Riesgos:
  - 2.3 (concurrencia) puede afectar a 3.4 (edit) y al `OrderStatusUpdater`
    existente. Coordinar el manejo de `409`.
  - 3.5 (integracion cross-context) introduce acoplamiento `orders -> tables`
    que los docs mantenian deliberadamente diferido.
