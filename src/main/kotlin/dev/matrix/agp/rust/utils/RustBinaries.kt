package dev.matrix.agp.rust.utils

import org.gradle.api.Project
import java.io.File
import java.io.Serializable
import java.util.Properties

@Suppress("SpellCheckingInspection")
internal data class RustBinaries(
    val cargo: String = "cargo",
    val cargoNdk: String = "cargo-ndk",
    val rustc: String = "rustc",
    val rustup: String = "rustup",
) : Serializable {
    companion object {
        operator fun invoke(project: Project): RustBinaries {
            var path = RustBinaries()
            try {
                val file = project.rootProject.file("local.properties")
                val properties = Properties().also {
                    it.load(file.inputStream())
                }

                val bin = File(properties.getProperty("cargo.bin").orEmpty())
                if (bin.exists()) {
                    path = path.copy(
                        cargo = File(bin, path.cargo).absolutePath,
                        cargoNdk = File(bin, path.cargoNdk).absolutePath,
                        rustc = File(bin, path.rustc).absolutePath,
                        rustup = File(bin, path.rustup).absolutePath,
                    )
                }
            } catch (ignore: Exception) {
            }
            return path
        }
    }
}