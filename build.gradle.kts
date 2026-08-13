import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.security.MessageDigest


plugins {
	`java-library`
	kotlin("jvm")
	kotlin("plugin.spring")
    id("com.google.protobuf") version "0.9.4"
    id("signing")
    id("com.vanniktech.maven.publish") version "0.34.0"
}

val javaToolchainVersion: String by project
val javaBytecodeVersion: String by project
val protobufJavaVersion: String by project
val springGrpcVersion: String by project // unused; gRPC starters ship with Boot 4.1
val grpcVersion: String by project
val grpcKotlinStubVersion: String by project

repositories {
	mavenCentral()
}

configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.group == "io.grpc" && requested.name !in setOf("grpc-kotlin-stub", "protoc-gen-grpc-java", "protoc-gen-grpc-kotlin")) {
			useVersion(grpcVersion)
			because("Align gRPC Java artifacts; spring-grpc-core 1.1 can pull a newer grpc-core than grpc-netty from the Boot BOM")
		}
	}
}

dependencies {
	val springBootVersion : String by project
	val springGrpcVersion : String by project
	val kotlinVersion : String by project
	val mockkVersion : String by project
	val protobufJavaVersion : String by project
	val dynamoDBLocalVersion : String by project
	val redisVersion: String by project

	api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
	compileOnly(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	compileOnly("org.springframework.boot:spring-boot-starter-grpc-server")
	compileOnly("org.springframework.boot:spring-boot-starter-grpc-client")
	compileOnly("io.grpc:grpc-inprocess")
	compileOnly("io.grpc:grpc-api")
	compileOnly("io.grpc:grpc-stub")
	testImplementation("io.grpc:grpc-kotlin-stub")
	implementation("org.springframework.boot:spring-boot-test")
	implementation("org.springframework.boot:spring-boot-restclient")
	implementation("org.springframework.boot:spring-boot-resttestclient")
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-kafka")
	implementation("org.springframework.boot:spring-boot-jackson")
	implementation("com.h2database:h2")
	implementation("org.springframework.kafka:spring-kafka-test")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("net.bytebuddy:byte-buddy")
	implementation("org.yaml:snakeyaml")
	implementation("org.apache.commons:commons-lang3")
	implementation("tools.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
	implementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")
	implementation("com.google.protobuf:protobuf-java-util:$protobufJavaVersion")
	implementation("io.mockk:mockk:$mockkVersion")
	testImplementation("org.springframework.boot:spring-boot-starter-grpc-server")
	testImplementation("org.springframework.boot:spring-boot-starter-grpc-client")
	testImplementation("org.springframework.boot:spring-boot-starter-grpc-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-grpc-client-test")
	implementation("org.testcontainers:testcontainers")
	implementation("org.testcontainers:testcontainers-junit-jupiter")
	implementation("org.testcontainers:testcontainers-kafka")
	implementation("org.testcontainers:testcontainers-elasticsearch")
	implementation("org.testcontainers:testcontainers-mysql")
	implementation("org.testcontainers:testcontainers-postgresql")
	implementation("org.testcontainers:testcontainers-toxiproxy")
	implementation("co.elastic.clients:elasticsearch-java")
	implementation("redis.clients:jedis:$redisVersion")
	implementation("com.github.codemonstur:embedded-redis:1.4.3")

	implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
	implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")
	implementation("io.github.resilience4j:resilience4j-micrometer:2.2.0")

	implementation("com.amazonaws:DynamoDBLocal:$dynamoDBLocalVersion") {
		exclude(group = "org.slf4j", module = "slf4j-api")
	}

	implementation("jakarta.annotation:jakarta.annotation-api")

	testImplementation("org.springframework.boot:spring-boot-starter-test-classic")
	testImplementation("org.springframework.boot:spring-boot-resttestclient")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc")
	testImplementation("com.mysql:mysql-connector-j")
    testImplementation("org.postgresql:postgresql")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
		jvmTarget.set(JvmTarget.fromTarget(javaBytecodeVersion))
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	minHeapSize = "1g"
	maxHeapSize = "5g"
	maxParallelForks = 8
	jvmArgs("--enable-native-access=ALL-UNNAMED")
	// DynamoDBLocal embeds an older shaded kotlin.reflect; prefer Boot/Kotlin BOM jars first.
	doFirst {
		classpath = files(
			classpath.files.sortedWith(
				compareBy(
					{ file ->
						when {
							file.name.startsWith("kotlin-stdlib") -> 0
							file.name.startsWith("kotlin-reflect") -> 1
							file.name.contains("DynamoDBLocal") -> 3
							else -> 2
						}
					},
					{ it.name },
				)
			)
		)
	}
}

tasks.withType<PublishToMavenRepository> {
	mustRunAfter(tasks.named("test"))
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaToolchainVersion.toInt()))
	}
	sourceCompatibility = JavaVersion.toVersion(javaBytecodeVersion)
	targetCompatibility = JavaVersion.toVersion(javaBytecodeVersion)
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
    generateProtoTasks {
        ofSourceSet("test").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(true)
    signAllPublications()
}

mavenPublishing {
    coordinates("${project.group}", "${project.name}", "${project.version}")

    pom {
        name.set("FIT4J")
        description.set("Functional Integration Testing Library for Java and Kotlin microservices")
        inceptionYear.set("2025")
        url.set("https://github.com/harezmi/fit4j")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("harezmi")
                name.set("Kenan Sevindik")
                email.set("ksevindik@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/harezmi/fit4j.git")
            developerConnection.set("scm:git:ssh://github.com:harezmi/fit4j.git")
            url.set("https://github.com/harezmi/fit4j")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

val signingTasks: TaskCollection<Sign> = tasks.withType<Sign>()
tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(signingTasks)
}

apply(from = "gradle/sync-docs-version.gradle.kts")
