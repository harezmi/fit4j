# FIT4J 1.0.0 Release Readiness Plan

This plan defines the remaining work required before publishing `io.github.harezmi:fit4j:1.0.0`. It is based
on the current FIT4J repository rather than assuming that another project's release criteria apply unchanged.
Work is ordered so that build reproducibility, dependency policy, and API decisions are resolved before the
1.0 compatibility contract is frozen.

## Current baseline

**Assessment date:** 2026-10-19

**Release status:** Not ready

Observed repository state:

- `gradle.properties` already declares `version = 1.0.0-SNAPSHOT` and `group = io.github.harezmi`.
- The working tree was clean when this plan was prepared.
- The Gradle wrapper is 9.1.0, while the README still describes Gradle 8.14.4.
- A clean verification build currently fails during Gradle configuration with
  `JvmVendorSpec does not have member field IBM_SEMERU`; tests and publication metadata were therefore not
  validated during this assessment.
- CI runs library tests on a Java 17/25 matrix and examples on Java 25, but it runs `test`, not a comprehensive
  release verification task.
- The examples resolve the released FIT4J `0.1.9` artifact. They do not currently prove that the candidate
  `1.0.0-SNAPSHOT` artifact works.
- README, website, and example version markers still contain `0.1.9` in multiple places.
- The project has no `CHANGELOG.md` or `SECURITY.md`.
- No binary API baseline or compatibility check is configured.
- The main artifact contains integrations for HTTP, gRPC, Kafka, Testcontainers, H2, PostgreSQL, Redis,
  DynamoDB, Elasticsearch, Toxiproxy, database cleanup, and declarative fixtures in a single module.
- Many framework and test-tool dependencies are published as `implementation`; their necessity, visibility,
  and consumer impact have not been reviewed as a 1.0 contract.
- `src/main/resources/application.properties` is empty and is currently eligible for inclusion in the library
  artifact.
- Maven Central publishing and GPG signing are configured, and the local publishing documentation describes
  Central Portal tokens and `keys.openpgp.org`.

## Phase 1: Restore a reproducible build baseline

**Status:** Complete (2026-10-19)

### Execution record

- Upgraded `org.gradle.toolchains.foojay-resolver-convention` from 0.9.0 to 1.0.0 in the library and examples,
  removing the Gradle 9.1 `JvmVendorSpec.IBM_SEMERU` incompatibility.
- Upgraded `com.google.protobuf` from 0.9.4 to 0.9.6, removing its Gradle 9 multi-string dependency
  deprecations.
- Added `testJavaVersion` toolchain selection and changed CI labels and commands so the JDK 17 and JDK 25 rows
  run tests on the runtime they claim. Compilation continues to use the Java 25 toolchain and emits Java 17
  bytecode.
- Replaced configuration-time test classpath mutation with a lazy, provider-backed classpath, allowing the
  configuration cache to be stored and reused.
- Updated README and contributor setup to match the checked-in Gradle 9.1.0 wrapper and the actual Java
  compiler/test-runtime policy.
- Removed actionable Kotlin warnings, including deprecated Java annotation meta-types, impossible or redundant
  casts, an invalid generic bound that Kotlin 2.4 would reject, and deprecated Jedis `hmset` usage.
- Verified `clean check --no-configuration-cache -PtestJavaVersion=17` and
  `clean check --configuration-cache -PtestJavaVersion=25` from a dedicated empty Gradle user home. Both passed;
  the latter stored a configuration-cache entry, and a follow-up test run reused it.

### Known warning and environment inventory

The remaining warnings are understood and are not Gradle build-script deprecations:

- Unchecked casts are concentrated at deliberately untyped YAML/SnakeYAML, Spring bean lookup, protobuf, and
  generic tracking boundaries. Replacing these casts with validated typed parsers belongs to the fixture/API
  hardening in Phases 2, 5, and 6; they are not hidden by a project-wide suppression.
- Spring Boot 4.1 deprecates `EnvironmentPostProcessor`. FIT4J still needs the processor to keep optional gRPC
  auto-configuration from loading on an absent classpath. Its replacement is part of the optional-classpath and
  auto-configuration review in Phases 2 and 3.
- Generated protobuf Java reports deprecated API use, and protobuf-java 4.34.2 emits `sun.misc.Unsafe` removal
  warnings on JDK 25. Both originate in generated/upstream code rather than FIT4J source and must be rechecked
  during the dependency freeze in Phase 8.
- Embedded Kafka 4.2.1 can log a shutdown-time `NoSuchFileException` while the suite still passes. This is test
  infrastructure teardown noise and is tracked for stabilization in Phase 6.
- Integration tests use embedded Kafka, Redis, PostgreSQL, DynamoDB Local, H2, and optional Testcontainers.
  Test tasks currently reserve up to 5 GiB heap and fresh builds require outbound access for Gradle toolchains
  and dependencies. Container-backed cases require a working Docker-compatible runtime; service ports are
  dynamically assigned rather than fixed.

### Resolve the Gradle/toolchain failure

Reproduce the `JvmVendorSpec.IBM_SEMERU` failure with `--stacktrace` and identify the incompatible component.
Likely candidates include the Gradle 9.1.0 wrapper, Foojay resolver convention 0.9.0, or a plugin compiled
against a different Gradle toolchain API.

Select compatible, documented versions instead of relying on a developer-local cache. Verify from an empty
Gradle user home because that is the closest local approximation to a new contributor and CI environment.

### Make the Java verification matrix truthful

The build requests a Java 25 toolchain even when CI installs Java 17. Decide and document what each matrix row
is intended to prove:

- build FIT4J and run its tests on JDK 25 while producing Java 17 bytecode;
- compile and run consumer fixtures on JDK 17;
- optionally run the library suite on additional supported JDKs if the selected toolchain permits it.

Do not label a CI job `JDK 17` if Gradle silently downloads and executes all relevant work on JDK 25.

### Establish the clean baseline

Run, from a fresh Gradle user home:

```bash
./gradlew clean check --no-configuration-cache
./gradlew clean check --configuration-cache
```

Inventory all warnings and flaky or environment-dependent tests. Record which suites require Docker, native
binaries, fixed ports, high memory, or outbound downloads.

Acceptance criteria:

- A clean checkout configures without the `IBM_SEMERU` failure.
- `clean check` passes twice from a fresh Gradle user home.
- CI and local setup use the same checked-in wrapper and compatible plugin versions.
- The Java 17 consumer guarantee and Java 25 development toolchain are tested separately and accurately named.
- Configuration-cache support is either verified or explicitly documented as unsupported with a tracked reason.
- No unexplained Gradle, Kotlin, Java, or framework deprecation warning remains.

## Phase 2: Define the 1.0 feature and compatibility contract

**Status:** Complete (2026-10-20)

### Execution record

- Added `COMPATIBILITY.md` as the normative FIT4J 1.x platform, stability, semantic-versioning, deprecation,
  configuration-format, and concurrency contract.
- Replaced the README's broad compatibility claims with one exact matrix derived from the resolved Gradle
  dependency graph and CI runtime matrix.
- Identified the Spring Boot 4.1 BOM as part of the then-current published dependency contract and deferred the
  final publication policy to Phase 3, where it was removed from consumer metadata.
- Classified every user-facing integration and extension surface as stable API, stable SPI, experimental, or
  internal implementation, with FIT4J maintainers as the explicit owner.
- Classified network fault injection and its Toxiproxy control surface as stable API. There are currently no
  experimental APIs and no APIs deprecated for removal before 1.0.
- Documented the 1.x source/binary compatibility promise, YAML and property compatibility, experimental API
  exception, minimum deprecation window, and emergency exception policy.
- Confirmed that same-JVM JUnit parallel execution remains unsupported while isolated multi-process forks are
  supported subject to memory, port, container, and external-service isolation.

### Freeze the supported platform matrix

Document exact supported and tested lines for:

| Component | 1.0 decision required |
|---|---|
| Java runtime | Minimum 17; list every CI-tested JDK |
| Java bytecode | 17 |
| Kotlin | Compiler used to build FIT4J and supported consumer range |
| Spring Boot | Exact 4.1.x baseline and forward-patch policy |
| JUnit Platform | Supported JUnit 5 line |
| gRPC | Boot starter, grpc-java, and grpc-kotlin lines |
| Testcontainers | Supported line and Docker requirements |
| Kafka | Embedded and container-backed support |
| Databases | H2, MySQL, and PostgreSQL behavior tested by FIT4J |
| Redis | Embedded and Testcontainers-backed support |
| Elasticsearch | Client and container versions |
| DynamoDB Local | Supported version and platform limitations |

Clarify whether Spring Boot's BOM is part of FIT4J's published dependency contract or only a build-time
alignment mechanism. A library should not unexpectedly constrain unrelated dependencies in consuming projects.

### Define the supported feature set

Classify each integration as one of:

- stable 1.0 API;
- stable 1.0 extension SPI;
- experimental, with an explicit compatibility disclaimer;
- deprecated and scheduled for removal before 1.0;
- internal implementation detail.

At minimum, classify `@FIT`, `@IT`, fixtures, HTTP DSL, gRPC DSL, Kafka tracking, database cleanup, embedded
PostgreSQL/Redis/DynamoDB, Testcontainers definitions, fault injection, and the public auto-configuration types.

### Document the semantic-versioning promise

Define what compatibility means for:

- Kotlin and Java source compatibility;
- JVM binary compatibility;
- Spring bean names and auto-configuration behavior;
- annotations and their default values;
- YAML/property keys and default values;
- DSL source compatibility;
- serialized fixture and Testcontainers configuration formats.

Acceptance criteria:

- README contains one exact, internally consistent compatibility table.
- Every integration has an owner and stability classification.
- Experimental APIs are visibly marked and excluded from the stable compatibility promise where tooling allows.
- The 1.x deprecation and removal policy is documented.
- Parallel-execution limitations and supported multi-process behavior match tested behavior.

## Phase 3: Rationalize the published dependency surface

**Status:** Complete (2026-10-27)

### Execution record

- Added `DEPENDENCY_SURFACE.md` with a source-backed inventory of every direct production dependency, public
  signature exposure, chosen Gradle scope, feature classpath additions, and publication guarantees.
- Finalized a single batteries-included artifact for the entire 1.x line. A module split is deferred to 2.0 or
  later because changing coordinates within 1.x would undermine the compatibility promise. A late split before
  1.0 would simultaneously change packages, coordinates, Spring discovery metadata, and DSL entry points; the
  FIT4J JAR itself is only about 0.5 MiB.
- Removed three unused Resilience4j dependencies and moved MockK from production runtime to test-only scope.
- Promoted dependencies exposed by stable signatures to `api`; retained implementation-only integration and
  reflective/container providers in runtime scope; kept Boot/gRPC starters compile-only.
- Stopped publishing the Spring Boot BOM. Direct published dependencies now have explicit versions, leaving
  consumer applications in control of their Boot BOM and unrelated dependency alignment.
- Added isolated-classloader tests for core/HTTP/database use without gRPC or container/client stacks and
  activation tests proving embedded/container integrations remain inactive unless selected.
- Corrected gRPC presence detection to use the active thread context classloader, which is required in isolated
  test engines, plugin hosts, and layered classloader environments.
- Regenerated and inspected the Maven POM and Gradle module metadata: API/runtime scopes are distinct, all
  direct versions are present, and the Boot BOM, MockK, Resilience4j, test-only dependencies, and compile-only
  gRPC dependencies are absent.
- Aligned the examples wrapper distribution with Gradle 9.1.0 and verified all 13 example projects through the
  composite build against the revised dependency surface.

### Inventory every main dependency

For every dependency in the root `build.gradle.kts`, identify:

- the FIT4J source files that require it;
- whether its types occur in public signatures;
- whether it is required for every consumer or only one integration;
- whether it belongs in `api`, `implementation`, `compileOnly`, `runtimeOnly`, or test scope;
- what appears in the generated Maven POM and Gradle module metadata.

Pay special attention to dependencies that appear unrelated to current main-source usage, including MockK and
Resilience4j, and to libraries usually considered test infrastructure, including Spring Boot test modules,
Spring Kafka test, H2, Testcontainers, and embedded services.

### Decide single artifact versus feature modules

Make an explicit 1.0 decision between:

1. retaining one batteries-included artifact and documenting its dependency cost; or
2. splitting optional integrations into modules such as `fit4j-core`, `fit4j-http`, `fit4j-grpc`,
   `fit4j-kafka`, `fit4j-testcontainers`, and `fit4j-embedded-*`.

Do not perform a late module split only for cosmetic reasons. Base the decision on clean consumer dependency
graphs, optional-classpath behavior, artifact size, and whether users can safely use core features without
installing every integration stack. The decision is final for FIT4J 1.x: publish only
`io.github.harezmi:fit4j`. Feature modules will not be introduced during 1.x. Reconsider modularization for
2.0 only, with measured consumer graphs and a migration plan.

### Verify optional-classpath behavior

Auto-configurations and Spring test context customizer factories must not fail class loading when an optional
integration dependency is absent. Add isolated context tests for representative combinations:

- HTTP/database support without gRPC;
- core annotations without Docker/Testcontainers;
- gRPC only when the Boot gRPC starters are present;
- embedded services absent unless explicitly selected;
- no Elasticsearch, Redis, Kafka, or DynamoDB client unless that feature is used.

Acceptance criteria:

- No unused production dependency remains.
- Public signatures are backed by `api` dependencies where consumers must compile against their types.
- Optional integrations either have isolated modules or verified conditional class loading.
- The generated POM and Gradle metadata express the intended dependency policy.
- A dependency report documents the minimal classpath for each supported feature.
- Adding FIT4J to a test scope does not unexpectedly constrain unrelated application dependencies.

## Phase 4: Define and freeze the public API

**Status:** Complete (2026-10-27)

### Execution record

- Added `API_SURFACE.md`, classifying supported application API, extension SPI, and framework-visible internal
  infrastructure. FIT4J 1.0 has no experimental API category.
- Added Kotlin Binary Compatibility Validator 0.18.2 and the reviewed `api/fit4j.api` baseline.
  Spring discovery packages and explicitly listed implementation helpers are excluded; generated test protobuf
  types never enter the main-source baseline.
- Wired `apiCheck` into `check`, so the existing CI `check` job and later release verification cannot silently
  accept an unreviewed binary API change.
- Added compile-only Java and Kotlin consumer fixtures covering `@FIT`, `@IT`, fixture selection, the HTTP DSL,
  Kafka tracking, embedded PostgreSQL, and extension interfaces. Existing Java/Kotlin integration tests cover
  the gRPC DSL and broader runtime behavior.
- Added Java overloads for `KafkaMessageTracker` constructor and topic-query defaults discovered during the
  Java ergonomics review.
- Added reflection-based contract tests for stable annotation defaults and FIT4J property keys, which are not
  represented by a JVM signature dump.
- Added lifecycle, concurrency, nullability, and failure KDoc to the primary annotations, `Fit4J`, and
  `KafkaMessageTracker`. Comprehensive KDoc coverage is explicitly non-blocking for 1.0.0 and is planned as
  follow-up documentation work for 1.0.1.

### Classify current public declarations

The main source set exposes many public classes across auto-configuration, context lifecycle, helpers, HTTP,
gRPC, Kafka, database cleanup, embedded services, and Testcontainers packages. Review every public declaration
and assign it to:

- supported application API;
- supported extension SPI;
- experimental API;
- internal infrastructure.

Reduce visibility or move infrastructure to an internal package where Spring reflection does not require a
public type. Avoid freezing implementation helpers as permanent 1.x API merely because Kotlin defaults to
public visibility.

### Add binary API validation

Introduce a maintained compatibility tool suitable for Kotlin/JVM, such as Kotlin Binary Compatibility
Validator, and commit the reviewed 1.0 API baseline:

```bash
./gradlew apiDump
./gradlew apiCheck
```

Exclude generated protobuf test code and intentionally internal packages. Wire `apiCheck` into `check`, CI,
and the release verification task.

### Validate Java and Kotlin ergonomics

Create compiled consumer fixtures that exercise representative stable APIs from both languages:

- `@FIT` and `@IT`;
- HTTP and gRPC DSL entry points;
- fixture selection;
- Kafka verification;
- embedded PostgreSQL and Testcontainers definitions;
- supported extension interfaces.

Review Kotlin default arguments, `@JvmStatic`, `@JvmOverloads`, nullability, SAM interfaces, and exception types
for usable Java call sites.

Acceptance criteria:

- The checked-in API baseline contains only intentional API and SPI.
- Accidental binary API changes fail CI.
- Primary public entry points and non-obvious API/SPI contracts document lifecycle, thread-safety, nullability,
  and failure behavior. Comprehensive stable-API KDoc is deferred to 1.0.1 and does not block 1.0.0.
- Java and Kotlin consumer fixtures compile without accessing internal implementation classes.
- Annotation defaults and configuration keys intended for 1.x are covered by compatibility tests.

## Phase 5: Harden lifecycle, isolation, and core behavior

**Status:** Not started

### Verify Spring context lifecycle

Add focused tests for:

- context caching across FIT and non-FIT tests;
- cleanup after successful and failed tests;
- cleanup when setup or teardown throws;
- parent/child application contexts;
- multiple test classes in the same JVM;
- sequential test methods with different fixtures;
- no retained test execution state after a suite completes.

### Verify execution correlation and concurrency boundaries

The README states that in-process concurrent FIT/IT execution is unsupported and that multiple Gradle forks are
supported. Convert those claims into tests:

- HTTP and gRPC worker-thread execution IDs are associated with the correct test;
- missing or malformed execution IDs fail predictably;
- separate JVM forks do not share state;
- fixed-port embedded resources fail with actionable diagnostics;
- unsupported same-JVM parallelism is detected or clearly guarded where possible.

### Review failure contracts

Inspect placeholder and exceptional behavior before freezing 1.0, including
`NoopDatabaseTestSupport.connection()` throwing `NotImplementedError`. Replace placeholders with intentional,
documented exceptions or supported behavior.

Acceptance criteria:

- Lifecycle tests pass repeatedly and in randomized class order.
- No static registry or thread-local state leaks between tests.
- Expected cleanup occurs after both assertion failures and infrastructure failures.
- Every unsupported operation throws a documented, actionable exception.
- The concurrency section of the README is backed by executable verification.

## Phase 6: Complete integration and platform coverage

**Status:** Not started

### Define the integration matrix

Create a test matrix for each stable integration:

| Area | Minimum 1.0 verification |
|---|---|
| HTTP | request matching, sequences, headers, JSON/body variants, error propagation |
| gRPC | unary calls, metadata propagation, descriptors, custom channels, status errors |
| Kafka | embedded and container modes, headers, protobuf, cleanup, timeouts |
| Database cleanup | H2, MySQL, PostgreSQL; exclusions, identifiers, transactions |
| Embedded PostgreSQL | random/fixed port, init scripts, startup failure, cleanup |
| Embedded Redis | random/fixed port, cleanup, platform behavior |
| DynamoDB Local | startup, isolation, shutdown, supported architectures |
| Testcontainers | selective registration, inheritance, data population, cleanup |
| Elasticsearch | readiness, document population, client compatibility |
| Toxiproxy | defaults, explicit upstream ports, lifecycle, invalid definitions |
| Declarative fixtures | class/method precedence, groups, predicates, cleanup |

Separate fast deterministic tests from Docker/native integration tests while keeping one release task that runs
all required suites.

### Eliminate environmental flakiness

Remove reliance on globally fixed ports where a random port is supported. Ensure containers and embedded
processes are stopped after failure. Add bounded readiness checks and actionable timeout messages. Document
minimum Docker resources and native-platform exclusions.

Acceptance criteria:

- Every stable integration has at least one end-to-end test.
- Docker-backed and Docker-free suites are independently runnable.
- All resource-owning tests prove cleanup on success and failure.
- Required suites pass on the supported Linux and macOS environments, or limitations are explicit.
- Repeated release-suite execution produces no intermittent port, process, or container leaks.

## Phase 7: Make examples and external consumers test the candidate artifact

**Status:** Not started

### Publish to an isolated local test repository

Add a verification-only publication repository under `build/`, and make consumer builds resolve the exact
candidate version from it. Do not use `mavenLocal()`, which can silently supply a stale artifact.

### Correct the examples build

The current examples pin `fit4jVersion=0.1.9`; consequently CI examples validate the previous release rather
than current source. Rework the examples verification so it consumes the locally published
`1.0.0-SNAPSHOT` candidate and its real POM/module metadata.

Remove duplicate or misleading project dependencies that bypass published coordinates. Each example should
declare only the application/integration dependencies a real consumer needs.

### Add minimal external consumer fixtures

In addition to the feature examples, add small isolated builds for:

- Kotlin Gradle consumer on Java 17;
- Java Gradle consumer on Java 17;
- Maven consumer using only POM metadata;
- non-gRPC application;
- application without Docker/Testcontainers;
- application using one optional integration at a time;
- Spring Boot context with FIT4J present but no FIT4J test active.

Acceptance criteria:

- Examples and fixtures consume the candidate publication, never a previous Central or Maven Local artifact.
- Both Maven POM and Gradle module metadata are tested.
- Java 17 consumers compile and run the Java 17 bytecode.
- Optional integrations do not become mandatory through metadata or class loading.
- Every documented example is included in automated verification or explicitly marked manual.

## Phase 8: Verify artifact contents, metadata, and supply-chain inputs

**Status:** Not started

### Add artifact-content verification

Create a Gradle task that inspects the candidate JARs and fails on unexpected content. Verify that the main
artifact contains:

- compiled production classes;
- Spring auto-configuration and environment processor metadata;
- required license and notice files.

It must not contain:

- test or example classes;
- generated test protobuf classes;
- credentials or local paths;
- debug logs;
- redundant empty application configuration unless explicitly justified.

Delete the empty `src/main/resources/application.properties` if it has no intentional runtime purpose.

### Validate Maven Central metadata

Generate and inspect:

- main JAR;
- sources JAR;
- Javadoc JAR;
- POM;
- Gradle module metadata;
- detached `.asc` signatures for all required artifacts.

Confirm coordinates, project URL, SCM URLs, developer information, license metadata, dependency scopes,
checksums, and Java target. Validate that no snapshot dependency appears in the final release metadata.

### Review licenses and vulnerabilities

Regenerate or verify the third-party license inventory against the resolved 1.0 runtime graph. Add automated
dependency vulnerability scanning with a documented exception process. Never fail a release solely on an
unreviewed scanner result; every finding must be triaged for reachability and actual exposure.

Acceptance criteria:

- Artifact-content verification is wired into `check`.
- `LICENSE` and `NOTICE` are present where required.
- Generated POM and module metadata pass automated assertions.
- Sources and Javadoc artifacts are usable and contain the intended public documentation.
- Every Central-required artifact has a verifiable signature.
- No secret, machine-specific path, snapshot dependency, or development-only resource is published.
- Third-party license and vulnerability reports are reviewed for the exact release graph.

## Phase 9: Complete documentation, support policy, and release notes

**Status:** Not started

### Synchronize versioned documentation

Run the existing `syncDocsVersion` workflow for `1.0.0-SNAPSHOT`, then for the final `1.0.0`, and review every
change. Fix stale claims rather than only replacing version strings, including the README's Gradle 8.14.4 text
when the wrapper is 9.1.0.

Ensure README, `index.html`, example properties, example README, publishing guides, and migration guide agree
on coordinates, Spring Boot support, Java requirements, optional gRPC dependencies, and snapshot resolution.

### Add release-governance documents

Create or update:

- `CHANGELOG.md`, including all user-visible changes since `0.1.9`;
- `SECURITY.md`, with reporting instructions and supported release lines;
- `CONTRIBUTING.md`, with the real build, Docker, examples, and release-check commands;
- semantic-versioning and deprecation policy;
- support policy for the 1.0.x line;
- 1.0.0 release notes and upgrade notes from 0.1.x.

### Document operational limitations

Confirm and document:

- same-JVM parallel test limitations;
- Docker and native embedded-service requirements;
- fixed-port behavior;
- context caching implications;
- database cleanup transaction requirements and exclusions;
- optional integration dependencies;
- supported configuration formats and property names;
- platform-specific embedded DynamoDB, Redis, and PostgreSQL limitations.

Acceptance criteria:

- No `0.1.9` dependency example remains in final 1.0 documentation except historical changelog/migration text.
- Documentation version markers match `gradle.properties` after synchronization.
- Installation examples work in clean Maven and Gradle consumers.
- Every known limitation is discoverable from the README.
- Changelog, security policy, support policy, and release notes are reviewed before the release commit.

## Phase 10: Add a single release-verification gate

**Status:** Not started

### Create `releaseCheck`

Add a non-publishing Gradle task:

```bash
./gradlew releaseCheck
```

It should depend on or orchestrate:

- clean compilation and all required test suites;
- API compatibility validation;
- artifact-content validation;
- POM and Gradle metadata assertions;
- sources and Javadoc JAR generation;
- isolated local publication;
- Java/Kotlin and Maven/Gradle consumer fixtures;
- candidate-based examples;
- documentation/version consistency checks;
- license and reviewed vulnerability checks.

Keep GPG signing and Central publication separate because they require private credentials and should not run
on ordinary pull requests.

### Strengthen CI

Run `releaseCheck` on pull requests and the main branch, with appropriate separation for Docker/native suites.
Pin third-party GitHub Actions to reviewed versions or commit SHAs. Upload test reports on failure and cache only
safe, reproducible build inputs.

Acceptance criteria:

- One documented command reproduces the complete non-secret release gate locally.
- CI executes the same release gate rather than a weaker collection of ad hoc tasks.
- CI fails if examples use an old FIT4J artifact, API changes are unreviewed, metadata is wrong, or docs are stale.
- No Maven Central or GPG private credential is available to pull-request jobs.
- A successful release gate leaves the Git working tree clean.

## Phase 11: Publish FIT4J 1.0.0

**Status:** Blocked by Phases 1-10

Publish locally from the verified main-branch commit using the already documented GPG and Central Portal setup:

1. Confirm all planned API and dependency decisions are complete.
2. Set `version=1.0.0` in `gradle.properties`.
3. Run `./gradlew syncDocsVersion` and review all generated documentation changes.
4. Run `./gradlew releaseCheck` from a clean environment.
5. Run `./gradlew signMavenPublication` and verify every expected signature with GPG.
6. Inspect the final JARs, POM, Gradle metadata, dependency graph, sources, Javadoc, license, and notice files.
7. Commit the release version and documentation on `main` and verify the working tree is clean.
8. Create an annotated local `v1.0.0` tag on that exact commit, but do not push it yet.
9. Publish with `./gradlew publishToMavenCentral --no-configuration-cache`.
10. Confirm Central Portal validation and automatic publication complete successfully.
11. Resolve `io.github.harezmi:fit4j:1.0.0` in fresh Maven and Gradle consumers without local repositories.
12. Push the release commit and `v1.0.0` tag.
13. Create the GitHub release from that tag using the reviewed 1.0.0 release notes.
14. Set `version=1.1.0-SNAPSHOT`, run `syncDocsVersion`, and commit the next development version separately.

The tag must identify the exact source commit that produced the immutable Maven Central artifacts. No source,
dependency, or build change may occur between final verification, tagging, signing, and publication.

If API review or external consumer testing reveals meaningful uncertainty, publish `1.0.0-RC1` first and run
the same consumer verification against Central. Do not use an RC merely to bypass incomplete release gates.

Acceptance criteria:

- Central Portal reports the deployment as published.
- Signatures resolve to the documented public key on `keys.openpgp.org`.
- Fresh Maven and Gradle consumers resolve and execute representative FIT4J tests.
- The Git tag, GitHub release, POM version, JAR manifest/content, and source commit all agree.
- Development continues from `1.1.0-SNAPSHOT` only after the final artifact is independently verified.

## Recommended execution order

1. Fix the Gradle/toolchain startup failure and establish a repeatable clean build.
2. Decide and document the supported platform, feature, and semantic-versioning contract.
3. Audit dependencies and decide whether the single-artifact design is acceptable for 1.0.
4. Classify and freeze the public API with automated binary compatibility validation.
5. Harden lifecycle, state cleanup, failure behavior, and concurrency boundaries.
6. Complete the stable-integration test matrix and eliminate environmental flakiness.
7. Make examples and external fixtures consume the locally published candidate artifact.
8. Add artifact, metadata, license, vulnerability, and documentation verification.
9. Create `releaseCheck`, make CI run it, and prepare the changelog, security policy, and release notes.
10. Publish, independently resolve, tag, announce, and advance to `1.1.0-SNAPSHOT`.

Phases 2-4 can change coordinates, dependency metadata, public signatures, or configuration contracts. Complete
them before generating the 1.0 API baseline or declaring a release candidate. Phases 5-10 should then prove that
the frozen contract works for real consumers and can be reproduced without developer-local state.
