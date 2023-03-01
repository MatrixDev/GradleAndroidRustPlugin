package dev.matrix.agp.rust.utils

import org.gradle.api.Project

internal enum class Abi(
    val rustName: String,
    val androidName: String,
    val compilerTriple: String,
    val binUtilsTriple: String,
    val rustTargetTriple: String,
) {
    X86(
        rustName = "x86",
        androidName = "x86",
        compilerTriple = "i686-linux-android",
        binUtilsTriple = "i686-linux-android",
        rustTargetTriple = "i686-linux-android",
    ),
    X86_64(
        rustName = "x86_64",
        androidName = "x86_64",
        compilerTriple = "x86_64-linux-android",
        binUtilsTriple = "x86_64-linux-android",
        rustTargetTriple = "x86_64-linux-android",
    ),
    Arm(
        rustName = "arm",
        androidName = "armeabi-v7a",
        compilerTriple = "armv7a-linux-androideabi",
        binUtilsTriple = "arm-linux-androideabi",
        rustTargetTriple = "armv7-linux-androideabi",
    ),
    Arm64(
        rustName = "arm64",
        androidName = "arm64-v8a",
        compilerTriple = "aarch64-linux-android",
        binUtilsTriple = "aarch64-linux-android",
        rustTargetTriple = "aarch64-linux-android",
    );

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {
        fun fromRustName(value: String) = values().find { it.rustName.equals(value, ignoreCase = true) }
        fun fromAndroidName(value: String) = values().find { it.androidName.equals(value, ignoreCase = true) }

        fun fromInjectedBuildAbi(project: Project): Set<Abi> {
            val values = project.properties["android.injected.build.abi"] ?: return emptySet()
            return values.toString().split(",")
                .asSequence()
                .mapNotNull { fromAndroidName(it.trim()) }
                .toSet()
        }

        fun fromRustNames(names: Collection<String>): Set<Abi> {
            return names.asSequence()
                .map { requireNotNull(fromRustName(it)) { "unsupported abi version string: $it" } }
                .toSet()
        }
    }

    fun cc(apiLevel: Int) = when (Os.current.isWindows) {
        true -> "${compilerTriple}${apiLevel}-clang.cmd"
        else -> "${compilerTriple}${apiLevel}-clang"
    }

    fun ccx(apiLevel: Int) = when (Os.current.isWindows) {
        true -> "${compilerTriple}${apiLevel}-clang++.cmd"
        else -> "${compilerTriple}${apiLevel}-clang++"
    }

    fun ar(ndkVersionMajor: Int) = when (ndkVersionMajor >= 23) {
        true -> "llvm-ar"
        else -> "${binUtilsTriple}-ar"
    }
}
