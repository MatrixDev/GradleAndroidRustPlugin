package dev.matrix.agp.rust

import dev.matrix.agp.rust.utils.Abi
import dev.matrix.agp.rust.utils.SemanticVersion
import java.io.File

@Suppress("MemberVisibilityCanBePrivate", "unused")
open class AndroidRustExtension {
    lateinit var path: File

    var profile = ""
    var targets = Abi.values().toSet()
    var minimumSupportedRustVersion: SemanticVersion? = null

    val buildTypes = hashMapOf(
        "debug" to AndroidRustBuildType().also {
            it.profile = "dev"
        },
        "release" to AndroidRustBuildType().also {
            it.profile = "release"
        },
    )

    fun buildType(name: String, configure: (AndroidRustBuildType) -> Unit) {
        configure(buildTypes.getOrPut(name, ::AndroidRustBuildType))
    }

    fun debugBuildType(configure: (AndroidRustBuildType) -> Unit) {
        buildType("debug", configure)
    }

    fun releaseBuildType(configure: (AndroidRustBuildType) -> Unit) {
        buildType("release", configure)
    }
}

@Suppress("unused")
open class AndroidRustBuildType {
    var profile = ""
    var targets: List<Abi>? = null
}
