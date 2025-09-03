rootProject.name = "examples"

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		mavenLocal()
	}
}

include("example-basic")
include("example-kafka")
include("example-grpc")
include("example-elastic-search")
include("example-redis")
include("example-mysql")
include("example-kafka-testcontainers")
include("example-rest")
include("example-h2")
include("example-dynamodb-testcontainers")
include("example-dynamodb")
include("example-redis-embedded")
include("example-s3")
