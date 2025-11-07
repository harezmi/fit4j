val testcontainersVersion : String by project
val elasticSearchVersion: String by project
dependencies{
   // dependencies for the project in addition to parent project dependencies
   testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
   testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
   testImplementation("org.testcontainers:elasticsearch:$testcontainersVersion")
   testImplementation("org.elasticsearch.client:elasticsearch-rest-client:$elasticSearchVersion")
   testImplementation("co.elastic.clients:elasticsearch-java:$elasticSearchVersion")
}

