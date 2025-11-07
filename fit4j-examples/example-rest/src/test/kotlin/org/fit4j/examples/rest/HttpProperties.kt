package org.fit4j.examples.rest

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("example-service")
data class HttpProperties(var protocol:String="http", var host:String="localhost", var port:Int=8080) {
}