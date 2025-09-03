package com.fit4j.examples.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.Projection
import com.amazonaws.services.dynamodbv2.model.ProjectionType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.SSESpecification
import com.amazonaws.services.dynamodbv2.model.SSEType
import com.amazonaws.services.dynamodbv2.model.StreamSpecification
import com.amazonaws.services.dynamodbv2.model.StreamViewType
import com.amazonaws.services.dynamodbv2.model.Tag
import com.fit4j.AcceptanceTest
import com.fit4j.dynamodb.EmbeddedDynamoDB
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@EmbeddedDynamoDB
@AcceptanceTest
class DynamodbExampleAcceptanceTest {

    @Autowired
    private lateinit var amazonDynamoDB: AmazonDynamoDB

    @Autowired
    private lateinit var dynamoDBMapper: DynamoDBMapper

    @TestConfiguration
    class TestConfig {
        @Bean
        fun dynamoDBMapper(amazonDynamoDB: AmazonDynamoDB): DynamoDBMapper {
            return DynamoDBMapper(amazonDynamoDB)
        }
    }

    @BeforeEach
    fun setUp() {
        val createTableRequest = CreateTableRequest()
        createTableRequest
            .withTableName("PlayerTable")
            .withTableClass("STANDARD")
            .withTags(Tag().withKey("service").withValue("my-service-dynamo"))
            .withAttributeDefinitions(
                AttributeDefinition("PK", "S"),
                AttributeDefinition("SK", "S")
            )
            .withKeySchema(
                KeySchemaElement("PK", "HASH"),
                KeySchemaElement("SK", "RANGE")
            )
            .withGlobalSecondaryIndexes(
                GlobalSecondaryIndex()
                    .withIndexName("Index_SK")
                    .withKeySchema(
                        KeySchemaElement("SK", "HASH"),
                        KeySchemaElement("PK", "RANGE")
                    )
                    .withProjection(Projection().withProjectionType(ProjectionType.ALL))
                    .withProvisionedThroughput(ProvisionedThroughput(1L, 1L))
            )
            .withBillingMode("PROVISIONED")
            .withProvisionedThroughput(ProvisionedThroughput(1L, 1L))
            .withStreamSpecification(
                StreamSpecification()
                    .withStreamEnabled(true)
                    .withStreamViewType(StreamViewType.NEW_AND_OLD_IMAGES)
            )
            .withSSESpecification(
                SSESpecification()
                    .withEnabled(true)
                    .withSSEType(SSEType.KMS)
                    .withKMSMasterKeyId("")
            )
        amazonDynamoDB.createTable(createTableRequest)
    }

    @AfterEach
    fun tearDown() {
        amazonDynamoDB.deleteTable("PlayerTable")
    }

    @Test
    fun `it should work`() {
        val playerItem1 = _root_ide_package_.com.fit4j.examples.dynamodb.PlayerItem(1L, "John Doe")
        val playerItem2 = _root_ide_package_.com.fit4j.examples.dynamodb.PlayerItem(2L, "Joe Doe")
        dynamoDBMapper.save(playerItem1)
        dynamoDBMapper.save(playerItem2)

        val playerItem1FromDB = dynamoDBMapper.load(
            PlayerItem::class.java,
            "PLAYER#1", "PLAYER#1"
        )

        val playerItem2FromDB = dynamoDBMapper.load(
            PlayerItem::class.java,
            "PLAYER#2", "PLAYER#2"
        )

        val playerItem3FromDB = dynamoDBMapper.load(
            PlayerItem::class.java,
            "PLAYER#3", "PLAYER#3"
        )

        Assertions.assertEquals(playerItem1,playerItem1FromDB)
        Assertions.assertEquals(playerItem2,playerItem2FromDB)
        Assertions.assertNull(playerItem3FromDB)
    }
}

