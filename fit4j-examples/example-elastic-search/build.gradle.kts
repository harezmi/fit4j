val elasticSearchVersion: String by project
dependencies{
   testImplementation("org.testcontainers:testcontainers")
   testImplementation("org.testcontainers:testcontainers-junit-jupiter")
   testImplementation("org.testcontainers:testcontainers-elasticsearch")
   testImplementation("org.elasticsearch.client:elasticsearch-rest-client:$elasticSearchVersion")
   testImplementation("co.elastic.clients:elasticsearch-java:$elasticSearchVersion")
}
