package dev.matrix.agp.rust

import com.android.build.gradle.internal.tasks.factory.dependsOn
import dev.matrix.agp.rust.utils.Abi
import dev.matrix.agp.rust.utils.SemanticVersion
import dev.matrix.agp.rust.utils.getAndroidComponentsExtension
import dev.matrix.agp.rust.utils.getAndroidExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.*

//
// TODO: migrate to variant API with artifacts when JNI will be supported
// https://developer.android.com/studio/build/extend-agp#access-modify-artifacts
//
class AndroidRustPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("androidRust", AndroidRustExtension::class.java)
        val androidExtension = project.getAndroidExtension()
        val androidComponents = project.getAndroidComponentsExtension()
        val tasksByBuildType = HashMap<String, ArrayList<TaskProvider<RustBuildTask>>>()

        androidComponents.finalizeDsl { dsl ->
            val allRustAbiSet = HashSet<Abi>()
            val ndkDirectory = androidExtension.ndkDirectory
            val ndkVersion = SemanticVersion(androidExtension.ndkVersion)
            val extensionBuildDirectory = File(project.buildDir, "intermediates/rust")

            for (buildType in dsl.buildTypes) {
                val buildTypeName = buildType.name.capitalize(Locale.getDefault())

                val variantBuildDirectory = File(extensionBuildDirectory, buildType.name)
                val variantJniLibsDirectory = File(variantBuildDirectory, "jniLibs")

                val rustBuildType = extension.buildTypes[buildType.name]
                val rustAbiSet = resolveAbiList(project, rustBuildType?.targets ?: extension.targets)
                allRustAbiSet.addAll(rustAbiSet)

                val cleanTaskName = "cleanRust${buildTypeName}"
                val cleanTask = project.tasks.register(cleanTaskName, RustCleanTask::class.java) {
                    this.variantJniLibsDirectory.set(variantJniLibsDirectory)
                }

                for (rustAbi in rustAbiSet) {
                    val buildTaskName = "buildRust${buildTypeName}[${rustAbi.androidName}]"
                    val buildTask = project.tasks.register(buildTaskName, RustBuildTask::class.java) {
                        this.abi.set(rustAbi)
                        this.apiLevel.set(dsl.defaultConfig.minSdk ?: 21)
                        this.ndkVersion.set(ndkVersion)
                        this.ndkDirectory.set(ndkDirectory)
                        this.rustProfile.set(rustBuildType?.profile.orEmpty().ifEmpty { extension.profile })
                        this.rustProjectDirectory.set(extension.path)
                        this.variantBuildDirectory.set(variantBuildDirectory)
                        this.variantJniLibsDirectory.set(variantJniLibsDirectory)
                    }
                    buildTask.dependsOn(cleanTask)
                    tasksByBuildType.getOrPut(buildType.name, ::ArrayList).add(buildTask)
                }

                val sourceSet = dsl.sourceSets.findByName(buildType.name)
                if (sourceSet != null) {
                    sourceSet.jniLibs.srcDir(variantJniLibsDirectory)
                }
            }

            installRustComponentsIfNeeded(project, extension.minimumSupportedRustVersion, allRustAbiSet)
        }

        androidComponents.onVariants { variant ->
            val tasks = tasksByBuildType[variant.buildType] ?: return@onVariants
            val variantName = variant.name.capitalize(Locale.getDefault())

            project.afterEvaluate {
                val parentTask = project.tasks.getByName("pre${variantName}Build")
                for (task in tasks) {
                    parentTask.dependsOn(task)
                }
            }
        }
    }

    private fun resolveAbiList(project: Project, requested: Collection<Abi>): Collection<Abi> {
        val injectedBuildAbi = Abi.fromInjectedBuildAbi(project)
        if (injectedBuildAbi.isEmpty()) {
            return requested
        }

        val intersection = requested.intersect(injectedBuildAbi)
        check(intersection.isNotEmpty()) {
            "ABIs requested by IDE ($injectedBuildAbi) are not supported by the build config ($requested)"
        }

        return when {
            intersection.contains(Abi.Arm64) -> listOf(Abi.Arm64)
            intersection.contains(Abi.X86_64) -> listOf(Abi.X86_64)
            else -> listOf(intersection.first())
        }
    }
}
