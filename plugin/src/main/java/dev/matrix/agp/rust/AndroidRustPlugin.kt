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
@Suppress("unused", "UnstableApiUsage")
class AndroidRustPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("androidRust", AndroidRustExtension::class.java)
        val androidExtension = project.getAndroidExtension()
        val androidComponents = project.getAndroidComponentsExtension()
        val tasksByBuildType = HashMap<String, ArrayList<TaskProvider<RustBuildTask>>>()

        androidComponents.finalizeDsl { dsl ->
            val allRustAbiSet = mutableSetOf<Abi>()
            val ndkDirectory = androidExtension.ndkDirectory
            val ndkVersion = SemanticVersion(androidExtension.ndkVersion)
            val extensionBuildDirectory = File(project.buildDir, "intermediates/rust")

            for (buildType in dsl.buildTypes) {
                val buildTypeNameCap = buildType.name.capitalize(Locale.getDefault())

                val variantBuildDirectory = File(extensionBuildDirectory, buildType.name)
                val variantJniLibsDirectory = File(variantBuildDirectory, "jniLibs")

                val cleanTaskName = "clean${buildTypeNameCap}RustJniLibs"
                val cleanTask = project.tasks.register(cleanTaskName, RustCleanTask::class.java) {
                    this.variantJniLibsDirectory.set(variantJniLibsDirectory)
                }

                for ((moduleName, module) in extension.modules) {
                    val moduleNameCap = moduleName.capitalize(Locale.getDefault())
                    val moduleBuildDirectory = File(variantBuildDirectory, "lib_$moduleName")

                    val rustBuildType = module.buildTypes[buildType.name]
                    val rustConfiguration = mergeRustConfigurations(rustBuildType, module, extension)

                    val testTask = when (rustConfiguration.runTests) {
                        true -> {
                            val testTaskName = "test${moduleNameCap}Rust"
                            project.tasks.register(testTaskName, RustTestTask::class.java) {
                                this.rustProjectDirectory.set(module.path)
                                this.cargoTargetDirectory.set(moduleBuildDirectory)
                            }.dependsOn(cleanTask)
                        }
                        else -> null
                    }

                    val rustAbiSet = resolveAbiList(project, rustConfiguration.targets)
                    allRustAbiSet.addAll(rustAbiSet)

                    for (rustAbi in rustAbiSet) {
                        val buildTaskName = "build${buildTypeNameCap}${moduleNameCap}Rust[${rustAbi.androidName}]"
                        val buildTask = project.tasks.register(buildTaskName, RustBuildTask::class.java) {
                            this.abi.set(rustAbi)
                            this.apiLevel.set(dsl.defaultConfig.minSdk ?: 21)
                            this.ndkVersion.set(ndkVersion)
                            this.ndkDirectory.set(ndkDirectory)
                            this.rustProfile.set(rustConfiguration.profile)
                            this.rustProjectDirectory.set(module.path)
                            this.cargoTargetDirectory.set(moduleBuildDirectory)
                            this.variantJniLibsDirectory.set(variantJniLibsDirectory)
                        }
                        buildTask.dependsOn(testTask ?: cleanTask)
                        tasksByBuildType.getOrPut(buildType.name, ::ArrayList).add(buildTask)
                    }
                }

                dsl.sourceSets.findByName(buildType.name)?.jniLibs?.srcDir(variantJniLibsDirectory)
            }

            val minimumSupportedRustVersion = SemanticVersion(extension.minimumSupportedRustVersion)
            installRustComponentsIfNeeded(project, minimumSupportedRustVersion, allRustAbiSet)
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

    private fun resolveAbiList(project: Project, requested: Collection<String>): Collection<Abi> {
        val requestedAbi = Abi.fromRustNames(requested)

        val injectedAbi = Abi.fromInjectedBuildAbi(project)
        if (injectedAbi.isEmpty()) {
            return requestedAbi
        }

        val intersectionAbi = requestedAbi.intersect(injectedAbi)
        check(intersectionAbi.isNotEmpty()) {
            "ABIs requested by IDE ($injectedAbi) are not supported by the build config ($requested)"
        }

        return when {
            intersectionAbi.contains(Abi.Arm64) -> listOf(Abi.Arm64)
            intersectionAbi.contains(Abi.X86_64) -> listOf(Abi.X86_64)
            else -> listOf(intersectionAbi.first())
        }
    }

    private fun mergeRustConfigurations(vararg configurations: AndroidRustConfiguration?): AndroidRustConfiguration {
        val defaultConfiguration = AndroidRustConfiguration().also {
            it.profile = "release"
            it.targets = Abi.values().mapTo(ArrayList(), Abi::rustName)
            it.runTests = null
        }

        return configurations.asSequence()
            .filterNotNull()
            .plus(defaultConfiguration)
            .reduce { result, base ->
                if (result.profile.isEmpty()) {
                    result.profile = base.profile
                }
                if (result.targets.isEmpty()) {
                    result.targets = base.targets
                }
                if (result.runTests == null) {
                    result.runTests = base.runTests
                }
                result
            }
    }
}
