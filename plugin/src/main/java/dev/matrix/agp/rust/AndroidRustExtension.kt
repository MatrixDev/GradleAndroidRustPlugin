package dev.matrix.agp.rust

import dev.matrix.agp.rust.utils.Abi
import java.io.File

@DslMarker
annotation class AndroidRustDslMarker

@AndroidRustDslMarker
@Suppress("unused")
open class AndroidRustExtension {
    lateinit var path: File

    var profile = ""
    var targets = Abi.values().asSequence().map { it.rustName }.toSet()
    var minimumSupportedRustVersion = ""
    var buildTypes = hashMapOf(
        "debug" to AndroidRustBuildType().also {
            it.profile = "dev"
        },
        "release" to AndroidRustBuildType().also {
            it.profile = "release"
        },
    )

    fun buildType(name: String, configure: AndroidRustBuildType.() -> Unit) {
        configure(buildTypes.getOrPut(name, ::AndroidRustBuildType))
    }
}

@AndroidRustDslMarker
@Suppress("unused")
open class AndroidRustBuildType {
    var profile = ""
    var targets = emptySet<String>()
}
