rootProject.name = "library-service-acceptance-tests"

pluginManagement {
    repositories {
        maven {
            name = "Artifactory"
            url = uri("https://pkg.udemy.dev/artifactory/maven")
            credentials {
                username = System.getenv("ART_USER_NAME")
                password = System.getenv("ART_API_KEY")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}