package dev.matrix.agp.rust

import dev.matrix.agp.rust.utils.Abi
import dev.matrix.agp.rust.utils.Os
import dev.matrix.agp.rust.utils.SemanticVersion
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

internal abstract class RustBuildTask : DefaultTask() {
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

    @TaskAction
    fun taskAction() {
        val abi = abi.get()
        val apiLevel = apiLevel.get()
        val ndkVersion = ndkVersion.get()
        val ndkDirectory = ndkDirectory.get()
        val rustProfile = rustProfile.get()
        val rustProjectDirectory = rustProjectDirectory.get()
        val cargoTargetDirectory = cargoTargetDirectory.get()
        val variantJniLibsDirectory = variantJniLibsDirectory.get()

        val platform = when (Os.current) {
            Os.Linux -> "linux-x86_64"
            Os.MacOs -> "darwin-x86_64"
            Os.Windows -> "windows-x86_64"
            Os.Unknown -> throw Exception("OS is not supported")
        }

        val toolchainFolder = File(ndkDirectory, "toolchains/llvm/prebuilt/$platform/bin")
        val cc = File(toolchainFolder, abi.cc(apiLevel))
        val cxx = File(toolchainFolder, abi.ccx(apiLevel))
        val ar = File(toolchainFolder, abi.ar(ndkVersion.major))

        val cargoTargetTriplet = abi.rustTargetTriple
            .replace('-', '_')
            .toUpperCase(Locale.getDefault())

        project.exec {
            standardOutput = System.out
            errorOutput = System.out
            workingDir = rustProjectDirectory

            environment("CC_${abi.rustTargetTriple}", cc)
            environment("CXX_${abi.rustTargetTriple}", cxx)
            environment("AR_${abi.rustTargetTriple}", ar)
            environment("CARGO_TARGET_DIR", cargoTargetDirectory.absolutePath)
            environment("CARGO_TARGET_${cargoTargetTriplet}_LINKER", cc)

            commandLine("cargo")

            args("build")
            args("--lib")
            args("--target", abi.rustTargetTriple)

            if (rustProfile.isNotEmpty()) {
                args("--profile", rustProfile)
            }
        }.assertNormalExitValue()

        project.copy {
            val dir = when (rustProfile == "dev") {
                true -> "debug"
                else -> rustProfile
            }
            include("*.so")
            from(File(cargoTargetDirectory, "${abi.rustTargetTriple}/${dir}/"))
            into(File(variantJniLibsDirectory, abi.androidName))
        }
    }
}
