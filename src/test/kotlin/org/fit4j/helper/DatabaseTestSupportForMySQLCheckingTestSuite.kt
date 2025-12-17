package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.testcontainers.Testcontainers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class DatabaseTestSupportForMySQLCheckingTestSuite {
    
    @Nested
    @Order(1)
    @FIT
    @Testcontainers(definitions = ["mySQLContainerDefinition"])
    inner class FirstFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Test
        fun `populate db with various table types`() {
            // Table with PK:id as AUTO_INCREMENT
            jdbcTemplate.execute("""
                CREATE TABLE my_foo (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo(name) VALUES ('Foo')")
            jdbcTemplate.update("INSERT INTO my_foo(name) VALUES ('Bar')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_foo WHERE name = 'Foo'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_foo WHERE name = 'Bar'"))

            // Table with PK:user_id as AUTO_INCREMENT (non-standard name)
            jdbcTemplate.execute("""
                CREATE TABLE my_users (
                    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_users(username) VALUES ('alice')")
            jdbcTemplate.update("INSERT INTO my_users(username) VALUES ('bob')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT user_id FROM my_users WHERE username = 'alice'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Int>("SELECT user_id FROM my_users WHERE username = 'bob'"))

            // Table with PK:id assigned by user (no AUTO_INCREMENT)
            jdbcTemplate.execute("""
                CREATE TABLE my_foo3 (
                    id INT PRIMARY KEY, 
                    name VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo3 (id, name) VALUES (?, ?)", 100, "Foo")
            Assertions.assertEquals(100, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_foo3 WHERE name = 'Foo'"))

            // Table with PK:foo_id assigned by user (non-standard name, no AUTO_INCREMENT)
            jdbcTemplate.execute("""
                CREATE TABLE my_foo4 (
                    foo_id INT PRIMARY KEY, 
                    name VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo4 (foo_id, name) VALUES (?, ?)", 200, "Foo")
            Assertions.assertEquals(200, jdbcTemplate.queryForObject<Int>("SELECT foo_id FROM my_foo4 WHERE name = 'Foo'"))

            // Table with composite PK (foo_id, bar_id) assigned by user
            jdbcTemplate.execute("""
                CREATE TABLE my_foo_bar (
                    foo_id BIGINT,
                    bar_id BIGINT,
                    PRIMARY KEY (foo_id, bar_id)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo_bar(foo_id, bar_id) VALUES (1, 1)")
            jdbcTemplate.update("INSERT INTO my_foo_bar(foo_id, bar_id) VALUES (1, 2)")
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar WHERE foo_id = 1"))

            // Table without PK, but with unique constraint
            jdbcTemplate.execute("""
                CREATE TABLE my_foo_bar2 (
                    foo_id BIGINT,
                    bar_id BIGINT,
                    CONSTRAINT uq_my_foo_bar2 UNIQUE (foo_id, bar_id)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo_bar2(foo_id, bar_id) VALUES (1, 1)")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar2 WHERE foo_id = 1 AND bar_id = 1"))

            // Table with foreign key relationship
            jdbcTemplate.execute("""
                CREATE TABLE my_orders (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT,
                    order_number VARCHAR(50),
                    FOREIGN KEY (user_id) REFERENCES my_users(user_id)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_orders(user_id, order_number) VALUES (1, 'ORD-001')")
            jdbcTemplate.update("INSERT INTO my_orders(user_id, order_number) VALUES (2, 'ORD-002')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_orders WHERE order_number = 'ORD-001'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_orders WHERE order_number = 'ORD-002'"))
        }
    }

    @Nested
    @Order(2)
    @FIT
    @Testcontainers(definitions = ["mySQLContainerDefinition"])
    inner class SecondFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Autowired
        private lateinit var databaseTestSupport: DatabaseTestSupport

        @Test
        fun `verify tables are cleared and auto-increment is reset`() {
            // Verify my_foo table is empty and AUTO_INCREMENT is reset
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo"))
            jdbcTemplate.update("INSERT INTO my_foo(name) VALUES ('Foo')")
            jdbcTemplate.update("INSERT INTO my_foo(name) VALUES ('Bar')")
            // Auto-increment should start from 1 again
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_foo WHERE name = 'Foo'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_foo WHERE name = 'Bar'"))

            // Verify my_users table is empty and AUTO_INCREMENT is reset
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_users"))
            jdbcTemplate.update("INSERT INTO my_users(username) VALUES ('alice')")
            jdbcTemplate.update("INSERT INTO my_users(username) VALUES ('bob')")
            // Auto-increment should start from 1 again
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT user_id FROM my_users WHERE username = 'alice'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Int>("SELECT user_id FROM my_users WHERE username = 'bob'"))

            // Verify my_foo3 table is empty (user-managed PK)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo3"))
            jdbcTemplate.update("INSERT INTO my_foo3 (id, name) VALUES (?, ?)", 100, "Foo")
            Assertions.assertEquals(100, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_foo3 WHERE name = 'Foo'"))

            // Verify my_foo4 table is empty (user-managed PK with non-standard name)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo4"))
            jdbcTemplate.update("INSERT INTO my_foo4 (foo_id, name) VALUES (?, ?)", 200, "Foo")
            Assertions.assertEquals(200, jdbcTemplate.queryForObject<Int>("SELECT foo_id FROM my_foo4 WHERE name = 'Foo'"))

            // Verify my_foo_bar table is empty (composite PK)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar"))
            jdbcTemplate.update("INSERT INTO my_foo_bar(foo_id, bar_id) VALUES (1, 1)")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar WHERE foo_id = 1 AND bar_id = 1"))

            // Verify my_foo_bar2 table is empty (no PK, unique constraint)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar2"))
            jdbcTemplate.update("INSERT INTO my_foo_bar2(foo_id, bar_id) VALUES (1, 1)")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar2 WHERE foo_id = 1 AND bar_id = 1"))

            // Verify my_orders table is empty and AUTO_INCREMENT is reset (with FK)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_orders"))
            jdbcTemplate.update("INSERT INTO my_orders(user_id, order_number) VALUES (1, 'ORD-001')")
            jdbcTemplate.update("INSERT INTO my_orders(user_id, order_number) VALUES (2, 'ORD-002')")
            // Auto-increment should start from 1 again
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_orders WHERE order_number = 'ORD-001'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Int>("SELECT id FROM my_orders WHERE order_number = 'ORD-002'"))
        }
    }
}

