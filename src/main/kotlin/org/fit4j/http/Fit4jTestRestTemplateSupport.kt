package org.fit4j.http

import org.springframework.boot.resttestclient.TestRestTemplate

/**
 * Keeps [TestRestTemplate]'s underlying [org.springframework.web.client.RestTemplate] ready to
 * propagate the FIT4J execution id after test code mutates the interceptor list (for example
 * {@code getInterceptors().clear()} in {@code @BeforeEach}).
 *
 * Boot 4's [TestRestTemplate] delegates to [org.springframework.web.client.RestTemplate];
 * [org.springframework.http.client.support.InterceptingHttpAccessor] caches an intercepting
 * request factory, so interceptor mutations must go through [RestTemplateInterceptorSupport].
 */
internal object Fit4jTestRestTemplateSupport {

    fun ensureExecutionIdPropagation(template: TestRestTemplate) {
        RestTemplateInterceptorSupport.updateInterceptors(template.restTemplate) { interceptors ->
            if (interceptors.none { it is Fit4jExecutionIdClientHttpRequestInterceptor }) {
                interceptors.add(0, Fit4jExecutionIdClientHttpRequestInterceptor())
            }
        }
    }
}
