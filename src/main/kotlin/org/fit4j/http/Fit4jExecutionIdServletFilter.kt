package org.fit4j.http

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.fit4j.context.Fit4jTestExecutionConstants
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Binds [Fit4jTestExecutionRegistry] on the servlet worker thread for inbound requests to the
 * system under test, using the same header that [Fit4jExecutionIdClientHttpRequestInterceptor]
 * attaches on [org.springframework.boot.resttestclient.TestRestTemplate] (and other configured clients).
 */
class Fit4jExecutionIdServletFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val executionId = request.getHeader(Fit4jTestExecutionConstants.EXECUTION_ID_HTTP_HEADER)
        Fit4jTestExecutionRegistry.runWithExecutionId(executionId) {
            filterChain.doFilter(request, response)
        }
    }
}
