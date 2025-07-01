package com.udemy.libraries.acceptancetests.mock

interface CallTrace {
    fun matchesRequestPath(path: String): Boolean

    fun getRequest(): Any

    fun getResponse(): Any?

    fun hasError(): Boolean

    fun getError(): Throwable?

    fun getStatus(): Int

    fun getErrorMessage(): String?
}