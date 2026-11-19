package example;

import org.fit4j.annotation.FIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@FIT
@Import(MavenConsumerTest.TestApplication.class)
class MavenConsumerTest {
    @Test
    void startsFit4jUsingOnlyPublishedPomMetadata() {
        assertNotNull(TestApplication.class);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
