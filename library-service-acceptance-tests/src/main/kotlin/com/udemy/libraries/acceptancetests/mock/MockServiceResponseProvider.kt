package com.udemy.libraries.acceptancetests.mock

import org.springframework.core.Ordered

interface MockServiceResponseProvider : Ordered {
    fun isApplicableFor(request: Any?): Boolean
    fun getResponseFor(request: Any?): Any?
    override fun getOrder(): Int = 0
}
