package com.udemy.libraries.acceptancetests

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServiceAcceptanceTestsApplication {
}

fun main(args: Array<String>) {
    runApplication<ServiceAcceptanceTestsApplication>(*args)
}