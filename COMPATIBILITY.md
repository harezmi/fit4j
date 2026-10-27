# FIT4J 1.x Compatibility Contract

This document defines the supported platform, public feature status, and semantic-versioning promise for
FIT4J 1.x. It applies from `1.0.0`; pre-release builds may still change while the release-readiness plan is in
progress.

## Supported platform

The versions below are the resolved and tested baseline for 1.0. Later patch releases in a listed line are
supported unless their release notes introduce a known incompatibility. A new minor or major dependency line
is supported only after it appears in this table and CI.

| Component | 1.0 baseline and policy |
|---|---|
| Java runtime | Java 17 minimum; CI runs the FIT4J suite on Java 17 and 25. Other Java releases are not claimed until added to CI. |
| Java bytecode | Java 17 (`--release`/JVM target 17), compiled with a Java 25 toolchain. |
| Kotlin | Kotlin 2.3.21 compiler and standard library; Kotlin 2.3.x consumers are supported. Java consumers do not need the Kotlin compiler. |
| Spring Boot | 4.1.0 baseline; compatible 4.1.x patches are supported. Spring Boot 3.x and 4.2+ are outside the 1.0 contract. |
| JUnit | JUnit Jupiter and JUnit Platform 6.0.3; the Jupiter extension model is required. JUnit 4 is unsupported. |
| gRPC and protobuf | Spring Boot gRPC starters 4.1.0, Spring gRPC 1.1.0, grpc-java 1.81.0, grpc-kotlin 1.5.0, and protobuf-java 4.34.2. gRPC remains optional at runtime. |
| Testcontainers | Testcontainers 2.0.5. A Docker-compatible runtime is required only for container-backed features. |
| Kafka | Spring Kafka 4.1.0 and Kafka clients 4.2.1. Embedded Kafka and Testcontainers Kafka are tested; the container fixture uses `confluentinc/cp-kafka:7.6.1`. |
| Databases | H2 2.4.240; MySQL Connector/J 9.7.0 with `mysql:8.0.37`; PostgreSQL JDBC 42.7.11 with `postgres:16.1`; embedded PostgreSQL 2.2.2. Database cleanup is tested for H2, MySQL, and PostgreSQL. |
| Redis | Jedis 7.4.1, `redis:6.2.1`, and embedded-redis 1.4.3. Embedded Redis depends on platform-native binaries supplied by that project. |
| Elasticsearch | Elasticsearch Java client 9.4.2 and the tested container image `elasticsearch:9.0.4`. |
| DynamoDB Local | DynamoDB Local 2.6.1. It includes native SQLite libraries; supported host architectures are limited to those for which the upstream artifact supplies a working native library. A Docker-backed DynamoDB definition remains available as an alternative. |

FIT4J uses the Spring Boot 4.1 BOM internally to resolve its build and test classpaths, but does not publish the
BOM as a platform dependency. Published Maven and Gradle metadata instead contains explicit versions only for
FIT4J's direct dependencies. Adding FIT4J therefore does not import the entire Boot dependency-management
policy into a consumer project. Applications should continue to use their own supported Spring Boot 4.1.x BOM.

## Feature and API stability

The owner for every row is **FIT4J maintainers**. Ownership means reviewing compatibility, documentation,
tests, and dependency upgrades for that feature.

| Feature or surface | Status | 1.x contract |
|---|---|---|
| `@FIT`, `@IT`, `@FixtureForFIT` and JUnit integration | Stable API | Annotation names, targets, documented attributes, defaults, and lifecycle behavior are covered. |
| Programmatic and declarative fixtures | Stable API | Public builders plus the documented `fit4j-fixtures.yml` structure and matching rules are covered. |
| HTTP mocking, tracing, and HTTP DSL | Stable API | Documented DSL entry points, request/response models, extension interfaces, and behavior are covered. |
| gRPC mocking, tracing, and gRPC DSL | Stable API | Documented DSL entry points and extension interfaces are covered when gRPC dependencies are present. |
| Kafka message tracking and consumer fixtures | Stable API | `KafkaMessageTracker`, documented activation properties, and `fit4j-kafka-consumers.yml` are covered. |
| H2, MySQL, and PostgreSQL cleanup | Stable API | `DatabaseTestSupport`, cleanup properties, exclusions, and documented post-test behavior are covered. |
| Embedded Redis, PostgreSQL, DynamoDB, and Kafka | Stable API | Enabling annotations, their defaults, and documented exposed properties are covered, subject to upstream native/platform limits. |
| Declarative Testcontainers definitions | Stable API | `@Testcontainers`, `TestContainerDefinition`, supported YAML fields, expression resolution, and documented exposed properties are covered. Container images themselves remain user-selected external dependencies. |
| Network fault injection and Toxiproxy controls | Stable API | `NetworkFault`, `NetworkFaultProxy`, documented proxy bean names, configuration properties, defaults, and behavior are covered. |
| Mock response and container extension points | Stable SPI | `MockResponseProvider`, `MockResponseJsonBuilder`, `CallTraceFactory`, `JsonToMockResponseConverter`, `TestContainerDataPopulator`, and documented builder interfaces are intended for third-party implementations. |
| Helper beans documented for direct injection | Stable API | `VerificationHelper`, `JsonHelper`, `MockResponseFactory`, `MockServiceCallTracker`, and documented tracker types are covered. |
| Spring auto-configuration and context-customizer classes | Internal implementation | Classes under `org.fit4j.autoconfigure` and `org.fit4j.context` are public for framework discovery, not for direct use. Their class names, ordering, conditions, and undocumented bean names may change. |
| Other undocumented public classes | Internal implementation | Public JVM visibility alone does not make a type supported. A type must be documented here or in the README as API/SPI. |

There are no experimental APIs and no APIs currently deprecated for removal before 1.0. Any such removal
discovered before the final release must be completed before `1.0.0`, not carried into the stable contract
accidentally.

## Semantic-versioning promise

For stable API and SPI, a FIT4J 1.x patch or minor release will preserve:

- Java source compatibility for documented Java entry points and JVM binary compatibility for already compiled
  Java and Kotlin callers;
- Kotlin source compatibility for documented Kotlin DSL calls when compiled with the supported Kotlin line;
- documented annotations, annotation defaults, DSL entry points, public extension-interface methods, and
  documented model semantics;
- documented Spring bean types and explicitly documented bean names; auto-configuration implementation class
  names and undocumented infrastructure bean names are excluded;
- documented `fit4j.*` property keys, meanings, and defaults;
- documented fixture, Kafka consumer, and Testcontainers YAML fields. New optional fields may be added in a
  backward-compatible minor release; existing fields will not be removed or reinterpreted incompatibly;
- serialized fixture/configuration formats. FIT4J does not promise Java native serialization compatibility for
  internal model objects.

Bug fixes may reject input that was never valid according to the documentation, close accidental loopholes,
or change logging without being considered breaking.

## Deprecation and removal policy

Stable API/SPI is not removed or incompatibly changed within 1.x without first being deprecated in a minor
release. A deprecation includes a replacement or migration path and remains for at least one subsequent minor
release. Normal removal occurs in 2.0. Security, data-loss, or ecosystem-breaking defects may require a faster
change; such an exception must be prominently documented in the release notes.

If experimental APIs are introduced later, they must be explicitly documented and can change or be removed in
a 1.x minor release without the stable deprecation window. Internal implementation can change in any release.

## Concurrency and process model

JUnit parallel execution of `@FIT`/`@IT` classes or methods inside one JVM is unsupported because FIT4J still
has context-associated shared state. Tests must execute sequentially within each test JVM. Multiple isolated
JVM forks are supported and are the recommended form of parallelism, provided each process has sufficient
memory and does not contend for user-configured fixed ports or external services. Testcontainers reuse and
network-fault proxy state must not be shared across concurrently mutating tests.
