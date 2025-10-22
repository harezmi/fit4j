package com.fit4j.mock

class MockResponseUnavailableException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}