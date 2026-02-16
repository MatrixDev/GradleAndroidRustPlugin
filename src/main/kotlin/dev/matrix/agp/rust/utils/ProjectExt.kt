package dev.matrix.agp.rust.utils

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project

internal fun Project.findAndroidExtension(): CommonExtension? =
    extensions.findByType(ApplicationExtension::class.java)
        ?: extensions.findByType(LibraryExtension::class.java)

internal fun Project.getAndroidExtension(): CommonExtension = checkNotNull(findAndroidExtension()) {
    "couldn't find android ApplicationExtension or LibraryExtension"
}

internal fun Project.findAndroidComponentsExtension() = when (val it = project.extensions.getByName("androidComponents")) {
    is LibraryAndroidComponentsExtension -> it
    is ApplicationAndroidComponentsExtension -> it
    else -> null
}

internal fun Project.getAndroidComponentsExtension() = checkNotNull(findAndroidComponentsExtension()) {
    "couldn't find android components extension"
}
