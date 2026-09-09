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
- Keep the gRPC design protocol-specific but architecture-compatible with the HTTP DSL:
  - fluent test-method-local entry point
  - Kotlin and Java overloads
  - typed internal request matcher and response definition
  - DSL over YAML precedence
  - sequence/stateful response support if already present in YAML
- Preserve the current declarative YAML behavior while allowing DSL-defined gRPC trainings to override it.

#### Phase 9.1 gRPC DSL surface

- Define the public `Fit4J.grpc { ... }` entry point, if not already present in the API surface.
- Provide Kotlin and Java-friendly overloads.
- Model the smallest useful fluent surface first:
  - request target type / fully qualified request class name
  - request predicate
  - response status
  - response body
  - optional response headers/metadata if required by current YAML parity
- Keep the Java API discoverable and avoid exposing low-level maps unless necessary.

#### Phase 9.2 gRPC matcher and runtime model

- Introduce typed internal gRPC request/response models mirroring the YAML builder path.
- Reuse the existing registry and test-method scoping mechanics.
- Ensure request matching can still use SpEL-based predicate evaluation where YAML currently does.
- Keep path-independent gRPC matching aligned with the existing YAML semantics.

#### Phase 9.3 gRPC runtime precedence and lifecycle

- Integrate the gRPC DSL provider into the existing mock response resolution chain.
- Preserve the same precedence contract:
  1. DSL-defined gRPC trainings inside the test method
  2. Programmatic gRPC response builders
  3. YAML gRPC fixtures
- Make sequence responses stateful per test method, just like HTTP.
- Ensure cleanup/reset behavior is handled by the same test lifecycle hooks.

#### Phase 9.4 gRPC tests

- Add Kotlin integration tests for the DSL entry point.
- Add Java integration tests for the DSL entry point.
- Add precedence tests covering DSL vs programmatic builder vs YAML.
- Add predicate and sequence response tests if those are part of the gRPC surface.
- Add compatibility tests against existing YAML gRPC fixtures to prove parity is preserved.

#### Phase 9.5 gRPC documentation

- Extend the README with a gRPC DSL section.
- Add Kotlin and Java examples side by side.
- Document the precedence order and any parity gaps explicitly.
- Keep the HTTP and gRPC sections parallel so users can transfer the same mental model between protocols.

## Phase 9 Technical Task List

This is the execution-oriented breakdown for the gRPC phase. The goal is to reuse the HTTP DSL architecture as much as possible while keeping gRPC-specific semantics explicit.

### 9.A Classes to Add or Update

- `src/main/kotlin/org/fit4j/Fit4J.kt`
  - Add the `grpc` entry point alongside `http`.
- `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDsl.kt`
  - Public gRPC DSL surface.
- `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDslBuilder.kt`
  - Collects gRPC trainings and registers them.
- `src/main/kotlin/org/fit4j/grpc/dsl/GrpcRequestTrainingDsl.kt`
  - Request matcher DSL for gRPC.
- `src/main/kotlin/org/fit4j/grpc/dsl/GrpcResponseDsl.kt`
  - Single response DSL for gRPC.
- `src/main/kotlin/org/fit4j/grpc/dsl/GrpcResponseSequenceDsl.kt`
  - Stateful response sequence DSL for repeated hits.
- `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDslModels.kt`
  - Typed request matcher, response definition, template, and training model.
- `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDslRegistry.kt`
  - Test-method scoped registry for DSL-defined trainings.
- `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDslExpressionSupport.kt`
  - Spring context and SpEL helper for gRPC DSL expressions.
- `src/main/kotlin/org/fit4j/grpc/DefaultGrpcMockResponseProvider.kt`
  - Update precedence so DSL wins before programmatic builders and YAML fixtures.
- `src/main/kotlin/org/fit4j/context/Fit4JTestExtension.kt`
  - Reset the gRPC DSL registry after each test.

### 9.B Method Signatures to Introduce

- `Fit4J.grpc(block: GrpcDsl.() -> Unit)`
- `Fit4J.grpc(block: java.util.function.Consumer<GrpcDsl>)`
- `GrpcDsl.requestType(type: Class<out com.google.protobuf.Message>)`
- `GrpcDsl.requestType(typeName: String)`
- `GrpcDsl.request(block: GrpcRequestTrainingDsl.() -> Unit)` if we want a path-less composition entry point similar to HTTP
- `GrpcRequestTrainingDsl.predicate(expression: String)`
- `GrpcRequestTrainingDsl.predicate(predicate: Predicate<Message>)`
- `GrpcRequestTrainingDsl.respond(block: GrpcResponseDsl.() -> Unit)`
- `GrpcRequestTrainingDsl.respond(block: Consumer<GrpcResponseDsl>)`
- `GrpcRequestTrainingDsl.responds(block: GrpcResponseSequenceDsl.() -> Unit)`
- `GrpcRequestTrainingDsl.responds(block: Consumer<GrpcResponseSequenceDsl>)`
- `GrpcResponseDsl.status(status: String)` or `GrpcResponseDsl.status(code: io.grpc.Status.Code)` depending on the final YAML parity decision
- `GrpcResponseDsl.bodyAsJson(json: String)`
- `GrpcResponseDsl.bodyAsJson(value: Any)`
- `GrpcResponseSequenceDsl.response(block: GrpcResponseDsl.() -> Unit)`
- `GrpcDslRegistry.register(training: GrpcTrainingDefinition)`
- `GrpcDslRegistry.resolveResponse(request: Message): Message?`
- `GrpcDslRegistry.resetCurrentTest()`

### 9.C File-Based Implementation Order

1. `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDslModels.kt`
2. `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDslRegistry.kt`
3. `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDslExpressionSupport.kt`
4. `src/main/kotlin/org/fit4j/grpc/dsl/GrpcResponseDsl.kt`
5. `src/main/kotlin/org/fit4j/grpc/dsl/GrpcRequestTrainingDsl.kt`
6. `src/main/kotlin/org/fit4j/grpc/dsl/GrpcResponseSequenceDsl.kt`
7. `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDslBuilder.kt`
8. `src/main/kotlin/org/fit4j/grpc/dsl/GrpcDsl.kt`
9. `src/main/kotlin/org/fit4j/Fit4J.kt`
10. `src/main/kotlin/org/fit4j/grpc/DefaultGrpcMockResponseProvider.kt`
11. `src/main/kotlin/org/fit4j/context/Fit4JTestExtension.kt`
12. `src/test/kotlin/org/fit4j/grpc/*`
13. `src/test/java/org/fit4j/grpc/*`
14. `README.md`

### 9.D Execution Notes

- Reuse the existing gRPC YAML machinery where possible instead of introducing a parallel, incompatible runtime.
- Keep the DSL implementation test-method scoped, matching the HTTP design.
- Preserve the current declarative fixture semantics and precedence rules.
- Add Kotlin and Java examples only after the runtime path is stable so the documentation reflects the final API, not an intermediate draft.

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
