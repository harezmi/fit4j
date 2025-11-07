package org.fit4j.http

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@FIT
@Disabled
class HttpResponseJsonBuilderOrderingFIT {

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
    override fun build(request: HttpRequest): String? {
        return null
    }

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}

class HttpResponseJsonBuilder2 : HttpResponseJsonBuilder {
    override fun build(request: HttpRequest): String? {
        return null
    }

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}