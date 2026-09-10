package org.fit4j.grpc.dsl

import org.fit4j.dsl.ResponseSequence

import com.google.protobuf.Message
import io.grpc.Status
import java.util.function.Predicate

data class GrpcRequestMatcher(
    val requestType: Class<out Message> = Message::class.java,
    val predicate: Predicate<Message>? = null
) {
    fun matches(request: Message): Boolean {
        if (!requestType.isAssignableFrom(request.javaClass)) {
            return false
        }
        if (predicate != null && !predicate.test(request)) {
            return false
        }
        return true
    }
}

data class GrpcResponseDefinition(
    val statusCode: Int = Status.Code.OK.value(),
    val body: String? = null
) {
    fun toRawJsonContent(): String {
        return if (statusCode > Status.Code.OK.value()) {
            """
                throw {
                    "status": "${Status.fromCodeValue(statusCode).code.name}"
                }
            """.trimIndent()
        } else {
            body?.takeIf { it.isNotBlank() } ?: "{}"
        }
    }
}

class GrpcTrainingDefinition(
    val requestMatcher: GrpcRequestMatcher,
    responses: List<GrpcResponseDefinition>
) {
    private val responses = ResponseSequence(responses)

    fun matches(request: Message): Boolean = requestMatcher.matches(request)

    fun buildResponse(request: Message): String? {
        return responses.next().toRawJsonContent()
    }

    fun reset() {
        responses.reset()
    }
}
