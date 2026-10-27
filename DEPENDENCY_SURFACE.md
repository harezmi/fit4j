# FIT4J 1.0 Dependency Surface

This report records the direct production dependencies published by FIT4J 1.0, why they exist, and why their
Gradle scope is either `api` or `implementation`. Versions are explicit in publication metadata; the Spring
Boot BOM is used only while building and testing FIT4J and is not exported to consumers.

## Packaging decision

FIT4J 1.0 deliberately remains one batteries-included artifact. Splitting it immediately before 1.0 would
move packages, coordinates, Spring discovery metadata, and DSL entry points at the same moment the public API
is being frozen. The current JAR is approximately 0.5 MiB; most cost is in transitive integration libraries,
not FIT4J bytecode. A module split is deferred to a future major release and must be driven by measured
consumer graphs and migration tooling.

The single artifact means supported HTTP, database, Kafka, embedded-service, Testcontainers, Redis, and
Elasticsearch integrations are present on the runtime graph. They remain inactive until their annotations,
properties, beans, or fixture definitions select them. gRPC is the exception: its Boot starters and grpc-java
transport APIs are `compileOnly`, so non-gRPC consumers do not receive a gRPC stack.

## Published production dependencies

`api` means a supported public signature exposes a type from that dependency. `implementation` means FIT4J
uses it internally or activates it through configuration/reflection; its types are not required to compile
ordinary calls to the stable API.

| Dependency | Scope | Main-source use and rationale |
|---|---|---|
| `spring-boot-test` | `api` | `@FIT`/`@IT` expose `SpringBootTest.WebEnvironment`; test context is core API. |
| `spring-boot-restclient` | `api` | HTTP extension types expose Spring HTTP/client contracts. |
| `spring-kafka` | `api` | `KafkaMessage` exposes Kafka `Headers`; Kafka trackers are stable API. |
| `jackson-module-kotlin` | `api` | `JsonHelper` exposes Jackson 3 `JsonMapper`; DSL and fixture conversion use it. |
| `kotlin-stdlib` | `api` | Public Kotlin declarations and metadata require the supported Kotlin runtime. |
| `protobuf-java`, `protobuf-java-util` | `api` | Stable gRPC DSL, traces, helpers, and extension points expose protobuf types. |
| `testcontainers`, `testcontainers-junit-jupiter` | `api` | `TestContainerDefinition` exposes `GenericContainer`; `@Testcontainers` carries the JUnit integration annotation. |
| `testcontainers-elasticsearch` | `api` | Elasticsearch connection helpers accept `ElasticsearchContainer`. |
| `testcontainers-toxiproxy` | `api` | Stable `NetworkFaultProxy` exposes Toxiproxy control types. |
| `elasticsearch-java` | `api` | Elasticsearch population helpers return `ElasticsearchClient` and request types. |
| `jedis` | `api` | `RedisDataPopulator.getJedis()` exposes `Jedis`. |
| `embedded-postgres` | `api` | `EmbeddedPostgresServer.builder` exposes the upstream builder type. |
| `spring-boot-starter-jdbc` | `implementation` | Database cleanup uses Spring JDBC and transactions internally. |
| `spring-boot-resttestclient` | `implementation` | HTTP test-client customization is auto-configuration infrastructure. |
| `spring-boot-starter` | `implementation` | Base Boot auto-configuration and lifecycle infrastructure. |
| `spring-boot-kafka` | `implementation` | Kafka auto-configuration integration; stable tracker signatures use `spring-kafka` directly. |
| `spring-boot-jackson` | `implementation` | Supplies Boot's Jackson 3 configuration internally. |
| `h2` | `implementation` | H2-specific cleanup and console support. |
| `spring-kafka-test` | `implementation` | Embedded Kafka context creation; no stable signature exposes its types. |
| `byte-buddy` | `implementation` | Generates internal mock gRPC service implementations. |
| `snakeyaml` | `implementation` | Parses fixture, Kafka-consumer, Redis, Elasticsearch, and container YAML. |
| `commons-lang3` | `implementation` | Internal reflective container-property conversion. |
| `kotlin-reflect` | `implementation` | Runtime implementation support, not part of stable signatures. |
| `testcontainers-kafka`, `testcontainers-mysql`, `testcontainers-postgresql` | `implementation` | Runtime definitions selected from YAML/class aliases; types are not stable signatures. |
| `embedded-redis` | `implementation` | Implementation behind `@EnableEmbeddedRedis`. |
| `DynamoDBLocal` | `implementation` | Implementation behind `@EmbeddedDynamoDB`; SLF4J is excluded to avoid overriding the application's logging API. |
| `jakarta.annotation-api` | `implementation` | Internal lifecycle annotations such as `@PostConstruct`. |

MockK is test-only. The three previously declared Resilience4j artifacts had no production or test source use
and were removed.

## Non-published compile-only integrations

The Spring Boot 4.1 BOM, Boot gRPC server/client starters, grpc-java API/stub/in-process artifacts, and their
version alignment are used to compile FIT4J but are not published as transitive dependencies. A gRPC consumer
must add the Boot gRPC server/client starter appropriate to its application. Test compilation additionally uses
the Boot starter test modules, database drivers, grpc-kotlin stubs, and coroutines; none are published.

## Feature classpath guide

Because 1.0 is a single artifact, the table lists additions a consumer needs beyond `fit4j` and its published
transitives.

| Feature | Consumer addition or environment |
|---|---|
| Core annotations, fixtures, HTTP, H2 cleanup | No extra FIT4J integration dependency; a supported Spring Boot 4.1 test application is required. |
| gRPC DSL and mocks | Add the Spring Boot 4.1 gRPC server/client starter(s); generated service/message classes remain application-owned. |
| Kafka tracking | Configure an embedded or external broker. Embedded Kafka support is already transitive. |
| MySQL/PostgreSQL cleanup | Add the application's JDBC driver; use an application datasource or the documented Testcontainers definition. |
| Declarative Testcontainers and network faults | Provide a Docker-compatible runtime and user-selected container images. |
| Redis and Elasticsearch population | No extra client library; configure embedded/container endpoints as documented. |
| Embedded Redis/PostgreSQL/DynamoDB | No extra Maven dependency; the host must support the upstream native binary. Docker definitions are the portable alternative. |

Isolated-classloader tests verify that core annotations and HTTP/database configuration load without gRPC or
container/client stacks, that gRPC absence is detected using the active context classloader, and that embedded
and container integrations remain inactive on an unannotated test class.

## Publication guarantees

- Maven POM and Gradle module metadata contain explicit versions for every published direct dependency.
- Neither metadata format exports `spring-boot-dependencies`; consumer applications retain control of their
  own Boot BOM and unrelated dependency versions.
- API dependencies appear in Maven compile scope and Gradle `apiElements`; implementation dependencies appear
  in Maven runtime scope and Gradle `runtimeElements`.
- Test-only dependencies, MockK, and compile-only gRPC dependencies are absent from publication metadata.
