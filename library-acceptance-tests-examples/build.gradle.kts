import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion : String by project
val udemyAcceptanceTestsLibVersion : String by project
val udemyProtobufKotlinLibVersion : String by project

plugins {
    `java-library`
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
    from(sourceSets.main.get().output)
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring")

    dependencies {
        testImplementation(project(":"))

        testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("com.udemy.libraries.tests:library-service-acceptance-tests:$udemyAcceptanceTestsLibVersion")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks {
        withType<KotlinCompile>().configureEach {
            kotlinOptions {
                freeCompilerArgs += "-Xjsr305=strict"
                jvmTarget = "17"
            }
        }

        test {
            useJUnitPlatform()
            minHeapSize = "2g"
            maxHeapSize = "15g"
        }
    }
}

