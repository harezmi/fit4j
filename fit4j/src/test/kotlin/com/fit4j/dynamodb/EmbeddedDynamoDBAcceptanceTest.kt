package com.fit4j.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.fit4j.AcceptanceTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@AcceptanceTest
@EmbeddedDynamoDB
class EmbeddedDynamoDBAcceptanceTest {
    @Autowired
    private lateinit var amazonDynamoDB: AmazonDynamoDB

    @Autowired
    private lateinit var dynamoDbClient: DynamoDbClient

    @Test
    fun `embedded dynamodb should work`() {
        Assertions.assertNotNull(amazonDynamoDB)
        Assertions.assertNotNull(dynamoDbClient)
    }
}