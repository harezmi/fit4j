package com.fit4j.http

import com.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.io.ByteArrayOutputStream

@FIT
class MockResponseJsonConverterFIT {

    @Autowired
    private lateinit var httpResponseJsonConverter: HttpResponseJsonConverter

    @Test
    fun `it should build mock response from json content`() {
        val jsonContent = """
            {
                "body": "{\"message\" : \"Hello, world!\"}",
                "headers": {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer token"
                },
                "status": 200
            }
        """.trimIndent()
        val response = httpResponseJsonConverter.fromJson(jsonContent)
        Assertions.assertEquals(200, response.statusCode)
        Assertions.assertEquals("""{"message" : "Hello, world!"}""".trimIndent(), response.body)
        Assertions.assertEquals("application/json", response.headers!!.get("Content-Type"))
        Assertions.assertEquals("Bearer token", response.headers.get("Authorization"))
    }
}
