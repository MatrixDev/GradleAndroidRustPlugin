package dev.matrix.agp.rust.utils

import java.io.Serializable

data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
) : Serializable, Comparable<SemanticVersion> {
    val isValid: Boolean
        get() = major != 0 || minor != 0 || patch != 0

    override fun toString() = "$major.$minor.$patch"

    override fun compareTo(other: SemanticVersion): Int {
        var result = major.compareTo(other.major)
        if (result == 0) {
            result = minor.compareTo(other.minor)
        }
        if (result == 0) {
            result = patch.compareTo(other.patch)
        }
        return result
    }
}

fun SemanticVersion(version: String?): SemanticVersion {
    val parts = version.orEmpty().split(".")
    return SemanticVersion(
        major = parts.getOrNull(0)?.toIntOrNull() ?: 0,
        minor = parts.getOrNull(1)?.toIntOrNull() ?: 0,
        patch = parts.getOrNull(2)?.toIntOrNull() ?: 0,
    )
}

fun SemanticVersion?.orEmpty(): SemanticVersion {
    return this ?: SemanticVersion(0, 0, 0)
}
