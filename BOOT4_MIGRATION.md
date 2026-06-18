# Spring Boot 4.1 migration guide (FIT4J consumers)

FIT4J **0.1.0+** targets **Spring Boot 4.1.x**. It is **not** compatible with Spring Boot 3.x in the same artifact — stay on an earlier FIT4J release if your service is still on Boot 3.5.

This guide summarizes what you need to change in **your service tests** when upgrading from FIT4J on Boot 3.5 to FIT4J on Boot 4.

## Version matrix

| Component | Boot 3.5 (previous) | Boot 4 (current FIT4J) |
|-----------|---------------------|-------------------------|
| Spring Boot | 3.5.x | **4.1.0** |
| gRPC integration | `net.devh:grpc-spring-boot-starter` | **Boot gRPC starters** (`spring-boot-starter-grpc-server` + `spring-boot-starter-grpc-client`) |
| Kotlin | 2.0.x | **2.3.21** (align with Boot 4.1 BOM) |
| JUnit | JUnit 5 | **JUnit 6** (via `spring-boot-starter-test-classic`) |
| Jackson (default HTTP) | Jackson 2 | **Jackson 3** (`tools.jackson.*`) |
| Testcontainers (BOM) | 1.x | **2.0.5** (via Boot 4.1 BOM — no pin required) |

Pins for the FIT4J build: [`gradle.properties`](gradle.properties).

## Gradle / Maven dependencies

### Spring Boot 4 modular starters

Boot 4 splits several starters. FIT4J and its examples use:

```kotlin
implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
implementation("org.springframework.boot:spring-boot-starter-classic") // or starter-jdbc etc.
testImplementation("org.springframework.boot:spring-boot-starter-test-classic")
testImplementation("org.springframework.boot:spring-boot-starter-webmvc")   // embedded HTTP server tests
testImplementation("org.springframework.boot:spring-boot-resttestclient") // TestRestTemplate
testImplementation("org.springframework.boot:spring-boot-restclient")   // RestTemplateBuilder / RestClient
testImplementation("org.springframework.boot:spring-boot-jackson2")       // optional: Jackson 2 stack
```

### gRPC (Boot 4.1 built-in starters)

Remove `net.devh:grpc-spring-boot-starter` and `org.springframework.grpc:spring-grpc-*` starters. Add:

```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-grpc-server")
testImplementation("org.springframework.boot:spring-boot-starter-grpc-client")
// optional test transport helpers:
testImplementation("org.springframework.boot:spring-boot-starter-grpc-server-test")
testImplementation("org.springframework.boot:spring-boot-starter-grpc-client-test")
```

Pin `io.grpc` artifacts to a single version if the BOM and spring-grpc-core disagree (FIT4J uses **1.81.0**):

```kotlin
val grpcVersion = "1.81.0"

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.grpc" && requested.name !in setOf("grpc-kotlin-stub", "protoc-gen-grpc-java", "protoc-gen-grpc-kotlin")) {
            useVersion(grpcVersion)
        }
    }
}
```

### Testcontainers 2.0

FIT4J **0.1.0+** uses Testcontainers **2.x** from the Spring Boot 4.1 BOM. **Do not** pin Testcontainers to 1.x — mixed 1.x/2.x classpaths cause `NoClassDefFoundError: org/testcontainers/shaded/...` at runtime.

Module artifact IDs changed in 2.0 (prefix `testcontainers-`):

| 1.x artifact | 2.x artifact |
|--------------|--------------|
| `junit-jupiter` | `testcontainers-junit-jupiter` |
| `kafka` | `testcontainers-kafka` |
| `mysql` | `testcontainers-mysql` |
| `postgresql` | `testcontainers-postgresql` |
| `elasticsearch` | `testcontainers-elasticsearch` |
| `localstack` | `testcontainers-localstack` |

Let the Boot BOM manage versions — declare dependencies without an explicit version:

```kotlin
testImplementation("org.testcontainers:testcontainers")
testImplementation("org.testcontainers:testcontainers-junit-jupiter")
```

**YAML container class names:** FIT4J resolves legacy 1.x FQCNs (e.g. `org.testcontainers.containers.MySQLContainer`) to their 2.x packages automatically. Updating YAML to canonical names (e.g. `org.testcontainers.mysql.MySQLContainer`) is optional.

**Confluent Kafka:** Testcontainers 2.x `KafkaContainer` targets `apache/kafka` (KRaft). For `confluentinc/cp-kafka` images, use **7.x+** (FIT4J auto-selects `ConfluentKafkaContainer` for Confluent images). Zookeeper-era images such as `cp-kafka:5.x` no longer work.

**LocalStack:** use `localStack.getEndpoint()` (not `getEndpointOverride(Service.S3)`). Package: `org.testcontainers.localstack.LocalStackContainer`.

### Elasticsearch client pin

Boot 4 BOM pulls `elasticsearch-java` **9.x**. FIT4J Testcontainer fixtures use Elasticsearch **8.x** images. Pin the client to match your ES image (FIT4J uses **8.15.5**):

```kotlin
val elasticSearchVersion = "8.15.5"

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "co.elastic.clients" && requested.name == "elasticsearch-java") {
            useVersion(elasticSearchVersion)
        }
        if (requested.group == "org.elasticsearch.client") {
            useVersion(elasticSearchVersion)
        }
    }
}
```

## gRPC client stubs in tests

`@GrpcClient` from net.devh is **not** available in spring-grpc 1.0. Use `@ImportGrpcClients` and `@Autowired`:

```kotlin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.grpc.client.ImportGrpcClients

@ImportGrpcClients(target = "testGrpcService", types = [FooServiceGrpc.FooServiceBlockingStub::class])
@FIT
class MyGrpcFIT {

    @Autowired
    private lateinit var fooGrpcService: FooServiceGrpc.FooServiceBlockingStub

    @Test
    fun `calls service under test`() {
        // ...
    }
}
```

FIT4J's `GrpcContextCustomizer` wires the in-process mock server channel automatically:

- Channel name: **`testGrpcService`** (not `inProcess`)
- Property set at runtime: `spring.grpc.client.channel.testGrpcService.target=in-process:<random-name>`

You normally **do not** need extra `application-test` properties for FIT mock redirection. For production-style channel names in your service code, map them under `spring.grpc.client.channel.*` in your test profile.

FIT4J attaches the test execution-id metadata on spring-grpc client channels via `GrpcChannelBuilderCustomizer` (`fit4jGrpcExecutionIdChannelCustomizer` in `TestGrpcAutoConfiguration`). Use `@ImportGrpcClients` rather than raw `ManagedChannel` `@Bean` definitions; if you must keep `ManagedChannel` beans, point them at `in-process:${spring.grpc.server.inprocess.name}` and add `Fit4jGrpcClientExecutionIdInterceptor` yourself.

### Properties migration (`grpc.*` → `spring.grpc.*`)

| Boot 3 / net.devh | Boot 4.1 / Boot gRPC |
|-------------------|----------------------|
| `grpc.server.inProcessName` | `spring.grpc.server.inprocess.name` |
| `grpc.client.foo.address` | `spring.grpc.client.channel.foo.target` |
| `spring.grpc.client.channels.foo.address` (4.0) | `spring.grpc.client.channel.foo.target` (4.1) |

## HTTP / `TestRestTemplate`

### `@AutoConfigureTestRestTemplate` is per test class

`@FIT` does **not** globally enable `TestRestTemplate` (that breaks gRPC-only tests with *"No local test web server"*). Add it only on tests that call your embedded server:

```kotlin
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.TestRestTemplate

@FIT
@AutoConfigureTestRestTemplate
class MyHttpFIT {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    // ...
}
```

### Package moves (Boot 4)

| Boot 3 | Boot 4 |
|--------|--------|
| `org.springframework.boot.web.client.RestTemplateBuilder` | `org.springframework.boot.restclient.RestTemplateBuilder` |
| `org.springframework.boot.test.web.client.TestRestTemplate` | `org.springframework.boot.resttestclient.TestRestTemplate` |
| `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestRestTemplate` | `org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate` |

### `@FIT` and `webEnvironment`

`@FIT` defaults to `webEnvironment = RANDOM_PORT` (embedded server). Use `@FIT(webEnvironment = SpringBootTest.WebEnvironment.MOCK)` for `MockMvc`-style tests without a real port.

## Jackson 3 vs Kotlin data classes

Boot 4 uses **Jackson 3** for HTTP message conversion by default. Kotlin `data class` DTOs used with `RestTemplate` / `RestClient` may fail deserialization unless you:

1. Add **`spring-boot-jackson2`** and **`jackson-module-kotlin`** and configure Jackson 2 for your clients, or
2. Use **JavaBean-style** mutable classes (`var` properties + no-arg constructor), or
3. Adopt a Jackson 3 Kotlin module when available in your stack.

See [`fit4j-examples/example-rest`](fit4j-examples/example-rest/) for a working Boot 4 example.

## Running `fit4j-examples` locally

Examples use a **composite Gradle build** (`includeBuild("..")`) so they always resolve the local FIT4J sources. You do not need `publishToMavenLocal` for day-to-day example development:

```bash
cd fit4j-examples
./gradlew test
```

## Further reading

- [README.md](README.md) — user guide (updated for Boot 4)
- [fit4j-examples/README.md](fit4j-examples/README.md) — runnable samples
- [index.html](index.html) — Fit4j Wiki (design companion)
