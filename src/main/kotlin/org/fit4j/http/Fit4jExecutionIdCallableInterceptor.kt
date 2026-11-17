package org.fit4j.http

import jakarta.servlet.http.HttpServletRequest
import org.fit4j.context.Fit4jTestExecutionConstants
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.async.CallableProcessingInterceptor
import java.util.concurrent.Callable

/**
 * Binds the request execution id while Spring MVC runs a controller [Callable] on its async executor.
 */
internal class Fit4jExecutionIdCallableInterceptor : CallableProcessingInterceptor {

    private val previousExecutionId = ThreadLocal<String?>()
    private val bindingActive = ThreadLocal<Boolean>()

    override fun <T : Any> preProcess(webRequest: NativeWebRequest, task: Callable<T>) {
        releaseBinding()
        val executionId = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?.getHeader(Fit4jTestExecutionConstants.EXECUTION_ID_HTTP_HEADER)
            ?.takeIf { it.isNotBlank() }
            ?: return
        previousExecutionId.set(Fit4jTestExecutionRegistry.swapCurrentExecutionId(executionId))
        bindingActive.set(true)
    }

    override fun <T : Any> postProcess(
        webRequest: NativeWebRequest,
        task: Callable<T>,
        concurrentResult: Any?,
    ) {
        releaseBinding()
    }

    override fun <T : Any> afterCompletion(
        request: NativeWebRequest,
        task: Callable<T>,
    ) {
        releaseBinding()
    }

    private fun releaseBinding() {
        if (bindingActive.get() != true) {
            return
        }
        Fit4jTestExecutionRegistry.restoreCurrentExecutionId(previousExecutionId.get())
        previousExecutionId.remove()
        bindingActive.remove()
    }
}
