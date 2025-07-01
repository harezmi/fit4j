package com.udemy.libraries.acceptancetests.grpc

import com.google.protobuf.Message

fun interface GrpcResponseJsonBuilder<R:Message> {
        fun build(request: R?): String?
}
