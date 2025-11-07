package org.fit4j.context

import org.fit4j.redis.EmbeddedRedis
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class EmbeddedRedisContextCustomizerFactory : AbstractContextCustomizerFactory() {
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (isAnnotationPresent(testClass, EmbeddedRedis::class.java))
            EmbeddedRedisContextCustomizer(AnnotationUtils.findAnnotation(testClass,EmbeddedRedis::class.java)!!)
            else null
    }

}
