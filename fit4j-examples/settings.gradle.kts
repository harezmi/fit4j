pluginManagement {
    val kotlinPluginVersion: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version kotlinPluginVersion
        kotlin("plugin.spring") version kotlinPluginVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		mavenLocal()
	}
}

rootProject.name = "fit4j-examples"

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
