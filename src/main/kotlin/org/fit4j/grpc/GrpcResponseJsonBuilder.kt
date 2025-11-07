package org.fit4j.grpc

import com.google.protobuf.Message
import org.fit4j.mock.MockResponseJsonBuilder

fun interface GrpcResponseJsonBuilder<R:Message> : MockResponseJsonBuilder<R>
