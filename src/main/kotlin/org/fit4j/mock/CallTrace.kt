package org.fit4j.mock

interface CallTrace<REQ,RES> {
    fun matchesRequestPath(path: String): Boolean

    fun getRequest(): REQ

    fun getResponse(): RES?

    fun hasError(): Boolean

    fun getError(): Throwable?

    fun getStatus(): Int

    fun getErrorMessage(): String?
}