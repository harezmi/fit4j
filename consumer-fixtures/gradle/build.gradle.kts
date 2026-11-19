plugins {
    kotlin("jvm") version "2.3.21" apply false
}

subprojects {
    apply(plugin = "java")

    dependencies {
        "testImplementation"("io.github.harezmi:fit4j:1.0.0-SNAPSHOT")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test:4.1.0")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
