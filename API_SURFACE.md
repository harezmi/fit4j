# FIT4J 1.x Public API Surface

This classification is the review boundary for `api/fit4j.api`. JVM `public` visibility is not by itself a
support promise; the compatibility baseline intentionally records only the API and SPI categories below.

## Stable application API

- `org.fit4j.Fit4J` and the HTTP/gRPC DSL types used by its registration blocks.
- `org.fit4j.annotation` test annotations and their defaults.
- Public fixture, request, response, trace, and tracker models in `org.fit4j.http`, `org.fit4j.grpc`,
  `org.fit4j.kafka`, `org.fit4j.mock`, and `org.fit4j.mock.declarative`.
- Database cleanup contracts and implementations in `org.fit4j.dbcleanup`.
- Embedded-service annotations and controls in `org.fit4j.dynamodb`, `org.fit4j.kafka`, `org.fit4j.postgres`,
  and `org.fit4j.redis`.
- Declarative container definitions, population models, and network-fault controls in
  `org.fit4j.testcontainers`.
- `JsonHelper` and `VerificationHelper`, which are documented for direct injection.

These APIs follow the deprecation and binary/source compatibility rules in `COMPATIBILITY.md`. Mutable test
state is scoped to one test execution and is not promised to support parallel tests in one JVM. Non-null Kotlin
parameters reject Java `null` according to Kotlin's generated runtime checks unless explicitly documented.

## Stable extension SPI

- Mocking: `MockResponseProvider`, `MockResponseJsonBuilder`, `CallTrace`, `CallTraceFactory`, and
  `MockResponseFactory`.
- Declarative fixtures: `DeclarativeTestFixtureBuilder`, `JsonToMockResponseConverter`, `TestFixture`, and
  related predicate/group contracts.
- Protocol-specific response builders and documented HTTP/gRPC extension interfaces.
- Containers: `TestContainerDefinition`, `DataPopulatingTestContainerDefinition`, and
  `TestContainerDataPopulator`.
- Kafka serializer/deserializer and consumer-definition contracts intended for application customization.

SPI implementations are created and called by FIT4J/Spring. Implementations should be stateless or scoped to
one test execution and should propagate configuration failures rather than returning ambiguous partial state.

## Internal infrastructure

The following remain JVM-public where Spring discovery, proxying, reflection, or framework callbacks require
it, but are excluded from the compatibility baseline:

- all `org.fit4j.autoconfigure`, `org.fit4j.context`, `org.fit4j.expression`, and `org.fit4j.scope` types;
- classpath scanners, registries, context/server dispatchers, bean post-processors, property-source factories,
  parsers, registrars, and definition providers listed in `apiValidation.ignoredClasses` in `build.gradle.kts`.

There is no experimental category for 1.0. A declaration must be added to one of the stable categories and to
the reviewed API dump before it becomes supported.
