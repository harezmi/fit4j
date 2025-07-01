val udemyExpPlatformVersion : String by project
dependencies{
   // dependencies for the project in addition to parent project dependencies
   testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
   testImplementation("com.udemy.libraries.exp:exp-platform-sdk:$udemyExpPlatformVersion")
   testImplementation("com.udemy.libraries.exp:exp-platform-sdk-spring-boot-starter:$udemyExpPlatformVersion")
}

