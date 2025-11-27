pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        mavenLocal() 
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal() 
        google()
        maven("https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "android-rust-example"
include(":app")

includeBuild("..") {
    dependencySubstitution {
        substitute(module("io.github.rodroidmods:android-rust-plugin")).using(project(":"))
    }
}