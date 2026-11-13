package org.fit4j.autoconfigure

import org.fit4j.http.Fit4jExecutionIdServletFilter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter

@AutoConfiguration
@EnableOnFIT
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OncePerRequestFilter::class)
class Fit4jExecutionIdServletAutoConfiguration {

    @Bean
    fun fit4jExecutionIdServletFilterRegistration(): FilterRegistrationBean<Fit4jExecutionIdServletFilter> {
        return FilterRegistrationBean(Fit4jExecutionIdServletFilter()).apply {
            order = Ordered.HIGHEST_PRECEDENCE
        }
    }
}
