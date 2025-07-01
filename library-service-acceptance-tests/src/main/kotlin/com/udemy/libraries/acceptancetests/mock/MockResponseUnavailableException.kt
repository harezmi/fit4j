package com.udemy.libraries.acceptancetests.mock

class MockResponseUnavailableException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}