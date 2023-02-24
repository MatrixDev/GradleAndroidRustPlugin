plugins {
    `kotlin-dsl`
    id("kotlin")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "dev.matrix.android-rust"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
}

pluginBundle {
    website = "https://github.com/MatrixDev/GradleAndroidRustPlugin"
    vcsUrl = "https://github.com/MatrixDev/GradleAndroidRustPlugin.git"
    tags = listOf("android", "rust")
}

gradlePlugin {
    plugins {
        create("androidRustPlugin") {
            id = "dev.matrix.android-rust"
            implementationClass = "dev.matrix.agp.rust.AndroidRustPlugin"
            displayName = "Plugin for building Rust with Cargo in Android projects"
            description = "A plugin that helps build Rust JNI libraries with Cargo for use in Android projects."
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    gradleApi()

    implementation("com.android.tools.build:gradle:7.1.3")
    implementation("com.android.tools.build:gradle-api:7.1.3")
}