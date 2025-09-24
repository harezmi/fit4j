val protobufJavaVersion : String by project

dependencies{
    // dependencies for the project in addition to parent project dependencies
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")
}
