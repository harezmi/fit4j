val grpcSpringBootVersion : String by project

dependencies{
    // dependencies for the project in addition to parent project dependencies
    testImplementation("net.devh:grpc-spring-boot-starter:$grpcSpringBootVersion")
}
