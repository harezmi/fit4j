package com.fit4j.autoconfigure

import com.fit4j.EnableOnFIT
import com.fit4j.helpers.FitHelper
import com.fit4j.helpers.FitHelperConfiguration
import com.fit4j.helpers.dbcleanup.DatabaseTestSupport
import com.fit4j.helpers.dbcleanup.DatabaseTestSupportForH2
import com.fit4j.helpers.dbcleanup.DatabaseTestSupportForMysql
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnFIT
class TestHelperAutoConfiguration(private val applicationContext: ApplicationContext) {

    @Bean
    fun acceptanceTestHelper(): FitHelper {
        return FitHelper()
    }

    @Bean
    fun acceptanceTestHelperConfiguration(): FitHelperConfiguration {
        return FitHelperConfiguration()
    }

    @Bean
    fun databaseTestSupport(): DatabaseTestSupport {
        val driverName = applicationContext.environment.getProperty("spring.datasource.driver-class-name")
        if("com.mysql.cj.jdbc.Driver".equals(driverName)) {
            return DatabaseTestSupportForMysql()
        }
        return DatabaseTestSupportForH2()
    }
}
