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

// Определяем контекст сборки по имени задачи
val isReleaseTask = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

compose.desktop {
    application {
        mainClass = "com.dnd.helper.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageVersion = versionNumber

            // Если запущена Release задача - используем продакшн имя, иначе QA
            packageName = if (isReleaseTask) "D&D Helper" else "D&D Helper QA"

            windows {
                menu = true
                iconFile.set(project.file("src/main/resources/icon.png"))
                // Разные ID, чтобы QA и Prod не конфликтовали при установке
                upgradeUuid = if (isReleaseTask) "67c9c3e4-8393-4a11-9231-6b834468f7f1" else "77c9c3e4-8393-4a11-9231-6b834468f7f2"
            }

            buildTypes.release {
                proguard {
                    version.set("7.5.0")
                    isEnabled.set(true)
                    optimize.set(false)
                    configurationFiles.from(project.file("proguard-rules.pro"))
                }
            }

            modules("java.net.http")
        }
    }
}

// Универсальный таск для переименования MSI
tasks.matching { it.name.startsWith("package") && it.name.endsWith("Msi") }.configureEach {
    val localBuildNumber = buildNumber
    val taskName = name
    val buildDirProvider = layout.buildDirectory

    doLast {
        val isRelease = taskName.contains("Release")
        val typeDir = if (isRelease) "main-release" else "main"
        val msiDir = buildDirProvider.dir("compose/binaries/$typeDir/msi").get().asFile
        
        val msiFile = msiDir.listFiles()?.firstOrNull { it.extension == "msi" }
        if (msiFile != null) {
            val newName = "${msiFile.nameWithoutExtension}-$localBuildNumber.msi"
            val newFile = File(msiDir, newName)
            msiFile.renameTo(newFile)
            println("MSI (${if (isRelease) "Release" else "QA"}) успешно переименован в: $newName")
        }
    }
}
