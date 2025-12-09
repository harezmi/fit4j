# Quick Start: Publishing to Maven Central

This is a condensed checklist to get FIT4J published to Maven Central. For detailed instructions, see [PUBLISHING.md](PUBLISHING.md).

## ⚡ Quick Setup Checklist

### 1. Sonatype Account Setup (One-time, ~2 days)

- [ ] Create account at https://issues.sonatype.org/secure/Signup!default.jspa
- [ ] Create JIRA ticket to request `org.fit4j` or `io.github.ksevindik` group ID
- [ ] Wait for approval (1-2 business days)

**⚠️ Important**: If you don't own `fit4j.org` domain, consider using `io.github.ksevindik` group ID instead for faster approval. You'll need to update:
- `gradle.properties`: `group = io.github.ksevindik`
- Users will use: `io.github.ksevindik:fit4j:version`

### 2. GPG Key Setup (One-time, 15 minutes)

```bash
# Install GPG
brew install gnupg  # macOS
# or: sudo apt-get install gnupg  # Linux

# Generate key
gpg --gen-key
# Follow prompts, use strong passphrase

# Get key ID
gpg --list-secret-keys --keyid-format SHORT
# Copy the 8-character key ID (e.g., ABCD1234)

# Publish key to servers
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver pgp.mit.edu --send-keys YOUR_KEY_ID

# Export for Gradle
gpg --export-secret-keys > ~/.gnupg/secring.gpg
```

### 3. Local Credentials (One-time, 2 minutes)

Create/edit `~/.gradle/gradle.properties`:

```properties
# From your Sonatype JIRA account
ossrhUsername=your-sonatype-username
ossrhPassword=your-sonatype-password

# From gpg --list-secret-keys command
signing.keyId=ABCD1234
signing.password=your-gpg-passphrase
signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
```

### 4. GitHub Secrets Setup (One-time, 5 minutes)

Go to your GitHub repository → Settings → Secrets and variables → Actions

Add these repository secrets:

| Secret Name | Value | How to Get |
|-------------|-------|------------|
| `OSSRH_USERNAME` | Your Sonatype username | Same as local |
| `OSSRH_PASSWORD` | Your Sonatype password | Same as local |
| `SIGNING_KEY_ID` | Your GPG key ID | Same as local |
| `SIGNING_PASSWORD` | Your GPG passphrase | Same as local |
| `SIGNING_SECRET_KEY` | Base64 encoded GPG key | See below |

**To encode GPG key for GitHub:**
```bash
gpg --export-secret-keys YOUR_KEY_ID | base64 > gpg-key.txt
# Copy entire contents of gpg-key.txt to SIGNING_SECRET_KEY secret
```

## 🚀 Publishing

### Publish Snapshot (Immediate)

```bash
# Version must end with -SNAPSHOT in gradle.properties
./gradlew publish
```

Available at: https://s01.oss.sonatype.org/content/repositories/snapshots/

### Publish Release (30 minutes manual process)

**Option A: Via GitHub Release (Recommended)**
1. Create GitHub release with tag (e.g., `v1.0.0`)
2. GitHub Actions automatically publishes
3. Login to https://s01.oss.sonatype.org/
4. Find staging repository → Close → Release

**Option B: Manual**
```bash
# 1. Update version in gradle.properties (remove -SNAPSHOT)
version=1.0.0

# 2. Commit and tag
git commit -am "Release 1.0.0"
git tag v1.0.0
git push origin main --tags

# 3. Publish
./gradlew publish

# 4. Login to Sonatype Nexus and manually close/release
# https://s01.oss.sonatype.org/

# 5. Bump to next snapshot
version=1.0.1-SNAPSHOT
git commit -am "Prepare next iteration"
git push
```

## ✅ Verification

```bash
# Check Maven Central Search (wait 2 hours after release)
# https://search.maven.org/search?q=g:org.fit4j

# Or test immediately after release in a project
dependencies {
    testImplementation("org.fit4j:fit4j:1.0.0")
}
```

## 🔥 Common Issues

### "401 Unauthorized" when publishing
- Check `~/.gradle/gradle.properties` credentials
- Verify Sonatype account is not locked
- Ensure you have permission for the group ID

### "Unable to find credentials for signing"
- Check GPG key is installed: `gpg --list-keys`
- Verify `signing.*` properties in gradle.properties
- Re-export secret key: `gpg --export-secret-keys > ~/.gnupg/secring.gpg`

### "Cannot close staging repository" in Nexus
- Click on "Activity" tab to see validation errors
- Common issues:
  - Missing POM metadata (already configured ✅)
  - Missing signatures (already configured ✅)
  - Wrong group ID (check permissions)

### "Release button is disabled"
- Wait for "Close" validation to complete (2-5 minutes)
- Check activity tab for errors

## 📚 More Information

- **Detailed Guide**: [PUBLISHING.md](PUBLISHING.md)
- **Sonatype Guide**: https://central.sonatype.org/publish/publish-guide/
- **Contributing**: [CONTRIBUTING.md](CONTRIBUTING.md)

## 🆘 Need Help?

1. Check [PUBLISHING.md](PUBLISHING.md) for detailed troubleshooting
2. Review [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
3. Search existing issues on GitHub
4. Create a new issue with `[Publishing]` prefix

