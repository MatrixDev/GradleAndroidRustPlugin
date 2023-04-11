package dev.matrix.agp.rust

import dev.matrix.agp.rust.utils.*
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

internal fun installRustComponentsIfNeeded(
    project: Project,
    minimalVersion: SemanticVersion?,
    abiSet: Collection<Abi>,
) {
    if (Os.current.isWindows) {
        return
    }

    if (minimalVersion != null && minimalVersion.isValid) {
        val actualVersion = readRustCompilerVersion(project)
        if (actualVersion < minimalVersion) {
            installRustUp(project)
            updateRust(project)
        }
    }

    if (abiSet.isNotEmpty()) {
        installRustUp(project)

        val installedAbiSet = readRustUpInstalledTargets(project)
        for (abi in abiSet) {
            if (installedAbiSet.contains(abi)) {
                continue
            }
            installRustTarget(project, abi)
        }
    }
}

private fun installRustUp(project: Project) {
    try {
        val result = project.exec {
            standardOutput = NullOutputStream
            errorOutput = NullOutputStream
            executable("rustup")
            args("-V")
        }

        if (result.exitValue == 0) {
            return
        }
    } catch (ignored: Exception) {
    }

    log("installing rustup")

    project.exec {
        standardOutput = NullOutputStream
        errorOutput = NullOutputStream
        commandLine("bash", "-c", "\"curl\" --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y")
    }.assertNormalExitValue()
}

private fun updateRust(project: Project) {
    log("updating rust version")

    project.exec {
        standardOutput = NullOutputStream
        errorOutput = NullOutputStream
        executable("rustup")
        args("update")
    }.assertNormalExitValue()
}

private fun installRustTarget(project: Project, abi: Abi) {
    log("installing rust target $abi (${abi.rustTargetTriple})")

    project.exec {
        standardOutput = NullOutputStream
        errorOutput = NullOutputStream
        executable("rustup")
        args("target", "add", abi.rustTargetTriple)
    }.assertNormalExitValue()
}

private fun readRustCompilerVersion(project: Project): SemanticVersion {
    val output = ByteArrayOutputStream()
    project.exec {
        standardOutput = output
        errorOutput = NullOutputStream
        executable("rustc")
        args("--version")
    }.assertNormalExitValue()

    val outputText = String(output.toByteArray())
    val regex = Regex("^rustc (\\d+\\.\\d+\\.\\d+)(-nightly)? .*$", RegexOption.DOT_MATCHES_ALL)
    val match = checkNotNull(regex.matchEntire(outputText)) {
        "failed to parse rust compiler version: $outputText"
    }

    return SemanticVersion(match.groupValues[1])
}

private fun readRustUpInstalledTargets(project: Project): Set<Abi> {
    val output = ByteArrayOutputStream()
    project.exec {
        standardOutput = output
        errorOutput = NullOutputStream
        executable("rustup")
        args("target", "list")
    }.assertNormalExitValue()

    val regex = Regex("^(\\S+) \\(installed\\)$", RegexOption.MULTILINE)
    return regex.findAll(String(output.toByteArray()))
        .mapNotNull { target ->
            Abi.values().find { it.rustTargetTriple == target.groupValues[1] }
        }
        .toSet()
}
