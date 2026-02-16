package dev.matrix.agp.rust

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class RustCleanTask : DefaultTask() {
    @get:OutputDirectory
    abstract val variantJniLibsDirectory: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val variantJniLibsDirectory = variantJniLibsDirectory.get()

        project.delete {
            delete(variantJniLibsDirectory)
        }
    }
}
