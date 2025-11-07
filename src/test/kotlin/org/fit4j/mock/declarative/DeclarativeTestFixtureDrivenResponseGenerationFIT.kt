package org.fit4j.mock.declarative


import com.example.fit4j.grpc.FooGrpcServiceGrpc
import com.example.fit4j.grpc.TestGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.fit4j.annotation.FIT
import org.fit4j.annotation.FixtureForFIT
import org.fit4j.http.MockWebServerProperties
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FIT("classpath:declarative-response-generation-fixture.yml")
@TestPropertySource(properties = [
    "fit4j.declarativeTestFixtureDrivenResponseGeneration.enabled=true",
    "grpc.client.testGrpcService.address=in-process:\${grpc.server.inProcessName}"
    ])
class DeclarativeTestFixtureDrivenResponseGenerationFIT {

    @GrpcClient("testGrpcService")
    private lateinit var fooGrpcService: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var mockWebServerProperties: MockWebServerProperties

    @Test
    @FixtureForFIT("it should return responses from test fixture yml")
    fun `it should return responses from test fixture yml`() {

        val getAgeRequest1 = TestGrpc.GetAgeRequest.getDefaultInstance()
        val getAgeResponse1 = fooGrpcService.getAgeRequest(getAgeRequest1)
        Assertions.assertEquals(10,getAgeResponse1.age)

        val getAgeRequest2 = TestGrpc.GetAgeRequest.getDefaultInstance()
        val getAgeResponse2 = fooGrpcService.getAgeRequest(getAgeRequest2)
        Assertions.assertEquals(20,getAgeResponse2.age)


        val request = TestGrpc.GetFooByIdRequest.newBuilder().setId(123).build()
        val response = fooGrpcService.getFooByIdResponse(request)
        Assertions.assertEquals(123L,response.foo.id)

        val httpResponse1= restTemplate.getForEntity("${mockWebServerProperties.baseUrl()}/test-1",Void::class.java)

        Assertions.assertEquals(200,httpResponse1.statusCodeValue)
    }
}