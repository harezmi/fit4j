package com.udemy.libraries.acceptancetests.autoconfigure

import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.sas.JWTProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

@AutoConfiguration
@ConditionalOnClass(JWTProvider::class)
@EnableOnAcceptanceTestClass
class TestSecurityAutoConfiguration {
    @Bean
    fun jwtProvider(environment: Environment): JWTProvider {
        return object : JWTProvider {
            override fun getJwt(): String {
                return environment.getProperty("udemy.test.jwt","udemy_test_jwt_value")
            }
        }
    }
}