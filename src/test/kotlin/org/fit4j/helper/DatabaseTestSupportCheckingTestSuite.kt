package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupportForH2
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

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
            //table with PK:id as identity
            jdbcTemplate.execute("""
                CREATE TABLE my_foo (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    name VARCHAR(255)
                );
            """.trimIndent())
            jdbcTemplate.update("insert into my_foo(name) values ('Foo')")
            jdbcTemplate.update("insert into my_foo(name) values ('Bar')")
            Assertions.assertEquals(1,jdbcTemplate.queryForObject<Int>("select id from my_foo where name = 'Foo'"))
            Assertions.assertEquals(2,jdbcTemplate.queryForObject<Int>("select id from my_foo where name = 'Bar'"))

            //table with PK:id as sequence
            jdbcTemplate.execute("""
                CREATE SEQUENCE my_foo2_seq START WITH 1 INCREMENT BY 1;
            """.trimIndent())
            jdbcTemplate.execute("""
                CREATE TABLE my_foo2 (
                    id BIGINT DEFAULT NEXT VALUE FOR my_foo2_seq PRIMARY KEY,
                    name VARCHAR(255)
                );
            """.trimIndent())
            jdbcTemplate.update("insert into my_foo2(name) values ('Foo')")
            jdbcTemplate.update("insert into my_foo2(name) values ('Bar')")
            Assertions.assertEquals(1,jdbcTemplate.queryForObject<Int>("select id from my_foo2 where name = 'Foo'"))
            Assertions.assertEquals(2,jdbcTemplate.queryForObject<Int>("select id from my_foo2 where name = 'Bar'"))

            //table with PK:id assigned by user
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS my_foo3 (id INT PRIMARY KEY, name VARCHAR(255));
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo3 (id, name) VALUES (?, ?);", 1, "Foo")

            //table with PK:id assigned by user
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS my_foo4 (foo_id INT PRIMARY KEY, name VARCHAR(255));
            """.trimIndent())
            jdbcTemplate.update("INSERT INTO my_foo4 (foo_id, name) VALUES (?, ?);", 1, "Foo")

            //table with PK:foo_id,bar_id as composite assigned by user
            jdbcTemplate.execute("""
                CREATE TABLE my_foo_bar (
                    foo_id BIGINT,
                    bar_id BIGINT,
                    PRIMARY KEY (foo_id, bar_id)
                );
            """.trimIndent())
            jdbcTemplate.update("insert into my_foo_bar(foo_id,bar_id) values (1,1)")
            Assertions.assertEquals(1,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo_bar where foo_id = 1 and bar_id = 1"))

            //table without PK, foo_id,bar_id together unique
            jdbcTemplate.execute("""
                CREATE TABLE my_foo_bar2 (
                    foo_id BIGINT,
                    bar_id BIGINT,
                    CONSTRAINT uq_my_foo_bar2 UNIQUE (foo_id, bar_id)
                );
            """.trimIndent())
            jdbcTemplate.update("insert into my_foo_bar2(foo_id,bar_id) values (1,1)")
            Assertions.assertEquals(1,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo_bar where foo_id = 1 and bar_id = 1"))
        }
    }

    @Nested
    @Order(2)
    @FIT
    inner class SecondFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Autowired
        private lateinit var databaseTestSupport: DatabaseTestSupport


        @Test
        fun `populate db`() {
            Assertions.assertTrue(databaseTestSupport is DatabaseTestSupportForH2)
            Assertions.assertEquals(0,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo"))
            jdbcTemplate.update("insert into my_foo(name) values ('Foo')")
            jdbcTemplate.update("insert into my_foo(name) values ('Bar')")
            Assertions.assertEquals(1,jdbcTemplate.queryForObject<Int>("select id from my_foo where name = 'Foo'"))
            Assertions.assertEquals(2,jdbcTemplate.queryForObject<Int>("select id from my_foo where name = 'Bar'"))

            Assertions.assertEquals(0,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo2"))
            jdbcTemplate.update("insert into my_foo2(name) values ('Foo')")
            jdbcTemplate.update("insert into my_foo2(name) values ('Bar')")
            Assertions.assertEquals(1,jdbcTemplate.queryForObject<Int>("select id from my_foo2 where name = 'Foo'"))
            Assertions.assertEquals(2,jdbcTemplate.queryForObject<Int>("select id from my_foo2 where name = 'Bar'"))

            Assertions.assertEquals(0,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo3"))

            Assertions.assertEquals(0,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo4"))

            Assertions.assertEquals(0,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo_bar"))
            jdbcTemplate.update("insert into my_foo_bar(foo_id,bar_id) values (1,1)")
            Assertions.assertEquals(1,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo_bar where foo_id = 1 and bar_id = 1"))

            Assertions.assertEquals(0,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo_bar2"))
            jdbcTemplate.update("insert into my_foo_bar2(foo_id,bar_id) values (1,1)")
            Assertions.assertEquals(1,jdbcTemplate.queryForObject<Int>("select count(*) from my_foo_bar2 where foo_id = 1 and bar_id = 1"))
       }
    }
}