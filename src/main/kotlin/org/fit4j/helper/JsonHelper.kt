package org.fit4j.helper

import com.google.protobuf.util.JsonFormat
import tools.jackson.databind.json.JsonMapper

data class JsonHelper(
    val jsonProtoParser: JsonFormat.Parser?,
    val jsonProtoPrinter: JsonFormat.Parser?,
    val jsonMapper: JsonMapper,
)
