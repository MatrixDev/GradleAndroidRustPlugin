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
        classpath("dev.matrix.android-rust:0.3.1")
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
    module("rust-library") {
        path = file("src/rust_library")
    }
}
```

# Additional configurations

This is the list of some additional flags that can be configured:

```kotlin
androidRust {
    // MSRV, plugin will update rust if installed version is lower than requested
    minimumSupportedRustVersion = "1.62.1"
    
    module("rust-library") {
        // path to your rust library
        path = file("src/rust_library")

        // default rust profile
        profile = "release"

        // default abi targets
        targets = listOf("arm", "arm64")

        // "debug" build type specific configuration
        buildType("debug") {
            // use "dev" profile in rust
            profile = "dev"
        }

        // "release" build type specific configuration
        buildType("release") {
            // run rust tests before build
            runTests = true

            // build all supported abi versions
            targets = listOf("arm", "arm64", "x86", "x86_64")
        }
    }

    // more than one library can be added 
    module("additional-library") {
        // ...
    }
}
```

# Development support

Plugin will check for a magic property `android.injected.build.abi` set by Android Studio when
running application on device. This will limit ABI targets to only required by the device and
should speedup development quite a bit.

In theory this should behave the same as a built-in support for the NdkBuild / CMake.

# Goals
- Building multiple rust libraries with ease
- Allow builds to be configurable for common scenarios

# Non-goals
- Supporting all Gradle versions
- Allow builds to be configurable for exotic scenarios
