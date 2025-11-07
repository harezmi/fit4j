package org.fit4j.mock

import org.springframework.core.Ordered

interface MockResponseProvider : Ordered {
    fun isApplicableFor(request: Any?): Boolean
    fun getResponseFor(request: Any?): Any?
    override fun getOrder(): Int = 0
}
