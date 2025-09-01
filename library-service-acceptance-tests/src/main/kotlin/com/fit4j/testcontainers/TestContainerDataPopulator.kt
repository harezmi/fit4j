package com.fit4j.testcontainers

import org.springframework.core.io.Resource

interface TestContainerDataPopulator {
    fun populateData(resource: Resource)
}