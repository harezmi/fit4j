package org.fit4j.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.http.server.LocalTestWebServer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata

@AutoConfiguration
@EnableOnFIT
@Conditional(TestRestTemplateAutoConfiguration.LocalServerCondition::class)
@AutoConfigureTestRestTemplate
class TestRestTemplateAutoConfiguration {
    internal class LocalServerCondition : Condition {
        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
            val applicationContext = context.resourceLoader as? ApplicationContext ?: return false
            return LocalTestWebServer.get(applicationContext) != null
        }
    }
}
