import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion : String by project
val protobufJavaVersion : String by project
val grpcVersion: String by project
val grpcKotlinStubVersion: String by project
val javaToolchainVersion: String by project
val javaBytecodeVersion: String by project
val fit4jVersion: String by project

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.spring")
    id("com.google.protobuf") version "0.9.4"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("com.google.protobuf:protobuf-java:${protobufJavaVersion}")
    implementation("com.google.protobuf:protobuf-java-util:${protobufJavaVersion}")
    implementation("io.grpc:grpc-api:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinStubVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaToolchainVersion.toInt()))
    }
    sourceCompatibility = JavaVersion.toVersion(javaBytecodeVersion)
    targetCompatibility = JavaVersion.toVersion(javaBytecodeVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.fromTarget(javaBytecodeVersion))
    }
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

        testImplementation("io.github.harezmi:fit4j:$fit4jVersion")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaToolchainVersion.toInt()))
        }
        sourceCompatibility = JavaVersion.toVersion(javaBytecodeVersion)
        targetCompatibility = JavaVersion.toVersion(javaBytecodeVersion)
    }

    tasks {
        withType<KotlinCompile>().configureEach {
            compilerOptions {
                freeCompilerArgs.add("-Xjsr305=strict")
                jvmTarget.set(JvmTarget.fromTarget(javaBytecodeVersion))
            }
        }

        test {
            useJUnitPlatform()
            minHeapSize = "2g"
            maxHeapSize = "15g"
            jvmArgs("--enable-native-access=ALL-UNNAMED")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufJavaVersion"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinStubVersion:jdk8@jar"
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

