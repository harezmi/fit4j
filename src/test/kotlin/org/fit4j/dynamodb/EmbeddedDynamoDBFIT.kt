package org.fit4j.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@FIT
@EmbeddedDynamoDB
class EmbeddedDynamoDBFIT {
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