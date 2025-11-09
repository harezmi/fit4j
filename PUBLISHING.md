# Publishing FIT4J to Maven Central

This document explains how to configure Gradle to publish the **FIT4J** library to Maven Central using the 
[Vanniktech Gradle Maven Publish Plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/central/).

---

## 1. Apply Required Plugins

Add the following plugins to your `build.gradle.kts`:

```kotlin
plugins {
    id("signing")
    id("com.vanniktech.maven.publish") version "0.34.0"
}
```

---

## 2. Configure Maven Publishing

### Enable publishing to Maven Central and signing

```kotlin
mavenPublishing {
    publishToMavenCentral(true)
    signAllPublications()
}
```

### Set project coordinates and POM metadata

```kotlin
mavenPublishing {
    coordinates("\${project.group}", "\${project.name}", "\${project.version}")

    pom {
        name.set("FIT4J")
        description.set("Functional Integration Testing Library for Java and Kotlin microservices")
        inceptionYear.set("2025")
        url.set("https://github.com/harezmi/fit4j")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("harezmi")
                name.set("Kenan Sevindik")
                email.set("ksevindik@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/harezmi/fit4j.git")
            developerConnection.set("scm:git:ssh://github.com:harezmi/fit4j.git")
            url.set("https://github.com/harezmi/fit4j")
        }
    }
}
```

---

## 3. Configure Signing

```kotlin
signing {
    useGpgCmd()
    sign(publishing.publications)
}

val signingTasks: TaskCollection<Sign> = tasks.withType<Sign>()
tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(signingTasks)
}
```

Set your GPG signing configuration:

```kotlin
signing.gnupg.executable = "gpg"
signing.gnupg.keyName = "<key id>"
signing.gnupg.passphrase = "<pgp keystore pass>"
signing.secretKeyRingFile = "/Users/<username>/.gnupg/secring.gpg"
```

Export your secret key:

```bash
gpg --export-secret-keys --output ~/.gnupg/secring.gpg --armor <key id>
```

---

## 4. Set Maven Central Credentials

Add your Sonatype credentials:

```kotlin
mavenCentralUsername = "<token username>"
mavenCentralPassword = "<token password>"
```

They are obtained from https://central.sonatype.com/usertoken by creating a new user token.

---

## 5. Publishing Task

Run the publishing task with:

```bash
./gradlew publishToMavenCentral --no-configuration-cache
```

---

## 6. Configure Repositories for Dependencies

To use Maven Central or Sonatype snapshots in your project:

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "sonatypeSnapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}
```
