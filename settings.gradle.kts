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

rootProject.name = "fit4j"