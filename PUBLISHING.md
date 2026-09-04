# Publishing FIT4J to Maven Central

FIT4J publishes through the [Sonatype Central Portal](https://central.sonatype.com/) using the
[Vanniktech Gradle Maven Publish Plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/central/).
For account, namespace, token, and GPG key setup, follow
[MAVEN_CENTRAL_SETUP.md](MAVEN_CENTRAL_SETUP.md).

## Publishing configuration

The root `build.gradle.kts` applies the required plugins:

```kotlin
plugins {
    id("signing")
    id("com.vanniktech.maven.publish") version "0.34.0"
}
```

Maven Central publishing and signing are enabled with:

```kotlin
mavenPublishing {
    publishToMavenCentral(true)
    signAllPublications()
}
```

Passing `true` enables automatic publication after Central Portal validation succeeds.

The published coordinates come from the root `gradle.properties` file:

```properties
group=io.github.harezmi
version=0.1.7
```

The publication uses the project group, name, and version:

```kotlin
mavenPublishing {
    coordinates("${project.group}", "${project.name}", "${project.version}")

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

## Artifact signing

FIT4J uses the installed GPG 2.x command and its normal key store:

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

Put the local signing configuration in `~/.gradle/gradle.properties`:

```properties
signing.gnupg.executable=/absolute/path/from/command-v-gpg
signing.gnupg.keyName=your-full-primary-key-fingerprint

# Optional when the passphrase is not supplied by gpg-agent:
# signing.gnupg.passphrase=your-gpg-key-passphrase
```

For an Apple Silicon Homebrew installation, the executable is usually:

```properties
signing.gnupg.executable=/opt/homebrew/bin/gpg
```

Publish the public key to `keys.openpgp.org` and complete its email verification:

```bash
gpg --keyserver keys.openpgp.org \
    --send-keys YOUR_FULL_PRIMARY_KEY_FINGERPRINT
```

Do not create a legacy `secring.gpg` file. The `signing.keyId`, `signing.password`, and
`signing.secretKeyRingFile` properties configure a different Gradle signing mode and are not used with
FIT4J's `useGpgCmd()` setup.

Verify signing before publishing:

```bash
./gradlew --stop
./gradlew signMavenPublication --stacktrace
find build -name '*.asc'
```

The task signs the main JAR, sources JAR, Javadoc JAR, Gradle module metadata, and POM. Avoid running publishing
tasks with `--debug`, because debug output can expose sensitive values.

## Maven Central credentials

Generate a user token at [Central Portal User Token](https://central.sonatype.com/usertoken), then place its
generated username and password in `~/.gradle/gradle.properties`:

```properties
mavenCentralUsername=your-token-username
mavenCentralPassword=your-token-password
```

These are token credentials, not the username and password used to sign in to the Central Portal. Never put
them in the repository's `gradle.properties` file.

## Publish a snapshot

Use a version ending in `-SNAPSHOT`:

```properties
version=0.1.8-SNAPSHOT
```

Synchronize the versioned documentation and publish:

```bash
./gradlew syncDocsVersion
```

```bash
./gradlew publishToMavenCentral --no-configuration-cache
```

Consumers can resolve snapshots from the Central Portal snapshot repository:

```kotlin
repositories {
    maven {
        name = "centralPortalSnapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}
```

## Publish a release

1. Set a non-snapshot `version` in `gradle.properties`.
2. Run `./gradlew syncDocsVersion` and review changes to the README, website, and examples.
3. Run the build and local signing check.
4. Commit the version and synchronized documentation together.
5. Publish to Maven Central.

```bash
./gradlew clean check signMavenPublication
```

```bash
./gradlew publishToMavenCentral --no-configuration-cache
```

Because automatic publication is configured, the plugin submits the validated deployment for release. Maven
Central releases are immutable; corrections require a new version.

After the release, set the next `-SNAPSHOT` version and run `./gradlew syncDocsVersion` again.

## Consume FIT4J

Released versions are available from Maven Central:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.harezmi:fit4j:<version>")
}
```

## References

- [FIT4J Maven Central setup](MAVEN_CENTRAL_SETUP.md)
- [Vanniktech plugin: Maven Central](https://vanniktech.github.io/gradle-maven-publish-plugin/central/)
- [Maven Central publishing requirements](https://central.sonatype.org/publish/requirements/)
- [Maven Central GPG signing guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Gradle Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)
