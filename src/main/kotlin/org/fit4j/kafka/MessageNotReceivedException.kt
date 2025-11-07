package org.fit4j.kafka

class MessageNotReceivedException : RuntimeException {
    constructor(message: String) : super(message)
}