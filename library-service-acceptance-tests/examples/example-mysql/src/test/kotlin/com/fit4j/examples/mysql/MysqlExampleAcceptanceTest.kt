package com.fit4j.examples.mysql

import com.fit4j.AcceptanceTest
import com.fit4j.helpers.dbcleanup.DatabaseTestSupportForH2
import com.fit4j.helpers.dbcleanup.DatabaseTestSupportForMysql
import com.fit4j.testcontainers.Testcontainers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate

@Testcontainers(definitions = ["mySQLContainerDefinition"])
@AcceptanceTest
class MysqlExampleAcceptanceTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @TestConfiguration
    class TestConfig {
        @Bean
        fun databaseTestSupport(): DatabaseTestSupportForMysql {
            return DatabaseTestSupportForMysql()
        }
    }

    @Test
    fun `it should work`() {
      val result = jdbcTemplate.query("select id,name from foo") { rs, rn ->
          val id = rs.getInt("id")
          val name = rs.getString("name")
          Foo(id, name)
      }
        MatcherAssert.assertThat(result, Matchers.containsInAnyOrder(
            Foo(1, "foo-1"),
            Foo(2, "foo-2"),
            Foo(3, "foo-3")
        ))
    }
}

data class Foo(val id: Int, val name: String)

