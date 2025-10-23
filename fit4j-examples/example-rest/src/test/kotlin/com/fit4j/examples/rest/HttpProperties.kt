package com.fit4j.examples.rest

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("externalService")
data class HttpProperties(val protocol:String="http", val hostname:String="localhost", val port:Int=8080) {
}