package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupportForH2
import org.fit4j.dbcleanup.DatabaseTestSupportForPostgreSQL
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
import org.springframework.test.context.TestPropertySource

@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class DatabaseTestSupportForPostgreSQLCheckingTestSuite {
    
    @Nested
    @Order(1)
    @FIT
    @Testcontainers(definitions = ["postgreSQLContainerDefinition"])
    @TestPropertySource(properties = [
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.url=\${fit4j.postgreSQLContainerDefinition.jdbcUrl}",
        "spring.datasource.username=\${fit4j.postgreSQLContainerDefinition.username}",
        "spring.datasource.password=\${fit4j.postgreSQLContainerDefinition.password}"
    ])
    inner class FirstFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Test
        fun `populate db with various table types`() {
            // Table with PK:id as SERIAL (auto-increment)
            jdbcTemplate.execute("""
                CREATE TABLE my_foo (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo(name) VALUES ('Foo')")
            jdbcTemplate.update("INSERT INTO my_foo(name) VALUES ('Bar')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo WHERE name = 'Foo'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo WHERE name = 'Bar'"))

            // Table with PK:id as IDENTITY (PostgreSQL 10+)
            jdbcTemplate.execute("""
                CREATE TABLE my_foo2 (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    name VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo2(name) VALUES ('Foo')")
            jdbcTemplate.update("INSERT INTO my_foo2(name) VALUES ('Bar')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo2 WHERE name = 'Foo'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo2 WHERE name = 'Bar'"))

            // Table with PK:user_id as SERIAL (non-standard name)
            jdbcTemplate.execute("""
                CREATE TABLE my_users (
                    user_id BIGSERIAL PRIMARY KEY,
                    username VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_users(username) VALUES ('alice')")
            jdbcTemplate.update("INSERT INTO my_users(username) VALUES ('bob')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT user_id FROM my_users WHERE username = 'alice'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT user_id FROM my_users WHERE username = 'bob'"))

            // Table with sequence following naming convention (table_name_seq)
            jdbcTemplate.execute("""
                CREATE SEQUENCE my_foo3_seq START WITH 1 INCREMENT BY 1
            """.trimIndent())
            jdbcTemplate.execute("""
                CREATE TABLE my_foo3 (
                    id BIGINT DEFAULT nextval('my_foo3_seq') PRIMARY KEY,
                    name VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo3(name) VALUES ('Foo')")
            jdbcTemplate.update("INSERT INTO my_foo3(name) VALUES ('Bar')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo3 WHERE name = 'Foo'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo3 WHERE name = 'Bar'"))

            // Table with PK:id assigned by user (no auto-increment)
            jdbcTemplate.execute("""
                CREATE TABLE my_foo4 (
                    id BIGINT PRIMARY KEY, 
                    name VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo4 (id, name) VALUES (?, ?)", 100, "Foo")
            Assertions.assertEquals(100, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo4 WHERE name = 'Foo'"))

            // Table with PK:foo_id assigned by user (non-standard name, no auto-increment)
            jdbcTemplate.execute("""
                CREATE TABLE my_foo5 (
                    foo_id BIGINT PRIMARY KEY, 
                    name VARCHAR(255)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo5 (foo_id, name) VALUES (?, ?)", 200, "Foo")
            Assertions.assertEquals(200, jdbcTemplate.queryForObject<Long>("SELECT foo_id FROM my_foo5 WHERE name = 'Foo'"))

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
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT REFERENCES my_users(user_id),
                    order_number VARCHAR(50)
                )
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_orders(user_id, order_number) VALUES (1, 'ORD-001')")
            jdbcTemplate.update("INSERT INTO my_orders(user_id, order_number) VALUES (2, 'ORD-002')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_orders WHERE order_number = 'ORD-001'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_orders WHERE order_number = 'ORD-002'"))
        }
    }

    @Nested
    @Order(2)
    @FIT
    @Testcontainers(definitions = ["postgreSQLContainerDefinition"])
    @TestPropertySource(properties = [
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.url=\${fit4j.postgreSQLContainerDefinition.jdbcUrl}",
        "spring.datasource.username=\${fit4j.postgreSQLContainerDefinition.username}",
        "spring.datasource.password=\${fit4j.postgreSQLContainerDefinition.password}"
    ])
    inner class SecondFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Autowired
        private lateinit var databaseTestSupport: DatabaseTestSupport

        @Test
        fun `verify tables are cleared and sequences are reset`() {
            Assertions.assertTrue(databaseTestSupport is DatabaseTestSupportForPostgreSQL)
            // Verify my_foo table is empty and SERIAL sequence is reset
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo"))
            jdbcTemplate.update("INSERT INTO my_foo(name) VALUES ('Foo')")
            jdbcTemplate.update("INSERT INTO my_foo(name) VALUES ('Bar')")
            // Sequence should start from 1 again
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo WHERE name = 'Foo'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo WHERE name = 'Bar'"))

            // Verify my_foo2 table is empty and IDENTITY is reset
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo2"))
            jdbcTemplate.update("INSERT INTO my_foo2(name) VALUES ('Foo')")
            jdbcTemplate.update("INSERT INTO my_foo2(name) VALUES ('Bar')")
            // Identity should start from 1 again
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo2 WHERE name = 'Foo'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo2 WHERE name = 'Bar'"))

            // Verify my_users table is empty and SERIAL sequence is reset
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_users"))
            jdbcTemplate.update("INSERT INTO my_users(username) VALUES ('alice')")
            jdbcTemplate.update("INSERT INTO my_users(username) VALUES ('bob')")
            // Sequence should start from 1 again
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT user_id FROM my_users WHERE username = 'alice'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT user_id FROM my_users WHERE username = 'bob'"))

            // Verify my_foo3 table is empty and sequence is reset
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo3"))
            jdbcTemplate.update("INSERT INTO my_foo3(name) VALUES ('Foo')")
            jdbcTemplate.update("INSERT INTO my_foo3(name) VALUES ('Bar')")
            // Convention-based sequence should be reset
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo3 WHERE name = 'Foo'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo3 WHERE name = 'Bar'"))

            // Verify my_foo4 table is empty (user-managed PK)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo4"))
            jdbcTemplate.update("INSERT INTO my_foo4 (id, name) VALUES (?, ?)", 100, "Foo")
            Assertions.assertEquals(100, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_foo4 WHERE name = 'Foo'"))

            // Verify my_foo5 table is empty (user-managed PK with non-standard name)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo5"))
            jdbcTemplate.update("INSERT INTO my_foo5 (foo_id, name) VALUES (?, ?)", 200, "Foo")
            Assertions.assertEquals(200, jdbcTemplate.queryForObject<Long>("SELECT foo_id FROM my_foo5 WHERE name = 'Foo'"))

            // Verify my_foo_bar table is empty (composite PK)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar"))
            jdbcTemplate.update("INSERT INTO my_foo_bar(foo_id, bar_id) VALUES (1, 1)")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar WHERE foo_id = 1 AND bar_id = 1"))

            // Verify my_foo_bar2 table is empty (no PK, unique constraint)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar2"))
            jdbcTemplate.update("INSERT INTO my_foo_bar2(foo_id, bar_id) VALUES (1, 1)")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_foo_bar2 WHERE foo_id = 1 AND bar_id = 1"))

            // Verify my_orders table is empty and SERIAL sequence is reset (with FK)
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM my_orders"))
            jdbcTemplate.update("INSERT INTO my_orders(user_id, order_number) VALUES (1, 'ORD-001')")
            jdbcTemplate.update("INSERT INTO my_orders(user_id, order_number) VALUES (2, 'ORD-002')")
            // Sequence should start from 1 again
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_orders WHERE order_number = 'ORD-001'"))
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM my_orders WHERE order_number = 'ORD-002'"))
        }
    }
}

