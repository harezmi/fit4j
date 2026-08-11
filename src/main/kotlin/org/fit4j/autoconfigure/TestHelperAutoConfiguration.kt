package org.fit4j.autoconfigure

import tools.jackson.databind.json.JsonMapper
import com.google.protobuf.util.JsonFormat
import org.fit4j.helper.BrowserLauncher
import org.fit4j.helper.JsonHelper
import org.fit4j.helper.VerificationHelper
import org.fit4j.kafka.KafkaMessageTracker
import org.fit4j.mock.MockServiceCallTracker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnFIT
class TestHelperAutoConfiguration(private val applicationContext: ApplicationContext) {

    @Bean
    @ConditionalOnMissingBean
    fun jsonHelper(
        @Autowired(required = false)
        jsonProtoParser: JsonFormat.Parser?,
        @Autowired(required = false)
        jsonProtoPrinter:JsonFormat.Parser?,
        jsonMapper: JsonMapper) : JsonHelper {
        return JsonHelper(jsonProtoParser, jsonProtoPrinter, jsonMapper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun verifier(
                jsonHelper: JsonHelper,
                 mockServiceCallTracker: MockServiceCallTracker,
                @Autowired(required = false)
                kafkaMessageTracker: KafkaMessageTracker?) : VerificationHelper {
        return VerificationHelper(jsonHelper,mockServiceCallTracker, kafkaMessageTracker)
    }

    @Bean
    fun browserLauncher() : BrowserLauncher {
        return BrowserLauncher()
    }

    private fun dbCleanUpEnabled() : Boolean {
        val prop = applicationContext.getEnvironment().getProperty("fit4j.dbcleanup","true")
        return if("none".equals(prop)) false
        else prop.toBoolean()
    }

}
