import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.security.MessageDigest


plugins {
	`java-library`
	kotlin("jvm")
	kotlin("plugin.spring")
    id("com.google.protobuf") version "0.9.6"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.2"
    id("signing")
    id("com.vanniktech.maven.publish") version "0.34.0"
}

apiValidation {
	// Public for Spring discovery, but explicitly outside the supported consumer API.
	ignoredPackages.add("org.fit4j.autoconfigure")
	ignoredPackages.add("org.fit4j.context")
	ignoredPackages.add("org.fit4j.expression")
	ignoredPackages.add("org.fit4j.scope")
	ignoredClasses.addAll(
		listOf(
			"org.fit4j.grpc.DefaultGrpcMockResponseProvider",
			"org.fit4j.grpc.GrpcClassScanner",
			"org.fit4j.grpc.GrpcResponseBuilderRegistry",
			"org.fit4j.grpc.GrpcTestFixtureBuilder",
			"org.fit4j.grpc.GrpcTypeDescriptorsProvider",
			"org.fit4j.grpc.JsonToGrpcResponseConverter",
			"org.fit4j.grpc.TestGrpcServiceConfigurer",
			"org.fit4j.grpc.TestGrpcServiceDefinition",
			"org.fit4j.grpc.TestGrpcServiceDefinitionProvider",
			"org.fit4j.helper.BrowserLauncher",
			"org.fit4j.helper.ClassScanner",
			"org.fit4j.helper.GrpcClasspath",
			"org.fit4j.http.DefaultHttpMockResponseProvider",
			"org.fit4j.http.HttpHeadersRegisteringRequestInterceptor",
			"org.fit4j.http.HttpServerDispatcher",
			"org.fit4j.http.HttpServerWrapper",
			"org.fit4j.http.HttpTestFixtureBuilder",
			"org.fit4j.http.JsonToHttpResponseConverter",
			"org.fit4j.http.MockWebServerProperties",
			"org.fit4j.kafka.KafkaListenerContainerFactoryTrackingBeanPostProcessor",
			"org.fit4j.kafka.KafkaMessageTrackingProducerListener",
			"org.fit4j.kafka.KafkaMessageTrackingRecordInterceptor",
			"org.fit4j.kafka.KafkaTemplateTrackingBeanPostProcessor",
			"org.fit4j.kafka.KafkaTestEnabledCondition",
			"org.fit4j.kafka.TestKafkaConsumerDefinitionProvider",
			"org.fit4j.kafka.TestMessageListener",
			"org.fit4j.kafka.TopicNameExpressionResolver",
			"org.fit4j.testcontainers.ContainerMapExpressionResolver",
			"org.fit4j.testcontainers.NetworkFaultPropertySourceFactory",
			"org.fit4j.testcontainers.NetworkFaultRegistrar",
			"org.fit4j.testcontainers.NetworkFaultUpstreamPortResolver",
			"org.fit4j.testcontainers.ProxiedTargetParser",
			"org.fit4j.testcontainers.TestContainerDefinitionRegistrar",
			"org.fit4j.testcontainers.TestContainerResourcePaths",
			"org.fit4j.testcontainers.TestContainersDefinitionProvider",
			"org.fit4j.testcontainers.ToxiproxyBootstrap",
		)
	)
}

tasks.named("check") {
	dependsOn("apiCheck")
}

val javaToolchainVersion: String by project
val javaBytecodeVersion: String by project
val protobufJavaVersion: String by project
val springGrpcVersion: String by project // unused; gRPC starters ship with Boot 4.1
val grpcVersion: String by project
val grpcKotlinStubVersion: String by project
val testJavaVersion: Provider<Int> = providers.gradleProperty("testJavaVersion")
	.orElse(javaToolchainVersion)
	.map(String::toInt)

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
	val springKafkaVersion : String by project
	val kotlinVersion : String by project
	val mockkVersion : String by project
	val protobufJavaVersion : String by project
	val dynamoDBLocalVersion : String by project
	val redisVersion: String by project
	val h2Version: String by project
	val byteBuddyVersion: String by project
	val snakeYamlVersion: String by project
	val commonsLangVersion: String by project
	val jacksonVersion: String by project
	val testcontainersVersion: String by project
	val elasticsearchVersion: String by project
	val embeddedRedisVersion: String by project
	val embeddedPostgresVersion: String by project
	val jakartaAnnotationVersion: String by project

	compileOnly(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
	testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
	implementation("org.springframework.boot:spring-boot-starter-jdbc:$springBootVersion")
	compileOnly("org.springframework.boot:spring-boot-starter-grpc-server")
	compileOnly("org.springframework.boot:spring-boot-starter-grpc-client")
	compileOnly("io.grpc:grpc-inprocess")
	compileOnly("io.grpc:grpc-api")
	compileOnly("io.grpc:grpc-stub")
	testImplementation("io.grpc:grpc-kotlin-stub")
	api("org.springframework.boot:spring-boot-test:$springBootVersion")
	api("org.springframework.boot:spring-boot-restclient:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-resttestclient:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-kafka:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-jackson:$springBootVersion")
	implementation("com.h2database:h2:$h2Version")
	implementation("org.springframework.kafka:spring-kafka-test:$springKafkaVersion")
	api("org.springframework.kafka:spring-kafka:$springKafkaVersion")
	implementation("net.bytebuddy:byte-buddy:$byteBuddyVersion")
	implementation("org.yaml:snakeyaml:$snakeYamlVersion")
	implementation("org.apache.commons:commons-lang3:$commonsLangVersion")
	api("tools.jackson.module:jackson-module-kotlin:$jacksonVersion")
	api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
	api("com.google.protobuf:protobuf-java:$protobufJavaVersion")
	api("com.google.protobuf:protobuf-java-util:$protobufJavaVersion")
	testImplementation("io.mockk:mockk:$mockkVersion")
	testImplementation("org.springframework.boot:spring-boot-starter-grpc-server")
	testImplementation("org.springframework.boot:spring-boot-starter-grpc-client")
	testImplementation("org.springframework.boot:spring-boot-starter-grpc-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-grpc-client-test")
	api("org.testcontainers:testcontainers:$testcontainersVersion")
	api("org.testcontainers:testcontainers-junit-jupiter:$testcontainersVersion")
	implementation("org.testcontainers:testcontainers-kafka:$testcontainersVersion")
	api("org.testcontainers:testcontainers-elasticsearch:$testcontainersVersion")
	implementation("org.testcontainers:testcontainers-mysql:$testcontainersVersion")
	implementation("org.testcontainers:testcontainers-postgresql:$testcontainersVersion")
	api("org.testcontainers:testcontainers-toxiproxy:$testcontainersVersion")
	api("co.elastic.clients:elasticsearch-java:$elasticsearchVersion")
	api("redis.clients:jedis:$redisVersion")
	implementation("com.github.codemonstur:embedded-redis:$embeddedRedisVersion")

	implementation("com.amazonaws:DynamoDBLocal:$dynamoDBLocalVersion") {
		exclude(group = "org.slf4j", module = "slf4j-api")
	}

	implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationVersion")

    api("io.zonky.test:embedded-postgres:$embeddedPostgresVersion")

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
	javaLauncher.set(javaToolchains.launcherFor {
		languageVersion.set(testJavaVersion.map(JavaLanguageVersion::of))
	})
	minHeapSize = "1g"
	maxHeapSize = "5g"
	maxParallelForks = 8
	jvmArgs("--enable-native-access=ALL-UNNAMED")
	// DynamoDBLocal embeds an older shaded kotlin.reflect; prefer Boot/Kotlin BOM jars first.
	val unresolvedClasspath = classpath
	classpath = objects.fileCollection().from(
		unresolvedClasspath.elements.map { elements ->
			elements.map { it.asFile }.sortedWith(
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
		}
	)
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
