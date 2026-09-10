package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import org.fit4j.dsl.DslExpressionSupport
import java.util.function.Predicate

internal object GrpcDslExpressionSupport {

    fun predicate(expression: String): Predicate<Message> =
        DslExpressionSupport.predicate(expression, "gRPC")
}
