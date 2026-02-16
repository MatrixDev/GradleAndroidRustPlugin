package dev.matrix.agp.rust.utils

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import org.gradle.api.Project

internal fun Project.findAndroidComponentsExtension(): AndroidComponentsExtension<out CommonExtension, out VariantBuilder, out Variant>? =
    when (val it = project.extensions.getByName("androidComponents")) {
        is LibraryAndroidComponentsExtension -> it
        is ApplicationAndroidComponentsExtension -> it
        else -> null
    }

internal fun Project.getAndroidComponentsExtension() = checkNotNull(findAndroidComponentsExtension()) {
    "couldn't find android components extension"
}
