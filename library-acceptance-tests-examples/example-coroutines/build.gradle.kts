val grpcSpringBootVersion : String by project
val udemyProtobufKotlinLibVersion : String by project

dependencies{
   // dependencies for the project in addition to parent project dependencies
   testImplementation("net.devh:grpc-spring-boot-starter:$grpcSpringBootVersion")
   testImplementation("com.udemy.libraries.protobufs:kotlin:$udemyProtobufKotlinLibVersion")

   // dependencies for the project in addition to parent project dependencies
   implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.0"))
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
   testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

   implementation("io.grpc:grpc-protobuf")
   implementation("io.grpc:grpc-stub")
   implementation("io.grpc:grpc-kotlin-stub:1.0.0")
}

