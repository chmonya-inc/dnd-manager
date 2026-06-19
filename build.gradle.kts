@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Disable automatic repository injection for Kotlin/JS and Kotlin/Wasm tools
// because we have declared them manually in settings.gradle.kts to satisfy
// RepositoriesMode.FAIL_ON_PROJECT_REPOS.

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        downloadBaseUrl = null
    }
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension> {
        downloadBaseUrl = null
    }
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenRootPlugin> {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenRootExtension> {
        @Suppress("DEPRECATION")
        version = "130"
    }
}

fun getVersion(ver: String): String {
    return ver.split("/").last().replace("v", "")
}

extra["appBuildNumber"] = providers.environmentVariable("APP_BUILD_NUMBER").getOrElse("1")
extra["appVersionNumber"] = getVersion(providers.environmentVariable("APP_VERSION_NUMBER").getOrElse("1.0.0"))
