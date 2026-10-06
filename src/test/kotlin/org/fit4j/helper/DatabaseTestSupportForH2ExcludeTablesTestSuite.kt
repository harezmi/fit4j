package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupportForH2
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
class DatabaseTestSupportForH2ExcludeTablesTestSuite {

    @Nested
    @Order(1)
    @FIT
    @TestPropertySource(properties = [
        "fit4j.dbcleanup.exclude-tables=seed_country",
    ])
    inner class FirstFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Test
        fun `populate seed and child tables`() {
            jdbcTemplate.execute(
                """
                CREATE TABLE seed_country (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    name VARCHAR(255)
                )
                """.trimIndent()
            )
            jdbcTemplate.update("INSERT INTO seed_country(name) VALUES ('TR')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM seed_country WHERE name = 'TR'"))

            jdbcTemplate.execute(
                """
                CREATE TABLE city (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    country_id BIGINT,
                    name VARCHAR(255),
                    CONSTRAINT fk_city_country FOREIGN KEY (country_id) REFERENCES seed_country(id)
                )
                """.trimIndent()
            )
            jdbcTemplate.update("INSERT INTO city(country_id, name) VALUES (1, 'Ankara')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM city WHERE name = 'Ankara'"))
        }
    }

    @Nested
    @Order(2)
    @FIT
    @TestPropertySource(properties = [
        "fit4j.dbcleanup.exclude-tables=seed_country",
    ])
    inner class SecondFIT {

        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @Autowired
        private lateinit var databaseTestSupport: DatabaseTestSupport

        @Test
        fun `excluded table keeps rows and identity while child is cleared`() {
            Assertions.assertTrue(databaseTestSupport is DatabaseTestSupportForH2)

            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM seed_country"))
            Assertions.assertEquals("TR", jdbcTemplate.queryForObject<String>("SELECT name FROM seed_country WHERE id = 1"))
            Assertions.assertEquals(0, jdbcTemplate.queryForObject<Int>("SELECT COUNT(*) FROM city"))

            jdbcTemplate.update("INSERT INTO city(country_id, name) VALUES (1, 'Istanbul')")
            Assertions.assertEquals(1, jdbcTemplate.queryForObject<Long>("SELECT id FROM city WHERE name = 'Istanbul'"))

            jdbcTemplate.update("INSERT INTO seed_country(name) VALUES ('DE')")
            Assertions.assertEquals(2, jdbcTemplate.queryForObject<Long>("SELECT id FROM seed_country WHERE name = 'DE'"))
        }
    }
}
