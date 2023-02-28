package dev.matrix.agp.rust.utils

import org.gradle.internal.os.OperatingSystem

internal enum class Os {
    Linux,
    MacOs,
    Windows,
    Unknown;

    companion object {
        val current: Os

        init {
            val os = OperatingSystem.current()
            current = when {
                os.isLinux -> Linux
                os.isMacOsX -> MacOs
                os.isWindows -> Windows
                else -> Unknown
            }
        }
    }

    val isLinux: Boolean
        get() {
            return this == Linux
        }

    val isMacOs: Boolean
        get() {
            return this == MacOs
        }

    val isWindows: Boolean
        get() {
            return this == Windows
        }
}
