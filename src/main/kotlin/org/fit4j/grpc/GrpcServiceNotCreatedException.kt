package org.fit4j.grpc

class GrpcServiceNotCreatedException : RuntimeException {
    constructor(message:String, cause: Throwable):super(message,cause)
}