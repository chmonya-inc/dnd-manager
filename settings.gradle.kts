pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://plugins.gradle.org/m2/")
        maven("https://jogamp.org/deployment/maven")

        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist") {
                    name = "Node Distributions"
                    patternLayout {
                        artifact("v[revision]/[artifact]-v[revision]-[classifier].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("org.nodejs") }
        }

        exclusiveContent {
            forRepository {
                ivy("https://github.com/yarnpkg/yarn/releases/download") {
                    name = "Yarn Distributions"
                    patternLayout {
                        artifact("v[revision]/[artifact]-v[revision].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.yarnpkg") }
        }

        exclusiveContent {
            forRepository {
                ivy("https://github.com/WebAssembly/binaryen/releases/download") {
                    name = "Binaryen Distributions"
                    patternLayout {
                        artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.github.webassembly") }
        }
    }
}

rootProject.name = "DndHelper"
include(":app")
include(":models")
include(":shared:core")
include(":shared:player")
include(":shared:desktop")
include(":desktop")
include(":web")
include(":server")
