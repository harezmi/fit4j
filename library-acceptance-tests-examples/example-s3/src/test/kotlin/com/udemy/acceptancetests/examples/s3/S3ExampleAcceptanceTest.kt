package com.udemy.acceptancetests.examples.s3

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.testcontainers.TestContainerDefinition
import com.udemy.libraries.acceptancetests.testcontainers.Testcontainers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.BufferedInputStream

@AcceptanceTest
@Testcontainers(definitions = ["localstackContainerDefinition"])
class S3ExampleAcceptanceTest {
    @Autowired
    private lateinit var s3Client: S3Client

    @TestConfiguration
    class TestConfig {

        @Autowired
        private lateinit var localstackContainerDefinition: TestContainerDefinition

        @Bean
        fun s3Client() : S3Client {
            val localStack = localstackContainerDefinition.getContainer() as LocalStackContainer
            val s3Client = S3Client.builder()
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
                .region(Region.of(localStack.getRegion()))
                .build();
            return s3Client
        }
    }

    @Test
    fun `it should work`() {
        s3Client.createBucket(CreateBucketRequest.builder().bucket("test-bucket").build())
        s3Client.putObject(PutObjectRequest.builder().bucket("test-bucket").key("test-key").build(),
            RequestBody.fromString("hello world!"))
        val inStream = s3Client.getObject(GetObjectRequest.builder()
            .bucket("test-bucket").key("test-key").build())
        val actualMessage = String(BufferedInputStream(inStream).readAllBytes())
        Assertions.assertEquals("hello world!", actualMessage)
    }
}

