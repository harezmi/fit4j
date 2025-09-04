import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*

val springBootVersion : String by project
val protobufJavaVersion : String by project
val grpcVersion: String by project

plugins {
    `java-library`
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("com.google.protobuf:protobuf-java:${protobufJavaVersion}")
    implementation("com.google.protobuf:protobuf-java-util:${protobufJavaVersion}")
    implementation("io.grpc:grpc-api:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
    implementation("io.grpc:grpc-kotlin-stub:1.4.3")
    implementation("io.grpc:grpc-protobuf:1.63.0")
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
        testImplementation("com.fit4j:fit4j:1.0.0-SNAPSHOT")


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

protobuf {
    // Configure the Protobuf compiler (protoc)
    protoc {
        // This will download the correct protoc binary for your OS and architecture
        artifact = "com.google.protobuf:protoc:3.21.2"
    }

    // Configure the Protobuf plugins
    plugins {
        // Specify the gRPC plugin
        id("grpc") {
            // Use the correct platform-specific artifact for the gRPC code generator
            artifact = "io.grpc:protoc-gen-grpc-java:1.63.0:osx-aarch_64"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.3:jdk8@jar"
        }
    }

    // 👇 THIS IS THE MISSING BLOCK!
    // It instructs the plugin to create and configure tasks for generating code.
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                // Generates the standard Java classes from .proto files
                java {
                    // This block can be empty, but it's required for the task to be created
                }
                // Generates the standard Kotlin classes from .proto files
                kotlin {
                    // This block can be empty as well
                }
            }
            task.plugins {
                // Applies the gRPC Java plugin to generate gRPC service stubs.
                id("grpc")
                // Applies the gRPC Kotlin plugin to generate Kotlin service stubs.
                id("grpckt")
            }
        }
    }
}

