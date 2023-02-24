package dev.matrix.agp.rust.utils

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project

fun Project.findAndroidPlugin() = plugins.asSequence()
    .mapNotNull { it as? AppPlugin ?: it as? LibraryPlugin }
    .firstOrNull()

fun Project.getAndroidPlugin() = checkNotNull(findAndroidPlugin()) {
    "couldn't find android AppPlugin or LibraryPlugin"
}

fun Project.findAndroidExtension() = extensions.findByType(AppExtension::class.java)
    ?: extensions.findByType(LibraryExtension::class.java)

fun Project.getAndroidExtension() = checkNotNull(findAndroidExtension()) {
    "couldn't find android AppExtension or LibraryExtension"
}

fun Project.findAndroidComponentsExtension() = when (val it = project.extensions.getByName("androidComponents")) {
    is LibraryAndroidComponentsExtension -> it
    is ApplicationAndroidComponentsExtension -> it
    else -> null
}

fun Project.getAndroidComponentsExtension() = checkNotNull(findAndroidComponentsExtension()) {
    "couldn't find android components extension"
}
