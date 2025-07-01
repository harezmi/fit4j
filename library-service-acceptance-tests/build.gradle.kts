import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`java-library`
	id("com.udemy.gradle.plugins.publish") version "2.1.1"
	kotlin("jvm") version "2.0.20"
	kotlin("plugin.spring") version "2.0.20"
}

repositories {
	mavenCentral()
	maven(url = "https://packages.confluent.io/maven/")
	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/udemy/packages-repo")
		credentials {
			username = System.getenv("GITHUB_PACKAGES_USERNAME")
			password = System.getenv("GITHUB_PACKAGES_TOKEN")
		}
	}
}

dependencies {
	val springBootVersion : String by project
	val kotlinVersion : String by project
	val testcontainersVersion : String by project
	val grpcSpringBootVersion : String by project
	val mockkVersion : String by project
	val protobufJavaVersion : String by project
	val dynamoDBLocalVersion : String by project
	val udemySASVersion : String by project
	val udemyProtobufKotlinVersion : String by project
	val grpcVersion : String by project
	val elasticSearchVersion: String by project
	val redisVersion: String by project
	val apacheAvroVersion: String by project
	val udemyEventTrackerVersion: String by project
	val udemyExpPlatformVersion: String by project
	val udemyRequestContextVersion: String by project

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
	implementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")
	implementation("com.google.protobuf:protobuf-java-util:$protobufJavaVersion")
	implementation("io.mockk:mockk:$mockkVersion")
	implementation("net.devh:grpc-spring-boot-starter:$grpcSpringBootVersion")
	implementation("com.squareup.okhttp3:mockwebserver")
	implementation("org.testcontainers:testcontainers:$testcontainersVersion")
	implementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
	implementation("org.testcontainers:kafka:$testcontainersVersion")
	implementation("org.testcontainers:elasticsearch:$testcontainersVersion")
	implementation("org.testcontainers:mysql:$testcontainersVersion")
	implementation("org.elasticsearch.client:elasticsearch-rest-client:$elasticSearchVersion")
	implementation("co.elastic.clients:elasticsearch-java:$elasticSearchVersion")
	implementation("redis.clients:jedis:$redisVersion")
	implementation("org.apache.avro:avro:$apacheAvroVersion")
	implementation("com.github.codemonstur:embedded-redis:1.4.3")

	implementation("com.udemy.libraries.protobufs:kotlin:$udemyProtobufKotlinVersion") //required for MoneyConverter
	implementation("com.udemy.libraries.sas:sas-core:$udemySASVersion") //required for JWTProvider defined in TestHttpConfig
	implementation("com.udemy.libraries.eventtracking:eventtracker:$udemyEventTrackerVersion")

	implementation("com.udemy.libraries.exp:exp-platform-sdk:$udemyExpPlatformVersion")
	implementation("com.udemy.libraries.exp:exp-platform-sdk-spring-boot-starter:$udemyExpPlatformVersion")
	implementation("com.udemy.libraries.requestcontext:spring:$udemyRequestContextVersion")

	implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
	implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")
	implementation("io.github.resilience4j:resilience4j-micrometer:2.2.0")

	implementation("com.amazonaws:DynamoDBLocal:$dynamoDBLocalVersion") {
		exclude(group = "org.slf4j", module = "slf4j-api")
	}

	implementation("jakarta.annotation:jakarta.annotation-api")

	testImplementation("com.mysql:mysql-connector-j")
	testImplementation("com.udemy.eventtracking.events:chatresponsegenerated:102.0.0-release")
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