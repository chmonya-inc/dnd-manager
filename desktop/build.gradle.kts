plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.hot.reload)
}

composeCompiler {
    enableIntrinsicRemember = true
}

group = "com.dnd.helper"
version = "1.0"

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(compose.runtime)

    implementation(libs.ui.tooling.preview)
}

compose.desktop {
    application {
        mainClass = "com.dnd.helper.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageName = "D&D Helper"
            packageVersion = "1.0.0"
        }
    }
}
