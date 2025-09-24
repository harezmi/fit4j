package com.fit4j.autoconfigure

import com.fit4j.EnableOnAcceptanceTestClass
import com.fit4j.context.ApplicationContextLifecycleListener
import com.fit4j.mock.CallTraceFactory
import com.fit4j.mock.MockServiceCallTracker
import com.fit4j.mock.MockServiceResponseFactory
import com.fit4j.mock.MockServiceResponseProvider
import com.fit4j.mock.declarative.DeclarativeTestFixtureDrivenServiceResponseProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment

@AutoConfiguration
@EnableOnAcceptanceTestClass
class TestMockAutoConfiguration {
    @Bean
    fun applicationContextLifecycleListener() : ApplicationContextLifecycleListener {
        return ApplicationContextLifecycleListener()
    }

    @Bean
    fun mockServiceCallTracker(callTaceFactoryList: List<CallTraceFactory>) : MockServiceCallTracker {
        return MockServiceCallTracker(callTaceFactoryList)
    }

    @Bean
    fun mockServiceResponseFactory(mockServiceResponseProviderList: List<MockServiceResponseProvider>,
                                   configurableEnvironment: ConfigurableEnvironment,
                                   declarativeTestFixtureDrivenServiceResponseProvider: DeclarativeTestFixtureDrivenServiceResponseProvider): MockServiceResponseFactory {
        return MockServiceResponseFactory(mockServiceResponseProviderList,
                                            configurableEnvironment,
                                            declarativeTestFixtureDrivenServiceResponseProvider)
    }
}