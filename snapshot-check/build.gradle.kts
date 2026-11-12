import java.util.Properties

/**
 * Smoke-check that the published FIT4J snapshot is resolvable from Central Portal.
 *
 * From the repository root:
 *
 *   ./gradlew -p snapshot-check verifySnapshot --refresh-dependencies
 *
 * Optional version override:
 *
 *   ./gradlew -p snapshot-check verifySnapshot -Pfit4jVersion=1.0.0-SNAPSHOT
 */

plugins {
    java
}

val repoProps = Properties().apply {
    rootProject.file("../gradle.properties").inputStream().use { load(it) }
}

val fit4jGroup = repoProps.getProperty("group", "io.github.harezmi").trim()
val fit4jVersion = (findProperty("fit4jVersion") as String?)
    ?: repoProps.getProperty("version", "1.0.0-SNAPSHOT").trim()
val fit4jCoordinates = "$fit4jGroup:fit4j:$fit4jVersion"

repositories {
    mavenCentral()
    maven {
        name = "centralPortalSnapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}

dependencies {
    implementation(fit4jCoordinates)
}

tasks.register("verifySnapshot") {
    group = "verification"
    description = "Resolves io.github.harezmi:fit4j (snapshot) and prints the JAR path"
    doLast {
        val config = configurations.getByName("runtimeClasspath")
        val fit4jJars = config.files.filter { it.name.startsWith("fit4j-") && it.extension == "jar" }
        if (fit4jJars.isEmpty()) {
            throw GradleException("Could not resolve $fit4jCoordinates from Maven Central snapshots")
        }
        println("Resolved $fit4jCoordinates:")
        fit4jJars.forEach { println("  - ${it.absolutePath}") }
    }
}
