pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        maven {
            name = "fit4jCandidate"
            url = uri(providers.gradleProperty("fit4jRepository").get())
            content {
                includeModule("io.github.harezmi", "fit4j")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "fit4j-gradle-consumer-fixtures"
include("java-consumer", "kotlin-consumer")
