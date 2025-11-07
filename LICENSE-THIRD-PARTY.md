# Third-Party Licenses

This document lists all third-party dependencies used by FIT4J and their respective licenses.

## Runtime Dependencies

### Apache License 2.0
- **Spring Boot** (org.springframework.boot:spring-boot-starter-*) - Apache 2.0
- **Spring Kafka** (org.springframework.kafka:spring-kafka) - Apache 2.0
- **ByteBuddy** (net.bytebuddy:byte-buddy) - Apache 2.0
- **SnakeYAML** (org.yaml:snakeyaml) - Apache 2.0
- **Apache Commons Lang** (org.apache.commons:commons-lang3) - Apache 2.0
- **Jackson** (com.fasterxml.jackson.module:jackson-module-kotlin) - Apache 2.0
- **Kotlin Standard Library** (org.jetbrains.kotlin:kotlin-stdlib) - Apache 2.0
- **Kotlin Coroutines** (org.jetbrains.kotlinx:kotlinx-coroutines-core) - Apache 2.0
- **gRPC** (io.grpc:grpc-*) - Apache 2.0
- **MockK** (io.mockk:mockk) - Apache 2.0
- **gRPC Spring Boot Starter** (net.devh:grpc-spring-boot-starter) - Apache 2.0
- **Elasticsearch REST Client** (org.elasticsearch.client:elasticsearch-rest-client) - Apache 2.0
- **Elasticsearch Java Client** (co.elastic.clients:elasticsearch-java) - Apache 2.0
- **Embedded Redis** (com.github.codemonstur:embedded-redis) - Apache 2.0
- **Resilience4j** (io.github.resilience4j:resilience4j-*) - Apache 2.0
- **DynamoDB Local** (com.amazonaws:DynamoDBLocal) - Apache 2.0

### MIT License
- **Testcontainers** (org.testcontainers:testcontainers) - MIT
- **Jedis** (redis.clients:jedis) - MIT

### BSD-3-Clause License
- **Protocol Buffers** (com.google.protobuf:protobuf-java) - BSD-3-Clause
- **Protocol Buffers Java Util** (com.google.protobuf:protobuf-java-util) - BSD-3-Clause

### Eclipse Public License 2.0
- **Jakarta Annotation API** (jakarta.annotation:jakarta.annotation-api) - EPL 2.0

### Mozilla Public License 2.0 / Eclipse Public License 1.0
- **H2 Database** (com.h2database:h2) - MPL 2.0 / EPL 1.0 (Dual Licensed)

## Test Dependencies

### GPL 2.0 with FOSS Exception (Classpath Exception)
- **MySQL Connector/J** (com.mysql:mysql-connector-j) - GPL 2.0 with Classpath Exception

**Note**: MySQL Connector/J is only used in test scope and is not bundled with FIT4J. The FOSS Exception allows linking this library with applications under other licenses (including Apache 2.0). Users who include this dependency in their tests are subject to GPL 2.0, though the FOSS Exception applies.

## Build Tool Dependencies

### Apache License 2.0
- **Kotlin Gradle Plugin** - Apache 2.0
- **Google Protobuf Gradle Plugin** - Apache 2.0
- **Maven Publish Plugin** - Apache 2.0

## License Compatibility

All dependencies listed above are compatible with Apache License 2.0. The MySQL Connector/J dependency is compatible due to its FOSS Exception clause, which explicitly allows linking with applications under other licenses.

For detailed license compatibility analysis, see LICENSE_COMPATIBILITY_ANALYSIS.md.

