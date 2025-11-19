package dev.matrix.agp.rust

import dev.matrix.agp.rust.utils.Abi
import dev.matrix.agp.rust.utils.NullOutputStream
import dev.matrix.agp.rust.utils.Os
import dev.matrix.agp.rust.utils.RustBinaries
import dev.matrix.agp.rust.utils.SemanticVersion
import dev.matrix.agp.rust.utils.log
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream

internal fun installRustComponentsIfNeeded(
    project: Project,
    execOperations: ExecOperations,
    minimalVersion: SemanticVersion?,
    abiSet: Collection<Abi>,
    rustBinaries: RustBinaries,
) {
    if (Os.current.isWindows) {
        return
    }

    if (minimalVersion != null && minimalVersion.isValid) {
        val actualVersion = readRustCompilerVersion(execOperations, rustBinaries)
        if (actualVersion < minimalVersion) {
            installRustUp(execOperations, rustBinaries)
            updateRust(execOperations, rustBinaries)
        }
    }

    if (abiSet.isNotEmpty()) {
        installRustUp(execOperations, rustBinaries)

        val installedAbiSet = readRustUpInstalledTargets(execOperations, rustBinaries)
        for (abi in abiSet) {
            if (installedAbiSet.contains(abi)) {
                continue
            }
            installRustTarget(execOperations, abi, rustBinaries)
        }
    }
}

private fun installRustUp(execOperations: ExecOperations, rustBinaries: RustBinaries) {
    try {
        val result = execOperations.exec {
            standardOutput = NullOutputStream
            errorOutput = NullOutputStream
            executable(rustBinaries.rustup)
            args("-V")
        }

        if (result.exitValue == 0) {
            return
        }
    } catch (ignored: Exception) {
    }

    log("installing rustup")

    execOperations.exec {
        standardOutput = NullOutputStream
        errorOutput = NullOutputStream
        commandLine("bash", "-c", "\"curl\" --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y")
    }.assertNormalExitValue()
}

private fun updateRust(execOperations: ExecOperations, rustBinaries: RustBinaries) {
    log("updating rust version")

    execOperations.exec {
        standardOutput = NullOutputStream
        errorOutput = NullOutputStream
        executable(rustBinaries.rustup)
        args("update")
    }.assertNormalExitValue()
}

private fun installRustTarget(execOperations: ExecOperations, abi: Abi, rustBinaries: RustBinaries) {
    log("installing rust target $abi (${abi.rustTargetTriple})")

    execOperations.exec {
        standardOutput = NullOutputStream
        errorOutput = NullOutputStream
        executable(rustBinaries.rustup)
        args("target", "add", abi.rustTargetTriple)
    }.assertNormalExitValue()
}

private fun readRustCompilerVersion(execOperations: ExecOperations, rustBinaries: RustBinaries): SemanticVersion {
    val output = ByteArrayOutputStream()
    execOperations.exec {
        standardOutput = output
        errorOutput = NullOutputStream
        executable(rustBinaries.rustc)
        args("--version")
    }.assertNormalExitValue()

    val outputText = String(output.toByteArray())
    val regex = Regex("^rustc (\\d+\\.\\d+\\.\\d+)(-nightly)? .*$", RegexOption.DOT_MATCHES_ALL)
    val match = checkNotNull(regex.matchEntire(outputText)) {
        "failed to parse rust compiler version: $outputText"
    }

    return SemanticVersion(match.groupValues[1])
}

private fun readRustUpInstalledTargets(execOperations: ExecOperations, rustBinaries: RustBinaries): Set<Abi> {
    val output = ByteArrayOutputStream()
    execOperations.exec {
        standardOutput = output
        errorOutput = NullOutputStream
        executable(rustBinaries.rustup)
        args("target", "list")
    }.assertNormalExitValue()

    val regex = Regex("^(\\S+) \\(installed\\)$", RegexOption.MULTILINE)
    return regex.findAll(String(output.toByteArray()))
        .mapNotNull { target ->
            Abi.values().find { it.rustTargetTriple == target.groupValues[1] }
        }
        .toSet()
}
