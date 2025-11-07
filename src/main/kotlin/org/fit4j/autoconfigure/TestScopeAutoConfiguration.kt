package org.fit4j.autoconfigure

import org.fit4j.scope.TestScope
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnFIT
class TestScopeAutoConfiguration {
    companion object {
        @Bean
        fun testScope(): TestScope {
            return TestScope()
        }

        @Bean
        fun customScopeConfigurer(testScope: TestScope): CustomScopeConfigurer {
            return CustomScopeConfigurer().apply {
                addScope("test", testScope)
            }
        }
    }
}