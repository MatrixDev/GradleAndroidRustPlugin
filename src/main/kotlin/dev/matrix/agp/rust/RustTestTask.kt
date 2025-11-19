package dev.matrix.agp.rust

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

internal abstract class RustTestTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val rustProjectDirectory: Property<File>

    @get:Input
    abstract val cargoTargetDirectory: Property<File>

    @TaskAction
    fun taskAction() {
        val rustProjectDirectory = rustProjectDirectory.get()
        val cargoTargetDirectory = cargoTargetDirectory.get()

        execOperations.exec {
            standardOutput = System.out
            errorOutput = System.out
            workingDir = rustProjectDirectory

            environment("CARGO_TARGET_DIR", cargoTargetDirectory.absolutePath)

            commandLine("cargo")

            args("test")
        }.assertNormalExitValue()
    }
}
