package dev.matrix.agp.rust

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class RustCleanTask : DefaultTask() {
    @get:Input
    abstract val variantJniLibsDirectory: Property<File>

    @TaskAction
    fun taskAction() {
        val variantJniLibsDirectory = variantJniLibsDirectory.get()

        project.delete {
            delete(variantJniLibsDirectory)
        }
    }
}
