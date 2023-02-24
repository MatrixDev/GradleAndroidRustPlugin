rootProject.name = "android-rust-example"

include(":app")

includeBuild("..") {
    dependencySubstitution {
        substitute(module("dev.matrix.android-rust:plugin")).using(project(":plugin"))
    }
}
