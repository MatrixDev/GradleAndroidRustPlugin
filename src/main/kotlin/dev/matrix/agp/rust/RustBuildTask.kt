package dev.matrix.agp.rust

import dev.matrix.agp.rust.utils.Abi
import dev.matrix.agp.rust.utils.RustBinaries
import dev.matrix.agp.rust.utils.SemanticVersion
import dev.matrix.agp.rust.utils.log
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

internal abstract class RustBuildTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val rustBinaries: Property<RustBinaries>

    @get:Input
    abstract val abi: Property<Abi>

    @get:Input
    abstract val apiLevel: Property<Int>

    @get:Input
    abstract val ndkVersion: Property<SemanticVersion>

    @get:Input
    abstract val ndkDirectory: Property<File>

    @get:Input
    abstract val rustProfile: Property<String>

    @get:Input
    abstract val rustProjectDirectory: Property<File>

    @get:Input
    abstract val cargoTargetDirectory: Property<File>

    @get:Input
    abstract val variantJniLibsDirectory: Property<File>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection
    
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val cargoToml: RegularFileProperty
    
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val rustBinaries = rustBinaries.get()
        val abi = abi.get()
        val apiLevel = apiLevel.get()
        val ndkDirectory = ndkDirectory.get()
        val rustProfile = rustProfile.get()
        val rustProjectDirectory = rustProjectDirectory.get()
        val variantJniLibsDirectory = variantJniLibsDirectory.get()

        require(rustProjectDirectory.exists()) {
            "Rust project directory not found: $rustProjectDirectory"
        }
        
        val cargoTomlFile = File(rustProjectDirectory, "Cargo.toml")
        require(cargoTomlFile.exists()) {
            "Cargo.toml not found in: $rustProjectDirectory"
        }
        
        require(ndkDirectory.exists()) {
            """
            Android NDK not found at: $ndkDirectory
            Please install NDK via Android Studio SDK Manager or set android.ndkDirectory
            """.trimIndent()
        }

        log("Building ${cargoTomlFile.parentFile.name} for ${abi.androidName} (API $apiLevel)")

        try {
            execOperations.exec {
                standardOutput = System.out
                errorOutput = System.out
                workingDir = rustProjectDirectory

                commandLine(rustBinaries.cargoNdk)
                
                args("-o", variantJniLibsDirectory.absolutePath)
                args("--platform", apiLevel)
                args("-t", abi.androidName)
                args("build")
                
                if (rustProfile.isNotEmpty() && rustProfile != "dev") {
                    args("--profile", rustProfile)
                }
            }.assertNormalExitValue()
        } catch (e: Exception) {
            throw GradleException(
                """
                Rust build failed for ${abi.androidName}
                
                Possible solutions:
                - Check that your Cargo.toml has [lib] crate-type = ["cdylib"]
                - Ensure NDK version ${ndkVersion.get()} is properly installed
                - Ensure cargo-ndk is installed: cargo install cargo-ndk
                - Try running: cargo ndk -t ${abi.androidName} build
                
                Error: ${e.message}
                """.trimIndent(),
                e
            )
        }
    }
}