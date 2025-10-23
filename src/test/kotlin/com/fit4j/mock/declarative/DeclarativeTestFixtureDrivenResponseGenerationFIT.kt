package com.fit4j.mock.declarative


import com.example.fit4j.grpc.FooGrpcServiceGrpc
import com.example.fit4j.grpc.TestGrpc
import com.fit4j.annotation.FIT
import com.fit4j.annotation.FixtureForFIT
import com.fit4j.helper.FitHelper
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FIT("classpath:declarative-response-generation-fixture.yml")
@TestPropertySource(properties = [
    "fit4j.declerativeTestFixtureDrivenResponseGeneration.enabled=true",
    "grpc.client.testGrpcService.address=in-process:\${grpc.server.inProcessName}"
    ])
class DeclarativeTestFixtureDrivenResponseGenerationFIT {

    @GrpcClient("testGrpcService")
    private lateinit var fooGrpcService: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub

    @Autowired
    private lateinit var helper: FitHelper

    @Test
    @FixtureForFIT("it should return responses from test fixture yml")
    fun `it should return responses from test fixture yml`() {

        val getRateRequest1 = TestGrpc.GetAgeRequest.getDefaultInstance()
        val getRateResponse1 = fooGrpcService.getAgeRequest(getRateRequest1)
        Assertions.assertEquals(10,getRateResponse1.age)

        val getRateRequest2 = TestGrpc.GetAgeRequest.getDefaultInstance()
        val getRateResponse2 = fooGrpcService.getAgeRequest(getRateRequest2)
        Assertions.assertEquals(20,getRateResponse2.age)


        val request = TestGrpc.GetFooByIdRequest.newBuilder().setId(123).build()
        val response = fooGrpcService.getFooByIdResponse(request)
        Assertions.assertEquals(123L,response.foo.id)

        val httpResponse1= helper.beans.restTemplate.getForEntity("${helper.mockWebServerBaseUrl()}/test-1",Void::class.java)

        Assertions.assertEquals(200,httpResponse1.statusCodeValue)
    }
}