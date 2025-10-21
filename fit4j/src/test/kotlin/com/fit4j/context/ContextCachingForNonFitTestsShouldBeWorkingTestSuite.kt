package com.fit4j.context

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class ContextCachingForNonFitTestsShouldBeWorkingTestSuite {
    @Nested
    @Order(1)
    @SpringBootTest
    @ActiveProfiles("test")
    inner class FirstFIT {
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
    inner class SecondFIT {

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


