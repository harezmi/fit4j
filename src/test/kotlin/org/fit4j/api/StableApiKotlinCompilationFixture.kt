package org.fit4j.api

import org.fit4j.Fit4J
import org.fit4j.annotation.FIT
import org.fit4j.annotation.FixtureForFIT
import org.fit4j.annotation.IT
import org.fit4j.kafka.KafkaMessageTracker
import org.fit4j.mock.MockResponseProvider
import org.fit4j.postgres.EmbeddedPostgresServer
import org.fit4j.testcontainers.TestContainerDataPopulator
import org.springframework.core.io.Resource

/** Compile-only coverage for supported Kotlin call sites. */
@FIT(fixtureFilePath = "fixtures.yml")
class StableApiKotlinCompilationFixture {
    private val tracker = KafkaMessageTracker()
    private val postgres = EmbeddedPostgresServer(0, "schema.sql")

    @FixtureForFIT("happy-path")
    fun supportedCallsCompile() {
        Fit4J.http {
            path("/health").respond { status(200) }
        }
        tracker.getMessagesPublishedAt("events")
        postgres.builder
    }

    private val responseProvider = object : MockResponseProvider {
        override fun isApplicableFor(request: Any?) = request is String
        override fun getResponseFor(request: Any?): Any = "response"
    }

    private val dataPopulator = object : TestContainerDataPopulator {
        override fun populateData(resource: Resource) = Unit
    }
}

@IT
class StableItKotlinCompilationFixture
