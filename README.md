<div style="text-align: center;"><h1>Functional Integration Testing Library for Java</h1></div>

# Table of Contents

- [Table of Contents](#table-of-contents)
- [What is This Library About?](#what-is-this-library-about)
- [Why Should You Use This Library in Your Service?](#why-should-you-use-this-library-in-your-service)
- [How to Start Working with This Library?](#how-to-start-working-with-this-library)
    * [Add the Library Dependency](#add-the-library-dependency)
    * [Create a Test Class](#create-a-test-class)
    * [Add the Necessary Configuration for Your Test Class](#add-the-necessary-configuration-for-your-test-class)
- [What is @IT Annotation? What is It Used For?](#what-is-integrationtest-annotation-what-is-it-used-for)
- [How to Define Request-Response Trainings for External Services?](#how-to-define-request-response-trainings-for-external-services)
    * [Define Request-Response Trainings for External gRPC Endpoints](#define-request-response-trainings-for-external-grpc-endpoints)
        + [How the gRPC Communication is Redirected to the Mock gRPC Server?](#how-the-grpc-communication-is-redirected-to-the-mock-grpc-server)
    * [Define Request-Response Trainings for External HTTP/REST Endpoints](#define-request-response-trainings-for-external-httprest-endpoints)
- [How to Initiate Request Processing Flow in Your Service?](#how-to-initiate-request-processing-flow-in-your-service)
    * [Calling gRPC Endpoints of Your Service](#calling-grpc-endpoints-of-your-service)
    * [Calling REST Endpoints of Your Service](#calling-rest-endpoints-of-your-service)
    * [Publishing Kafka Messages to be Consumed by Your Service](#publishing-kafka-messages-to-be-consumed-by-your-service)
    * [Verify the Kafka Messages Published by Your Service](#verify-the-kafka-messages-published-by-your-service)
        + [Define Kafka Consumer Definitions to Consume Kafka Messages for Verification in Your Tests](#define-kafka-consumer-definitions-to-consume-kafka-messages-for-verification-in-your-tests)
        + [Waiting For Kafka Messages to be Published or Consumed](#waiting-for-kafka-messages-to-be-published-or-consumed)
        + [Verify Published or Consumed Kafka Messages](#verify-published-or-consumed-kafka-messages)
- [How to Work with TestContainers?](#how-to-work-with-testcontainers)
    * [Selectively Registering Your TestContainers](#selectively-registering-your-testcontainers)
    * [Initial Data Population for TestContainers](#initial-data-population-for-testcontainers)
        + [Initial Data Population for MySQL Container](#initial-data-population-for-mysql-container)
        + [Initial Data Population for ElasticSearch Container](#initial-data-population-for-elasticsearch-container)
        + [Initial Data Population for Redis Container](#initial-data-population-for-redis-container)
- [How to Work with Embedded DynamoDB?](#how-to-work-with-embedded-dynamodb)
- [How to Work with Embedded Redis?](#how-to-work-with-embedded-redis)
- [How to Work with Other Misc Stuff?](#how-to-work-with-other-misc-stuff)
    * [Using Protobuf JsonFormat.Parser and JsonFormat.Printer Beans For State Verification](#using-protobuf-jsonformatparser-and-jsonformatprinter-beans-for-state-verification)
- [How to Get More Help & Support?](#how-to-get-more-help--support)
- [FAQ](#faq)

# What is This Library About?

The Functional Integration Testing Library (fit4j) helps you write functional integration tests for your Java/Kotlin 
microservices. The scope of those tests usually aim to test the complete request-response flow of your microservice for 
a given endpoint. Therefore, those functional integration (aka acceptance) tests written to verify complete request-response 
flows could be regarded as the acceptance criteria of the service from the perspective of its clients.

The difference between functional integration tests and other types of integration tests is that functional integration
tests contain almost no mock objects within the system boundary itself. The only mocked parts are the external boundaries,
in this case, gRPC or REST endpoints of other internal microservices, or any other third-party external systems.
It is sufficient to mock only these external boundaries so that they would respond according to the current request flow being tested.

# Why Should You Use This Library in Your Service?

This library greatly simplifies the mock setup and request-response training for these external boundaries, aside from providing
you with the other necessary infrastructure, such as running your service against a real database (H2, MySQL, DynamoDB),
Kafka broker, Redis, and ElasticSearch, to write and run your functional integration tests.

While writing functional integration tests, the possible entry points for your service could be gRPC, REST, or Kafka controller layers.
With the help of this library, you can write tests to simulate handling requests from these entry points and verify the
eventual response of your service. This way, you can verify the end-to-end functionality of your service, including any
side effects that might be created around.

Here is a short list of the features provided by this library:

* Declarative &  programmatic request-response training for gRPC and REST endpoints
* Declarative Kafka Consumer definitions for consuming Kafka messages to verify within the test method
* Declarative TestContainers support for MySQL, DynamoDB, Kafka, Redis, ElasticSearch etc.
* Initial data population for MySQL, Redis and ElasticSearch containers
* Embedded DynamoDB support
* Embedded Redis support
* Built-in Embedded Kafka broker configuration for Kafka message publishing and verification
* Automatic tracing of Kafka messages published and consumed by your service
* Built-in in-process gRPC Server configuration and automatic discovery of all GRPC endpoints available
* Built-in MockWebServer configuration for REST endpoints

# How to Start Working with This Library?

There is a separate examples project which demonstrates the 
usage of this library. There are various examples in that project to show you how to write functional integration tests for 
different scenarios. You can make use of example tests in those examples project to get an idea about how to 
write functional integration tests for your service.

## Add the Library Dependency

The **latest version** of the library is `1.0.0-SNAPSHOT`. You can add the library dependency to your service's `build.gradle.kts`
file as follows.

```kotlin
testImplementation("com.fit4j:fit4j:1.0.0-SNAPSHOT")
```

## Create a Test Class

```kotlin
import com.fit4j.annotation.FIT
import org.junit.jupiter.api.Test

@FIT
class SampleFIT {

    @Test
    fun `test something`() {
        
    }
}
```
The `@FIT` annotation marks the test class as an FIT test class. FIT test classes are basically
`@SpringBootTest` classes with some additional capability. It also activates `test` profile in
your service and enable bean overriding capability of the Spring Container.

## Add the Necessary Configuration for Your Test Class

Before proceeding with the sample configuration, let's also speak about the basic structure of any FIT test method,
or any test method in general. A test method usually consists of 3 parts:
1. **Arrange**: In this part, you prepare the test environment within which the test method will be run. This includes creating the necessary data in the database, setting up mock objects, training them etc.
2. **Act**: In this part, you execute the code under test. This includes sending a request to your service, and let the service do the necessary processing, and produce the response.
3. **Assert**: In this part, you verify the result of the code under test. This includes verifying the response returned from your service, verifying the data in the database, verifying the external calls made, messages published to Kafka, etc.

You can add an inner class to your test class and mark it with the `@TestConfiguration` annotation as follows.

```kotlin
import com.fit4j.annotation.FIT
import org.springframework.boot.test.context.TestConfiguration
import org.junit.jupiter.api.Test

@FIT
class SampleFIT {
    
    @TestConfiguration
    class TestConfig {
        
    }

    @Test
    fun `test something`() {
        
    }
}
```
This inner configuration class will be part of  the **Arrange** phase. For example, setting up and training any external
service dependencies, what they will return for a given request will be defined in this configuration class unless the
declarative way of defining those request-response trainings wouldn't suffice in your scenario. Any data that is necessary
during the execution of the current request, such as any database record, or Redis cache entry should also be prepared in
this **Arrange** phase. Simply populating necessary data could either be done within a `setUp` method marked with
`@BeforeEach` annotation. In the **Act** phase, you basically send the request, or submit the message for processing, then
wait for the response. In the **Assert** phase, you verify the response from your service, fetch any data from the environment
and verify them, verify the interactions with the external systems, published message payloads etc.

# What is @IT Annotation? What is It Used For?

As we said before FIT tests are actually integration tests, so this library's features are basically
founded on top of typical integration test concepts. `@IT` annotation serves as a base annotation which
enables some basic configuration and beans which are necessary for any kind of integration tests you already write in your
services, such as configuring gRPC server as in process mode, enabling declarative testcontainer support etc. Our 
`@FIT` annotation already inherits from `@IT` annotation as well. You can also use this annotation 
in your already existing integration tests in order to get rid of verbose configurations you already made. Those tests are
typically more fine-grained tests compared to FIT tests in terms of the functionality they verify, such as verifying
only service - repository, or controller - service layer integrations etc.

```kotlin
import com.fit4j.IntegrationTest
import org.junit.jupiter.api.Test

@IT
class SampleIntegrationTest {
    @Test
    fun `it should work`() {
        
    }
}
```
The following are the features that are enabled when you employ `@IT` annotation in your ordinary integration
tests:

* activating `test` profile, and enabling bean definition override in Spring ApplicationContext
* running gRPC server in process mode, assigning a random value to the in-process name in order to avoid collisions among tests using grpc functionality
* exposing embedded kafka broker as a bean and its address as property if `@EmbeddedKafka` annotation is available
* exposing embedded redis as a bean and its port as a property if `@EmbeddedRedis` annotation is available
* enabling declarative test container support if `@Testcontainers` annotation is available. Look at the related section for further info about declarative test containers support
* exposing embedded dynamo db as a bean if `@EmbeddedDynamoDB` annotation is available

# How to Define Request-Response Trainings for External Services?

Your microservice might have several external service dependencies. Those dependencies could be other internal microservices, 
or any other 3rd party services. In case of internal microservice dependencies, your service might be communicating synchronously 
with them using either REST or gRPC, so you can define the request-response trainings for those service calls in a declarative fashion.

## Define Request-Response Trainings for External gRPC Endpoints

The following example shows how to define grpc request-response trainings in your services through the file `fit4j-fixtures.yml` 
in your test classpath.

```yaml
tests:
  - name: SampleFIT
    fixtures:
      - request:
          protocol: grpc
          type: com.example.FooGrpcService$GetFooByIdRequest
          predicate: "#request.id == 123"
          response:
            body:
              id: 123
              name: "Foo1"
```

Current request object is provided to the predicate expression as `#request` variable. You can write SpEL expressions as 
predicates which should evaluate to a boolean value. If the expression evaluates to true, then the response defined in 
the same block will be returned. The predicate is optional, unless it is provided, the response will be returned for any 
request of the same type. You can write SpEL expressions in other parts of the declarative test fixtures, for example in
the body part as well. The current `request` object is already exposed within the evaluation context. Indeed, the full SpEL 
functionality is available here, so for example you can even refer to Spring managed beans within those SpEL expressions 
aside from accessing the current request object. In the above example, you already notice that `fooId`attribute value
will be obtained via evaluating `#{#request.fooId}` SpEL expression. As you may have already noticed, you need to write
the expression within `#{}` here. In the `predicate` attribute this is not necessary, as the whole `predicate`attribute
value is regarded as SpEL expression itself.

In case declarative way of defining request-response trainings is not capable of what you are trying to do, you can still
return responses programmatically within the test class via creating a bean from
`com.fit4j.grpc.GrpcResponseJsonBuilder` interface as follows.

```kotlin
@FIT
class SampleFIT {
    
    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcResponseJsonBuilder(): GrpcResponseJsonBuilder<Message> {
            return GrpcResponseJsonBuilder {
                if (it is GetFooByIdRequest) {
                    """
                        throw {
                            "status": "PERMISSION_DENIED"
                        }
                    """.trimIndent()
                }
                else {
                    null
                }
            }
        }
    }

    @Test
    fun `test something`() {
        //perform your test here
    }
}
```

It is just enough to define the bean with type `GrpcResponseJsonBuilder` in the test configuration class and within its `build`
method you can inspect the current request and return the response accordingly. In the above example, if the current
request contains a particular `fooId` value, then the service is trained to throw an exception with the status
`"PERMISSION_DENIED"`. The order of those request-response training evaluations is that first the `GrpcResponseJsonBuilder`
bean is asked for an answer, then if it returns null the `fit4j-fixtures.yml` yml file is queried. If there is
no answer found either of those two places, your test will fail while stating you are expected to train for that particular
service call.

### How the gRPC Communication is Redirected to the Mock gRPC Server?

If your service configures gRPC communication via defining `ManagedChannel` bean definitions, the FIT4J library
already detects those bean definitions and replaces them with the `InProcessChannel` instances. Therefore, you don't need
to do anything for your gRPC calls to hit at the mock gRPC server. However, if your configuration involves `@GrpcClient`
annotation to obtain a stub (as below) then you need to override client name property definition in your
`application-test.yml` file.

```kotlin
import com.example.services.rpc.currency_exchange.v1.CurrencyExchangeRateServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class CurrencyExchangeRateServiceAdapter {
    @GrpcClient("currencyExchangeRateService")
    private lateinit var currencyExchangeRateService: CurrencyExchangeRateServiceGrpc.CurrencyExchangeRateServiceBlockingStub
}
```

```properties
grpc:
  client:
    currencyExchangeRateService:
      address: in-process:${grpc.server.inProcessName}
```
## Define Request-Response Trainings for External HTTP/REST Endpoints

In case your service hits some REST endpoints of any other service, you will need to provide request-response
trainings for those calls as well. Let's assume you have already defined an interface to access those REST endpoints using
the Retrofit Library in your service codebase as follows.

```kotlin
interface ExampleRestClient {
    @POST("/hello")
    fun sayHello(@Body helloRequest: ExampleRestRequest): Call<ExampleRestResponse>

    @POST("/bye")
    fun sayBye(@Body byeRequest: ExampleRestRequest): Call<ExampleRestResponse>
}

@EnableConfigurationProperties(HttpProperties::class)
@Configuration
class HttpConfig {
    @Bean
    fun createExampleRestClient(retrofit: Retrofit) = retrofit.create(ExampleRestClient::class.java)

    @Bean
    fun okHttpExampleRestClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .build()
    }

    @Bean
    fun retrofit(okHttpExampleRestClient: OkHttpClient, httpProperties: HttpProperties): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${httpProperties.protocol}://${httpProperties.hostname}:${httpProperties.port}")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpExampleRestClient)
            .build()
    }
}

@ConfigurationProperties("externalService")
data class HttpProperties(val protocol:String="http", val hostname:String="localhost", val port:Int=8080) {
}
```

Then you can prepare the following request-response trainings for those REST endpoints for your FITs either
in declarative way or programmatically.

```yml
tests:
  - name: RestExampleFIT
    fixtures:
      - request:
          protocol: http
          path: "/hello"
          response:
            status: 200
            body:
              message: "Hello, John!"
      - request:
          protocol: http
          path: "/bye"
          predicate: "#request.method == 'POST'"
          response:
            status: 200
            body:
              message: "Bye, Joe!"
```

The above example shows how it can be done in declarative fashion within `fit4j-fixtures.yml` file.
Similar to GRPC, you can write SpEL expression in your HTTP test fixtures as well. For HTTP requests, the current request 
state is captured as `HttpRequestContext` object and exposed again with the `#request` variable in the SpEL expressions.
You can access `path`, `method`, `body`, `headers`, `requestUrl` values through this variable. Similar to gRPC, the predicate 
attribute is optional, and unless it is provided, the response will be returned for any request matching with the given path.
Again you can write SpEL expressions in any place of your HTTP test fixtures similar to explained in the gRPC section above.

```yaml
tests:
  - name: RestExampleFIT
    fixtures:
      - request:
          protocol: http
          path: "/hello"
          response:
            status: 200
            body: "#{@testFixtureCreator.EXAMPLE_RESPONSE}"
```
In the above example, the body part is written as a SpEL expression, and it refers to a Spring managed bean `testFixtureCreator`
which is defined in the `RestExampleFIT` test class as below.

```kotlin
@FIT
class RestExampleFIT {
    @TestConfiguration
    class TestConfig {
        @Bean
        fun testFixtureCreator(): TestFixtureCreator {
            return TestFixtureCreator()
        }
    }
}
```

Similar to gRPC, if you need to return HTTP responses programmatically the library provides the necessary mechanism for you.
In that case, you will need to implement a bean from `com.fit4j.http.HttpResponseJsonBuilder` 
interface in the test configuration class, and prepare the response programmatically as follows.

```kotlin
@FIT
class SampleFIT {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcResponseBuilder(): GrpcResponseJsonBuilder<Message> {
            return GrpcResponseJsonBuilder {
                when (it) {
                    is FooGrpcService.GetFooByIdRequest ->
                        when (it.id) {
                            123.toLong() -> {
                                """
                                    {
                                        "foo": {
                                            "id":123,
                                            "name":"Foo1"
                                        }
                                    }
                                                """.trimIndent()
                            }
                            456.toLong() -> {
                                """
                                    {
                                        "foo": {
                                            "id":456,
                                            "name":"Foo2"
                                        }
                                    }
                                                """.trimIndent()
                            }
                            else -> {
                                null
                            }
                        }
                    is FooGrpcService.GetFooNameByIdRequest ->{
                        when(it.id) {
                            123.toLong() -> {
                                """
                                {
                                    "name": "Foo"
                                }
                                """.trimIndent()
                            } else -> {
                            null
                        }
                        }
                    }
                    else -> null
                }
            }
        }
    }

    @Test
    fun `test something`() {
        //perform your test here
    }
}
```

You need to define the following properties in your `application-test.yml`
file of your service. They simply override externalService `hostname` and `port` properties of `HttpProperties` configuration
with the `hostname` and `port` values of the mockwebserver.

```properties
externalService.hostname=${fit4j.mockWebServer.hostName}
externalService.port=${fit4j.mockWebServer.port}
``` 

In case your service accesses a 3rd party service using Spring `RestTemplate`, you can similarly define request-response
trainings in a similar way, and pass `fit4j.mockWebServer.hostName` and `fit4j.mockWebServer.port` properties
to the place where you perform your REST calls via `RestTemplate`.

# How to Initiate Request Processing Flow in Your Service?

The request processing flow of your service currently being tested might be triggered by a gRPC or REST call, or by
submitting a Kafka message.

## Calling gRPC Endpoints of Your Service

You can call a particular grpc endpoint of your service via a grpc stub instance injected into your test class as follows.

```kotlin
@FIT
class SampleFIT {
    @GrpcClient("inProcess")
    private lateinit var grpcClient: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub
  
    @Test
    fun `test something`() {
        //given (arrange)
        val request = GetFooByIdRequest.newBuilder()
          .setId(123).build()
        //when (act)
        val response = grpcClient.getFooByIdResponse(request)
        //then (assert)
        Assertions.assertNotNull(response)
    }
}
```
After defining an instance variable of your particular grpc service stub class, you need to annotate it with `@GrpcClient`
annotation. `inProcess` is the name of the grpc client address that your grpc service uses, which
is already defined by the test library behind the scenes.

## Calling REST Endpoints of Your Service

`@SpringBootTest` annotation which is inherited by `@FIT` annotation by default creates a MOCK WebEnvironment. 
In the MOCK web environment, the Spring Boot application context is started but the web server itself is not. Instead of 
starting a real server, a mock server environment is created using Spring’s `MockMvc` framework. This setup is actually 
enough for all test scenarios unless your service has REST endpoints, and you would like to test them via a 
REST call. For that purpose, either RANDOM_PORT or DEFINED_PORT WebEnvironments needs to be used. Both of them create a 
real embedded web server at a random port or predefined port with `server.port` property. In this case the application is 
fully initialized including the embedded server, and tests can interact with the server using actual HTTP requests at the 
cost of running a bit slower compared to MOCK WebEnvironment. You can change the webEnvironment attribute value to RANDOM_PORT
via the same `@FIT` annotation like `@FIT((webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` so that embedded web 
server should be created at a random port. That way you can test your REST endpoints either using a `RestTemplate` object 
you instantiate in your test, or make use of `TestRestTemplate`bean provided by Spring Boot, which is also accessible through 
the test library's `FitTestHelper` component.

```kotlin
@FIT((webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SampleFIT {
    @Autowired
    private lateinit var helper: FitHelper

    @Test
    fun `test rest endpoint`() {
        val response = helper.beans.restTemplate.getForObject("/sayHello", String::class.java)
        Assertions.assertEquals("Hello World!", response)
    }
}

@RestController
class TestRestController {
    @GetMapping("/sayHello")
    fun sayHello(): String {
        return "Hello World!"
    }
}
```

## Publishing Kafka Messages to be Consumed by Your Service

You can either use Embedded Kafka broker coming with Spring Kafka Test library, or you can use a real Kafka broker via
test containers. In order to enable `EmbeddedKafka` in your test, it is enough to add `@EmbeddedKafka`
annotation to your test class. If you want to use a real Kafka broker via test containers, you can look at
[Use Testcontainers](#how-to-work-with-testcontainers) section of this document for more details.

According to JavaDoc documentation of Spring Kafka Test library, in case you are using `@EmbeddedKafka` annotation, you
should also use `@DirtiesContext` annotation in order to make sure that the context is closed after test class. This is
important  in order to prevent certain race conditions on JVM shutdown when running multiple tests. It will also cause
resources held by the embedded Kafka broker to be released.

It will be easier to publish and verify Kafka messages if you utilize `FitHelper` as it provides several
helper beans and methods for message publishing, processing and verification steps. You can easily inject it as a bean. 
You can publish Kafka messages to be consumed by your service via the `KafkaTemplate` instance accessible via `helper.beans` 
property defined in the bean. You can then make use of `KafkaMessageTracer` bean to wait for the processing of the published
message and verify it.

```kotlin
@EmbeddedKafka
@FIT
class SampleFIT {

  @Autowired
  private lateinit var fooRepository: FooRepository
  
  private lateinit var helper: FitHelper

  @Test
  override fun testFoo() {
      val message = Foo.newBuilder().setId(123).setName("Foo").build()
      helper.beans.kafkaTemplate.send("foo-create-topic", message).get()

      helper.beans.kafkaMessageTracker.waitForProcessing(Foo::class)

      val foo = fooRepository.findById(123)
      Assertions.assertNotNull(foo)
  }
}
```

If you need to clean up the topics in between the tests or test classes, you can benefit from the `KafkaTopicCleaner` which
deletes topics after each test method execution. It is disabled by default, you can enable it by setting the property
`fit4j.kafka.topicCleaner.enabled=true` in your `application-test.properties` file.

# How to Verify gRPC & REST Calls, Kafka Messages?

The FIT4J test library provides you with the ability to verify the GRPC and REST calls made by your service.
This can be done via a bean of type `com.fit4j.mock.MockServiceCallTracker` provided by the library. It provides methods 
like `getGrpcRequest`, `getHttpRequest` to get the requests submitted by
your service. It also provides methods like `hasAnyError`, `hasGrpcError`, `hasHttpError` which will let you check
whether there are any errors with the given status codes.

By default, there is a method called `verifyEndpointCallsResultedWithoutErrors` defined in `BaseAcceptanceTest` class which
is already called at the end of the `runTestSteps` method. This method checks if there are any errors with the status codes
404 and 500 and fails the test method in case there is any.

```kotlin
protected fun verifyEndpointCallsResultedWithoutErrors(vararg statusCodes: Int = intArrayOf( 404,500)) {
    helper.beans.mockServiceCallTracker.logFailures()
    if(helper.beans.mockServiceCallTracker.hasAnyError(statusCodes)) {
        Assertions.fail<Any>("There are errors with given status codes: ${statusCodes.joinToString(",")}")
    }
}
```

You can override this method and provide your own implementation if you want to check for different status codes.

## Verify the Kafka Messages Published by Your Service

During the request processing flow, your service might publish Kafka messages to be consumed by other services. You can
verify those published messages within your test method. In order to do that, you need to define Kafka Consumer definitions
for your test class so that any published message could be consumed and be available to be verified from within the test
method.

### Define Kafka Consumer Definitions to Consume Kafka Messages for Verification in Your Tests

For each different Kafka topic you publish messages, you need to define a different Kafka Consumer definition.
Kafka Consumer definitions can be done in a declarative fashion. For this purpose, you need to create a file called
`fit4j-kafka-consumers.yml` in your test classpath.

```yml
- consumer:
    topic: sample-topic-1
    containerFactory:
      consumerFactory:
        configs:
          - key.deserializer: org.apache.kafka.common.serialization.StringDeserializer
          - value.deserializer: org.apache.kafka.common.serialization.StringDeserializer
      containerProperties:
        ackMode: MANUAL_IMMEDIATE
        groupId: sample-consumer-group
```

You can define several different consumers for each different topic that should be tested against. For each Kafka consumer
definition, the test library registers a `TestMessageListener` bean that is used to track the messages published to
those topics from within the service.

###  Waiting For Kafka Messages to be Published or Consumed

The FIT4J test library behind the scenes intercepts execution of methods marked with `@KafkaListener` in the service
and marks those messages handled by them as `processed`. Therefore, you can wait for the processing of those messages to
be completed by calling `waitForProcessing` method of the `KafkaMessageTracker` in your test method. In a similar manner,
execution of `org.springframework.kafka.core.KafkaTemplate.send()` methods is also intercepted and the messages sent via
`KafkaTemplate` from within the service are marked as `published`. Therefore, you can also wait for the publishing of those
messages as well. The library also provides you with the ability to wait for the messages to be received by the
`TestMessageListener`bean configured through Kafka Consumer Definitions mentioned earlier. All of those `waitForPublish`,
`waitForProcessing`, and `waitForReceiving` methods of the `KafkaMessageTracker` expect the message type, and returns the
corresponding message instance.

### Verify Published or Consumed Kafka Messages

A bean of type `com.fit4j.helper.FitHelper` provides several convenience `verifyEntity` and `verifyEvent` methods for 
verification of JPA entities and Kafka messages.

```kotlin
@FIT
class SampleFIT {
    @Autowired
    private lateinit var fooRepository: FooRepository
    
   @Test 
    override fun testVerifications() {
        val foo = refundEntityRepository.findById(123)

        helper.verifyEntity(
            foo,
            """
            {
                "id":123,
                "name":"Foo"
            }
            """.trimIndent()
        )

       val actualJsonString = helper.beans.jsonMapper.writeValueAsString(foo)
       helper.verifyValues(
           actualJsonString,
           """
            {
                "id":123,
                "name":"Foo"
            }
            """.trimIndent()
       )
        
        helper.verifyEvent(
            FooCreatedEvent::class,
            """
            {
                "id":123,
                "name":"Foo"
            }
            """.trimIndent()
        )
    }
}
```
In the above example, the `Foo` instance is fetched from the database and its state is verified against the
expected state represented as JSON content. Similarly, Kafka message of `FooCreatedEvent` type is verified against
expected state represented as JSON content.

# How to Work with TestContainers?

The FIt4J test library provides you with the ability to run your service against a real database, Kafka broker, Redis etc.
using Testcontainers in a completely declarative fashion. In order to enable this feature, you need to add `@Testcontainers` annotation
on top of your test class.

```kotlin
import org.testcontainers.junit.jupiter.Testcontainers
import com.fit4j.annotation.FIT
import org.junit.jupiter.api.Test

@Testcontainers
@FIT
class SampleFIT {

  @Test
  fun `test something`() {

  }
}
```
Afterward, you can create a `fit4j-test-containers.yml` file in the test resources source folder of your service
and add the container definitions your service needs during the execution of test method. You can make use of the following
example to get started.

```yaml
- container: org.testcontainers.containers.MySQLContainer
  name: mySQLContainerDefinition
  image: mysql:5.7.33
  exposedPorts:
    - 3306
  username: root
  password: root
  databaseName: v1
  initScript: scripts/v1_init.sql
  env:
    - TZ: "America/Los_Angeles"
  urlParam:
    - serverTimezone: "America/Los_Angeles"
    - useLegacyDatetimeCode: "false"
  exposedProperties:
    - jdbcUrl
    - username
    - password
  reuse: true
- container: org.testcontainers.containers.GenericContainer
  name: redisContainerDefinition
  image: redis:6.2.1
  exposedPorts:
    - 6379
  exposedProperties:
    - host
    - firstMappedPort
  reuse: true
- container: org.testcontainers.elasticsearch.ElasticsearchContainer
  name: elasticSearchContainerDefinition
  image: docker.elastic.co/elasticsearch/elasticsearch:8.10.2
  initScript: scripts/elasticsearch_initial_data.yml
  exposedPorts:
    - 9200
    - 9300
  env:
    - ELASTICSEARCH_USERNAME: root
    - ELASTICSEARCH_PASSWORD: root
    - xpack.security.enabled: false
    - bootstrap.memory_lock: true
    - cluster.routing.allocation.disk.threshold_enabled: false
  reuse: false
```

You can inject **exposedProperties** into your `application.properties` file of your service as follows.

```properties
spring.datasource.url=${fit4j.mySQLContainerDefinition.jdbcUrl}
spring.datasource.username=${fit4j.mySQLContainerDefinition.username}
spring.datasource.password=${fit4j.mySQLContainerDefinition.password}
```

The format to access exposed properties is of form `fit4j.<container-name>.<exposed-property-name>`.

The FIT4J test library creates those containers  to be reused in case they are marked with `reuse: true` property.
However, for reuse to work properly, developers need to enable it in their local environment by adding following property in
`.testcontainers.properties` file in their **HOME** directory.

```properties
testcontainers.reuse.enable=true
```
The testcontainers capability of the test library can be independently used by the services regardless of whether
they use rest of the library features.

## Selectively Registering Your TestContainers

By default, the test library registers all test container definitions discovered within `fit4j-test-containers.yml`
file if the test class also has `@org.testcontainers.junit.jupiter.Testcontainers` annotation. However, your test classes might not need all of them to be
bootstrapped, instead it might just need a couple of those test container definitions to be instantiated. For this purpose,
the test library provides another annotation `@com.fit4j.testcontainers.Testcontainers`
which expects you to list the container definition names for registration, and only those explicitly listed in the annotation
will be registered and available for your test. This annotation also inherits from `@org.testcontainers.junit.jupiter.Testcontainers`
so it is  just enough to place the `@com.fit4j.testcontainers.Testcontainers` annotation on the
test class.

```kotlin
@FIT
@com.fit4j.testcontainers.Testcontainers(definitions = ["redisContainerDefinition"])
class TestContainersWithSelectiveRegistrationFIT {
    
    @Test
    fun `test something`() {
        
    }
}
```

## Initial Data Population for TestContainers

### Initial Data Population for MySQL Container

If you are using `org.testcontainers.containers.MySQLContainer` to create your test container it already has built-in
capability to process an init script provided in the `initScript` property of the container definition. The file content
should be SQL statements that you want to execute to populate initial data in the database.

### Initial Data Population for ElasticSearch Container

For elastic search users, if you are using `org.testcontainers.elasticsearch.ElasticsearchContainer` to create your test
container, the FIT4J Library provides you with a similar capability to populate initial data in the elastic search index.
You can provide an `initScript` property in the container definition with the path to the file containing the initial data in
YAML format. Here is an example of how you can define the initial data for elastic search.

```yaml
- document:
    index: "test"
    id: "1"
    body:
      name: "John Doe"
      age: 25
      email: "john.doe@example.com"
- document:
    index: "test"
    id: "2"
    body:
      name: "Jane Doe"
      age: 30
      email: "jane.doe@example.com"
```

### Initial Data Population for Redis Container

For redis users, if you are using `org.testcontainers.containers.GenericContainer` to create your test container, you can
provide an `initScript` property in the container definition with the path to the file containing the initial data in YAML
format. Here is an example of how you can define the initial data for redis.

```yaml
- entry:
    type: "string"
    data:
        key: "stringKey"
        value: "stringValue"
- entry:
    type: "hash"
    data:
        key: "hashKey"
        fields:
            field1: "value1"
            field2: "value2"
```
The supported data types for redis are `string`, `hash`, `list`, `set`, `sortedset`, `bitmap`, `hyperloglog`,
`geospatial`, `stream`. You can look at this [file](src/test/resources/scripts/redis_initial_data.yml) for the data
structure of each data type.

# How to Work with Embedded DynamoDB?

Aside from utilizing declarative Testcontainers support to test against dynamoDB usage, the test library also 
provides you with the ability to run your service against a local embedded DynamoDB instance independent of Testcontainers.
In order to enable this feature, all you need to add `@EmbeddedDynamoDB` annotation on top of your test class. That way
the test library will start an embedded DynamoDB instance and override the AWS DynamoDB client bean definition
in your service with the embedded DynamoDB client.

# How to Work with Embedded Redis?

If you don't want to use Testcontainers to bootstrap Redis server, you have another choice, running redis embedded. You
can use `@EmbeddedRedis` annotation on top of your test to enable it. By default, it runs on a random port, but
you can change this through the annotation. The port on which Redis runs is exposed as `fit4j.embeddedRedisServer.port`
property in the ApplicationContext.

# How to Work with Other Misc Stuff?

## Using Protobuf JsonFormat.Parser and JsonFormat.Printer Beans For State Verification

The test library provides you with beans of type `JsonFormat.Parser` and `JsonFormat.Printer` to deal
with JSON serialization and deserialization of protobuf objects. This is especially useful when you need to verify the
state of protobuf objects against expected state represented as JSON content. Indeed, the `verifyEntity` and `verifyEvent`
(kafka events) methods mentioned in the previous section make use of those beans under the hood.

Sometimes those protobuf types may contain `com.google.protobuf.Any` fields to store other protobuf types. In that case, 
in order to work with those`Parser` and `Printer` beans, you would need to configure them with 
`com.google.protobuf.Descriptors.Descriptor` instances so that they could properly serialize or deserialize messages. 
However, the FIT4J test library transparently does this sort of `Descriptor` registration automatically behind the scenes. 
Therefore, you can immediately work with such kind of nested structures as demonstrated below.

```kotlin
verifyEvent(
            AnyRecord::class,
            """
            {
                "values": {
                    "@type": "type.googleapis.com/com.example.Foo",
                    "id": 123,
                    "name": "Foo"
                }
            }
            """.trimIndent()
        )
```

# FAQ

**Q**: Which method should I prefer to use for FITs, declarative or programmatic fixture definitions?

**A**: The FIT4J test library provides you with the ability to define your test fixtures both in a programmatic and
declarative fashion. They are functionally equivalent, and you can choose the one that fits your needs better. However,
be aware that the programmatic fixture definitions are defined with the creation of a `@TestConfiguration` class in your test
and this will cause the Spring ApplicationContext to be recreated for your test class. This means that if the number of
your tests are high, and for example, you also depend on Testcontainers, it might slow down your test execution
and memory consumption of the TestContainers platform (Rancher Desktop) might increase. On the other hand, declarative
fixture definitions are defined in a YAML file, and they are read only once by the test infrastructure and
served to all test classes. This means that the Spring ApplicationContext is not recreated for each test class, and the
test execution is faster and more memory efficient. Therefore, it is recommended to use declarative fixture definitions
whenever possible.

**Q**: What is the difference between `@FIT` and `@IT` annotations?

**A**: `@IT` annotation just enables several configuration properties and infrastructural beans that are useful or
necessary for ordinary integration tests which try to verify interactions among several components such as interaction between
gRPC controller and service layers, or interactions between service and repository layers etc. Those configuration properties
and infrastructural beans are also provides a basis for the tests written with `@FIT` annotation. That annotation enable full 
FIT environment such as programmatic and declarative request-response trainings support, sync call and async message tracking 
etc, which are not available when `@IT` annotation is used. Here is the complete list of features that are available for tests
written with those annotations. Here is a more detailed table that lists available functionalities with each annotation.

|                                                                                                                                                                                                                                          | @IT | @FIT | @FIT(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT) |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|-----------------|----------------------------------------------------------------|
| testClass FQN and simple names are exposed as an environment properties fit4j.testClass.name, fit4j.testClass.simpleName                                                                                                                 | Yes              | Yes             | Yes                                                            |
| DynamoDBEmbedded is exposed as a Spring bean if @EmbeddedDynamoDB annotation is used in test class                                                                                                                                       | Yes              | Yes             | Yes                                                            |
| spring.kafka.bootstrap-servers property is set if @EmbeddedKafka is used in test class                                                                                                                                                   | Yes              | Yes             | Yes                                                            |
| EmbeddedRedisServer is exposed as a Spring bean along with its port as an environment property fit4j.embeddedRedisServer.port if @EmbeddedRedis annotation is used in test class                                                         | Yes              | Yes             | Yes                                                            |
| gRPC server is run in-process mode at random port along with in-process name is assigned random name and clients are enabled to access it through several alternative names inProcess, inProcessClient, inProcessClientForAcceptanceTest | Yes              | Yes             | Yes                                                            |
| okhttp3 MockWebServer is exposed as a Spring bean along with its host and port values as environment properties fit4j.mockWebServer.host, fit4j.mockWebServer.port if it is available in test classpath                                  | Yes              | Yes             | Yes                                                            |
| Declarative Test Container support is enabled if @Testcontainers annotation is used in test class                                                                                                                                        | Yes              | Yes             | Yes                                                            |
| gRPC & HTTP request-response training and call tracking capability is enabled                                                                                                                                                            | No               | Yes             | Yes                                                            |
| gRPC automatic service and type descriptor discovery capability is enabled                                                                                                                                                               | No               | Yes             | Yes                                                            |
| Kafka message tracking capability is enabled                                                                                                                                                                                             | No               | Yes             | Yes                                                            |
| Google JsonFormat Printer & Parser classes are exposed as Spring bean if they are in class path                                                                                                                                          | No               | Yes             | Yes                                                            |
| Spring TestRestTemplate bean is enabled                                                                                                                                                                                                  | No               | No              | Yes                                                            |



