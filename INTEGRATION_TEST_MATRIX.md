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
| Kafka | Embedded broker activation, headers, protobuf tracking, YAML consumers, topic cleanup, timeouts, producer/listener execution propagation, and a real Kafka Testcontainer producer/listener tracking path | No known 1.0 blocker; retain regression coverage |
| Database cleanup | H2, MySQL, and PostgreSQL cleanup; exclusions; generated and assigned identifiers; composite keys; foreign keys; data-source selection and missing transaction manager; repeated macOS arm64 execution | Confirm the current candidate on Linux CI |
| Embedded PostgreSQL | Random-port context wiring; successful fixed-port startup; init-script effects; fixed-port conflict diagnostics; failed initialization and normal shutdown; macOS arm64 verification | Confirm the current candidate on Linux CI |
| Embedded Redis | Random-port context wiring and data operations; successful fixed-port startup and ping; fixed-port conflict diagnostics; shutdown property cleanup; macOS arm64 verification | Confirm the current candidate on Linux CI |
| DynamoDB Local | Startup, client access, independent context storage, explicit shutdown on context close, and documented native-platform limits | Confirm the current candidate on Linux CI |
| Testcontainers | Selective registration, inheritance, expression resolution, backward-compatible YAML, data population, normal network shutdown, and reverse-order cleanup after a multi-container startup failure | No known 1.0 blocker; retain regression coverage |
| Elasticsearch | Readiness strategy, YAML document parsing, container population, client readback, repeated macOS arm64 execution, and documented release-suite resources | Confirm the current candidate on Linux CI |
| Toxiproxy | Default/explicit upstream parsing, property generation, invalid definitions, live latency/disable/reset lifecycle, and immediate cleanup when proxy registration fails | No known 1.0 blocker; retain regression coverage |
| Declarative fixtures | Class/method precedence, groups, predicates, generated HTTP/gRPC responses, per-test cleanup | No known 1.0 blocker; retain regression coverage |

## Execution policy

- `test` remains the single comprehensive developer and CI suite until `releaseCheck` is introduced in Phase 10.
- Integration tests are not duplicated into manually maintained Docker and Docker-free classifications. If CI
  later requires separate execution stages, their selection mechanism must discover tests without a class list.
- Fixed ports are used only when the behavior under test is fixed-port selection or conflict reporting. Normal
  integration tests must prefer dynamically assigned ports.
- A resource-owning test must close its resource in `finally`, Spring destruction, or an equivalent lifecycle
  callback, including when initialization fails.

## Platform verification record

| Environment | Result |
|---|---|
| GitHub Actions `ubuntu-latest`, JDK 17 and 25 | Required by `.github/workflows/ci.yml`; the current candidate must pass after push before Phase 6 closes |
| macOS 26.6 arm64, JDK 25 test toolchain, OrbStack aarch64 | Two consecutive complete `check` runs passed on 2026-11-19; the second used `--rerun-tasks` |

The verified macOS Docker runtime exposed 18 CPUs and 16 GiB of memory. This is a verification record, not the
minimum requirement; the supported release-suite resource floor is defined in `COMPATIBILITY.md`. No partial
GitHub-hosted macOS job is used as release evidence because those runners cannot execute Docker/Testcontainers;
continuous full-suite macOS verification would require a self-hosted Mac runner with a compatible container runtime.
