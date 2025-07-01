package com.udemy.libraries.acceptancetests.autoconfigure

import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.scope.TestScope
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