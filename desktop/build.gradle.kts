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

afterEvaluate {
    tasks.named("packageDistributionForCurrentOS") {
        doLast {
            // Указываем путь, куда Compose складывает бинарники по умолчанию
            val msiDir = layout.buildDirectory.dir("compose/binaries/main/msi").get().asFile

            // Ищем сгенерированный .msi файл в этой папке
            val msiFile = msiDir.listFiles()?.firstOrNull { it.extension == "msi" }

            if (msiFile != null) {
                // Формируем новое имя: оригинальноеИмя-buildNumber.msi
                // Если msiFile.nameWithoutExtension уже "D&D Helper-1.1.1",
                // то на выходе получится красивое "D&D Helper-1.1.1-твойномерабилды.msi"
                val newName = "${msiFile.nameWithoutExtension}-$buildNumber.msi"
                val newFile = File(msiDir, newName)

                msiFile.renameTo(newFile)
                println("MSI успешно переименован в: $newName")
            } else {
                println("Файл MSI не найден для переименования")
            }
        }
    }
}