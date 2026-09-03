# Quick Start: Publishing to Maven Central

This checklist describes the current FIT4J release process through the
[Sonatype Central Portal](https://central.sonatype.com/). For the Gradle publishing configuration, see
[PUBLISHING.md](PUBLISHING.md).

## 1. Configure the Central Portal account and namespace

1. Sign in to the [Central Portal](https://central.sonatype.com/).
2. Register and verify the `io.github.harezmi` namespace under **Namespaces**.
3. Open [Generate User Token](https://central.sonatype.com/usertoken) and create a publishing token.

The generated token username and password are publishing credentials. They are not the credentials used to
sign in to the Central Portal.

The project coordinates are defined in `gradle.properties`:

```properties
group=io.github.harezmi
version=0.1.7
```

Change the version for each release, but do not change the group unless the corresponding Central Portal
namespace has been verified.

## 2. Create and publish a GPG key

Maven Central requires release artifacts to have OpenPGP signatures.

```bash
# macOS
brew install gnupg

# Debian/Ubuntu
# sudo apt-get install gnupg

gpg --full-generate-key
gpg --list-secret-keys --keyid-format LONG
```

Use a strong passphrase. From the listing, copy the full fingerprint of the primary key. It is the long value
on the line below `sec`, for example:

```text
sec   rsa3072/1234567890ABCDEF 2026-01-01 [SC]
      0123456789ABCDEF0123456789ABCDEF01234567
```

Publish that public key to `keys.openpgp.org`, which is supported by Maven Central:

```bash
gpg --keyserver keys.openpgp.org \
    --send-keys 0123456789ABCDEF0123456789ABCDEF01234567
```

After the upload, complete the email-address verification sent by `keys.openpgp.org`. Until an address is
verified, the server may distribute the public key without its user ID (name and email address). You only need
to publish the public key; never upload or share the private key.

Do not export a `~/.gnupg/secring.gpg` file. FIT4J uses Gradle's `useGpgCmd()` integration, which invokes the
installed GPG 2.x command and uses its normal key store.

## 3. Configure local credentials

Create or edit the user-level Gradle properties file at `~/.gradle/gradle.properties`. Do not put secrets in
the repository's `gradle.properties` file.

```properties
mavenCentralUsername=your-token-username
mavenCentralPassword=your-token-password

# Use the absolute path so Gradle daemons started outside a terminal can find Homebrew's GPG.
signing.gnupg.executable=/opt/homebrew/bin/gpg
signing.gnupg.keyName=0123456789ABCDEF0123456789ABCDEF01234567
```

On Intel macOS, Linux, or another installation layout, obtain the executable path with `command -v gpg` and
use that value for `signing.gnupg.executable`.

By default, GPG can obtain the key passphrase through `gpg-agent`. Test that interaction first:

```bash
printf 'signing test\n' | gpg --clearsign
```

If non-interactive signing is required, the passphrase may instead be added to the same user-level properties
file:

```properties
signing.gnupg.passphrase=your-gpg-key-passphrase
```

This stores the passphrase as plain text. Restrict access to the file:

```bash
chmod 600 ~/.gradle/gradle.properties
```

The following legacy properties belong to Gradle's key-ring-file signing mode and must not be used with this
project's `useGpgCmd()` configuration:

```properties
# Do not use these for FIT4J:
# signing.keyId=...
# signing.password=...
# signing.secretKeyRingFile=.../secring.gpg
```

## 4. Verify signing locally

After changing GPG configuration, stop existing Gradle daemons so they reload their environment and
properties:

```bash
./gradlew --stop
./gradlew signMavenPublication --stacktrace
find build -name '*.asc'
```

The signing task should create detached `.asc` signatures for the main artifact, sources, Javadoc, Gradle
module metadata, and POM.

Avoid `--debug` when working with publishing secrets because debug logs can expose sensitive values.

## 5. Publish a snapshot

Set a version ending in `-SNAPSHOT` in `gradle.properties`, sync the documentation, and publish:

```properties
version=0.1.8-SNAPSHOT
```

```bash
./gradlew syncDocsVersion
./gradlew publishToMavenCentral --no-configuration-cache
```

Central Portal snapshots are available from:

```text
https://central.sonatype.com/repository/maven-snapshots/
```

Signing is not required by Central for snapshots, but this project's configured signing tasks will sign them.

## 6. Publish a release

1. Set a non-snapshot version in `gradle.properties`, such as `version=0.1.8`.
2. Run `./gradlew syncDocsVersion` and review the version changes.
3. Run the tests and local signing check.
4. Commit the version and synchronized documentation together.
5. Publish the deployment.

```bash
./gradlew clean check signMavenPublication
./gradlew publishToMavenCentral --no-configuration-cache
```

The build currently calls `publishToMavenCentral(true)`, so the plugin automatically publishes the deployment
after Central Portal validation succeeds. A released Maven Central version is immutable; publish a new version
to correct a release.

After releasing, set the next development version ending in `-SNAPSHOT` and run `./gradlew syncDocsVersion`
again.

## 7. Verify a release

Allow time for Maven Central to synchronize, then check
[Maven Central Search](https://central.sonatype.com/search?q=io.github.harezmi%3Afit4j) or test the dependency:

```kotlin
dependencies {
    testImplementation("io.github.harezmi:fit4j:0.1.8")
}
```

## Troubleshooting

### GPG process cannot be started

Set `signing.gnupg.executable` to the absolute result of `command -v gpg`, then run `./gradlew --stop` before
retrying.

### GPG reports that no secret key exists

- Check that the primary key appears in `gpg --list-secret-keys --keyid-format LONG`.
- Ensure `signing.gnupg.keyName` is the full primary-key fingerprint, without spaces.
- Check that Gradle and the terminal use the same GPG home directory.

### GPG cannot read the passphrase

- Verify interactive signing with `printf 'test\n' | gpg --clearsign`.
- Make sure `gpg-agent` and the platform's pinentry program are working.
- For non-interactive local use, set `signing.gnupg.passphrase` in `~/.gradle/gradle.properties`.

### 401 Unauthorized when publishing

- Use the username and password generated at the Central Portal's **Generate User Token** page, not account
  login credentials.
- Generate a new token if the old one was revoked or expired.
- Confirm that `io.github.harezmi` is verified for the publishing account.

### Central Portal validation fails

Open the deployment in the [Central Portal](https://central.sonatype.com/publishing/deployments) and inspect its
validation messages. Common causes are missing signatures, unavailable public keys, incomplete POM metadata,
or coordinates outside the verified namespace.

## More information

- [Vanniktech plugin: Maven Central](https://vanniktech.github.io/gradle-maven-publish-plugin/central/)
- [Maven Central publishing requirements](https://central.sonatype.org/publish/requirements/)
- [Maven Central GPG signing guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Gradle Signing Plugin: using GPG Agent](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:using_gpg_agent)
