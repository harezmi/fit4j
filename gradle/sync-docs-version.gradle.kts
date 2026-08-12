import java.io.File

val versionToken = Regex("""\d+\.\d+\.\d+(-[A-Za-z0-9.]+)?""")
val fit4jCoordVersion = Regex("""(?<=:fit4j:)\d+\.\d+\.\d+(-[A-Za-z0-9.]+)?""")
val fit4jVersionProp = Regex("""(?<=fit4jVersion=)\d+\.\d+\.\d+(-[A-Za-z0-9.]+)?""")
val mavenFit4jVersion = Regex(
    """(?is)(<artifactId>\s*fit4j\s*</artifactId>\s*<version>)\d+\.\d+\.\d+(-[A-Za-z0-9.]+)?(</version>)"""
)
val mavenVersionOnly = Regex("""<version>\d+\.\d+\.\d+(-[A-Za-z0-9.]+)?</version>""")
val fit4jCoordGroup = Regex("""[^:\\s"']+:fit4j""")
val markerPattern = Regex(
    """<!--(fit4jVersion|fit4jGroup)-->([\s\S]*?)<!--/\1-->"""
)

fun rewriteVersionBlock(body: String, version: String, fileLabel: String): String {
    val trimmed = body.trim()
    if (trimmed.isEmpty()) {
        error("Empty fit4jVersion marker body in $fileLabel")
    }
    if (versionToken.matches(trimmed)) {
        return body.replaceFirst(trimmed, version)
    }
    var out = body
    var changed = false
    val afterCoord = fit4jCoordVersion.replace(out) { version }.also { if (it != out) changed = true }
    out = afterCoord
    val afterProp = fit4jVersionProp.replace(out) { version }.also { if (it != out) changed = true }
    out = afterProp
    if (out.contains("fit4j", ignoreCase = true)) {
        val afterMavenArtifact = mavenFit4jVersion.replace(out) { m ->
            changed = true
            m.groupValues[1] + version + m.groupValues[3]
        }
        out = afterMavenArtifact
        if (!changed) {
            val afterMaven = mavenVersionOnly.replace(out) { "<version>$version</version>" }
            if (afterMaven != out) {
                out = afterMaven
                changed = true
            }
        }
    }
    if (!changed) {
        error("fit4jVersion block in $fileLabel has no recognizable version pattern")
    }
    return out
}

fun rewriteGroupBlock(body: String, group: String, fileLabel: String): String {
    val trimmed = body.trim()
    if (trimmed.isEmpty()) {
        error("Empty fit4jGroup marker body in $fileLabel")
    }
    if (!trimmed.contains(Regex("""\s"""))) {
        return body.replaceFirst(trimmed, group)
    }
    var changed = false
    val out = fit4jCoordGroup.replace(body) {
        changed = true
        "$group:fit4j"
    }
    if (!changed) {
        error("fit4jGroup block in $fileLabel has no recognizable group pattern")
    }
    return out
}

fun syncDocsVersionContent(content: String, version: String, group: String, fileLabel: String): String {
    val opens = Regex("""<!--(fit4jVersion|fit4jGroup)-->""").findAll(content).count()
    val closes = Regex("""<!--/(fit4jVersion|fit4jGroup)-->""").findAll(content).count()
    if (opens != closes) {
        error("Malformed fit4j version/group markers in $fileLabel (open=$opens close=$closes)")
    }

    return markerPattern.replace(content) { match ->
        val tag = match.groupValues[1]
        val body = match.groupValues[2]
        if (markerPattern.containsMatchIn(body)) {
            error("Nested fit4j markers are not supported in $fileLabel")
        }
        val isBlock = body.contains('\n')
        val newBody = when {
            tag == "fit4jVersion" && !isBlock -> {
                if (body.trim().isEmpty()) error("Empty inline fit4jVersion in $fileLabel")
                version
            }
            tag == "fit4jGroup" && !isBlock -> {
                if (body.trim().isEmpty()) error("Empty inline fit4jGroup in $fileLabel")
                group
            }
            tag == "fit4jVersion" && isBlock -> rewriteVersionBlock(body, version, fileLabel)
            tag == "fit4jGroup" && isBlock -> rewriteGroupBlock(body, group, fileLabel)
            else -> error("Unknown marker $tag in $fileLabel")
        }
        "<!--$tag-->$newBody<!--/$tag-->"
    }
}

tasks.register("syncDocsVersion") {
    group = "documentation"
    description =
        "Rewrite allowlisted docs/examples so <!--fit4jVersion--> / <!--fit4jGroup--> markers match gradle.properties"

    doLast {
        val version = project.version.toString()
        val group = project.group.toString()
        val root = rootProject.projectDir
        val files = listOf(
            File(root, "README.md"),
            File(root, "index.html"),
            File(root, "fit4j-examples/gradle.properties"),
            File(root, "fit4j-examples/README.md"),
        )
        files.forEach { file ->
            if (!file.isFile) {
                error("Allowlisted file missing: ${file.path}")
            }
            val original = file.readText()
            val updated = syncDocsVersionContent(original, version, group, file.path)
            if (updated != original) {
                file.writeText(updated)
                logger.lifecycle("Updated ${file.relativeTo(root)}")
            } else {
                logger.lifecycle("Unchanged ${file.relativeTo(root)}")
            }
        }
    }
}
