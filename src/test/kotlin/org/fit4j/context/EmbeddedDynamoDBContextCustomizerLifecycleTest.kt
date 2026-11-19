package org.fit4j.context

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.context.support.GenericApplicationContext
import org.springframework.test.context.MergedContextConfiguration

class EmbeddedDynamoDBContextCustomizerLifecycleTest {

    @Test
    fun `application contexts use isolated databases and shut them down on close`() {
        val firstContext = createContext()
        val secondContext = createContext()
        val firstEmbedded = firstContext.getBean(AmazonDynamoDBLocal::class.java)
        val secondEmbedded = secondContext.getBean(AmazonDynamoDBLocal::class.java)
        val firstClient = firstEmbedded.amazonDynamoDB()
        val secondClient = secondEmbedded.amazonDynamoDB()

        try {
            assertNotSame(firstEmbedded, secondEmbedded)
            firstClient.createTable(createTableRequest("context_isolation_probe"))

            assertTrue(firstClient.listTables().tableNames.contains("context_isolation_probe"))
            assertFalse(secondClient.listTables().tableNames.contains("context_isolation_probe"))
        } finally {
            firstContext.close()
            secondContext.close()
        }

        assertClientIsShutdown(firstClient)
        assertClientIsShutdown(secondClient)
    }

    private fun createContext(): GenericApplicationContext = GenericApplicationContext().also { context ->
        EmbeddedDynamoDBContextCustomizer().customizeContext(
            context,
            mockk<MergedContextConfiguration>(relaxed = true),
        )
        context.refresh()
    }

    private fun createTableRequest(tableName: String) = CreateTableRequest()
        .withTableName(tableName)
        .withKeySchema(KeySchemaElement("id", KeyType.HASH))
        .withAttributeDefinitions(AttributeDefinition("id", ScalarAttributeType.S))
        .withProvisionedThroughput(ProvisionedThroughput(1, 1))

    private fun assertClientIsShutdown(client: AmazonDynamoDB) {
        assertThrows(AmazonServiceException::class.java) {
            client.listTables()
        }
    }
}
