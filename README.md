# AndroidRust Gradle Plugin

This plugin helps with building Rust JNI libraries with Cargo for use in Android projects.

# Usage

Add dependencies to the root `build.gradle.kts` file

```kotlin
buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("dev.matrix.android-rust:0.1.0")
    }
}
```

Add plugin to the module's `build.gradle.kts` file

```kotlin
plugins {
    id("dev.matrix.android-rust")
}
```

Add `androidRust` configuration

```kotlin
androidRust {
    path = file("src/rust_library") // path to your rust library
}
```

# Additional configurations

This is the list of some additional flags that can be configured:

```kotlin
androidRust {
    path = file("src/rust_library")

    // default rust profile
    profile = "release"

    // default abi targets
    targets = setOf("arm", "arm64")

    // MSRV, plugin will update rust if installed version is lower than requested
    minimumSupportedRustVersion = "1.62.1"

    // "debug" build type specific configuration
    buildType("debug") {
        // use "dev" profile in rust
        profile = "dev"
    }

    // "release" build type specific configuration
    buildType("release") {
        // build all supported abi versions
        targets = setOf("arm", "arm64", "x86", "x86_64")
    }
}
```

