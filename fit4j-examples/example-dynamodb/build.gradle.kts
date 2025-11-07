val dynamodbVersion : String by project

dependencies{
   // dependencies for the project in addition to parent project dependencies
   testImplementation("com.amazonaws:aws-java-sdk:$dynamodbVersion")
   testImplementation("com.amazonaws:aws-java-sdk-dynamodb:$dynamodbVersion")
}

