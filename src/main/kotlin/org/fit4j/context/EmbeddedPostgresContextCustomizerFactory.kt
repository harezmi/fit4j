package org.fit4j.context

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.fit4j.context.AbstractContextCustomizerFactory
import org.fit4j.postgres.EnableEmbeddedPostgres
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration
import java.net.ServerSocket
import kotlin.io.use
import kotlin.to

class EmbeddedPostgresContextCustomizerFactory : AbstractContextCustomizerFactory() {
    companion object {
        val customizer = EmbeddedPostgresContextCustomizer()
    }
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (isAnnotationPresent(testClass, EnableEmbeddedPostgres::class.java))
            customizer else null
    }
}



