val dynamodbVersion : String by project

dependencies{
   testImplementation("org.testcontainers:testcontainers")
   testImplementation("org.testcontainers:testcontainers-junit-jupiter")

   testImplementation("com.amazonaws:aws-java-sdk:$dynamodbVersion")
   testImplementation("com.amazonaws:aws-java-sdk-dynamodb:$dynamodbVersion")
}
