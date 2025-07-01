package com.udemy.libraries.acceptancetests.context

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class ContextCachingForNonAcceptanceTestsShouldBeWorkingSuite {
    @Nested
    @Order(1)
    @SpringBootTest
    @ActiveProfiles("test")
    inner class FirstTest {
        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @Test
        fun `initialize static application context`() {
            Assertions.assertNull(applicationContextStatic)
            applicationContextStatic = applicationContext
        }
    }


    @Nested
    @Order(2)
    @SpringBootTest
    @ActiveProfiles("test")
    inner class SecondTest {

        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @AfterEach
        fun `reset static context`() {
            applicationContextStatic = null
        }

        @Test
        fun `check if application contexts are the same`() {
            Assertions.assertNotNull(applicationContextStatic)
            Assertions.assertSame(applicationContextStatic, applicationContext)
        }
    }

    companion object {
        var applicationContextStatic: ApplicationContext? = null
    }
}


