# Functional Integration Tests Library Usage Examples

This project contains examples of how to use FIT with various other libraries and frameworks. The example projects are completely self-contained. They can be run independently. 
Aside from the library dependency itself, the other dependencies listed in each example project's `build.gradle.kts` file is sufficient 
to  make use of the example in your own service.

Here is the list of examples:
* [Basic](example-basic/) contains test examples to demonstrate how to start writing FITs using the library.
* [gRPC](example-grpc/) contains test examples to demonstrate how to write FITs for gRPC calls.
* [HTTP/REST](example-rest/) contains test examples to demonstrate how to write FITs for HTTP/REST calls.
* [Kafka Embedded](example-kafka/) contains test examples to demonstrate how to write FITs for Kafka using Embedded Kafka Broker coming with Spring Kafka Test Library.
* [Kafka with Testcontainers](example-kafka-testcontainers) contains test examples to demonstrate how to write FITs for Kafka using Testcontainers.
* [Elasticsearch](example-elasticsearch/) contains test examples to demonstrate how to write FITs accessing Elasticsearch using Testcontainers.
* [Redis](example-redis/) contains test examples to demonstrate how to write FITs tests accessing Redis using Testcontainers.
* [Redis Embedded](example-redis-embedded) contains test examples to demonstrate how to write FITs accessing Redis running as Embedded.
* [MySQL DB](example-mysql/) contains test examples to demonstrate how to write FITs accessing MySQL using Testcontainers.
* [H2 DB InMemory](example-h2/) contains test examples to demonstrate how to write FITs using H2 Embedded Database.
* [DynamoDB Embedded](example-dynamodb/) contains test examples to demonstrate how to write FITs accessing DynamoDB running as Embedded.
* [DynamoDB with Testcontainers](example-dynamodb-testcontainers/) contains test examples to demonstrate how to write FITs accessing DynamoDB using Testcontainers.
* [S3](example-s3) contains test examples to demonstrate how to write FITs accessing AWS S3 using Localstack Testcontainers.