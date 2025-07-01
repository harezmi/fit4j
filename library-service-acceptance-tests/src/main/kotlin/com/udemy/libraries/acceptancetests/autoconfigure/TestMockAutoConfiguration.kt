package com.udemy.libraries.acceptancetests.autoconfigure

import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.context.ApplicationContextLifecycleListener
import com.udemy.libraries.acceptancetests.mock.CallTraceFactory
import com.udemy.libraries.acceptancetests.mock.MockServiceCallTracker
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseFactory
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseProvider
import com.udemy.libraries.acceptancetests.mock.declarative.DeclarativeTestFixtureDrivenServiceResponseProvider
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