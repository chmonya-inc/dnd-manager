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
                // Use source-map instead of eval-based devtool for CSP compliance.
                // Kotlin/Wasm runtime requires 'unsafe-eval' CSP; eval-based source maps
                // trigger additional CSP violations in some browsers.
                devtool = "source-map"
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
                implementation(compose.materialIconsExtended)
                implementation(project(":shared"))
            }
        }
    }
}
