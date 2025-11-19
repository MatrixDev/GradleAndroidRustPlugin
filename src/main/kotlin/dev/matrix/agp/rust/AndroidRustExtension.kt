package dev.matrix.agp.rust

import java.io.File

@DslMarker
annotation class AndroidRustDslMarker

@AndroidRustDslMarker
@Suppress("unused")
open class AndroidRustExtension : AndroidRustConfiguration() {
    /**
     * Specify minimum supported Rust version.
     *
     * Plugin will automatically use `rustup update` command to
     * update rust version in case installed versions is not high enough.
     */
    var minimumSupportedRustVersion = ""

    /**
     * Configuration map of all Rust libraries to build.
     *
     * Keys of this map are Rust crates names.
     */
    var modules = mutableMapOf<String, AndroidRustModule>()

    /**
     * Configure Rust module/library to build.
     *
     * @param name Rust crate name.
     */
    fun module(name: String, configure: AndroidRustModule.() -> Unit) {
        modules.getOrPut(name, ::AndroidRustModule).configure()
    }
}

@AndroidRustDslMarker
@Suppress("unused")
class AndroidRustModule : AndroidRustConfiguration() {
    /**
     * Path to the Rust project folder.
     *
     * This is the folder containing `Cargo.toml` file.
     */
    lateinit var path: File

    /**
     * All supported build type configurations.
     *
     * Keys of this map should correspond to the current project build variants.
     */
    var buildTypes = hashMapOf(
        "debug" to AndroidRustBuildType().also {
            it.profile = "dev"
        },
        "release" to AndroidRustBuildType().also {
            it.profile = "release"
        },
    )

    /**
     * Configure Rust build options.
     *
     * @param name current project build variant.
     */
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
    /**
     * Rust profile (dev, release, etc.).
     *
     * See: https://doc.rust-lang.org/cargo/reference/profiles.html
     */
    var profile = ""

    /**
     * List of ABIs to build.
     */
    var targets = listOf<String>()

    /**
     * Run tests after the build.
     *
     * This will run `cargo test` command and check its result.
     */
    var runTests: Boolean? = null

    /**
     * Disable IDE ABI injection optimization.
     * - When `true`, all requested ABIs will be built regardless of IDE deployment target.
     * - When `false` (default), only the IDE target ABI will be built to speed up development builds.
     *
     * Set to `true` if you experience "library not found" errors when running from Android Studio.
     * See: https://github.com/MatrixDev/GradleAndroidRustPlugin/issues/3
     */
    var disableAbiOptimization: Boolean? = null
}
