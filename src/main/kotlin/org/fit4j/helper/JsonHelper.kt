package org.fit4j.helper

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.util.JsonFormat

data class JsonHelper(
    val jsonProtoParser: JsonFormat.Parser?,
    val jsonProtoPrinter:JsonFormat.Parser?,
    val objectMapper: ObjectMapper)