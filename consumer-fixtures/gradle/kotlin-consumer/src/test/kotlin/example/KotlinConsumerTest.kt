package example

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import

@FIT
@Import(KotlinConsumerTest.TestApplication::class)
class KotlinConsumerTest {
    @Test
    fun `starts FIT4J from the published Java 17 artifact`() {
        assertNotNull(TestApplication::class.java)
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    class TestApplication
}
