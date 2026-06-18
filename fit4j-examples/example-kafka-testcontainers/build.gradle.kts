val protobufJavaVersion : String by project

dependencies{
   testImplementation("org.springframework.kafka:spring-kafka")
   testImplementation("org.springframework.kafka:spring-kafka-test")
   testImplementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")

   testImplementation("org.testcontainers:testcontainers")
   testImplementation("org.testcontainers:testcontainers-junit-jupiter")
   testImplementation("org.testcontainers:testcontainers-kafka")
}
