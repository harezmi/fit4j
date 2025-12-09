package org.fit4j.autoconfigure

import org.fit4j.context.ApplicationContextLifecycleListener
import org.fit4j.mock.CallTrace
import org.fit4j.mock.CallTraceFactory
import org.fit4j.mock.MockResponseFactory
import org.fit4j.mock.MockResponseProvider
import org.fit4j.mock.MockServiceCallTracker
import org.fit4j.mock.declarative.DeclarativeTestFixtureDrivenServiceResponseProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment

@AutoConfiguration
@EnableOnFIT
class TestMockAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun applicationContextLifecycleListener() : ApplicationContextLifecycleListener {
        return ApplicationContextLifecycleListener()
    }

    @Bean
    fun mockServiceCallTracker(callTaceFactoryList: List<CallTraceFactory<*,*,*>>) : MockServiceCallTracker {
        return MockServiceCallTracker(callTaceFactoryList as List<CallTraceFactory<Any,Any, CallTrace<*,*>>>)
    }

    @Bean
    fun mockServiceResponseFactory(mockResponseProviderList: List<MockResponseProvider>,
                                   configurableEnvironment: ConfigurableEnvironment,
                                   declarativeTestFixtureDrivenServiceResponseProvider: DeclarativeTestFixtureDrivenServiceResponseProvider): MockResponseFactory {
        return MockResponseFactory(mockResponseProviderList,
                                            configurableEnvironment,
                                            declarativeTestFixtureDrivenServiceResponseProvider)
    }
}