package com.udemy.libraries.acceptancetests.autoconfigure

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal
import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@AutoConfiguration
@ConditionalOnBean(AmazonDynamoDBLocal::class)
@EnableOnAcceptanceTestClass
class TestDynamoDBAutoConfiguration {
    @Bean
    fun amazonDynamoDB(embedded:AmazonDynamoDBLocal) : AmazonDynamoDB {
        return  embedded.amazonDynamoDB()
    }

    @Bean
    fun dynamoDbClient(embedded:AmazonDynamoDBLocal) : DynamoDbClient {
        return embedded.dynamoDbClient()
    }
}