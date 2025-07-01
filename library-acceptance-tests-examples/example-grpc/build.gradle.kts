val grpcSpringBootVersion : String by project
val udemyProtobufKotlinLibVersion : String by project
val udemyRequestContextVersion : String by project

dependencies{
    // dependencies for the project in addition to parent project dependencies
    testImplementation("net.devh:grpc-spring-boot-starter:$grpcSpringBootVersion")
    testImplementation("com.udemy.libraries.protobufs:kotlin:$udemyProtobufKotlinLibVersion")

    // the below requestcontext dependency is only required if you are going to populate
    // the RequestContextProvider with the service context in the test
    implementation("com.udemy.libraries.requestcontext:spring:$udemyRequestContextVersion")

}
