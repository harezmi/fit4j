package com.fit4j.context

import com.fit4j.annotation.FIT
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class ContextCachingForFitTestsShouldBeWorkingTestSuite {
    @Nested
    @Order(1)
    @FIT
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
    @FIT
    inner class SecondFIT {

        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @Test
        fun `check if application contexts are the same`() {
            Assertions.assertNotNull(applicationContextStatic)
            Assertions.assertSame(applicationContextStatic, applicationContext)
        }
    }

    @Nested
    @Order(3)
    @FIT
    @TestPropertySource(properties = ["foo=bar"])
    inner class ThirdFIT {

        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @AfterEach
        fun `reset static context`() {
            applicationContextStatic = null
        }

        @Test
        fun `check if application contexts are the same`() {
            Assertions.assertNotNull(applicationContextStatic)
            Assertions.assertNotSame(applicationContextStatic, applicationContext)
        }
    }

    companion object {
        var applicationContextStatic: ApplicationContext? = null
    }
}


