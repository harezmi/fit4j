package org.fit4j.mock.declarative

import org.springframework.core.Ordered

interface JsonToMockResponseConverter : Ordered {
    override fun getOrder(): Int = 0
    fun isApplicableFor(request: Any?): Boolean
    fun convert(rawJsonContent: String, request: Any): Any
}