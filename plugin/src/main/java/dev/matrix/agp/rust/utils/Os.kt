package dev.matrix.agp.rust.utils

object Os {
    val isWindows: Boolean
        get() {
            return System.getProperty("os.name").orEmpty().startsWith("windows", ignoreCase = true)
        }
}
