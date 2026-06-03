import io.papermc.hangarpublishplugin.model.Platforms
import org.gradle.api.file.DuplicatesStrategy
import java.util.zip.ZipFile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.gradleup.shadow") version "9.4.2"
    id("de.comahe.i18n4k") version "0.11.2"
    id("io.papermc.hangar-publish-plugin") version "0.1.3"
}

group = "dev.tarkan.titlemanager"
version = providers.gradleProperty("titleManagerJvmVersion").get()
val minecraftVersion = providers.gradleProperty("titleManagerPaperApiVersion").get()
val supportedMinecraftVersions = providers.gradleProperty("titleManagerSupportedMinecraftVersions")
val paperweightNmsModules = listOf(
    "v1_20_R2" to project(":modules:nms:direct:v1-20-r2"),
    "v1_20_R3" to project(":modules:nms:direct:v1-20-r3")
)
val legacyNmsModules = listOf(
    "v1_8_R1" to project(":modules:nms:legacy:v1-8-r1"),
    "v1_8_R2" to project(":modules:nms:legacy:v1-8-r2"),
    "v1_8_R3" to project(":modules:nms:legacy:v1-8-r3"),
    "v1_9_R1" to project(":modules:nms:legacy:v1-9-r1"),
    "v1_9_R2" to project(":modules:nms:legacy:v1-9-r2"),
    "v1_10_R1" to project(":modules:nms:legacy:v1-10-r1"),
    "v1_11_R1" to project(":modules:nms:legacy:v1-11-r1"),
    "v1_12_R1" to project(":modules:nms:legacy:v1-12-r1"),
    "v1_13_R1" to project(":modules:nms:legacy:v1-13-r1"),
    "v1_13_R2" to project(":modules:nms:legacy:v1-13-r2"),
    "v1_14_R1" to project(":modules:nms:legacy:v1-14-r1"),
    "v1_15_R1" to project(":modules:nms:legacy:v1-15-r1"),
    "v1_16_R1" to project(":modules:nms:legacy:v1-16-r1")
)
val exactNmsModules = legacyNmsModules + paperweightNmsModules
fun String.toFactoryClassName(): String = split('_').joinToString("_") { segment ->
    if (segment.firstOrNull()?.isDigit() == true) segment else segment.replaceFirstChar(Char::uppercaseChar)
}
fun String.toLegacyFactoryClassName(): String = "Legacy" + toFactoryClassName()


i18n4k {
    sourceCodeLocales = listOf("en", "da")
    packageName = "dev.tarkan.titlemanager.bukkit.localization"
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    val fatJar by named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        val exactNmsReobfJars = paperweightNmsModules.map { (_, nmsProject) ->
            nmsProject.layout.buildDirectory.file("libs/${nmsProject.name}-${nmsProject.version}.jar")
        }

        dependsOn(paperweightNmsModules.map { (_, nmsProject) -> "${nmsProject.path}:reobfJar" })

        dependencies {
            include(dependency("org.jetbrains.kotlin:.*"))
            include(dependency("org.jetbrains.kotlinx:.*"))
            include(dependency("com.google.dagger:.*"))
            include(dependency("javax.inject:.*"))
            include(dependency("org.bstats:.*"))
            include(dependency("net.kyori:.*"))
            include(dependency("com.charleskorn.kaml:.*"))
            include(dependency("org.snakeyaml:.*"))
            include(dependency("it.krzeminski:.*"))
            include(dependency("net.thauvin.erik.urlencoder:.*"))
            include(dependency("com.squareup.okio:.*"))
            include(dependency("io.insert-koin:.*"))
            include(dependency("de.comahe.i18n4k:.*"))
            include(dependency("org.apache.commons:.*"))
            include(dependency("org.xerial:.*"))
            include(dependency("org.slf4j:.*"))
            include(dependency("dev.tarkan.titlemanager:.*"))
        }

        exactNmsReobfJars.forEach { reobfJar ->
            from(reobfJar.map { zipTree(it).matching { exclude("META-INF/services/dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModuleFactory") } })
        }

        mergeServiceFiles()
        filesMatching("META-INF/services/**") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        relocate("kotlin", "dev.tarkan.titlemanager.shaded.kotlin")
        relocate("_COROUTINE", "dev.tarkan.titlemanager.shaded._COROUTINE")
        relocate("org.jetbrains", "dev.tarkan.titlemanager.shaded.org.jetbrains")
        relocate("dagger", "dev.tarkan.titlemanager.shaded.dagger")
        relocate("javax.inject", "dev.tarkan.titlemanager.shaded.javax.inject")
        relocate("org.bstats", "dev.tarkan.titlemanager.shaded.org.bstats")
        relocate("com.charleskorn.kaml", "dev.tarkan.titlemanager.shaded.com.charleskorn.kaml")
        relocate("org.snakeyaml", "dev.tarkan.titlemanager.shaded.org.snakeyaml")
        relocate("it.krzeminski", "dev.tarkan.titlemanager.shaded.it.krzeminski")
        relocate("net.thauvin.erik.urlencoder", "dev.tarkan.titlemanager.shaded.net.thauvin.erik.urlencoder")
        relocate("okio", "dev.tarkan.titlemanager.shaded.okio")
        relocate("org.koin", "dev.tarkan.titlemanager.shaded.org.koin")
        relocate("de.comahe.i18n4k", "dev.tarkan.titlemanager.shaded.de.comahe.i18n4k")
        relocate("org.apache.commons", "dev.tarkan.titlemanager.shaded.org.apache.commons")
        relocate("org.sqlite", "dev.tarkan.titlemanager.shaded.org.sqlite")
        relocate("org.slf4j", "dev.tarkan.titlemanager.shaded.org.slf4j")
        relocate("net.kyori", "dev.tarkan.titlemanager.shaded.net.kyori")
        doLast {
            val archive = archiveFile.get().asFile
            val archiveEntries = zipTree(archive)
            val containsCoreClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/animation/Animation.class") }
                .files
                .isNotEmpty()
            val containsApiClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/bukkit/api/TitleManagerApi.class") }
                .files
                .isNotEmpty()
            val containsRuntimeClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/bukkit/diagnostics/RuntimeVersionModule.class") }
                .files
                .isNotEmpty()
            val containsBukkitRuntimeClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/bukkit/runtime/adapter/bukkitapi/BukkitApiRuntimeAdapter.class") }
                .files
                .isNotEmpty()
            val containsLegacySpigotRuntimeClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/bukkit/runtime/adapter/bukkitapi/LegacySpigotRuntimeAdapter.class") }
                .files
                .isNotEmpty()
            val containsLegacySpigotTitleRuntimeClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/bukkit/runtime/adapter/bukkitapi/LegacySpigotTitleOnlyRuntimeAdapter.class") }
                .files
                .isNotEmpty()
            val containsAdventureClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/shaded/net/kyori/adventure/text/Component.class") }
                .files
                .isNotEmpty()
            val containsSnakeYamlEngineKmpClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/shaded/it/krzeminski/snakeyaml/engine/kmp/exceptions/MarkedYamlEngineException.class") }
                .files
                .isNotEmpty()
            val containsUrlEncoderClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/shaded/net/thauvin/erik/urlencoder/UrlEncoderUtil.class") }
                .files
                .isNotEmpty()
            val classEntries = ZipFile(archive).use { zipFile ->
                zipFile.entries().asSequence()
                    .map { it.name }
                    .filter { it.endsWith(".class") }
                    .toList()
            }
            val unexpectedClassEntries = classEntries
                .filterNot { entry ->
                    entry.startsWith("dev/tarkan/titlemanager/") ||
                        (entry.startsWith("META-INF/versions/") && "/dev/tarkan/titlemanager/" in entry)
                }
                .sorted()
            val unrelocatedShadedLibraries = classEntries
                .filter { entry ->
                    listOf(
                        "_COROUTINE/",
                        "com/charleskorn/kaml/",
                        "dagger/",
                        "de/comahe/i18n4k/",
                        "it/krzeminski/",
                        "javax/inject/",
                        "kotlin/",
                        "kotlinx/",
                        "okio/",
                        "org/apache/commons/",
                        "org/bstats/",
                        "org/jetbrains/",
                        "org/koin/",
                        "org/slf4j/",
                        "org/snakeyaml/",
                        "org/sqlite/",
                        "net/kyori/"
                    ).any(entry::startsWith)
                }
                .sorted()

            val containsPaperweightNmsModuleClasses = paperweightNmsModules.associate { (nmsVersion, _) ->
                nmsVersion to zipTree(archive)
                    .matching { include("dev/tarkan/titlemanager/nms/direct/$nmsVersion/${nmsVersion.toFactoryClassName()}RuntimeVersionModuleFactory.class") }
                    .files
                    .isNotEmpty()
            }
            val containsPaperweightDirectNmsRuntimeClasses = paperweightNmsModules.associate { (nmsVersion, _) ->
                nmsVersion to zipTree(archive)
                    .matching { include("dev/tarkan/titlemanager/nms/direct/$nmsVersion/${nmsVersion.toFactoryClassName()}RuntimeVersionModule.class") }
                    .files
                    .isNotEmpty()
            }
            val containsLegacyNmsModuleClasses = legacyNmsModules.associate { (nmsVersion, _) ->
                nmsVersion to zipTree(archive)
                    .matching { include("dev/tarkan/titlemanager/nms/legacy/${nmsVersion}/${nmsVersion.toLegacyFactoryClassName()}RuntimeVersionModulePacketSink.class") }
                    .files
                    .isNotEmpty()
            }
            val containsNmsCommonClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/nms/common/DelegatingNmsRuntimeVersionModuleFactory.class") }
                .files
                .isNotEmpty()
            val containsLegacyNmsRuntimeClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/nms/legacy/LegacyDirectNmsRuntimeVersionModule.class") }
                .files
                .isNotEmpty()
            val containsLegacyNmsFactoryClass = zipTree(archive)
                .matching { include("dev/tarkan/titlemanager/nms/legacy/LegacyV1_8_R3RuntimeVersionModuleFactory.class") }
                .files
                .isNotEmpty()
            val runtimeServiceFile = zipTree(archive)
                .matching { include("META-INF/services/dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModuleFactory") }
                .files
                .singleOrNull()
                ?.readText()
                .orEmpty()
            val containsPaperweightRuntimeServiceProviders = paperweightNmsModules.associate { (nmsVersion, _) ->
                nmsVersion to runtimeServiceFile.contains("dev.tarkan.titlemanager.nms.direct.$nmsVersion.${nmsVersion.toFactoryClassName()}RuntimeVersionModuleFactory")
            }
            val containsLegacyRuntimeServiceProviders = legacyNmsModules.associate { (nmsVersion, _) ->
                nmsVersion to runtimeServiceFile.contains("dev.tarkan.titlemanager.nms.legacy.${nmsVersion.toLegacyFactoryClassName()}RuntimeVersionModuleFactory")
            }
            val containsBundledMinecraftClasses = archiveEntries
                .matching { include("net/minecraft/**") }
                .files
                .isNotEmpty()
            val containsBundledMojangClasses = archiveEntries
                .matching { include("com/mojang/**") }
                .files
                .isNotEmpty()
            val containsBundledCraftBukkitClasses = archiveEntries
                .matching { include("org/bukkit/craftbukkit/**") }
                .files
                .isNotEmpty()
            val containsBundledPaperAdventureClasses = archiveEntries
                .matching { include("io/papermc/paper/adventure/**") }
                .files
                .isNotEmpty()

            check(containsCoreClass) {
                "Shadow jar does not contain titlemanager-core classes"
            }
            check(containsApiClass) {
                "Shadow jar does not contain titlemanager-bukkit-api classes"
            }
            check(containsRuntimeClass) {
                "Shadow jar does not contain titlemanager-bukkit-runtime classes"
            }
            check(containsBukkitRuntimeClass) {
                "Shadow jar does not contain titlemanager-bukkit-runtime-bukkit-api classes"
            }
            check(containsLegacySpigotRuntimeClass) {
                "Shadow jar does not contain legacy Spigot runtime fallback classes"
            }
            check(unexpectedClassEntries.isEmpty()) {
                "Shadow jar contains classes outside dev.tarkan.titlemanager packages: ${unexpectedClassEntries.take(20)}"
            }
            check(containsLegacySpigotTitleRuntimeClass) {
                "Shadow jar does not contain title-only legacy Spigot runtime fallback classes"
            }
            check(containsAdventureClass) {
                "Shadow jar does not contain relocated Adventure API classes required by runtime contracts on legacy servers"
            }
            check(containsSnakeYamlEngineKmpClass) {
                "Shadow jar does not contain relocated SnakeYAML Engine KMP classes required by kaml"
            }
            check(containsUrlEncoderClass) {
                "Shadow jar does not contain relocated URL encoder classes required by SnakeYAML Engine KMP"
            }
            check(unrelocatedShadedLibraries.isEmpty()) {
                "Shadow jar contains unrelocated shaded library classes: ${unrelocatedShadedLibraries.take(20)}"
            }
            paperweightNmsModules.forEach { (nmsVersion, nmsProject) ->
                check(containsPaperweightNmsModuleClasses[nmsVersion] == true) {
                    "Shadow jar does not contain ${nmsProject.name} classes"
                }
                check(containsPaperweightDirectNmsRuntimeClasses[nmsVersion] == true) {
                    "Shadow jar does not contain the direct $nmsVersion runtime implementation"
                }
                check(containsPaperweightRuntimeServiceProviders[nmsVersion] == true) {
                    "Shadow jar does not contain RuntimeVersionModuleFactory service provider metadata for $nmsVersion"
                }
            }
            legacyNmsModules.forEach { (nmsVersion, nmsProject) ->
                check(containsLegacyNmsModuleClasses[nmsVersion] == true) {
                    "Shadow jar does not contain ${nmsProject.name} packet sink classes"
                }
                check(containsLegacyRuntimeServiceProviders[nmsVersion] == true) {
                    "Shadow jar does not contain legacy RuntimeVersionModuleFactory service provider metadata for $nmsVersion"
                }
            }
            check(containsNmsCommonClass) {
                "Shadow jar does not contain titlemanager-nms-common classes"
            }
            check(containsLegacyNmsRuntimeClass) {
                "Shadow jar does not contain legacy direct NMS runtime classes"
            }
            check(containsLegacyNmsFactoryClass) {
                "Shadow jar does not contain legacy direct NMS factory classes"
            }
            check(runtimeServiceFile.isNotBlank()) {
                "Shadow jar does not contain RuntimeVersionModuleFactory service provider metadata"
            }
            check(!containsBundledMinecraftClasses) {
                "Shadow jar must not bundle Minecraft server classes; direct NMS modules should link to server-provided classes"
            }
            check(!containsBundledMojangClasses) {
                "Shadow jar must not bundle Mojang server classes; direct NMS modules should link to server-provided classes"
            }
            check(!containsBundledCraftBukkitClasses) {
                "Shadow jar must not bundle CraftBukkit server classes; direct NMS modules should link to server-provided classes"
            }
            check(!containsBundledPaperAdventureClasses) {
                "Shadow jar must not bundle PaperAdventure server classes; direct NMS modules should link to server-provided classes"
            }
        }
    }

    named("assemble") {
        dependsOn("shadowJar")
    }
}


hangarPublish {
    publications.register("plugin") {
        version.set(System.getenv("HANGAR_VERSION") ?: project.version.toString())
        channel.set(System.getenv("HANGAR_CHANNEL") ?: "Snapshot")
        id.set(System.getenv("HANGAR_PROJECT_ID") ?: "TitleManager")
        apiKey.set(System.getenv("HANGAR_API_TOKEN") ?: "")
        changelog.set(System.getenv("HANGAR_CHANGELOG") ?: "")
        val hangarDownloadUrl = System.getenv("HANGAR_DOWNLOAD_URL")?.takeIf { it.isNotBlank() }

        platforms {
            register(Platforms.PAPER) {
                if (hangarDownloadUrl == null) {
                    jar.set(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").flatMap { it.archiveFile })
                } else {
                    url.set(hangarDownloadUrl)
                }
                platformVersions.set(supportedMinecraftVersions.get().split(',').map { it.trim() })

                dependencies {
                    url("PlaceholderAPI", "https://www.spigotmc.org/resources/placeholderapi.6245/") {
                        required.set(false)
                    }
                    url("Vault", "https://www.spigotmc.org/resources/vault.34315/") {
                        required.set(false)
                    }
                    url("CombatLogX", "https://www.spigotmc.org/resources/combatlogx.31689/") {
                        required.set(false)
                    }
                }
            }
        }
    }
}
repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        name = "Vault"
        url = uri("https://nexus.hc.to/content/repositories/pub_releases")
    }

    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation("io.papermc.paper:paper-api:$minecraftVersion")
    implementation(project(":modules:core"))
    implementation(project(":modules:bukkit:api"))
    implementation(project(":modules:bukkit:defaults"))
    implementation(project(":modules:bukkit:runtime-contracts"))
    implementation(project(":modules:bukkit:runtime-bukkit-api"))
    implementation(project(":modules:nms:common"))
    implementation(project(":modules:nms:legacy-common"))
    exactNmsModules.forEach { (_, nmsProject) ->
        implementation(nmsProject)
    }
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kaml)
    implementation(libs.snakeyaml)
    implementation(libs.i18n4k)
    implementation(libs.commons.imaging)
    implementation(libs.koin)
    implementation(libs.sqlite.jdbc)
    implementation(libs.slf4j.nop)
    implementation(libs.bstats.bukkit)
    compileOnly(libs.vault.api)
    compileOnly(libs.placeholderapi)

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.bundles.test)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.vault.api)
    testImplementation(libs.placeholderapi)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}