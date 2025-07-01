package com.udemy.libraries.acceptancetests.context

import com.udemy.libraries.acceptancetests.redis.EmbeddedRedis
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
