# HTTP Fluent DSL Implementation Plan

Date: 2026-09-07

## Purpose

This document captures the agreed implementation plan for a fluent DSL that lets tests define HTTP/REST request-response trainings directly inside the test method.

## Core Decisions

- `scenario` is not part of the public API.
- The test method itself is the natural scope.
- DSL-defined trainings must override YAML-defined trainings.
- The DSL must remain compatible with the current declarative infrastructure:
  - `DeclarativeTestFixtureProvider`
  - `HttpTestFixtureBuilder`
  - `HttpTestFixture`
- The implementation must support both Kotlin and Java consumers.
- The first milestone is HTTP; gRPC comes later using the same architecture.

## Public DSL Shape

### Request-side capabilities

- `path(...)`
- `method(...)`
- `predicate(...)`
- `header(...)`
- `headers { ... }`
- `respond { ... }`
- `responds { ... }`

### Response-side capabilities

- `status(...)`
- `header(...)`
- `headers { ... }`
- `bodyAsText(...)`
- `bodyAsJson(...)`
- `bodyAsBytes(...)`
- `bodyAsResource(...)`

### No-content behavior

- `status(204)` must work without any body method.
- No extra `noContent()` helper is required initially.

## Design Principles

- Keep the API small and fluent.
- Avoid exposing `Map<String, Any>` unless strictly necessary internally.
- Prefer explicit body helpers over forcing model-class instantiation.
- `bodyAsText`, `bodyAsJson`, `bodyAsBytes`, and `bodyAsResource` should cover the common REST mock use cases.
- Type-safe object-body helpers can be added later if needed, but they are not required for the first version.
- Expression support remains available as an escape hatch, but it should not become the default path.

## Compatibility Goals

The DSL must support everything currently possible with YAML:

- path matching
- method matching
- predicate matching
- single response
- sequence responses
- headers
- body
- SpEL-based expression evaluation
- request-path interpolation
- hit-order statefulness
- wildcard/global fallback semantics
- method-level fixture selection behavior
- reset/cleanup after each test

## Precedence Order

1. DSL-defined trainings inside the test method
2. Programmatic response builders
3. YAML declarative fixtures

## Implementation Phases

### Phase 1. Response body model and runtime support

- Make HTTP response bodies capable of representing:
  - empty/no-content
  - text
  - bytes
- Update HTTP serialization in the server dispatcher.
- Ensure YAML-based string bodies still work.

### Phase 2. Public DSL entry point

- Add a public `Fit4J.http { ... }` entry point.
- Provide Kotlin and Java-friendly overloads.

### Phase 3. HTTP DSL interfaces

- Add request DSL interfaces.
- Add response DSL interfaces.
- Add response-sequence DSL interfaces.
- Add header DSL support.

### Phase 4. Internal training model

- Introduce typed internal models for HTTP request matching and response definitions.
- Compile DSL calls into these internal objects.
- Keep YAML and DSL on the same runtime path where possible.

### Phase 5. Test-method scoped registry

- Store DSL-defined trainings in a test-method scoped registry.
- Track stateful sequence responses there.
- Clean the registry automatically after the test finishes.

### Phase 6. Runtime precedence integration

- Make DSL-defined trainings win over:
  - programmatic builders
  - YAML fixtures
- Preserve wildcard/global fallback behavior.

### Phase 7. Tests

- Unit tests for model conversion and matching.
- Integration tests for Kotlin usage.
- Integration tests for Java usage.
- Tests for:
  - `bodyAsText`
  - `bodyAsJson`
  - `bodyAsBytes`
  - `bodyAsResource`
  - `status(204)` no-content
  - sequence responses
  - DSL vs YAML precedence

### Phase 8. Documentation and examples

- Update the README.
- Update example REST tests and fixtures.
- Add side-by-side Kotlin and Java examples.

### Phase 9. gRPC follow-up

- Reuse the same registry, lifecycle, and precedence rules.
- Add gRPC as a second protocol-specific adapter after HTTP stabilizes.

## Suggested File Order

1. `src/main/kotlin/org/fit4j/http/HttpResponse.kt`
2. `src/main/kotlin/org/fit4j/http/JsonToHttpResponseConverter.kt`
3. `src/main/kotlin/org/fit4j/http/HttpServerDispatcher.kt`
4. `src/main/kotlin/org/fit4j/http/dsl/*`
5. `src/main/kotlin/org/fit4j/http/training/*`
6. `src/main/kotlin/org/fit4j/Fit4J.kt`
7. `src/main/kotlin/org/fit4j/autoconfigure/TestHttpAutoConfiguration.kt`
8. `src/main/kotlin/org/fit4j/http/DefaultHttpMockResponseProvider.kt`
9. `src/main/kotlin/org/fit4j/http/HttpTestFixtureBuilder.kt`
10. `src/main/kotlin/org/fit4j/http/HttpTestFixture.kt`
11. Tests
12. README and examples

## Notes for Future Work

- Keep the API Java-friendly from day one.
- Do not make `ExampleRestResponse` or any specific domain type part of the DSL API itself.
- The DSL should be expressive enough that developers do not feel forced back into YAML for missing features.
