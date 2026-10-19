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
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "fit4j"
