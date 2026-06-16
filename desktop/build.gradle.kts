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
            packageVersion = versionNumber

            val isQa = project.hasProperty("qa")
            packageName = "D&D Helper"

            windows {
                menu = true
                upgradeUuid = if (isQa) "77c9c3e4-8393-4a11-9231-6b834468f7f2" else "67c9c3e4-8393-4a11-9231-6b834468f7f1"
                iconFile.set(project.file("src/main/resources/icon.png"))
            }

            buildTypes.release {
                packageName = if (isQa) "D&D Helper QA" else "D&D Helper"
                proguard {
                    version.set("7.5.0")
                    isEnabled.set(true)
                    optimize.set(true)
                    configurationFiles.from(project.file("proguard-rules.pro"))
                }
                windows {
                    menu = true
                    iconFile.set(project.file("src/main/resources/icon.png"))
                    upgradeUuid = if (isQa) "77c9c3e4-8393-4a11-9231-6b834468f7f2" else "67c9c3e4-8393-4a11-9231-6b834468f7f1"
                }
            }

            modules("java.net.http")
        }
    }
}

// Универсальный таск для переименования MSI
tasks.matching { it.name.startsWith("package") && it.name.endsWith("Msi") }.configureEach {
    val localBuildNumber = buildNumber
    val isQa = project.hasProperty("qa")
    val taskName = name
    val buildDirProvider = layout.buildDirectory

    doLast {
        val isRelease = taskName.contains("Release")
        val typeDir = if (isRelease) "main-release" else "main"
        val msiDir = buildDirProvider.dir("compose/binaries/$typeDir/msi").get().asFile
        
        val msiFile = msiDir.listFiles()?.firstOrNull { it.extension == "msi" }
        if (msiFile != null) {
            val suffix = if (isQa) "-QA" else ""
            val newName = "${msiFile.nameWithoutExtension}${suffix}-$localBuildNumber.msi"
            val newFile = File(msiDir, newName)
            msiFile.renameTo(newFile)
            println("MSI (${if (isRelease) "Release" else "Dev"}${if (isQa) " QA" else ""}) успешно переименован в: $newName")
        }
    }
}
