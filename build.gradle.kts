import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`java-library`
	kotlin("jvm") version "2.0.20"
	kotlin("plugin.spring") version "2.0.20"
    id("com.google.protobuf") version "0.9.4"
    id("maven-publish")
}

repositories {
	mavenCentral()
}

dependencies {
	val springBootVersion : String by project
	val kotlinVersion : String by project
	val testcontainersVersion : String by project
	val grpcSpringBootVersion : String by project
	val mockkVersion : String by project
	val protobufJavaVersion : String by project
	val dynamoDBLocalVersion : String by project
	val grpcVersion : String by project
	val elasticSearchVersion: String by project
	val redisVersion: String by project

	implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-test")
	implementation("com.h2database:h2")
	implementation("org.springframework.kafka:spring-kafka-test")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("net.bytebuddy:byte-buddy")
	implementation("org.yaml:snakeyaml")
	implementation("org.apache.commons:commons-lang3")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
	implementation("io.grpc:grpc-api:$grpcVersion")
	implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-kotlin-stub:1.4.3")
	implementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")
	implementation("com.google.protobuf:protobuf-java-util:$protobufJavaVersion")
	implementation("io.mockk:mockk:$mockkVersion")
	compileOnly("net.devh:grpc-spring-boot-starter:$grpcSpringBootVersion")
    testImplementation("net.devh:grpc-spring-boot-starter:${grpcSpringBootVersion}")
	implementation("org.testcontainers:testcontainers:$testcontainersVersion")
	implementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
	implementation("org.testcontainers:kafka:$testcontainersVersion")
	implementation("org.testcontainers:elasticsearch:$testcontainersVersion")
	implementation("org.testcontainers:mysql:$testcontainersVersion")
	implementation("org.elasticsearch.client:elasticsearch-rest-client:$elasticSearchVersion")
	implementation("co.elastic.clients:elasticsearch-java:$elasticSearchVersion")
	implementation("redis.clients:jedis:$redisVersion")
	implementation("com.github.codemonstur:embedded-redis:1.4.3")

	implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
	implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")
	implementation("io.github.resilience4j:resilience4j-micrometer:2.2.0")

	implementation("com.amazonaws:DynamoDBLocal:$dynamoDBLocalVersion") {
		exclude(group = "org.slf4j", module = "slf4j-api")
	}

	implementation("jakarta.annotation:jakarta.annotation-api")

	testImplementation("com.mysql:mysql-connector-j")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	minHeapSize = "2g"
	maxHeapSize = "15g"
}

tasks.withType<PublishToMavenRepository> {
	mustRunAfter(tasks.named("test"))
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
	withSourcesJar()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.43.2"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.1.0:jdk7@jar"
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

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            
            pom {
                name.set("FIT4J")
                description.set("Functional Integration Testing Library for Java")
                url.set("https://github.com/your-org/fit4j")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("fit4j-contributors")
                        name.set("FIT4J Contributors")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/your-org/fit4j.git")
                    developerConnection.set("scm:git:ssh://github.com:your-org/fit4j.git")
                    url.set("https://github.com/your-org/fit4j")
                }
            }
        }
    }
}