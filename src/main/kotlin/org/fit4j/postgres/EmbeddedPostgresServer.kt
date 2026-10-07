package org.fit4j.postgres

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.jdbc.datasource.init.ScriptUtils

class EmbeddedPostgresServer(port: Int, private val initScript: String) : InitializingBean, DisposableBean {
    val builder: EmbeddedPostgres.Builder = EmbeddedPostgres.builder().setPort(port)
    private var db : EmbeddedPostgres? = null

    override fun afterPropertiesSet() {
        db = builder.start()

        val rs = ClassPathResource(initScript)
        if(rs.exists()) {
            val dataSource = db!!.postgresDatabase
            val populator = ResourceDatabasePopulator()
            populator.setSeparator(ScriptUtils.EOF_STATEMENT_SEPARATOR)
            populator.addScript(rs)
            populator.execute(dataSource)
        }
    }

    override fun destroy() {
        db!!.close()
    }
}