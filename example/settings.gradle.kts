includeBuild("..") {
    dependencySubstitution {
        substitute(module("dev.matrix.android-rust:plugin")).using(project(":plugin"))
    }
}

include(":app")
