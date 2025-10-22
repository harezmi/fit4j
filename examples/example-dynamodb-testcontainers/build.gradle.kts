val testcontainersVersion : String by project
val dynamodbVersion : String by project

dependencies{
   // dependencies for the project in addition to parent project dependencies
   testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
   testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")

   testImplementation("com.amazonaws:aws-java-sdk:$dynamodbVersion")
   testImplementation("com.amazonaws:aws-java-sdk-dynamodb:$dynamodbVersion")
}