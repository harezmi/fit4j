package com.udemy.libraries.acceptancetests.http

import com.udemy.libraries.acceptancetests.AcceptanceTest
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered

@AcceptanceTest
class HttpResponseJsonBuilderOrderingIntegrationTests {

    @Autowired
    private lateinit var responseBuilders: List<HttpResponseJsonBuilder>

    @TestConfiguration
    class TestConfig {
        @Bean
        fun httpResponseJsonBuilder1() : HttpResponseJsonBuilder {
            return HttpResponseJsonBuilder1()
        }

        @Bean
        fun httpResponseJsonBuilder2() : HttpResponseJsonBuilder {
            return HttpResponseJsonBuilder2()
        }
    }

    @Test
    fun `http response json builders should be provided according to the precedence`() {
        responseBuilders.map { it.toString() }.also {
            Assertions.assertEquals(listOf("HttpResponseJsonBuilder2", "HttpResponseJsonBuilder1"), it)
        }
    }
}

class HttpResponseJsonBuilder1 : HttpResponseJsonBuilder {
    override fun build(request: RecordedRequest): String? {
        return null
    }

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}

class HttpResponseJsonBuilder2 : HttpResponseJsonBuilder {
    override fun build(request: RecordedRequest): String? {
        return null
    }

    override fun toString(): String {
        return this.javaClass.simpleName
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }
}