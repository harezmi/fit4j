package org.fit4j.http

import org.fit4j.context.Fit4jTestExecutionConstants
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class Fit4jExecutionIdClientHttpRequestInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: org.springframework.http.HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        Fit4jTestExecutionRegistry.currentExecutionId()?.let { id ->
            if (request.headers[Fit4jTestExecutionConstants.EXECUTION_ID_HTTP_HEADER].isNullOrEmpty()) {
                request.headers.set(Fit4jTestExecutionConstants.EXECUTION_ID_HTTP_HEADER, id)
            }
        }
        return execution.execute(request, body)
    }
}
