package org.fit4j.http

import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.InterceptingClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

internal object RestTemplateInterceptorSupport {

    fun updateInterceptors(
        restTemplate: RestTemplate,
        configure: (MutableList<ClientHttpRequestInterceptor>) -> Unit,
    ) {
        val interceptors = restTemplate.interceptors.toMutableList()
        val before = interceptors.toList()
        configure(interceptors)
        if (interceptors == before) {
            return
        }
        restTemplate.setInterceptors(interceptors)
        val requestFactory = restTemplate.requestFactory
        val delegate = if (requestFactory is InterceptingClientHttpRequestFactory) {
            requestFactory.getDelegate()
        } else {
            requestFactory
        }
        restTemplate.setRequestFactory(delegate)
    }
}
