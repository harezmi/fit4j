package com.udemy.acceptancetests.examples.coroutines

import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger(Constants::class.java)

val RED: String = "\u001B[31m"
val GREEN: String = "\u001B[32m"
val BLUE: String = "\u001B[34m"
val RESET: String = "\u001B[0m"


class Constants {
    companion object {
        init {
            System.setProperty("kotlinx.coroutines.scheduler.core.pool.size","1")
            System.setProperty("kotlinx.coroutines.scheduler.max.pool.size","1")
        }
    }
}