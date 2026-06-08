@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

composeCompiler {
    enableIntrinsicRemember = true
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer?.apply {
                    port = 8080 // You can change this if needed
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":shared:core"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)

                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.coil.compose)
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.navigation.compose)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        val desktopMain by getting {
            dependencies {}
        }

        val wasmJsMain by getting {
            dependencies {}
        }
    }
}

android {
    namespace = "com.dnd.helper.shared.player"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}
