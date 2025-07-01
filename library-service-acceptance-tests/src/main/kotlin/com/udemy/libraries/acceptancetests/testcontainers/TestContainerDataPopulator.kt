package com.udemy.libraries.acceptancetests.testcontainers

import org.springframework.core.io.Resource

interface TestContainerDataPopulator {
    fun populateData(resource: Resource)
}