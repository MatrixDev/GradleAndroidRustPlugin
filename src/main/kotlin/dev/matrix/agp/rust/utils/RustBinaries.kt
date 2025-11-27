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
            
            val customBinPath = try {
                val file = project.rootProject.file("local.properties")
                val properties = Properties().also {
                    it.load(file.inputStream())
                }
                properties.getProperty("cargo.bin")?.let { File(it) }
            } catch (ignore: Exception) {
                null
            }

            val binDirectory = customBinPath ?: detectCargoHome()

            if (binDirectory?.exists() == true) {
                path = path.copy(
                    cargo = File(binDirectory, path.cargo).absolutePath,
                    cargoNdk = File(binDirectory, path.cargoNdk).absolutePath,
                    rustc = File(binDirectory, path.rustc).absolutePath,
                    rustup = File(binDirectory, path.rustup).absolutePath,
                )
            }
            
            return path
        }

        private fun detectCargoHome(): File? {
            val cargoHome = System.getenv("CARGO_HOME")
            if (cargoHome != null) {
                return File(cargoHome, "bin")
            }

            val home = System.getProperty("user.home") ?: return null
            val defaultCargoHome = File(home, ".cargo/bin")
            
            if (defaultCargoHome.exists()) {
                return defaultCargoHome
            }

            return null
        }
    }
}