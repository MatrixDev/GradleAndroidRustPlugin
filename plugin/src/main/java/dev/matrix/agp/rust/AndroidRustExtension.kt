package dev.matrix.agp.rust

import java.io.File

@DslMarker
annotation class AndroidRustDslMarker

@AndroidRustDslMarker
@Suppress("unused")
open class AndroidRustExtension : AndroidRustConfiguration() {
    var minimumSupportedRustVersion = ""
    var modules = mutableMapOf<String, AndroidRustModule>()

    fun module(name: String, configure: AndroidRustModule.() -> Unit) {
        modules.getOrPut(name, ::AndroidRustModule).configure()
    }
}

@AndroidRustDslMarker
@Suppress("unused")
class AndroidRustModule : AndroidRustConfiguration() {
    lateinit var path: File

    var buildTypes = hashMapOf(
        "debug" to AndroidRustBuildType().also {
            it.profile = "dev"
        },
        "release" to AndroidRustBuildType().also {
            it.profile = "release"
        },
    )

    fun buildType(name: String, configure: AndroidRustBuildType.() -> Unit) {
        buildTypes.getOrPut(name, ::AndroidRustBuildType).configure()
    }
}

@AndroidRustDslMarker
@Suppress("unused")
class AndroidRustBuildType : AndroidRustConfiguration()

@AndroidRustDslMarker
@Suppress("unused")
open class AndroidRustConfiguration {
    var profile = ""
    var targets = listOf<String>()
    var runTests: Boolean? = null
}
