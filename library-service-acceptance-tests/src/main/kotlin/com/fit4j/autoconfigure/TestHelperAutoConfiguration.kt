package com.fit4j.autoconfigure

import com.fit4j.EnableOnAcceptanceTestClass
import com.fit4j.helpers.AcceptanceTestHelper
import com.fit4j.helpers.AcceptanceTestHelperConfiguration
import com.fit4j.helpers.dbcleanup.DatabaseTestSupportForH2
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnAcceptanceTestClass
class TestHelperAutoConfiguration(private val applicationContext: ApplicationContext) {

    @Bean
    fun acceptanceTestHelper(): AcceptanceTestHelper {
        return AcceptanceTestHelper()
    }

    @Bean
    fun acceptanceTestHelperConfiguration(): AcceptanceTestHelperConfiguration {
        return AcceptanceTestHelperConfiguration()
    }

    @Bean
    fun databaseTestSupport(): DatabaseTestSupportForH2 {
        return DatabaseTestSupportForH2()
    }
}
