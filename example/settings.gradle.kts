rootProject.name = "android-rust-example"

include(":app")

includeBuild("..") {
    dependencySubstitution {
        substitute(module("io.github.MatrixDev.android-rust:plugin")).using(project(":plugin"))
    }
}
