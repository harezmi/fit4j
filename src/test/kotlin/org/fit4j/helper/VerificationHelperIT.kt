package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

@FIT
class VerificationHelperIT {
    @Autowired
    private lateinit var verificationHelper: VerificationHelper

    @Test
    fun `verifyObject should handle object with nested objects `() {
        val parent = Parent("parent-1", Child("child-1", BigDecimal("15.12")))
        verificationHelper.verifyObject("""
            {
                "name":"parent-1",
                "child": {
                    "name":"child-1",
                    "amount":15.12
                }
            }
        """.trimIndent(),parent)
    }
}

data class Child(val name:String, val amount: BigDecimal)

data class Parent(val name:String,val child: Child)