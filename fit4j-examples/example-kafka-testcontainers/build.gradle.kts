val protobufJavaVersion : String by project
val testcontainersVersion : String by project

dependencies{
   // dependencies for the project in addition to parent project dependencies
   testImplementation("org.springframework.kafka:spring-kafka")
   testImplementation("org.springframework.kafka:spring-kafka-test")
   testImplementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")

   testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
   testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
   testImplementation("org.testcontainers:kafka:$testcontainersVersion")
}

