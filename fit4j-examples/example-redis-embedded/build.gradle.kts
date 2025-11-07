val redisVersion: String by project
dependencies{
   // dependencies for the project in addition to parent project dependencies
   testImplementation("redis.clients:jedis:$redisVersion")
}

