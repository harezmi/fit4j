package com.fit4j.autoconfigure

import com.fit4j.EnableOnAcceptanceTestClass
import com.fit4j.scope.TestScope
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnAcceptanceTestClass
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