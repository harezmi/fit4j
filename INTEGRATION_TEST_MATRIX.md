# FIT4J 1.0 Integration Test Matrix

This matrix records the executable coverage required for the stable integrations listed in
`COMPATIBILITY.md`. It distinguishes coverage already present from work still required before Phase 6 can be
closed.

## Test suites

| Suite | Command | Environment |
|---|---|---|
| Complete suite | `./gradlew test` | Docker plus supported native embedded-service binaries |

## Coverage

| Area | Current executable coverage | Remaining Phase 6 work |
|---|---|---|
| HTTP | Matching by method, path, query, headers, exact/partial/JSON/empty bodies; response sequences; text, JSON, bytes, resource and empty responses; header registration; embedded-server propagation, including a trained non-2xx response, and async MVC | No known 1.0 blocker; retain regression coverage |
| gRPC | Unary calls, DSL sequences, status errors, descriptor discovery, custom channel routing, JSON/map responses, execution metadata propagation | Add an explicit metadata matcher/response test if metadata becomes part of the stable DSL contract |
| Kafka | Embedded broker activation, headers, protobuf tracking, YAML consumers, topic cleanup, timeouts, producer/listener execution propagation | Add a real Kafka Testcontainer producer/tracker path; current container coverage only validates container definitions and Kafka UI wiring |
| Database cleanup | H2, MySQL, and PostgreSQL cleanup; exclusions; generated and assigned identifiers; composite keys; foreign keys; data-source selection and missing transaction manager | Re-run Docker database suites repeatedly and record platform results |
| Embedded PostgreSQL | Random-port context wiring; successful fixed-port startup; init-script effects; fixed-port conflict diagnostics; failed initialization and normal shutdown | Document and verify supported native platforms in CI |
| Embedded Redis | Random-port context wiring and data operations; successful fixed-port startup and ping; fixed-port conflict diagnostics; shutdown property cleanup | Document and verify supported native platforms in CI |
| DynamoDB Local | Startup and client access | Add isolation and explicit shutdown verification; document supported architectures |
| Testcontainers | Selective registration, inheritance, expression resolution, backward-compatible YAML, data population, container stop paths | Add failure-during-start cleanup coverage across a multi-container definition set |
| Elasticsearch | Readiness strategy, YAML document parsing, container population and client readback | Repeat on supported Docker platforms and record resource requirements |
| Toxiproxy | Default/explicit upstream parsing, property generation, invalid definitions, live latency/disable/reset lifecycle | Add failure cleanup verification when a proxied target cannot start |
| Declarative fixtures | Class/method precedence, groups, predicates, generated HTTP/gRPC responses, per-test cleanup | No known 1.0 blocker; retain regression coverage |

## Execution policy

- `test` remains the single comprehensive developer and CI suite until `releaseCheck` is introduced in Phase 10.
- Integration tests are not duplicated into manually maintained Docker and Docker-free classifications. If CI
  later requires separate execution stages, their selection mechanism must discover tests without a class list.
- Fixed ports are used only when the behavior under test is fixed-port selection or conflict reporting. Normal
  integration tests must prefer dynamically assigned ports.
- A resource-owning test must close its resource in `finally`, Spring destruction, or an equivalent lifecycle
  callback, including when initialization fails.
