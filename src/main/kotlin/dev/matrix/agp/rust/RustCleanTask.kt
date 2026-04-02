package dev.matrix.agp.rust

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal abstract class RustCleanTask : DefaultTask() {
    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:OutputDirectory
    abstract val variantJniLibsDirectory: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val variantJniLibsDirectory = variantJniLibsDirectory.get()

        fileSystemOperations.delete {
            delete(variantJniLibsDirectory)
        }
    }
}
