buildscript {
    val kotlinVersion by extra { "1.7.0" }

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("io.github.MatrixDev.android-rust:plugin:*")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
    rootProject.childProjects.values.forEach { project ->
        delete(project.buildDir)
    }
}
