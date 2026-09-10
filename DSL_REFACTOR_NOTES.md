# Shared HTTP/gRPC DSL Internals

## Scope

The public Kotlin and Java DSL entry points, builders, overloads and fluent
return types are unchanged. No scenario scope, protocol-neutral public builder
or new inheritance hierarchy is introduced.

## Shared Components

- `ResponseSequence<T>`: snapshots response definitions, selects responses in
  order, repeats the last response and supports reset. Selection and reset are
  synchronized; completed request objects are no longer retained.
- `TestTrainingRegistry<T, Q, R>`: handles current-test storage, first-match
  selection and cleanup. HTTP and gRPC retain their separate namespaces and
  storage keys. Existing registry objects delegate through composition.
- `DslExpressionSupport`: shares Spring-backed predicate validation/evaluation
  and text interpolation. Small protocol-specific adapters preserve call sites
  and diagnostic messages.

## Preserved Boundaries

Request matching, HTTP headers/body handling, protobuf conversion, gRPC status
handling and response serialization remain protocol-specific. The existing
gRPC response expression conversion pipeline is unchanged; its unused DSL text
resolver was removed.

DSL/provider/builder/YAML precedence is unchanged. Registration order still
determines the first matching DSL training. Duplicate registration and unfinished
training validation are not changed. Empty DSL response sequences still use
their existing builder defaults; raw empty training definitions still fail on
consumption. YAML fixture request tracking is not modified.

## Verification

Run:

```shell
./gradlew --no-daemon test --tests 'org.fit4j.dsl.*' --tests 'org.fit4j.http.*' --tests 'org.fit4j.grpc.*' --tests 'org.fit4j.mock.declarative.TestFixtureGroupStateCleanupFIT'
```

New tests cover sequence snapshots, final-response repetition, reset, concurrent
consumption, raw empty sequences, first-match precedence, unmatched requests,
protocol isolation, registry cleanup/re-registration and test-method isolation.
Existing HTTP/gRPC Java and Kotlin integration tests cover the unchanged public
DSL usage.

Latest run: 40 tests, 36 passed, 3 failed, 1 skipped. All five new tests
passed. The same three failures were reproduced from an unchanged HEAD archive:
two HTTP cleanup assertions compare a string with HttpResponseBody.Text, and
the HTTP DSL integration test records an untrained /dsl/request-body/json call.
These pre-existing failures were left outside this refactor.
