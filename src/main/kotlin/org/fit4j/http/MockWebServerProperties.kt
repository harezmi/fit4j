package org.fit4j.http

class MockWebServerProperties(
    val host: String = "localhost",
    val port: Int = 8080) {

    fun baseUrl(): String {
        return "http://$host:$port"
    }
}