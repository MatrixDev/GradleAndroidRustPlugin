package dev.matrix.agp.rust.utils

import org.gradle.api.Project

enum class Abi(
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
        rustName = "aarch64",
        androidName = "arm64-v8a",
        compilerTriple = "aarch64-linux-android",
        binUtilsTriple = "aarch64-linux-android",
        rustTargetTriple = "aarch64-linux-android",
    );

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {
        fun fromRustName(value: String) = values().find { it.rustName == value }
        fun fromAndroidName(value: String) = values().find { it.androidName == value }

        fun fromInjectedBuildAbi(project: Project): Set<Abi> {
            val values = project.properties["android.injected.build.abi"] ?: return emptySet()
            return values.toString().split(",").mapNotNullTo(HashSet()) { fromAndroidName(it.trim()) }
        }
    }

    fun cc(apiLevel: Int) = when (Os.isWindows) {
        true -> "${compilerTriple}${apiLevel}-clang.cmd"
        else -> "${compilerTriple}${apiLevel}-clang"
    }

    fun ccx(apiLevel: Int) = when (Os.isWindows) {
        true -> "${compilerTriple}${apiLevel}-clang++.cmd"
        else -> "${compilerTriple}${apiLevel}-clang++"
    }

    fun ar(ndkVersionMajor: Int) = when (ndkVersionMajor >= 23) {
        true -> "llvm-ar"
        else -> "${binUtilsTriple}-ar"
    }
}
