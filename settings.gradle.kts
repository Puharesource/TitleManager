pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "TitleManager"

include("modules:core")
include("modules:bukkit:api")
include("modules:bukkit:defaults")
include("modules:bukkit:runtime-contracts")
include("modules:bukkit:runtime-bukkit-api")
include("modules:nms:common")
include("modules:nms:legacy-common")
include("modules:nms:legacy:v1-8-r1")
include("modules:nms:legacy:v1-8-r2")
include("modules:nms:legacy:v1-8-r3")
include("modules:nms:legacy:v1-9-r1")
include("modules:nms:legacy:v1-9-r2")
include("modules:nms:legacy:v1-10-r1")
include("modules:nms:legacy:v1-11-r1")
include("modules:nms:legacy:v1-12-r1")
include("modules:nms:legacy:v1-13-r1")
include("modules:nms:legacy:v1-13-r2")
include("modules:nms:legacy:v1-14-r1")
include("modules:nms:legacy:v1-15-r1")
include("modules:nms:legacy:v1-16-r1")
include("modules:nms:direct:v1-20-r2")
include("modules:nms:direct:v1-20-r3")
include("apps:bukkit-plugin")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("okio", "3.17.0")
            version("kotlinx-coroutines", "1.11.0")
            version("kotlinx-datetime", "0.8.0")
            version("commons-imaging", "1.0.0-alpha6")
            version("junit", "6.1.0")
            version("mockk", "1.14.11")
            version("mockbukkit", "4.110.0")
            version("koin", "4.2.1")
            version("sqlite-jdbc", "3.53.1.0")
            version("slf4j", "2.0.18")
            version("vault-api", "1.7")
            version("placeholderapi", "2.12.2")
            version("bstats", "3.2.1")

            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("kotlinx-coroutines")
            library("kotlinx-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").versionRef("kotlinx-datetime")
            library("kotlinx-coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test").versionRef("kotlinx-coroutines")
            library("okio", "com.squareup.okio", "okio").versionRef("okio")
            library("commons-imaging", "org.apache.commons", "commons-imaging").versionRef("commons-imaging")
            library("junit-jupiter-params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit")
            library("mockk", "io.mockk", "mockk").versionRef("mockk")
            library("mockbukkit", "org.mockbukkit.mockbukkit", "mockbukkit-v1.21").versionRef("mockbukkit")
            library("kaml", "com.charleskorn.kaml", "kaml").version("0.104.0")
            library("snakeyaml", "org.snakeyaml", "snakeyaml-engine").version("3.0.1")
            library("i18n4k", "de.comahe.i18n4k", "i18n4k-core-jvm").version("0.11.2")
            library("koin", "io.insert-koin", "koin-core").versionRef("koin")
            library("sqlite-jdbc", "org.xerial", "sqlite-jdbc").versionRef("sqlite-jdbc")
            library("slf4j-nop", "org.slf4j", "slf4j-nop").versionRef("slf4j")
            library("vault-api", "net.milkbowl.vault", "VaultAPI").versionRef("vault-api")
            library("placeholderapi", "me.clip", "placeholderapi").versionRef("placeholderapi")
            library("bstats-bukkit", "org.bstats", "bstats-bukkit").versionRef("bstats")

            bundle("kotlinx", listOf("kotlinx-coroutines-core", "kotlinx-datetime"))
            bundle("test", listOf("junit-jupiter-params", "mockk", "kotlinx-coroutines-test"))
        }
    }
}
