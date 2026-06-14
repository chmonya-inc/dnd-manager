plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

composeCompiler {
    enableIntrinsicRemember = true
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

group = "com.dnd.helper"
val buildNumber = rootProject.extra["appBuildNumber"] as String
val versionNumber = rootProject.extra["appVersionNumber"] as String

dependencies {
    implementation(project(":shared:desktop"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
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
            packageVersion = versionNumber

            windows {
                // Fixed UpgradeCode is mandatory for MSI updates to work correctly
                upgradeUuid = "67c9c3e4-8393-4a11-9231-6b834468f7f1"
                menu = true
                // iconFile.set(project.file("icon.ico")) // Раскомментируйте, если есть иконка
            }

            modules("java.net.http")
        }
    }
}

val msiDirProvider = layout.buildDirectory.dir("compose/binaries/main/msi")

tasks.matching { it.name == "packageDistributionForCurrentOS" }.configureEach {
    val localBuildNumber = buildNumber
    val localMsiDirProvider = msiDirProvider

    doLast {
        // 2. Внутри doLast используем только наши чистые локальные переменные
        val msiDir = localMsiDirProvider.get().asFile
        val msiFile = msiDir.listFiles()?.firstOrNull { it.extension == "msi" }

        if (msiFile != null) {
            val newName = "${msiFile.nameWithoutExtension}-$localBuildNumber.msi"
            val newFile = File(msiDir, newName)
            msiFile.renameTo(newFile)
            println("MSI успешно переименован в: $newName")
        } else {
            println("Файл MSI не найден для переименования")
        }
    }
}