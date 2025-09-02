package com.fit4j.autoconfigure

import com.fit4j.EnableOnAcceptanceTestClass
import com.fit4j.legacy_api.sas.JWTProvider
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
                return environment.getProperty("fit4j.jwt","test_jwt_value")
            }
        }
    }
}