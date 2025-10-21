package com.fit4j.scope

import com.fit4j.annotation.FIT
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class TestScopeCheckingTestSuite {
    @Nested
    @Order(1)
    @FIT
    @Import(TestConfig::class)
    inner class FirstFIT {
        @Autowired
        private lateinit var testBean: TestBean

        @Test
        fun `put message into test bean`() {
            testBean.addMessage("First message")
        }
    }

    @Nested
    @Order(2)
    @FIT
    @Import(TestConfig::class)
    inner class SecondFIT {

        @Autowired
        private lateinit var testBean: TestBean

        @Test
        fun `check if test beans are not same`() {
            Assertions.assertTrue(testBean.getMessages().isEmpty(),"Test bean should be empty")
        }
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        @TestScoped
        fun testBean(): TestBean {
            return TestBean()
        }
    }

    open class TestBean {
        private val messages = mutableListOf<String>()

        open fun addMessage(message: String) {
            messages.add(message)
        }

        open fun getMessages(): List<String> {
            return messages
        }
    }
}