package dev.matrix.agp.rust.utils

import java.io.Serializable

internal data class SemanticVersion(
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

internal fun SemanticVersion(version: String?): SemanticVersion {
    if (version.isNullOrEmpty()) {
        return SemanticVersion(0, 0, 0)
    }

    val parts = version.split(".")
    val major = requireNotNull(parts.getOrNull(0)?.toIntOrNull()) {
        "failed to parse 'major' part of the version from '$version'"
    }
    val minor = requireNotNull(parts.getOrNull(1)?.toIntOrNull()) {
        "failed to parse 'minor' part of the version from '$version'"
    }
    val patch = requireNotNull(parts.getOrNull(2)?.toIntOrNull()) {
        "failed to parse 'patch' part of the version from '$version'"
    }

    return SemanticVersion(
        major = major,
        minor = minor,
        patch = patch,
    )
}
