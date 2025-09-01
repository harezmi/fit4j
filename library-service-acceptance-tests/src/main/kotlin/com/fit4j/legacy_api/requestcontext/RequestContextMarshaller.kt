package com.fit4j.legacy_api.requestcontext

import io.grpc.Metadata

class RequestContextMarshaller : Metadata.BinaryMarshaller<Any>{
    override fun toBytes(p0: Any?): ByteArray? {
        return null
    }

    override fun parseBytes(p0: ByteArray?): Any? {
        return null
    }
}