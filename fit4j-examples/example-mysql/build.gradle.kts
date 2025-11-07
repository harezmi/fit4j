val testcontainersVersion : String by project

dependencies{
   // dependencies for the project in addition to parent project dependencies
   testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
   testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
   testImplementation("org.testcontainers:mysql:$testcontainersVersion")

   testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc")
   testImplementation("com.mysql:mysql-connector-j")
}