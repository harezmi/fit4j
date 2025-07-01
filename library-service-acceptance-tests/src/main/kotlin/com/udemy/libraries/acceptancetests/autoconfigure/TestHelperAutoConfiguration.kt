package com.udemy.libraries.acceptancetests.autoconfigure

import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelper
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelperConfiguration
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

}
