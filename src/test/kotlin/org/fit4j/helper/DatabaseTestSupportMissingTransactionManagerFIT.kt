package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

@FIT
@TestPropertySource(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration,org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
    ],
)
class DatabaseTestSupportMissingTransactionManagerFIT {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `database test support should not be created when transaction manager is absent`() {
        Assertions.assertTrue(applicationContext.containsBean("dataSource"))
        Assertions.assertFalse(applicationContext.containsBean("transactionManager"))
        Assertions.assertFalse(applicationContext.containsBean("databaseTestSupport"))
        Assertions.assertTrue(applicationContext.getBeansOfType(DatabaseTestSupport::class.java).isEmpty())
    }
}
