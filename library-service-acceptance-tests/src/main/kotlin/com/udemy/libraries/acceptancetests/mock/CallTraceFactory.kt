package com.udemy.libraries.acceptancetests.mock

import org.springframework.core.Ordered

interface CallTraceFactory : Ordered {
    fun create(request: Any, response: Any?, exception: Throwable? = null): CallTrace?

    override fun getOrder(): Int {
        return 0
    }
}