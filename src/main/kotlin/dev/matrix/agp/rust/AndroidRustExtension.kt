package dev.matrix.agp.rust

import java.io.File

@DslMarker
annotation class AndroidRustDslMarker

@AndroidRustDslMarker
@Suppress("unused")
open class AndroidRustExtension : AndroidRustConfiguration() {
    var minimumSupportedRustVersion = ""
    var modules = mutableMapOf<String, AndroidRustModule>()

    /**
     * Disable IDE ABI injection optimization.
     * When true, all requested ABIs will be built regardless of IDE deployment target.
     * When false (default), only the IDE target ABI will be built to speed up development builds.
     *
     * Set to true if you experience "library not found" errors when running from Android Studio.
     * See: https://github.com/MatrixDev/GradleAndroidRustPlugin/issues/3
     */
    var disableAbiOptimization = false

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
