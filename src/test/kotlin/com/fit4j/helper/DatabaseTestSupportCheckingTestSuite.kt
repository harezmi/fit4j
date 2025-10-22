package com.fit4j.helper

import com.fit4j.annotation.FIT
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class DatabaseTestSupportCheckingTestSuite {
    @Nested
    @Order(1)
    @FIT
    inner class FirstFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Test
        fun `populate db`() {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(255));")
            jdbcTemplate.update("INSERT INTO test_table (id, name) VALUES (?, ?);", 1, "Test Name")
        }
    }

    @Nested
    @Order(2)
    @FIT
    inner class SecondFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Test
        fun `populate db`() {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_table;") { rs, _ -> rs.getInt(1) }.let {
                Assertions.assertEquals(0, it, "Database should be cleaned between tests")
            }
       }
    }
}