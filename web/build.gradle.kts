@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    wasmJs {
        moduleName = "web"
        browser {
            commonWebpackConfig {
                outputFileName = "web.js"
                // Sourcemaps are heavy on memory during generation.
                // Disable them on CI to prevent OOM.
                devtool = if (System.getenv("CI") != null || System.getenv("DOCKER") != null) null else "source-map"
                devServer?.apply {
                    port = 8081
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(project(":shared:player"))
                implementation(libs.koin.core)
            }
        }
    }
}
