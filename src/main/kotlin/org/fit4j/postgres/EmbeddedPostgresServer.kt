package org.fit4j.postgres

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.jdbc.datasource.init.ScriptUtils
import java.net.InetSocketAddress
import java.net.Socket

class EmbeddedPostgresServer(private val port: Int, private val initScript: String) : InitializingBean, DisposableBean {
    val builder: EmbeddedPostgres.Builder = EmbeddedPostgres.builder().setPort(port)
    private var db : EmbeddedPostgres? = null

    override fun afterPropertiesSet() {
        try {
            checkPortAvailable()
            db = builder.start()

            val rs = ClassPathResource(initScript)
            if(rs.exists()) {
                val dataSource = db!!.postgresDatabase
                val populator = ResourceDatabasePopulator()
                populator.setSeparator(ScriptUtils.EOF_STATEMENT_SEPARATOR)
                populator.addScript(rs)
                populator.execute(dataSource)
            }
        } catch (exception: Exception) {
            db?.close()
            db = null
            throw IllegalStateException(
                "Failed to start embedded PostgreSQL on port $port or initialize it from '$initScript'; " +
                    "verify that the port is available and the script is valid",
                exception,
            )
        }
    }

    private fun checkPortAvailable() {
        if (port <= 0) {
            return
        }
        Socket().use { socket ->
            try {
                socket.connect(InetSocketAddress("localhost", port), 200)
            } catch (_: Exception) {
                return
            }
        }
        throw IllegalStateException("Port $port is already accepting connections")
    }

    override fun destroy() {
        db?.close()
        db = null
    }
}
