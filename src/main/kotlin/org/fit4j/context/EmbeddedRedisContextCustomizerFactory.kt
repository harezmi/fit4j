package org.fit4j.context

import org.fit4j.redis.EnableEmbeddedRedis
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class EmbeddedRedisContextCustomizerFactory : AbstractContextCustomizerFactory() {
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (isAnnotationPresent(testClass, EnableEmbeddedRedis::class.java))
            EmbeddedRedisContextCustomizer(AnnotationUtils.findAnnotation(testClass, EnableEmbeddedRedis::class.java)!!)
            else null
    }

}
