val redisVersion: String by project
dependencies{
   testImplementation("org.testcontainers:testcontainers")
   testImplementation("org.testcontainers:testcontainers-junit-jupiter")
   testImplementation("redis.clients:jedis:$redisVersion")
}
