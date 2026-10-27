package org.fit4j.api;

import org.fit4j.Fit4J;
import org.fit4j.annotation.FIT;
import org.fit4j.annotation.FixtureForFIT;
import org.fit4j.annotation.IT;
import org.fit4j.kafka.KafkaMessageTracker;
import org.fit4j.mock.MockResponseProvider;
import org.fit4j.postgres.EmbeddedPostgresServer;
import org.fit4j.testcontainers.TestContainerDataPopulator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

/** Compile-only coverage for supported Java call sites. */
@FIT(fixtureFilePath = "fixtures.yml", webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class StableApiJavaCompilationFixture {

    private final KafkaMessageTracker tracker = new KafkaMessageTracker();
    private final EmbeddedPostgresServer postgres = new EmbeddedPostgresServer(0, "schema.sql");

    @FixtureForFIT(name = "happy-path")
    void supportedCallsCompile() {
        Fit4J.http(http -> http.path("/health").respond(response -> response.status(200)));
        tracker.getMessagesPublishedAt("events");
        postgres.getBuilder();
    }

    private final MockResponseProvider responseProvider = new MockResponseProvider() {
        @Override
        public boolean isApplicableFor(Object request) {
            return request instanceof String;
        }

        @Override
        public Object getResponseFor(Object request) {
            return "response";
        }

        @Override
        public int getOrder() {
            return 0;
        }
    };

    private final TestContainerDataPopulator dataPopulator = new TestContainerDataPopulator() {
        @Override
        public void populateData(Resource resource) {
            // Consumer-provided population hook.
        }
    };
}

@IT
class StableItJavaCompilationFixture {
}
